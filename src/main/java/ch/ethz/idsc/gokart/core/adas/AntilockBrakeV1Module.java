// code by jph and am
package ch.ethz.idsc.gokart.core.adas;

import java.util.Optional;

import ch.ethz.idsc.gokart.calib.steer.RimoTireConfiguration;
import ch.ethz.idsc.gokart.dev.linmot.LinmotPutEvent;
import ch.ethz.idsc.gokart.dev.linmot.LinmotPutOperation;
import ch.ethz.idsc.gokart.dev.linmot.LinmotSocket;
import ch.ethz.idsc.gokart.dev.rimo.RimoSocket;
import ch.ethz.idsc.retina.joystick.ManualControlInterface;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Clips;
import ch.ethz.idsc.tensor.sca.Round;

/** class is used to develop and test anti lock brake logic */
public class AntilockBrakeV1Module extends AntilockBrakeModule {
  public AntilockBrakeV1Module() {
    this(HapticSteerConfig.GLOBAL);
  }

  public AntilockBrakeV1Module(HapticSteerConfig hapticSteerConfig) {
    super(hapticSteerConfig);
  }

  @Override // from AbstractModule
  protected void first() {
    LinmotSocket.INSTANCE.addPutProvider(this);
    RimoSocket.INSTANCE.addGetListener(rimoGetListener);
  }

  @Override // from AbstractModule
  protected void last() {
    RimoSocket.INSTANCE.removeGetListener(rimoGetListener);
    LinmotSocket.INSTANCE.removePutProvider(this);
  }

  @Override
  public Optional<LinmotPutEvent> putEvent() {
    Optional<ManualControlInterface> optional = manualControlProvider.getManualControl();
    if (optional.isPresent()) {
      ManualControlInterface manualControlInterface = optional.get();
      if (manualControlInterface.isAutonomousPressed() && lidarLocalizationModule != null) {
        return smartBraking(rimoGetEvent.getAngularRate_Y_pair(), lidarLocalizationModule.getVelocity());
      }
      // reset to full Braking value for next braking maneuvre
      brakePosition = hapticSteerConfig.fullBraking;
    }
    return Optional.empty();
  }

  /** @param angularRate_Y_pair
   * @param velocityOrigin
   * @return braking command with suitable relative position */
  Optional<LinmotPutEvent> smartBraking(Tensor angularRate_Y_pair, Tensor velocityOrigin) {
    Scalar angularRate_Origin = velocityOrigin.Get(0).divide(RimoTireConfiguration._REAR.radius());
    Tensor angularRate_Origin_pair = Tensors.of(angularRate_Origin, angularRate_Origin);
    Tensor slip = angularRate_Origin_pair.subtract(angularRate_Y_pair); // vector of length 2 with entries of unit [s^-1]
    System.out.println(slip.map(Round._3) + ", " + brakePosition + ", " + velocityOrigin.Get(0).map(Round._3));
    // the brake cannot be constantly applied otherwise the brake motor heats up too much
    // there is a desired range for slip (in theory 0.1-0.25)
    // if the slip is outside this range, the position of the brake is increased/decreased
    // if (hapticSteerConfig.slipClip().isOutside(slip.Get(0)))
    for (int i = 0; i < 2; i++) {
      if (Scalars.lessThan(slip.Get(i), hapticSteerConfig.minSlip)) {
        brakePosition = Clips.unit().apply(brakePosition.add(HapticSteerConfig.GLOBAL.incrBraking));
      }
      if (Scalars.lessThan(hapticSteerConfig.maxSlip, slip.Get(i))) {
        brakePosition = Clips.unit().apply(brakePosition.subtract(HapticSteerConfig.GLOBAL.incrBraking));
      }
    }
    LinmotPutEvent relativePosition = LinmotPutOperation.INSTANCE.toRelativePosition(brakePosition);
    return Optional.of(relativePosition);
  }
}

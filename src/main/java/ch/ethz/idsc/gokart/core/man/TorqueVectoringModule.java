// code by mh
package ch.ethz.idsc.gokart.core.man;

import java.util.Objects;
import java.util.Optional;

import ch.ethz.idsc.gokart.calib.steer.SteerMapping;
import ch.ethz.idsc.gokart.core.fuse.Vlp16PassiveSlowing;
import ch.ethz.idsc.gokart.core.pos.GokartPoseEvent;
import ch.ethz.idsc.gokart.core.slam.LidarLocalizationModule;
import ch.ethz.idsc.gokart.core.slam.LocalizationConfig;
import ch.ethz.idsc.gokart.core.tvec.TorqueVectoringInterface;
import ch.ethz.idsc.gokart.dev.linmot.LinmotPutProvider;
import ch.ethz.idsc.gokart.dev.linmot.LinmotSocket;
import ch.ethz.idsc.gokart.dev.rimo.RimoGetEvent;
import ch.ethz.idsc.gokart.dev.rimo.RimoGetListener;
import ch.ethz.idsc.gokart.dev.rimo.RimoPutEvent;
import ch.ethz.idsc.gokart.dev.rimo.RimoPutHelper;
import ch.ethz.idsc.gokart.dev.rimo.RimoSocket;
import ch.ethz.idsc.gokart.dev.steer.SteerColumnInterface;
import ch.ethz.idsc.gokart.dev.steer.SteerConfig;
import ch.ethz.idsc.owl.car.slip.AngularSlip;
import ch.ethz.idsc.retina.joystick.ManualControlInterface;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.retina.util.sys.ModuleAuto;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.alg.Differences;

/** abstract base class for all torque vectoring modules:
 * 
 * {@link DirectTorqueVectoringModule}
 * {@link ImprovedTorqueVectoringModule}
 * {@link NormalizedTorqueVectoringModule}
 * {@link PredictiveTorqueVectoringModule} */
/* package */ abstract class TorqueVectoringModule extends GuideManualModule<RimoPutEvent> implements RimoGetListener {
  private final SteerMapping steerMapping = SteerConfig.GLOBAL.getSteerMapping();
  private final TorqueVectoringInterface torqueVectoringInterface;
  private final Vlp16PassiveSlowing vlp16PassiveSlowing = //
      ModuleAuto.INSTANCE.getInstance(Vlp16PassiveSlowing.class);
  private final LidarLocalizationModule lidarLocalizationModule = //
      ModuleAuto.INSTANCE.getInstance(LidarLocalizationModule.class);
  private final LinmotPutProvider linmotPutProvider = new LinmotManualOverride();

  TorqueVectoringModule(TorqueVectoringInterface torqueVectoringInterface) {
    this.torqueVectoringInterface = torqueVectoringInterface;
  }

  @Override // from ManualModule
  protected final void first() {
    RimoSocket.INSTANCE.addPutProvider(this);
    RimoSocket.INSTANCE.addGetListener(this);
    LinmotSocket.INSTANCE.addPutProvider(linmotPutProvider);
  }

  @Override // from ManualModule
  protected final void last() {
    LinmotSocket.INSTANCE.removePutProvider(linmotPutProvider);
    RimoSocket.INSTANCE.removePutProvider(this);
    RimoSocket.INSTANCE.removeGetListener(this);
  }

  /***************************************************/
  @Override // from GuideJoystickModule
  final Optional<RimoPutEvent> control( //
      SteerColumnInterface steerColumnInterface, ManualControlInterface manualControlInterface) {
    GokartPoseEvent gokartPoseEvent = lidarLocalizationModule.createPoseEvent();
    return LocalizationConfig.GLOBAL.isQualityOk(gokartPoseEvent) //
        ? Optional.of(derive( //
            steerColumnInterface, //
            Differences.of(manualControlInterface.getAheadPair_Unit()).Get(0), //
            gokartPoseEvent.getVelocity()))
        : Optional.empty();
  }

  /** @param steerColumnInterface
   * @param power unitless in the interval [-1, 1]
   * @param velocity {vx[m*s^-1], vy[m*s^-1], gyroZ[s^-1]}
   * @return */
  final RimoPutEvent derive(SteerColumnInterface steerColumnInterface, Scalar power, Tensor velocity) {
    Scalar ratio = steerMapping.getRatioFromSCE(steerColumnInterface); // steering angle of imaginary front wheel
    // compute (negative) angular slip
    AngularSlip angularSlip = new AngularSlip(velocity, ratio);
    // ---
    Tensor powers = torqueVectoringInterface.powers(angularSlip, power);
    Tensor torquesARMS = powers.multiply(ManualConfig.GLOBAL.torqueLimit); // vector of length 2
    // ---
    short arms_rawL = Magnitude.ARMS.toShort(torquesARMS.Get(0));
    short arms_rawR = Magnitude.ARMS.toShort(torquesARMS.Get(1));
    // System.out.println("arms_rawl: " + arms_rawL + " arms_rawr " + arms_rawR);
    return RimoPutHelper.operationTorque( //
        (short) -arms_rawL, // sign left invert
        (short) +arms_rawR // sign right id
    );
  }

  @Override // from RimoGetListener
  public final void getEvent(RimoGetEvent rimoGetEvent) {
    if (Objects.nonNull(vlp16PassiveSlowing))
      vlp16PassiveSlowing.bypassSafety();
    // use tangent speed from lidarLocalizationModule
    // instead of RimoTwdOdometry.tangentSpeed(getEvent);
  }
}

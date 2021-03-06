// code by jph
package ch.ethz.idsc.gokart.calib.steer;

import java.io.Serializable;

import ch.ethz.idsc.owl.car.core.AxleConfiguration;
import ch.ethz.idsc.owl.car.core.WheelConfiguration;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class RimoFrontAxleConfiguration implements AxleConfiguration, Serializable {
  private final WheelConfiguration[] wheelConfiguration;

  /** @param scalar with unit "SCE" */
  public RimoFrontAxleConfiguration(Scalar scalar) {
    wheelConfiguration = new WheelConfiguration[] { //
        new WheelConfiguration(Tensors.of( //
            RimoAxleConstants.xAxleRtoF, //
            RimoAxleConstants.yTireFront, //
            FrontWheelAngleMapping._LEFT.getAngleFromSCE(scalar)), //
            RimoTireConfiguration.FRONT), //
        new WheelConfiguration(Tensors.of( //
            RimoAxleConstants.xAxleRtoF, //
            RimoAxleConstants.yTireFront.negate(), //
            FrontWheelAngleMapping.RIGHT.getAngleFromSCE(scalar)), //
            RimoTireConfiguration.FRONT) };
  }

  @Override // from AxleConfiguration
  public WheelConfiguration wheel(int index) {
    return wheelConfiguration[index];
  }
}

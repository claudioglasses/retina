// code by jph
package ch.ethz.idsc.gokart.dev.steer;

import ch.ethz.idsc.tensor.Scalar;

public interface SteerColumnInterface {
  /** @return true if steering is operational */
  boolean isSteerColumnCalibrated();

  /** Important: only call function if {@link #isSteerColumnCalibrated()}
   * returns true, otherwise an exception is thrown.
   * 
   * @return quantity centered around zero with unit "SCE"
   * Sign.of(value) == 0 means driving straight
   * Sign.of(value) == +1 means steering left
   * Sign.of(value) == -1 means steering right
   * @throws Exception if {@link #isSteerColumnCalibrated()} returns false */
  Scalar getSteerColumnEncoderCentered();
}

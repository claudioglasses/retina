// code by jph
package ch.ethz.idsc.owl.car.model;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

/** References:
 * Time-Optimal Vehicle Posture Control to Mitigate Unavoidable
 * Collisions Using Conventional Control Inputs */
/* package */ enum FrictionCoefficients {
  ;
  public static final Scalar TIRE_DRY_ROAD = RealScalar.of(0.85); // also 0.8
  public static final Scalar TIRE_WET_ROAD = RealScalar.of(0.5);
}

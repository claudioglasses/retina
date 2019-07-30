// code by jph
package ch.ethz.idsc.gokart.calib.steer;

import ch.ethz.idsc.gokart.core.GetListener;

/** receives rimo get events from left and right wheel */
@FunctionalInterface
public interface SteerColumnListener extends GetListener<SteerColumnEvent> {
  // ---
}

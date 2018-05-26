// code by jph
package ch.ethz.idsc.owl.car.math;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import ch.ethz.idsc.owl.math.map.Se2ForwardAction;
import ch.ethz.idsc.owl.math.map.Se2Utils;
import ch.ethz.idsc.retina.util.math.Se2AxisYProject;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Min;
import ch.ethz.idsc.tensor.sca.Clip;

// TODO make collection of obstacle points optional
// TODO make dependent on actual speed and require sufficient time to stop
public class CircleClearanceTracker implements ClearanceTracker, Serializable {
  private static final Scalar UNIT_SPEED = DoubleScalar.of(1);
  // ---
  private final Clip clip_Y;
  private final Clip clip_X;
  private final Se2ForwardAction se2ForwardAction;
  private final Tensor u;
  private final Collection<Tensor> collection = new LinkedList<>();
  // ---
  private Scalar min;

  /** @param half width along y-axis
   * @param angle steering
   * @param xya reference frame of sensor as 3-vector {px, py, angle}
   * @param clearanceFront */
  public CircleClearanceTracker(Scalar half, Scalar angle, Tensor xya, Clip clip_X) {
    clip_Y = Clip.function(half.negate(), half); // TODO there is a small error as gokart turns
    this.clip_X = clip_X;
    Scalar speed = UNIT_SPEED; // assume unit speed // use actual speed in logic
    u = Tensors.of(speed, RealScalar.ZERO, angle.multiply(speed)).unmodifiable();
    min = clip_X.max();
    se2ForwardAction = new Se2ForwardAction(xya);
  }

  @Override // from ClearanceTracker
  public boolean isObstructed(Tensor local) {
    Tensor point = se2ForwardAction.apply(local);
    Scalar t = Se2AxisYProject.of(u, point);
    return private_probe(point, t);
  }

  /** @param local coordinates of obstacle in sensor reference frame */
  public void feed(Tensor local) {
    Tensor point = se2ForwardAction.apply(local);
    Scalar t = Se2AxisYProject.of(u, point);
    if (private_probe(point, t)) {
      min = Min.of(min, t); // negate t again
      collection.add(point);
    }
  }

  private boolean private_probe(Tensor point, Scalar t) {
    // negate() in the next line helps to move point from front of gokart to y-axis of rear axle
    Se2ForwardAction se2ForwardAction = new Se2ForwardAction(Se2Utils.integrate_g0(u.multiply(t.negate())));
    Tensor v = se2ForwardAction.apply(point);
    return clip_Y.isInside(v.Get(1)) && clip_X.isInside(t);
  }

  /** @return closest of all obstructing points, or empty */
  public Optional<Tensor> violation() {
    if (Scalars.lessThan(min, clip_X.max())) // strictly less than
      return Optional.of(Se2Utils.integrate_g0(u.multiply(min)));
    return Optional.empty();
  }

  /** @return unmodifiable collection with points that were determined to be in path */
  public Collection<Tensor> getPointsInViolation() {
    return Collections.unmodifiableCollection(collection);
  }
}
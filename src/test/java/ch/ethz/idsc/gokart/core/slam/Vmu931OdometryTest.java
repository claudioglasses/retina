// code by jph
package ch.ethz.idsc.gokart.core.slam;

import ch.ethz.idsc.gokart.calib.vmu931.PlanarVmu931Type;
import ch.ethz.idsc.retina.imu.vmu931.Vmu931ImuFrames;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.retina.util.pose.PoseHelper;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Chop;
import junit.framework.TestCase;

public class Vmu931OdometryTest extends TestCase {
  public void testInitial() {
    Vmu931Odometry vmu931Odometry = new Vmu931Odometry(PlanarVmu931Type.FLIPPED.planarVmu931Imu());
    assertEquals(vmu931Odometry.getPose(), Tensors.fromString("{0[m], 0[m], 0}"));
    assertEquals(vmu931Odometry.getVelocity(), Tensors.fromString("{0[m*s^-1], 0[m*s^-1], 0[s^-1]}"));
    vmu931Odometry.resetPose(PoseHelper.attachUnits(Tensors.vector(1, 2, 3)));
    assertEquals(vmu931Odometry.getPose(), Tensors.fromString("{1[m], 2[m], 3}"));
    vmu931Odometry.resetVelocity();
    assertEquals(vmu931Odometry.getVelocity(), Tensors.fromString("{0[m*s^-1], 0[m*s^-1], 0[s^-1]}"));
  }

  public void testIntegrate() {
    Vmu931Odometry vmu931Odometry = new Vmu931Odometry(PlanarVmu931Type.FLIPPED.planarVmu931Imu());
    vmu931Odometry.resetPose(PoseHelper.attachUnits(Tensors.vector(1, 0, 0)));
    vmu931Odometry.integrateImu( //
        Tensors.fromString("{0.3[m*s^-2], 0.1[m*s^-2]}"), Quantity.of(0.3, SI.PER_SECOND), Quantity.of(0.1, SI.SECOND));
    Chop._10.requireClose(vmu931Odometry.getPose(), //
        Tensors.fromString("{1.002984551145215818[m], 0.0010448466318511013[m], 0.03}"));
    Tensor velocity = vmu931Odometry.getVelocity();
    Chop._10.requireClose(velocity, Tensors.fromString("{0.03[m*s^-1], 0.01[m*s^-1], 0.3[s^-1]}"));
  }

  public void testIntegrateVmu931() {
    Vmu931Odometry vmu931Odometry = new Vmu931Odometry(PlanarVmu931Type.NATIVE.planarVmu931Imu());
    vmu931Odometry.resetPose(PoseHelper.attachUnits(Tensors.vector(1, 0, 0)));
    vmu931Odometry.vmu931ImuFrame(Vmu931ImuFrames.create( //
        123_000, //
        Tensors.fromString("{0.3[m*s^-2], 0.1[m*s^-2], 0.0[m*s^-2]}"), //
        Tensors.fromString("{0[s^-1], 0[s^-1], 0.1[s^-1]}")));
    Chop._10.requireClose(vmu931Odometry.getPose(), //
        Tensors.fromString("{1.0000299949955427[m], 1.0014997904120474E-5[m], 0.0010000000116728047}"));
    Chop._10.requireClose(vmu931Odometry.getVelocity(), //
        Tensors.fromString("{0.003000000054202974[m*s^-1], 9.999999571591617E-4[m*s^-1], 0.10000000116728047[s^-1]}"));
    vmu931Odometry.vmu931ImuFrame(Vmu931ImuFrames.create( //
        121_000, // <- time decrement should never happen
        Tensors.fromString("{0.3[m*s^-2], 0.1[m*s^-2], 0.0[m*s^-2]}"), //
        Tensors.fromString("{0[s^-1], 0[s^-1], 0.1[s^-1]}")));
    Chop._10.requireClose(vmu931Odometry.getPose(), //
        Tensors.fromString("{1.0000299949955427[m], 1.0014997904120474E-5[m], 0.0010000000116728047}"));
    Chop._10.requireClose(vmu931Odometry.getVelocity(), //
        Tensors.fromString("{0.003000000054202974[m*s^-1], 9.999999571591617E-4[m*s^-1], 0.10000000116728047[s^-1]}"));
  }

  public void testFail() {
    try {
      new Vmu931Odometry(null);
      fail();
    } catch (Exception exception) {
      // ---
    }
  }
}

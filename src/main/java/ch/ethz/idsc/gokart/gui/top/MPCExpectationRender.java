// code by mh
package ch.ethz.idsc.gokart.gui.top;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.ethz.idsc.gokart.calib.steer.RimoTwdOdometry;
import ch.ethz.idsc.gokart.core.mpc.MPCControlUpdateCapture;
import ch.ethz.idsc.gokart.dev.rimo.RimoGetEvent;
import ch.ethz.idsc.gokart.dev.rimo.RimoGetListener;
import ch.ethz.idsc.owl.data.IntervalClock;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.sophus.flt.ga.GeodesicIIR1;
import ch.ethz.idsc.sophus.lie.rn.RnGeodesic;
import ch.ethz.idsc.sophus.lie.se2.Se2Matrix;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.N;

public class MPCExpectationRender extends MPCControlUpdateCapture implements RenderInterface, RimoGetListener {
  private static final Tensor DIAGONAL = DiagonalMatrix.of(0.2, 0.1, 1);
  private static final Tensor MPC_LINE = Tensors.fromString("{{0, 0}, {0, 3}}").map(N.DOUBLE).unmodifiable();
  private static final Tensor RIMO_LINE = Tensors.fromString("{{0, -3}, {0, 0}}").map(N.DOUBLE).unmodifiable();
  private final Tensor xya;
  private final IntervalClock intervalClock = new IntervalClock();
  /** acceleration filter */
  private final GeodesicIIR1 geodesicIIR1 = new GeodesicIIR1(RnGeodesic.INSTANCE, RealScalar.of(0.1));
  // ---
  private Scalar lastTangentSpeed = Quantity.of(0, SI.VELOCITY);
  private Scalar currentRimoAcc = Quantity.of(-3, SI.ACCELERATION);

  public MPCExpectationRender(Tensor xya) {
    this.xya = xya;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    geometricLayer.pushMatrix(Se2Matrix.of(xya));
    geometricLayer.pushMatrix(DIAGONAL);
    // rimo line
    Tensor rimoAccXY = Tensors.of(Magnitude.ACCELERATION.apply(currentRimoAcc), RealScalar.ZERO);
    geometricLayer.pushMatrix(Se2Matrix.translation(rimoAccXY));
    graphics.setColor(Color.BLUE);
    graphics.draw(geometricLayer.toPath2D(RIMO_LINE));
    geometricLayer.popMatrix();
    // mpc line
    Scalar currentMPCAcc = getFirstWantedAcceleration();
    Tensor mpcAccXY = Tensors.of(Magnitude.ACCELERATION.apply(currentMPCAcc), RealScalar.ZERO);
    geometricLayer.pushMatrix(Se2Matrix.translation(mpcAccXY));
    graphics.setColor(Color.RED);
    graphics.draw(geometricLayer.toPath2D(MPC_LINE));
    geometricLayer.popMatrix();
    //
    geometricLayer.popMatrix();
    geometricLayer.popMatrix();
  }

  @Override // from RimoGetListener
  public void getEvent(RimoGetEvent getEvent) {
    /** odometry-based acceleration estimate is robust
     * but could be replaced by acceleration value from IMU */
    Scalar currentTangentSpeed = RimoTwdOdometry.tangentSpeed(getEvent);
    Scalar acceleration = currentTangentSpeed //
        .subtract(lastTangentSpeed)//
        .divide(Quantity.of(intervalClock.seconds(), SI.SECOND));
    lastTangentSpeed = currentTangentSpeed;
    currentRimoAcc = (Scalar) geodesicIIR1.apply(acceleration);
  }
}

// code by mh
package ch.ethz.idsc.gokart.gui.top;

import java.awt.Color;
import java.awt.Graphics2D;

import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.sophus.lie.se2.Se2Matrix;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;

public class BrakeCalibrationRender implements RenderInterface {
  // TODO JPH make calibrationValue transient variable of SafetyConfig
  public static Scalar calibrationValue = RealScalar.ONE;
  private static final Tensor DIAGONAL = DiagonalMatrix.of(.5, .5, 1);
  // ---
  private final Tensor xya;

  BrakeCalibrationRender(Tensor xya) {
    this.xya = xya;
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    geometricLayer.pushMatrix(Se2Matrix.of(xya));
    geometricLayer.pushMatrix(DIAGONAL);
    // calibration line
    graphics.setColor(Color.BLUE);
    Tensor polygon = Tensors.of(Tensors.vector(0, 0), Tensors.of(calibrationValue, RealScalar.ZERO));
    graphics.draw(geometricLayer.toPath2D(polygon));
    //
    geometricLayer.popMatrix();
    geometricLayer.popMatrix();
  }
}

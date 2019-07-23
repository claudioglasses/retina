// code by jph
package ch.ethz.idsc.gokart.core.track;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.Optional;

import javax.swing.JButton;

import ch.ethz.idsc.owl.bot.util.RegionRenders;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.ren.AxesRender;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.owl.math.region.ImageRegion;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.sophus.app.api.AbstractDemo;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.io.Import;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TrackRefinementDemo extends BSplineTrackDemo {
  private static final Tensor MODEL2PIXEL_INITIAL = Tensors.matrix(new Number[][] { //
      { 30, 0, 0 }, //
      { 0, -30, 800 }, //
      { 0, 0, 1 }, //
  }).unmodifiable();
  // ---
  private final JButton jButtonRefine = new JButton("refine");
  private final ImageRegion imageRegion;
  private final RenderInterface background;
  boolean flagRefine = false;

  public TrackRefinementDemo() throws IOException {
    jButtonRefine.addActionListener(a -> flagRefine = true);
    timerFrame.jToolBar.add(jButtonRefine);
    Tensor obstacleImage = Import.of(HomeDirectory.file("TrackRefinement0.png"));
    imageRegion = new ImageRegion(obstacleImage, Tensors.vector(40, 40), true);
    background = RegionRenders.create(imageRegion);
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    background.render(geometricLayer, graphics);
    AxesRender.INSTANCE.render(geometricLayer, graphics);
    super.render(geometricLayer, graphics);
    if (flagRefine) {
      flagRefine = false;
      Tensor controlPoints = getGeodesicControlPoints();
      Tensor points_xyr = Tensor.of(controlPoints.stream().map(row -> row.append(RealScalar.of(1))));
      points_xyr = points_xyr.map(s -> Quantity.of(s, SI.METER));
      TrackRefinement trackRefinement = new TrackRefinement(imageRegion);
      Optional<Tensor> optional = //
          Optional.ofNullable(trackRefinement.getRefinedTrack(points_xyr, RealScalar.of(8), 10, jToggleClosed.isSelected()));
      if (optional.isPresent()) {
        Tensor tensor_xyr = optional.get();
        tensor_xyr = tensor_xyr.map(Magnitude.METER);
        setControlPointsSe2(tensor_xyr);
      } else
        System.out.println("no can do");
    }
  }

  public static void main(String[] args) {
    try {
      AbstractDemo abstractDemo = new TrackRefinementDemo();
      abstractDemo.timerFrame.geometricComponent.setModel2Pixel(MODEL2PIXEL_INITIAL);
      abstractDemo.timerFrame.jFrame.setBounds(100, 100, 800, 800);
      abstractDemo.timerFrame.jFrame.setVisible(true);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}

// code by jph
package ch.ethz.idsc.demo.jph.video;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.stream.IntStream;

import ch.ethz.idsc.gokart.core.pos.GokartPoseContainer;
import ch.ethz.idsc.gokart.core.pos.GokartPoseHelper;
import ch.ethz.idsc.gokart.dev.steer.SteerConfig;
import ch.ethz.idsc.gokart.gui.GokartStatusEvent;
import ch.ethz.idsc.gokart.gui.top.AxisAlignedBox;
import ch.ethz.idsc.gokart.gui.top.ChassisGeometry;
import ch.ethz.idsc.gokart.gui.top.ExtrudedFootprintRender;
import ch.ethz.idsc.owl.car.core.VehicleModel;
import ch.ethz.idsc.owl.car.shop.RimoSinusIonModel;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.owl.math.map.Se2Bijection;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.sophus.app.api.PathRender;
import ch.ethz.idsc.sophus.group.Se2Utils;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.UnitVector;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.io.ResourceData;
import ch.ethz.idsc.tensor.lie.AngleVector;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Ramp;

/* package */ class TrackDriving implements RenderInterface {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._063.cyclic().deriveWithAlpha(128);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_32 = COLOR_DATA_INDEXED.deriveWithAlpha(32);
  private static final ColorDataIndexed COLOR_DATA_INDEXED_64 = COLOR_DATA_INDEXED.deriveWithAlpha(64);
  private static final VehicleModel VEHICLE_MODEL = RimoSinusIonModel.standard();
  private static final Tensor FOOTPRINT = VEHICLE_MODEL.footprint();
  private static final Tensor COG = Tensors.of( //
      Magnitude.METER.apply(ChassisGeometry.GLOBAL.xAxleRtoCoM), //
      RealScalar.ZERO);
  private static final int ICON_SIZE = 32;
  // ---
  private final Tensor tensor;
  private final int id;
  private final int offset;
  private BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
  boolean enableTrace = false;
  private final GokartPoseContainer gokartPoseContainer = new GokartPoseContainer();
  private final ExtrudedFootprintRender extrudedFootprintRender = new ExtrudedFootprintRender(gokartPoseContainer);

  public TrackDriving(Tensor tensor, int id) {
    this.tensor = tensor;
    this.id = id;
    offset = IntStream.range(0, tensor.length()) //
        .filter(index -> Scalars.lessThan(RealScalar.of(1000), tensor.Get(index, 1))) //
        .findFirst().getAsInt();
  }

  public Tensor row(int index) {
    index += offset;
    index = Math.min(tensor.length() - 1, index);
    return tensor.get(index);
  }

  public int maxIndex() {
    return tensor.length() - offset;
  }

  public void setDriver(String name) {
    BufferedImage icon = ResourceData.bufferedImage("/image/driver/" + name + ".png");
    if (Objects.nonNull(icon))
      bufferedImage = icon;
    else
      System.err.println("driver icon not found [" + name + "]");
  }

  int render_index;

  public void setRenderIndex(int render_index) {
    this.render_index = render_index;
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor row = row(render_index);
    Tensor xya = row.extract(10, 13); // unitless
    {
      gokartPoseContainer.setPose(GokartPoseHelper.attachUnits(xya), RealScalar.ONE);
      extrudedFootprintRender.gokartStatusListener.getEvent(new GokartStatusEvent(row.Get(8).number().floatValue()));
      extrudedFootprintRender.render(geometricLayer, graphics);
    }
    {
      PathRender pathRender = new PathRender(COLOR_DATA_INDEXED_64.getColor(id), 1.5f);
      Tensor points = Tensor.of(tensor.stream().skip(offset).limit(render_index) //
          .map(v -> v.extract(10, 13)) //
          .map(Se2Bijection::new) //
          .map(Se2Bijection::forward) //
          .map(tuo -> tuo.apply(COG)));
      pathRender.setCurve(points, false).render(geometricLayer, graphics);
    }
    geometricLayer.pushMatrix(Se2Utils.toSE2Matrix(xya));
    {
      Path2D path2d = geometricLayer.toPath2D(FOOTPRINT);
      path2d.closePath();
      graphics.setColor(COLOR_DATA_INDEXED_32.getColor(id));
      graphics.fill(path2d);
      graphics.setColor(COLOR_DATA_INDEXED.getColor(id));
      graphics.draw(path2d);
    }
    {
      Point2D point2d = geometricLayer.toPoint2D(COG);
      graphics.drawImage(bufferedImage, //
          (int) point2d.getX() - ICON_SIZE / 2, //
          (int) point2d.getY() - ICON_SIZE / 2, ICON_SIZE, ICON_SIZE, null);
    }
    graphics.setStroke(new BasicStroke(2.5f));
    {
      final AxisAlignedBox axisAlignedBox = //
          new AxisAlignedBox(ChassisGeometry.GLOBAL.tireHalfWidthRear().multiply(RealScalar.of(.5)));
      double factor = 3E-4;
      double[] trq = new double[] { //
          row.Get(1).number().doubleValue() * factor, //
          row.Get(2).number().doubleValue() * factor //
      };
      Tensor[] ofs = new Tensor[] { Tensors.vector(0, 0, 0), Tensors.vector(0, 0, 0) };
      graphics.setColor(new Color(0, 0, 255, 64));
      for (int wheel = 0; wheel < 2; ++wheel) {
        Tensor vector = VEHICLE_MODEL.wheel(2 + wheel).lever();
        geometricLayer.pushMatrix(Se2Utils.toSE2Translation(vector.add(ofs[wheel])));
        Path2D path = geometricLayer.toPath2D(axisAlignedBox.alongY(RealScalar.of(trq[0 + wheel])));
        path.closePath();
        graphics.fill(path);
        geometricLayer.popMatrix();
      }
      // Scalar factor = row.Get(1).divide(RealScalar.of(4000));
      // graphics.setColor(new Color(0, 0, 255, 128));
      // graphics.draw(geometricLayer.toVector(Tensors.vector(1, 0), UnitVector.of(2, 0).multiply(factor)));
    }
    {
      Scalar factor = Ramp.FUNCTION.apply(row.Get(9).negate().subtract(RealScalar.of(0.02))).divide(RealScalar.of(-0.06));
      graphics.setColor(new Color(255, 0, 0, 128));
      graphics.draw(geometricLayer.toVector(Tensors.vector(1, 0), UnitVector.of(2, 0).multiply(factor)));
    }
    graphics.setStroke(new BasicStroke(2.5f));
    {
      Scalar angle = SteerConfig.GLOBAL.getSteerMapping().getAngleFromSCE(Quantity.of(row.Get(8), "SCE"));
      Tensor pair = ChassisGeometry.GLOBAL.getAckermannSteering().pair(angle);
      graphics.setColor(new Color(128, 128, 128, 128));
      Tensor v1 = AngleVector.of(pair.Get(0)).multiply(RealScalar.of(.2));
      graphics.draw(geometricLayer.toVector(Tensors.vector(1.19, +.5), v1));
      graphics.draw(geometricLayer.toVector(Tensors.vector(1.19, +.5), v1.negate()));
      Tensor v2 = AngleVector.of(pair.Get(1)).multiply(RealScalar.of(.2));
      graphics.draw(geometricLayer.toVector(Tensors.vector(1.19, -.5), v2));
      graphics.draw(geometricLayer.toVector(Tensors.vector(1.19, -.5), v2.negate()));
    }
    graphics.setStroke(new BasicStroke(1));
    geometricLayer.popMatrix();
  }

  public Scalar timeFor(int index) {
    return row(index).Get(0).subtract(row(0).Get(0));
  }
}

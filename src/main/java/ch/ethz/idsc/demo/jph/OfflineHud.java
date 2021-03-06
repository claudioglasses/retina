// code by jph
package ch.ethz.idsc.demo.jph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import javax.imageio.ImageIO;

import ch.ethz.idsc.gokart.calib.steer.RimoTwdOdometry;
import ch.ethz.idsc.gokart.calib.steer.SteerColumnEvent;
import ch.ethz.idsc.gokart.core.pos.GokartPoseEvent;
import ch.ethz.idsc.gokart.core.slam.LocalizationConfig;
import ch.ethz.idsc.gokart.core.slam.PredefinedMap;
import ch.ethz.idsc.gokart.dev.rimo.RimoGetEvent;
import ch.ethz.idsc.gokart.gui.GokartLcmChannel;
import ch.ethz.idsc.gokart.gui.top.AccumulatedEventRender;
import ch.ethz.idsc.gokart.gui.top.ExtrudedFootprintRender;
import ch.ethz.idsc.gokart.gui.top.GlobalGokartRender;
import ch.ethz.idsc.gokart.gui.top.GokartRender;
import ch.ethz.idsc.gokart.gui.top.TrigonometryRender;
import ch.ethz.idsc.gokart.lcm.OfflineLogListener;
import ch.ethz.idsc.gokart.lcm.OfflineLogPlayer;
import ch.ethz.idsc.gokart.lcm.autobox.RimoLcmServer;
import ch.ethz.idsc.gokart.lcm.davis.DavisLcmClient;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.region.ImageRender;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.sophus.lie.se2.Se2Matrix;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.mat.DiagonalMatrix;
import ch.ethz.idsc.tensor.mat.IdentityMatrix;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Round;

/** visualization of gokart odometry and sensor information
 * 
 * https://www.youtube.com/watch?v=noqpenUZ34w */
// public because class is referenced outside of retina
public class OfflineHud implements OfflineLogListener {
  public static final Dimension DIMENSION = new Dimension(1920, 480);
  public static final PredefinedMap PREDEFINED_MAP = LocalizationConfig.GLOBAL.getPredefinedMap();
  // ---
  private final Scalar delta;
  final RenderInterface renderInterface = ImageRender.scale( //
      PREDEFINED_MAP.getImage(), Tensors.vector(1, 1));
  final GokartRender gokartRender = new GlobalGokartRender();
  final DavisLcmClient davisLcmClient = new DavisLcmClient(GokartLcmChannel.DAVIS_OVERVIEW);
  final AccumulatedEventRender accumulatedEventRender = new AccumulatedEventRender();
  final TrigonometryRender trigonometryRender = new TrigonometryRender();
  final ExtrudedFootprintRender extrudedFootprintRender = new ExtrudedFootprintRender();
  // ---
  private Scalar time_next = Quantity.of(0, SI.SECOND);
  private RimoGetEvent rimoGetEvent;
  private SteerColumnEvent steerColumnEvent;
  private GokartPoseEvent gokartPoseEvent;

  public OfflineHud(Scalar period) {
    this.delta = period;
    accumulatedEventRender.isSelected = true;
    davisLcmClient.addDvsListener(accumulatedEventRender.abstractAccumulatedImage);
  }

  @Override // from OfflineLogListener
  public void event(Scalar time, String channel, ByteBuffer byteBuffer) {
    if (channel.equals(RimoLcmServer.CHANNEL_GET)) {
      rimoGetEvent = new RimoGetEvent(byteBuffer);
    } else //
    if (channel.equals(GokartLcmChannel.STATUS)) {
      steerColumnEvent = new SteerColumnEvent(byteBuffer);
    } else //
    if (channel.equals(GokartLcmChannel.POSE_LIDAR)) {
      gokartPoseEvent = GokartPoseEvent.of(byteBuffer);
    } else //
    if (channel.equals("davis240c.overview.dvs")) {
      davisLcmClient.messageReceived(byteBuffer);
    }
    // ---
    if (Scalars.lessThan(time_next, time)
    // && Scalars.lessThan(time, Quantity.of(5, SI.SECOND))
    ) {
      // System.out.println(time_next);
      if (Objects.nonNull(rimoGetEvent) && //
          Objects.nonNull(steerColumnEvent)) {
        BufferedImage bufferedImage = //
            new BufferedImage(DIMENSION.width, DIMENSION.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, DIMENSION.width, DIMENSION.height);
        GeometricLayer geometricLayer = GeometricLayer.of(IdentityMatrix.of(3));
        double s = 7.5 * 3.5;
        int h = 640 + 1000;
        Tensor model2pixel = Tensors.matrix(new Number[][] { //
            { s, 0, 0 }, //
            { 0, -s, h }, //
            { 0, 0, 1 }, //
        }).unmodifiable();
        // geometricLayer.pushMatrix(PREDEFINED_MAP.getModel2Pixel());
        geometricLayer.pushMatrix(model2pixel);
        geometricLayer.pushMatrix(Se2Matrix.of(Tensors.vector(-25, 40, -.6)));
        {
          geometricLayer.pushMatrix(DiagonalMatrix.of(1 / 7.5, 1 / 7.5, 1));
          renderInterface.render(geometricLayer, graphics);
          geometricLayer.popMatrix();
        }
        trigonometryRender.gokartPoseListener.getEvent(gokartPoseEvent);
        trigonometryRender.steerColumnListener.getEvent(steerColumnEvent);
        trigonometryRender.render(geometricLayer, graphics);
        extrudedFootprintRender.gokartPoseListener.getEvent(gokartPoseEvent);
        extrudedFootprintRender.steerColumnListener.getEvent(steerColumnEvent);
        extrudedFootprintRender.color = Color.CYAN;
        extrudedFootprintRender.render(geometricLayer, graphics);
        gokartRender.rimoGetListener.getEvent(rimoGetEvent);
        gokartRender.steerColumnListener.getEvent(steerColumnEvent);
        gokartRender.gokartPoseListener.getEvent(gokartPoseEvent);
        gokartRender.render(geometricLayer, graphics);
        accumulatedEventRender.render(geometricLayer, graphics);
        // ---
        graphics.setColor(Color.GREEN);
        Scalar vel = RimoTwdOdometry.tangentSpeed(rimoGetEvent);
        String string = String.format("%+3.1f[m/s]", vel.map(Round._1).Get().number().doubleValue());
        // System.out.println(string);
        graphics.setFont(new Font(Font.DIALOG, Font.BOLD, 10));
        graphics.drawString(time.map(Round._6).toString(), 0, 10);
        graphics.setFont(new Font(Font.DIALOG, Font.BOLD, 50));
        graphics.drawString(string, 0, 60);
        callback(bufferedImage);
      }
      time_next = time_next.add(delta);
    }
  }

  public void callback(BufferedImage bufferedImage) {
    try {
      ImageIO.write(bufferedImage, "png", HomeDirectory.file("asd.png"));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    OfflineHud offlineHud = new OfflineHud(Quantity.of(RationalScalar.of(1, 30), SI.SECOND));
    OfflineLogPlayer.process(HomeDirectory.file("20180522T111414.lcm"), offlineHud);
  }
}

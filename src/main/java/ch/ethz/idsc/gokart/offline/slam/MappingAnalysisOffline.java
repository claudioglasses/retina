// code by ynager
package ch.ethz.idsc.gokart.offline.slam;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;
import java.util.function.Consumer;

import ch.ethz.idsc.gokart.core.fuse.SafetyConfig;
import ch.ethz.idsc.gokart.core.map.BayesianOccupancyGrid;
import ch.ethz.idsc.gokart.core.map.MappingConfig;
import ch.ethz.idsc.gokart.core.perc.SpacialXZObstaclePredicate;
import ch.ethz.idsc.gokart.core.pos.GokartPoseEvent;
import ch.ethz.idsc.gokart.core.slam.LocalizationConfig;
import ch.ethz.idsc.gokart.core.slam.PredefinedMap;
import ch.ethz.idsc.gokart.gui.GokartLcmChannel;
import ch.ethz.idsc.gokart.gui.top.GlobalGokartRender;
import ch.ethz.idsc.gokart.gui.top.GokartRender;
import ch.ethz.idsc.gokart.gui.top.SensorsConfig;
import ch.ethz.idsc.gokart.lcm.OfflineLogListener;
import ch.ethz.idsc.gokart.lcm.lidar.VelodyneLcmChannels;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.retina.lidar.LidarAngularFiringCollector;
import ch.ethz.idsc.retina.lidar.LidarRayBlockEvent;
import ch.ethz.idsc.retina.lidar.LidarRayBlockListener;
import ch.ethz.idsc.retina.lidar.LidarRotationProvider;
import ch.ethz.idsc.retina.lidar.LidarSpacialProvider;
import ch.ethz.idsc.retina.lidar.VelodyneDecoder;
import ch.ethz.idsc.retina.lidar.VelodyneModel;
import ch.ethz.idsc.retina.lidar.vlp16.Vlp16Decoder;
import ch.ethz.idsc.retina.lidar.vlp16.Vlp16SegmentProvider;
import ch.ethz.idsc.retina.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

// TODO contains redundancies with GokartMappingModule 
public class MappingAnalysisOffline implements OfflineLogListener, LidarRayBlockListener {
  private static final String CHANNEL_LIDAR = //
      VelodyneLcmChannels.ray(VelodyneModel.VLP16, GokartLcmChannel.VLP16_CENTER);
  // ---
  private final VelodyneDecoder velodyneDecoder = new Vlp16Decoder();
  private final BayesianOccupancyGrid bayesianOccupancyGrid;
  private final Consumer<BufferedImage> consumer;
  // ---
  private GokartPoseEvent gokartPoseEvent;
  private ScatterImage scatterImage;
  private Scalar time_next = Quantity.of(0, SI.SECOND);
  private Scalar delta = Quantity.of(0.1, SI.SECOND);
  private SpacialXZObstaclePredicate predicate = SafetyConfig.GLOBAL.createSpacialXZObstaclePredicate();

  public MappingAnalysisOffline(MappingConfig mappingConfig, Consumer<BufferedImage> consumer) {
    this.consumer = consumer;
    bayesianOccupancyGrid = mappingConfig.createBayesianOccupancyGrid();
    LidarAngularFiringCollector lidarAngularFiringCollector = //
        new LidarAngularFiringCollector(10_000, 3);
    double offset = SensorsConfig.GLOBAL.vlp16_twist.number().doubleValue();
    LidarSpacialProvider lidarSpacialProvider = new Vlp16SegmentProvider(offset, -1);
    lidarSpacialProvider.addListener(lidarAngularFiringCollector);
    LidarRotationProvider lidarRotationProvider = new LidarRotationProvider();
    lidarRotationProvider.addListener(lidarAngularFiringCollector);
    velodyneDecoder.addRayListener(lidarSpacialProvider);
    velodyneDecoder.addRayListener(lidarRotationProvider);
    lidarAngularFiringCollector.addListener(this);
  }

  @Override // from OfflineLogListener
  public void event(Scalar time, String channel, ByteBuffer byteBuffer) {
    if (channel.equals(GokartLcmChannel.POSE_LIDAR)) {
      gokartPoseEvent = GokartPoseEvent.of(byteBuffer);
      bayesianOccupancyGrid.setPose(gokartPoseEvent.getPose());
    } else if (channel.equals(CHANNEL_LIDAR)) {
      velodyneDecoder.lasers(byteBuffer);
    }
    if (Scalars.lessThan(time_next, time) && Objects.nonNull(gokartPoseEvent)) {
      time_next = time.add(delta);
      PredefinedMap predefinedMap = LocalizationConfig.GLOBAL.getPredefinedMap();
      scatterImage = new WallScatterImage(predefinedMap);
      BufferedImage image = scatterImage.getImage();
      GeometricLayer gl = new GeometricLayer(predefinedMap.getModel2Pixel(), Tensors.vector(0, 0, 0));
      Graphics2D graphics = image.createGraphics();
      GokartRender gokartRender = new GlobalGokartRender();
      gokartRender.gokartPoseListener.getEvent(gokartPoseEvent);
      bayesianOccupancyGrid.render(gl, graphics);
      gokartRender.render(gl, graphics);
      // if (Scalars.lessEquals(RealScalar.of(3), Magnitude.SECOND.apply(time)) && flag == false) {
      // grid.setNewlBound(Tensors.vector(20, 20));
      // flag = true;
      // }
      // ---
      // grid.genObstacleMap();
      // System.out.println(time);
      bayesianOccupancyGrid.genObstacleMap();
      consumer.accept(image);
    }
  }

  @Override // from LidarRayBlockListener
  public void lidarRayBlock(LidarRayBlockEvent lidarRayBlockEvent) {
    FloatBuffer floatBuffer = lidarRayBlockEvent.floatBuffer;
    if (lidarRayBlockEvent.dimensions == 3)
      while (floatBuffer.hasRemaining()) {
        float x = floatBuffer.get();
        float y = floatBuffer.get();
        float z = floatBuffer.get();
        //
        boolean isObstacle = predicate.isObstacle(x, z);
        bayesianOccupancyGrid.processObservation( //
            Tensors.vectorDouble(x, y), //
            isObstacle ? 1 : 0);
      }
  }
}
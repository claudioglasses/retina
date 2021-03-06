// code by jph
package ch.ethz.idsc.gokart.offline.slam;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import ch.ethz.idsc.gokart.gui.GokartLcmChannel;
import ch.ethz.idsc.gokart.lcm.OfflineLogListener;
import ch.ethz.idsc.gokart.lcm.lidar.VelodyneLcmChannels;
import ch.ethz.idsc.retina.lidar.LidarAngularFiringCollector;
import ch.ethz.idsc.retina.lidar.LidarRayBlockEvent;
import ch.ethz.idsc.retina.lidar.LidarRayBlockListener;
import ch.ethz.idsc.retina.lidar.LidarRotationProvider;
import ch.ethz.idsc.retina.lidar.LidarSpacialProvider;
import ch.ethz.idsc.retina.lidar.VelodyneDecoder;
import ch.ethz.idsc.retina.lidar.VelodyneModel;
import ch.ethz.idsc.retina.lidar.vlp16.Vlp16Decoder;
import ch.ethz.idsc.tensor.Scalar;

public abstract class LidarProcessOffline implements LidarRayBlockListener, OfflineLogListener {
  private static final String CHANNEL_LIDAR = //
      VelodyneLcmChannels.ray(VelodyneModel.VLP16, GokartLcmChannel.VLP16_CENTER);
  // ---
  private final VelodyneDecoder velodyneDecoder = new Vlp16Decoder();

  /** @param lidarSpacialProvider */
  public LidarProcessOffline(LidarSpacialProvider lidarSpacialProvider) {
    LidarAngularFiringCollector lidarAngularFiringCollector = //
        new LidarAngularFiringCollector(10_000, 3);
    lidarSpacialProvider.addListener(lidarAngularFiringCollector);
    LidarRotationProvider lidarRotationProvider = new LidarRotationProvider();
    lidarRotationProvider.addListener(lidarAngularFiringCollector);
    velodyneDecoder.addRayListener(lidarSpacialProvider);
    velodyneDecoder.addRayListener(lidarRotationProvider);
    lidarAngularFiringCollector.addListener(this);
  }

  @Override // from LidarRayBlockListener
  public final void lidarRayBlock(LidarRayBlockEvent lidarRayBlockEvent) {
    FloatBuffer floatBuffer = lidarRayBlockEvent.floatBuffer;
    while (floatBuffer.hasRemaining()) {
      float x = floatBuffer.get();
      float y = floatBuffer.get();
      float z = floatBuffer.get();
      process(x, y, z);
    }
  }

  @Override // from OfflineLogListener
  public final void event(Scalar time, String channel, ByteBuffer byteBuffer) {
    if (channel.equals(CHANNEL_LIDAR))
      velodyneDecoder.lasers(byteBuffer);
    protected_event(time, channel, byteBuffer);
  }

  /** function processes message from log file
   *
   * @param time since begin of log with unit [s]
   * @param channel
   * @param byteBuffer */
  protected abstract void protected_event(Scalar time, String channel, ByteBuffer byteBuffer);

  /** (x, y, z) is the coordinate of ray-obstacle intersection provided
   * by the given lidarSpacialProvider
   * 
   * @param x
   * @param y
   * @param z */
  protected abstract void process(float x, float y, float z);
}

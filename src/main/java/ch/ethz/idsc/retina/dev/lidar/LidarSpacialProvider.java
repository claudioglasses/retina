// code by jph
package ch.ethz.idsc.retina.dev.lidar;

/** the spacial provider listens to ray data and transforms information
 * to events with time and 3d, or 2d coordinates */
public interface LidarSpacialProvider extends LidarRayDataListener {
  void addListener(LidarSpacialEventListener lidarSpacialEventListener);
}

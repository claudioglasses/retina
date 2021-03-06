// code by mg
package ch.ethz.idsc.demo.mg;

import java.nio.ByteBuffer;

import ch.ethz.idsc.gokart.lcm.OfflineLogListener;
import ch.ethz.idsc.gokart.lcm.davis.DvsLcmClient;
import ch.ethz.idsc.retina.app.blob.BlobTrackConfig;
import ch.ethz.idsc.retina.app.blob.algo.BlobTrackProvider;
import ch.ethz.idsc.retina.app.blob.vis.BlobTrackViewer;
import ch.ethz.idsc.retina.app.slam.config.SlamDvsConfig;
import ch.ethz.idsc.tensor.Scalar;

/** wrapper to run the blob tracking algorithm offline */
/* package */ class OfflineBlobTrackWrap implements OfflineLogListener {
  // TODO JPH class design
  private static final String CHANNEL_DVS = SlamDvsConfig.eventCamera.slamCoreConfig.dvsConfig.channel_DVS;
  // ---
  private final DvsLcmClient dvsLcmClient;
  // specific to blob tracking algorithm
  private final BlobTrackProvider blobTrackProvider;
  private final BlobTrackViewer blobTrackViewer;

  OfflineBlobTrackWrap(BlobTrackConfig blobTrackConfig) {
    blobTrackProvider = new BlobTrackProvider(blobTrackConfig);
    blobTrackViewer = new BlobTrackViewer(blobTrackConfig, blobTrackProvider);
    dvsLcmClient = SlamDvsConfig.eventCamera.slamCoreConfig.dvsConfig.dvsLcmClient;
    dvsLcmClient.addDvsListener(blobTrackProvider);
    dvsLcmClient.addDvsListener(blobTrackViewer);
  }

  @Override // from OfflineLogListener
  public void event(Scalar time, String channel, ByteBuffer byteBuffer) {
    if (channel.equals(CHANNEL_DVS))
      dvsLcmClient.messageReceived(byteBuffer);
  }
}

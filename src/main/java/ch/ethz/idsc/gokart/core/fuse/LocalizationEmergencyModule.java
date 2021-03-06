// code by jph
package ch.ethz.idsc.gokart.core.fuse;

import java.util.Optional;

import ch.ethz.idsc.gokart.core.pos.GokartPoseEvent;
import ch.ethz.idsc.gokart.core.pos.GokartPoseEvents;
import ch.ethz.idsc.gokart.core.pos.GokartPoseLcmClient;
import ch.ethz.idsc.gokart.core.pos.GokartPoseListener;
import ch.ethz.idsc.gokart.core.slam.LocalizationConfig;
import ch.ethz.idsc.gokart.dev.rimo.RimoPutEvent;
import ch.ethz.idsc.gokart.dev.rimo.RimoPutProvider;
import ch.ethz.idsc.gokart.dev.rimo.RimoSocket;
import ch.ethz.idsc.owl.ani.api.ProviderRank;
import ch.ethz.idsc.retina.util.sys.AbstractModule;
import ch.ethz.idsc.retina.util.time.SoftWatchdog;
import ch.ethz.idsc.retina.util.time.Watchdog;
import ch.ethz.idsc.tensor.Scalars;

/** prevents driving if pose is has insufficient quality for timeout duration */
public final class LocalizationEmergencyModule extends AbstractModule implements GokartPoseListener, RimoPutProvider {
  /** timeout 0.3[s] */
  private final Watchdog watchdog = SoftWatchdog.barking(0.3);
  private final GokartPoseLcmClient gokartPoseLcmClient = new GokartPoseLcmClient();
  private GokartPoseEvent gokartPoseEvent = GokartPoseEvents.motionlessUninitialized();

  @Override // from AbstractModule
  protected void first() {
    gokartPoseLcmClient.addListener(this);
    gokartPoseLcmClient.startSubscriptions();
    RimoSocket.INSTANCE.addPutProvider(this);
  }

  @Override // from AbstractModule
  protected void last() {
    RimoSocket.INSTANCE.removePutProvider(this);
    gokartPoseLcmClient.stopSubscriptions();
  }

  @Override // from GokartPoseListener
  public void getEvent(GokartPoseEvent gokartPoseEvent) {
    this.gokartPoseEvent = gokartPoseEvent;
    if (LocalizationConfig.GLOBAL.isQualityOk(gokartPoseEvent))
      watchdog.notifyWatchdog();
  }

  @Override // from RimoPutProvider
  public ProviderRank getProviderRank() {
    return ProviderRank.EMERGENCY;
  }

  @Override // from RimoPutProvider
  public Optional<RimoPutEvent> putEvent() {
    if (SafetyConfig.GLOBAL.checkPoseQuality)
      if (watchdog.isBarking() || // bad tracking
          Scalars.isZero(gokartPoseEvent.getQuality())) // systematic fault
        return RimoPutEvent.OPTIONAL_RIMO_PASSIVE;
    return Optional.empty();
  }
}
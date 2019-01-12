// code by jph
package ch.ethz.idsc.gokart.dev.u3;

import java.nio.ByteBuffer;
import java.util.Optional;

import ch.ethz.idsc.gokart.lcm.BinaryLcmClient;
import ch.ethz.idsc.retina.joystick.ManualControlInterface;
import ch.ethz.idsc.retina.joystick.ManualControlProvider;
import ch.ethz.idsc.retina.util.data.TimedFuse;

public final class GokartLabjackLcmClient extends BinaryLcmClient implements ManualControlProvider {
  /** if no message is received for a period of 0.2[s]
   * the labjack adc frame is set to passive */
  private final TimedFuse timedFuse;
  // ---
  private ManualControlInterface manualControlInterface = null;

  /** @param channel
   * @param timeout in [s] */
  public GokartLabjackLcmClient(String channel, double timeout) {
    super(channel);
    timedFuse = new TimedFuse(timeout);
  }

  @Override
  protected void messageReceived(ByteBuffer byteBuffer) {
    manualControlInterface = new GokartLabjackFrame(byteBuffer);
    timedFuse.pacify();
  }

  @Override
  public void start() {
    startSubscriptions();
  }

  @Override
  public void stop() {
    stopSubscriptions();
  }

  @Override
  public Optional<ManualControlInterface> getManualControl() {
    return Optional.ofNullable(timedFuse.isBlown() //
        ? null
        : manualControlInterface);
  }
}

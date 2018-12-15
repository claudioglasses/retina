// code by jph
package ch.ethz.idsc.gokart.dev;

import java.nio.ByteBuffer;
import java.util.Optional;

import ch.ethz.idsc.retina.dev.joystick.GokartJoystickInterface;
import ch.ethz.idsc.retina.dev.joystick.JoystickDecoder;
import ch.ethz.idsc.retina.dev.joystick.ManualControlProvider;
import ch.ethz.idsc.retina.lcm.BinaryLcmClient;
import ch.ethz.idsc.retina.util.data.TimedFuse;

/** client to lcm channel with joystick information */
public final class JoystickLcmProvider extends BinaryLcmClient implements ManualControlProvider {
  private final TimedFuse timedFuse;
  // ---
  private GokartJoystickInterface gokartJoystickInterface = null;

  /** @param channel for instance "generic_xbox_pad"
   * @param timeout_ms maximum age of joystick information relayed to application layer */
  public JoystickLcmProvider(String channel, double timeout) {
    super(channel);
    timedFuse = new TimedFuse(timeout);
  }

  @Override // from LcmClientAdapter
  protected void messageReceived(ByteBuffer byteBuffer) {
    timedFuse.pacify();
    gokartJoystickInterface = (GokartJoystickInterface) JoystickDecoder.decode(byteBuffer);
  }

  @Override
  public void start() {
    startSubscriptions();
  }

  @Override
  public void stop() {
    stopSubscriptions();
  }

  /** @return recent joystick readout, or empty */
  @Override
  public Optional<GokartJoystickInterface> getJoystick() {
    return Optional.ofNullable(timedFuse.isBlown() //
        ? null
        : gokartJoystickInterface);
  }
}
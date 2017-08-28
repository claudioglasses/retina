// code by jph
package ch.ethz.idsc.retina.dev.velodyne.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.ethz.idsc.retina.dev.velodyne.VelodyneDecoder;
import ch.ethz.idsc.retina.dev.velodyne.hdl32e.Hdl32eDecoder;
import ch.ethz.idsc.retina.dev.velodyne.vlp16.Vlp16Decoder;
import ch.ethz.idsc.retina.util.io.PcapPacketListener;

/** default packet distribution
 * 
 * implementation decides based on length of packet to
 * process the data either as firing packet or as GPS */
public class VelodynePcapPacketDecoder implements PcapPacketListener {
  public static VelodynePcapPacketDecoder hdl32e() {
    return new VelodynePcapPacketDecoder(new Hdl32eDecoder());
  }

  public static VelodynePcapPacketDecoder vlp16() {
    return new VelodynePcapPacketDecoder(new Vlp16Decoder());
  }

  // ---
  public final VelodyneDecoder velodyneDecoder;

  public VelodynePcapPacketDecoder(VelodyneDecoder posDecoder) {
    this.velodyneDecoder = posDecoder;
  }

  @Override
  public void packet(int sec, int usec, byte[] packet_data, int length) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(packet_data);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    /** the answer to life the universe and everything
     * hdl32e user's manual refers to first 42 bytes as ethernet header
     * they are only present in pcap file, but not in upd packets from live sensor */
    byteBuffer.position(42);
    switch (length) {
    case 1248:
      velodyneDecoder.lasers(byteBuffer);
      break;
    case 554:
      velodyneDecoder.positioning(byteBuffer);
      break;
    default:
      System.err.println("unknown length " + length);
    }
  }
}

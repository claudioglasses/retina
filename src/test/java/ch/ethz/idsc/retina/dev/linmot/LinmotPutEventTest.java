// code by jph
package ch.ethz.idsc.retina.dev.linmot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import junit.framework.TestCase;

public class LinmotPutEventTest extends TestCase {
  public void testSimple() {
    LinmotPutEvent linmotPutEvent = //
        new LinmotPutEvent(LinmotPutHelper.CMD_HOME, LinmotPutHelper.MC_POSITION, (short) 302, (short) 303, (short) 304, (short) 305);
    assertTrue(Objects.nonNull(linmotPutEvent.toInfoString()));
    byte[] array = linmotPutEvent.asArray();
    assertEquals(array.length, 12);
    ByteBuffer byteBuffer = ByteBuffer.wrap(array);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    assertEquals(byteBuffer.getShort(), 0x083f);
    assertEquals(byteBuffer.getShort(), 0x0900);
    assertEquals(byteBuffer.getShort(), 302);
    assertEquals(byteBuffer.getShort(), 303);
    assertEquals(byteBuffer.getShort(), 304);
    assertEquals(byteBuffer.getShort(), 305);
  }
}
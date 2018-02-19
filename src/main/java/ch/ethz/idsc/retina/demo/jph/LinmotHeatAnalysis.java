// code by jph
package ch.ethz.idsc.retina.demo.jph;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import ch.ethz.idsc.retina.demo.DubendorfHangarLog;
import ch.ethz.idsc.retina.dev.linmot.LinmotGetEvent;
import ch.ethz.idsc.retina.dev.rimo.RimoGetEvent;
import ch.ethz.idsc.retina.gui.gokart.top.ChassisGeometry;
import ch.ethz.idsc.retina.lcm.autobox.LinmotLcmServer;
import ch.ethz.idsc.retina.lcm.autobox.RimoLcmServer;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.retina.util.math.TableBuilder;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.sca.Round;

class LinmotHeatAnalysis implements OfflineTableSupplier {
  final TableBuilder tableBuilder = new TableBuilder();
  private RimoGetEvent rge;

  @Override
  public void event(Scalar time, String channel, ByteBuffer byteBuffer) {
    if (channel.equals(RimoLcmServer.CHANNEL_GET)) {
      rge = new RimoGetEvent(byteBuffer);
    } else //
    if (channel.equals(LinmotLcmServer.CHANNEL_GET)) {
      LinmotGetEvent linmotGetEvent = new LinmotGetEvent(byteBuffer);
      if (Objects.nonNull(rge)) {
        tableBuilder.appendRow( //
            time.map(Magnitude.SECOND).map(Round._6), //
            linmotGetEvent.getActualPosition().map(Magnitude.METER), //
            linmotGetEvent.getDemandPosition().map(Magnitude.METER), //
            linmotGetEvent.getPositionDiscrepancy().map(Magnitude.METER), //
            linmotGetEvent.getWindingTemperature1().map(Magnitude.DEGREE_CELSIUS).map(Round._1), //
            linmotGetEvent.getWindingTemperature2().map(Magnitude.DEGREE_CELSIUS).map(Round._1), //
            rge.getAngularRate_Y_pair().map(Magnitude.ANGULAR_RATE), //
            ChassisGeometry.GLOBAL.tangentSpeed(rge).map(Magnitude.VELOCITY) //
        );
      }
    }
  }

  @Override
  public Tensor getTable() {
    return tableBuilder.toTable();
  }

  private static final File LOG_ROOT = new File("/media/datahaki/media/ethz/gokartlogs");

  public static void main(String[] args) throws IOException {
    OfflineProcessing.single( //
        DubendorfHangarLog._20171213T162832_55710a6b.file(LOG_ROOT), //
        new LinmotHeatAnalysis(), //
        "linmot_temperature");
  }
}

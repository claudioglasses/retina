// code by jph
package ch.ethz.idsc.retina.demo.jph;

import java.io.File;

import ch.ethz.idsc.retina.core.DvsEvent;
import ch.ethz.idsc.retina.digest.DvsEventBuffer;
import ch.ethz.idsc.retina.digest.DvsEventStatistics;
import ch.ethz.idsc.retina.io.txt.TxtFileSupplier;
import ch.ethz.idsc.retina.supply.DvsEventSupplier;
import ch.ethz.idsc.retina.util.io.ImageDimensions;

class TxtSupplierTest {
  public static void main(String[] args) throws Exception {
    DvsEventStatistics stats = new DvsEventStatistics();
    int maxx = 0;
    int maxy = 0;
    File file = new File("/media/datahaki/media/ethz/davis/shapes_6dof", //
        "events.txt");
    System.out.println(file.exists());
    try {
      DvsEventSupplier sup = new TxtFileSupplier(file, ImageDimensions.UZ);
      DvsEventBuffer buf = new DvsEventBuffer(10000);
      while (true) {
        DvsEvent dvsEvent = sup.next();
        maxx = Math.max(dvsEvent.x, maxx);
        maxy = Math.max(dvsEvent.y, maxy);
        stats.digest(dvsEvent);
        buf.digest(dvsEvent);
        // if (count%100000==0)
        // System.out.println(dvsEvent.toString());
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    System.out.println(maxx + " " + maxy);
    stats.printSummary();
  }
}

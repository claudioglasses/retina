// code by jph
package ch.ethz.idsc.demo.az;

import java.io.File;

import ch.ethz.idsc.retina.davis.app.AedatLogConverter;
import ch.ethz.idsc.tensor.io.Timing;

/* package */ enum AedatLogConverterDemo {
  ;
  @SuppressWarnings("unused")
  public static void main(String[] args) throws Exception {
    final File file1 = new File("/home/ale/Datasets/OurCityscapes/5", "DAVIS240C-2017-09-06_5.aedat");
    final File file2 = new File("/tmp", "DAVIS240C-2017-08-03T18-16-55+0200-02460045-0.aedat");
    final File file3 = new File("/tmp", "DAVIS240C-2017-08-04T10-13-29+0200-02460045-0.aedat");
    Timing timing = Timing.started();
    AedatLogConverter.of(file1, new File("/home/ale/Datasets/OurCityscapes/5"));
    System.out.println(timing.seconds() + "[s]");
  }
}

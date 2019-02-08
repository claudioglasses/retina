// code by jph
package ch.ethz.idsc.demo.jph.sys;

import java.io.File;

/* package */ enum LogPostBulk {
  ;
  public static void main(String[] args) throws Exception {
    final File root = new File("/media/datahaki/data/gokart/cuts/20190204");
    for (File folder : root.listFiles())
      if (folder.isDirectory()) {
        System.out.println(folder);
        LogPoseInjectSingle.post(folder);
      }
  }
}

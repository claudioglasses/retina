// code by mg
package ch.ethz.idsc.demo.mg.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import ch.ethz.idsc.demo.mg.LogFileLocations;
import ch.ethz.idsc.owl.bot.util.UserHome;
import ch.ethz.idsc.retina.util.data.TensorProperties;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.io.StringScalar;

/** defines all parameters of the control pipeline and optionally saves them to a .properties file */
public class PipelineConfig {
  // log file parameters
  public Scalar logFileName = StringScalar.of("DUBI10d"); // must match name in LogFileLocations
  public Scalar maxDuration = RealScalar.of(2000); // [ms]
  // general parameters
  public Scalar width = RealScalar.of(240);
  public Scalar height = RealScalar.of(180);
  public Scalar unitConversion = RealScalar.of(1000);
  // event filtering
  public Scalar filterConstant = RealScalar.of(500); // [us]
  public Scalar boarder = RealScalar.of(4);
  // feature tracking
  public Scalar initNumberOfBlobs = RealScalar.of(24);
  public Scalar numberRows = RealScalar.of(6);
  public Scalar initVariance = RealScalar.of(250);
  public Scalar aUp = RealScalar.of(0.15);
  public Scalar aDown = RealScalar.of(0.12);
  public Scalar scoreThreshold = RealScalar.of(4e-4f);
  public Scalar alphaOne = RealScalar.of(0.9);
  public Scalar alphaTwo = RealScalar.of(0.998);
  public Scalar alphaAttr = RealScalar.of(0.002);
  public Scalar dAttr = RealScalar.of(50);
  public Scalar dMerge = RealScalar.of(20);
  public Scalar boundaryDistance = RealScalar.of(1);
  public Scalar tau = RealScalar.of(8000);
  // feature selection
  public Scalar upperBoarder = RealScalar.of(100);
  // image to world transform
  public Scalar calibrationFileName = StringScalar.of("/demo/mg/dubi0008.csv"); // path in main/resources/..
  // image saving
  public Scalar saveImages = RealScalar.of(0); // used as boolean: 0 == false, else == true
  public Scalar savingInterval = RealScalar.of(33); // [ms]
  /** tracking evaluation in HandLabelFileLocations.labels(..) */
  public Scalar handLabelFileName = StringScalar.of(logFileName.toString() + "_labeledFeatures.csv");
  /** used as boolean: 0 == false, else == true
   * access via function isPerformanceEvaluated() */
  public Scalar evaluatePerformance = RealScalar.of(0);
  /** visualization used as boolean: 0 == false, else == true
   * access via function isVisualized() */
  public Scalar visualizePipeline = RealScalar.of(1);
  public Scalar visualizationInterval = RealScalar.of(33); // [ms]

  /***************************************************/
  public File getLogFile() {
    LogFileLocations logFileLocations = LogFileLocations.valueOf(logFileName.toString());
    if (Objects.isNull(logFileLocations))
      throw new RuntimeException("invalid logFileName: " + logFileName);
    return logFileLocations.getFile();
  }

  public boolean isVisualized() {
    return Scalars.nonZero(visualizePipeline);
  }

  public boolean isPerformanceEvaluated() {
    return Scalars.nonZero(evaluatePerformance);
  }

  // for testing
  public static void main(String[] args) throws IOException {
    PipelineConfig test = new PipelineConfig();
    System.out.println(test.getLogFile());
    TensorProperties.manifest(UserHome.file("config2.properties"), test);
    // private final PipelineConfig pipelineConfig = TensorProperties.retrieve(UserHome.file("config.properties"), new PipelineConfig());
  }
}

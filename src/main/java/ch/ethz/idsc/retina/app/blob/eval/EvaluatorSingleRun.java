// code by mg
package ch.ethz.idsc.retina.app.blob.eval;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import ch.ethz.idsc.retina.app.SaveFrame;
import ch.ethz.idsc.retina.app.blob.BlobTrackConfig;
import ch.ethz.idsc.retina.app.blob.ImageBlob;
import ch.ethz.idsc.retina.app.blob.vis.ImageBlobRender;

/** loads estimated features from a CSV file and provides functions to run an evaluation.
 * A single evaluation instant is compared in a TrackingEvaluatorInstant object. */
/* package */ class EvaluatorSingleRun {
  private final List<List<ImageBlob>> groundTruthFeatures;
  private final List<List<ImageBlob>> estimatedFeatures;
  private final EvaluatorInstant[] evaluatorInstants;
  private final String logFileName;
  private final File evaluationImagesFilePath;
  private final File handLabelFile;
  private final File estimatedLabelFile;
  private final int[] groundTruthTimeStamps;
  private final boolean saveEvaluationFrame;
  private final int numberOfFiles;
  private String estimatedLabelFileName;
  private float averageRecall;
  private float averagePrecision;
  private int currentLabelInstant = 0;

  EvaluatorSingleRun(BlobTrackConfig pipelineConfig) {
    logFileName = pipelineConfig.davisConfig.logFilename();
    numberOfFiles = ImageEvaluationFolders.HANDLABEL.subfolder(logFileName).list().length;
    evaluationImagesFilePath = ImageEvaluationFolders.EVALUATED.subfolder(logFileName);
    handLabelFile = EvaluationFileLocations.HANDLABEL_CSV.subfolder(pipelineConfig.handLabelFileName.toString());
    groundTruthTimeStamps = EvalUtil.getTimestampsFromImages(numberOfFiles, logFileName);
    groundTruthFeatures = EvalUtil.loadFromCSV(handLabelFile, groundTruthTimeStamps);
    estimatedLabelFileName = pipelineConfig.estimatedLabelFileName.toString();
    estimatedLabelFile = EvaluationFileLocations.ESTIMATED_CSV.subfolder(estimatedLabelFileName);
    estimatedFeatures = EvalUtil.loadFromCSV(estimatedLabelFile, groundTruthTimeStamps);
    evaluatorInstants = new EvaluatorInstant[numberOfFiles];
    for (int i = 0; i < numberOfFiles; ++i)
      evaluatorInstants[i] = new EvaluatorInstant(pipelineConfig, groundTruthFeatures.get(i), estimatedFeatures.get(i));
    saveEvaluationFrame = pipelineConfig.saveEvaluationFrame;
  }

  public void runEvaluation() {
    for (int i = 0; i < numberOfFiles; ++i) {
      currentLabelInstant = i;
      evaluatorInstants[i].compareFeatures();
      if (saveEvaluationFrame)
        saveEvaluationFrame();
    }
    computePerformance();
  }

  /** accumulatedEventFrame with estimated and ground truth features */
  private void saveEvaluationFrame() {
    BufferedImage rawEventsFrame = loadImage();
    // overlay groundtruthFeatures
    for (int i = 0; i < groundTruthFeatures.get(currentLabelInstant).size(); ++i)
      ImageBlobRender.of(rawEventsFrame.createGraphics(), groundTruthFeatures.get(currentLabelInstant).get(i), Color.GREEN);
    // overlay estimatedFeatures
    for (int i = 0; i < estimatedFeatures.get(currentLabelInstant).size(); ++i)
      ImageBlobRender.of(rawEventsFrame.createGraphics(), estimatedFeatures.get(currentLabelInstant).get(i), Color.RED);
    SaveFrame.of(rawEventsFrame, evaluationImagesFilePath, logFileName, currentLabelInstant + 1, groundTruthTimeStamps[currentLabelInstant]);
  }

  /** @return hand-labeled image of the current evaluation instant */
  private BufferedImage loadImage() {
    // load matching accumulatedEventFrame (very similar as in HandLabeler)
    String imgNumberString = String.format("%04d", currentLabelInstant + 1);
    String fileName = logFileName + "_" + imgNumberString + "_" + groundTruthTimeStamps[currentLabelInstant] + ".png";
    File pathToFile = new File(ImageEvaluationFolders.HANDLABEL.subfolder(logFileName), fileName);
    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_INDEXED);
    try {
      bufferedImage = ImageIO.read(pathToFile);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    return bufferedImage;
  }

  private void computePerformance() {
    for (int i = 0; i < numberOfFiles; ++i) {
      averageRecall += evaluatorInstants[i].getRecall() / numberOfFiles;
      averagePrecision += evaluatorInstants[i].getPrecision() / numberOfFiles;
    }
  }

  public double[] getResults() {
    return new double[] { averageRecall, averagePrecision };
  }

  // standalone application
  public static void main(String[] args) {
    BlobTrackConfig pipelineConfig = new BlobTrackConfig();
    EvaluatorSingleRun test = new EvaluatorSingleRun(pipelineConfig);
    test.runEvaluation();
  }
}

// code by vc
package ch.ethz.idsc.retina.app.area;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import ch.ethz.idsc.gokart.core.perc.ClusterConfig;
import ch.ethz.idsc.gokart.core.perc.LidarClustering;
import ch.ethz.idsc.gokart.core.perc.LinearPredictor;
import ch.ethz.idsc.gokart.core.slam.LocalizationConfig;
import ch.ethz.idsc.gokart.gui.top.ObstacleClusterTrackingRender;
import ch.ethz.idsc.owl.bot.util.RegionRenders;
import ch.ethz.idsc.owl.gui.RenderInterface;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.owl.math.region.ImageRegion;
import ch.ethz.idsc.retina.app.cluster.ClusterCollection;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.HomeDirectory;

/* package */ class ClusterAreaEvaluationListener {
  static final File DIRECTORY_CLUSTERS = HomeDirectory.Pictures("clusters");
  private static final File DIRECTORY_PF = HomeDirectory.Pictures("pf");
  private static final Tensor MODEL2PIXEL = Tensors.matrix(new Number[][] { //
      { 15, 0, -320 }, //
      { 0, -15, 960 }, //
      { 0, 0, 1 }, //
  });
  /** image height and width */
  private static final int SIZE = 640;
  private final ClusterCollection collection = new ClusterCollection();
  private int count = 0;
  private double recallAveragedLP = 0;
  private double precisionAveragedLP = 0;
  private double precisionAveragedSP = 0;
  private double recallAveragedSP = 0;
  private double perfAveragedLP = 0;
  private double perfAveragedSP = 0;
  private double noiseAveraged = 0;
  public final LidarClustering lidarClustering;
  private final double side = 0.04;
  private final ObstacleClusterTrackingRender octr;

  public ClusterAreaEvaluationListener(ClusterConfig clusterConfig) {
    ImageRegion imageRegion = LocalizationConfig.GLOBAL.getPredefinedMap().getImageRegion();
    RenderInterface create = RegionRenders.create(imageRegion);
    lidarClustering = new LidarClustering(clusterConfig, collection) {
      private LinearPredictor linearPredictor;

      @Override
      public void anteScan() {
        linearPredictor = new LinearPredictor(collection);
      }

      @Override
      public void postScan(Tensor newScan, double noiseRatio) {
        SimplePredictor simplePredictor = new SimplePredictor(collection);
        Tensor hullsSP = simplePredictor.getHullPredictions();
        Tensor meansSP = simplePredictor.getMeanPredictions();
        Tensor hullsLP = linearPredictor.getHullPredictions();
        Tensor meansLP = linearPredictor.getMeanPredictions();
        double evaluatePerformanceSP = StaticHelper.evaluatePerformance(meansSP, hullsSP);
        double evaluatePerformanceLP = StaticHelper.evaluatePerformance(meansLP, hullsSP);
        PerformanceMeasures measuresSP = recallPrecision(hullsSP, newScan);
        PerformanceMeasures measuresLP = recallPrecision(hullsLP, newScan);
        // update average values for performance, recall and precision
        if (5 < count) {
          noiseAveraged = averageValue(noiseAveraged, noiseRatio);
          if (Double.isFinite(evaluatePerformanceLP))
            perfAveragedLP = averageValue(perfAveragedLP, evaluatePerformanceLP);
          if (Double.isFinite(evaluatePerformanceSP))
            perfAveragedSP = averageValue(perfAveragedSP, evaluatePerformanceSP);
          if (Double.isFinite(measuresLP.recall))
            recallAveragedLP = averageValue(recallAveragedLP, measuresLP.recall);
          if (Double.isFinite(measuresLP.precision))
            precisionAveragedLP = averageValue(precisionAveragedLP, measuresLP.precision);
          if (Double.isFinite(measuresSP.recall))
            recallAveragedSP = averageValue(recallAveragedSP, measuresSP.recall);
          if (Double.isFinite(measuresSP.precision))
            precisionAveragedSP = averageValue(precisionAveragedSP, measuresSP.precision);
          if (count == 80) {
            try {
              DIRECTORY_PF.mkdir();
              Export.of(new File(DIRECTORY_PF, //
                  String.format("epsilon%fminPoints%d.csv", clusterConfig.epsilon.Get().number().doubleValue(), //
                      clusterConfig.minPoints.Get().number().intValue())), //
                  Tensors.of(
                      Tensors.fromString(
                          "{Average perf SP, Average perf LP, Average recall SP, Average recall LP, Average precision SP, Average precision LP, Noise ratio}"), //
                      Tensors.vectorDouble(perfAveragedSP, perfAveragedLP, recallAveragedSP, recallAveragedLP, //
                          precisionAveragedSP, precisionAveragedLP, noiseRatio)));
            } catch (Exception exception) {
              exception.printStackTrace();
            }
          }
        }
        count++;
        System.out.println(count);
        GeometricLayer geometricLayer = new GeometricLayer(MODEL2PIXEL, Array.zeros(3));
        BufferedImage bufferedImage = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_BGR);
        Graphics2D graphics2d = bufferedImage.createGraphics();
        graphics2d.setColor(Color.WHITE);
        graphics2d.fillRect(0, 0, SIZE, SIZE);
        create.render(geometricLayer, graphics2d);
        octr.render(geometricLayer, graphics2d);
        try {
          DIRECTORY_CLUSTERS.mkdir();
          ImageIO.write(bufferedImage, "png", new File(DIRECTORY_CLUSTERS, String.format("clusters%04d.png", count)));
        } catch (Exception exception) {
          exception.printStackTrace();
        }
      }
    };
    octr = new ObstacleClusterTrackingRender(lidarClustering);
  }

  public double averageValue(double old, double newValue) {
    return (old * count + newValue) / (count + 1);
  }

  public PerformanceMeasures recallPrecision(Tensor predictedShapes, Tensor newScan) {
    EnlargedPoints enlargedPoints = new EnlargedPoints(newScan, side);
    EnlargedPoints predictedAreas = new EnlargedPoints(predictedShapes);
    Area ep = predictedAreas.getArea();
    ep.intersect(enlargedPoints.getArea());
    double areaIntersection = AreaMeasure.of(ep);
    return new PerformanceMeasures( //
        areaIntersection / enlargedPoints.getTotalArea(), //
        areaIntersection / predictedAreas.getTotalArea());
  }
}
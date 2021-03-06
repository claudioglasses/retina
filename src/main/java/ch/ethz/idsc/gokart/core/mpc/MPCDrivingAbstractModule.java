// code by mh
package ch.ethz.idsc.gokart.core.mpc;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import ch.ethz.idsc.gokart.core.man.ManualConfig;
import ch.ethz.idsc.gokart.core.track.BSplineTrack;
import ch.ethz.idsc.gokart.core.track.BSplineTrackLcmClient;
import ch.ethz.idsc.gokart.core.track.BSplineTrackListener;
import ch.ethz.idsc.gokart.dev.led.LEDSocket;
import ch.ethz.idsc.gokart.dev.linmot.LinmotSocket;
import ch.ethz.idsc.gokart.dev.rimo.RimoSocket;
import ch.ethz.idsc.gokart.dev.steer.SteerSocket;
import ch.ethz.idsc.retina.joystick.ManualControlInterface;
import ch.ethz.idsc.retina.joystick.ManualControlProvider;
import ch.ethz.idsc.retina.util.math.Magnitude;
import ch.ethz.idsc.retina.util.sys.AbstractModule;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.Timing;
import ch.ethz.idsc.tensor.red.Max;

/** super class of
 * {@link MPCDrivingDynamicModule}
 * {@link MPCDrivingKinematicModule} */
public abstract class MPCDrivingAbstractModule extends AbstractModule implements //
    BSplineTrackListener, Runnable {
  private final List<BSplineTrackLcmClient> bSplineTrackLcmClients = Arrays.asList( //
      BSplineTrackLcmClient.string(), //
      BSplineTrackLcmClient.cyclic());
  private final MPCRequestPublisher mpcRequestPublisher;
  private final MPCControlUpdateLcmClient mpcControlUpdateLcmClient = new MPCControlUpdateLcmClient();
  private final MPCOptimizationConfig mpcOptimizationConfig = MPCOptimizationConfig.GLOBAL;
  // private final MPCBraking mpcBraking = new MPCSimpleBraking();
  // private final MPCBraking mpcBraking = new MPCAggressiveTorqueVectoringBraking();
  private final MPCAggressiveCorrectedTorqueVectoringBraking mpcBraking = //
      new MPCAggressiveCorrectedTorqueVectoringBraking();
  private final MPCStateEstimationProvider mpcStateEstimationProvider;
  private final Thread thread = new Thread(this);
  private final MPCPreviewableTrack track;
  private final ManualControlProvider manualControlProvider = ManualConfig.GLOBAL.getProvider();
  private final MPCSteerProvider mpcSteerProvider;
  private final MpcLedProvider mpcLEDProvider;
  // ---
  final MPCRimoProvider mpcRimoProvider;
  final MPCLinmotProvider mpcLinmotProvider;
  // ---
  private boolean running = true;
  private Optional<BSplineTrack> mpcBSplineTrack = Optional.empty();

  /** create Module with standard estimator
   * 
   * @param mpcRequestPublisher
   * @param timing */
  MPCDrivingAbstractModule(MPCRequestPublisher mpcRequestPublisher, Timing timing) {
    this(mpcRequestPublisher, //
        new SimpleDynamicMPCStateEstimationProvider(timing), // the use of "dynamic" is intended
        timing, null);
  }

  /** Hint: constructor only for testing
   * create Module with custom estimator
   * 
   * @param mpcRequestPublisher
   * @param mpcStateEstimationProvider the custom estimator
   * @param timing that shows the same time that also was used for the custom estimator
   * @param track */
  MPCDrivingAbstractModule( //
      MPCRequestPublisher mpcRequestPublisher, //
      MPCStateEstimationProvider mpcStateEstimationProvider, Timing timing, MPCPreviewableTrack track) {
    this.mpcRequestPublisher = mpcRequestPublisher;
    this.mpcStateEstimationProvider = mpcStateEstimationProvider;
    this.track = track;
    // link mpc steering
    // MPCPower mpcPower = new MPCTorqueVectoringPower(mpcSteering);
    MPCSteering mpcSteering = new MPCOpenLoopSteering();
    MPCPower mpcPower = createPower(mpcStateEstimationProvider, mpcSteering);
    mpcRimoProvider = new MPCRimoProvider(timing, mpcPower);
    mpcLinmotProvider = new MPCLinmotProvider(timing, mpcBraking);
    mpcSteerProvider = new MPCSteerProvider(timing, mpcSteering, torqueBased());
    mpcLEDProvider = new MpcLedProvider(timing, mpcSteering);
    // link mpc steering
    mpcControlUpdateLcmClient.addListener(mpcSteering);
    mpcControlUpdateLcmClient.addListener(mpcPower);
    mpcControlUpdateLcmClient.addListener(mpcBraking);
  }

  private void requestControl() {
    MPCOptimizationParameter mpcOptimizationParameter = //
        createOptimizationParameter(mpcOptimizationConfig, manualControlProvider.getManualControl());
    mpcRequestPublisher.publishOptimizationParameter(mpcOptimizationParameter);
    // send the newest state and start the update state
    GokartState gokartState = mpcStateEstimationProvider.getState();
    Tensor safetyRadiusPosition = gokartState.getCenterPosition();
    MPCPathParameter mpcPathParameter = null;
    MPCPreviewableTrack liveTrack = mpcBSplineTrack.map(MPCBSplineTrack::new).orElse(null);
    Scalar padding = MPCOptimizationConfig.GLOBAL.padding;
    Scalar qpFactor = MPCOptimizationConfig.GLOBAL.qpFactor;
    Scalar qpLimit = MPCOptimizationConfig.GLOBAL.qpLimit;
    final int previewSize = MPCNative.SPLINE_PREVIEW_SIZE;
    if (Objects.nonNull(track))
      mpcPathParameter = track.getPathParameterPreview(previewSize, safetyRadiusPosition, padding, qpFactor, qpLimit);
    else if (Objects.nonNull(liveTrack))
      mpcPathParameter = liveTrack.getPathParameterPreview(previewSize, safetyRadiusPosition, padding, qpFactor, qpLimit);
    if (Objects.nonNull(mpcPathParameter))
      mpcRequestPublisher.publishControlRequest(gokartState, mpcPathParameter);
    else
      System.out.println("No Track to drive on!");
  }

  @Override // from AbstractModule
  protected final void first() {
    bSplineTrackLcmClients.forEach(bSplineTrackLcmClient -> bSplineTrackLcmClient.addListener(this));
    bSplineTrackLcmClients.forEach(BSplineTrackLcmClient::startSubscriptions);
    // ---
    mpcControlUpdateLcmClient.startSubscriptions();
    mpcStateEstimationProvider.first();
    // ---
    SteerSocket.INSTANCE.addPutProvider(mpcSteerProvider);
    RimoSocket.INSTANCE.addPutProvider(mpcRimoProvider);
    LinmotSocket.INSTANCE.addPutProvider(mpcLinmotProvider);
    LEDSocket.INSTANCE.addPutProvider(mpcLEDProvider);
    // ---
    mpcBraking.start();
    mpcControlUpdateLcmClient.addListener(new MPCControlUpdateInterrupt(thread));
    thread.start();
    // ---
    System.out.println("Scheduling Timer: start");
  }

  @Override // from AbstractModule
  protected final void last() {
    System.out.println("cancel timer: ending");
    running = false;
    thread.interrupt();
    // ---
    LinmotSocket.INSTANCE.removePutProvider(mpcLinmotProvider);
    SteerSocket.INSTANCE.removePutProvider(mpcSteerProvider);
    RimoSocket.INSTANCE.removePutProvider(mpcRimoProvider);
    LEDSocket.INSTANCE.removePutProvider(mpcLEDProvider);
    // ---
    mpcBraking.stop();
    mpcControlUpdateLcmClient.stopSubscriptions();
    mpcStateEstimationProvider.last();
    // ---
    bSplineTrackLcmClients.forEach(BSplineTrackLcmClient::stopSubscriptions);
  }

  /***************************************************/
  @Override // from BSplineTrackListener
  public final void bSplineTrack(Optional<BSplineTrack> optional) {
    System.out.println("kinematic mpc bspline track, present=" + optional.isPresent());
    this.mpcBSplineTrack = optional;
  }

  @Override // from Runnable
  public final void run() {
    while (running) {
      requestControl();
      try {
        Thread.sleep(Magnitude.MILLI_SECOND.toLong(mpcOptimizationConfig.updateCycle));
      } catch (InterruptedException e) {
        // sleep is interrupted once data arrives
      }
    }
    System.out.println("Thread terminated");
  }

  protected static Function<ManualControlInterface, Scalar> toMPCmaxSpeed(Scalar minSpeed, Scalar maxSpeed) {
    return manualControlInterface -> {
      Scalar forward = manualControlInterface.getAheadPair_Unit().Get(1);
      return Max.of(minSpeed, maxSpeed.multiply(forward));
    };
  }

  /***************************************************/
  /** @param mpcOptimizationConfig non-null
   * @param optional
   * @return */
  abstract MPCOptimizationParameter createOptimizationParameter( //
      MPCOptimizationConfig mpcOptimizationConfig, //
      Optional<ManualControlInterface> optional);

  /** @param mpcStateEstimationProvider
   * @param mpcSteering
   * @return */
  abstract MPCPower createPower(MPCStateEstimationProvider mpcStateEstimationProvider, MPCSteering mpcSteering);

  abstract boolean torqueBased();
}

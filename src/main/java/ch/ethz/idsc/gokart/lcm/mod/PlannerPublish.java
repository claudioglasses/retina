// code by ynager
package ch.ethz.idsc.gokart.lcm.mod;

import java.util.List;

import ch.ethz.idsc.gokart.lcm.ArrayFloatBlob;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owl.math.state.TrajectorySample;
import ch.ethz.idsc.tensor.Tensor;
import idsc.BinaryBlob;
import lcm.lcm.LCM;

/**
 * 
 */
public enum PlannerPublish {
  ;
  // TODO JPH encoding not final: node info may be sufficient, flow not considered yet
  public static void trajectory(String channel, List<TrajectorySample> trajectory) {
    Tensor tensor = Tensor.of(trajectory.stream() //
        .map(TrajectorySample::stateTime) //
        .map(StateTime::joined));
    BinaryBlob binaryBlob = ArrayFloatBlob.encode(tensor);
    LCM.getSingleton().publish(channel, binaryBlob);
  }
}

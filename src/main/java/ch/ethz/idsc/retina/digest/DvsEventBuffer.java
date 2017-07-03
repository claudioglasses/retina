// code by jph
package ch.ethz.idsc.retina.digest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import ch.ethz.idsc.retina.core.DvsEvent;

/**
 * 
 */
public class DvsEventBuffer implements DvsEventDigest {
  private final int window_us;
  private final Queue<DvsEvent> queue = new LinkedList<>();

  public DvsEventBuffer(int window_us) {
    this.window_us = window_us;
  }

  @Override
  public void digest(DvsEvent dvsEvent) {
    long limit = dvsEvent.time_us - window_us;
    while (!queue.isEmpty())
      if (queue.peek().time_us <= limit)
        queue.remove();
      else
        break;
    queue.add(dvsEvent);
  }

  public Collection<DvsEvent> collection() {
    return Collections.unmodifiableCollection(queue);
  }

  public int size() {
    return queue.size();
  }

  public int window_us() {
    return window_us;
  }
}

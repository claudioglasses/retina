// code by mg
package ch.ethz.idsc.demo.mg.slam.algo;

import java.util.Objects;

import ch.ethz.idsc.demo.mg.slam.SlamContainer;

/** update of occurrence map using the particles */
/* package */ class SlamOccurrenceMapStep extends EventActionSlamStep {
  private final int relevantParticles;

  protected SlamOccurrenceMapStep(SlamContainer slamContainer, int relevantParticles) {
    super(slamContainer);
    this.relevantParticles = relevantParticles;
  }

  @Override
  void davisDvsAction() {
    if (Objects.nonNull(slamContainer.getEventGokartFrame()))
      SlamOccurrenceMapStepUtil.updateOccurrenceMap( //
          slamContainer.getSlamParticles(), //
          slamContainer.getOccurrenceMap(), //
          slamContainer.getEventGokartFrame(), //
          relevantParticles);
  }
}
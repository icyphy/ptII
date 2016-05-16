/* Statistics implementation for maintaining counts and timings of actor iterations.

Copyright (c) 2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY
*/
package org.ptolemy.testsupport.statistics;

import ptolemy.actor.ActorFiringListener;
import ptolemy.actor.FiringEvent;
import ptolemy.kernel.ComponentEntity;

/**
 * Statistics implementation for maintaining counts and timings of actor iterations.
 *
 * @author ErwinDL
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (ErwinDL)
 * @Pt.AcceptedRating Red (ErwinDL)
 */
public class ActorStatistics implements NamedStatistics, ActorFiringListener {

  /**
   * Create a statistics instance associated with the given actor.
   *
   * @param actor
   */
  public ActorStatistics(ComponentEntity<?> actor) {
    this._actor = actor;
  }

  @Override
  public void firingEvent(FiringEvent event) {
    if (_actor.equals(event.getActor())) {
      FiringEvent fe = (FiringEvent) event;
      if (FiringEvent.BEFORE_FIRE.equals(fe.getType())) {
        beginCycle();
      } else if (FiringEvent.AFTER_FIRE.equals(fe.getType())) {
        endCycle();
      }
    }
  }

  @Override
  public String getName() {
    return _actor.getFullName();
  }

  /**
   * Log the count and timing of an actor iteration start.
   */
  public void beginCycle() {
    _cycleStatistics.acceptCycleBegin();
  }

  /**
   * Log the count and timing of an actor iteration end.
   */
  public void endCycle() {
    _cycleStatistics.acceptCycleEnd();
  }

  /**
   * @return the number of actor iterations/cycles until now.
   */
  public long getNrCycles() {
    return _cycleStatistics.getNrCycles();
  }

  /**
   * reset all statistics to 0
   */
  public void reset() {
    _cycleStatistics.reset();
  }

  /**
   * @return the average cycle time of the associated actor's iterations
   */
  public long getAvgCycleTime() {
    return _cycleStatistics.getAvgProcessingTime();
  }

  /**
   * @return the average idle time between the associated actor's iterations
   */
  public long getAvgIdleTime() {
    return _cycleStatistics.getAvgIdleTime();
  }

  /**
   * @return the minimum cycle time of the associated actor's iterations
   */
  public long getMinCycleTime() {
    return _cycleStatistics.getMinProcessingTime();
  }

  /**
   * @return the minimum idle time between the associated actor's iterations
   */
  public long getMinIdleTime() {
    return _cycleStatistics.getMinIdleTime();
  }

  /**
   * @return the maximum cycle time of the associated actor's iterations
   */
  public long getMaxCycleTime() {
    return _cycleStatistics.getMaxProcessingTime();
  }

  /**
   * @return the maximum idle time between the associated actor's iterations
   */
  public long getMaxIdleTime() {
    return _cycleStatistics.getMaxIdleTime();
  }

  // private properties

  private ComponentEntity<?> _actor;

  private CycleStatistics _cycleStatistics = new CycleStatistics();

}

/* This is a helper class for execution aspects.

@Copyright (c) 2008-2018 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
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
package ptolemy.actor;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.ExecutionAspectListener.ExecutionEventType;
import ptolemy.actor.util.Time;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
  This is a helper class for execution aspects used in AtomicExecutionAspect and
  CompositeExecutionAspect.
  @author Patricia Derler
  @version $Id$
  @since Ptolemy II 10.0
  @Pt.ProposedRating Red (derler)
  @Pt.AcceptedRating Red (derler)
 */
public class ExecutionAspectHelper {

    /** Execution aspects are decorators and this method recursively computes
     *  all entities inside a given container that are decorated by execution aspects.
     *  @param container The container.
     *  @return All entities to decorate.
     */
    public static List<NamedObj> getEntitiesToDecorate(
            CompositeEntity container) {
        List<NamedObj> toDecorate = new ArrayList<NamedObj>();
        for (Object entity : container.entityList(ComponentEntity.class)) {
            if (!(entity instanceof ActorExecutionAspect)) {
                toDecorate.add((NamedObj) entity);
                if (entity instanceof CompositeEntity) {
                    toDecorate.addAll(ExecutionAspectHelper
                            .getEntitiesToDecorate((CompositeEntity) entity));
                }
            }
        }
        return toDecorate;
    }

    /** Schedule an actor for execution on an aspect and return the next time
     *  this aspect has to perform an action. Derived classes
     *  must implement this method to actually schedule actors, this
     *  base class implementation just creates events for aspect
     *  activity that is displayed in the plotter. This
     *  base class implementation just creates events for aspect
     *  activity that is displayed in the plotter.
     *  @param aspect The aspect.
     *  @param actor The actor to be scheduled.
     *  @param environmentTime The current platform time.
     *  @param deadline The deadline timestamp of the event to be scheduled.
     *  This can be the same as the environmentTime.
     *  @return Relative time when this aspect has to be executed
     *    again to perform rescheduling actions.
     *  @exception IllegalActionException Thrown if actor parameters such
     *    as execution time or priority cannot be read.
     */
    public static Time schedule(ActorExecutionAspect aspect, NamedObj actor,
            Time environmentTime, Time deadline) throws IllegalActionException {
        Director director = ((CompositeActor) ((ComponentEntity) aspect)
                .getContainer()).getDirector();
        double executionTime = aspect.getExecutionTime(actor);
        aspect.notifyExecutionListeners(((NamedObj) aspect),
                environmentTime.getDoubleValue(), ExecutionEventType.START);
        aspect.notifyExecutionListeners(((NamedObj) aspect),
                environmentTime.getDoubleValue(), ExecutionEventType.STOP);
        return aspect.schedule(actor, environmentTime, deadline,
                new Time(director, executionTime));
    }

}

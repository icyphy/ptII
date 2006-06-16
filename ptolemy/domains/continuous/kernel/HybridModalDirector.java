/* An HybridModalDirector governs the execution of the discrete dynamics of a
 hybrid system model.

 Copyright (c) 2005-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.domains.continuous.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.domains.fsm.kernel.ModalDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// HybridModalDirector

/**
 An HybridModalDirector governs the execution of the discrete dynamics of a hybrid
 system model. It extends ModalDirector by implementing the ContinuousStatefulComponent
 and ContinuousStepSizeController interfaces by delegating the function of those
 interfaces to the currently active state refinement.
 <p>
 This director is based on HSFSMDirector by Xiaojun Liu and Haiyang Zheng.
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (liuxj)
 */
public class HybridModalDirector extends ModalDirector
        implements ContinuousStatefulComponent, ContinuousStepSizeController {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public HybridModalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if all actors that were fired in the last invocation
     *  of the fire() method since the last invocation of the prefire() method
     *  report that the step size is accurate.
     *  @return True if the current step is accurate.
     */
    public boolean isStepSizeAccurate() {
        Iterator actors = _actorsFired.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStepSizeController) {
                if (!((ContinuousStepSizeController) actor).isStepSizeAccurate()) {
                    return false;
                }
            } else if (actor instanceof CompositeActor) {
                Iterator insideActors = ((CompositeActor)actor).deepEntityList().iterator();
                while (insideActors.hasNext()) {
                    Actor insideActor = (Actor)insideActors.next();
                    if (insideActor instanceof ContinuousStepSizeController) {
                        if (!((ContinuousStepSizeController) insideActor).isStepSizeAccurate()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** Return the minimum of the step sizes suggested by any
     *  actors that were fired in the last invocation
     *  of the fire() method since the last invocation of the prefire() method.
     *  @return The suggested next step size.
     */
    public double suggestedStepSize() {
        double result = Double.POSITIVE_INFINITY;
        Iterator actors = _actorsFired.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStepSizeController) {
                double candidate = ((ContinuousStepSizeController) actor).suggestedStepSize();
                if (candidate < result) {
                    result = candidate;
                }
            } else if (actor instanceof CompositeActor) {
                Iterator insideActors = ((CompositeActor)actor).deepEntityList().iterator();
                while (insideActors.hasNext()) {
                    Actor insideActor = (Actor)insideActors.next();
                    if (insideActor instanceof ContinuousStepSizeController) {
                        double candidate = ((ContinuousStepSizeController) insideActor).suggestedStepSize();
                        if (candidate < result) {
                            result = candidate;
                        }
                    }
                }
            }
        }
        return result;
    }

    /** Return the minimum of the step sizes suggested by any
     *  actors that were fired in the last invocation
     *  of the fire() method since the last invocation of the prefire() method.
     *  @return The suggested refined step size.
     *  @throws IllegalActionException If the step size cannot be further refined.
     */
    public double refinedStepSize() throws IllegalActionException {
        double result = Double.POSITIVE_INFINITY;
        Iterator actors = _actorsFired.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStepSizeController) {
                double candidate = ((ContinuousStepSizeController) actor).refinedStepSize();
                if (candidate < result) {
                    result = candidate;
                }
            } else if (actor instanceof CompositeActor) {
                Iterator insideActors = ((CompositeActor)actor).deepEntityList().iterator();
                while (insideActors.hasNext()) {
                    Actor insideActor = (Actor)insideActors.next();
                    if (insideActor instanceof ContinuousStepSizeController) {
                        double candidate = ((ContinuousStepSizeController) insideActor).refinedStepSize();
                        if (candidate < result) {
                            result = candidate;
                        }
                    }
                }
            }
        }
        return result;
    }

    /** Roll back to committed state.
     *  This will roll back any actors that were fired in the last invocation
     *  of the fire() method since the last invocation of the prefire() method.
     */
    public void rollBackToCommittedState() {
        Iterator actors = _actorsFired.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStatefulComponent) {
                ((ContinuousStatefulComponent) actor).rollBackToCommittedState();
            } else if (actor instanceof CompositeActor) {
                Iterator insideActors = ((CompositeActor)actor).deepEntityList().iterator();
                while (insideActors.hasNext()) {
                    Actor insideActor = (Actor)insideActors.next();
                    if (insideActor instanceof ContinuousStatefulComponent) {
                        ((ContinuousStatefulComponent) insideActor).rollBackToCommittedState();
                    }
                }
            }
        }
    }
}

/* This is a resource scheduler Ptolemy model.

@Copyright (c) 2008-2012 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/** This is a resource scheduler Ptolemy model. Special naming
 *  conventions are used to connect the functional model to the
 *  scheduler model.
 *
 * @author Patricia Derler
   @version $Id$
   @since Ptolemy II 0.2

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class SchedulerModel extends ResourceScheduler {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public SchedulerModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _model = new TypedCompositeActor(workspace());
        // FIXME: How can i set the top level as a container for this model?
        //((TypedCompositeActor)_model).setContainer((CompositeEntity) container.getContainer());
    }

    /** Initialize local variables.
     * @exception IllegalActionException Thrown if list of actors
     *   scheduled by this scheduler cannot be retrieved.
     */
    @Override
    public Time initialize() throws IllegalActionException {
        super.initialize();
        ((TypedCompositeActor) _model).preinitialize();
        ((TypedCompositeActor) _model).initialize();
        ((TypedCompositeActor) _model).getDirector().setEmbedded(true);

        // FIXME: How can I do automatic type checking?
        // This ((TypedCompositeActor)_model).resolveTypes((TypedCompositeActor) _model);
        // works for simple cases, but when arrays are involved,
        // the model asks for a manager. How can I give the model
        // a manager?
        //((CompositeActor)this.getContainer()).getManager().
        //((TypedCompositeActor)_model).setManager(((CompositeActor)this.getContainer()).getManager());
        //((TypedCompositeActor)_model).setManager(new Manager());
        //        try {
        //            ((TypedCompositeActor)_model).resolveTypes((TypedCompositeActor) _model);
        //        } catch (TypeConflictException e) {
        //            // TODO Auto-generated catch block
        //            throw new IllegalActionException(this, e.getMessage());
        //        }
        _currentlyExecuting = new ArrayList();
        return ((CompositeActor) _model).getDirector()
                .getModelNextIterationTime();
    }

    /** Schedule a new actor for execution. Find the const
     *  actor in the _model that is mapped to this actor and
     *  trigger a firing of that one, if the actor is not
     *  already in execution. If the actor finished execution,
     *  return zero time, otherwise return the next time the
     *  model has something to do.
     *  @param actor The actor to be scheduled.
     *  @param currentPlatformTime The current platform time.
     *  @param deadline. The deadline of the event.
     *  @param executionTime The execution time of the actor.
     *  @return Relative time when this Scheduler has to be executed
     *    again.
     *  @exception IllegalActionException Thrown if actor paramaters such
     *    as execution time or priority cannot be read.
     */
    @Override
    public Time schedule(Actor actor, Time currentPlatformTime,
            Double deadline, Time executionTime) throws IllegalActionException {
        super.schedule(actor, currentPlatformTime, deadline, executionTime);
        boolean finished = false;
        Time time = null;

        if (!_currentlyExecuting.contains(actor)) {
            _currentlyExecuting.add(actor);
            event((NamedObj) actor, currentPlatformTime.getDoubleValue(),
                    ExecutionEventType.START);
            Actor mappedActor = _getActor(actor, "");
            if (mappedActor != null) {
                ((CompositeActor) _model).getDirector().setModelTime(
                        currentPlatformTime);
                ((CompositeActor) _model).getDirector().fireAtCurrentTime(
                        mappedActor);
            }
        }
        ((CompositeActor) _model).getDirector().setModelTime(
                currentPlatformTime);
        _fireModel(currentPlatformTime);

        Parameter parameter = (Parameter) ((CompositeActor) _model)
                .getAttribute("resume" + actor.getName());
        finished = ((BooleanToken) parameter.getToken()).booleanValue();
        if (finished) {
            time = getTime(0.0);
            parameter.setToken(new BooleanToken(false));
            _lastActorFinished = true;
            _currentlyExecuting.remove(actor);
            event((NamedObj) actor, currentPlatformTime.getDoubleValue(),
                    ExecutionEventType.STOP);
        } else {
            time = ((CompositeActor) _model).getDirector()
                    .getModelNextIterationTime().subtract(currentPlatformTime);
            _lastActorFinished = false;
        }
        return time;
    }

    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        ((CompositeActor) _model).wrapup();
    }

    /** List of currently executing actors. */
    protected List<Actor> _currentlyExecuting;

    private void _fireModel(Time currentPlatformTime)
            throws IllegalActionException {
        Time time = currentPlatformTime;
        int index = 1;
        while (time.equals(currentPlatformTime)) {
            ((DEDirector) ((CompositeActor) _model).getDirector())
                    .setIndex(index);
            ((CompositeActor) _model).prefire();
            ((CompositeActor) _model).fire();
            ((CompositeActor) _model).postfire();
            time = ((CompositeActor) _model).getDirector()
                    .getModelNextIterationTime();
            index++;
        }
    }

    private Actor _getActor(Actor actor, String suffix) {
        for (int i = 0; i < ((CompositeActor) _model).entityList().size(); i++) {
            Actor mappedActor = (Actor) ((CompositeActor) _model).entityList()
                    .get(i);
            if (mappedActor.getName().equals(actor.getName() + suffix)) {
                return mappedActor;
            }
        }
        return null;
    }

}

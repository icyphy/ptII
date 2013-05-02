/* Director for MetroII compatible Synchronous Reactive semantic.

 Copyright (c) 2012-2013 The Regents of the University of California.
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

package ptolemy.domains.metroII.kernel;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIISRDirector

public class MetroIISRDirector extends SRDirector implements
        MetroIIEventHandler {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MetroIISRDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public MetroIISRDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public MetroIISRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Clone the object into the specified workspace. The new object
     *  is <i>not</i> added to the directory of that workspace (you
     *  must do this yourself if you want it there).
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIISRDirector newObject = (MetroIISRDirector) super
                .clone(workspace);
        newObject._nameToActor = (Hashtable<String, Actor>) _nameToActor
                .clone();
        newObject._events = (ArrayList<Builder>) _events.clone();
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * YieldAdapter interface
     */
    public YieldAdapterIterable<Iterable<Event.Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        try {
            if (_debugging) {
                _debug("MetroIISRDirector: invoking fire().");
            }
            Schedule schedule = getScheduler().getSchedule();
            int iterationCount = 0;
            do {
                Iterator firingIterator = schedule.firingIterator();
                while (firingIterator.hasNext() && !_stopRequested) {
                    Actor actor = ((Firing) firingIterator.next()).getActor();
                    // If the actor has previously returned false in postfire(),
                    // do not fire it.
                    if (!_actorsFinishedExecution.contains(actor)) {
                        // check if the actor is ready to fire.
                        if (_isReadyToFire(actor)) {
                            Event.Builder builder = makeEventBuilder(
                                    actor.getFullName());
                            _nameToActor.put(builder.getName(), actor);
                            _events.add(builder);
                        } else {
                            if (_debugging) {
                                if (!_actorsFinishedFiring.contains(actor)
                                        && actor.isStrict()) {
                                    _debug("Strict actor has uknown inputs: "
                                            + actor.getFullName());
                                }
                            }
                        }
                    } else {
                        // The postfire() method of this actor returned false in
                        // some previous iteration, so here, for the benefit of
                        // connected actors, we need to explicitly call the
                        // send(index, null) method of all of its output ports,
                        // which indicates that a signal is known to be absent.
                        if (_debugging) {
                            _debug("MetroIISRDirector: no longer enabled (return false in postfire): "
                                    + actor.getFullName());
                        }
                        _sendAbsentToAllUnknownOutputsOf(actor);
                    }
                }
                while (_events.size() > 0) {
                    resultHandler.handleResult(_events);
                    ArrayList<Event.Builder> tmp_events = new ArrayList<Event.Builder>();
                    for (Builder etb : _events) {
                        if (etb.getName().contains("Begin") && 
                                etb.getStatus() == Event.Status.NOTIFIED) {
                            Actor actor = _nameToActor.get(etb.getName());
                            if (_debugging) {
                                _debug("Firing " + actor.getFullName());
                            }
                            _fireActor(actor);
                            _actorsFired.add(actor);

                        } else {
                            tmp_events.add(etb);
                        }
                    }
                    _events = tmp_events;
                }
                iterationCount++;
            } while (!_hasIterationConverged() && !_stopRequested);

            if (_debugging) {
                _debug(this.getFullName() + ": Fixed point found after "
                        + iterationCount + " iterations.");
            }
        } catch (IllegalActionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize the object.
     */
    public void _init() {
        _nameToActor = new Hashtable();
        _events = new ArrayList();
    }

    /**
     * Create a MetroII event
     * @param name MetroII event name
     * @param t MetroII event type
     * @param s MetroII event status
     * @return MetroII event builder
     */
    private Event.Builder makeEventBuilder(String name) {
        Event.Builder builder = Event.newBuilder();
        builder.setName(name);
        builder.setStatus(Event.Status.PROPOSED);
        builder.setType(Event.Type.DEFAULT_NOTIFIED);
        return builder;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * Lookup table for actor by MetroII event name
     */
    private Hashtable<String, Actor> _nameToActor = new Hashtable<String, Actor>();

    /**
     * Current MetroII event list
     */
    private ArrayList<Event.Builder> _events = new ArrayList<Event.Builder>();

}

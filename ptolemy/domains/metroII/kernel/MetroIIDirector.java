/* Director for Modified MetroII semantics.

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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIIDirector

/**
 * <p> A MetroII Director governs the execution of a CompositeActor with
 * simplified MetroII semantics. </p>
 *
 * <p> The CompositeActor could contain two types
 * of actors: Ptolemy actors and Metropolis actors. Ptolemy actors are the
 * usual actors in Ptolemy, such as Const, Ramp, Display, etc. Metropolis
 * actor is either a customized Ptolemy actor (e.g. MetroIIModalModel) or a
 * MetroIICompositeActor that contains a MetroII compatible director, (e.g.
 * MetroIISRDirector, MetroIIPNDirector). A MetroIICompositeActor can still
 * contain any Ptolemy actors. The Metropolis actors are supposed to have
 * the identical behaviors as the corresponding Ptolemy actor (e.g.
 * MetroIIModalModel should react the same way as ModalModel does). The
 * difference is the behaviors of Metropolis actor are associated with MetroII
 * events. A MetroII event is in one of the three statuses: PROPOSED, WAITING,
 * NOTIFIED. The reaction of a Metropolis actor is not executed until the
 * associated event is notified. The executions of Metropolis actors are
 * governed by the MetroIIDirector via updating the statuses of events. </p>
 *
 * <p> The semantic has the two phases:
 * <ol>
 * <li> Phase 1: Model Execution </li>
 * <li> Phase 2: Constraint Resolution</li>
 * </ol>
 * In phase 1, the MetroIIDirector calls prefire(), fire(), and postfire()
 * of each Ptolemy actor in the model and calls prefire(), getfire() and
 * postfire() of each Metropolis actor in the model. The getfire() function
 * can be seen as the same as fire() except for some event proposing code
 * inserted in the middle. The execution of getfire() is in a separate thread.
 * When the thread proceeds to the event proposing code, the thread will send
 * a message (a list of events with PROPOSED) to the director on the
 * upper level and then suspend. The thread will not proceed until the status of
 * at least one event in the list is changed to NOTIFIED. </p>
 *
 * <p> In phase 2, the MetroIIDirector collects all the events and updates the
 * event status based on the mapping constraints. A mapping constraint is a
 * rendezvous constraint that requires all the specified events are in the status
 * of PROPOSED or NOTIFIED. If an event satisfies all the constraints, the status
 * will be updated to NOTIFIED, otherwise the status is updated to WAITING. In the
 * next iteration, the Metropolis Actors are executed based on the event statuses. </p>
 *
 * <p> Known issues:
 * <ol>
 * <li> the 'stop execution' may not work properly. </li>
 * </ol>
 * </p>
 *
 * @author Liangpeng Guo
 * @version $ld$
 * @since Ptolemy II 9.1
 * @Pt.ProposeRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */

public class MetroIIDirector extends Director {
    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MetroIIDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _initializeParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A mapping constraint is a pair of events that are rendezvous.
     *  Mapping file is a text file that specifies such constraints.
     *  In mapping file, each line is a mapping constraint, which
     *  contains two event names separated by a space.
     *
     *  _mappingFileName is a string that contains the absolute path of the mapping file.
     *
     *  The default value of _mappingFileName is null, which means no
     *  mapping constraint is specified.
     */
    public FileParameter mappingFileName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute
     *  matches a parameter of the director, then the corresponding
     *  local copy of the parameter value will be updated.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If the parameter set is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mappingFileName) {
            StringToken mappingFileNameToken = (StringToken) mappingFileName
                    .getToken();
            if (mappingFileNameToken == null
                    || mappingFileNameToken.equals("")) {
                mappingFileName = null;
            } else {
                String filename = mappingFileNameToken.stringValue();
                if (!filename.equals("")) {
                    try {
                        readMapping(filename);
                    } catch (IOException ex) {
                        throw new IllegalActionException(this, ex,
                                "Failed to open mapping file \"" + filename + "\".");
                    }
                    if (_debugging) {
                        _debug(_mappingConstraintSolver.toString());
                    }
                }
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
    * Call the prefire(), fire(), and postfire() methods of each actor in the model. If the
    * actor is a CompositeActor that contains a MetroII compatible director, the
    * getfire() method is called instead of fire(). getfire() does everything
    * fire() does and proposes events in firing. When events are proposed, the
    * CompositeActor is blocked, waiting for the resolution of mapping constraints
    * The MetroIIDirector collects all the events and resets the statuses of events
    * based on the mapping constraints. In the next iteration, CompositeActor
    * executes based on the updated statuses of events.
    */
    public void fire() throws IllegalActionException {
        super.fire();
        Nameable container = getContainer();

        Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                .iterator();
        LinkedList<MetroIIActorThread> actorThreadList = new LinkedList<MetroIIActorThread>();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof MetroIIEventHandler) {
                actorThreadList.add(new MetroIIActorThread(actor,
                        MetroIIActorThread.Type.Metropolis,
                        MetroIIActorThread.State.WAITING, null));
            } else {
                actorThreadList.add(new MetroIIActorThread(actor,
                        MetroIIActorThread.Type.Ptolemy,
                        MetroIIActorThread.State.WAITING, null));
            }
        }

        while (!_stopRequested) {
            LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();

            // Phase I: base model execution
            for (MetroIIActorThread actorThread : actorThreadList) {
                if (actorThread.actor.prefire()) {
                    if (actorThread.type == MetroIIActorThread.Type.Metropolis) {
                        if (actorThread.state == MetroIIActorThread.State.WAITING) {
                            // The getfire() of each Metropolis actor is invoked by a separate thread.
                            // Each thread is encapsulated by a YieldAdapterIterable, which is used to iterate
                            // the events proposed by the thread.
                            final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) actorThread.actor)
                                    .adapter();
                            actorThread.thread = results.iterator();
                            actorThread.state = MetroIIActorThread.State.ACTIVE;
                        }
                    } else if (actorThread.type == MetroIIActorThread.Type.Ptolemy) {
                        actorThread.state = MetroIIActorThread.State.ACTIVE;
                    }
                }
            }

            // The thread of getfire() can be seen as a list of events.
            for (MetroIIActorThread actorThread : actorThreadList) {
                if (actorThread.type == MetroIIActorThread.Type.Metropolis
                        && actorThread.state == MetroIIActorThread.State.ACTIVE) {
                    Actor actor = actorThread.actor;
                    Iterator<Iterable<Event.Builder>> thread = actorThread.thread;

                    // Every time hasNext() is called, the thread runs until the next event
                    // is proposed. If any event is proposed, hasNext() returns true.
                    // The proposed event is returned by next().
                    // If the getfire() terminates without proposing event, hasNext()
                    // returns false.
                    if (thread.hasNext()) {
                        Iterable<Event.Builder> result = thread.next();
                        for (Builder builder : result) {
                            Event.Builder eventBuilder = builder;
                            String event_name = eventBuilder.getName();

                            if (!_eventName2ID.containsKey(event_name)) {
                                _eventName2ID.put(event_name, _nextAvailableID);
                                _nextAvailableID++;
                            }

                            eventBuilder.setStatus(Event.Status.WAITING);

                            metroIIEventList.add(eventBuilder);
                        }
                    } else {
                        actorThread.state = MetroIIActorThread.State.WAITING;
                        if (!actor.postfire()) {
                            if (_debugging) {
                                _debug("Actor requests halt: "
                                        + ((Nameable) actor).getFullName());
                            }
                        }
                        if (_stopRequested) {
                            if (_debugging) {
                                _debug("Actor requests halt: "
                                        + ((Nameable) actor).getFullName());
                            }
                        }
                    }
                }
            }

            // Call the fire() function of each Ptolemy actor
            for (MetroIIActorThread actorThread : actorThreadList) {
                if (actorThread.type == MetroIIActorThread.Type.Ptolemy
                        && actorThread.state == MetroIIActorThread.State.ACTIVE) {
                    actorThread.actor.fire();
                    actorThread.state = MetroIIActorThread.State.WAITING;

                    if (!actorThread.actor.postfire()) {
                        if (_debugging) {
                            _debug("Actor requests halt: "
                                    + ((Nameable) actorThread.actor)
                                            .getFullName());
                        }
                    }
                }
            }

            // Phase II: constraint resolution

            // The constraints are resolved in three steps.
            // STEP 1: reset the constraint solver.
            _mappingConstraintSolver.reset();

            // Step 2: present all the proposed events to the event solver.
            for (Event.Builder eventBuilder : metroIIEventList) {
                String eventName = eventBuilder.getName();
                _mappingConstraintSolver.presentMetroIIEvent(_eventName2ID
                        .get(eventName));
            }

            if (_debugging) {
                _debug(_mappingConstraintSolver.toString());
                _debug("Before mapping resolution: ");
                for (Event.Builder eventBuilder : metroIIEventList) {
                    _debug(_eventName2ID.get(eventBuilder.getName())
                            + eventBuilder.getName() + " "
                            + eventBuilder.getStatus().toString());
                }
            }

            // Step 3: update the statuses of all events.
            for (Event.Builder eventBuilder : metroIIEventList) {
                String eventName = eventBuilder.getName();
                if (_mappingConstraintSolver.isSatisfied(_eventName2ID
                        .get(eventName))) {
                    eventBuilder.setStatus(Event.Status.NOTIFIED);
                }
            }
            if (_debugging) {
                _debug("After mapping resolution: ");
                for (Event.Builder eventBuilder : metroIIEventList) {
                    _debug(_eventName2ID.get(eventBuilder.getName())
                            + eventBuilder.getName() + " "
                            + eventBuilder.getStatus().toString());
                }
            }
        }

        if (_stopRequested) {
            for (MetroIIActorThread actorThread : actorThreadList) {
                if (actorThread.type == MetroIIActorThread.Type.Metropolis
                        && actorThread.state == MetroIIActorThread.State.ACTIVE) {
                    actorThread.thread.dispose();
                }
            }
        }
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIIDirector newObject = (MetroIIDirector) super.clone(workspace);
        newObject._mappingConstraintSolver = new MappingConstraintSolver(
                _maxEvent);
        newObject._eventName2ID = new Hashtable<String, Integer>();
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize parameters. This is called by the constructor.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
            NameDuplicationException {
        mappingFileName = new FileParameter(this, "mappingFileName");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /** The next available event ID. If an new event is proposed, the
     *  _nextAvailableID is assigned to the new event and _nextAvailableID
     *  is increased by one.
     */
    private int _nextAvailableID = 0;

    /** The maximum number of events.
     */
    private final int _maxEvent = 1000;

    /** The dictionary of event name and ID pair.
     *
     */
    private Hashtable<String, Integer> _eventName2ID = new Hashtable<String, Integer>();

    /** The constraint solver
     *
     */
    private MappingConstraintSolver _mappingConstraintSolver = new MappingConstraintSolver(
            _maxEvent);

    /**
     * Read constraints from the mapping file.
     * @param filename mapping file
     */
    private void readMapping(String filename) throws IOException {
        FileInputStream stream = new FileInputStream(filename);
        DataInputStream in = new DataInputStream(stream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] actorNames = line.split(",");
                assert actorNames.length == 2;
                if (!_eventName2ID.containsKey(actorNames[0])) {
                    _eventName2ID.put(actorNames[0], _nextAvailableID);
                    _nextAvailableID++;
                }
                if (!_eventName2ID.containsKey(actorNames[1])) {
                    _eventName2ID.put(actorNames[1], _nextAvailableID);
                    _nextAvailableID++;
                }
                _mappingConstraintSolver.add(_eventName2ID.get(actorNames[0]),
                        _eventName2ID.get(actorNames[1]));
            }
        } finally {
            reader.close();
        }
    }

}

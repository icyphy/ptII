/* Director for Modified MetroII semantics.

 Copyright (c) 2012 The Regents of the University of California.
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
import ptolemy.data.expr.Parameter;
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
 * modified MetroII semantics. The semantic has the two phases: </p> 
 * <ol>
 * <li> model execution </li>
 * <li> mapping constraint resolution </li>
 * </ol>
 * <p> In model execution phase, MetroIIDirector tries to call prefire(), 
 * fire(), and postfire() of each actor in the model. If the actor 
 * is a CompositeActor that has a MetroII compatible director, the 
 * getfire() is called instead of fire(). In addition to do everything
 * fire() does, getfire() proposes events in firing. And when proposing
 * events, the CompositeActor is blocked. </p>
 *  
 * <p> In mapping constraint resolution phase, the MetroIIDirector collects
 * all the events and reset the statuses of events based on the 
 * mapping constraints. In the next iteration, CompositeActor executes 
 * based on the updated statuses of events. </p>  
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
    private Parameter _mappingFileName;

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == _mappingFileName) {
            StringToken mappingFileNameToken = (StringToken) _mappingFileName
                    .getToken();

            if (mappingFileNameToken == null) {
                _mappingFileName = null;
            } else {
                try {
                    readMapping(mappingFileNameToken.stringValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (_debugging) {
                    _debug(_mappingConstraintSolver.toString());
                }
            }

        } else {
            super.attributeChanged(attribute);
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
        newObject._mappingConstraintSolver = new MappingConstraintSolver(100);
        newObject._eventName2ID = new Hashtable<String, Integer>();
        return newObject;
    }

    void _init() {
        try {
            _mappingFileName.moveToLast();
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void _initializeParameters() {
        _verbose = true;
        try {
            _mappingFileName = new Parameter(this, "mapping");
            _mappingFileName.setTypeEquals(BaseType.STRING);
        } catch (IllegalActionException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (NameDuplicationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private int _nextAvailID = 0;
    private final int _maxEvent = 1000;
    private Hashtable<String, Integer> _eventName2ID = new Hashtable<String, Integer>();
    private MappingConstraintSolver _mappingConstraintSolver = new MappingConstraintSolver(
            _maxEvent);

    /**    
    * Call prefire(), fire(), and postfire() of each actor in the model. If the 
    * actor is a CompositeActor that contains a MetroII compatible director, the 
    * getfire() is called instead of fire(). getfire() is supposed to do everything
    * fire() does and proposes events in firing. When events are proposed, the 
    * CompositeActor is blocked, waiting for the resolution of mapping constraints
    * The MetroIIDirector collects all the events and reset the statuses of events 
    * based on the mapping constraints. In the next iteration, CompositeActor 
    * executes based on the updated statuses of events.  
    */
    public void fire() throws IllegalActionException {

        try {
            if (_debugging) {
                _debug("Director: Called fire().");
            }

            Nameable container = getContainer();

            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();
            LinkedList<MetroIIActorThread> actor_thread_list = new LinkedList<MetroIIActorThread>();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (actor instanceof MetroIIEventHandler) {
                    actor_thread_list.add(new MetroIIActorThread(actor,
                            MetroIIActorThread.Type.Metropolis,
                            MetroIIActorThread.State.WAITING, null));
                } else {
                    actor_thread_list.add(new MetroIIActorThread(actor,
                            MetroIIActorThread.Type.Ptolemy,
                            MetroIIActorThread.State.WAITING, null));
                }
            }

            boolean stable = false;
            while (!_stopRequested) {
                LinkedList<Event.Builder> m2event_list = new LinkedList<Event.Builder>();
                stable = true;

                // Phase I: base model execution
                for (MetroIIActorThread actor_thread : actor_thread_list) {
                    if (actor_thread._actor.prefire()) {
                        if (actor_thread._type == MetroIIActorThread.Type.Metropolis) {
                            if (actor_thread._state == MetroIIActorThread.State.WAITING) {
                                final YieldAdapterIterable<Iterable<Event.Builder>> results = ((MetroIIEventHandler) actor_thread._actor)
                                        .adapter();
                                actor_thread._thread = results.iterator();
                                actor_thread._state = MetroIIActorThread.State.ACTIVE;
                            }
                        } else if (actor_thread._type == MetroIIActorThread.Type.Ptolemy) {
                            actor_thread._state = MetroIIActorThread.State.ACTIVE;
                        }
                    }
                }

                for (MetroIIActorThread actor_thread : actor_thread_list) {
                    if (actor_thread._type == MetroIIActorThread.Type.Metropolis
                            && actor_thread._state == MetroIIActorThread.State.ACTIVE) {
                        Actor actor = actor_thread._actor;
                        Iterator<Iterable<Event.Builder>> thread = actor_thread._thread;

                        if (thread.hasNext()) {
                            Iterable<Event.Builder> result = thread.next();
                            for (Builder builder : result) {
                                Event.Builder etb = builder;
                                String event_name = etb.getName();

                                if (!_eventName2ID.containsKey(event_name)) {
                                    _eventName2ID.put(event_name, _nextAvailID);
                                    _nextAvailID++;
                                }

                                etb.setStatus(Event.Status.WAITING);

                                m2event_list.add(etb);
                                stable = false;
                            }

                        } else {
                            boolean pfire = actor.postfire();
                            actor_thread._state = MetroIIActorThread.State.WAITING;
                            if (!pfire) {
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

                for (MetroIIActorThread actor_thread : actor_thread_list) {
                    if (actor_thread._type == MetroIIActorThread.Type.Ptolemy
                            && actor_thread._state == MetroIIActorThread.State.ACTIVE) {
                        actor_thread._actor.fire();
                        boolean pfire = actor_thread._actor.postfire();
                        actor_thread._state = MetroIIActorThread.State.WAITING;

                        if (!pfire) {
                            if (_debugging) {
                                _debug("Actor requests halt: "
                                        + ((Nameable) actor_thread._actor)
                                                .getFullName());
                            }
                        }
                    }
                }

                // Phase II: mapping constraint resolution
                for (Event.Builder etb : m2event_list) {
                    String event_name = etb.getName();
                    _mappingConstraintSolver.presentM2Event(_eventName2ID
                            .get(event_name));
                }
                if (_debugging) {
                    _debug(_mappingConstraintSolver.toString());
                    _debug("Before mapping resolution: ");
                    for (Event.Builder etb : m2event_list) {
                        _debug(_eventName2ID.get(etb.getName()) + etb.getName()
                                + " " + etb.getStatus().toString());
                    }
                }
                for (Event.Builder etb : m2event_list) {
                    String event_name = etb.getName();
                    if (_mappingConstraintSolver.isSatisfied(_eventName2ID
                            .get(event_name))) {
                        etb.setStatus(Event.Status.NOTIFIED);
                    }
                }
                if (_debugging) {
                    _debug("After mapping resolution: ");
                    for (Event.Builder etb : m2event_list) {
                        _debug(_eventName2ID.get(etb.getName()) + etb.getName()
                                + " " + etb.getStatus().toString());
                    }
                }
                _mappingConstraintSolver.reset();
            }
            if (_stopRequested) {
                for (MetroIIActorThread actor_thread : actor_thread_list) {
                    if (actor_thread._type == MetroIIActorThread.Type.Metropolis
                            && actor_thread._state == MetroIIActorThread.State.ACTIVE) {
                        actor_thread._thread.dispose();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Read constraints from the mapping file.
     * @param finename mapping file
     */
    private void readMapping(String finename) throws IOException {
        // System.out.println(finename);
        FileInputStream fstream = new FileInputStream(finename);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        try {
            while ((strLine = br.readLine()) != null) {
                String[] actor_name_list = strLine.split(",");
                assert actor_name_list.length == 2;
                if (!_eventName2ID.containsKey(actor_name_list[0])) {
                    _eventName2ID.put(actor_name_list[0], _nextAvailID);
                    _nextAvailID++;
                }
                if (!_eventName2ID.containsKey(actor_name_list[1])) {
                    _eventName2ID.put(actor_name_list[1], _nextAvailID);
                    _nextAvailID++;
                }
                _mappingConstraintSolver.add(
                        _eventName2ID.get(actor_name_list[0]),
                        _eventName2ID.get(actor_name_list[1]));
                System.out.println(strLine);
            }
        } finally {
            in.close();
        }
    }

}

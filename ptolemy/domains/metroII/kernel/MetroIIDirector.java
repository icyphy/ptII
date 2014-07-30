/* A MetroII Director governs the execution of actors with simplified MetroII semantics.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroIIDirector

/**
 * <p>
 * A MetroII Director governs the execution of actors with simplified MetroII
 * execution semantics. The major distinction from other directors is the way
 * MetroIIDirector fires the actors. In stead of explicitly calling fire() of
 * the governing actors, MetroIIDirector implicitly fires actors by exchanging
 * MetroII events with actors (@see
 * ptolemy.domains.metroII.kernel.util.ProtoBuf.Event). Under MetroIIDirector,
 * each actor is wrapped by either BlockingFire (@see BlockingFire) or
 * ResumableFire (@see ResumableFire). With a wrapper, the firing of an actor is
 * a process which executes and then blocks to generate MetroII events. Each
 * MetroII event has one of the following states: PROPOSED, WAITING, and
 * NOTIFIED. When the firing of an actor blocks to generate MetroII events, the
 * events are sent to MetroIIDirector with the state of PROPOSED. This is also
 * referred to as proposing MetroII events. MetroIIDirector delegates the events
 * to constraint solvers (@see ConstraintSolver) which update the event states
 * to either WAITING or NOTIFIED. If any event state of a process is updated to
 * NOTIFIED, the process is supposed to resume execution. The resumed execution
 * may depend on the updated events.
 * </p>
 *
 * <p>
 * The execution of MetroIIDirector has two phases. In Phase 1, MetroIIDirector
 * repeatedly fires each actor (no particular order should be presumed). As
 * mentioned, each firing is a process runs and then blocks to propose MetroII
 * events. Phase 1 ends when all processes of firing are blocked. In Phase 2,
 * MetroIIDirector delegates all the proposed events to constraint solvers (@see
 * ConstraintSolver), which updates the states of MetroII events based on the
 * constraints. In particular, MappingConstraintSolver (MappingConstraintSolver)
 * is a constraint solver that resolves rendezvous constraints. A rendezvous
 * constraint requires the specified pair of MetroII events must be proposed
 * together, otherwise the states will be updated to WAITING. A collection of
 * two completed phases is referred to as an iteration. After constraint
 * resolving in Phase 2, MetroIIDirector goes back to Phase 1, in which each
 * existing process has a chance to react to the updated MetroII events. The
 * process with at least one NOTIFIED event is supposed to resume execution
 * while the process with all events WAITING keeps blocked. If the process of
 * firing successfully completes in last iteration, a new process will be
 * created as long as prefire() returns true. The actor with postfire() returns
 * false will not be fired any more.
 * </p>
 *
 * <p>
 * An actor that implements GetFirable interface (@see GetFirable) is wrapped by
 * ResumableFire (@see ResumableFire). Otherwise it's wrapped by BlockingFire (@see
 * BlockingFire). Particularly, the MetroIIComposite, MetroIIModalModel,
 * MetroIIPtidesPlatform have GetFirable interface implemented and are thus
 * wrapped by ResumableFire. And all other ordinary Ptolemy actors are wrapped
 * by BlockingFire. If an actor is wrapped by BlockingFire, the firing of an
 * actor has two MetroII events associated: FIRE_BEGIN and FIRE_END. FIRE_BEGIN
 * is first proposed and the firing blocks. When FIRE_BEGIN is NOTIFIED, fire()
 * is called and FIRE_END is proposed. When FIRE_END is NOTIFIED, the firing
 * successfully completes. The firing is atomic and there is no chance to
 * propose events during firing. If an actor is wrapped by ResumableFire, in
 * addition to FIRE_BEGIN and FIRE_END, getfire(MetroII event list) is called
 * instead of fire(). The process of firing may not only block on FIRE_BEGIN and
 * FIRE_END, but also block on the internal events of getfire(MetroII event
 * list).
 * </p>
 *
 * <p>
 * In ResumableFire, the 'start', 'block', and 'resume' are realized using
 * YieldAdapter, see <a
 * href="http://jimblackler.net/blog/?p=61">http://jimblackler
 * .net/blog/?p=61</a>. The underlying mechanism is to create, suspend, and
 * resume a java thread. And proposed MetroII events are returned by the
 * parameters of startOrResume().
 * </p>
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIDirector extends Director {
    /**
     * Constructs a director in the given container with the given name. The
     * container argument must not be null, or a NullPointerException will be
     * thrown. If the name argument is null, then the name is set to the empty
     * string. Increment the version number of the workspace.
     *
     * @param container
     *            Container of the director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the director is not compatible with the specified
     *                container. May be thrown in a derived class.
     * @exception NameDuplicationException
     *                If the container is not a CompositeActor and the name
     *                collides with an entity in the container.
     */
    public MetroIIDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _actorList = new LinkedList<FireMachine>();
        _mappingConstraintSolver = new MappingConstraintSolver();
        _timeScheduler = new TimeScheduler();
        _initializeParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /**
     * A mapping constraint is a pair of events that are rendezvous. Mapping
     * file is a text file that specifies such constraints. In mapping file,
     * each line is a mapping constraint, which contains two event names
     * separated by a space.
     *
     * <p>
     * _mappingFileName is a string that contains the absolute path of the
     * mapping file.
     * </p>
     *
     * The default value of _mappingFileName is null, which means no mapping
     * constraint is specified.
     */
    public FileParameter mappingFileName;

    /**
     * A Parameter representing the number of times that postfire may be called
     * before it returns false. If the value is less than or equal to zero, then
     * the execution will never return false in postfire, and thus the execution
     * can continue forever.
     *
     */
    public Parameter iterations;

    /**
     * Option parameter whether trace info is printed out.
     */
    public Parameter printTrace;

    /**
     * Option parameter whether debug info is printed out.
     */
    public Parameter printDebug;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Reacts to a change in an attribute. If the changed attribute matches a
     * parameter of the director, then the corresponding local copy of the
     * parameter value will be updated.
     *
     * @param attribute
     *            The changed parameter.
     * @exception IllegalActionException
     *                If the parameter set is not valid.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == mappingFileName) {

        } else if (attribute == printDebug) {
            if (((BooleanToken) printDebug.getToken()).booleanValue()) {
                _mappingConstraintSolver.turnOnDebugging();
                _timeScheduler.turnOnDebugging();
                _debugger.turnOnDebugging();
            } else {
                _mappingConstraintSolver.turnOffDebugging();
                _timeScheduler.turnOffDebugging();
                _debugger.turnOffDebugging();
            }
        }
        super.attributeChanged(attribute);

    }

    /**
     * Initializes the model controlled by this director. Call the initialize()
     * of super class and then wrap each actor that is controlled by this
     * director.
     *
     * This method should typically be invoked once per execution, after the
     * preinitialization phase, but before any iteration. It may be invoked in
     * the middle of an execution, if reinitialization is desired.
     *
     * This method is <i>not</i> synchronized on the workspace, so the caller
     * should be.
     *
     * @exception IllegalActionException
     *                If the initialize() method of one of the associated actors
     *                throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _initializeMapping();

        Nameable container = getContainer();
        int numActor = 0;
        // In the actor library, the container might be an moml.EntityLibrary.
        if (container instanceof CompositeActor) {
            Iterator<?> actors = ((CompositeActor) container).deepEntityList()
                    .iterator();

            _actorList.clear();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                if (actor instanceof GetFirable) {
                    _actorList.add(new ResumableFire(actor));
                } else {
                    _actorList.add(new BlockingFire(actor));
                }
                numActor++;
            }
        }

        _iterationCount = 0;

        _timeScheduler.initialize(numActor);

    }

    /**
     * Each iteration has two phases. In Phase 1, MetroIIDirector calls each
     * actor (no particular order should be presumed. See Note 1). Each actor
     * runs until it wants to propose MetroII events: the actor saves the state
     * and returns with MetroII events. In Phase 2, MetroIIDirector calls the
     * MappingConstraintSolver, which updates the MetroII events based on the
     * mapping constraints.
     */
    @Override
    public void fire() throws IllegalActionException {

        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        if (iterationsValue == 0) {
            return;
        }

        if (!_stopRequested) {
            LinkedList<Event.Builder> globalMetroIIEventList = new LinkedList<Event.Builder>();

            //Debug.Out.println(this.getFullName() + ": " + "Iteration "
            //        + Integer.toString(_iterationCount));

            // Phase 1: base model execution
            //Debug.Out.println(this.getFullName() + ": " + "Phase 1");
            _debugger.printTitle(getFullName() + " begins " + "iteration "
                    + Integer.toString(_iterationCount));
            _debugger.printText("Base model execution:");

            for (FireMachine firing : _actorList) {
                LinkedList<Event.Builder> metroIIEventList = new LinkedList<Event.Builder>();
                if (firing.getState() == FireMachine.State.START) {
                    boolean result = firing.actor().prefire();
                    assert result;
                }
                firing.startOrResume(metroIIEventList);
                if (firing.getState() == FireMachine.State.FINAL) {
                    boolean pf = firing.actor().postfire();
                    if (pf) {
                        firing.reset();
                        firing.startOrResume(metroIIEventList);
                    }
                }
                globalMetroIIEventList.addAll(metroIIEventList);
            }

            _debugger.printText("Before resolution:");
            _debugger.printMetroEvents(globalMetroIIEventList);
            //            if (((BooleanToken) printTrace.getToken()).booleanValue()) {
            //                for (Event.Builder builder : globalMetroIIEventList) {
            //                    // System.out.format("%-50s %-10s\n", builder.getName(),
            //                    //        builder.getStatus());
            //                    System.out.println(this.getFullName() + ": " + "Iteration "
            //                            + Integer.toString(_iterationCount) + " "
            //                            + "Phase 1" + " " + builder.getStatus() + " "
            //                            + builder.getName());
            //                }
            //            }
            // Phase 2: constraint resolution
            //Debug.Out.println(this.getFullName() + ": " + "Phase 2");

            _mappingConstraintSolver.resolve(globalMetroIIEventList);
            _timeScheduler.resolve(globalMetroIIEventList);

            _debugger.printText("After resolution:");
            _debugger.printMetroEvents(globalMetroIIEventList);
            _debugger.printTitle(getFullName() + " ends " + "iteration "
                    + Integer.toString(_iterationCount));

            if (((BooleanToken) printTrace.getToken()).booleanValue()) {
                for (Event.Builder event : globalMetroIIEventList) {
                    if (event.getStatus() == Status.NOTIFIED) {
                        System.out.println("Time " + _timeScheduler.getTime()
                                + " s: " + event.getName());
                    }
                }
            }
        }
    }

    /**
     * Clones the object into the specified workspace. The new object is
     * <i>not</i> added to the directory of that workspace (you must do this
     * yourself if you want it there).
     *
     * @param workspace
     *            The workspace for the cloned object.
     * @exception CloneNotSupportedException
     *                Not thrown in this base class
     * @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIIDirector newObject = (MetroIIDirector) super.clone(workspace);
        newObject._debugger = _debugger.clone();
        newObject._mappingConstraintSolver = _mappingConstraintSolver.clone();
        newObject._actorList = (LinkedList<FireMachine>) _actorList.clone();
        newObject._timeScheduler = _timeScheduler.clone();
        return newObject;
    }

    /**
     * The postfire() counts the number of iterations and returns false when the
     * number of iteration exceeds the parameter iterations or the time in
     * TimeScheduler exceeds stopTime, whichever comes first.
     *
     *
     * postfire() will always return true if the parameter iterations is less or
     * equal to 0.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        double stopTimeValue = ((DoubleToken) stopTime.getToken())
                .doubleValue();
        if (_stopRequested || iterationsValue >= 0
                && _iterationCount >= iterationsValue || stopTimeValue > 0
                && _timeScheduler.getTime() > stopTimeValue) {
            _stopRequested = true;
            _iterationCount = 0;
            for (StartOrResumable actor : _actorList) {
                actor.reset();
            }
            return false;
        }
        return true;
    }

    /**
     * Stops firing as soon as possible.
     */
    @Override
    public void stop() {
        _stopRequested = true;
        System.out.println(this.getFullName() + " stops!");
    }

    /**
     * Calls stopFire() of the superclass and show a message.
     */
    @Override
    public void stopFire() {
        super.stopFire();
        // System.out.println(this.getFullName() + " stopFire!");
    }

    /**
     * Resets all the StartOrResumable wrapped actors before calling the
     * wrapup() of Director.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (!_stopRequested) {
            _iterationCount = 0;
            for (StartOrResumable actor : _actorList) {
                actor.reset();
            }
        }
        super.wrapup();
        // System.out.println(this.getFullName() + " wrapups!");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Initialize mapping from the mapping file.
     *
     * @exception IllegalActionException
     *             If error occurs when reading the mapping file.
     */
    private void _initializeMapping() throws IllegalActionException {
        StringToken mappingFileNameToken = (StringToken) mappingFileName
                .getToken();
        if (mappingFileNameToken == null
                || mappingFileNameToken.stringValue().equals("")) {
            // mappingFileName = null;
            _mappingConstraintSolver.clear();
        } else {
            File file = mappingFileName.asFile();
            if (file != null) {
                String filename = file.getAbsolutePath();
                if (!filename.equals("")) {
                    try {
                        System.out.println(filename);
                        _mappingConstraintSolver.clear();
                        _mappingConstraintSolver.readMapping(filename);
                    } catch (IOException ex) {
                        throw new IllegalActionException(this, ex,
                                "Failed to open mapping file \"" + filename
                                + "\".");
                    }
                    if (_debugging) {
                        _debug(_mappingConstraintSolver.toString());
                    }
                }
            }
        }
    }

    /**
     * Initializes parameters. This is called by the constructor.
     *
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
    NameDuplicationException {
        startTime.setVisibility(Settable.NOT_EDITABLE);
        startTime.setExpression("0.0");
        mappingFileName = new FileParameter(this, "mappingFileName");
        iterations = new Parameter(this, "iterations");
        iterations.setTypeEquals(BaseType.INT);
        iterations.setExpression("-1");
        stopTime.setExpression("-1");
        printTrace = new Parameter(this, "printTrace");
        printTrace.setTypeEquals(BaseType.BOOLEAN);
        printTrace.setExpression("true");
        printDebug = new Parameter(this, "printDebug");
        printDebug.setTypeEquals(BaseType.BOOLEAN);
        printDebug.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * Debugger
     */
    private MetroIIDebugger _debugger = new MetroIIDebugger();

    /** The iteration count. */
    protected int _iterationCount = 0;

    /**
     * The constraint solver
     *
     */
    private MappingConstraintSolver _mappingConstraintSolver;

    /**
     * The time scheduler
     */
    private TimeScheduler _timeScheduler;

    /**
     * The list of actors governed by MetroIIDirector.
     */
    private LinkedList<FireMachine> _actorList;

}

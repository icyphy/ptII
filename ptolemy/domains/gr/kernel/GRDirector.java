/* Graphics (GR) domain director with synchronous/reactive semantics

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.actor.util.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.gr.lib.*;

import javax.media.j3d.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// GRDirector
/**

<h1>GR overview</h1>
GR is a domain/infrastructure for displaying three-dimensional graphics in
Ptolemy II.  GR. is an untimed domain that follows loop-less
synchronous/reactive (SR) semantics.

The basic idea behind the GR domain is to arrange geometry and transform
actors in a directed-acyclic-graph to represent the location and orientation
of objects in a natural world scene. This topology of connected GR actors
form what is commonly called in computer graphics literature as a scene graph.
The GR director converts the GR scene graph into a Java3D representation for
rendering on the computer screen.

@see ptolemy.domains.gr.kernel.GRReceiver
@see ptolemy.domains.gr.kernel.GRActor

@author C. Fong
@version $Id$
*/
public class GRDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public GRDirector() {
    	super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public GRDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public GRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    public Parameter iterations;
    public Parameter iterationTimeUpperBound;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  The period parameter is explicitly cloned in this method.
     *  @param workspace The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        _reset();
        GRDirector newObject = (GRDirector)(super.clone(workspace));
        newObject.iterations = (Parameter) newObject.getAttribute("iterations");
        newObject.iterationTimeUpperBound = (Parameter)
            newObject.getAttribute("iteration time upper bound");
        return newObject;
    }


    /** Advance time to the next requested firing time.  The GR domain is not
     *  a timed domain, so this function is quite meaningless.  However, it is
     *  implemented in order to get timed domains to work inside the GR domain.
     *  @param actor, The actor to be fired.
     *  @param time, The next time when the actor should be fired.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        setCurrentTime(time);
    }

    /** Return the current time.  The GR domain is not a timed domain, so this
     * function is quite meaningless.  However, it is implemented in order to
     * get timed domains to work inside the GR domain.
     *  @param actor, The actor to be fired.
     */
    public double getCurrentTime() {
        if (_pseudoTimeEnabled == true) {
            return _insideDirector.getCurrentTime();
        } else return super.getCurrentTime();
    }


    /** Return maximum value for type double. Since the 3D domain is not a timed
     *  domain, this function does not return any meaningful value.  However, this
     *  function is needed by timed domains that are inside the 3D domain.
     *  @return The maximum value for type double.
     */
    public double getNextIterationTime() {
        return Double.MAX_VALUE;
    }



    /** Go through the schedule and iterate every actor with calls to
     *  prefire() , fire() , and postfire().
     *
     *  @exception IllegalActionException If an actor executed by this
     *  director return false in its prefire().
     */
    public void fire() throws IllegalActionException {
        // -fire-
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();

        long currentTime = System.currentTimeMillis();
        int frameRate = ((IntToken)iterationTimeUpperBound.getToken()).intValue();
        long timeElapsed = currentTime - _lastIterationTime;
        long timeRemaining = frameRate - timeElapsed;
        if (timeRemaining > 0) {
            try {
                java.lang.Thread.sleep(timeRemaining);
            } catch (InterruptedException e) {

            }
        }
        _lastIterationTime = currentTime;

        if (container == null) {
            throw new InvalidStateException("GRDirector " + getName() +
                    " fired, but it has no container!");
        }

        Scheduler s = getScheduler();
        if (s == null)
            throw new IllegalActionException("Attempted to fire " +
                    "GR system with no scheduler");
        Enumeration allactors = s.schedule();
        while (allactors.hasMoreElements()) {

            Actor actor = (Actor)allactors.nextElement();
            if(!actor.prefire()) {
                debug.println("Actor --> "+actor);
                throw new IllegalActionException(this,
                        (ComponentEntity) actor, "Actor " +
                        "is not ready to fire.");
            }

            if (actor instanceof CompositeActor) {
                CompositeActor compositeActor = (CompositeActor) actor;
                Director  insideDirector = compositeActor.getDirector();

                _insideDirector = insideDirector;
                _pseudoTimeEnabled = true;
                actor.fire();
                _pseudoTimeEnabled = false;

            } else {
                if ((actor instanceof GRActor) && !_isSceneGraphInitialized) {
                    ((GRActor)actor).makeSceneGraphConnection();
                }
                actor.fire();
            }


            _postfirereturns = actor.postfire();
        }
        _isSceneGraphInitialized = true;
    }



    /** Initialize all the actors associated with this director by calling
     *  super.initialize(). Build the Java3D scene graph.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        //  -initialize-
        super.initialize();
        _buildActorTable();
        // CASE#2 for TypedCompositeActor to AtomicActor connection
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        if (container.isOpaque()) {
            List list = container.outputPortList();
            Iterator listIterator = list.iterator();

            while(listIterator.hasNext()) {

                IOPort port = (IOPort) listIterator.next();
                Receiver[][] portReceivers = port.getInsideReceivers();

                if (portReceivers[0][0] != null) {
                    GRReceiver receiver;
                    receiver = (GRReceiver) portReceivers[0][0];
                    receiver.createGroupNode();
                }
            }
        }
    }



    /** Process the mutation that occurred.  Reset this director
     *  to an uninitialized state.  Notify parent class about
     *  invalidated schedule.  This method is called when an entity
     *  is instantiated under this director. This method is also
     *  called when a link is made between ports and/or relations.
     *  see also other mutation methods:
     *    <p><UL>
     *    <LI> void attributeChanged(Attribute)
     *    <LI> void attributeTypeChanged(Attribute)
     *    </UL></p>
     */
    public void invalidateSchedule() {
        //  -invalidateSchedule-
        _reset();
        super.invalidateSchedule();
    }

    /** Return a new receiver consistent with the GR domain.
     *  This function is called when a connection between an output port
     *  of an actor and an input port of another actor is made in Vergil.
     *  This function is also called during the preinitialize() stage of
     *  a toplevel director.  This function may also be called prior to
     *  the preinitialize() stage of a non-toplevel director.
     *
     *  @return A new GRReceiver.
     */
    public Receiver newReceiver() {

        GRReceiver currentReceiver = new GRReceiver();
        _receiverTable.add(currentReceiver);
        return currentReceiver;
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do.  If there are no input ports, then also return true.
     *  Otherwise, return false.  Note that this does not call prefire()
     *  on the contained actors.
     *  @exception IllegalActionException If port methods throw it.
     *  @return True.
     */
    public boolean prefire() throws IllegalActionException {
        // -prefire-
        _postfirereturns = true;

        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        Iterator inputPorts = container.inputPortList().iterator();
        int inputCount = 0;
        while(inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int threshold = 1;
            Receiver receivers[][] = inputPort.getReceivers();

            int channel;
            for(channel = 0; channel < inputPort.getWidth(); channel++) {
                /*if(!receivers[channel][0].hasToken(threshold)) {
                  System.out.println("Channel " + channel +
                  " does not have enough tokens." +
                  " Prefire returns false on " +
                  container.getFullName());
                  return false;
                  }*/
            }
        }
        return true;
    }


    /** Set current time to zero. Invoke the preinitialize() methods of
     *  all actors deeply contained by the container by calling
     *  super.preinitialize(). This method is invoked once per execution,
     *  before any iteration; i.e. every time the GO button is pressed.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method
     *   of the container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        //  -preinitialize-
        super.preinitialize();
        _iteration = 0;
    }


    /** Request the outside director to fire this director's container
     *  again for the next period.
     *
     *  @return true if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the parent class throws
     *  it.
     */
    public boolean postfire() throws IllegalActionException {
        //  -postfire-

        boolean returnValue = super.postfire();
        int totalIterations = ((IntToken) (iterations.getToken())).intValue();
        _iteration++;
        if((totalIterations > 0) && (_iteration >= totalIterations)) {
            _iteration = 0;
            return false;
        }
        return _postfirereturns;
    }


    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  input port. If any channel of the input port has no data, then
     *  that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean transferred = false;
        Receiver[][] insideReceivers = port.deepGetReceivers();
        for (int i = 0; i < port.getWidth(); i++) {
            ptolemy.data.Token t = port.get(i);
            if (insideReceivers != null && insideReceivers[i] != null) {
                for (int j = 0; j < insideReceivers[i].length; j++) {
                    insideReceivers[i][j].put(t);
                }
                transferred = true;
            }
        }
        return transferred;
    }



    /** This is called by the outside director to get tokens
     *  from the inside director.
     *  Return true if transfers data from an output port of the
     *  container to the ports it is connected to on the outside.
     *  This method differs from the base class method in that this
     *  method will transfer all available tokens in the receivers,
     *  while the base class method will transfer at most one token.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
        //  -transferOutputs-
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not " +
                    "an opaque output port.");
        }
        boolean trans = false;


        Receiver[][] portReceivers = port.getInsideReceivers();
        if (portReceivers != null) {
            for (int i = 0; i < portReceivers.length; i++) {
                if (portReceivers[i] != null) {
                    for (int j = 0; j < portReceivers[i].length; j++) {
                        while (portReceivers[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = portReceivers[i][j].get();
                                port.send(i, t);
                                trans = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return trans;
    }



    /** Reset this director to an uninitialized state.
     *  @exception IllegalActionException If the parent class
     *  throws it
     */
    public void wrapup() throws IllegalActionException {
        //  -wrapup-
        super.wrapup();
        _reset();
    }




    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *  @exception IllegalActionException If the scheduler is null
     */
    private void _buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler== null)
            throw new IllegalActionException("Attempted to fire " +
                    "GR system with no scheduler");
        Enumeration allActorsScheduled = currentScheduler.schedule();


        int actorsInSchedule = 0;
        while (allActorsScheduled.hasMoreElements()) {
            Actor actor = (Actor) allActorsScheduled.nextElement();
            String name = ((Nameable)actor).getFullName();
            _GRActor grActor = (_GRActor) _allActorsTable.get(actor);
            if (grActor==null) {
                _allActorsTable.put(actor, new _GRActor(actor));
                grActor = (_GRActor) _allActorsTable.get(actor);
                _actorTable.add(grActor);
            }
            actorsInSchedule++;
        }

        // include the container as an actor.  This is needed for TypedCompositeActors
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor, new _GRActor((Actor)getContainer()));
        _GRActor grActor = (_GRActor) _allActorsTable.get(actor);
        _actorTable.add(grActor);

        _debugViewActorTable();
        ListIterator receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            GRReceiver currentReceiver = (GRReceiver) receiverIterator.next();
            //currentReceiver.determineEnds(this);
        }

        receiverIterator = _receiverTable.listIterator();


        _debugViewActorTable();
        _debugViewReceiverTable();
    }

    /** Build the internal cache of all the ports directed by this director
     *
     *  @exception IllegalActionException If methods called throw it.
     */
    private void _buildOutputPortTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();

        Iterator outports = container.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort port = (IOPort)outports.next();

            _outputPortTable.add(new _GRIOPort(port));
        }
    }

    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException if there is a problem in
     *   obtaining the number of initial token for delay actors
     */
    private void _debugViewActorTable() throws IllegalActionException {
        debug.println("\nACTOR TABLE with "+_actorTable.size()+" unique actors");
        debug.println("---------------------------------------");
        ListIterator actorIterator = _actorTable.listIterator();
        while(actorIterator.hasNext()) {
            _GRActor currentActor = (_GRActor) actorIterator.next();
            String actorName = ((Nameable) currentActor._actor).getName();

            if ( !((ComponentEntity) currentActor._actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
        }
    }

    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _debugViewReceiverTable() {
        //  -displayReceiverTable-
        debug.print("\nARC RECEIVER table with "+_receiverTable.size());
        debug.println(" unique receivers");

        ListIterator receiverIterator = _receiverTable.listIterator();

        while(receiverIterator.hasNext()) {
            GRReceiver currentReceiver = (GRReceiver) receiverIterator.next();
            debug.println(" receiver "+currentReceiver);
        }
        debug.println("\n");
    }

    /** For debugging purposes.  Display the list of attributes
     *  inside a given named object
     *  @param obj The named object that has a list of attributes
     */
    private void _debugViewAttributesList(NamedObj obj)
        {
            List list = obj.attributeList();
            Iterator listIterator = list.iterator();

            debug.println("attribute List:");
            while(listIterator.hasNext()) {
                Attribute attribute = (Attribute) listIterator.next();
                debug.println(attribute);
            }
        }


    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     *  @param obj The composite entity with a list of contained entities.
     */
    private void _debugViewEntityList(CompositeEntity obj) {

        List list = obj.entityList();
    	Iterator listIterator = list.iterator();

    	debug.println("\nentity List:");
    	while(listIterator.hasNext()) {
    	    Entity entity = (Entity) listIterator.next();
    	    debug.println(entity);
    	}
    	debug.println("\n");
    }

    /** For debugging purposes. This is mainly used for figuring out
     *  the list of output ports in a TypedCompositeActor container.
     *  Note: This method seems to work only for opaque
     *        TypedCompositeActor containers.
     *  Note: Output ports of actors inside the TypedCompositeActor
     *  container will not be listed.
     */
    private void _debugViewContainerOutputPorts() throws IllegalActionException {

        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();

        debug.println("\nTypedCompositeActor container output port list:");
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            debug.println(" ->"+port);
            _debugViewPortInsideReceivers(port);
            //_debugViewPortRemoteReceivers(port);
        }
        debug.println("\n");
    }

    /** For debugging purposes. This is mainly used for figuring out
     *  which outside receivers are connected to an output port of an
     *  TypedCompositeActor. The TypedCompositeActor can be transparent
     *  or opaque.
     */
    private void _debugViewPortRemoteReceivers(IOPort port) {
        Receiver[][] remoteReceivers = port.getRemoteReceivers();

    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<remoteReceivers[i].length;j++) {
    	        debug.println("  -->"+remoteReceivers[i][j]);
    	        debug.println("  ==>"+remoteReceivers[i][j].getContainer());
    	    }
    	}
    }

    /** For debugging purposes.  This function only makes sense
     *  if the port argument is an opaque / cross-hierarchy
     *  output port.
     *  Note: The only output ports with receivers are
     *        opaque / cross-hierarchy output ports.
     *  IMPORTANT: There are no receivers inside transparent
     *             TypedCompositeActors ports
     */
    private void _debugViewPortInsideReceivers(IOPort port) throws IllegalActionException {
        Receiver[][] portReceivers = port.getInsideReceivers();

    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<portReceivers[i].length;j++) {
    	        debug.println("  ->"+portReceivers[i][j]);
    	        debug.println("  =>"+portReceivers[i][j].getContainer());
    	        // FIXME: remove comments when debugging
    	        // ((GRReceiver)portReceivers[i][j]).displayReceiverInfo();
    	    }
    	}
    }

    /** Convenience method for getting the director of the container that
     *  holds this director.  If this director is inside a toplevel
     *  container, then the returned value is null.
     *  @returns The executive director
     */
    private Director _getOutsideDirector() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();

        return outsideDirector;
    }


    /** Most of the constructor initialization is relegated to this method.
     *  Initialization process includes :
     *    - create a new actor table to cache all actors contained
     *    - create a new receiver table to cache all receivers contained
     *    - set default number of iterations
     *    - set period value
     */
    private void _init() {
        try {
            GRScheduler scheduler = new GRScheduler(workspace());
            setScheduler(scheduler);
        } catch (IllegalActionException e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }

        try {
            iterations = new Parameter(this,"iterations",new IntToken(0));
            iterationTimeUpperBound = new Parameter(this,
                    "iteration time upper bound",new IntToken(33));
        } catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    	try {
            //period = new Parameter(this,"period",new DoubleToken(1.0));
            _reset();
            iterations.setToken(new IntToken(0));
            debug = new GRDebug(false);
    	} catch (Exception e) {
    	    throw new InternalErrorException(
                    "unable to initialize GR Director:\n" +
                    e.getMessage());
    	}
    }

    private void _reset() {
        _actorTable = new ArrayList();
        _receiverTable = new ArrayList();
        _outputPortTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _currentTime = 0.0;
        _formerTimeFired = 0.0;
        _formerValidTimeFired = 0.0;
        _lastIterationTime = (long)0;
        _isSceneGraphInitialized = false;
    }




    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // ArrayList to keep track of all actors scheduled by GRDirector
    private ArrayList _actorTable;

    // ArrayList used to cache all receivers managed by GRDirector
    private ArrayList _receiverTable;

    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;

    // The time when the previous valid prefire() was called
    private double _formerValidTimeFired;

    // The time when the previous valid or invalid prefire() was called
    private double _formerTimeFired;

    // ArrayList to keep track of all container output ports
    private ArrayList _outputPortTable;

    // display for debugging purposes
    private GRDebug debug;

    private long _lastIterationTime;
    private boolean _pseudoTimeEnabled = false;
    private Director _insideDirector;
    private boolean _isSceneGraphInitialized;




    private static final double TOLERANCE = 0.0000000001;

    private int _iteration = 0;
    protected boolean _postfirereturns = true;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Inner class to cache important variables for contained actors
    private class _GRActor {
    	private Actor    _actor;

    	/* Construct the information on the contained Actor
    	 * @param a The actor
    	 */
    	public _GRActor(Actor actor) {
            _actor = actor;
    	}
    }

    // Inner class to cache important variables for container output ports
    private class _GRIOPort {
        private IOPort _port;
        private boolean _shouldTransferOutputs;

        /*  Construct the information on the output port
         *  @param p The port
         */
        public _GRIOPort(IOPort port) {
            _port = port;
            _shouldTransferOutputs = false;
        }
    }
}

/* An SDFDirector governs the execution of a CompositeActor containing
   SDFActors

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// SDFDirector
/**
<h1>SDF overview</h1>
The Synchronous Dataflow(SDF) domain supports the efficient
execution of Dataflow graphs that
lack control structures.   Dataflow graphs that contain control structures
should be executed using the Process Networks(PN) domain instead.
SDF allows efficient execution, with very little overhead at runtime.  It
requires that the rates on the ports of all actors be known before hand.
SDF also requires that the rates on the ports not change during
execution.  In addition, in some cases (namely systems with feedback) delays,
which are represented by initial tokens on relations must be explicitly
noted.  SDF uses this rate and delay information to determine
the execution sequence of the actors before execution begins.
<h2>Schedule Properties</h2>
<ul>
<li>The number of tokens accumulated on every relation is bounded, given
an infinite number of executions of the schedule.
<li>Deadlock will never occur, given and infinite number of executions of
the schedule.
</ul>
<h1>Class comments</h1>
An SDFDirector is the class that controls execution of actors under the
SDF domain.  By default, actor scheduling is handled by the SDFScheduler
class.  Furthermore, the newReceiver method creates Receivers of type
SDFReceiver, which extends QueueReceiver to support optimized gets
and puts of arrays of tokens.
<p>
The SDF director has a single parameter, "iterations", corresponding to a
limit on the number of times the director will fire its hierarchy
before it returns false in postfire.  If this number is not greater
than zero, then no limit is set and postfire will always return true.
The default value of the iterations parameter is an IntToken with value zero.
@see ptolemy.domains.sdf.kernel.SDFScheduler
@see ptolemy.domains.sdf.kernel.SDFReceiver

@author Steve Neuendorffer
@version $Id$
*/
public class SDFDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     */
    public SDFDirector() {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The SDFDirector will have a default scheduler of type SDFScheduler.
     *
     *  @param workspace The workspace of this object.
     */
    public SDFDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SDFDirector will have a default scheduler of type
     *   SDFScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in a derived class.
     */
    public SDFDirector(TypedCompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        _init();
    }

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the simulation will never return false in postfire.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            SDFDirector newobj = (SDFDirector)(super.clone(ws));
            newobj.iterations =
                (Parameter)newobj.getAttribute("iterations");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Calculate the current schedule, if necessary,
     *  and iterate the contained actors
     *  in the order given by the schedule.  No internal state of the
     *  director is updated during fire, so it may be used with domains that
     *  require this property, such as CT.
     *  <p>
     *  Iterating an actor involves calling the actor's prefire, fire and
     *  postfire methods in succession.  If prefire returns false, indicating
     *  that the actor is not ready to execute, then an IllegalActionException
     *  will be thrown.   The values returned from postfire are recorded and
     *  are used to determine the value that postfire will return at the
     *  end of the director's iteration.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    public void fire() throws IllegalActionException {
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());

        if (container == null) {
            throw new InvalidStateException("SDFDirector " + getName() +
                    " fired, but it has no container!");
        } else {
            Scheduler s = getScheduler();
            if (s == null)
                throw new IllegalActionException("Attempted to fire " +
                        "SDF system with no scheduler");
            Enumeration allactors = s.schedule();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();

		// Notify of the prefire event. 
		if(_debugging) {
		    _debug(new FiringEvent(this, actor, FiringEvent.PREFIRE));
		}

                if(!actor.prefire()) {
                    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
                }

                if(_debugging) {		    
                    _debug("Firing " + ((Nameable)actor).getFullName());
		    _debug(new FiringEvent(this, actor, FiringEvent.FIRE));
		}

                actor.fire();

		if(_debugging) {
		    _debug(new FiringEvent(this, actor, FiringEvent.POSTFIRE));
		}

                _postfirereturns = _postfirereturns && actor.postfire();

		if(_debugging) {
		    _debug(new FiringEvent(this, actor, FiringEvent.POSTPOSTFIRE));
		}
            }
        }
    }

    /** Return a new receiver consistent with the SDF domain.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** The SDFDirector always returns true,
     *  assuming that it can be fired.   It does
     *  not call prefire on any contained actors.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        _postfirereturns = true;

        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
	Iterator inputPorts = container.inputPortList().iterator();
	int inputCount = 0;
	while(inputPorts.hasNext()) {
	    IOPort inputPort = (IOPort) inputPorts.next();
	    if (_debugging) _debug("checking input " +
                    inputPort.getFullName());

	    int threshold =
		SDFScheduler.getTokenConsumptionRate(inputPort);
	    if (_debugging) _debug("Threshold = " + threshold);
	    Receiver receivers[][] = inputPort.getReceivers();

	    int channel;
	    for(channel = 0;
		channel < inputPort.getWidth();
		channel++) {
		if(!receivers[channel][0].hasToken(threshold)) {
		    if(_debugging) _debug("Channel " + channel + 
					  " does not have enough tokens." +
					  " Prefire returns false on " + 
					  container.getFullName());
		    return false;
		}
	    }
	}
	if(_debugging) _debug("Prefire returns true on " + 
			      container.getFullName());
	return true;
    }

    /** Initialize the actors associated with this director and
     *  initialize the number of iterations to zero.  The order in which
     *  the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _iteration = 0;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the system return
     *  false in postfire.
     *  Increment the number of iterations.
     *  If the "iterations" parameter is greater than zero, then
     *  see if the limit has been reached.  If so, return false.
     *  Otherwise return true if all of the fired actors since the last
     *  call to prefire returned true.
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        int numiterations = ((IntToken) (iterations.getToken())).intValue();
        _iteration++;
        if((numiterations > 0) && (_iteration >= numiterations)) {
            _iteration = 0;
            return false;
        }
        return _postfirereturns;
    }

    /** Return true if transfers data from an input port of the
     *  container to the ports it is connected to on the inside.
     *  This method differs from the base class method in that this
     *  method will transfer enough tokens to complete an internal iteration,
     *  while the base class method will transfer at most one token.
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
        boolean trans = false;
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i = 0; i < port.getWidth(); i++) {
	    int rate = SDFScheduler.getTokenConsumptionRate(port);
	    for(int k = 0; k < rate; k++) {
                try {
                    ptolemy.data.Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        if(_debugging) _debug(getName(),
                                "transferring input from " + port.getName());
                        for (int j = 0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }
                        trans = true;
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "SDFDirector.transferInputs: Not enough tokens " +
                            ex.getMessage());
                }
            }
        }
        return trans;
    }

    /** Return true if transfers data from an output port of the
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
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not " +
                    "an opaque output port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j = 0; j < insiderecs[i].length; j++) {
			while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = insiderecs[i][j].get();
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


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if this director requires write access
     *  on the workspace during execution. Most director functions
     *  during execution do not need write access on the workspace.
     *  A director will generally only need write access on the workspace if
     *  it performs mutations locally, instead of queueing them with the
     *  manager.
     *  <p>
     *  In this class, return true, indicating that SDF does not perform local
     *  mutations.
     *
     *  @return false
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler.
     */

    private void _init() {
        try {
            SDFScheduler scheduler = new SDFScheduler(workspace());
            setScheduler(scheduler);
        }
        catch (IllegalActionException e) {
            // if setScheduler fails, then we should just set it to Null.
            // this should never happen because we don't override
            // setScheduler() to do sanity checks.
            throw new InternalErrorException(
                    "Could not create Default Scheduler:\n" +
                    e.getMessage());
        }

        try {
            iterations
                = new Parameter(this,"iterations",new IntToken(0));
        }
        catch (Exception e) {
            throw new InternalErrorException(
                    "Cannot create default iterations parameter:\n" +
                    e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _iteration = 0;
    private boolean _postfirereturns = true;

    // Support for mutations.
    // private CircularList _pendingMutations = null;
    // private CircularList _mutationListeners = null;
    //    private ActorListener _actorListener = null;
}

/* A Director governs the execution of a CompositeActor.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Director
/**
A Director governs the execution within a CompositeActor.  A composite actor
that contains a director is considered opaque, and the execution model
within the composite actor is determined by the contained Director.   This
director is called the local director of a composite actor.   A composite
actor is also aware of the director of its container, which is referred to
as its executive director.
<p>
A top-level composite actor is generally associated with a Manager as well as
a local director.  The Manager has overall responsibility for
executing the application, and is often associated with a GUI.   Top-level
composite actors have no executive director and getExecutiveDirector() will
return null.
<p>
A local director is responsible for invoking the actors contained by the
composite.  If there is no local director, then the executive director
is given the responsibility.  The getDirector() method of CompositeActor,
therefore, returns the local director, if there is one, and otherwise
returns the executive director.  Thus, it returns whichever director
is responsible for executing the contained actors, or null if there is none.
Whatever it returns is called simply the <i>director</i> (vs. local
director or executive director).
<p>
A director implements the action methods (initialize(), prefire(), fire(),
postfire(), and wrapup()).  In this base class, default implementations
are provided that may or may not be useful in specific domains.   In general,
these methods will perform domain-dependant actions, and then call the
respective methods in all contained actors.
<p>
A director also provides services for cleanly handling mutations of the
topology.  Mutations include such changes as adding or removing an entity,
port, or relation, and creating or destroying a link.  Usually,
mutations cannot safely occur at arbitrary points in the execution of
an application.  Applications can queue mutations with the director,
and the director will then perform the mutations at the first opportunity,
when it is safe.  In this base-class implementation, mutations are performed
at the beginning of each iteration by the prefire() method.
<p>
A service is also provided whereby an object can be registered with the
director as a mutation listener.  A mutation listener is informed of
mutations that occur when they occur (in this base class, at the beginning
of the iterate() method).
<p>
One particular mutation listener, called an ActorListener, is added
to a director the first time a mutation is performed.  This listener
ignores all mutations except those that add or remove an actor.
For those mutations, it records the addition or deletion.
After all the mutations have been completed in the prefire() method,
any actors that are new to the composite have their initialize() methods
invoked.
<p>
An initialize() method may queue further mutations with the director.
<p>
The director also provides methods to optimize the iteration portion of a
simulation. This is done by letting the workspace to be write-protected during
an iteration. In this base class, the default implementation prevent the
workspace to be write-protected. Derived classes (e.g. domain specific
directors) should override the _writeAccessPreference() method to let
the workspace to be write-protected. (Note that the workspace might still be
not-write-protected, it all depends on other directors in the simulation).

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer, John Reekie
@version: $Id$
*/
public class Director extends NamedObj implements Executable {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Director() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public Director(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public Director(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Add a topology change listener to this director. The listener
     * will be notified of each change in the topology that
     * happens when the Director decide it is safe to make
     * topology changes. Change requests are queued by using
     * the queueToploguChangeRequest() method.
     *
     *  @param listener The TopologyListener to add.
     */
    public void addTopologyListener(TopologyListener listener) {
        if (_topologyListeners == null) {
            _topologyListeners = new TopologyMulticaster();
        }
        _topologyListeners.addTopologyListener(listener);
    }

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Director.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Director newobj = (Director)super.clone(ws);
        newobj._container = null;
        newobj._queuedTopologyRequests = null;
        newobj._topologyListeners = null;
        return newobj;
    }

    /** Invoke an iteration on all of the deeply contained actors of the
     *  container of this Director.  In general, this may be called more
     *  than once in the same iteration of the Directors container.
     *  An iteration is defined as multiple invocations of prefire(), until
     *  it returns true, any number of invocations of fire(),
     *  followed by one invocation of postfire().
     *  Notice that we ignore the return value of postfire() in this base
     *  class.   In general, derived classes will want to do something
     *  intelligent with the returned value.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If any called method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        // Somewhere in here, constrained mutations should
        // probably be allowed to occur.
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                if(actor.prefire()) {
                    actor.fire();
                    actor.postfire();
                }
            }
        }
    }

    /** Return the container, which is the composite actor for which this
     *  is the local director.
     *  @return The CompositeActor that this director is responsible for.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Return the current time of the simulation. In this base class,
     *  it returns 0. The derived class should override this method
     *  and return the current time.
     */
    // FIXME: complete this.
    public double getCurrentTime() {
        return 0.0;
    }

    /** Get the next iteration time.
     */
    // FIXME: complete this.
    public double getNextIterationTime() {
        return 0.0;
    }

    /** Create receivers and then invoke the initialize()
     *  methods of all its deeply contained actors.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.createReceivers();
                actor.initialize();
            }
        }
    }

    /** Return whether this director and all its lower level directors would
     *  ever need write access on the workspace. This method first check
     *  the 'personal' preference of this director, if this director wants
     *  write access then just return true. Otherwise, this method continue by
     *  recursively call ones in the lower level directors. If any of those
     *  lower level directors disagree to have the workspace write-protected,
     *  then return true.
     *  @return true If this director disagree to have the workspace
     *  write protected, false otherwise.
     *
     */
    public final boolean needWriteAccess() {
        if (_writeAccessPreference()) {
            return true;
        }
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                // find out which of those actors has a local director.
                if (actor instanceof CompositeActor &&
                        ((CompositeActor)actor).isOpaque()) {
                    CompositeActor ca = (CompositeActor) actor;
                    // ca.getDirector() is guaranteed to return a local
                    // director, not the executive director.
                    if (ca.getDirector().needWriteAccess()) {
                        // If any of the directors need a write access, then
                        // everyone has to respect it.
                        return true;
                    }

                }
            }
            // Up to this point, all lower level directors have been queried
            // and none of them returned true (or else we would have returned)
            // Therefore, return false.
            return false;
        } else {
            System.out.println("Is this a bug, can a director not have a " +
                    "container.");

        }
        // This is hack, since otherwise the compiler will barf.
        // I choose to return the safe default case.
        return true;
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return false.   The default director will only get fired once, and will
     *  terminate execution afterwards.   Domain Directors will probably want
     *  to override this method.   Note that this is called by the container of
     *  this Director to see if the Director wishes to execute anymore, and
     *  should *NOT*, in general, just take the logical AND of calling
     *  postfire on all the contained actors.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException *Deprecate* If the postfire()
     *  method of the container or one of the deeply contained actors
     *  throws it.
     */
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /** return True, indicating that the Director is ready to fire.
     *  Domain Directors will probably want
     *  to override this method.   Note that this is called by the container of
     *  this Director to see if the Director is ready to execute, and
     *  should *NOT*, in general, just take the logical AND of calling
     *  prefire on all the contained actors.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException *Deprecate* If the postfire()
     *  method of the container or one of the deeply contained actors
     *  throws it.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Add a mutation object to the mutation queue. These mutations
     *  are executed when the _performTopologyChanges() method is called,
     *  which in this base class is in the prefire() method.  This method
     *  also arranges that all additions of new actors are recorded.
     *  The prefire() method then invokes the initialize() method of all
     *  new actors after the mutations have been completed.
     *
     *  @param mutation A object with a perform() and update() method that
     *   performs a mutation and informs any listeners about it.
     */
    public void queueTopologyChangeRequest(TopologyChangeRequest request) {
        // Create the list of requests if it doesn't already exist
        if (_queuedTopologyRequests == null) {
            _queuedTopologyRequests = new LinkedList();
        }
        _queuedTopologyRequests.insertLast(request);
    }

    /** Schedule to be refired after a specified delay with respect to the
     *  current time.
     *  @exception IllegalActionException FIXME
     */
    // FIXME: complete this.
    public void fireAfterDelay(Actor actor, double delay)
            throws IllegalActionException {
        // do nothing.
    }

    /** Remove a topology listener from this director.
     *  If the listener is not attached to this director, do nothing.
     *
     *  @param listener The TopologyListener to be removed.
     */
    public void removeTopologyListener(TopologyListener listener) {
        _topologyListeners.removeTopologyListener(listener);
    }

    /** Set the current time.
     *  Do nothing in this base class implementation.
     *  @exception IllegalActionException If time cannot be changed
     *   due to the state of the simulation. Only thrown in derived
     *   classes.
     *  @param newTime The new current simulation time.
     *
     */
    // FIXME: complete this.
    public void setCurrentTime(double newTime) throws IllegalActionException {

    }


    /** Recursively terminate all of our actors.   Domains may need to
     *  override this to properly deal with any threads they've created.
     */
    public void terminate() {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.terminate();
            }
        }
    }

    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public void transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque input port.");
        }
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j=0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
    }

    /** Transfer data from an output port of the container to the
     *  ports it is connected to on the outside.  The port argument must
     *  be an opaque output port.  If any channel of the output port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    public void transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not an opaque output port.");
        }
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i=0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j=0; j < insiderecs[i].length; j++) {
                        if (insiderecs[i][j].hasToken()) {
                            try {
                                Token t = insiderecs[i][j].get();
                                port.send(i,t);
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
    }

    /** Invoke the wrapup() method of all the actors contained in the
     *  Director's container.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            // FIXME: Add director-specific information here, like
            // what is the state of the director.
            // if ((detail & FIXME) != 0 ) {
            //  if (result.trim().length() > 0) {
            //      result += " ";
            //  }
            //  result += "fixme {\n";
            //  result += _getIndentPrefix(indent) + "}";
            // }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Make this director the local director of the specified composite
     *  actor.  This method should not be called directly.  Instead, call
     *  setDirector of the CompositeActor class (or a derived class).
     */
    protected void _makeDirectorOf (CompositeActor cast) {
        _container = cast;
        if (cast != null) {
            workspace().remove(this);
        }
    }

    /** Return an enumeration over the actors added to the topology in
     * the most recent call to _processTopologyRequests(). This is intended
     * so that directors can then initialize any new actors. The enumeration
     * is over a copy of the list, so it is safe for actors to perform
     * additional mutations during initialization.
     */
    protected Enumeration _newActors () {
        LinkedList copy = new LinkedList();
        copy.appendElements(_newActors.elements());
        return copy.elements();
    }


    /** Process the queued topology change requests. Registered topology
     * listeners are informed of each change in a series of calls
     * after successful completion of each request. If any queued
     * request fails, the request is undone, snd no further requests
     * are processed. Note that change requests processed successfully
     * prior to the failed request are <i>not</i> undone.
     *
     * Any new actors added to the topology during the course of
     * processing the mutation requests can be obtained with the
     * _newActors() method.
     *
     *  @exception IllegalActionException If any of the pending requests have
     *   already been implemented.
     *  @exception TopologyChangeFailedException If any of the requests fails.
     */
    protected void _processTopologyRequests()
            throws IllegalActionException, TopologyChangeFailedException {
        if (_queuedTopologyRequests == null) {
            return;
        }
        _newActors.clear();

        Enumeration enum = _queuedTopologyRequests.elements();
        while (enum.hasMoreElements()) {
            TopologyChangeRequest r =
                (TopologyChangeRequest)enum.nextElement();

            // Change the topology. This might throw a
            // TopologyChangeFailedException
            try {
                r.constructEventQueue();
            } catch (Exception ex) {
                //FIXME: better error message???
                System.out.println("arrggg" + ex.getClass().getName() + ex.getMessage());
            }
            r.performRequest();

            // Record any new actors that in this request
            Enumeration events = r.queuedEvents();
            while (events.hasMoreElements()) {
                TopologyEvent e = (TopologyEvent) events.nextElement();
                if (e.getID() == TopologyEvent.ENTITY_ADDED) {
                    if (e.getComponentEntity() instanceof Actor &&
                            !_newActors.includes(e.getEntity())) {
                        _newActors.insertLast(e.getComponentEntity());
                    }
                } else if (e.getID() == TopologyEvent.ENTITY_REMOVED) {
                    // Why on earth would you want do do this???
                    _newActors.removeOneOf(e.getComponentEntity());
                }
            }

            // Inform all listeners. Of course, this won't happen
            // if the change request failed
            if (_topologyListeners != null) {
                r.notifyListeners(_topologyListeners);
            }
        }
        // Clear the request queue
        _queuedTopologyRequests = null;
    }

    /** Indicate whether this director would like to have write access
     *  during its iteration. By default, the return value is true, indicating
     *  the need for a write access.
     *
     *  @return True if this director need write access, false otherwise.
     */
    protected boolean _writeAccessPreference() {
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The composite of which this is the local director.
    private CompositeActor _container = null;

    // Support for mutations.
    private LinkedList _queuedTopologyRequests = null;
    private TopologyMulticaster _topologyListeners = null;

    private LinkedList _newActors = new LinkedList();

}

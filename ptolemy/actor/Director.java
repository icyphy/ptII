/* Director governs the execution of a CompositeActor.

 Copyright (c) 1997- The Regents of the University of California.
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

package pt.actor;

import pt.kernel.*;
import pt.kernel.util.*;
import pt.kernel.mutation.*;
import pt.data.*;

import collections.LinkedList;
import java.util.Enumeration;
//import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// Director
/**
A Director governs the execution of a CompositeActor. It can serve either
as a local director or as an executive director.  To make it a local director,
use the setDirector() method of CompositeActor. To make it an executive
director, use the setExecutiveDirector() method of CompositeActor. In both
cases, the director will report the CompositeActor as its container
when queried by calling getContainer().
<p>
A director implements the action methods (initialize, prefire, fire,
postfire, and wrapup).  In this base class, default implementations
are provided that may or may not be useful in specific domains.
These default implementations behave differently depending on whether
the director is serving the role of a local director or an executive
director.  If it is an executive director, then the action methods
simply invoke the corresponding action methods of the local director.
If it is a local director, then the action methods invoke the
corresponding action methods of all the deeply contained actors.

@author Mudit Goel, Edward A. Lee
@version $Id$
*/
public class Director extends NamedObj implements Executable {

    /** FIXME: Does this constructor belong?  Used for testing IOPort.
     */
    public Director() {
        super();
    }

    /** Constructor.  FIXME: This constructor now doesn't belong,
     *  since we can't tell whether its a local or executive director.
     */
    public Director(CompositeActor container, String name) {
        super(name);
        _container = container;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Adds a new Mutation Listener to the list of listeners to be informed
     *  about any mutation that occurs in the graph for which this director
     *  is responsible
     * @param listener is the new MutationListener
     */
    public void addMutationListener(MutationListener listener) {
        synchronized(workspace()) {
            if (_mutationListeners == null) {
                _mutationListeners = new LinkedList();
            }
            _mutationListeners.insertLast(listener);
        }
    }

    /** Clears all the actors from the list of new actors
     */
    public void clearNewActors() {
        _newactors = null;
    }

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Director.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Director newobj = (Director)super.clone(ws);
        newobj._container = null;
        newobj._complete = true;
        newobj._schedulevalid = false;
        newobj._newactors = null;
        newobj._pendingMutations = null;
        newobj._mutationListeners = null;
        return newobj;
    }

    /** Indicates whether the execution is complete or not
     * @return true indicates that execution is complete
     */
    public boolean complete() {
        return _complete;
    }

    /** Remove the argument (all instances of it) from the new actors list.
     *  If it is not on the new actors list, do nothing.
     *
     *  @param actor The actor to remove.
     */
    public void deregisterNewActor(Actor actor) {
        synchronized(workspace()) {
            if (_newactors != null) {
                _newactors.removeOneOf(actor);
            }
        }
    }

    /** This should invoke the fire methods of the actors according to a
     *  schedule. This can be called more than once in the same iteration.
     *  This method need not be synchronized on the workspace, if as usual
     *  it is called by the CompositeActor. 
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        // If we are the executive director of a composite with a director,
        // we could invoke it's fire method now.  Be careful that that
        // doesn't turn around and call this fire() method (i.e., the
        // actor must return true to isAtomic().
    }

    /** Return the top-level container, of the executable application
     *  that this director is responsible for.
     * @return the top-level CompositeActor
     */
    public Nameable getContainer() {
        return _container;
    }

    /** Returns an enumeration of all the actors that have not begun execution
     * @return the enumeration of all new actors
     */
    public Enumeration getNewActors() {
        synchronized(workspace()) {
            if (_newactors == null) {
                _newactors = new LinkedList();
            }
            return _newactors.elements();
        }
    }

    /** Test if there are new actors waiting to be scheduled.
     * @return true if there are new actors
     */
    public boolean hasNewActors() {
        synchronized(workspace()) {
            return (_newactors != null);
        }
    }

    /** This does the initialization for the entire simulation. This should
     *  be called exactly once at the start of the entire execution
     */
    public void initialize() {
    }

    /** This sets a flag, indicating that on the next execution, the
     *  static schedule schedule should be recomputed
     */
    public void invalidateSchedule() {
        _schedulevalid = false;
    }

    /** This controls the execution.
     * @return true if the execution is complete and should be terminated
     * @exception IllegalActionException is thrown.
     * @see #fire()
     */
    public boolean iterate() throws IllegalActionException {
        if (prefire()) {
            fire();
            postfire();
        }
        return _complete;
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** This should be called after the fire methods have been called. This
     *  should invoke the postfire methods of actors according to a schedule
     */
    public void postfire() {
    }

    /** This determines if the schedule is valid. If the schedule is valid
     *  then it returns, else it calls methods for scheduling and takes care
     *  of changes due to mutation
     * @return false if application is not ready for invocation of fire()
     *  else returns true
     */
    public boolean prefire() {
        return true;
    }

    /** This does all the mutations and informs the listeners of the mutations
     *  @exception IllegalActionException if the port is not of the expected
     *   class, or the port has no name.
     *  @exception NameDuplicationException if the name collides with a name
     *   already on the port list.
     */
    public final void processPendingMutations()
            throws IllegalActionException, NameDuplicationException {
        synchronized(workspace()) {
            Mutation m;
            if (!_pendingMutations.isEmpty()) {
                Enumeration mutations = _pendingMutations.elements();
                while (mutations.hasMoreElements()) {
                    m = (Mutation)mutations.nextElement();
                    // do the mutation
                    m.perform();

                    // inform all listeners
                    // FIXME the listeners should probably be attached to
                    // the graph, not the director
                    if (_mutationListeners != null) {
                        Enumeration listeners = _mutationListeners.elements();
                        while (listeners.hasMoreElements()) {
                            m.update((MutationListener)listeners.nextElement());
                        }
                    }
                }
                // Clear the mutations
                _pendingMutations = null;
            }
        }
    }

    /** This adds a mutation object to the director queue. These mutations
     *  are finally incorporated when the processPendingMutations() is called
     * @param mutation The new Mutation objects that contains a list of
     *  mutations that should be executed later
     * @see #processPendingMutations()
     */
    public void queueMutation(Mutation mutation) {
        synchronized(workspace()) {
            if (_pendingMutations == null) {
                _pendingMutations = new LinkedList();
            }
            _pendingMutations.insertLast(mutation);
        }
    }

    /** Add an actor to the list of new actors that have been created
     *  after the last call to the iterate method, and have not begun
     *  execution.  If the actor is already on the list, do nothing.
     *  @param actor The actor to add.
     */
    public void registerNewActor(Actor actor) {
        synchronized(workspace()) {
            if (_newactors == null) {
                _newactors = new LinkedList();
            }
            if (!_newactors.includes(actor)) {
                _newactors.insertLast(actor);
            }
        }
    }

    /** This removes the Mutation listener that does not want to be informed
     *  of any future mutations by this director. This does not do anything
     *  if the listener was not listed with this director
     * @param listener is the MutationListener to be removed
     */
    public void removeMutationListener(MutationListener listener) {
        synchronized(workspace()) {
            _mutationListeners.removeOneOf(listener);
        }
    }

    /** This method is called to invoke an executable application. This
     *  would normally be overriden in different domains
     * @exception IllegalActionException is thrown by iterate()
     */
    public void run() throws IllegalActionException {
        initialize();
        while (!iterate());
        wrapup();
        return;
    }

    /** Indicates if the current schedule is valid or not
     * @return true indicates schedule is valid and need not be recomputed
     */
    public boolean scheduleValid() {
        return _schedulevalid;
    }

    /** This sets a flag indicating whether the iteration is complete or not
     * @param complete true indicates the end of execution
     */
    public void setComplete(boolean complete) {
        _complete = complete;
    }

    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @exception CloneNotSupportedException If the token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public void transferInputs(IOPort port)
            throws CloneNotSupportedException, IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
            "transferInputs: port argument is not an opaque input port.");
        }
        Receiver[][] insiderecs = port.getInsideReceivers();
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        boolean first = true;
                        for (int j=0; j < insiderecs[i].length; j++) {
                            if (first) {
                                insiderecs[i][j].put(t);
                                first = false;
                            } else {
                                insiderecs[i][j].put((Token)t.clone());
                            }
                        }
                    }
                } catch (NoSuchItemException ex) {
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
     *  @exception CloneNotSupportedException If the token cannot be cloned
     *   and there is more than one destination.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    public void transferOutputs(IOPort port)
            throws CloneNotSupportedException, IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
            "transferOutputs: port argument is not an opaque output port.");
        }
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    port.send(i,t);
                } catch (NoSuchItemException ex) {
                    throw new InternalErrorException(
                    "Director.transferOutputs: Internal error: " +
                    ex.getMessage());
                }
            }
        }
    }

    /** Mark the current schedule to be valid.
     *  @see #invalidateSchedule()
     */
    public void validateSchedule() {
        _schedulevalid = true;
    }

    /** This invokes the corresponding methods of all the actors at the end
     *  of simulation
     *
     *  @exception IllegalActionException If one of the actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        Enumeration allactors =
            ((CompositeActor)getContainer()).deepGetEntities();
        while (allactors.hasMoreElements()) {
            Actor actor = (Actor)allactors.nextElement();
            actor.wrapup();
        }
    }

    ///////////////////////////////////////////////////////////////////////
    ////                      protected methods                        ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is speicified it is the the leading bracket.
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
        _executivedirector = false;
    }
        
    /** Make this director the executive director of the specified composite
     *  actor.  This method should not be called directly.  Instead, call
     *  setExecutiveDirector of the CompositeActor class (or a derived class).
     */
    protected void _makeExecDirectorOf (CompositeActor cast) {
        _container = cast;
        _executivedirector = true;
    }
        
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The composite of which this is either a local or an executive director.
    private CompositeActor _container = null;

    // True if this is an executive director of the container.
    private boolean _executivedirector;

    private boolean _complete = true;
    private boolean _schedulevalid = false;
    private LinkedList _newactors = null;

    private LinkedList _pendingMutations = null;
    private LinkedList _mutationListeners = null;
}

/* A SCDirector governs the execution of a *chart model.

 Copyright (c)  The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)

*/

package ptolemy.domains.sc.kernel;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;
import ptolemy.actor.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// SCDirector
/**
An SCDirector governs the execution of a *charts model.

@author Xiaojun Liu
@version: @(#)SCDirector.java	1.2 11/27/98
*/
public class SCDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public SCDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public SCDirector(String name) {
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
    public SCDirector(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        SCDirector newobj = (SCDirector)super.clone(ws);
        newobj._controller = null;
        return newobj;
    }

    /** Return the current state of the controller.
     */
    public SCState currentState() {
        return _controller.currentState();
    }

    public Actor currentRefinement() {
        return _controller.currentRefinement();
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
    // FIXME!!
    // The controller may delegate firing its current refinement to this
    // director.
    // Note that the fire sequence of SCController is: 1. evaluate preemptive
    // transitions; 2. invoke refinement; 3. evaluate non-preemptive 
    // transitions.
    public void fire() throws IllegalActionException {
        // _controller must not be null
        _controller.fire();
    }

    /** Return the current time of the simulation. In this base class,
     *  it returns 0. The derived class should override this method
     *  and return the current time.
     */
    // FIXME: complete this.
    // Should ask its executive director for current time.
    public double getCurrentTime() {
        double ctime = 0.0;
        CompositeActor cont = (CompositeActor)getContainer();
        if (cont == null) {
            // In fact this should not happen, this director must have
            // a container.
            ctime = 0.0;
        } else {
            Director execdir = (Director)cont.getExecutiveDirector();
            if (execdir == null) {
                // PANIC!!
                throw new InvalidStateException(this,
                        "SCDirector must have an executive director!");
            }
            ctime = execdir.getCurrentTime();
        }
        return ctime;
    }

    /** Get the next iteration time.
     */
    // FIXME: complete this.
    // Note we should make clear which entities call this method: the
    // executive director, or the directors of the composite actors
    // governed by this director.
    // Should only be called by executive director.
    public double getNextIterationTime() {
        // Enumerate the actors, ask them the next iteration time,
        // return the minimum.
        // ADD THIS.
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
    // This now initializes the controller and all refinements.
    // Possible to initialize only the controller.
    // FIXME!!
    public void initialize() throws IllegalActionException {
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.createReceivers();
                actor.initialize();
            }
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    // Use QueueReceiver?
    // NOTE! There is a problem of token accumulating in the reveivers.
    // FIXME!!
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /** Return false. The default director will only get fired once, and will
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
    // NOTE! There is the problem of how to deal with refinements' return
    // value.
    public boolean postfire() throws IllegalActionException {
        // elaborate
        Actor refine = _controller.currentRefinement();
        if (refine != null) {
            refine.postfire();
        }
        return _controller.postfire();
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
        // elaborate
        Actor refine = _controller.currentRefinement();
        boolean result = true;
        if (refine != null) {
            result = refine.prefire();
        }
        // result = result & _controller.prefire();
        return result;
    }

    public void setController(SCController ctrl) 
            throws IllegalActionException {
        if (getContainer() == null) {        
            throw new IllegalActionException(this, ctrl,
                    "SCDirector must have a container to set its "
                    + "controller.");
        }
        if (getContainer() != ctrl.getContainer()) {
            throw new IllegalActionException(this, ctrl,
                    "SCDirector must have the same container as its "
                    + "controller.");
        }
        _controller = ctrl;
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
    // Call this method on all CompositeActor refinements.
    public void setCurrentTime(double newTime) throws IllegalActionException {
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
        // do not handle multiple tokens, multiple channels now
        Entity refine = (Entity)_controller.currentRefinement();
        IOPort p;
        Receiver rec;
        if (port.hasToken(0)) {
            try {
                Token t = port.get(0);
                p = (IOPort)_controller.getPort(port.getName());
                if (p != null) {
                    rec = (p.getReceivers())[0][0];
                    rec.put(t);
                }
                p = (IOPort)refine.getPort(port.getName());
                if (p != null) {
                    rec = (p.getReceivers())[0][0];
                    rec.put(t);
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " +
                        ex.getMessage());
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
        // do not handle multiple tokens, multiple channels now
        Receiver insiderec = (port.getInsideReceivers())[0][0];
        CompositeActor cont = (CompositeActor)getContainer();
        IOPort p = (IOPort)cont.getPort(port.getName());
        if (insiderec.hasToken()) {
            try {
                Token t = insiderec.get();
                _controller.currentState().setLocalInputVar(port.getName(), t);
                if (p != null) {
                    Receiver rec = (p.getInsideReceivers())[0][0];
                    rec.put(t);
                }
            } catch (NoTokenException ex) {
                throw new InternalErrorException(
                    "Director.transferOutputs: " +
                    "Internal error: " +
                    ex.getMessage());
            }
        }
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

    private SCController _controller = null;

}



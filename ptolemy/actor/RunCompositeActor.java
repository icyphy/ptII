/* A composite actor that executes a submodel in fire().

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.actor;

import java.util.Iterator;
import java.util.LinkedList;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// RunCompositeActor
/**
A composite actor that performs a complete execution of the submodel
in the fire() method.
<p>
FIXME: More details.
<P>

@author Edward A. Lee, (Contributor: Yang Zhao)
@version $Id$
*/
public class RunCompositeActor extends TypedCompositeActor {
    
    /** Construct an actor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */
    public RunCompositeActor() {
        super();
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is RunCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as RunCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be RunCompositeActor.
        setClassName("ptolemy.actor.RunCompositeActor");
    }

    /** Construct a RunCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public RunCompositeActor(Workspace workspace) {
        super(workspace);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is RunCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as RunCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be RunCompositeActor.
        setClassName("ptolemy.actor.RunCompositeActor");
    }

    /** Construct a RunCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public RunCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is RunCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as RunCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be RunCompositeActor.
        setClassName("ptolemy.actor.RunCompositeActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Execute requested changes. In this class,
     *  do not delegate the change request to the container, but
     *  execute the request immediately.  Listeners will be notified
     *  of success or failure.
     *  @see #addChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see #setDeferChangeRequests(boolean)
     */
    public void executeChangeRequests() {
        synchronized(_changeLock) {
            if (_changeRequests != null && _changeRequests.size() > 0) {
                // Copy the change requests lists because it may
                // be modified during execution.
                LinkedList copy = new LinkedList(_changeRequests);

                // Remove the changes to be executed.
                // We remove them even if there is a failure because
                // otherwise we could get stuck making changes that
                // will continue to fail.
                _changeRequests.clear();

                Iterator requests = copy.iterator();
                boolean previousDeferStatus = isDeferringChangeRequests();
                try {
                    // Get write access once on the outside, to make
                    // getting write access on each individual
                    // modification faster.
                    _workspace.getWriteAccess();

                    // Defer change requests so that if changes are
                    // requested during execution, they get queued.                    
                    setDeferringChangeRequests(true);
                    while (requests.hasNext()) {
                        ChangeRequest change = (ChangeRequest)requests.next();
                        change.setListeners(_changeListeners);
                        if (_debugging) {
                            _debug("-- Executing change request "
                                    + "with description: "
                                    + change.getDescription());
                        }
                        change.execute();
                    }
                } finally {
                    _workspace.doneWriting();
                    setDeferringChangeRequests(previousDeferStatus);
                }
                
                // Change requests may have been queued during the execute.
                // Execute those by a recursive call.
                executeChangeRequests();
            }
        }
    }

    /** Run a complete execution of the contained model.  A complete
     *  execution consists of invocation of super.initialize(), repeated
     *  invocations of super.prefire(), super.fire(), and super.postfire(),
     *  followed by super.wrapup().  The invocations of prefire(), fire(),
     *  and postfire() are repeated until either the model indicates it
     *  is not ready to execute (prefire() returns false), or it requests
     *  a stop (postfire() returns false or stop() is called).
     *  Before running the complete execution, this method calls the
     *  director's transferInputs() method to read any available inputs.
     *  After running the complete execution, it calls transferOutputs().
     *  The subclass of this can set the <i>_isSubclassOfThis<i> to
     *  be true to call the fire method of the superclass of this.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("---- Firing RunCompositeActor, which will execute a subsystem.");
        }
        if (_isSubclassOfThis) {
            super.fire();
        } else {
        // Use the local director to transfer inputs.
        try {
            _workspace.getReadAccess();
            // Make sure that change requests are not executed when requested,
            // but rather only executed when executeChangeRequests() is called.
            setDeferringChangeRequests(true);

            Iterator inputPorts = inputPortList().iterator();
            while (inputPorts.hasNext() && !_stopRequested) {
                IOPort p = (IOPort)inputPorts.next();
                getDirector().transferInputs(p);
            }
        } finally {
            _workspace.doneReading();
        }
        if (_stopRequested) return;

        try {
            _executeInsideModel();
        } finally {
            try {
                wrapup();
            } finally {
                // Indicate that it is now safe to execute
                // change requests when they are requested.
                setDeferringChangeRequests(false);
            }
            if (!_stopRequested) {
                try {
                    _workspace.getReadAccess();
                    // Use the local director to transfer outputs.
                    Iterator outports = outputPortList().iterator();
                    while (outports.hasNext() && !_stopRequested) {
                        IOPort p = (IOPort)outports.next();
                        getDirector().transferOutputs(p);
                    }
                } finally {
                    _workspace.doneReading();
                }
            }
            if (_debugging) {
                _debug("---- Firing of RunCompositeActor is complete.");
            }
        }
        }//else
    }
    
    /** Initialize this actor, which in this case, does nothing. 
     *  The initialization of the submodel is accomplished in fire().
     *  The subclass of this can set the <i>_isSubclassOfThis<i> to
     *  be true to call the initailize method of the superclass of this.
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }
        //Call the initialize method of the superclass. 
        if (_isSubclassOfThis) {
            super.initialize();
        }
    }
    
    /** Return true, since this actor is always opaque.
     *  This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.
     */
    public boolean isOpaque() {
        return true;
    }
    
    /** Return true, indicating that execution can continue.
     *  The subclass of this can set the <i>_isSubclassOfThis<i> to
     *  be true to call the postfire method of the superclass of this.
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public boolean postfire() throws IllegalActionException {
        //Call the initialize method of the superclass. 
        if (_isSubclassOfThis) {
            return super.postfire();
        }
        return true;
    }
    
    /** Return true, indicating that this actor is always ready to fire.
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Request that given change be executed.   In this class,
     *  do not delegate the change request to the container, but
     *  execute the request immediately or record it, depending on
     *  whether setDeferChangeRequests() has been called. If
     *  setDeferChangeRequests() has been called with a true argument,
     *  then simply queue the request until either setDeferChangeRequests()
     *  is called with a false argument or executeChangeRequests() is called.
     *  If this object is already in the middle of executing a change
     *  request, then that execution is finished before this one is performed.
     *  Change listeners will be notified of success (or failure) of the
     *  request when it is executed.
     *  @param change The requested change.
     *  @see #executeChangeRequests()
     *  @see #setDeferChangeRequests(boolean)
     */
    public void requestChange(ChangeRequest change) {
        // Have to ensure that the _deferChangeRequests status and
        // the collection of change listeners doesn't change during
        // this execution.  But we don't want to hold a lock on the
        // this NamedObj during execution of the change because this
        // could lead to deadlock.  So we synchronize to _changeLock.
        synchronized(_changeLock) {
            // Queue the request.
            // Create the list of requests if it doesn't already exist
            if (_changeRequests == null) {
                _changeRequests = new LinkedList();
            }
            _changeRequests.add(change);
            if (!isDeferringChangeRequests()) {
                executeChangeRequests();
            }
        }
    }
    
    /** Override the base class to do nothing.
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }
        //Call the method of the superclass. 
        if (_isSubclassOfThis) {
            super.wrapup();
        }
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
   
    /** Call the initialize method of the superclass.  This is
     *  provided so that subclasses have a mechanism for doing
     *  this, since Java doesn't supported super.super.initialize().
     *  @exception IllegalActionException If super.initialize() throws it.
     */
    //protected void _callSuperInitialize() throws IllegalActionException {
      //  super.initialize();
    //}
    
    /** Run a complete execution of the contained model.  A complete
     *  execution consists of invocation of super.initialize(), repeated
     *  invocations of super.prefire(), super.fire(), and super.postfire(),
     *  followed by super.wrapup().  The invocations of prefire(), fire(),
     *  and postfire() are repeated until either the model indicates it
     *  is not ready to execute (prefire() returns false), or it requests
     *  a stop (postfire() returns false or stop() is called).
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    protected void _executeInsideModel() throws IllegalActionException {
        // FIXME: Should preinitialize() also be called?
         // FIXME: Reset time to zero.
         // NOTE: Use the superclass initialize() because this method overrides
         // initialize() and does not initialize the model.
         super.initialize();
            
            
         // Call iterate() until finish() is called or postfire()
         // returns false.
         _debug("-- RunCompositeActor beginning to iterate.");

         // FIXME: This result is not used... Should it be to determine postfire() result?
         _lastIterateResult = COMPLETED;
         while (!_stopRequested) {
             executeChangeRequests();
             if (super.prefire()) {
                 super.fire();
                 if (!super.postfire()) {
                     _lastIterateResult = STOP_ITERATING;
                     break;
                 }
             } else {
                 _lastIterateResult = NOT_READY;
                 break;
             }
         }
    }
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    
    /** Indicator of what the last call to iterate() returned. */
    private int _lastIterateResult = NOT_READY; 
    
    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                  ////
    /** This flag is used to indicate whether to call corresponding
     *  method of the superclass. This is
     *  provided so that subclasses have a mechanism for doing
     *  this, since Java doesn't supported super.super.initialize(), etc.
     */
    protected boolean _isSubclassOfThis = false;
}
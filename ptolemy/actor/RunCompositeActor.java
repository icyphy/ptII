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

import ptolemy.kernel.CompositeEntity;
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

@author Edward A. Lee
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
        getMoMLInfo().className = "ptolemy.actor.RunCompositeActor";
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
        getMoMLInfo().className = "ptolemy.actor.RunCompositeActor";
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
        getMoMLInfo().className = "ptolemy.actor.RunCompositeActor";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("---- Firing RunCompositeActor, which will execute a subsystem.");
        }
        // Use the local director to transfer inputs.
        try {
            _workspace.getReadAccess();
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
        } finally {
            super.wrapup();
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
    }
    
    /** Initialize this actor, which in this case, does nothing.
     *  The initialization of the submodel is accomplished in fire().
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
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
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public boolean postfire() throws IllegalActionException {
        return true;
    }
    
    /** Return true, indicating that this actor is always ready to fire.
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Override the base class to do nothing.
     *  @exception IllegalActionException Not thrown, but declared
     *   so the subclasses can throw it.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    
    /** Indicator of what the last call to iterate() returned. */
    private int _lastIterateResult = NOT_READY; 
}
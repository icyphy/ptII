/* A composite actor that executes a submodel in fire().

Copyright (c) 2003-2004 The Regents of the University of California.
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

package ptolemy.actor.lib.hoc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// RunCompositeActor
/**
   This is a composite actor that can executes the contained model
   completely, as if it were a top-level model, on each firing.
   This can be used to define an actor whose firing behavior
   is given by a complete execution of a submodel.
   <p>
   An instance of this actor can have ports added to it.  If it has
   input ports, then on each firing, before executing the referenced
   model, this actor will read an input token from the input port, if
   there is one, and use it to set the value of a top-level parameter
   in the referenced model that has the same name as the port, if there
   is one.  The simplest way to ensure that there is a matching parameter
   is to use a PortParameter for inputs.  However, this actor will work
   also for ordinary ports. In this case, if this actor has a
   parameter with the same name as the port, and it is an instance
   of Variable (or its derived class Parameter), then the token
   read at the input is moved into it using its setToken() method.
   Otherwise, if it is an instance of Settable, then a string representation
   of the token is copied using the setExpression() method.
   Input ports should not be multiports, and if they are, then
   all but the first channel will be ignored.
   <p>
   If this actor has output ports and the contained model is executed,
   then upon completion of that execution, if this actor has parameters
   whose names match those of the output ports, then the final value of
   those parameters is sent to the output ports. If such a parameter is an
   instance of Variable (or its derived class Parameter), then its
   contained token is sent to the output token. Otherwise, if it is an
   instance of Settable, then a string token is produced on the output
   with its value equal to that returned by getExpression() of the
   Settable. Output ports should not be multiports. If they are,
   then all but the first channel will be ignored.
   A typical use of this actor will use the SetVariable actor
   inside to define the value of the output port.
   <p>
   In preinitialize(), type constraints are set up so that input
   and output ports with (name) matching parameters are constrained
   to have compatible types. Note that if the ports or parameters
   are changed during execution, then it will be necessary to set
   up matching type constraints by hand.  Since this isn't possible
   to do from Vergil, the ports and parameters of this actor
   should not be changed using Vergil during execution.
   <p>
   The subclass of this may need to call a method, for example prefire(), 
   of  the superclass of this when execute the inside model. Since Java 
   doesn't supported super.super.prefire(), this class uses a boolean 
   variable <i>_isSubclassOfRunCompositeActor</i> to provide a mechanism to
   call the method of the superclass of this. To to so, Subclass of this can
   set the <i>_isSubclassOfRunCompositeActor</i> to be true.
   <p>
   This actor also overrides the requestChange() method and the 
   executeChangerRequests() method to execute the given change. It does not
   delegate the change request to the container, but executes the request 
   immediately or records it, depending on whether setDeferringChangeRequests()
   has been called with a true argument. 

   @author Edward A. Lee, Yang Zhao
   @version $Id$
   @since Ptolemy II 4.0
   @see ModelReference
   @see ptolemy.actor.lib.SetVariable
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (eal)
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
     *  @see #setDeferringChangeRequests(boolean)
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
                        // The change listeners should be those of this
                        // actor and any container that it has!
                        // FIXME: This is expensive... Better solution?
                        // We previously tried issuing a dummy change
                        // request to the container, but this caused big
                        // problems... (weird null-pointer expections
                        // deep in diva when making connections).
                        // Is it sufficient to just go to the top level?
                        List changeListeners = new LinkedList();
                        NamedObj container = getContainer();
                        while (container != null) {
                            List list = container.getChangeListeners();
                            if (list != null) {
                                changeListeners.addAll(list);
                            }
                            container = container.getContainer();
                        }
                        change.setListeners(changeListeners);

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
     *  The subclass of this can set the <i>_isSubclassOfRunCompositeActor<i> to
     *  be true to call the fire method of the superclass of this.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("---- Firing RunCompositeActor, which will execute a subsystem.");
        }
        if (_isSubclassOfRunCompositeActor) {
            super.fire();
            return;
        }
        try {
            // Make sure that change requests are not executed when requested,
            // but rather only executed when executeChangeRequests() is called.
            setDeferringChangeRequests(true);
            
            _readInputs();
            if (_stopRequested) return;

            _executeInsideModel();
            
        } finally {
            try {
                executeChangeRequests();
                wrapup();
            } finally {
                // Indicate that it is now safe to execute
                // change requests when they are requested.
                setDeferringChangeRequests(false);
            }
            if (!_stopRequested) {
                _writeOutputs();
            }
            if (_debugging) {
                _debug("---- Firing of RunCompositeActor is complete.");
            }
        }
    }

    /** Initialize this actor, which in this case, does nothing.
     *  The initialization of the submodel is accomplished in fire().
     *  The subclass of this can set the <i>_isSubclassOfRunCompositeActor<i> to
     *  be true to call the initialize method of the superclass of this.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }
        //Call the initialize method of the superclass.
        if (_isSubclassOfRunCompositeActor) {
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
     *  The subclass of this can set the <i>_isSubclassOfRunCompositeActor<i> to
     *  be true to call the postfire method of the superclass of this.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public boolean postfire() throws IllegalActionException {
        //Call the initialize method of the superclass.
        if (_isSubclassOfRunCompositeActor) {
            return super.postfire();
        }
        return true;
    }

    /** Return true, indicating that this actor is always ready to fire.
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }
    
    /** Override the base class to set type constraints between the
     *  output ports and parameters of this actor whose name matches
     *  the output port. If there is no such parameter, then create
     *  an instance of Variable with a matching name and set up the
     *  type constriants to that instance.  The type of the output
     *  port is constrained to be at least that of the parameter
     *  or variable.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        
        Iterator ports = outputPortList().iterator();
        while (ports.hasNext()) {
            TypedIOPort port = (TypedIOPort) ports.next();
            
            // Ensure that the production rate is one.
            // FIXME: This may not be right if there is no
            // actual source of data for this port (e.g. no
            // SetVariable actor).
            Variable rate = (Variable)port.getAttribute("tokenProductionRate");
            if (rate == null) {
                try {
                    rate = new Variable(port, "tokenProductionRate");
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            }
            rate.setToken(new IntToken(1));
            
            String portName = port.getName();
            Attribute attribute = getAttribute(portName);
            if (attribute == null) {
                try {
                    workspace().getWriteAccess();
                    attribute = new Variable(this, portName);
                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(ex);
                } finally {
                    workspace().doneWriting();
                }
            }
            // attribute is now assured to be non-null.
            if (attribute instanceof Variable) {
                port.setTypeAtLeast((Variable)attribute);
            } else {
                // Assume the port type must be a string.
                port.setTypeEquals(BaseType.STRING);
            }
        }
    }
    
    /** Request that given change be executed.   In this class,
     *  do not delegate the change request to the container, but
     *  execute the request immediately or record it, depending on
     *  whether setDeferringChangeRequests() has been called. If
     *  setDeferChangeRequests() has been called with a true argument,
     *  then simply queue the request until either setDeferChangeRequests()
     *  is called with a false argument or executeChangeRequests() is called.
     *  If this object is already in the middle of executing a change
     *  request, then that execution is finished before this one is performed.
     *  Change listeners will be notified of success (or failure) of the
     *  request when it is executed.
     *  @param change The requested change.
     *  @see #executeChangeRequests()
     *  @see #setDeferringChangeRequests(boolean)
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
     *  @exception IllegalActionException Not thrown in this base class,
     *  but declared so the subclasses can throw it.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }
        //Call the method of the superclass.
        if (_isSubclassOfRunCompositeActor) {
            super.wrapup();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

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
        // FIXME: Reset time to zero. How?
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
    ////                        protected variables                ////

    /** This flag is used to indicate whether to call corresponding
     *  method of the superclass. This is
     *  provided so that subclasses have a mechanism for doing
     *  this, since Java doesn't supported super.super.initialize(), etc.
     */
    // FIXME: This variable is misnamed.
    protected boolean _isSubclassOfRunCompositeActor = false;

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Iterate over input ports and read any available values into
     *  the referenced model parameters.
     *  @exception IllegalActionException If reading the ports or
     *   setting the parameters causes it.
     */
    private void _readInputs() throws IllegalActionException {
        // NOTE: This is an essentially exact copy of the code in ModelReference,
        // but this class and that one can't easily share a common base class.
        if (_debugging) {
            _debug("** Reading inputs (if any).");
        }
        Iterator ports = inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            if (port instanceof ParameterPort) {
                PortParameter parameter = ((ParameterPort)port).getParameter();
                if (_debugging) {
                    _debug("** Updating PortParameter: " + port.getName());
                }
                parameter.update();
                continue;
            }
            if (port.getWidth() > 0 && port.hasToken(0)) {
                Token token = port.get(0);
                Attribute attribute = getAttribute(port.getName());
                // Use the token directly rather than a string if possible.
                if (attribute instanceof Variable) {
                    if (_debugging) {
                        _debug("** Transferring input to parameter: " + port.getName());
                    }
                    ((Variable) attribute).setToken(token);
                } else if (attribute instanceof Settable) {
                    if (_debugging) {
                        _debug("** Transferring input as string to parameter: " + port.getName());
                    }
                    ((Settable) attribute).setExpression(token.toString());
                }
            }
        }
    }

    /** Iterate over output ports and read any available values from
     *  the referenced model parameters and produce them on the outputs.
     *  @exception IllegalActionException If reading the parameters or
     *   writing to the ports causes it.
     */
    private void _writeOutputs() throws IllegalActionException {
        // NOTE: This is an essentially exact copy of the code in ModelReference,
        // but this class and that one can't easily share a common base class.
        if (_debugging) {
            _debug("** Writing outputs (if any).");
        }
        Iterator ports = outputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            // Only write if the port has a connected channel.
            if (port.getWidth() > 0) {
                Attribute attribute = getAttribute(port.getName());
                // Use the token directly rather than a string if possible.
                if (attribute instanceof Variable) {
                    if (_debugging) {
                        _debug("** Transferring parameter to output: " + port.getName());
                    }
                    port.send(0, ((Variable) attribute).getToken());
                } else if (attribute instanceof Settable) {
                    if (_debugging) {
                        _debug("** Transferring parameter as string to output: " + port.getName());
                    }
                    port.send(
                            0,
                            new StringToken(
                                    ((Settable) attribute).getExpression()));
                }
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    /** Indicator of what the last call to iterate() returned. */
    private int _lastIterateResult = NOT_READY;
}

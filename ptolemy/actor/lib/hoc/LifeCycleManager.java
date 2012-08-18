/* A base class for actors that perform life-cycle management.

 Copyright (c) 2003-2011 The Regents of the University of California.
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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Changeable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// LifeCycleManager

/**
 This is a composite actor with some services for life-cycle management.

 FIXME: More.

 @author Edward A. Lee, Yang Zhao
 @version $Id$
 @since Ptolemy II 4.0
 @see ModelReference
 @see ptolemy.actor.lib.SetVariable
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class LifeCycleManager extends TypedCompositeActor {
    /** Construct an actor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */
    public LifeCycleManager() {
        super();
    }

    /** Construct a LifeCycleManager in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public LifeCycleManager(Workspace workspace) {
        super(workspace);
    }

    /** Construct a LifeCycleManager with a name and a container.
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
    public LifeCycleManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to delegate to the container AND
     *  also record the listener locally.
     *  @param listener The listener to add.
     *  @see #removeChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see Changeable
     */
    public void addChangeListener(ChangeListener listener) {
        NamedObj container = getContainer();

        if (container != null) {
            container.addChangeListener(listener);
        }
        synchronized (_changeLock) {
            if (_changeListeners == null) {
                _changeListeners = new LinkedList<WeakReference<ChangeListener>>();
            } else {
                // In case there is a previous instance, remove it.
                removeChangeListener(listener);
            }

            _changeListeners.add(0, new WeakReference(listener));
        }
    }

    /** Override the base class to not delegate up the hierarchy
     *  but rather to handle the request locally.
     *  @see #addChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see #isDeferringChangeRequests()
     *  @see Changeable
     */
    public void executeChangeRequests() {
        // Have to execute a copy of the change request list
        // because the list may be modified during execution.
        List<ChangeRequest> copy = _copyChangeRequestList();

        if (copy != null) {
            _executeChangeRequests(copy);
            // Change requests may have been queued during the execute.
            // Execute those by a recursive call.
            executeChangeRequests();
        }
    }

    /** Override the base class to not delegate to the container.
     *  @return True if change requests are being deferred.
     *  @see #setDeferringChangeRequests(boolean)
     *  @see Changeable
     */
    public boolean isDeferringChangeRequests() {
        return _deferChangeRequests;
    }

    /** Override the base class to remove the listener in
     *  the container AND locally.
     *  @param listener The listener to remove.
     *  @see #addChangeListener(ChangeListener)
     *  @see Changeable
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        NamedObj container = getContainer();
        if (container != null) {
            container.removeChangeListener(listener);
        }
        synchronized (_changeLock) {
            if (_changeListeners != null) {
                ListIterator<WeakReference<ChangeListener>> listeners = _changeListeners
                        .listIterator();

                while (listeners.hasNext()) {
                    WeakReference<ChangeListener> reference = listeners
                            .next();

                    if (reference.get() == listener) {
                        listeners.remove();
                    } else if (reference.get() == null) {
                        listeners.remove();
                    }
                }
            }
        }
    }

    /** Override the base class to not delegate up the hierarchy.
     *  @param change The requested change.
     *  @see #executeChangeRequests()
     *  @see #setDeferringChangeRequests(boolean)
     *  @see Changeable
     */
    public void requestChange(ChangeRequest change) {
        // Have to ensure that
        // the collection of change listeners doesn't change during
        // this execution.  But we don't want to hold a lock on the
        // this NamedObj during execution of the change because this
        // could lead to deadlock.  So we synchronize to _changeLock.
        List<ChangeRequest> copy = null;
        synchronized (_changeLock) {
            // Queue the request.
            // Create the list of requests if it doesn't already exist
            if (_changeRequests == null) {
                _changeRequests = new LinkedList<ChangeRequest>();
            }

            _changeRequests.add(change);
            if (!_deferChangeRequests) {
                copy = _copyChangeRequestList();
            }
        }

        // Do not want to hold the _changeLock while
        // executing change requests. See comments inside
        // executeChangeRequests().
        if (copy != null) {
            _executeChangeRequests(copy);
        }
    }

    /** Override the base class to not delegate to the container.
     *  @param isDeferring If true, defer change requests.
     *  @see #addChangeListener(ChangeListener)
     *  @see #executeChangeRequests()
     *  @see #isDeferringChangeRequests()
     *  @see #requestChange(ChangeRequest)
     *  @see Changeable
     */
    public void setDeferringChangeRequests(boolean isDeferring) {
        // Make sure to avoid modification of this flag in the middle
        // of a change request or change execution.
        List<ChangeRequest> copy = null;
        synchronized (_changeLock) {
            _deferChangeRequests = isDeferring;

            if (isDeferring == false) {
                // Must not hold _changeLock while executing change requests.
                copy = _copyChangeRequestList();
            }
        }
        if (copy != null) {
            _executeChangeRequests(copy);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Run a complete execution of the contained model.  A complete
     *  execution consists of invocation of super.initialize(), repeated
     *  invocations of super.prefire(), super.fire(), and super.postfire(),
     *  followed by super.wrapup().  The invocations of prefire(), fire(),
     *  and postfire() are repeated until either the model indicates it
     *  is not ready to execute (prefire() returns false), or it requests
     *  a stop (postfire() returns false or stop() is called).
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     *  @return One of COMPLETED, STOP_ITERATING, or NOT_READY to
     *   indicate that either the execution completed or stop
     *   was externally requested, stopped because
     *   postfire() returned false, or stopped because prefire() returned
     *   false, respectively.
     */
    protected int _executeInsideModel() throws IllegalActionException {
        try {
            // Make sure that change requests are not executed when requested,
            // but rather only executed when executeChangeRequests() is called.
            setDeferringChangeRequests(true);
            
            Director insideDirector = getDirector();
            Director outsideDirector = getExecutiveDirector();
            if (insideDirector == outsideDirector) {
                throw new IllegalActionException(this, "An inside director is required to execute the inside model.");
            }
            // Force the inside director to behave as if it were at the top level.
            insideDirector.setEmbedded(false);

            _readInputs();

            if (_stopRequested) {
                return COMPLETED;
            }

            // FIXME: Reset time to zero. How?
            // NOTE: Use the superclass initialize() because this method overrides
            // initialize() and does not initialize the model.
            super.initialize();

            // Call iterate() until finish() is called or postfire()
            // returns false.
            _debug("-- Beginning to iterate.");

            int lastIterateResult = COMPLETED;

            while (!_stopRequested) {
                executeChangeRequests();

                if (super.prefire()) {
                    // Cannot use super.fire() here because it does
                    // some inappropriate things like reading port parameters
                    // and transferring inputs.
                    _fireInsideModel();

                    if (!super.postfire()) {
                        lastIterateResult = STOP_ITERATING;
                        break;
                    }
                } else {
                    lastIterateResult = NOT_READY;
                    break;
                }
            }

            return lastIterateResult;
        } finally {
            try {
                executeChangeRequests();
                super.wrapup();
            } finally {
                // Indicate that it is now safe to execute
                // change requests when they are requested.
                setDeferringChangeRequests(false);
            }

            if (!_stopRequested) {
                _writeOutputs();
            }

            if (_debugging) {
                _debug("---- Firing is complete.");
            }
        }
    }

    /** Invoke the fire() method of its local director.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it.
     */
    protected void _fireInsideModel() throws IllegalActionException {
        if (_debugging) {
            _debug("Firing the inside model.");
        }

        try {
            _workspace.getReadAccess();
            if (!_stopRequested) {
                getDirector().fire();
            }
        } finally {
            _workspace.doneReading();
        }
        if (_debugging) {
            _debug("Done firing inside model.");
        }
    }

    /** Iterate over input ports and read any available values into
     *  the referenced model parameters.
     *  @exception IllegalActionException If reading the ports or
     *   setting the parameters causes it.
     */
    protected void _readInputs() throws IllegalActionException {
        // NOTE: This is an essentially exact copy of the code in ModelReference,
        // but this class and that one can't easily share a common base class.
        if (_debugging) {
            _debug("** Reading inputs (if any).");
        }

        Iterator ports = inputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            if (port instanceof ParameterPort) {
                PortParameter parameter = ((ParameterPort) port).getParameter();

                parameter.update();
                // Have to make sure we set the persistent value
                // of the parameter, not just the current value, otherwise
                // it will be reset when the model is initialized.
                parameter.setExpression(parameter.getToken().toString());
                
                if (_debugging) {
                    _debug("** Updated PortParameter: " + port.getName()
                            + " to value "
                            + parameter.getToken());
                }

                continue;
            }

            if ((port.isOutsideConnected()) && port.hasToken(0)) {
                Token token = port.get(0);
                Attribute attribute = getAttribute(port.getName());

                // Use the token directly rather than a string if possible.
                if (attribute instanceof Variable) {
                    if (_debugging) {
                        _debug("** Transferring input to parameter: "
                                + port.getName());
                    }

                    ((Variable) attribute).setToken(token);
                } else if (attribute instanceof Settable) {
                    if (_debugging) {
                        _debug("** Transferring input as string to parameter: "
                                + port.getName());
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
    protected void _writeOutputs() throws IllegalActionException {
        // NOTE: This is an essentially exact copy of the code in ModelReference,
        // but this class and that one can't easily share a common base class.
        if (_debugging) {
            _debug("** Writing outputs (if any).");
        }

        Iterator ports = outputPortList().iterator();

        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();

            // Only write if the port has a connected channel.
            if (port.isOutsideConnected()) {
                Attribute attribute = getAttribute(port.getName());

                // Use the token directly rather than a string if possible.
                if (attribute instanceof Variable) {
                    if (_debugging) {
                        _debug("** Transferring parameter to output: "
                                + port.getName()
                                + " ("
                                + ((Variable) attribute).getToken()
                                + ")");
                    }

                    port.send(0, ((Variable) attribute).getToken());
                } else if (attribute instanceof Settable) {
                    if (_debugging) {
                        _debug("** Transferring parameter as string to output: "
                                + port.getName()
                                + " ("
                                + ((Settable) attribute).getExpression()
                                + ")");
                    }

                    port.send(
                            0,
                            new StringToken(((Settable) attribute)
                                    .getExpression()));
                }
            }
        }
    }
}

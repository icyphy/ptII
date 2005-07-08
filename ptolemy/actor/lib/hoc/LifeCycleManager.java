/* A base class for actors that perform life-cycle management.

 Copyright (c) 2003-2005 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
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

    /** Execute requested changes. In this class,
     *  do not delegate the change request to the container, but
     *  execute the request immediately.  Listeners will be notified
     *  of success or failure.
     *  @see #addChangeListener(ChangeListener)
     *  @see #requestChange(ChangeRequest)
     *  @see #setDeferringChangeRequests(boolean)
     */
    public void executeChangeRequests() {
        synchronized (_changeLock) {
            if ((_changeRequests != null) && (_changeRequests.size() > 0)) {
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
                    previousDeferStatus = setDeferringChangeRequests(true);

                    while (requests.hasNext()) {
                        ChangeRequest change = (ChangeRequest) requests.next();
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

    /** Return true, since this actor is always opaque.
     *  This method is <i>not</i> synchronized on the workspace,
     *  so the caller should be.
     */
    public boolean isOpaque() {
        // FIXME: Override getDirector() to ensure that there is always a director.
        return true;
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
        synchronized (_changeLock) {
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
                    super.fire();

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

                if (_debugging) {
                    _debug("** Updating PortParameter: " + port.getName());
                }

                parameter.update();
                continue;
            }

            if ((port.getWidth() > 0) && port.hasToken(0)) {
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
            if (port.getWidth() > 0) {
                Attribute attribute = getAttribute(port.getName());

                // Use the token directly rather than a string if possible.
                if (attribute instanceof Variable) {
                    if (_debugging) {
                        _debug("** Transferring parameter to output: "
                                + port.getName());
                    }

                    port.send(0, ((Variable) attribute).getToken());
                } else if (attribute instanceof Settable) {
                    if (_debugging) {
                        _debug("** Transferring parameter as string to output: "
                                + port.getName());
                    }

                    port.send(0, new StringToken(((Settable) attribute)
                            .getExpression()));
                }
            }
        }
    }
}

/* An atomic actor that executes a model specified by a file or URL.

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

package ptolemy.actor.lib.hoc;

import java.net.URL;
import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// ModelReference
/**
This is an atomic actor that can execute a model specified by
a file or URL. This can be used to define an actor whose firing behavior
is given by a complete execution of another model.
<p>
An instance of this actor can have ports added to it.  If it has
input ports, then on each firing, before executing the referenced
model, this actor will read an input token from the input port, if
there is one, and use it to set the value of a top-level parameter
in the referenced model that has the same name as the port, if there
is one.  If the top-level parameter of the referenced model is an
instance of Variable (or its derived class Parameter), then the token
read at the input is moved into it using its setToken() method.
Otherwise, if it is an instance of Settable, then a string representation
of the token is copied using the setExpression() method.
Input ports should not be multiports, and if they are, then
all but the first channel will be ignored.
<p>
If this actor has output ports and the referenced model is executed,
then upon completion of that execution, this actor looks for top-level
parameters in the referenced model whose names match those of the output
ports. If there are such parameters, then the final value of those
parameters is sent to the output ports. If such a parameter is an
instance of Variable (or its derived class Parameter), then its
contained token is sent to the output. Otherwise, if it is an
instance of Settable, then a string token is produced on the output
with its value equal to that returned by getExpression() of the
Settable.  If the model is executed in the calling thread, then
the outputs will be produced before the fire() method returns.
If the model is executed in a new thread, then the outputs will
be produced whenever that thread completes execution of the model.
Output ports should not be multiports. If they are, then all but
the first channel will be ignored.
Normally, when you create output ports for this actor, you will have
to manually set the type.  There is no type inference from the
parameter of the referenced model.
<p>
A suite of parameters is provided to control what happens when this
actor executes:
<ul>
<li> <i>executionOnFiring</i>: 
The value of this string attribute determines what execution
happens when the fire() method is invoked.  The recognized
values are:
<ul>
<li> "run in calling thread" (the default)
<li> "run in a new thread"
<li> "do nothing".
</ul>
If execution in a separate thread is selected, then the execution can
optionally be stopped by the postfire() method (see below). If the model
is still executing the next time fire() is called on this actor, then
the fire() method will wait for completion of the first execution.
If an exception occurs during a run in another thread, then it will
be reported at the next invocation of fire(), postfire(), or wrapup().
Note that if you select "run in a new thread" and this actor has
output ports, the data is produced to those output ports when
the execution completes, whenever that might be.  This may make
output ports difficult to use in some domains.

<li> <i>lingerTime</i>:
The amount of time (in milliseconds) to linger in the fire()
method of this actor.  This is a long that defaults to 0L.
If the model is run, then the linger occurs after the run
is complete (if the run occurs in the calling thread) or
after the run starts (if the run occurs in a separate thread).
This can be used, for example, to run a model for a specified
amount of time, then ask it to finish() and continue.

<li> <i>modelFileOrURL</i>:
The file name or URL of the model that this actor will execute.

<li> <i>postfireAction</i>:
The value of this string attribute determines what happens
in the postfire() method.  The recognized values are:
<ul>
<li> "do nothing" (the default)
<li> "stop executing"
</ul>
The "stop executing" choices will only have an effect if
if <i>executionOnFiring</i> is set to "run in a new thread".
This can be used, for example, to run a model for a specified
amount of time, and then stop it.
</ul>
<p>
There are currently a number of serious limitations:
<ul>
<li>
FIXME: Pausing the referring model doesn't pause the referenced model.
<li>
FIXME: Need options for error handling.
</ul>
<P>

@author Edward A. Lee
@version $Id$
*/
public class ModelReference
        extends TypedAtomicActor
        implements ExecutionListener {

    /** Construct a ModelReference with a name and a container.
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
    public ModelReference(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: Need a way to specify a filter for the file browser.
        modelFileOrURL = new FileParameter(this, "modelFileOrURL");

        // Create the executionOnFiring parameter.
        executionOnFiring = new StringParameter(this, "executionOnFiring");
        executionOnFiring.setExpression("run in calling thread");
        executionOnFiring.addChoice("run in calling thread");
        executionOnFiring.addChoice("run in a new thread");
        executionOnFiring.addChoice("do nothing");

        // Create the lingerTime parameter.
        lingerTime = new Parameter(this, "lingerTime");
        lingerTime.setTypeEquals(BaseType.LONG);
        lingerTime.setExpression("0L");

        // Create the postfireAction parameter.
        postfireAction = new StringParameter(this, "postfireAction");
        postfireAction.setExpression("do nothing");
        postfireAction.addChoice("do nothing");
        postfireAction.addChoice("stop executing");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The value of this string parameter determines what execution
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "run in calling thread" (the default)
     *  <li> "run in a new thread"
     *  <li> "do nothing".
     *  </ul>
     */
    public StringParameter executionOnFiring;

    /** The amount of time (in milliseconds) to linger in the fire()
     *  method of this actor.  This is a long that defaults to 0L.
     *  If the model is run, then the linger occurs after the run
     *  is complete (if the run occurs in the calling thread) or
     *  after the run starts (if the run occurs in a separate thread).
     */
    public Parameter lingerTime;

    /** The file name or URL of the model that this actor represents.
     */
    public FileParameter modelFileOrURL;

    /** The value of this string attribute determines what happens
     *  in the postfire() method.  The recognized values are:
     *  <ul>
     *  <li> "do nothing" (the default)
     *  <li> "stop executing"
     *  </ul>
     *  The "stop executing" choices will only have an effect if
     *  if <i>executionOnFiring</i> is set to "run in a new thread".
     *  This can be used, for example, to run a model for a specified
     *  amount of time, and then stop it.
     */
    public StringParameter postfireAction;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to open the model specified if the
     *  attribue is modelFileOrURL, or for other parameters, to cache
     *  their values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == modelFileOrURL) {
            // Open the file and read the MoML to create a model.
            URL url = modelFileOrURL.asURL();
            if (url != null) {
                // By specifying no workspace argument to the parser, we
                // are asking it to create a new workspace for the referenced
                // model.  This is necessary because the execution of that
                // model will proceed through its own sequences, and it
                // will need to get write permission on the workspace.
                // Particularly if it is executing in a new thread, then
                // during the fire() method of this actor it would be
                // inappropriate to grant write access on the workspace
                // of this actor.
                MoMLParser parser = new MoMLParser();
                try {
                    _model = parser.parse(null, url);
                } catch (Exception ex) {
                    throw new IllegalActionException(
                        this,
                        ex,
                        "Failed to read model.");
                }
                // Create a manager, if appropriate.
                if (_model instanceof CompositeActor) {
                    _manager = new Manager(_model.workspace(), "Manager");
                    ((CompositeActor)_model).setManager(_manager);
                    if (_debugging) {
                        _debug("** Created new manager.");
                    }
                }
            } else {
                // URL is null... delete the current model.
                _model = null;
                _manager = null;
                _throwable = null;
            }
        } else if (attribute == executionOnFiring) {
            String executionOnFiringValue = executionOnFiring.stringValue();
            if (executionOnFiringValue.equals("run in calling thread")) {
                _executionOnFiringValue = _RUN_IN_CALLING_THREAD;
            } else if (executionOnFiringValue.equals("run in a new thread")) {
                _executionOnFiringValue = _RUN_IN_A_NEW_THREAD;
            } else if (executionOnFiringValue.equals("do nothing")) {
                _executionOnFiringValue = _DO_NOTHING;
            } else {
                throw new IllegalActionException(this,
                "Unrecognized option for executionOnFiring: " + executionOnFiringValue);
            }
        } else if (attribute == postfireAction) {
            String postfireActionValue = postfireAction.stringValue();
            if (postfireActionValue.equals("do nothing")) {
                _postfireActionValue = _DO_NOTHING;
            } else if (postfireActionValue.equals("stop executing")) {
                _postfireActionValue = _STOP_EXECUTING;
            } else {
                throw new IllegalActionException(this,
                "Unrecognized value for postfireAction: " + postfireActionValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to ensure that private variables are reset to null.
     *  @return A new instance of ModelReference.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ModelReference newActor = (ModelReference) super.clone(workspace);
        newActor._manager = null;
        newActor._model = null;
        newActor._throwable = null;
        return newActor;
    }

    /** React to the fact that execution has failed by unregistering
     *  as an execution listener and by allowing subsequent executions.
     *  Report an execution failure at the next opportunity.
     *  This method will be called when an exception or error
     *  is caught by a manager during a run in another thread
     *  of the referenced model.
     *  @param manager The manager controlling the execution.
     *  @param throwable The throwable to report.
     */
    public synchronized void executionError(
        Manager manager,
        Throwable throwable) {
        _throwable = throwable;
        _executing = false;
        // NOTE: Can't remove these now!  The list is being
        // currently used to notify me!
        // manager.removeExecutionListener(this);
        manager.removeDebugListener(this);
        notifyAll();
    }

    /** React to the fact that execution is finished by unregistering
     *  as an execution listener and by allowing subsequent executions.
     *  This is called when an execution of the referenced
     *  model in another thread has finished and the wrapup sequence
     *  has completed normally. The number of successfully completed
     *  iterations can be obtained by calling getIterationCount()
     *  on the manager.
     *  @param manager The manager controlling the execution.
     */
    public synchronized void executionFinished(Manager manager) {
        _executing = false;
        // NOTE: Can't remove these now!  The list is being
        // currently used to notify me!
        // manager.removeExecutionListener(this);
        manager.removeDebugListener(this);
        notifyAll();
    }

    /** Run a complete execution of the referenced model.  A complete
     *  execution consists of invocation of super.initialize(), repeated
     *  invocations of super.prefire(), super.fire(), and super.postfire(),
     *  followed by super.wrapup().  The invocations of prefire(), fire(),
     *  and postfire() are repeated until either the model indicates it
     *  is not ready to execute (prefire() returns false), or it requests
     *  a stop (postfire() returns false or stop() is called).
     *  Before running the complete execution, this method examines input
     *  ports, and if they are connected, have data, and if the referenced
     *  model has a top-level parameter with the same name, then one token
     *  is read from the input port and used to set the value of the
     *  parameter in the referenced model.
     *  After running the complete execution, if there are any output ports,
     *  then this method looks for top-level parameters in the referenced
     *  model with the same name as the output ports, and if there are any,
     *  reads their values and produces them on the output.
     *  If no model has been specified, then this method does nothing.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_throwable != null) {
            Throwable throwable = _throwable;
            _throwable = null;
            throw new IllegalActionException(
                this,
                throwable,
                "Run in a new thread threw an exception "
                + "on the previous firing.");
        }

        if (_model instanceof CompositeActor) {
            CompositeActor executable = (CompositeActor) _model;

            _manager = executable.getManager();
            if (_manager == null) {
                throw new InternalErrorException("No manager!");
            }
            if (_debugging) {
                _manager.addDebugListener(this);
                Director director = executable.getDirector();
                if (director != null) {
                    director.addDebugListener(this);
                }
            } else {
                _manager.removeDebugListener(this);
                Director director = executable.getDirector();
                if (director != null) {
                    director.removeDebugListener(this);
                }
            }
            // If there is a previous execution, then wait for it to finish.
            // Avoid the synchronize block if possible.
            if (_executing) {
                synchronized (this) {
                    while (_executing) {
                        try {
                            if (_debugging) {
                                _debug("** Waiting for previous execution to finish.");
                            }
                            // Use workspace version of wait to release
                            // read permission on the workspace.
                            workspace().wait(this);
                        } catch (InterruptedException ex) {
                            // Cancel subsequent execution.
                            getManager().finish();
                            return;
                        }
                    }
                    if (_debugging) {
                        _debug("** Previous execution has finished.");
                    }
                }
            }
            // Iterate over input ports and read any available values into
            // the referenced model parameters.
            if (_debugging) {
                _debug("** Reading inputs (if any).");
            }
            _readInputs();

            if (_executionOnFiringValue == _RUN_IN_CALLING_THREAD) {
                if (_debugging) {
                    _debug("** Executing referenced model in the calling thread.");
                }
                try {
                    _manager.execute();
                } catch (KernelException ex) {
                    throw new IllegalActionException(this, ex,
                    "Execution failed.");
                }
                if (_debugging) {
                    _debug("** Writing outputs (if any).");
                }
                _writeOutputs();
            } else if (_executionOnFiringValue == _RUN_IN_A_NEW_THREAD) {
                // Listen for exceptions. The listener is
                // removed in the listener methods, executionError()
                // and executionFinished().
                if (_debugging) {
                    _debug("** Creating a new thread to execute the model.");
                }
                _manager.addExecutionListener(this);
                    
                // Create a thread.  Can't directly use _manager.startRun()
                // because we need to write outputs upon completion.
                if (_manager.getState() != Manager.IDLE) {
                    throw new IllegalActionException(this,
                            "Cannot start an execution. "
                            + "Referenced model is "
                            + _manager.getState().getDescription());
                }
                // NOTE: There is a possible race condition. We would like to
                // set this within the calling thread to avoid race conditions
                // where finish() might be called before the spawned thread
                // actually starts up. But the variable is not accessible.
                // _manager._finishRequested = false;
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            if (_debugging) {
                                _debug("** Executing model in a new thread.");
                            }
                            _manager.execute();
                            if (_debugging) {
                                _debug("** Writing outputs (if any).");
                            }
                            _writeOutputs();
                        } catch (Throwable throwable) {
                            // If running tried to load in some native code using JNI
                            // then we may get an Error here
                            _manager.notifyListenersOfThrowable(throwable);
                        // } finally {
                            // NOTE: Race condition!  postfire() sets _manager to null.
                            // So now we do this in postfire.
                            // _manager.removeExecutionListener(ModelReference.this);
                        }
                    }
                };
                // Priority set to the minimum to get responsive UI during execution.
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();   
            }
            long lingerTimeValue =
                ((LongToken) lingerTime.getToken()).longValue();
            if (lingerTimeValue > 0L) {
                try {
                    if (_debugging) {
                        _debug("** Lingering for " + lingerTimeValue + " milliseconds.");
                    }
                    _lingeringThread = Thread.currentThread();
                    Thread.sleep(lingerTimeValue);
                } catch (InterruptedException ex) {
                    // Ignore.
                } finally {
                    _lingeringThread = null;
                }
            }
        }
    }

    /** Report in debugging statements that the manager state has changed.
     *  This method is called if the referenced model
     *  is executed in another thread and the manager changes state.
     *  @param manager The manager controlling the execution.
     *  @see Manager#getState()
     */
    public void managerStateChanged(Manager manager) {
        if (_debugging) {
            _debug("Referenced model manager state: " + manager.getState());
        }
    }

    /** Override the base class to perform requested postfire actions.
     *  @return Whatever the superclass returns (probably true).
     */
    public boolean postfire() throws IllegalActionException {
        if (_postfireActionValue == _STOP_EXECUTING
                && _manager != null) {
            if (_debugging) {
                _debug("** Calling finish() on the Manager to request termination.");
            }
            _manager.finish();
            // Wait for the finish.
            if (_debugging) {
                _debug("** Waiting for completion of execution.");
            }
            _manager.waitForCompletion();
        }

        // Test auto/ModelReference2.xml seems to end up here with
        // _manager == null
        if (_manager != null) {
            // If we specified to run in a new thread, then we are listening.
            // If we didn't, this is harmless.
            _manager.removeExecutionListener(ModelReference.this);
            _manager = null;
        }

        return super.postfire();
    }

    /** Override the base class to call stop() on the referenced model.
     */
    public void stop() {
        if (_model instanceof Executable) {
            ((Executable)_model).stop();
        }
        if (_lingeringThread != null) {
            _lingeringThread.interrupt();
        }
        super.stop();
    }

    /* Override the base class to call stopFire() on the referenced model.
     */
    public void stopFire() {
        if (_model instanceof Executable) {
            ((Executable)_model).stopFire();
        }
        if (_lingeringThread != null) {
            _lingeringThread.interrupt();
        }
        super.stopFire();
    }

    /** Override the base class to call terminate() on the referenced model.
     */
    public void terminate() {
        if (_model instanceof Executable) {
            ((Executable)_model).terminate();
        }
        super.terminate();
    }

    /** Report an exception if it occurred in a background run.
     *  @exception IllegalActionException If there is no director, or if
     *   a background run threw an exception.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_throwable != null) {
            Throwable throwable = _throwable;
            _throwable = null;
            throw new IllegalActionException(
                this,
                throwable,
                "Background run threw an exception");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////

    /** Iterate over input ports and read any available values into
     *  the referenced model parameters.
     *  @exception IllegalActionException If reading the ports or
     *   setting the parameters causes it.
     */
    private void _readInputs() throws IllegalActionException {
        Iterator ports = inputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            if (port.getWidth() > 0 && port.hasToken(0)) {
                Token token = port.get(0);
                Attribute attribute = _model.getAttribute(port.getName());
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
        Iterator ports = outputPortList().iterator();
        while (ports.hasNext()) {
            IOPort port = (IOPort) ports.next();
            // Only write if the port has a connected channel.
            if (port.getWidth() > 0) {
                Attribute attribute = _model.getAttribute(port.getName());
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
    ////                        protected variables                ////

    /** The model. */
    protected NamedObj _model;
    
    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // Possible values for executionOnFiring.
    private static int _DO_NOTHING = 0;
    private static int _RUN_IN_CALLING_THREAD = 1;
    private static int _RUN_IN_A_NEW_THREAD = 2;
    
    /** The value of the executionOnFiring parameter. */
    private transient int _executionOnFiringValue = _RUN_IN_CALLING_THREAD;

    // Flag indicating that the previous execution is in progress.
    private transient boolean _executing = false;

    /** Indicator of what the last call to iterate() returned. */
    private transient int _lastIterateResult = NOT_READY;
    
    /** Reference to a thread that is lingering. */
    private Thread _lingeringThread = null;

    /** The manager currently managing execution. */
    private Manager _manager = null;

    /** The value of the postfireAction parameter. */
    private transient int _postfireActionValue = _DO_NOTHING;

    // Possible values for postfireAction (plus _DO_NOTHING,
    // which is defined above).
    private static int _STOP_EXECUTING = 1;

    // Error from a previous run.
    private transient Throwable _throwable = null;
}

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

package ptolemy.vergil.actor.lib;

import java.net.URL;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.style.ChoiceStyle;
import ptolemy.data.LongToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLParser;
import ptolemy.vergil.basic.ExtendedGraphFrame;

//////////////////////////////////////////////////////////////////////////
//// ModelReference
/**
An atomic actor that can execute and/or open a model specified by
a file or URL.
<p>
FIXME: More details.

If execution in a separate thread is selected, then the execution can be
stopped by the postfire() method (FIXME... describe better). A subsequent
invocation of the fire() method will wait for completion of the first
execution...  FIXME
If an exception occurs during a run in another thread, then it will
be reported at the next invocation of fire() or postfire().

FIXME: Note that using full screen is dangerous.

FIXME: Note that can use to execute infinite running models by executing
in a new thread and specifying a linger time.

FIXME: Pausing the referring model doesn't pause the referenced model.

FIXME: Closing the master model doesn't close open referenced models.

FIXME: Close option fails.

FIXME: Need options for error handling.
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
        modelFileOrURL = new FileAttribute(this, "modelFileOrURL");

        // Create the openOnFiring parameter.
        openOnFiring = new StringAttribute(this, "openOnFiring");
        // Set the options for the parameters.
        ChoiceStyle style2 = new ChoiceStyle(openOnFiring, "choiceStyle");
        new StringAttribute(style2, "doNotOpen").setExpression("do not open");
        new StringAttribute(style2, "openInVergil").setExpression(
            "open in Vergil");
        new StringAttribute(style2, "openInVergilFullScreen").setExpression(
            "open in Vergil (full screen)");
        new StringAttribute(style2, "openRunControlPanel").setExpression(
            "open run control panel");

        // Create the executionOnFiring parameter.
        executionOnFiring = new StringAttribute(this, "executionOnFiring");
        // Set the options for the parameters.
        ChoiceStyle style = new ChoiceStyle(executionOnFiring, "choiceStyle");
        new StringAttribute(style, "runInCallingThread").setExpression(
            "run in calling thread");
        new StringAttribute(style, "runInNewThread").setExpression(
            "run in a new thread");
        new StringAttribute(style, "doNothing").setExpression("do nothing");

        // Create the lingerTime parameter.
        lingerTime = new Parameter(this, "lingerTime");
        lingerTime.setTypeEquals(BaseType.LONG);
        lingerTime.setExpression("0L");

        // Create the postfireAction parameter.
        postfireAction = new StringAttribute(this, "postfireAction");
        // Set the options for the parameters.
        ChoiceStyle style3 = new ChoiceStyle(postfireAction, "choiceStyle");
        new StringAttribute(style3, "doNothing").setExpression("do nothing");
        new StringAttribute(style3, "closeVergilGraph").setExpression(
            "close Vergil graph");
        new StringAttribute(style3, "stopExecuting").setExpression(
            "stop executing");
        new StringAttribute(
            style3,
            "stopExecutingAndCloseInVergil").setExpression(
            "stop executing and close Vergil graph");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The value of this string attribute determines what execution
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "run in calling thread" (the default)
     *  <li> "run in a new thread"
     *  <li> "do nothing".
     *  </ul>
     */
    public StringAttribute executionOnFiring;

    /** The amount of time (in milliseconds) to linger in the fire()
     *  method of this actor.  This is a long that defaults to 0L.
     *  If the model is run, then the linger occurs after the run
     *  is complete (if the run occurs in the calling thread) or
     *  after the run starts (if the run occurs in a separate thread).
     */
    public Parameter lingerTime;

    /** The file name or URL of the model that this actor represents.
     */
    public FileAttribute modelFileOrURL;

    /** The value of this string attribute determines what open
     *  happens when the fire() method is invoked.  The recognized
     *  values are:
     *  <ul>
     *  <li> "do not open" (the default)
     *  <li> "open in Vergil"
     *  <li> "open in Vergil (full screen)"
     *  <li> "open run control panel"
     *  </ul>
     */
    public StringAttribute openOnFiring;

    /** The value of this string attribute determines what happens
     *  in the postfire() method.  The recognized values are:
     *  <ul>
     *  <li> "do nothing" (the default)
     *  <li> "close Vergil graph"
     *  <li> "stop executing"
     *  <li> "stop executing and close Vergil graph"
     *  </ul>
     *  The "stop executing" choices will only have an effect if
     *  if <i>executionOnFiring</i> is set to "run in a new thread".
     *  This can be used, for example, to run a model for a specified
     *  amount of time, and then stop it.
     */
    public StringAttribute postfireAction;

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
                    // FIXME
                    System.out.println("Created new manager: " + _manager);
                }
            }
        } else if (attribute == executionOnFiring) {
            String executionOnFiringValue = executionOnFiring.getExpression();
            // FIXME:
            System.out.println("****: " + getFullName() + " " + executionOnFiringValue);
            if (executionOnFiringValue.equals("run in calling thread")) {
                _executionOnFiringValue = _RUN_IN_CALLING_THREAD;
            } else if (executionOnFiringValue.equals("run in a new thread")) {
                _executionOnFiringValue = _RUN_IN_A_NEW_THREAD;
            } else {
                _executionOnFiringValue = _DO_NOTHING;
            }
        } else if (attribute == openOnFiring) {
            String openOnFiringValue = openOnFiring.getExpression();
            if (openOnFiringValue.equals("do not open")) {
                _openOnFiringValue = _DO_NOT_OPEN;
            } else if (openOnFiringValue.equals("open in Vergil")) {
                _openOnFiringValue = _OPEN_IN_VERGIL;
            } else if (
                openOnFiringValue.equals("open in Vergil (full screen)")) {
                _openOnFiringValue = _OPEN_IN_VERGIL_FULL_SCREEN;
            } else if (openOnFiringValue.equals("open run control panel")) {
                _openOnFiringValue = _OPEN_RUN_CONTROL_PANEL;
            }
        } else if (attribute == postfireAction) {
            String postfireActionValue = postfireAction.getExpression();
            if (postfireActionValue.equals("do nothing")) {
                _postfireActionValue = _DO_NOTHING;
            } else if (postfireActionValue.equals("close Vergil graph")) {
                _postfireActionValue = _CLOSE_VERGIL_GRAPH;
            } else if (postfireActionValue.equals("stop executing")) {
                _postfireActionValue = _STOP_EXECUTING;
            } else if (
                postfireActionValue.equals(
                    "stop executing and close Vergil graph")) {
                _postfireActionValue = _STOP_EXECUTING_AND_CLOSE_VERGIL_GRAPH;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    // FIXME: clone method needed.  Make variables transient.

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
        manager.removeExecutionListener(this);
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
        manager.removeExecutionListener(this);
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
     *  Before running the complete execution, this method calls the
     *  director's transferInputs() method to read any available inputs.
     *  After running the complete execution, it calls transferOutputs().
     *  If no model has been specified, then this method does nothing.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("---- Firing ModelReference.");
        }
        if (_throwable != null) {
            Throwable throwable = _throwable;
            _throwable = null;
            throw new IllegalActionException(
                this,
                throwable,
                "Run in a new thread threw an exception.");
        }

        if (_model instanceof CompositeActor) {
            CompositeActor executable = (CompositeActor) _model;

            // Will need the effigy for the model this actor is in.
            NamedObj toplevel = toplevel();
            Effigy myEffigy = Configuration.findEffigy(toplevel);
            // FIXME
            System.out.println(
                "Found effigy for controlling model: " + myEffigy);

            // If there is no such effigy, then skip trying to open a tableau.
            // The model may have no graphical elements.
            if (myEffigy != null) {
                try {
                    // Conditionally show the model in Vergil. The openModel()
                    // method also creates the right effigy.
                    if (_openOnFiringValue == _OPEN_IN_VERGIL
                        || _openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                        Configuration configuration =
                            (Configuration) myEffigy.toplevel();
                        _tableau = configuration.openModel(_model, myEffigy);
                        
                        // Do not allow editing on this tableau.  In particular,
                        // if editing were allowed, then an attempt to save the
                        // changes will result in a spectacular failure.  The
                        // model that will be saved will actually be the referring
                        // model rather than the referred to model.  This will
                        // trash the referred to model, and will result in an
                        // infinite loop when attempting to open either model.
                        // FIXME: This doesn't work!!!! Can still save model!!!!
                        _tableau.setEditable(false);
                        
                        // FIXME
                        System.out.println(
                            "Tableau for referenced model: " + _tableau);
                        _tableau.show();
                    } else {
                        // Need an effigy for the model, or else graphical elements
                        // of the model will not work properly.  That effigy needs
                        // to be contained by the effigy responsible for this actor.
                        PtolemyEffigy newEffigy =
                            new PtolemyEffigy(
                                myEffigy,
                                myEffigy.uniqueName(_model.getName()));
                        newEffigy.setModel(_model);
                        // FIXME
                        System.out.println(
                            "Created new effigy for referenced model: "
                                + newEffigy);
                    }
                } catch (NameDuplicationException ex) {
                    // This should not be thrown.
                    throw new InternalErrorException(ex);
                }
            }
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

            try {
                JFrame frame = null;
                // If we did not open in Vergil, then there is no tableau.
                if (_tableau != null) {
                    frame = _tableau.getFrame();
                }
                // If there is a previous execution, then wait for it to finish.
                // Avoid the synchronize block if possible.
                if (_executing) {
                    synchronized (this) {
                        while (_executing) {
                            try {
                                // Use workspace version of wait to release
                                // read permission on the workspace.
                                workspace().wait(this);
                            } catch (InterruptedException ex) {
                                // Cancel subsequent execution.
                                getManager().finish();
                                return;
                            }
                        }
                    }
                }
                // FIXME
                System.out.println("Frame: " + frame);
                if (_openOnFiringValue == _OPEN_IN_VERGIL && frame != null) {
                    frame.toFront();
                } else if (_openOnFiringValue == _OPEN_IN_VERGIL_FULL_SCREEN) {
                    if (frame instanceof ExtendedGraphFrame) {
                        ((ExtendedGraphFrame) frame).fullScreen();
                    } else if (frame != null) {
                        // No support for full screen.
                        frame.toFront();
                    }
                }

                if (_executionOnFiringValue == _RUN_IN_CALLING_THREAD) {
                    // FIXME
                    System.out.println("Executing in calling thread.");
                    _manager.execute();
                } else if (_executionOnFiringValue == _RUN_IN_A_NEW_THREAD) {
                    // Listen for exceptions. The listener is
                    // removed in the listener methods, executionError()
                    // and executionFinished().
                    // FIXME
                    System.out.println("Executing in a new thread.");
                    _manager.addExecutionListener(this);
                    _manager.startRun();
                }
                long lingerTimeValue =
                    ((LongToken) lingerTime.getToken()).longValue();
                if (lingerTimeValue > 0L) {
                    try {
                        Thread.sleep(lingerTimeValue);
                    } catch (InterruptedException ex) {
                        // Ignore.
                    }
                }
            } catch (KernelException ex) {
                throw new IllegalActionException(
                    this,
                    ex,
                    "Failed to execute referenced model");
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
        JFrame frame = null;
        if (_tableau != null) {
            _tableau.getFrame();
        }
        // FIXME
        System.out.println("In postfire, tableau is: " + _tableau);
        if ((_postfireActionValue | _STOP_EXECUTING) != 0) {
            // FIXME:
            System.out.println("Calling finish.");
            _manager.finish();
            // Wait for the finish.
            _manager.waitForCompletion();
        }
        _manager = null;
        if ((_postfireActionValue | _CLOSE_VERGIL_GRAPH) != 0
            && _tableau != null) {
            if (frame instanceof ExtendedGraphFrame) {
                ((ExtendedGraphFrame) frame).cancelFullScreen();
            }
            if (frame instanceof TableauFrame) {
                // FIXME: Do this only if explicitly requested.
                 ((TableauFrame) frame).close();
            } else if (frame != null) {
                frame.hide();
            }
        }
        return super.postfire();
    }

    /** Override the base class to call stop() on the referenced model.
     */
    public void stop() {
        if (_model instanceof Executable) {
            ((Executable)_model).stop();
        }
        super.stop();
    }

    /* Override the base class to call stopFire() on the referenced model.
     */
    public void stopFire() {
        if (_model instanceof Executable) {
            ((Executable)_model).stopFire();
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
    ////                        private variables                  ////

    /** The value of the executionOnFiring parameter. */
    private int _executionOnFiringValue = _RUN_IN_CALLING_THREAD;

    // Flag indicating that the previous execution is in progress.
    private boolean _executing = false;

    // Possible values for executionOnFiring.
    private static int _DO_NOTHING = 0;
    private static int _RUN_IN_CALLING_THREAD = 1;
    private static int _RUN_IN_A_NEW_THREAD = 2;

    /** Indicator of what the last call to iterate() returned. */
    private int _lastIterateResult = NOT_READY;

    /** The manager currently managing execution. */
    private Manager _manager = null;

    /** The model. */
    private NamedObj _model;

    /** The value of the executionOnFiring parameter. */
    private int _openOnFiringValue = _DO_NOT_OPEN;

    // Possible values for executionOnFiring.
    private static int _DO_NOT_OPEN = 0;
    private static int _OPEN_IN_VERGIL = 1;
    private static int _OPEN_IN_VERGIL_FULL_SCREEN = 2;
    private static int _OPEN_RUN_CONTROL_PANEL = 3;

    /** The value of the postfireAction parameter. */
    private int _postfireActionValue = _DO_NOTHING;

    // Possible values for postfireAction (plus _DO_NOTHING,
    // which is defined above).
    private static int _CLOSE_VERGIL_GRAPH = 1;
    private static int _STOP_EXECUTING = 2;
    private static int _STOP_EXECUTING_AND_CLOSE_VERGIL_GRAPH = 3;

    // Tableau that has been created (if any).
    private Tableau _tableau;

    // Error from a previous run.
    private Throwable _throwable = null;
}
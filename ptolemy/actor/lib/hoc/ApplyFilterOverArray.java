/* An atomic actor that filter an array via applying a model specified by a file or URL.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

import java.net.URL;
import java.util.LinkedList;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
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

///////////////////////////////////////////////////////////////////
//// ApplyFilterOverArray

/**

 This is an atomic actor that filters an array received at its
 <i>inputArray</i> input port via applying a model specified by a
 file or URL. The specified model is evaluated on each input array
 element and should return a boolean value, and the output is an array
 that only contains elements satisfying the specified model (the
 evaluated result is true). An element of the array received at
 <i>inputArray</i> is provided to the model by setting its
 <i>inputArrayElement</i> parameter (which it must have defined).
 The result of executing the model is obtained by reading its
 <i>evaluatedValue</i> parameter (which it must have defined).

 <p> Instead of outputting all the satisfied elements in the input
 array, the parameter <i>maxOutputLength</i> can be used to specify
 how many elements this actor should only output. If the specified
 length is larger than the number of satisfied elements, it will
 ignore the specified length and only output all the satisfied
 elements.

 <p>
 FIXME: what should be the correct behavior if there are not enough elements to output?
 <p>
 FIXME: make a convention, say when the maxOutputLength is -1, for output
 all the satisfied elements?

 FIXME: should add a ModelToken and an input port for receiving the filter
 model. Create a composite actor for providing a ModelToken from its inside
 model.
 <P>

 @author Yang Zhao, Ilkay Altintas
 @version $Id$
 @since Ptolemy II 4.1
 @see ptolemy.actor.lib.hoc.ModelReference
 @Pt.ProposedRating Yellow (ellen_zh)
 @Pt.AcceptedRating Red (ellen_zh)
 */
public class ApplyFilterOverArray extends TypedAtomicActor implements
ExecutionListener {
    /** Construct a ApplyFilterOverArray with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ApplyFilterOverArray(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        inputArray = new TypedIOPort(this, "inputArray", true, false);
        inputArray.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        outputArray = new TypedIOPort(this, "outputArray", false, true);
        outputArray.setTypeSameAs(inputArray);

        modelFileOrURL = new FileParameter(this, "modelFileOrURL");
        maxOutputLength = new Parameter(this, "maxOutputLength");
        maxOutputLength.setTypeEquals(BaseType.INT);
        maxOutputLength.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** The input port for an input array. It is an ArrayType.
     *
     */
    public TypedIOPort inputArray;

    /** The output port for output the filtered array. It has the same
     *  type as the inputArray port.
     */
    public TypedIOPort outputArray;

    /** The max amount of elements in the output array.
     */
    public Parameter maxOutputLength;

    /** The file name or URL of the filter model.
     */
    public FileParameter modelFileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to open the model specified if the
     *  attribute is modelFileOrURL, or for other parameters, to cache
     *  their values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
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
                    throw new IllegalActionException(this, ex,
                            "Failed to read model.");
                }

                // Create a manager, if appropriate.
                if (_model instanceof CompositeActor) {
                    _manager = new Manager(_model.workspace(), "Manager");
                    ((CompositeActor) _model).setManager(_manager);

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
        } else if (attribute == maxOutputLength) {
            IntToken length = (IntToken) maxOutputLength.getToken();

            if (length.intValue() > 0) {
                _outputLength = length.intValue();
            } else {
                throw new IllegalActionException(this,
                        "output array length is less than or equal 0?!");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This overrides
     *  the base class ensure that private variables are reset to
     *  null.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of ApplyFilterOverArray.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ApplyFilterOverArray newActor = (ApplyFilterOverArray) super
                .clone(workspace);
        newActor._manager = null;
        newActor._model = null;
        newActor._throwable = null;

        // Set type constraints.
        newActor.inputArray.setTypeAtLeast(ArrayType.ARRAY_BOTTOM);
        newActor.outputArray.setTypeSameAs(newActor.inputArray);

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
    @Override
    public synchronized void executionError(Manager manager, Throwable throwable) {
        _throwable = throwable;

        //_executing = false;
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
    @Override
    public synchronized void executionFinished(Manager manager) {
        //_executing = false;
        // NOTE: Can't remove these now!  The list is being
        // currently used to notify me!
        // manager.removeExecutionListener(this);
        manager.removeDebugListener(this);
        notifyAll();
    }

    /** Execute the filter model on each input array element until it gets
     *  as many elements as specified by the <i>maxOutputLength</i> parameter.
     *  If there are no enough elements satisfying the filter model, then
     *  only output all the satisfied elements.
     *  Before running the filter model, this method update the filter
     *  model's <i>inputArrayElement</i> parameter for each array
     *  element. After running the filter model, this method looks for
     *  the <i>evaluatedValue</i> parameter and keep the input element
     *  if the evaluated value is ture, otherwise, skip the element.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's action methods throw it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

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

            int i = 0;
            int j = 0;
            LinkedList list = new LinkedList();
            ArrayToken array = (ArrayToken) inputArray.get(0);

            while (i < _outputLength && j < array.length()) {
                Token t = array.getElement(j);
                _updateParameter(t);

                if (_debugging) {
                    _debug("** Executing filter model.");
                }

                try {
                    _manager.execute();
                } catch (KernelException ex) {
                    throw new IllegalActionException(this, ex,
                            "Execution failed.");
                }

                if (_getResult()) {
                    i++;
                    list.add(t);
                }

                j++;
            }

            Token[] result = new Token[list.size()];

            for (i = 0; i < list.size(); i++) {
                result[i] = (Token) list.get(i);
            }

            outputArray.send(0, new ArrayToken(array.getElementType(), result));
        }
    }

    /** Report in debugging statements that the manager state has changed.
     *  This method is called if the referenced model
     *  is executed in another thread and the manager changes state.
     *  @param manager The manager controlling the execution.
     *  @see Manager#getState()
     */
    @Override
    public void managerStateChanged(Manager manager) {
        if (_debugging) {
            _debug("Referenced model manager state: " + manager.getState());
        }
    }

    /** Remove this class from the manager's list of execution listeners.
     *  @return Whatever the superclass returns (probably true).
     *  @exception IllegalActionException If removing the execution listener
     *  throws it, or if thrown by the superclass.
     *   is not valid.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // Test auto/ApplyFilterOverArray2.xml seems to end up here with
        // _manager == null
        if (_manager != null) {
            // If we specified to run in a new thread, then we are listening.
            // If we didn't, this is harmless.
            _manager.removeExecutionListener(ApplyFilterOverArray.this);
            _manager = null;
        }

        return super.postfire();
    }

    /** Override the base class to call stop() on the referenced model.
     */
    @Override
    public void stop() {
        if (_model instanceof Executable) {
            ((Executable) _model).stop();
        }

        super.stop();
    }

    /** Override the base class to call stopFire() on the referenced model.
     */
    @Override
    public void stopFire() {
        if (_model instanceof Executable) {
            ((Executable) _model).stopFire();
        }

        super.stopFire();
    }

    /** Override the base class to call terminate() on the referenced model.
     */
    @Override
    public void terminate() {
        if (_model instanceof Executable) {
            ((Executable) _model).terminate();
        }

        super.terminate();
    }

    /** Report an exception if it occurred in a background run.
     *  @exception IllegalActionException If there is no director, or if
     *   a background run threw an exception.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (_throwable != null) {
            Throwable throwable = _throwable;
            _throwable = null;
            throw new IllegalActionException(this, throwable,
                    "Background run threw an exception");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the parameter "inputArrayElement" of the model to an element
     *  of the input array.
     *  @param t The element value.
     *  @exception IllegalActionException If the model does not have a
     *   settable attribute named "inputArrayElement".
     */
    private void _updateParameter(Token t) throws IllegalActionException {
        Attribute attribute = _model.getAttribute("inputArrayElement");

        // Use the token directly rather than a string if possible.
        if (attribute instanceof Variable) {
            if (_debugging) {
                _debug("** Transferring input to parameter inputArrayElement.");
            }

            ((Variable) attribute).setToken(t);
        } else if (attribute instanceof Settable) {
            if (_debugging) {
                _debug("** Transferring input as string to inputArrayElement.");
            }

            ((Settable) attribute).setExpression(t.toString());
        } else {
            throw new IllegalActionException(this,
                    "The specified model does not have an inputArrayElement parameter.");
        }
    }

    /** Retrieve the value of the parameter "evaluatedValue" of the model.
     *  @return The value of the "evaluatedValue" parameter.
     *  @exception IllegalActionException If the model does not have a
     *   settable attribute named "evaluatedValue".
     */
    private boolean _getResult() throws IllegalActionException {
        Attribute attribute = _model.getAttribute("evaluatedValue");

        if (attribute instanceof Variable) {
            Token t = ((Variable) attribute).getToken();
            return ((BooleanToken) t).booleanValue();
        } else if (attribute instanceof Settable) {
            BooleanToken t = new BooleanToken(
                    ((Settable) attribute).getExpression());
            return t.booleanValue();
        } else {
            throw new IllegalActionException(this,
                    "The specified model does not have an evaluatedValue parameter.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The model. */
    protected NamedObj _model;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The manager currently managing execution. */
    private Manager _manager = null;

    /** the output array length if there are enough elements satisfying
     * the filter model.
     */
    private int _outputLength = 1;

    // Error from a previous run.
    private transient Throwable _throwable = null;
}

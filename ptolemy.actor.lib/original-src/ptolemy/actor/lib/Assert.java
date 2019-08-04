/* Check that an assertion predicate is satisfied, and throw an exception if not.

 Copyright (c) 2013-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** Check that an assertion predicate is satisfied, and throw an exception if not.
 *  To use this actor, add any number of input ports.
 *  Corresponding output ports will be automatically added.
 *  Specify an expression that references the inputs and yields a boolean result.
 *  When the actor fires, if the expression evaluates to false, then the actor
 *  will throw an exception with the message given by the {@link #message} parameter.
 *  Otherwise, it will copy the inputs to the corresponding output ports.
 *
 *  @author Ilge Akkaya, David Broman, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (eal)
 */
public class Assert extends Expression {

    /** Construct an instance of Assert.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Assert(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Hide the output port.
        SingletonParameter showName = new SingletonParameter(output, "_hide");
        showName.setPersistent(false);
        showName.setExpression("true");

        // Set the type of the output port to ensure that the expression is predicate.
        output.setTypeEquals(BaseType.BOOLEAN);

        message = new StringParameter(this, "message");
        message.setExpression("Assertion failed.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The error message to display when the assertion is violated.
     *  This is a string that defaults to "Assertion failed.".
     */
    public StringParameter message;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Assert newObject = null;
        // NOTE: The following flag will be copied into the clone.
        _cloning = true;
        try {
            newObject = (Assert) super.clone(workspace);
            newObject._outputPortMap = new HashMap<String, TypedIOPort>();
            // Reconstruct the output port map.
            List<TypedIOPort> inputs = newObject.inputPortList();
            for (TypedIOPort input : inputs) {
                String name = input.getName();
                String outputPortName = _OUTPUT_PORT_PREFIX + name;
                TypedIOPort output = (TypedIOPort) newObject
                        .getPort(outputPortName);
                newObject._outputPortMap.put(name, output);
            }
        } finally {
            _cloning = false;
            if (newObject == null) {
                throw new CloneNotSupportedException(
                        "super.clone(Workspace) returned null?");
            } else {
                newObject._cloning = false;
            }
        }
        return newObject;
    }

    /** Override the base class to check the result of the evaluation
     *  of the expression. If the result is false, throw an exception.
     *  Otherwise, copy the inputs to the corresponding outputs.
     *  @exception IllegalActionException If the expression evaluates to false,
     *   or if the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (!((BooleanToken) _result).booleanValue()) {
            StringBuffer info = new StringBuffer();
            info.append(message.stringValue());
            info.append("\nAssertion: ");
            info.append(expression.getExpression());
            info.append("\nInput values:\n");
            for (String name : _tokenMap.keySet()) {
                info.append("  ");
                info.append(name);
                info.append(" = ");
                info.append(_tokenMap.get(name).toString());
                info.append("\n");
            }
            throw new IllegalActionException(this, info.toString());
        }

        // If we get here, assertion has passed.
        // Copy the inputs to the outputs.
        for (String inputName : _tokenMap.keySet()) {
            TypedIOPort outputPort = _outputPortMap.get(inputName);
            // NOTE: Expression does not seem to allow an input port to be a multiport.
            // If that changes, then we need to iterate here over all input channels.
            outputPort.send(0, _tokenMap.get(inputName));
        }
    }

    /** Override the base class to create a specialized port.
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            return new AssertPort(this, name);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to create an output port corresponding
     *  to each new input port added. The output port will have as its
     *  display name the name of the input port. It will not be persistent
     *  (will not be exported to the MoML file).
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the entity.
     */
    @Override
    protected void _addPort(TypedIOPort port) throws IllegalActionException,
    NameDuplicationException {
        super._addPort(port);

        if (_creatingOutputPort || _cloning) {
            return;
        }

        final String name = port.getName();
        if (name.equals("output") || name.startsWith(_OUTPUT_PORT_PREFIX)) {
            return;
        }
        // NOTE: Don't want to do this if this Assert is a clone
        // under construction because later the superclass will try
        // to clone the output port and will fail.
        // The _cloning flag above tells us that.
        _createOutputPort(port);
    }

    /** Override the base class to remove the corresponding
     *  output port, if the specified port is an input port, or
     *  the corresponding input port, if the specified port is an
     *  output port.
     *  @param port The port to remove from this entity.
     *   name already in the entity.
     */
    @Override
    protected void _removePort(Port port) {
        super._removePort(port);
        String name = port.getName();

        // Remove the corresponding output port.
        // NOTE: If a corresponding output port exists, then remove
        // it whether this is an output port or not. The user may
        // have accidentally added an output port.
        String outputPortName = _OUTPUT_PORT_PREFIX + name;
        Port outputPort = getPort(outputPortName);
        if (outputPort != null) {
            try {
                outputPort.setContainer(null);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        }
        // If this port name matches the pattern of an output
        // port, then remove the corresponding input port, if it exists.
        if (name.startsWith(_OUTPUT_PORT_PREFIX)) {
            // Remove the corresponding input port.
            String inputName = name.substring(_OUTPUT_PORT_PREFIX.length());
            Port inputPort = getPort(inputName);
            if (inputPort != null) {
                try {
                    inputPort.setContainer(null);
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the corresponding output port for the given input port.
     *  @param port The input port.
     *  @exception InternalErrorException If something goes wrong.
     */
    private void _createOutputPort(final TypedIOPort port) {
        // Show the name of the input port.
        SingletonParameter showName;
        _creatingOutputPort = true;
        try {
            showName = new SingletonParameter(port, "_showName");
            showName.setPersistent(false);
            showName.setExpression("true");

            String name = port.getName();

            // If there is already a port with the correct name, use that.
            String outputPortName = _OUTPUT_PORT_PREFIX + name;
            TypedIOPort outputPort = (TypedIOPort) Assert.this
                    .getPort(outputPortName);
            if (outputPort == null) {
                outputPort = new TypedIOPort(Assert.this, outputPortName,
                        false, true) {
                    // Make sure that this output port _never_ appears in MoML.
                    // If it is allowed to appear, subtle bugs will arise, for example
                    // when copying and pasting in actor-oriented classes.
                    @Override
                    public void exportMoML(Writer output, int depth, String name) {
                    }
                };
                // Display name should match the input port name.
                outputPort.setDisplayName(name);
                showName = new SingletonParameter(outputPort, "_showName");
                showName.setExpression("true");
            }
            _outputPortMap.put(name, outputPort);
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        } finally {
            _creatingOutputPort = false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag indicating that we are being cloned.
     *  Note that this flag will be cloned into the clone, so it needs
     *  to be reset in the both the clone and clonee.
     */
    private boolean _cloning = false;

    /** Flag indicating that we are adding a corresponding output port. */
    private boolean _creatingOutputPort;

    /** Map from input port name to the corresponding output port. */
    private HashMap<String, TypedIOPort> _outputPortMap = new HashMap<String, TypedIOPort>();

    /** Prefix given to output port names. */
    private final static String _OUTPUT_PORT_PREFIX = "_correspondingOutputPort_";

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Class for ports created by the user for this actor.
     *  These should all be input ports, in theory.
     *  This class ensures that if you change the name of the
     *  port, then the name and displayName of the corresponding output
     *  port are both changed.
     */
    public static class AssertPort extends TypedIOPort {
        /** Construct a port for this actor.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the port cannot be contained
         *   by the proposed container.
         *  @exception NameDuplicationException If the container already has a
         *  port with this name.
         */
        public AssertPort(Assert container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        // Override setName() to also change the name of the corresponding
        // output port.
        @Override
        public void setName(final String name) throws IllegalActionException,
        NameDuplicationException {
            final String oldName = getName();
            super.setName(name);
            // No need to do anything for the first name setting
            // or if the name is not changing.
            if (oldName != null && !oldName.equals(name)) {
                // FIXME: The port dialog complains about this!
                // But the operation succeeds.
                Assert container = (Assert) getContainer();
                TypedIOPort outputPort = container._outputPortMap.get(oldName);
                if (outputPort != null) {
                    outputPort.setName(_OUTPUT_PORT_PREFIX + name);
                    outputPort.setDisplayName(name);
                    container._outputPortMap.remove(oldName);
                    container._outputPortMap.put(name, outputPort);
                }
            }
        }
    }
}

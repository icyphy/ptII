/* Check that an assertion predicate is satisfied, and throw an exception if not.

 Copyright (c) 2013 The Regents of the University of California.
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

import java.util.HashMap;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** Check that an assertion predicate is satisfied, and throw an exception if not.
 *  To use this actor, add any number of input ports.
 *  Corresponding output ports will be automatically added.
 *  Specify an expression that references the inputs and yield a boolean result.
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
    ////                     public methods                        ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Assert newObject = (Assert) super.clone(workspace);
        newObject._outputPortMap = new HashMap<String,TypedIOPort>();
        return newObject;
    }

    /** Override the base class to check the result of the evaluation
     *  of the expression. If the result is false, throw an exception.
     *  Otherwise, copy the inputs to the corresponding outputs.
     *  @throws IllegalActionException If the expression evaluates to false,
     *   or if the superclass throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        
        if (!((BooleanToken)_result).booleanValue()) {
            throw new IllegalActionException(this, message.stringValue()
                    + "\nAssertion: " + expression.getExpression());
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
    
    ///////////////////////////////////////////////////////////////////
    ////                     protected methods                     ////

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
    protected void _addPort(final TypedIOPort port) throws IllegalActionException,
            NameDuplicationException {
        
        // FIXME: Need to also override _removePort to remove corresponding output ports.
        // FIXME: Need to make sure the port being added is not an output port.
        // FIXME: If an input port is renamed, need to change the display name of the output port.
        // Do this by creating a port that is a subclass of TypedIOPort that overrides setName(),
        // and return that subclass from newPort().
        super._addPort(port);
        
        if (_creatingOutputPort) {
            return;
        }

        final String name = port.getName();
        if (name.equals("output") || name.startsWith(_OUTPUT_PORT_PREFIX)) {
            return;
        }
        
        // Show the name of the input port.
        SingletonParameter showName = new SingletonParameter(port, "_showName");
        showName.setPersistent(false);
        showName.setExpression("true");
        
        ChangeRequest request = new ChangeRequest(this, "Add a matching output port", true) {
            @Override
            protected void _execute() throws Exception {
                _creatingOutputPort = true;
                try {
                    String name = port.getName();
                    
                    // If there is already a port with the correct name, use that.
                    String outputPortName = _OUTPUT_PORT_PREFIX + name;
                    TypedIOPort outputPort = (TypedIOPort)Assert.this.getPort(outputPortName);
                    if (outputPort == null) {
                        outputPort = new TypedIOPort(Assert.this, outputPortName, false, true);
                        // Display name should match the input port name.
                        outputPort.setDisplayName(name);
                        SingletonParameter showName = new SingletonParameter(outputPort, "_showName");
                        showName.setExpression("true");
                    }
                    _outputPortMap.put(name, outputPort);
                } finally {
                    _creatingOutputPort = false;
                }
            }
        };
        requestChange(request);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** Flag indicating that we are adding a corresponding output port. */
    private boolean _creatingOutputPort;
    
    /** Map from input port name to the corresponding output port. */
    private HashMap<String,TypedIOPort> _outputPortMap = new HashMap<String,TypedIOPort>();
    
    /** Prefix given to output port names. */
    private final String _OUTPUT_PORT_PREFIX = "_correspondingOutputPort_";
}

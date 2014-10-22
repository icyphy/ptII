/* A static integer arithmetic logic unit.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.tutorial;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// StaticALU

/**
 <p>
 A static integer arithmetic logic unit.
 </p>

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class StaticALU extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public StaticALU(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        A = new TypedIOPort(this, "A", true, false);
        A.setTypeEquals(BaseType.INT);
        B = new TypedIOPort(this, "B", true, false);
        B.setTypeEquals(BaseType.INT);

        operation = new StringParameter(this, "operation");
        operation.setExpression("0 : (NOP)");
        operation.addChoice("0 : (NOP)");
        operation.addChoice("1 : (A + B)");
        operation.addChoice("2 : (A - B)");
        operation.addChoice("3 : (A * B)");
        operation.addChoice("4 : (A / B)");

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port for the first operand. The port type is integer.
     */
    public TypedIOPort A;

    /** Input port for the second operand. The port type is integer.
     */
    public TypedIOPort B;

    /** Input port for the operation code. The port type is integer.
     * The value of the input tokens should not exceed the number of
     * operations supported.
     */
    public StringParameter operation;

    /** Output port.  The type is inferred from the connections.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to set type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new instance of StaticALU.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        StaticALU newObject = (StaticALU) super.clone(workspace);
        newObject.A.setTypeEquals(BaseType.INT);
        newObject.B.setTypeEquals(BaseType.INT);
        return newObject;
    }

    /** If there is at least one token on each of the input ports, the
     *  first token from each port is consumed. The value of the token
     *  from the operation input port determines which operation will be
     *  perform on the tokens from input port A and B. The opcode is
     *  defined as follow:
     *      0 - NOP
     *      1 - Addition (A + B)
     *      2 - Subtraction (A - B)
     *      3 - Multiplication (A * B)
     *      4 - Division (A / B)
     *  @exception IllegalActionException If the input operation code
     *  is not supported.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Token result = null;

        if (A.hasToken(0) && B.hasToken(0)) {
            int opcode = Integer.parseInt(operation.getExpression().substring(
                    0, 1));
            IntToken tokenA = (IntToken) A.get(0);
            IntToken tokenB = (IntToken) B.get(0);

            switch (opcode) {
            case 0: // NOP
                result = IntToken.ZERO;
                break;
            case 1: // addition
                result = tokenA.add(tokenB);
                break;
            case 2: // subtraction
                result = tokenA.subtract(tokenB);
                break;
            case 3: // multiplication
                result = tokenA.multiply(tokenB);
                break;
            case 4: // division
                result = tokenA.divide(tokenB);
                break;
            default:
                throw new IllegalActionException(this,
                        "Unsupported operation code: " + opcode + ".\n");
            }
        }
        output.send(0, result);
    }
}

/* An actor that computes a specified String comparison function on
 the two String inputs.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Green (kapokasa@ptolemy.eecs.berkeley.edu)
@AcceptedRating Green (net@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.string;


import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// StringCompare
/**
 Compare two strings specified either as inputs or parameters. The output
 is either true or false, depending on whether the comparison function is
 satisfied. The comparison functions are:
 <ul>
 <li> <b>equals</b>: Output true if the strings are equal (Default).
 <li> <b>startsWith</b>: Output true if <i>firstString</i> starts with
      <i>secondString</i>.
 <li> <b>endsWith</b>: Output true if <i>firstString</i> ends with
      <i>secondString</i>.
 <li> <b>contains</b>: Output true if <i>firstString</i> contains
      <i>secondString</i>.
 </ul>
 The strings to be compared will be taken from the inputs if they are
 available, and otherwise will be taken from the corresponding parameters.

@author Vinay Krishnan, Daniel L&aacute;zaro Cuadrado (contributor: Edward A. Lee)
@version $Id$
@since Ptolemy II 3.0.3
*/

public class StringCompare extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  Construct the two operand input PortParameters (initialized to "")
     *  and the output port which outputs the result of the various comparison
     *  functions executed by the actor. The function to be executed is
     *  decided by the parameter <i>function</i>, which is also initialized
     *  here to the comparison function equals. The <i>ignoreCase</i> parameter
     *  allows to ignore case when comparing.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringCompare(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Parameters
        function = new Parameter(this, "function");
        function.setStringMode(true);
        function.setExpression("equals");
        function.addChoice("equals");
        function.addChoice("startsWith");
        function.addChoice("endsWith");
        function.addChoice("contains");
        _function = _EQUALS;

        ignoreCase = new Parameter(this, "ignoreCase");
        ignoreCase.setTypeEquals(BaseType.BOOLEAN);
        ignoreCase.setToken(new BooleanToken(false));

        // Ports
        firstString = new PortParameter(this, "firstString");
        firstString.setExpression("");
        firstString.setStringMode(true);

        secondString = new PortParameter(this, "secondString");
        secondString.setExpression("");
        secondString.setStringMode(true);

        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-15\" "
                + "width=\"60\" height=\"30\" "
                + "style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The comparison function to be performed. The choices are:
     * <ul>
     * <li> <b>equals</b>: Compares firstString to another secondString
     * (Default).
     * <li> <b>startsWith</b>: Tests whether firstString starts with
     *  secondString.
     * <li> <b>endsWith</b>: Tests whether firstString ends with secondString.
     * <li> <b>contains</b>: Tests whether firstString contains secondString.
     * </ul>
     */
    public Parameter function;

    /** The input PortParameter for the first string of type string.
     */
    public PortParameter firstString;

    /** The parameter to state whether to ignore case. This is a
     *  boolean that defaults to false.
     */
    public Parameter ignoreCase;

    /** The output port for the result of type BooleanToken.
     */
    public TypedIOPort output;

    /** The input PortParameter for the second string of type string.
     */
    public PortParameter secondString;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == function) {
            // Use getToken() not getExpression() so that substitutions happen.
            String functionName
                = ((StringToken)function.getToken()).stringValue();
            if (functionName.equals("equals")) {
                _function = _EQUALS;
            } else if (functionName.equals("startsWith")) {
                _function = _STARTSWITH;
            } else if (functionName.equals("endsWith")) {
                _function = _ENDSWITH;
            } else if (functionName.equals("contains")) {
                _function = _CONTAINS;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized string function: " + functionName);
            }
        } else super.attributeChanged(attribute);
    }

    /** Consume exactly one input token from each input port, and compute the
     *  specified string function of the input taking into account the
     *  <i>ignoreCase</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // They are port parameters and they need to be updated.
        firstString.update();
        secondString.update();

        String input1 = ((StringToken) firstString.getToken()).stringValue();
        String input2 = ((StringToken) secondString.getToken()).stringValue();

        if (((BooleanToken) ignoreCase.getToken()).booleanValue()) {
            input1 = input1.toLowerCase();
            input2 = input2.toLowerCase();
        }

        output.send(0, new BooleanToken(_doFunction(input1, input2)));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the function selected on the given inputs.
     *  @param input1 The first input String.
     *  @param input2 The second input String.
     *  @return The result of applying the function.
     */
    private boolean _doFunction(String input1, String input2) {
        boolean result;
        switch(_function) {
        case _EQUALS:
            result = input1.equals(input2);
            break;
        case _STARTSWITH:
            result = input1.startsWith(input2);
            break;
        case _ENDSWITH:
            result = input1.endsWith(input2);
            break;
        case _CONTAINS:
            result = input1.indexOf(input2) >= 0;
            break;
        default:
            throw new InternalErrorException(
                    "Invalid value for _function private variable. "
                    + "StringCompare actor (" + getFullName()
                    + ")"
                    + " on function type " + _function);
        }
        return result;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // An indicator for the function to compute.
    private int _function;

    // Constants used for more efficient execution.
    private static final int _EQUALS = 0;
    private static final int _STARTSWITH = 1;
    private static final int _ENDSWITH = 2;
    private static final int _CONTAINS = 3;
}

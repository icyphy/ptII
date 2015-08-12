/* An actor that computes a specified rounded value of the input.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/math.xml.
///////////////////////////////////////////////////////////////////
//// Round

/**
 Produce an output token on each firing with a value that is
 equal to the specified rounded value of the input.
 The input type is DoubleToken. The output type is IntToken.
 The functions are a subset of those in the java.lang.Math class.
 They are:
 <ul>
 <li> <b>ceil</b>: Round towards positive infinity.
 <li> <b>floor</b>: Round towards negative infinity.
 <li> <b>round</b>: Round towards nearest integer.  This is the
 default behavior.
 <li> <b>truncate</b>: Round towards zero.
 </ul>

 If the input is NaN, then an exception is thrown.
 The reason for this is that there is no way to represent a NaN
 as an integer.  Thus, even though java.lang.Math.round(Double.NaN)
 returns 0, ceil(Double.NaN), floor(Double.NaN) and truncate(DoubleNaN) all
 return a Double.NaN.  However, this actor has an integer output,
 so there is no way to represent the Double.NaN as an integer, so
 we throw an exception.


 @author C. Fong, Contributor: Christopher Brooks
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (chf)
 @Pt.AcceptedRating Green (janneck)
 */
public class Round extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public Round(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Parameters
        function = new StringAttribute(this, "function");
        function.setExpression("round");
        _function = _ROUND;

        // Ports
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.INT);

        _attachText("_iconDescription", "<svg>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"17\""
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The rounding strategy to use.  This is a string-valued parameter
     *  that defaults to "round".
     */
    public StringAttribute function;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == function) {
            String functionName = function.getExpression();

            if (functionName.equals("ceil")) {
                _function = _CEIL;
            } else if (functionName.equals("floor")) {
                _function = _FLOOR;
            } else if (functionName.equals("round")) {
                _function = _ROUND;
            } else if (functionName.equals("truncate")) {
                _function = _TRUNCATE;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized rounding function: " + functionName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Round newObject = (Round) super.clone(workspace);

        // The non-primitive fields of the clone must refer to objects
        // distinct from the objects of the same name in the class.
        // If this is not done, then there may be problems with actor
        // oriented classes.
        newObject._resultArray = new IntToken[0];
        return newObject;
    }

    /** This computes the specified rounded value of the input.
     *  This consumes and produces at most one token for each firing.
     *  If there is no input, then produce no output.
     *  @exception IllegalActionException If there is no director,
     *   or if the input is NaN.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        double in = ((DoubleToken) input.get(0)).doubleValue();
        if (Double.isNaN(in)) {
            throw new IllegalActionException("Input is Double.NaN, "
                    + "there is no way to represent a NaN as an integer.");
        }
        output.send(0, new IntToken(_doFunction(in)));
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration computes the rounding function specified by the
     *  <i>function</i> attribute on a single token. An invocation
     *  of this method therefore applies the function to <i>count</i>
     *  successive input tokens.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY, and do
     *   not consume any input tokens.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        // Check whether we need to reallocate the output token array.
        if (count > _resultArray.length) {
            _resultArray = new IntToken[count];
        }

        if (input.hasToken(0, count)) {
            // NOTE: inArray.length may be > count, in which case
            // only the first count tokens are valid.
            Token[] inArray = input.get(0, count);

            for (int i = 0; i < count; i++) {
                double value = ((DoubleToken) inArray[i]).doubleValue();
                if (Double.isNaN(value)) {
                    throw new IllegalActionException(
                            "Input is Double.NaN, "
                                    + "there is no way to represent a NaN as an integer.");
                }

                _resultArray[i] = new IntToken(_doFunction(value));
            }

            output.send(0, _resultArray, count);
            return COMPLETED;
        } else {
            return NOT_READY;
        }
    }

    /** Return false if there is no available input token, and otherwise
     *  return whatever the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Calculate the function on the given argument.
     *  @param in The input value.
     *  @return The result of applying the function.
     */
    private int _doFunction(double in) {
        int result;

        switch (_function) {
        case _CEIL:
            result = (int) Math.ceil(in);
            break;

        case _FLOOR:
            result = (int) Math.floor(in);
            break;

        case _ROUND:
            result = (int) Math.round(in);
            break;

        case _TRUNCATE:

            if (in > 0) {
                result = (int) Math.floor(in);
            } else {
                result = (int) Math.ceil(in);
            }

            break;

        default:
            throw new InvalidStateException(
                    "Invalid value for _function private variable. "
                            + "Round actor (" + getFullName() + ")"
                            + " on function type " + _function);
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private IntToken[] _resultArray = new IntToken[0];

    // An indicator for the function to compute.
    // This variable has values specified in the constants below
    private int _function;

    // Constants used for more efficient execution.
    private static final int _CEIL = 0;

    private static final int _FLOOR = 1;

    private static final int _ROUND = 2;

    private static final int _TRUNCATE = 3;
}

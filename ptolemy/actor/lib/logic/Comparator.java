/* An actor that compares two doubles.

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
package ptolemy.actor.lib.logic;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

// NOTE: If you update the list of comparisons, then you will want
// to update the list in actor/lib/logic/logic.xml.
///////////////////////////////////////////////////////////////////
//// Comparator

/**
 <p>Compare two double-valued inputs, and output the boolean result
 of the comparison.  The exact comparison performed is given by the
 <i>comparison</i> attribute, which can take any of the following
 values:
 <ul>
 <li> <b>&gt;</b>: <i>left</i> &gt; <i>right</i></li>
 <li> <b>&gt;=</b>: <i>left</i> &gt;= <i>right</i></li>
 <li> <b>&lt;</b>: <i>left</i> &lt; <i>right</i></li>
 <li> <b>&lt;=</b>: <i>left</i> &lt;= <i>right</i></li>
 <li> <b>==</b>: <i>left</i> == <i>right</i></li>
 </ul>
 The default is "&gt;".
 The input ports are named <i>left</i> and <i>right</i> to indicate
 which side of the comparison operator their value appears on.
 </p>
 <p>
 The <i>tolerance</i> parameter, which defaults to zero, defines
 an error tolerance.  That is, the actor may produce true even if
 the specified test is not exactly satisfied, but rather is almost
 satisfied, within the specified tolerance.
 </p>
 <p>
 Note that this actor will work with any data type that can be losslessly
 converted to doubles, such as integers.
 </p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (neuendor)
 */
public class Comparator extends TypedAtomicActor {
    /** Construct an actor with the given container and name.  Set the
     *  comparison to the default ("&gt;").  Set the types of
     *  the input ports to double, and the type of the output port
     *  to boolean.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Comparator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Parameters
        comparison = new StringAttribute(this, "comparison");
        comparison.setExpression(">");

        tolerance = new Parameter(this, "tolerance");
        tolerance.setExpression("0.0");

        // Ports
        left = new TypedIOPort(this, "left", true, false);
        right = new TypedIOPort(this, "right", true, false);
        output = new TypedIOPort(this, "output", false, true);
        left.setTypeEquals(BaseType.DOUBLE);
        right.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-30,-10, -10,-10, -10,0\" "
                + "style=\"stroke:grey\"/>\n"
                + "<polyline points=\"-30,10, 10,10, 10,0\" "
                + "style=\"stroke:grey\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The left input port, which has type double. */
    public TypedIOPort left;

    /** The right input port, which has type double. */
    public TypedIOPort right;

    /** The output port, which has type boolean. */
    public TypedIOPort output;

    /** The comparison operator.  This is a string-valued attribute
     *  that defaults to "&gt;".
     */
    public StringAttribute comparison;

    /** The tolerance for the comparison. This has type double,
     *  and defaults to 0.0.
     */
    public Parameter tolerance;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which comparison is being
     *  specified.  Read the value of the comparison attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the comparison is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == tolerance) {
            _tolerance = ((ScalarToken) tolerance.getToken()).doubleValue();
        } else if (attribute == comparison) {
            String comparisonName = comparison.getExpression().trim();

            if (comparisonName.equals(">")) {
                _comparison = _GT;
            } else if (comparisonName.equals(">=")) {
                _comparison = _GE;
            } else if (comparisonName.equals("<")) {
                _comparison = _LT;
            } else if (comparisonName.equals("<=")) {
                _comparison = _LE;
            } else if (comparisonName.equals("==")) {
                _comparison = _EQ;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized comparison: " + comparisonName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume exactly one input token from each input port,
     *  and compute the specified comparison. This method assumes
     *  that both ports have an input, as checked by prefire().
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        BooleanToken result = BooleanToken.FALSE;
        double leftIn = ((ScalarToken) left.get(0)).doubleValue();
        double rightIn = ((ScalarToken) right.get(0)).doubleValue();

        switch (_comparison) {
        case _GT:

            if (leftIn + _tolerance > rightIn) {
                result = BooleanToken.TRUE;
            }

            break;

        case _GE:

            if (leftIn + _tolerance >= rightIn) {
                result = BooleanToken.TRUE;
            }

            break;

        case _LT:

            if (leftIn < rightIn + _tolerance) {
                result = BooleanToken.TRUE;
            }

            break;

        case _LE:

            if (leftIn <= rightIn + _tolerance) {
                result = BooleanToken.TRUE;
            }

            break;

        case _EQ:

            if (leftIn <= rightIn + _tolerance
            && leftIn >= rightIn - _tolerance) {
                result = BooleanToken.TRUE;
            }

            break;

        default:
            throw new InternalErrorException(
                    "Invalid value for _comparison private variable. "
                            + "Comparator actor (" + getFullName() + ")"
                            + " on comparison type " + _comparison);
        }

        output.send(0, result);
    }

    /** Check that each input port has at least one token, and if
     *  so, return the result of the superclass prefire() method.
     *  Otherwise, return false.
     *  @return True if there inputs available on both input ports.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!left.hasToken(0) || !right.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // An indicator for the comparison to compute.
    private int _comparison;

    // The cached value of the tolerance parameter.
    private double _tolerance;

    // Constants used for more efficient execution.
    private static final int _LT = 0;

    private static final int _LE = 1;

    private static final int _GT = 2;

    private static final int _GE = 3;

    private static final int _EQ = 4;
}

/* An actor that compares two doubles.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.actor.lib.logic;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.*;

// NOTE: If you update the list of comparisons, then you will want
// to update the list in actor/lib/logic/logic.xml.

//////////////////////////////////////////////////////////////////////////
//// Compare
/**
Compare two double-valued inputs, and output the boolean result
of the comparison.  The exact comparison performed is given by the
<i>comparison</i> attribute, which can take any of the following
values:
<ul>
<li> <b>&gt;</b>: <i>left</i> &gt; <i>right</i></li>
<li> <b>&gt;=</b>: <i>left</i> &gt;= <i>right</i></li>
<li> <b>&lt;</b>: <i>left</i> &lt; <i>right</i></li>
<li> <b>&lt;=</b>: <i>left</i> &lt;= <i>right</i></li>
</ul>
The default is "&gt;".
The input ports are named <i>left</i> and <i>right</i> to indicate
which side of the comparison operator their value appears on.

@author Edward A. Lee
@version $Id$
*/
public class Compare extends TypedAtomicActor {

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
    public Compare(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Parameters
        comparison = new StringAttribute(this, "comparison");
        comparison.setExpression(">");
        _comparison = _GT;

        // Ports
        left = new TypedIOPort(this, "left", true, false);
        right = new TypedIOPort(this, "right", true, false);
        output = new TypedIOPort(this, "output", false, true);
        left.setTypeEquals(BaseType.DOUBLE);
        right.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.BOOLEAN);
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Override the base class to determine which comparison is being
     *  specified.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the comparison is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws  IllegalActionException {

        if (attribute == comparison) {
            String comparisonName = comparison.getExpression();

            if (comparisonName.equals(">")) {
                _comparison = _GT;
            } else if (comparisonName.equals(">=")) {
                _comparison = _GE;
            } else if (comparisonName.equals("<")) {
                _comparison = _LT;
            } else if (comparisonName.equals("<=")) {
                _comparison = _LE;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized comparison: " + comparisonName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume at most one input token from each input port,
     *  and compute the specified comparison. This method assumes
     *  that both ports have an input, as checked by prefire().
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        BooleanToken result = BooleanToken.FALSE;
        double leftIn = ((DoubleToken)(left.get(0))).doubleValue();
        double rightIn = ((DoubleToken)(right.get(0))).doubleValue();

        switch(_comparison) {
        case _GT:
            if (leftIn > rightIn) result = BooleanToken.TRUE;
            break;
        case _GE:
            if (leftIn >= rightIn) result = BooleanToken.TRUE;
            break;
        case _LT:
            if (leftIn < rightIn) result = BooleanToken.TRUE;
            break;
        case _LE:
            if (leftIn <= rightIn) result = BooleanToken.TRUE;
            break;
        default:
            throw new InternalErrorException(
                    "Invalid value for _comparison private variable. "
                    + "Compare actor (" + getFullName()
                    + ")"
                    + " on comparison type " + _comparison);
        }
        output.broadcast(result);
    }

    /** Check that each input port has at least one token, and if
     *  so, return the result of the superclass prefire() method.
     *  Otherwise, return false.
     *  @return True if there inputs available on both input ports.
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (!left.hasToken(0) || !right.hasToken(0)) return false;
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // An indicator for the comparison to compute.
    private int _comparison;

    // Constants used for more efficient execution.
    private static final int _LT = 0;
    private static final int _LE  = 1;
    private static final int _GT = 2;
    private static final int _GE = 3;
}

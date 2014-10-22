/* An actor that performs a specified logic operation on the input.

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

import java.util.Locale;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

// NOTE: If you update the list of functions, then you will want
// to update the list in actor/lib/logic/logic.xml.
///////////////////////////////////////////////////////////////////
//// LogicGate

/**
 <p>Produce an output token on each firing with a value that is
 equal to the specified logic operator of the input(s).
 The functions are:</p>
 <ul>
 <li> <b>and</b>: The logical and operator.
 This is the default function for this actor.</li>
 <li> <b>or</b>: The logical or operator.</li>
 <li> <b>xor</b>: The logical xor operator.</li>
 <li> <b>nand</b>: The logical nand operator.
 Equivalent to the negation of <i>and</i>.</li>
 <li> <b>nor</b>: The logical nor operator.
 Equivalent to the negation of <i>or</i>.</li>
 <li> <b>xnor</b>: The logical xnor operator.
 Equivalent to the negation of <i>xor</i>.</li>
 </ul>
 <p>
 NOTE: All operators have
 a single input port, which is a multiport, and a single output port, which
 is not a multiport.  All ports have type boolean.</p>
 <p>
 This actor does not require that each input
 channel have a token upon firing.  As long as one channel contains a
 token, output will be produced.  If no input tokens are available at
 all, then no output is produced.  At most one token is consumed
 on each input channel.</p>

 @author Paul Whitaker
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class LogicGate extends Transformer {
    /** Construct an actor with the given container and name.  Set the
     *  logic function to the default ("and").  Set the types of the ports
     *  to boolean.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LogicGate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Parameters
        logic = new StringParameter(this, "logic");
        logic.setExpression("and");
        logic.addChoice("and");
        logic.addChoice("or");
        logic.addChoice("xor");
        logic.addChoice("nand");
        logic.addChoice("nor");
        logic.addChoice("xnor");
        _function = _AND;
        _negate = false;

        // Ports
        input.setMultiport(true);
        output.setMultiport(false);
        input.setTypeEquals(BaseType.BOOLEAN);
        output.setTypeEquals(BaseType.BOOLEAN);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"60\" height=\"30\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute.  This is a string-valued attribute
     *  that defaults to "and".
     */
    public StringParameter logic;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which function is being
     *  specified.  Read the value of the function attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == logic) {
            String functionName = logic.getExpression().trim()
                    .toLowerCase(Locale.getDefault());

            if (functionName.equals("and")) {
                _function = _AND;
                _negate = false;
            } else if (functionName.equals("or")) {
                _function = _OR;
                _negate = false;
            } else if (functionName.equals("xor")) {
                _function = _XOR;
                _negate = false;
            } else if (functionName.equals("nand")) {
                _function = _AND;
                _negate = true;
            } else if (functionName.equals("nor")) {
                _function = _OR;
                _negate = true;
            } else if (functionName.equals("xnor")) {
                _function = _XOR;
                _negate = true;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized logic function: " + functionName
                                + ".  Valid functions are 'and', 'or', 'xor', "
                                + "'nand', 'nor', and 'xnor'.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume at most one input token from each input channel,
     *  and produce a token on the output port.  If there is no
     *  input on any channel, then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        BooleanToken value = null;
        BooleanToken in = null;

        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                in = (BooleanToken) input.get(i);

                if (in != null) {
                    value = _updateFunction(in, value);
                }
            }
        }

        if (value != null) {
            if (_negate) {
                value = value.not();
            }

            output.send(0, value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Calculate the function on the given arguments.
     *  @param in The new input value.  Should never be null.
     *  @param old The old result value, or null if there is none.
     *  @return The result of applying the function.
     *  @exception IllegalActionException If thrown by BooleanToken operations.
     */
    protected BooleanToken _updateFunction(BooleanToken in, BooleanToken old)
            throws IllegalActionException {
        Token result;

        if (old == null) {
            result = in;
        } else {
            switch (_function) {
            case _AND:
                result = old.and(in);
                break;

            case _OR:
                result = old.or(in);
                break;

            case _XOR:
                result = old.xor(in);
                break;

            default:
                throw new InternalErrorException(
                        "Invalid value for _function private variable. "
                                + "LogicGate actor (" + getFullName() + ")"
                                + " on function type " + _function);
            }
        }

        return (BooleanToken) result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** An indicator for the function to compute.
     *  Valid values are {@link #_AND}, {@link #_OR}, and {@link #_XOR}.
     */
    protected int _function;

    /** True if the intermediate results should be negated. */
    protected boolean _negate;

    /** Perform a logical AND. */
    protected static final int _AND = 0;

    /** Perform a logical OR. */
    protected static final int _OR = 1;

    /** Perform a logical XOR. */
    protected static final int _XOR = 2;
}

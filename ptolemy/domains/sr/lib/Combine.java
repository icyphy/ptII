/* When there is a token combine it with current value, if there are no tokens at all send a clear.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import java.util.Locale;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// SRCombine

/**
 Combine actor for combining synchronous signals (ports with a token
 or no token) using a combine function.

 @author Christian Motika
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating red (cmot)
 @Pt.AcceptedRating red (cmot)
 */
public class Combine extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Combine(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Parameters
        function = new StringAttribute(this, "function");
        function.setExpression("add");

        _function = _ADD;
        _present = false;
        _value = 0;

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        value = new TypedIOPort(this, "value", false, true);
        input.setMultiport(true);
        output.setMultiport(false);
        value.setMultiport(false);
        input.setTypeEquals(BaseType.INT);
        output.setTypeEquals(BaseType.INT);
        value.setTypeEquals(BaseType.INT);

        // FIXME: add appropriate svg icon here
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"55\" height=\"40\" "
                + "style=\"fill:red\"/>\n" + "</svg>\n");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The function to compute a combine operation on signals.
     *  This is a string-valued attribute that defaults to "add".
     */
    public StringAttribute function;

    /** This is a multiport for input (known) signals.
     */
    public TypedIOPort input;

    /** Output a signal here iff any connected input signal is known.
     * This is a constant value of "1" indicating the presence, always.
     */
    public TypedIOPort output;

    /** Output a resulting value.
     * This is a constant value of "1", always.
     */
    public TypedIOPort value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Collect the integer tokens of all available inputs and combine them
     *  using the combine function.
     *  Do this non-strict to take part in a fixed point iteration process.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Actor is scheduled
        if (_debugging) {
            _debug("Combine scheduled.");
        }

        // Check if any ports have known inputs.
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.isKnown(i) && input.hasToken(i)) {
                _present = true;
                if (_debugging) {
                    _debug("Combine: Port " + i + " has token.");
                }

                IntToken in = (IntToken) input.get(i);
                if (in != null) {
                    _value = _updateFunction(in.intValue(), _value);
                }
            }
        }

        if (!_present) {
            if (_debugging) {
                _debug("Checking iff unknown by all connected ports");
            }
            //check if all ports are cleared (known w/o any token)
            boolean allKnown = true;
            for (int i = 0; i < input.getWidth(); i++) {
                allKnown &= input.isKnown(i);
                _debug("allKnown after " + input.getName() + ":" + allKnown);
            }
            if (allKnown) {
                _debug("Sending clear out");
                output.sendClear(0);
                value.sendClear(0);
            }
        } else {
            // Send out integer token if presentToken
            _debug("Sending value " + _value + " out");
            output.send(0, new IntToken(1));
            value.send(0, new IntToken(_value));
        }
    }

    /** This actor must be *NON-strict* because it must not wait for more than
     * one input within an SR director iteration.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        //cleanup for next run
        _present = false;
        resetValue();
        return true;
    }

    /** Set the RailwayInterface and open a TCP connection to the
     *  Model Railway interface program w/ the given <i>host</i> and
     *  <i>port</i> parameters.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        //cleanup for first run
        _present = false;
        resetValue();
    }

    /** This should reset the value according to the current combine function.
     */
    private void resetValue() {
        if (_function == _MINIMUM) {
            _value = 10000000;
        } else if (_function == _MULTIPLY || _function == _AND) {
            _value = 1;
        } else {
            _value = 0;
        }
    }

    /** Terminate the TCP connection of the Model Railway interface.
     *  Set RI object to null so that for the next execution a new
     *  connection will be made.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
    }

    /** Calculate the function on the given arguments.
     *  @param in The new input value.  Should never be null.
     *  @param old The old result value, or null if there is none.
     *  @return The result of applying the function.
     *  @exception IllegalActionException If thrown by BooleanToken operations.
     */
    protected int _updateFunction(int in, int old)
            throws IllegalActionException {
        int result;

        if (_function == _NONE) {
            throw new IllegalActionException("Combine actor (" + getFullName()
                    + ")" + " function must not be NONE");
        }

        switch (_function) {
        case _CONSTANT:
            result = _constValue;
            break;
        case _ADD:
            result = old + in;
            break;
        case _MULTIPLY:
            result = old * in;
            break;
        case _MAXIMUM:
            if (in > old) {
                result = in;
            } else {
                result = old;
            }
            break;
        case _MINIMUM:
            if (in < old) {
                result = in;
            } else {
                result = old;
            }
            break;
        case _AND:
            //assume: 1 true, 0 false
            if (old == 0 || in == 0) {
                result = 0;
            } else {
                result = 1;
            }
            break;
        case _OR:
            //assume: 1 true, 0 false
            if (old == 1 || in == 1) {
                result = 1;
            } else {
                result = 0;
            }
            break;
        default:
            throw new IllegalActionException(
                    "Invalid value for _function private variable. "
                            + "Combine actor (" + getFullName() + ")"
                            + " on function type " + _function);
        }

        return result;
    }

    /** Override the base class to determine which function is being
     *  specified.  Read the value of the function attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == function) {
            String functionName = function.getExpression().trim()
                    .toLowerCase(Locale.getDefault());

            if (functionName.equals("add")) {
                _function = _ADD;
            } else if (functionName.equals("mult")) {
                _function = _MULTIPLY;
            } else if (functionName.equals("max")) {
                _function = _MAXIMUM;
            } else if (functionName.equals("min")) {
                _function = _MINIMUM;
            } else if (functionName.equals("and")) {
                _function = _AND;
            } else if (functionName.equals("or")) {
                _function = _OR;
            } else {
                try {
                    _constValue = Integer.parseInt(function.getValueAsString());
                    _function = _CONSTANT;
                } catch (Exception e) {
                    throw new IllegalActionException(
                            this,
                            "Unrecognized synchronous signal combine function: "
                                    + functionName
                                    + ". Valid combine functions are 'add', 'mult', 'max', "
                                    + "'min', 'and', 'or' and any constant number.");
                }
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Noop. */
    protected final int _NONE = 0;
    /** Add. */
    protected final int _ADD = 1;
    /** Multiply. */
    protected final int _MULTIPLY = 2;
    /** Maximum. */
    protected final int _MAXIMUM = 3;
    /** Minimum. */
    protected final int _MINIMUM = 4;
    /** Logical Or. */
    protected final int _OR = 5;
    /** Logical And. */
    protected final int _AND = 6;
    /** Constant. */
    protected final int _CONSTANT = 8;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Const function value. */
    private int _constValue;

    /** Active combine function. */
    private int _function;

    /** True if the signal is present. */
    private boolean _present;

    /** Current (combined) signal value. */
    private int _value;

}

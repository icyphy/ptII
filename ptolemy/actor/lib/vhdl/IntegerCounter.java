/** The integer counter.

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
package ptolemy.actor.lib.vhdl;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// Integer Counter

/**
 A class for a integer counter.
 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class IntegerCounter extends SynchronousFixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public IntegerCounter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        enable = new TypedIOPort(this, "enable", true, false);
        enable.setTypeEquals(BaseType.FIX);

        reset = new TypedIOPort(this, "reset", true, false);
        reset.setTypeEquals(BaseType.FIX);

        width = new Parameter(this, "width");
        width.setTypeEquals(BaseType.INT);
        width.setExpression("4");

        hasEnable = new Parameter(this, "hasEnable");
        hasEnable.setTypeEquals(BaseType.BOOLEAN);
        hasEnable.setExpression("true");

        _showQuantizationParameters(false, true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The enable port. If this input port
     *  receives a true token, then the counter is incremented.
     */
    public TypedIOPort enable;

    /** The reset port. If this port receive a true token,
     * the counter is reset.
     */
    public TypedIOPort reset;

    /** The bit width for this counter.
     */
    public Parameter width;

    /** Specify whether this counter has a enable port.
     */
    public Parameter hasEnable;

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
        if (attribute == hasEnable) {
            try {
                if (hasEnable.getExpression().equals("true")) {
                    enable.setContainer(this);
                } else {
                    enable.setContainer(null);
                }
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot set the container for the enable port");
            }
        } else if (attribute == width) {
            _setQuantizationParameters("U" + width.getExpression() + ".0",
                    null, null);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume at most one token from each input and update the
     *  counter appropriately. Send the current value of the counter
     *  to the output.  If there are no input tokens available, no
     *  output will be produced.  If a token is consumed from only the
     *  <i>increment</i> port the output value will be one more than
     *  the previous output value.  If a token consumed from only the
     *  <i>decrement</i> port the output value will be one less than
     *  the previous output value.  If a token is consumed from both
     *  input ports, then the output value will be the same as the
     *  previous output value.  If the fire method is invoked multiple
     *  times in one iteration, then only the input read on the last
     *  invocation in the iteration will affect future outputs of the
     *  counter.
     *
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (reset.isKnown()
                && (enable.getContainer() == null || enable.getContainer() != null
                && enable.isKnown())) {

            _currentCount = _previousCount;

            if (enable.getContainer() != null && enable.hasToken(0)) {
                FixToken enableToken = (FixToken) enable.get(0);

                _checkFixTokenWidth(enableToken, 1);

                boolean enableValue = enableToken.fixValue().toBitString()
                        .equals("1");

                if (enableValue) {
                    _currentCount++;
                }
            } else if (enable.getContainer() == null) {
                _currentCount++;
            }

            if (reset.hasToken(0)) {
                FixToken resetToken = (FixToken) reset.get(0);

                _checkFixTokenWidth(resetToken, 1);

                boolean resetValue = resetToken.fixValue().toBitString()
                        .equals("1");

                if (resetValue) {
                    _currentCount = 0;
                }
            }

            // Produce an output if we consumed an input.
            FixPoint result = new FixPoint(_currentCount);
            Token outputToken = new FixToken(result);
            sendOutput(output, 0, outputToken);

        } else {

            output.resend(0);
        }
    }

    /** Reset the count of inputs to zero.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _previousCount = 0;
    }

    /** Record the most recent output count as the actual count.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _previousCount = _currentCount;
        return super.postfire();
    }

    /** Override the base class to declare that the <i>enable</i> and
     *  <i>reset</i> ports do not depend on the <i>output</i> in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(enable, output);
        removeDependency(reset, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /**
     * The internal state of the previous count.
     */
    private int _previousCount = 0;

    /**
     * The internal state of the current count.
     */
    private int _currentCount = 0;
}

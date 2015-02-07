/* Create a smooth signal.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.qss.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// SmoothSignal

/**
 Given inputs that are either doubles or {@link SmoothToken}s, construct
 a SmoothToken output. If all the inputs are doubles, then channel 0
 specifies the value of the output, channel 1 specifies the first
 derivative, channel 2 specifies the second derivative, etc.
 If any channel is missing an input, then the most recently received
 input on that channel is used.
 <p>
 If any of the inputs is a SmoothToken, then in addition to
 their values specifying the value and derivatives of the output,
 their derivatives will be added to the derivatives of the output.
 For example, if channel zero has a smoothToken(1.0, {1.0, 2.0})
 and channel one has a smoothToken(2.0, {3.0}), then the output
 will be a smoothToken(1.0, {3.0, 5.0}).
 </p><p>
 The inputs need not arrive all at once. Each time an input arrives,
 the value of the input is interpreted as an <i>update</i> to the
 output value or one of the derivatives. Any previously arrived
 input tokens that are instances of SmoothToken are extrapolated
 before being used to update the output.  So for example, if this
 actor receives only an event on channel 0 at time 0 with value
 smoothToken(1.0, {1.0}), then it will produce an output with
 value smoothToken(1.0, {1.0}) at time 0. If then at time 1 it
 receives only an event on channel 1 with value smoothToken(-1.0, {1.0}),
 then it will first extrapolate the event it previously received
 on channel 0, to value smoothToken(2.0, {1.0}), and then combine
 it with the update on channel 1 to yield
 smoothToken(2.0, {-1.0, 1.0}). The new token updates the first
 derivative to -1.0, overwriting the previously specified first
 derivative of 1.0. The new token also provides a second derivative 1.0.
 Any derivatives provided by higher numbered channels will overwrite
 derivatives provided by lower numbered channels.
 </p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class SmoothSignal extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public SmoothSignal(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:blue\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume and record all available inputs and produce the current
     *  value on all output channels.
     *  @exception IllegalActionException If there is no director or
     *   if the number of input channels does not equal the number of
     *   output channels.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time time = getDirector().getModelTime();

        int width = input.getWidth();
        // The order is at least one less than the width.
        int order = width - 1;
        // The order will at least match the previous output, if there is one.
        double[] previousDerivatives = null;
        if (_previous != null) {
            // Replace the previous token with an extrapolation.
            _previous = _previous.extrapolate(time);
            // Collect the derivatives for updating.
            previousDerivatives = _previous.derivativeValues();
            if (previousDerivatives != null) {
        	order = previousDerivatives.length;
            }
        }
        // First pass collects the inputs and figures out how many derivatives the output will have.
        // Entries will be null if there is no input.
        DoubleToken[] received = new DoubleToken[width];
        for (int i = 0; i < width; i++) {
            while (input.hasToken(i)) {
        	received[i] = (DoubleToken)input.get(i);
            }
            if (received[i] instanceof SmoothToken) {
        	double[] derivatives = ((SmoothToken)received[i]).derivativeValues();
        	if (derivatives != null) {
        	    int max = i + derivatives.length;
        	    if (order < max) {
        		order = max;
        	    }
        	}
            }
        }
        // Construct a default result based on the previous output, if there is one.
        // Default value and derivatives are all 0.0.
        double[] result = new double[order + 1];
        if (_previous != null) {
            result[0] = _previous.doubleValue();
            if (previousDerivatives != null) {
        	System.arraycopy(
        		previousDerivatives, 0, result, 1, previousDerivatives.length);
            }
        }
        
        // Second pass updates the value and derivatives based on inputs provided.
        for (int i = 0; i < width; i++) {
            if (received[i] != null) {
        	result[i] = received[i].doubleValue();
        	if (received[i] instanceof SmoothToken) {
        	    double[] derivatives = ((SmoothToken)received[i]).derivativeValues();
        	    if (derivatives != null && derivatives.length > 0) {
        		System.arraycopy(derivatives, 0, result, i + 1, derivatives.length);
        	    }
        	}
            }
        }
        _previous = new SmoothToken(result, time);
        output.send(0, _previous);
    }
    
    /** Initialize this actor by clearing any stored inputs.
     *  @throws IllegalActionException If the width of the input does
     *   not match the width of the output.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();
        _previous = null;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The previous output, so it can be extrapolated before being updated. */
    private SmoothToken _previous;
}

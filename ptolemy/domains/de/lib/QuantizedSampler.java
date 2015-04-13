/* A Quanitzed Sampler
   Below is the copyright agreement for the Ptolemy II system.

   Copyright (c) 2014-2015 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.SmoothToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// QuantizedSampler

/**
 *  Output the input if the signal has changed more than a quantum
 *  value since the last input.
 *
 *  <p>This quantizer is designed to take an input signal and send it to its output port
 *  if the signal has changed by more than a quantum relative to the last signal seen 
 *  at the input port.</p>
 * 
 *  <p>The number of outputs produced by this actor is dependent on the
 *  input values, so it should not be used with domains like SDF.</p>
 *
 *  @author Thierry S. Nouidui, contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class QuantizedSampler extends Transformer {

    /** Construct a new instance of the quantizer.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If setting up ports and
     *  parameters fails.
     *  @exception NameDuplicationException If the container already
     *  contains an object with this name.
     */
    public QuantizedSampler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(BaseType.DOUBLE);

        quantum = new Parameter(this, "quantum");
        quantum.setTypeEquals(BaseType.DOUBLE);
        quantum.setExpression("1.0");

        attributeChanged(quantum);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The quantum. The default value is a double with
     * the value 1.0, which means that if the input changes by more
     * than 1.0, the new output will be produced. 
     */
    public Parameter quantum;
	
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
	
    /** If the argument is the quantum parameter, then cache
     *  the value.   
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If there is a problem
     *  getting the value of the quantum parameter.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == quantum) {
            _quantum = ((DoubleToken) quantum.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Produce an output equal to the input if the input has crossed
     * the quantum; otherwise, produce no output. 
     * 
     * @throws IllegalActionException If sending an output fails.
     */
	public void fire() throws IllegalActionException {
		super.fire();
		if (input.hasToken(0)) {
			DoubleToken newInputToken = DoubleToken.convert(input.get(0));
			if (_firstFiring){
				// Initialize last input token with first input token received.
				_lastInputToken = newInputToken;
				// Send first input token received to the output port. 
				 output.send(0, newInputToken);
				 _firstFiring = false;
				return;
			}
			final double newInput = newInputToken.doubleValue();
			final double lastInput = _lastInputToken.doubleValue();
			if (newInputToken instanceof SmoothToken) {
				if (!_compareSmoothTokenDerivatives(newInputToken,
						_lastInputToken)) {
					// If the derivatives are different, send new token to the 
					// output port without doing any further comparison.
					_lastInputToken = newInputToken;
					output.send(0, newInputToken);
				} else {
					// If the derivatives are the same, check if the input 
					// has crossed the quantum.
					if (Math.abs(newInput - lastInput) > Math.abs((_quantum))) {
						_lastInputToken = newInputToken;
						output.send(0, newInputToken);
					}
				}
			} else {
				if (Math.abs(newInput - lastInput) > Math.abs((_quantum))) {
					_lastInputToken = newInputToken;
					output.send(0, new DoubleToken(newInput));
				}
			}
		}
	}
    
    /**
     * Initialize this actor.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastInputToken = null;
        _firstFiring = true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////	
    /**
     * Compare the derivative values of two smooth token.
     * @param newToken The new input token received at the port.
     * @param newToken The last input token seen at the port.
     * @retun True if the derivatives are identical. 
     */
    private boolean _compareSmoothTokenDerivatives(DoubleToken newToken, DoubleToken lastToken){
	    // Now we just have to check the derivatives.
        double[] derivativesNewToken = ((SmoothToken) newToken).derivativeValues();
        double[] derivativesLastToken = ((SmoothToken) lastToken).derivativeValues();
        if (derivativesNewToken == derivativesLastToken) {
            // Derivatives are identical (should be true only if null).
            return true;
        }
        if (derivativesNewToken == null && derivativesLastToken != null
                // Findbugs wants us to check for null here to avoid dereferencing
                // a null when we check the length below.
            || derivativesNewToken == null && derivativesLastToken == null
    	    || derivativesNewToken != null && derivativesLastToken == null) {
            return false;
        }
        // Both tokens have derivatives.
        if (derivativesNewToken.length != derivativesLastToken.length) {
            return false;
        }
        // Both tokens have the same number of derivatives.
        for(int i = 0; i < derivativesLastToken.length; i++){
            if (derivativesNewToken[i] != derivativesLastToken[i]) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The last recent input. */
    private DoubleToken _lastInputToken;

    /** Quantum. */
    private double _quantum;
    
    /** Flag to indicate first firing. */
    private boolean _firstFiring;
}

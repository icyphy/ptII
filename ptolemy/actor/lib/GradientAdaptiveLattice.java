/* An IIR filter actor that uses a direct form II implementation.

 Copyright (c) 2003 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// GradientAdaptiveLattice
/**
An adaptive FIR filter with a lattice structure.  This class extends
the base class to dynamically adapt the reflection coefficients to
minimize the power of the output sequence.  The output reflection
coefficients are guaranteed to lie between -1.0 and 1.0, ensuring that the
resulting filter is a minimum phase linear predictor.  The
reflectionCoefficients parameter is interpreted as the initial
coefficients.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1
*/
public class GradientAdaptiveLattice extends Lattice {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GradientAdaptiveLattice(
            CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        // Parameters
        timeConstant = new Parameter(this, "timeConstant");
        timeConstant.setExpression("1.0");
        timeConstant.setTypeEquals(BaseType.DOUBLE);
        timeConstant.validate();
        
        // The currently adapted reflection coefficients
        adaptedReflectionCoefficients = new TypedIOPort(this,
                "adaptedReflectionCoefficients", false, true);
        adaptedReflectionCoefficients.setTypeEquals(
                new ArrayType(BaseType.DOUBLE));

        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port that produces the current reflection
     * coefficients.  The port is of type array of double.
     */
    public TypedIOPort adaptedReflectionCoefficients;
    
    /** The time constant of the filter, which determines how fast the 
     *  filter adapts.
     *  The default value of this parameter is 1.0.
     */
    public Parameter timeConstant;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle parameter change events on the
     *  <i>order</i> and <i>timeConstant</i> parameters. The
     *  filter state vector is reinitialized to zero state.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If this method is invoked
     *   with an unrecognized parameter.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == timeConstant) {
            double timeConstantValue =
                ((DoubleToken)timeConstant.getToken()).doubleValue();
            _oneMinusAlpha = 
                ((timeConstantValue - 1.0) / (timeConstantValue + 1.0));
            _alpha = 1.0 - _oneMinusAlpha;
        }

        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        GradientAdaptiveLattice newObject = 
            (GradientAdaptiveLattice)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** Initialize the state of the filter.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        for(int i = 0; i <= _order; i ++) {
            _estimatedErrorPowerCache[i] = 0;
        }
    }

    /** Update the filter state.
     *
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        System.arraycopy(_estimatedErrorPowerCache, 0,
                _estimatedErrorPower, 0,
                _order + 1);
        System.arraycopy(_reflectionCoefficientsCache, 0,
                _reflectionCoefficients, 0,
                _order);
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    // Compute the filter, updating the caches, based on the current
    // values.  Extend the base class to adapt the reflection coefficients
    protected void _doFilter() throws IllegalActionException {
        double k;
        // NOTE: The following code is ported from Ptolemy Classic.
        // Update forward errors.
        for (int i = 0; i < _order; i++) {
            k = _reflectionCoefficients[i];
            _forwardCache[i+1] = -k * _backwardCache[i] + _forwardCache[i];
        }
               
        Token[] outputArray = new Token[_order];

        // Backward: Compute the weights for the next round Note:
        // strictly speaking, _backwardCache[_order] is not necessary
        // for computing the output.  It is computed for the use of
        // subclasses which adapt the reflection coefficients.
        for (int i = _order; i > 0 ; i--) {
            k = _reflectionCoefficients[i-1];
            _backwardCache[i] = -k * _forwardCache[i-1]
                + _backwardCache[i-1];
    
            double fe_i = _forwardCache[i];
            double be_i = _backwardCache[i];
            double fe_ip = _forwardCache[i-1];
            double be_ip = _backwardCache[i-1];
            
            double newError = 
                _estimatedErrorPower[i] * _oneMinusAlpha +
                _alpha * ( fe_ip * fe_ip + be_ip * be_ip);
            double newCoefficient = _reflectionCoefficients[i-1];
            if (newError != 0.0) {
                newCoefficient +=
                    _alpha * (fe_i * be_ip + be_i * fe_ip) / newError;
                if (newCoefficient > 1.0) {
                    newCoefficient = 1.0;
                } else if (newCoefficient < -1.0) {
                    newCoefficient = -1.0;
                }
            }
            outputArray[i - 1] = new DoubleToken(newCoefficient);
            _reflectionCoefficientsCache[i - 1] = newCoefficient;
            _estimatedErrorPowerCache[i] = newError;
        }
       
        adaptedReflectionCoefficients.send(0, new ArrayToken(outputArray));
    }

    // Reallocate the internal arrays. Extend the base class to
    // reallocate the power estimation array.
    protected void _reallocate() {
        super._reallocate();
        _estimatedErrorPower = new double[_order + 1];
        _estimatedErrorPowerCache = new double[_order + 1];
        _reflectionCoefficientsCache = new double[_order];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _alpha = 0.0;
    private double _oneMinusAlpha = 1.0;

    // The error power in the output signal.  The length is _order.
    private double[] _estimatedErrorPower;

    // Cache of the error power.  The length is _order.
    private double[] _estimatedErrorPowerCache;

    // Cache of the reflection coefficients.  The length is _order;
    private double[] _reflectionCoefficientsCache;
}


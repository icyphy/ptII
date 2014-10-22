/* An IIR filter actor that uses a direct form II implementation.

 Copyright (c) 1998-2014 The Regents of the University of California and
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

 */
package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// IIR

/**

 This actor is an implementation of an infinite impulse response IIR
 filter.  A direct form II [1] implementation is used. This actor is type
 polymorphic. Its input, output,
 numerator and denominator types can be any type of Token supporting the
 basic arithmetic operations (add, subtract and multiply).
 <p>
 This filter has a transfer function given by:

 <b>References</b>
 <p>[1]A. V. Oppenheim, R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
 Prentice Hall, 1989.

 @author Brian K. Vogel, Steve Neuendorffer
 @author Aleksandar Necakov, Research in Motion Limited
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (cxh)
 */
public class IIR extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public IIR(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Parameters
        numerator = new Parameter(this, "numerator");
        numerator.setExpression("{1.0}");
        attributeChanged(numerator);
        denominator = new Parameter(this, "denominator");
        denominator.setExpression("{1.0}");
        attributeChanged(denominator);

        output.setTypeAtLeast(ArrayType.elementType(numerator));
        output.setTypeAtLeast(ArrayType.elementType(denominator));
        input.setTypeAtLeast(output);
        output.setTypeAtLeast(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** This parameter represents the numerator coefficients as an array
     *  of tokens. The format is
     *  {b<sub>0</sub>, b<sub>1</sub>, ..., b<sub>M</sub>}. The default
     *  value of this parameter is {1.0}.
     */
    public Parameter numerator;

    /** This  parameter represents the denominator coefficients as an
     *  array of a tokens. The format is
     *  {a<sub>0</sub>, a<sub>1</sub>, ..., a<sub>N</sub>}. Note that
     *  the value of a<sub>0</sub> is constrained to be 1.0. This
     *  implementation will issue a warning if it is not.
     *  The default value of this parameter is {1.0}.
     */
    public Parameter denominator;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle parameter change events on the
     *  <i>numerator</i> and <i>denominator</i> parameters. The
     *  filter state vector is reinitialized to zero state.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If this method is invoked
     *   with an unrecognized parameter.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numerator) {
            ArrayToken numeratorValue = (ArrayToken) numerator.getToken();
            _numerator = numeratorValue.arrayValue();
        } else if (attribute == denominator) {
            ArrayToken denominatorValue = (ArrayToken) denominator.getToken();
            _denominator = denominatorValue.arrayValue();

            // Note: a<sub>0</sub> must always be 1.
            // Issue a warning if it isn't.
            if (!_denominator[0].isEqualTo(_denominator[0].one())
                    .booleanValue()) {
                try {
                    MessageHandler
                            .warning("First denominator value is required to be 1. "
                                    + "Using 1.");
                } catch (CancelException ex) {
                    throw new IllegalActionException(this,
                            "Canceled parameter change.");
                }

                // Override the user and just use 1.
                _denominator[0] = _denominator[0].one();
            }
        } else {
            super.attributeChanged(attribute);
            return;
        }

        // Initialize filter state.
        if (_numerator != null && _denominator != null) {
            _initStateVector();
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        IIR newObject = (IIR) super.clone(workspace);

        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.numerator));
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.denominator));
            newObject.input.setTypeAtLeast(newObject.output);
            newObject.output.setTypeAtLeast(newObject.input);

            ArrayToken numeratorValue = (ArrayToken) numerator.getToken();
            newObject._numerator = numeratorValue.arrayValue();

            ArrayToken denominatorValue = (ArrayToken) denominator.getToken();
            newObject._denominator = denominatorValue.arrayValue();
        } catch (IllegalActionException ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }

        newObject._stateVector = new Token[_stateVector.length];
        System.arraycopy(_stateVector, 0, newObject._stateVector, 0,
                _stateVector.length);

        return newObject;
    }

    /** If at least one input token is available, consume a single
     *  input token, apply the filter to that input token, and
     *  compute a single output token. If this method is invoked
     *  multiple times in one iteration, then only the input read
     *  on the last invocation in the iteration will affect the
     *  filter state.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            // Save state vector value.
            Token savedState = _stateVector[_currentTap];

            // Compute the current output sample given the input sample.
            Token yCurrent = _computeOutput(input.get(0));

            // Shadowed state. used in postfire().
            _latestWindow = _stateVector[_currentTap];

            // Restore state vector to previous state.
            _stateVector[_currentTap] = savedState;
            output.send(0, yCurrent);
        }
    }

    /**  Initialize the filter state vector with zero state.
     *   @exception IllegalActionException If the base class throws
     *    it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Initialize filter state.
        _initStateVector();
        _currentTap = 0;
    }

    /** Return false if the input does not have a token.
     * @exception IllegalActionException */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean result = super.prefire();
        return result && input.hasToken(0);
    }

    /** Update the filter state.
     *
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _stateVector[_currentTap] = _latestWindow;

        // Update the state vector pointer.
        if (--_currentTap < 0) {
            _currentTap = _stateVector.length - 1;
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private Token _computeOutput(Token xCurrent) throws IllegalActionException {
        for (int j = 1; j < _denominator.length; j++) {
            xCurrent = xCurrent.subtract(_denominator[j]
                    .multiply(_stateVector[(_currentTap + j)
                            % _stateVector.length]));
        }

        _stateVector[_currentTap] = xCurrent;

        Token yCurrent = _numerator[0].zero();

        for (int k = 0; k < _numerator.length; k++) {
            yCurrent = yCurrent.add(_numerator[k]
                    .multiply(_stateVector[(_currentTap + k)
                            % _stateVector.length]));
        }

        return yCurrent;
    }

    private void _initStateVector() throws IllegalActionException {
        if (_numerator.length > 0) {
            int stateSize = java.lang.Math.max(_numerator.length,
                    _denominator.length);
            _stateVector = new Token[stateSize];

            Token zero = _numerator[0].zero();

            for (int j = 0; j < _stateVector.length; j++) {
                _stateVector[j] = zero;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Filter parameters
    private Token[] _numerator = new Token[0];

    private Token[] _denominator = new Token[0];

    // Filter state vector
    private Token[] _stateVector;

    // State vector pointer
    private int _currentTap;

    // Shadow state.
    private Token _latestWindow;
}

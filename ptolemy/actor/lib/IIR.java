/* An IIR filter actor that uses a direct form II implementation.

 Copyright (c) 1998-2003 The Regents of the University of California and
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

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.graph.InequalityTerm;
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
<pre>
       b<sub>0</sub> + b<sub>1</sub>z<sup>-1</sup> + ... +
b<sub>M</sub>z<sup>-M</sup>
      ------------------------
       1 + a<sub>1</sub>z<sup>-1</sup> + ... + a<sub>N</sub>z<sup>-N</sup>
</pre>
The constant terms of the numerator polynomial are specified by the
<i>numerator</i> parameter and the constant terms of the denominator
polynomial are specified by the <i>denominator</i> parameter.
Both of these are given as arrays, using the syntax
<pre>
   {b<sub>0</sub>, b<sub>1</sub>, ..., b<sub>M</sub>}
</pre>
The default numerator and denominator are both {1.0}, resulting in
a filter with unity transfer function.
<p>
The first coefficient of the
<i>denominator</i> parameter is required to be 1 of the denominator token
type. This
implementation will issue a warning if the user enters something
other than 1, and will use 1 instead.  The value is shown explicitly
nonetheless so that the numerator and denominator polynomials are easy
to read.
<p>
<b>References</b>
<p>
[1]
A. V. Oppenheim, R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
Prentice Hall, 1989.

@author Brian K. Vogel, Steve Neuendorffer
@author Aleksandar Necakov, Research in Motion Limited
@version $Id$
@since Ptolemy II 1.0
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
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // Parameters
        numerator = new Parameter(this, "numerator");
        numerator.setExpression("{1.0}");
        attributeChanged(numerator);
        denominator = new Parameter(this, "denominator");
        denominator.setExpression("{1.0}");
        attributeChanged(denominator);

        numerator.setTypeEquals(new ArrayType(BaseType.UNKNOWN));
        denominator.setTypeEquals(new ArrayType(BaseType.UNKNOWN));

        // Set the type of the output port.
        // Set type constraints.
        ArrayType numeratorType = (ArrayType)numerator.getType();
        InequalityTerm elementTerm = numeratorType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm);

        ArrayType denominatorType = (ArrayType)denominator.getType();
        InequalityTerm elementTerm2 = denominatorType.getElementTypeTerm();
        output.setTypeAtLeast(elementTerm2);

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
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numerator) {
            ArrayToken numeratorValue = (ArrayToken)numerator.getToken();
            _numerator = numeratorValue.arrayValue();
        } else if (attribute == denominator) {
            ArrayToken denominatorValue =
                (ArrayToken)denominator.getToken();
            _denominator = denominatorValue.arrayValue();

            // Note: a<sub>0</sub> must always be 1.
            // Issue a warning if it isn't.
            if (!_denominator[0].isEqualTo(_denominator[0].one())
                    .booleanValue()) {
                try {
                    MessageHandler.warning(
                            "First denominator value is required to be 1. "
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
        if ((_numerator != null) && (_denominator != null)) {
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        IIR newObject = (IIR)super.clone(workspace);
        try {
            newObject.numerator
                .setTypeEquals(new ArrayType(BaseType.UNKNOWN));
            newObject.denominator
                .setTypeEquals(new ArrayType(BaseType.UNKNOWN));

            ArrayType numeratorType = (ArrayType)newObject.numerator.getType();
            InequalityTerm elementTerm = numeratorType.getElementTypeTerm();
            newObject.output.setTypeAtLeast(elementTerm);

            ArrayType denominatorType =
                (ArrayType)newObject.denominator.getType();
            InequalityTerm elementTerm2 =
                denominatorType.getElementTypeTerm();
            newObject.output.setTypeAtLeast(elementTerm2);

            newObject.input.setTypeAtLeast(newObject.output);
            newObject.output.setTypeAtLeast(newObject.input);
        } catch (IllegalActionException ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable
                = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }
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
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            // Save state vector value.
            Token savedState = _stateVector[_currentTap];
            // Compute the current output sample given the input sample.
            Token yCurrent = _computeOutput((Token)input.get(0));
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
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Initialize filter state.
        _initStateVector();
        _currentTap = 0;
    }

    /** Update the filter state.
     *
     *  @exception IllegalActionException If the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _stateVector[_currentTap] = _latestWindow;
        // Update the state vector pointer.
        if (--_currentTap < 0) _currentTap = _stateVector.length - 1;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private Token _computeOutput(Token xCurrent) throws
            IllegalActionException {
        for (int j = 1; j < _denominator.length; j++) {
            xCurrent = xCurrent.subtract(_denominator[j].multiply(
                    _stateVector[(_currentTap + j) %
                            _stateVector.length]));
        }
        _stateVector[_currentTap] = xCurrent;
        Token yCurrent = _numerator[0].zero();
        for (int k = 0; k < _numerator.length; k++) {
            yCurrent = yCurrent.add(_numerator[k].multiply(
                    _stateVector[(_currentTap +k) %
                            _stateVector.length]));
        }
        return yCurrent;
    }

    private void _initStateVector() throws  IllegalActionException {
        if (_numerator.length > 0) {
            int stateSize = (int)java.lang.Math.max(_numerator.length,
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


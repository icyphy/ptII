/* An actor that produces tokens with a given probability mass function.

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
package ptolemy.actor.lib;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.SignalProcessing;

///////////////////////////////////////////////////////////////////
//// DiscreteRandomSource

/**
 <p>
 An actor that produces tokens with a given probability mass function.
 </p><p>
 The probability mass function is a parameter, <i>pmf</i>, of this
 actor. The <i>pmf</i> must be an array that contains entries that
 are all between 0.0 and 1.1, and sum to 1.0. By default, <i>pmf</i> is
 initialized to {0.5, 0.5}.
 </p><p>
 Output values are selected at random from the <i>values</i> parameter,
 which contains an ArrayToken. This array must have the same length as
 <i>pmf</i>.  Thus the <i>i</i>-th token in <i>values</i> has probability
 <i>pmf</i>[<i>i</i>]. The output port has the same type as the elements of
 the <i>values</i> array.  The default <i>values</i> are {0, 1}, which are
 integers.</p>

 @author Jeff Tsay, Yuhong Xiong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (ssachs)
 */
public class DiscreteRandomSource extends RandomSource {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DiscreteRandomSource(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        pmf = new Parameter(this, "pmf");
        pmf.setExpression("{0.5, 0.5}");
        pmf.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        // set the values parameter
        values = new Parameter(this, "values");
        values.setExpression("{0, 1}");

        // set type constraint
        output.setTypeAtLeast(ArrayType.elementType(values));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The probability mass function.
     *  This parameter contains an array of doubles, with default value
     *  {0.5, 0.5}.
     */
    public Parameter pmf;

    /** The values to be sent to the output.
     *  This parameter contains an ArrayToken, initially with value
     *  {0, 1} (an int array).
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>pmf</i>, then check that its
     *  entries are all between zero and one, and that they add to one,
     *  and that its dimension is correct.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the requirements are
     *   violated.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pmf) {
            ArrayToken pmfValue = (ArrayToken) pmf.getToken();
            _pmf = new double[pmfValue.length()];

            double sum = 0.0;

            for (int i = 0; i < _pmf.length; i++) {
                _pmf[i] = ((DoubleToken) pmfValue.getElement(i)).doubleValue();
                sum += _pmf[i];
            }

            // Allow for roundoff error.
            if (!SignalProcessing.close(sum, 1.0)) {
                throw new IllegalActionException(this,
                        "Parameter values are required to sum to one.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter public members to refer
     *  to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DiscreteRandomSource newObject = (DiscreteRandomSource) super
                .clone(workspace);
        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .elementType(newObject.values));
        } catch (IllegalActionException e) {
            // Should have been caught before.
            throw new InternalErrorException(e);
        }

        // Copy the array _pmf
        newObject._pmf = null;
        try {
            ArrayToken pmfValue = (ArrayToken) pmf.getToken();
            newObject._pmf = new double[pmfValue.length()];
        } catch (IllegalActionException ex) {
            CloneNotSupportedException exception = new CloneNotSupportedException();
            exception.initCause(ex);
            throw exception;
        }

        if (_pmf != null) {
            System.arraycopy(_pmf, 0, newObject._pmf, 0, _pmf.length);
        }

        return newObject;
    }

    /** Output the token selected in the prefire() method.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _current);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Choose one of the tokens in <i>values</i> randomly, using
     *  the <i>pmf</i> parameter to select one.  The chosen token
     *  will be sent to the output in the fire() method.
     *  @exception IllegalActionException If parameter values are incorrect.
     */
    @Override
    protected void _generateRandomNumber() throws IllegalActionException {
        // Generate a double between 0 and 1, uniformly distributed.
        double randomValue = _random.nextDouble();
        ArrayToken valuesToken = (ArrayToken) values.getToken();

        if (_pmf.length != valuesToken.length()) {
            throw new IllegalActionException(this,
                    "Parameters values and pmf are required to be arrays "
                            + "with the same length.");
        }

        double cdf = 0.0;

        for (int i = 0; i < _pmf.length; i++) {
            cdf += _pmf[i];

            if (randomValue <= cdf) {
                _current = valuesToken.getElement(i);
                return;
            }
        }

        // We shouldn't get here, but if we do, we output the last value.
        _current = valuesToken.getElement(_pmf.length - 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Random value calculated in prefire(). */
    private Token _current;

    /** Cache of probability mass function. */
    private transient double[] _pmf;
}

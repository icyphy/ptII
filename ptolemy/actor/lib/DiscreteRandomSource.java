/* An actor that produces tokens with a given probability mass function.

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import java.util.Random;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// DiscreteRandomSource
/**
An actor that produces tokens with a given probability mass function.

The probability mass function (pmf) is a parameter of this actor. The
pmf must be of the format of a 2D row vector which contains entries that
are all between 0 and 1, and sum to 1. By default, the pmf is initialized
to [0.5 0.5].

Corresponding with the pmf is another parameter which is a 1D row vector of
values to be sent to the output port. Thus the ith symbol has probability
pmf[i] and value val[i]. The output port is of the type contained by
the value matrix.

@author Jeff Tsay
@version $Id$
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
    public DiscreteRandomSource(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        pmf = new Parameter(this, "pmf", new DoubleMatrixToken(
                new double[][] {{0.5, 0.5}}));
        pmf.setTypeEquals(BaseType.DOUBLE_MATRIX);

        // initialize cache of pmf
        attributeChanged(pmf);

        values = new Parameter(this, "values", new IntMatrixToken(
                new int[][] {{0, 1}}));

        //values.setTypeAtLeast(new IntMatrixToken());

        // initialize cache of values matrix
        attributeChanged(values);

        // initialize type of output port
        attributeTypeChanged(values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The probability mass function.
     *  This parameter contains a DoubleMatrixToken, initially with value
     *  [0.5 0.5].
     */
    public Parameter pmf;

    /** The values corresponding to the pmf.
     *  This parameter contains a MatrixToken, initially with value
     *  [0 1] (an IntMatrixToken).
     */
    public Parameter values;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the pmf parameter, update the stored value
     *  of the DoubleMatrixToken representation of pmf.
     *  If the argument is the values parameter, update the stored value
     *  of the MatrixToken representation of values.
     *  @exception IllegalActionException If the values parameter does not
     *  evaluate to a MatrixToken.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == pmf) {
            try {
                DoubleMatrixToken pmfMatrixToken =
                    (DoubleMatrixToken) pmf.getToken();
                double[][] temp = pmfMatrixToken.doubleMatrix();
                // get first (and only) row of the matrix
                _pmfDoubleArray = temp[0];

            } catch (ClassCastException cce) {
                throw new IllegalActionException(
                        "pmf parameter is not a double matrix");
            }
        } else if (attribute == values) {
            try {
                _valuesMatrixToken = (MatrixToken) values.getToken();
            } catch (ClassCastException cce) {
                throw new IllegalActionException(
                        "values parameter is not a matrix");
            }

        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Allow the type of values to change. This will cause the type of the
     *  output port to change. Notify the director, which will cause
     *  type resolution to be redone at the next opportunity. It is assumed that
     *  type changes in the parameter are implemented by the director's change
     *  request mechanism, so they are implement when it is safe to redo type
     *  resolution.
     *  If there is no director, do not notify any objects.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == values) {

            // set the output type to be the type of the element at values[0][0]
            Token value =
                ((MatrixToken) values.getToken()).getElementAsToken(0, 0);

            output.setTypeEquals(value.getType());

            Director dir = getDirector();

            if (dir != null) {
                dir.invalidateResolvedTypes();
            }

        } else {
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
        DiscreteRandomSource newobj = (DiscreteRandomSource) super.clone(ws);
        newobj.pmf = (Parameter) newobj.getAttribute("pmf");
        newobj.values = (Parameter) newobj.getAttribute("values");
        return newobj;
    }

    /** Output one of the Tokens in values randomly, using the pmf parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        // Generate a double between 0 and 1, uniformly distributed.
        double randomValue = _random.nextDouble();

        double cdf = 0.0;

        for (int i = 0; i < _pmfDoubleArray.length; i++) {
            cdf += _pmfDoubleArray[i];

            if (randomValue <= cdf) {
                output.send(0, _valuesMatrixToken.getElementAsToken(0, i));
            }
        }
    }

    /** The cached matrix of pmf values. */
    protected double[] _pmfDoubleArray = null;

    /** The cached matrix of value tokens. */
    protected MatrixToken _valuesMatrixToken = null;
}

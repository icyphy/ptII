/* An actor that produces tokens with a given probability mass function.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import java.util.Random;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.math.DoubleArrayMath;
import ptolemy.math.SignalProcessing;

//////////////////////////////////////////////////////////////////////////
//// DiscreteRandomSource
/**
An actor that produces tokens with a given probability mass function.
<p>
The probability mass function is a parameter, <i>pmf</i>, of this
actor. The <i>pmf</i> must be a row vector that contains entries that
are all between 0 and 1, and sum to 1. By default, the PMF is
initialized to [0.5 0.5].
<p>
Output values are selected at random from the <i>values</i> parameter,
which is a row vector with the same dimensions as <i>pmf</i>.
Thus the <i>i</i>-th symbol <i>values</i>[<i>i</i>] has probability
<i>pmf</i>[<i>i</i>]. The output port is of the type contained by
<i>values</i>.  The default <i>values</i> are [0, 1], which are
integers.

@author Jeff Tsay
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

        values = new Parameter(this, "values", new IntMatrixToken(
                new int[][] {{0, 1}}));
        attributeTypeChanged(values);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The probability mass function.
     *  This parameter contains a DoubleMatrixToken, with default value 
     *  [0.5 0.5].
     */
    public Parameter pmf;

    /** The values to be sent to the output.
     *  This parameter contains a MatrixToken, initially with value
     *  [0 1] (an IntMatrixToken).
     */
    public Parameter values;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>pmf</i>, then check that its
     *  entries are all between zero and one, and that they add to one,
     *  and that its dimension is correct.
     *  @exception IllegalActionException If the requirements are
     *   violated.
     */
    public void attributeChanged(Attribute attribute) 
            throws IllegalActionException {
        if (attribute == pmf) {
            DoubleMatrixToken pmfMatrixToken
                    = (DoubleMatrixToken) pmf.getToken();
            if (pmfMatrixToken.getRowCount() != 1) {
                throw new IllegalActionException(this,
                "Parameter pmf is required to be a row vector.");
            }
            double[] pmfArray = pmfMatrixToken.doubleMatrix()[0];
            double sum = DoubleArrayMath.sum(pmfArray);
            // Allow for roundoff error.
            if (!SignalProcessing.close(sum, 1.0)) {
                throw new IllegalActionException(this,
                "Parameter values is required to sum to one.");
            }
       } else {
          super.attributeChanged(attribute);
       }
    }

    /** Allow the type of <i>values</i> to change. This will cause
     *  the type of the output port to change. Notify the director,
     *  which will cause type resolution to be redone at the next
     *  opportunity. It is assumed that type changes in the parameter
     *  are implemented by the director's change request mechanism,
     *  so they are implement when it is safe to redo type 
     *  resolution. 
     *  If there is no director, do not notify anyone of the change.
     */
    public void attributeTypeChanged(Attribute attribute) 
            throws IllegalActionException {
       if (attribute == values) {
           // set the output type to be the type of the
           // element at values[0][0]
           Token value = ((MatrixToken) values.getToken())
                  .getElementAsToken(0, 0);
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

    /** Output the token selected in the prefire() method.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        output.send(0, _current);
    }

    /** Choose one of the tokens in <i>values</i> randomly, using
     *  the <i>pmf</i> parameter to select one.  The chosen token
     *  will be sent to the output in the fire() method.
     *  @exception IllegalActionException If there is no director, or
     *   if the lengths of the two parameters are not equal.
     */
    public boolean prefire() throws IllegalActionException {
        // Generate a double between 0 and 1, uniformly distributed.
        double randomValue = _random.nextDouble();
        DoubleMatrixToken pmfMatrixToken = (DoubleMatrixToken) pmf.getToken();
        double[] pmfArray = pmfMatrixToken.doubleMatrix()[0];
        MatrixToken valuesMatrixToken = (MatrixToken) values.getToken();
        if (pmfArray.length != valuesMatrixToken.getColumnCount() ||
               valuesMatrixToken.getRowCount() != 1) {
            throw new IllegalActionException(this,
            "Parameters values and pmf are required to be row vectors "
            + "of the same dimension.");
        }
        double cdf = 0.0;
        for (int i = 0; i < pmfArray.length; i++) {
            cdf += pmfArray[i];
            if (randomValue <= cdf) {
               _current = valuesMatrixToken.getElementAsToken(0, i);
               return true;
            }
        }
        // We shouldn't get here, but if we do, we output the last value.
        _current = valuesMatrixToken.getElementAsToken(0, pmfArray.length - 1);
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////

    /** Random value calculated in prefire(). */
    private Token _current;
}
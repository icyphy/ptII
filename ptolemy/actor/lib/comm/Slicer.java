/* A Slicer, which functions as a decoder of the LineCoder
 of complex type.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.comm;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////////
//// Slicer

/**
 The Slicer functions as a decoder of the LineCoder. The parameter
 <i>table</i> and <i>wordLength</i> has the same meaning as in LineCoder,
 except that the type of <i>table</i> is constrained to an ArrayToken
 of complex numbers. On each firing, the Slicer consumes one complex
 token from its input port and computes the Euclidean distance between
 the input data and the elements in the Slicer. The actor produces
 <i>wordLength</i> booleans on each firing. The values of these booleans
 correspond to the index of the entry that minimizes the distance. For
 example, if the first entry minimizes the distance, then all of these
 values are <i>false</i>. If the second entry minimizes the distance,
 then only the first boolean is true.

 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (zhouye)
 @Pt.AcceptedRating Red (cxh)
 */
public class Slicer extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Slicer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.COMPLEX);
        new Parameter(input, "tokenConsumptionRate", new IntToken(1));
        output.setTypeEquals(BaseType.BOOLEAN);
        _outputRate = new Parameter(output, "tokenProductionRate",
                new IntToken(1));

        table = new Parameter(this, "table");
        table.setTypeEquals(new ArrayType(BaseType.COMPLEX));
        table.setExpression("{-1.0, 1.0}");

        //attributeChanged(table);
        wordLength = new Parameter(this, "wordLength", new IntToken(1));
        wordLength.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The code table.  It is an array token of complex type.
     *  The number of values in this array must be at least
     *  2<sup><i>wordLength</i></sup>, or an exception
     *  will be thrown. Its default value is {-1.0, 1.0}.
     */
    public Parameter table;

    /** The word length is the number of boolean output that
     *  are produced on each firing.  Its value is an IntToken,
     *  with default value one.
     */
    public Parameter wordLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Slicer newObject = (Slicer) super.clone(workspace);

        newObject._outputRate = (Parameter) newObject
                .getAttribute("_outputRate");
        return newObject;
    }

    /** Consume the inputs and produce the corresponding symbol.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ComplexToken inputToken = (ComplexToken) input.get(0);
        int index = 0;
        double distance = _computeEuclideanDistance(_table[0],
                inputToken.complexValue());

        for (int i = 1; i < _size; i++) {
            double tempDistance = _computeEuclideanDistance(_table[i],
                    inputToken.complexValue());

            if (tempDistance < distance) {
                index = i;
                distance = tempDistance;
            }
        }

        BooleanToken[] result = new BooleanToken[_wordLength];

        for (int i = 0; i < _wordLength; i++) {
            result[i] = new BooleanToken((index & 1) == 1);
            index = index >> 1;
        }

        output.broadcast(result, _wordLength);
    }

    /** Set up the production constant.
     *  @exception IllegalActionException If the length of the table is not
     *   a power of two.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // FIXME: Handle mutations.
        _wordLength = ((IntToken) wordLength.getToken()).intValue();
        _outputRate.setToken(new IntToken(_wordLength));

        ArrayToken tableToken = (ArrayToken) table.getToken();
        _size = 1 << _wordLength;

        if (tableToken.length() < _size) {
            /*
             throw new IllegalActionException(this, "Table parameter must " +
             "have at least " + _size + " entries, but only has " +
             tableToken.length());
             */
            _size = tableToken.length();
        }

        _table = new Complex[_size];

        for (int i = 0; i < _size; i++) {
            _table[i] = ((ComplexToken) tableToken.getElement(i))
                    .complexValue();
        }
    }

    /** Compute the Euclidean distance between two complex numbers.
     *  @param x The first complex number.
     *  @param y The second complex number.
     *  @return The distance.
     */
    private double _computeEuclideanDistance(Complex x, Complex y) {
        Complex z = x.subtract(y);
        return z.magnitude();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Local cache of these parameter values.
    private int _wordLength;

    private int _size;

    private Complex[] _table;

    // Production rate of the output port.
    private Parameter _outputRate;
}

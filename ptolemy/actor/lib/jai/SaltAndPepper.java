/* Randomly change values in a double matrix to 0.0 or 255.0.

@Copyright (c) 2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.DoubleToken;

//////////////////////////////////////////////////////////////////////////
//// SaltAndPepper
/** Randomly change values in a double matrix to 0.0 or 255.0.

<p>This actor reads in a double matrix and then randomly changes 
some of the matrix elements to 0.0 or to 255.0.  Some of the elements
remain unchanged.  If the double matrix represents an image, then
the image would appear to have Salt and Pepper scattered on it.

@author James Yeh, Contributor: Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.2
*/
public class SaltAndPepper extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SaltAndPepper(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);

        probability =
            new Parameter(this, "probability", new DoubleToken("0.1F"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The probability that a pixel will be turned black or white.
     *  This parameter contains a double between 0.0 and 1.0, the
     *  initial default value is 0.1.  With the initial default value
     *  of 0.1, then there is a 5% chance a pixel will be turned
     *  white, a 5% chance that the pixel will be turned black
     *  and a 90% chance that the pixel will remain unchanged.
     */
    public Parameter probability;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the value of an attribute.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == probability) {
            _probability = ((DoubleToken)probability.getToken()).doubleValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read in a matrix of doubles, randomly change some matrix
     *  elements to either 0.0 or 255.0 and send the results to the
     *  output.
     *  @exception IllegalActionException If there is a problem reading
     *  or writing a token.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        DoubleMatrixToken doubleMatrixToken = (DoubleMatrixToken) input.get(0);
        double data[][] = doubleMatrixToken.doubleMatrix();
        int width = doubleMatrixToken.getRowCount();
        int height = doubleMatrixToken.getColumnCount();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                double value = Math.random();
                if (value < _probability) {
                    if (value < _probability/2) {
                        data[i][j] = 0.0F;
                    } else {
                        data[i][j] = 255.0F;
                    }
                }
            }
        }
        output.send(0, new DoubleMatrixToken(data));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double _probability;
}

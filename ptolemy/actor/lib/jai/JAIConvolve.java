/* An actor that convolves an image with a filter.

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIConvolve
/**
   An actor that convolves an image with a given filter.  The filter must
   be rectangular.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/
public class JAIConvolve extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIConvolve(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        filter = new Parameter(this, "filter",
                new DoubleMatrixToken(_initialMatrix));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The filter to convolve the image width.  It is represented by
     *  a DoubleMatrixToken.
     */
    public Parameter filter;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the filter up to be used.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == filter) {
            DoubleMatrixToken matrix = (DoubleMatrixToken)filter.getToken();
            double[][] matrixValue = matrix.doubleMatrix();
            int height = matrix.getRowCount();
            int width = matrix.getColumnCount();
            float[] floatArray = new float[width*height];
            int count = 0;
            for (int i = 0; i < height; i = i+1) {
                for (int j = 0; j < width; j = j+1) {
                    floatArray[count] = (float)matrixValue[i][j];
                    count = count + 1;
                }
            }
            _filter = new KernelJAI(width, height, floatArray);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the filtered image.
     *  @exception IllegalActionException If a contained method throws
     *  it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        RenderedOp newImage = JAI.create("convolve", oldImage, _filter);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The filter to convolve the image with */
    private KernelJAI _filter;

    /** A filter that does nothing to an image when convolved with it. */
    private double[][] _initialMatrix = {{0.0F, 0.0F, 0.0F},
                                         {0.0F, 1.0F, 0.0F},
                                         {0.0F, 0.0F, 0.0F}};
}

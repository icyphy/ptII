/* An actor that combines input bands for an output image.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.lib.jai;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// JAIBandCombine

/**
 Linearly combines input bands into an output image.  The matrix parameter
 specifies how many input bands there are, and how many output bands
 there will be.  The width of the matrix is equal to the number of
 input bands plus one.  The last column is used for offsetting.  The
 number of rows in the matrix dictates how many bands there are in the
 output image.  For example, to swap the second and third bands in a
 three banded image, the following matrix can be used:
 <pre>
 1 0 0 0
 0 0 1 0
 0 1 0 0
 </pre>

 @see JAIBandSelect
 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIBandCombine extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIBandCombine(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The initial value of the transformation matrix.
        double[][] initialMatrix = { { 1.0D, 0.0D, 0.0D, 0.0D },
                { 0.0D, 1.0D, 0.0D, 0.0D }, { 0.0D, 0.0D, 1.0D, 0.0D } };

        matrix = new Parameter(this, "matrix", new DoubleMatrixToken(
                initialMatrix));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The transformation matrix.  The entries in this matrix should
     *  doubles.  The default matrix passes each band with no offsetting.
     */
    public Parameter matrix;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the matrix values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == matrix) {
            _matrixValue = ((DoubleMatrixToken) matrix.getToken())
                    .doubleMatrix();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new attribute
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JAIBandCombine newObject = (JAIBandCombine) super.clone(workspace);
        newObject._matrixValue = null;
        return newObject;
    }

    /** Fire this actor.
     *  @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        parameters.addSource(oldImage);
        parameters.add(_matrixValue);

        RenderedOp newImage;

        try {
            newImage = JAI.create("bandCombine", parameters);
        } catch (IllegalArgumentException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to band combine the image\n" + ex.getMessage()
                            + "\n  Number of bands: " + oldImage.getNumBands()
                            + "\n  Image: " + oldImage.toString());
        }

        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The value of the transformation matrix */
    private double[][] _matrixValue;
}

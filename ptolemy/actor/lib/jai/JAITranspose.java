/* An actor that transposes a javax.media.jai.RenderedOp

@Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.TransposeDescriptor;
import javax.media.jai.operator.TransposeType;

//////////////////////////////////////////////////////////////////////////
//// JAITranspose

/**
 Transpose a RenderedOp.  The user can choose among six different
 transpositions.

 <i>flip antidiagonal</i> flips an image along the antidiagonal.
 <i>flip diagonal</i> flips an image along the diagonal.
 <i>flip horizontal</i> flips an image along the horizontal.
 <i>flip vertical</i> flips an image along the vertical.
 <i>rotate 90</i>, <i>rotate 180</i>, <i>rotate 270</i>, rotate an image
 90, 180, and 270 degrees respectively.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
*/

public class JAITranspose extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAITranspose(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        transposeType = new StringAttribute(this, "transposeType");
        transposeType.setExpression("flip horizontal");
        _transposeType = TransposeDescriptor.FLIP_HORIZONTAL;

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type of transposing to do.  This is a string valued
     *  attribute that defaults to type "flip horizontal",
     */
    public StringAttribute transposeType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and determine which operation to perform.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == transposeType) {
            String typeName = transposeType.getExpression();
            if (typeName.equals("flip antidiagonal")) {
                _transposeType = TransposeDescriptor.FLIP_ANTIDIAGONAL;
            } else if (typeName.equals("flip diagonal")) {
                _transposeType = TransposeDescriptor.FLIP_DIAGONAL;
            } else if (typeName.equals("flip horizontal")) {
                _transposeType = TransposeDescriptor.FLIP_HORIZONTAL;
            } else if (typeName.equals("flip vertical")) {
                _transposeType = TransposeDescriptor.FLIP_VERTICAL;
            } else if (typeName.equals("rotate 90")) {
                _transposeType = TransposeDescriptor.ROTATE_90;
            } else if (typeName.equals("rotate 180")) {
                _transposeType = TransposeDescriptor.ROTATE_180;
            } else if (typeName.equals("rotate 270")) {
                _transposeType = TransposeDescriptor.ROTATE_270;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized interpolation type: " + typeName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the transposed RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        RenderedOp newImage =
            JAI.create("transpose", oldImage, _transposeType);
        output.send(0, new JAIImageToken(newImage));
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An indicator for the type of transposition to use */
    private TransposeType _transposeType;

    private final int _FLIP_ANTIDIAGONAL = 3;
    private final int _FLIP_DIAGONAL = 2;
    private final int _FLIP_HORIZONTAL = 1;
    private final int _FLIP_VERTICAL = 0;
    private final int _ROTATE_90 = 4;
    private final int _ROTATE_180 = 5;
    private final int _ROTATE_270 = 6;
}

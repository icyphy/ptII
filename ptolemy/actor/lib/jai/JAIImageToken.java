/* A token that contains a javax.media.jai.RenderedOp.

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

import ptolemy.data.ImageToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIImageToken

/**
 A token that contains a javax.media.jai.RenderedOp.  This token is used
 when dealing with images in the Java Advanced Imaging (JAI) library.
 Because it extends ImageToken, it can be used with the standard image
 processing tools by simply calling asAWTImage().
 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
*/
public class JAIImageToken extends ImageToken {

    /** Construct a token with a specified RenderedOp.
     */
    public JAIImageToken(RenderedOp value) {
        _renderedOp = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token and
     *  the argument.  The image size is the size of the intersection of
     *  the two images.  The number of bands in the image is equal to
     *  the smallest number of bands of the two sources.  The data type
     *  is the smallest data type to have sufficient range for both input
     *  data types.
     *
     * @param rightArgument The token to add to this token.
     * @return A new token containing the result.
     * @exception IllegalActionException If the data type is not supported.
     */
    public Token add(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("add", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    /** Convert a javax.media.jai.RenderedOp to a BufferedImage, a
     *  subclass of awt.Image, and return it.
     *
     * @return A bufferedImage that is a rendering of the internal image.
     */
    public Image asAWTImage() {
        _bufferedImage = _renderedOp.getRendering().getAsBufferedImage();
        return _bufferedImage;
    }

    /** Return a new token whose value is the division of this
     *  token and the argument.  The image size is the size of the
     *  intersection of the two images.  The number of bands in the image
     *  is equal to the smallest number of bands of the two sources.  The
     *  data type is the bigger of the two input data types
     *
     * @param rightArgument The token to divide this token by.
     * @return A new token containing the result.
     * @exception IllegalActionException If the data type is not supported.
     */
    public Token divide(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("divide", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    //FIXME: There should be a new type for ImageTokens.
    /** Return the type of this token.
     *  @return BaseType.OBJECT
     */
    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the value of the token, a renderedop.
     *  @return The RenderedOp in this token.
     */
    public RenderedOp getValue() {
        return _renderedOp;
    }

    /** Return a new token whose value is the multiplication of this
     *  token and the argument.  The image size is the size of the
     *  intersection of the two images.  The number of bands in the image
     *  is equal to the smallest number of bands of the two sources.  The
     *  data type is the bigger of the two input data types
     *
     * @param rightArgument The token to multiply this token by.
     * @return A new token containing the result.
     * @exception IllegalActionException If the data type is not supported.
     */
    public Token multiply(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("multiply", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    /** Return a new token whose value is the subtraction of this token
     *  from the argument.  The image size is the size of the intersection
     *  of the two images.  The number of bands in the image is equal to
     *  the smallest number of bands of the two sources.  The data type
     *  is the smallest data type to have sufficient range for both input
     *  data types.
     *
     * @param rightArgument The token to subtract from this token.
     * @return A new token containing the result.
     * @exception IllegalActionException If the data type is not supported.
     */
    public Token subtract(Token rightArgument)
            throws IllegalActionException {
        if (rightArgument instanceof JAIImageToken) {
            ParameterBlock parameters =
                _parameterize(_renderedOp, (JAIImageToken) rightArgument);
            return new JAIImageToken(JAI.create("subtract", parameters));
        } else {
            throw new IllegalActionException("illegal data type");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Create a ParameterBlock containing two RenderedOp's, the first
    // being the internal Image, the second being from an ImageToken.
//     private ParameterBlock _parameterize(RenderedOp left, ImageToken right) {
//         ParameterBlock parameters = new ParameterBlock();
//         parameters.addSource(right.asAWTImage());
//         RenderedOp rightOp = JAI.create("awtImage", parameters);
//         parameters = new ParameterBlock();
//         parameters.addSource(left);
//         parameters.addSource(rightOp);
//         return parameters;
//     }

    // Create a ParameterBlock containing two RenderedOp's, the first
    // being the internal Image, the second being from a JAIImageToken.
    private ParameterBlock _parameterize(RenderedOp left, JAIImageToken right) {
        ParameterBlock parameters = new ParameterBlock();
        parameters.addSource(left);
        parameters.addSource(right.getValue());
        return parameters;
    }

    // Create a ParameterBlock containing one RenderedOp and one double,
    // the first being the internal Image, the second from a ScalarToken.
//     private ParameterBlock _parameterize(RenderedOp left, ScalarToken right)
//             throws IllegalActionException {
//         ParameterBlock parameters = new ParameterBlock();
//         parameters.addSource(left);
//         parameters.add(right.doubleValue());
//         return parameters;
//     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A buffered image that contains a rendering of the image.
    private BufferedImage _bufferedImage;

    // The internal RenderedOp.
    private RenderedOp _renderedOp;
}

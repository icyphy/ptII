/* A token that contains a javax.media.jai.RenderedOp.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

package ptolemy.actor.lib.jai;

import java.awt.image.BufferedImage;
import java.awt.Image;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import ptolemy.data.ImageToken;
import ptolemy.data.type.*;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// JAIImageToken
/**
   A token that contains a javax.media.jai.RenderedOp.  This token is used
   when dealing with images in the Java Advanced Imaging (JAI) library.
   Because it extends ImageToken, it can be used with the standard image
   processing tools by simply casting it, and calling asAWTImage().
   @author James Yeh
   @version $Id$
*/
public class JAIImageToken extends ImageToken {

    /** Construct a token with a specified RenderedOp.
     */
    public JAIImageToken(RenderedOp value) {
        _renderedOp = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert a javax.media.jai.RenderedOp to a java.awt.Image and
     *  return it.
     */
    public Image asAWTImage() {
        _planarImage = _renderedOp.getRendering();
        _bufferedImage = _planarImage.getAsBufferedImage();
        _awtImage = (Image) _bufferedImage;
        return _awtImage;
    }

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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Image _awtImage;
    private BufferedImage _bufferedImage;
    private PlanarImage _planarImage;
    private RenderedOp _renderedOp;

}

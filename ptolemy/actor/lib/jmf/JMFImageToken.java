/* A token that contains a javax.media.Buffer.

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

package ptolemy.actor.lib.jmf;

import java.awt.Image;

import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import ptolemy.data.ImageToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

//////////////////////////////////////////////////////////////////////////
//// JMFImageToken
/**
   A token that contains a javax.media.Buffer.  This token is used when
   dealing with images in the Java Media Framework (JMF) library.  Because
   it extends ImageToken, it can be used with the standard image processing
   tools by simply casting it, and calling asAWTImage().

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/
public class JMFImageToken extends ImageToken {

    /** Construct a token with a specified Buffer
     */
    public JMFImageToken(Buffer value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert a javax.media.Buffer to a java.awt.Image and return it.
     *  A new javax.media.util.BufferToImage is created when the
     *  video format of the Buffer changes.
     */
    public Image asAWTImage() {
        if (_bufferToImage == null || _videoFormat != _value.getFormat()) {
            _videoFormat = (VideoFormat) _value.getFormat();
            _bufferToImage = new BufferToImage(_videoFormat);
        }
        _awtImage = _bufferToImage.createImage(_value);
        return _awtImage;
    }

    /** Return the type of this token.
     *  @return BaseType.OBJECT
     */
    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the value of this token.
     */
    public Buffer getValue() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Buffer _value;
    private static BufferToImage _bufferToImage;
    private VideoFormat _videoFormat;
    private Image _awtImage;
}

/* A token that contains a java.awt.Image.

 Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.data;

import java.awt.Image;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

///////////////////////////////////////////////////////////////////
//// AWTImageToken

/**
 A token that contains a java.awt.Image.  This token is used in the
 standard image processing library.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class AWTImageToken extends ImageToken {
    /** Construct a token with a specified java.awt.Image.
     *  @param value  The given value
     */
    public AWTImageToken(Image value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Because all tokens that contain images must extend ImageToken,
     *  we must include the following method.
     */
    @Override
    public Image asAWTImage() {
        return _value;
    }

    @Override
    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the java.awt.Image object associated with this token.
     *  @return the java.awt.Image object.
     */
    public Image getValue() {
        return _value;
    }

    /** Return a description of the token.
     *  If possible, derived classes should override this method and
     *  return the value of this token as a string that can be parsed
     *  by the expression language to recover a token with the same value.
     *  Unfortunately, in this base class, we can only return the
     *  classname, the width and the height as a string representation of
     *  a record.
     *  @return The classname, width and height as string representation
     *  of a record.
     */
    @Override
    public String toString() {
        return "{type=\"" + getClass() + "\" width=\""
                + (_value == null ? -1 : _value.getWidth(null))
                + "\" height=\""
                + (_value == null ? -1 : _value.getHeight(null)) + "\"}";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The java.awt.Image */
    private Image _value;
}

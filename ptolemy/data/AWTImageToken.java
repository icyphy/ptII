/* A token that contains a java.awt.Image.

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
*/
package ptolemy.data;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;

import java.awt.Image;

//////////////////////////////////////////////////////////////////////////
//// AWTImageToken
/**
   A token that contains a java.awt.Image.  This token is used in the
   standard image processing library.
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class AWTImageToken extends ImageToken {

    /** Construct a token with a specified java.awt.Image.
     */
    public AWTImageToken(Image value) {
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Because all tokens that contain images must extend ImageToken,
     *  we must include the following method.
     */
    public Image asAWTImage() {
        return _value;
    }

    public Type getType() {
        return BaseType.OBJECT;
    }

    /** Return the java.awt.Image
     */
    public Image getValue() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The java.awt.Image */
    private Image _value;

}



/* This class provides basic utilities used across a variety of classes.

 Copyright (c) 2002-2005 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.copernicus.c;

//////////////////////////////////////////////////////////////////////////
//// Utilities

/**
 This class provides basic utilities used across a variety of classes.

 @author Ankush Varma
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (ankush)
 @Pt.AcceptedRating Red (ssb)
 */
public class Utilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enclose a given string of text within appropriate delimiters to
     *  form a comment in the generated code.
     *  Also, append a new line after the comment.
     *  @param text The text to place in the generated comment.
     *  @return The generated comment.
     *  Standard ANSI C comments are used here.
     */
    public static String comment(String text) {
        return ("/* " + text + " */\n");
    }

    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given
     *  indentation level.
     */
    public static String indent(int level) {
        StringBuffer indent = new StringBuffer();
        int i;

        for (i = 0; i < level; i++) {
            indent.append("    ");
        }

        return indent.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
}

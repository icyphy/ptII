/* Utilities for manipulating strings.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// StringUtilities
/**
A collection of utilities for manipulating strings.

@see ptolemy.gui.GUIStringUtilities
@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
public class StringUtilities {

    /** Instances of this class cannot be created.
     */
    private StringUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a string, replace all the instances of XML special characters
     *  with their corresponding XML entities.  This is necessary to
     *  allow arbitrary strings to be encoded within XML.  This method
     *  <pre>
     *  & becomes &amp;amp;
     *  " becomes &amp;quot;
     *  < becomes &amp;lt;
     *  > becomes &amp;gt;
     *  </pre>
     *
     *  @param string The string to escape.
     *  @return A new string with special characters replaced.
     */
    public static String escapeForXML(String string) {
        string = substitute(string, "&", "&amp;");
        string = substitute(string, "\"", "&quot;");
        string = substitute(string, "<", "&lt;");
        string = substitute(string, ">", "&gt;");
        return string;
    }

    /** Sanitize a String so that it can be used as a Java identifier.
     *  Section 3.8 of the Java language spec says:
     *  <blockquote>
     *  "An identifier is an unlimited-length sequence of Java letters
     *  and Java digits, the first of which must be a Java letter. An
     *  identifier cannot have the same spelling (Unicode character
     *  sequence) as a keyword (3.9), boolean literal (3.10.3), or
     *  the null literal (3.10.7).  "
     *  </blockquote>
     *  Java characters are A-Z, a-z, $ and _.
     *  <p> Characters that are not permitted in a Java identifier are changed
     *  to an underscores.
     *  This method does not check that the returned string is a
     *  keyword or literal.
     *  Note that two different strings can sanitize to the same
     *  string.
     *  This method is commonly used during code generation to map the
     *  name of a ptolemy object to a valid identifier name.
     *  @param name A string with spaces and other characters that
     *  cannot be in a Java name.
     *  @return A String that follows the Java identifier rules.
     */
    public static String sanitizeName(String name) {
	char [] nameArray = name.toCharArray();
       	for (int i = 0; i < nameArray.length; i++) {
	    if (!Character.isJavaIdentifierPart(nameArray[i])) {
		nameArray[i] = '_';
	    }
	}
      	if (!Character.isJavaIdentifierStart(nameArray[0])) {
            return "_" + new String(nameArray);
	} else {
            return new String(nameArray);
        }
    }

    /** Replace all occurrences of <i>old</i> in the specified
     *  string with <i>replacement</i>.
     *  @param string The string to edit.
     *  @param old The string to replace.
     *  @param replacement The string to replace it with.
     *  @return A new string with the specified replacements.
     */
    public static String substitute(String string,
            String old, String replacement) {
        int start = string.indexOf(old);
        while (start != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.delete(start, start + old.length());
            buffer.insert(start, replacement);
            string = new String(buffer);
            start = string.indexOf(old, start + replacement.length());
        }
        return string;
    }
}

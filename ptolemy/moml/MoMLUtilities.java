/* Utilities to make it easier to write MoML

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// MoMLUtilities
/**
A collection of utilities to make it easier to write MoML.

@author Steve Neuendorffer
@version $Id$
*/
public class MoMLUtilities {
    /** Instances of this class cannot be created.
     */
    private MoMLUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Given a string, replace all the instances of special characters
     * with their corresponding XML entities.  This is necessary to
     * allow arbitrary strings to be encoded within XML.  This method
     * replaces all instances of double quotes with "&quot;", all instances
     * of less than with "&lt;" and all instances of ampersand with "&amp;".
     */
    public static String escapeAttribute(String string) {
        string = substitute(string, "&", "&amp;");
        string = substitute(string, "\"", "&quot;");
        string = substitute(string, "<", "&lt;");
        return string;
    }

    /** Replace all occurances of old in string with new.
     *  Instances of new within old are not replaced.
     */
    public static String substitute(String string,
            String oldsub, String newsub) {
        int start = string.indexOf(oldsub);
        while(start != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.delete(start, start + oldsub.length());
            buffer.insert(start, newsub);
            string = new String(buffer);
            start = string.indexOf(oldsub, start + newsub.length());
        }
        return string;
    }
}

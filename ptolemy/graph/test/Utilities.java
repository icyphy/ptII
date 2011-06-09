/** Utilities for testing graphs.

 Copyright (c) 2001-2005 The University of Maryland. All rights reserved.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.graph.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Utilities

/**
 Utilities for testing graphs.
 This class provides utilities, in the form of static methods, for testing
 graphs.

 @author Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Utilities {
    // Private constructor to prevent instantiation.
    private Utilities() {
    }

    /** Given a collection, return a string representation of the collection.
     *  The representation returned is obtained by concatenating the
     *  string representations of the individual elements in their sorted
     *  order, as determined by the compareTo method of java.lang.String.
     *  If the <code>recursive</code> argument is true, then elements of
     *  the collection that are themselves collections are recursively
     *  converted to sorted strings using this method. When printing
     *  test results, this method can be used to guarantee a consistent
     *  representation for a collection regardless of the order in which
     *  elements are inserted and removed.
     *  @param collection The collection.
     *  @param recursive True if elements of the collection that are themselves
     *  collections should be recursively converted to sorted strings.
     *  @return The string representation.
     */
    public static String toSortedString(Collection collection, boolean recursive) {
        ArrayList result = new ArrayList(collection.size());
        Iterator elements = collection.iterator();

        while (elements.hasNext()) {
            Object element = elements.next();
            String elementString;

            if ((element instanceof Collection) && recursive) {
                elementString = toSortedString((Collection) element, recursive);
            } else {
                elementString = element.toString();
            }

            int i;

            for (i = 0; (i < result.size())
                    && (((String) result.get(i)).compareTo(elementString) < 0); i++) {
                ;
            }

            result.add(i, elementString);
        }

        return result.toString();
    }
}

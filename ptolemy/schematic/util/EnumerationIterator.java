/* A class for turning an enumeration into an iterator.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;
import java.util.*;

/**
 * An iterator that turns an Enumeration into an Iterator.  The Enumeration
 * is consumed in the process.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Id$
 */
public class EnumerationIterator implements Iterator {
    private Enumeration _enumeration;

    /* Construct a new iterator over the contents of the given iterator
     */
    public EnumerationIterator(Enumeration e) {
        _enumeration = e;
    }

    /* Test if there are more elements. 
     */
    public boolean hasNext() {
	return _enumeration.hasMoreElements();
    }

    /* Return the next object.
     */
    public Object next() {
        return _enumeration.nextElement();
    }

    public void remove() {
        throw new UnsupportedOperationException(
                "Enumeration iterator cannot delete element");
    }
}

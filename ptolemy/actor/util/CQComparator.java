/* Extension of Comparator interface for CalendarQueue

 Copyright (c) 1998 The Regents of the University of California.
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

package pt.actor.util;

import collections.*;

//////////////////////////////////////////////////////////////////////////
//// CQComparator
/**
This interface extends the Comparator interface which defines
int compare(Object, Object) method. The extension defines these
two additional methods:
<ul>
<li> long getBinIndex(Object key, Object zeroReference, Object binWidth)
<li> Object getBinWidth(Object[] keyArray)
</ul>
<p>
An object implementing this interface can be shared among different 
CalendarQueue instances, because it doesn't contain any state information.

@author Lukito Muliadi
@version $Id$
@see CalendarQueue
@see collections.Comparator
*/

public interface CQComparator extends Comparator{

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Given a key, a zero reference, and a bin width, return the index of 
     *  the bin containing the key.
     *  This operation is conceptually equivalent to:
     *  <i>(key-zeroReference) / binWidth</i>,
     *  where the division is an integer division
     *  <p>
     *  Classes that implement this interface will in general need to
     *  perform a downcast on the arguments (of type Object) to the
     *  appropriate user defined classes. If the arguments are not of 
     *  appropriate type, the implementation should throw a 
     *  ClassCastException.
     * @param key an object representing the sort key.
     * @param zeroReference an object representing the zero reference.
     * @param binWidth an object representing the bin width.
     * @return The index of the bin.
     * @exception ClassCastException Incompatible argument type.
     */
    public long getBinIndex(Object key, Object zeroReference, Object binWidth);

    /** Given an array of objects, return the bin width.
     * @param keyArray the array of key objects.
     * @return The bin width.
     * @exception ClassCastException Incompatible argument type.
     */
    public Object getBinWidth(Object[] keyArray);
}














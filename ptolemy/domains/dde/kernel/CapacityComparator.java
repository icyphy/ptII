/* A CapacityComparator is used to facilitate comparisons of the 
relative queue sizes of write blocked queues. 

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.kernel;

import collections.Comparator;

//////////////////////////////////////////////////////////////////////////
//// CapacityComparator
/**
CapacityComparator is used to facilitate comparisons of the relative
queue sizes of write blocked queues. Using the compare method, write
blocked queues can be sorted in O(nlogn) time.

@author John S. Davis II
@version $Id$
*/

public interface CapacityComparator extends Comparator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare two DDE receivers with respect to their relative 
     *  queue capacities.
     * @return A positive number if first > second; a negative number
     *  if first < second; otherwise returns zero. 
     */
    public int compare(DDEReceiver first, DDEReceiver second);


}

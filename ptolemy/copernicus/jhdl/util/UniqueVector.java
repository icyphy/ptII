/* A Data Flow Graph generated from an IntervalChain

 Copyright (c) 2001-2003 The Regents of the University of California.
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

package ptolemy.copernicus.jhdl.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// UniqueVector
/**
This class is a Vector object that guarantees that an object is
added only once. This method overrides the add and addAll methods
of Vector.

@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class UniqueVector extends Vector {

    public UniqueVector() { super(); }
    public UniqueVector(int i) { super(i); }
    public UniqueVector(Collection c) {
	super(c.size());
	addAll(c);
    }

    public boolean add(Object o) {
	if (!contains(o))
	    super.add(o);
	return true;
    }

    public boolean addAll(Collection c) {
	if (c!= null)
	    for (Iterator i=c.iterator();i.hasNext();)
		add(i.next());
	return true;
    }

}


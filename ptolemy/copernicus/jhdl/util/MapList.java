/*

 Copyright (c) 2001-2002 The Regents of the University of California.
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

import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// MapList
/**
 *
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/

/**
 * This interface provides a mapping between Objects and lists.
 * The value of the key/value pair of the Map is a List object.
 * This allows multiple Objects to be mapped with a single key.
 *
 **/
public interface MapList extends Map {

    public void add(Object key, Object value);
    /** Get the Object at position <code>index</code>
     * associated with Object key **/
    public Object get(Object key, int index);
    /** Get the first object associated with Object key **/
    public Object getFirst(Object key);
    /** Get the last object associated with Object key **/
    public Object getLast(Object key);
    /** Get the List object associated with Object key **/
    public List getList(Object key);
    public void setList(Object key, List l);
    public Object clone();
}

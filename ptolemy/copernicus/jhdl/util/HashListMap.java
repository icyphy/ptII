/*

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

import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import ptolemy.copernicus.jhdl.util.MapList;

//////////////////////////////////////////////////////////////////////////
//// HashListMap
/**
 *
@author Mike Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class HashListMap extends HashMap implements MapList {

    public HashListMap() { super(); }
    public HashListMap(int i) { super(i); }
    public HashListMap(HashListMap hlm) {
        this(hlm.size());
        for (Iterator i=hlm.keySet().iterator();i.hasNext();) {
            Object o = i.next();
            Vector l = (Vector) hlm.get(o);
            Vector copyl = (Vector) l.clone();
            setList(o,copyl);
        }
    }

    public void add(Object key, Object value) {
        List l = getCreateList(key);
        l.add(value);
    }

    public List getCreateList(Object key) {
        List l = getList(key);
        if (l == null) {
            l = new Vector();
            put(key,l);
        }
        return l;
    }

    public Object get(Object key, int index) {
        List l=getList(key);
        if (l == null)
            return null;
        int size = l.size();
        if (0 <= index && index < size) {
            return l.get(index);
        } else
            return null;
    }

    public Object getFirst(Object key) {
        List l=getList(key);
        if (l==null)
            return null;
        int size = l.size();
        if (size > 0) {
            return l.get(0);
        } else
            return null;
    }

    public Object getLast(Object key) {
        List l=getList(key);
        if (l == null)
            return null;
        int size = l.size();
        if (size > 0) {
            return l.get(size-1);
        } else
            return null;
    }

    public List getList(Object o) {
        return (List) get(o);
    }

    public void setList(Object o, List l) {
        put(o,l);
    }

    public Object clone() {
        return new HashListMap(this);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator i=keySet().iterator();i.hasNext();) {
            Object o = i.next();
            sb.append(o+" MAPS TO: ");
            for (Iterator j=getList(o).iterator();j.hasNext();) {
                sb.append(j.next()+" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}

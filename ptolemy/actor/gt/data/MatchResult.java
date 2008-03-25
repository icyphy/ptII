/*

@Copyright (c) 1997-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor.gt.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.kernel.util.NamedObj;

public class MatchResult extends TwoWayHashMap<Object, Object> {

    public void clear() {
        super.clear();
        _keySequence.clear();
    }

    public Object clone() {
        MatchResult result = (MatchResult) super.clone();
        result._keySequence = new LinkedList<Object>(_keySequence);
        return result;
    }

    public Object put(Object key, Object value) {
        if (!super.containsKey(key)) {
            _keySequence.add(key);
        }
        return super.put(key, value);
    }

    public Object remove(Object key) {
        Object oldValue = super.remove(key);
        if (oldValue != null) {
            _keySequence.remove(key);
        }
        return oldValue;
    }

    public void retain(int count) {
        int size = _keySequence.size();
        if (size > count) {
            ListIterator<Object> iterator = _keySequence.listIterator(size);
            for (; size > count; size--) {
                Object key = iterator.previous();
                iterator.remove();
                super.remove(key);
            }
        }
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean allMatches) {
        Comparator<Object> keyComparator = new Comparator<Object>() {
            public int compare(Object key1, Object key2) {
                return key1.toString().compareTo(key2.toString());
            }
        };

        // Return a deterministic string for the map.
        StringBuffer buffer = new StringBuffer("{");
        List<Object> keys = new LinkedList<Object>(keySet());
        Collections.sort(keys, keyComparator);
        int i = 0;
        for (Object key : keys) {
            if (!allMatches && !(key instanceof NamedObj)) {
                continue;
            }
            if (i++ != 0) {
                buffer.append(", ");
            }
            buffer.append(key);
            buffer.append(" = ");
            buffer.append(get(key));
        }
        buffer.append("}");
        return buffer.toString();
    }

    private List<Object> _keySequence = new LinkedList<Object>();

}

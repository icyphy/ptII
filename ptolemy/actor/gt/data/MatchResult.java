/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

import ptolemy.actor.gt.ValueIterator;
import ptolemy.data.Token;
import ptolemy.kernel.util.NamedObj;

public class MatchResult extends SequentialTwoWayHashMap<Object, Object> {

    public MatchResult() {
    }

    public MatchResult(
            SequentialTwoWayHashMap<ValueIterator, Token> parameterValues) {
        _parameterValues = parameterValues;
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        MatchResult result = (MatchResult) super.clone();
        result._parameterValues = (SequentialTwoWayHashMap) _parameterValues
                .clone();
        return result;
    }

    public SequentialTwoWayHashMap<ValueIterator, Token> getParameterValues() {
        return _parameterValues;
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

    private SequentialTwoWayHashMap<ValueIterator, Token> _parameterValues;

}

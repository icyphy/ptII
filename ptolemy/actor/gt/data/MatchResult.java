/* A two-way hash map data structure to store matches found in a pattern
   matching.

@Copyright (c) 2007-2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// MatchResult

/**
 A two-way hash map data structure to store matches found in a pattern matching.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
@SuppressWarnings("serial")
public class MatchResult extends SequentialTwoWayHashMap<Object, Object> {

    /** Construct an empty match result.
     */
    public MatchResult() {
    }

    /** Construct an empty match result and tag it to be obtained by a specific
     *  assignment to the parameters.
     *
     *  @param parameterValues The two-way hash map specifying the assignment to
     *   the parameters. In this case, values may be the same for some keys
     *   because the reverse hash map is never used.
     */
    public MatchResult(
            SequentialTwoWayHashMap<ValueIterator, Token> parameterValues) {
        _parameterValues = parameterValues;
    }

    /** Clone this match result and return the clone.
    *
    *  @return The clone.
    */
    public Object clone() {
        MatchResult result = (MatchResult) super.clone();
        result._parameterValues = (SequentialTwoWayHashMap) _parameterValues
                .clone();
        return result;
    }

    /** Get the parameter assignment.
     *
     *  @return The two-way hash map for the assignment.
     */
    public SequentialTwoWayHashMap<ValueIterator, Token> getParameterValues() {
        return _parameterValues;
    }

    /** Generate a string describing this match result, ignoring the matches
     *  between objects that are not instances of {@link NamedObj}.
     *
     *  @return The string.
     */
    public String toString() {
        return toString(false);
    }

    /** Generate a string describing this match result. If allMatches is false,
     *  the matches between objects that are not instances of {@link NamedObj}
     *  are ignored. Otherwise, those matches are also included in the string.
     *
     *  @param allMatches Whether the matches between objects that are not
     *   instances of {@link NamedObj} are included.
     *  @return The string.
     */
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

    /** The assignment of values to the parameters.
     */
    private SequentialTwoWayHashMap<ValueIterator, Token> _parameterValues;
}

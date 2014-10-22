/* A helper for testing the graph matching.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.MatchCallback;
import ptolemy.actor.gt.data.MatchResult;

/**
 A helper for testing the graph matching.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AllMatchingTestHelper {

    /** Generate a string that represents all the received match results.
     *
     *  @return The string of match results.
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        Iterator<MatchResult> iterator = _results.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (first) {
                first = false;
            } else {
                buffer.append('\n');
            }
            buffer.append(iterator.next());
        }
        return buffer.toString();
    }

    /** A callback that records all the match results, and keeps the matching
     *  algorithm executing until all matches are found.
     */
    public final MatchCallback callback = new MatchCallback() {
        @Override
        public boolean foundMatch(GraphMatcher matcher) {
            _results.add((MatchResult) matcher.getMatchResult().clone());
            return false;
        }
    };

    /** The list of match results.
     */
    private List<MatchResult> _results = new LinkedList<MatchResult>();
}

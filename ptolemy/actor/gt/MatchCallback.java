/* An interface of callback routines to be invoked during the matching.

@Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.gt;

import ptolemy.actor.gt.data.MatchResult;

//////////////////////////////////////////////////////////////////////////
////MatchCallback

/**
 An interface of callback routines to be invoked during the matching.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see GraphMatcher#setMatchCallback(MatchCallback)
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface MatchCallback {

    /** A routine to be called when a match is found in the graph matching. The
     *  graph matcher is passed in as the parameter, and the current match can
     *  be obtained by calling {@link GraphMatcher#getMatchResult()}.
     *  This match result should not be kept, however, because it may be changed
     *  by future matching operations. To keep a copy of this result, invoke
     *  {@link MatchResult#clone()} and keep the cloned copy. The return value
     *  indicates whether the match is the one looked for. If it is
     *  <tt>true</tt>, the matching will terminate after this routine returns,
     *  and no more match result will be reported.
     *
     *  @param matcher The graph matcher.
     *  @return Whether the matching should terminate right away.
     */
    public boolean foundMatch(GraphMatcher matcher);

}

/* An optimized scheduler for the SR domain.

 Copyright (c) 2000-2008 The Regents of the University of California.
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
package ptolemy.domains.sr.kernel;

import ptolemy.actor.sched.FixedPointScheduler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// SROptimizedScheduler

/**
 A scheduler the Synchronous Reactive (SR) domain.  This scheduler returns
 a static schedule for the graph.  The schedule guarantees that the values will
 converge to a fixed-point.
 <p>
 The recursive scheduling algorithm is due to Stephen Edwards, and is
 described in his Ph.D. thesis.  First, a dependency graph is constructed, and
 the strongly connected components (SCC) are determined.  A schedule for each
 SCC is obtained by separating the sub-graph into a head and a tail, and then
 recursively applying this algorithm to both the head and the tail.  The
 schedule for the SCC is (<i>TH</i>)<super><i>n</i></super><i>T</i> where
 <i>H</i> and <i>T</i> are the schedules of the head and tail, respectively, and
 <i>n</i> is the number of nodes in the head and represents the number of
 repetitions of the parenthesized expression.  Finally, the schedules of the
 top-level SCCs are concatenated in topological order to obtain the schedule
 for the entire graph.
 <p>
 FIXME: This is not implemented!  This is a placeholder that uses the same
 naive scheduler as the base class.

 @author Paul Whitaker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (pwhitake)
 @Pt.AcceptedRating Red (pwhitake)
 @see ptolemy.domains.sr.kernel.SRDirector
 */
public class SROptimizedScheduler extends FixedPointScheduler {

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this scheduler.
     *  @exception IllegalActionException If the scheduler is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SROptimizedScheduler(SRDirector container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
}

/* A sequential scheduler that enables guessing the schedule.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.sequence.kernel;

import java.util.List;
import java.util.Vector;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PartialSequenceScheduler

/**
 * A sequential scheduler that enables guessing the schedule.
 * @see SequenceScheduler for scheduling behavior.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ristau)
 * @Pt.AcceptedRating Red (ristau)
 */
public class PartialSequenceScheduler extends SequenceScheduler {

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PartialSequenceScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _estimator = new ListSchedulingSequenceEstimator(container);
    }

    /** Estimate a sequenced schedule. Currently only supports basic Dijkstra
     * algorithm for getting the maximal distances. Thus, it cannot deal with
     * cyclic graphs.
     *
     * @param independentList The already present SequenceAttributes for the
     * Actors controlled by this scheduler.
     *
     * @return A vector with the ordered actors. Note that the sequence numbers
     * are not changed. This has to be done somewhere else.
     *
     * @exception NotSchedulableException If the schedule is acyclic.
     */
    public Vector<Actor> estimateSequencedSchedule(
            List<SequenceAttribute> independentList)
                    throws NotSchedulableException {
        // FIXME: It may occur that the _actorGraph is null.
        // If this is the case and the graph is not acyclic, this
        // method will hang in an infinite while loop.

        if (_actorGraph == null) {
            return _estimator.estimateSequencedSchedule(independentList);
        } else if (_actorGraph.isAcyclic()) {
            return _estimator.estimateSequencedSchedule(independentList);
        } else {
            throw new NotSchedulableException(this,
                    "Cannot estimate schedule for cyclic graphs.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private SequenceEstimator _estimator;

}

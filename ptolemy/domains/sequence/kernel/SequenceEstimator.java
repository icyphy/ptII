/* A base class for estimating sequence numbers in the sequence domain.

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

///////////////////////////////////////////////////////////////////
//// SequenceEstimator

/** A base class for estimating sequence numbers in the sequence domain.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ristau)
 * @Pt.AcceptedRating Red (ristau)
 */
public abstract class SequenceEstimator {

    /** Construct an estimator for the given director.
     *
     *  @param director The director that needs to guess a schedule.
     */
    public SequenceEstimator(Director director) {
        _director = director;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Estimate a sequenced schedule.
     *
     * @param independentList The already present SequenceAttributes for the
     * Actors controlled by this scheduler.
     *
     * @return A vector with the ordered actors. Note that the sequence numbers
     * are not changed. This has to be done somewhere else.
     *
     * @exception NotSchedulableException If the underlying graph of the
     * actors is not acyclic.
     */
    public abstract Vector<Actor> estimateSequencedSchedule(
            List<SequenceAttribute> independentList)
                    throws NotSchedulableException;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The director that controls the execution of the actors to be sequenced. */
    protected Director _director;

}

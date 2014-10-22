/* Firing object to be listed in a static Schedule to allow for different
 * firing modes.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.domains.sdf.optimize;

import ptolemy.actor.Actor;
import ptolemy.actor.sched.Firing;

///////////////////////////////////////////////////////////////////
//// BufferingProfileFiring

/**
<h1>Class comments</h1>
BufferingProfileFiring is a subclass of Firing. A Schedule object represents a static
schedule of actor firings. An object of this class enable the use of different firing modes.
It is used by the OptimizingSDFDirector and OptimizingSDFSchedule duo when generating
schedules
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector},
{@link ptolemy.domains.sdf.optimize.OptimizingSDFScheduler} and
{@link ptolemy.domains.sdf.optimize.BufferingProfile} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.OptimizingSDFScheduler
@see ptolemy.domains.sdf.optimize.BufferingProfile

@author Marc Geilen
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
 */
public class BufferingProfileFiring extends Firing {

    /**
     * Construct an instance representing a firing of the given actor
     * in the given mode, shared or exclusive.
     * @param actor the actor which is to fire
     * @param exclusive indicates firing is to be exclusive
     */
    public BufferingProfileFiring(Actor actor, boolean exclusive) {
        super(actor);
        fireExclusive = exclusive;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Indicates whether this firing is to be performed exclusively (true)
     * or shared (false).
     */
    public boolean fireExclusive;

}

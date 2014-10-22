/* DummyDisplay is a simple sink implementing the BufferingProfile interface.
It is used for testing the OptimizingSDFDirector.

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

package ptolemy.domains.sdf.optimize.lib;

import ptolemy.actor.lib.Sink;
import ptolemy.domains.sdf.optimize.BufferingProfile;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
<h1>Class comments</h1>
A DummyDisplay is a simple sink implementing the BufferingProfile interface.
It is used for testing the OptimizingSDFDirector.
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

public class DummyDisplay extends Sink implements BufferingProfile {

    /**
     * Create an instance of a testing actor to mimic a display sink actor.
     *
     * @param container The container.
     * @param name The name of this actor.
     * @exception IllegalActionException If the entity cannot be contained
     *  by the proposed container.
     * @exception NameDuplicationException If the container already has an
     *  actor with this name.
     */
    public DummyDisplay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /**
     *  Iterates the dummy display actor.
     *  @param iterationCount The number of iterations to perform.
     *  @param fireExclusive Indicates whether firing is exclusive.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    @Override
    public int iterate(int iterationCount, boolean fireExclusive)
            throws IllegalActionException {
        return super.iterate(iterationCount);
    }

    /**
     * Provides the buffering profile, number of buffers required for a shared firing.
     * @return number of buffers for shared firing
     */
    @Override
    public int sharedBuffers() {
        return 0;
    }

    /**
     * Provides the buffering profile, number of buffers required for an exclusive firing.
     * @return number of buffers for exclusive firing
     */
    @Override
    public int exclusiveBuffers() {
        return 0;
    }

    /**
     * Provides the buffering profile, execution time estimate required for a shared firing.
     * @return execution time for shared firing
     */
    @Override
    public int sharedExecutionTime() {
        return 1;
    }

    /**
     * Provides the buffering profile, execution time estimate required for an exclusive
     * firing.
     * @return execution time for exclusive firing
     */
    @Override
    public int exclusiveExecutionTime() {
        return 1;
    }

}

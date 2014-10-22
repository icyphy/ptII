/* A dummy source actor for testing purposes

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

package ptolemy.domains.sdf.optimize.lib;

import ptolemy.actor.lib.Source;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.optimize.BufferingProfile;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DummySource

/**
<h1>Class comments</h1>
A simple actor acting as a dummy source used for testing the OptimizingSDFDirector.
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

public class DummySource extends Source implements BufferingProfile {
    private int _counter;

    /**
     * Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DummySource(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Fire the source actor and output a frame.
     *  @exception IllegalActionException If thrown while writing to the port.
     */
    @Override
    public void fire() throws IllegalActionException {
        DummyFrame f = new DummyFrame();
        f.value = _counter;
        output.send(0, new DummyReferenceToken(f));
    }

    /**
     * Post-fire the source actor and update the counter to give
     * next token a unique sequence number.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException Not thrown in this class or it base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _counter++;
        return super.postfire();
    }

    /** Initialize the actor. Initializes its counter.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _counter = 0;
    }

    /**
     * Iterate the actor.
     * @param iterationCount The number of iterations to perform.
     * @param fireExclusive Determines whether the firing is exclusive.
     * @return NOT_READY, STOP_ITERATING, or COMPLETED.
     * @exception IllegalActionException If iterating is not
     *  permitted, or if prefire(), fire(), or postfire() throw it.
     */
    @Override
    public int iterate(int iterationCount, boolean fireExclusive)
            throws IllegalActionException {
        return super.iterate(iterationCount);
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
     * Provides the buffering profile, execution time estimate required for an exclusive
     * firing.
     * @return execution time for exclusive firing
     */
    @Override
    public int exclusiveExecutionTime() {
        return 0;
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
     * Provides the buffering profile, execution time estimate required for a shared firing.
     * @return execution time for shared firing
     */
    @Override
    public int sharedExecutionTime() {
        return 0;
    }

}

/* An actor that must have preinitialize() invoked each iteration.

 Copyright (c) 2016 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc.test;

import ptolemy.actor.lib.Ramp;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Preinitialize() must be invoked after wrapup.
 *
 * <p>Test for problems similar to what we see in Matlab Engine, where wrapup()
 * closes the connection.  If this actor is used in a RunComposite, preinitialize()
 * must be called before the other action methods.</p>
 *
 * <p>See ptolemy/matlab/test/MatlabRunComposite.xml.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PreinitializeMustBeInvoked extends Ramp {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PreinitializeMustBeInvoked(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (!_preinitializedCalled) {
            throw new IllegalActionException("fire()" + _ERROR_MESSAGE);
        }
    }

    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (!_preinitializedCalled) {
            throw new IllegalActionException("initialize()" + _ERROR_MESSAGE);
        }
    }

    @Override
    public boolean prefire() throws IllegalActionException {
        if (!_preinitializedCalled) {
            throw new IllegalActionException("prefire()" + _ERROR_MESSAGE);
        }
        return super.prefire();
    }

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _preinitializedCalled = true;
    }

    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _preinitializedCalled = false;
    }

    private String _ERROR_MESSAGE = " was called after wrapup() was called but preinitialize() was not called after wrapup().";

    /** True if preinitialize() has been called and wraup() has not
     * yet been called.
     */
    private boolean _preinitializedCalled = false;
}

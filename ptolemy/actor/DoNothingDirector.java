/* A director that does nothing.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

///////////////////////////////////////////////////////////////////
//// DoNothingDirector

/**
 A director that does nothing, for use in models that have no useful
 execution.

 <p>This director is added to models for code generation purposes when
 the model has no useful execution but the code generator runs
 CompositeActor.preinitialize().</p>

 <p>If this director is used in a model, then adding a <code>_hide</code>
 attribute to the director will make the director invisible to the user.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DoNothingDirector extends Director {
    /** Construct a director with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DoNothingDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public void addInitializable(Initializable initializable) {
    }

    @Override
    public void fire() {
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean postfire() {
        return false;
    }

    @Override
    public boolean prefire() {
        return false;
    }

    @Override
    public void preinitialize() {
        // When exporting the ClassesIllustrated model, the
        // model would run forever because the value returned
        // by prefire() was not being checked in Manager.
        // If finish() is called then the bug would have
        // been avoided.
        Nameable container = getContainer();

        if (container instanceof CompositeActor) {
            ((CompositeActor) container).getManager().finish();
        }
    }

    @Override
    public boolean transferInputs(IOPort port) {
        return false;
    }

    @Override
    public boolean transferOutputs(IOPort port) {
        return false;
    }
}

/* A composite actor for the taskpt domain that performs some computation.

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
package ptolemy.domains.taskpt.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.taskpt.kernel.TaskDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Task

/** A task in the taskpt domain. It consumes all input tokens at the
 * beginning and produces all output tokens at the end.
 *
 * <p>Input and output tokens are not passed directly, but via pointers that point to
 * an address (range) in a shared memory that is controlled by the director
 * of the container of a task. That's why a task has input/output ports for
 * specifying the outputs. The token on the outside of this port specifies
 * the address, where the tokens have to be stored in the shared memory. The
 * inside of this port receives the tokens to be stored. A task
 * works on copies of the data read from memory. A
 * TaskDirector takes care of transferring the outputs and inputs. The TaskDirector
 * also provides a shared memory that is exclusive to all actors within this task.
 * This ensures that a task is free of side effects.</p>
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public class Task extends TypedCompositeActor {

    /** Construct a Task with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  The director of the Task is
     *  set to be a TaskDirector.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException Thrown if the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException Thrown if the name coincides with
     *   an actor already in the container.
     */
    public Task(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        new TaskDirector(this, "TaskDirector");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}

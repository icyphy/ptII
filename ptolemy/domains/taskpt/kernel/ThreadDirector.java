/* A director for controlling threads in the taskpt domain.

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
package ptolemy.domains.taskpt.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ThreadDirector

/** A director for controlling a thread in the taskpt domain.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 * @see ptolemy.domains.taskpt.kernel.TaskPtDirector
 */
public class ThreadDirector extends TaskPtDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The director will have a default scheduler of type
     *  SequenceScheduler. In addition to invoking the base class constructor
     *  the memory is initialized.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ThreadDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the actors in sequence.

     *  @exception IllegalActionException Thrown if any actor executed by this
     *  actor return false in prefire().
     */
    @Override
    public void fire() throws IllegalActionException {
        //FIXME: Currently all actors are fired in sequence as defined in
        //the sequenced schedule. In the future, this should defer tasks
        //(ptolemy.domains.taskpt.lib.Task) to
        //some sort of "CoreManager" that executes these tasks superscalar and
        //binds the tasks to different processors. Once a task is deferred, the
        //next actors in sequence should be fired until some actor is in the sequence
        //that is dependent upon the output of deferred and not already finished
        //tasks.
        super.fire();
    }
}

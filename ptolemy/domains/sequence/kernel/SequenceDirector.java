/* Director for the sequencing model of computation.

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
package ptolemy.domains.sequence.kernel;

import java.util.Collections;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SequenceDirector

/**
 * A director that executes actors in a sequence explicitly specified
 * by the model. Actors are required to either contain an instance
 * of SequenceAttribute or to be upstream of an actor that contains
 * an instance of SequenceAttribute. Each SequenceAttribute contains
 * a unique integer that specifies the position in the sequence for
 * the actor. This director will sort the list of actors that
 * contain a SequenceAttribute according to these integers, and
 * fire them in order. Before each firing, it will also fire any
 * upstream actors actors that do not contain an instance of SequenceAttribute.
 * FIXME: In what order are those actors fired?
 * FIXME: What if the upstream actors do contain a SequenceAttribute?
 * FIXME: If an actor is upstream of two actors with SequenceAttribute,
 * does it fire twice in an iteration? Are it's output valued queued?
 *
 * <p>
 *
 * The SequenceDirector computes the sequenced actors and passes these in two lists to the
 * SequenceScheduler (one list for independent sequenced actors, and one list
 * for sequenced actors that are dependent on other actors e.g. control actors)
 *
 * Please see SequencedModelDirector for more details on how the schedule
 * is computed.
 *
 * @author Elizabeth Latronico (Bosch), rrs1pal
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 */
public class SequenceDirector extends SequencedModelDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *
     *  The SequenceDirector will have a default scheduler of type SequenceScheduler.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SequenceDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The SequenceDirector will have a default scheduler of type SequenceScheduler.
     *
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SequenceDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *   The SequenceDirector will have a default scheduler of type
     *   SequenceScheduler.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public SequenceDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Iterating an actor involves calling the actor's iterate() method,
     *  which is equivalent to calling the actor's prefire(), fire() and
     *  postfire() methods in succession.  If iterate() returns NOT_READY,
     *  indicating that the actor is not ready to execute, then an
     *  IllegalActionException will be thrown. The values returned from
     *  iterate() are recorded and are used to determine the value that
     *  postfire() will return at the end of the director's iteration. <p>
     *
     *  This method may be overridden by some domains to perform additional
     *  domain-specific operations.
     *  @exception IllegalActionException If any actor executed by this
     *  actor return false in prefire.
     *  @exception InvalidStateException If this director does not have a
     *  container.
     */
    @Override
    public void fire() throws IllegalActionException {

        // Must call preinitialize() before fire(), since otherwise the
        // schedule will not be set
        // Call superclass function with _schedule as argument to handle firing
        fireSchedule(_schedule);

        // If the fireUnexectedActors parameter is true,
        // fire all the unexecuted actors on branches that are not taken
        // after the full schedule is completed.
        if (((BooleanToken) fireUnexecutedActors.getToken()).booleanValue()) {

            List<SequenceAttribute> unexecutedList = _schedule
                    .getUnexecutedList();
            SequenceSchedule unexecutedSchedule = null;

            while (!unexecutedList.isEmpty()) {
                unexecutedSchedule = _scheduler.getSchedule(unexecutedList,
                        false);

                fireSchedule(unexecutedSchedule);
                unexecutedList = unexecutedSchedule.getUnexecutedList();
            }
        }
    }

    /** Preinitialize the actors associated with this director and
     *  compute the schedule.  The schedule is computed during
     *  preinitialization so that hierarchical opaque composite actors
     *  can be scheduled properly.  In addition, performing scheduling
     *  during preinitialization enables it to be present during code generation.
     *  The order in which the actors are preinitialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */

    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Sort the list of sequenced actors
        // The scheduler will check for duplicates
        // The ProcessDirector is different from the SequenceDirector
        // in that we don't want everything to be in one list.  However,
        // .sort() knows how to handle sequence attributes vs. process attributes
        Collections.sort(_sequencedList);

        // Call getSchedule and pass in the _sequencedList
        _schedule = _scheduler.getSchedule(_sequencedList);

        // Check to see if there are any unreachable upstream actors
        // Different in that all schedules must be processed before calling this
        if (_scheduler.unreachableActorExists()) {
            // Throw an exception for unreachable actors
            // One exclusion:  TestExceptionHandler actors are not reported
            // since these are disconnected from the model, but don't have any
            // functionality for the model
            // This could be changed in the future to have an option to allow unreachable actors
            StringBuffer unreachableActors = new StringBuffer("");

            for (Actor a : _scheduler.unreachableActorList()) {
                unreachableActors.append(a.getFullName() + ", ");
            }

            // Remove the last two characters ", "
            // Throw exception
            throw new IllegalActionException(
                    "There are unreachable upstream actors in the model: "
                            + unreachableActors.substring(0,
                                    unreachableActors.length() - 2));
        }

        // The firing iterator is called in prefire()
        // This ensures a new sequence of firings for each iteration
        // FIXME:  Also get an actor iterator?  Actor iterators are not implemented yet in SequenceSchedule.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The top level SequenceSchedule object.  */
    private SequenceSchedule _schedule;
}

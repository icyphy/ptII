/* Director for the Synchronous Reactive model of computation.

 Copyright (c) 2000-2007 The Regents of the University of California.
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
package ptolemy.domains.sr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SRDirector

/**
 A director for the Synchronous Reactive (SR) model of computation.

 @author Paul Whitaker, Contributor: Ivan Jeukens, Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class SRDirector extends FixedPointDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SRDirector() throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SRDirector(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public SRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Request a firing of the given actor at the given absolute
     *  time.  This method delegates to the enclosing director
     *  if there is one, and otherwise ignores the request.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     */
    public void fireAt(Actor actor, Time time) throws IllegalActionException {
        Actor container = (Actor)getContainer();
        if (container != null) {
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                executiveDirector.fireAt(container, time);
            }
        }
    }

    /** Request a firing of the given actor at the current
     *  time.  This method delegates to the enclosing director
     *  if there is one, and otherwise ignores the request.
     *  @param actor The actor scheduled to be fired.
     *  @exception IllegalActionException If the enclosing director
     *   throws it.
     */
    public void fireAtCurrentTime(Actor actor) throws IllegalActionException {
        Actor container = (Actor)getContainer();
        if (container != null) {
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                executiveDirector.fireAtCurrentTime(container);
            }
        }
    }
}

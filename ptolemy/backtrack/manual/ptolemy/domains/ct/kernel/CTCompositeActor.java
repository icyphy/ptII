/* Composite actor that supports backtracking in the CT domain.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.manual.ptolemy.domains.ct.kernel;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.backtrack.Checkpoint;
import ptolemy.backtrack.Rollbackable;
import ptolemy.domains.ct.kernel.CTStatefulActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// CTCompositeActor

/**
 Composite actor that supports backtracking in the CT domain.
 <p>
 This composite actor records the state of all the actors (atomic or
 composite) in it when requested by the Continuous-Time (CT) director. The
 CT director may request a rollback to the previous state later.
 <p>
 As a property of CT, only one-level backtracking is needed. This means the
 previous states of the actors earlier than the last state can be safely
 discarded.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class CTCompositeActor extends TypedCompositeActor implements
        CTStatefulActor {
    /** Construct a CTCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */
    public CTCompositeActor() {
        super();

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is CTCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as CTCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be CTCompositeActor.
        setClassName("ptolemy.backtrack.manual.ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    /** Construct a CTCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CTCompositeActor(Workspace workspace) {
        super(workspace);

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is CTCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as CTCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be CTCompositeActor.
        setClassName("ptolemy.backtrack.manual.ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    /** Construct a CTCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CTCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is CTCompositeActor.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as CTCompositeActor.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be CTCompositeActor.
        setClassName("ptolemy.backtrack.manual.ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    /** Restore the previously recorded state to all the actors in this
     *  composite actor.
     *
     *  @throws IllegalActionException Not thowned.
     */
    public void goToMarkedState() throws IllegalActionException {
        Set checkpoints = new HashSet();
        Iterator objectsIter = containedObjectsIterator();

        while (objectsIter.hasNext()) {
            Object object = objectsIter.next();

            if (object instanceof Rollbackable) {
                Rollbackable rollbackObject = (Rollbackable) object;
                Checkpoint checkpoint = rollbackObject.$GET$CHECKPOINT();

                if (!checkpoints.contains(checkpoint)) {
                    // Rollback with the current timestamp.
                    // States taken at the time when the timestamp is created
                    // are restored to the actors managed by this checkpoint
                    // object.
                    checkpoint.rollback(checkpoint.getTimestamp(), true);
                    checkpoints.add(checkpoint);
                }
            }
        }
    }

    /** Record the current state of all the actors in this composite actor.
     */
    public void markState() {
        Set checkpoints = new HashSet();
        Iterator objectsIter = containedObjectsIterator();

        while (objectsIter.hasNext()) {
            Object object = objectsIter.next();

            if (object instanceof Rollbackable) {
                Rollbackable rollbackObject = (Rollbackable) object;
                Checkpoint checkpoint = rollbackObject.$GET$CHECKPOINT();

                if (!checkpoints.contains(checkpoint)) {
                    // FIXME: older states should be discarded.
                    checkpoint.createCheckpoint();
                    checkpoints.add(checkpoint);
                }
            }
        }
    }
}

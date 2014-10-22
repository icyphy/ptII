/* A wrapper for dynamic Pthales domain to interface to be used inside
 other domains like PN.

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

package ptolemy.domains.pthales.lib;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
A composite actor that wraps Pthales domain to interface to external domains like PN.
A PthalesCompositeActor can contain actors from different model (as SDF),
but the port must be a PthalesIOPort, because of the ArrayOL parameters.

@author Dai Bui
@see ptolemy.actor.TypedIOPort
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (daib)
@Pt.AcceptedRating Red (daib)
 */

public class PthalesWrapperCompositeActor extends TypedCompositeActor {

    /** Construct a PthalesWrapperCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     */

    public PthalesWrapperCompositeActor() {
        super();
        setClassName("ptolemy.domains.pthales.lib.PthalesWrapperCompositeActor");
    }

    /** Construct a PthalesWrapperCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */

    public PthalesWrapperCompositeActor(Workspace workspace) {
        super(workspace);
        setClassName("ptolemy.domains.pthales.lib.PthalesWrapperCompositeActor");
    }

    /** Construct a PthalesWrapperCompositeActor with a name and a container.
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

    public PthalesWrapperCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setClassName("ptolemy.domains.pthales.lib.PthalesWrapperCompositeActor");
    }

    /** Return a new receiver of flexible buffer size to interface with PN actors.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.  This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @exception IllegalActionException If there is no local director.
     *  @return A new object implementing the Receiver interface.
     */
    @Override
    public Receiver newInsideReceiver() throws IllegalActionException {
        return new SDFReceiver();
    }
}

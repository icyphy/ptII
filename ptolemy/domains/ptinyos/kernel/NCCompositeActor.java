/* Composite Actor in the NC domain.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// NCCompositeActor

/**

// FIXME
Composite actor in the PtinyOS domain. This class is derived from
CompositeActor.  NCCompositeActor is always a opaque composite
actor.

// FIXME is this class needed?

@author  Elaine Cheong
@version $Id$
@since Ptolemy II 0.2
@Pt.ProposedRating Red (celaine)
@Pt.AcceptedRating Red (celaine)
*/
public class NCCompositeActor extends CompositeActor {
    /** Construct a NCCompositeActor in the default workspace with no container
     *  and an empty string as its name. Add the actor to the workspace
     *  directory.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     */
    public NCCompositeActor() {
        super();

        // When exporting MoML, set the class name to NCCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ptinyos.kernel.NCCompositeActor");
    }

    /** Create an NCCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  The director should be set before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NCCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // When exporting MoML, set the class name to NCCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ptinyos.kernel.NCCompositeActor");

        // Create an inside director.
        PtinyOSDirector director = new PtinyOSDirector(this, "PtinyOSDirector");
        Location location = new Location(director, "_location");
        location.setExpression("[65.0, 35.0]");
    }

    /** Construct a NCCompositeActor in the specified workspace with no
     *  container
     *  and an empty string as a name. You can then change the name with
     *  setName(). If the workspace argument is null, then use the default
     *  workspace.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public NCCompositeActor(Workspace workspace) {
        super(workspace);

        // When exporting MoML, set the class name to NCCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ptinyos.kernel.NCCompositeActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* (non-Javadoc)
     * @see ptolemy.kernel.ComponentEntity#isOpaque()
     */
    /*
      public boolean isOpaque() {
      return true;
      }
    */
}

/* A composite actor in the PtinyOS domain.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

import ptolemy.actor.TypeOpaqueCompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// NCCompositeActor

/**
 A composite actor in the PtinyOS domain with no ports.  This actor is
 always a type opaque composite actor.

 <p> This actor represents a nesC component with no interfaces and no
 .nc implementation.  See {@link PtinyOSDirector} for more information
 on nesC.  This actor should be used when it is not necessary to have
 typed ports that interface to regular Ptolemy II models.  If typed
 ports are needed, then an actor that implements {@link
 ptolemy.domains.ptinyos.kernel.PtinyOSCompositeActor} should be used.

 <p> NCCompositeActor is used as a place holder for nesC (.nc) code
 that the user must write separately in an editor like {@link
 ptolemy.domains.ptinyos.gui.NCCompositeActorTableauFactory}.  The
 user should add ports in the GUI as necessary when adding nesC
 interfaces to their nesC component.  These ports should connect to
 other nesC components.

 <p>FIXME: Test creation of NCCompositeActor that is not at the
 toplevel.

 @author  Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (celaine)
 @Pt.AcceptedRating Green (celaine)
 */
public class NCCompositeActor extends TypeOpaqueCompositeActor {
    /** Construct a NCCompositeActor in the default workspace with no
     *  container and an empty string as its name. Add the actor to
     *  the workspace directory.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     */
    public NCCompositeActor() {
        super();
        _setClassName();
    }

    /** Construct a NCCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then
     *  change the name with setName(). If the workspace argument is
     *  null, then use the default workspace.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public NCCompositeActor(Workspace workspace) {
        super(workspace);
        _setClassName();
    }

    /** Create an NCCompositeActor with a name and a container.  The
     *  container argument must not be null, or a NullPointerException
     *  will be thrown.  This actor will use the workspace of the
     *  container for synchronization and version counts.  If the name
     *  argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no local director initially, and its
     *  executive director will be simply the director of the
     *  container.
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
        _setClassName();

        // Create an inside director.
        PtinyOSDirector director =
            new PtinyOSDirector(this, "PtinyOSDirector");
        Location location = new Location(director, "_location");
        location.setExpression("[65.0, 35.0]");

        // Set the port orientation so that input ports are on top
        // edge of the icon.
        rotatePorts = new Parameter(this, "_rotatePorts", new IntToken(90));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Relative orientation of ports on the icon of this actor.  The
     *  default value is an integer value of 90 (input ports are on
     *  the top edge of the icon).
     */
    public Parameter rotatePorts;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Constructor initialization.  When exporting MoML, set the
     *  class name to NCCompositeActor instead of the default
     *  TypedCompositeActor.
     */
    private void _setClassName() {
        setClassName("ptolemy.domains.ptinyos.kernel.NCCompositeActor");
    }
}

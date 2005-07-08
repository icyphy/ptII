/* FIXME comment

 Copyright (c) 1997-2005 The Regents of the University of California.
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
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// PtinyOSActor

/**
 * This composite actor is designed for use in the PtinyOS domain.
 *
 * FIXME comment
 *
 * @author Elaine Cheong
 * @version $Id$
 * @Pt.ProposedRating Red (celaine)
 * @Pt.AcceptedRating Red (celaine)
 */
public abstract class PtinyOSActor extends TypeOpaqueCompositeActor {
    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public PtinyOSActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public PtinyOSActor(Workspace workspace) {
        super(workspace);
    }

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PtinyOSActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        setClassName("ptolemy.domains.ptinyos.kernel.PtinyOSActor");

        // Create an inside director.
        PtinyOSDirector director = new PtinyOSDirector(this, "PtinyOSDirector");
        Location location = new Location(director, "_location");
        location.setExpression("[65.0, 35.0]");

        // packetOut and packetIn must be RecordToken
        packetOut = new TypedIOPort(this, "packetOut", false, true);
        packetOut.setTypeEquals(BaseType.STRING);

        packetIn = new TypedIOPort(this, "packetIn", true, false);
        packetOut.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort packetIn;

    public TypedIOPort packetOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME comment
     */
    public void fire() throws IllegalActionException {
        // Grab the packet before it gets thrown away, since it is not
        // connected to any actors on the inside.
        if (packetIn.getWidth() > 0) {
            if (packetIn.hasToken(0)) {
                StringToken token = (StringToken) packetIn.get(0);

                PtinyOSDirector director = (PtinyOSDirector) getDirector();

                if (director == null) {
                    throw new IllegalActionException(
                            "Could not find a local director!");
                }

                director.receivePacket(token.stringValue());
            }
        }

        super.fire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
}

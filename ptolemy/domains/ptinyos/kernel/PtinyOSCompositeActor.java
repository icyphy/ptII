/* A base class for a PtinyOS composite actor with typed packet ports.

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

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PtinyOSCompositeActor

/**
 * A base class for a PtinyOS composite actor with typed packet ports.
 * This actor is always a type opaque composite actor.
 *
 * <p> This actor should be used to interface to or be embedded in a
 * regular Ptolemy II model.
 *
 * <p>The local director of this actor must be a
 *  {@link ptolemy.domains.ptinyos.kernel.PtinyOSDirector}.
 *
 * @author Elaine Cheong
 * @version $Id$
 * @Pt.ProposedRating Green (celaine)
 * @Pt.AcceptedRating Green (celaine)
 * @since Ptolemy II 5.1
 */
public abstract class PtinyOSCompositeActor extends NCCompositeActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public PtinyOSCompositeActor() {
        super();
        _setClassName();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public PtinyOSCompositeActor(Workspace workspace) {
        super(workspace);
        _setClassName();
    }

    /** Construct an actor in the specified container with the
     *  specified name and in the super class, instantiate a
     *  PtinyOSDirector that is contained by this CompositeActor.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PtinyOSCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _setClassName();

        // Set the port orientation so the input ports are on the left
        // edge of the icon.
        rotatePorts.setToken(new IntToken(0));

        // Create an output port for packets.
        packetOut = new TypedIOPort(this, "packetOut", false, true);
        packetOut.setTypeEquals(BaseType.STRING);

        // Create an input port for packets.
        packetIn = new TypedIOPort(this, "packetIn", true, false);
        packetOut.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An input port of type String. */
    public TypedIOPort packetIn;

    /** An output port of type String. */
    public TypedIOPort packetOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the {@link #packetIn} port and pass the
     *  string value of the token to the PtinyOSDirector.  The local
     *  director of this actor must be a PtinyOSDirector.
     *  @see
     *  ptolemy.domains.ptinyos.kernel.PtinyOSDirector#receivePacket(String)
     *  @exception IllegalActionException If the fire() method in the
     *  super class throws it, or a local director cannot be found.
     */
    public void fire() throws IllegalActionException {
        // Grab the packet before it gets thrown away, since it is not
        // connected to any actors on the inside.
        if (packetIn.isOutsideConnected()) {
            if (packetIn.hasToken(0)) {
                StringToken token = (StringToken) packetIn.get(0);

                Director director = getDirector();
                if (director != null) {
                    if (director instanceof PtinyOSDirector) {
                        ((PtinyOSDirector)director).receivePacket(
                                token.stringValue());

                    } else {
                        throw new IllegalActionException(
                                "Local director was not of type "
                                + "PtinyOSDirector");
                    }
                } else {
                    throw new IllegalActionException(
                            "Could not find a local director!");
                }
            }
        }
        // Call super.fire() after passing the packet to TOSSIM, so
        // that TOSSIM can enqueue a packet event.  If super.fire() is
        // called before passing the packet to TOSSIM, ptII may have
        // already advanced its clock past the TOSSIM time, and the
        // packet will be enqueued in TOSSIM with a time that is in
        // the past.
        super.fire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Constructor initialization.  When exporting MoML, set the
     *  class name to PtinyOSCompositeActor instead of the default
     *  TypedCompositeActor.
     */
    private void _setClassName() {
        setClassName("ptolemy.domains.ptinyos.kernel.PtinyOSCompositeActor");
    }
}

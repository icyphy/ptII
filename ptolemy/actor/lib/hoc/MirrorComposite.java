/*  A composite that contain one actor and mirror the ports and parameters of that actor.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import ptolemy.actor.parameters.ParameterMirrorPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MirrorComposite

/**
 A composite that contains one actor and mirrors the ports and
 parameters of that actor. This is identical to the base class
 except that it requires its ports to be of type MirrorPort or
 ParameterMirrorPort. That is, every port of this composite is
 mirrored in the inside model, whereas the base class tolerates ports
 that are not mirrored (typically instances of TypedIOPort).

 @author Ilge Akkaya, Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class MirrorComposite extends ReflectComposite {

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MirrorComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a MirrorComposite in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public MirrorComposite(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Create an actor with a name and a container that optionally
     *  mirrors the ports that are instances of ParameterPort.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @param mirrorParameterPorts If false, then ports that are instances of
     *   ParameterPort are not mirrored.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MirrorComposite(CompositeEntity container, String name,
            boolean mirrorParameterPorts) throws IllegalActionException,
            NameDuplicationException {
        super(container, name, mirrorParameterPorts);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a port to this actor. This overrides the base class to
     *  throw an exception if the port is not an instance of MirrorPort
     *  or ParameterMirrorPort.
     *  @param port The TypedIOPort to add to this actor.
     *  @exception IllegalActionException If the port is not an instance
     *   of IteratePort, or the port has no name.
     *  @exception NameDuplicationException If the port name collides with a
     *   name already in the actor.
     */
    @Override
    protected void _addPort(Port port) throws IllegalActionException,
    NameDuplicationException {

        if (!(port instanceof MirrorPort || port instanceof ParameterMirrorPort)) {
            throw new IllegalActionException(this,
                    "MirrorComposite ports are required to be "
                            + "instances of MirrorPort");
        }

        super._addPort(port);
    }

    private void _init() {
        setClassName("ptolemy.actor.lib.hoc.MirrorComposite");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// MirrorCompositeContents

    /** This is a specialized composite actor for use in MirrorComposite.
     *  In particular, it ensures that if ports are added or deleted
     *  locally, then corresponding ports will be added or deleted
     *  in the container.  That addition will result in appropriate
     *  connections being made.
     */
    public static class MirrorCompositeContents extends
            ReflectCompositeContents {
        // NOTE: This has to be a static class so that MoML can
        // instantiate it.

        /** Construct an actor with a name and a container.
         *  @param container The container.
         *  @param name The name of this actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public MirrorCompositeContents(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Add a port to this actor. This overrides the base class to
         *  add a corresponding port to the container using a change
         *  request, if that port does not already exist.
         *  @param port The TypedIOPort to add to this actor.
         *  @exception IllegalActionException If the port is not an instance of
         *   MirrorPort, or the port has no name.
         *  @exception NameDuplicationException If the port name
         *  collides with a name already in the actor.
         */
        @Override
        protected void _addPort(final Port port) throws IllegalActionException,
        NameDuplicationException {

            if (!(port instanceof MirrorPort || port instanceof ParameterMirrorPort)) {
                throw new IllegalActionException(this,
                        "Ports in MirrorComposiMirrorCompositeContentsite must be MirrorPort.");
            }

            super._addPort(port);
        }
    }
}

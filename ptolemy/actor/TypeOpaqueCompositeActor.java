/* An composite actor whose ports have types but is not typed inside.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.HashSet;
import java.util.Set;

import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TypeOpaqueCompositeActor

/**
 * A composite actor whose ports have types, but the actors inside are
 * not required to be typed. This actor does not impose any type
 * constraints between its ports, so any output ports of this actor
 * have to have explicitly declared types. There is no mechanism for
 * inferring the types of the output ports. Note that the ports of
 * this actor cannot be linked on the inside to untyped relations, and
 * by default, any relation created on the inside will be untyped.
 *
 * <p> The intended use of this actor is for scenarios where the
 * inside structure is not part of the Ptolemy type system. For
 * example, the inside structure may define a component that will be
 * translated into executable code by a code generator. A subclass of
 * this actor would typically include the code generator an a
 * mechanism for executing the generating code.
 *
 * @author Elaine Cheong and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 4.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 * @see ptolemy.actor.TypedCompositeActor
 * @see ptolemy.actor.TypedIOPort
 */
public class TypeOpaqueCompositeActor extends CompositeActor implements
TypedActor {
    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public TypeOpaqueCompositeActor() {
        super();
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     */
    public TypeOpaqueCompositeActor(Workspace workspace) {
        super(workspace);
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public TypeOpaqueCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return false because backward type inference is not implemented
     *  for this actor.
     *  @return false
     */
    @Override
    public boolean isBackwardTypeInferenceEnabled() {
        return false;
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return the type constraints of this actor.
     *  This is always an empty list because the types of ports
     *  have to be explicitly declared.
     *  @return An empty list.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public Set<Inequality> typeConstraints() {
        return new HashSet<Inequality>();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to throw an exception if the added port
     *  is not an instance of TypedIOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this actor.
     *  Derived classes may override this method to further constrain the
     *  port to be a subclass of TypedIOPort. This method is <i>not</i>
     *  synchronized on the workspace, so the caller should be.
     *
     *  @param port The port to add to this actor.
     *  @exception IllegalActionException If the port is not an instance
     *   of TypedIOPort, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with
     *   the name of another port already in the actor.
     */
    @Override
    protected void _addPort(Port port) throws IllegalActionException,
    NameDuplicationException {
        // In the future, this method can be changed to allow IOPort to be
        // added. In that case, the type system just ignores instances of
        // IOPort during type checking. Since there is no intended application
        // for that change yet, constrain the port to be TypedIOPort for now.
        if (!(port instanceof TypedIOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this actor.");
        }

        super._addPort(port);
    }
}

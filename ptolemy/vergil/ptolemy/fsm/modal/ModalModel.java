/* Modal models.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.fsm.modal;

import java.util.Iterator;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.*;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;

//////////////////////////////////////////////////////////////////////////
//// ModalModel
/**
This is a typed composite actor designed to be a modal model.
Inside the modal model is a finite-state machine controller, and
inside each state in the FSM is a refinement model. Adding or
removing ports in this actor results in the same ports appearing
or disappearing in the FSM controller and in each of the refinements.
Similarly, adding or removing ports in the controller or in the
refinements results in this actor and the other refinements
reflecting the same change to the ports.  That is, this actor,
the controller, and the refinments all contain the same ports.
This class is designed to work closely with ModalController and
Refinement, since changes to ports can be initiated in this class
or in those.

@see ModalController
@see Refinement
@author Edward A. Lee
@version $Id$
*/
public class ModalModel extends TypedCompositeActor {

    /** Construct a modal model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ModalModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        getMoMLInfo().className = "ptolemy.vergil.ptolemy.fsm.modal.ModalModel";

        // This actor contains an FSMDirector and an FSMActor.
        // The names are preceded with underscore to minimize the
        // likelihood of a conflict with a user-desired name.
        // NOTE This director will be described in the exported MoML
        // file, so when that is encountered, it will match this director.
        FSMDirector director = new FSMDirector(this, "_Director");
        // NOTE This controller will be described in the exported MoML
        // file, so when that is encountered, it will match this.
        _controller = new ModalController(this, "_Controller");

        director.controllerName.setExpression("_Controller");

        // NOTE This library will be described in the exported MoML
        // file, so when that is encountered, it will match this.
        // Configure the controller so it has the appropriate library.
        LibraryAttribute attribute = new LibraryAttribute(
                _controller, "_library");
        CompositeEntity library = new CompositeEntity(new Workspace("Library"));
        library.setName("state library");
        attribute.setLibrary(library);
        State state = new State(library, "state");
        new Attribute(state, "_centerName");
        new HierarchicalStateControllerFactory(state, "_controllerFactory");

        // Import annotations file.
        // Do this as a MoML change request so we can easily read the library
        // spec from a file, rather than replicating it here.
        // NOTE: Because this library has no association with a director,
        // the change will always be executed immediately.
        // This should be OK, since the library is in its own workspace,
        // and modifying the library cannot possibly affect the executing
        // model.
        String moml = "<input source=\"ptolemy/configs/annotation.xml\"/>";
        MoMLChangeRequest request = new MoMLChangeRequest(
                this, library, moml);
        library.requestChange(request);

        // Putting this attribute in causes look-inside to be handled
        // by it.
        new ModalTableauFactory(this, "_tableauFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new port with the specified name in this entity, the
     *  controller, and all the refinements.  Link these ports so that
     *  if the new port is set to be an input, output, or multiport, then
     *  the change is mirrored in the other ports.  The new port will be
     *  an instance of ModalPort, which extends TypedIOPort.
     *  This method is write-synchronized on the workspace, and increments
     *  its version number.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    public Port newPort(String name)
            throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            Port port = new ModalPort(this, name);
            // Create mirror ports.
            Iterator entities = entityList().iterator();
            while (entities.hasNext()) {
                Entity entity = (Entity)entities.next();
                if (entity instanceof ModalController) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((ModalController)entity)._mirrorDisable = true;
                            entity.newPort(name);
                        } finally {
                            ((ModalController)entity)._mirrorDisable = false;
                        }
                    }
                } else if (entity instanceof Refinement) {
                    if (entity.getPort(name) == null) {
                        try {
                            ((Refinement)entity)._mirrorDisable = true;
                            entity.newPort(name);
                        } finally {
                            ((Refinement)entity)._mirrorDisable = false;
                        }
                    }
                }
            }
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "ModalModel.newPort: Internal error: " +
                    ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The FSM controller. */
    private FSMActor _controller;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// ModalTableauFactory

    /** A tableau factory that opens an editor on the contained controller
     *  rather than this composite actor.
     */
    public class ModalTableauFactory extends TableauFactory {

        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public ModalTableauFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a tableau for the specified effigy, which is assumed to
         *  be an effigy for an instance of ModalModel.  This class
         *  defers to the configuration containing the specified effigy
         *  to open a tableau for the embedded controller.
         *  @param effigy The model effigy.
         *  @return A tableau for the effigy, or null if one cannot be created.
         *  @exception Exception If the factory should be able to create a
         *   Tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {

            // Check to see whether the specified effigy contains an effigy
            // for the controller, and if not, create one.
            PtolemyEffigy controllerEffigy = null;
            Iterator effigies =
                    effigy.entityList(PtolemyEffigy.class).iterator();
            while (effigies.hasNext()) {
                PtolemyEffigy candidate = (PtolemyEffigy)effigies.next();

                // First see whether this effigy matches.
                if (candidate.getModel() == _controller) {
                    controllerEffigy = candidate;
                    break;
                }
            }
            if (controllerEffigy == null) {
                controllerEffigy = new PtolemyEffigy(
                        effigy, effigy.uniqueName("effigy"));
                controllerEffigy.setModel(_controller);
            }

            // Display all open tableaux.
            return controllerEffigy.showTableaux();
        }
    }
}

/* Ptera modal models.

@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptera.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.BreakCausalityInterface;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

/**
 This is a typed composite actor to be a Ptera modal model. A Ptera modal model
 has a Ptera (Ptolemy Event Relation Actor) model inside. The Ptera model has a
 number of events, and scheduling relations may exist between events, meaning
 that the processing of the starting event causes the ending event to occur
 after a certain amount of model time. A scheduling relation may also be guarded
 by a Boolean expression.
 <p>
 The controller of a Ptera modal model is a Ptera controller (instance of {@link
 PteraController}). The Ptera controller contains an {@link PteraDirector},
 which has an event queue to store scheduled events in time-stamp order. The
 Ptera modal model itself also has a Ptera director. The Ptera director in the
 controller maintains its own event queue, and schedules itself to be fired by
 putting the controller in the event queue of the Ptera modal model's director.
 <p>
 An event can have one or more refinements. A refinement can be an actor model,
 an FSM model, or a Ptera model. If a refinement is a Ptera model, then its
 controller reports its earliest event to the controller of this modal model,
 which fires it when the model time reaches the reported time in the future. If
 the Ptera refinement itself contains an event that has a refinement in Ptera,
 that controller reports to the controller one level above, which in turn
 reports to the modal model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PteraModalModel extends ModalModel {

    /** Construct a Ptera modal model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PteraModalModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a Ptera modal model in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PteraModalModel(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Return a causality interface for this actor.  This method returns the
     *  causality interface where no output port depends on the input ports.  It
     *  is an instance of {@link BreakCausalityInterface}.
     *
     *  FIXME: A causality interface special for Ptera should be returned
     *  instead.
     *
     *  @return A representation of the dependencies between input ports
     *   and output ports.
     */
    @Override
    public CausalityInterface getCausalityInterface() {
        return new BreakCausalityInterface(this,
                BooleanDependency.OPLUS_IDENTITY);
    }

    /** Create a Ptera controller within this Ptera modal model. A subclass may
     *  return a different controller to be used in this Ptera modal model.
     *
     *  @return A controller to be used in this modal model.
     *  @exception IllegalActionException If the Ptera modal model is
     *   incompatible with the controller.
     *  @exception NameDuplicationException If the name of the controller
     *   collides with a name already in the container.
     */
    protected PteraController _createController()
            throws IllegalActionException, NameDuplicationException {
        return new PteraController(this, "_Controller");
    }

    /** Initialize the Ptera modal model by creating the parameter directorClass
     *  and a Ptera controller inside.
     *
     *  @exception IllegalActionException If the Ptera modal model is
     *   incompatible with the controller.
     *  @exception NameDuplicationException If the name of the controller
     *   collides with a name already in the container.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        setClassName("ptolemy.domains.ptera.kernel.PteraModalModel");

        // Set the director before changing directorClass, because changing the
        // latter causes a ChangeRequest to be issued (in superclass'
        // attributeChanged(), which then causes the expanded node in the actor
        // library to be collapsed immediately.
        Director director = getDirector();
        if (director != null && director.getContainer() == this) {
            director.setContainer(null);
        }
        PteraDirector pteraDirector = new PteraDirector(this,
                uniqueName("_Director"));
        pteraDirector.controllerName.setExpression(_controller.getName());

        directorClass.removeAllChoices();
        directorClass
                .setExpression("ptolemy.domains.ptera.kernel.PteraDirector");
        directorClass.setVisibility(Settable.NONE);

        ComponentEntity controller = getEntity("_Controller");
        if (controller != null) {
            controller.setContainer(null);
        }
        _controller = _createController();
    }
}

/* A common superclass of events created for model transformation.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.modal.modal.RefinementExtender;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.domains.ptera.kernel.PteraController;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// GTEvent

/**
 A common superclass of events created for model transformation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTEvent extends Event {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public GTEvent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        refinementExtender = new RefinementExtender(this,
                uniqueName("refinementExtender"));
        refinementExtender.description
        .setExpression("Embedded Transformation Controller");
        refinementExtender.setPersistent(false);
        refinementExtender.moveToFirst();
        _setRefinementExtender();

        Parameter allowRefinement = new Parameter(this, "_allowRefinement");
        allowRefinement.setTypeEquals(BaseType.BOOLEAN);
        allowRefinement.setToken(BooleanToken.FALSE);
        allowRefinement.setVisibility(Settable.EXPERT);
    }

    /** Get the model parameter that stores the current model to be transformed.
     *
     *  @return The model parameter.
     *  @exception IllegalActionException If the model parameter cannot be found
     *   in the model hierarchy.
     */
    public ModelParameter getModelParameter() throws IllegalActionException {
        NamedObj container = getContainer();
        if (!(container instanceof PteraController)) {
            return null;
        }

        PteraController controller = (PteraController) container;
        ModelParameter actorParameter = null;
        while (actorParameter == null && controller != null) {
            actorParameter = (ModelParameter) controller.getAttribute("Model",
                    ModelParameter.class);
            if (actorParameter == null) {
                Event event = (Event) controller.getRefinedState();
                if (event != null) {
                    controller = (PteraController) event.getContainer();
                }
            }
        }
        if (actorParameter == null) {
            throw new IllegalActionException("Unable to find the Model "
                    + "parameter in the Ptera controller of type "
                    + "ModelParameter.");
        }
        return actorParameter;
    }

    /** Specify the container, adding the entity to the list
     *  of entities in the container.  If the container already contains
     *  an entity with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this entity, throw an exception.  If this entity is
     *  a class element and the proposed container does not match
     *  the current container, then also throw an exception.
     *  If the entity is already contained by the container, do nothing.
     *  If this entity already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the directory of the workspace, if it is present.
     *  If the argument is null, then unlink the ports of the entity
     *  from any relations and remove it from its container.
     *  It is not added to the workspace directory, so this could result in
     *  this entity being garbage collected.
     *  Derived classes may further constrain the container
     *  to subclasses of CompositeEntity by overriding the protected
     *  method _checkContainer(). This method validates all
     *  deeply contained instances of Settable, since they may no longer
     *  be valid in the new context.  This method is write-synchronized
     *  to the workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        _setRefinementExtender();
    }

    /** The refinement extender to suggest the type of refinement for this
     *  event.
     */
    public RefinementExtender refinementExtender;

    /** Set the refinement extender to suggest either an embedded transformation
     *  controller or an embedded transformation controller with ports as the
     *  new refinement.
     */
    private void _setRefinementExtender() {
        NamedObj container = getContainer();
        if (refinementExtender != null) {
            if (container instanceof PteraController) {
                PteraController controller = (PteraController) container;
                if (controller.getPort("modelInput") != null
                        && controller.getPort("modelOutput") != null) {
                    refinementExtender.className.setExpression("ptolemy."
                            + "actor.gt.controller."
                            + "EmbeddedTransformationControllerWithPorts");
                    return;
                }
            }
            refinementExtender.className.setExpression("ptolemy.actor.gt."
                    + "controller.EmbeddedTransformationController");
        }
    }
}

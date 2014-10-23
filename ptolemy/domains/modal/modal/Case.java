/* Actor representing one of several refinements.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.modal;

import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Case

/**
 An actor that executes one of several refinements depending on the
 value provided by the <i>control</i> port-parameter. To use this,
 look inside, add refinement cases, and populate them with computations.
 Each refinement is a composite that is required to have its own director.
 The name of the refinement is value that the control must have to
 execute this refinement.
 This actor always provides one case called "default". This is
 the refinement that is executed if no other refinement matches
 the control input.  All refinements have the same ports,
 and adding ports to any one refinement or to the case actor
 itself results in identical ports being added to all refinements.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 @deprecated Use ptolemy.actor.lib.hoc.Case instead.
 */
@Deprecated
public class Case extends ModalModel {

    /** Construct
     * a modal model with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Case(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port-parameter on which the control token is provided.
     *  This can have any type, and is initialized with a default value
     *  of true.
     */
    public PortParameter control;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to ensure that the _default member
     *  points to the default refinement.
     *  @param workspace The workspace for the new object.
     *  @return A new Case.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Case newObject = (Case) super.clone(workspace);
        newObject._default = (Refinement) newObject.getEntity("default");
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to create the transition associated
     *  with this refinement if the argument is an instance of Refinement.
     *  @param entity Entity to contain.
     *  @exception IllegalActionException If the entity has no name, or the
     *   action would result in a recursive containment structure.
     *  @exception NameDuplicationException If the name collides with a name
     *  already in the entity.
     */
    @Override
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        super._addEntity(entity);
        if (entity instanceof Refinement) {
            String controlValue = entity.getName();
            // Create a self-loop transition if one does not exist.
            // FIXME: This assumes it is right!  Can it be wrong?
            // Need to ensure that these relations are deleted when a
            // refinement is deleted.
            if (_controller.getRelation(controlValue) == null) {
                Transition transition = (Transition) _controller
                        .newRelation(controlValue);
                transition.guardExpression.setExpression("control == "
                        + controlValue);
                transition.refinementName.setExpression(controlValue);
                transition.preemptive.setToken(BooleanToken.TRUE);
                if (controlValue.equals("default")) {
                    transition.defaultTransition.setToken(BooleanToken.TRUE);
                }
                // Create a self loop connection.
                _state.incomingPort.link(transition);
                _state.outgoingPort.link(transition);
            }
            // Ensure that the default refinement remains the last one.
            // Note however that this is called on the default itself,
            // at which time the local member has not been set.
            if (_default != null) {
                _default.moveToLast();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The one and only state. */
    protected State _state;

    /** The default refinement. */
    protected Refinement _default;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the model with a single state.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        if (!_printedDeprecatedMessage) {
            _printedDeprecatedMessage = true;
            System.out.println("Warning: " + getFullName()
                    + ": modal.modal.Case has been deprecated"
                    + " since March, 2006.  Use actor.lib.hoc.Case instead.");
        }
        // Create the one and only state in the controller.
        _state = new State(_controller, "State");
        _controller.initialStateName.setExpression("State");
        // FIXME: The following doesn't give much info.
        // _controller.addDebugListener(this);
        // Make the controller transient (it is reconstructed
        // at construction time).

        // Hide the directorClass parameter.
        directorClass.setVisibility(Settable.EXPERT);

        // Create the control port.
        control = new PortParameter(this, "control");
        // FIXME: This is awkward... If I provide some
        // non-boolean control input, I get obscure type
        // conflict error messages and have to change this
        // to match.
        control.setExpression("true");
        ParameterPort port = control.getPort();
        // Put the control input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(port, "_cardinal");
        controlCardinal.setExpression("SOUTH");

        // Create the default refinement.
        // NOTE: We do not use a TransitionRefinement because we don't
        // want the sibling input ports that come with output ports.
        _default = new Refinement(this, "default");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** True if we have printed the deprecated message. */
    private static boolean _printedDeprecatedMessage = false;
}

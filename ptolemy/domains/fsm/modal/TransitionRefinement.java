/* TransitionRefinement for modal models.

 Copyright (c) 1998-2003 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.modal;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TransitionRefinement
/**

Transition refinements provide a way to use composite actors which are run
whenever a transition is taken in an FSM modal, much in the same way
that State refinements are run every time an FSM is in a particular state.
This provides an alternative mechanism for performing calculations during
a Transition other than expressions in the Set Actions and Output Actions
of that Transition. The expression actions are still executed but only
after the Transition refinement has been fired.<p>

This typed composite actor supports mirroring of its ports in its container
(which is required to be a ModalModel), which in turn assures
mirroring of ports in each of the refinements and the controller.
TransitionRefinement fulfills the CTStepSizeControlActor interface so that
it can be used to construct hybrid systems using the CT domain.<p>

@author David Hermann, Research In Motion Limited
@version $Id$
@since Ptolemy II 2.1
*/
public class TransitionRefinement extends Refinement {

    /** Construct a modal controller with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public TransitionRefinement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.domains.fsm.modal.TransitionRefinement");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new port with the specified name in the container of
     *  this refinement, which in turn creates a port in this refinement
     *  all other refinements, and the controller.
     *  This method is write-synchronized on the workspace.
     *  @param name The name to assign to the newly created port.
     *  @return The new port.
     *  @exception NameDuplicationException If the entity already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            if (_mirrorDisable || getContainer() == null) {
                // Have already called newPort() in the container.
                // This time, process the request.
                TransitionRefinementPort port =
                    new TransitionRefinementPort(this, name);

                // NOTE: This is a total kludge, but when a port is created
                // this way, rather than by parsing MoML that specifies the
                // class, we assume that it is being created interactively,
                // rather than by reading a stored MoML file, so we enable
                // mirroring in the port.
                port._mirrorDisable = false;

                // Create the appropriate links.
                ModalModel container = (ModalModel)getContainer();
                if (container != null) {
                    String relationName = name + "Relation";
                    Relation relation = container.getRelation(relationName);
                    if (relation == null) {
                        relation = container.newRelation(relationName);
                        Port containerPort = container.getPort(name);
                        containerPort.link(relation);
                    }
                    port.link(relation);
                }
                return port;
            } else {
                _mirrorDisable = true;
                ((ModalModel)getContainer()).newPort(name);
                return getPort(name);
            }
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TransitionRefinement.newPort: Internal error: " +
                    ex.getMessage());
        } finally {
            _mirrorDisable = false;
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
}

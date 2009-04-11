/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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
import ptolemy.domains.fsm.modal.RefinementExtender;
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


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTEvent extends Event {

    public GTEvent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        refinementExtender = new RefinementExtender(this, "refinementExtender");
        refinementExtender.description.setExpression(
                "Embedded Transformation Controller");
        refinementExtender.setPersistent(false);
        _setRefinementExtender();

        refinementSuggestion = new RefinementSuggestion(this,
                "refinementSuggestion");

        Parameter allowRefinement = new Parameter(this, "_allowRefinement");
        allowRefinement.setTypeEquals(BaseType.BOOLEAN);
        allowRefinement.setToken(BooleanToken.FALSE);
        allowRefinement.setVisibility(Settable.EXPERT);
    }

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
            throw new IllegalActionException("Unable to find the Model " +
                    "parameter in the Ptera controller of type " +
                    "ModelParameter.");
        }
        return actorParameter;
    }

    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        _setRefinementExtender();
    }

    public RefinementExtender refinementExtender;

    public RefinementSuggestion refinementSuggestion;

    private void _setRefinementExtender() {
        NamedObj container = getContainer();
        if (refinementExtender != null) {
            if (container instanceof PteraController) {
                PteraController controller = (PteraController) container;
                if (controller.getPort("modelInput") != null &&
                        controller.getPort("modelOutput") != null) {
                    refinementExtender.className.setExpression("ptolemy." +
                            "actor.gt.controller." +
                            "EmbeddedTransformationControllerWithPorts");
                    return;
                }
            }
            refinementExtender.className.setExpression("ptolemy.actor.gt." +
                    "controller.EmbeddedTransformationController");
        }
    }
}

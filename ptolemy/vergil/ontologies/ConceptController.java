/* The node controller for ontology concept model elements.
 *
 * Copyright (c) 1998-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */
package ptolemy.vergil.ontologies;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

/** The node controller for ontology concept model elements.
 *  This class provides interaction with nodes that represent concepts in an
 *  ontology model. It extends the AttributeController which provides a double
 *  click binding to edit the parameters of the concept, and a context menu
 *  containing commands to edit parameters ("Configure"), rename, and get
 *  documentation. In addition, it binds the action to edit the acceptability
 *  flag for a concept.
 *
 *  @author Charles Shelton, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConceptController extends AttributeInOntologyController {

    /** Create a concept controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public ConceptController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a concept controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public ConceptController(GraphController controller, Access access) {
        super(controller, access);

        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                _toggleAcceptabilityAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot keys to the actions in the given JGraph. It would be better that
     *  this method was added higher in the hierarchy. Now most controllers
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _toggleAcceptabilityAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the class label of the component which is a Concept.
     *  @return The string "Concept".
     */
    @Override
    protected String _getComponentType() {
        return "Concept";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The toggle acceptability context menu action. */
    protected ToggleAcceptabilityAction _toggleAcceptabilityAction = new ToggleAcceptabilityAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private inner classes             ////

    ///////////////////////////////////////////////////////////////////
    //// ToggleAcceptabilityAction

    /** An action to toggle the isAcceptable attribute of the concept.
     *  NOTE: This requires that the configuration be non null, or it will
     *   report an error with a fairly cryptic message.
     */
    @SuppressWarnings("serial")
    private class ToggleAcceptabilityAction extends FigureAction {

        /** Create a new ToggleAcceptabilityAction object.
         */
        public ToggleAcceptabilityAction() {
            super("Toggle Acceptability");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_A, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Execute the Toggle Acceptability action on the ontology concept.
         *  @param e The ActionEvent that is received to execute the
         *   toggle acceptability action.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            NamedObj target = getTarget();

            // If the target is not an instance of LatticeElement, do nothing.
            if (target instanceof Concept) {
                Parameter isAcceptableParameter = ((Concept) target).isAcceptable;

                try {
                    // Use a MoML change request here so that if the action
                    // is executed via hotkey, the model graph is automatically
                    // repainted to reflect the change in the isAcceptable
                    // parameter on the Concept's icon.
                    String moml = _getToggleAcceptabilityMoML(isAcceptableParameter);
                    MoMLChangeRequest toggleAcceptabilityRequest = new MoMLChangeRequest(
                            ConceptController.this, target, moml);
                    toggleAcceptabilityRequest.setUndoable(true);
                    target.requestChange(toggleAcceptabilityRequest);

                } catch (IllegalActionException ex) {
                    MessageHandler.error("Toggle acceptability failed: ", ex);
                }
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Return the MoML string that will change the boolean value of the
         *  isAcceptable parameter in a Concept to its opposite value (false
         *  to true, and true to false).
         *  @param isAcceptableParameter The isAcceptable parameter.
         *  @return The MoML string that will execute the change.
         *  @exception IllegalActionException Thrown if there is a problem getting
         *   the current value of the isAcceptable parameter.
         */
        private String _getToggleAcceptabilityMoML(
                Parameter isAcceptableParameter) throws IllegalActionException {
            BooleanToken value = (BooleanToken) isAcceptableParameter
                    .getToken();
            value = value.not();

            return "<property name=\"" + isAcceptableParameter.getName()
                    + "\" value = \"" + value.toString() + "\" />";
        }
    }
}

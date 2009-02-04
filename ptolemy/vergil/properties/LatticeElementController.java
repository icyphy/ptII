/* The node controller for lattice elements.

 Copyright (c) 1998-2006 The Regents of the University of California.
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
package ptolemy.vergil.properties;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.properties.LatticeElement;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.fsm.StateController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.gui.GUIUtilities;

public class LatticeElementController extends StateController {

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public LatticeElementController(GraphController controller) {
        super(controller);
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_toggleAcceptabilityAction));
    }

    public LatticeElementController(GraphController controller, 
            Access access) {
        super(controller, access);
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_toggleAcceptabilityAction));
    }

    /** The edit custom icon action. */
    protected toggleAcceptabilityAction _toggleAcceptabilityAction = 
        new toggleAcceptabilityAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to look inside a state at its refinement, if it has one.
     *  NOTE: This requires that the configuration be non null, or it
     *  will report an error with a fairly cryptic message.
     */
    protected class toggleAcceptabilityAction extends FigureAction {
        public toggleAcceptabilityAction() {
            super("Toggle Acceptability");

            // FIXME: the hot key doesn't work.
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_A, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {

            super.actionPerformed(e);

            NamedObj target = getTarget();

            // If the target is not an instance of LatticeElement, do nothing.
            if (target instanceof LatticeElement) {
                Parameter parameter = ((LatticeElement) target).isAcceptableSolution;
                
                try {
                    BooleanToken value = (BooleanToken) parameter.getToken();
                    parameter.setToken(value.not());
                    target.attributeChanged(parameter);
                    
                    // FIXME: how do we force a repaint immediately?
                    
                } catch (IllegalActionException ex) {
                    MessageHandler.error("Toggle acceptability failed: ", ex);
                }
                
            }
        }
    }

}

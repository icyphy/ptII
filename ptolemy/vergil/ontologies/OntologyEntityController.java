/*  The node controller for an ontology in an ontology solver model.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.vergil.ontologies;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.data.ontologies.Ontology;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// OntologyEntityController

/** The node controller for an ontology in an ontology solver model. This
 *  class duplicates code from {@link ptolemy.vergil.actor.ActorController
 *  ActorController} to provide the Look Inside and Open Instance actions in
 *  the context menu.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 2.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class OntologyEntityController extends AttributeInOntologyController {
    
    /** Create an ontology controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public OntologyEntityController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an ontology controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public OntologyEntityController(GraphController controller, Access access) {
        super(controller, access);

        // NOTE: The following requires that the configuration be
        // non-null, or it will report an error.  However, in order to
        // get the "Look Inside" menu to work for composite actors in
        // Kepler, we create these menu items now.
        _menuFactory
                .addMenuItemFactory(new MenuActionFactory(_lookInsideAction));
        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                _openInstanceAction));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot keys to the actions in the given JGraph.
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the class label of the component which is an Ontology.
     *  @return The string "Ontology".
     */
    protected String _getComponentType() {
        return "Ontology";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The action that handles opening an ontology model. */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    /** The action that handles opening an instance of an ontology class. */
    protected OpenInstanceAction _openInstanceAction = new OpenInstanceAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// LookInsideAction
    
    /** An action to open an ontology model. */
    private class LookInsideAction extends FigureAction {
        
        /** Create a new LookInsideAction object. */
        public LookInsideAction() {
            super("Open Ontology Class");

            // Attach a key binding for look inside.
            // If we are in an applet, so Control-L or Command-L will
            // be caught by the browser as "Open Location", so we don't
            // supply Control-L or Command-L as a shortcut under applets.
            if (!StringUtilities.inApplet()) {
                putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                                .getMenuShortcutKeyMask()));
            }
        }

        /** React to the action event received by the user interface.
         *  @param event The event received to execute the action.
         */
        public void actionPerformed(ActionEvent event) {
            if (_configuration == null) {
                MessageHandler.error("Cannot open an actor "
                        + "without a configuration.");
                return;
            }

            // Determine which entity was selected for the open actor action.
            super.actionPerformed(event);
            Ontology ontologyModel = (Ontology) getTarget();            
            try {
                _configuration.openModel(ontologyModel);
            } catch (Exception ex) {
                MessageHandler.error("Open actor failed.", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// OpenInstanceAction

    /** An action to open an instance. This is similar to LookInsideAction except
     *  that it does not open the class definition, but rather opens the
     *  instance.
     */
    private class OpenInstanceAction extends FigureAction {
        
        /** Create a new OpenInstanceAction object. */
        public OpenInstanceAction() {
            super("Open Ontology Instance");
        }

        /** React to the action event received by the user interface.
         *  @param event The event received to execute the action.
         */
        public void actionPerformed(ActionEvent event) {
            if (_configuration == null) {
                MessageHandler.error("Cannot open an instance "
                        + "without a configuration.");
                return;
            }

            // Determine which entity was selected for the open actor action.
            super.actionPerformed(event);
            Ontology ontologyModel = (Ontology) getTarget();            
            try {
                _configuration.openInstance(ontologyModel);
            } catch (Exception ex) {
                MessageHandler.error("Open instance failed.", ex);
            }
        }
    }
}

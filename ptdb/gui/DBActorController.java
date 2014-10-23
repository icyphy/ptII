/* Controller for database reference actors.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptdb.gui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.load.LoadManager;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.StringConstantParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// DBActorController

/**
 * A controller for database reference actors.  They are rendered with a green
 * box.  Additionally, when the user right clicks on the composite actor,
 * a menu item is added to the context menu that allows opening the referenced
 * model for editing.  Changes made with the actor opened from the database can
 * be propagated to all other parents that include it by reference.  If
 * modifications are only made from the instance that is opened, the changes
 * are not saved.  This preserves data integrity.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */
public class DBActorController extends ActorController {

    /** Create a controller for database reference actors.
     * @param controller The graph controller.
     */
    public DBActorController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a controller for database reference actors.
     * @param controller The graph controller.
     * @param access The access level for the controller.
     */
    public DBActorController(GraphController controller, Access access) {
        super(controller, access);

        if (access == FULL) {
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    _openActorFromDB));
        }
    }

    /** Add hot keys to the actions in the given JGraph.
     * The hot key to open the referenced model from the database is Ctrl-D.
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _openActorFromDB);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Draw the node at its location. This overrides the base class
     *  to highlight the actor to indicate that it is a DB reference actor.
     */
    @Override
    protected Figure _renderNode(Object node) {
        Figure nf = super._renderNode(node);

        if (nf instanceof CompositeFigure) {
            CompositeFigure cf = (CompositeFigure) nf;
            Figure backgroundFigure = cf.getBackgroundFigure();

            // This might be null because the node is hidden.
            if (backgroundFigure != null) {
                BasicFigure bf = new BasicFigure(backgroundFigure.getBounds(),
                        4.0f);
                bf.setStrokePaint(_HIGHLIGHT_COLOR);
                // Put the highlighting in the background,
                // behind the actor label.
                int index = cf.getFigureCount();
                if (index < 0) {
                    index = 0;
                }
                cf.add(index, bf);
            }
            return cf;
        }
        return nf;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The action for selection of the "Open Actor From Database" menu item.
     */
    protected OpenActorFromDB _openActorFromDB = new OpenActorFromDB();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The color of the box rendered around the DB reference actor.
     */
    private static Color _HIGHLIGHT_COLOR = Color.green;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// OpenActorFromDB

    /**
     *  An action to open a model from the database.
     */
    @SuppressWarnings("serial")
    private class OpenActorFromDB extends FigureAction {
        public OpenActorFromDB() {
            super("Open Actor From Database");

            if (!StringUtilities.inApplet()) {
                putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_D, Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error("Cannot open an actor "
                        + "without a configuration.");
                return;
            }

            // Determine which entity was selected for the open actor action.
            super.actionPerformed(e);

            NamedObj object = getTarget();

            //FIXME: DB Reference Parameters use StringParameter class.
            //  Consider changing to a more generic Parameter.
            if (object.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {
                if (object.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) instanceof StringConstantParameter
                        && ((StringParameter) object
                                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                                .getExpression().equalsIgnoreCase("TRUE")) {
                    try {
                        PtolemyEffigy effigy = LoadManager
                                .loadModelUsingId(
                                        ((StringParameter) object
                                                .getAttribute(XMLDBModel.DB_MODEL_ID_ATTR))
                                                .getExpression(),
                                                _configuration);

                        if (effigy != null) {
                            effigy.showTableaux();
                        } else {
                            MessageHandler.error("The specified model could "
                                    + "not be found in the database.");

                        }
                    } catch (Exception e1) {
                        MessageHandler.error(
                                "Cannot load the specified model. ", e1);
                    }
                }
            }
        }
    }
}

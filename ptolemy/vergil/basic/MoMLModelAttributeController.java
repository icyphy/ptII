/* A controller that provides binding of an attribute and a refinement model.
 * 
 * Copyright (c) 2009 The Regents of the University of California. All
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

package ptolemy.vergil.basic;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLModelAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

/**
 * A controller that provides binding of an attribute and a refinement model.
 * 
 * @author Dai Bui
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class MoMLModelAttributeController extends AttributeController {

    /**
     * Create a model attribute controller associated with the specified graph
     * controller.
     * @param controller The specified graph controller.
     */
    public MoMLModelAttributeController(GraphController controller) {
        this(controller, FULL);
    }

    /**
     * Create a model attribute controller associated with the specified graph
     * controller.
     * @param controller The associated graph controller.
     * @param access The access level.
     */
    public MoMLModelAttributeController(GraphController controller,
            Access access) {
        super(controller, access);

        _menuFactory
                .addMenuItemFactory(new MenuActionFactory(_lookInsideAction));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add hot keys to the look inside action in the given JGraph. It would be
     * better that this method was added higher in the hierarchy.
     * @param jgraph The JGraph to which hot keys are to be added.
     */
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The action that handles opening an actor. This is accessed by by
     * ActorViewerController to create a hot key for the editor. The name
     * "lookInside" is historical and preserved to keep backward compatibility
     * with subclasses.
     */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    //// LookInsideAction
    /**
     * An action to open a composite. This private class must remain named
     * LookInsideAction for backward compatibility.
     */
    protected class LookInsideAction extends FigureAction {

        public LookInsideAction() {
            super("Open Model");

            // If we are in an applet, so Control-L or Command-L will
            // be caught by the browser as "Open Location", so we don't
            // supply Control-L or Command-L as a shortcut under applets.
            if (!StringUtilities.inApplet()) {
                putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_J, Toolkit.getDefaultToolkit()
                                .getMenuShortcutKeyMask()));
                putValue(GUIUtilities.MNEMONIC_KEY, Integer
                        .valueOf(KeyEvent.VK_J));
            }
        }

        public void actionPerformed(ActionEvent event) {
            super.actionPerformed(event);

            MoMLModelAttribute attribute = (MoMLModelAttribute) getTarget();
            TableauFrame frame = (TableauFrame) getFrame();
            Configuration configuration = frame.getConfiguration();
            if (configuration == null) {
                MessageHandler.error("Cannot open a model "
                        + "without a configuration.");
                return;
            }
            try {
                NamedObj model = attribute.getContainedModel();
                configuration.openInstance(model);
            } catch (Exception e) {
                throw new InternalErrorException(null, e, "Unable to create "
                        + "transformation editor for " + attribute.getName());
            }
        }
    }
}

/* The node controller for class definitions.

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
package ptolemy.vergil.actor;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.OffsetMoMLChangeRequest;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ClassDefinitionController

/**
 This class provides interaction with nodes that represent Ptolemy II
 classes.  This extends the base class by providing mechanisms in the
 context menu for creating an instance, creating a subclass,
 and converting to an instance.
 <p>
 NOTE: There should be only one instance of this class associated with
 a given GraphController. This is because this controller listens for
 changes to the graph and re-renders the ports of any actor instance
 in the graph when the graph changes. If there is more than one instance,
 this rendering will be done twice, which can result in bugs like port
 labels appearing twice.

 @author Edward A. Lee and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ClassDefinitionController extends ActorController {
    /** Create an actor instance controller associated with the
     *  specified graph controller with full access.
     *  @param controller The associated graph controller.
     */
    public ClassDefinitionController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a controller associated with the specified graph
     *  controller with the specified access.
     *  @param controller The associated graph controller.
     *  @param access The access level, one of FULL or PARTIAL.
     */
    public ClassDefinitionController(GraphController controller, Access access) {
        super(controller, access);

        if (access == FULL) {
            // Use a submenu.
            Action[] actions = { _createInstanceAction, _createSubclassAction,
                    _convertToInstanceAction };
            _menuFactory.addMenuItemFactory(new MenuActionFactory(actions,
                    "Class Actions"));
        }
    }

    /** Add hot keys to the actions in the given JGraph.
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        // _convertToInstanceAction does not have a hot key.
        // GUIUtilities.addHotKey(jgraph, _convertToInstanceAction);
        GUIUtilities.addHotKey(jgraph, _createInstanceAction);
        GUIUtilities.addHotKey(jgraph, _createSubclassAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Draw the node at its location. This overrides the base class
     *  to highlight the actor to indicate that it is a class definition.
     */
    @Override
    protected Figure _renderNode(Object node) {
        Figure nf = super._renderNode(node);

        if (nf instanceof CompositeFigure) {
            // This cast should be safe...
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

    /** The action that handles converting a class to an instance.
     */
    protected ConvertToInstanceAction _convertToInstanceAction = new ConvertToInstanceAction(
            "Convert to Instance");

    /** The action that handles creating an instance from a class.
     */
    protected CreateInstanceAction _createInstanceAction = new CreateInstanceAction(
            "Create Instance");

    /** The action that handles creating a subclass from a class.
     */
    protected CreateSubclassAction _createSubclassAction = new CreateSubclassAction(
            "Create Subclass");

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create a change request to create an instance or a subclass
     *  of the object. Do nothing if the specified object does not
     *  implement Instantiable.
     *  @see Instantiable
     *  @param object The class to subclass or instantiate.
     *  @param subclass True to create a subclass, false to create
     *   an instance.
     */
    private void _createChangeRequest(NamedObj object, boolean subclass) {
        if (!(object instanceof Instantiable)) {
            return;
        }
        NamedObj container = object.getContainer();
        StringBuffer moml = new StringBuffer();
        moml.append("<group name=\"auto\">");

        // FIXME: Can we adjust the location here?
        // NOTE: This controller is expected to be used
        // only for class definitions, which must be instances
        // of InstantiableNamedObj, so this cast should be safe.
        // However, the key bindings are active even if it's not
        // a class, so if it's not a class, we just do nothing here.
        if (((Instantiable) object).isClassDefinition()) {
            if (subclass) {
                moml.append("<class name=\"" + "SubclassOf" + object.getName()
                        + "\" extends=\"" + object.getName() + "\"/>");
            } else {
                moml.append("<entity name=\"" + "InstanceOf" + object.getName()
                        + "\" class=\"" + object.getName() + "\"/>");
            }

            moml.append("</group>");

            MoMLChangeRequest request = new OffsetMoMLChangeRequest(this,
                    container, moml.toString());
            container.requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A fourth argument would make this highlight translucent, which enables
     * combination with other highlights. However, this forces printing
     * to PDF to rasterize the image, which results in far lower quality.
     */
    private static Color _HIGHLIGHT_COLOR = new Color(150, 150, 255);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// ConvertToInstanceAction
    // An action to convert a class to an instance.
    @SuppressWarnings("serial")
    private class ConvertToInstanceAction extends FigureAction {
        public ConvertToInstanceAction(String commandName) {
            super(commandName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // If access is not full, do nothing.
            if (_access != FULL) {
                return;
            }

            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);

            // NOTE: This cast should be safe because this controller is
            // used for actors.
            InstantiableNamedObj object = (InstantiableNamedObj) getTarget();
            NamedObj container = object.getContainer();

            // Assumes MoML parser will convert to instance.
            if (!object.isClassDefinition()) {
                // Object is already an instance. Do nothing.
                return;
            }

            // If the class has objects that defer to it, then
            // refuse to convert.
            boolean hasDeferrals = false;
            List deferred = object.getChildren();
            StringBuffer names = new StringBuffer();

            if (deferred != null) {
                // List contains weak references, so it's not
                // sufficient to just check the length.
                Iterator deferrers = deferred.iterator();

                while (deferrers.hasNext()) {
                    WeakReference deferrer = (WeakReference) deferrers.next();
                    NamedObj deferrerObject = (NamedObj) deferrer.get();

                    if (deferrerObject != null) {
                        hasDeferrals = true;

                        if (names.length() > 0) {
                            names.append(", ");
                        }

                        names.append(deferrerObject.getFullName());
                    }
                }
            }

            if (hasDeferrals) {
                MessageHandler.error("Cannot convert to instance because "
                        + "there are instances and/or subclasses:\n"
                        + names.toString());
                return;
            }

            String moml = "<entity name=\"" + object.getName() + "\"/>";
            MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                    moml);
            container.requestChange(request);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// CreateInstanceAction
    // An action to instantiate a class.
    @SuppressWarnings("serial")
    private class CreateInstanceAction extends FigureAction {
        public CreateInstanceAction(String commandName) {
            super(commandName);
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_I, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // If access is not full, do nothing.
            if (_access != FULL) {
                return;
            }

            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);

            NamedObj object = getTarget();
            _createChangeRequest(object, false);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// CreateSubclassAction
    // An action to subclass a class.
    @SuppressWarnings("serial")
    private class CreateSubclassAction extends FigureAction {
        public CreateSubclassAction(String commandName) {
            super(commandName);
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_U, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // If access is not full, do nothing.
            if (_access != FULL) {
                return;
            }

            // Determine which entity was selected for the
            // create subclass action.
            super.actionPerformed(e);

            NamedObj object = getTarget();
            _createChangeRequest(object, true);
        }
    }
}

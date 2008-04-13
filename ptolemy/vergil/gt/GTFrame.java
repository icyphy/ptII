/*

@Copyright (c) 2007-2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.vergil.gt;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import ptolemy.actor.gt.GTEntity;
import ptolemy.actor.gt.GTIngredientsAttribute;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.PortMatcher;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorInstanceController;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JCanvasPanner;
import diva.gui.toolbox.JContextMenu;

public class GTFrame extends ExtendedGraphFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one) or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public GTFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  This constructor results in a graph frame that obtains its library
     *  either from the model (if it has one), or the <i>defaultLibrary</i>
     *  argument (if it is non-null), or the default library defined
     *  in the configuration.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     *  @param defaultLibrary An attribute specifying the default library
     *   to use if the model does not have a library.
     */
    public GTFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    public GTFrameController getFrameController() {
        return _frameController;
    }

    /** Return the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @return the JGraph.
     *  @see #setJGraph(JGraph)
     */
    public JGraph getJGraph() {
        JGraph graph = _frameController.getJGraph();
        if (graph == null) {
            graph = super.getJGraph();
        }
        return graph;
    }

    protected boolean _close() {
        boolean result = super._close();
        if (result) {
            _frameController._removeListeners();
        }
        return result;
    }

    protected GraphPane _createGraphPane(NamedObj entity) {
        return _frameController._createGraphPane(entity);
    }

    protected JComponent _createRightComponent(NamedObj entity) {
        _frameController = new GTFrameController(this);
        JComponent component = _frameController._createRightComponent(entity);
        if (component == null) {
            component = super._createRightComponent(entity);
        }
        return component;
    }

    protected JCanvasPanner _getGraphPanner() {
        return _graphPanner;
    }

    protected static class ConfigureOperationsAction extends FigureAction {

        public void actionPerformed(ActionEvent event) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            Frame frame = getFrame();
            if (target instanceof GTEntity) {
                List<?> attributeList = target
                        .attributeList(EditorFactory.class);
                if (attributeList.size() > 0) {
                    EditorFactory factory = (EditorFactory) attributeList
                            .get(0);
                    factory.createEditor(target, frame);
                } else {
                    new EditParametersDialog(frame, target);
                }
            } else {
                List<?> ingredientsAttributes = target
                        .attributeList(GTIngredientsAttribute.class);
                try {
                    if (ingredientsAttributes.isEmpty()) {
                        Attribute attribute = new GTIngredientsAttribute(
                                target, target.uniqueName("operations"));
                        attribute.setPersistent(false);
                    }

                    EditorFactory factory = new GTIngredientsEditor.Factory(
                            target, target
                                    .uniqueName("ingredientsEditorFactory"));
                    factory.setPersistent(false);
                    factory.createEditor(target, frame);
                    factory.setContainer(null);
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
                }
            }
        }

        ConfigureOperationsAction(String name) {
            super(name);
        }
    }

    protected class GTActorGraphController extends ActorEditorGraphController {

        protected GTActorGraphController() {
            _newRelationAction = new NewRelationAction(new String[][] {
                    { "/ptolemy/vergil/actor/img/relation.gif",
                        GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/actor/img/relation_o.gif",
                        GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/actor/img/relation_ov.gif",
                        GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/actor/img/relation_on.gif",
                        GUIUtilities.SELECTED_ICON } });
        }

        protected void _createControllers() {
            super._createControllers();
            _entityController = new EntityController();
            _portController = new PortController();
        }

        protected void initializeInteraction() {
            super.initializeInteraction();

            MenuActionFactory newFactory = new GTMenuActionFactory(
                    _configureMenuFactory, _configureAction);
            _replaceFactory(_menuFactory, _configureMenuFactory, newFactory);
            _configureMenuFactory = newFactory;
        }

        private void _replaceFactory(PtolemyMenuFactory menuFactory,
                MenuItemFactory replacedFactory, MenuItemFactory replacement) {
            List<?> factories = menuFactory.menuItemFactoryList();
            int size = factories.size();
            for (int i = 0; i < size; i++) {
                MenuItemFactory factory = (MenuItemFactory) factories.get(0);
                menuFactory.removeMenuItemFactory(factory);
                if (factory == replacedFactory) {
                    menuFactory.addMenuItemFactory(replacement);
                } else {
                    menuFactory.addMenuItemFactory(factory);
                }
            }
        }

        private class EntityController extends ActorInstanceController {
            EntityController() {
                super(GTActorGraphController.this);

                MenuActionFactory newFactory = new GTMenuActionFactory(
                        _configureMenuFactory, _configureAction);
                _replaceFactory(_menuFactory, _configureMenuFactory, newFactory);
                _configureMenuFactory = newFactory;

                FigureAction operationsAction = new ConfigureOperationsAction(
                        "Operations");
                _configureMenuFactory.addAction(operationsAction, "Customize");
            }
        }

        private class NewRelationAction extends
                ActorEditorGraphController.NewRelationAction {

            public void actionPerformed(ActionEvent e) {
                if (getFrameController().isTableActive()) {
                    return;
                } else {
                    super.actionPerformed(e);
                }
            }

            private NewRelationAction(String[][] iconRoles) {
                super(iconRoles);
            }

        }

        private class PortController extends ExternalIOPortController {

            public PortController() {
                super(GTActorGraphController.this, AttributeController.FULL);

                MenuActionFactory newFactory = new GTMenuActionFactory(
                        _configureMenuFactory, _configureAction);
                _replaceFactory(_menuFactory, _configureMenuFactory, newFactory);
                _configureMenuFactory = newFactory;
            }
        }
    }

    protected static class GTFSMGraphController extends FSMGraphController {

    }

    protected static class GTMenuActionFactory extends MenuActionFactory {

        public void addAction(Action action, String label) {
            _oldFactory.addAction(action, label);
        }

        public void addActions(Action[] actions, String label) {
            _oldFactory.addActions(actions, label);
        }

        public JMenuItem create(JContextMenu menu, NamedObj object) {
            JMenuItem menuItem = _oldFactory.create(menu, object);
            if (menuItem instanceof JMenu) {
                JMenu subMenu = (JMenu) menuItem;
                if (subMenu.getText().equals("Customize")) {
                    Component[] menuItems = subMenu.getMenuComponents();
                    for (Component itemComponent : menuItems) {
                        JMenuItem item = (JMenuItem) itemComponent;
                        Action action = item.getAction();
                        if (object instanceof PortMatcher) {
                            // Disable all the items for a PortMatcher, which
                            // should be configured by double-clicking the
                            // containing CompositeActor.
                            item.setEnabled(false);
                        } else if (action instanceof PortDialogAction
                                && object instanceof GTEntity) {
                            // Disable the PortDialogAction from the context
                            // menu.
                            item.setEnabled(false);
                        } else if (action instanceof ConfigureOperationsAction
                                && (!(object instanceof Entity) || !GTTools
                                        .isInReplacement(object))) {
                            // Hide the ConfigureOperationsAction from the
                            // context menu.
                            item.setVisible(false);
                        }
                    }
                }
            }
            return menuItem;
        }

        GTMenuActionFactory(MenuActionFactory oldFactory, Action configureAction) {
            super(configureAction);

            _oldFactory = oldFactory;
        }

        private MenuActionFactory _oldFactory;
    }

    private GTFrameController _frameController;
}

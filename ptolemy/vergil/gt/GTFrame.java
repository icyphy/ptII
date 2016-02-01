/*

@Copyright (c) 2007-2014 The Regents of the University of California.
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

import javax.swing.JComponent;

import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.toolbox.JCanvasPanner;
import ptolemy.actor.gt.GTEntity;
import ptolemy.actor.gt.GTIngredientsAttribute;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.toolbox.FigureAction;

@SuppressWarnings("serial")
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

    @Override
    public void cancelFullScreen() {
        super.cancelFullScreen();
        _fullscreen = false;
    }

    @Override
    public void fullScreen() {
        _fullscreen = true;
        super.fullScreen();
    }

    public GTFrameController getFrameController() {
        return _frameController;
    }

    /** Return the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @return the JGraph.
     *  @see #setJGraph(JGraph)
     */
    @Override
    public JGraph getJGraph() {
        JGraph graph = _frameController.getJGraph();
        if (graph == null) {
            graph = super.getJGraph();
        }
        return graph;
    }

    public boolean isFullscreen() {
        return _fullscreen;
    }

    @Override
    protected boolean _close() {
        boolean result = super._close();
        if (result) {
            _frameController._removeListeners();
        }
        return result;
    }

    protected RunnableGraphController _createActorGraphController() {
        return new ActorEditorGraphController();
    }

    protected RunnableGraphController _createFSMGraphController() {
        return new FSMGraphController();
    }

    @Override
    protected GraphPane _createGraphPane(NamedObj entity) {
        return _frameController._createGraphPane(entity);
    }

    @Override
    protected JComponent _createRightComponent(NamedObj entity) {
        _frameController = new GTFrameController(this);
        JComponent component = _frameController._createRightComponent(entity);
        if (component == null) {
            if (entity instanceof TransformationRule
                    && ((TransformationRule) entity).mode.isMatchOnly()) {
                component = super
                        ._createRightComponent(((TransformationRule) entity)
                                .getPattern());
            } else {
                component = super._createRightComponent(entity);
            }
        }
        return component;
    }

    @Override
    protected SizeAttribute _createSizeAttribute()
            throws IllegalActionException, NameDuplicationException {
        SizeAttribute size = super._createSizeAttribute();
        if (_frameController.hasTabs()) {
            Component component = _frameController.getTabbedPane()
                    .getComponent(0);
            size.recordSize(component);
        }
        return size;
    }

    protected JCanvasPanner _getGraphPanner() {
        return _graphPanner;
    }

    protected static class ConfigureCriteriaAction extends
    ConfigureIngredientsAction {

        @Override
        protected String _getAttributeName() {
            return "criteria";
        }

        ConfigureCriteriaAction() {
            super("Criteria");
        }
    }

    protected static abstract class ConfigureIngredientsAction extends
    FigureAction {

        @Override
        public void actionPerformed(ActionEvent event) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            Frame frame = getFrame();
            if (target instanceof GTEntity) {
                EditorFactory factory = null;
                try {
                    target.workspace().getReadAccess();
                    List<?> attributeList = target
                            .attributeList(EditorFactory.class);
                    if (attributeList.size() > 0) {
                        factory = (EditorFactory) attributeList.get(0);
                    }
                } finally {
                    target.workspace().doneReading();
                }
                if (factory != null) {
                    factory.createEditor(target, frame);
                } else {
                    new EditParametersDialog(frame, target);
                }
            } else {
                try {
                    target.workspace().getReadAccess();
                    List<?> ingredientsAttributes = target
                            .attributeList(GTIngredientsAttribute.class);
                    if (ingredientsAttributes.isEmpty()) {
                        Attribute attribute = new GTIngredientsAttribute(
                                target, target.uniqueName(_getAttributeName()));
                        attribute.setPersistent(false);
                    }
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
                } finally {
                    target.workspace().doneReading();
                }
                try {
                    EditorFactory factory = new GTIngredientsEditor.Factory(
                            target,
                            target.uniqueName("ingredientsEditorFactory"));
                    factory.setPersistent(false);
                    factory.createEditor(target, frame);
                    factory.setContainer(null);
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
                }
            }
        }

        protected abstract String _getAttributeName();

        ConfigureIngredientsAction(String name) {
            super(name);
        }
    }

    protected static class ConfigureOperationsAction extends
    ConfigureIngredientsAction {

        @Override
        protected String _getAttributeName() {
            return "operations";
        }

        ConfigureOperationsAction() {
            super("Operations");
        }
    }

    private GTFrameController _frameController;

    private boolean _fullscreen = false;
}

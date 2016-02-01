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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.Site;
import diva.canvas.connector.AbstractConnector;
import diva.canvas.connector.Connector;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.RoundedRectangle;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.gui.GUIUtilities;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.GraphTransformer;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ExternalIOPortController;
import ptolemy.vergil.actor.IOPortController;
import ptolemy.vergil.actor.LinkController;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.gt.GTFrameController.UpdateController;
import ptolemy.vergil.gt.GTFrameTools.ModelChangeRequest;
import ptolemy.vergil.kernel.RelationController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.StateController;
import ptolemy.vergil.modal.TransitionController;
import ptolemy.vergil.toolbox.FigureAction;

@SuppressWarnings("serial")
public class MatchResultViewer extends GTFrame {

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
    public MatchResultViewer(CompositeEntity entity, Tableau tableau) {
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
    public MatchResultViewer(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                _windowClosed();
            }
        });

        _checkContainingViewer();
        _enableOrDisableActions();
        _rerender();
    }

    public void clearFileSelectionStatus() {
        _fileSelectionStatus = FileSelectionStatus.NONE;
    }

    public FileSelectionStatus getFileSelectionStatus() {
        return _fileSelectionStatus;
    }

    public void setBatchMode(boolean batchMode) {
        _isBatchMode = batchMode;
        _previousFileItem.setVisible(batchMode);
        _nextFileItem.setVisible(batchMode);
        _previousFileButton.setVisible(batchMode);
        _nextFileButton.setVisible(batchMode);
    }

    public void setMatchResult(TransformationRule rule, String sourceFileName,
            List<MatchResult> results) {
        setMatchResult(rule, sourceFileName, results, 0);
    }

    public void setMatchResult(TransformationRule rule, String sourceFileName,
            List<MatchResult> results, int position) {
        _rule = rule;
        _sourceFileName = sourceFileName;
        _results = results;

        int size = results.size();
        if (position < size) {
            _currentPosition = position;
        } else if (size > 0) {
            _currentPosition = size - 1;
        } else {
            _currentPosition = 0;
        }
        _enableOrDisableActions();
        _rerender();
        _refreshStatusBars();
    }

    public void setNextFileEnabled(boolean nextFileEnabled) {
        _isNextFileEnabled = nextFileEnabled;
        _enableOrDisableActions();
    }

    public void setPreviousFileEnabled(boolean previousFileEnabled) {
        _isPreviousFileEnabled = previousFileEnabled;
        _enableOrDisableActions();
    }

    public enum FileSelectionStatus {
        NEXT, NONE, PREVIOUS;
    }

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        PreviousAction previousAction = new PreviousAction();
        NextAction nextAction = new NextAction();
        PreviousFileAction previousFileAction = new PreviousFileAction();
        NextFileAction nextFileAction = new NextFileAction();
        TransformAction transformAction = new TransformAction();
        TransformUntilFixpointAction transformUntilFixpointAction = new TransformUntilFixpointAction();
        CloseAction closeAction = new CloseAction();

        _viewMenu.addSeparator();
        _previousItem = GUIUtilities.addMenuItem(_viewMenu, previousAction);
        _nextItem = GUIUtilities.addMenuItem(_viewMenu, nextAction);
        _previousFileItem = GUIUtilities.addMenuItem(_viewMenu,
                previousFileAction);
        _nextFileItem = GUIUtilities.addMenuItem(_viewMenu, nextFileAction);

        _transformMenu = new JMenu("Transform");
        _transformMenu.setMnemonic(KeyEvent.VK_T);
        _transformItem = GUIUtilities.addMenuItem(_transformMenu,
                transformAction);
        _transformUntilFixpointItem = GUIUtilities.addMenuItem(_transformMenu,
                transformUntilFixpointAction);
        GUIUtilities.addMenuItem(_transformMenu, closeAction);
        _menubar.add(_transformMenu);

        _previousFileButton = GUIUtilities.addToolBarButton(_toolbar,
                previousFileAction);
        _previousButton = GUIUtilities.addToolBarButton(_toolbar,
                previousAction);
        _nextButton = GUIUtilities.addToolBarButton(_toolbar, nextAction);
        _nextFileButton = GUIUtilities.addToolBarButton(_toolbar,
                nextFileAction);
        _transformButton = GUIUtilities.addToolBarButton(_toolbar,
                transformAction);
        _transformUntilFixpointButton = GUIUtilities.addToolBarButton(_toolbar,
                transformUntilFixpointAction);
        GUIUtilities.addToolBarButton(_toolbar, closeAction);

        setBatchMode(_isBatchMode);
        _enableOrDisableActions();
    }

    @Override
    protected RunnableGraphController _createActorGraphController() {
        return new MatchResultActorGraphController();
    }

    @Override
    protected RunnableGraphController _createFSMGraphController() {
        return new MatchResultFSMGraphController();
    }

    protected static void _setTableauFactory(Object originator,
            final CompositeEntity entity) {
        Class<?> tableauFactoryClass = MatchResultTableau.Factory.class;
        try {
            entity.workspace().getReadAccess();
            List<?> factoryList = entity.attributeList(tableauFactoryClass);
            if (factoryList.isEmpty()) {
                try {
                    new MatchResultTableau.Factory(entity,
                            entity.uniqueName("_tableauFactory"))
                    .setPersistent(false);
                } catch (KernelException e) {
                    throw new KernelRuntimeException(e, "Unexpected exception");
                }
            }
            for (Object subentity : entity.entityList(CompositeEntity.class)) {
                _setTableauFactory(originator, (CompositeEntity) subentity);
            }
            for (Object subentity : entity.classDefinitionList()) {
                if (subentity instanceof CompositeEntity) {
                    _setTableauFactory(originator, (CompositeEntity) subentity);
                }
            }
        } finally {
            entity.workspace().doneReading();
        }
    }

    protected static void _unsetTableauFactory(Object originator,
            CompositeEntity entity) {
        try {
            entity.workspace().getReadAccess();
            List<?> factoryList = entity
                    .attributeList(MatchResultTableau.Factory.class);
            for (Object attributeObject : factoryList) {
                MatchResultTableau.Factory factory = (MatchResultTableau.Factory) attributeObject;
                String momlTxt = "<deleteProperty name=\"" + factory.getName()
                        + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(originator,
                        entity, momlTxt);
                entity.requestChange(request);
            }
            for (Object subentity : entity.entityList(CompositeEntity.class)) {
                _unsetTableauFactory(originator, (CompositeEntity) subentity);
            }
        } finally {
            entity.workspace().doneReading();
        }
    }

    protected void _windowClosed() {
        if (_topFrame != null) {
            synchronized (_topFrame) {
                _topFrame._subviewers.remove(this);
            }
        }
    }

    protected JMenu _transformMenu;

    protected class MatchResultActorController extends ActorController {

        @Override
        protected Figure _renderNode(Object node) {
            if (node != null && !_hide(node)) {
                Figure nf = super._renderNode(node);
                GraphModel model = getController().getGraphModel();
                Object object = model.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(nf);
                _renderNamedObj(cf, object);
                return nf;
            }

            return super._renderNode(node);
        }

        MatchResultActorController(GraphController controller) {
            super(controller);
        }
    }

    protected class MatchResultActorGraphController extends
    ActorEditorGraphController {

        @Override
        protected void _createControllers() {
            super._createControllers();

            _entityController = new MatchResultActorController(this);
            _entityPortController = new MatchResultPortController(this);
            _linkController = new MatchResultLinkController(this);
            _portController = new MatchResultExternalPortController(this);
            _relationController = new MatchResultRelationController(this);
        }
    }

    protected class MatchResultExternalPortController extends
    ExternalIOPortController {

        MatchResultExternalPortController(GraphController controller) {
            super(controller);

            setNodeRenderer(new Renderer());
        }

        private class Renderer extends PortRenderer {

            @Override
            public Figure render(Object node) {
                if (node != null && !_hide(node)) {
                    Figure nf = super.render(node);
                    GraphModel graphModel = getController().getGraphModel();
                    Object object = graphModel.getSemanticObject(node);
                    CompositeFigure cf = _getCompositeFigure(nf);
                    if (cf == null) {
                        cf = new CompositeFigure(nf);
                        _renderNamedObj(cf, object);
                        return cf;
                    } else {
                        _renderNamedObj(cf, object);
                        return nf;
                    }
                }
                return null;
            }
        }
    }

    protected class MatchResultFSMGraphController extends FSMGraphController {

        @Override
        public Figure drawNode(Object node) {
            Figure figure = super.drawNode(node);
            ((MatchResultStateController) _stateController)._highlightNode(
                    node, figure);
            return figure;
        }

        @Override
        protected void _createControllers() {
            super._createControllers();

            _stateController = new MatchResultStateController(this);
            _transitionController = new MatchResultTransitionController(this);
        }
    }

    protected class MatchResultLinkController extends LinkController {

        @Override
        public Connector render(Object edge, FigureLayer layer, Site tailSite,
                Site headSite) {
            Connector connector = super.render(edge, layer, tailSite, headSite);
            if (connector instanceof AbstractConnector) {
                GraphModel graphModel = getController().getGraphModel();
                Object semanticObject = graphModel.getSemanticObject(edge);
                _renderLink(connector, semanticObject);
            }
            return connector;
        }

        MatchResultLinkController(GraphController controller) {
            super(controller);
        }
    }

    protected class MatchResultPortController extends IOPortController {

        MatchResultPortController(GraphController controller) {
            super(controller);

            setNodeRenderer(new Renderer());
        }

        private class Renderer extends EntityPortRenderer {

            @Override
            protected Figure _decoratePortFigure(Object node, Figure figure) {
                GraphModel graphModel = getController().getGraphModel();
                Object object = graphModel.getSemanticObject(node);
                CompositeFigure composite = _getCompositeFigure(figure);
                if (composite == null) {
                    composite = new CompositeFigure(figure);
                    _renderNamedObj(composite, object);
                    return composite;
                } else {
                    _renderNamedObj(composite, object);
                    return figure;
                }
            }
        }
    }

    protected class MatchResultRelationController extends RelationController {

        @Override
        protected Figure _renderNode(Object node) {
            if (node != null && !_hide(node)) {
                Figure nf = super._renderNode(node);
                GraphModel model = getController().getGraphModel();
                Object object = model.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(nf);
                _renderNamedObj(cf, object);
                return nf;
            }

            return super._renderNode(node);
        }

        MatchResultRelationController(GraphController controller) {
            super(controller);
        }
    }

    protected class MatchResultStateController extends StateController {

        protected void _highlightNode(Object node, Figure figure) {
            if (node != null && !_hide(node)) {
                GraphModel model = getController().getGraphModel();
                Object object = model.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(figure);
                _renderState(cf, object);
            }
        }

        MatchResultStateController(GraphController controller) {
            super(controller);
        }
    }

    protected class MatchResultTransitionController extends
    TransitionController {

        public MatchResultTransitionController(GraphController controller) {
            super(controller);
        }

        @Override
        public Connector render(Object edge, FigureLayer layer, Site tailSite,
                Site headSite) {
            Connector connector = super.render(edge, layer, tailSite, headSite);
            if (connector instanceof AbstractConnector) {
                GraphModel graphModel = getController().getGraphModel();
                Object semanticObject = graphModel.getSemanticObject(edge);
                _renderLink(connector, semanticObject);
            }
            return connector;
        }
    }

    private void _beginTransform() {
        ((UpdateController) _getGraphModel()).stopUpdate();
    }

    private void _checkContainingViewer() {
        NamedObj toplevel = getModel().toplevel();
        for (Frame frame : getFrames()) {
            if (frame != this && frame instanceof MatchResultViewer) {
                MatchResultViewer viewer = (MatchResultViewer) frame;
                if (viewer.getModel() == toplevel) {
                    synchronized (viewer) {
                        _results = viewer._results;
                        _currentPosition = viewer._currentPosition;
                        _isBatchMode = viewer._isBatchMode;
                        _isPreviousFileEnabled = viewer._isPreviousFileEnabled;
                        _isNextFileEnabled = viewer._isNextFileEnabled;
                        _rule = viewer._rule;
                        _sourceFileName = viewer._sourceFileName;
                        viewer._subviewers.add(this);
                        _topFrame = viewer;
                    }
                    break;
                }
            }
        }
        if (_topFrame == null) {
            _subviewers = new HashSet<MatchResultViewer>();
        }
    }

    private void _closeSubviewers() {
        if (_topFrame != null) {
            _topFrame._closeSubviewers();
        } else {
            for (MatchResultViewer subviewer : _subviewers) {
                if (subviewer != null) {
                    Effigy effigy = subviewer.getEffigy();
                    if (effigy != null) {
                        effigy.setModified(false);
                    }
                    subviewer.close();
                }
            }
        }
    }

    private void _delegateUndoStack(NamedObj from, NamedObj to)
            throws CloneNotSupportedException, IllegalActionException,
            NameDuplicationException {
        UndoStackAttribute prevStack = UndoStackAttribute.getUndoInfo(from);
        UndoStackAttribute stack = (UndoStackAttribute) prevStack.clone(to
                .workspace());
        stack.setContainer(to);
    }

    private void _enableOrDisableActions() {
        if (_previousItem != null && _results != null) {
            _previousItem.setEnabled(!_results.isEmpty()
                    && _currentPosition > 0);
        }
        if (_previousButton != null && _results != null) {
            _previousButton.setEnabled(!_results.isEmpty()
                    && _currentPosition > 0);
        }
        if (_nextItem != null && _results != null) {
            _nextItem.setEnabled(_currentPosition < _results.size() - 1);
        }
        if (_nextButton != null && _results != null) {
            _nextButton.setEnabled(_currentPosition < _results.size() - 1);
        }
        if (_previousFileItem != null && _results != null) {
            _previousFileItem.setEnabled(_isPreviousFileEnabled);
        }
        if (_previousFileButton != null && _results != null) {
            _previousFileButton.setEnabled(_isPreviousFileEnabled);
        }
        if (_nextFileItem != null && _results != null) {
            _nextFileItem.setEnabled(_isNextFileEnabled);
        }
        if (_nextFileButton != null && _results != null) {
            _nextFileButton.setEnabled(_isNextFileEnabled);
        }
        if (_transformItem != null && _results != null) {
            _transformItem.setEnabled(_currentPosition < _results.size()
                    && _rule != null);
        }
        if (_transformButton != null && _results != null) {
            _transformButton.setEnabled(_currentPosition < _results.size()
                    && _rule != null);
        }
        if (_transformUntilFixpointItem != null && _results != null) {
            _transformUntilFixpointItem.setEnabled(_currentPosition < _results
                    .size() && _rule != null);
        }
        if (_transformUntilFixpointButton != null && _results != null) {
            _transformUntilFixpointButton
            .setEnabled(_currentPosition < _results.size()
                    && _rule != null);
        }
    }

    private void _finishTransform(CompositeEntity oldModel) {
        CompositeEntity currentModel = (CompositeEntity) getModel();
        CompositeEntity model;
        Workspace workspace = currentModel.workspace();
        try {
            model = (CompositeEntity) GTTools.cleanupModel(currentModel,
                    workspace);
            workspace.remove(currentModel);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(currentModel, e,
                    "Unable to clean up model.");
        }
        if (_topFrame == null) {
            GTFrameTools.changeModel(this, model, true, true,
                    new UndoChangeModelAction(oldModel, _currentPosition));
        }

        _setTableauFactory(this, model);

        List<MatchResult> results = new LinkedList<MatchResult>();
        if (_rule != null) {
            Pattern pattern = _rule.getPattern();
            MatchResultRecorder recorder = new MatchResultRecorder();
            GraphMatcher matcher = new GraphMatcher();
            matcher.setMatchCallback(recorder);
            matcher.match(pattern, model);
            results = recorder.getResults();
        }
        setMatchResult(_rule, _sourceFileName, results);
        _closeSubviewers();
        _enableOrDisableActions();

        ((UpdateController) _getGraphModel()).startUpdate();
    }

    private Color _getHighlightColor(NamedObj object) {
        if (_results != null && _currentPosition < _results.size()
                && _results.get(_currentPosition).containsValue(object)) {
            return _HIGHLIGHT_COLOR;
        } else {
            return null;
        }
    }

    private void _next() {
        if (_currentPosition < _results.size() - 1) {
            _currentPosition++;
            _rerender();
            if (_topFrame == null) {
                for (MatchResultViewer viewer : _subviewers) {
                    viewer._next();
                }
            }
            _enableOrDisableActions();
            _refreshStatusBars();
        }
    }

    private void _nextFile() {
        _fileSelectionStatus = FileSelectionStatus.NEXT;
        for (MatchResultViewer viewer : _subviewers) {
            viewer.setVisible(false);
        }
        setVisible(false);
        _refreshStatusBars();
    }

    private void _previous() {
        if (_currentPosition > 0) {
            _currentPosition--;
            _rerender();
            if (_topFrame == null) {
                for (MatchResultViewer viewer : _subviewers) {
                    viewer._previous();
                }
            }
            _enableOrDisableActions();
            _refreshStatusBars();
        }
    }

    private void _previousFile() {
        _fileSelectionStatus = FileSelectionStatus.PREVIOUS;
        for (MatchResultViewer viewer : _subviewers) {
            viewer.setVisible(false);
        }
        setVisible(false);
        _refreshStatusBars();
    }

    private void _refreshStatusBars() {
        if (_topFrame != null) {
            _topFrame._refreshStatusBars();
        } else {
            StringBuffer text = new StringBuffer();
            if (_sourceFileName != null) {
                text.append("Match: ");
                text.append(_sourceFileName);
                text.append("  ");
            }
            if (_results != null) {
                text.append('(');
                text.append(_currentPosition + 1);
                text.append('/');
                text.append(_results.size());
                text.append(')');
            }
            _statusBar.setMessage(text.toString());
            int max = 0;
            if (_results != null) {
                max = _results.size();
                _statusBar.progressBar().setMaximum(max);
                _statusBar.progressBar().setValue(_currentPosition + 1);
            }
            for (MatchResultViewer subviewer : _subviewers) {
                subviewer._statusBar.setMessage(text.toString());
                if (_results != null) {
                    subviewer._statusBar.progressBar().setValue(
                            _currentPosition + 1);
                    subviewer._statusBar.progressBar().setMaximum(max);
                }
            }
        }
    }

    private void _renderLink(Connector connector, Object semanticObject) {
        if (semanticObject instanceof NamedObj && connector != null) {
            Color color = _getHighlightColor((NamedObj) semanticObject);
            if (color != null) {
                AbstractConnector c = (AbstractConnector) connector;
                c.setStrokePaint(color);
                c.setStroke(new BasicStroke(_HIGHLIGHT_THICKNESS));
            }
        }
    }

    private void _renderNamedObj(CompositeFigure figure, Object semanticObject) {
        if (semanticObject instanceof NamedObj && figure != null) {
            Color color = _getHighlightColor((NamedObj) semanticObject);
            if (color != null) {
                Rectangle2D bounds = figure.getBackgroundFigure().getBounds();
                float padding = _HIGHLIGHT_PADDING;
                BasicFigure bf = new BasicRectangle(bounds.getX() - padding,
                        bounds.getY() - padding, bounds.getWidth() + padding
                        * 2.0, bounds.getHeight() + padding * 2.0,
                        _HIGHLIGHT_THICKNESS);
                bf.setStrokePaint(color);

                int index = figure.getFigureCount();
                if (index < 0) {
                    index = 0;
                }
                figure.add(index, bf);
            }
        }
    }

    private void _renderState(CompositeFigure figure, Object semanticObject) {
        if (semanticObject instanceof NamedObj && figure != null
                && _results != null && _currentPosition < _results.size()
                && _results.get(_currentPosition).containsValue(semanticObject)) {
            Rectangle2D bounds = figure.getBackgroundFigure().getBounds();
            float padding = _HIGHLIGHT_PADDING;
            RoundedRectangle rf = new RoundedRectangle(bounds.getX() - padding,
                    bounds.getY() - padding, bounds.getWidth() + padding * 2.0,
                    bounds.getHeight() + padding * 2.0, null,
                    _HIGHLIGHT_THICKNESS, 32.0, 32.0);
            rf.setStrokePaint(_HIGHLIGHT_COLOR);

            int index = figure.getFigureCount();
            if (index < 0) {
                index = 0;
            }
            figure.add(index, rf);
        }
    }

    private void _rerender() {
        _getGraphController().rerender();
    }

    private void _showInDefaultEditor() {
        boolean modified = isModified();
        setModified(false);
        close();

        try {
            CompositeEntity currentModel = (CompositeEntity) getModel();
            Workspace workspace = currentModel.workspace();
            CompositeEntity model = (CompositeEntity) GTTools.cleanupModel(
                    currentModel, workspace);
            workspace.remove(currentModel);
            Tableau tableau = getFrameController().getConfiguration()
                    .openModel(model);
            ((Effigy) tableau.getContainer()).uri.setURI(null);
            String name = model.getName();
            if (name.equals("")) {
                name = "Unnamed";
            }
            tableau.setTitle(name);
            Frame frame = tableau.getFrame();
            if (modified && frame instanceof TableauFrame) {
                ((TableauFrame) tableau.getFrame()).setModified(true);
            }
        } catch (Exception e) {
            MessageHandler.error("Cannot open default tableau for the "
                    + "model.", e);
        }
    }

    private void _transform() {
        _beginTransform();

        CompositeEntity currentModel = (CompositeEntity) getModel();
        CompositeEntity oldModel;
        try {
            oldModel = (CompositeEntity) GTTools.cleanupModel(currentModel);
            _delegateUndoStack(currentModel, oldModel);

            GraphTransformer.transform(_rule, _results.get(_currentPosition));
        } catch (Exception e) {
            MessageHandler.error("Unable to transform model.", e);
            return;
        }
        _finishTransform(oldModel);
    }

    private void _transformUntilFixpoint() {
        _beginTransform();

        CompositeEntity currentModel = (CompositeEntity) getModel();
        CompositeEntity oldModel;
        try {
            oldModel = (CompositeEntity) GTTools.cleanupModel(currentModel);
            _delegateUndoStack(getModel(), oldModel);

            GraphMatcher matcher = null;
            int i = 0;
            while (!_results.isEmpty()) {
                int pos = (int) (Math.random() * _results.size());
                GraphTransformer.transform(_rule, _results.get(pos));
                MatchResultRecorder recorder = new MatchResultRecorder();
                if (matcher == null) {
                    matcher = new GraphMatcher();
                }
                matcher.setMatchCallback(recorder);
                matcher.match(_rule.getPattern(), currentModel);
                _results = recorder.getResults();

                if (i >= 0) {
                    i++;
                }
                if (i >= _PROMPT_TO_CONTINUE_COUNT && !_results.isEmpty()) {
                    boolean answer = MessageHandler.yesNoQuestion("The "
                            + "transformation process has not terminated "
                            + "within " + _PROMPT_TO_CONTINUE_COUNT
                            + " randomly chosen steps.\nIt is possible that "
                            + "the transformations never reach a fixpoint.\n"
                            + "Do you intend to continue? (If so, no more "
                            + "questions will be asked.)");
                    if (!answer) {
                        break;
                    }
                    i = -1;
                }
            }
        } catch (Exception e) {
            MessageHandler.error("Unable to transform model", e);
            return;
        }
        _finishTransform(oldModel);
    }

    private static final Color _HIGHLIGHT_COLOR = new Color(160, 112, 255);

    private static final float _HIGHLIGHT_PADDING = 1.0f;

    private static final float _HIGHLIGHT_THICKNESS = 3.0f;

    private static final int _PROMPT_TO_CONTINUE_COUNT = 100;

    private int _currentPosition;

    private FileSelectionStatus _fileSelectionStatus = FileSelectionStatus.NONE;

    private boolean _isBatchMode = false;

    private boolean _isNextFileEnabled = false;

    private boolean _isPreviousFileEnabled = false;

    private JButton _nextButton;

    private JButton _nextFileButton;

    private JMenuItem _nextFileItem;

    private JMenuItem _nextItem;

    private JButton _previousButton;

    private JButton _previousFileButton;

    private JMenuItem _previousFileItem;

    private JMenuItem _previousItem;

    private List<MatchResult> _results;

    private TransformationRule _rule;

    private String _sourceFileName;

    private Set<MatchResultViewer> _subviewers;

    /** The top frame that shows the toplevel model, or <tt>null</tt> if the top
     *  frame is this frame itself.
     */
    private MatchResultViewer _topFrame;

    private JButton _transformButton;

    private JMenuItem _transformItem;

    private JButton _transformUntilFixpointButton;

    private JMenuItem _transformUntilFixpointItem;

    private class CloseAction extends FigureAction {

        public CloseAction() {
            super("Close Transformation Window");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/close.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/close_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/close_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/close_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Close the current view and open the model in "
                    + "model editor");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_K, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            super.actionPerformed(event);

            if (_topFrame != null) {
                _topFrame._showInDefaultEditor();
            } else {
                _showInDefaultEditor();
            }
        }
    }

    private class NextAction extends FigureAction {

        public NextAction() {
            super("Next");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/next.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/next_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/next_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/next_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Highlight next match (Ctrl+->)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._next();
            } else {
                _next();
            }
        }
    }

    private class NextFileAction extends FigureAction {

        public NextFileAction() {
            super("Next File");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/nextfile.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/nextfile_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/nextfile_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/nextfile_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match next file (Ctrl+.)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._nextFile();
            } else {
                _nextFile();
            }
        }
    }

    private class PreviousAction extends FigureAction {

        public PreviousAction() {
            super("Previous");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/previous.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/previous_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/previous_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/previous_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Highlight previous match (Ctrl+<-)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._previous();
            } else {
                _previous();
            }
        }

    }

    private class PreviousFileAction extends FigureAction {

        public PreviousFileAction() {
            super("Previous File");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/previousfile.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/previousfile_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/previousfile_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/previousfile_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match previous file (Ctrl+,)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._previousFile();
            } else {
                _previousFile();
            }
        }
    }

    private class TransformAction extends FigureAction {

        public TransformAction() {
            super("Transform");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/transform.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/transform_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/transform_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/transform_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Transform the current highlighted occurrence "
                    + "(Ctrl+/)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_SLASH, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._transform();
            } else {
                _transform();
            }
        }

    }

    private class TransformUntilFixpointAction extends FigureAction {

        public TransformUntilFixpointAction() {
            super("Transform Until Fixpoint");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/transformfixpoint.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/gt/img/transformfixpoint_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/gt/img/transformfixpoint_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/gt/img/transformfixpoint_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Transform a random occurrence of the pattern "
                    + "until no more matches can be found (Ctrl+\\)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_BACK_SLASH, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._transformUntilFixpoint();
            } else {
                _transformUntilFixpoint();
            }
        }
    }

    private class UndoChangeModelAction implements UndoAction {

        @Override
        public void execute() throws Exception {
            MatchResultViewer viewer = MatchResultViewer.this;
            CompositeEntity currentModel = (CompositeEntity) getModel();
            CompositeEntity oldModel = (CompositeEntity) GTTools
                    .cleanupModel(currentModel);
            _delegateUndoStack(currentModel, oldModel);
            ModelChangeRequest request = new ModelChangeRequest(viewer, viewer,
                    _model, new UndoChangeModelAction(oldModel,
                            _currentPosition));
            request.setUndoable(true);
            request.execute();

            List<MatchResult> results = new LinkedList<MatchResult>();
            if (_rule != null) {
                Pattern pattern = _rule.getPattern();
                MatchResultRecorder recorder = new MatchResultRecorder();
                GraphMatcher matcher = new GraphMatcher();
                matcher.setMatchCallback(recorder);
                matcher.match(pattern, _model);
                results = recorder.getResults();
            }
            setMatchResult(_rule, _sourceFileName, results, _position);

            _closeSubviewers();
            _enableOrDisableActions();
        }

        UndoChangeModelAction(CompositeEntity model, int position) {
            _model = model;
            _position = position;
        }

        private CompositeEntity _model;

        private int _position;
    }
}

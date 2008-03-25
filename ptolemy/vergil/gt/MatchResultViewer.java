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

import java.awt.Color;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import ptolemy.actor.gt.GraphTransformer;
import ptolemy.actor.gt.TransformationException;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorController;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.StateController;
import ptolemy.vergil.gt.GTFrameController.GTActorGraphModel;
import ptolemy.vergil.toolbox.FigureAction;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.RoundedRectangle;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.gui.GUIUtilities;

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

    public void setMatchResult(List<MatchResult> results) {
        _results = results;
        _currentPosition = 0;
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

    public void setSourceFileName(String sourceFileName) {
        _sourceFileName = sourceFileName;
        _refreshStatusBars();
    }

    public void setTransformationRule(TransformationRule rule) {
        _rule = rule;
        _enableOrDisableActions();
    }

    public enum FileSelectionStatus {
        NEXT, NONE, PREVIOUS;
    }

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        setSourceFileName(_sourceFileName);

        PreviousAction previousAction = new PreviousAction();
        NextAction nextAction = new NextAction();
        PreviousFileAction previousFileAction = new PreviousFileAction();
        NextFileAction nextFileAction = new NextFileAction();
        TransformAction transformAction = new TransformAction();
        TransformAllAction transformAllAction = new TransformAllAction();
        CloseAction closeAction = new CloseAction();

        _viewMenu.addSeparator();
        _previousItem = GUIUtilities.addMenuItem(_viewMenu, previousAction);
        _nextItem = GUIUtilities.addMenuItem(_viewMenu, nextAction);
        _previousFileItem = GUIUtilities.addMenuItem(_viewMenu,
                previousFileAction);
        _nextFileItem = GUIUtilities.addMenuItem(_viewMenu, nextFileAction);
        _transformItem = GUIUtilities.addMenuItem(_viewMenu, transformAction);
        _transformAllItem = GUIUtilities.addMenuItem(_viewMenu,
                transformAllAction);
        GUIUtilities.addMenuItem(_viewMenu, closeAction);

        _previousFileButton = GUIUtilities.addToolBarButton(_toolbar,
                previousFileAction);
        _previousButton = GUIUtilities.addToolBarButton(_toolbar,
                previousAction);
        _nextButton = GUIUtilities.addToolBarButton(_toolbar, nextAction);
        _nextFileButton = GUIUtilities.addToolBarButton(_toolbar,
                nextFileAction);
        _transformButton = GUIUtilities.addToolBarButton(_toolbar,
                transformAction);
        _transformAllButton = GUIUtilities.addToolBarButton(_toolbar,
                transformAllAction);
        GUIUtilities.addToolBarButton(_toolbar, closeAction);

        setBatchMode(_isBatchMode);
        _enableOrDisableActions();
    }

    protected static void _setTableauFactory(Object originator,
            final CompositeEntity entity) {
        Class<?> tableauFactoryClass = MatchResultTableau.Factory.class;
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
    }

    protected static void _unsetTableauFactory(Object originator,
            CompositeEntity entity) {
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
    }

    protected void _windowClosed() {
        if (_topFrame != null) {
            synchronized (_topFrame) {
                _topFrame._subviewers.remove(this);
            }
        }
    }

    protected class MatchResultActorController extends ActorController {

        protected Figure _renderNode(Object node) {
            if ((node != null) && !_hide(node)) {
                Figure nf = super._renderNode(node);
                GraphModel model = getController().getGraphModel();
                Object object = model.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(nf);

                if (object instanceof NamedObj && cf != null && !_transformed
                        && _results != null
                        && _results.get(_currentPosition).containsValue(object)) {
                    Rectangle2D bounds = cf.getBackgroundFigure().getBounds();
                    float padding = _HIGHLIGHT_PADDING;
                    BasicFigure bf = new BasicRectangle(bounds.getX() - padding,
                            bounds.getY() - padding,
                            bounds.getWidth() + padding * 2.0,
                            bounds.getHeight() + padding * 2.0,
                            _HIGHLIGHT_THICKNESS);
                    bf.setStrokePaint(_HIGHLIGHT_COLOR);

                    int index = cf.getFigureCount();
                    if (index < 0) {
                        index = 0;
                    }
                    cf.add(index, bf);
                }
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

        protected void _createControllers() {
            super._createControllers();

            _entityController = new MatchResultActorController(this);
        }
    }

    protected class MatchResultFSMGraphController extends FSMGraphController {

        public Figure drawNode(Object node) {
            Figure figure = super.drawNode(node);
            ((MatchResultStateController) _stateController)._highlightNode(node,
                    figure);
            return figure;
        }

        protected void _createControllers() {
            super._createControllers();

            _stateController = new MatchResultStateController(this);
        }
    }

    protected class MatchResultStateController extends StateController {

        protected void _highlightNode(Object node, Figure figure) {
            if ((node != null) && !_hide(node)) {
                GraphModel model = getController().getGraphModel();
                Object object = model.getSemanticObject(node);
                CompositeFigure cf = _getCompositeFigure(figure);

                if (object instanceof NamedObj && cf != null && !_transformed
                        && _results != null
                        && _results.get(_currentPosition).containsValue(object)) {
                    Rectangle2D bounds = cf.getBackgroundFigure().getBounds();
                    float padding = _HIGHLIGHT_PADDING;
                    RoundedRectangle rf = new RoundedRectangle(
                            bounds.getX() - padding, bounds.getY() - padding,
                            bounds.getWidth() + padding * 2.0,
                            bounds.getHeight() + padding * 2.0,
                            null, _HIGHLIGHT_THICKNESS, 32.0, 32.0);
                    rf.setStrokePaint(_HIGHLIGHT_COLOR);

                    int index = cf.getFigureCount();
                    if (index < 0) {
                        index = 0;
                    }
                    cf.add(index, rf);
                }
            }
        }

        MatchResultStateController(GraphController controller) {
            super(controller);
        }
    }

    private void _beginTransform() {
        ((GTActorGraphModel) _getGraphModel()).stopUpdate();
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
                        _transformed = viewer._transformed;
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

    private void _enableOrDisableActions() {
        if (_previousItem != null && _results != null) {
            _previousItem.setEnabled(!_transformed && _currentPosition > 0);
        }
        if (_previousButton != null && _results != null) {
            _previousButton.setEnabled(!_transformed && _currentPosition > 0);
        }
        if (_nextItem != null && _results != null) {
            _nextItem.setEnabled(!_transformed
                    && _currentPosition < _results.size() - 1);
        }
        if (_nextButton != null && _results != null) {
            _nextButton.setEnabled(!_transformed
                    && _currentPosition < _results.size() - 1);
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
            _transformItem.setEnabled(!_transformed && _rule != null);
        }
        if (_transformButton != null && _results != null) {
            _transformButton.setEnabled(!_transformed && _rule != null);
        }
        if (_transformAllItem != null && _results != null) {
            _transformAllItem.setEnabled(!_transformed && _rule != null);
        }
        if (_transformAllButton != null && _results != null) {
            _transformAllButton.setEnabled(!_transformed && _rule != null);
        }
    }

    private void _finishTransform() {
        _setTableauFactory(this, (CompositeEntity) getModel());
        _transformed = true;
        if (_topFrame == null) {
            for (MatchResultViewer viewer : _subviewers) {
                viewer._finishTransform();
            }
        }
        _enableOrDisableActions();
        ((GTActorGraphModel) _getGraphModel()).startUpdate();
        _rerender();
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
                _statusBar.progressBar().setValue(_currentPosition + 1);
                _statusBar.progressBar().setMaximum(max);
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

    private void _rerender() {
        _getGraphController().rerender();
    }

    private void _showInDefaultEditor() {
        String moml = getModel().exportMoML();
        boolean modified = isModified();
        setModified(false);
        close();

        MoMLParser parser = new MoMLParser();
        try {
            Tableau tableau = getFrameController().getConfiguration().openModel(
                    parser.parse(moml));
            Frame frame = tableau.getFrame();
            if (modified && (frame instanceof TableauFrame)) {
                ((TableauFrame) tableau.getFrame()).setModified(true);
            }
        } catch (Exception e) {
            MessageHandler.error("Cannot open default tableau for the "
                    + "model.", e);
        }
    }

    private void _transform() {
        _beginTransform();
        try {
            GraphTransformer.transform(_rule, _results.get(_currentPosition));
        } catch (TransformationException e) {
            MessageHandler.error("Unable to transform model.", e);
        }
        _finishTransform();
    }

    private void _transformAll() {
        _beginTransform();
        try {
            GraphTransformer.transform(_rule, _results);
        } catch (TransformationException e) {
            MessageHandler.error("Unable to transform model.", e);
        }
        _finishTransform();
    }

    private static final Color _HIGHLIGHT_COLOR = new Color(96, 32, 128, 128);
    
    private static final float _HIGHLIGHT_PADDING = 3.0f;
    
    private static final float _HIGHLIGHT_THICKNESS = 6.0f;

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

    private JButton _transformAllButton;

    private JMenuItem _transformAllItem;

    private JButton _transformButton;

    private JMenuItem _transformItem;

    private boolean _transformed = false;

    private class CloseAction extends FigureAction {

        public CloseAction() {
            super("Close Match Window");

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
        }

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

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._transform();
            } else {
                _transform();
            }
        }

    }

    private class TransformAllAction extends FigureAction {

        public TransformAllAction() {
            super("Transform All");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/transformall.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/transformall_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/transformall_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/transformall_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Transform the current highlighted occurrence "
                    + "(Ctrl+\\)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_BACK_SLASH, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            if (_topFrame != null) {
                _topFrame._transformAll();
            } else {
                _transformAll();
            }
        }

    }
}

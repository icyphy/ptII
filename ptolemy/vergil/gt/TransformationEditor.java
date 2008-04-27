/* A graph editor frame for ptolemy graph transformation models.

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.DefaultDirectoryAttribute;
import ptolemy.actor.gt.DefaultModelAttribute;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.MatchCallback;
import ptolemy.actor.gt.Pattern;
import ptolemy.actor.gt.PatternObjectAttribute;
import ptolemy.actor.gt.Replacement;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.toolbox.FigureAction;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

//////////////////////////////////////////////////////////////////////////
//// GTGraphFrame

/**
 A graph editor frame for ptolemy graph transformation models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see ptolemy.vergil.actor.ActorGraphFrame
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationEditor extends GTFrame implements
        ActionListener, TableModelListener, ValueListener {

    ///////////////////////////////////////////////////////////////////
    ////                          constructors                     ////

    /** Construct a frame associated with the specified case actor.
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
    public TransformationEditor(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified case actor.
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
    public TransformationEditor(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, entity instanceof FSMActor ? defaultLibrary
                : _importActorLibrary(tableau, defaultLibrary));

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("add")) {
            addRow();
        } else if (command.equals("remove")) {
            int[] rows = _table.getSelectedRows();
            if (rows.length > 0) {
                removeRows(rows);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    public void addRow() {
        int index = _tableModel.getRowCount() + 1;
        _tableModel.addRow(new Object[] {
                _createCellPanel(Integer.toString(index)),
                _createCellPanel(""), _createCellPanel("") });
    }

    public void cancelFullScreen() {
        if (!getFrameController().hasTabs()) {
            super.cancelFullScreen();
            return;
        }

        _screen.dispose();

        // Put the component back into the original window.
        _splitPane.setRightComponent(_getRightComponent());
        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        tabbedPane.add(_fullScreenComponent, _selectedIndexBeforeFullScreen);
        tabbedPane.setSelectedIndex(_selectedIndexBeforeFullScreen);

        // Restore association with the graph panner.
        if (_fullScreenComponent instanceof JGraph) {
            _graphPanner.setCanvas((JGraph) _fullScreenComponent);
        } else {
            _graphPanner.setCanvas(null);
        }

        _fullScreenComponent.removeKeyListener(getFrameController());
        if (_selectedIndexBeforeFullScreen == 2) {
            _setOrUnsetKeyListenersForAllComponents(
                    (JPanel) _fullScreenComponent, false);
        }
        pack();
        show();
        GraphicalMessageHandler.setContext(_previousDefaultContext);
        toFront();
        _getRightComponent().requestFocus();
    }

    public void copy() {
        if (!getFrameController().isTableActive()) {
            CompositeEntity model = getFrameController().getActiveModel();
            if (GTTools.isInPattern(model)) {
                _setOrClearPatternObjectAttributes(model, true,
                        _getSelectionSet());
                super.copy();
                _setOrClearPatternObjectAttributes(model, false,
                        _getSelectionSet());
            } else {
                super.copy();
            }
        }
    }

    public void delete() {
        if (!getFrameController().isTableActive()) {
            super.delete();
        }
    }

    public void fullScreen() {
        if (!getFrameController().hasTabs()) {
            super.fullScreen();
            return;
        }

        _screen = new JDialog();
        _screen.getContentPane().setLayout(new BorderLayout());

        // Set to full-screen size.
        Toolkit toolkit = _screen.getToolkit();
        int width = toolkit.getScreenSize().width;
        int height = toolkit.getScreenSize().height;
        _screen.setSize(width, height);

        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        _selectedIndexBeforeFullScreen = tabbedPane.getSelectedIndex();
        _fullScreenComponent = tabbedPane.getSelectedComponent();
        _screen.setUndecorated(true);
        _screen.getContentPane().add(_fullScreenComponent, BorderLayout.CENTER);

        // NOTE: Have to avoid the following, which forces the
        // dialog to resize the preferred size of _jgraph, which
        // nullifies the call to setSize() above.
        // _screen.pack();
        _screen.setVisible(true);

        // Make the new screen the default context for modal messages.
        Component _previousDefaultContext = GraphicalMessageHandler
                .getContext();
        GraphicalMessageHandler.setContext(_screen);

        // NOTE: As usual with swing, what the UI does is pretty
        // random, and doesn't correlate much with the documentation.
        // The following two lines do not work if _screen is a
        // JWindow instead of a JDialog.  There is no apparent
        // reason for this, but this is why we use JDialog.
        // Unfortunately, apparently the JDialog does not appear
        // in the Windows task bar.
        _screen.toFront();
        _fullScreenComponent.requestFocus();

        _screen.setResizable(false);

        _fullScreenComponent.addKeyListener(getFrameController());
        if (_selectedIndexBeforeFullScreen == 2) {
            // The correspondence table is selected before full screen.
            // Set the key listener for the table.
            _setOrUnsetKeyListenersForAllComponents(
                    (JPanel) _fullScreenComponent, true);
        }

        // Remove association with the graph panner.
        _graphPanner.setCanvas(null);

        setVisible(false);
        GraphicalMessageHandler.setContext(_previousDefaultContext);
    }

    public void paste() {
        if (!getFrameController().isTableActive()) {
            super.paste();

            CompositeEntity model = getFrameController().getActiveModel();
            if (GTTools.isInPattern(model)) {
                // FIXME: modify only newly created entities and relations.
                _setOrClearPatternObjectAttributes(model, false, null);
            } else {
                _refreshTable();
            }
        }
    }

    public void redo() {
        if (getFrameController().isTableActive() && _cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        super.redo();
    }

    public void removeRows(int[] rows) {
        if (_cellEditor != null) {
            // Stop editing so that the current edit value will be recorded in
            // the undo history.
            _cellEditor.stopCellEditing();
        }
        TransformationRule transformer = (TransformationRule) getModel();
        CompositeActorMatcher replacement = transformer.getReplacement();
        List<ComponentEntity> entities = new LinkedList<ComponentEntity>();
        boolean needRefresh = false;
        for (int i = 0; i < rows.length; i++) {
            String replacementName = _getCellEditorValue((JPanel) _tableModel
                    .getValueAt(rows[i], 2));
            ComponentEntity entity = replacement.getEntity(replacementName);
            if (entity == null) {
                needRefresh = true;
            } else {
                entities.add(entity);
            }
        }
        int i = 0;
        for (ComponentEntity entity : entities) {
            _setPatternObject(entity, "", i++ > 0);
        }
        if (entities.isEmpty() && needRefresh) {
            _refreshTable();
        }
    }

    public void tableChanged(TableModelEvent event) {
        if (event.getType() != TableModelEvent.UPDATE) {
            return;
        }

        int row = event.getFirstRow();
        int column = event.getColumn();
        if (column != TableModelEvent.ALL_COLUMNS && row == event.getLastRow()) {
            // Get the value in the transformer's correspondence attribute.
            TransformationRule transformer = (TransformationRule) getModel();
            Pattern pattern = transformer.getPattern();
            Replacement replacement = transformer.getReplacement();
            String newValue = _getCellEditorValue((JPanel) _tableModel
                    .getValueAt(row, column));
            String previousString = _cellEditor.getPreviousString();
            if (previousString.equals(newValue)) {
                return;
            }

            if (column == 1) {
                String patternObjectName = newValue;
                if (patternObjectName.length() > 0) {
                    NamedObj patternObject = pattern
                            .getEntity(patternObjectName);
                    if (patternObject == null) {
                        patternObject = pattern.getRelation(patternObjectName);
                    }
                    if (patternObject == null) {
                        String message = "Entity or relation with name \""
                                + patternObjectName
                                + "\" cannot be found in the pattern of the "
                                + "transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }
                }

                String replacementObjectName = _getCellEditorValue((JPanel) _tableModel
                        .getValueAt(row, 2));
                if (replacementObjectName.length() > 0) {
                    // Updated the pattern object.
                    NamedObj replacementObject = replacement
                            .getEntity(replacementObjectName);
                    if (replacementObject == null) {
                        replacementObject = replacement
                                .getRelation(replacementObjectName);
                    }

                    if (replacementObject == null) {
                        String message = "Entity or relation with name \""
                                + replacementObjectName
                                + "\" cannot be found in the replacement of the "
                                + "transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    PatternObjectAttribute attribute = GTTools
                            .getPatternObjectAttribute(replacementObject);
                    if (attribute == null) {
                        try {
                            attribute = new PatternObjectAttribute(
                                    replacementObject, "patternObject");
                        } catch (KernelException e) {
                            throw new KernelRuntimeException(e, "Unable to "
                                    + "create patternObject attribute.");
                        }
                    }
                    if (!attribute.getExpression().equals(patternObjectName)) {
                        _setPatternObject(replacementObject, patternObjectName,
                                false);
                    }
                }

            } else if (column == 2) {
                String replacementObjectName = newValue;
                if (replacementObjectName.length() > 0) {
                    NamedObj replacementObject = replacement
                            .getEntity(replacementObjectName);
                    if (replacementObject == null) {
                        replacementObject = replacement
                                .getRelation(replacementObjectName);
                    }

                    if (replacementObject == null) {
                        String message = "Entity or relation with name \""
                                + replacementObjectName
                                + "\" cannot be found in the replacement of the "
                                + "transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    PatternObjectAttribute attribute = GTTools
                            .getPatternObjectAttribute(replacementObject);
                    if (attribute == null) {
                        String message = "Entity or relation with name \""
                                + replacementObject
                                + "\" in the replacement part of the "
                                + "transformation rule does not have a "
                                + "\"patternObject\" attribute.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    _cellEditor.setPreviousString(replacementObjectName);
                    String patternObjectName = _getCellEditorValue((JPanel) _tableModel
                            .getValueAt(row, 1));

                    if (previousString.length() > 0) {
                        NamedObj previousObject = replacement
                                .getEntity(previousString);
                        _setPatternObject(previousObject, "", false);
                    }
                    _setPatternObject(replacementObject, patternObjectName,
                            true);
                }
            }
        }
    }

    public void undo() {
        if (getFrameController().isTableActive() && _cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        super.undo();
    }

    public void valueChanged(Settable settable) {
        if (_cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        _refreshTable();
    }

    public void zoom(double factor) {
        if (!getFrameController().isTableActive()) {
            super.zoom(factor);
        }
    }

    public void zoomFit() {
        if (!getFrameController().isTableActive()) {
            super.zoomFit();
        }
    }

    public void zoomReset() {
        if (!getFrameController().isTableActive()) {
            super.zoomReset();
        }
    }

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();

        _ruleMenu = new JMenu("Rule");
        _ruleMenu.setMnemonic(KeyEvent.VK_R);
        _menubar.add(_ruleMenu);

        SingleMatchAction singleMatchAction = new SingleMatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, singleMatchAction);

        BatchMatchAction batchMatchAction = new BatchMatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, batchMatchAction);

        _ruleMenu.addSeparator();

        LayoutAction layoutAction = new LayoutAction();
        GUIUtilities.addMenuItem(_ruleMenu, layoutAction);

        GraphController controller = (GraphController) _getGraphController();
        if (controller instanceof RunnableGraphController) {
            ((RunnableGraphController) controller).addToMenuAndToolbar(
                    _ruleMenu, _toolbar);
        }
        _removeUnusedToolbarButtons();

        GUIUtilities.addToolBarButton(_toolbar, singleMatchAction);
        GUIUtilities.addToolBarButton(_toolbar, batchMatchAction);
    }

    protected JComponent _createRightComponent(NamedObj entity) {
        JComponent component = super._createRightComponent(entity);
        if (component instanceof JTabbedPane) {
            _createTable((TransformationRule) entity);
        }
        return component;
    }

    /** The case menu. */
    protected JMenu _ruleMenu;

    private JPanel _createCellPanel(String value) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField textField = new JTextField(value, SwingConstants.CENTER);
        textField.setBorder(_EMPTY_BORDER);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setOpaque(false);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private void _createTable(TransformationRule transformer) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Correspondence");

        _tableModel = new DefaultTableModel(new Object[] { "",
                "Pattern Entity", "Replacement Entity" }, 0) {
            public boolean isCellEditable(int row, int column) {
                if (column == 0) {
                    return false;
                } else {
                    return super.isCellEditable(row, column);
                }
            }
        };

        _table = new JTable(_tableModel);
        _table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _table.setEnabled(true);
        _table.setRowHeight(22);
        _table.setSelectionBackground(_SELECTED_COLOR);
        _table.setSelectionForeground(Color.BLACK);

        TableColumnModel model = _table.getColumnModel();
        TableColumn indexColumn = model.getColumn(0);
        indexColumn.setMinWidth(10);
        indexColumn.setPreferredWidth(15);
        indexColumn.setMaxWidth(30);
        DefaultTableCellRenderer indexRenderer = new DefaultTableCellRenderer();
        indexRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        _cellEditor = new CellEditor();
        for (int i = 0; i < 3; i++) {
            TableColumn column = model.getColumn(i);
            column.setCellRenderer(_cellEditor);
            if (i > 0) {
                column.setCellEditor(_cellEditor);
            }
        }

        JTableHeader header = _table.getTableHeader();
        header.setFont(new Font("Dialog", Font.BOLD, 11));
        header.setForeground(Color.BLUE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 25));

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header
                .getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBorder(new EtchedBorder());
        JButton addButton = new JButton("add");
        addButton.addActionListener(this);
        JButton removeButton = new JButton("remove");
        removeButton.addActionListener(this);
        buttonsPanel.add(addButton);
        buttonsPanel.add(removeButton);

        panel.add(header, BorderLayout.NORTH);
        panel.add(_table, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = getFrameController().getTabbedPane();
        int index = tabbedPane.getComponentCount();
        tabbedPane.add(panel, index);

        _refreshTable();
    }

    private static String _getCellEditorValue(JPanel editorPanel) {
        JTextField textField = (JTextField) editorPanel.getComponent(0);
        return textField.getText();
    }

    private static String _getNameWithinContainer(NamedObj object,
            CompositeEntity container) {
        StringBuffer name = new StringBuffer(object.getName());
        NamedObj parent = object.getContainer();
        while (parent != null && parent != container) {
            name.insert(0, '.');
            name.insert(0, parent.getName());
            parent = parent.getContainer();
        }
        if (parent == null) {
            return null;
        } else {
            return name.toString();
        }
    }

    private TransformationEditor _getToplevelFrame() {
        NamedObj toplevel = getFrameController().getTransformationRule();
        for (Frame frame : getFrames()) {
            if (frame instanceof TransformationEditor) {
                TransformationEditor gtGraphFrame = (TransformationEditor) frame;
                if (gtGraphFrame.getModel() == toplevel) {
                    return gtGraphFrame;
                }
            }
        }
        return null;
    }

    private static LibraryAttribute _importActorLibrary(Tableau tableau,
            LibraryAttribute gtLibrary) {
        if (gtLibrary != null) {
            try {
                Configuration configuration =
                    (Configuration) tableau.toplevel();
                CompositeEntity actorLibrary = (CompositeEntity) configuration
                        .getEntity("actor library");
                CompositeEntity library = gtLibrary.getLibrary();
                Workspace workspace = actorLibrary.workspace();
                try {
                    workspace.getReadAccess();
                    for (Object entityObject : actorLibrary.entityList()) {
                        try {
                            ComponentEntity libraryEntity =
                                (ComponentEntity) entityObject;
                            ComponentEntity entity =
                                (ComponentEntity) libraryEntity.clone(
                                        library.workspace());
                            entity.setContainer(library);
                        } catch (Exception e) {
                            // Ignore this entity in the actor library because
                            // we don't know how to import it.
                        }
                    }
                } finally {
                    workspace.doneReading();
                }

                EntityLibrary utilitiesLibrary =
                    (EntityLibrary) library.getEntity("Utilities");
                for (Object entityObject : utilitiesLibrary.entityList()) {
                    if (entityObject instanceof CompositeActor) {
                        CompositeActor actor = (CompositeActor) entityObject;
                        if (actor.attributeList(GTTableau.Factory.class)
                                .isEmpty()) {
                            new GTTableau.Factory(actor, actor.uniqueName(
                                    "_tableauFactory"));
                        }
                    }
                }

                gtLibrary.setLibrary(library);
            } catch (Exception e) {
                // Ignore, just return a library without any actors or
                // directors.
            }
        }
        return gtLibrary;
    }

    private void _refreshTable() {
        TransformationEditor frame = _getToplevelFrame();
        if (frame._cellEditor != null) {
            frame._cellEditor.stopCellEditing();
        }
        frame._tableModel.removeTableModelListener(this);

        while (frame._tableModel.getRowCount() > 0) {
            frame._tableModel.removeRow(0);
        }

        TransformationRule transformer =
            (TransformationRule) getFrameController().getTransformationRule();
        CompositeActorMatcher replacement = transformer.getReplacement();
        _refreshTable(frame, replacement, 1, replacement);

        frame._tableModel.addTableModelListener(this);
    }

    private int _refreshTable(TransformationEditor topLevelFrame,
            CompositeActorMatcher replacement, int index,
            CompositeEntity container) {
        try {
            container.workspace().getReadAccess();
            Collection<?> objectCollection = new CombinedCollection<Object>(
                    new Collection<?>[] { container.entityList(),
                            container.relationList() });
            for (Object entityObject : objectCollection) {
                NamedObj object = (NamedObj) entityObject;
                PatternObjectAttribute attribute = GTTools
                        .getPatternObjectAttribute(object);
                if (attribute != null) {
                    attribute.addValueListener(this);
                    String patternObject = attribute.getExpression();
                    if (patternObject.length() != 0) {
                        String name = _getNameWithinContainer(object, replacement);
                        topLevelFrame._tableModel.addRow(new Object[] {
                                _createCellPanel(Integer.toString(index++)),
                                _createCellPanel(patternObject),
                                _createCellPanel(name) });
                    }
                }
                if (object instanceof CompositeEntity) {
                    index = _refreshTable(topLevelFrame, replacement, index,
                            (CompositeEntity) object);
                }
            }
        } finally {
            container.workspace().doneReading();
        }
        return index;
    }

    private void _removeUnusedToolbarButtons() {
        Component[] components = _toolbar.getComponents();
        int i = 0;
        for (Component component : components) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                Action action = button.getAction();
                String className = action.getClass().getName();
                if (className.endsWith("$RunModelAction")
                        || className.endsWith("$PauseModelAction")
                        || className.endsWith("$StopModelAction")) {
                    _toolbar.remove(i);
                    continue;
                }
            }
            i++;
        }
    }

    private void _setCellEditorValue(JPanel editorPanel, String value) {
        JTextField textField = (JTextField) editorPanel.getComponent(0);
        textField.setText(value);
    }

    private void _setCellString(int row, int column, String cellString) {
        JPanel panel = (JPanel) _tableModel.getValueAt(row, column);
        _setCellEditorValue(panel, cellString);
        _tableModel.fireTableCellUpdated(row, column);
    }

    private void _setOrClearPatternObjectAttributes(CompositeEntity container,
            boolean isSet, Collection<?> filter) {
        try {
            Collection<?> objectCollection;
            if (filter == null) {
                container.workspace().getReadAccess();
                objectCollection = GTTools.getChildren(container, true, true,
                        true, true);
            } else {
                objectCollection = filter;
            }
            for (Object objectObject : objectCollection) {
                NamedObj object = (NamedObj) objectObject;
                PatternObjectAttribute patternObject = GTTools
                        .getPatternObjectAttribute(object);
                if (isSet) {
                    if (patternObject == null) {
                        patternObject = new PatternObjectAttribute(object,
                                "patternObject");
                    }
                    String name = _getNameWithinContainer(object,
                            getFrameController().getTransformationRule()
                            .getPattern());
                    patternObject.setPersistent(true);
                    patternObject.setExpression(name);
                } else if (patternObject != null) {
                    patternObject.setPersistent(false);
                    patternObject.setExpression("");
                }
                if (object instanceof CompositeEntity) {
                    _setOrClearPatternObjectAttributes(
                            (CompositeEntity) object, isSet, null);
                }
            }
        } catch (KernelException e) {
            throw new KernelRuntimeException(e, "Cannot set attribute.");
        } finally {
            if (filter == null) {
                container.workspace().doneReading();
            }
        }
    }

    private void _setOrUnsetKeyListenersForAllComponents(Container container,
            boolean isSet) {
        for (Component component : container.getComponents()) {
            if (isSet) {
                component.addKeyListener(getFrameController());
            } else {
                component.removeKeyListener(getFrameController());
            }
            if (component instanceof Container) {
                _setOrUnsetKeyListenersForAllComponents((Container) component,
                        isSet);
            }
        }
    }

    private void _setPatternObject(NamedObj replacementObject,
            String patternObjectName, boolean mergeWithPrevious) {
        String moml = "<property name=\"patternObject\" value=\""
                + patternObjectName + "\"/>";
        MoMLChangeRequest request = new MoMLChangeRequest(this,
                replacementObject, moml);
        request.setUndoable(true);
        request.setMergeWithPreviousUndo(mergeWithPrevious);
        replacementObject.requestChange(request);
    }

    private void _showTableError(String message, final int row,
            final int column, final String previousString) {
        String[] options = new String[] { "Edit", "Revert" };
        int selected = JOptionPane.showOptionDialog(null, message,
                "Validation Error", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[1]);
        if (selected == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (_table.isEditing()
                            && (_table.getEditingRow() != row || _table
                                    .getEditingColumn() != column)) {
                        _cellEditor.cancelCellEditing();
                    }
                    _table.editCellAt(row, column);
                    _cellEditor.setPreviousString(previousString);
                }
            });
        } else if (selected == 1) {
            _setCellString(row, column, previousString);
        }
    }

    private static final Border _EMPTY_BORDER = BorderFactory
            .createEmptyBorder();

    private static final Color _SELECTED_COLOR = new Color(230, 230, 255);

    private CellEditor _cellEditor;

    private Component _fullScreenComponent;

    private Component _previousDefaultContext;

    private JDialog _screen;

    private int _selectedIndexBeforeFullScreen;

    private JTable _table;

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private DefaultTableModel _tableModel;

    private class BatchMatchAction extends MatchAction {

        public BatchMatchAction() {
            super("Match Models in a Directory");

            _attribute = new DefaultDirectoryAttribute((Workspace) null);
            _attribute.directory.setBaseDirectory(URIAttribute
                    .getModelURI(getModel()));

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/batchmatch.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/batchmatch_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/batchmatch_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/batchmatch_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match Ptolemy models in a directory (Ctrl+2)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_2, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            new MultipleViewController();
        }

        private File[] _getModelFiles() {
            TransformationRule rule =
                getFrameController().getTransformationRule();
            Pattern pattern = rule.getPattern();
            DefaultDirectoryAttribute attribute = (DefaultDirectoryAttribute) pattern
                    .getAttribute("DefaultDirectory");
            File directoryFile = null;
            String fileFilter = "";
            boolean subdirs = true;
            if (attribute != null) {
                try {
                    directoryFile = attribute.directory.asFile();
                    fileFilter = attribute.fileFilter.getExpression();
                    subdirs = ((BooleanToken) attribute.subdirs.getToken())
                            .booleanValue();
                } catch (IllegalActionException e) {
                    throw new KernelRuntimeException(e,
                            "Unable to get boolean " + "token.");
                }
            }

            if (directoryFile == null) {
                ComponentDialog dialog = new ComponentDialog(
                        TransformationEditor.this, "Select Model Directory",
                        new Configurer(_attribute));
                if (dialog.buttonPressed().equalsIgnoreCase("OK")) {
                    try {
                        directoryFile = _attribute.directory.asFile();
                        fileFilter = _attribute.fileFilter.getExpression();
                        subdirs = ((BooleanToken) _attribute.subdirs.getToken())
                                .booleanValue();
                    } catch (IllegalActionException e) {
                        throw new KernelRuntimeException(e, "Unable to get "
                                + "boolean token.");
                    }
                }
            }

            if (directoryFile != null && !directoryFile.exists()) {
                MessageHandler.error("Directory " + directoryFile.getPath()
                        + " does not exist.");
                return null;
            }

            if (directoryFile == null) {
                return null;
            } else {
                File[] files = _listFiles(directoryFile, subdirs, fileFilter);
                return files;
            }
        }

        private File[] _listFiles(File directory, boolean includeSubdir,
                String fileFilter) {
            ModelFileFilter collector = new ModelFileFilter(includeSubdir,
                    fileFilter);
            directory.list(collector);
            List<File> files = collector._files;
            Collections.sort(files, new FileComparator());
            return files.toArray(new File[files.size()]);
        }

        private DefaultDirectoryAttribute _attribute;

        private/*static*/class ModelFileFilter implements FilenameFilter {
            // FindBugs suggests making this class static so as to decrease
            // the size of instances and avoid dangling references.
            // However, jdk1.5.0_11 says: modifier static not allowed here
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                boolean isDirectory = _includeSubdir && file.isDirectory();
                boolean isFile = file.isFile()
                        && (_pattern == null ? name.toLowerCase().endsWith(
                                ".xml") : _pattern.matcher(name).matches());
                if (isDirectory) {
                    file.list(this);
                } else if (isFile) {
                    _files.add(file);
                }
                return false;
            }

            ModelFileFilter(boolean includeSubdir, String fileFilter) {
                _includeSubdir = includeSubdir;
                if (!fileFilter.equals("")) {
                    _pattern = java.util.regex.Pattern
                            .compile(_escape(fileFilter));
                }
            }

            private String _escape(String string) {
                String escaped = _ESCAPER.matcher(string).replaceAll("\\\\$1");
                return escaped.replaceAll("\\\\\\*", ".*").replaceAll(
                        "\\\\\\?", ".?");
            }

            private final java.util.regex.Pattern _ESCAPER = java.util.regex.Pattern
                    .compile("([^a-zA-z0-9])");

            private List<File> _files = new LinkedList<File>();

            private boolean _includeSubdir;

            private java.util.regex.Pattern _pattern;
        }

        private class MultipleViewController extends ViewController {

            public void windowDeactivated(WindowEvent e) {
                Window window = e.getWindow();
                if (!(window instanceof MatchResultViewer)) {
                    return;
                }

                MatchResultViewer viewer = (MatchResultViewer) window;
                if (viewer.isVisible()) {
                    return;
                }

                MatchResultViewer.FileSelectionStatus status = viewer
                        .getFileSelectionStatus();
                viewer.clearFileSelectionStatus();
                switch (status) {
                case PREVIOUS:
                    _index = _previousIndex;
                    _viewCurrentModel();
                    break;
                case NEXT:
                    _index = _nextIndex;
                    _viewCurrentModel();
                    break;
                default:
                    _close();
                }
            }

            protected void _close() {
                super._close();
                if (_viewers != null) {
                    for (int i = 0; i < _viewers.length; i++) {
                        MatchResultViewer viewer = _viewers[i];
                        if (viewer != null) {
                            viewer.removeWindowListener(this);
                            viewer.setModified(false);
                            viewer.close();
                            _viewers[i] = null;
                        }
                    }
                    _viewers = null;
                }
            }

            @SuppressWarnings("unchecked")
            MultipleViewController() {
                _files = _getModelFiles();
                if (_files == null) {
                    return;
                }

                _viewers = new MatchResultViewer[_files.length];
                _models = new CompositeEntity[_files.length];
                _allResults = (List<MatchResult>[]) new List[_files.length];

                try {
                    _index = _findNextMatch(-1);
                    if (_index < 0) {
                        MessageHandler.message("No match found.");
                        return;
                    }
                    _viewCurrentModel();
                } catch (Throwable throwable) {
                    _handleErrors(throwable);
                }
            }

            private int _findNextMatch(int index) throws MalformedURLException,
                    Exception {
                for (int i = index + 1; i < _files.length; i++) {
                    List<MatchResult> currentResult = _allResults[i];
                    if (currentResult == null) {
                        CompositeEntity model = _getModel(_files[i]);
                        currentResult = _getMatchResult(model);

                        _models[i] = model;
                        _allResults[i] = currentResult;

                        if (!currentResult.isEmpty()) {
                            return i;
                        }
                    } else if (!currentResult.isEmpty()) {
                        return i;
                    }
                }
                return -1;
            }

            private int _findPreviousMatch(int index) {
                for (int i = index - 1; i >= 0; i--) {
                    if (!_allResults[i].isEmpty()) {
                        return i;
                    }
                }
                return -1;
            }

            private void _viewCurrentModel() {
                try {
                    _previousIndex = _findPreviousMatch(_index);
                    _nextIndex = _findNextMatch(_index);

                    if (_viewers[_index] != null) {
                        _viewers[_index].setVisible(true);
                    } else {
                        _viewers[_index] = _showViewer(_models[_index],
                                _allResults[_index], _files[_index]
                                        .getCanonicalPath());
                        _viewers[_index].setBatchMode(true);
                        _viewers[_index]
                                .setPreviousFileEnabled(_previousIndex >= 0);
                        _viewers[_index].setNextFileEnabled(_nextIndex >= 0);
                        _viewers[_index].addWindowListener(this);
                    }
                } catch (Throwable throwable) {
                    _handleErrors(throwable);
                }
            }

            private List<MatchResult>[] _allResults;

            private File[] _files;

            private int _index;

            private CompositeEntity[] _models;

            private int _nextIndex;

            private int _previousIndex;

            private MatchResultViewer[] _viewers;

        }
    }

    private static class CellEditor extends CellPanelEditor {

        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            JPanel panel = (JPanel) super.getTableCellEditorComponent(table,
                    value, isSelected, row, column);
            _previousString = _getCellEditorValue(panel);
            return panel;
        }

        private String getPreviousString() {
            return _previousString;
        }

        private void setPreviousString(String previousString) {
            _previousString = previousString;
        }

        private String _previousString;
    }

    private static class FileComparator implements Comparator<File>,
    Serializable {

        public int compare(File file1, File file2) {
            return file1.getAbsolutePath().compareTo(file2.getAbsolutePath());
        }

    }

    /** Action to automatically layout the graph.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 6.1
     @see ActorGraphFrame.LayoutAction
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class LayoutAction extends AbstractAction {

        /** Create a new action to automatically lay out the graph. */
        public LayoutAction() {
            super("Automatic Layout");
            putValue("tooltip", "Layout the Graph (Ctrl+T)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_T, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        /** Lay out the graph. */
        public void actionPerformed(ActionEvent e) {
            try {
                layoutGraph();
            } catch (Exception ex) {
                MessageHandler.error("Layout failed", ex);
            }
        }
    }

    private class MatchAction extends FigureAction {

        protected List<MatchResult> _getMatchResult(CompositeEntity model) {
            TransformationRule transformerActor =
                getFrameController().getTransformationRule();
            Pattern pattern = transformerActor.getPattern();
            MatchResultRecorder recorder = new MatchResultRecorder();
            _matcher.setMatchCallback(recorder);
            _matcher.match(pattern, model);
            return recorder.getResults();
        }

        protected CompositeEntity _getModel(File file)
                throws MalformedURLException, Exception {
            if (!file.exists()) {
                MessageHandler.error("Model file " + file.getPath()
                        + " does not exist.");
                return null;
            }

            _parser.reset();
            InputStream stream = file.toURI().toURL().openStream();
            CompositeEntity model = (CompositeEntity) _parser.parse(null,
                    stream);
            return model;
        }

        protected MatchResultViewer _showViewer(CompositeEntity model,
                List<MatchResult> results, String sourceFileName)
                throws IllegalActionException, NameDuplicationException {

            MatchResultViewer._setTableauFactory(this, model);
            Configuration configuration =
                getFrameController().getConfiguration();
            if (configuration == null) {
                throw new InternalErrorException("Cannot get configuration.");
            }

            Tableau tableau = configuration.openModel(model);
            MatchResultViewer viewer = (MatchResultViewer) tableau.getFrame();
            viewer.setMatchResult(results);
            viewer.setSourceFileName(sourceFileName);
            viewer.setTransformationRule(
                    getFrameController().getTransformationRule());
            return viewer;
        }

        MatchAction(String name) {
            super(name);
        }

        private GraphMatcher _matcher = new GraphMatcher();

        private MoMLParser _parser = new MoMLParser();
    }

    private static class MatchResultRecorder implements MatchCallback {

        public boolean foundMatch(GraphMatcher matcher) {
            _results.add((MatchResult) matcher.getMatchResult().clone());
            return false;
        }

        public List<MatchResult> getResults() {
            return _results;
        }

        private List<MatchResult> _results = new LinkedList<MatchResult>();
    }

    private class SingleMatchAction extends MatchAction {

        public SingleMatchAction() {
            super("Match Model");

            _attribute = new DefaultModelAttribute((Workspace) null);
            ((FileParameter) _attribute.parameter)
                    .setBaseDirectory(URIAttribute.getModelURI(getModel()));

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/match.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/match_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/match_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/match_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match a Ptolemy model in an external file "
                    + "(Ctrl+1)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            new SingleViewController();
        }

        private File _getModelFile() {
            File modelFile = null;
            try {
                TransformationRule rule =
                    getFrameController().getTransformationRule();
                Pattern pattern = rule.getPattern();
                DefaultModelAttribute attribute = (DefaultModelAttribute) pattern
                        .getAttribute("DefaultModel");
                if (attribute != null) {
                    FileParameter parameter = (FileParameter) attribute.parameter;
                    if (parameter.getExpression() != null) {
                        modelFile = parameter.asFile();
                    }
                }

                if (modelFile == null) {
                    ComponentDialog dialog = new ComponentDialog(
                            TransformationEditor.this, "Select Model File",
                            new Configurer(_attribute));
                    if (dialog.buttonPressed().equalsIgnoreCase("OK")) {
                        modelFile = ((FileParameter) _attribute.parameter)
                                .asFile();
                    }
                }

                if (modelFile != null && !modelFile.exists()) {
                    MessageHandler.error("Model file " + modelFile.getPath()
                            + " does not exist.");
                    return null;
                }

                return modelFile;
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(e, "Cannot obtain model file.");
            }
        }

        private DefaultModelAttribute _attribute;

        private class SingleViewController extends ViewController {

            public void windowDeactivated(WindowEvent e) {
                Window window = e.getWindow();
                if (!(window instanceof MatchResultViewer)) {
                    return;
                }

                MatchResultViewer viewer = (MatchResultViewer) window;
                if (viewer.isVisible()) {
                    return;
                }

                _close();
            }

            protected void _close() {
                super._close();
                if (_viewer != null) {
                    _viewer.removeWindowListener(this);
                    _viewer.close();
                    _viewer = null;
                }
            }

            SingleViewController() {
                try {
                    File file = _getModelFile();
                    if (file == null) {
                        return;
                    }

                    CompositeEntity model = _getModel(file);
                    if (model == null) {
                        return;
                    }

                    List<MatchResult> results = _getMatchResult(model);
                    if (results.isEmpty()) {
                        MessageHandler.message("No match found.");
                    } else {
                        _viewer = _showViewer(model, results, file
                                .getCanonicalPath());
                        _viewer.addWindowListener(this);
                    }
                } catch (MalformedURLException ex) {
                    MessageHandler.error("Unable to obtain URL from the "
                            + "input file name.", ex);
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            }

            private MatchResultViewer _viewer;
        }
    }

    private class ViewController implements WindowListener {

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            Window window = e.getWindow();
            if (window == TransformationEditor.this) {
                _close();
            }
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        protected void _close() {
            removeWindowListener(this);
        }

        protected void _handleErrors(Throwable throwable) {
            if (throwable instanceof MalformedURLException) {
                MessageHandler.error("Unable to obtain URL from the input "
                        + "file name.", throwable);
            } else {
                throw new InternalErrorException(throwable);
            }
        }

        ViewController() {
            addWindowListener(this);
        }
    }
}

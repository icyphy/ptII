/* A graph editor frame for ptolemy graph transformation models.

 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.vergil.gt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

import ptolemy.actor.gt.AtomicActorMatcher;
import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.GraphMatcher;
import ptolemy.actor.gt.MatchCallback;
import ptolemy.actor.gt.PatternEntityAttribute;
import ptolemy.actor.gt.SingleRuleTransformer;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.FileParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorInstanceController;
import ptolemy.vergil.kernel.PortDialogAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.graph.JGraph;
import diva.gui.ExtensionFileFilter;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// GTRuleGraphFrame

/**
 A graph editor frame for ptolemy graph transformation models.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @see ptolemy.vergil.actor.ActorGraphFrame
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTRuleGraphFrame extends AbstractGTFrame implements ActionListener,
TableModelListener, ValueListener {

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
    public GTRuleGraphFrame(CompositeEntity entity, Tableau tableau) {
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
    public GTRuleGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        // Override the default help file.
        // FIXME
        // helpFile = "ptolemy/configs/doc/vergilFsmEditorHelp.htm";
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

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

    public void addRow() {
        int index = _tableModel.getRowCount() + 1;
        _tableModel.addRow(new Object[] {
                _createCellPanel(Integer.toString(index)), _createCellPanel(""),
                _createCellPanel("")
        });
    }

    public void cancelFullScreen() {
        if (!hasTabs()) {
            super.cancelFullScreen();
            return;
        }

        _screen.dispose();

        // Put the component back into the original window.
        _splitPane.setRightComponent(_getRightComponent());
        JTabbedPane tabbedPane = _getTabbedPane();
        tabbedPane.add(_fullScreenComponent, _selectedIndexBeforeFullScreen);
        tabbedPane.setSelectedIndex(_selectedIndexBeforeFullScreen);

        // Restore association with the graph panner.
        if (_fullScreenComponent instanceof JGraph) {
            _graphPanner.setCanvas((JGraph) _fullScreenComponent);
        } else {
            _graphPanner.setCanvas(null);
        }

        _fullScreenComponent.removeKeyListener(this);
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
        if (!_isTableActive()) {
            CompositeEntity model = getActiveModel();
            if (_isInPattern(model)) {
                _setOrClearPatternEntityAttributes(model, true);
                super.copy();
                _setOrClearPatternEntityAttributes(model, false);
            } else {
                super.copy();
            }
        }
    }

    public void delete() {
        if (!_isTableActive()) {
            super.delete();
        }
    }

    public void fullScreen() {
        if (!hasTabs()) {
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

        JTabbedPane tabbedPane = _getTabbedPane();
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
        Component _previousDefaultContext =
            GraphicalMessageHandler.getContext();
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

        _fullScreenComponent.addKeyListener(this);
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
        if (!_isTableActive()) {
            super.paste();
            _refreshTable();
        }
    }

    public void redo() {
        if (_isTableActive() && _cellEditor != null) {
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
        SingleRuleTransformer transformer = (SingleRuleTransformer) getModel();
        CompositeActorMatcher replacement = transformer.getReplacement();
        List<ComponentEntity> entities = new LinkedList<ComponentEntity>();
        boolean needRefresh = false;
        for (int i = 0; i < rows.length; i++) {
            String replacementName = _getCellEditorValue(
                    (JPanel) _tableModel.getValueAt(rows[i], 2));
            ComponentEntity entity = replacement.getEntity(replacementName);
            if (entity == null) {
                needRefresh = true;
            } else {
                entities.add(entity);
            }
        }
        int i = 0;
        for (ComponentEntity entity : entities) {
            _setPatternEntity(entity, "", i++ > 0);
        }
        if (entities.isEmpty() && needRefresh) {
            _refreshTable();
        }
    }

    public void tableChanged(TableModelEvent e) {
        if (e.getType() != TableModelEvent.UPDATE) {
            return;
        }

        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column != TableModelEvent.ALL_COLUMNS && row == e.getLastRow()) {
            // Get the value in the transformer's correspondence attribute.
            SingleRuleTransformer transformer =
                (SingleRuleTransformer) getModel();
            String newValue = _getCellEditorValue(
                    (JPanel) _tableModel.getValueAt(row, column));
            String previousString = _cellEditor.getPreviousString();

            if (column == 1) {
                String patternEntityName = newValue;
                if (patternEntityName.length() != 0) {
                    ComponentEntity patternEntity =
                        (ComponentEntity) transformer.getPattern().getEntity(
                                patternEntityName);
                    if (patternEntity == null) {
                        String message = "Entity with name \""
                            + patternEntityName
                            + "\" cannot be found in the pattern part of "
                            + "the transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }
                }

                String replacementEntityName = _getCellEditorValue(
                        (JPanel) _tableModel.getValueAt(row, 2));
                if (replacementEntityName.length() == 0) {
                    return;
                }

                // Updated the pattern object.
                ComponentEntity replacementEntity = (ComponentEntity)
                        transformer.getReplacement().getEntity(
                                replacementEntityName);
                if (!_getPatternEntityAttribute(replacementEntity)
                        .getExpression().equals(patternEntityName)) {
                    _setPatternEntity(replacementEntity, patternEntityName,
                            false);
                }

            } else if (column == 2) {
                String replacementEntityName = _getCellEditorValue(
                        (JPanel) _tableModel.getValueAt(row, 2));
                if (replacementEntityName.length() == 0) {
                    return;
                }

                if (!previousString.equals(replacementEntityName)) {
                    ComponentEntity replacementEntity =
                        (ComponentEntity) transformer.getReplacement()
                                .getEntity(replacementEntityName);
                    if (replacementEntity == null) {
                        String message = "Entity with name \""
                            + replacementEntityName
                            + "\" cannot be found in the replacement part of "
                            + "the transformation rule.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    PatternEntityAttribute attribute =
                        _getPatternEntityAttribute(replacementEntity);
                    if (attribute == null) {
                        String message = "Entity with name \""
                            + replacementEntityName
                            + "\" in the replacement part of the "
                            + "transformation rule does not have a "
                            + "\"patternEntity\" attribute.";
                        _showTableError(message, row, column, previousString);
                        return;
                    }

                    _cellEditor.setPreviousString(replacementEntityName);
                    ComponentEntity previousEntity =
                        (ComponentEntity) transformer.getReplacement()
                                .getEntity(previousString);
                    String patternObject = _getCellEditorValue(
                            (JPanel) _tableModel.getValueAt(row, 1));

                    _setPatternEntity(previousEntity, "", false);
                    _setPatternEntity(replacementEntity, patternObject, true);
                }
            }
        }
    }

    public void undo() {
        if (_isTableActive() && _cellEditor != null) {
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
        if (!_isTableActive()) {
            super.zoom(factor);
        }
    }

    public void zoomFit() {
        if (!_isTableActive()) {
            super.zoomFit();
        }
    }

    public void zoomReset() {
        if (!_isTableActive()) {
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

        MatchAction matchAction = new MatchAction();
        GUIUtilities.addMenuItem(_ruleMenu, matchAction);

        _ruleMenu.addSeparator();

        LayoutAction layoutAction = new LayoutAction();
        GUIUtilities.addMenuItem(_ruleMenu, layoutAction);

        ActorEditorGraphController controller =
            (ActorEditorGraphController) _getGraphController();
        if (hasTabs()) {
            _ruleMenu.addSeparator();
            Action newRelationAction = controller.new NewRelationAction();
            GUIUtilities.addMenuItem(_ruleMenu, newRelationAction);
            GUIUtilities.addToolBarButton(_toolbar, newRelationAction);
        } else {
            controller.addToMenuAndToolbar(_ruleMenu, _toolbar);
            _removeUnusedToolbarButtons();
        }

        GUIUtilities.addToolBarButton(_toolbar, matchAction);
    }

    protected ActorEditorGraphController _createController() {
        return new GTActionGraphController();
    }

    protected JComponent _createRightComponent(NamedObj entity) {
        JComponent component = super._createRightComponent(entity);
        if (component instanceof JTabbedPane) {
            _createTable((SingleRuleTransformer) entity);
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

    private void _createTable(SingleRuleTransformer transformer) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setName("Correspondence");

        _tableModel = new DefaultTableModel(new Object[] {
                "", "Pattern Entity", "Replacement Entity"
        }, 0);

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
        DefaultTableCellRenderer indexRenderer =
            new DefaultTableCellRenderer();
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

        DefaultTableCellRenderer renderer =
            (DefaultTableCellRenderer) header.getDefaultRenderer();
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

        JTabbedPane tabbedPane = _getTabbedPane();
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

    private static PatternEntityAttribute _getPatternEntityAttribute(NamedObj object) {
        Attribute attribute = object.getAttribute("patternEntity");
        if (attribute != null && attribute instanceof PatternEntityAttribute) {
            return (PatternEntityAttribute) attribute;
        } else {
            return null;
        }
    }

    private GTRuleGraphFrame _getToplevelFrame() {
        NamedObj toplevel = getTransformer();
        for (Frame frame : getFrames()) {
            if (frame instanceof GTRuleGraphFrame) {
                GTRuleGraphFrame gtRuleGraphFrame = (GTRuleGraphFrame) frame;
                if (gtRuleGraphFrame.getModel() == toplevel) {
                    return gtRuleGraphFrame;
                }
            }
        }
        return null;
    }

    private boolean _isInPattern(NamedObj object) {
        CompositeActorMatcher pattern = getTransformer().getPattern();
        NamedObj container = object;
        while (container != null && container != pattern) {
            container = container.getContainer();
        }
        return container == pattern;
    }

    private boolean _isTableActive() {
        return hasTabs() && getSelectedIndex() == 2;
    }

    private void _refreshTable() {
        GTRuleGraphFrame frame = _getToplevelFrame();
        if (frame._cellEditor != null) {
            frame._cellEditor.stopCellEditing();
        }
        frame._tableModel.removeTableModelListener(this);

        while (frame._tableModel.getRowCount() > 0) {
            frame._tableModel.removeRow(0);
        }

        SingleRuleTransformer transformer =
            (SingleRuleTransformer) getTransformer();
        CompositeActorMatcher replacement = transformer.getReplacement();
        _refreshTable(frame, replacement, 1, replacement);

        frame._tableModel.addTableModelListener(this);
    }

    private int _refreshTable(GTRuleGraphFrame topLevelFrame,
            CompositeActorMatcher replacement, int index,
            CompositeEntity container) {
       for (Object entityObject : container.entityList()) {
           NamedObj object = (NamedObj) entityObject;
           PatternEntityAttribute attribute =
               _getPatternEntityAttribute(object);
           if (attribute != null) {
               attribute.addValueListener(this);
               String patternEntity = attribute.getExpression();
               if (patternEntity.length() != 0) {
                   String name = _getNameWithinContainer(object, replacement);
                   topLevelFrame._tableModel.addRow(new Object[] {
                           _createCellPanel(Integer.toString(index++)),
                           _createCellPanel(patternEntity),
                           _createCellPanel(name)
                   });
               }
           }
           if (object instanceof CompositeEntity) {
               index = _refreshTable(topLevelFrame, replacement, index,
                       (CompositeEntity) object);
           }
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

    private void _setOrClearPatternEntityAttributes(
            CompositeEntity container, boolean isSet) {
        try {
            for (Object entityObject : container.entityList()) {
                NamedObj object = (NamedObj) entityObject;
                PatternEntityAttribute patternEntity =
                    _getPatternEntityAttribute(object);
                if (patternEntity != null) {
                    if (isSet) {
                        String name = _getNameWithinContainer(object,
                                getTransformer().getPattern());
                        patternEntity.setPersistent(true);
                        patternEntity.setExpression(name);
                    } else {
                        patternEntity.setPersistent(false);
                        patternEntity.setExpression("");
                    }
                }
                if (object instanceof CompositeEntity) {
                    _setOrClearPatternEntityAttributes((CompositeEntity) object,
                            isSet);
                }
            }
        } catch (IllegalActionException e) {
            throw new KernelRuntimeException(e, "Cannot set attribute.");
        }
    }

    private void _setOrUnsetKeyListenersForAllComponents(Container container,
            boolean isSet) {
        for (Component component : container.getComponents()) {
            if (isSet) {
                component.addKeyListener(this);
            } else {
                component.removeKeyListener(this);
            }
            if (component instanceof Container) {
                _setOrUnsetKeyListenersForAllComponents((Container) component,
                        isSet);
            }
        }
    }

    private void _setPatternEntity(ComponentEntity replacementEntity,
            String patternEntityName, boolean mergeWithPrevious) {
        String moml = "<property name=\"patternEntity\" value=\""
            + patternEntityName + "\"/>";
        MoMLChangeRequest request =
            new MoMLChangeRequest(this, replacementEntity, moml);
        request.setUndoable(true);
        request.setMergeWithPreviousUndo(mergeWithPrevious);
        replacementEntity.requestChange(request);
    }

    private void _showTableError(String message, final int row,
            final int column, final String previousString) {
        String[] options = new String[] {"Edit", "Revert"};
        int selected = JOptionPane.showOptionDialog(null, message,
                "Validation Error", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE, null, options, options[1]);
        if (selected == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (_table.isEditing() &&
                            (_table.getEditingRow() != row ||
                             _table.getEditingColumn() != column)) {
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

    private CellEditor _cellEditor;

    private static final Border _EMPTY_BORDER =
        BorderFactory.createEmptyBorder();

    private Component _fullScreenComponent;

    private static final String[] _MATCH_FILE_CHOOSER_BUTTONS = new String[] {
        "Match", "Cancel"
    };

    private String _matchFileName;

    private Component _previousDefaultContext;

    private JDialog _screen;

    private static final Color _SELECTED_COLOR = new Color(230, 230, 255);

    private int _selectedIndexBeforeFullScreen;

    private JTable _table;

    private DefaultTableModel _tableModel;

    private static final long serialVersionUID = 5919681658644668772L;

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private static class CellEditor extends CellPanelEditor {

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
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

        private static final long serialVersionUID = 5226766789270435413L;
    }

    private class GTActionGraphController extends ActorEditorGraphController {

        public class NewRelationAction
        extends ActorEditorGraphController.NewRelationAction {

            public void actionPerformed(ActionEvent e) {
                if (_isTableActive()) {
                    return;
                } else {
                    super.actionPerformed(e);
                }
            }

            private static final long serialVersionUID = 131078285900819894L;

        }

        protected void _createControllers() {
            super._createControllers();
            _entityController = new EntityController();
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
                super(GTActionGraphController.this);

                MenuActionFactory newFactory = new GTMenuActionFactory(
                        _configureMenuFactory, _configureAction);
                _replaceFactory(_menuFactory, _configureMenuFactory,
                        newFactory);
                _configureMenuFactory = newFactory;
            }
        }
    }

    private static class GTMenuActionFactory extends MenuActionFactory {

        public void addAction(Action action, String label) {
            _oldFactory.addAction(action, label);
        }

        public void addActions(Action[] actions, String label) {
            _oldFactory.addActions(actions, label);
        }

        public JMenuItem create(JContextMenu menu, NamedObj object) {
            JMenuItem menuItem = _oldFactory.create(menu, object);
            if (menuItem instanceof JMenu) {
                if (object instanceof AtomicActorMatcher) {
                    JMenu subMenu = (JMenu) menuItem;
                    if (subMenu.getText().equals("Customize")) {
                        Component[] menuItems = subMenu.getMenuComponents();
                        for (Component itemComponent : menuItems) {
                            JMenuItem item = (JMenuItem) itemComponent;
                            if (item.getAction() instanceof PortDialogAction) {
                                // Disable the PortDialogAction from the context
                                // menu.
                                item.setEnabled(false);
                                break;
                            }
                        }
                    }
                }
            }
            return menuItem;
        }

        GTMenuActionFactory(MenuActionFactory oldFactory,
                Action configureAction) {
            super(configureAction);

            _oldFactory = oldFactory;
        }

        private MenuActionFactory _oldFactory;
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

        private static final long serialVersionUID = -31790471585661407L;
    }

    private class MatchAction extends FigureAction {

        public MatchAction() {
            super("Match Model");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/gt/img/match.gif",
                            GUIUtilities.LARGE_ICON },
                    { "/ptolemy/vergil/gt/img/match_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                    { "/ptolemy/vergil/gt/img/match_ov.gif",
                            GUIUtilities.ROLLOVER_SELECTED_ICON },
                    { "/ptolemy/vergil/gt/img/match_on.gif",
                            GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Match a Ptolemy model in an external file");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_1, Toolkit.getDefaultToolkit()
                            .getMenuShortcutKeyMask()));

            _attribute = new Attribute((Workspace) null);

            try {
                _inputModel = new FileParameter(_attribute, "inputModel");
                _inputModel.setDisplayName("Input model");
            } catch (KernelException e) {
                throw new KernelRuntimeException(e, "Unable to create action " +
                        "instance.");
            }
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            MatchFileChooser fileChooser =
                new MatchFileChooser(getFrame(), _attribute);
            if (fileChooser.buttonPressed().equals(
                    _MATCH_FILE_CHOOSER_BUTTONS[0])) {
                _matchFileName = fileChooser.getFileName();
                File input = new File(_matchFileName);
                if (!input.exists()) {
                    MessageHandler.message("Unable to read input model " +
                            _matchFileName + ".");
                    return;
                }

                try {
                    NamedObj toplevel =
                        GTRuleGraphFrame.this.getTableau().toplevel();
                    if (!(toplevel instanceof Configuration)) {
                        throw new InternalErrorException(
                                "Expected top-level to be a Configuration: "
                                        + toplevel.getFullName());
                    }

                    _parser.reset();
                    CompositeEntity model = (CompositeEntity) _parser.parse(
                            null, input.toURI().toURL().openStream());

                    SingleRuleTransformer transformerActor = getTransformer();
                    CompositeActorMatcher matcher =
                        transformerActor.getPattern();
                    MatchResultRecorder recorder = new MatchResultRecorder();
                    _matcher.setMatchCallback(recorder);
                    _matcher.match(matcher, model);
                    List<MatchResult> results = recorder.getResults();
                    if (!results.isEmpty()) {
                        MatchResultViewer._setTableauFactory(this, model);

                        Configuration configuration = (Configuration) toplevel;
                        Tableau tableau = configuration.openModel(model);
                        MatchResultViewer viewer =
                            (MatchResultViewer) tableau.getFrame();

                        viewer.setMatchResult(results);
                    }
                } catch (MalformedURLException ex) {
                    MessageHandler.message("Unable to obtain URL from the " +
                            "input file name.");
                    return;
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            }
        }

        private Attribute _attribute;

        private FileParameter _inputModel;

        private GraphMatcher _matcher = new GraphMatcher();

        private MoMLParser _parser = new MoMLParser();

        private static final long serialVersionUID = -696919249330217870L;
    }

    private class MatchFileChooser extends ComponentDialog
    implements ActionListener {

        public MatchFileChooser(Frame owner, NamedObj target) {
            super(owner, "Choose Input File", new Configurer(target),
                    _MATCH_FILE_CHOOSER_BUTTONS);
        }

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source == _textField) {
                _buttonPressed = _MATCH_FILE_CHOOSER_BUTTONS[0];
                setVisible(false);
            } else if (source == _button) {
                String currentFileName = _textField.getText();
                JFileChooser fileChooser = null;
                if (currentFileName.length() > 0) {
                    File currentFile = new File(currentFileName);
                    File parentFile = currentFile.getParentFile();
                    if (parentFile != null) {
                        fileChooser = new JFileChooser(parentFile);
                    }
                }
                if (fileChooser == null) {
                    fileChooser = new JFileChooser(".");
                }
                fileChooser.setApproveButtonText("Select");

                // FIXME: The following doesn't have any effect.
                fileChooser.setApproveButtonMnemonic('S');

                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setFileFilter(new ExtensionFileFilter("xml"));
                if (fileChooser.showOpenDialog(MatchFileChooser.this)
                        == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    _textField.setText(selectedFile.getPath());
                }
            }
        }

        public String getFileName() {
            return _textField == null ? null : _textField.getText();
        }

        public void pack() {
            super.pack();

            Configurer configurer = (Configurer) contents;
            List<JTextField> textFields = new LinkedList<JTextField>();
            List<JButton> buttons = new LinkedList<JButton>();
            _findComponents(configurer, JTextField.class, textFields);
            _findComponents(configurer, JButton.class, buttons);
            for (JTextField textField : textFields) {
                for (ActionListener listener : textField.getActionListeners()) {
                    textField.removeActionListener(listener);
                }
                textField.addActionListener(this);
                if (_matchFileName != null) {
                    textField.setText(_matchFileName);
                }
                _textField = textField;
            }
            for (JButton button : buttons) {
                if (!button.getText().equals("Browse")) {
                    continue;
                }
                for (ActionListener listener : button.getActionListeners()) {
                    button.removeActionListener(listener);
                }
                button.addActionListener(this);
                _button = button;
            }
        }

        @SuppressWarnings("unchecked")
        private <E extends Component> void _findComponents(Container container,
                Class<? extends E> componentClass, List<E> list) {
            for (Component component : container.getComponents()) {
                if (componentClass.isInstance(component)) {
                    list.add((E) component);
                } else if (component instanceof Container) {
                    _findComponents((Container) component, componentClass,
                            list);
                }
            }
        }

        private JButton _button;

        private JTextField _textField;

        private static final long serialVersionUID = 2369054217750135740L;
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
}

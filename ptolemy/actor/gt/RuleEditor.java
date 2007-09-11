/*

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ptolemy.actor.gui.EditorFactory;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

//////////////////////////////////////////////////////////////////////////
//// RuleEditor

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class RuleEditor extends JDialog implements ActionListener {

    public RuleEditor(Frame owner, RuleList initialRules) {
        this(owner, "Rule Editor", initialRules);
    }

    public RuleEditor(Frame owner, String title, RuleList initialRules) {
        super(owner, title, true);
        _createComponents();
        resetTable(initialRules);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("add".equals(command)) {
            addNewRow();
        } else if ("remove".equals(command)) {
            removeSelectedRows();
        } else if ("commit".equals(command)) {
            commit();
        } else if ("cancel".equals(command)) {
            cancel();
        }
    }

    public void addNewRow() {
        try {
            Row row = new Row(_ruleClasses.get(0).newInstance());
            _tableModel.addRow(new Object[] {_tableModel.getRowCount() + 1, row,
                    row});
        } catch (Exception e) {
            throw new KernelRuntimeException(e, "Unable to create a new " +
                    "rule instance.");
        }
    }

    public void cancel() {
        setVisible(false);
        _isCanceled = true;
    }

    public void centerOnScreen() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getSize().width) / 2,
                 (tk.getScreenSize().height - getSize().height) / 2);
    }

    public void commit() {
        _committedRuleList.clear();
        Vector<?> dataVector = _tableModel.getDataVector();
        for (Object rowData : dataVector) {
            Vector<?> rowVector = (Vector<?>) rowData;
            Row row = (Row) rowVector.get(1);
            Rule rule = _createRuleFromRow(row);
            _committedRuleList.add(rule);
        }

        try {
            _committedRuleList.validate();
            _isCanceled = false;
        } catch (RuleValidationException e) {
            String message = e.getMessage();
            message += "\nPress Edit to return to modify the rules, or press "
                + "Cancel to cancel all the changes.";

            String[] options = new String[] {"Edit", "Cancel"};
            int selected = JOptionPane.showOptionDialog(null, message,
                    "Validation Error", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            if (selected == 0) {
                return;
            } else {
                _isCanceled = true;
            }
        }

        setVisible(false);
    }

    public RuleList getRuleList() {
        return _committedRuleList;
    }

    public boolean isCanceled() {
        return _isCanceled;
    }

    public void removeSelectedRows() {
        _editor.stopCellEditing();
        int[] rows = _table.getSelectedRows();
        int size = _tableModel.getRowCount();
        for (int i = 0, deleted = 0; i < size; i++) {
            if (deleted < rows.length && i == rows[deleted]) {
                _tableModel.removeRow(i - deleted);
                deleted++;
            } else {
                _tableModel.setValueAt(i - deleted + 1, i - deleted, 0);
            }
        }
    }

    public void resetTable(RuleList ruleList) {
        _tableModel.getDataVector().removeAllElements();
        int i = 0;
        for (Rule rule : ruleList) {
            Row row = new Row(rule);
            _tableModel.addRow(new Object[] {i++ + 1, row, row});
        }
    }

    @SuppressWarnings("unchecked")
    public static void searchRuleClasses(String[] packages,
            ClassLoader loader) {
        _ruleClasses.clear();
        for (String pkg : packages) {
            try {
                Enumeration<URL> urls =
                    loader.getResources(pkg.replace('.', '/'));
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    File directory =
                        new File(URLDecoder.decode(url.getPath(), "UTF-8"));
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        if (file.exists() && file.isFile()) {
                            String filePath = file.getName();
                            if (filePath.endsWith(".class")) {
                                String className = filePath.substring(0,
                                        filePath.length() - 6);
                                className = className.replace('$', '.');
                                String fullClassName = pkg + "." + className;
                                try {
                                    Class<?> cls =
                                        loader.loadClass(fullClassName);
                                    if (_isSubclass(cls, Rule.class)) {
                                        _ruleClasses.add(
                                                (Class<? extends Rule>) cls);
                                    }
                                } catch (ClassNotFoundException e) {
                                } catch (NoClassDefFoundError e) {
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    public static int ROW_HEIGHT = 45;

    public static Color ROW_SELECTED_COLOR = new Color(230, 230, 255);

    public static Color ROW_UNSELECTED_COLOR = Color.WHITE;

    /**
     * @author tfeng
     *
     * @see VisibleParameterEditorFactory
     */
    public static class Factory extends EditorFactory {

        public Factory(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);

            attributeName = new StringAttribute(this, "attributeName");
        }

        public void createEditor(NamedObj object, Frame parent) {
            try {
                String name = attributeName.getExpression();
                NamedObj container = getContainer();
                RuleListAttribute attributeToEdit =
                    (RuleListAttribute) container.getAttribute(name,
                            RuleListAttribute.class);
                _editor = new RuleEditor(parent,
                        "Editor for " + name + " of " + container.getFullName(),
                        attributeToEdit.getRuleList());

                _editor.pack();
                _editor.centerOnScreen();
                _editor.setVisible(true);

                if (!_editor.isCanceled()) {
                    RuleList list = _editor.getRuleList();
                    String listString = list.toString();
                    attributeToEdit.setExpression(listString);

                    String moml = "<property name=\"" + name + "\" value=\""
                            + StringUtilities.escapeForXML(listString)
                            + "\"/>";
                    MoMLChangeRequest request = new MoMLChangeRequest(this,
                            object, moml, null);
                    attributeToEdit.requestChange(request);
                }
            } catch (KernelException ex) {
                MessageHandler.error(
                        "Cannot get specified string attribute to edit.", ex);
            }
        }

        public StringAttribute attributeName;

        private RuleEditor _editor;

        private static final long serialVersionUID = 6581490244784855795L;
    }

    protected void _createComponents() {
        _tableModel = new DefaultTableModel(
                new Object[] {"", "Class", "Attributes"}, 0) {
                    public boolean isCellEditable(int row, int column) {
                        if (column == 0) {
                            return false;
                        } else {
                            return super.isCellEditable(row, column);
                        }
                    }
                    private static final long serialVersionUID =
                        -6967159767501555584L;
                };
        _table = new JTable(_tableModel);
        _table.setRowHeight(ROW_HEIGHT);
        _table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _table.setSelectionBackground(ROW_SELECTED_COLOR);
        _table.setSelectionForeground(Color.BLACK);
        _table.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    commit();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                }
            }

            public void keyReleased(KeyEvent e) {
            }

            public void keyTyped(KeyEvent e) {
            }
        });

        JTableHeader header = _table.getTableHeader();

        header.setFont(new Font("Dialog", Font.BOLD, 11));

        header.setForeground(Color.BLUE);
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer renderer =
            (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setPreferredSize(new Dimension(0, 22));

        _editor = new AttributesEditor();
        TableColumnModel model = _table.getColumnModel();
        model.getColumn(1).setCellEditor(_editor);
        model.getColumn(1).setCellRenderer(_editor);
        model.getColumn(2).setCellEditor(_editor);
        model.getColumn(2).setCellRenderer(_editor);
        JScrollPane scrollPane = new JScrollPane(_table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        _addButton = new JButton("Add");
        _addButton.setActionCommand("add");
        _addButton.addActionListener(this);
        buttonsPanel.add(_addButton);
        _removeButton = new JButton("Remove");
        _removeButton.setActionCommand("remove");
        _removeButton.addActionListener(this);
        buttonsPanel.add(_removeButton);
        _commitButton = new JButton("Commit");
        _commitButton.setActionCommand("commit");
        _commitButton.addActionListener(this);
        buttonsPanel.add(_commitButton);
        getRootPane().setDefaultButton(_commitButton);
        _cancelButton = new JButton("Cancel");
        _cancelButton.setActionCommand("cancel");
        _cancelButton.addActionListener(this);

        buttonsPanel.add(_cancelButton);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        TableColumn column0 = model.getColumn(0);
        column0.setMinWidth(10);
        column0.setPreferredWidth(15);
        column0.setMaxWidth(30);
        DefaultTableCellRenderer indexRenderer =
            new DefaultTableCellRenderer() {
                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus,
                        int row, int column) {
                    return super.getTableCellRendererComponent(table, value,
                            isSelected, false, row, column);
                }
                private static final long serialVersionUID =
                    2746000543494635898L;
        };
        indexRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        column0.setCellRenderer(indexRenderer);

        model.getColumn(1).setPreferredWidth(120);
        model.getColumn(2).setPreferredWidth(480);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        setPreferredSize(new Dimension(600, 420));
    }

    @SuppressWarnings("unchecked")
    protected Rule _createRuleFromRow(Row row) {
        JComboBox classSelector = row.getClassSelector();
        ComboElement element = (ComboElement) classSelector.getSelectedItem();
        Class<? extends Rule> ruleClass =
            (Class<? extends Rule>) element.getRuleClass();
        Rule rule;
        try {
            rule = ruleClass.newInstance();
        } catch (Exception e) {
            throw new KernelRuntimeException(e, "Unable to create rule from " +
                    "class \"" + ruleClass.getName() + "\".");
        }

        JTable attributeTable = row.getAttributeTable();
        DefaultTableModel attributeModel =
            (DefaultTableModel) attributeTable.getModel();
        Vector<?> attributeVector =
            (Vector<?>) attributeModel.getDataVector().get(0);
        for (int i = 1; i < attributeVector.size(); i++) {
            JPanel attributePanel = (JPanel) attributeVector.get(i);
            // The first component (index 0) is the enablement.
            Component component = attributePanel.getComponent(0);
            rule.setAttributeEnabled(i - 1, Boolean.valueOf(
                    ((JCheckBox) component).isSelected()));
            // The second component (index 1) is the value.
            component = attributePanel.getComponent(1);
            if (component instanceof JTextField) {
                rule.setAttributeValue(i - 1,
                        ((JTextField) component).getText());
            } else if (component instanceof JCheckBox) {
                rule.setAttributeValue(i - 1, Boolean.valueOf(
                        ((JCheckBox) component).isSelected()));
            }
        }

        return rule;
    }

    private static boolean _isSubclass(Class<?> subclass, Class<?> superclass) {
        if (subclass == null) {
            return false;
        } else if (subclass.equals(superclass)) {
            return true;
        } else {
            return _isSubclass(subclass.getSuperclass(), superclass);
        }
    }

    private static void _setCaretForAllJTextFields(JPanel panel,
            boolean visible) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JTextField) {
                ((JTextField) c).getCaret().setVisible(visible);
            }
        }
    }

    private JButton _addButton;

    private JButton _cancelButton;

    private JButton _commitButton;

    private RuleList _committedRuleList = new RuleList();

    private AttributesEditor _editor;

    private boolean _isCanceled = false;

    private JButton _removeButton;

    private static List<Class<? extends Rule>> _ruleClasses =
        new LinkedList<Class<? extends Rule>>();

    private JTable _table;

    private DefaultTableModel _tableModel;

    private static final long serialVersionUID = -2788727943126991098L;

    private static class AttributesEditor extends AbstractCellEditor
    implements TableCellEditor, TableCellRenderer {

        public Object getCellEditorValue() {
            return _currentRow;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            _currentRow = (Row) value;
            JPanel leftPanel = _currentRow.getLeftPanel();
            JPanel rightPanel = _currentRow.getRightPanel();
            Color color =
                isSelected ? ROW_SELECTED_COLOR : ROW_UNSELECTED_COLOR;
            leftPanel.setBackground(color);
            rightPanel.setBackground(color);
            return column == 1 ? leftPanel : rightPanel;
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Row currentRow = (Row) value;
            JPanel leftPanel = currentRow.getLeftPanel();
            JPanel rightPanel = currentRow.getRightPanel();
            Color color =
                isSelected ? ROW_SELECTED_COLOR : ROW_UNSELECTED_COLOR;
            leftPanel.setBackground(color);
            rightPanel.setBackground(color);

            if (column == 1) {
                DefaultTableModel attributeModel =
                    (DefaultTableModel) currentRow._attributeTable.getModel();
                for (int i = 0; i < attributeModel.getColumnCount(); i++) {
                    JPanel panel = (JPanel) attributeModel.getValueAt(0, i);
                    if (panel != null) {
                        _setCaretForAllJTextFields(panel, false);
                    }
                }
            }
            return column == 1 ? leftPanel : rightPanel;
        }

        private Row _currentRow;

        private static final long serialVersionUID = -8545086228933217848L;

    }

    private static class ComboElement {

        public ComboElement(Rule rule) {
            _ruleClass = rule.getClass();
            _rule = rule;
        }

        public Rule getRule() {
            return _rule;
        }

        public Class<?> getRuleClass() {
            return _ruleClass;
        }

        public String toString() {
            return _ruleClass.getSimpleName();
        }

        private Rule _rule;

        private Class<?> _ruleClass;
    }

    private static class JPanelCellEditor extends AbstractCellEditor
    implements TableCellEditor, TableCellRenderer, ActionListener {

        public void actionPerformed(ActionEvent e) {
        }

        public Object getCellEditorValue() {
            return _currentValue;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            JPanel panel = (JPanel) value;
            _setCaretForAllJTextFields(panel, true);
            _currentValue = panel;
            return panel;
        }

        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            JPanel panel = (JPanel) value;
            _setCaretForAllJTextFields(panel, false);
            return panel;
        }

        private JPanel _currentValue;

        private static final long serialVersionUID = -3898792308707116805L;
    }

    private class Row implements ItemListener {

        public Row(Rule rule) {
            _rightPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

            JTableHeader header = _attributeTable.getTableHeader();
            DefaultTableCellRenderer renderer =
                (DefaultTableCellRenderer) header.getDefaultRenderer();
            header.setReorderingAllowed(false);
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
            header.setPreferredSize(new Dimension(0, 18));
            header.setOpaque(false);
            header.setBackground(Color.WHITE);

            _attributeTable.setCellSelectionEnabled(false);
            _attributeTable.setIntercellSpacing(new Dimension(10, 0));
            _attributeTable.setShowGrid(false);
            _attributeTable.setOpaque(false);
            _attributeTable.setRowHeight(ROW_HEIGHT);

            JScrollPane scrollPane = new JScrollPane(_attributeTable,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
            _rightPanel.add(scrollPane, BorderLayout.CENTER);

            Class<?> ruleClass = rule.getClass();
            _classSelector.addItemListener(this);
            _classSelector.setEditable(true);
            _classSelector.setOpaque(false);
            _classSelector.setBorder(new EmptyBorder(0, 0, 0, 0));
            _classSelector.setEditor(new ClassSelectorEditor(_classSelector));
            for (Class<? extends Rule> listedRule : _ruleClasses) {
                if (listedRule == null && ruleClass == null ||
                        listedRule != null && listedRule.equals(ruleClass)) {
                    ComboElement element = new ComboElement(rule);
                    _classSelector.addItem(element);
                    _classSelector.setSelectedItem(element);
                } else {
                    try {
                        Rule newRule = listedRule.newInstance();
                        ComboElement element = new ComboElement(newRule);
                        _classSelector.addItem(element);
                    } catch (Exception e) {
                        throw new KernelRuntimeException(e,
                                "Unable to create rule from class \"" +
                                listedRule.getName() + "\".");
                    }
                }
            }
            JLabel classLabel = new JLabel("Rule Class");
            classLabel.setHorizontalAlignment(SwingConstants.CENTER);
            classLabel.setPreferredSize(new Dimension(0, 18));
            classLabel.setVerticalAlignment(SwingConstants.TOP);
            _leftPanel.add(classLabel, BorderLayout.NORTH);
            _leftPanel.add(_classSelector, BorderLayout.CENTER);
            _leftPanel.setBorder(new EmptyBorder(0, 5, 3, 5));
        }

        public JTable getAttributeTable() {
            return _attributeTable;
        }

        public JComboBox getClassSelector() {
            return _classSelector;
        }

        public JPanel getLeftPanel() {
            return _leftPanel;
        }

        public JPanel getRightPanel() {
            return _rightPanel;
        }

        public void itemStateChanged(ItemEvent e) {
            _initRightPanel();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                DefaultTableModel tableModel =
                    (DefaultTableModel) _table.getModel();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1) == this) {
                        tableModel.fireTableRowsUpdated(i, i);
                        break;
                    }
                }
            }
        }

        protected int _getColumnWidth(JComponent component) {
            if (component instanceof JTextField) {
                return 80;
            } else if (component instanceof JCheckBox) {
                return 40;
            } else {
                return 80;
            }
        }

        protected JComponent _getComponent(RuleAttribute attribute) {
            JComponent component = null;
            switch (attribute.getType()) {
            case RuleAttribute.BOOLEAN:
                JCheckBox checkBox = new JCheckBox();
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                component = checkBox;
                break;
            case RuleAttribute.STRING:
                component = new JTextField();
                break;
            }
            component.setBackground(new Color(240, 240, 240));
            return component;
        }

        protected void _initRightPanel() {
            ComboElement selectedElement =
                (ComboElement) _classSelector.getSelectedItem();
            Rule rule = selectedElement.getRule();
            RuleAttribute[] attributes = rule.getAttributes();
            _components = new JComponent[attributes.length];

            // Create a table with one empty column (will be deleted)
            DefaultTableModel tableModel =
                new DefaultTableModel(new Object[] {null}, 1);
            _attributeTable.setModel(tableModel);
            _attributeTable.setTableHeader(null);

            TableColumnModel columnModel = _attributeTable.getColumnModel();

            for (int i = 0; i < attributes.length; i++) {
                RuleAttribute attribute = attributes[i];

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(new EmptyBorder(2, 0, 4, 0));
                panel.setOpaque(false);

                String columnName = attribute.getName();
                JCheckBox checkBox = new JCheckBox(columnName);
                checkBox.setOpaque(false);
                checkBox.setBorder(new EmptyBorder(0, 0, 0, 0));
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setPreferredSize(new Dimension(0, 18));
                checkBox.setVerticalAlignment(SwingConstants.TOP);
                checkBox.addActionListener(new CheckBoxActionListener(i));
                panel.add(checkBox, BorderLayout.NORTH);

                JComponent component = _getComponent(attribute);
                _setComponentValue(attribute, component,
                        rule.getAttributeValue(i));
                panel.add(component, BorderLayout.CENTER);

                tableModel.addColumn(attribute.getName(), new Object[] {panel});

                TableColumn column =
                    _attributeTable.getColumnModel().getColumn(i + 1);
                column.setHeaderValue(attribute.getName());

                _components[i] = component;

                boolean enabled = rule.isAttributeEnabled(i);
                checkBox.setSelected(enabled);
                _setEnablement(component, enabled);
            }

            columnModel.removeColumn(columnModel.getColumn(0));

            int columnCount = columnModel.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                columnModel.getColumn(i).setCellEditor(_cellEditor);
                columnModel.getColumn(i).setCellRenderer(_cellEditor);
            }
        }

        protected void _setColumnWidths() {
            int totalWidth = 0;
            int[] widths = new int[_components.length];
            for (int i = 0; i < _components.length; i++) {
                widths[i] = _getColumnWidth(_components[i]);
                totalWidth += widths[i];
            }

            TableColumnModel columnModel = _attributeTable.getColumnModel();
            int tableWidth = _attributeTable.getWidth();
            for (int i = 0; i < widths.length; i++) {
                float percentage = (float) widths[i] / totalWidth;
                columnModel.getColumn(i).setPreferredWidth(
                        (int) (tableWidth * percentage));
            }
        }

        protected void _setComponentValue(RuleAttribute attribute,
                JComponent component, Object value) {
            switch (attribute.getType()) {
            case RuleAttribute.BOOLEAN:
                ((JCheckBox) component).setSelected(
                        ((Boolean) value).booleanValue());
                break;
            case RuleAttribute.STRING:
                ((JTextField) component).setText((String) value);
                break;
            }
        }

        private void _setEnablement(JComponent component, boolean enabled) {
            component.setEnabled(enabled);
            component.setOpaque(!enabled);
        }

        private JTable _attributeTable = new JTable() {
            public void doLayout() {
                _setColumnWidths();
                super.doLayout();
            }

            private static final long serialVersionUID = -1516263162184646883L;
        };

        private JPanelCellEditor _cellEditor = new JPanelCellEditor();

        private JComboBox _classSelector = new JComboBox();

        private JComponent[] _components;

        private JPanel _leftPanel = new JPanel(new BorderLayout());

        private JPanel _rightPanel = new JPanel(new BorderLayout());

        private class CheckBoxActionListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBox) e.getSource()).isSelected();
                _setEnablement(_components[_index], selected);
            }

            CheckBoxActionListener(int index) {
                _index = index;
            }

            private int _index;
        }

        private class ClassSelectorEditor implements ComboBoxEditor,
        MouseListener, FocusListener {

            public void addActionListener(ActionListener l) {
            }

            public void focusGained(FocusEvent e) {
            }

            public void focusLost(FocusEvent e) {
                _comboBox.hidePopup();
            }

            public Component getEditorComponent() {
                return _textField;
            }

            public Object getItem() {
                return _value;
            }

            public void mouseClicked(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
                if (_comboBox.isPopupVisible()) {
                    _comboBox.hidePopup();
                } else {
                    _comboBox.showPopup();
                }
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void removeActionListener(ActionListener l) {
            }

            public void selectAll() {
            }

            public void setItem(Object value) {
                _value = value;
                if (value == null) {
                    _textField.setText("");
                } else {
                    _textField.setText(value.toString());
                }
            }

            ClassSelectorEditor(JComboBox comboBox) {
                _comboBox = comboBox;
                _textField.setEditable(false);
                _textField.setOpaque(false);
                _textField.addMouseListener(this);
                _textField.addFocusListener(this);
            }

            private JComboBox _comboBox;

            private JTextField _textField = new JTextField();

            private Object _value;
        }
    }

    static {
        String[] packages = new String[] { "ptolemy.actor.gt.rules" };
        searchRuleClasses(packages, ClassLoader.getSystemClassLoader());
    }
}

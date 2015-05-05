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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ptolemy.actor.gt.GTEntity;
import ptolemy.actor.gt.GTIngredient;
import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.GTIngredientsAttribute;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.MalformedStringException;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.ingredients.criteria.BooleanCriterionElement;
import ptolemy.actor.gt.ingredients.criteria.ChoiceCriterionElement;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.gt.ingredients.criteria.StringCriterionElement;
import ptolemy.actor.gt.ingredients.operations.BooleanOperationElement;
import ptolemy.actor.gt.ingredients.operations.Operation;
import ptolemy.actor.gt.ingredients.operations.StringOperationElement;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.HTMLViewer;
import ptolemy.actor.gui.PtolemyDialog;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

//////////////////////////////////////////////////////////////////////////
//// GTIngredientsEditor

/**

@author Thomas Huining Feng
@version $Id$
@since Ptolemy II 6.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
 */
@SuppressWarnings("serial")
public class GTIngredientsEditor extends PtolemyDialog {

    public GTIngredientsEditor(DialogTableau tableau, Frame owner,
            NamedObj target, Configuration configuration) {
        super("", tableau, owner, null, configuration);

        _owner = owner;
        _target = target;

        Attribute attribute = null;

        if (GTTools.isInPattern(target)) {
            if (target instanceof GTEntity) {
                attribute = target.getAttribute("criteria");
            } else {
                try {
                    target.workspace().getReadAccess();
                    attribute = target.attributeList(
                            GTIngredientsAttribute.class).get(0);
                } finally {
                    target.workspace().doneReading();
                }
            }
            _ingredientClasses = _criterionClasses;
            tableau.setTitle("Criteria editor for " + target.getName());
        } else if (GTTools.isInReplacement(target)) {
            if (target instanceof GTEntity) {
                attribute = target.getAttribute("operations");
            } else {
                try {
                    target.workspace().getReadAccess();
                    attribute = target.attributeList(
                            GTIngredientsAttribute.class).get(0);
                } finally {
                    target.workspace().doneReading();
                }
            }
            _ingredientClasses = _operationClasses;
            tableau.setTitle("Operations editor for " + target.getName());
        }

        _attribute = (GTIngredientsAttribute) attribute;

        _temporaryIngredientList = new GTIngredientList(_attribute);
        _createComponents();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("add".equals(command)) {
            addNewRow();
        } else if ("remove".equals(command)) {
            removeSelectedRows();
        } else if ("commit".equals(command)) {
            commit();
        } else if ("apply".equals(command)) {
            apply();
        } else if ("cancel".equals(command)) {
            cancel();
        }
    }

    public void addNewRow() {
        try {
            GTIngredient ingredient = null;
            for (Class<? extends GTIngredient> theClass : _ingredientClasses) {
                ingredient = _createTemporaryIngredient(theClass);
                if (ingredient.isApplicable(_target)) {
                    break;
                }
            }
            if (ingredient == null) {
                return;
            }

            Row row = new Row(ingredient);
            int rowCount = _tableModel.getRowCount();
            _tableModel.addRow(new Object[] { rowCount + 1, row, row });
            if (rowCount == 0) {
                _table.getSelectionModel().addSelectionInterval(0, 0);
            }
        } catch (Exception e) {
            throw new KernelRuntimeException(e, "Unable to create a new "
                    + "criterion or operation instance.");
        }
    }

    public boolean apply() {
        GTIngredientList ingredientList = new GTIngredientList(_attribute);
        Vector<?> dataVector = _tableModel.getDataVector();
        for (Object rowData : dataVector) {
            Vector<?> rowVector = (Vector<?>) rowData;
            Row row = (Row) rowVector.get(1);
            GTIngredient incredient = _createIngredientFromRow(row);
            ingredientList.add(incredient);
        }

        try {
            ingredientList.validate();
        } catch (ValidationException e) {
            String message = e.getMessage()
                    + "\nPress Edit to return to modify the criterion or "
                    + "operation, or press Revert to revert to its previous "
                    + "value.";

            String[] options = new String[] { "Edit", "Revert" };
            int selected = JOptionPane.showOptionDialog(null, message,
                    "Validation Error", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, options, options[1]);
            if (selected == 1) {
                resetTable(_initialIngredientList);
            }
            return false;
        }

        String moml = "<property name=\"" + _attribute.getName()
                + "\" value=\""
                + StringUtilities.escapeForXML(ingredientList.toString())
                + "\"/>";
        MoMLChangeRequest request = new MoMLChangeRequest(this, _target, moml,
                null);
        request.setUndoable(true);
        _attribute.requestChange(request);
        return true;
    }

    public void cancel() {
        setVisible(false);
    }

    public void centerOnScreen() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getSize().width) / 2,
                (tk.getScreenSize().height - getSize().height) / 2);
    }

    public void commit() {
        if (apply()) {
            setVisible(false);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        } else {
            return _PREFERRED_SIZE;
        }
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

    public void resetTable(GTIngredientList ingredientList) {
        int[] selectedRows = _table.getSelectedRows();
        _editor.stopCellEditing();
        while (_tableModel.getRowCount() > 0) {
            _tableModel.removeRow(0);
        }
        int i = 0;
        for (GTIngredient ingredient : ingredientList) {
            Row row = new Row(ingredient);
            _tableModel.addRow(new Object[] { i++ + 1, row, row });
        }
        if (selectedRows.length == 0) {
            if (i > 0) {
                _table.getSelectionModel().addSelectionInterval(0, 0);
            }
        } else {
            for (i = 0; i < selectedRows.length; i++) {
                _table.getSelectionModel().addSelectionInterval(
                        selectedRows[i], selectedRows[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Class<? extends GTIngredient>> searchIngredientClasses(
            String[] packages, ClassLoader loader) {
        List<Class<? extends GTIngredient>> ingredientClasses = new LinkedList<Class<? extends GTIngredient>>();
        for (String pkg : packages) {
            try {
                Enumeration<URL> urls = loader.getResources(pkg.replace('.',
                        '/'));
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    File directory = new File(URLDecoder.decode(url.getPath(),
                            "UTF-8"));
                    File[] files = directory.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (!file.exists() || !file.isFile()) {
                                continue;
                            }

                            String filePath = file.getName();
                            if (!filePath.endsWith(".class")) {
                                continue;
                            }

                            String className = filePath.substring(0,
                                    filePath.length() - 6);
                            className = className.replace('$', '.');
                            String fullClassName = pkg + "." + className;
                            try {
                                Class<?> cls = loader.loadClass(fullClassName);
                                if (!Modifier.isAbstract(cls.getModifiers())
                                        && GTIngredient.class.isAssignableFrom(cls)) {
                                    ingredientClasses
                                        .add((Class<? extends GTIngredient>) cls);
                                }
                            } catch (ClassNotFoundException e) {
                            } catch (NoClassDefFoundError e) {
                            }
                        }
                    }
                }
            } catch (IOException e) {
            }
        }
        return ingredientClasses;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible && _attribute != null) {
            try {
                _initialIngredientList = _attribute.getIngredientList();
                resetTable(_initialIngredientList);
            } catch (MalformedStringException e) {
                throw new KernelRuntimeException(e, "Attribute \""
                        + _attribute.getName() + "\" of " + "entity "
                        + _target.getName() + " is malformed.");
            }
        }
        super.setVisible(visible);
    }

    public static final String REGULAR_EXPRESSION_HELP_FILE = "ptolemy/configs/doc/basicHelp.htm";

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

        @Override
        public void createEditor(NamedObj object, Frame parent) {
            Configuration configuration = ((TableauFrame) parent)
                    .getConfiguration();
            Effigy effigy = ((TableauFrame) parent).getEffigy();

            // The dialog now only works with TransitionMatcher, which is not an
            // Entity.
            /*DialogTableau dialogTableau = DialogTableau.createDialog(parent,
                    configuration, effigy, GTIngredientsEditor.class,
                    (Entity) object);
            if (dialogTableau != null) {
                dialogTableau.show();
            }*/

            DialogTableau tableau;
            try {
                tableau = new DialogTableau(effigy, effigy.uniqueName("dialog"));
                tableau.setFrame(new GTIngredientsEditor(tableau, parent,
                        object, configuration));
                tableau.show();
            } catch (Throwable throwable) {
                throw new InternalErrorException(null, throwable,
                        "Unable to create " + "ingredient editor for "
                                + object.getName());
            }
        }

        public StringAttribute attributeName;

    }

    protected void _createComponents() {
        // Clear all the buttons and panels created by superclasses.
        getContentPane().removeAll();

        _tableModel = new DefaultTableModel(new Object[] { "", "Class",
        "Elements" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0) {
                    return false;
                } else {
                    return super.isCellEditable(row, column);
                }
            }
        };
        _table = new JTable(_tableModel);
        _table.setRowHeight(_ROW_HEIGHT);
        _table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _table.setSelectionBackground(_SELECTED_COLOR);
        _table.setSelectionForeground(Color.BLACK);
        _table.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    commit();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancel();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent e) {
            }
        });
        _table.setEnabled(_attribute != null);

        JTableHeader header = _table.getTableHeader();
        header.setFont(new Font("Dialog", Font.BOLD, 11));
        header.setForeground(Color.BLUE);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header
                .getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setPreferredSize(new Dimension(0, 22));

        _editor = new IngredientContentEditor();
        TableColumnModel model = _table.getColumnModel();
        model.getColumn(1).setCellEditor(_editor);
        model.getColumn(1).setCellRenderer(_editor);
        model.getColumn(2).setCellEditor(_editor);
        model.getColumn(2).setCellRenderer(_editor);
        JScrollPane scrollPane = new JScrollPane(_table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        GridLayout gridLayout = new GridLayout();
        gridLayout.setHgap(5);
        JPanel helpPanel = new JPanel(gridLayout);
        helpPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 5));
        helpPanel.setPreferredSize(new Dimension(0, 25));

        TableauFrame owner = _owner instanceof TableauFrame ? (TableauFrame) _owner
                : null;

        helpPanel.add(new HelpLabel("Normal text only",
                _NON_REGULAR_EXPRESSION_BACKGROUND));
        try {
            helpPanel.add(new HelpLabel("Regular expression",
                    _REGULAR_EXPRESSION_BACKGROUND, new URL(
                            _REGULAR_EXPRESSION_HELP_FILE), owner));
        } catch (MalformedURLException e1) {
            helpPanel.add(new HelpLabel("Regular expression",
                    _REGULAR_EXPRESSION_BACKGROUND));
        }
        helpPanel.add(new HelpLabel("Evaluated expression",
                _PTOLEMY_EXPRESSION_BACKGROUND, _PTOLEMY_EXPRESSION_HELP_FILE,
                owner));
        helpPanel.add(new HelpLabel("Do not match", _DISABLED_BACKGROUND));

        bottomPanel.add(helpPanel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        JButton addButton = new JButton("Add");
        addButton.setActionCommand("add");
        addButton.addActionListener(this);
        addButton.setEnabled(_attribute != null);
        buttonsPanel.add(addButton);
        JButton removeButton = new JButton("Remove");
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);
        removeButton.setEnabled(_attribute != null);
        buttonsPanel.add(removeButton);
        JButton commitButton = new JButton("Commit");
        commitButton.setActionCommand("commit");
        commitButton.addActionListener(this);
        commitButton.setEnabled(_attribute != null);
        buttonsPanel.add(commitButton);
        getRootPane().setDefaultButton(commitButton);
        JButton applyButton = new JButton("Apply");
        applyButton.setActionCommand("apply");
        applyButton.addActionListener(this);
        applyButton.setEnabled(_attribute != null);
        buttonsPanel.add(applyButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(this);

        buttonsPanel.add(cancelButton);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        TableColumn column0 = model.getColumn(0);
        column0.setMinWidth(10);
        column0.setPreferredWidth(15);
        column0.setMaxWidth(30);
        DefaultTableCellRenderer indexRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row, int column) {
                return super.getTableCellRendererComponent(table, value,
                        isSelected, false, row, column);
            }
        };
        indexRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        column0.setCellRenderer(indexRenderer);

        model.getColumn(1).setPreferredWidth(150);
        model.getColumn(2).setPreferredWidth(650);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        setPreferredSize(_PREFERRED_SIZE);
    }

    @Override
    protected void _createExtendedButtons(JPanel _buttons) {
    }

    @SuppressWarnings("unchecked")
    protected GTIngredient _createIngredientFromRow(Row row) {
        JComboBox classSelector = row.getClassSelector();
        ComboElement element = (ComboElement) classSelector.getSelectedItem();
        Class<? extends GTIngredient> ingredientClass = (Class<? extends GTIngredient>) element
                .getIngredientClass();
        GTIngredient ingredient;
        try {
            ingredient = _createTemporaryIngredient(ingredientClass);
        } catch (Exception e) {
            throw new KernelRuntimeException(e,
                    "Unable to create criterion or "
                            + "operation from class \""
                            + ingredientClass.getName() + "\".");
        }

        JCheckBox[] checkBoxes = row.getCheckBoxs();
        JComponent[] components = row.getEditingComponents();
        for (int i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i] != null) {
                ingredient.setEnabled(i,
                        Boolean.valueOf(checkBoxes[i].isSelected()));
            }
            JComponent editor = components[i];
            if (editor instanceof JTextField) {
                ingredient.setValue(i, ((JTextField) editor).getText());
            } else if (editor instanceof JComboBox) {
                ingredient.setValue(i, ((JComboBox) editor).getSelectedItem()
                        .toString());
            } else if (editor instanceof JCheckBox) {
                ingredient.setValue(i,
                        Boolean.valueOf(((JCheckBox) editor).isSelected()));
            }
        }

        return ingredient;
    }

    @Override
    protected URL _getHelpURL() {
        URL helpURL = getClass().getClassLoader().getResource(
                REGULAR_EXPRESSION_HELP_FILE);
        return helpURL;
    }

    private GTIngredient _createTemporaryIngredient(
            Class<? extends GTIngredient> ingredientClass)
                    throws SecurityException, NoSuchMethodException,
                    IllegalArgumentException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        Constructor<? extends GTIngredient> constructor = ingredientClass
                .getConstructor(GTIngredientList.class);
        return constructor
                .newInstance(new Object[] { _temporaryIngredientList });
    }

    private static final Color _DISABLED_BACKGROUND = new Color(220, 220, 220);

    private static final Border _EMPTY_BORDER = BorderFactory
            .createEmptyBorder();

    private static final Color _NON_REGULAR_EXPRESSION_BACKGROUND = new Color(
            230, 230, 255);

    private static final Dimension _PREFERRED_SIZE = new Dimension(800, 500);

    private static final Color _PTOLEMY_EXPRESSION_BACKGROUND = new Color(255,
            200, 200);

    private static final String _PTOLEMY_EXPRESSION_HELP_FILE = "doc/expressions.htm";

    private static final Color _REGULAR_EXPRESSION_BACKGROUND = new Color(200,
            255, 255);

    // Use #in_browser so that the URL is opened using the browser
    // application.  The problem is that the Java HTML viewer cannot
    // handle complex pages.
    private static final String _REGULAR_EXPRESSION_HELP_FILE = "http://download.oracle.com/javase/tutorial/essential/regex/#in_browser";

    private static final int _ROW_HEIGHT = 45;

    private static final Color _SELECTED_COLOR = new Color(230, 230, 255);

    private static final Border _TEXT_FIELD_BORDER = new JTextField()
    .getBorder();

    private static final Color _UNSELECTED_COLOR = Color.WHITE;

    private GTIngredientsAttribute _attribute;

    private static List<Class<? extends GTIngredient>> _criterionClasses;

    private IngredientContentEditor _editor;

    private List<Class<? extends GTIngredient>> _ingredientClasses;

    private GTIngredientList _initialIngredientList;

    private static List<Class<? extends GTIngredient>> _operationClasses;

    private Frame _owner;

    private JTable _table;

    private DefaultTableModel _tableModel;

    private NamedObj _target;

    private GTIngredientList _temporaryIngredientList;

    private static class ColorizedComboBox extends JComboBox {

        public Color getCustomBackground() {
            return _background;
        }

        @Override
        public void setBackground(Color color) {
            super.setBackground(color);
            ComboBoxEditor editor = getEditor();
            if (editor != null) {
                editor.getEditorComponent().setBackground(color);
            }
        }

        @Override
        public void setEditable(boolean editable) {
            ((JTextField) _editor.getEditorComponent()).setEditable(editable);
        }

        ColorizedComboBox(Color background) {
            super.setEditable(true);
            _background = background;
            setBorder(_EMPTY_BORDER);
            setEditor(_editor);
        }

        private Color _background;

        private Editor _editor = new Editor();

        private class Editor extends MouseAdapter implements ActionListener,
        ComboBoxEditor, FocusListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (_textField.isEditable()) {
                    setSelectedItem(_textField.getText());
                }
                setPopupVisible(!isPopupVisible());
            }

            @Override
            public void addActionListener(ActionListener l) {
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (_textField.isEditable()) {
                    _textField.getCaret().setVisible(true);
                    if (_textField.isEnabled()) {
                        setPopupVisible(true);
                    }
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                hidePopup();
                if (_textField.isEditable()) {
                    setSelectedItem(_textField.getText());
                    _textField.getCaret().setVisible(false);
                }
            }

            @Override
            public Component getEditorComponent() {
                return _textField;
            }

            @Override
            public Object getItem() {
                return _textField.isEditable() ? _textField.getText() : _value;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (_textField.hasFocus() && _textField.isEnabled()) {
                    setPopupVisible(!isPopupVisible());
                }
            }

            @Override
            public void removeActionListener(ActionListener l) {
            }

            @Override
            public void selectAll() {
                _textField.selectAll();
            }

            @Override
            public void setItem(Object value) {
                _value = value;
                if (value == null) {
                    _textField.setText("");
                } else {
                    _textField.setText(value.toString());
                }
            }

            Editor() {
                _textField.setEditable(false);
                _textField.setOpaque(true);
                _textField.addMouseListener(this);
                _textField.addFocusListener(this);
                _textField.addActionListener(this);
            }

            private JTextField _textField = new JTextField();

            private Object _value;
        }
    }

    private static class ColorizedTextField extends JTextField {

        public Color getCustomBackground() {
            return _background;
        }

        ColorizedTextField(Color background) {
            _background = background;
        }

        private Color _background;

    }

    private static class ComboElement {

        public ComboElement(GTIngredient ingredient) {
            _ingredientClass = ingredient.getClass();
            _ingredient = ingredient;
        }

        public GTIngredient getIngredient() {
            return _ingredient;
        }

        public Class<?> getIngredientClass() {
            return _ingredientClass;
        }

        @Override
        public String toString() {
            return _ingredientClass.getSimpleName();
        }

        private GTIngredient _ingredient;

        private Class<?> _ingredientClass;
    }

    private static class HelpLabel extends JLabel implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent event) {
            if (_help != null) {
                String host = _help.getHost();
                if (host.length() > 0) {
                    // The help file is located on another server.
                    String message = "The help file is located on server "
                            + host + ". Do you want to open it? (A network "
                            + "connection is required.)";

                    int selected = JOptionPane.showConfirmDialog(null, message,
                            "Validation Error", JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE, null);
                    if (selected == 1) {
                        return;
                    }
                }

                try {
                    Configuration configuration = _owner.getConfiguration();
                    configuration
                    .openModel(null, _help, _help.toExternalForm());
                } catch (Exception e1) {
                    HTMLViewer viewer = new HTMLViewer();
                    try {
                        viewer.setPage(_help);
                        viewer.pack();
                        viewer.show();
                    } catch (IOException e2) {
                        throw new KernelRuntimeException("Cannot open help "
                                + "file.");
                    }
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            Color color = new Color(Math.min(
                    (int) (_background.getRed() * 1.1), 255), Math.min(
                            (int) (_background.getGreen() * 1.1), 255), Math.min(
                                    (int) (_background.getBlue() * 1.1), 255));
            setBackground(color);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setBackground(_background);
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        HelpLabel(String label, Color background) {
            this(label, background, (URL) null, null);
        }

        HelpLabel(String label, Color background, String helpFile,
                TableauFrame owner) {
            this(label, background, HelpLabel.class.getClassLoader()
                    .getResource(helpFile), owner);
        }

        HelpLabel(String label, Color background, URL help, TableauFrame owner) {
            super(label, SwingConstants.CENTER);

            setBackground(background);
            setBorder(_TEXT_FIELD_BORDER);
            setOpaque(true);

            _background = background;
            _help = help;
            if (_help != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(this);
            }
            _owner = owner;
        }

        private Color _background;

        private URL _help;

        private TableauFrame _owner;

    }

    private static class IngredientContentEditor extends AbstractCellEditor
    implements TableCellEditor, TableCellRenderer {

        @Override
        public Object getCellEditorValue() {
            return _currentRow;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            _currentRow = (Row) value;
            _currentRow.setSelected(isSelected, false);

            return column == 1 ? _currentRow.getLeftPanel() : _currentRow
                    .getRightPanel();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            Row currentRow = (Row) value;
            currentRow.setSelected(isSelected, true);

            return column == 1 ? currentRow.getLeftPanel() : currentRow
                    .getRightPanel();
        }

        private Row _currentRow;

    }

    private class Row implements ItemListener {

        public Row(GTIngredient ingredient) {
            _rightPanel.setBorder(_EMPTY_BORDER);

            Class<?> ingredientClass = ingredient.getClass();
            _classSelector.addItemListener(this);
            _classSelector.setEditable(false);
            for (Class<? extends GTIngredient> listedIngerdient : _ingredientClasses) {
                if (listedIngerdient == null && ingredientClass == null
                        || listedIngerdient != null
                        && listedIngerdient.equals(ingredientClass)) {
                    ComboElement element = new ComboElement(ingredient);
                    _classSelector.addItem(element);
                    _classSelector.setSelectedItem(element);
                } else {
                    try {
                        GTIngredient newIngredient = _createTemporaryIngredient(listedIngerdient);
                        if (newIngredient.isApplicable(_target)) {
                            ComboElement element = new ComboElement(
                                    newIngredient);
                            _classSelector.addItem(element);
                        }
                    } catch (Exception e) {
                        throw new KernelRuntimeException(e,
                                "Unable to create criterion or operation from "
                                        + "class \""
                                        + listedIngerdient.getName() + "\".");
                    }
                }
            }
            JLabel classLabel;
            if (ingredient instanceof Criterion) {
                classLabel = new JLabel("Criterion Class");
            } else if (ingredient instanceof Operation) {
                classLabel = new JLabel("Operation Class");
            } else {
                classLabel = new JLabel();
            }
            classLabel.setHorizontalAlignment(SwingConstants.CENTER);
            classLabel.setPreferredSize(new Dimension(0, 18));
            classLabel.setVerticalAlignment(SwingConstants.TOP);
            _leftPanel.add(classLabel, BorderLayout.NORTH);
            _leftPanel.add(_classSelector, BorderLayout.CENTER);
            _leftPanel.setBorder(new EmptyBorder(2, 5, 4, 5));
        }

        public JCheckBox[] getCheckBoxs() {
            return _checkBoxes;
        }

        public JComboBox getClassSelector() {
            return _classSelector;
        }

        public JComponent[] getEditingComponents() {
            return _components;
        }

        public JPanel getLeftPanel() {
            return _leftPanel;
        }

        public JPanel getRightPanel() {
            return _rightPanel;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            _initRightPanel();
            if (e.getStateChange() == ItemEvent.SELECTED) {
                DefaultTableModel tableModel = (DefaultTableModel) _table
                        .getModel();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    if (tableModel.getValueAt(i, 1) == this) {
                        tableModel.fireTableRowsUpdated(i, i);
                        break;
                    }
                }
            }
        }

        public void setSelected(boolean selected, boolean renderOnly) {
            Color color = selected ? _SELECTED_COLOR : _UNSELECTED_COLOR;
            _leftPanel.setBackground(color);
            _rightPanel.setBackground(color);
            _classSelector.setBackground(color);

            if (renderOnly) {
                for (Component component : _components) {
                    if (component instanceof JComboBox) {
                        component = ((JComboBox) component).getEditor()
                                .getEditorComponent();
                    }
                    if (component instanceof JTextField) {
                        ((JTextField) component).getCaret().setVisible(false);
                    }
                }
            }

            for (int i = 0; i < _checkBoxes.length; i++) {
                JComponent component = _components[i];
                if (_checkBoxes[i] == null || _checkBoxes[i].isSelected()) {
                    if (selected) {
                        if (component instanceof ColorizedTextField) {
                            ColorizedTextField textField = (ColorizedTextField) component;
                            textField.setBackground(textField
                                    .getCustomBackground());
                        } else if (component instanceof ColorizedComboBox) {
                            ColorizedComboBox comboBox = (ColorizedComboBox) component;
                            comboBox.setBackground(comboBox
                                    .getCustomBackground());
                        } else {
                            component.setBackground(_SELECTED_COLOR);
                        }
                    } else {
                        component.setBackground(_UNSELECTED_COLOR);
                    }
                } else {
                    component.setBackground(_DISABLED_BACKGROUND);
                }
            }
        }

        protected int _getColumnWidth(JComponent component) {
            if (component instanceof JTextField
                    || component instanceof JComboBox) {
                return 80;
            } else if (component instanceof JCheckBox) {
                return 40;
            } else {
                return 80;
            }
        }

        protected JComponent _getComponent(GTIngredientElement element) {
            JComponent component = null;
            if (element instanceof BooleanCriterionElement
                    || element instanceof BooleanOperationElement) {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                component = checkBox;
            } else if (element instanceof StringCriterionElement) {
                StringCriterionElement stringElement = (StringCriterionElement) element;
                boolean acceptRE = stringElement.acceptRegularExpression();
                boolean acceptExp = stringElement.acceptPtolemyExpression();
                Color background;
                if (acceptRE) {
                    background = _REGULAR_EXPRESSION_BACKGROUND;
                } else if (acceptExp) {
                    background = _PTOLEMY_EXPRESSION_BACKGROUND;
                } else {
                    background = _NON_REGULAR_EXPRESSION_BACKGROUND;
                }

                if (element instanceof ChoiceCriterionElement) {
                    ChoiceCriterionElement choiceElement = (ChoiceCriterionElement) element;
                    ColorizedComboBox comboBox = new ColorizedComboBox(
                            background);
                    comboBox.setEditable(choiceElement.isEditable());
                    for (Object choice : choiceElement.getChoices()) {
                        comboBox.addItem(choice);
                    }
                    component = comboBox;
                } else {
                    component = new ColorizedTextField(background);
                }
            } else if (element instanceof StringOperationElement) {
                StringOperationElement stringElement = (StringOperationElement) element;
                boolean acceptExp = stringElement.acceptPtolemyExpression();
                Color background;
                if (acceptExp) {
                    background = _PTOLEMY_EXPRESSION_BACKGROUND;
                } else {
                    background = _NON_REGULAR_EXPRESSION_BACKGROUND;
                }
                component = new ColorizedTextField(background);
            }
            return component;
        }

        protected void _initRightPanel() {
            _rightPanel.removeAll();

            ComboElement selectedElement = (ComboElement) _classSelector
                    .getSelectedItem();
            GTIngredient ingredient = selectedElement.getIngredient();
            GTIngredientElement[] elements = ingredient.getElements();
            _components = new JComponent[elements.length];
            _checkBoxes = new JCheckBox[elements.length];

            GridBagConstraints c = new GridBagConstraints();
            for (int i = 0; i < elements.length; i++) {
                GTIngredientElement element = elements[i];

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(new EmptyBorder(0, 3, 2, 3));
                panel.setOpaque(false);

                String columnName = element.getName();

                JPanel captionPanel = new JPanel(new FlowLayout(
                        FlowLayout.CENTER, 0, 0));
                captionPanel.setOpaque(false);
                captionPanel.setPreferredSize(new Dimension(0, 18));

                JCheckBox checkBox = null;
                boolean enabled;
                if (element.canDisable()) {
                    checkBox = new JCheckBox(columnName);
                    checkBox.setOpaque(false);
                    checkBox.setBorder(_EMPTY_BORDER);
                    checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                    checkBox.setVerticalAlignment(SwingConstants.TOP);
                    checkBox.addActionListener(new CheckBoxActionListener(i));
                    captionPanel.add(checkBox);

                    enabled = ingredient.isEnabled(i);
                    checkBox.setSelected(enabled);
                } else {
                    JLabel label = new JLabel(columnName);
                    captionPanel.add(label);

                    enabled = true;
                }
                panel.add(captionPanel, BorderLayout.NORTH);

                JComponent component = _getComponent(element);
                component.setPreferredSize(new Dimension(0, 20));
                _setComponentValue(element, component, ingredient.getValue(i));
                panel.add(component, BorderLayout.CENTER);

                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = _getColumnWidth(component);
                c.gridx = i + 1;
                c.gridy = 0;
                _rightPanel.add(panel, c);

                _checkBoxes[i] = checkBox;
                _components[i] = component;

                _setEnablement(component, enabled);
            }
        }

        protected void _setComponentValue(GTIngredientElement element,
                JComponent component, Object value) {
            if (element instanceof BooleanCriterionElement
                    || element instanceof BooleanOperationElement) {
                ((JCheckBox) component).setSelected(((Boolean) value)
                        .booleanValue());
            } else if (element instanceof StringCriterionElement) {
                if (element instanceof ChoiceCriterionElement) {
                    ((JComboBox) component).setSelectedItem(value.toString());
                } else {
                    ((JTextField) component).setText(value.toString());
                }
            } else if (element instanceof StringOperationElement) {
                ((JTextField) component).setText(value.toString());
            }
        }

        private void _setEnablement(JComponent component, boolean enabled) {
            component.setEnabled(enabled);
            if (enabled) {
                if (component instanceof ColorizedTextField) {
                    ColorizedTextField textField = (ColorizedTextField) component;
                    textField.setBackground(textField.getCustomBackground());
                } else if (component instanceof ColorizedComboBox) {
                    ColorizedComboBox comboBox = (ColorizedComboBox) component;
                    comboBox.setBackground(comboBox.getCustomBackground());
                } else {
                    component.setBackground(_SELECTED_COLOR);
                }
            } else {
                component.setBackground(_DISABLED_BACKGROUND);
            }
        }

        private JCheckBox[] _checkBoxes;

        private JComboBox _classSelector = new ColorizedComboBox(
                _NON_REGULAR_EXPRESSION_BACKGROUND);

        private JComponent[] _components;

        private JPanel _leftPanel = new JPanel(new BorderLayout());

        private JPanel _rightPanel = new JPanel(new GridBagLayout());

        private class CheckBoxActionListener implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean selected = ((JCheckBox) e.getSource()).isSelected();
                _setEnablement(_components[_index], selected);
            }

            CheckBoxActionListener(int index) {
                _index = index;
            }

            private int _index;
        }
    }

    static {
        _criterionClasses = searchIngredientClasses(
                new String[] { "ptolemy.actor.gt.ingredients.criteria" },
                ClassLoader.getSystemClassLoader());
        _operationClasses = searchIngredientClasses(
                new String[] { "ptolemy.actor.gt.ingredients.operations" },
                ClassLoader.getSystemClassLoader());
    }
}

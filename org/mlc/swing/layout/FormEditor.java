/*
 * Copyright (c) 2004-2007 by Michael Connor. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of FormLayoutBuilder or Michael Connor nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mlc.swing.layout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.AbstractSpinnerModel;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;

/**
 * This is the main panel that is used in LayoutFrame serving as the user
 * interface for the builder. This is a pretty juicy file because there is a lot
 * going on here. Better docs to come...
 *
 * @author Michael Connor mlconnor&#064;yahoo.com
@version $Id$
@since Ptolemy II 8.0
 */
@SuppressWarnings("serial")
public class FormEditor extends JPanel {
    String[] verticalAlignmentList = { LayoutConstraintsManager.DEFAULT,
            LayoutConstraintsManager.FILL, LayoutConstraintsManager.CENTER,
            LayoutConstraintsManager.TOP, LayoutConstraintsManager.BOTTOM };

    String[] horizontalAlignmentList = { LayoutConstraintsManager.DEFAULT,
            LayoutConstraintsManager.FILL, LayoutConstraintsManager.CENTER,
            LayoutConstraintsManager.LEFT, LayoutConstraintsManager.RIGHT };

    ColSpanSpinnerModel colSpinnerModel = new ColSpanSpinnerModel();

    RowSpanSpinnerModel rowSpinnerModel = new RowSpanSpinnerModel();

    Action newComponentAction = new NewComponentAction();

    Action removeComponentAction = new RemoveComponentAction();

    Action insertRowBeforeAction = new InsertRowBeforeAction();

    Action insertRowAfterAction = new InsertRowAfterAction();

    Action deleteRowAction = new DeleteRowAction();

    Action insertColumnBeforeAction = new InsertColumnBeforeAction();

    Action insertColumnAfterAction = new InsertColumnAfterAction();

    Action deleteColumnAction = new DeleteColumnAction();

    JComboBox verticalAlignmentCombo = new JComboBox(verticalAlignmentList);

    JComboBox horizontalAlignmentCombo = new JComboBox(horizontalAlignmentList);

    JSpinner rowSpanSpinner = new JSpinner(rowSpinnerModel);

    JSpinner columnSpanSpinner = new JSpinner(colSpinnerModel);

    JLabel columnSpanLabel = new JLabel("Column Span");

    JLabel horizontalAlignmentLabel = new JLabel("Horizontal Alignment");

    JLabel rowSpanLabel = new JLabel("Row Span");

    JLabel verticalAlignmentLabel = new JLabel("Vertical Alignment");

    JPanel contentPanel = new JPanel();

    JPanel insetsPanel = new JPanel();

    SpinnerNumberModel rightInsetSpinnerModel = new SpinnerNumberModel(0, 0,
            Integer.MAX_VALUE, 1);

    SpinnerNumberModel topInsetSpinnerModel = new SpinnerNumberModel(0, 0,
            Integer.MAX_VALUE, 1);

    SpinnerNumberModel bottomInsetSpinnerModel = new SpinnerNumberModel(0, 0,
            Integer.MAX_VALUE, 1);

    SpinnerNumberModel leftInsetSpinnerModel = new SpinnerNumberModel(0, 0,
            Integer.MAX_VALUE, 1);

    JSpinner rightInsetSpinner = new JSpinner(rightInsetSpinnerModel);

    JSpinner bottomInsetSpinner = new JSpinner(bottomInsetSpinnerModel);

    JSpinner leftInsetSpinner = new JSpinner(leftInsetSpinnerModel);

    JSpinner topInsetSpinner = new JSpinner(topInsetSpinnerModel);

    GridTableModel tableModel = new GridTableModel();

    JLabel insetsLabel = new JLabel("Insets");

    JLabel componentsLabel = new JLabel("Components (Drag n Drop)");

    JLabel componentPaletteLabel = new JLabel("Palette (Drag n Drop)");

    ComponentPaletteListModel componentPaletteListModel = new ComponentPaletteListModel();

    // KBR JList componentPalette = new JList(componentPaletteListModel);
    DndList componentPalette = new DndList(this, componentPaletteListModel);

    JScrollPane componentPaletteScrollPane = new JScrollPane(componentPalette);

    ComponentSelectionListModel componentSelectionListModel = new ComponentSelectionListModel();

    DndList componentList = new DndList(this, componentSelectionListModel);

    JScrollPane componentListScrollPane = new JScrollPane(componentList);

    ComponentListCellRenderer componentListCellRenderer = new ComponentListCellRenderer();

    Component constraintsSeparator = DefaultComponentFactory.getInstance()
            .createSeparator("Component Constraints");

    Component positionsSeparator = DefaultComponentFactory.getInstance()
            .createSeparator("Component Positions (Drag n Drop)");

    JPanel componentsPanel = new JPanel();

    JPanel propertiesPanel = new JPanel();

    JSplitPane componentsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            componentsPanel, propertiesPanel);

    JTextField colSpecField = new JTextField();

    JTextField rowSpecField = new JTextField();

    Set<Component> newComponents = new HashSet<Component>();

    LayoutConstraintsManager layoutConstraintsManager;

    JToolBar toolbar = new JToolBar();

    JButton newComponentButton = new JButton(newComponentAction);

    JButton removeComponentButton = new JButton(removeComponentAction);

    JButton columnDeleteButton = new JButton(deleteColumnAction);

    JButton columnInsertAfterButton = new JButton(insertColumnAfterAction);

    JButton columnInsertBeforeButton = new JButton(insertColumnBeforeAction);

    JButton rowDeleteButton = new JButton(deleteRowAction);

    JButton rowInsertBeforeButton = new JButton(insertRowBeforeAction);

    JButton rowInsertAfterButton = new JButton(insertRowAfterAction);

    Container container;

    ContainerLayout containerLayout;

    MultiContainerFrame layoutFrame;
    DnDTable table = null;
    JScrollPane tableScrollPane = null;
    JSplitPane constraintsSplitPane = null;

    Component topComponent = null;

    boolean suspendConstraintControlUpdates = false;

    void setContainer(Container container) {
        java.awt.LayoutManager layoutManager = container.getLayout();
        if (!(layoutManager instanceof ContainerLayout)) {
            throw new RuntimeException(
                    "Container layout must be of type ContainerLayout");
        }
        this.container = container;
    }

    public FormEditor(MultiContainerFrame layoutFrame, ContainerLayout layout,
            Container container) {
        super();

        this.layoutFrame = layoutFrame;
        table = new DnDTable(layoutFrame, this);
        tableScrollPane = new JScrollPane(table);
        constraintsSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tableScrollPane, componentsSplitPane);

        setContainer(container);
        containerLayout = layout;

        table.setBackground(java.awt.Color.white);
        table.setSelectionBackground(new Color(220, 220, 255));
        table.setSelectionForeground(Color.black);

        table.setDefaultRenderer(Object.class,
                new ConstraintTableCellRenderer());
        table.setRowHeight(20);
        table.setModel(tableModel);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // let's put the cursor in the table so the create component
        // icon is enabled. at least then the user knows what to do...
        if (tableModel.getRowCount() > 1 && tableModel.getColumnCount() > 1) {
            // KBR do NOT force visible so constraints row can still be seen
            setSelectedCell(1, 1, false);
        }

        componentList.setCellRenderer(componentListCellRenderer);

        // let's setup all of the usability stuff...
        componentsLabel.setLabelFor(componentListScrollPane);
        // componentsLabel.setDisplayedMnemonic(KeyEvent.VK_S);
        verticalAlignmentLabel.setLabelFor(verticalAlignmentCombo);
        verticalAlignmentLabel.setDisplayedMnemonic(KeyEvent.VK_V);
        horizontalAlignmentLabel.setLabelFor(horizontalAlignmentCombo);
        horizontalAlignmentLabel.setDisplayedMnemonic(KeyEvent.VK_H);
        columnSpanLabel.setLabelFor(columnSpanSpinner);
        columnSpanLabel.setDisplayedMnemonic(KeyEvent.VK_C);
        rowSpanLabel.setLabelFor(rowSpanSpinner);
        rowSpanLabel.setDisplayedMnemonic(KeyEvent.VK_R);

        columnInsertAfterButton
        .setToolTipText("Insert a column after this column");
        columnInsertBeforeButton
        .setToolTipText("Insert a column before this column");
        columnDeleteButton.setToolTipText("Delete this column");
        rowInsertBeforeButton.setToolTipText("Insert a row before this row");
        rowInsertAfterButton.setToolTipText("Insert a row after this row");

        // let's setup the table toolbar
        toolbar.add(newComponentButton);
        toolbar.add(removeComponentButton);
        toolbar.addSeparator();
        toolbar.add(columnDeleteButton);
        toolbar.add(columnInsertBeforeButton);
        toolbar.add(columnInsertAfterButton);
        toolbar.addSeparator();
        toolbar.add(rowDeleteButton);
        toolbar.add(rowInsertBeforeButton);
        toolbar.add(rowInsertAfterButton);

        setFormComponent(null);

        layoutConstraintsManager = LayoutConstraintsManager
                .getLayoutConstraintsManager(this.getClass()
                        .getResourceAsStream("editableLayoutConstraints.xml"));

        layoutConstraintsManager.setLayout("mainLayout", contentPanel);
        layoutConstraintsManager.setLayout("insetsLayout", insetsPanel);
        layoutConstraintsManager.setLayout("componentsLayout", componentsPanel);
        layoutConstraintsManager.setLayout("propertiesLayout", propertiesPanel);

        insetsPanel.add(rightInsetSpinner, "rightInsetSpinner");
        insetsPanel.add(leftInsetSpinner, "leftInsetSpinner");
        insetsPanel.add(topInsetSpinner, "topInsetSpinner");
        insetsPanel.add(bottomInsetSpinner, "bottomInsetSpinner");

        componentsPanel.add(componentListScrollPane, "componentListScrollPane");
        componentsPanel.add(componentsLabel, "componentsLabel");
        propertiesPanel.add(componentPaletteScrollPane,
                "componentPaletteScrollPane");
        componentPalette.setCellRenderer(new ComponentPaletteListRenderer());
        propertiesPanel.add(componentPaletteLabel, "componentPaletteLabel");

        contentPanel.add(rowSpanLabel, "rowSpanLabel");
        contentPanel.add(horizontalAlignmentCombo, "horizontalAlignmentCombo");
        contentPanel.add(horizontalAlignmentLabel, "horizontalAlignmentLabel");
        contentPanel.add(rowSpanSpinner, "rowSpanSpinner");
        contentPanel.add(verticalAlignmentCombo, "verticalAlignmentCombo");
        contentPanel.add(columnSpanLabel, "columnSpanLabel");
        contentPanel.add(verticalAlignmentLabel, "verticalAlignmentLabel");
        contentPanel.add(columnSpanSpinner, "columnSpanSpinner");
        contentPanel.add(insetsPanel, "insetsPanel");
        contentPanel.add(insetsLabel, "insetsLabel");
        contentPanel.add(constraintsSeparator, "constraintsSeparator");
        contentPanel.add(positionsSeparator, "positionsSeparator");
        contentPanel.add(toolbar, "toolbar");
        contentPanel.add(constraintsSplitPane, "constraintsSplitPane");

        constraintsSplitPane.setDividerLocation(605);

        contentPanel.setBorder(Borders.DIALOG_BORDER);

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        setupListeners();
    }

    private void setupListeners() {

        verticalAlignmentCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component component = table.getSelectedControl();
                if (component != null) {
                    CellConstraints cellConstraints = getComponentConstraints(component);
                    cellConstraints.vAlign = LayoutConstraintsManager
                            .getAlignment((String) verticalAlignmentCombo
                                    .getSelectedItem());
                    updateLayout(component);
                }
            }
        });

        horizontalAlignmentCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component component = table.getSelectedControl();
                if (component != null) {
                    CellConstraints cellConstraints = getComponentConstraints(component);
                    cellConstraints.hAlign = LayoutConstraintsManager
                            .getAlignment((String) horizontalAlignmentCombo
                                    .getSelectedItem());
                    updateLayout(component);
                }
            }
        });

        topInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!suspendConstraintControlUpdates) {
                    Component component = table.getSelectedControl();
                    CellConstraints constraints = getComponentConstraints(component);
                    Insets insets = new Insets(topInsetSpinnerModel.getNumber()
                            .intValue(), constraints.insets.left,
                            constraints.insets.bottom, constraints.insets.right);
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });

        leftInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!suspendConstraintControlUpdates) {
                    Component component = table.getSelectedControl();
                    CellConstraints constraints = getComponentConstraints(component);
                    Insets insets = new Insets(constraints.insets.top,
                            leftInsetSpinnerModel.getNumber().intValue(),
                            constraints.insets.bottom, constraints.insets.right);
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });

        rightInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!suspendConstraintControlUpdates) {
                    Component component = table.getSelectedControl();
                    CellConstraints constraints = getComponentConstraints(component);
                    Insets insets = new Insets(constraints.insets.top,
                            constraints.insets.left, constraints.insets.bottom,
                            rightInsetSpinnerModel.getNumber().intValue());
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });

        bottomInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!suspendConstraintControlUpdates) {
                    Component component = table.getSelectedControl();
                    CellConstraints constraints = getComponentConstraints(component);
                    Insets insets = new Insets(constraints.insets.top,
                            constraints.insets.left, bottomInsetSpinnerModel
                            .getNumber().intValue(),
                            constraints.insets.right);
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Point p = e.getPoint();
                    int row = table.rowAtPoint(p);
                    int col = table.columnAtPoint(p);
                    // support double-click:
                    Component component = table.getSelectedControl();
                    if (component == null) {
                        return;
                    }

                    /* invoke componentDef editor on double-clicked control */
                    String name = getComponentName(component);
                    ComponentDef componentDef = containerLayout
                            .getComponentDef(name);
                    if (componentDef != null) {
                        editComponent(componentDef, component,
                                new CellConstraints(col, row));
                    }
                }
            }
        });
        componentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    /* set selected component as selected, which causes a
                     * scroll-to-visible in the table */
                    Component thisComponent = (Component) componentList
                            .getSelectedValue();

                    CellConstraints constraints = getComponentConstraints(thisComponent);
                    if (constraints == null) {
                        throw new RuntimeException(
                                "Unable to find constraints for component "
                                        + thisComponent + " in layout "
                                        + containerLayout.getName());
                    }
                    int col = constraints.gridX;
                    int row = constraints.gridY;

                    table.changeSelection(row, col, false, false);
                    topComponent = thisComponent;
                }
            }
        });

        componentList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = componentList.locationToIndex(e.getPoint());
                    if (index == -1) {
                        return;
                    }

                    // Get item
                    ListModel lm = ((DndList) e.getSource()).getModel();
                    Component thisComponent = (Component) lm
                            .getElementAt(index);

                    String name = getComponentName(thisComponent);
                    ComponentDef compDef = containerLayout
                            .getComponentDef(name);
                    CellConstraints constraints = getComponentConstraints(thisComponent);
                    if (constraints == null) {
                        throw new RuntimeException(
                                "Unable to find constraints for component "
                                        + thisComponent + " in layout "
                                        + containerLayout.getName());
                    }
                    editComponent(compDef, thisComponent, constraints);
                }
            }
        });
    }

    String getComponentName(Component control) {
        return containerLayout.getComponentName(control);
    }

    CellConstraints getComponentConstraints(Component component) {
        return containerLayout.getComponentConstraints(component);
    }

    private void specsChanged() {
        updateLayouts();

        // lets go down the tree
        Component[] children = container.getComponents();
        for (Component component : children) {
            if (component instanceof Container) {
                ((Container) component).doLayout();
            }
        }
    }

    void updateLayouts() {
        container.validate();
        container.doLayout();

        Container parent = container;

        while (parent != null) {
            parent.validate();
            parent = parent.getParent();
        }
    }

    private void setSelectedCell(int columnIndex, int rowIndex,
            boolean forceVisible) {
        // we don't want to update the selection interval if nothing changed...
        table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
        table.getColumnModel().getSelectionModel()
        .setSelectionInterval(columnIndex, columnIndex);

        if (forceVisible) {
            // let's make sure the cell is in the visible range...
            JViewport viewport = (JViewport) table.getParent();
            Rectangle rect = table.getCellRect(rowIndex, columnIndex, true);
            Point pt = viewport.getViewPosition();
            rect.setLocation(rect.x - pt.x, rect.y - pt.y);
            viewport.scrollRectToVisible(rect);
        }
    }

    private class ComponentSelectionListModel extends
    javax.swing.AbstractListModel {
        //    private String selectedName = null;

        List<Component> sortedComponents = new ArrayList<Component>();

        public ComponentSelectionListModel() {
            super();
        }

        // Bug: when the user does the following:
        // 1) drags a new control into place
        // 2) drags a 2nd new control into place
        // 3) deletes the 2nd control
        // 4) drags a new 2nd control into place
        // we get an array out of bounds exception in getElementAt. The
        // delete probably failed to update our list.
        @Override
        public Object getElementAt(int index) {
            Component component = sortedComponents.get(index);
            return component;
        }

        @Override
        public int getSize() {
            sortedComponents = new ArrayList<Component>();

            if (container != null) {
                Component[] containerComponents = container.getComponents();
                for (Component insertComponent : containerComponents) {
                    String insertComponentName = getComponentName(insertComponent);

                    int insertIndex = 0;
                    while (insertIndex < sortedComponents.size()
                            && insertComponentName != null) {
                        Component testComponent = sortedComponents
                                .get(insertIndex);
                        String testName = getComponentName(testComponent);
                        if (testName != null) {
                            testName = testName
                                    .toUpperCase(Locale.getDefault());
                        }
                        if (insertComponentName
                                .toUpperCase(Locale.getDefault()).compareTo(
                                        testName) <= 0) {
                            break;
                        } else {
                            insertIndex++;
                        }
                    }
                    sortedComponents.add(insertIndex, insertComponent);
                }
            }

            return container != null ? container.getComponentCount() : 0;
        }

        public void fireDelete() {
            super.fireContentsChanged(this, 0,
                    Math.max(0, container.getComponents().length - 1));
        }

        public void fireInsert() {
            super.fireContentsChanged(this, 0,
                    Math.max(0, container.getComponents().length - 1));
        }
    }

    class ComponentListCellRenderer extends JLabel implements ListCellRenderer {
        private static final long serialVersionUID = 1L;
        Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public ComponentListCellRenderer() {
            super();
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component component = (Component) value;
            String name = getComponentName(component);

            setComponentOrientation(list.getComponentOrientation());
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(name != null ? name : "(Untitled)");

            setEnabled(list.isEnabled());
            Font font = list.getFont();
            setFont(font.deriveFont(component.isVisible() ? Font.PLAIN
                    : Font.BOLD));
            setBorder(cellHasFocus ? UIManager
                    .getBorder("List.focusCellHighlightBorder") : noFocusBorder);

            return this;
        }
    }

    private void insertColumn(int column) {
        for (int index = 0; index < container.getComponentCount(); index++) {
            Component component = container.getComponent(index);
            CellConstraints constraints = getComponentConstraints(component);
            if (constraints.gridX > column) {
                constraints.gridX++;
            }
        }

        try {
            containerLayout.addColumnSpec(column, "pref");
            tableModel.fireTableStructureChanged();
            setSelectedCell(column + 1, 0, true);
            specsChanged();
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(FormEditor.this, iae.getMessage(),
                    "Invalid Layout", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void insertRow(int rowIndex) {
        for (int index = 0; index < container.getComponentCount(); index++) {
            Component component = container.getComponent(index);
            CellConstraints constraints = getComponentConstraints(component);
            if (constraints.gridY > rowIndex) {
                constraints.gridY++;
            }
        }

        try {
            containerLayout.addRowSpec(rowIndex, "pref");
            tableModel.fireTableStructureChanged();
            setSelectedCell(0, rowIndex + 1, true);
            specsChanged();
        } catch (IllegalArgumentException iae) {
            JOptionPane.showMessageDialog(FormEditor.this, iae.getMessage(),
                    "Invalid Layout", JOptionPane.ERROR_MESSAGE);
        }
    }

    Component formComponent = null;

    // this method will act as the controller for the buttons
    // and the cell constraints form
    public void setFormComponent(Component component) {
        // KBR with this, selecting header row then table body row, doesn't enable
        // 'add'
        // if ( component == formComponent )
        // return;
        formComponent = component;

        CellConstraints constraints = formComponent != null ? getComponentConstraints(formComponent)
                : null;

        suspendConstraintControlUpdates = true;

        if (formComponent != null) {
            rowSpinnerModel.setComponent(formComponent);
            colSpinnerModel.setComponent(formComponent);
            verticalAlignmentCombo.setSelectedItem(LayoutConstraintsManager
                    .getAlignment(constraints.vAlign));
            horizontalAlignmentCombo.setSelectedItem(LayoutConstraintsManager
                    .getAlignment(constraints.hAlign));
            topInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.top));
            bottomInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.bottom));
            rightInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.right));
            leftInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.left));
        }

        verticalAlignmentCombo.setEnabled(constraints != null);
        horizontalAlignmentCombo.setEnabled(constraints != null);
        rightInsetSpinner.setEnabled(constraints != null);
        leftInsetSpinner.setEnabled(constraints != null);
        topInsetSpinner.setEnabled(constraints != null);
        bottomInsetSpinner.setEnabled(constraints != null);
        rowSpanSpinner.setEnabled(constraints != null);
        columnSpanSpinner.setEnabled(constraints != null);

        int col = table.getSelectedColumn();
        int row = table.getSelectedRow();

        // Don't allow 'add' on top of existing component
        newComponentAction.setEnabled(col > 0 && row > 0
                && formComponent == null);
        removeComponentAction.setEnabled(constraints != null);
        columnDeleteButton.setEnabled(row == 0 && col > 0
                && containerLayout.getColumnCount() > 1);
        columnInsertAfterButton.setEnabled(col > -1);
        columnInsertBeforeButton.setEnabled(col > 0);
        rowDeleteButton.setEnabled(col == 0 && row > 0
                && containerLayout.getRowCount() > 1);
        rowInsertBeforeButton.setEnabled(row > 0);
        rowInsertAfterButton.setEnabled(row > -1);

        suspendConstraintControlUpdates = false;
    }

    public void updateLayout(Component component) {
        if (suspendConstraintControlUpdates) {
            return;
        }

        CellConstraints constraints = getComponentConstraints(component);

        // we have to update the containerLayout which is the keeper of all
        // constraints. if we didn't do this then we wouldn't notice any changes
        // when we went to print everything out.
        String name = getComponentName(component);
        // i don't like this direct access thing. this should be changed...
        containerLayout.setCellConstraints(name, constraints);
        // updateForm();

        // be careful when modifying the next few lines of code. this
        // is tricky to get right. this seems to work.
        container.invalidate();
        container.doLayout();

        // KBR list (for example) doesn't seem to re-layout properly on drag&drop
        // without these
        container.validate();
        container.repaint();

        if (component instanceof Container) {
            Container cContainer = (Container) component;
            cContainer.invalidate();
            cContainer.doLayout();
        }
    }

    private class ColSpanSpinnerModel extends AbstractSpinnerModel {
        CellConstraints constraints;

        Component component;

        public ColSpanSpinnerModel() {
        }

        public void setComponent(Component component) {
            this.component = component;
            if (component != null) {
                constraints = getComponentConstraints(component);
                fireStateChanged();
            } else {
                constraints = null;
            }
        }

        @Override
        public Object getNextValue() {
            if (constraints == null) {
                return null;
            }
            Integer next = constraints.gridX + constraints.gridWidth - 1 < containerLayout
                    .getColumnCount() ? Integer
                            .valueOf(constraints.gridWidth + 1) : null;
                            return next;
        }

        @Override
        public Object getPreviousValue() {
            if (constraints == null) {
                return null;
            } else {
                Integer previous = constraints.gridWidth > 1 ? Integer
                        .valueOf(constraints.gridWidth - 1) : null;
                        return previous;
            }
        }

        @Override
        public Object getValue() {
            if (constraints == null) {
                return "";
            } else {
                return Integer.valueOf(constraints.gridWidth);
            }
        }

        @Override
        public void setValue(Object value) {
            if (constraints == null || value == null) {
                return;
            }

            //      Number val = (Number) value;
            constraints.gridWidth = ((Number) value).intValue();
            super.fireStateChanged();
            updateLayout(component);

            // firing table data changed messes up the
            // selection so we'll get it and then restore it...
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            tableModel.fireTableDataChanged();
            setSelectedCell(col, row, true);
        }
    }

    private class RowSpanSpinnerModel extends AbstractSpinnerModel {
        CellConstraints constraints;

        Component component;

        public RowSpanSpinnerModel() {
        }

        public void setComponent(Component component) {
            this.component = component;
            if (component != null) {
                constraints = getComponentConstraints(component);
                fireStateChanged();
            } else {
                constraints = null;
            }
        }

        @Override
        public Object getNextValue() {
            if (constraints == null) {
                return null;
            } else {
                Integer next = constraints.gridY + constraints.gridHeight - 1 < containerLayout
                        .getRowCount() ? Integer
                                .valueOf(constraints.gridHeight + 1) : null;
                                return next;
            }
        }

        @Override
        public Object getPreviousValue() {
            if (constraints == null) {
                return null;
            } else {
                Integer previous = constraints.gridHeight > 1 ? Integer
                        .valueOf(constraints.gridHeight - 1) : null;
                        return previous;
            }
        }

        @Override
        public Object getValue() {
            if (constraints == null) {
                return "";
            } else {
                return Integer.valueOf(constraints.gridHeight);
            }
        }

        @Override
        public void setValue(Object value) {
            if (constraints == null || value == null) {
                return;
            }
            //      Number val = (Number) value;
            constraints.gridHeight = ((Number) value).intValue();
            super.fireStateChanged();
            updateLayout(component);

            // firing table data changed messes up the
            // selection so we'll get it and then restore it...
            int col = table.getSelectedColumn();
            int row = table.getSelectedRow();
            tableModel.fireTableDataChanged();
            setSelectedCell(col, row, true);
        }
    }

    /**
     * Returns true if the named component was created by hand in this session
     */
    public boolean isNewComponent(Component component) {
        return newComponents.contains(component);
    }

    private class NewComponentDialog0 extends JDialog {
        JTextField nameField = new JTextField();

        JLabel nameLabel = new JLabel("Name");

        JLabel typeLabel = new JLabel("Type");

        JButton okButton = new JButton("OK");

        JButton cancelButton = new JButton("Cancel");

        JComboBox typeCombo = new JComboBox();

        PropertyTableModel propertyTableModel = new PropertyTableModel();

        JTable propertyTable = new JTable();

        JScrollPane propertyScrollPane = new JScrollPane(propertyTable);

        boolean wasSuccessful = false;

        Component component = null;

        Map<String, Object> controlProperties = new HashMap<String, Object>();

        public NewComponentDialog0(Frame owner) throws Exception {
            super(owner, "Add Component", true);

            okButton.setMnemonic(KeyEvent.VK_O);
            cancelButton.setMnemonic(KeyEvent.VK_C);
            nameLabel.setDisplayedMnemonic(KeyEvent.VK_N);
            typeLabel.setDisplayedMnemonic(KeyEvent.VK_T);
            nameLabel.setLabelFor(nameField);
            typeLabel.setLabelFor(typeCombo);

            //      FormLayout layout = new FormLayout("right:max(40dlu;pref), 3dlu, 130dlu",
            //          "");
            JPanel content = new JPanel();
            content.setBorder(Borders.DIALOG_BORDER);
            layoutConstraintsManager.setLayout("newComponentContent", content);
            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(content, BorderLayout.CENTER);

            content.add(typeCombo, "typeCombo");
            content.add(nameField, "nameField");
            content.add(typeLabel, "typeLabel");
            content.add(nameLabel, "nameLabel");
            content.add(propertyScrollPane, "propertyScrollPane");
            content.add(com.jgoodies.forms.factories.ButtonBarFactory
                    .buildRightAlignedBar(new JButton[] { okButton,
                            cancelButton }), "buttonPanel");

            propertyTable.putClientProperty("terminateEditOnFocusLost",
                    Boolean.TRUE);
            propertyTable.setModel(propertyTableModel);

            pack();

            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JButton.class, new String[] { "text" }));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JCheckBox.class, new String[] { "text" }));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JComboBox.class));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JLabel.class, new String[] { "text" }));
            typeCombo.addItem(new JListComponentBuilder());
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JPanel.class));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JPasswordField.class));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JRadioButton.class, new String[] { "text" }));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JScrollPane.class));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JSpinner.class));
            typeCombo.addItem(new JTableComponentBuilder());
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JTextArea.class));
            typeCombo.addItem(new DefaultComponentBuilder(
                    javax.swing.JTextField.class));
            typeCombo.addItem(new JToolBarComponentBuilder());
            typeCombo.addItem(new JTreeComponentBuilder());
            typeCombo.addItem(new SeparatorComponentBuilder());
            typeCombo.addItem(new ButtonBarComponentBuilder());

            typeCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    controlProperties = new HashMap<String, Object>();
                    propertyTableModel.fireTableDataChanged();
                }
            });

            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    try {
                        if (nameField.getText().trim().length() == 0) {
                            throw new Exception("The name field is required");
                        }

                        //            int currentCol = propertyTable.getSelectedColumn();
                        //            int currentRow = propertyTable.getSelectedRow();

                        ComponentBuilder builder = (ComponentBuilder) typeCombo
                                .getSelectedItem();
                        component = builder.getInstance(controlProperties);

                        wasSuccessful = true;
                        dispose();

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        wasSuccessful = false;
                        JOptionPane.showMessageDialog(null,
                                exception.getMessage(),
                                "Error Creating Component",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    wasSuccessful = false;
                    dispose();
                }
            });
        }

        public boolean wasSuccessful() {
            return wasSuccessful;
        }

        public String getComponentName() {
            return nameField.getText();
        }

        //        public String getComponentDeclaration() {
        //            ComponentBuilder builder = (ComponentBuilder) typeCombo
        //                    .getSelectedItem();
        //            return builder
        //                    .getDeclaration(getComponentName(), controlProperties);
        //        }

        public boolean isUsingLayoutComponent() {
            ComponentBuilder builder = (ComponentBuilder) typeCombo
                    .getSelectedItem();
            return builder.isComponentALayoutContainer();
        }

        public Component getComponent() {
            return component;
        }

        public ComponentDef getComponentDef() {
            ComponentBuilder builder = (ComponentBuilder) typeCombo
                    .getSelectedItem();
            return builder.getComponentDef(getComponentName(),
                    controlProperties);
        }

        private class PropertyTableModel extends
        javax.swing.table.AbstractTableModel {
            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public int getRowCount() {
                ComponentBuilder builder = (ComponentBuilder) typeCombo
                        .getSelectedItem();
                return builder != null ? builder.getProperties().size() : 0;
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 1;
            }

            @Override
            public String getColumnName(int col) {
                return col == 0 ? "Property" : "Value";
            }

            @Override
            public void setValueAt(Object aValue, int row, int col) {
                ComponentBuilder builder = (ComponentBuilder) typeCombo
                        .getSelectedItem();
                List<BeanProperty> properties = builder.getProperties();
                BeanProperty property = properties.get(row);
                controlProperties.put(property.getName(), aValue);
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                ComponentBuilder builder = (ComponentBuilder) typeCombo
                        .getSelectedItem();
                List<BeanProperty> properties = builder.getProperties();
                BeanProperty property = properties.get(rowIndex);

                return columnIndex == 0 ? property.getName()
                        : controlProperties.get(property.getName());
            }

            //            public Component getComponent() throws Exception {
            //                ComponentBuilder builder = (ComponentBuilder) typeCombo
            //                        .getSelectedItem();
            //                Component instance = builder.getInstance(controlProperties);
            //                return instance;
            //            }
        }
    }

    private class ConstraintTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            String stringValue = null;
            if (value != null) {
                if (value instanceof Component) {
                    String name = getComponentName((Component) value);
                    stringValue = name == null ? "(Untitled)" : name;
                } else {
                    // in this case it's a row or col header
                    stringValue = (String) value;
                }
            }

            return super.getTableCellRendererComponent(table, stringValue,
                    isSelected, hasFocus, row, column);
        }
    }

    private class GridTableModel extends javax.swing.table.AbstractTableModel {

        @Override
        public int getColumnCount() {
            return containerLayout != null ? containerLayout.getColumnCount() + 1
                    : 1;
        }

        @Override
        public int getRowCount() {
            return containerLayout != null ? containerLayout.getRowCount() + 1
                    : 1;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return (row == 0 || col == 0) && !(row == 0 && col == 0);
        }

        @Override
        public String getColumnName(int col) {
            return col == 0 ? "*" : "" + col;
        }

        @Override
        public void setValueAt(Object aValue, int row, int col) {
            String value = (String) aValue;
            if (row == 0) // a column was changed
            {
                try {
                    containerLayout.setColumnSpec(col - 1, value);
                    specsChanged();
                } catch (IllegalArgumentException iae) {
                    JOptionPane.showMessageDialog(FormEditor.this,
                            iae.getMessage(), "Invalid Layout",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (col == 0) {
                try {
                    containerLayout.setRowSpec(row - 1, value);
                    specsChanged();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(),
                            "Invalid row specification",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == 0 && columnIndex == 0) {
                return null;
            }
            if (rowIndex == 0) {
                return containerLayout.getColumnSpec(columnIndex - 1);
            }
            if (columnIndex == 0) {
                return containerLayout.getRowSpec(rowIndex - 1);
            }

            Component component = null;

            for (int index = 0; index < container.getComponentCount(); index++) {
                Component thisComponent = container.getComponent(index);
                // we don't want to show invisible components. we
                // have decided to make components that are added without
                // constraints invisible until they are dropped onto the form.
                // this is our way of hiding them.
                if (thisComponent.isVisible()) {
                    CellConstraints constraints = getComponentConstraints(thisComponent);
                    if (constraints == null) {
                        throw new RuntimeException(
                                "Unable to find constraints for component "
                                        + thisComponent + " in layout "
                                        + containerLayout.getName());
                    }
                    if (columnIndex >= constraints.gridX
                            && columnIndex < constraints.gridX
                            + constraints.gridWidth
                            && rowIndex >= constraints.gridY
                            && rowIndex < constraints.gridY
                            + constraints.gridHeight) {
                        component = thisComponent;
                        if (component == topComponent) {
                            break;
                        }
                    }
                }
            }

            return component;
        }
    }

    private class RemoveComponentAction extends AbstractAction {
        public RemoveComponentAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Remove the component (Alt+D)");
            putValue(Action.LONG_DESCRIPTION, "Remove the component (Alt+D)");
            putValue(Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class.getResource("Remove24.gif")));
            putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Component selectedControl = table.getSelectedControl();
            String controlName = getComponentName(selectedControl);
            container.remove(selectedControl);
            tableModel.fireTableDataChanged();

            if (selectedControl instanceof Container
                    && layoutFrame.hasContainer(controlName)) {
                layoutFrame.removeContainer(controlName);
            }
            container.doLayout();
            container.repaint();
            componentSelectionListModel.fireDelete();

            setFormComponent(null);
            table.requestFocus();
        }
    }

    private class NewComponentAction extends AbstractAction {
        public NewComponentAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Create a new component (Alt+N)");
            putValue(Action.LONG_DESCRIPTION, "Create a new component (Alt+N)");
            putValue(Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class.getResource("New24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_N));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Frame frame = (Frame) SwingUtilities.getAncestorOfClass(
                    Frame.class, FormEditor.this);

            NewComponentDialog0 newComponentDialog = null;
            int columnIndex = table.getSelectedColumn();
            int rowIndex = table.getSelectedRow();

            try {
                newComponentDialog = new NewComponentDialog0(frame);
                newComponentDialog.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (newComponentDialog.wasSuccessful()) {
                String controlName = newComponentDialog.getComponentName();
                Component newControl = newComponentDialog.getComponent();
                ComponentDef newCD = newComponentDialog.getComponentDef();

                if (containerLayout.getCellConstraints(controlName) != null) {
                    JOptionPane.showMessageDialog(FormEditor.this,
                            "A component named '" + controlName
                            + "' already exists", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    // the best way to add this control is to setup the constraints
                    // in the map of name->constraints and then add it to the container.
                    // this layout manager will intercept it, find the constraints and
                    // then
                    // set it up properly in the table and assign the maps.
                    CellConstraints newConstraints = new CellConstraints(
                            columnIndex, rowIndex);
                    if (newCD != null) {
                        containerLayout.addComponent(controlName, newCD,
                                newConstraints);
                    }
                    containerLayout.getCellConstraints().put(controlName,
                            newConstraints);
                    container.add(newControl, controlName);

                    // we need to keep track of the new components added so we can present
                    // some code to the user to add back into their module.
                    // newComponents.put(controlName,
                    // newComponentDialog.getComponentDeclaration());

                    // if it's a panel, let's add it to the LayoutFrame so it can be
                    // manipulated too...
                    if (newComponentDialog.isUsingLayoutComponent()) {
                        Container newContainer = (Container) newControl;
                        layoutFrame.addContainer(controlName, newContainer);
                    }

                    componentSelectionListModel.fireInsert();

                    table.changeSelection(newConstraints.gridY,
                            newConstraints.gridX, false, false);
                    updateLayout(newControl); // relayout preview
                    updateLayouts(); // relayout all panels

                    newComponents.add(newControl); // we're not generating code unless this happens
                }
            }

            table.requestFocus();
        }
    }

    private class InsertRowBeforeAction extends AbstractAction {
        public InsertRowBeforeAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a row before the selected row");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a row before the selected row");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class
                            .getResource("RowInsertBefore24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            insertRow(row - 1);
            table.requestFocus();
        }
    }

    private class InsertRowAfterAction extends AbstractAction {
        public InsertRowAfterAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a row after the selected row");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a row after the selected row");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class
                            .getResource("RowInsertAfter24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_O));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = table.getSelectedRow();
            insertRow(row);
            table.requestFocus();
        }
    }

    private class InsertColumnBeforeAction extends AbstractAction {
        public InsertColumnBeforeAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a column before the selected column");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a column before the selected column");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class
                            .getResource("ColumnInsertBefore24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_K));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int column = table.getSelectedColumn();
            insertColumn(column - 1);
            table.requestFocus();
        }
    }

    private class InsertColumnAfterAction extends AbstractAction {
        public InsertColumnAfterAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a column after the selected column");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a column after the selected column");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class
                            .getResource("ColumnInsertAfter24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int column = table.getSelectedColumn();
            insertColumn(column);
            table.requestFocus();
        }
    }

    private class DeleteRowAction extends AbstractAction {
        public DeleteRowAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Deletes the selected row");
            putValue(Action.LONG_DESCRIPTION, "Deletes the selected row");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class
                            .getResource("RowDelete24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int rowIndex = table.getSelectedRow();

            // move any components that are on the deleted row or
            // above it down one
            for (int index = 0; index < container.getComponentCount(); index++) {
                Component component = container.getComponent(index);
                CellConstraints constraints = getComponentConstraints(component);

                if (constraints.gridY >= rowIndex && constraints.gridY > 1) {
                    constraints.gridY--;
                } else {
                    // if the row deleted was within the span of the component and the
                    // component
                    // is bigger than one cell...
                    if (constraints.gridY + constraints.gridHeight - 1 >= rowIndex
                            && constraints.gridHeight > 1) {
                        constraints.gridHeight--;
                    }
                }
            }

            containerLayout.removeRowSpec(rowIndex - 1);

            tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
            table.changeSelection(
                    Math.min(rowIndex, containerLayout.getRowCount()), 0,
                    false, false);
            specsChanged();
            table.requestFocus();
        }
    }

    private class DeleteColumnAction extends AbstractAction {
        public DeleteColumnAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Deletes the selected column");
            putValue(Action.LONG_DESCRIPTION, "Deletes the selected column");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FormEditor.class
                            .getResource("ColumnDelete24.gif")));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int columnIndex = table.getSelectedColumn();

            for (int index = 0; index < container.getComponentCount(); index++) {
                Component component = container.getComponent(index);
                CellConstraints constraints = getComponentConstraints(component);

                if (constraints.gridX >= columnIndex && constraints.gridX > 1) {
                    constraints.gridX--;
                } else {
                    // if the col deleted was within the span of the component and the
                    // component
                    // is bigger than one cell...
                    if (constraints.gridX + constraints.gridWidth - 1 >= columnIndex
                            && constraints.gridWidth > 1) {
                        constraints.gridWidth--;
                    }
                }
            }

            containerLayout.removeColumnSpec(columnIndex - 1);
            tableModel.fireTableStructureChanged();
            table.changeSelection(0,
                    Math.min(columnIndex, containerLayout.getColumnCount()),
                    false, false);
            specsChanged();
            table.requestFocus();
        }
    }

    class ComponentPaletteListModel extends AbstractListModel {
        List<ComponentDef> componentDefs = ComponentDef.createComponentDefs();

        @Override
        public int getSize() {
            return componentDefs.size();
        }

        @Override
        public Object getElementAt(int index) {
            return componentDefs.get(index);
        }
    }

    class ComponentPaletteListRenderer extends JLabel implements
    ListCellRenderer {
        public ComponentPaletteListRenderer() {
            setOpaque(true);
        }

        /*
         * This method finds the image and text corresponding to the selected value
         * and returns the label, set up to display the text and image.
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            ComponentDef componentDef = (ComponentDef) value;
            setIcon(componentDef.icon != null ? componentDef.icon : null);
            setText(componentDef.name);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    public void updateList() {
        componentSelectionListModel.fireInsert();
    }

    public String uniqueName(String name, Component comp) {
        // insure the component name doesn't collide

        String newname = name;
        int suffix = 1;
        for (;;) {
            Component temp = containerLayout.getComponentByName(newname);
            // no such component, or found component was ourself, stop the search
            if (temp == null || temp == comp) {
                break; // exitloop
            }

            newname = name + "_" + suffix;
            suffix++;
        }
        return newname;
    }

    public boolean editComponent(ComponentDef componentDef,
            Component component, CellConstraints cellConstraints) {
        if (componentDef.isContainer) {
            return false; //punt!
        }

        // get original name for remove
        String name = getComponentName(component);

        NewComponentDialog dlg = NewComponentDialog.editDialog(
                (JFrame) layoutFrame, componentDef);
        if (!dlg.succeeded()) {
            return false;
        }

        componentDef.name = uniqueName(dlg.getComponentName(), component);
        String newname = componentDef.name;

        Component newcomponent = dlg.getInstance();
        containerLayout.removeLayoutComponent(component);
        containerLayout.addComponent(newname, componentDef, cellConstraints);

        container.remove(component);
        container.add(newcomponent, newname);

        newComponents.remove(component);
        newComponents.add(newcomponent);

        if (componentDef.isContainer) {
            /** @todo losing components INSIDE the container!! */
            //        layoutFrame.replaceContainer(name, newName, (Container) newcomponent);
            layoutFrame.removeContainer(name);
            layoutFrame.addContainer(newname, (Container) newcomponent);
        }

        updateLayout(newcomponent);
        updateList();
        updateLayouts();
        repaint();
        return true;
    }
}

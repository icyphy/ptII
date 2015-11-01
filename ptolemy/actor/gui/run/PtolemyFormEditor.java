/* A panel for editing the layout of a customizable run control panel.

 Copyright (c) 2007-2014 The Regents of the University of California.
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

 This class is based on FormEditor by Michael Connor, which
 bears the following copyright:

 * Copyright (c) 2004-2014 by Michael Connor. All Rights Reserved.
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
package ptolemy.actor.gui.run;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.AbstractSpinnerModel;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.mlc.swing.layout.ContainerLayout;
import org.mlc.swing.layout.FormEditor;
import org.mlc.swing.layout.LayoutConstraintsManager;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;

///////////////////////////////////////////////////////////////////
//// PtolemyFormEditor

/**
A customized version of the FormEditor class by
Michael Connor (mlconnor&#064;yahoo.com).

@see FormEditor
@author Michael Connor and Edward A. Lee
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class PtolemyFormEditor extends JPanel {

    /** Construct a new form editor.
     *  @param layoutFrame The frame within which this editor will be added.
     *  @param layout The layout manager.
     *  @param container The container.
     */
    public PtolemyFormEditor(RunLayoutFrame layoutFrame,
            ContainerLayout layout, Container container) {
        super();
        _layoutFrame = layoutFrame;

        // Create the layout table.
        _table = new LayoutTable(_layoutFrame, this);
        JScrollPane tableScrollPane = new JScrollPane(_table);

        // Create the component palette for this Ptolemy model.
        ComponentPaletteListModel componentPaletteListModel = new ComponentPaletteListModel();
        PaletteList componentPalette = new PaletteList(this,
                componentPaletteListModel);
        JScrollPane componentPaletteScrollPane = new JScrollPane(
                componentPalette);

        JPanel propertiesPanel = new JPanel();

        _container = container;
        _containerLayout = layout;

        _table.setBackground(java.awt.Color.white);
        _table.setSelectionBackground(new Color(220, 220, 255));
        _table.setSelectionForeground(Color.black);

        _table.setDefaultRenderer(Object.class,
                new ConstraintTableCellRenderer());
        _table.setRowHeight(20);
        _table.setModel(_tableModel);
        _table.setCellSelectionEnabled(true);
        _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JLabel verticalAlignmentLabel = new JLabel("Vertical Alignment");
        verticalAlignmentLabel.setLabelFor(_verticalAlignmentCombo);
        verticalAlignmentLabel.setDisplayedMnemonic(KeyEvent.VK_V);

        JLabel horizontalAlignmentLabel = new JLabel("Horizontal Alignment");
        horizontalAlignmentLabel.setLabelFor(_horizontalAlignmentCombo);
        horizontalAlignmentLabel.setDisplayedMnemonic(KeyEvent.VK_H);

        JLabel columnSpanLabel = new JLabel("Column Span");
        columnSpanLabel.setLabelFor(_columnSpanSpinner);
        columnSpanLabel.setDisplayedMnemonic(KeyEvent.VK_C);

        JLabel rowSpanLabel = new JLabel("Row Span");
        rowSpanLabel.setLabelFor(_rowSpanSpinner);
        rowSpanLabel.setDisplayedMnemonic(KeyEvent.VK_R);

        _columnInsertAfterButton
        .setToolTipText("Insert a column after this column");
        _columnInsertBeforeButton
        .setToolTipText("Insert a column before this column");
        _columnDeleteButton.setToolTipText("Delete this column");
        _rowInsertBeforeButton.setToolTipText("Insert a row before this row");
        _rowInsertAfterButton.setToolTipText("Insert a row after this row");

        JToolBar toolbar = new JToolBar();
        toolbar.add(_removeComponentButton);
        toolbar.addSeparator();
        toolbar.add(_columnDeleteButton);
        toolbar.add(_columnInsertBeforeButton);
        toolbar.add(_columnInsertAfterButton);
        toolbar.addSeparator();
        toolbar.add(_rowDeleteButton);
        toolbar.add(_rowInsertBeforeButton);
        toolbar.add(_rowInsertAfterButton);
        toolbar.addSeparator();
        toolbar.add(_packAction);

        // Specify that no component is selected.
        setFormComponent(null);

        LayoutConstraintsManager layoutConstraintsManager = LayoutConstraintsManager
                .getLayoutConstraintsManager(this
                        .getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                "/ptolemy/actor/gui/run/editableLayoutConstraints.xml"));

        JPanel insetsPanel = new JPanel();
        JPanel contentPanel = new JPanel();

        layoutConstraintsManager.setLayout("mainLayout", contentPanel);
        layoutConstraintsManager.setLayout("insetsLayout", insetsPanel);
        layoutConstraintsManager.setLayout("propertiesLayout", propertiesPanel);

        insetsPanel.add(_rightInsetSpinner, "rightInsetSpinner");
        insetsPanel.add(_leftInsetSpinner, "leftInsetSpinner");
        insetsPanel.add(_topInsetSpinner, "topInsetSpinner");
        insetsPanel.add(_bottomInsetSpinner, "bottomInsetSpinner");

        propertiesPanel.add(componentPaletteScrollPane,
                "componentPaletteScrollPane");
        componentPalette.setCellRenderer(new ComponentPaletteListRenderer());
        JLabel componentPaletteLabel = new JLabel("Palette");
        propertiesPanel.add(componentPaletteLabel, "componentPaletteLabel");

        contentPanel.add(rowSpanLabel, "rowSpanLabel");
        contentPanel.add(_horizontalAlignmentCombo, "horizontalAlignmentCombo");
        contentPanel.add(horizontalAlignmentLabel, "horizontalAlignmentLabel");
        contentPanel.add(_rowSpanSpinner, "rowSpanSpinner");
        contentPanel.add(_verticalAlignmentCombo, "verticalAlignmentCombo");
        contentPanel.add(columnSpanLabel, "columnSpanLabel");
        contentPanel.add(verticalAlignmentLabel, "verticalAlignmentLabel");
        contentPanel.add(_columnSpanSpinner, "columnSpanSpinner");
        contentPanel.add(insetsPanel, "insetsPanel");
        JLabel insetsLabel = new JLabel("Insets");
        contentPanel.add(insetsLabel, "insetsLabel");
        Component constraintsSeparator = DefaultComponentFactory.getInstance()
                .createSeparator("Component Constraints");
        contentPanel.add(constraintsSeparator, "constraintsSeparator");
        Component positionsSeparator = DefaultComponentFactory.getInstance()
                .createSeparator("Component Positions");
        contentPanel.add(positionsSeparator, "positionsSeparator");
        contentPanel.add(toolbar, "toolbar");

        // Put the layout table and palette side-by-side.
        JSplitPane constraintsSplitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, propertiesPanel);
        contentPanel.add(constraintsSplitPane, "constraintsSplitPane");
        // The following would make the palette invisible!
        // constraintsSplitPane.setDividerLocation(605);

        contentPanel.setBorder(Borders.DIALOG_BORDER);

        setLayout(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);

        _setupListeners();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Edit the component by presenting a dialog that infers the
     *  settable properties of the components.
     *  @param component The component.
     *  @return true upon successful completion.
     */
    public boolean editComponent(Component component) {
        String name = _containerLayout.getComponentName(component);
        Query query = new Query();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(component.getClass());
            PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
            Map<String, String> previousValues = new HashMap<String, String>();
            for (PropertyDescriptor propertyDescriptor : props) {
                // Present String and Color-valued properties.
                if (propertyDescriptor.getPropertyType() == String.class) {
                    String propertyName = propertyDescriptor.getName();
                    if (_propertiesToIgnore.contains(propertyName)) {
                        continue;
                    }
                    Method readMethod = propertyDescriptor.getReadMethod();
                    String value = (String) readMethod.invoke(component,
                            new Object[] {});
                    query.addLine(propertyName, propertyName, value);
                    previousValues.put(propertyName, value);
                } else if (propertyDescriptor.getPropertyType() == Color.class) {
                    String propertyName = propertyDescriptor.getName();
                    Method readMethod = propertyDescriptor.getReadMethod();
                    Color value = (Color) readMethod.invoke(component,
                            new Object[] {});
                    float[] components = value.getRGBComponents(null);
                    StringBuffer string = new StringBuffer("{");
                    // Use the syntax of arrays to present the color.
                    for (int j = 0; j < components.length; j++) {
                        string.append(components[j]);
                        if (j < components.length - 1) {
                            string.append(",");
                        } else {
                            string.append("}");
                        }
                    }
                    query.addColorChooser(propertyName, propertyName,
                            string.toString());
                    previousValues.put(propertyName, string.toString());
                }
            }
            ComponentDialog dialog = new ComponentDialog(_layoutFrame, name,
                    query);
            if (dialog.buttonPressed().equals("OK")) {
                // Set each property that has changed.
                for (PropertyDescriptor propertyDescriptor : props) {
                    // Present String and Color-valued properties.
                    if (propertyDescriptor.getPropertyType() == String.class) {
                        String propertyName = propertyDescriptor.getName();
                        if (_propertiesToIgnore.contains(propertyName)) {
                            continue;
                        }
                        String newValue = query.getStringValue(propertyName);
                        if (!newValue.equals(previousValues.get(propertyName))) {
                            Method writeMethod = propertyDescriptor
                                    .getWriteMethod();
                            writeMethod.invoke(component,
                                    new Object[] { newValue });
                            _containerLayout.setProperty(name, propertyName,
                                    newValue);
                        }
                    } else if (propertyDescriptor.getPropertyType() == Color.class) {
                        String propertyName = propertyDescriptor.getName();
                        String newValue = query.getStringValue(propertyName);
                        Color newColor = Query.stringToColor(newValue);
                        if (!newValue.equals(previousValues.get(propertyName))) {
                            Method writeMethod = propertyDescriptor
                                    .getWriteMethod();
                            writeMethod.invoke(component,
                                    new Object[] { newColor });
                            _containerLayout.setProperty(name, propertyName,
                                    newColor);
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            // FIXME Auto-generated catch block
            throwable.printStackTrace();
        }

        /* FIXME
         *

        NewComponentDialog dlg = NewComponentDialog.editDialog(
                (JFrame) _layoutFrame, componentDef);
        if (!dlg.succeeded())
            return false;

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
            // @todo losing components INSIDE the container!!
            //layoutFrame.replaceContainer(name, newName, (Container) newcomponent);
            layoutFrame.removeContainer(name);
            layoutFrame.addContainer(newname, (Container) newcomponent);
        }

        updateLayout(newcomponent);
        _updateList();
         */
        _updateLayouts();
        repaint();
        return true;
    }

    /** Specify the selected component. This causes the various
     *  constraints controls for the component to be enabled and
     *  to show the current values for the component.
     *  @param component The selected component.
     */
    public void setFormComponent(Component component) {
        CellConstraints constraints = component != null ? _getComponentConstraints(component)
                : null;

        _suspendConstraintControlUpdates = true;

        if (component != null) {
            _rowSpanSpinnerModel.setComponent(component);
            _columnSpanSpinnerModel.setComponent(component);
            _verticalAlignmentCombo.setSelectedItem(LayoutConstraintsManager
                    .getAlignment(constraints.vAlign));
            _horizontalAlignmentCombo.setSelectedItem(LayoutConstraintsManager
                    .getAlignment(constraints.hAlign));
            _topInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.top));
            _bottomInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.bottom));
            _rightInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.right));
            _leftInsetSpinnerModel.setValue(Integer
                    .valueOf(constraints.insets.left));
        }

        _verticalAlignmentCombo.setEnabled(constraints != null);
        _horizontalAlignmentCombo.setEnabled(constraints != null);
        _rightInsetSpinner.setEnabled(constraints != null);
        _leftInsetSpinner.setEnabled(constraints != null);
        _topInsetSpinner.setEnabled(constraints != null);
        _bottomInsetSpinner.setEnabled(constraints != null);
        _rowSpanSpinner.setEnabled(constraints != null);
        _columnSpanSpinner.setEnabled(constraints != null);

        int col = _table.getSelectedColumn();
        int row = _table.getSelectedRow();

        _removeComponentAction.setEnabled(constraints != null);
        _columnDeleteButton.setEnabled(row == 0 && col > 0
                && _containerLayout.getColumnCount() > 1);
        _columnInsertAfterButton.setEnabled(col > -1);
        _columnInsertBeforeButton.setEnabled(col > 0);
        _rowDeleteButton.setEnabled(col == 0 && row > 0
                && _containerLayout.getRowCount() > 1);
        _rowInsertBeforeButton.setEnabled(row > 0);
        _rowInsertAfterButton.setEnabled(row > -1);

        _suspendConstraintControlUpdates = false;
    }

    /** Update the layout for the specified component.
     *  @param component  The component to have its layout updated.
     */
    public void updateLayout(Component component) {
        if (_suspendConstraintControlUpdates) {
            return;
        }
        CellConstraints constraints = _getComponentConstraints(component);

        // we have to update the _containerLayout which is the keeper of all
        // constraints. if we didn't do this then we wouldn't notice any changes
        // when we went to print everything out.
        String name = _getComponentName(component);
        // i don't like this direct access thing. this should be changed...
        _containerLayout.setCellConstraints(name, constraints);
        // updateForm();

        // be careful when modifying the next few lines of code. this
        // is tricky to get right. this seems to work.
        _container.invalidate();
        _container.doLayout();

        // KBR list (for example) doesn't seem to re-layout properly on drag&drop
        // without these
        _container.validate();
        _container.repaint();

        if (component instanceof Container) {
            Container cContainer = (Container) component;
            cContainer.invalidate();
            cContainer.doLayout();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the layouts. */
    protected void _updateLayouts() {
        _container.validate();
        _container.doLayout();

        Container parent = _container;

        while (parent != null) {
            parent.validate();
            if (parent instanceof Window) {
                // Packing has the unfortunate
                // side effect of centering on the
                // screen, which is annoying...
                // ((Window)parent).pack();
                parent.setVisible(true);
            }
            parent = parent.getParent();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the component constraints from the layout manager.
     *  @param component The component.
     *  @return The constraints.
     */
    private CellConstraints _getComponentConstraints(Component component) {
        return _containerLayout.getComponentConstraints(component);
    }

    /** Return the name of the specified component as known by the layout
     *  controller.
     *  @param control The component.
     *  @return The name of the component in the layout.
     */
    private String _getComponentName(Component control) {
        return _containerLayout.getComponentName(control);
    }

    /** Insert a column.
     *  @param column The column index.
     */
    private void _insertColumn(int column) {
        for (int index = 0; index < _container.getComponentCount(); index++) {
            Component component = _container.getComponent(index);
            CellConstraints constraints = _getComponentConstraints(component);
            if (constraints.gridX > column) {
                constraints.gridX++;
            }
        }

        try {
            _containerLayout.addColumnSpec(column, "pref");
            _tableModel.fireTableStructureChanged();
            _setSelectedCell(column + 1, 0, true);
            _specsChanged();
        } catch (IllegalArgumentException iae) {
            // FIXME: Use our error reporting.
            JOptionPane.showMessageDialog(PtolemyFormEditor.this,
                    iae.getMessage(), "Invalid Layout",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Insert a row.
     *  @param rowIndex The row index.
     */
    private void _insertRow(int rowIndex) {
        for (int index = 0; index < _container.getComponentCount(); index++) {
            Component component = _container.getComponent(index);
            CellConstraints constraints = _getComponentConstraints(component);
            if (constraints.gridY > rowIndex) {
                constraints.gridY++;
            }
        }

        try {
            _containerLayout.addRowSpec(rowIndex, "pref");
            _tableModel.fireTableStructureChanged();
            _setSelectedCell(0, rowIndex + 1, true);
            _specsChanged();
        } catch (IllegalArgumentException iae) {
            // FIXME: Use our error reporting.
            JOptionPane.showMessageDialog(PtolemyFormEditor.this,
                    iae.getMessage(), "Invalid Layout",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Put an icon onto an AbstractAction.  */
    private void _putValue(AbstractAction abstractAction, String iconName) {
        String iconDirectory = "$CLASSPATH/ptolemy/actor/gui/run/";
        try {
            // We use nameToURL() here so that this code will work
            // in webstart.
            // FIXME: we should use rollover icons like what is in
            // vergil.basic.BasicGraphFrame, where we call
            // diva.util.GUIUtilities.
            abstractAction.putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(FileUtilities.nameToURL(iconDirectory
                            + iconName, null, null)));
        } catch (IOException ex) {
            System.out.println("Failed to open " + iconDirectory + iconName);
            ex.printStackTrace();
        }
    }

    /** Specify the cell in the table that is selected. */
    private void _setSelectedCell(int columnIndex, int rowIndex,
            boolean forceVisible) {
        // we don't want to update the selection interval if nothing changed...
        _table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
        _table.getColumnModel().getSelectionModel()
        .setSelectionInterval(columnIndex, columnIndex);

        if (forceVisible) {
            // let's make sure the cell is in the visible range...
            JViewport viewport = (JViewport) _table.getParent();
            Rectangle rect = _table.getCellRect(rowIndex, columnIndex, true);
            Point pt = viewport.getViewPosition();
            rect.setLocation(rect.x - pt.x, rect.y - pt.y);
            viewport.scrollRectToVisible(rect);
        }
    }

    /** Set up the listeners. */
    private void _setupListeners() {
        _verticalAlignmentCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component component = _table.getSelectedControl();
                if (component != null) {
                    CellConstraints cellConstraints = _getComponentConstraints(component);
                    cellConstraints.vAlign = LayoutConstraintsManager
                            .getAlignment((String) _verticalAlignmentCombo
                                    .getSelectedItem());
                    updateLayout(component);
                }
            }
        });
        _horizontalAlignmentCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component component = _table.getSelectedControl();
                if (component != null) {
                    CellConstraints cellConstraints = _getComponentConstraints(component);
                    cellConstraints.hAlign = LayoutConstraintsManager
                            .getAlignment((String) _horizontalAlignmentCombo
                                    .getSelectedItem());
                    updateLayout(component);
                }
            }
        });
        _topInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!_suspendConstraintControlUpdates) {
                    Component component = _table.getSelectedControl();
                    CellConstraints constraints = _getComponentConstraints(component);
                    Insets insets = new Insets(_topInsetSpinnerModel
                            .getNumber().intValue(), constraints.insets.left,
                            constraints.insets.bottom, constraints.insets.right);
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });
        _leftInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!_suspendConstraintControlUpdates) {
                    Component component = _table.getSelectedControl();
                    CellConstraints constraints = _getComponentConstraints(component);
                    Insets insets = new Insets(constraints.insets.top,
                            _leftInsetSpinnerModel.getNumber().intValue(),
                            constraints.insets.bottom, constraints.insets.right);
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });
        _rightInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!_suspendConstraintControlUpdates) {
                    Component component = _table.getSelectedControl();
                    CellConstraints constraints = _getComponentConstraints(component);
                    Insets insets = new Insets(constraints.insets.top,
                            constraints.insets.left, constraints.insets.bottom,
                            _rightInsetSpinnerModel.getNumber().intValue());
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });
        _bottomInsetSpinnerModel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!_suspendConstraintControlUpdates) {
                    Component component = _table.getSelectedControl();
                    CellConstraints constraints = _getComponentConstraints(component);
                    Insets insets = new Insets(constraints.insets.top,
                            constraints.insets.left, _bottomInsetSpinnerModel
                            .getNumber().intValue(),
                            constraints.insets.right);
                    constraints.insets = insets;
                    updateLayout(component);
                }
            }
        });
        _table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    //Point p = e.getPoint();
                    //int row = _table.rowAtPoint(p);
                    //int col = _table.columnAtPoint(p);
                    // support double-click:
                    Component component = _table.getSelectedControl();
                    if (component == null) {
                        return;
                    }

                    /* invoke componentDef editor on double-clicked control */
                    //String name = _getComponentName(component);
                    editComponent(component);
                }
            }
        });
    }

    /** Specify that the specifications have changed. */
    private void _specsChanged() {
        _updateLayouts();

        // lets go down the tree
        Component[] children = _container.getComponents();
        for (Component component : children) {
            if (component instanceof Container) {
                ((Container) component).doLayout();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Model for the column span control. */
    private class ColSpanSpinnerModel extends AbstractSpinnerModel {
        CellConstraints constraints;

        Component component;

        public ColSpanSpinnerModel() {
        }

        public void setComponent(Component component) {
            this.component = component;
            if (component != null) {
                constraints = _getComponentConstraints(component);
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
            Integer next = constraints.gridX + constraints.gridWidth - 1 < _containerLayout
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

            constraints.gridWidth = ((Number) value).intValue();
            super.fireStateChanged();
            updateLayout(component);

            // firing _table data changed messes up the
            // selection so we'll get it and then restore it...
            int col = _table.getSelectedColumn();
            int row = _table.getSelectedRow();
            _tableModel.fireTableDataChanged();
            _setSelectedCell(col, row, true);
        }
    }

    /** Model for the palette. */
    private class ComponentPaletteListModel extends AbstractListModel {
        List<String> components = new LinkedList<String>();

        public ComponentPaletteListModel() {
            // FIXME: The text of the following buttons can in theory be customized.
            // How to provide the interface for that?
            components.add("GoButton");
            components.add("PauseButton");
            components.add("ResumeButton");
            components.add("StopButton");

            components.add("ConfigureTopLevel");
            components.add("ConfigureDirector");

            components.add("Label");

            // Iterate over all the components that implement Placeable.
            if (_layoutFrame._pane._model != null) {
                Iterator atomicEntities = _layoutFrame._pane._model
                        .allAtomicEntityList().iterator();
                while (atomicEntities.hasNext()) {
                    NamedObj object = (NamedObj) atomicEntities.next();
                    if (object instanceof Placeable
                            || object instanceof PortablePlaceable) {
                        components.add("Placeable:"
                                + object.getName(_layoutFrame._pane._model));
                    }
                }
            }

            // FIXME: Do we need to be able to customize the name?
            components.add("Subpanel:Subpanel");

            // FIXME: This isn't useful without being able to specify the entity.
            components.add("Configure:Entity");
        }

        @Override
        public int getSize() {
            return components.size();
        }

        @Override
        public Object getElementAt(int index) {
            return components.get(index);
        }
    }

    /** Renderer for the palette items. */
    private static class ComponentPaletteListRenderer extends JLabel implements
    ListCellRenderer {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public ComponentPaletteListRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setText(value.toString());
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

    /** Renderer for table cells. */
    private class ConstraintTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            String stringValue = null;
            if (value != null) {
                if (value instanceof Component) {
                    String name = _getComponentName((Component) value);
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

    /** Action to delete a column. */
    private class DeleteColumnAction extends AbstractAction {
        public DeleteColumnAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Deletes the selected column");
            putValue(Action.LONG_DESCRIPTION, "Deletes the selected column");
            _putValue(this, "ColumnDelete24.gif");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int columnIndex = _table.getSelectedColumn();
            for (int index = 0; index < _container.getComponentCount(); index++) {
                Component component = _container.getComponent(index);
                CellConstraints constraints = _getComponentConstraints(component);
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
            _containerLayout.removeColumnSpec(columnIndex - 1);
            _tableModel.fireTableStructureChanged();
            _table.changeSelection(0,
                    Math.min(columnIndex, _containerLayout.getColumnCount()),
                    false, false);
            _specsChanged();
            _table.requestFocus();
        }
    }

    /** Action to delete a row. */
    private class DeleteRowAction extends AbstractAction {
        public DeleteRowAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Deletes the selected row");
            putValue(Action.LONG_DESCRIPTION, "Deletes the selected row");
            _putValue(this, "RowDelete24.gif");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int rowIndex = _table.getSelectedRow();

            // move any components that are on the deleted row or
            // above it down one
            for (int index = 0; index < _container.getComponentCount(); index++) {
                Component component = _container.getComponent(index);
                CellConstraints constraints = _getComponentConstraints(component);

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
            _containerLayout.removeRowSpec(rowIndex - 1);
            _tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
            _table.changeSelection(
                    Math.min(rowIndex, _containerLayout.getRowCount()), 0,
                    false, false);
            _specsChanged();
            _table.requestFocus();
        }
    }

    /** The data model for the table. */
    private class GridTableModel extends javax.swing.table.AbstractTableModel {
        @Override
        public int getColumnCount() {
            return _containerLayout != null ? _containerLayout.getColumnCount() + 1
                    : 1;
        }

        @Override
        public int getRowCount() {
            return _containerLayout != null ? _containerLayout.getRowCount() + 1
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
            if (row == 0) {
                // a column was changed
                try {
                    _containerLayout.setColumnSpec(col - 1, value);
                    _specsChanged();
                } catch (IllegalArgumentException iae) {
                    JOptionPane.showMessageDialog(PtolemyFormEditor.this,
                            iae.getMessage(), "Invalid Layout",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else if (col == 0) {
                try {
                    _containerLayout.setRowSpec(row - 1, value);
                    _specsChanged();
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
                return _containerLayout.getColumnSpec(columnIndex - 1);
            }
            if (columnIndex == 0) {
                return _containerLayout.getRowSpec(rowIndex - 1);
            }
            Component component = null;
            for (int index = 0; index < _container.getComponentCount(); index++) {
                Component thisComponent = _container.getComponent(index);
                // we don't want to show invisible components. we
                // have decided to make components that are added without
                // constraints invisible until they are dropped onto the form.
                // this is our way of hiding them.
                if (thisComponent.isVisible()) {
                    CellConstraints constraints = _getComponentConstraints(thisComponent);
                    if (constraints == null) {
                        throw new RuntimeException(
                                "Unable to find constraints for component "
                                        + thisComponent + " in layout "
                                        + _containerLayout.getName());
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

    /** Action to insert a column after the selected column. */
    private class InsertColumnAfterAction extends AbstractAction {
        public InsertColumnAfterAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a column after the selected column");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a column after the selected column");
            _putValue(this, "ColumnInsertAfter24.gif");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int column = _table.getSelectedColumn();
            _insertColumn(column);
            _table.requestFocus();
        }
    }

    /** Action to insert a column before the selected column. */
    private class InsertColumnBeforeAction extends AbstractAction {
        public InsertColumnBeforeAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a column before the selected column");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a column before the selected column");
            _putValue(this, "ColumnInsertBefore24.gif");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_K));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int column = _table.getSelectedColumn();
            _insertColumn(column - 1);
            _table.requestFocus();
        }
    }

    /** Action to insert a row after the selected one. */
    private class InsertRowAfterAction extends AbstractAction {
        public InsertRowAfterAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a row after the selected row");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a row after the selected row");
            _putValue(this, "RowInsertAfter24.gif");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_O));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = _table.getSelectedRow();
            _insertRow(row);
            _table.requestFocus();
        }
    }

    /** Action to insert a row before the selected one. */
    private class InsertRowBeforeAction extends AbstractAction {
        public InsertRowBeforeAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION,
                    "Inserts a row before the selected row");
            putValue(Action.LONG_DESCRIPTION,
                    "Inserts a row before the selected row");
            _putValue(this, "RowInsertBefore24.gif");
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int row = _table.getSelectedRow();
            _insertRow(row - 1);
            _table.requestFocus();
        }
    }

    /** Action to pack the run control panel. */
    private class PackAction extends AbstractAction {
        public PackAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Pack the run control panel");
            putValue(Action.LONG_DESCRIPTION, "Pack the run control panel");
            _putValue(this, "Pack.gif");
            putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, 0));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            _container.validate();
            _container.doLayout();

            Container parent = _container;

            while (parent != null) {
                parent.validate();
                if (parent instanceof Window) {
                    ((Window) parent).pack();
                    parent.setVisible(true);
                }
                parent = parent.getParent();
            }
        }
    }

    /** Action to remove a component. */
    private class RemoveComponentAction extends AbstractAction {
        public RemoveComponentAction() {
            super();
            putValue(Action.SHORT_DESCRIPTION, "Delete the component");
            putValue(Action.LONG_DESCRIPTION, "Delete the selected component. "
                    + "A component must be selected for this to be enabled.");
            _putValue(this, "Remove24.gif");
            putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Component selectedControl = _table.getSelectedControl();
            String controlName = _getComponentName(selectedControl);
            _container.remove(selectedControl);
            _tableModel.fireTableDataChanged();

            if (selectedControl instanceof Container
                    && _layoutFrame.hasContainer(controlName)) {
                _layoutFrame.removeContainer(controlName);
            }
            _container.doLayout();
            _container.repaint();
            setFormComponent(null);
            _table.requestFocus();
        }
    }

    /** Model for the row span control. */
    private class RowSpanSpinnerModel extends AbstractSpinnerModel {
        CellConstraints constraints;

        Component component;

        public RowSpanSpinnerModel() {
        }

        public void setComponent(Component component) {
            this.component = component;
            if (component != null) {
                constraints = _getComponentConstraints(component);
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
                Integer next = constraints.gridY + constraints.gridHeight - 1 < _containerLayout
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
            //        Number val = (Number) value;
            constraints.gridHeight = ((Number) value).intValue();
            super.fireStateChanged();
            updateLayout(component);

            // firing _table data changed messes up the
            // selection so we'll get it and then restore it...
            int col = _table.getSelectedColumn();
            int row = _table.getSelectedRow();
            _tableModel.fireTableDataChanged();
            _setSelectedCell(col, row, true);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The layout manager. */
    protected ContainerLayout _containerLayout;

    /** The container. */
    protected Container _container;

    /** The set of new components. */
    protected Set<Component> newComponents = new HashSet<Component>();

    /** The top level component. */
    protected Component topComponent = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Model for the bottom inset control. */
    private SpinnerNumberModel _bottomInsetSpinnerModel = new SpinnerNumberModel(
            0, 0, Integer.MAX_VALUE, 1);

    /** The bottom inset control. */
    private JSpinner _bottomInsetSpinner = new JSpinner(
            _bottomInsetSpinnerModel);

    /** The button to delete a column. */
    private JButton _columnDeleteButton = new JButton(new DeleteColumnAction());

    /** The button to insert after the selected column. */
    private JButton _columnInsertAfterButton = new JButton(
            new InsertColumnAfterAction());

    /** The button to insert before the selected column. */
    private JButton _columnInsertBeforeButton = new JButton(
            new InsertColumnBeforeAction());

    /** The model for the column span control. */
    private ColSpanSpinnerModel _columnSpanSpinnerModel = new ColSpanSpinnerModel();

    /** The column span control. */
    private JSpinner _columnSpanSpinner = new JSpinner(_columnSpanSpinnerModel);

    /** The list of horizontal alignment options. */
    private String[] _horizontalAlignmentList = {
            LayoutConstraintsManager.DEFAULT, LayoutConstraintsManager.FILL,
            LayoutConstraintsManager.CENTER, LayoutConstraintsManager.LEFT,
            LayoutConstraintsManager.RIGHT };

    /** The horizontal alignment control. */
    private JComboBox _horizontalAlignmentCombo = new JComboBox(
            _horizontalAlignmentList);

    /** The layout frame. */
    private RunLayoutFrame _layoutFrame;

    /** Model for the left inset control. */
    private SpinnerNumberModel _leftInsetSpinnerModel = new SpinnerNumberModel(
            0, 0, Integer.MAX_VALUE, 1);

    /** The left inset control. */
    private JSpinner _leftInsetSpinner = new JSpinner(_leftInsetSpinnerModel);

    /** Action to pack the run control window. */
    private Action _packAction = new PackAction();

    /** Button to pack the run control window. */
    //private JButton _packButton = new JButton(_packAction);
    /** Properties to ignore and not present to the user. */
    private static Set<String> _propertiesToIgnore = new HashSet<String>();
    static {
        _propertiesToIgnore.add("actionCommand");
        _propertiesToIgnore.add("name");
        _propertiesToIgnore.add("UIClassID");
    }

    /** Action to remove a component. */
    private Action _removeComponentAction = new RemoveComponentAction();

    /** Button to remove a component. */
    private JButton _removeComponentButton = new JButton(_removeComponentAction);

    /** Model for the right inset control. */
    private SpinnerNumberModel _rightInsetSpinnerModel = new SpinnerNumberModel(
            0, 0, Integer.MAX_VALUE, 1);

    /** The right inset control. */
    private JSpinner _rightInsetSpinner = new JSpinner(_rightInsetSpinnerModel);

    /** The button to delete a row. */
    private JButton _rowDeleteButton = new JButton(new DeleteRowAction());

    /** The button to insert a row after the selected one. */
    private JButton _rowInsertAfterButton = new JButton(
            new InsertRowAfterAction());

    /** The button to insert a row before the selected one. */
    private JButton _rowInsertBeforeButton = new JButton(
            new InsertRowBeforeAction());

    /** The model for the row span control. */
    private RowSpanSpinnerModel _rowSpanSpinnerModel = new RowSpanSpinnerModel();

    /** The row span control. */
    private JSpinner _rowSpanSpinner = new JSpinner(_rowSpanSpinnerModel);

    /** Flag to suspend updates. */
    private boolean _suspendConstraintControlUpdates = false;

    /** The layout table, built in the constructor. */
    private LayoutTable _table = null;

    /** The data model for the table. */
    private GridTableModel _tableModel = new GridTableModel();

    /** Model for the top inset control. */
    private SpinnerNumberModel _topInsetSpinnerModel = new SpinnerNumberModel(
            0, 0, Integer.MAX_VALUE, 1);

    /** The top inset control. */
    private JSpinner _topInsetSpinner = new JSpinner(_topInsetSpinnerModel);

    /** The list of vertical alignment options. */
    private String[] _verticalAlignmentList = {
            LayoutConstraintsManager.DEFAULT, LayoutConstraintsManager.FILL,
            LayoutConstraintsManager.CENTER, LayoutConstraintsManager.TOP,
            LayoutConstraintsManager.BOTTOM };

    /** The vertical alignment control. */
    private JComboBox _verticalAlignmentCombo = new JComboBox(
            _verticalAlignmentList);
}

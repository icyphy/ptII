/* A top-level dialog window for configuring the ports of an entity.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@Pt.ProposedRating Yellow (eal@eecs.berkeley.edu)
@Pt.AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.Popup;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.unit.ParseException;
import ptolemy.data.unit.UnitAttribute;
import ptolemy.data.unit.UnitExpr;
import ptolemy.data.unit.UnitLibrary;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.LocatableNodeController;

//////////////////////////////////////////////////////////////////////////
//// PortConfigurerDialog
/**
This class is a non-modal dialog for configuring the ports of an entity.

@author Rowland R Johnson
@version $Id$
@since Ptolemy II 1.0
*/
public class PortConfigurerDialog
    extends PtolemyDialog
    implements ChangeListener {

    /**
     * Construct a dialog that presents the ports as a table. Each row of the
     * table corresponds to one port. The user modifies the table to specify
     * changes in the ports. When the apply button is pressed the contents of
     * the table is used to update the ports. When Commit is pressed an apply
     * is done before exiting.
     * <p>
     * This dialog is is not modal. In particular, changes can be undone by
     * clicking Edit->Undo, and the help screen can be manipulated while this
     * dialog exists. The dialog is placed relative to the owner.
     *
     * @param tableau The DialogTableau.
     * @param owner The object that, per the user, appears to be generating the
     * dialog.
     * @param target The object whose ports are being configured.
     * @param configuration The configuration to use to open the help screen
     * (or null if help is not supported).
     */
    public PortConfigurerDialog(
        DialogTableau tableau,
        Frame owner,
        Entity target,
        Configuration configuration) {
        super(
            "Configure ports for " + target.getName(),
            tableau,
            owner,
            target,
            configuration);
        // Listen for changes that may need to be reflected in the table.
        getTarget().addChangeListener(this);
        // Create the JComboBox that used to select the location of the port
        _portLocationComboBox = new JComboBox();
        _portLocationComboBox.addItem("DEFAULT");
        _portLocationComboBox.addItem("NORTH");
        _portLocationComboBox.addItem("EAST");
        _portLocationComboBox.addItem("SOUTH");
        _portLocationComboBox.addItem("WEST");
        _portTable = new JTable();
        _portTable.setPreferredScrollableViewportSize(new Dimension(600, 70));
        // Listen for selections on the table. There can only be one row
        // selected. When a row is selected, the Remove button will show the
        // port name associated with the row.
        ListSelectionModel rowSM = _portTable.getSelectionModel();
        rowSM.addListSelectionListener(_rowSelectionListener);
        _portTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                    Point point = me.getPoint();
                    int row = _portTable.rowAtPoint(point);
                    _setSelectedRow(row);
                }
            }
        });
        // Create the TableModel and set certain cell editors and renderers
        _setupTableModel();
        _initColumnSizes();
        setScrollableContents(_portTable);
        // The following sets up a listener for mouse clicks on the header cell
        // of the Show Name column. A click causes the values in this column to
        // all change.
        _jth = _portTable.getTableHeader();
        _jth.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                Rectangle headerShowNameRect =
                    _jth.getHeaderRect(PortTableModel.COL_SHOW_NAME);
                Rectangle headerHidePortRect =
                    _jth.getHeaderRect(PortTableModel.COL_HIDE);
                if (headerShowNameRect.contains(me.getPoint())) {
                    _portTableModel.toggleShowAllNames();
                }
                if (headerHidePortRect.contains(me.getPoint())) {
                    _portTableModel.toggleHidePorts();
                }
            }
        });
        pack();
        setVisible(true);
    }
    // The table model for the table.
    class PortTableModel extends AbstractTableModel {

        // Populates the _ports Vector. Each element of _ports represents
        // a TypedIOPort on the Entity that is having its ports configured.
        // Said reppresentation is with an Object array. By convention, all
        // methods in this class that need to materialize an element of the
        // _ports Vector use the variable Object portInfo[7].
        public PortTableModel(List portList) {
            Iterator ports = portList.iterator();
            int index = 0;
            _ports = new Vector();
            while (ports.hasNext()) {
                Port p = (Port) ports.next();
                if (p instanceof TypedIOPort) {
                    TypedIOPort port = (TypedIOPort) p;
                    Object portInfo[] = new Object[getColumnCount() + 1];
                    portInfo[COL_NAME] = port.getName();
                    portInfo[COL_INPUT] = Boolean.valueOf(port.isInput());
                    portInfo[COL_OUTPUT] = Boolean.valueOf(port.isOutput());
                    portInfo[COL_MULTIPORT] =
                        Boolean.valueOf(port.isMultiport());
                    TypeAttribute _type =
                        (TypeAttribute) (port.getAttribute("_type"));
                    if (_type != null) {
                        portInfo[COL_TYPE] = _type.getExpression();
                    } else {
                        portInfo[COL_TYPE] = "unknown";
                    }
                    String _direction;
                    StringAttribute _cardinal =
                        (StringAttribute) (port.getAttribute("_cardinal"));
                    if (_cardinal != null) {
                        _direction = _cardinal.getExpression().toUpperCase();
                    } else {
                        _direction = "DEFAULT";
                    }
                    portInfo[COL_DIRECTION] = _direction;
                    Attribute _show =
                        (Attribute) (port.getAttribute("_showName"));
                    if (_show == null) {
                        portInfo[COL_SHOW_NAME] = Boolean.FALSE;
                    } else {
                        portInfo[COL_SHOW_NAME] = Boolean.TRUE;
                    }
                    Attribute hide = (Attribute) (port.getAttribute("_hide"));
                    if (hide == null) {
                        portInfo[COL_HIDE] = Boolean.FALSE;
                    } else {
                        portInfo[COL_HIDE] = Boolean.TRUE;
                    }
                    String _units = "";
                    UnitAttribute _unitsAttribute =
                        (UnitAttribute) port.getAttribute("_units");
                    if (_unitsAttribute != null) {
                        _units = _unitsAttribute.getExpression();
                    }
                    portInfo[COL_UNITS] = _units;
                    portInfo[COL_ACTUAL_PORT] = port;
                    _ports.add(portInfo);
                }
            }
        }

        /**
         * Add a port The new port gets added with a name of <NewPort>. It is
         * assumed that the user will change this to the real name at some
         * point.
         */
        public void addNewPort() {
            Object portInfo[] = new Object[getColumnCount() + 1];
            portInfo[COL_NAME] = "";
            portInfo[COL_INPUT] = Boolean.FALSE;
            portInfo[COL_OUTPUT] = Boolean.FALSE;
            portInfo[COL_MULTIPORT] = Boolean.FALSE;
            portInfo[COL_TYPE] = "unknown";
            portInfo[COL_DIRECTION] = "DEFAULT";
            portInfo[COL_SHOW_NAME] = Boolean.FALSE;
            portInfo[COL_HIDE] = Boolean.FALSE;
            portInfo[COL_UNITS] = "";
            portInfo[COL_ACTUAL_PORT] = null;
            _ports.add(portInfo);
            // Now tell the GUI so that it can update itself.
            fireTableRowsInserted(getRowCount(), getRowCount());
        }

        /**
         * Removes a port.
         *
         */
        public void removePort() {
            // First remove it from the _ports, and then tell the GUI that it
            // is
            // gone so that it can update itself.
            _ports.remove(_selectedRow);
            fireTableRowsDeleted(_selectedRow, _selectedRow);
            _enableApplyButton(true);
            _setDirty(true);
        }

        /**
         * Get the number of columns.
         *
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return _columnNames.length;
        }

        /**
         * Get the number of rows.
         *
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return _ports.size();
        }

        /**
         * Get the column header
         *
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int col) {
            return _columnNames[col];
        }

        /**
         * Get the value at a particular row and column
         *
         * @param row
         * @param col
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col) {
            Object portInfo[] = (Object[]) (_ports.elementAt(row));
            return portInfo[col];
        }

        /**
         * Set the value at a particular row and column
         *
         * @param row
         * @param col
         * @return value
         * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
         */
        public void setValueAt(Object value, int row, int col) {
            Object portInfo[] = (Object[]) (_ports.elementAt(row));
            portInfo[col] = value;
            _enableApplyButton(true);
            _setDirty(true);
        }

        /**
         * Get the Java Class associated with a column param column
         *
         * @return class
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /**
         * Is a cell editable
         *
         * @param row
         * @param col
         * @return editable
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int row, int col) {
            if (!_units) {
                if (col == COL_UNITS)
                    return false;
            }
            Object portInfo[] = (Object[]) (_ports.elementAt(row));
            Port port = (Port) (portInfo[COL_ACTUAL_PORT]);
            if (port != null
                && port.isInherited()
                && (col == COL_NAME
                    || col == COL_INPUT
                    || col == COL_OUTPUT
                    || col == COL_MULTIPORT)) {
                return false;
            }
            return true;
        }

        public void toggleShowAllNames() {
            _showAllNames = !_showAllNames;
            Boolean _show = new Boolean(_showAllNames);
            for (int i = 0; i < getRowCount(); i++) {
                setValueAt(_show, i, COL_SHOW_NAME);
            }
        }

        public void toggleHidePorts() {
            _hideAllPorts = !_hideAllPorts;
            Boolean _hide = new Boolean(_hideAllPorts);
            for (int i = 0; i < getRowCount(); i++) {
                setValueAt(_hide, i, COL_HIDE);
            }
        }
        // The columns of portInfo[].
        public final static int COL_NAME = 0;
        public final static int COL_INPUT = 1;
        public final static int COL_OUTPUT = 2;
        public final static int COL_MULTIPORT = 3;
        public final static int COL_TYPE = 4;
        public final static int COL_DIRECTION = 5;
        public final static int COL_SHOW_NAME = 6;
        public final static int COL_HIDE = 7;
        public final static int COL_UNITS = 8;
        public final static int COL_ACTUAL_PORT = 9;
    }
    class PortBooleanCellRenderer
        extends JCheckBox
        implements TableCellRenderer {

        public PortBooleanCellRenderer() {
            super();
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int col) {
            if (value == Boolean.TRUE) {
                setSelected(true);
            } else {
                setSelected(false);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!table.isCellEditable(row, col))
                setBackground(
                    LocatableNodeController.CLASS_ELEMENT_HIGHLIGHT_COLOR);
            else
                setBackground(Color.white);
            return this;
        }
    }
    class StringCellRenderer extends JLabel implements TableCellRenderer {

        public StringCellRenderer() {
            super();
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int col) {
            setOpaque(true);
            setText((String) value);
            if (!table.isCellEditable(row, col))
                setBackground(
                    LocatableNodeController.CLASS_ELEMENT_HIGHLIGHT_COLOR);
            else
                setBackground(Color.white);
            return this;
        }
    }
    class PortStringCellEditor
        extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

        Popup popup;
        String currentLabel;
        JButton button;
        JTable _jTable;

        public PortStringCellEditor() {
            button = new JButton();
            button.setActionCommand("edit");
            button.addActionListener(this);
        }

        public Object getCellEditorValue() {
            return currentLabel;
        }

        public Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int col) {
            _jTable = table;
            currentLabel = (String) value;
            button.setText(currentLabel);
            return button;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("edit")) {
                Component source = (Component) e.getSource();
                JOptionPane pane = new JOptionPane();
                pane.setWantsInput(true);
                pane.setInitialSelectionValue(currentLabel);
                pane.setMessage("");
                pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
                pane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
                Point p = source.getLocation();
                SwingUtilities.convertPointToScreen(p, _jTable);
                JDialog dialog = pane.createDialog(source, null);
                dialog.setLocation(p);
                dialog.show();
                if (pane.getValue() != null) {
                    String nv = (String) (pane.getInputValue());
                    if (!nv.equals(JOptionPane.UNINITIALIZED_VALUE)) {
                        currentLabel = nv;
                        fireEditingStopped();
                        return;
                    }
                }
                fireEditingCanceled();
                return;
            }
        }

        protected String _getText() {
            return currentLabel;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// public methods ////
    /**
     * Notify the listener that a change has been successfully executed.
     *
     * @param change
     *            The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is the originator or if this is a change
        // from above that is anything other than an undo. Detecting that it
        // is an undo from above seems awkward. A better way would be to extend
        // the ChangeRequest system to include ChangeRequest types so that
        // an undo would be explicitly represented.
        if (change == null
            || change.getSource() == this
            || !(change instanceof UndoChangeRequest))
            return;
        // The ports of the _target may have changed.
        _setupTableModel();
    }

    /**
     * Notify the listener that a change has resulted in an exception.
     *
     * @param change
     *            The change that was attempted.
     * @param exception
     *            The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // TODO Determine best way to handle failed change request. This method
        // _should_ never be invoked if the source is this. For now, at least,
        // test to see if the source is this, and report it.
        if (change == null)
            return;
        if (!change.isErrorReported()) {
            MessageHandler.error("Change failed: ", exception);
        }
    }

    public boolean close() {
        if (_isDirty()) {
            int option =
                JOptionPane.showConfirmDialog(
                    getOwner(),
                    "Save port modifications on " + getTarget().getFullName(),
                    "Unsaved Port Modifications",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            switch (option) {
                case (JOptionPane.YES_OPTION) :
                    {
                        _apply();
                        return true;
                    }
                case (JOptionPane.NO_OPTION) :
                    {
                        return true;
                    }
                case (JOptionPane.CANCEL_OPTION) :
                    {
                        return false;
                    }
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    //// protected methods ////
    /**
     * apply any changes that may have been made in the table.
     *
     */
    protected void _apply() {
        Iterator portIterator;
        TypedIOPort actualPort;
        // The port names in the table will be used many times, so extract
        // them here.
        String portNameInTable[] = new String[_portTableModel.getRowCount()];
        for (int i = 0; i < _portTableModel.getRowCount(); i++) {
            portNameInTable[i] =
                (String) (_portTableModel
                    .getValueAt(i, PortTableModel.COL_NAME));
        }
        //  Do some basic checks on table for things that are obviously
        // incorrect. First, make sure all the new ports have names other than
        // the empty string.
        for (int i = 0; i < _portTableModel.getRowCount(); i++) {
            if (portNameInTable[i].equals("")) {
                JOptionPane.showMessageDialog(
                    this,
                    "All Ports need to have a name");
                return;
            }
        }
        //  Now, make sure all port names are unique
        for (int i = 0; i < _portTableModel.getRowCount(); i++) {
            for (int j = i + 1; j < _portTableModel.getRowCount(); j++) {
                if (portNameInTable[i].equals(portNameInTable[j])) {
                    JOptionPane.showMessageDialog(
                        this,
                        portNameInTable[i]
                            + " is a duplicate port name.\n"
                            + "Please remove all but one");
                    return;
                }
            }
        }
        //   Determine which ports have been removed. If a port exists on the
        //   target but is not represented by a row in the table then it needs
        //   to be removed.
        Vector portsToBeRemoved = new Vector();
        portIterator = getTarget().portList().iterator();
        actualPort = null;
        while (portIterator.hasNext()) {
            Object candidate = portIterator.next();
            if (candidate instanceof TypedIOPort) {
                boolean foundPort = false;
                actualPort = ((TypedIOPort) candidate);
                for (int i = 0; i < _ports.size(); i++) {
                    Object portInfo[] = (Object[]) (_ports.elementAt(i));
                    if (actualPort
                        == ((TypedIOPort) portInfo[PortTableModel
                            .COL_ACTUAL_PORT])) {
                        foundPort = true;
                        break;
                    }
                }
                if (!foundPort) {
                    portsToBeRemoved.add(actualPort);
                }
            }
        }
        Iterator actualPorts = portsToBeRemoved.iterator();
        while (actualPorts.hasNext()) {
            StringBuffer moml = new StringBuffer();
            actualPort = (TypedIOPort) (actualPorts.next());
            // The context for the MoML should be the first
            // container above this port in the hierarchy
            // that defers its MoML definition, or the
            // immediate parent if there is none.
            NamedObj container = (NamedObj) actualPort.getContainer();
            NamedObj composite = (NamedObj) container.getContainer();
            if (composite != null) {
                moml.append(
                    "<deletePort name=\""
                        + actualPort.getName()
                        + "\" entity=\""
                        + container.getName()
                        + "\" />");
            } else {
                moml.append(
                    "<deletePort name=\""
                        + actualPort.getName(container)
                        + "\" />");
            }
            // NOTE: the context is the composite entity containing
            // the entity if possible
            MoMLChangeRequest request = null;
            if (composite != null) {
                request =
                    new MoMLChangeRequest(this, composite, moml.toString());
            } else {
                request =
                    new MoMLChangeRequest(this, container, moml.toString());
            }
            request.setUndoable(true);
            container.addChangeListener(this);
            if (_debug)
                System.out.println(
                    "RequestChange on " + container.toString() + " " + moml);
            container.requestChange(request);
        }
        //   Iterate over the table rows that represent ports. If a row
        // corresponds to an actual port then look to see if that row is
        // different from the actual port. If it is, then update that actual
        // port. If a row does not correspond to an actual port then that row
        // represents a new actual port which must be created.
        StringBuffer moml = new StringBuffer("<group>");
        boolean haveSomeUpdate = false;
        for (int i = 0; i < _ports.size(); i++) {
            Object portInfo[] = (Object[]) (_ports.elementAt(i));
            portIterator = getTarget().portList().iterator();
            // actualPort will be the TypedIOPort found on the _target, if
            // there
            // is one.
            actualPort =
                (TypedIOPort) (portInfo[PortTableModel.COL_ACTUAL_PORT]);
            boolean updates[] = new boolean[_portTableModel.getColumnCount()];
            if (actualPort != null) {
                // actualPort is a TypeIOPort found on the _target. Check to
                // see
                // if the actualPort is different and needs to be updated.
                for (int updateNum = 0; updateNum < 7; updateNum++) {
                    updates[updateNum] = false;
                }
                boolean havePortUpdate = false;
                if (!(actualPort
                    .getName()
                    .equals((String) (portInfo[PortTableModel.COL_NAME])))) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_NAME] = true;
                }
                if (actualPort.isInput()
                    != (((Boolean) (portInfo[PortTableModel.COL_INPUT]))
                        .booleanValue())) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_INPUT] = true;
                }
                if (actualPort.isOutput()
                    != (((Boolean) (portInfo[PortTableModel.COL_OUTPUT]))
                        .booleanValue())) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_OUTPUT] = true;
                }
                if (actualPort.isMultiport()
                    != (((Boolean) (portInfo[PortTableModel.COL_MULTIPORT]))
                        .booleanValue())) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_MULTIPORT] = true;
                }
                Attribute _show =
                    (Attribute) (actualPort.getAttribute("_showName"));
                if ((_show == null)
                    == (((Boolean) (portInfo[PortTableModel.COL_SHOW_NAME]))
                        .booleanValue())) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_SHOW_NAME] = true;
                }
                Attribute hide = actualPort.getAttribute("_hide");
                if ((hide == null)
                    == (((Boolean) (portInfo[PortTableModel.COL_HIDE]))
                        .booleanValue())) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_HIDE] = true;
                }
                String Type = "unknown";
                TypeAttribute _type =
                    (TypeAttribute) (actualPort.getAttribute("_type"));
                if (_type != null) {
                    Type = _type.getExpression();
                }
                if (!((String) (portInfo[PortTableModel.COL_TYPE]))
                    .equals(Type)) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_TYPE] = true;
                }
                // Look for a change in direction
                String _direction = null;
                String direction =
                    (String) (portInfo[PortTableModel.COL_DIRECTION]);
                StringAttribute _cardinal =
                    (StringAttribute) (actualPort.getAttribute("_cardinal"));
                if (_cardinal != null)
                    _direction = _cardinal.getExpression().toUpperCase();
                if (((_direction == null) && !direction.equals("DEFAULT"))
                    || ((_direction != null)
                        && (!direction.equals(_direction)))) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_DIRECTION] = true;
                }
                String _units = null;
                UnitAttribute _unitsAttribute =
                    (UnitAttribute) actualPort.getAttribute("_units");
                if (_unitsAttribute != null) {
                    _units = _unitsAttribute.getExpression();
                }
                if ((_units == null
                    && (!((String) (portInfo[PortTableModel.COL_UNITS]))
                        .equals("")))
                    || ((_units != null)
                        && (!((String) (portInfo[PortTableModel.COL_UNITS]))
                            .equals(_units)))) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_UNITS] = true;
                }
                if (havePortUpdate) {
                    // The context for the MoML should be the first container
                    // above this port in the hierarchy that defers its
                    // MoML definition, or the immediate parent
                    // if there is none.
                    NamedObj parent = (NamedObj) actualPort.getContainer();
                    moml.append(
                        _createMoMLUpdate(
                            updates,
                            portInfo,
                            (String) (((TypedIOPort) (portInfo[PortTableModel
                                .COL_ACTUAL_PORT]))
                                .getName()),
                            (String) (portInfo[PortTableModel.COL_NAME])));
                    haveSomeUpdate = true;
                }
            } else {
                // actualPort is not found on the _target so make a new one.
                for (int updateNum = 0; updateNum < 7; updateNum++) {
                    updates[updateNum] = true;
                }
                updates[PortTableModel.COL_NAME] = false;
                updates[PortTableModel.COL_SHOW_NAME] =
                    ((Boolean) (portInfo[PortTableModel.COL_SHOW_NAME]))
                        .booleanValue();
                updates[PortTableModel.COL_HIDE] =
                    ((Boolean) (portInfo[PortTableModel.COL_HIDE]))
                        .booleanValue();
                String direction =
                    (String) (portInfo[PortTableModel.COL_DIRECTION]);
                if (!direction.equals("DEFAULT")) {
                    updates[PortTableModel.COL_DIRECTION] = true;
                    _portTableModel.fireTableDataChanged();
                } else {
                    updates[PortTableModel.COL_DIRECTION] = false;
                }
                moml.append(
                    _createMoMLUpdate(
                        updates,
                        portInfo,
                        (String) (portInfo[PortTableModel.COL_NAME]),
                        null));
                haveSomeUpdate = true;
            }
        }
        if (haveSomeUpdate) {
            moml.append("</group>");
            if (_debug)
                System.out.println(
                    "RequestChange on " + getTarget().toString() + " " + moml);
            MoMLChangeRequest request =
                new MoMLChangeRequest(this, getTarget(), moml.toString(), null);
            request.setUndoable(true);
            // NOTE: There is no need to listen for completion
            // or errors in this change request, since, in theory,
            // it will just work. Will someone report the error
            // if one occurs? I hope so...
            getTarget().requestChange(request);
            _populateActualPorts();
        }
        _setDirty(false);
        _enableApplyButton(false);
    }

    protected void _cancel() {
        getTarget().removeChangeListener(this);
        super._cancel();
    }

    protected void _createExtendedButtons(JPanel _buttons) {
        _commitButton = new JButton("Commit");
        _buttons.add(_commitButton);
        _applyButton = new JButton("Apply");
        _buttons.add(_applyButton);
        _addButton = new JButton("Add");
        _buttons.add(_addButton);
        _removeButton = new JButton("Remove           ");
        _removeButton.setEnabled(false);
        _buttons.add(_removeButton);
    }

    protected URL _getHelpURL() {
        URL helpURL =
            getClass().getClassLoader().getResource(
                "ptolemy/actor/gui/doc/portConfigurerDialog.htm");
        return helpURL;
    }

    // The button semantics are
    // Add - Add a new port.
    protected void _processButtonPress(String button) {
        if (button.equals("Apply")) {
            _apply();
        } else if (button.equals("Commit")) {
            _apply();
            _cancel();
        } else if (button.equals("Add")) {
            _portTableModel.addNewPort();
        } else if (
            (button.length() > 5)
                && (button.substring(0, 6).equals("Remove"))) {
            _portTableModel.removePort();
            _setSelectedRow(-1);
        } else {
            super._processButtonPress(button);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// private methods ////
    // Create the MoML expression that represents the update.
    private String _createMoMLUpdate(
        boolean updates[],
        Object portInfo[],
        String currentPortName,
        String newPortName) {
        StringBuffer momlUpdate =
            new StringBuffer("<port name=\"" + currentPortName + "\">");
        if (updates[PortTableModel.COL_NAME]) {
            momlUpdate.append("<rename name=\"" + newPortName + "\"/>");
        }
        if (updates[PortTableModel.COL_INPUT]) {
            if (((Boolean) (portInfo[PortTableModel.COL_INPUT]))
                .booleanValue()) {
                momlUpdate.append("<property name=\"input\"/>");
            } else {
                momlUpdate.append("<property name=\"input\" value=\"false\"/>");
            }
        }
        if (updates[PortTableModel.COL_OUTPUT]) {
            if (((Boolean) (portInfo[PortTableModel.COL_OUTPUT]))
                .booleanValue()) {
                momlUpdate.append("<property name=\"output\"/>");
            } else {
                momlUpdate.append(
                    "<property name=\"output\" value=\"false\"/>");
            }
        }
        if (updates[PortTableModel.COL_MULTIPORT]) {
            if (((Boolean) (portInfo[PortTableModel.COL_MULTIPORT]))
                .booleanValue()) {
                momlUpdate.append("<property name=\"multiport\"/>");
            } else {
                momlUpdate.append(
                    "<property name=\"multiport\" value=\"false\"/>");
            }
        }
        if (updates[PortTableModel.COL_TYPE]) {
            String type = (String) (portInfo[PortTableModel.COL_TYPE]);
            momlUpdate.append(
                "<property name=\"_type\" "
                    + "class = \"ptolemy.actor.TypeAttribute\" "
                    + "value = \""
                    + StringUtilities.escapeForXML(type)
                    + "\"/>");
        }
        if (updates[PortTableModel.COL_DIRECTION]) {
            String direction =
                ((String) (portInfo[PortTableModel.COL_DIRECTION]));
            if (direction.equals("DEFAULT")) {
                momlUpdate.append("<deleteProperty name=\"_cardinal\" />");
            } else {
                momlUpdate.append(
                    "<property name=\"_cardinal\" "
                        + "class = \"ptolemy.kernel.util.StringAttribute\" "
                        + "value = \""
                        + direction
                        + "\"/>");
            }
        }
        if (updates[PortTableModel.COL_SHOW_NAME]) {
            if (((Boolean) (portInfo[PortTableModel.COL_SHOW_NAME]))
                .booleanValue()) {
                momlUpdate.append(
                    "<property name=\"_showName\""
                        + "class=\"ptolemy.kernel.util.SingletonAttribute\"/>");
            } else {
                momlUpdate.append("<deleteProperty name=\"_showName\" />");
            }
        }
        if (updates[PortTableModel.COL_HIDE]) {
            if (((Boolean) (portInfo[PortTableModel.COL_HIDE]))
                .booleanValue()) {
                momlUpdate.append(
                    "<property name=\"_hide\""
                       + " class=\"ptolemy.kernel.util.SingletonAttribute\"/>");
            } else {
                momlUpdate.append("<deleteProperty name=\"_hide\" />");
            }
        }
        if (_units && updates[PortTableModel.COL_UNITS]) {
            momlUpdate.append(
                "<property name=\"_units\" "
                    + "class = \"ptolemy.data.unit.UnitAttribute\" "
                    + "value = \""
                    + ((String) (portInfo[PortTableModel.COL_UNITS]))
                    + "\"/>");
        }
        momlUpdate.append("</port>");
        return momlUpdate.toString();
    }

    private void _enableApplyButton(boolean e) {
        _applyButton.setEnabled(e);
    }
    private final static String[] _columnNames =
        {
            "Name",
            "Input",
            "Output",
            "Multiport",
            "Type",
            "Direction",
            "Show Name",
            "Hide",
            "Units" };
    // The Listener that is sensitive to selection changes in the table.
    // When a row is selected change the label in the Remove button to
    // show that the associated port is the one that will be removed when
    // the Remove button is pressed.
    private ListSelectionListener _rowSelectionListener =
        new ListSelectionListener() {

            ///////////////////////////////////////////////////////////////////
        //// inner class ////
    public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            //Ignore extra messages.
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                _setSelectedRow(-1);
            } else {
                _selectedRow = lsm.getMinSelectionIndex();
                _setSelectedRow(_selectedRow);
            }
        }
    };

    private void _initColumnSizes() {
        TableColumn column = null;
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_INPUT);
        column.setPreferredWidth(30);
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_OUTPUT);
        column.setPreferredWidth(30);
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_MULTIPORT);
        column.setPreferredWidth(40);
        column = _portTable.getColumnModel().getColumn(PortTableModel.COL_TYPE);
        column.setPreferredWidth(50);
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_DIRECTION);
        column.setPreferredWidth(50);
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_SHOW_NAME);
        column.setPreferredWidth(70);
        column = _portTable.getColumnModel().getColumn(PortTableModel.COL_HIDE);
        column.setPreferredWidth(30);
    }

    private void _populateActualPorts() {
        for (int i = 0; i < _ports.size(); i++) {
            Object portInfo[] = (Object[]) (_ports.elementAt(i));
            String portName = (String) portInfo[PortTableModel.COL_NAME];
            Iterator portIterator = getTarget().portList().iterator();
            TypedIOPort actualPort;
            boolean foundActualPort = false;
            while (portIterator.hasNext()) {
                Object candidate = portIterator.next();
                if (candidate instanceof TypedIOPort) {
                    actualPort = ((TypedIOPort) candidate);
                    if (actualPort.getName().equals(portName)) {
                        portInfo[PortTableModel.COL_ACTUAL_PORT] = actualPort;
                        foundActualPort = true;
                        break;
                    }
                }
            }
            if (!foundActualPort) {
                // TODO throw an exception here
            }
        }
    }

    private void _setSelectedRow(int row) {
        _selectedRow = row;
        if (row < 0) {
            _removeButton.setText("Remove");
            _removeButton.setEnabled(false);
        } else {
            String portName =
                ((String) ((Object[]) (_ports.elementAt(row)))[0]);
            if (portName.length() < 10) {
                portName += "          ";
                portName = portName.substring(0, 9);
            } else if (portName.length() > 10) {
                portName = portName.substring(0, 7) + "...";
            }
            _removeButton.setText("Remove " + portName);
            _removeButton.setEnabled(true);
        }
    }

    // Creates and sets the TableModel. Also arranges for some columns to have
    // their particular renderers and/or editors. This method will be invoked
    // when the dialog is created, and everytime a change request from above
    // causes the table to change.
    private void _setupTableModel() {
        _portTableModel = new PortTableModel(getTarget().portList());
        _portTable.setModel(_portTableModel);
        _portTable.setDefaultRenderer(
            Boolean.class,
            new PortBooleanCellRenderer());
        _portTable.setDefaultRenderer(String.class, new StringCellRenderer());
        _portTable.setDefaultEditor(String.class, new PortStringCellEditor());
        _enableApplyButton(false);
        TableColumn _portLocationColumn =
            ((TableColumn) (_portTable
                .getColumnModel()
                .getColumn(PortTableModel.COL_DIRECTION)));
        _portLocationColumn.setCellEditor(
            new DefaultCellEditor(_portLocationComboBox));
        TableColumn _portTypeColumn =
            ((TableColumn) (_portTable
                .getColumnModel()
                .getColumn(PortTableModel.COL_TYPE)));
        final PortStringCellEditor portTypeEditor = new PortStringCellEditor();
        _portTypeColumn.setCellEditor(portTypeEditor);
        portTypeEditor.addCellEditorListener(new CellEditorListener() {

            public void editingStopped(ChangeEvent arg0) {
                // TODO Auto-generated method stub
            }

            public void editingCanceled(ChangeEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        TableColumn _portUnitColumn =
            ((TableColumn) (_portTable
                .getColumnModel()
                .getColumn(PortTableModel.COL_UNITS)));
        final PortStringCellEditor portUnitEditor = new PortStringCellEditor();
        _portUnitColumn.setCellEditor(portUnitEditor);
        portUnitEditor.addCellEditorListener(new CellEditorListener() {

            public void editingStopped(ChangeEvent arg0) {
                String expression = portUnitEditor._getText();
                UnitExpr uExpr;
                try {
                    uExpr = UnitLibrary.getParser().parseUnitExpr(expression);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(
                        null,
                        e.getMessage(),
                        "alert",
                        JOptionPane.ERROR_MESSAGE);
                }
            }

            public void editingCanceled(ChangeEvent arg0) {
            }
        });
    }
    ///////////////////////////////////////////////////////////////////
    //// private variables ////

    private boolean _hideAllPorts = false;
    // Following is true if we have full units capability.
    private boolean _units = true;
    // The combination box used to select the location of a port.
    private JComboBox _portLocationComboBox;
    JTable _portTable;
    // Port TableModel
    PortTableModel _portTableModel = null;
    JTableHeader _jth;
    // Each element of _ports is a row in the table that PortTableModel is
    // based
    // on.
    Vector _ports = null;
    private int _selectedRow = -1;
    private boolean _showAllNames = false;
    // The various buttons.
    private JButton _applyButton, _commitButton, _addButton, _removeButton;
    /* (non-Javadoc)
     * @see ptolemy.actor.gui.PtolemyDialog#_getHelpURL()
     */

}

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.unit.Units;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// PortConfigurerDialog
/**
This class is a non-modal dialog for configuring the ports of an entity.

@author Edward A. Lee, Rowland R Johnson
@version $Id$
@since Ptolemy II 1.0
*/
public class PortConfigurerDialog
    extends JFrame
    implements ActionListener, ChangeListener {

    /** Construct a dialog that presents the ports as a table.
     *  Each row of the table corresponds to one port. The user modifies the
     *  table to specify changes in the ports. When the apply button is pressed
     *  the contents of the table is used to update the ports. When Commit is
     *  pressed an apply is done before exiting.
     *  <p>
     *  This dialog is is not modal. In particular, changes can be undone by
     *  clicking Edit->Undo, and the help screen can be manipulated while this
     *  dialog exists. The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose ports are being configured.
     *  @param configuration The configuration to use to open the
     *   help screen (or null if help is not supported).
     */
    public PortConfigurerDialog(
        DialogTableau tableau,
        Frame owner,
        Entity target,
        Configuration configuration) {
        super("Configure ports for " + target.getName());

        _tableau = tableau;
        _owner = owner;
        _target = target;
        _configuration = configuration;

        // Listen for changes that may need to be reflected in the table.
        _target.addChangeListener(this);

        // Create the JComboBox that used to select the location of the port
        _portLocationComboBox = new JComboBox();
        _portLocationComboBox.addItem("NORTH");
        _portLocationComboBox.addItem("EAST");
        _portLocationComboBox.addItem("SOUTH");
        _portLocationComboBox.addItem("WEST");

        // This JDialog consists of two components. On top is a JTable inside
        // of a JScrolPane that displays the ports and their attributes.
        // On bottom is a JPanel that contains buttons that cause various
        // actions.
        _portTable = new JTable();
        _portTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        JScrollPane scrollPane = new JScrollPane(_portTable);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel _buttons = _createButtonsPanel();
        getContentPane().add(_buttons, BorderLayout.SOUTH);

        // Listen for selections on the table. There can only be one row
        // selected. When a row is selected, the Remove button will show the
        // port name associated with the row.
        ListSelectionModel rowSM = _portTable.getSelectionModel();
        rowSM.addListSelectionListener(_rowSelectionListener);

        // Create the TableModel and set certain cell editors and renderers
        _setupTableModel();
        _initColumnSizes();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                _cancel();
            }
        });

        owner.addWindowListener(new WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                _iconify();
            }
            public void windowDeiconified(WindowEvent e) {
                _deiconify();
            }
        });

        // The following sets up a listener for mouse clicks on the header cell
        // of the Show Name column. A click causes the values in this column to
        // all change.
        _jth = _portTable.getTableHeader();
        _jth.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                Rectangle headerRect =
                    _jth.getHeaderRect(PortTableModel.COL_SHOW_NAME);
                if (headerRect.contains(me.getPoint())) {
                    _portTableModel.toggleShowAllNames();
                }
            }
        });

        pack();
        setLocationRelativeTo(owner);
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
                    } else if (port.isInput() && !port.isOutput()) {
                        _direction = "WEST";
                    } else if (port.isOutput() && !port.isInput()) {
                        _direction = "EAST";
                    } else { // multiport
                        _direction = "WEST";
                    }
                    portInfo[COL_DIRECTION] = _direction;

                    SingletonAttribute _show =
                        (SingletonAttribute) (port.getAttribute("_showName"));
                    if (_show == null) {
                        portInfo[COL_SHOW_NAME] = Boolean.FALSE;
                    } else {
                        portInfo[COL_SHOW_NAME] = Boolean.TRUE;
                    }

                    String _units = "";
                    Units _unitsAttribute = (Units) port.getAttribute("_units");
                    if (_unitsAttribute != null) {
                        _units = _unitsAttribute.getExpression();
                    }
                    portInfo[COL_UNITS] = _units;

                    portInfo[COL_ACTUAL_PORT] = port;

                    _ports.add(portInfo);
                }
            }
        }

        /** Add a port
        *  The new port gets added with a name of <NewPort>. It is assumed that
        *  the user will change this to the real name at some point.
        */
        public void addNewPort() {
            Object portInfo[] = new Object[getColumnCount() + 1];
            portInfo[COL_NAME] = "";
            portInfo[COL_INPUT] = Boolean.FALSE;
            portInfo[COL_OUTPUT] = Boolean.FALSE;
            portInfo[COL_MULTIPORT] = Boolean.FALSE;
            portInfo[COL_TYPE] = "unknown";
            portInfo[COL_DIRECTION] = "";
            portInfo[COL_SHOW_NAME] = Boolean.FALSE;
            portInfo[COL_UNITS] = "";
            portInfo[COL_ACTUAL_PORT] = null;
            _ports.add(portInfo);
            // Now tell the GUI so that it can update itself.
            fireTableRowsInserted(getRowCount(), getRowCount());
        }

        /** Removes a port.
         *
         */
        public void removePort() {
            // First remove it from the _ports, and then tell the GUI that it is
            // gone so that it can update itself.
            _ports.remove(_selectedRow);
            fireTableRowsDeleted(_selectedRow, _selectedRow);
        }

        /** Get the number of columns.
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return _columnNames.length;
        }

        /** Get the number of rows.
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return _ports.size();
        }

        /** Get the column header
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        public String getColumnName(int col) {
            return _columnNames[col];
        }

        /** Get the value at a particular row and column
         * @param row
         * @param col
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col) {
            Object portInfo[] = (Object[]) (_ports.elementAt(row));
            return portInfo[col];
        }

        /** Set the value at a particular row and column
         * @param row
         * @param col
         * @return value
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object value, int row, int col) {
            Object portInfo[] = (Object[]) (_ports.elementAt(row));
            portInfo[col] = value;
            _applyButton.setEnabled(true);
            _isDirty = true;
        }

        /** Get the Java Class associated with a column
         * param column
         * @return class
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /** Is a cell editable
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
            return true;
        }

        public void toggleShowAllNames() {
            _showAllNames = !_showAllNames;
            Boolean _show = new Boolean(_showAllNames);
            for (int i = 0; i < getRowCount(); i++) {
                setValueAt(_show, i, COL_SHOW_NAME);
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
        public final static int COL_UNITS = 7;
        public final static int COL_ACTUAL_PORT = 8;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    // This method gets invoked as a result of a GUI action. Presently, it
    // handles button presses. It has to be a public, not protected, or private,
    // since it is inherited from ActionListener where it is public. Since it
    // isn't meant to be invoked by the developer there is no javadoc. The
    // button semantics are
    // Commit - Apply and then cancel the dialog.
    // Apply  - make the changes that have been expressed thus far.
    // Add    - Add a new port.
    // Remove - Remove the port currently selected.
    // Help   - Show the associated help.
    // Cancel - Remove the dialog without making any pending changes.

    public void actionPerformed(ActionEvent aEvent) {
        String command = aEvent.getActionCommand();
        if (command.equals("Apply")) {
            _apply();
        } else if (command.equals("Commit")) {
            _apply();
            _cancel();
        } else if (command.equals("Add")) {
            _portTableModel.addNewPort();
        } else if (command.equals("Help")) {
            {
                URL toRead =
                    getClass().getClassLoader().getResource(
                        "ptolemy/actor/gui/doc/portDialog.htm");
                if (toRead != null && _configuration != null) {
                    try {
                        _configuration.openModel(
                            null,
                            toRead,
                            toRead.toExternalForm());
                    } catch (Exception ex) {
                        MessageHandler.error("Help screen failure", ex);
                    }
                } else {
                    MessageHandler.error("No help available.");
                }
            }
        } else if (command.equals("Cancel")) {
            _cancel();
        } else if (
            (command.length() > 5)
                && (command.substring(0, 6).equals("Remove"))) {
            _portTableModel.removePort();
        } else {
            //TODO throw an exception here
        }
    }

    /** Notify the listener that a change has been successfully executed.
    *  @param change The change that has been executed.
    */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is the originator or if this is a change
        // from above that is anything other than an undo. Detecting that it
        // is an undo from above seems awkward. A better way would be to extend
        // the ChangeRequest system to include ChangeRequest types so that
        // an undo would be explicitly represented.
        if (change == null
            || change.getSource() == this
            || !change.getDescription().equals(
                "Request to undo/redo most recent MoML change"))
            return;
        // The ports of the _target may have changed.
        _setupTableModel();

    }

    /** Notify the listener that a change has resulted in an exception.
    *  @param change The change that was attempted.
    *  @param exception The exception that resulted.
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
        if (_isDirty) {
            int option =
                JOptionPane.showConfirmDialog(
                    _owner,
                    "Save port modifications on " + _target.getFullName(),
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

    /** Return the DialogTableau.
     * @return The DialogTableau
     */
    public DialogTableau getDialogTableau() {
        return _tableau;
    }

    /** Return the target.
     * @return The target.
     */
    public Entity getTarget() {
        return _target;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    /** apply any changes that may have been made in the table.
     *
     */
    private void _apply() {
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
        portIterator = _target.portList().iterator();
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

            NamedObj container =
                MoMLChangeRequest.getDeferredToParent(actualPort);
            if (container == null) {
                container = (NamedObj) actualPort.getContainer();
            }

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
            if (_debug)
                System.out.println("MOML " + moml);
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

            portIterator = _target.portList().iterator();
            // actualPort will be the TypedIOPort found on the _target, if there
            // is one.
            actualPort =
                (TypedIOPort) (portInfo[PortTableModel.COL_ACTUAL_PORT]);

            boolean updates[] = new boolean[_portTableModel.getColumnCount()];

            if (actualPort != null) {
                // actualPort is a TypeIOPort found on the _target. Check to see
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

                SingletonAttribute _show =
                    (SingletonAttribute) (actualPort.getAttribute("_showName"));
                if ((_show == null)
                    == (((Boolean) (portInfo[PortTableModel.COL_SHOW_NAME]))
                        .booleanValue())) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_SHOW_NAME] = true;
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
                StringAttribute _cardinal =
                    (StringAttribute) (actualPort.getAttribute("_cardinal"));
                if (_cardinal != null)
                    _direction = _cardinal.getExpression().toUpperCase();
                if ((_direction == null)
                    || (!((String) (portInfo[PortTableModel.COL_DIRECTION]))
                        .equals(_direction))) {
                    havePortUpdate = true;
                    updates[PortTableModel.COL_DIRECTION] = true;
                }

                String _units = null;
                Units _unitsAttribute =
                    (Units) actualPort.getAttribute("_units");
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
                    NamedObj parent =
                        MoMLChangeRequest.getDeferredToParent(actualPort);
                    if (parent == null) {
                        parent = (NamedObj) actualPort.getContainer();
                    }
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
                if (((String) (portInfo[PortTableModel.COL_DIRECTION]))
                    .equals("")) {
                    String _direction;
                    if (((Boolean) (portInfo[PortTableModel.COL_INPUT]))
                        .booleanValue()
                        && !((Boolean) (portInfo[PortTableModel.COL_OUTPUT]))
                            .booleanValue()) {
                        _direction = "WEST";
                    } else if (
                        ((Boolean) (portInfo[PortTableModel.COL_OUTPUT]))
                            .booleanValue()
                            && !((Boolean) (portInfo[PortTableModel.COL_INPUT]))
                                .booleanValue()) {
                        _direction = "EAST";
                    } else {
                        // multiport
                        _direction = "WEST";
                    }
                    portInfo[PortTableModel.COL_DIRECTION] = _direction;
                    _portTableModel.fireTableDataChanged();
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
                System.out.println("MOML " + moml);
            MoMLChangeRequest request =
                new MoMLChangeRequest(this, _target, moml.toString(), null);
            request.setUndoable(true);
            // NOTE: There is no need to listen for completion
            // or errors in this change request, since, in theory,
            // it will just work.  Will someone report the error
            // if one occurs?  I hope so...
            _target.requestChange(request);
            _populateActualPorts();
        }
        _isDirty = false;
        _applyButton.setEnabled(false);
    }

    private void _cancel() {
        _target.removeChangeListener(this);
        dispose();
    }

    private JPanel _createButtonsPanel() {
        JPanel _buttons = new JPanel();
        _commitButton = new JButton("Commit");
        _commitButton.addActionListener(this);
        _applyButton = new JButton("Apply");
        _applyButton.addActionListener(this);
        _addButton = new JButton("Add");
        _addButton.addActionListener(this);
        _removeButton = new JButton("Remove           ");
        _removeButton.setEnabled(false);
        _removeButton.addActionListener(this);
        _helpButton = new JButton("Help");
        _helpButton.addActionListener(this);
        _cancelButton = new JButton("Cancel");
        _cancelButton.addActionListener(this);
        _buttons.add(_commitButton);
        _buttons.add(_applyButton);
        _buttons.add(_addButton);
        _buttons.add(_removeButton);
        _buttons.add(_helpButton);
        _buttons.add(_cancelButton);
        return _buttons;
    }

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
            momlUpdate.append(
                "<property name=\"_cardinal\" "
                    + "class = \"ptolemy.kernel.util.StringAttribute\" "
                    + "value = \""
                    + ((String) (portInfo[PortTableModel.COL_DIRECTION]))
                    + "\"/>");
        }

        if (updates[PortTableModel.COL_SHOW_NAME]) {
            if (((Boolean) (portInfo[PortTableModel.COL_SHOW_NAME]))
                .booleanValue()) {
                momlUpdate.append(
                    "<property name=\"_showName\" class=\"ptolemy.kernel.util.SingletonAttribute\"/>");
            } else {
                momlUpdate.append("<deleteProperty name=\"_showName\" />");
            }
        }
        if (_units && updates[PortTableModel.COL_UNITS]) {
            momlUpdate.append(
                "<property name=\"_units\" "
                    + "class = \"ptolemy.data.unit.Units\" "
                    + "value = \""
                    + ((String) (portInfo[PortTableModel.COL_UNITS]))
                    + "\"/>");
        }

        momlUpdate.append("</port>");
        return momlUpdate.toString();
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
            "Units" };

    // The Listener that is sensitive to selection changes in the table.
    // When a row is selected change the label in the Remove button to
    // show that the associated port is the one that will be removed when
    // the Remove button is pressed.
    private ListSelectionListener _rowSelectionListener =
        new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            //Ignore extra messages.
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
                _removeButton.setText("Remove");
                _removeButton.setEnabled(false);
                _selectedRow = -1;
            } else {
                _selectedRow = lsm.getMinSelectionIndex();
                String portName =
                    ((String) ((Object[]) (_ports.elementAt(_selectedRow)))[0]);
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
    };
    private void _deiconify() {
        setExtendedState(Frame.NORMAL);
    }

    private void _iconify() {
        setExtendedState(Frame.ICONIFIED);
    }

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
        column.setPreferredWidth(30);
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_DIRECTION);
        column.setPreferredWidth(30);
        column =
            _portTable.getColumnModel().getColumn(PortTableModel.COL_SHOW_NAME);
        column.setPreferredWidth(50);
    }

    private void _populateActualPorts() {
        for (int i = 0; i < _ports.size(); i++) {
            Object portInfo[] = (Object[]) (_ports.elementAt(i));
            String portName = (String) portInfo[PortTableModel.COL_NAME];
            Iterator portIterator = _target.portList().iterator();
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

    // Creates and sets the TableModel. Also arranges for some columns to have
    // their particular renderers and/or editors. This method will be invoked
    // when the dialog is created, and everytime a change request from above
    // causes the table to change.
    private void _setupTableModel() {
        _portTableModel = new PortTableModel(_target.portList());
        _portTable.setModel(_portTableModel);
        _applyButton.setEnabled(false);
        TableColumn _portNameColumn =
            ((TableColumn) (_portTable
                .getColumnModel()
                .getColumn(PortTableModel.COL_NAME)));
        JTextField nameTextField = new JTextField();
        nameTextField.setBackground(Color.yellow);
        final DefaultCellEditor portNameEditor =
            new DefaultCellEditor(nameTextField);
        portNameEditor.setClickCountToStart(1);
        _portNameColumn.setCellEditor(portNameEditor);
        nameTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fe) {
                portNameEditor.stopCellEditing();
            }
        });
        _portNameColumn.setCellRenderer(new DefaultTableCellRenderer());

        TableColumn _portLocationColumn =
            ((TableColumn) (_portTable
                .getColumnModel()
                .getColumn(PortTableModel.COL_DIRECTION)));
        _portLocationColumn.setCellEditor(
            new DefaultCellEditor(_portLocationComboBox));
        _portLocationColumn.setCellRenderer(new DefaultTableCellRenderer());

        TableColumn _portTypeColumn =
            ((TableColumn) (_portTable
                .getColumnModel()
                .getColumn(PortTableModel.COL_TYPE)));
        JTextField typeTextField = new JTextField();
        typeTextField.setBackground(Color.yellow);
        final DefaultCellEditor portTypeEditor =
            new DefaultCellEditor(typeTextField);
        portTypeEditor.setClickCountToStart(1);
        _portTypeColumn.setCellEditor(portTypeEditor);
        typeTextField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent fe) {
                portTypeEditor.stopCellEditing();
            }
        });
        _portTypeColumn.setCellRenderer(new DefaultTableCellRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The configuration.
    private Configuration _configuration;
    private boolean _debug = false;
    // The following is true if any of the values have been changed but not
    // applied.
    private boolean _isDirty = false;
    // Following is true if we have full units capability.
    private boolean _units = false;
    // The owner window.
    private Frame _owner;
    private DialogTableau _tableau;
    // The target object whose ports are being configured.
    private Entity _target;
    // The combination box used to select the location of a port.
    private JComboBox _portLocationComboBox;
    JTable _portTable;
    // Port TableModel
    PortTableModel _portTableModel = null;
    JTableHeader _jth;
    // Each element of _ports is a row in the table that PortTableModel is based
    // on.
    Vector _ports = null;

    private int _selectedRow = -1;
    private boolean _showAllNames = false;
    // The various buttons.
    private JButton _applyButton,
        _commitButton,
        _addButton,
        _helpButton,
        _removeButton,
        _cancelButton;
}

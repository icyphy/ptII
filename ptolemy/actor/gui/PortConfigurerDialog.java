/* A top-level dialog window for configuring the ports of an entity.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.gui.PtGUIUtilities;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.unit.ParseException;
import ptolemy.moml.unit.UnitAttribute;
import ptolemy.moml.unit.UnitLibrary;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// PortConfigurerDialog

/**
 This class is a non-modal dialog for configuring the ports of an
 entity.  The columns of the dialog displayed depend on the type of
 the Entity (target) for which we are configuring the ports.

 By default, "Name", "Direction, "Show Name", and "Hide" are
 displayed for all target types.  We assume that the ports are of
 type Port or ComponentPort.

 If the target is an Actor, then the ports are of type IOPort and we
 add the "Input", "Output", and "Multiport" columns.

 If the target is a TypedActor, then the ports are of type
 TypedIOPort, and we add the "Type" and "Units" columns.

 NOTE: This code checks for the existence of each column that may be
 used, but it sometimes assumes the existence of the "Name" column.

 @author Rowland R Johnson, Elaine Cheong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class PortConfigurerDialog extends PtolemyDialog implements
ChangeListener {
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
    public PortConfigurerDialog(DialogTableau tableau, Frame owner,
            Entity target, Configuration configuration) {
        super("Configure ports for " + target.getName(), tableau, owner,
                target, configuration);

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
        // If you change the height, then check that a few rows can be added.
        // Also, check the setRowHeight call below.
        _portTable.setPreferredScrollableViewportSize(new Dimension(600, 100));

        _portTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (PtGUIUtilities.macOSLookAndFeel()
                        && (mouseEvent.isPopupTrigger() || mouseEvent
                                .getButton() == MouseEvent.BUTTON1
                                && (mouseEvent.getModifiersEx() | java.awt.event.InputEvent.CTRL_MASK) == java.awt.event.InputEvent.CTRL_MASK)
                                || !PtGUIUtilities.macOSLookAndFeel()
                                && mouseEvent.getButton() == MouseEvent.BUTTON3) {
                    Point point = mouseEvent.getPoint();
                    int row = _portTable.rowAtPoint(point);
                    _setSelectedRow(row);
                }
            }
        });

        _portTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ke) {
                if (ke.getKeyChar() == '\n') {
                    if (_apply()) {
                        _cancel();
                    }
                }
            }
        });
        _portTable.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                // Set the selected row so the remove key gets updated
                _setSelectedRow(_portTable.getSelectionModel()
                        .getAnchorSelectionIndex());
            }

            @Override
            public void focusLost(FocusEvent event) {
            }
        });

        // Initialize which columns will be visible for this target.
        _initColumnNames();

        // Create the TableModel and set certain cell editors and renderers
        _setupTableModel();

        // Initialize the displayed column widths.
        _initColumnSizes();

        // Make the contents of the table scrollable
        setScrollableContents(_portTable);

        // The following sets up a listener for mouse clicks on the
        // header cell of the Show Name column. A click causes the
        // values in this column to all change.
        // FIXME: this doesn't seem to work if you click multiple
        // times in a session
        _jth = _portTable.getTableHeader();

        if (_columnNames.contains(ColumnNames.COL_SHOW_NAME)
                || _columnNames.contains(ColumnNames.COL_HIDE)) {
            _jth.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent me) {
                    // indexOf() returns -1 if element is not in ArrayList
                    int showName = _columnNames
                            .indexOf(ColumnNames.COL_SHOW_NAME);

                    if (showName != -1) {
                        Rectangle headerShowNameRect = _jth
                                .getHeaderRect(showName);

                        if (headerShowNameRect.contains(me.getPoint())) {
                            _portTableModel.toggleShowAllNames();
                        }
                    }

                    int hide = _columnNames.indexOf(ColumnNames.COL_HIDE);

                    if (hide != 1) {
                        Rectangle headerHidePortRect = _jth.getHeaderRect(hide);

                        if (headerHidePortRect.contains(me.getPoint())) {
                            _portTableModel.toggleHidePorts();
                        }
                    }
                }
            });
        }

        pack();
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The background color of an uneditable cell. */
    public static final Color UNEDITABLE_CELL_COLOR = new Color(255, 128, 128);

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Notify the listener that a change has been successfully executed.
     *
     * @param change The change that has been executed.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is the originator or if this is a change
        // from above that is anything other than an undo. Detecting
        // that it is an undo from above seems awkward. A better way
        // would be to extend the ChangeRequest system to include
        // ChangeRequest types so that an undo would be explicitly
        // represented.
        if (change == null || change.getSource() == this
                || !(change instanceof UndoChangeRequest)) {
            return;
        }

        // The ports of the _target may have changed.
        _setupTableModel();
    }

    /**
     * Notify the listener that a change has resulted in an exception.
     *
     * @param change The change that was attempted.
     * @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        // TODO Determine best way to handle failed change
        // request. This method _should_ never be invoked if the
        // source is this.  For now, at least, test to see if the
        // source is this, and report it.
        if (change == null) {
            return;
        }

        if (!change.isErrorReported()) {
            MessageHandler.error("Change failed: ", exception);
        }
    }

    /** Close this dialog.  If the state has not be saved, prompt
     *  the user to save the modifications.
     *  @return false if the user selects cancel, otherwise return true.
     */
    public boolean close() {
        if (_isDirty()) {
            int option = JOptionPane.showConfirmDialog(getOwner(),
                    "Save port modifications on " + getTarget().getFullName(),
                    "Unsaved Port Modifications",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            switch (option) {
            case JOptionPane.YES_OPTION: {
                _apply();
                return true;
            }

            case JOptionPane.NO_OPTION:
                return true;

            case JOptionPane.CANCEL_OPTION:
                return false;
            }
        }

        return true;
    }

    @Override
    public void saveIfRequired() {
        if (_isDirty()) {
            int option = JOptionPane.showConfirmDialog(getOwner(),
                    "Save port modifications on " + getTarget().getFullName()
                    + "?", "Unsaved Port Modifications",
                    JOptionPane.YES_NO_OPTION);

            switch (option) {
            case JOptionPane.YES_OPTION:
                _apply();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Apply any changes that may have been made in the table.
     *  @return true if the change was successfully applied
     */
    protected boolean _apply() {
        // The port names in the table will be used many times, so extract
        // them here.
        String[] portNameInTable = new String[_portTableModel.getRowCount()];

        for (int i = 0; i < _portTableModel.getRowCount(); i++) {
            portNameInTable[i] = (String) _portTableModel.getValueAt(i,
                    _columnNames.indexOf(ColumnNames.COL_NAME));
        }

        // Do some basic checks on table for things that are obviously
        // incorrect. First, make sure all the new ports have names
        // other than the empty string.
        for (int i = 0; i < _portTableModel.getRowCount(); i++) {
            if (portNameInTable[i].equals("")) {
                JOptionPane.showMessageDialog(this,
                        "All Ports need to have a name.");
                return false;
            }
        }

        //  Now, make sure all port names are unique.
        for (int i = 0; i < _portTableModel.getRowCount(); i++) {
            for (int j = i + 1; j < _portTableModel.getRowCount(); j++) {
                if (portNameInTable[i].equals(portNameInTable[j])) {
                    JOptionPane.showMessageDialog(this, portNameInTable[i]
                            + " is a duplicate port name.\n"
                            + "Please remove all but one");
                    return false;
                }
            }
        }

        // Determine which ports have been removed. If a port exists on the
        // target but is not represented by a row in the table then it needs
        // to be removed.
        Vector portsToBeRemoved = new Vector();
        Iterator portIterator = getTarget().portList().iterator();
        Port actualPort = null;

        while (portIterator.hasNext()) {
            Object candidate = portIterator.next();

            if (candidate instanceof Port) {
                boolean foundPort = false;
                actualPort = (Port) candidate;

                for (int i = 0; i < _ports.size(); i++) {
                    Hashtable portInfo = (Hashtable) _ports.elementAt(i);

                    if (actualPort == (Port) portInfo
                            .get(ColumnNames.COL_ACTUAL_PORT)) {
                        foundPort = true;
                        break;
                    }
                }

                if (!foundPort) {
                    portsToBeRemoved.add(actualPort);
                }
            } else {
                Exception exception = new InternalErrorException(
                        "The target portList contains" + " an object \""
                                + candidate + "\"that is not of type Port.");

                MessageHandler.error("Internal Error while removing a port.",
                        exception);
            }
        }

        Iterator actualPorts = portsToBeRemoved.iterator();

        while (actualPorts.hasNext()) {
            StringBuffer moml = new StringBuffer();
            actualPort = (Port) actualPorts.next();

            // The context for the MoML should be the first container
            // above this port in the hierarchy that defers its MoML
            // definition, or the immediate parent if there is none.
            NamedObj container = actualPort.getContainer();
            NamedObj composite = container.getContainer();

            if (composite != null) {
                moml.append("<deletePort name=\""
                        + StringUtilities.escapeForXML(actualPort.getName())
                        + "\" entity=\"" + container.getName() + "\" />");
            } else {
                moml.append("<deletePort name=\""
                        + StringUtilities.escapeForXML(actualPort
                                .getName(container)) + "\" />");
            }

            // NOTE: the context is the composite entity containing
            // the entity if possible
            MoMLChangeRequest request = null;

            if (composite != null) {
                request = new MoMLChangeRequest(this, composite,
                        moml.toString());
            } else {
                request = new MoMLChangeRequest(this, container,
                        moml.toString());
            }

            request.setUndoable(true);
            container.addChangeListener(this);

            container.requestChange(request);
        }

        // Iterate over the table rows that represent ports.  If a row
        // corresponds to an actual port then look to see if that row
        // is different from the actual port.  If it is, then update
        // that actual port.  If a row does not correspond to an
        // actual port then that row represents a new actual port
        // which must be created.
        StringBuffer moml = new StringBuffer("<group>");
        boolean haveSomeUpdate = false;

        for (int i = 0; i < _ports.size(); i++) {
            Hashtable portInfo = (Hashtable) _ports.elementAt(i);
            portIterator = getTarget().portList().iterator();

            // actualPort will be the Port found on the _target, if
            // there is one.
            actualPort = (Port) portInfo.get(ColumnNames.COL_ACTUAL_PORT);

            Hashtable updates = new Hashtable();

            // FIXME is it necessary to add unchanged fields to hashtable ?
            if (actualPort != null) {
                // actualPort is a Port found on the _target. Check to
                // see if the actualPort is different and needs to be
                // updated.
                boolean havePortUpdate = false;

                if (_columnNames.contains(ColumnNames.COL_NAME)) {
                    String tableValue = (String) portInfo
                            .get(ColumnNames.COL_NAME);

                    if (!actualPort.getName().equals(tableValue)) {
                        if (tableValue.contains(".")) {
                            MessageHandler
                            .error("Failed to rename port "
                                    + actualPort.getName()
                                    + "; port names are not allowed to contain periods.",
                                    new InternalErrorException(
                                            null,
                                            null,
                                            "Instead, alias the port by setting display name. "
                                                    + "Right-click on the port,\nchoose Rename, then enter "
                                                    + "the desired alias into the field Display Name."));
                            _applyChangeRequestFailed = true;
                        } else {
                            havePortUpdate = true;
                            updates.put(ColumnNames.COL_NAME, Boolean.TRUE);
                        }
                    }
                }

                if (actualPort instanceof IOPort) {
                    IOPort iop = (IOPort) actualPort;

                    if (_columnNames.contains(ColumnNames.COL_INPUT)) {
                        Boolean tableValue = (Boolean) portInfo
                                .get(ColumnNames.COL_INPUT);

                        if (iop.isInput() != tableValue.booleanValue()) {
                            havePortUpdate = true;
                            updates.put(ColumnNames.COL_INPUT, Boolean.TRUE);
                        }
                    }

                    if (_columnNames.contains(ColumnNames.COL_OUTPUT)) {
                        Boolean tableValue = (Boolean) portInfo
                                .get(ColumnNames.COL_OUTPUT);

                        if (iop.isOutput() != tableValue.booleanValue()) {
                            havePortUpdate = true;
                            updates.put(ColumnNames.COL_OUTPUT, Boolean.TRUE);
                        }
                    }

                    if (_columnNames.contains(ColumnNames.COL_MULTIPORT)) {
                        Boolean tableValue = (Boolean) portInfo
                                .get(ColumnNames.COL_MULTIPORT);

                        if (iop.isMultiport() != tableValue.booleanValue()) {
                            havePortUpdate = true;
                            updates.put(ColumnNames.COL_MULTIPORT, Boolean.TRUE);
                        }
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_SHOW_NAME)) {
                    boolean isShowSet = _isPropertySet(actualPort, "_showName");
                    Boolean tableValue = (Boolean) portInfo
                            .get(ColumnNames.COL_SHOW_NAME);

                    if (isShowSet != tableValue.booleanValue()) {
                        havePortUpdate = true;
                        updates.put(ColumnNames.COL_SHOW_NAME, Boolean.TRUE);
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_HIDE)) {
                    boolean isHideSet = _isPropertySet(actualPort, "_hide");
                    Boolean tableValue = (Boolean) portInfo
                            .get(ColumnNames.COL_HIDE);

                    if (isHideSet != tableValue.booleanValue()) {
                        havePortUpdate = true;
                        updates.put(ColumnNames.COL_HIDE, Boolean.TRUE);
                    }
                }

                if (actualPort instanceof TypedIOPort) {
                    TypedIOPort tiop = (TypedIOPort) actualPort;

                    if (_columnNames.contains(ColumnNames.COL_TYPE)) {
                        String type = null;
                        // NOTE: It is possible for there to be a type attribute
                        // with some other name than "_type". In theory, there
                        // should only be one, but just in case, always use the last
                        // one.
                        List<TypeAttribute> attributes = tiop
                                .attributeList(TypeAttribute.class);
                        if (attributes.size() > 0) {
                            TypeAttribute typeAttribute = attributes
                                    .get(attributes.size() - 1);
                            type = typeAttribute.getExpression();
                        }

                        String tableValue = (String) portInfo
                                .get(ColumnNames.COL_TYPE);

                        // NOTE: bypassing the MoML parser and setting the type
                        // directly, in order to propagate the type change
                        // required when the input field is set to blank.
                        // See bug #518 in Bugzilla
                        // This is no longer necessary as it is handled by TypeAttribute.
                        /*
                        if (tableValue.equals("")) {
                            tiop.setTypeEquals(BaseType.UNKNOWN);
                        }
                         */
                        if (type == null && !tableValue.equals("")
                                || type != null && !tableValue.equals(type)) {
                            havePortUpdate = true;
                            updates.put(ColumnNames.COL_TYPE, Boolean.TRUE);
                        }
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_DIRECTION)) {
                    // Look for a change in direction
                    String _direction = null;
                    String direction = (String) portInfo
                            .get(ColumnNames.COL_DIRECTION);
                    StringAttribute _cardinal = (StringAttribute) actualPort
                            .getAttribute("_cardinal");

                    if (_cardinal != null) {
                        _direction = _cardinal.getExpression().toUpperCase(
                                Locale.getDefault());
                    }

                    if (_direction == null && !direction.equals("DEFAULT")
                            || _direction != null
                            && !direction.equals(_direction)) {
                        havePortUpdate = true;
                        updates.put(ColumnNames.COL_DIRECTION, Boolean.TRUE);
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_UNITS)) {
                    String units = null;
                    UnitAttribute _unitsAttribute = (UnitAttribute) actualPort
                            .getAttribute("_units");

                    if (_unitsAttribute != null) {
                        units = _unitsAttribute.getExpression();
                    }

                    String tableValue = (String) portInfo
                            .get(ColumnNames.COL_UNITS);

                    // tableValue will not be null because we put ""
                    // into portInfo in the constructor of
                    // PortTableModel.
                    if (units == null && !tableValue.equals("")
                            || units != null && !tableValue.equals(units)) {
                        havePortUpdate = true;
                        updates.put(ColumnNames.COL_UNITS, Boolean.TRUE);
                    }
                }

                if (havePortUpdate) {
                    String currentPortName = ((Port) portInfo
                            .get(ColumnNames.COL_ACTUAL_PORT)).getName();
                    String newPortName = (String) portInfo
                            .get(ColumnNames.COL_NAME);

                    String momlString = _createMoMLUpdate(updates, portInfo,
                            currentPortName, newPortName);

                    moml.append(momlString);
                    haveSomeUpdate = true;
                }
            } else {
                // actualPort is not found on the _target so make a new one.
                // Initialize all columns to be updated for this port entry.
                Iterator it = _columnNames.iterator();

                while (it.hasNext()) {
                    String element = (String) it.next();
                    updates.put(element, Boolean.TRUE);
                }

                // FIXME is it necessary to remove unchanged fields
                // from updates hashtable?
                // Make this false, since this is a new port that does
                // not have a pre-existing name.  Note that "rename"
                // is used for pre-existing ports with new names.
                if (_columnNames.contains(ColumnNames.COL_NAME)) {
                    String tableValue = (String) portInfo
                            .get(ColumnNames.COL_NAME);
                    if (tableValue.contains(".")) {
                        MessageHandler
                        .error("Failed to add port \""
                                + tableValue
                                + "\"; port names are not allowed to contain periods.",
                                new InternalErrorException(
                                        null,
                                        null,
                                        "Instead, provide a name without periods and then alias the port by setting display name.\n"
                                                + "Right-click on the port, choose Rename, then enter "
                                                + "the desired alias into the field Display Name."));
                        break;
                    } else {
                        updates.put(ColumnNames.COL_NAME, Boolean.FALSE);
                    }

                }

                // Put this in the MoMLChangeRequest if the value is
                // not the default of false.
                if (_columnNames.contains(ColumnNames.COL_SHOW_NAME)) {
                    updates.put(ColumnNames.COL_SHOW_NAME,
                            portInfo.get(ColumnNames.COL_SHOW_NAME));
                }

                // Put this in the MoMLChangeRequest if the value is
                // not the default of false.
                if (_columnNames.contains(ColumnNames.COL_HIDE)) {
                    updates.put(ColumnNames.COL_HIDE,
                            portInfo.get(ColumnNames.COL_HIDE));
                }

                // FIXME: should we compare against "unknown" instead of ""?
                if (_columnNames.contains(ColumnNames.COL_TYPE)) {
                    String type = (String) portInfo.get(ColumnNames.COL_TYPE);

                    if (!type.equals("")) {
                        updates.put(ColumnNames.COL_TYPE, Boolean.TRUE);
                        _portTableModel.fireTableDataChanged();
                    } else {
                        // Do not make this part of the
                        // MoMLChangeRequest if the value is equal to
                        // "".
                        updates.put(ColumnNames.COL_TYPE, Boolean.FALSE);
                    }
                }

                // Put this in the MoMLChangeRequest if the value is
                // not the default.
                if (_columnNames.contains(ColumnNames.COL_DIRECTION)) {
                    String direction = (String) portInfo
                            .get(ColumnNames.COL_DIRECTION);

                    if (!direction.equals("DEFAULT")) {
                        updates.put(ColumnNames.COL_DIRECTION, Boolean.TRUE);
                        _portTableModel.fireTableDataChanged();
                    } else {
                        updates.put(ColumnNames.COL_DIRECTION, Boolean.FALSE);
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_UNITS)) {
                    String unit = (String) portInfo.get(ColumnNames.COL_UNITS);

                    if (!unit.equals("")) {
                        updates.put(ColumnNames.COL_UNITS, Boolean.TRUE);
                        _portTableModel.fireTableDataChanged();
                    } else {
                        // Do not make this part of the
                        // MoMLChangeRequest if the value is equal to
                        // "".
                        updates.put(ColumnNames.COL_UNITS, Boolean.FALSE);
                    }
                }

                moml.append(_createMoMLUpdate(updates, portInfo,
                        (String) portInfo.get(ColumnNames.COL_NAME), null));

                haveSomeUpdate = true;
            }
        }

        if (haveSomeUpdate) {
            moml.append("</group>");

            MoMLChangeRequest request = new MoMLChangeRequest(this,
                    getTarget(), moml.toString(), null);
            request.setUndoable(true);

            // NOTE: There is no need to listen for completion or
            // errors in this change request, since, in theory, it
            // will just work. Will someone report the error if one
            // occurs? I hope so...
            _applyChangeRequestFailed = false;
            try {
                getTarget().requestChange(request);
            } catch (Throwable throwable) {
                MessageHandler.error(
                        "Failed to apply changes",
                        new InternalErrorException(getTarget(), throwable, moml
                                .toString()));
                _applyChangeRequestFailed = true;
                return false;
            }
            _populateActualPorts();
        }

        _setDirty(false);
        _enableApplyButton(false);

        // Update the remove button label.
        _setSelectedRow(_portTable.getSelectionModel()
                .getAnchorSelectionIndex());
        return true;
    }

    @Override
    protected void _cancel() {
        getTarget().removeChangeListener(this);
        super._cancel();
    }

    @Override
    protected void _createExtendedButtons(JPanel _buttons) {
        Button = new JButton("Commit");
        _buttons.add(Button);
        _applyButton = new JButton("Apply");
        _buttons.add(_applyButton);
        _addButton = new JButton("Add");
        _buttons.add(_addButton);
        _removeButton = new JButton("Remove           ");
        _removeButton.setEnabled(false);
        _buttons.add(_removeButton);
    }

    /** Return a URL that points to the help page.
     *  @return A URL that points to the help page
     */
    @Override
    protected URL _getHelpURL() {
        URL helpURL = getClass().getClassLoader().getResource(
                "ptolemy/actor/gui/doc/portConfigurerDialog.htm");
        return helpURL;
    }

    /** Process a button press.
     *  @param button The button.
     */
    @Override
    protected void _processButtonPress(String button) {
        // If the user has typed in a port name, but not
        // moved the focus, we want to tell the model the
        // data has changed.
        if (_portTable.isEditing()) {
            _portTable.editingStopped(new ChangeEvent(button));
        }

        _portTableModel.fireTableDataChanged();

        // The button semantics are
        // Add - Add a new port.
        if (button.equals("Apply")) {
            if (_apply()) {
                if (_applyChangeRequestFailed) {
                    // If the change request fails, the close the dialog,
                    // as there are problems which we cannot recover from.
                    // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4478
                    _cancel();
                }
            }
        } else if (button.equals("Commit")) {
            if (_apply()) {
                // We always close the window after commit.
                _cancel();
            }
        } else if (button.equals("Add")) {
            _portTableModel.addNewPort();
        } else if (
                // FIXME this depends on button name string length.
                button.length() > 5 && button.substring(0, 6).equals("Remove")) {
            _portTableModel.removePort();
            _setSelectedRow(-1);
        } else {
            super._processButtonPress(button);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The table model for the table. */
    class PortTableModel extends AbstractTableModel {
        /** Populates the _ports Vector. Each element of _ports is a
         * Hashtable that represents a Port on the Entity that is
         * having its ports configured.  If the Port exists on the
         * Entity, a reference to it is stored in the Hashtable with
         * key = ColumnNames.COL_ACTUAL_PORT.
         * @param portList The list of ports.
         */
        public PortTableModel(List portList) {
            Iterator ports = portList.iterator();
            _ports = new Vector();

            while (ports.hasNext()) {
                Port p = (Port) ports.next();
                Hashtable portInfo = new Hashtable();

                if (_columnNames.contains(ColumnNames.COL_NAME)) {
                    portInfo.put(ColumnNames.COL_NAME, p.getName());
                }

                if (_columnNames.contains(ColumnNames.COL_DIRECTION)) {
                    String _direction;
                    StringAttribute _cardinal = (StringAttribute) p
                            .getAttribute("_cardinal");

                    if (_cardinal != null) {
                        _direction = _cardinal.getExpression().toUpperCase(
                                Locale.getDefault());
                    } else {
                        _direction = "DEFAULT";
                    }

                    portInfo.put(ColumnNames.COL_DIRECTION, _direction);
                }

                if (_columnNames.contains(ColumnNames.COL_SHOW_NAME)) {
                    boolean isShowSet = _isPropertySet(p, "_showName");

                    if (isShowSet) {
                        portInfo.put(ColumnNames.COL_SHOW_NAME, Boolean.TRUE);
                    } else {
                        portInfo.put(ColumnNames.COL_SHOW_NAME, Boolean.FALSE);
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_HIDE)) {
                    boolean isHideSet = _isPropertySet(p, "_hide");

                    if (isHideSet) {
                        portInfo.put(ColumnNames.COL_HIDE, Boolean.TRUE);
                    } else {
                        portInfo.put(ColumnNames.COL_HIDE, Boolean.FALSE);
                    }
                }

                if (p instanceof IOPort) {
                    IOPort iop = (IOPort) p;

                    if (_columnNames.contains(ColumnNames.COL_INPUT)) {
                        portInfo.put(ColumnNames.COL_INPUT,
                                Boolean.valueOf(iop.isInput()));
                    }

                    if (_columnNames.contains(ColumnNames.COL_OUTPUT)) {
                        portInfo.put(ColumnNames.COL_OUTPUT,
                                Boolean.valueOf(iop.isOutput()));
                    }

                    if (_columnNames.contains(ColumnNames.COL_MULTIPORT)) {
                        portInfo.put(ColumnNames.COL_MULTIPORT,
                                Boolean.valueOf(iop.isMultiport()));
                    }
                }

                if (p instanceof TypedIOPort) {
                    TypedIOPort tiop = (TypedIOPort) p;

                    if (_columnNames.contains(ColumnNames.COL_TYPE)) {
                        // NOTE: It is possible for there to be a type attribute
                        // with some other name than "_type". In theory, there
                        // should only be one, but just in case, always use the last
                        // one.
                        List<TypeAttribute> attributes = tiop
                                .attributeList(TypeAttribute.class);
                        if (attributes.size() > 0) {
                            TypeAttribute type = attributes.get(attributes
                                    .size() - 1);
                            portInfo.put(ColumnNames.COL_TYPE,
                                    type.getExpression());
                        } else {
                            portInfo.put(ColumnNames.COL_TYPE, "");
                        }
                    }
                }

                if (_columnNames.contains(ColumnNames.COL_UNITS)) {
                    String units = "";
                    UnitAttribute _unitsAttribute = (UnitAttribute) p
                            .getAttribute("_units");

                    if (_unitsAttribute != null) {
                        units = _unitsAttribute.getExpression();

                        if (units != null) {
                            portInfo.put(ColumnNames.COL_UNITS, units);
                        } else {
                            portInfo.put(ColumnNames.COL_UNITS, "");
                        }
                    } else {
                        // Set units to "" anyways.  If the user
                        // doesn't change the value, nothing will be
                        // added to the MoMLChangeRequest for units in
                        // _apply().
                        portInfo.put(ColumnNames.COL_UNITS, "");
                    }
                }

                portInfo.put(ColumnNames.COL_ACTUAL_PORT, p);

                _ports.add(portInfo);
            }
        }

        /**
         * Add a port The new port gets added with a name of "". It is
         * assumed that the user will change this to the real name at
         * some point.
         */
        public void addNewPort() {
            Hashtable portInfo = new Hashtable();

            if (_columnNames.contains(ColumnNames.COL_NAME)) {
                portInfo.put(ColumnNames.COL_NAME, "");
            }

            if (_columnNames.contains(ColumnNames.COL_DIRECTION)) {
                portInfo.put(ColumnNames.COL_DIRECTION, "DEFAULT");
            }

            if (_columnNames.contains(ColumnNames.COL_SHOW_NAME)) {
                portInfo.put(ColumnNames.COL_SHOW_NAME, Boolean.FALSE);
            }

            if (_columnNames.contains(ColumnNames.COL_HIDE)) {
                portInfo.put(ColumnNames.COL_HIDE, Boolean.FALSE);
            }

            if (_columnNames.contains(ColumnNames.COL_INPUT)) {
                portInfo.put(ColumnNames.COL_INPUT, Boolean.FALSE);
            }

            if (_columnNames.contains(ColumnNames.COL_OUTPUT)) {
                portInfo.put(ColumnNames.COL_OUTPUT, Boolean.FALSE);
            }

            if (_columnNames.contains(ColumnNames.COL_MULTIPORT)) {
                portInfo.put(ColumnNames.COL_MULTIPORT, Boolean.FALSE);
            }

            if (_columnNames.contains(ColumnNames.COL_TYPE)) {
                portInfo.put(ColumnNames.COL_TYPE, "");
            }

            if (_columnNames.contains(ColumnNames.COL_UNITS)) {
                portInfo.put(ColumnNames.COL_UNITS, "");
            }

            _ports.add(portInfo);

            // Now tell the GUI so that it can update itself.
            fireTableRowsInserted(getRowCount(), getRowCount());

            // FIXME: Move the focus to the last row
        }

        /**
         * Removes a port.
         */
        public void removePort() {
            // First remove it from the _ports, and then tell the GUI
            // that it is gone so that it can update itself.
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
        @Override
        public int getColumnCount() {
            return _columnNames.size();
        }

        /** Get the number of rows.
         * @see javax.swing.table.TableModel#getRowCount()
         */
        @Override
        public int getRowCount() {
            return _ports.size();
        }

        /** Get the column header name.
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int col) {
            return (String) _columnNames.get(col);
        }

        /** Get the value at a particular row and column.
         * @param row
         * @param col
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int col) {
            Hashtable portInfo = (Hashtable) _ports.elementAt(row);
            return portInfo.get(getColumnName(col));
        }

        /** Set the value at a particular row and column.
         * @param value The value to be set.
         * @param row The row.
         * @param col The column.
         * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            Hashtable portInfo = (Hashtable) _ports.elementAt(row);
            portInfo.put(getColumnName(col), value);
            _enableApplyButton(true);
            _setDirty(true);
        }

        /** Get the Java Class associated with a column param column.
         * @return class
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /** Is a cell editable?
         *
         * @param row
         * @param col
         * @return true if editable
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            Hashtable portInfo = (Hashtable) _ports.elementAt(row);
            Port port = (Port) portInfo.get(ColumnNames.COL_ACTUAL_PORT);

            if (port != null) {
                if (port.getDerivedLevel() < Integer.MAX_VALUE) {
                    if (col == _columnNames.indexOf(ColumnNames.COL_NAME)
                            || col == _columnNames
                            .indexOf(ColumnNames.COL_INPUT)
                            || col == _columnNames
                            .indexOf(ColumnNames.COL_OUTPUT)
                            || col == _columnNames
                            .indexOf(ColumnNames.COL_MULTIPORT)) {
                        return false;
                    }
                }
            }

            return true;
        }

        /**
         * Make the "Show Name" column values be either all true or
         * all false.
         */
        public void toggleShowAllNames() {
            _showAllNames = !_showAllNames;

            Boolean show = Boolean.valueOf(_showAllNames);

            for (int i = 0; i < getRowCount(); i++) {
                setValueAt(show, i,
                        _columnNames.indexOf(ColumnNames.COL_SHOW_NAME));
            }
        }

        /**
         * Make the "Hide" column values be either all true or
         * all false.
         */
        public void toggleHidePorts() {
            _hideAllPorts = !_hideAllPorts;

            Boolean _hide = Boolean.valueOf(_hideAllPorts);

            for (int i = 0; i < getRowCount(); i++) {
                setValueAt(_hide, i, _columnNames.indexOf(ColumnNames.COL_HIDE));
            }
        }
    }

    /** Render a boolean cell. */
    static class PortBooleanCellRenderer extends JCheckBox implements
    TableCellRenderer {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public PortBooleanCellRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            // FindBugs: Use equals, not == and avoid RC: Suspicious
            // reference comparison of Boolean values.
            if (value.equals(Boolean.TRUE)) {
                setSelected(true);
            } else {
                setSelected(false);
            }

            setHorizontalAlignment(SwingConstants.CENTER);

            if (!table.isCellEditable(row, col)) {
                setBackground(UNEDITABLE_CELL_COLOR);
            } else {
                setBackground(Color.white);
            }

            return this;
        }
    }

    /**
     * Default renderer for _portTable.
     *
     * see _setupTableModel()
     */
    static class StringCellRenderer extends JLabel implements TableCellRenderer {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public StringCellRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int col) {
            setOpaque(true);
            setText((String) value);

            if (!table.isCellEditable(row, col)) {
                setBackground(UNEDITABLE_CELL_COLOR);
            } else {
                setBackground(Color.white);
            }

            return this;
        }
    }

    /** Validate a cell. */
    abstract class CellValidator {
        /** Return true if the value is valid.
         *  @param value The value to validate.
         *  @return True if the value is valid.
         */
        public abstract boolean isValid(String value);

        /** Set the message.
         *  @param message The message.
         *  @see #getMessage()
         */
        public void setMessage(String message) {
            _message = message;
        }

        /** Get the message.
         *  @return The message
         *  @see #setMessage(String)
         */
        public String getMessage() {
            return _message;
        }

        /** The message. */
        private String _message = null;
    }

    /**
     A validating JTextField table cell editor for use with JTable.
     To determine if a selection is valid, this class uses the
     CellValidator class.

     <p>Based on IntegerEditor from
     http://download.oracle.com/javase/tutorial/uiswing/examples/components/TableFTFEditDemoProject/src/components/IntegerEditor.java

     @author Christopher Brooks, Sun Microsystems
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (eal)
     @Pt.AcceptedRating Red (eal)
     */
    public class ValidatingJTextFieldCellEditor extends DefaultCellEditor {
        /** Construct a validating JTextField JTable Cell editor.
         */
        public ValidatingJTextFieldCellEditor() {
            super(new JFormattedTextField());
        }

        /** Construct a validating JTextField JTable Cell editor.
         *  @param jFormattedTextField The JTextField that provides choices.
         */
        public ValidatingJTextFieldCellEditor(
                final JFormattedTextField jFormattedTextField) {
            super(jFormattedTextField);

            _jFormattedTextField = (JFormattedTextField) getComponent();

            // React when the user presses Enter while the editor is
            // active.  (Tab is handled as specified by
            // JFormattedTextField's focusLostBehavior property.)
            jFormattedTextField.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
            jFormattedTextField.getActionMap().put("check",
                    new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean valid = true;

                    if (_validator != null) {
                        valid = _validator.isValid(jFormattedTextField
                                .getText());
                    }

                    if (!valid) {
                        userSaysRevert(jFormattedTextField.getText());
                    } else {
                        jFormattedTextField.postActionEvent(); //stop editing
                    }
                }
            });
            _jFormattedTextField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent ke) {
                    _setDirty(true);
                    _enableApplyButton(true);

                    if (ke.getKeyChar() == '\n') {
                        if (_apply()) {
                            _cancel();
                        }
                    }
                }
            });
            _jFormattedTextField.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent event) {
                    // Set the selected row so the remove key gets updated
                    _setSelectedRow(_portTable.getSelectionModel()
                            .getAnchorSelectionIndex());
                }

                @Override
                public void focusLost(FocusEvent event) {
                }
            });
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         */
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            JTextField jTextField = (JTextField) super
                    .getTableCellEditorComponent(table, value, isSelected, row,
                            column);
            _oldValue = jTextField.getText();
            jTextField.setText((String) value);
            return jTextField;
        }

        /** Get the cell editor value.
         *  @return The string value of the selected item in the combobox.
         */
        @Override
        public Object getCellEditorValue() {
            // FIXME: do we need to get jTextField like this each time?
            JTextField jTextField = (JTextField) getComponent();
            Object o = jTextField.getText();
            return o.toString();
        }

        /** Set the validator.
         *  @param validator The validator.
         */
        public void setValidator(CellValidator validator) {
            _validator = validator;
        }

        /** Check the selection and determine whether we should stop editing.
         *  If the selection is invalid, ask the user if they want to revert.
         *  If the selection is valid, then call stopCellEditing in the super
         *  class
         *  @return False if the selection is invalid.  Otherwise,
         *  return whatever super.stopCellEditing() returns.
         */
        @Override
        public boolean stopCellEditing() {
            // FIXME: do we need to get jTextField like this each time?
            JFormattedTextField jFormattedTextField = (JFormattedTextField) getComponent();

            if (jFormattedTextField.getText() == null) {
                // FIXME: why does the selected item get set to null sometimes?
                jFormattedTextField.setText("");
            }

            boolean valid = true;

            if (_validator != null) {
                valid = _validator.isValid(jFormattedTextField.getText());
            }

            if (!valid) {
                if (_userWantsToEdit) {
                    // User already selected edit, don't ask twice.
                    _userWantsToEdit = false;
                    return false;
                } else {
                    if (!userSaysRevert(jFormattedTextField.getText())) {
                        _userWantsToEdit = true;
                        return false; //don't let the editor go away
                    }
                }
            }

            return super.stopCellEditing();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected methods                 ////

        /** Return true if the user wants to revert to the original value.
         *  A dialog box pops up that tells the user that their selection
         *  is invalid.
         *  @param selectedItem The selected item.
         *  @return True if the user elects to revert to the last good
         *  value.  Otherwise, returns false, indicating that the user
         *  wants to continue editing.
         */
        protected boolean userSaysRevert(String selectedItem) {
            Toolkit.getDefaultToolkit().beep();
            _jFormattedTextField.selectAll();

            Object[] options = { "Edit", "Revert" };
            int answer = JOptionPane.showOptionDialog(
                    SwingUtilities.getWindowAncestor(_jFormattedTextField),
                    "The value \"" + selectedItem + "\" is not valid:\n"
                            + _validator.getMessage()
                            + "\nYou can either continue editing "
                            + "or revert to the last valid value \""
                            + _oldValue + "\".", "Invalid Text Entered",
                            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
                            options, options[1]);

            if (answer == 1) { //Revert!
                _jFormattedTextField.setText((String) _oldValue);
                return true;
            }

            return false;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** The JTextField. */
        private JFormattedTextField _jFormattedTextField;

        /** Old value of the JTextField. */
        private Object _oldValue;

        /** True if the user wants to edit after having an invalid selection.*/
        private boolean _userWantsToEdit;

        /** Class that validates the cell. */
        private CellValidator _validator;
    }

    /**
     A validating ComboBox table cell editor for use with JTable.
     To determine if a selection is valid, this class uses the
     CellValidator class.

     <p>Based on IntegerEditor from
     http://download.oracle.com/javase/tutorial/uiswing/examples/components/TableFTFEditDemoProject/src/components/IntegerEditor.java

     @author Christopher Brooks, Sun Microsystems
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (eal)
     @Pt.AcceptedRating Red (eal)
     */
    public static class ValidatingComboBoxCellEditor extends DefaultCellEditor {
        // FindBugs suggested refactoring this into a static class.

        /** Construct a validating combo box JTable Cell editor.
         *  @param comboBox The combo box that provides choices.
         */
        public ValidatingComboBoxCellEditor(final JComboBox comboBox) {
            super(comboBox);
            _comboBox = (JComboBox) getComponent();

            // React when the user presses Enter while the editor is
            // active.  (Tab is handled as specified by
            // JFormattedTextField's focusLostBehavior property.)
            comboBox.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
            comboBox.getActionMap().put("check", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean valid = true;

                    if (_validator != null) {
                        valid = _validator.isValid((String) comboBox
                                .getSelectedItem());
                    }

                    if (!valid) {
                        userSaysRevert((String) comboBox.getSelectedItem());
                    }
                }
            });
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /**
         */
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            JComboBox comboBox = (JComboBox) super.getTableCellEditorComponent(
                    table, value, isSelected, row, column);
            _oldValue = comboBox.getSelectedItem();
            comboBox.setSelectedItem(value);
            return comboBox;
        }

        /** Get the cell editor value.
         *  @return The string value of the selected item in the combobox.
         */
        @Override
        public Object getCellEditorValue() {
            // FIXME: do we need to get comboBox like this each time?
            JComboBox comboBox = (JComboBox) getComponent();
            Object o = comboBox.getSelectedItem();
            return o.toString();
        }

        /** Set the validator.
         *  @param validator The validator.
         */
        public void setValidator(CellValidator validator) {
            _validator = validator;
        }

        /** Check the selection and determine whether we should stop editing.
         *  If the selection is invalid, ask the user if they want to revert.
         *  If the selection is valid, then call stopCellEditing in the super
         *  class
         *  @return False if the selection is invalid.  Otherwise,
         *  return whatever super.stopCellEditing() returns.
         */
        @Override
        public boolean stopCellEditing() {
            // FIXME: do we need to get comboBox like this each time?
            JComboBox comboBox = (JComboBox) getComponent();

            if (comboBox.getSelectedItem() == null) {
                // FIXME: why does the selected item get set to null sometimes?
                comboBox.setSelectedItem("");
            }

            boolean valid = true;

            if (_validator != null) {
                valid = _validator.isValid((String) comboBox.getSelectedItem());
            }

            if (!valid) {
                if (_userWantsToEdit) {
                    // User already selected edit, don't ask twice.
                    _userWantsToEdit = false;
                    return false;
                } else {
                    if (!userSaysRevert((String) comboBox.getSelectedItem())) {
                        _userWantsToEdit = true;
                        return false; //don't let the editor go away
                    }
                }
            }

            return super.stopCellEditing();
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected methods                 ////

        /** Return true if the user wants to revert to the original value.
         *  A dialog box pops up that tells the user that their selection
         *  is invalid.
         *  @param selectedItem The selected item.
         *  @return True if the user elects to revert to the last good
         *  value.  Otherwise, returns false, indicating that the user
         *  wants to continue editing.
         */
        protected boolean userSaysRevert(String selectedItem) {
            Toolkit.getDefaultToolkit().beep();

            //_comboBox.selectAll();
            Object[] options = { "Edit", "Revert" };
            int answer = JOptionPane.showOptionDialog(
                    SwingUtilities.getWindowAncestor(_comboBox),
                    "The value \"" + selectedItem + "\" is not valid:\n"
                            + _validator.getMessage()
                            + "\nYou can either continue editing "
                            + "or revert to the last valid value \""
                            + _oldValue + "\".", "Invalid Text Entered",
                            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
                            options, options[1]);

            if (answer == 1) { //Revert!
                _comboBox.setSelectedItem(_oldValue);
                return true;
            }

            return false;
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** The combo box. */
        private JComboBox _comboBox;

        /** Old value of the combo box. */
        private Object _oldValue;

        /** True if the user wants to edit after having an invalid selection.*/
        private boolean _userWantsToEdit;

        /** Class that validates the cell. */
        private CellValidator _validator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create the MoML expression that represents the update. */
    private String _createMoMLUpdate(Hashtable updates, Hashtable portInfo,
            String currentPortName, String newPortName) {
        StringBuffer momlUpdate = new StringBuffer("<port name=\""
                + StringUtilities.escapeForXML(currentPortName) + "\">");

        // Assumes that updates only contains keys that are in _columnNames.
        // Assumes that updates only contains COL_NAME as key if the
        // pre-existing port needs to be renamed.
        if (updates.containsKey(ColumnNames.COL_NAME)) {
            Boolean updateValue = (Boolean) updates.get(ColumnNames.COL_NAME);

            if (updateValue.booleanValue()) {
                momlUpdate.append("<rename name=\""
                        + StringUtilities.escapeForXML(newPortName) + "\"/>");
            }
        }

        if (updates.containsKey(ColumnNames.COL_INPUT)) {
            Boolean updateValue = (Boolean) updates.get(ColumnNames.COL_INPUT);

            if (updateValue.booleanValue()) {
                if (((Boolean) portInfo.get(ColumnNames.COL_INPUT))
                        .booleanValue()) {
                    momlUpdate.append(_momlProperty("input"));
                } else {
                    momlUpdate.append(_momlProperty("input", null, "false"));
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_OUTPUT)) {
            Boolean updateValue = (Boolean) updates.get(ColumnNames.COL_OUTPUT);

            if (updateValue.booleanValue()) {
                if (((Boolean) portInfo.get(ColumnNames.COL_OUTPUT))
                        .booleanValue()) {
                    momlUpdate.append(_momlProperty("output"));
                } else {
                    momlUpdate.append(_momlProperty("output", null, "false"));
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_MULTIPORT)) {
            Boolean updateValue = (Boolean) updates
                    .get(ColumnNames.COL_MULTIPORT);

            if (updateValue.booleanValue()) {
                if (((Boolean) portInfo.get(ColumnNames.COL_MULTIPORT))
                        .booleanValue()) {
                    momlUpdate.append(_momlProperty("multiport"));
                } else {
                    momlUpdate
                    .append(_momlProperty("multiport", null, "false"));
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_TYPE)) {
            Boolean updateValue = (Boolean) updates.get(ColumnNames.COL_TYPE);

            if (updateValue.booleanValue()) {
                String type = (String) portInfo.get(ColumnNames.COL_TYPE);

                if (type.equals("")) {
                    momlUpdate.append(_momlDeleteProperty("_type"));
                } else {
                    momlUpdate.append(_momlProperty("_type",
                            "ptolemy.actor.TypeAttribute",
                            StringUtilities.escapeForXML(type)));
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_DIRECTION)) {
            Boolean updateValue = (Boolean) updates
                    .get(ColumnNames.COL_DIRECTION);

            if (updateValue.booleanValue()) {
                String direction = (String) portInfo
                        .get(ColumnNames.COL_DIRECTION);

                if (direction.equals("DEFAULT")) {
                    momlUpdate.append(_momlDeleteProperty("_cardinal"));
                } else {
                    momlUpdate.append(_momlProperty("_cardinal",
                            _STRING_ATTRIBUTE, direction));
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_SHOW_NAME)) {
            Boolean updateValue = (Boolean) updates
                    .get(ColumnNames.COL_SHOW_NAME);

            if (updateValue.booleanValue()) {
                if (((Boolean) portInfo.get(ColumnNames.COL_SHOW_NAME))
                        .booleanValue()) {
                    momlUpdate.append(_momlProperty("_showName",
                            _SINGLETON_PARAMETER, "true"));
                } else {
                    // NOTE: If there is already a property that is not
                    // a boolean-valued parameter, then remove it rather
                    // than setting it to false.  This is done for more
                    // robust backward compatibility.
                    boolean removed = false;
                    Port port = (Port) portInfo
                            .get(ColumnNames.COL_ACTUAL_PORT);

                    if (port != null) {
                        Attribute attribute = port.getAttribute("_showName");

                        if (!(attribute instanceof Parameter)) {
                            momlUpdate.append(_momlDeleteProperty("_showName"));
                            removed = true;
                        }
                    }

                    if (!removed) {
                        momlUpdate.append(_momlProperty("_showName",
                                _SINGLETON_PARAMETER, "false"));
                    }
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_HIDE)) {
            Boolean updateValue = (Boolean) updates.get(ColumnNames.COL_HIDE);

            if (updateValue.booleanValue()) {
                if (((Boolean) portInfo.get(ColumnNames.COL_HIDE))
                        .booleanValue()) {
                    momlUpdate.append(_momlProperty("_hide",
                            _SINGLETON_PARAMETER, "true"));
                } else {
                    // NOTE: If there is already a property that is not
                    // a boolean-valued parameter, then remove it rather
                    // than setting it to false.  This is done for more
                    // robust backward compatibility.
                    boolean removed = false;
                    Port port = (Port) portInfo
                            .get(ColumnNames.COL_ACTUAL_PORT);

                    if (port != null) {
                        Attribute attribute = port.getAttribute("_hide");

                        if (!(attribute instanceof Parameter)) {
                            momlUpdate.append(_momlDeleteProperty("_hide"));
                            removed = true;
                        }
                    }

                    if (!removed) {
                        momlUpdate.append(_momlProperty("_hide",
                                _SINGLETON_PARAMETER, "false"));
                    }
                }
            }
        }

        if (updates.containsKey(ColumnNames.COL_UNITS)) {
            Boolean updateValue = (Boolean) updates.get(ColumnNames.COL_UNITS);

            if (updateValue.booleanValue()) {
                momlUpdate.append(_momlProperty("_units", _UNIT_ATTRIBUTE,
                        (String) portInfo.get(ColumnNames.COL_UNITS)));
            }
        }

        momlUpdate.append("</port>");
        return momlUpdate.toString();
    }

    /** Create a JComboBox with the appropriate listeners. */
    private JComboBox _createComboBox() {
        JComboBox jComboBox = new JComboBox();

        // If the user types in the comboBox, enable Apply.
        jComboBox.getEditor().getEditorComponent()
        .addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent ke) {
                _setDirty(true);
                _enableApplyButton(true);
            }
        });
        jComboBox.getEditor().getEditorComponent()
        .addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent event) {
                // Set the selected row so the remove key gets updated
                _setSelectedRow(_portTable.getSelectionModel()
                        .getAnchorSelectionIndex());
            }

            @Override
            public void focusLost(FocusEvent event) {
            }
        });
        jComboBox.setEditable(true);

        // Add this item first so it is first on the list.
        jComboBox.addItem("");
        return jComboBox;
    }

    /** Generate a combo box based on the type names. */
    private JComboBox _createPortTypeComboBox() {
        JComboBox jComboBox = _createComboBox();

        //         // Add the types from data.expr.Constants
        //         TreeMap typeMap = Constants.types();
        //         Iterator types = typeMap.keySet().iterator();

        //         while (types.hasNext()) {
        //             String type = (String) (types.next());
        //             jComboBox.addItem(type);
        //         }

        // Get the types from the TypeLattice.
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5627
        Object[] types = ((DirectedAcyclicGraph) TypeLattice.basicLattice())
                .topologicalSort();
        List<String> typeList = new LinkedList<String>();
        for (Object type : types) {
            typeList.add(type.toString());
        }
        // Add some common types
        typeList.add("arrayType(int)");
        typeList.add("arrayType(int,5)");
        typeList.add("{x=double, y=double}");
        typeList.add("record");

        Collections.sort(typeList);

        for (String typeName : typeList) {
            jComboBox.addItem(typeName);
        }
        return jComboBox;
    }

    /** Generate a combo box based on the unit names. */
    private JComboBox _createPortUnitComboBox() {
        JComboBox jComboBox = _createComboBox();

        ArrayList unitsArrayList = ptolemy.data.unit.UnitUtilities
                .categoryList();
        Collections.sort(unitsArrayList);

        Iterator units = unitsArrayList.iterator();

        while (units.hasNext()) {
            String unit = (String) units.next();
            jComboBox.addItem(unit);
        }

        // Add these items last so they are at the bottom.
        jComboBox.addItem("meter second ^-1");
        return jComboBox;
    }

    private void _enableApplyButton(boolean e) {
        _applyButton.setEnabled(e);
    }

    // Initialize which columns will be visible for this target.
    private void _initColumnNames() {
        // Get the Entity for which we are configuring the ports.
        Entity target = getTarget();

        // Set up the column names that will be visible.
        String[] tempColumnNames = null;

        if (target instanceof TypedActor) {
            String[] temp = { ColumnNames.COL_NAME, ColumnNames.COL_INPUT,
                    ColumnNames.COL_OUTPUT, ColumnNames.COL_MULTIPORT,
                    ColumnNames.COL_TYPE, ColumnNames.COL_DIRECTION,
                    ColumnNames.COL_SHOW_NAME, ColumnNames.COL_HIDE,
                    ColumnNames.COL_UNITS, };
            tempColumnNames = temp;
        } else if (target instanceof Actor) {
            String[] temp = { ColumnNames.COL_NAME, ColumnNames.COL_INPUT,
                    ColumnNames.COL_OUTPUT, ColumnNames.COL_MULTIPORT,
                    ColumnNames.COL_DIRECTION, ColumnNames.COL_SHOW_NAME,
                    ColumnNames.COL_HIDE, };
            tempColumnNames = temp;
        } else {
            String[] temp = { ColumnNames.COL_NAME, ColumnNames.COL_DIRECTION,
                    ColumnNames.COL_SHOW_NAME, ColumnNames.COL_HIDE, };
            tempColumnNames = temp;
        }

        // Store the column names as an ArrayList.
        List columnList = Arrays.asList(tempColumnNames);
        _columnNames = new ArrayList(columnList);
    }

    // Initialize the displayed column widths.
    private void _initColumnSizes() {
        TableColumn column = null;

        if (_columnNames.contains(ColumnNames.COL_INPUT)) {
            int index = _columnNames.indexOf(ColumnNames.COL_INPUT);
            column = _portTable.getColumnModel().getColumn(index);
            column.setPreferredWidth(30);
        }

        if (_columnNames.contains(ColumnNames.COL_OUTPUT)) {
            int index = _columnNames.indexOf(ColumnNames.COL_OUTPUT);
            column = _portTable.getColumnModel().getColumn(index);
            column.setPreferredWidth(30);
        }

        if (_columnNames.contains(ColumnNames.COL_MULTIPORT)) {
            int index = _columnNames.indexOf(ColumnNames.COL_MULTIPORT);
            column = _portTable.getColumnModel().getColumn(index);
            column.setPreferredWidth(40);
        }

        if (_columnNames.contains(ColumnNames.COL_TYPE)) {
            int index = _columnNames.indexOf(ColumnNames.COL_TYPE);
            column = _portTable.getColumnModel().getColumn(index);
            // Set the preferred with of the type column to 100.
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5545
            // "The default horizontal space is too short to show a number of the default
            // options."
            column.setPreferredWidth(100);
            // Increase the row height.
            // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5545
            // "On OS X, when choosing or typing in a Type, there isn't enough vertical space
            // and characters can be hard to read."
            // If you change the height, then check that a few rows can be added,
            // see the _portTable.setPreferredScrollableViewportSize(new Dimension(... call above
            _portTable
            .setRowHeight((int) Math.round(_portTable.getRowHeight() * 1.20));
        }

        if (_columnNames.contains(ColumnNames.COL_DIRECTION)) {
            int index = _columnNames.indexOf(ColumnNames.COL_DIRECTION);
            column = _portTable.getColumnModel().getColumn(index);
            column.setPreferredWidth(50);
        }

        if (_columnNames.contains(ColumnNames.COL_SHOW_NAME)) {
            int index = _columnNames.indexOf(ColumnNames.COL_SHOW_NAME);
            column = _portTable.getColumnModel().getColumn(index);
            column.setPreferredWidth(70);
        }

        if (_columnNames.contains(ColumnNames.COL_HIDE)) {
            int index = _columnNames.indexOf(ColumnNames.COL_HIDE);
            column = _portTable.getColumnModel().getColumn(index);
            column.setPreferredWidth(30);
        }
    }

    /** Return true if the property of the specified name is set for
     *  the specified object. A property is specified if the specified
     *  object contains an attribute with the specified name and that
     *  attribute is either not a boolean-valued parameter, or it is a
     *  boolean-valued parameter with value true.
     *  @param object The object.
     *  @param name The property name.
     *  @return True if the property is set.
     */
    private boolean _isPropertySet(NamedObj object, String name) {
        Attribute attribute = object.getAttribute(name);

        if (attribute == null) {
            return false;
        }

        if (attribute instanceof Parameter) {
            try {
                Token token = ((Parameter) attribute).getToken();

                if (token instanceof BooleanToken) {
                    if (!((BooleanToken) token).booleanValue()) {
                        return false;
                    }
                }
            } catch (IllegalActionException e) {
                // Ignore, using default of true.
            }
        }

        return true;
    }

    private String _momlDeleteProperty(String name) {
        return "<deleteProperty name=\"" + name + "\"/>";
    }

    private String _momlProperty(String name) {
        return "<property name=\"" + name + "\"/>";
    }

    private String _momlProperty(String name, String clz, String value) {
        if (clz != null) {
            return "<property name=\"" + name + "\" " + "class = \"" + clz
                    + "\" " + "value = \"" + value + "\"/>";
        }

        return "<property name=\"" + name + "\" " + "value = \"" + value
                + "\"/>";
    }

    private void _populateActualPorts() {
        for (int i = 0; i < _ports.size(); i++) {
            Hashtable portInfo = (Hashtable) _ports.elementAt(i);
            String portName = (String) portInfo.get(ColumnNames.COL_NAME);
            Iterator portIterator = getTarget().portList().iterator();

            Port actualPort;
            boolean foundActualPort = false;

            while (portIterator.hasNext()) {
                Object candidate = portIterator.next();

                if (candidate instanceof Port) {
                    actualPort = (Port) candidate;

                    if (actualPort.getName().equals(portName)) {
                        portInfo.put(ColumnNames.COL_ACTUAL_PORT, actualPort);
                        foundActualPort = true;
                        break;
                    }
                }
            }

            if (!foundActualPort) {
                Exception exception = new InternalErrorException(
                        "Port \""
                                + portName
                                + "\"stored in _ports "
                                + "not found in \""
                                + getTarget().getFullName()
                                + "\". "
                                + "This can occur when two port names are being swapped. "
                                + "The workaround when swapping A and B is to first set A to C, then C to A, then C to B. "
                                + "See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4478");
                MessageHandler.error("Failed to find \"" + portName + "\",",
                        exception);
            }
        }
    }

    private void _setSelectedRow(int row) {
        _selectedRow = row;

        if (row < 0) {
            _removeButton.setText("Remove");
            _removeButton.setEnabled(false);
        } else {
            Hashtable portInfo = (Hashtable) _ports.elementAt(row);
            String portName = (String) portInfo.get(ColumnNames.COL_NAME);

            // FIXME this depends on button name string length.
            if (portName.length() == 0) {
                portName = "#" + (row + 1);
            }

            if (portName.length() < 10) {
                portName += "          ";
                portName = portName.substring(0, 9);
            } else if (portName.length() > 10) {
                portName = portName.substring(0, 7) + "...";
            }

            _removeButton.setText("Remove " + portName);

            // Some ports cannot be removed.
            _removeButton.setEnabled(_portTable.isCellEditable(row, 0));
        }
    }

    /** Creates and sets the TableModel. Also arranges for some columns
     * to have their particular renderers and/or editors. This method
     * will be invoked when the dialog is created, and every time a
     * change request from above causes the table to change.
     */
    private void _setupTableModel() {
        _portTableModel = new PortTableModel(getTarget().portList());
        _portTable.setModel(_portTableModel);
        _portTable.setDefaultRenderer(Boolean.class,
                new PortBooleanCellRenderer());
        _portTable.setDefaultRenderer(String.class, new StringCellRenderer());
        _portTable.setDefaultEditor(String.class,
                new ValidatingJTextFieldCellEditor());
        _enableApplyButton(false);

        if (_columnNames.contains(ColumnNames.COL_NAME)) {
            int col = _columnNames.indexOf(ColumnNames.COL_NAME);
            TableColumn _portNameColumn = _portTable.getColumnModel()
                    .getColumn(col);
            final ValidatingJTextFieldCellEditor portNameEditor = new ValidatingJTextFieldCellEditor(
                    new JFormattedTextField());
            _portNameColumn.setCellEditor(portNameEditor);
            portNameEditor.setValidator(new CellValidator() {
                /////////////////////////////////////////
                //////////// inner class/////////////////
                @Override
                public boolean isValid(String cellValue) {
                    int index = cellValue.indexOf(".");

                    if (index >= 0) {
                        setMessage(cellValue + " contains a period in col "
                                + (index + 1));
                        return false;
                    }

                    if (cellValue.equals("")) {
                        setMessage("Ports cannot have the empty string "
                                + "as a name.");
                        return false;
                    }

                    return true;
                }
            });
        }

        if (_columnNames.contains(ColumnNames.COL_DIRECTION)) {
            int col = _columnNames.indexOf(ColumnNames.COL_DIRECTION);
            TableColumn _portLocationColumn = _portTable.getColumnModel()
                    .getColumn(col);
            _portLocationColumn.setCellEditor(new DefaultCellEditor(
                    _portLocationComboBox));
        }

        if (_columnNames.contains(ColumnNames.COL_TYPE)) {
            int col = _columnNames.indexOf(ColumnNames.COL_TYPE);
            TableColumn _portTypeColumn = _portTable.getColumnModel()
                    .getColumn(col);

            final ValidatingComboBoxCellEditor portTypeEditor = new ValidatingComboBoxCellEditor(
                    _createPortTypeComboBox());

            _portTypeColumn.setCellEditor(portTypeEditor);
            portTypeEditor.setValidator(new CellValidator() {
                /////////////////////////////////////////
                //////////// inner class/////////////////
                @Override
                public boolean isValid(String cellValue) {
                    try {
                        if (cellValue.equals("")) {
                            return true;
                        } else if (cellValue.equals("pointer")) {
                            return true;
                        }

                        ASTPtRootNode tree = _typeParser
                                .generateParseTree(cellValue);
                        /* Token result = */_parseTreeEvaluator
                        .evaluateParseTree(tree, null);
                    } catch (IllegalActionException e) {
                        setMessage(e.getMessage());
                        return false;
                    }

                    return true;
                }
            });
        }

        if (_columnNames.contains(ColumnNames.COL_UNITS)) {
            int col = _columnNames.indexOf(ColumnNames.COL_UNITS);
            TableColumn _portUnitColumn = _portTable.getColumnModel()
                    .getColumn(col);
            final ValidatingComboBoxCellEditor portUnitEditor = new ValidatingComboBoxCellEditor(
                    _createPortUnitComboBox());
            _portUnitColumn.setCellEditor(portUnitEditor);

            portUnitEditor.setValidator(new CellValidator() {
                /////////////////////////////////////////
                //////////// inner class/////////////////
                @Override
                public boolean isValid(String cellValue) {
                    try {
                        UnitLibrary.getParser().parseUnitExpr(cellValue);
                    } catch (ParseException e) {
                        setMessage(e.getMessage());
                        return false;
                    }

                    return true;
                }
            });
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** List of names of columns that will be used for this target. */
    private ArrayList _columnNames;

    /** When you click on the "Hide" column header, toggle this value.
     * @see ptolemy.actor.gui.PortConfigurerDialog.PortTableModel#toggleHidePorts()
     */
    private boolean _hideAllPorts = false;

    /** The combination box used to select the location of a port. */
    private JComboBox _portLocationComboBox;

    JTable _portTable;

    PortTableModel _portTableModel = null;

    /** JTableHeader of _portTable.  MouseListener is added to this. */
    JTableHeader _jth;

    static ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();

    Vector _ports = null;

    private int _selectedRow = -1;

    private static String _SINGLETON_PARAMETER = "ptolemy.data.expr.SingletonParameter";

    /** When you click on the "Show Name" column header, toggle this value.
     * @see ptolemy.actor.gui.PortConfigurerDialog.PortTableModel#toggleShowAllNames()
     */
    private boolean _showAllNames = false;

    private static String _STRING_ATTRIBUTE = "ptolemy.kernel.util.StringAttribute";

    static PtParser _typeParser = new PtParser();

    private static String _UNIT_ATTRIBUTE = "ptolemy.data.unit.UnitAttribute";

    /** The various buttons. */
    private JButton _applyButton;

    /** The various buttons. */
    private JButton Button;

    /** The various buttons. */
    private JButton _addButton;

    /** True if the change request in _apply() failed */
    private boolean _applyChangeRequestFailed;

    /** The various buttons. */
    private JButton _removeButton;

    /** Strings that are available for the column names. */
    private static class ColumnNames {
        public final static String COL_NAME = "Name";

        public final static String COL_INPUT = "Input";

        public final static String COL_OUTPUT = "Output";

        public final static String COL_MULTIPORT = "Multiport";

        public final static String COL_TYPE = "Type";

        public final static String COL_DIRECTION = "Direction";

        public final static String COL_SHOW_NAME = "Show Name";

        public final static String COL_HIDE = "Hide";

        public final static String COL_UNITS = "Units";

        public final static String COL_ACTUAL_PORT = "9";
    }
}

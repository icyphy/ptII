/* User interface to the backtracking controller.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

package ptolemy.backtrack.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// BacktrackControllerFrame
/**
 User interface to the backtracking controller. This tableau is a dialog that
 allows the user to create checkpoints for individual models opened in
 Ptolemy, and also to roll back the states of those models.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackControllerFrame extends TableauFrame {

    /** Construct a backtrack controller frame with a model selector, a list
     *  of checkpoint handles and three buttons: "checkpoint", "rollback"
     *  and "commit".
     */
    public BacktrackControllerFrame() {
        super(null, null);

        setTitle(NAME);

        Container container = getContentPane();

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        container.add(topPanel, BorderLayout.NORTH);

        // The model selector.
        _modelSelector = new JComboBox();
        topPanel.add(_modelSelector, BorderLayout.CENTER);

        // The "refresh" button.
        _refresh = new JButton("refresh");
        _refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshModelSelector();
            }
        });
        topPanel.add(_refresh, BorderLayout.EAST);

        // The list of checkpoint handles.
        _handleTable = new JTable(_handleTableModel) {
            public void changeSelection(int rowIndex, int columnIndex,
                    boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                _rollbackButton.setEnabled(true);
                _commitButton.setEnabled(true);
            }
        };
        _handleTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        _handleTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        _handleTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        container.add(new JScrollPane(_handleTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());
        container.add(bottomPanel, BorderLayout.SOUTH);

        // The "checkpoint" button.
        _checkpointButton = new JButton("checkpoint");
        _checkpointButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createCheckpoint();
            }
        });
        bottomPanel.add(_checkpointButton);

        // The "rollback" button.
        _rollbackButton = new JButton("rollback");
        _rollbackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rollback();
            }
        });
        _rollbackButton.setEnabled(false);
        bottomPanel.add(_rollbackButton);

        // The "commit" button.
        _commitButton = new JButton("commit");
        _commitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                commit();
            }
        });
        _commitButton.setEnabled(false);
        bottomPanel.add(_commitButton);

        pack();
        setVisible(true);
    }

    /** Commit the selected checkpoint handle.
     */
    public synchronized void commit() {
        int selectedRow = _handleTable.getSelectedRow();
        Long handle = (Long) _handleTableModel.getValueAt(selectedRow, 0);
        _controller.commit(handle.longValue());
        for (int i = 0; i <= selectedRow; i++) {
            _handleTableModel.removeElement(0);
        }
        if (_handleTable.getSelectedRow() < 0) {
            _commitButton.setEnabled(false);
            _rollbackButton.setEnabled(false);
        }
    }

    /** Create a checkpoint, and add its handle to the list of checkpoint
     *  handles.
     */
    public synchronized void createCheckpoint() {
        ModelSelectorElement selectedModel = (ModelSelectorElement) _modelSelector
                .getSelectedItem();
        if (selectedModel != null) {
            NamedObj model = selectedModel.getFrame().getModel();
            if (model instanceof CompositeActor) {
                long handle = _controller
                        .createCheckpoint((CompositeActor) model);
                _handleTableModel.insertElement(new HandleTableElement(handle,
                        new Date()));
            }
        }
    }

    /** Look up the opened Ptolemy models, and add their names to the model
     *  selector.
     */
    public synchronized void refreshModelSelector() {
        try {
            Frame[] frames = getFrames();
            _modelSelector.removeAllItems();
            for (int i = 0; i < frames.length; i++) {
                Frame frame = frames[i];
                if (frame.isVisible() && frame instanceof PtolemyFrame) {
                    PtolemyFrame ptolemyFrame = (PtolemyFrame) frame;
                    NamedObj model = ptolemyFrame.getModel();
                    if (model instanceof CompositeActor) {
                        _modelSelector.addItem(new ModelSelectorElement(
                                ptolemyFrame));
                    }
                }
            }
        } catch (Exception e) {
            MessageHandler.error("Unable to refresh model selector.", e);
        }
    }

    /** Roll back the selected checkpoint handle, and delete the records
     *  for that checkpoint and for all older checkpoints.
     */
    public synchronized void rollback() {
        int selectedRow = _handleTable.getSelectedRow();
        Long handle = (Long) _handleTableModel.getValueAt(selectedRow, 0);
        _controller.rollback(handle.longValue(), true);
        while (_handleTableModel.getRowCount() > selectedRow) {
            _handleTableModel.removeElement(selectedRow);
        }
        if (_handleTable.getSelectedRow() < 0) {
            _commitButton.setEnabled(false);
            _rollbackButton.setEnabled(false);
        }
    }

    /** The title of this tableau.
     */
    public static final String NAME = "Backtrack Controller";

    /** The "checkpoint" button.
     */
    private JButton _checkpointButton;

    /** The "commit" button.
     */
    private JButton _commitButton;

    /** The backtracking controller to be used to create checkpoints, roll
     *  back checkpoints, or commit checkpoints.
     */
    private BacktrackController _controller = new BacktrackController();

    /** The list of checkpoint handles.
     */
    private JTable _handleTable;

    /** Data for the list of checkpoint handles.
     */
    private HandleTableModel _handleTableModel = new HandleTableModel();

    /** The model selector.
     */
    private JComboBox _modelSelector;

    /** The "refresh" button.
     */
    private JButton _refresh;

    /** The "rollback" button.
     */
    private JButton _rollbackButton;

    ///////////////////////////////////////////////////////////////////
    //// HandleTableElement

    /**
     Element of the checkpoint handle table.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class HandleTableElement {

        /** Return the checkpoint handle.
         *
         *  @return The checkpoint handle.
         */
        public long getHandle() {
            return _handle;
        }

        /** Return the time at which the checkpoint was created.
         *
         *  @return The checkpoint creation time.
         */
        public Date getTime() {
            return _time;
        }

        /** Construct an element in the checkpoint handle list.
         *
         *  @param handle The checkpoint handle.
         *  @param time The time at which the checkpoint was created.
         */
        HandleTableElement(long handle, Date time) {
            _handle = handle;
            _time = time;
        }

        private long _handle;

        private Date _time;
    }

    ///////////////////////////////////////////////////////////////////
    //// HandleTableModel

    /**
     The data store for the list of checkpoint handles.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class HandleTableModel extends AbstractTableModel {

        /** Return the number of columns in the list.
         *
         *  @return The number of columns.
         */
        public int getColumnCount() {
            return _columnNames.length;
        }

        /** Return the name of the specified column.
         *
         *  @return The column name.
         */
        public String getColumnName(int column) {
            return _columnNames[column];
        }

        /** Return the number of rows in the list.
         *
         *  @return The number of rows.
         */
        public int getRowCount() {
            return _data.size();
        }

        /** Return the object that represents the value at the given row and the
         *  given column. Java will call toString() of the returned object to
         *  get it textual representation to be shown in the cell.
         *
         *  @return The value of the given cell.
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            HandleTableElement element = _data.get(rowIndex);
            switch (columnIndex) {
            case 0:
                return Long.valueOf(element.getHandle());
            case 1:
                return null; // TODO: We cannot count element number.
            case 2:
                return element.getTime();
            default:
                return null;
            }
        }

        /** Insert a element to the end of the data store. One more row will be
         *  added to the checkpoint handle list.
         *
         *  @param element The element to be added.
         */
        public synchronized void insertElement(HandleTableElement element) {
            _data.add(element);
            fireTableRowsInserted(_data.size() - 1, _data.size() - 1);
        }

        /** Remove the element at the given position from the data store.
         *
         *  @param position The position of the element.
         */
        public synchronized void removeElement(int position) {
            _data.remove(position);
            this.fireTableRowsDeleted(position, position);
        }

        /** Names of the columns.
         */
        private String[] _columnNames = new String[] { "Handle", "Objects",
                "Time Created" };

        /** The list of elements.
         */
        private List<HandleTableElement> _data = new LinkedList<HandleTableElement>();
    }

    ///////////////////////////////////////////////////////////////////
    //// ModelSelectorElement

    /**
     Elements of the model selector. It is a wrapper around PtolemyFrame.
     In the model selector (a JTable), the item names are retrieved with
     toString(), so this class returns the PtolemyFrame's title in
     toString().

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class ModelSelectorElement {

        /** Get the frame of the Ptolemy model.
         *
         *  @return The frame of the Ptolemy model.
         */
        public PtolemyFrame getFrame() {
            return _frame;
        }

        /** Return the title of the Ptolemy frame.
         *
         *  @return The title of the Ptolemy frame.
         */
        public String toString() {
            return _frame.getTitle();
        }

        /** Construct a model selector element.
         *
         *  @param frame The frame of the Ptolemy model.
         */
        ModelSelectorElement(PtolemyFrame frame) {
            _frame = frame;
        }

        /** The frame.
         */
        private PtolemyFrame _frame;
    }
}

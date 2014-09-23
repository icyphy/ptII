/* The frame for configuring an SCR Model.

 Copyright (c) 2000-2014 The Regents of the University of California.
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

package ptolemy.vergil.scr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

/**
The frame for configuring an SCR Model.

@author Patricia Derler
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (pd)
@Pt.AcceptedRating Red (pd)
 */
@SuppressWarnings("serial")
public class SCRTableFrame extends PtolemyFrame {

    /** Construct a frame associated with an SCR Model.
     * @param model The model to put in this frame, or null if none.
     */
    public SCRTableFrame(NamedObj model) {
        this(model, null);
        if (model instanceof FSMActor) {
            _model = (FSMActor) model;
        } else {
            MessageHandler.error("Cannot initialize SCRTableFrame with a "
                    + model.getClassName());
        }
        _init();
    }

    /** Construct a frame associated with an SCR Model.
     * @param model The model to put in this frame, or null if none.
     * @param tableau The tableau responsible for this frame, or null if none.
     */
    public SCRTableFrame(NamedObj model, Tableau tableau) {
        super(tableau);
        if (model instanceof FSMActor) {
            _model = (FSMActor) model;
        } else {
            MessageHandler.error("Cannot initialize SCRTableFrame with a "
                    + model.getClassName());
        }
        _init();
    }

    private void _addParameterTable(Parameter parameter,
            JTabbedPane eventTablesPanel) {
        final JPanel tabPanel1 = new JPanel(new BorderLayout());
        final EventTableModel tableModel = new EventTableModel(parameter,
                _model);
        final JTable table = new JTable(tableModel);
        table.setColumnSelectionAllowed(true);
        _setCellRenderer(table);
        table.setGridColor(Color.black);
        JButton addColumn = new JButton("Add Column");
        addColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.addColumn();
                _setCellRenderer(table);
            }
        });

        JButton deleteColumn = new JButton("Delete Column");
        deleteColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.deleteColumn(table.getSelectedColumn());
                _setCellRenderer(table);
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tableModel.saveModel();
                } catch (IllegalActionException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                } catch (NameDuplicationException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                }
            }
        });

        JButton checkDButton = new JButton("Check Disjointness");
        checkDButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tableModel.checkDisjointness();
                } catch (IllegalActionException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                }
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(addColumn);
        buttons.add(deleteColumn);
        buttons.add(checkDButton);
        buttons.add(saveButton);

        tabPanel1.add(table, BorderLayout.CENTER);
        tabPanel1.add(buttons, BorderLayout.SOUTH);
        eventTablesPanel.addTab(parameter.getName(), tabPanel1);
    }

    private JComponent _getModeTransitionPanel() {
        JPanel modeTransitionTablePanel = new JPanel(new BorderLayout());

        final ModeTransitionTableModel tableModel = new ModeTransitionTableModel(
                _model);

        final JTable table = new JTable(tableModel);
        table.setGridColor(Color.black);

        JButton addRowButton = new JButton("Add Row");
        addRowButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.addRow();

            }
        });

        JButton deleteRowButton = new JButton("Delete Row");
        deleteRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRow() != -1) {
                    tableModel.deleteRow(table.getSelectedRow());
                }
            }
        });

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    tableModel.saveModel();
                } catch (IllegalActionException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                } catch (NameDuplicationException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                }
            }
        });

        final JComboBox box = new JComboBox();
        box.setPrototypeDisplayValue("text here");
        box.setModel(new ComboBoxModel() {

            List<String> _states;
            State _initialState;

            @Override
            public void addListDataListener(ListDataListener arg0) {

            }

            @Override
            public Object getElementAt(int arg0) {
                if (_states == null) {
                    _initializeStates();
                }
                if (arg0 >= 0 && _states.size() > arg0) {
                    return _states.get(arg0);
                } else {
                    return "";
                }
            }

            private void _initializeStates() {
                _states = new ArrayList<String>();
                for (Object o : _model.entityList()) {
                    if (o instanceof State) {
                        State state = (State) o;
                        _states.add(state.getName());
                        try {
                            if (((BooleanToken) state.isInitialState.getToken())
                                    .booleanValue()) {
                                _initialState = state;
                            }
                        } catch (IllegalActionException e) {
                            MessageHandler.error(
                                    "Error retrieving initial state", e);
                        }
                    }
                }
            }

            @Override
            public int getSize() {
                if (_states == null) {
                    _initializeStates();
                }
                return _states.size();
            }

            @Override
            public void removeListDataListener(ListDataListener arg0) {
            }

            @Override
            public Object getSelectedItem() {
                if (_states == null) {
                    _initializeStates();
                }
                if (_initialState != null) {
                    return _initialState.getName();
                } else {
                    return null;
                }
            }

            @Override
            public void setSelectedItem(Object arg0) {
                if (_states == null) {
                    _initializeStates();
                }
                String stateName = (String) arg0;
                State state = (State) _model.getEntity(stateName);
                try {
                    _initialState = state;
                    state.isInitialState.setToken("true");
                } catch (IllegalActionException e1) {
                    MessageHandler.error("Could not set initial state", e1);
                }
            }

        });
        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (box.getSelectedItem() instanceof State) {
                    State state = (State) box.getSelectedItem();
                    try {
                        state.isInitialState.setToken("true");
                    } catch (IllegalActionException e1) {
                        MessageHandler.error("Could not set initial state", e1);
                    }
                }
            }
        });

        modeTransitionTablePanel.add(table.getTableHeader(),
                BorderLayout.PAGE_START);
        modeTransitionTablePanel.add(table, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.add(addRowButton);
        buttons.add(deleteRowButton);
        buttons.add(new JLabel("Initial State:"));
        buttons.add(box);
        buttons.add(saveButton);

        modeTransitionTablePanel.add(buttons, BorderLayout.SOUTH);

        return modeTransitionTablePanel;
    }

    private JComponent _getEventTablePanel() {
        final JTabbedPane eventTablesPanel = new JTabbedPane();
        Parameter parameter = null;
        for (Object entity : _model.attributeList()) {
            if (entity instanceof Parameter) {
                parameter = (Parameter) entity;
                if (parameter.getName().startsWith("scr_")) {
                    _addParameterTable(parameter, eventTablesPanel);
                }
            }
        }

        JButton addParameter = new JButton("Add Parameter");
        addParameter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JDialog inputbox = new JDialog(SCRTableFrame.this,
                        "New Parameter");
                inputbox.getContentPane().setLayout(new FlowLayout());

                JLabel lblname = new JLabel("Parameter Name:");
                final JTextField parameterNameTxt = new JTextField("");
                parameterNameTxt.setColumns(10);
                JLabel lblvalue = new JLabel("Parameter Value:");
                final JTextField parameterValueTxt = new JTextField("");
                parameterValueTxt.setColumns(10);

                JButton ok = new JButton("Ok");

                inputbox.getContentPane().add(lblname);
                inputbox.getContentPane().add(parameterNameTxt);
                inputbox.getContentPane().add(lblvalue);
                inputbox.getContentPane().add(parameterValueTxt);

                Parameter parameter = null;
                try {
                    parameter = new Parameter(_model, parameterNameTxt
                            .getText());
                } catch (IllegalActionException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                } catch (NameDuplicationException e1) {
                    MessageHandler.error(e1.getMessage(), e1);
                }
                final Parameter param = parameter;

                ok.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        inputbox.setVisible(false);
                        try {
                            param.setName(parameterNameTxt.getText());
                            param.setToken(parameterValueTxt.getText());
                            _addParameterTable(param, eventTablesPanel);
                        } catch (IllegalActionException e1) {
                            MessageHandler.error(e1.getMessage(), e1);
                        } catch (NameDuplicationException e1) {
                            MessageHandler.error(e1.getMessage(), e1);
                        }
                    }
                });

                inputbox.getContentPane().add(ok);
                final Toolkit toolkit = Toolkit.getDefaultToolkit();
                final Dimension screenSize = toolkit.getScreenSize();
                final int x = (screenSize.width - inputbox.getWidth()) / 2;
                final int y = (screenSize.height - inputbox.getHeight()) / 2;
                inputbox.setLocation(x, y);
                inputbox.setSize(250, 150);
                inputbox.setVisible(true);
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(addParameter);

        JPanel eventTablePanel = new JPanel(new BorderLayout());
        eventTablePanel.add(eventTablesPanel, BorderLayout.CENTER);
        eventTablePanel.add(buttons, BorderLayout.SOUTH);
        return eventTablePanel;
    }

    private JComponent _getConditionTablePanel() {
        JTabbedPane eventTablesPanel = new JTabbedPane();

        for (Object entity : _model.outputPortList()) {

            final IOPort outputPort = (IOPort) entity;

            final ConditionsTableModel tableModel = new ConditionsTableModel(
                    outputPort, _model);

            final JTable table = new JTable(tableModel);
            _setCellRenderer(table);
            table.setGridColor(Color.black);

            JButton addColumn = new JButton("Add Column");
            addColumn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    tableModel.addColumn();
                    _setCellRenderer(table);
                }
            });

            JButton deleteColumn = new JButton("Delete Column");
            deleteColumn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tableModel.deleteColumn(table.getSelectedColumn());
                    _setCellRenderer(table);
                }
            });

            JButton checkDButton = new JButton("Check Disjointness");
            checkDButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        tableModel.checkDisjointness();
                    } catch (IllegalActionException e1) {
                        MessageHandler.error(e1.getMessage(), e1);
                    }
                }
            });

            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        tableModel.saveModel();
                    } catch (NameDuplicationException e1) {
                        MessageHandler.error(e1.getMessage(), e1);
                    }
                }
            });

            JPanel buttons = new JPanel();
            buttons.add(addColumn);
            buttons.add(deleteColumn);
            buttons.add(checkDButton);
            buttons.add(saveButton);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(table, BorderLayout.CENTER);
            panel.add(buttons, BorderLayout.SOUTH);

            eventTablesPanel.addTab(((IOPort) entity).getName(), panel);
        }

        return eventTablesPanel;
    }

    private void _init() {
        JTabbedPane pane = new JTabbedPane();

        pane.addTab("Mode Transition Table", _getModeTransitionPanel());
        pane.addTab("Event Table", _getEventTablePanel());
        pane.addTab("Condition Table", _getConditionTablePanel());

        getContentPane().add(pane, BorderLayout.CENTER);
        this.setSize(new Dimension(700, 400));

        super.show();
    }

    private void _setCellRenderer(JTable table) {
        Enumeration<TableColumn> en = table.getColumnModel().getColumns();
        while (en.hasMoreElements()) {
            TableColumn tc = en.nextElement();
            tc.setCellRenderer(new SCRTableCellRenderer());
        }
        ;
    }

    private FSMActor _model;

    /**
    The cell renderer for SCR event and condition tables.
    @author Patricia Derler
    @version $Id$
    @since Ptolemy II 10.0
    @Pt.ProposedRating Red (pd)
    @Pt.AcceptedRating Red (pd)
     */
    private static class SCRTableCellRenderer extends DefaultTableCellRenderer
    implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            setBackground(null);
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            setText(String.valueOf(value));
            if (row + 1 == table.getRowCount()) {
                setBackground(Color.lightGray);
            }
            return this;
        }

    }

}

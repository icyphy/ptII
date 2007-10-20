/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ptolemy.actor.gt.CompositeActorMatcher;
import ptolemy.actor.gt.SingleRuleTransformer;
import ptolemy.actor.gt.data.Pair;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.GraphicalMessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.EditorDropTarget;
import ptolemy.vergil.basic.ExtendedGraphFrame;
import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.JGraph;

public abstract class AbstractGTFrame extends ExtendedGraphFrame
implements ActionListener, ChangeListener, KeyListener, TableModelListener, ValueListener {

    /** Construct a frame associated with the specified Ptolemy II model.
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
    public AbstractGTFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /** Construct a frame associated with the specified Ptolemy II model.
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
    public AbstractGTFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);

        ((SingleRuleTransformer) entity)._correspondenceAttribute
                .addValueListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("add")) {
            int number = _correspondenceTableModel.getRowCount() + 1;
            _correspondenceTableModel.addRow(new Object[] {
                    _createCellPanel(Integer.toString(number)),
                    _createCellPanel(""), _createCellPanel("")
            });
        } else if (command.equals("remove")) {
            if (_cellEditor != null) {
                _cellEditor.stopCellEditing();
            }
            int[] rows = _correspondenceTable.getSelectedRows();
            int size = _correspondenceTableModel.getRowCount();
            for (int i = 0, deleted = 0; i < size; i++) {
                if (deleted < rows.length && i == rows[deleted]) {
                    _correspondenceTableModel.removeRow(i - deleted);
                    deleted++;
                } else {
                    _correspondenceTableModel.setValueAt(i - deleted + 1,
                            i - deleted, 0);
                }
            }
        }
    }

    public void cancelFullScreen() {
        _screen.dispose();

        // Put the component back into the original window.
        _splitPane.setRightComponent(_getRightComponent());
        _tabbedPane.add(_fullScreenJGraph, _selectedIndex);
        // -1 because the index was automatically increased by 1 when the tab
        // was deleted last time.
        _tabbedPane.setSelectedIndex(_selectedIndex - 1);

        // Restore association with the graph panner.
        _graphPanner.setCanvas(_fullScreenJGraph);
        pack();
        show();
        GraphicalMessageHandler.setContext(_previousDefaultContext);
        toFront();
        _getRightComponent().requestFocus();
    }

    public void fullScreen() {
        if (_selectedIndex != 2) {
            _screen = new JDialog();
            _screen.getContentPane().setLayout(new BorderLayout());

            // Set to full-screen size.
            Toolkit toolkit = _screen.getToolkit();
            int width = toolkit.getScreenSize().width;
            int height = toolkit.getScreenSize().height;
            _screen.setSize(width, height);

            _fullScreenJGraph = getJGraph();
            _screen.setUndecorated(true);
            _screen.getContentPane().add(_fullScreenJGraph, BorderLayout.CENTER);

            // NOTE: Have to avoid the following, which forces the
            // dialog to resize the preferred size of _jgraph, which
            // nullifies the call to setSize() above.
            // _screen.pack();
            _screen.setVisible(true);

            // Make the new screen the default context for modal messages.
            Component _previousDefaultContext = GraphicalMessageHandler.getContext();
            GraphicalMessageHandler.setContext(_screen);

            // NOTE: As usual with swing, what the UI does is pretty
            // random, and doesn't correlate much with the documentation.
            // The following two lines do not work if _screen is a
            // JWindow instead of a JDialog.  There is no apparent
            // reason for this, but this is why we use JDialog.
            // Unfortunately, apparently the JDialog does not appear
            // in the Windows task bar.
            _screen.toFront();
            _fullScreenJGraph.requestFocus();

            _screen.setResizable(false);

            _fullScreenJGraph.addKeyListener(this);

            // Remove association with the graph panner.
            _graphPanner.setCanvas(null);

            setVisible(false);
            GraphicalMessageHandler.setContext(_previousDefaultContext);
        }
    }

    /** Return the JGraph instance that this view uses to represent the
     *  ptolemy model.
     *  @return the JGraph.
     *  @see #setJGraph(JGraph)
     */
    public JGraph getJGraph() {
        if (hasTabs()) {
            Component selected = _tabbedPane.getSelectedComponent();
            if (selected instanceof JGraph) {
                return (JGraph) selected;
            } else {
                return null;
            }
        } else {
            return super.getJGraph();
        }
    }

    public int getSelectedIndex() {
        return _selectedIndex;
    }

    public boolean hasTabs() {
        return _tabbedPane != null;
    }

    public void keyPressed(KeyEvent e) {
        if (e.getSource() != _fullScreenJGraph) {
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode()
                == (KeyEvent.VK_ALT | KeyEvent.VK_S)) {
            e.consume();
            cancelFullScreen();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void redo() {
        if (_cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        super.redo();
    }

    /** React to a change in the state of the tabbed pane.
     *  @param event The event.
     */
    public void stateChanged(ChangeEvent event) {
        if (event.getSource() == _tabbedPane) {
            _selectedIndex = _tabbedPane.getSelectedIndex();
            Component selected = _tabbedPane.getSelectedComponent();
            if (selected instanceof JGraph) {
                setJGraph((JGraph) selected);
                if (_graphPanner != null) {
                    _graphPanner.setCanvas((JGraph) selected);
                }
            } else {
                if (_graphPanner != null) {
                    _graphPanner.setCanvas(null);
                }
            }
        }
    }

    public void tableChanged(TableModelEvent e) {
        _commitCorrespondenceChange();
    }

    public void undo() {
        if (_cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        super.undo();
    }

    public void valueChanged(Settable settable) {
        SingleRuleTransformer transformer = (SingleRuleTransformer) getModel();
        if (settable == transformer._correspondenceAttribute) {
            _updateCorrespondenceTable();
        }
    }

    public void zoom(double factor) {
        if (_selectedIndex != 2) {
            super.zoom(factor);
        }
    }

    public void zoomFit() {
        if (_selectedIndex != 2) {
            super.zoomFit();
        }
    }

    public void zoomReset() {
        if (_selectedIndex != 2) {
            super.zoomReset();
        }
    }

    protected abstract ActorEditorGraphController _createController();

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = _createController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // The cast is safe because the constructor only accepts
        // CompositeEntity.
        final ActorGraphModel graphModel = new ActorGraphModel(entity);
        return new GraphPane(_controller, graphModel);
    }

    protected JComponent _createRightComponent(NamedObj entity) {
        if (!(entity instanceof SingleRuleTransformer)) {
            return super._createRightComponent(entity);
        }

        _graphs = new LinkedList<JGraph>();

        _tabbedPane = new JTabbedPane() {
            public void setMinimumSize(Dimension minimumSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setMinimumSize(minimumSize);
                }
            }

            public void setPreferredSize(Dimension preferredSize) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setPreferredSize(preferredSize);
                }
            }

            public void setSize(int width, int height) {
                Iterator<JGraph> graphsIterator = _graphs.iterator();
                while (graphsIterator.hasNext()) {
                    graphsIterator.next().setSize(width, height);
                }
            }

            /** Serial ID */
            private static final long serialVersionUID = -4998226270980176175L;
        };

        _tabbedPane.addChangeListener(this);
        Iterator<?> cases = ((SingleRuleTransformer) entity).entityList(
                CompositeActorMatcher.class).iterator();
        boolean first = true;
        while (cases.hasNext()) {
            CompositeActorMatcher matcher = (CompositeActorMatcher) cases.next();
            JGraph jgraph = _addTabbedPane(matcher, false);
            // The first JGraph is the one with the focus.
            if (first) {
                first = false;
                setJGraph(jgraph);
            }
            _graphs.add(jgraph);
        }

        _createCorrespondenceTab((SingleRuleTransformer) entity);

        return _tabbedPane;
    }

    protected CompositeEntity _getCurrentMatcher() {
        ActorGraphModel graphModel =
            (ActorGraphModel) _controller.getGraphModel();
        CompositeEntity model =
            (CompositeEntity) graphModel.getPtolemyModel();
        if (hasTabs()) {
            NamedObj parent = model.getContainer();
            while (!(parent instanceof SingleRuleTransformer)) {
                parent = parent.getContainer();
            }
            List<?> entityList =
                ((SingleRuleTransformer) parent).entityList(
                        CompositeActorMatcher.class);
            return (CompositeActorMatcher) entityList.get(_selectedIndex);
        } else {
            return model;
        }
    }

    protected GraphController _getGraphController() {
        return _controller;
    }

    protected JTabbedPane _getTabbedPane() {
        return _tabbedPane;
    }

    protected SingleRuleTransformer _getTransformer() {
        CompositeEntity model = _getCurrentMatcher();
        while (!(model instanceof SingleRuleTransformer)) {
            model = (CompositeEntity) model.getContainer();
        }
        return (SingleRuleTransformer) model;
    }

    /** Add a tabbed pane for the specified case.
     *  @param refinement The case.
     *  @param newPane True to add the pane prior to the last pane.
     *  @return The pane.
     */
    private JGraph _addTabbedPane(CompositeActorMatcher matcher,
            boolean newPane) {
        GraphPane pane = _createGraphPane(matcher);
        pane.getForegroundLayer().setPickHalo(2);
        pane.getForegroundEventLayer().setConsuming(false);
        pane.getForegroundEventLayer().setEnabled(true);
        pane.getForegroundEventLayer().addLayerListener(new LayerAdapter() {
            /** Invoked when the mouse is pressed on a layer
             * or figure.
             */
            public void mousePressed(LayerEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }
        });
        JGraph jgraph = new JGraph(pane);
        String name = matcher.getName();
        jgraph.setName(name);
        int index = _tabbedPane.getComponentCount();
        // Put before the default pane, unless this is the default.
        if (newPane) {
            index--;
        }
        _tabbedPane.add(jgraph, index);
        jgraph.setBackground(BACKGROUND_COLOR);
        // Create a drop target for the jgraph.
        new EditorDropTarget(jgraph);
        return jgraph;
    }

    private void _commitCorrespondenceChange() {
        SingleRuleTransformer transformer = (SingleRuleTransformer) getModel();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < _correspondenceTableModel.getRowCount(); i++) {
            if (buffer.length() > 0) {
                buffer.append("<..>");
            }
            buffer.append(_getCorrespondenceValue(i, 1));
            buffer.append("<..>");
            buffer.append(_getCorrespondenceValue(i, 2));
        }
        String moml = "<property name=\"correspondence\" value=\""
            + StringUtilities.escapeForXML(buffer.toString())
            + "\"/>";
        MoMLChangeRequest request =
            new MoMLChangeRequest(this, transformer, moml, null);
        request.setUndoable(true);
        transformer.requestChange(request);
    }

    private JPanel _createCellPanel(String value) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextField textField = new JTextField(value, SwingConstants.CENTER);
        textField.setBorder(_EMPTY_BORDER);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setOpaque(false);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private void _createCorrespondenceTab(SingleRuleTransformer transformer) {
        JPanel panel = new JPanel(new BorderLayout());

        _correspondenceTableModel = new DefaultTableModel(
                new Object[] {"", "Pattern Object", "Replacement Object"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }

            private static final long serialVersionUID = -3761868358642028952L;
        };

        _correspondenceTable = new JTable(_correspondenceTableModel);
        _correspondenceTable.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        _correspondenceTable.setEnabled(true);
        _correspondenceTable.setRowHeight(22);
        _correspondenceTable.setSelectionBackground(_SELECTED_COLOR);
        _correspondenceTable.setSelectionForeground(Color.BLACK);

        TableColumnModel model = _correspondenceTable.getColumnModel();
        TableColumn indexColumn = model.getColumn(0);
        indexColumn.setMinWidth(10);
        indexColumn.setPreferredWidth(15);
        indexColumn.setMaxWidth(30);
        DefaultTableCellRenderer indexRenderer =
            new DefaultTableCellRenderer();
        indexRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        _cellEditor = new CellPanelEditor();
        for (int i = 0; i < 3; i++) {
            TableColumn column = model.getColumn(i);
            column.setCellRenderer(_cellEditor);
            if (i > 0) {
                column.setCellEditor(_cellEditor);
            }
        }

        JTableHeader header = _correspondenceTable.getTableHeader();
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
        panel.add(_correspondenceTable, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        int index = _tabbedPane.getComponentCount();
        _tabbedPane.add(panel, index);
        _tabbedPane.setTitleAt(index, "Correspondence");

        _updateCorrespondenceTable();
    }

    private String _getCorrespondenceValue(int row, int column) {
        JPanel panel =
            (JPanel) _correspondenceTableModel.getValueAt(row, column);
        JTextField textField = (JTextField) panel.getComponent(0);
        return textField.getText();
    }

    private void _updateCorrespondenceTable() {
        if (_cellEditor != null) {
            _cellEditor.stopCellEditing();
        }
        _correspondenceTableModel.removeTableModelListener(this);

        while (_correspondenceTableModel.getRowCount() > 0) {
            _correspondenceTableModel.removeRow(0);
        }

        SingleRuleTransformer transformer = (SingleRuleTransformer) getModel();
        List<Pair<String, String>> correspondenceList =
            transformer.getCorrespondence();
        int i = 1;
        for (Pair<String, String> correspondence : correspondenceList) {
            _correspondenceTableModel.addRow(new Object[] {
                    _createCellPanel(Integer.toString(i++)),
                    _createCellPanel(correspondence.getFirst()),
                    _createCellPanel(correspondence.getSecond())
            });
        }

        _correspondenceTableModel.addTableModelListener(this);
    }

    private CellPanelEditor _cellEditor;

    /** The graph controller.  This is created in _createGraphPane(). */
    private ActorEditorGraphController _controller;

    private JTable _correspondenceTable;

    private DefaultTableModel _correspondenceTableModel;

    private static final Border _EMPTY_BORDER =
        BorderFactory.createEmptyBorder();

    private JGraph _fullScreenJGraph;

    private List<JGraph> _graphs;

    private Component _previousDefaultContext;

    private JDialog _screen;

    private static final Color _SELECTED_COLOR = new Color(230, 230, 255);

    private int _selectedIndex = 0;

    private JTabbedPane _tabbedPane;

    private static final long serialVersionUID = -7057287804004272089L;
}

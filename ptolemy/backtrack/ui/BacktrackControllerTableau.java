/* 

Copyright (c) 2005-2006 The Regents of the University of California.
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
import java.util.HashMap;
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
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// BacktrackControllerTableau
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class BacktrackControllerTableau extends Tableau {
    public BacktrackControllerTableau(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super(container, NAME);
        setTitle(NAME);
        
        BacktrackControllerFrame frame = new BacktrackControllerFrame();
        setFrame(frame);
        frame.setTableau(this);
        
        frame.setVisible(true);
    }
    
    public static final String NAME = "Backtrack Controller";
    
    public class BacktrackControllerFrame extends TableauFrame {
        public BacktrackControllerFrame() {
            super(null, null);
            
            Container container = getContentPane();
            
            _modelSelector = new JComboBox();
            container.add(_modelSelector, BorderLayout.NORTH);
            
            _handleTable = new JTable(_handleTableModel) {
                public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
                    _rollbackButton.setEnabled(true);
                    _discardButton.setEnabled(true);
                }
            };
            _handleTable.getColumnModel().getColumn(0).setPreferredWidth(30);
            _handleTable.getColumnModel().getColumn(1).setPreferredWidth(30);
            _handleTable.getColumnModel().getColumn(2).setPreferredWidth(120);
            container.add(new JScrollPane(_handleTable), BorderLayout.CENTER);
            
            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new FlowLayout());
            container.add(bottomPanel, BorderLayout.SOUTH);
            
            _checkpointButton = new JButton("checkpoint");
            _checkpointButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createCheckpoint();
                }
            });
            bottomPanel.add(_checkpointButton);

            _rollbackButton = new JButton("rollback");
            _rollbackButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    rollback();
                }
            });
            _rollbackButton.setEnabled(false);
            bottomPanel.add(_rollbackButton);
            
            _discardButton = new JButton("discard");
            _discardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            });
            _discardButton.setEnabled(false);
            bottomPanel.add(_discardButton);
            
            pack();
            
            refreshModelSelector();
        }
        
        public synchronized void createCheckpoint() {
            ModelSelectorElement selectedModel =
                (ModelSelectorElement)_modelSelector.getSelectedItem();
            if (selectedModel != null) {
                NamedObj model = selectedModel.getFrame().getModel();
                long handle =
                    _controller.createCheckpoint((CompositeActor)model);
                _handleTableModel.insertElement(
                        new HandleTableElement(handle, new Date()));
                
            }
        }
        
        public synchronized void refreshModelSelector() {
            try {
                Frame[] frames = getFrames();
                _modelSelector.removeAllItems();
                for (int i = 0; i < frames.length; i++) {
                    Frame frame = frames[i];
                    if (frame.isVisible() && frame instanceof PtolemyFrame) {
                        PtolemyFrame ptolemyFrame = (PtolemyFrame)frame;
                        NamedObj model =
                            ((PtolemyFrame)ptolemyFrame).getModel();
                        if (model instanceof CompositeActor) {
                            if (!_modelFrames.containsKey(frame)) {
                                _modelFrames.put(ptolemyFrame,
                                        new ModelSelectorElement(
                                                ptolemyFrame));
                            }
                            _modelSelector.addItem(
                                    _modelFrames.get(ptolemyFrame));
                        }
                    }
                }
            } catch (Exception e) {
                MessageHandler.error("Unable to refresh model selector.", e);
            }
        }
        
        public synchronized void rollback() {
            Long handle = (Long)
                _handleTableModel.getValueAt(_handleTable.getSelectedRow(), 0);
            _controller.rollback(handle.longValue(), true);
        }
        
        private JButton _checkpointButton;
        
        private BacktrackController _controller = new BacktrackController();

        private JButton _discardButton;

        private JTable _handleTable;
        
        private HandleTableModel _handleTableModel = new HandleTableModel();
        
        private HashMap _modelFrames = new HashMap();
        
        private JComboBox _modelSelector;
        
        private JButton _rollbackButton;
        
        private class ModelSelectorElement {
            ModelSelectorElement(PtolemyFrame frame) {
                _frame = frame;
            }
            
            public PtolemyFrame getFrame() {
                return _frame;
            }
            
            public String toString() {
                return _frame.getTitle();
            }
            
            private PtolemyFrame _frame;
        }
        
        private class HandleTableElement {
            HandleTableElement(long handle, Date time) {
                _handle = handle;
                _time = time;
            }
            
            public long getHandle() {
                return _handle;
            }
            
            public Date getTime() {
                return _time;
            }
            
            private long _handle;
            
            private Date _time;
        }
        
        private class HandleTableModel extends AbstractTableModel {

            public int getColumnCount() {
                return _columnNames.length;
            }

            public String getColumnName(int column) {
                return _columnNames[column];
            }

            public int getRowCount() {
                return _data.size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                HandleTableElement element =
                    (HandleTableElement)_data.get(rowIndex);
                switch (columnIndex) {
                case 0:
                    return new Long(element.getHandle());
                case 1:
                    return null;    // TODO
                case 2:
                    return element.getTime();
                default:
                    return null;
                }
            }
            
            public synchronized void insertElement(
                    HandleTableElement element) {
                _data.add(element);
                fireTableRowsInserted(_data.size() - 1, _data.size() - 1);
            }
            
            private String[] _columnNames = new String[] {
                    "Handle", "Objects", "Time Created"
            };
            
            private List _data = new LinkedList();
        }
    }
}

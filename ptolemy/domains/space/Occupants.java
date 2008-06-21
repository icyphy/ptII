/* Display occupants of a room.

 Copyright (c) 1998-2007 The Regents of the University of California.
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
package ptolemy.domains.space;

import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import ptolemy.actor.gui.ArrayOfRecordsPane;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.lib.database.ArrayOfRecordsRecorder;
import ptolemy.data.ArrayToken;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// Occupants

/**
 A Occupants display actor.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Occupants extends ArrayOfRecordsRecorder {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Occupants(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        // NOTE: The following depends on vergil, so with this
        // here, the actor can't run headless.
        OccupantsConfigureFactory factory
                = new OccupantsConfigureFactory(this, "factory");
        
        columns.setExpression("{\"deskno\", \"lname\"}");
        colorKey.setExpression("sponsorlname");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The query. */
    private Query _query;
    
    /** The table. */
    private JTable _table;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    /** An interactive editor that configures the occupants. */
    public class OccupantsConfigureFactory extends EditorFactory {

        /** Construct a factory with the specified container and name.
         *  @param container The container.
         *  @param name The name of the factory.
         *  @exception IllegalActionException If the factory is not of an
         *   acceptable attribute for the container.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public OccupantsConfigureFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create a top-level viewer for the specified object with the
         *  specified parent window.
         *  @param object The object to configure, which is required to
         *   contain a parameter with name matching <i>parameterName</i>
         *   and value that is an array of records.
         *  @param parent The parent window, which is required to be an
         *   instance of TableauFrame.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Parameter attributeToEdit = Occupants.this.records;
                ArrayToken value = (ArrayToken)attributeToEdit.getToken();
                ArrayOfRecordsPane pane = new ArrayOfRecordsPane();
                pane.display(value);
                _table = pane.table;
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.add(pane);
                
                // Add a query to support editing of selected lines.
                _query = new Query();
                TableModel model = _table.getModel();
                // Populate the query first with the first row.
                for (int c = 0; c < model.getColumnCount(); c++) {
                    String columnName = model.getColumnName(c);
                    String currentValue = "";
                    if (model.getRowCount() > 0) {
                        currentValue = model.getValueAt(0, c).toString();
                    }
                    _query.addLine(columnName, columnName, currentValue);
                }
                panel.add(_query);
                
                // Set up table selection interaction.
                // Set the table to allow only one row selected at a time.
                _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                _table.getSelectionModel().addListSelectionListener(new RowListener());
                
                ComponentDialog dialog = new ComponentDialog(
                        parent, object.getFullName(), panel);
            } catch (KernelException ex) {
                MessageHandler.error(
                        "Cannot get specified string attribute to edit.", ex);
                return;
            }
        }
    }
    
    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting() || _table == null) {
                return;
            }
            int row = _table.getSelectionModel().getLeadSelectionIndex();
            TableModel model = _table.getModel();
            // Populate the query first with the first row.
            for (int c = 0; c < model.getColumnCount(); c++) {
                String columnName = model.getColumnName(c);
                String currentValue = "";
                if (model.getRowCount() > row) {
                    currentValue = model.getValueAt(row, c).toString();
                }
                _query.set(columnName, currentValue);
            }
        }
    }
}

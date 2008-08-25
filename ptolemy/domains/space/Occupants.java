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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.ArrayOfRecordsPane;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.lib.database.ArrayOfRecordsRecorder;
import ptolemy.actor.lib.database.DatabaseManager;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// Occupants

/**
 A Occupants display actor. This actor specializes its superclass
 for use with a space database.

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
        new OccupantsConfigureFactory(this, "factory");
        
        iconColumns.setExpression("{\"deskno\", \"lname\"}");
        colorKey.setExpression("sponsorlname");
        columns.setExpression("{\"deskno\", \"lname\", \"fnames\"," +
        		" \"email\", \"sponsorlname\", \"spacenotes\"}");
        
        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");

        table = new StringParameter(this, "table");
        table.setExpression("v_spaces");
        
        // Construct database schema information.
        
        // Construct a set of names of fields that remain with the
        // space rather than with the occupant.
        // FIXME: There must be a better way to do this using SQL.
        // How to parameterize this?
        _spaceFields = new LinkedHashSet<String>();
        _spaceFields.add("bldg");
        _spaceFields.add("room");
        _spaceFields.add("spaceid");
        _spaceFields.add("deskno");
        _spaceFields.add("hasdesk");
        _spaceFields.add("roomtype");
        _spaceFields.add("spaceid");

        // Construct a set of names of fields that remain with the
        // occupant rather than with the space.
        // FIXME: There must be a better way to do this using SQL.
        // How to parameterize this?
        // NOTE: The commented-out items we don't want to be visible.
        _occupantFields = new LinkedHashSet<String>();
        // _occupantFields.add("calnetid");
        _occupantFields.add("lname");
        _occupantFields.add("fnames");
        _occupantFields.add("email");
        // _occupantFields.add("personid");
        _occupantFields.add("spacenotes");
        // _occupantFields.add("sponsorfnames");
        // _occupantFields.add("sponsorid");
        // _occupantFields.add("sponsorlname");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** Name of the DatabaseManager to use. 
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;
    
    /** Table to use within the database.
     *  This is a string that defaults to "v_spaces".
     */
    public StringParameter table;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** A set of names of fields that remain with the occupant rather the space. */
    private Set<String> _occupantFields;

    /** The selected row. */
    private int _selectedRow = -1;
    
    /** A set of names of fields that remain with the space rather the occupant. */
    private Set<String> _spaceFields;
    
    /** The table. */
    private JTable _table;
    
    /** Indicator that a move to a new location should add a new space. */
    private static int _ADD_NEW = 0;
    
    /** Indicator that a move to a new location should add a new space. */
    private static int _REPLACE = 1;
    
    /** Indicator that a move to a new location should add a new space. */
    private static int _SWAP = 2;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Given a record and a string, return the entry in the record
     *  if there is one and it is a StringToken, and otherwise, return
     *  an empty string.
     *  @param record The record.
     *  @param field The field name.
     */
    private String _getField(RecordToken record, String field) {
        Token fieldValue = record.get(field);
        if (fieldValue instanceof StringToken) {
            return ((StringToken)fieldValue).stringValue();
        }
        return "";
    }

    /** Using the specified database, move the contents of <i>sourceSpaceID</i>
     *  to the <i>destinationSpaceID</i>, either adding to what is there,
     *  overwriting it, or swapping with it. In the latter two cases, if
     *  there is more than one occupant in the destination, this implementation
     *  simply works with the first one returned by the database.
     *  @param database The database manager.
     *  @param sourceSpaceID The spaceid of the source.
     *  @param destinationSpaceID The spaceid of the destination.
     *  @param mode One of _ADD_NEW, _REPLACE, or _SWAP.
     *  @return False if the user cancels, true otherwise.
     *  @throws KernelException If the table parameter can't
     *   be evaluated or if executing the database query throws it,
     *   or if re-executing the model throws it.
     */
    private boolean _move(
            DatabaseManager database, 
            String sourceSpaceID, 
            String destinationSpaceID,
            int mode) 
            throws KernelException {
        
        // If the source and destination IDs are the same, don't do anything.
        if (sourceSpaceID.equals(destinationSpaceID)) {
            try {
                MessageHandler.warning("Source and destination are the same!");
            } catch (CancelException ex) {
                return false;
            }
            return true;
        }

        // First use a query to get a complete record of the source.
        StringBuffer sql1 = new StringBuffer();
        sql1.append("select * from ");
        sql1.append(table.stringValue());
        sql1.append(" where trim(spaceid)='");
        sql1.append(sourceSpaceID);
        sql1.append("';");
        String databaseName = databaseManager.stringValue();
        database = DatabaseManager.findDatabaseManager(databaseName, Occupants.this);
        ArrayToken sourceArray = database.executeQuery(sql1.toString());
        // If the above returns null, the user canceled.
        if (sourceArray == null) {
            return false;
        }
        // FIXME: sourceArray should have at most one entry. Check this?
        if (sourceArray.length() == 0) {
            try {
                MessageHandler.warning("No space with ID " + sourceSpaceID);
            } catch (CancelException ex) {
                return false;
            }
            return true;
        }
        RecordToken sourceRecord = (RecordToken)sourceArray.getElement(0);
        
        // Next use a query to get a complete record of the destination.
        StringBuffer sql2 = new StringBuffer();
        sql2.append("select * from ");
        sql2.append(table.stringValue());
        sql2.append(" where trim(spaceid)='");
        sql2.append(destinationSpaceID);
        sql2.append("';");
        ArrayToken destinationArray = database.executeQuery(sql2.toString());
        // If the above returns null, the user canceled.
        if (destinationArray == null) {
            return false;
        }
        // FIXME: destinationArray should have at most one entry. Check this?
        if (destinationArray.length() == 0) {
            try {
                MessageHandler.warning("No space with ID " + sourceSpaceID);
            } catch (CancelException ex) {
                return false;
            }
            return true;
        }
        RecordToken destinationRecord = (RecordToken)destinationArray.getElement(0);

        // Now do different things depending on the mode.
        if (mode == _ADD_NEW) {
            // Create a new entry that duplicates all the fields of source
            // except the ones that identify the destination location.
            StringBuffer fieldNames = new StringBuffer();
            StringBuffer fieldValues = new StringBuffer();
            StringBuffer clearedFieldValues = new StringBuffer();
            Set<String> labels = sourceRecord.labelSet();
            boolean first = true;
            for (String label : labels) {
                // Do not create a spaceid. This is auto generated.
                if (label.equals("spaceid")) {
                    continue;
                }
                if (first) {
                    first = false;
                    fieldValues.append("'");
                } else {
                    fieldNames.append(",");
                    fieldValues.append("','");
                }
                fieldNames.append(label);
                // If the field identifies the space, then use the destination value.
                // Otherwise, use the source value.
                String fieldValue = ((StringToken)sourceRecord.get(label)).stringValue();
                if (_spaceFields.contains(label)) {
                    fieldValue = ((StringToken)destinationRecord.get(label)).stringValue();
                } else {
                    if (clearedFieldValues.length() > 0) {
                        clearedFieldValues.append(", ");
                    }
                    clearedFieldValues.append(label);
                    clearedFieldValues.append("=''");
                }
                fieldValues.append(fieldValue);
            }
            fieldValues.append("'");
            
            // Now construct the query
            StringBuffer sql = new StringBuffer();
            sql.append("insert into ");
            sql.append(table.stringValue());
            sql.append(" (");
            sql.append(fieldNames.toString());
            sql.append(") values(");
            sql.append(fieldValues.toString());
            sql.append(");");

            database.executeUpdate(sql.toString(), 1);

            // Append to the query to clear the source location.
            StringBuffer clear = new StringBuffer();
            clear.append("update ");
            clear.append(table.stringValue());
            clear.append(" set ");
            clear.append(clearedFieldValues.toString());
            clear.append(" where trim(spaceid)='");
            clear.append(sourceSpaceID);
            clear.append("';");
            
            database.executeUpdate(clear.toString(), 1);
        } else if (mode == _REPLACE) {
            // Create a new entry that replaces all the fields of destination
            // except the ones that identify the destination location.
            StringBuffer setFieldValues = new StringBuffer();
            StringBuffer clearedFieldValues = new StringBuffer();
            Set<String> labels = sourceRecord.labelSet();
            for (String label : labels) {
                // Skip the spaceid.
                if (label.equals("spaceid")) {
                    continue;
                }
                // Only set fields that are not bound to the space.
                if (!_spaceFields.contains(label)) {
                    if (setFieldValues.length() > 0) {
                        setFieldValues.append(", ");
                    }
                    setFieldValues.append(label);
                    setFieldValues.append("='");
                    String sourceFieldValue = ((StringToken)sourceRecord.get(label)).stringValue();
                    setFieldValues.append(sourceFieldValue);
                    setFieldValues.append("'");

                    if (clearedFieldValues.length() > 0) {
                        clearedFieldValues.append(", ");
                    }
                    clearedFieldValues.append(label);
                    clearedFieldValues.append("=''");
                }
            }
            
            // Now construct the query
            StringBuffer sql = new StringBuffer();
            sql.append("update ");
            sql.append(table.stringValue());
            sql.append(" set ");
            sql.append(setFieldValues.toString());
            sql.append(" where trim(spaceid)='");
            sql.append(destinationSpaceID);
            sql.append("';");
            
            database.executeUpdate(sql.toString(), 1);

            // Append to the query to clear the source location.
            StringBuffer clear = new StringBuffer();
            clear.append("update ");
            clear.append(table.stringValue());
            clear.append(" set ");
            clear.append(clearedFieldValues.toString());
            clear.append(" where trim(spaceid)='");
            clear.append(sourceSpaceID);
            clear.append("';");
            
            database.executeUpdate(clear.toString(), 1);
        } else if (mode == _SWAP) {
            // Create a new entry that swaps all the fields
            // except the ones that identify the destination location.
            StringBuffer destinationFieldValues = new StringBuffer();
            StringBuffer sourceFieldValues = new StringBuffer();
            Set<String> labels = sourceRecord.labelSet();
            for (String label : labels) {
                // Skip the spaceid.
                if (label.equals("spaceid")) {
                    continue;
                }
                // Only set fields that are not bound to the space.
                if (!_spaceFields.contains(label)) {
                    if (destinationFieldValues.length() > 0) {
                        destinationFieldValues.append(", ");
                    }
                    destinationFieldValues.append(label);
                    destinationFieldValues.append("='");
                    String sourceFieldValue = ((StringToken)sourceRecord.get(label)).stringValue();
                    destinationFieldValues.append(sourceFieldValue);
                    destinationFieldValues.append("'");

                    if (sourceFieldValues.length() > 0) {
                        sourceFieldValues.append(", ");
                    }
                    sourceFieldValues.append(label);
                    sourceFieldValues.append("='");
                    String destinationFieldValue = ((StringToken)destinationRecord.get(label)).stringValue();
                    sourceFieldValues.append(destinationFieldValue);
                    sourceFieldValues.append("'");
                }
            }
            
            // Now construct the query
            StringBuffer sql = new StringBuffer();
            sql.append("update ");
            sql.append(table.stringValue());
            sql.append(" set ");
            sql.append(sourceFieldValues.toString());
            sql.append(" where trim(spaceid)='");
            sql.append(sourceSpaceID);
            sql.append("';");
            
            // FIXME: Show the query.
            System.out.println(sql.toString());
            
            database.executeUpdate(sql.toString(), 1);

            // Append to the query to clear the source location.
            StringBuffer clear = new StringBuffer();
            clear.append("update ");
            clear.append(table.stringValue());
            clear.append(" set ");
            clear.append(destinationFieldValues.toString());
            clear.append(" where trim(spaceid)='");
            clear.append(destinationSpaceID);
            clear.append("';");

            // FIXME: Show the query.
            System.out.println(clear.toString());
            
            database.executeUpdate(clear.toString(), 1);
        }

        // Run the model to update all the records.
        Manager manager = getManager();
        if (manager == null) {
            manager = new Manager(workspace(), "manager");
            ((CompositeActor) toplevel()).setManager(manager);
        }
        getManager().execute();                                        
        return true;
    }
    
    /** Construct a query for adding a new person to the space database.
     *  @param occupantsFields The fields to include in the query.
     *  @param initialPersonInfo A record to populate the query with initially.
     *  @param database The database manager to use.
     *  @param object The Ptolemy II object for which this is an editor.
     *  @param parent The owning frame.
     *  @return A record for a person to add to the database.
     */
    private RecordToken _newPersonQuery(
            Set<String> occupantFields,
            RecordToken initialPersonInfo,
            DatabaseManager database,
            NamedObj object,
            Frame parent)
            throws CancelException, IllegalActionException {
        Query query = new Query();
        for (String field : occupantFields) {
            query.addLine(field, field, _getField(initialPersonInfo, field));
        }
        ComponentDialog subdialog = new ComponentDialog(
                parent, "Add a new person", query);
        
        if (!"OK".equals(subdialog.buttonPressed())) {
            // User canceled.
            throw new CancelException();
        }
        
        Map<String,String> map = _recordAsMap(initialPersonInfo);
        for (String field : occupantFields) {
            map.put(field, query.getStringValue(field));
        }
        return new RecordToken(map);
    }

    /** Return the name of the first found occupant in the
     *  specified records, or null if there is no such occupant.
     */
    private String _occupants(ArrayToken records) {
        String result = null;
        if (records.length() > 0) {
            RecordToken occupant = (RecordToken)records.getElement(0);
            Token priorLname = occupant.get("lname");
            String lname = "";
            boolean foundOccupant = false;
            if (priorLname != null) {
                lname = ((StringToken)priorLname).stringValue().trim();
                if (lname.length() > 0) {
                    foundOccupant = true; 
                }
            }
            Token priorFnames = occupant.get("fnames");
            String fnames = "";
            if (priorFnames != null) {
                fnames = ((StringToken)priorFnames).stringValue().trim();
                if (fnames.length() > 0) {
                    foundOccupant = true; 
                }
            }
            if (foundOccupant) {
                if (fnames.length() > 0) {
                    result = fnames + " " + lname;
                } else {
                    result = lname;
                }
            }
        }
        return result;
    }
    
    /** Return a map representing the specified record token.
     *  @param record The record token.
     *  @return A map with the contents of the record.
     */
    private Map<String,String> _recordAsMap(RecordToken record) {
        Map<String,String> result = new LinkedHashMap<String,String>();
        Set<String> labels = record.labelSet();
        for (String label : labels) {
            result.put(label, _getField(record, label));
        }
        return result;
    }
    
    /** Open a dialog to search for a person.
     *  @param lname Initial entry for the last name.
     *  @param fnames Initial entry for the first names.
     *  @param classcd Initial entry for the class.
     *  @param email Initial entry for the email.
     *  @param sponsorlname Initial entry for the sponsor last name.
     *  @param parent The parent frame for dialogs.
     *  @param object The Ptolemy object owning the dialog.
     *  @param database The database manager.
     *  @param sponsor If non-null, then a record specifying a sponsor. When this is
     *   non-null, a match is not required. If it is null, and no match is found,
     *   the user will be given the option of creating a new person.
     *  @param label A text message to include in the dialog, or null to not specify one.
     *  @return A record for a person.
     */
    private RecordToken _searchForPerson(
            String lname,
            String fnames,
            String classcd,
            String email,
            String sponsorlname,
            Frame parent,
            NamedObj object,
            DatabaseManager database,
            RecordToken sponsor,
            String message
            ) throws CancelException, IllegalActionException {
        Query query = new Query();
        query.addLine("lname", "lastname", lname);
        query.addLine("fnames", "first names", fnames);
        // FIXME: Find available classes via a database query.
        String[] classes = {"grad", "faculty"};
        query.addChoice("classcd", "class", classes, classcd, true);
        query.addLine("email", "email", email);
        // FIXME: Find available sponsors via a database query.
        query.addLine("sponsorlname", "sponsor last name", sponsorlname);
        
        ComponentDialog subdialog = new ComponentDialog(
                parent, "Find a person", query, null, message);
        
        if (!"OK".equals(subdialog.buttonPressed())) {
            // User canceled.
            throw new CancelException();
        }
        lname = query.getStringValue("lname");
        fnames = query.getStringValue("fnames");
        classcd = query.getStringValue("classcd");
        email = query.getStringValue("email");
        sponsorlname = query.getStringValue("sponsorlname");

        StringBuffer sql = new StringBuffer();
        sql.append("select * from ");
        // FIXME: People database should be a parameter!
        // sql.append(peopleTable.stringValue());
        sql.append("v_people");
        sql.append(" where ");
        boolean needConnector = false;
        if (lname.trim().length() > 0) {
            sql.append("lname like '");
            sql.append(lname);
            sql.append("%'");
            needConnector = true;
        }
        if (fnames.trim().length() > 0) {
            if (needConnector) {
                sql.append(" AND ");
            }
            sql.append("fnames like '");
            sql.append(fnames);
            sql.append("%'");
            needConnector = true;
        }
        if (classcd.trim().length() > 0) {
            if (needConnector) {
                sql.append(" AND ");
            }
            sql.append("classcd='");
            sql.append(classcd);
            sql.append("'");
            needConnector = true;
        }
        if (email.trim().length() > 0) {
            if (needConnector) {
                sql.append(" AND ");
            }
            sql.append("email like '");
            sql.append(email);
            sql.append("%'");
            needConnector = true;
        }
        if (sponsorlname.trim().length() > 0) {
            if (needConnector) {
                sql.append(" AND ");
            }
            sql.append("sponsorlname like '");
            sql.append(sponsorlname);
            sql.append("%'");
            needConnector = true;
        }

        if (!needConnector) {
            // No pattern was specified.
            MessageHandler.warning("Please specify a search pattern.");
            // Re-open the dialog.
            return _searchForPerson(
                    lname, fnames, classcd, email, sponsorlname,
                    parent, object, database, sponsor, message);
        }
        sql.append(";");
        ArrayToken matches = database.executeQuery(sql.toString());

        // A null value means the user canceled.
        if (matches == null) {
            throw new CancelException();
        }

        // If there is more than one match, then ask the user to select one.
        // Otherwise, proceed to the dialog (which will have a button to
        // to redo the search).
        if (sponsor == null) {
            while (matches.length() == 0) {
                if (MessageHandler.yesNoQuestion("No matching entries. Create a new entry?")) {
                    // The final null argument forces the sponsor to match in the database.
                    sponsor = _searchForPerson(
                            sponsorlname, "", "", "", "", parent, object, 
                            database, null, "Please specify a sponsor:");
                    return _searchForPerson(
                            lname, fnames, classcd, email, sponsorlname,
                            parent, object, database, sponsor, "New person profile:");
                } else {
                    // Re-open the dialog to search again.
                    return _searchForPerson(
                            lname, fnames, classcd, email, sponsorlname,
                            parent, object, database, sponsor, message);
                }
            }
        } else {
            // Match not required. Construct a return value based on available information.
            // Do not include in the result the personid, which is auto-generated.
            String[] labels = new String[8];
            Token[] values = new Token[8];
            labels[0] = "calnetid";
            // No calnet id for a person not in the database.
            values[0] = new StringToken("");
            labels[1] = "classcd";
            values[1] = new StringToken(classcd);
            labels[2] = "email";
            values[2] = new StringToken(email);
            labels[3] = "fnames";
            values[3] = new StringToken(fnames);
            labels[4] = "lname";
            values[4] = new StringToken(lname);
            labels[5] = "sponsorfnames";
            values[5] = sponsor.get("fnames");
            labels[6] = "sponsorid";
            values[6] = sponsor.get("personid");
            labels[5] = "sponsorlname";
            values[5] = sponsor.get("lname");
            return new RecordToken(labels, values);
        }
        // At this point, we have one or more matching people.
        if(matches.length() > 1) {
            int selectedOne = -1;
            // Iterate until either the user selects one or cancels.
            while (selectedOne < 0) {
                // Multiple matches. Need to select one.
                ArrayOfRecordsPane selectOne = new ArrayOfRecordsPane();
                // FIXME: What to display here should be parameterized somewhere.
                ArrayToken columns = new ArrayToken(
                        "{\"lname\", \"fnames\", \"email\", \"classcd\"}");
                selectOne.display(matches, columns);
                ComponentDialog selectOneDialog = new ComponentDialog(
                        parent, "Select a person", selectOne);
                if (!"OK".equals(selectOneDialog.buttonPressed())) {
                    // User canceled.
                    throw new CancelException();
                }
                selectedOne = selectOne.table.getSelectedRow();
            }
            // User selected one. Pre-populate the information.
            return (RecordToken)matches.getElement(selectedOne);
        } else {
            // There is only one match. Use that.
            return (RecordToken)matches.getElement(0);                            
        }
    }
    
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
                ArrayToken columnsValue = (ArrayToken)columns.getToken();
                if (columnsValue != null && columnsValue.length() == 0) {
                    columnsValue = null;
                }
                pane.display(value, columnsValue);
                _table = pane.table;
                
                String[] buttons = {"Close",
                        "Move person",
                        "Add person",
                        "Remove space",
                        "Add space"};

                // Set up table selection interaction.
                // Set the table to allow only one row selected at a time.
                _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                _table.getSelectionModel().addListSelectionListener(new RowListener());

                // If there was previously a selection, then make sure it is still selected.
                if (_selectedRow >= 0 && _selectedRow < _table.getRowCount()) {
                    _table.getSelectionModel().setSelectionInterval(_selectedRow, _selectedRow);
                }
                
                ComponentDialog dialog = new ComponentDialog(
                        parent, object.getFullName(), pane, buttons);
                
                String response = dialog.buttonPressed();
                
                // First ensure that there is a selected row if one is needed.
                if ("Move person".equals(response)
                        || "Add person".equals(response)
                        || "Remove space".equals(response)) {
                    if (_selectedRow < 0 || _selectedRow >= _table.getRowCount()) {
                        try {
                            MessageHandler.warning("Please select a row.");
                        } catch (CancelException e) {
                            // If the user cancels, just return.
                            return;
                        }
                        // Re-open the dialog.
                        createEditor(object, parent);
                        return;
                    }
                }

                if ("Move person".equals(response)) {
                    
                    ///////////////////////////////////////////// Move a person
                    DatabaseManager database = null;
                    try {
                        // Guess about the space information and construct a query.
                        ArrayToken array = (ArrayToken)records.getToken();
                        RecordToken record = (RecordToken)array.getElement(_selectedRow);
                        String building = _getField(record, "bldg");
                        String room = _getField(record, "room");
                        String deskno = _getField(record, "deskno");
                        String spacenotes = _getField(record, "spacenotes");
                        String fnames = _getField(record, "fnames");
                        String lname = _getField(record, "lname");
                        
                        // If there is no person, warn and abort.
                        if (fnames.trim().equals("") && lname.trim().equals("")) {
                            MessageHandler.warning("No person occupying the selected space.");
                            createEditor(object, parent);
                            return;
                        }

                        Query query = new Query();
                        query.addLine("bldg", "building", building);
                        query.addLine("room", "room", room);
                        query.addLine("deskno", "desk", deskno);
                        query.addLine("spacenotes", "notes", spacenotes);
                        
                        String message = "Move " + fnames + " " + lname + " to location:";
                        // The null below says to use default buttons.
                        ComponentDialog subdialog = new ComponentDialog(
                                parent, "Move a person", query, null, message);
                        
                        if ("OK".equals(subdialog.buttonPressed())) {
                            building = query.getStringValue("bldg");
                            room = query.getStringValue("room");
                            deskno = query.getStringValue("deskno");
                            spacenotes = query.getStringValue("spacenotes");
                            
                            // Check to see whether the requested space is vacant.
                            StringBuffer sql1 = new StringBuffer();
                            sql1.append("select spaceid,fnames,lname from ");
                            sql1.append(table.stringValue());
                            sql1.append(" where trim(bldg)='");
                            sql1.append(building.trim());
                            sql1.append("' AND trim(room)='");
                            sql1.append(room);
                            // Desk number may not be given.
                            if (!deskno.trim().equals("")) {
                                sql1.append("' AND trim(deskno)='");
                                sql1.append(deskno);
                            }
                            sql1.append("';");

                            String databaseName = databaseManager.stringValue();
                            database = DatabaseManager.findDatabaseManager(databaseName, Occupants.this);
                            ArrayToken priorOccupants = database.executeQuery(sql1.toString());
                            // If the above returns null, the user canceled.
                            if (priorOccupants == null) {
                                return;
                            }
                            if (priorOccupants.length() == 0) {
                                // No space was found matching the specification.
                                MessageHandler.warning("No such space found.");
                            } else {
                                // Use for the destination spaceID the first of the
                                // prior occupants. They should all have the same space-specific
                                // information.
                                RecordToken destination = (RecordToken)priorOccupants.getElement(0);
                                String priorOccupantsSpaceID 
                                        = ((StringToken)destination.get("spaceid")).stringValue();
                                String sourceSpaceID
                                        = ((StringToken)record.get("spaceid")).stringValue();

                                // Get the name of the occupant of the first matching space.
                                String name = _occupants(priorOccupants);
                                if (name != null) {
                                    // Space is occupied. Get confirmation.
                                    StringBuffer question = new StringBuffer();
                                    question.append("Space is occupied by ");
                                    question.append(name);
                                    question.append(". Move anyway?");
                                    
                                    String[] confirmButtons = {"Share with occupant",
                                            "Replace occupant",
                                            "Swap with occupant",
                                            "Cancel"};
                                    JLabel label = new JLabel(question.toString());
                                    ComponentDialog confirm = new ComponentDialog(
                                            parent, message, label, confirmButtons);
                                    String confirmResponse = confirm.buttonPressed();
                                    
                                    if ("Cancel".equals(confirmResponse)) {
                                        return;
                                    } else if ("Share with occupant".equals(confirmResponse)) {
                                        if (_move(database, sourceSpaceID, priorOccupantsSpaceID, _ADD_NEW)) {
                                            // User did not cancel.
                                            // Re-open the dialog until the Close button is pressed.
                                            createEditor(object, parent);
                                        }
                                        return;
                                    } else if ("Swap with occupant".equals(confirmResponse)) {
                                        if (_move(database, sourceSpaceID, priorOccupantsSpaceID, _SWAP)) {
                                            // User did not cancel.
                                            // Re-open the dialog until the Close button is pressed.
                                            createEditor(object, parent);
                                        }
                                        return;
                                    }
                                    // If the response is "Replace occupant",
                                    // then we fall through and do the same thing as
                                    // if the space were not occupied.
                                }
                                // Space is not occupied or user selected to replace current
                                // occupant.
                                if (!_move(database, sourceSpaceID, priorOccupantsSpaceID, _REPLACE)) {
                                    // User canceled.
                                    return;
                                }
                            }
                        }
                        // Re-open the dialog until the Close button is pressed.
                        createEditor(object, parent);
                    } catch (KernelException e) {
                        // This should have been caught earlier.
                        MessageHandler.error(
                                "Update failed. Perhaps you need to resynchronize with the database?", e);
                        return;
                    } catch (CancelException e) {
                        return;
                    } finally {
                        if (database != null) {
                            try {
                                database.closeConnection();
                            } catch (IllegalActionException e) {
                                MessageHandler.error(
                                        "Failed to close database connection.", e);
                            }
                        }
                    }                    
                } else if ("Add person".equals(response)) {

                    ///////////////////////////////////////////// Add a person
                    
                    try {
                        // First bring up a dialog to specify a search for an existing person.
                        String databaseName = databaseManager.stringValue();
                        DatabaseManager database = DatabaseManager
                                .findDatabaseManager(databaseName, Occupants.this);
                        RecordToken initialPersonInfo = _searchForPerson(
                                "", "", "", "", "", parent, object, database,
                                null, "Enter information to search for (partial information is OK):");

                        // Construct a query pre-populated with the specified information.
                        // FIXME: The following displays too much information, and allows too
                        // much editing of that information!
                        RecordToken newPerson = _newPersonQuery(
                                _occupantFields, initialPersonInfo, database, object, parent);
                        
                        // FIXME: put the result in the database.
                        System.out.println(newPerson);
                    } catch (CancelException e) {
                        return;
                    }

                } else if ("Remove space".equals(response)) {

                    ///////////////////////////////////////////// Remove a space
                    DatabaseManager database = null;
                    try {
                        ArrayToken array = (ArrayToken)records.getToken();
                        if (array.length() <= _selectedRow) {
                            MessageHandler.error("No such row with index " + _selectedRow);
                            return;
                        }
                        RecordToken record = (RecordToken)array.getElement(_selectedRow);
                        String spaceid = ((StringToken)record.get("spaceid")).stringValue();
                        if (spaceid == null) {
                            MessageHandler.error("No space ID for the selected space.");
                            return;
                        }
                        // Get confirmation.
                        if (!MessageHandler.yesNoQuestion(
                                "Are you sure you want to permanently remove space with ID " + spaceid)) {
                            return;
                        }
                        StringBuffer sql = new StringBuffer();
                        sql.append("delete from ");
                        sql.append(table.stringValue());
                        sql.append(" where trim(spaceid)='");
                        sql.append(spaceid);
                        sql.append("';");
                        String databaseName = databaseManager.stringValue();
                        database = DatabaseManager
                                .findDatabaseManager(databaseName, Occupants.this);
                        database.executeUpdate(sql.toString(), 1);
                        
                        // Run the model to update all the records.
                        Manager manager = getManager();
                        if (manager == null) {
                            manager = new Manager(workspace(), "manager");
                            ((CompositeActor) toplevel()).setManager(manager);
                        }
                        getManager().execute();
                        // Re-open the dialog until the Close button is pressed.
                        createEditor(object, parent);
                    } catch (KernelException e) {
                        // This should have been caught earlier.
                        MessageHandler.error(
                                "Update failed. Perhaps you need to resynchronize with the database?", e);
                        return;
                    } finally {
                        if (database != null) {
                            try {
                                database.closeConnection();
                            } catch (IllegalActionException e) {
                                MessageHandler.error(
                                        "Failed to close database connection.", e);
                            }
                        }
                    }
                } else if ("Add space".equals(response)) {

                    ///////////////////////////////////////////// Add a space
                    DatabaseManager database = null;
                    try {
                        // Guess about the space information and construct a query.
                        ArrayToken array = (ArrayToken)records.getToken();
                        RecordToken record = (RecordToken)array.getElement(_selectedRow);
                        String building = _getField(record, "bldg");
                        String room = _getField(record, "room");
                        // FIXME: could be smarter and guess the desk number.
                        String deskno = _getField(record, "deskno");
                        String hasdesk = _getField(record, "hasdesk");
                        String roomtype = _getField(record, "roomtype");
                        String spacenotes = _getField(record, "spacenotes");

                        Query query = new Query();
                        query.addLine("bldg", "building", building);
                        query.addLine("room", "room", room);
                        query.addLine("deskno", "desk", deskno);
                        query.addLine("hasdesk", "has desk", hasdesk);
                        query.addLine("roomtype", "room type", roomtype);
                        query.addLine("spacenotes", "notes", spacenotes);
                        
                        ComponentDialog subdialog = new ComponentDialog(
                                parent, "Add a Space", query);
                        
                        if ("OK".equals(subdialog.buttonPressed())) {
                            building = query.getStringValue("bldg");
                            room = query.getStringValue("room");
                            deskno = query.getStringValue("deskno");
                            hasdesk = query.getStringValue("hasdesk");
                            roomtype = query.getStringValue("roomtype");
                            spacenotes = query.getStringValue("spacenotes");

                            StringBuffer sql = new StringBuffer();
                            sql.append("insert into ");
                            sql.append(table.stringValue());
                            sql.append(" (bldg,room,deskno,hasdesk,roomtype,spacenotes) values('");
                            sql.append(building);
                            sql.append("','");
                            sql.append(room);
                            sql.append("','");
                            sql.append(deskno);
                            sql.append("','");
                            sql.append(hasdesk);
                            sql.append("','");
                            sql.append(roomtype);
                            sql.append("','");
                            sql.append(spacenotes);
                            sql.append("');");
                            String databaseName = databaseManager.stringValue();
                            database = DatabaseManager
                            .findDatabaseManager(databaseName, Occupants.this);
                            database.executeUpdate(sql.toString(), 1);

                            // Run the model to update all the records.
                            Manager manager = getManager();
                            if (manager == null) {
                                manager = new Manager(workspace(), "manager");
                                ((CompositeActor) toplevel()).setManager(manager);
                            }
                            getManager().execute();
                        }
                        // Re-open the dialog until the Close button is pressed.
                        createEditor(object, parent);
                    } catch (KernelException e) {
                        // This should have been caught earlier.
                        MessageHandler.error(
                                "Update failed. Perhaps you need to resynchronize with the database?", e);
                        return;
                    } finally {
                        if (database != null) {
                            try {
                                database.closeConnection();
                            } catch (IllegalActionException e) {
                                MessageHandler.error(
                                        "Failed to close database connection.", e);
                            }
                        }
                    }
                }
            } catch (KernelException ex) {
                MessageHandler.error(
                        "Cannot get specified string attribute to edit.", ex);
                return;
            }
        }        
    }

    /** If a row is selected, then record which row is selected. */
    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting() || _table == null) {
                return;
            }
            _selectedRow = _table.getSelectionModel().getLeadSelectionIndex();
        }
    }
}

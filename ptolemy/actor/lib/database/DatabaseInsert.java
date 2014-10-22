/* Insert input arrays of records into a table.

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
package ptolemy.actor.lib.database;

import java.util.Set;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DatabaseInsert

/**
 Insert the input arrays of records into the specified table.
 The table needs to exist in the database and have columns with
 the same names as the record field names. This actor optionally
 clears the table in its initialize() method. If no errors occur
 during insertion, then it commits the changes in its wrapup()
 method.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseInsert extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseInsert(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");

        table = new StringParameter(this, "table");
        table.setExpression("v_people");

        clear = new Parameter(this, "clear");
        clear.setExpression("false");
        clear.setTypeEquals(BaseType.BOOLEAN);

        // Constrain the output type to be a record type with
        // unspecified fields.
        // NOTE: The output is actually a subtype of this.
        // This is OK because lossless conversion occurs at the
        // output, which (as of 6/19/08) leaves the record unchanged.
        input.setTypeEquals(new ArrayType(BaseType.RECORD));

        // The fire() method only reads from channel 0 of the input port.
        input.setMultiport(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If true, clear the table at initialization of the model.
     *  This is a boolean that defaults to false.
     */
    public Parameter clear;

    /** Name of the DatabaseManager to use.
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;

    /** Name of the table to set.
     *  This defaults to "v_people".
     */
    public StringParameter table;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the table to contain all the rows in input array of records.
     *  @exception IllegalActionException If the database update fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            String databaseName = databaseManager.stringValue();
            DatabaseManager database = DatabaseManager.findDatabaseManager(
                    databaseName, this);

            String prefix = "insert into " + table.stringValue() + " (";
            ArrayToken inputArray = (ArrayToken) input.get(0);
            for (int i = 0; i < inputArray.length(); i++) {
                StringBuffer columnNames = new StringBuffer();
                StringBuffer values = new StringBuffer();
                RecordToken row = (RecordToken) inputArray.getElement(i);
                Set<String> columns = row.labelSet();
                for (String column : columns) {
                    if (columnNames.length() != 0) {
                        columnNames.append(", ");
                        values.append(", ");
                    }
                    columnNames.append(column);
                    values.append(row.get(column).toString());
                }
                String sql = prefix + columnNames.toString() + ") values ("
                        + values.toString() + ")";
                if (_debugging) {
                    _debug("Issuing statement:\n" + sql);
                }
                // It would be nice to have an option to not commit the
                // transaction until wrapup, but, sadly, this doens't
                // work, at least not with MySQL. So we have to
                // commit each time.
                database.execute(sql);
            }
        }
    }

    /** Clear the specified table if the <i>clear</i> parameter is true.
     *  @exception IllegalActionException If the database query fails.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (((BooleanToken) clear.getToken()).booleanValue()) {
            String databaseName = databaseManager.stringValue();
            DatabaseManager database = DatabaseManager.findDatabaseManager(
                    databaseName, this);
            String query = "delete from " + table.stringValue() + ";";
            if (_debugging) {
                _debug("Issuing statement:\n" + query);
            }
            // It would be nice to have an option to not commit the
            // transaction until wrapup, but, sadly, this doens't
            // work, at least not with MySQL. So we have to
            // commit each time.
            database.execute(query);
        }
    }
}

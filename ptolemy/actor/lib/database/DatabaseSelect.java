/* Base class for simple source actors.

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

import java.util.Iterator;

import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DatabaseSelect

/**
 Select the columns from rows that match the specified pattern via the specified
 database manager. The output is an array of records, one for each row that
 matches the pattern. In each record, there is field for each column,
 where the name of the field is the name of the column and the value
 is the value from the matching row. If no rows match the specified
 pattern, then the output is an empty array of the appropriate type.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseSelect extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseSelect(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        columns = new PortParameter(this, "columns");
        // Require that the columns be a record token.
        columns.setTypeAtMost(BaseType.RECORD);
        // Set the default value.
        columns.setExpression("{lname=string, deskno=string}");

        pattern = new PortParameter(this, "pattern");
        pattern.setStringMode(true);
        pattern.setTypeEquals(BaseType.STRING);
        // Set the default value.
        pattern.setExpression("trim(room)='545Q' and trim(bldg)='Cory'");

        distinct = new Parameter(this, "distinct");
        distinct.setExpression("false");
        distinct.setTypeEquals(BaseType.BOOLEAN);

        orderBy = new StringParameter(this, "orderBy");

        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");

        output.setTypeAtLeast(ArrayType.arrayOf(columns));

        table = new StringParameter(this, "table");
        table.setExpression("v_spaces");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A record indicating what to query for.
     *  The names of the fields are the names of the columns to
     *  retrieve from the database, and the value of the field is
     *  the type. This is a record that defaults to
     *  {LNAME=string, DESKNO=string}, indicating that two columns,
     *  LNAME (for last name) and DESKNO (for desk number) should
     *  be retrieved from the database.
     */
    public PortParameter columns;

    /** Name of the DatabaseManager to use.
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;

    /** Indicator of whether to return only distinct records.
     *  This is a boolean that defaults to false.
     */
    public Parameter distinct;

    /** Optional ordering of the results.
     *  For example, to order first by DESKNO (ascending)
     *  and then by LNAME (descending), you would change
     *  the value of this parameter to
     *  "DESKNO asc, LNAME desc".
     *  This parameter is a string that defaults to empty,
     *  meaning that the ordering of the results is arbitrary.
     */
    public StringParameter orderBy;

    /** A pattern specifying which rows to select from the database.
     *  Any pattern understood in the 'where' clause of an SQL
     *  statement is acceptable. This is a string that defaults to
     *  "trim(room)='545Q' and trim(bldg)='Cory'", indicating that the retrieved
     *  records should have "Cory" in the bldg column and "545Q"
     *  in the room column.
     */
    public PortParameter pattern;

    /** Table to use within the database.
     *  This is a string that defaults to "v_spaces".
     */
    public StringParameter table;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DatabaseSelect newObject = (DatabaseSelect) super.clone(workspace);

        try {
            newObject.output.setTypeAtLeast(ArrayType
                    .arrayOf(newObject.columns));
        } catch (IllegalActionException ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }
        return newObject;
    }

    /** Perform the query on the database and produce the result
     *  on the output port.
     *  @exception IllegalActionException If the database query fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        columns.update();
        pattern.update();

        String databaseName = databaseManager.stringValue();
        DatabaseManager database = DatabaseManager.findDatabaseManager(
                databaseName, this);
        // Prepare query.
        // Construct a SQL query from the specified parameters.
        StringBuffer sqlQuery = new StringBuffer();
        sqlQuery.append("select ");
        if (((BooleanToken) distinct.getToken()).booleanValue()) {
            sqlQuery.append("distinct ");
        }
        RecordToken columnValue = (RecordToken) columns.getToken();
        Iterator<String> columnEntries = columnValue.labelSet().iterator();
        int i = 0;
        while (columnEntries.hasNext()) {
            if (i++ > 0) {
                sqlQuery.append(", ");
            }
            String label = columnEntries.next();
            sqlQuery.append(label);
        }
        sqlQuery.append(" from ");
        sqlQuery.append(table.stringValue());
        sqlQuery.append(" where ");
        sqlQuery.append(((StringToken) pattern.getToken()).stringValue());

        String orderByValue = orderBy.stringValue();
        if (!orderByValue.trim().equals("")) {
            sqlQuery.append(" order by ");
            sqlQuery.append(orderByValue);
        }
        String query = sqlQuery.toString();
        if (_debugging) {
            _debug("Issuing query:\n" + query);
        }
        ArrayToken result = database.executeQuery(query);
        if (result != null) {
            if (_debugging) {
                _debug("Result of query:\n" + result);
            }
            output.send(0, result);
        }
    }
}

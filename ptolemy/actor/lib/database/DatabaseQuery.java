/* Base class for simple source actors.

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
package ptolemy.actor.lib.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DatabaseQuery

/**
 Issue a database query via the specified
 database manager. The output is an array of records, one for each row
 in the returned result.

 @author Edward A. Lee
 @version $Id: DatabaseQuery.java 49830 2008-06-15 20:18:03Z eal $
 @since Ptolemy II 0.3
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseQuery extends Source {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseQuery(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        
        query = new PortParameter(this, "query");
        query.setStringMode(true);
        query.setTypeEquals(BaseType.STRING);
        query.setExpression("select * from desks");

        databaseManager = new StringParameter(this, "databaseManager");
        databaseManager.setExpression("DatabaseManager");

        // Constrain the output type to be a record type with
        // unspecified fields.
        // NOTE: The output is actually a subtype of this.
        // This is OK because lossless conversion occurs at the
        // output, which (as of 6/19/08) leaves the record unchanged.
        output.setTypeEquals(new ArrayType(BaseType.RECORD));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
   
    /** Name of the DatabaseManager to use. 
     *  This defaults to "DatabaseManager".
     */
    public StringParameter databaseManager;
    
    /** An SQL query. This is a string that defaults to
     *  "trim(room)='545Q' and trim(bldg)='Cory'", indicating that the retrieved
     *  records should have "Cory" in the bldg column and "545Q"
     *  in the room column.
     */
    public PortParameter query;

    /** Table to use within the database.
     *  This is a string that defaults to "v_spaces".
     */
    public StringParameter table;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Perform the query on the database and produce the result
     *  on the output port.
     *  @throws IllegalActionException If the database query fails.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        query.update();

        String databaseName = databaseManager.stringValue();
        CompositeActor container = (CompositeActor)getContainer();
        NamedObj database = container.getEntity(databaseName);
        while (!(database instanceof DatabaseManager)) {
            // Work recursively up the tree.
            container = (CompositeActor)container.getContainer();
            if (container == null) {
                throw new IllegalActionException(this,
                    "Cannot find database manager named " + databaseName);
            }
            database = container.getEntity(databaseName);
        }
        PreparedStatement statement = null;
        ArrayList<RecordToken> matches = new ArrayList<RecordToken>();
        try {
            Connection connection = ((DatabaseManager)database).getConnection();
            // If there is no connection, return without producing a token.
            if (connection == null) {
                return;
            }
            statement = connection.prepareStatement(
                    ((StringToken)query.getToken()).stringValue());
            
            // Perform the query.
            ResultSet rset = statement.executeQuery();
            ResultSetMetaData metaData = rset.getMetaData();
            int columnCount = metaData.getColumnCount();
            // For each matching row, construct a record token.
            while (rset.next()) {
                HashMap<String,Token> map = new HashMap<String,Token>();
                for (int c = 1; c <= columnCount; c++) {
                    String columnName = metaData.getColumnName(c);
                    String value = rset.getString(c);
                    if (value == null) {
                        value = "";
                    }
                    map.put(columnName, new StringToken(value));
                }
                matches.add(new RecordToken(map));
            }
        } catch (SQLException e) {
            throw new IllegalActionException(this, e,
                    "Failed to update room from database.");
        }
        int numberOfMatches = matches.size();
        ArrayToken result;
        if (numberOfMatches == 0) {
            // There are no matches.
            // Output an empty array of empty records.
            result = new ArrayToken(BaseType.RECORD);
        } else {
            RecordToken[] array = new RecordToken[numberOfMatches];
            int k = 0;
            for (RecordToken recordToken : matches) {
                array[k++] = recordToken;
            }
            result = new ArrayToken(array);
        }
        if(_debugging) {
            _debug("Result of query:\n" + result);
        }
        output.send(0, result);
    }
}

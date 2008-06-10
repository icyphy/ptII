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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.Source;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// Select

/**
 Select the columns from rows that match the specified pattern via the specified
 database manager. The output is an array of records, one for each row that
 matches the pattern. In each record, there is field for each column,
 where the name of the field is the name of the column and the value
 is the value from the matching row. If no rows match the specified
 pattern, then the output is an empty array of the appropriate type.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
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
        String[] labels = new String[0];
        Type[] types = new Type[0];
        RecordType declaredType = new RecordType(labels, types);
        columns.setTypeAtMost(declaredType);
        // Set the default value.
        columns.setExpression("{lname=string, deskno=int}");
        
        pattern = new PortParameter(this, "pattern");
        // Require that the pattern be a record token.
        pattern.setTypeAtMost(declaredType);
        // Set the default value.
        pattern.setExpression("{room=\"545Q\", bldg=\"Cory\"}");

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
    
    /** A record indicating what pattern to match in the database.
     *  The names of the fields are the names of the columns to
     *  match in the database, and the value of the field is
     *  the pattern to match. This is a record that defaults to
     *  {BLDG="Cory", ROOM="545Q"}, indicating that the retrieved
     *  records should have "Cory" in the BLDG column and "545Q"
     *  in the ROOM column.
     */
    public PortParameter pattern;

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
        columns.update();
        pattern.update();

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
        // Prepare query.
        // FIXME: This should have more fields and should be parameterized.
        /* Relevant column names:
BLDG            CHAR(10)   
ROOM            VARCHAR2(10)
SPACEID         NUMBER(6)  
DESKNO          NUMBER(3)  
PERSONID        NUMBER(6)  
LNAME           VARCHAR2(25)
FNAMES          VARCHAR2(25)
SPONSORID       NUMBER(6)  
SPONSORLNAME    VARCHAR2(25)
SPONSORFNAMES   VARCHAR2(25)
         */
        // Construct a SQL query from the specified parameters.
        StringBuffer sqlQuery = new StringBuffer();
        sqlQuery.append("select ");
        RecordToken columnValue = (RecordToken)columns.getToken();
        Iterator columnEntries = columnValue.labelSet().iterator();
        int i = 0;
        while (columnEntries.hasNext()) {
            if (i++ > 0) {
                sqlQuery.append(", ");
            }
            String label = (String)columnEntries.next();
            sqlQuery.append(label);            
        }
        sqlQuery.append(" from ");
        sqlQuery.append(table.stringValue());
        sqlQuery.append(" where ");

        RecordToken patternValue = (RecordToken)pattern.getToken();
        Iterator patternEntries = patternValue.labelSet().iterator();
        i = 0;
        while (patternEntries.hasNext()) {
            if (i++ > 0) {
                sqlQuery.append(" and ");
            }
            String label = (String)patternEntries.next();
            sqlQuery.append("trim(");
            sqlQuery.append(label);
            sqlQuery.append(") = ?");
        }

        PreparedStatement statement = null;
        ArrayList<RecordToken> matches = new ArrayList<RecordToken>();
        try {
            Connection connection = ((DatabaseManager)database).getConnection();
            // If there is no connection, return without producing a token.
            if (connection == null) {
                return;
            }
            // FIXME: We could prepare the statement once and re-use it for multiple
            // queries. This would presumably be more efficient. This would need to
            // redone whenever the parameters changed.
            String query = sqlQuery.toString();
            statement = connection.prepareStatement(query);
            
            patternEntries = patternValue.labelSet().iterator();
            i = 1;
            while (patternEntries.hasNext()) {
                String label = (String)patternEntries.next();
                Token value = patternValue.get(label);
                // A StringToken will have spurious quotation marks around it,
                // so we need to remove those.
                if (value instanceof StringToken) {
                    statement.setString(i++, ((StringToken)value).stringValue());
                } else {
                    statement.setString(i++, value.toString());
                }
            }
            // Perform the query.
            ResultSet rset = statement.executeQuery();
            
            // For each matching row, construct a record token.
            while (rset.next()) {
                HashMap<String,Token> map = new HashMap<String,Token>();
                columnEntries = columnValue.labelSet().iterator();
                while (columnEntries.hasNext()) {
                    String label = (String)columnEntries.next();
                    String value = rset.getString(label);
                    if (value == null) {
                        value = "";
                    }
                    map.put(label, new StringToken(value));
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
            // Output an empty record.
            result = new ArrayToken(columns.getToken().getType());
        } else {
            RecordToken[] array = new RecordToken[numberOfMatches];
            int k = 0;
            for (RecordToken recordToken : matches) {
                array[k++] = recordToken;
            }
            result = new ArrayToken(array);
        }
        output.send(0, result);
    }
}

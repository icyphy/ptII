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

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.ArrayToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// DatabaseManager

/**
 A DatabaseManager. When preinitialized, this actor opens a connection
 to the specified database. When wrapup() is called, it closes the
 connection. A user of this class can also separately call getConnection()
 to open a connection, but then that user must also call closeConnection()
 when finished.
 <p>
 This class polls all available JDBC database drivers until one is willing
 to open the string given by the <i>database</i> parameter. Depending
 on the driver, this string may be a URL. Some database drivers are
 packaged with Ptolemy II and available from a vergil process. You can
 make others available by setting the jdbc.drivers property. For
 example, when invoking vergil, do something like this:
 <pre>
  export JAVAFLAGS=-Djdbc.drivers=com.mysql.jdbc.Driver:bad.tast.ourDriver
  $PTII/bin/vergil -verbose
 </pre>
 The above lists two drivers, in order of preference, separated by
 a ":". More information can be found at
 <a href="http://download.oracle.com/javase/6/docs/api/java/sql/DriverManager.html#in_browser">
 http://download.oracle.com/javase/6/docs/api/java/sql/DriverManager.html</a>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class DatabaseManager extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DatabaseManager(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        database = new StringParameter(this, "database");
        // Default database is the EECS database at Berkeley.
        // NOTE: the server and database name ("acgeecs")
        // are going to change in summer 2008...
        database.setExpression("jdbc:oracle:thin:@buffy.eecs.berkeley.edu:1521:acgeecs");

        passwordFile = new FileParameter(this, "passwordFile");
        passwordFile.setExpression("");

        userName = new StringParameter(this, "userName");
        userName.setExpression("ptolemy");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** JDBC connection string to access the database.
     *  This always starts with "jdbc:driver", where the specific
     *  driver chosen determines how to interpret fields in the
     *  rest of the string.  For example,
     *  <pre>
     *    jdbc:oracle:thin:@buffy.eecs.berkeley.edu:1521:acgeecs
     *  </pre>
     *  specifies a thin client for an Oracle database located
     *  at host buffy.eecs.berkeley.edu, which listens on port
     *  1521. The "acgeecs" is the database name.
     *  Another example is
     *  <pre>
     *    jdbc:mysql://localhost:3306/space
     *  </pre>
     *  which specifies a MySQL database on the local host,
     *  where "space" is the name of the database.
     */
    public StringParameter database;

    /** The file that contains the password.  If this parameter is
     *  non-empty, then it is assumed to refer to a file that contains
     *  the password.  If this parameter is empty, or names a file
     *  that cannot be read, then a dialog is displayed for the user
     *  to enter the password.  It is up to the user to properly
     *  protect the file from unauthorized readers by using the file
     *  system permissions.  The default value is the empty string,
     *  meaning that the dialog will be displayed.
     */
    public FileParameter passwordFile;

    /** User name. */
    public StringParameter userName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  In this class,
     *  if the database or userName is changed, this method closes the
     *  connection with the database and forgets the password.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == database || attribute == userName) {
            closeConnection();
            _password = null;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Close the connection to the database, if one is open.
     *  @exception IllegalActionException If closing the connection fails.
     */
    public void closeConnection() throws IllegalActionException {
        // If updating the database, need to commit or roll back here.
        if (_connection != null) {
            try {
                _connection.close();
            } catch (SQLException e) {
                throw new IllegalActionException(this, e,
                        "Failed to close the database connection.");
            }
            _connection = null;
        }
    }

    /** Execute the specified SQL statement and return the result as
     *  a string.
     *  Note that if there is no connection to the database, this
     *  will open one. The caller is responsible for calling
     *  closeConnection() after this.
     *  @param sql The query.
     *  @return The result as a string.
     *  @exception IllegalActionException If the statement fails.
     */
    public String execute(String sql) throws IllegalActionException {
        PreparedStatement statement = null;
        Connection connection = getConnection();
        try {
            // If there is no connection, return -1.
            // This should only occur if the user cancels.
            if (connection == null) {
                return "No database connection.";
            }
            connection.setAutoCommit(false); //use transaction!
            // FIXME: Findbugs: SQL A prepared statement is generated from a nonconstant String
            // It would be nice if we offered a statement that takes a prepared
            // statement as input.

            statement = connection.prepareStatement(sql);
            boolean result = statement.execute();
            // Get all the results into a string.
            // According to the docs, there are no more results when
            // ((statement.getMoreResults() == false) && (statement.getUpdateCount() == -1))
            StringBuffer resultString = new StringBuffer();
            while (true) {
                if (result) {
                    ResultSet resultSet = statement.getResultSet();
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    List<String[]> rows = new LinkedList<String[]>();

                    // First, put in the result table the column names
                    String[] columnNames = new String[columnCount];
                    int[] columnWidths = new int[columnCount];
                    for (int c = 0; c < columnCount; c++) {
                        String columnName = metaData.getColumnName(c + 1);
                        columnWidths[c] = columnName.length() + 1;
                        columnNames[c] = columnName;
                    }
                    rows.add(columnNames);

                    // Next add each of the rows.
                    while (resultSet.next()) {
                        String[] row = new String[columnCount];
                        for (int c = 0; c < columnCount; c++) {
                            String value = resultSet.getString(c + 1);
                            if (value == null) {
                                value = "NULL";
                            }
                            row[c] = value;
                            if (value.length() > columnWidths[c]) {
                                columnWidths[c] = value.length();
                            }
                        }
                        rows.add(row);
                    }
                    // Finally, pretty print the table.
                    for (String[] row : rows) {
                        for (int c = 0; c < columnCount; c++) {
                            resultString.append(row[c]);
                            for (int i = 0; i <= columnWidths[c]
                                    - row[c].length(); i++) {
                                resultString.append(" ");
                            }
                        }
                        resultString.append("\n");
                    }
                } else {
                    int count = statement.getUpdateCount();
                    if (count != -1) {
                        resultString.append("Statement OK. " + count
                                + " rows affected.");
                        resultString.append("\n");
                    } else {
                        connection.commit();
                        return resultString.toString();
                    }
                }
                result = statement.getMoreResults();
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                // Not much to do here.
            }
            // Send the error message to the output.
            return "Error:\n" + e.getMessage();
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e1) {
                    // Not much to do here.
                }
            }
        }
    }

    /** Execute the SQL query given in the specified string
     *  and return an array of record tokens containing the results.
     *  Note that if there is no connection to the database, this
     *  will open one. The caller is responsible for calling
     *  closeConnection() after this.
     *  @param sql The query.
     *  @return An array of record tokens containing the results,
     *   which may be empty (zero length), or null if the connection
     *   fails or is canceled.
     *  @exception IllegalActionException If the query fails.
     */
    public ArrayToken executeQuery(String sql) throws IllegalActionException {
        PreparedStatement statement = null;
        ResultSet rset = null;
        ArrayList<RecordToken> matches = new ArrayList<RecordToken>();
        try {
            Connection connection = getConnection();
            // If there is no connection, return without producing a token.
            if (connection == null) {
                return null;
            }
            // FIXME: Findbugs: SQL A prepared statement is generated from a nonconstant String
            // It would be nice if we offered a statement that takes a prepared
            // statement as input.
            statement = connection.prepareStatement(sql);

            // Perform the query.
            rset = statement.executeQuery();
            ResultSetMetaData metaData = rset.getMetaData();
            int columnCount = metaData.getColumnCount();
            // For each matching row, construct a record token.
            while (rset.next()) {
                HashMap<String, Token> map = new HashMap<String, Token>();
                for (int c = 1; c <= columnCount; c++) {
                    String columnName = StringUtilities.sanitizeName(metaData
                            .getColumnName(c));
                    String value = rset.getString(c);
                    if (value == null) {
                        value = "";
                    }
                    map.put(columnName, new StringToken(value));
                }
                matches.add(new RecordToken(map));
            }
        } catch (SQLException e) {
            throw new IllegalActionException(this, e, "Database query failed.");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e1) {
                    // Not much to do here.
                }
            }
            if (rset != null) {
                try {
                    rset.close();
                } catch (SQLException e1) {
                    // Not much to do here.
                }
            }

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
        return result;
    }

    /** Execute the SQL update given in the specified string
     *  and return the number of affected rows or zero if the update
     *  does not return anything.
     *  Note that if there is no connection to the database, this
     *  will open one. The caller is responsible for calling
     *  closeConnection() after this.
     *  @param sql The query.
     *  @param expectedResult If a non-negative number is given here, then
     *   the update is not committed unless the result matches.
     *  @return The number of rows affected or 0 if the update
     *   does not return a value, or -1 if the connection is canceled.
     *  @exception IllegalActionException If the query fails or if the
     *   result does not match the value of <i>expectedResult</i>.
     */
    public int executeUpdate(String sql, int expectedResult)
            throws IllegalActionException {
        PreparedStatement statement = null;
        Connection connection = getConnection();
        try {
            // If there is no connection, return -1.
            // This should only occur if the user cancels.
            if (connection == null) {
                return -1;
            }
            connection.setAutoCommit(false); //use transaction!
            // FIXME: Findbugs: SQL A prepared statement is generated from a nonconstant String
            // It would be nice if we offered a statement that takes a prepared
            // statement as input.
            statement = connection.prepareStatement(sql);
            int result = statement.executeUpdate();
            if (expectedResult >= 0 && result != expectedResult) {
                throw new IllegalActionException(this, "Update affected "
                        + result + " rows, but should have affected "
                        + expectedResult);
            }
            connection.commit();
            return result;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                // Not much we can do here...
            }
            throw new IllegalActionException(this, e, "Update failed.");
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e1) {
                    // Not much to do here.
                }
            }
        }
    }

    /** Find a database manager with the specified name for the specified
     *  actor.
     *  @param name Database manager name.
     *  @param actor The actor.
     *  @return A database manager.
     *  @exception IllegalActionException If no database manager is found.
     */
    public static DatabaseManager findDatabaseManager(String name,
            NamedObj actor) throws IllegalActionException {
        CompositeActor container = (CompositeActor) actor.getContainer();
        NamedObj database = container.getEntity(name);
        while (!(database instanceof DatabaseManager)) {
            // Work recursively up the tree.
            container = (CompositeActor) container.getContainer();
            if (container == null) {
                throw new IllegalActionException(actor,
                        "Cannot find database manager named " + name);
            }
            database = container.getEntity(name);
        }
        return (DatabaseManager) database;
    }

    /** Get a connection to the database.
     *  If one is already open, then simply return that one.
     *  Otherwise, use the parameter values and prompt for a password to
     *  open a new connection.
     *  @return A connection to the database, or null if the user cancels.
     *  @exception IllegalActionException If the password file cannot
     *  be read and "ptolemy.ptII.isRunningNightlyBuild" property
     *  exists and is not empty or if the "ptolemy.ptII.batchMode"
     *  property exists and is not empty and the property
     *  "ptolemyII.ptII.testingMessageHandler" is not set.
     */
    public Connection getConnection() throws IllegalActionException {
        if (_connection != null) {
            return _connection;
        }
        if (_password == null) {
            if (passwordFile.stringValue().length() > 0) {
                // Read the password from a file.
                BufferedReader reader = null;
                try {
                    reader = passwordFile.openForReading();
                    String line = reader.readLine();
                    if (line != null) {
                        _password = line.toCharArray();
                        line = "";
                    } else {
                        throw new NullPointerException(
                                "Failed to read a line from " + passwordFile);
                    }
                } catch (Exception ex) {
                    if (MessageHandler.isRunningNightlyBuild()) {
                        throw new IllegalActionException(this, ex, "Failed to read "
                                + passwordFile.stringValue());
                    } else {
                        System.out.println(getFullName() + ": Failed to read "
                                + passwordFile.stringValue() + ex);
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            throw new IllegalActionException(this, ex,
                                    "Failed to close "
                                            + passwordFile.stringValue());
                        }
                    }
                }
            }
            if (_password == null) {
                // Open a dialog to get the password.
                // First find a frame to "own" the dialog.
                // Note that if we run in an applet, there may
                // not be an effigy.
                Effigy effigy = Configuration.findEffigy(toplevel());
                JFrame frame = null;
                if (effigy != null) {
                    Tableau tableau = effigy.showTableaux();
                    if (tableau != null) {
                        frame = tableau.getFrame();
                    }
                }

                // Next construct a query for user name and password.
                Query query = new Query();
                query.setTextWidth(60);
                query.addLine("database", "Database", database.stringValue());
                query.addLine("userName", "User name", userName.stringValue());
                query.addPassword("password", "Password", "");
                ComponentDialog dialog = new ComponentDialog(frame,
                        "Open Connection", query);

                if (dialog.buttonPressed().equals("OK")) {
                    // Update the parameter values.
                    database.setExpression(query.getStringValue("database"));
                    userName.setExpression(query.getStringValue("userName"));

                    // The password is not stored as a parameter.
                    _password = query.getCharArrayValue("password");
                } else {
                    return null;
                }
            }
        }
        if (_connection == null) {
            // Get database connection.
            try {
                _connection = DriverManager.getConnection(
                        database.getExpression(), userName.getExpression(),
                        new String(_password));
                // If updating, use single transaction.
                _connection.setAutoCommit(false);
            } catch (SQLException ex) {
                try {
                    // The following blocks explicitly register drivers.
                    // But maybe these aren't necessary if the driver is
                    // in the classpath?  If the model fails to find the
                    // drivers, then try adding the jar files to the
                    // build path in Eclipse.

                    // Try the mysql driver
                    DriverManager.registerDriver((java.sql.Driver) Class
                            .forName("com.mysql.jdbc.Driver").newInstance());
                    _connection = DriverManager.getConnection(
                            database.getExpression(), userName.getExpression(),
                            new String(_password));
                    // If updating, use single transaction.
                    _connection.setAutoCommit(false);
                } catch (Throwable throwable) {
                    // Try the Oracle driver
                    try {
                        DriverManager.registerDriver((java.sql.Driver) Class
                                .forName("oracle.jdbc.OracleDriver")
                                .newInstance());
                        _connection = DriverManager
                                .getConnection(database.getExpression(),
                                        userName.getExpression(), new String(
                                                _password));
                        // If updating, use single transaction.
                        _connection.setAutoCommit(false);
                    } catch (Throwable throwable2) {
                        _password = null;
                        // Note that we throw the original exception.
                        throw new IllegalActionException(this, ex,
                                "Failed to open connection to the database.");
                    }
                }
            }
        }

        return _connection;
    }

    /** Open a connection to the database, if one is not already open,
     *  prompting the user for a password. If the user declines to
     *  provide the password (by clicking Cancel on the dialog box),
     *  then request that execution be stopped.
     *  @exception IllegalActionException If opening the database fails.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        getConnection();
        if (_connection == null) {
            getDirector().stop();
        }
    }

    /** Close the connection to the database, if it is open.
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it, or if we fail to
     *   close the database connection.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        try {
            super.wrapup();
        } finally {
            closeConnection();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The currently open connection. */
    private Connection _connection;

    /** The password last entered. */
    private char[] _password;
}

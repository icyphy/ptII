/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.common.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.SetupParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.kernel.database.AsynchronousDBConnection;
import ptdb.kernel.database.DBConnection;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
//// DBConnectorFactory
/**
 * A factory class that creates different types of XML database connections.
 *
 * @author Ashwini Bijwe

 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class DBConnectorFactory {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /**
     * Name for the property for XML database class
     * in the config/ptdb-properties file.
     */
    public final static String _DB_CLASS_NAME = "DB_Class_Name";

    /**
     * Name for the property for XML database URL/Location
     * in the config/ptdb-properties file.
     */
    public final static String _DB_URL = "DB_Url";

    /**
     * Name for the property for XML database container name
     * in the config/ptdb-properties file.
     */
    public final static String _XMLDB_CONTAINER_NAME = "DB_Container_Name";

    /**
     * Name for the property for PtolemyDB cache container
     * name in the config/ptdb-properties file.
     */
    public final static String _CACHE_CONTAINER_NAME = "Cache_Container_Name";

    /**
     * Path to the configuration file.
     */
    public final static String _PROPERTIES_FILE_PATH = "$CLASSPATH/ptdb/config/ptdb-params.properties";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * create a setup parameter object that contains the database setup parameters
     * and return that setup parameter object to the caller.
     *
     * @return The setup parameter object.
     */
    public static SetupParameters getSetupParameters() {

        if (isSetupDone() == true) {

            SetupParameters setupParameters = new SetupParameters(_dbUrl,
                    _dbContainerName, _cacheContainerName);

            return setupParameters;

        } else {
            return null;
        }
    }

    /**
     * This API is used to get a synchronous connection to the database.
     * A synchronous connection is a connection that
     * executes the query as soon as the executeQuery is called on the connection.
     * A synchronous connection supports both execution of queries
     * with transaction and without transactions.
     *
     * @param isTransactionRequired - Boolean to specify whether
     * this connection needs a transaction or not.
     * @return DBConnection - The executes queries as
     * and when the execute query is called.
     * @exception DBConnectionException - Whenever we face a problem while
     * creating a database connection. These problems could be that
     * configured connection class does not exist, the path for the
     * database is not found, the container name is incorrect,
     * the connection to the database could not be established etc.
     */
    public static DBConnection getSyncConnection(boolean isTransactionRequired)
            throws DBConnectionException {
        return _createConnection(_dbContainerName, isTransactionRequired);
    }

    /**
     * This API is used to get a synchronous connection to the database. A
     * synchronous connection is a connection that executes the query as soon as
     * the executeQuery is called on the connection. A synchronous connection
     * supports both execution of queries with transaction and without
     * transactions.
     *
     * @param dbConnectionParameters Connection parameters for creating the
     * connection.
     * @return DBConnection To execute queries as and when the execute query
     * is called.
     * @exception DBConnectionException Whenever we face a problem while
     * creating a database connection. These problems could be that configured
     * connection class does not exist, the path for the database is not found,
     * the container name is incorrect, the connection to the database could not
     * be established etc.
     */
    public static DBConnection getSyncConnection(
            DBConnectionParameters dbConnectionParameters)
            throws DBConnectionException {
        return _createConnection(dbConnectionParameters);
    }

    /**
     * Get an asynchronous connection to the database.
     *
     * An asynchronous connection is a connection that
     * enqueues the query as soon as the executeQuery is
     * called on the connection and executes them asynchronously.
     *
     * An asynchronous connection should be used for the execution
     * of queries that require a transaction and do not return
     * data from the database.
     *
     * @return DBConnection - That enqueues queries as and when
     * the execute query is called and executes them asynchronously.
     *
     * @exception DBConnectionException - Whenever we face a problem while creating
     * a database connection. These problems could be that configured connection
     * class does not exist, the path for the database is not found, the container name
     * is incorrect, the connection to the database could not be established etc.
     */
    public static DBConnection getAsyncConnection()
            throws DBConnectionException {
        return new AsynchronousDBConnection();
    }

    /**
     * Get a synchronous connection to the cache database.
     * A synchronous connection is a connection that executes the query
     * as soon as the executeQuery is called on the connection.
     * A synchronous connection supports both execution of queries
     * with transaction and without transactions.
     *
     * @param isTransactionRequired - Boolean to specify whether this connection
     * needs a transaction or not.
     *
     * @return DBConnection - That executes queries as and
     * when the execute query is called.
     *
     * @exception DBConnectionException - Whenever we face a problem while creating
     * a database connection. These problems could be that configured connection
     * class does not exist, the path for the database is not found,
     * the container name is incorrect,
     * the connection to the database could not be established etc.
     */
    public static DBConnection getCacheConnection(boolean isTransactionRequired)
            throws DBConnectionException {
        return _createConnection(_cacheContainerName, isTransactionRequired);
    }

    /**
     * Return the database connection parameters.
     * @return Database connection parameters like URL and container name.
     */
    public static DBConnectionParameters getDBConnectionParameters() {
        DBConnectionParameters connectionParameters = new DBConnectionParameters(
                _dbUrl, _dbContainerName, false);
        return connectionParameters;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     *
     * Create a connection based on given parameters.
     * @param containerName Container to connect to.
     * @param isTransactionRequired True if transaction is needed, false otherwise.
     * @return Required connection object
     * @exception DBConnectionException When there is an error in connecting to the database.
     */
    private static DBConnection _createConnection(String containerName,
            boolean isTransactionRequired) throws DBConnectionException {

        if (!isSetupDone()) {
            throw new DBConnectionException(
                    "XML Database Connection is not configured. "
                            + "Please provide details in ptdb-params.properties");
        }

        DBConnectionParameters dbConnParams = new DBConnectionParameters(
                _dbUrl, containerName, isTransactionRequired);

        return _createConnection(dbConnParams);

    }

    /**
     * Create a connection based on given parameters.
     * @param dbConnectionParameters Connection parameters for creating the connection.
     * @return Required connection object
     * @exception DBConnectionException When there is an error in connecting
     * to the database.
     */
    private static DBConnection _createConnection(
            DBConnectionParameters dbConnectionParameters)
            throws DBConnectionException {

        if (_dbClassName == null) {
            throw new DBConnectionException(
                    "DBConnection class for ptdb is undefined. "
                            + "Please provide valid classname in ptdb-params.properties");
        }

        DBConnection xmlDBConnection = null;
        try {

            Class xmlDBClass = Class.forName(_dbClassName);
            Class[] parameterTypes = new Class[1];
            parameterTypes[0] = DBConnectionParameters.class;
            Constructor<DBConnection> xmlDBConstructor = xmlDBClass
                    .getConstructor(parameterTypes);

            xmlDBConnection = xmlDBConstructor
                    .newInstance(dbConnectionParameters);

        } catch (ClassNotFoundException e) {

            throw new DBConnectionException(
                    "PTDB database connector class not "
                            + "specified in ptdb-params.properties", e);

        } catch (SecurityException e) {

            throw new DBConnectionException("PTDB security exception - "
                    + e.getMessage(), e);

        } catch (NoSuchMethodException e) {

            throw new DBConnectionException("PTDB database connector class "
                    + _dbClassName + " does not have "
                    + "the constructor DBConnection(DBConnectionParameters p)",
                    e);

        } catch (IllegalArgumentException e) {

            throw new DBConnectionException(
                    "PTDB illegal argument exception - " + _dbClassName + " "
                            + e.getMessage(), e);

        } catch (InstantiationException e) {

            throw new DBConnectionException("PTDB instantiation exception - "
                    + _dbClassName + " " + e.getMessage(), e);

        } catch (IllegalAccessException e) {

            throw new DBConnectionException("PTDB illegal access exception - "
                    + _dbClassName + " " + e.getMessage(), e);

        } catch (InvocationTargetException e) {

            throw new DBConnectionException(
                    "PTDB invocation target exception - " + _dbClassName
                            + " for url - " + dbConnectionParameters.getUrl()
                            + " and container name - "
                            + dbConnectionParameters.getContainerName(), e);

        }
        return xmlDBConnection;

    }

    /**
     * Return true if the database setup is completed.
     * @return True if setup is completed; false otherwise.
     */
    public static boolean isSetupDone() {
        return _isDBSetupDone;
    }

    /**
     * Load the properties from config/ptdb.properties
     * and set them for use during creating connections
     * to the XML database.
     *
     * <p>Ascertain if the database setup has been done
     * and if the properties for the database have been set.</p>
     *
     * <p>Throw an exception if the config/ptdb.properties
     * file is not found.</p>
     *
     */
    public static void loadDBProperties() {

        String ptdbParams = "$CLASSPATH/ptdb/config/ptdb-params.properties";
        Properties props = new Properties();

        try {
            // Use FileUtilities.nameToURL() because it handles jar urls from Web Start.
            URL url = FileUtilities.nameToURL(ptdbParams, null, null);
            if (url == null) {
                throw new ExceptionInInitializerError("Did not find the "
                        + ptdbParams + " file.");
            }

            props.load(url.openStream());
            clearParameters();
            _dbClassName = props.getProperty(_DB_CLASS_NAME);
            _dbUrl = props.getProperty(_DB_URL);

            if (_dbUrl != null && !"".equals(_dbUrl)) {

                _dbContainerName = props.getProperty(_XMLDB_CONTAINER_NAME);
                _cacheContainerName = props.getProperty(_CACHE_CONTAINER_NAME);
                _isDBSetupDone = true;

                Class dbConnectionClass = Class.forName(_dbClassName);
                Method initializeDatabaseMethod = dbConnectionClass.getMethod(
                        "initializeDatabase", String.class);
                initializeDatabaseMethod.invoke(null, _dbUrl);
            }
        } catch (IOException ex) {
            //ExceptionInInitializerError exception = new ExceptionInInitializerError(
            //        "Did not find " + ptdbParams + " file.");
            throw new ExceptionInInitializerError(ex);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Return the parameters set as a concatenated string.
     * @return Concatenated String of database parameters -
     * class name, URL, container name.
     */
    public static String getParametersString() {
        StringBuffer strBuf = new StringBuffer();

        strBuf.append("_isDBSetupDone = ").append(_isDBSetupDone).append(";");
        strBuf.append("_dbClassName = ").append(_dbClassName).append(";");
        strBuf.append("_dbUrl = ").append(_dbUrl).append(";");
        strBuf.append("_dbContainerName = ").append(_dbContainerName)
                .append(";");
        strBuf.append("_cacheContainerName = ").append(_cacheContainerName)
                .append(";");

        return strBuf.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * Clear Parameters to maintain consistency.
     */
    private static void clearParameters() {
        _dbUrl = null;
        _dbClassName = null;
        _dbContainerName = null;
        _cacheContainerName = null;
        _isDBSetupDone = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * Container name for XML database cache for Ptolemy DB.
     * The name is stored in config/ptdb.properties
     * file as Cache_Container_Name Property.
     */
    private static String _cacheContainerName;

    /**
     * Class name for the class that will be used by the
     * database framework to manage connections and transaction
     * to the actual XML database.
     * The name is stored in config/ptdb.properties file as DB_Class_Name Property.
     * This class should be of type DBConnection.
     */
    private static String _dbClassName;

    /**
     * Container name for XML database Ptolemy DB.
     * The name is stored in config/ptdb.properties file
     * as DB_Container_Name Property.
     */
    private static String _dbContainerName;

    /** Path for the location where the XML database file is located.
     *  The name is stored in config/ptdb.properties file as DB_Url Property.
     */
    private static String _dbUrl;

    /**
     * This boolean is set when the database properties are
     * read from the config/ptdb.properties file.
     * This signifies if the database setup for Ptolemy XML
     * Database is done or not.
     *
     * Set to true if the setup is done.
     * Set to false if the setup is incomplete.
     *
     */
    private static boolean _isDBSetupDone;

    /**
     * Whenever the class is loaded, we load the properties from
     * config/ptdb.properties and set them for use during creating
     * connections to the XML database.
     *
     * This is also used to ascertain if the database setup has been
     * done and if the properties for the database have been set.
     *
     * If the config/ptdb.properties file is not found then it will
     * throw an exception.
     */
    static {
        try {
            loadDBProperties();
        } catch (ExceptionInInitializerError e) {

        }

    }
}

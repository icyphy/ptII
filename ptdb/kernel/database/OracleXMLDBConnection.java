package ptdb.kernel.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlTransaction;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

//////////////////////////////////////////////////////////////////////////
////OracleXMLDBConnection
/**
 * Create and manage the actual connection to the Oracle XML database. 
 * Manage the transactions for the database.
 * 
 * This is a wrapper class that acts as a <p> Facade </p> to the <p> Oracle Berkeley XML 
 * Database </p> interface.
 *  
 *     
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 */
public class OracleXMLDBConnection implements DBConnection {

    /**
    *  Construct an instance that initializes the environment 
    *  and creates a database connection 
    *  based on the given parameters.
    *   
    * @param dbConnParams - Encapsulate the parameters required to 
    *                       create a database connection  
    * @throws DBConnectionException - When the XML DB encounters 
    *                                 an error while creating a 
    *                                 connection
    */
    public OracleXMLDBConnection(DBConnectionParameters dbConnParams)
            throws DBConnectionException {

        try {

            _params = dbConnParams;

            EnvironmentConfig config = new EnvironmentConfig();
            config.setCacheSize(100 * 1024 * 1024); // 50MB
            config.setAllowCreate(true);
            config.setInitializeCache(true);
            config.setTransactional(true);
            config.setInitializeLocking(true);
            config.setInitializeLogging(true);
            config.setErrorStream(System.err);
            File dbFile = new File(_params.getUrl());

            _environment = new Environment(dbFile, config);

            _xmlManager = new XmlManager(_environment, null);

            _xmlContainer = _xmlManager.openContainer(_params
                    .getContainerName());

            if (_params.isTransactionRequired())
                _xmlTransaction = _xmlManager.createTransaction();

            isConnectionAlive = true;

        } catch (FileNotFoundException e) {

            throw new DBConnectionException(
                    "Exception while connecting to the database : "
                            + "Database not found at the given location : "
                            + _params.getUrl(), e);

        } catch (DatabaseException e) {

            throw new DBConnectionException(
                    "Exception while connecting to the database : "
                            + e.getMessage(), e);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Abort the connection and roll back the transaction
     * Invoke in case of errors.
     */
    public void abortConnection() throws DBConnectionException {
        try {
            checkConnectionAlive();
            if (_xmlTransaction != null)
                _xmlTransaction.abort();

            _cleanUp();

        } catch (XmlException e) {

            _cleanUp();
            throw new DBConnectionException(
                    "Database transaction could not be aborted - "
                            + e.getMessage(), e);

        } finally {
            isConnectionAlive = false;
        }

    }

    /** 
     * Commit the transaction and close the connection to 
     * the database.
     * Invoke in case of successful completion of processing.
     */
    public void closeConnection() throws DBConnectionException {
        try {
            checkConnectionAlive();
            if (_xmlTransaction != null)
                _xmlTransaction.commit();

            _cleanUp();

        } catch (XmlException e) {

            _cleanUp();
            throw new DBConnectionException(
                    "Database transaction could not be committed - "
                            + e.getMessage(), e);

        } finally {
            isConnectionAlive = false;
        }
    }

    /**
     * 
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException {
        // TODO Auto-generated method stub
        return null; 
    }
    /**
     * Provide information regarding the state of the internal variables
     * useful for unit testing purposes 
     */
    public String toString() {

        StringBuffer classState = new StringBuffer();
        if (_xmlManager != null)
            classState.append("_xmlManager:Initialized");
        else
            classState.append("_xmlManager:Not Initialized");

        if (_xmlTransaction != null)
            classState.append("_xmlTransaction:Initialized");
        else
            classState.append("_xmlTransaction:Not Initialized");

        return classState.toString();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /**
     * Check if the connection is alive 
     * @throws DBConnectionException - when the connection is not alive
     * 
     */
    private void checkConnectionAlive() throws DBConnectionException {
        if(!isConnectionAlive)
            throw new DBConnectionException("This connection is not alive anymore. It has been closed or aborted.");
    }
    /**
     * Close the environment and invoke delete on the container 
     * that closes the connection and releases the resources 
     * @throws DBConnectionException
     */
    private void _cleanUp() throws DBConnectionException {
        try {
            if (_xmlContainer != null)
                _xmlContainer.delete();

            if (_xmlTransaction != null)
                _xmlTransaction.delete();

            if (_environment != null)
                _environment.close();

        } catch (DatabaseException e) {

            throw new DBConnectionException(
                    "Database transaction could not be committed - "
                            + e.getMessage(), e);

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     *  The environment for the Oracle Berkeley XMLDatabase that defines the 
     *  parameters like cache size, locking mechanism, storing mechanism etc. 
     *  required for creating a database connection. 
     */
    private Environment _environment;

    /**
     * This object contains the parameters like path, container name etc. 
     * required to connect with the database 
     */
    private DBConnectionParameters _params;

    /**
     * This is the Oracle Berkeley XML DB Container that is used to 
     * add and remove documents. 
     */
    private XmlContainer _xmlContainer;

    /**
     * This is the Oracle BerkeleyXML DB manager that is used to execute 
     * queries over the opened container 
     */
    private XmlManager _xmlManager;

    /**
     * This is the Oracle Berkeley XML DB Transaction that is used 
     * to commit or abort certain transactions.
     * This will be set only if a transaction enabled 
     * connection is requested. 
     */
    private XmlTransaction _xmlTransaction;

    /**
     * Denote whether the database connection is active or not
     */
    private boolean isConnectionAlive;
}

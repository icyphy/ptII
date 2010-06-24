package ptdb.kernel.bl.setup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.SetupParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;
import ptdb.kernel.database.OracleXMLDBConnection;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////
////SetupManager

/**
 * Manage the setup parameters that is being passed to it from the GUI layer and
 * communicate that to the database layer as needed.
 * 
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 * 
 */

public class SetupManager {

    //////////////////////////////////////////////////////////////////////
    ////		public methods 				      ////

    /**
     * Return the existing database setup parameters. <p>Delegate this task to
     * DBConnectionFactory to perform it and return the value without any
     * modifications.</p>
     * 
     * @return The existing database setup parameters.
     */
    public SetupParameters getSetupParameters() {

        return DBConnectorFactory.getSetupParameters();
    }

    /**
     * Test the database connection with the parameters given.
     * @param params The parameters for the database connection.
     * @exception DBConnectionException Thrown if the connection fails.
     */
    public void testConnection(SetupParameters params)
            throws DBConnectionException {

        if (params == null) {
            throw new DBConnectionException("Failed to test the connection - "
                    + "The connection parameters passed is null");
        }

        String url = params.getUrl();
        String containerName = params.getContainerName();
        String cacheContainerName = params.getCacheContainerName();

        DBConnectionParameters dbMainConnParams = new DBConnectionParameters(
                url, containerName, false);

        DBConnectionParameters dbCacheConnParams = new DBConnectionParameters(
                url, cacheContainerName, false);
        
        DBConnection mainConnection = null;
        DBConnection cacheConnection = null;
        

        try {
            
            mainConnection = DBConnectorFactory.getSyncConnection(
                    dbMainConnParams);
            
            if (mainConnection == null) {
                throw new DBConnectionException(
                        "Failed to create connection with the following parameters: " 
                        + "\nURL: " + url
                        + "\nContainer Name: " + containerName);
            }
           
            cacheConnection = DBConnectorFactory.getSyncConnection(
                    dbCacheConnParams);
            

            if (cacheConnection == null) {
                throw new DBConnectionException(
                        "Failed to create connection with the following parameters: " 
                        + "\nURL: " + url
                        + "\nCache Container Name: " + cacheContainerName);
            }
           
        } catch (DBConnectionException e) {
            throw new DBConnectionException(
                    "Unable to create connection with the given parameters."
                            + " - " + e.getMessage(), e);
        } finally {
            if (mainConnection != null) {
                mainConnection.closeConnection();
            }
            
            if (cacheConnection != null) {
                cacheConnection.closeConnection();
            }
        }
    }

    /**
     * Update the existing database connection parameters with the given
     * parameters.
     * 
     * @param params The new database connection parameters.
     * @exception DBConnectionException Thrown if the connection fails.
     * @exception IOException Thrown if an error occurs while writing the new
     * parameters.
     */
    public void updateDBConnectionSetupParameters(SetupParameters params)
            throws DBConnectionException, IOException {

        if (params == null) {
            throw new DBConnectionException(
                    "Failed to update the connection parameters."
                            + " The setup parameters object sent was null.");
        }

        String ptdbParams = DBConnectorFactory._PROPERTIES_FILE_PATH;
        Properties props = new Properties();

        
        File file = FileUtilities.nameToFile(ptdbParams, null);
        
        // if the file does not exist, then create it.
        if(file.exists() == false) {
            file.createNewFile();
        }


        URL url = FileUtilities.nameToURL(ptdbParams, null, null);
        
        if (url == null) {
            throw new IOException(
                    "Could not find or create the properties file "
                            + ptdbParams);
        }

        String defaultDBClassName = "";

        props.load(url.openStream());
        
        String dbClassName = (String) props
                .getProperty(DBConnectorFactory._DB_CLASS_NAME);

        if (dbClassName == null || dbClassName.length() == 0) {
            dbClassName = defaultDBClassName;
        }


        props.setProperty(DBConnectorFactory._DB_CLASS_NAME, dbClassName);
        props.setProperty(DBConnectorFactory._DB_URL, params.getUrl());
        props.setProperty(DBConnectorFactory._XMLDB_CONTAINER_NAME, params
                .getContainerName());
        props.setProperty(DBConnectorFactory._CACHE_CONTAINER_NAME, params
                .getCacheContainerName());

        
        props.store(new FileOutputStream(url.getPath()), null);

        DBConnectorFactory.loadDBProperties();

    }
}

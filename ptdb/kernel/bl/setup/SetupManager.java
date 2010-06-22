package ptdb.kernel.bl.setup;

import java.io.File;
import java.io.IOException;
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
     * Return the existing database setup parameters.
     * <p>Delegate this task to DBConnectionFactory to perform it and return
     * the value without any modifications.</p>
     * 
     * @return The existing database setup parameters.
     */
    public SetupParameters getSetupParameters() {

        return DBConnectorFactory.getSetupParameters();
    }

    /**
     * Test the database connection with the parameters given.
     * @param params The parameters for the database connection. 
     * @Exception DBConnectionException Thrown if the connection fails.
     */
    public void testConnection(SetupParameters params)
            throws DBConnectionException {

        if(params == null) {
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

        try {
            DBConnection mainConnection = new OracleXMLDBConnection(
                    dbMainConnParams);

            if (mainConnection == null) {
                throw new DBConnectionException(
                        "Unable to create connection with the given parameters."
                        + " Connection Parameters tested are: \n"
                        + "URL: " + url + "\nContainer: " + containerName);
            }

            DBConnection cacheConnection = new OracleXMLDBConnection(
                    dbCacheConnParams);

            if (cacheConnection == null) {

                throw new DBConnectionException(
                        "Unable to create connection with the given parameters."
                        + " Connection Parameters tested are: \n"
                        + "URL: " + url + "\nCache Container: " + cacheContainerName);
            }

        } catch (DBConnectionException e) {
            throw new DBConnectionException(
                    "Unable to create connection with the given parameters."
                            + " - " + e.getMessage(), e);
        }
    }

    /**
     * Update the existing database connection parameters with the given
     * parameters.
     * 
     * @param params The new database connection parameters.
     * @Exception DBConnectionException Thrown if the connection fails.
     * @Exception IOException Thrown if an error occurs while writing the new
     * parameters.
     */
    public void updateConnection(SetupParameters params)
            throws DBConnectionException, IOException {

        if (params == null) {
            throw new DBConnectionException(
                    "Failed to update the connection parameters." 
                    + " The setup parameters object sent was null.");
        }

        
        String ptdbParams = "$CLASSPATH/ptdb/config/ptdb-params.properties";
        Properties props = new Properties();
        
        File propertiesFile = new File(ptdbParams);
        
        URL url = null;
        
        if(propertiesFile.exists() == false) {
            
            propertiesFile.createNewFile();
            url = FileUtilities.nameToURL(ptdbParams, null, null);
            
        } else {
            
            url = FileUtilities.nameToURL(ptdbParams, null, null);
        }
        
        if(url == null) {
            throw new IOException("Could not fine or create the properties file " 
                    + ptdbParams);
        }

        
        props.setProperty("DB_Url", params.getUrl());
        props.setProperty("DB_Container_Name", params.getContainerName());
        props.setProperty("Cache_Container_Name", params
                .getCacheContainerName());

        props.store(url.openConnection().getOutputStream(), null);
                
        DBConnectorFactory.loadDBProperties();

    }
}

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
package ptdb.kernel.bl.setup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.SetupParameters;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;
import ptolemy.util.FileUtilities;

///////////////////////////////////////////////////////////////////
////SetupManager

/**
 * Manage the setup parameters that is being passed to it from the GUI layer and
 * communicate that to the database layer as needed.
 *
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class SetupManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

            mainConnection = DBConnectorFactory
                    .getSyncConnection(dbMainConnParams);

            if (mainConnection == null) {
                throw new DBConnectionException(
                        "Failed to create connection with the following parameters: "
                                + "\nURL: " + url + "\nContainer Name: "
                                + containerName);
            }

            cacheConnection = DBConnectorFactory
                    .getSyncConnection(dbCacheConnParams);

            if (cacheConnection == null) {
                throw new DBConnectionException(
                        "Failed to create connection with the following parameters: "
                                + "\nURL: " + url + "\nCache Container Name: "
                                + cacheContainerName);
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

        File tempFile = FileUtilities.nameToFile(ptdbParams, null);
        File file = new File(tempFile.getPath().replaceAll("%20", " "));
        // if the file does not exist, then create it.
        if (file.exists() == false) {

            if (file.createNewFile() == false) {

                throw new IOException("Could not create the properties file "
                        + ptdbParams);
            }
        }

        URL url = FileUtilities.nameToURL(ptdbParams, null, null);

        if (url == null) {
            throw new IOException("Could not find the properties file "
                    + ptdbParams);
        }

        String defaultDBClassName = "";

        props.load(url.openStream());

        String dbClassName = props
                .getProperty(DBConnectorFactory._DB_CLASS_NAME);

        if (dbClassName == null || dbClassName.length() == 0) {
            dbClassName = defaultDBClassName;
        }

        props.setProperty(DBConnectorFactory._DB_CLASS_NAME, dbClassName);
        props.setProperty(DBConnectorFactory._DB_URL, params.getUrl());
        props.setProperty(DBConnectorFactory._XMLDB_CONTAINER_NAME,
                params.getContainerName());
        props.setProperty(DBConnectorFactory._CACHE_CONTAINER_NAME,
                params.getCacheContainerName());

        // The stream is closed inside the store method and we do not have control over it.
        props.store(new FileOutputStream(url.getPath().replaceAll("%20", " ")),
                null);

        DBConnectorFactory.loadDBProperties();

    }
}

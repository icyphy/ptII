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
package ptdb.kernel.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;

///////////////////////////////////////////////////////////////////
//// CacheManager

/**
 * The class that interfaces with the model cache.  Cached models need not be
 * constructed with references.  They are completely self-contained.  Use of
 * a cache saves time when loading models that contain many references.
 * When a model is loaded from the database, the complete model is added to the
 * cache.  When a model is saved, the entry of that model and all other models
 * that reference it are removed from the cache.
 *
 * @author Lyle Holsinger
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (lholsing)
 * @Pt.AcceptedRating Red (lholsing)
 *
 */
public class CacheManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove a set of models from the cache.
     *
     * @param modelsToRemove
     *          An ArrayList of XMLDBModel objects.  The names of these
     *          are used to remove the respective MoMLs from the cache.
     * @return
     *          True if the removal was successful.  False if it was not.
     * @exception DBConnectionException
     *          Thrown if a problem occurs with the cache connection.
     * @exception DBExecutionException
     *          Thrown if a problem occurs removing a model from the cache.
     */
    public static boolean removeFromCache(ArrayList<XMLDBModel> modelsToRemove)
            throws DBConnectionException, DBExecutionException {

        boolean isSuccessful = false;

        DBConnection dbConnection = null;

        try {

            if (modelsToRemove == null) {
                throw new IllegalArgumentException(
                        "Failed to remove models from the cache."
                                + " The array of XMLDBModel objects was null.");
            }

            dbConnection = DBConnectorFactory.getCacheConnection(false);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection to the cache.");
            }

            RemoveModelsTask removeModelsTask = new RemoveModelsTask(
                    modelsToRemove);
            dbConnection.executeRemoveModelsTask(removeModelsTask);

            isSuccessful = true;

            dbConnection.commitConnection();

        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw new DBExecutionException("Failed to remove "
                    + "models from the cache - " + e.getMessage(), e);
        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();

            }
        }

        return isSuccessful;

    }

    /** Given a model name, load a model from the cache.
     *
     * @param modelName
     *          The name of the model to be loaded form the cache.
     * @return
     *          An XMLDBModel populated with the model's MoML string.
     * @exception DBConnectionException
     *          Thrown if a problem occurs with the cache connection.
     * @exception DBExecutionException
     *          Thrown if a problem occurs retrieving the
     *          MoML from the database.
     */
    public static XMLDBModel loadFromCache(String modelName)
            throws DBConnectionException, DBExecutionException {

        XMLDBModel returnXMLDBModel = null;

        DBConnection dbConnection = null;

        try {

            if (modelName == null) {
                throw new IllegalArgumentException(
                        "Failed to load the model from the cache."
                                + " The model name was null.");
            }

            dbConnection = DBConnectorFactory.getCacheConnection(false);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get asynchronous connection to the cache.");
            }

            GetModelTask getModelTask = new GetModelTask(modelName);
            getModelTask.setModelFromCache(true);
            returnXMLDBModel = dbConnection.executeGetModelTask(getModelTask);
            //System.out.println("Got from cache - " + modelName);

        } catch (DBExecutionException e) {
            //System.out.println("Error while getting model from cache - " + e.getMessage());
            throw e;
        } finally {
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }

        return returnXMLDBModel;
    }

    /** Update a collection of models in the cache.  If a model is not in the
     * cache, add it.  If it is, update it.
     *
     * @param assemblies
     *          A HashMap that maps a collection of models names to
     *          their respective MoML strings.  These are the models to be
     *          updated in the cache.
     * @exception DBConnectionException
     *          Thrown if a problem occurs with the cache connection.
     * @exception DBExecutionException
     *          Thrown if a problem occurs updating the cache.
     */
    public static void updateCache(HashMap assemblies)
            throws DBConnectionException, DBExecutionException {

        DBConnection dbConnection = null;

        try {

            if (assemblies == null) {
                throw new IllegalArgumentException(
                        "Failed to remove models from the cache."
                                + " The expected HashMap was null.");
            }

            dbConnection = DBConnectorFactory.getCacheConnection(false);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get asynchronous connection to the cache.");
            }

            Iterator iterator = assemblies.keySet().iterator();

            while (iterator.hasNext()) {

                String modelName = (String) iterator.next();
                XMLDBModel cacheModel = new XMLDBModel(modelName);
                //Intentionally using keySet iterator.
                cacheModel.setModel((String) assemblies.get(modelName));
                dbConnection.executeUpdateModelInCache(cacheModel);
                //System.out.println("Updated in cache - " + modelName);
            }

            dbConnection.commitConnection();

        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw new DBExecutionException("Failed to update the cache - "
                    + e.getMessage(), e);
        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();

            }
        }

    }

}

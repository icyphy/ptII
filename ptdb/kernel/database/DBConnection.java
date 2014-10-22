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
import java.util.List;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetFirstLevelParentsTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.GetReferenceStringTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.ModelNameSearchTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.RenameModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.UpdateParentsToNewVersionTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.CircularDependencyException;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.DBModelNotFoundException;
import ptdb.common.exception.ModelAlreadyExistException;

///////////////////////////////////////////////////////////////////
//// DBConnection
/**
 *  Interface for XML Database connections.
 *
 *  @author Ashwini Bijwe, Yousef Alsaeed
 *
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)
 *
 */
public interface DBConnection {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Abort the connection to the database and roll back the transaction.
     * @exception DBConnectionException
     */
    public void abortConnection() throws DBConnectionException;

    /**
     * Close the connection to the database and commit the transaction.
     * @exception DBConnectionException
     */
    public void closeConnection() throws DBConnectionException;

    /**
     * Commit the transaction running over the connection.
     * @exception DBConnectionException - When there is a problem while committing
     * transaction in the database.
     */
    public void commitConnection() throws DBConnectionException;

    /**
     * Execute the necessary commands to create a new attribute in the database
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateAttributeTask.
     *          This will tell the DB layer to create a new attribute in the database.
     *
     * @return The XMLDBAttribute object that was stored in the Database.
     *
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public XMLDBAttribute executeCreateAttributeTask(CreateAttributeTask task)
            throws DBExecutionException;

    /**
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateModelTask.
     *          This will tell the DB layer to create a new model in the database.
     *
     * @return The Id of the newly created model.
     *
     * @exception DBExecutionException
     * @exception ModelAlreadyExistException Thrown if the model being created
     * already exists.
     * @exception CircularDependencyException If thrown while creating reference
     * string.
     */
    public String executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException,
            CircularDependencyException;

    /**
     * Execute the necessary commands to delete an attribute from the database
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, DeleteAttributeTask.
     *          This will tell the DB layer to delete an attribute from the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public void executeDeleteAttributeTask(DeleteAttributeTask task)
            throws DBExecutionException;

    /**
     * Fetch the parent model hierarchies for the given models.
     *
     * @param task - Task that contains the list of models.
     * @return - List of models that contain the parent hierarchies.
     * @exception DBExecutionException - When the database encounters
     * error while searching.
     */
    public ArrayList<XMLDBModel> executeFetchHierarchyTask(
            FetchHierarchyTask task) throws DBExecutionException;

    /**
     * Retrieve and return the list of all models in the database.
     * @return List of models in the database.
     * @exception DBExecutionException thrown if there is an error while reading
     * the model list from the database.
     */
    public List<XMLDBModel> executeGetListOfAllModels()
            throws DBExecutionException;

    /**
     * Search models that contain the given attributes in the database.
     *
     * @param task Task that contains a list of attributes that
     * need to be searched in the database.
     *
     * @return List of models that contain the attributes.
     *
     * @exception DBExecutionException - When the database encounters error while searching.
     */
    public ArrayList<XMLDBModel> executeAttributeSearchTask(
            AttributeSearchTask task) throws DBExecutionException;

    /**
     * Get the attributes defined from the database.
     * @param task The criteria to get the attribute.
     * @return List of attributes stored in the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public List<XMLDBAttribute> executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException;

    /**
     * Execute the given task to fetch the first level parents for the given model.
     * First level parents are models that are immediate parents for the given model.
     *
     * @param task Task that contains the model for which the first level
     * parents list needs to be fetched.
     *
     * @return List of models that are the first-level parents of the given model.
     *
     * @exception DBExecutionException If thrown while fetching the parents list from the
     * database.
     */
    public List<XMLDBModel> executeGetFirstLevelParents(
            GetFirstLevelParentsTask task) throws DBExecutionException;

    /**
     * Execute the necessary commands to retrieve a model from the database.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBExecutionException Thrown if the operations fails.
     */
    public XMLDBModel executeGetModelTask(GetModelTask task)
            throws DBExecutionException;

    /**
     * Get the model reference string for the given model name.
     * @param task Task that contains the model name.
     * @return The reference string for the given model name.
     * @exception DBExecutionException If thrown while fetching the reference
     * string.
     */
    public String executeGetReferenceStringTask(GetReferenceStringTask task)
            throws DBExecutionException;

    /**
     * Execute the necessary commands to retrieve a model from the database
     * and resolve all the references in it if any.
     *
     * @param task
     *          The task to be completed.  In this case, GetModelTask.
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string.
     * @exception DBExecutionException Thrown if the operations fails.
     */
    public XMLDBModel executeGetCompleteModelTask(GetModelTask task)
            throws DBExecutionException;

    /**
     * Search models that contain given graphical pattern in the database.
     *
     * @param task - Task that contains the graph search criteria.
     * @return - List of models that match the given search criteria.
     * @exception DBExecutionException - When the database encounters
     * error while searching.
     */
    public ArrayList<XMLDBModel> executeGraphSearchTask(GraphSearchTask task)
            throws DBExecutionException;

    /** Execute the model name search task.
     *
     * @param modelNameSearchTask Task that contains the model name to be searched for.
     * @return List of matching models.
     * @exception DBExecutionException If thrown while searching the database.
     */
    public ArrayList<XMLDBModel> executeModelNameSearchTask(
            ModelNameSearchTask modelNameSearchTask)
            throws DBExecutionException;

    /**
     * Execute the necessary commands to save/update a model in the database
     * according to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, SaveModelTask.
     *          This will tell the DB layer to save/update a model already
     *          existing in the database.
     *
     * @return The Id of the saved model.
     *
     * @exception DBExecutionException Thrown when there is a problem in
     * executing the task.
     * @exception CircularDependencyException If thrown while creating reference
     * string.
     */
    public String executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException, CircularDependencyException;

    /**
     * Execute remove models task to delete a list of models from the database.
     * @param task Contains a list of models to be deleted from the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public void executeRemoveModelsTask(RemoveModelsTask task)
            throws DBExecutionException;

    /**
     * Execute rename model task which will change the name of the model in
     * the database and reflect the change in the reference file.
     * @param task RenameModelTask object that contains the XMLDBModel
     * object and the new name.
     * @exception DBConnectionException Thrown if there was a problem with the connection.
     * @exception DBExecutionException Thrown if there is a problem in executing the task.
     * @exception DBModelNotFoundException Thrown if the model with the name to be changed does not exist.
     * @exception ModelAlreadyExistException Thrown if the new name is a name of a model that is already in the database.
     */
    public void executeRenameModelTask(RenameModelTask task)
            throws DBConnectionException, DBExecutionException,
            ModelAlreadyExistException, DBModelNotFoundException;

    /**
     * Execute the necessary commands to update an attribute in the database
     * according to the attribute specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, UpdateAttributeTask.
     *          This will tell the DB layer to update an attribute in the database.
     * @exception DBExecutionException Thrown if the operation fails.
     */
    public void executeUpdateAttributeTask(UpdateAttributeTask task)
            throws DBExecutionException;

    /**
     * Execute the necessary commands to update the cache with the given model.
     *
     * <p>If the model exists, replace it with the new model.
     * <br> If the model does not exist, save it in the cache.</p>
     *
     * @param xmlDBModel The model object that needs to be added to the cache.
     * @exception DBExecutionException Thrown if the operation fails.
     * already exists.
     */
    public void executeUpdateModelInCache(XMLDBModel xmlDBModel)
            throws DBExecutionException;

    /**
     * Execute the given task to update the referenced version for the given
     * parents from the old model to the new model.
     * @param task Task that contains the list of parents, the old model and the
     * new model.
     * @exception DBExecutionException If thrown while updating the parents in the
     * database.
     */
    public void executeUpdateParentsToNewVersion(
            UpdateParentsToNewVersionTask task) throws DBExecutionException;

}

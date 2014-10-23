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
package ptdb.kernel.bl.save;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetFirstLevelParentsTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.RenameModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.UpdateParentsToNewVersionTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.dto.XMLDBModelWithReferenceChanges;
import ptdb.common.exception.CircularDependencyException;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.DBModelNotFoundException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;
import ptdb.kernel.database.CacheManager;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// SaveModelManager

/**
 * The business layer of the save to database function.
 *
 * <p> It is responsible for:
 *
 * <br>- Get the XMLDBModel which is the Ptolemy model wrapped in a special Data
 * Transfer Object (dto).
 *
 * <br>- Wrap the model object in another dto (SaveModelTask or CreateModelTask)
 * to either create it in the database or save the modification made on it.
 *
 * <br>- Get a database connection and run the execute method on the database
 * connection object to perform the saving or creation of the model. </p>
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 *
 */
public class SaveModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the first level parents that reference a give model.
     *
     * @param baseModel An XMLDBModel object that represents the model for which
     * the first level parents are retrieved.
     * @return A list of first level parent models for the given model.
     *
     * @exception DBConnectionException Thrown if there is a problem creating a
     * connection to the database.
     * @exception DBExecutionException Thrown if the operation fails to execute.
     */
    public ArrayList<XMLDBModel> getFirstLevelParents(XMLDBModel baseModel)
            throws DBConnectionException, DBExecutionException {

        // Findbugs: avoid a dead local store here.  FindBugs is
        // mistaken in thinking that parentList could be a dead local
        // store if we set it here.
        ArrayList<XMLDBModel> parentsList = null;

        if (baseModel == null || baseModel.getModelName() == null
                || baseModel.getModelName().length() == 0) {

            throw new IllegalArgumentException(
                    "The base model passed is not properly created.");
        }

        GetFirstLevelParentsTask getFirstLevelParentsTask = new GetFirstLevelParentsTask(
                baseModel);

        DBConnection dbConnection = null;

        try {

            dbConnection = DBConnectorFactory.getSyncConnection(false);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            parentsList = (ArrayList<XMLDBModel>) dbConnection
                    .executeGetFirstLevelParents(getFirstLevelParentsTask);

        } catch (DBExecutionException e) {

            throw e;

        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();
            }
        }

        if (parentsList == null) {
            parentsList = new ArrayList<XMLDBModel>();
        }

        return parentsList;
    }

    /**
     * Save the changes of an existing model in the database or create a new
     * model in the database. Remove all prior entries to the saved model from
     * the cache, including any other models that reference it.
     *
     * @param xmlDBModel The model object that is required to be saved or
     * created in the database.
     *
     * @return A string representing the model id that was saved.
     *
     * @exception DBConnectionException Thrown if there is a database connection
     * error.
     * @exception DBExecutionException Thrown if the execution failed.
     * @exception IllegalArgumentException Thrown if the parameters were not
     * right.
     * @exception ModelAlreadyExistException Thrown if the model being created
     * already exists.
     * @exception XMLDBModelParsingException Thrown if the model is parsed
     * incorrectly.
     * @exception CircularDependencyException Thrown if there is a circular
     * dependency.
     *
     */
    public String save(XMLDBModel xmlDBModel) throws DBConnectionException,
    DBExecutionException, IllegalArgumentException,
    ModelAlreadyExistException, XMLDBModelParsingException,
    CircularDependencyException {

        String modelId = null;

        DBConnection dbConnection = null;

        try {

            if (xmlDBModel == null) {
                throw new IllegalArgumentException(
                        "Failed while attempting to save."
                                + " The XMLDBModel to be saved is null");
            }

            dbConnection = DBConnectorFactory.getSyncConnection(true);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            modelId = save(xmlDBModel, dbConnection);

            dbConnection.commitConnection();

        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw new DBExecutionException("Failed to save the model - "
                    + e.getMessage(), e);
        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();

            }
        }

        return modelId;

    }

    /**
     * Save a model but keep a list of models that reference it point to the old
     * model and update the rest of the models.
     *
     * @param xmlDBModelWithReferenceChanges An object that contains the model
     * to be saved, the list of parents that should have the old reference, and
     * the new version name that will be placed as a reference in the parents'
     * list.
     * @return A string that represents the model Id of the model being saved.
     * @exception DBConnectionException Thrown if the connection to the database
     * fails.
     * @exception DBExecutionException Thrown if the operation fails to execute.
     * @exception DBModelNotFoundException Thrown if the model to be saved is
     * not in the database.
     * @exception ModelAlreadyExistException Thrown if the new version name matches
     * a name of a model that is already in the database.
     * @exception IllegalArgumentException Thrown if the parameters passed are not
     * properly set.
     * @exception XMLDBModelParsingException Thrown if the MoML in the model object
     * is corrupted.
     * @exception CircularDependencyException Thrown if the save operation causes a
     * circular dependency.
     */
    public String saveWithParents(
            XMLDBModelWithReferenceChanges xmlDBModelWithReferenceChanges)
                    throws DBConnectionException, DBExecutionException,
                    DBModelNotFoundException, ModelAlreadyExistException,
                    IllegalArgumentException, XMLDBModelParsingException,
                    CircularDependencyException {

        String modelId = "";

        // Check if the object sent is properly set.

        if (xmlDBModelWithReferenceChanges == null
                || xmlDBModelWithReferenceChanges.getModelToBeSaved() == null) {

            throw new IllegalArgumentException(
                    "The parameters sent was not set properly.");
        }

        DBConnection dbConnection = null;

        try {

            dbConnection = DBConnectorFactory.getSyncConnection(true);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            if (!xmlDBModelWithReferenceChanges.getModelToBeSaved().getIsNew()
                    && xmlDBModelWithReferenceChanges.getVersionName() != null
                    && xmlDBModelWithReferenceChanges.getVersionName().length() > 0
                    && xmlDBModelWithReferenceChanges.getParentsList() != null
                    && xmlDBModelWithReferenceChanges.getParentsList().size() > 0) {

                GetModelTask getModelTask = new GetModelTask(
                        xmlDBModelWithReferenceChanges.getModelToBeSaved()
                        .getModelName());

                // Get the content of the model to be saved from the database.
                XMLDBModel dbModelToBeSaved = dbConnection
                        .executeGetModelTask(getModelTask);

                XMLDBModel newXMLDBModel = new XMLDBModel(
                        xmlDBModelWithReferenceChanges.getVersionName());

                newXMLDBModel.setIsNew(true);
                String modelContent = dbModelToBeSaved.getModel();
                modelContent = modelContent.replaceFirst("name=\""
                        + dbModelToBeSaved.getModelName() + "\"", "name=\""
                                + xmlDBModelWithReferenceChanges.getVersionName()
                                + "\"");
                newXMLDBModel.setModel(modelContent);

                String newModelId = save(newXMLDBModel, dbConnection);

                newXMLDBModel.setModelId(newModelId);

                UpdateParentsToNewVersionTask updateParentsToNewVersionTask = new UpdateParentsToNewVersionTask();

                updateParentsToNewVersionTask.setNewModel(newXMLDBModel);

                updateParentsToNewVersionTask
                .setOldModel(xmlDBModelWithReferenceChanges
                        .getModelToBeSaved());

                updateParentsToNewVersionTask
                .setParentsList(xmlDBModelWithReferenceChanges
                        .getParentsList());

                dbConnection
                .executeUpdateParentsToNewVersion(updateParentsToNewVersionTask);

                ArrayList<String> parentsList = xmlDBModelWithReferenceChanges
                        .getParentsList();

                ArrayList<XMLDBModel> modelsToRemove = new ArrayList<XMLDBModel>();

                for (String modelName : parentsList) {
                    XMLDBModel xmlDBModel = new XMLDBModel(modelName);
                    modelsToRemove.add(xmlDBModel);
                }

                CacheManager.removeFromCache(modelsToRemove);

            }

            //String oldModelId = "";

            modelId = save(xmlDBModelWithReferenceChanges.getModelToBeSaved(),
                    dbConnection);

            dbConnection.commitConnection();

        } catch (DBConnectionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } catch (DBExecutionException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } catch (ModelAlreadyExistException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } catch (IllegalArgumentException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } catch (XMLDBModelParsingException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } catch (CircularDependencyException e) {

            if (dbConnection != null) {

                dbConnection.abortConnection();
            }

            throw e;

        } finally {

            if (dbConnection != null) {

                dbConnection.closeConnection();
            }
        }

        return modelId;

    }

    /**
     * Populate the referenced child models list and update the model XML by
     * replacing the referenced models with place holder.
     *
     * @param model Model with references to be resolved.
     *
     * @return The updates model containing the list of child models and updated
     * content.
     *
     * @exception XMLDBModelParsingException If thrown while parsing the XML.
     */
    public XMLDBModel populateChildModelsList(XMLDBModel model)
            throws XMLDBModelParsingException {

        if (model.getModel() == null) {
            return model;
        }
        model.setReferencedChildren(new ArrayList<String>());
        Document modelDocument = (Document) Utilities
                .parseXML(model.getModel());
        /*
         * First level nodes.
         */
        Node topEntityNode = modelDocument.getElementsByTagName("entity").item(
                0);

        //        if (topEntityNode != null) {

        NodeList entityList = topEntityNode.getChildNodes();

        boolean isChanged = false;

        if (entityList != null) {

            for (int i = 0; i < entityList.getLength(); i++) {

                Node entity = entityList.item(i);

                if (!"entity".equals(entity.getNodeName())) {
                    continue;
                }

                /* Get all first-level nodes inside the given entity. */
                NodeList parameterList = entity.getChildNodes();

                String referencedModelId = null;
                boolean isReferenced = false;
                boolean isReferencedFound = false;
                boolean dbModelIdFound = false;

                /* Get value for the DBReference and DBModelName properties.*/
                for (int j = 0; j < parameterList.getLength(); j++) {

                    Node parameter = parameterList.item(j);

                    if ("property".equals(parameter.getNodeName())) {

                        String name = Utilities.getValueForAttribute(parameter,
                                "name");

                        if (XMLDBModel.DB_MODEL_ID_ATTR.equals(name)
                                && !dbModelIdFound) {

                            referencedModelId = Utilities.getValueForAttribute(
                                    parameter, "value");

                            dbModelIdFound = true;

                        } else if (XMLDBModel.DB_REFERENCE_ATTR.equals(name)
                                && !isReferencedFound) {

                            String value = Utilities.getValueForAttribute(
                                    parameter, "value");
                            isReferenced = "TRUE".equals(value);

                            isReferencedFound = true;
                        }

                        if (isReferencedFound && dbModelIdFound) {
                            break;
                        }
                    }
                }

                if (isReferenced && referencedModelId != null) {

                    /*
                     * As we are considering only "entity" nodes, we can be
                     * sure that the type conversion will not fail.
                     */
                    Element entityElement = (Element) entity.cloneNode(false);
                    NodeList childNodesList = entity.getChildNodes();

                    /*
                     * Create an entity node with the required properties and
                     * replace the current referenced entity.
                     */
                    int k = 0;
                    while (k < childNodesList.getLength()) {
                        Node childNode = childNodesList.item(k);

                        if ("property".equals(childNode.getNodeName())) {

                            String name = Utilities.getValueForAttribute(
                                    childNode, "name");

                            if (name != null
                                    && (name.startsWith("_")
                                            || XMLDBModel.DB_REFERENCE_ATTR
                                            .equals(name) || XMLDBModel.DB_MODEL_ID_ATTR
                                            .equals(name))) {
                                entityElement.appendChild(childNode);
                            } else {
                                k++;
                            }
                        } else {
                            k++;
                        }

                    }

                    entityElement.setAttribute(XMLDBModel.DB_MODEL_ID_ATTR,
                            referencedModelId);
                    topEntityNode.replaceChild(entityElement, entity);

                    model.addReferencedChild(referencedModelId);
                    isChanged = true;
                }
            }
        }

        /* Update model content only if the model has changed. */
        if (isChanged) {

            String newModelContent = Utilities
                    .getDocumentXMLString(modelDocument);
            model.setModel(newModelContent);

            //            }
        }

        return model;
    }

    /**
     * Rename an existing model in the database.
     *
     * @param originalModel XMLDBModel object that represent the model that its
     * name needs to be changed.
     * @param newName A string that contains the new model name.
     * @exception DBConnectionException Thrown if the connection to the database
     * fails.
     * @exception DBExecutionException Thrown if the operation failed during
     * execution.
     * @exception IllegalArgumentException Thrown if one of the parameters is
     * not proper.
     * @exception ModelAlreadyExistException Thrown if the new name of the model
     * represent a model that already exists in the database.
     * @exception DBModelNotFoundException Thrown if the original model does not
     * exit in the database.
     */
    public void renameModel(XMLDBModel originalModel, String newName)
            throws DBConnectionException, DBExecutionException,
            IllegalArgumentException, ModelAlreadyExistException,
            DBModelNotFoundException {

        if (originalModel == null) {

            throw new IllegalArgumentException(
                    "The original model cannot be null.");
        }

        if (originalModel.getModelId() == null
                && (originalModel.getModelName() == null || originalModel
                .getModelName().length() == 0)) {

            throw new IllegalArgumentException(
                    "The original model must contain either"
                            + " the model name or model Id.");
        }

        if (newName == null || newName.length() == 0) {

            throw new IllegalArgumentException(
                    "The new model name cannot be empty.");
        }

        RenameModelTask renameModelTask = new RenameModelTask(originalModel,
                newName);

        DBConnection dbConnection = DBConnectorFactory.getSyncConnection(true);

        try {

            dbConnection.executeRenameModelTask(renameModelTask);

        } catch (DBExecutionException e) {

            dbConnection.abortConnection();

            throw e;

        } catch (ModelAlreadyExistException e) {

            dbConnection.abortConnection();

            throw e;

        } catch (DBModelNotFoundException e) {

            dbConnection.abortConnection();

            throw e;

        } finally {

            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }

        // Remove the original model from the cache.
        ArrayList<XMLDBModel> removeFromCacheList = new ArrayList<XMLDBModel>();

        removeFromCacheList.add(originalModel);
        CacheManager.removeFromCache(removeFromCacheList);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Save the changes of an existing model in the database or create a new
     * model in the database. Remove all prior entries to the saved model from
     * the cache, including any other models that reference it.
     *
     * @param xmlDBModel The model object that is required to be saved or
     * created in the database.
     *
     * @param dbConnection The connection to the database that will be used to
     * execute the task.
     *
     * @return A string representing the model id that was saved.
     *
     * @exception DBConnectionException Thrown if there is a database connection
     * error.
     * @exception DBExecutionException Thrown if the execution failed.
     * @exception IllegalArgumentException Thrown if the parameters were not
     * right.
     * @exception ModelAlreadyExistException Thrown if the model being created
     * already exists.
     * @exception XMLDBModelParsingException Thrown if the model is parsed
     * incorrectly.
     * @exception CircularDependencyException
     *
     */
    private String save(XMLDBModel xmlDBModel, DBConnection dbConnection)
            throws DBConnectionException, DBExecutionException,
            IllegalArgumentException, ModelAlreadyExistException,
            XMLDBModelParsingException, CircularDependencyException {

        String newModelId = null;
        if (xmlDBModel == null) {
            throw new IllegalArgumentException(
                    "Failed while attempting to save."
                            + " The XMLDBModel to be saved is null");
        }
        try {

            String modelBody = xmlDBModel.getModel();

            modelBody = removeDTD(modelBody);

            //            System.out.println(modelBody);

            //            if (modelBody.indexOf("<!DOCTYPE") >= 0) {
            //
            //                modelBody = modelBody.substring(modelBody.indexOf("<!DOCTYPE"));
            //                modelBody = modelBody.substring(modelBody.indexOf(">") + 1);
            //            }

            xmlDBModel.setModel(modelBody);

            xmlDBModel = populateChildModelsList(xmlDBModel);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            if (xmlDBModel.getIsNew()) {

                CreateModelTask createModelTask = new CreateModelTask(
                        xmlDBModel);

                newModelId = dbConnection
                        .executeCreateModelTask(createModelTask);

            } else {

                SaveModelTask saveModelTask = new SaveModelTask(xmlDBModel);

                newModelId = dbConnection.executeSaveModelTask(saveModelTask);

            }

        } catch (DBExecutionException e) {

            throw new DBExecutionException("Failed to save the model - "
                    + e.getMessage(), e);

        }

        updateCache(xmlDBModel);

        return newModelId;
    }

    /**
     * Update the cache with the model provided.
     *
     * @param xmlDBModel The model to be updated in the cache.
     * @exception DBConnectionException Thrown if the connection to the cache
     * fails.
     * @exception DBExecutionException Thrown if the execution of the task fails.
     */
    private void updateCache(XMLDBModel xmlDBModel)
            throws DBConnectionException, DBExecutionException {

        ArrayList<XMLDBModel> modelsToRemoveList = new ArrayList();

        DBConnection dbConnection = null;

        try {

            dbConnection = DBConnectorFactory.getSyncConnection(false);

            ArrayList<XMLDBModel> modelList = new ArrayList();
            modelList.add(xmlDBModel);
            FetchHierarchyTask fetchHierarchyTask = new FetchHierarchyTask();
            fetchHierarchyTask.setModelsList(modelList);

            ArrayList<XMLDBModel> hierarchy = dbConnection
                    .executeFetchHierarchyTask(fetchHierarchyTask);

            if (hierarchy != null && hierarchy.size() > 0) {

                if (hierarchy.get(0) != null) {

                    if (hierarchy.get(0).getParents() != null) {

                        for (List<XMLDBModel> branch : hierarchy.get(0)
                                .getParents()) {

                            for (XMLDBModel modelToRemove : branch) {

                                if (!modelsToRemoveList.contains(modelToRemove)) {

                                    modelsToRemoveList.add(modelToRemove);
                                }

                            }

                        }

                    }

                }

            }

        } catch (DBExecutionException dbEx) {
            throw dbEx;
        } finally {
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }

        modelsToRemoveList.add(xmlDBModel);

        CacheManager.removeFromCache(modelsToRemoveList);

    }

    /**
     * Remove the DTD from the model content.
     * @param modelContent The model content.
     * @return The model content after the DTD part is removed.
     */
    private String removeDTD(String modelContent) {

        String newModelContent = "";

        newModelContent = modelContent;

        if (newModelContent.indexOf("<?xml") == 0) {

            newModelContent = newModelContent.substring(newModelContent
                    .indexOf("<?xml"));

            newModelContent = newModelContent.substring(newModelContent
                    .indexOf(">") + 1);

            newModelContent = newModelContent.substring(newModelContent
                    .indexOf("<"));

            newModelContent = newModelContent.trim();

        }

        if (newModelContent.indexOf("<!DOCTYPE") == 0) {

            newModelContent = newModelContent.substring(newModelContent
                    .indexOf("<!DOCTYPE"));

            newModelContent = newModelContent.substring(newModelContent
                    .indexOf(">") + 1);

            newModelContent = newModelContent.substring(newModelContent
                    .indexOf("<"));

            newModelContent = newModelContent.trim();
        }
        return newModelContent;
    }
}

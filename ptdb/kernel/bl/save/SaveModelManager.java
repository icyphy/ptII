/*
@Copyright (c) 2010 The Regents of the University of California.
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
import ptdb.common.dto.RenameModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
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
 * <p> 
 * It is responsible for:
 * 
 * <br>- Get the XMLDBModel which is the Ptolemy model wrapped in a special Data
 * Transfer Object (dto).
 * 
 * <br>- Wrap the model object in another dto (SaveModelTask or CreateModelTask)
 * to either create it in the database or save the modification made on it.
 * 
 * <br>- Get a database connection and run the execute method on the database
 * connection object to perform the saving or creation of the model. 
 * </p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 * 
 */
public class SaveModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                            ////

    /**
     * Save the changes of an existing model in the database or create a new
     * model in the database.  Remove all prior entries to the saved model
     * from the cache, including any other models that reference it.
     * 
     * @param xmlDBModel The model object that is required to be saved or
     * created in the database.
     * 
     * @return A boolean indicator of weather the operation was successful or
     * not.
     * 
     * @exception DBConnectionException Thrown if there is a database connection error.
     * @exception DBExecutionException Thrown if the execution failed.
     * @exception IllegalArgumentException Thrown if the parameters were not right.
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     * @throws XMLDBModelParsingException Thrown if the model is parsed incorrectly.
     * 
     */
    public String save(XMLDBModel xmlDBModel) throws DBConnectionException,
            DBExecutionException, IllegalArgumentException,
            ModelAlreadyExistException, XMLDBModelParsingException {

        String returnString = null;

        DBConnection dbConnection = null;

        try {

            if (xmlDBModel == null) {
                throw new IllegalArgumentException(
                        "Failed while attempting to save."
                                + " The XMLDBModel to be saved is null");
            }
            xmlDBModel = populateChildModelsList(xmlDBModel);
            dbConnection = DBConnectorFactory.getSyncConnection(true);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            if (xmlDBModel.getIsNew()) {

                CreateModelTask createModelTask = new CreateModelTask(
                        xmlDBModel);

                returnString = dbConnection.executeCreateModelTask(createModelTask);

                dbConnection.commitConnection();

            } else {

                SaveModelTask saveModelTask = new SaveModelTask(xmlDBModel);

                returnString = dbConnection.executeSaveModelTask(saveModelTask);

                dbConnection.commitConnection();

            }

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

        updateCache(xmlDBModel);
        
        return returnString;

    }

    /**
     * Populate the referenced child models list and update the model XML by 
     * replacing the referenced models with place holder.  
     * 
     * @param model Model with references to be resolved.
     * 
     * @return The updates model containing the list of child models and 
     * updated content.
     * 
     * @throws XMLDBModelParsingException If thrown while parsing the XML. 
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
        Node topEntityNode = modelDocument.getElementsByTagName("entity").item(0);
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
                    for (int k = 0; k < childNodesList.getLength(); k++) {
                        Node childNode = childNodesList.item(k);

                        if ("property".equals(childNode.getNodeName())) {

                            String name = Utilities.getValueForAttribute(
                                    childNode, "name");

                            if (name != null
                                    && (name.startsWith("_")
                                            || XMLDBModel.DB_REFERENCE_ATTR.equals(name) 
                                            || XMLDBModel.DB_MODEL_ID_ATTR
                                            .equals(name))) {
                                entityElement.appendChild(childNode);
                            }
                        }
                    }

                    entityElement.setAttribute(XMLDBModel.DB_MODEL_ID_ATTR,
                            referencedModelId);
                    topEntityNode.replaceChild(entityElement,
                            entity);

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

        }

        return model;
    }
    
    /**
     * Rename an existing model in the database.
     * 
     * @param originalModel XMLDBModel object that represent the model that its name needs to be changed.
     * @param newName A string that contains the new model name.
     * @exception DBConnectionException Thrown if the connection to the database fails.
     * @exception DBExecutionException Thrown if the operation failed during execution.
     * @exception IllegalArgumentException Thrown if one of the parameters is not proper.
     * @exception ModelAlreadyExistException Thrown if the new name of the model represent a model that already exists in the database.
     * @exception DBModelNotFoundException Thrown if the original model does not exit in the database.
     */
    public void renameModel(XMLDBModel originalModel, String newName) 
            throws DBConnectionException, DBExecutionException, 
            IllegalArgumentException, ModelAlreadyExistException, 
            DBModelNotFoundException {
        
        if (originalModel.getModelId() == null && 
                (originalModel.getModelName() == null || 
                        originalModel.getModelName().length() == 0)) {
            
            throw new IllegalArgumentException("The original model must contain either"
                    + " the model name or model Id.");
        }
        
        if (newName == null || newName.length() == 0) {
            
            throw new IllegalArgumentException("The new model name cannot be empty.");
        }
        
        
        RenameModelTask renameModelTask = new RenameModelTask(originalModel, newName);
        
        DBConnection dbConnection = DBConnectorFactory.getSyncConnection(true);
        
        
     
        try {
            
            //FIXME: remove the commented line below once the database layer is ready.
//            
//          
//          dbConnection.executeRenameModelTask(renameModelTask);
//          
//            
//        } catch (DBExecutionException e){
//            
//            dbConnection.abortConnection();
//            
//            throw new DBExecutionException(e.getMessage(),e);
//            
//        } catch (ModelAlreadyExistException e){
//            
//            dbConnection.abortConnection();
//            
//            throw new ModelAlreadyExistException(e.getMessage(),e);
//            
//        } catch (DBModelNotFoundException e){
//            
//            dbConnection.abortConnection();
//            
//            throw new DBModelNotFoundException(e.getMessage(),e);
//            
        } finally {
            
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }
        
        
        
        
    }
    
    //////////////////////////////////////////////////////////////////////
    ////                private methods                               ////
    
    private void updateCache(XMLDBModel xmlDBModel) 
        throws DBConnectionException, DBExecutionException{
    
        ArrayList <XMLDBModel> hierarchy = new ArrayList();
        ArrayList <XMLDBModel> modelsToRemoveList = new ArrayList();
        
        DBConnection dbConnection = null;
        
        try {
            
            dbConnection = DBConnectorFactory.getSyncConnection(false);
            
            ArrayList<XMLDBModel> modelList = new ArrayList();
            modelList.add(xmlDBModel);
            FetchHierarchyTask fetchHierarchyTask 
                = new FetchHierarchyTask();
            fetchHierarchyTask.setModelsList(modelList);
            
            hierarchy = 
                  dbConnection.executeFetchHierarchyTask(fetchHierarchyTask);
    
            if(hierarchy != null && hierarchy.size()>0) {
                    
                if (hierarchy.get(0) != null){
                   
                    if(hierarchy.get(0).getParents() != null){
                        
                        for(List<XMLDBModel> branch : hierarchy.get(0).getParents()){
                            
                            for(XMLDBModel modelToRemove : branch){
                                
                                if(!modelsToRemoveList.contains(modelToRemove)){
            
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
}

package ptdb.kernel.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.DBGraphSearchCriteria;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.RemoveModelsTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptdb.common.util.Utilities;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlQueryExpression;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;
import com.sleepycat.dbxml.XmlValue;

///////////////////////////////////////////////////////////////////
//// OracleXMLDBConnection
/**
 * Create and manage the actual connection to the Oracle XML database. Manage
 * the transactions for the database.
 * 
 * <p>This is a wrapper class that acts as a Facade to the Oracle Berkeley XML
 * Database interface.</p>
 * 
 * @author Ashwini Bijwe, Yousef Alsaeed
 * 
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 */
public class OracleXMLDBConnection implements DBConnection {

    /**
     * Construct an instance that initializes the environment and creates a
     * database connection based on the given parameters.
     * 
     * @param dbConnParams Encapsulate the parameters required to create a
     * database connection.
     * 
     * @exception DBConnectionException If thrown while creating a connection.
     */
    public OracleXMLDBConnection(DBConnectionParameters dbConnParams)
            throws DBConnectionException {

        try {

            _params = dbConnParams;

            EnvironmentConfig config = new EnvironmentConfig();
            config.setRunRecovery(true);
            config.setCacheSize(5 * 1024 * 1024); // 50MB
            config.setAllowCreate(true);
            config.setInitializeCache(true);
            config.setTransactional(true);
            config.setInitializeLocking(true);
            config.setInitializeLogging(true);
            config.setErrorStream(System.err);
            config.setMaxLockers(1000);
            config.setMaxLocks(1000);
            config.setMaxLockObjects(1000);
            config.setLockDetectMode(LockDetectMode.DEFAULT);

            File dbFile = new File(_params.getUrl());

            _environment = new Environment(dbFile, config);

            _xmlManager = new XmlManager(_environment, null);

            _xmlContainer = _xmlManager.openContainer(_params
                    .getContainerName());

            if (_params.isTransactionRequired()) {
                _xmlTransaction = _xmlManager.createTransaction();
                _isTransactionActive = true;
            } else {
                _isTransactionActive = false;
            }

            _isConnectionAlive = true;

        } catch (FileNotFoundException e) {

            throw new DBConnectionException(
                    "Exception while connecting to the database : "
                            + "Database not found at the given location : "
                            + _params.getUrl(), e);

        } catch (DatabaseException e) {

            System.out.println("Exception while connecting to the database : "
                    + e.getMessage());

            throw new DBConnectionException(
                    "Exception while connecting to the database : "
                            + e.getMessage(), e);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Abort the connection and roll back the transaction Invoke in case of
     * errors.
     * 
     * @exception DBConnectionException If thrown while aborting transaction in
     * the database.
     */
    public void abortConnection() throws DBConnectionException {
        try {
            _checkConnectionAlive();

            if (_xmlTransaction != null) {
                _checkTransactionActive();
                _xmlTransaction.abort();
                _isTransactionActive = false;
            }
        } catch (XmlException e) {

            throw new DBConnectionException(
                    "Database transaction could not be aborted - "
                            + e.getMessage(), e);

        }
    }

    /**
     * Commit the transaction running over the connection.
     * @exception DBConnectionException If thrown while committing transaction
     * in the database.
     */
    public void commitConnection() throws DBConnectionException {
        try {

            _checkConnectionAlive();
            if (_xmlTransaction != null) {
                _checkTransactionActive();
                _xmlTransaction.commitSync();
                _isTransactionActive = false;
            }

        } catch (XmlException e) {
            throw new DBConnectionException(
                    "Database transaction could not be committed - "
                            + e.getMessage(), e);
        }
    }

    /**
     * Commit the transaction and close the connection to the database.
     * 
     * <p>If the transaction on this connection is alive/open while closing the
     * connection, the transaction will be aborted.</p>
     * 
     * @exception DBConnectionException If thrown while closing database
     * connection.
     */
    public void closeConnection() throws DBConnectionException {
        try {
            _checkConnectionAlive();
            if (_isTransactionActive == true) {
                System.out.println("The transaction was alive "
                        + "while closing the connection. "
                        + "So aborting the transaction "
                        + "before closing the connection.");
                abortConnection();
            }
            _cleanUp();

        } finally {
            _isConnectionAlive = false;
        }
    }
    
    

    /**
     * Execute the necessary commands to create a new model in the database
     * according to the model specification given in the task parameter.
     * 
     * @param task The task to be completed. In this case, CreateModelTask. This
     * will tell the DB layer to create a new model in the database.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     * @exception ModelAlreadyExistException Thrown if the model being created
     * already exists.
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException {

        try {

            _checkXMLDBConnectionObjects(true, false, true);
            


            XMLDBModel model = task.getXMLDBModel();



            XmlDocument doc = null;

            try {
                doc = _xmlContainer.getDocument(model.getModelName());
            } catch (XmlException e) {
                //do nothing
            }

            if (doc != null) {
                throw new ModelAlreadyExistException(
                        "Failed to execute CreateModelTask"
                                + " - The model already exist in the database. "
                                + "Please use the executeSaveModelTask to "
                                + "update the model.");
            } else {

                String modelBody = model.getModel();

                if (modelBody.indexOf("<!DOCTYPE") >= 0) {
                    
                    modelBody = modelBody.substring(modelBody.indexOf("<!DOCTYPE"));
                    modelBody = modelBody.substring(modelBody.indexOf(">") + 1);
                }

                _xmlContainer.putDocument(_xmlTransaction,
                        model.getModelName(), modelBody);

            }
        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute GetModelsTask - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Fetch the parent model hierarchies for the given models.
     * 
     * @param task Task that contains the list of models.
     * @return List of models that contain the parent hierarchies.
     * @exception DBExecutionException If thrown while searching in the
     * database.
     */
    public ArrayList<XMLDBModel> executeFetchHierarchyTask(
            FetchHierarchyTask task) throws DBExecutionException {
        ArrayList<XMLDBModel> modelsList = task.getModelsList();
        for (XMLDBModel model : modelsList) {
            try {
         
                _fetchHierarchyForModel(model);
            
            } catch (XMLDBModelParsingException e) {

                throw new DBExecutionException(
                        "Failed to execute GetAttributesTask - "
                                + e.getMessage(), e);
            }
        }
        return modelsList;
    }

    /**
     * Get the attributes defined from the database.
     * @param task The criteria to get the attribute.
     * @return List of attributes stored in the database.
     * @exception DBExecutionException Thrown if there is a problem in executing the operation.
     */
    public ArrayList<XMLDBAttribute> executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {

        ArrayList<XMLDBAttribute> attributeList = new ArrayList<XMLDBAttribute> ();
        
        
        try {
            
            
            _checkXMLDBConnectionObjects(true, false, false);


            XmlDocument doc = null;

            try {
                doc = _xmlContainer.getDocument("Attributes.ptdbxml");
            } catch (XmlException e) {
                throw new DBExecutionException(
                        "Failed to execute GetAttributesTask"
                                + " - Could not fetch the Attribute.ptdbxml.");
            }

            Document attributeDocument = (Document) Utilities.parseXML(doc
                    .getContentAsString());

            if (attributeDocument != null) {
                //get the first node in the attributes SAX document returned
                Node firstNode = attributeDocument.getElementsByTagName("attributes").item(0);
            
                if (firstNode != null) {
                
                    NodeList children = firstNode.getChildNodes();
                    
                    for (int i = 0; i < children.getLength(); i++) {
                        if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                            Node child = children.item(i);
                            
                            XMLDBAttribute xmlDBAttribute = _constructXMLDBAttribute(child);
                            
                            if (xmlDBAttribute != null) {
                            
                                attributeList.add(xmlDBAttribute);
                            }
                        }
                    }
                }
            }
            
        } catch (XmlException e) {
            
            throw new DBExecutionException("Failed to execute GetAttributesTask - "
                    + e.getMessage(), e);
        } catch (XMLDBModelParsingException e) {
            
            throw new DBExecutionException("Failed to execute GetAttributesTask - "
                    + e.getMessage(), e);
        }
        
        return attributeList;
    }

    
    /**
     * Execute the get model task and return the model requested as XMLDBModel
     * object as it is represented in the database.
     * @param task The GetModelTask object that contains the model name.
     * @return Model retrieved from the database in XMLDBModel object format.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    public XMLDBModel executeGetModelTask(GetModelTask task)
            throws DBExecutionException {

        XMLDBModel xmlDBModel = null;
        
        try {

            XmlDocument dbModel = _getModelFromDB(task);

            if (dbModel != null) {

                xmlDBModel = new XMLDBModel(dbModel.getName());

                String modelBody = dbModel.getContentAsString();

                xmlDBModel.setModel(modelBody);

                xmlDBModel.setIsNew(false);

                xmlDBModel.setParents(null);

            }

        } catch (XmlException e) {

            throw new DBExecutionException("Failed to execute GetModelTask - "
                    + e.getMessage(), e);
        }
        
        return xmlDBModel;
        
        
    }

    
    
    /**
     * Execute the get model task and return the model requested as XMLDBModel
     * object and resolve any references in it.
     * @param task The GetModelTask object that contains the model name.
     * @return Model retrieved from the database as XMLDBModel task.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    public XMLDBModel executeGetCompleteModelTask(GetModelTask task)
            throws DBExecutionException {

        XMLDBModel completeXMLDBModel = null;
        
        try {
            
            
//            try {
//                
//                XMLDBModel xmlDBModelCache = CacheManager.loadFromCache(task.getModelName());
//                
//                if (xmlDBModelCache != null) {
//                    
//                    return xmlDBModelCache;
//                }
//                
//            } catch (Exception e) {
//                //do nothing
//            }
            
            
            
            
            XmlDocument dbModel = _getModelFromDB(task);

            if (dbModel != null) {

                completeXMLDBModel = new XMLDBModel(dbModel.getName());

                String completeModelBody = "";

                String references = _getModelReferences(dbModel.getName());

                if (references != null && references.length() > 0) {

                    Node modelNode = Utilities.parseXML(references);

                    modelNode = modelNode.getChildNodes().item(0);

                    completeModelBody = _buildCompleteModel(modelNode);
                    
                  try {
                  
                    
                      if (_xmlModelHerarichyMap != null && 
                              _xmlModelHerarichyMap.size() > 0) {
                    
                          CacheManager.updateCache(_xmlModelHerarichyMap);
                      }
                      
                  } catch (Exception e) {
                      //do nothing since the updating of the cache 
                      //should not affect the load operation.
                  }

                } else {

                    completeModelBody = dbModel.getContentAsString();
                }

                completeXMLDBModel.setModel(completeModelBody);

                completeXMLDBModel.setIsNew(false);

                completeXMLDBModel.setParents(null);

            }

            return completeXMLDBModel;

        } catch (XmlException e) {

            throw new DBExecutionException("Failed to execute GetModelsTask - "
                    + e.getMessage(), e);
            
        } catch (DBConnectionException e) {

            throw new DBExecutionException("Failed to execute GetModelsTask - "
                    + e.getMessage(), e);
            
        } catch (XMLDBModelParsingException e) {
            
            throw new DBExecutionException("Failed to execute GetModelsTask - "
                    + e.getMessage(), e);
        }   
    }

    /**
     * Search models that contain the given attributes in the database.
     * 
     * @param task Task that contains a list of attributes that need to be
     * searched in the database.
     * 
     * @return List of models that contain the attributes; Empty list if the
     * search returns zero results; and Null list if the search is not performed
     * due to empty or invalid search criteria.
     * 
     * @exception DBExecutionException If thrown while searching in the
     * database.
     */
    public ArrayList<XMLDBModel> executeAttributeSearchTask(
            AttributeSearchTask task) throws DBExecutionException {

        ArrayList<XMLDBModel> finalModelsList = new ArrayList<XMLDBModel>();
        ArrayList<String> matchingModelNamesList = null;
        ArrayList<Attribute> attributesList = task.getAttributesList();

        if (attributesList != null && attributesList.size() > 0) {
            for (Attribute attribute : attributesList) {
                if (attribute instanceof Variable) {
                    try {

                        String attributeClause = _createAttributeClause((Variable) attribute);
                        ArrayList<String> modelNamesList = _executeSingleAttributeMatch(attributeClause);
                        if (matchingModelNamesList == null) {
                            matchingModelNamesList = new ArrayList<String>();
                            matchingModelNamesList.addAll(modelNamesList);
                        } else {
                            matchingModelNamesList.retainAll(modelNamesList);
                        }

                    } catch (IllegalActionException e) {

                        System.out
                                .println("Exception while retriving value for attribute - "
                                        + attribute.getName()
                                        + " - "
                                        + attribute.getClassName());

                    } catch (XmlException e) {

                        throw new DBExecutionException(
                                "Error while executing GetAttributesSearch - "
                                        + e.getMessage(), e);
                    }
                    if (matchingModelNamesList != null
                            && matchingModelNamesList.size() == 0) {
                        return finalModelsList;
                    }
                }
            }

            if (matchingModelNamesList != null
                    && matchingModelNamesList.size() > 0) {

				return _getDistinctModelsList(matchingModelNamesList);
            }
        }
        return null;
    }
    

    /**
     * Execute a create attribute task which adds a new attribute to the database.
     * @param task The task that contains the new attribute to be created.
     * @return The XMLDBAttribute object that was stored in the Database.
     * @throws DBExecutionException Thrown if the operation fails.
     */
    public XMLDBAttribute executeCreateAttributeTask(CreateAttributeTask task) 
            throws DBExecutionException {
        
        try {
            
            _checkXMLDBConnectionObjects(true, true, true);


            
            XMLDBAttribute xmlDBAttribute = task.getXMLDBAttribute();
            

            
            String attributeName = xmlDBAttribute.getAttributeName();
            
            // check if the attribute already exists.            
            String query = "doc('dbxml:/" + _xmlContainer.getName() 
                + "/Attributes.ptdbxml')/attributes/attribute[@name='" 
                + attributeName + "']";
            
            XmlQueryContext xmlQueryContext = _xmlManager.createQueryContext();

            if (xmlQueryContext == null)
                throw new DBExecutionException(
                        "Failed to CreateAttributeTask - The Query context is null "
                                + "and cannot be used to execute queries.");
            
            
            XmlResults results = _xmlManager.query(
                    _xmlTransaction, query, xmlQueryContext, null);
                        
        
            if (results != null && results.size() > 0) {
                
                throw new DBExecutionException(
                        "Failed to CreateAttributeTask - An attribute with the "
                                + "same name already exist.");
            }
            
            //create the attribute id as a combination of the name and time stamp.
            Date date = new Date ();
            
            String attributeId = attributeName + "_" + date.getTime();
            
            xmlDBAttribute.setAttributeId(attributeId);

            String attributeNode = xmlDBAttribute.getAttributeXMLStringFormat();
            
            String insertQuery = "insert node " + attributeNode + " into doc('dbxml:/"
                + _xmlContainer.getName() + "/Attributes.ptdbxml')/attributes";
            
            
            _xmlManager.query(insertQuery, xmlQueryContext, null);
            
            return xmlDBAttribute;
             
            
        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute CreateAttributeTask - "
                    + e.getMessage(), e);
        }
        
    }
    

    /**
     * Execute delete attribute task which deletes a given attribute from the database.
     * @param task The task that contains the attribute to be deleted.
     * @throws DBExecutionException Thrown if the operation fails.
     */
    public void executeDeleteAttributeTask(DeleteAttributeTask task) 
            throws DBExecutionException {
        
        try {
            
            _checkXMLDBConnectionObjects(true, true, true);

            
            XMLDBAttribute xmlDBAttribute = task.getXMLDBAttribute();
            
            
            String attributeId = xmlDBAttribute.getAttributeId();
            
            
            XmlQueryContext xmlQueryContext = _xmlManager.createQueryContext();

            if (xmlQueryContext == null)
                throw new DBExecutionException(
                        "Failed to DeleteAttributeTask - The Query context is null "
                                + "and cannot be used to execute queries.");
            
            String query = "delete node doc('dbxml:/" + _xmlContainer.getName() 
                + "/Attributes.ptdbxml')/attributes/attribute[@id='" 
                + attributeId + "']";
            
            _xmlManager.query(_xmlTransaction, query, xmlQueryContext, null);        
            
        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute DeleteAttributeTask - "
                    + e.getMessage(), e);
        }
    }
    
    

    /**
     * Search models that contain given graphical pattern in the database.
     * 
     * @param task Task that contains the graph search criteria.
     * 
     * @return List of models that match the given search criteria.
     * @exception DBExecutionException If thrown while searching in the
     * database.
     */
    public ArrayList<XMLDBModel> executeGraphSearchTask(GraphSearchTask task)
            throws DBExecutionException {

        ArrayList<XMLDBModel> modelList = null;
        ArrayList<String> modelFilesList = null;
        DBGraphSearchCriteria criteria = task.getGraphSearchCriteria();
        ArrayList<Port> portsList = criteria.getPortsList();
        ArrayList<ComponentEntity> actorsList = criteria
                .getComponentEntitiesList();
        if (portsList != null) {
            for (Port port : portsList) {

                String portQuery = _createPortSearchQuery(port);

                if (portQuery != null && portQuery.length() > 0) {

                    modelFilesList = _executeGraphSearchQuery(portQuery,
                            modelFilesList);

                    if (modelFilesList.size() == 0) {
                        return new ArrayList<XMLDBModel>();
                    }
                }
            }
        }

        if (modelFilesList == null || modelFilesList.size() > 0) {
            if (actorsList != null) {
                HashSet<String> evaluatedPairs = new HashSet();
                HashSet<String> actorsAlreadySearched = new HashSet();
                boolean isSearched;

                for (ComponentEntity actor : actorsList) {

                    isSearched = false;
                    if (actor.portList() != null) {
                        for (Object object : actor.portList()) {
                            Port portAttached = (Port) object;

                            if (portAttached.connectedPortList() != null) {
                                for (Object object1 : portAttached
                                        .connectedPortList()) {
                                    Port portConnected = (Port) object1;

                                    if (portConnected.getContainer() != null
                                            && portConnected.getContainer() 
                                            instanceof ComponentEntity) {

                                        String componentQuery = _createComponentSearchQuery(
                                                actor, portAttached,
                                                portConnected,
                                                (ComponentEntity) portConnected
                                                        .getContainer(),
                                                evaluatedPairs);

                                        if (componentQuery != null) {
                                            modelFilesList = _executeGraphSearchQuery(
                                                    componentQuery,
                                                    modelFilesList);

                                            if (modelFilesList.size() == 0) {
                                                return new ArrayList<XMLDBModel>();
                                            }

                                            isSearched = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!isSearched) {
                        /*
                         * If the Graph Search Query was not executed, then search 
                         * for the given entity in all the models in the database. 
                         */
                        String singleActorQuery = _createSingleActorQuery(
                                actor, actorsAlreadySearched);

                        if (singleActorQuery != null) {

                            modelFilesList = _executeGraphSearchQuery(
                                    singleActorQuery, modelFilesList);

                            if (modelFilesList.size() == 0) {
                                return new ArrayList<XMLDBModel>();
                            }
                        }
                    }
                }
            }
        }

        if (modelFilesList != null && modelFilesList.size() > 0) {
           
            return _getDistinctModelsList(modelFilesList);
            
        }
        return modelList;
    }

    /**
     * Execute the necessary commands to save/update a model in the database
     * according to the model specification given in the task parameter.
     * 
     * @param task a SaveModelTask to be completed.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

        try {
            
            _checkXMLDBConnectionObjects(true, false, true);


            XMLDBModel xmlDBModel = task.getXMLDBModel();


            XmlDocument currentDbModel = null;

            try {
                currentDbModel = _xmlContainer.getDocument(_xmlTransaction,
                        xmlDBModel.getModelName());
            } catch (XmlException e) {
                //do nothing
            }

            if (currentDbModel == null) {

                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the model does not exist in the database."
                                + " Please use executeCreateModelTask instead.");
            } else {

                String modelBody = xmlDBModel.getModel();

                if (modelBody.indexOf("<!DOCTYPE") >= 0) {
                    
                    modelBody = modelBody.substring(modelBody.indexOf("<!DOCTYPE"));
                    modelBody = modelBody.substring(modelBody.indexOf(">") + 1);
                }

//                currentDbModel.setContent(modelBody);
                
                _xmlContainer.deleteDocument(_xmlTransaction, currentDbModel);
                
                _xmlContainer.putDocument(_xmlTransaction, xmlDBModel.getModelName(), modelBody);
                
//                _xmlContainer.updateDocument(_xmlTransaction, currentDbModel);
            }

        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute SaveModelTask - "
                    + e.getMessage(), e);
        }

    }
    
    /**
     * Execute remove models task to delete a list of models from the database.
     * @param task Contains a list of models to be deleted from the database.
     * @throws DBExecutionException Thrown if the operation fails.
     */
    public void executeRemoveModelsTask (RemoveModelsTask task) 
            throws DBExecutionException {
        
        try {
            
            _checkXMLDBConnectionObjects(true, false, true);
            
            ArrayList<XMLDBModel> modelsList = task.getModelsList();
            
            if (modelsList != null && modelsList.size() > 0) {
                
                for (XMLDBModel xmlDBModel : modelsList) {
                    
                    _xmlContainer.deleteDocument(
                            _xmlTransaction, xmlDBModel.getModelName());
                }
                
            }
            
        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute RemoveModelsTask - "
                    + e.getMessage(), e);
        }
        
        
    }

    /**
     * Execute update attribute task to update a given attribute in the database.
     * @param task The task that holds the attribute to be updated.
     * @throws DBExecutionException Thrown if the operation fails with an error.
     */
    public void executeUpdateAttributeTask(UpdateAttributeTask task)
            throws DBExecutionException {
        
        try {
            
            _checkXMLDBConnectionObjects(true, true, true);

            XMLDBAttribute xmlDBAttribute = task.getXMLDBAttribute();
            
            

            // check if the attribute already exists.            
                
            String query = "doc('dbxml:/" + _xmlContainer.getName() 
                    + "/Attributes.ptdbxml')/attributes/attribute[@id='" 
                    + xmlDBAttribute.getAttributeId() + "']";
                
                
            XmlQueryContext xmlQueryContext = _xmlManager.createQueryContext();

                
            if (xmlQueryContext == null)
                throw new DBExecutionException(
                        "Failed to DeleteAttributeTask - The Query context is null "
                        + "and cannot be used to execute queries.");
                
                
                
            XmlResults results = _xmlManager.query(
                    _xmlTransaction, query, xmlQueryContext, null);
                            
            
                
            if (results == null || results.size() == 0) {                    
                throw new DBExecutionException(
                        "Failed to UpdateAttributeTask  - The attribute does not "                                    
                        + "exist.");
            }
            
            
            String attributeNode = xmlDBAttribute.getAttributeXMLStringFormat();
            
            query = "replace node doc('dbxml:/"+ _xmlContainer.getName() 
                    + "/Attributes.ptdbxml')/attributes/attribute[@id='" 
                    + xmlDBAttribute.getAttributeId() + "'] with "+ attributeNode;

            if (xmlQueryContext == null)
                throw new DBExecutionException(
                        "Failed to UpdateAttributeTask - The Query context is null "
                                + "and cannot be used to execute queries.");

            _xmlManager.query(_xmlTransaction, query, xmlQueryContext, null);        
            
        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute UpdateAttributeTask - "
                    + e.getMessage(), e);
        }
    }

    

    /**
     * Provide information regarding the state of the internal variables useful
     * for unit testing purposes.
     * @return String representation for state of internal variables.
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
     * Build the complete model by resolving all the references in it.
     * 
     * @param currentNode The node in the reference file that points to the model.
     * @return Model body without references in xml format.
     * @exception DBExecutionException Thrown if there is a problem executing
     * @exception DBConnectionException Thrown if the connection to the cache failed.
     * the task.
     */
    private String _buildCompleteModel(Node currentNode)
            throws DBExecutionException, DBConnectionException {

        if (_xmlModelHerarichyMap == null) {
            _xmlModelHerarichyMap = new HashMap<String, String>();
        }

        NamedNodeMap attributes = currentNode.getAttributes();

        String currentModelName = "";

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {

                Node node = attributes.item(i);

                if (node.getNodeName().equalsIgnoreCase("name")) {

                    currentModelName = node.getNodeValue();

                    break;

                }
            }
        }

        if (currentModelName != null && currentModelName.length() > 0) {

            if (_xmlModelHerarichyMap.containsKey(currentModelName)) {

                return (String) _xmlModelHerarichyMap.get(currentModelName);
            }

            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the XmlContainer object was not instantiated properly");
            }

            XmlDocument currentDbModel = null;
            
            XMLDBModel currentXMLDBModel = null;

            String currentModelContent = "";
            
            boolean isLoadedFromCache = true;

            try {

                try {
                    currentXMLDBModel = CacheManager.loadFromCache(currentModelName);
                } catch (Exception e) {
                    //do nothing...
                }
               
                if (currentXMLDBModel == null) {
                    
                    currentDbModel = _xmlContainer.getDocument(currentModelName);
                    isLoadedFromCache = false;
                }

                if (currentDbModel == null && currentXMLDBModel == null) {

                    throw new DBExecutionException(
                            "Failed to execute GetModelsTask"
                                    + " - Could not find a model with the name "
                                    + currentModelName
                                    + " in the database when"
                                    + " trying to get the references.");

                }

                if (isLoadedFromCache) {
                    
                    currentModelContent = currentXMLDBModel.getModel();
                    
                } else {
                    
                    currentModelContent = currentDbModel.getContentAsString();
                }
                

            } catch (XmlException e) {

                throw new DBExecutionException(
                        "Failed to execute GetModelTask - " + e.getMessage(),
                        e);
            }

            if (currentNode.hasChildNodes() && isLoadedFromCache == false) {

                NodeList children = currentNode.getChildNodes();

                for (int i = 0; i < children.getLength(); i++) {

                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        Node child = children.item(i);

                        String childContent = _buildCompleteModel(child);

                        childContent = childContent.substring(childContent
                                .indexOf("<entity"));
                        
                        String childName =  childContent.substring(childContent
                                .indexOf("name=\"") + 6);
                        
                        childName = childName.substring(0, childName
                                .indexOf("\""));

                        String childBodyContent = childContent.substring(childContent
                                .indexOf(">") + 1);
                        
                        childBodyContent = childBodyContent.substring(
                                0 , childBodyContent.lastIndexOf("</entity>"));
                        
                        
                        currentModelContent = _replaceReferenceWithContent(
                                currentModelContent, childBodyContent, childName);
                        

                        currentModelContent = currentModelContent.replaceAll(
                                ">", ">\n");
                    }
                }
            }

            _xmlModelHerarichyMap.put(currentModelName, currentModelContent);
            
            
            

            return currentModelContent;

        } else {
            return "";
        }
    }
    
    

    /**
     * Check if the connection is alive/open.
     * @exception DBConnectionException If the connection is not alive/open.
     */
    private void _checkConnectionAlive() throws DBConnectionException {
        if (!_isConnectionAlive) {
            throw new DBConnectionException(
                    "This connection is not alive anymore. "
                            + "It has been closed.");
        }
    }

    /**
     * Check if the transaction is active.
     * @exception DBConnectionException If the connection is not alive
     * 
     */
    private void _checkTransactionActive() throws DBConnectionException {
        if (!_isTransactionActive) {
            throw new DBConnectionException(
                    "The transaction is no longer active. "
                            + "It has already been committed or aborted.");
        }
    }
    
    /**
     * Check the xml db connection objects (xmlContainer, xmlManager, xmlTransaction
     * to see if they are set properly.
     * 
     * @param checkContainer Boolean to indicate that the caller is using the 
     * Container object and needs to check if it is instantiated properly.
     * @param checkManager Boolean to indicate that the caller is using the 
     * Manager object and needs to check if it is instantiated properly.
     * @param checkTransaction Boolean to indicate that the caller is using
     * a transaction and needs to check if it is instantiated properly.
     * 
     * @throws DBExecutionException Thrown if one of the objects for handling 
     * XMLDBConnection operations is not set properly.
     */
    private void _checkXMLDBConnectionObjects(
            boolean checkContainer, boolean checkManager, 
            boolean checkTransaction) throws DBExecutionException {
        
        if (checkContainer && _xmlContainer == null) {
            throw new DBExecutionException(
                    "The XmlContainer object was not "
                    + "instantiated properly");
        }

        if (checkManager && _xmlManager == null) {
            throw new DBExecutionException(
                    "The XmlManager object was not "
                    + "instantiated properly");
        }
        
        if (checkTransaction && _xmlTransaction == null) {
            
            throw new DBExecutionException(
                    "The XmlTransaction object was not "   
                    + "instantiated properly");
        }
    }

    /**
     * Close the environment and invoke delete on the container that closes the
     * connection and releases the resources.
     * 
     * @exception DBConnectionException If thrown while closing the environment.
     */
    private void _cleanUp() throws DBConnectionException {
        try {

            if (_xmlTransaction != null) {
                _xmlTransaction.delete();
            }

            if (_xmlContainer != null) {
                _xmlContainer.close();
                _xmlContainer.delete();
            }

            /*if (_environment != null)
                _environment.close();
            */
        } catch (DatabaseException e) {
            e.printStackTrace();
            throw new DBConnectionException(
                    "Database transaction could not be committed - "
                            + e.getMessage(), e);

        }
    }

    /**
     * Build an XMLDBAttribute from a given SAX node.
     * @param node the node that represent the attribute. 
     * @return XMLDBAttribute object.
     */
    private XMLDBAttribute _constructXMLDBAttribute(Node node) {
        
        XMLDBAttribute xmlDBAttribute = null;
        String attributeName = "";
        String attributeType = "";
        String attributeId = "";
        List<String> itemValues = new ArrayList<String>();
        
        if (node != null) {
            
            NamedNodeMap attributes = node.getAttributes();
            
            
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attributeNode = attributes.item(i);
                
                if (attributeNode.getNodeName().equalsIgnoreCase("id")) {
                    
                    attributeId = attributeNode.getNodeValue();
                    
                } else if (attributeNode.getNodeName().equalsIgnoreCase("name")) {
                    
                    attributeName = attributeNode.getNodeValue();
                    
                } else if (attributeNode.getNodeName().equalsIgnoreCase("type")) {
                    
                    attributeType = attributeNode.getNodeValue();
                    
                    if (attributeType.equalsIgnoreCase(XMLDBAttribute.ATTRIBUTE_TYPE_LIST)) {
                        NodeList items = node.getChildNodes();
                        
                        for (int j = 0; j < items.getLength(); j++) {
                            if (items.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                
                                Node itemNode = items.item(j).getAttributes().getNamedItem("name");
                                itemValues.add(itemNode.getNodeValue());
                            }
                        }
                    }
                }
            }
            
            xmlDBAttribute = new XMLDBAttribute(
                    attributeName, attributeType, attributeId);
            
            if (itemValues != null && itemValues.size() > 0) {
                xmlDBAttribute.setAttributeValue(itemValues);
            }
            
            
        } 
        
        return xmlDBAttribute;
    }
    
    
    /**
     * Create the attribute sub-query for the given attribute.
     * 
     * @param attribute Attribute for which the sub-query needs to be created.
     * 
     * @return Sub-query for the given attribute.
     * @exception IllegalActionException If thrown whie retrieving attribute
     * data.
     */
    private String _createAttributeClause(Variable attribute)
            throws IllegalActionException {

        StringBuffer attributesQuery = new StringBuffer();
        if (attribute.getName() != null
                && !"".equals(attribute.getName().trim())) {
            attributesQuery.append("$const/@name=\"").append(
                    attribute.getName()).append("\"");

            attributesQuery.append(" and ");
        }

        if (attribute.getToken() != null) {
            attributesQuery.append("$const/@value[contains(.,\"").append(
                    _removeQuotes(attribute.getToken().toString())).append(
                    "\")]");

            attributesQuery.append(" and ");
        }

        attributesQuery.append("$const/@class=\"").append(
                attribute.getClassName()).append("\"");

        return attributesQuery.toString();

    }

    /** Create a graph query for the given combination of 
     * actor - port - port- actor only if it has already not been evaluated.
     * 
     * @param actor The first actor in the pattern.
     * @param portAttached The port for the first actor in the pattern.     
     * @param portConnected The port of the second actor in the two actor 
     * pattern.
     * @param actorConnected The second actor in the two-actor pattern.
     * @param evaluatedPairs Strings that represent already evaluated two-actor
     * patterns.
     * @return The two-actor pattern xQuery if the pattern has not already been 
     * evaluated, else return null. 
     */
    private String _createComponentSearchQuery(ComponentEntity actor,
            Port portAttached, Port portConnected,
            ComponentEntity actorConnected, HashSet evaluatedPairs) {

        String componentQuery = null;
        String actorIdentifier = actor.getClassName();
        String portAttachedIdentifier = portAttached.getName();
        String portConnectedIdentifier = portConnected.getName();
        String actorConnectedIdentifier = actorConnected.getClassName();

        String key = actorIdentifier + " - " + portAttachedIdentifier + " - "
                + portConnectedIdentifier + " - " + actorConnectedIdentifier;
        String keyReverse = actorConnectedIdentifier + " - "
                + portConnectedIdentifier + " - " + portAttachedIdentifier
                + " - " + actorIdentifier;

        if (!evaluatedPairs.contains(key)
                && !evaluatedPairs.contains(keyReverse)) {

            evaluatedPairs.add(key);
            evaluatedPairs.add(keyReverse);

            componentQuery = "for $entity1 in collection(\""
                    + _params.getContainerName()
                    + "\")/entity/entity [@class=\""
                    + actorIdentifier
                    + "\"] "
                    + " return "
                    + " for $entity2 in collection(\""
                    + _params.getContainerName()
                    + "\")/entity/entity "
                    + " [@class=\""
                    + actorConnectedIdentifier
                    + "\"] "
                    + " where $entity1/@name != $entity2/@name "
                    + " and base-uri($entity1) = base-uri($entity2) "
                    + " return "
                    + " for $link1 in collection (\""
                    + _params.getContainerName()
                    + "\")/entity/link "
                    + " where $link1/@port[starts-with(., concat($entity1/@name, \"."
                    + portAttachedIdentifier + "\"))] "
                    + " and base-uri($link1) = base-uri($entity1) "
                    + " return " + " for $link2 in collection(\""
                    + _params.getContainerName() + "\")/entity/link "
                    + " [@port[starts-with(.,concat($entity2/@name,\"."
                    + portConnectedIdentifier + "\"))]] "
                    + " where $link1/@relation = $link2/@relation "
                    + " and base-uri($link1) = base-uri($link2) "
                    + " return base-uri($link1) ";

        }
        return componentQuery;
    }

    /**
     * Create the parent hierarchy for the given base model.
     * 
     * @param currentNode Current node for which processing needs to be done.
     * @param parentNodeName Parent node for the current node.
     * @param dBModelsMap Map that contains all the DBModels as they are created
     * so that their parent lists can be populated.
     * @param baseModel Base model for which the hierarchy is being created.
     */
    private void _createParentHierarchy(Node currentNode,
            String parentNodeName, HashMap<String, DBModel> dBModelsMap,
            XMLDBModel baseModel) {
        /*
         * If the currentNode is not already visited
         * and it is not the base model,
         * then visit all its children and call
         * _createParentHierarchy over them.
         *
         * If the current model is already visited,
         * then add the current model to the parent list.
         */
        String currentNodeName = Utilities.getValueForAttribute(currentNode, "name");
        //System.out.println(parentNodeName + " - " + currentNodeName);
        if (currentNodeName != null) {

            if (!dBModelsMap.containsKey(currentNodeName)
                    && currentNode.hasChildNodes()) {
                NodeList children = currentNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Node child = children.item(i);
                        _createParentHierarchy(child, currentNodeName,
                                dBModelsMap, baseModel);
                    }
                }
            }
            if (parentNodeName != null
                    && dBModelsMap.containsKey(currentNodeName)) {
                DBModel currentDBModel = dBModelsMap.get(currentNodeName);
                DBModel parentDBModel = new DBModel(parentNodeName);
                //System.out.println("Adding parent " + parentNodeName
                //        + " for child " + currentNodeName);
                currentDBModel.addParent(parentDBModel);
                dBModelsMap.put(parentNodeName, parentDBModel);
            }
        }
    }
    /**
     * Create a query to search for ports within the model in the database.
     * @param port Port for which the query needs to be created.
     * @return Query to search for ports within the models in the database.
     */
    private String _createPortSearchQuery(Port port) {

        StringBuffer portSearchQuery = new StringBuffer();

        portSearchQuery
                .append(" for $x in collection(\"")
                .append(_params.getContainerName())
                .append(
                        "\")/entity/port[@class = \"ptolemy.actor.TypedIOPort\"] where ");
        boolean isFirstClause = true;
        /*
         * If the port is an output port, 
         * search for port with property called input.
         */
        if (((IOPort) port).isInput()) {
            portSearchQuery.append(" $x/property[@name=\"input\"] ");
            isFirstClause = false;
        }
        /*
         * If the port is an output port, 
         * search for port with property called output.
         */
        if (((IOPort) port).isOutput()) {
            if (!isFirstClause) {
                portSearchQuery.append(" and ");
            }
            portSearchQuery.append(" $x/property[@name=\"output\"] ");
            isFirstClause = false;
        }

        /*
         * If the port is a multi-port, 
         * search for port with property called multiport.
         */
        if (((IOPort) port).isMultiport()) {
            if (!isFirstClause) {
                portSearchQuery.append(" and ");
            }
            portSearchQuery.append(" $x/property[@name=\"multiport\"] ");
            isFirstClause = false;
        }

        portSearchQuery.append(" return base-uri($x) ");

        return portSearchQuery.toString();
    }
    
    /** Create an xQuery to search for a single actor within the models in the 
     * database. 
     * 
     * @param actor The actor that needs to be searched. 
     * @param actorsAlreadySearched List of already searched actors.
     * @return Query to search for a single actor within the models in the 
     * database.
     */
    private String _createSingleActorQuery(ComponentEntity actor,
            HashSet actorsAlreadySearched) {
        String singleActorQuery = null;
        if (!actorsAlreadySearched.contains(actor.getClassName())) {

            actorsAlreadySearched.add(actor.getClassName());
            singleActorQuery = "for $entity1 in collection(\""
                    + _params.getContainerName()
                    + "\")/entity/entity[@class=\"" + actor.getClassName()
                    + "\"] return base-uri($entity1)";
        }

        return singleActorQuery;

    }
    
    /**
     * 
     * @param query Search xQuery that needs to be executed.  
     * @param matchedModelsList Models within which we need to search for the 
     * given query criterion.
     * @return Returns a List of matching model names.
     * @throws DBExecutionException If thrown while searching in the database.
     */

    private ArrayList<String> _executeGraphSearchQuery(String query,
            ArrayList<String> matchedModelsList) throws DBExecutionException {
        ArrayList<String> modelsList = new ArrayList<String>();

        System.out.println("graphSearchQuery - " + query);

        XmlQueryContext context;
        try {
            context = _xmlManager.createQueryContext();

            if (context == null)
                throw new DBExecutionException(
                        "Failed to executeAttributeSearch - The Query context is null "
                                + "and cannot be used to execute queries.");
            context.setEvaluationType(XmlQueryContext.Lazy);

            XmlResults results = _xmlManager.query(query, context, null);
            if (results != null) {
                XmlValue value;
                while (results.hasNext()) {
                    value = results.next();
                    modelsList.add(value.asString());
                }
            }
            /*
             *  Intersect results to find the matching models list from within 
             *  the given list.
             */
            if (matchedModelsList != null) {
                modelsList.retainAll(matchedModelsList);
            }

        } catch (XmlException e) {
            throw new DBExecutionException("Failed to executeGraphSearch - "
                    + e.getMessage(), e);
        }
        return modelsList;
    }

    /**
     * Execute the attribute search on the database for the given attribute
     * clause.
     * 
     * @param attributeClause Attribute sub-query for which search needs to be
     * done.
     * 
     * @return List of models matching the attribute search.
     * 
     * @exception XmlException If thrown while executing query.
     * @exception DBExecutionException Thrown if the query context is not
     * initialized properly.
     */
    private ArrayList<String> _executeSingleAttributeMatch(
            String attributeClause) throws XmlException, DBExecutionException {

        ArrayList<String> modelsList = new ArrayList<String>();

        String attributeSearchQuery = "for $const in collection(\""
                + _params.getContainerName() + "\")/entity/property where "
                + attributeClause + " " + " return base-uri($const)";

        System.out.println("attributeSearchQuery - " + attributeSearchQuery);

        XmlQueryContext context = _xmlManager.createQueryContext();
        if (context == null)
            throw new DBExecutionException(
                    "Failed to executeAttributeSearch - The Query context is null "
                            + "and cannot be used to execute queries.");
        context.setEvaluationType(XmlQueryContext.Lazy);

        XmlResults results = _xmlManager.query(attributeSearchQuery, context,
                null);
        if (results != null) {
            XmlValue value;
            while (results.hasNext()) {
                value = results.next();
                modelsList.add(value.asString());
            }
        }

        return modelsList;
    }

    /**
     * Fetch the parent hierarchies for the given model.
     * 
     * @param model The model for which the parent hierarchies are required.
     * 
     * @exception DBExecutionException If thrown while fetching model hierarchy.
     * @throws XMLDBModelParsingException If thrown while parsing the model.
     */
    private XMLDBModel _fetchHierarchyForModel(XMLDBModel model)
            throws DBExecutionException, XMLDBModelParsingException {
        /*
         * Fetch references from database reference file.
         */
        String referencesXML = _getParentHierarchiesForModelFromDB(model);
        if (referencesXML != null) {
            /*
             * Create document for the given references XML.
             */
            Document document = (Document) Utilities.parseXML(referencesXML);
            /*
             * "entities" is the root tag which contains entity tags.
             * For every child entity tag populate the dbModelsMap
             * with parent hierarchy.
             */
            Node firstNode = document.getElementsByTagName("entities").item(0);
            if (firstNode != null) {
                HashMap<String, DBModel> dBModelsMap = new HashMap<String, DBModel>();
                String modelName = model.getModelName();
                dBModelsMap.put(modelName, new DBModel(modelName));
                NodeList children = firstNode.getChildNodes();

                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Node child = children.item(i);
                        //                       System.out.println("Start of New Entity");
                        _createParentHierarchy(child, null, dBModelsMap, model);
                    }
                }

                DBModel baseDBModel = dBModelsMap.get(model.getModelName());
                /*
                 * Get unique and maximal hierarchies from the baseDBModel
                 */
                _populateParentList(model, baseDBModel,
                        new LinkedList<XMLDBModel>(), model);
            }
        }
        return model;
    }

    /**
     * Get all the parent entries in the reference file where the given model is
     * a child entity.
     * 
     * @param model Model for which the parent references are to be fetched.
     * 
     * @return String representation of the matching reference XML entities.
     * 
     * @exception DBExecutionException If thrown while fetching parent
     * hierarchies from database.
     */
    private String _getParentHierarchiesForModelFromDB(XMLDBModel model)
            throws DBExecutionException {
        StringBuffer referencesXMLBuf = null;

        String fetchHierarchyQuery = "doc(\""
                + _params.getContainerName()
                + "/ReferenceFile.ptdbxml\")/reference/*[descendant::entity[attribute::name=\""
                + model.getModelName() + "\"]]";

        try {
            XmlQueryContext context = _xmlManager.createQueryContext();
            if (context == null)
                throw new DBExecutionException(
                        "Failed to executeFetchHierarchy - The Query context is null and "
                                + "cannot be used to execute queries.");
            XmlResults results = _xmlManager.query(fetchHierarchyQuery,
                    context, null);
            /*
             * Create well formed XML for document parser to parse.
             */
            if (results != null) {
                XmlValue value;
                while (results.hasNext()) {
                    value = results.next();
                    if (referencesXMLBuf == null) {
                        referencesXMLBuf = new StringBuffer();
                    }
                    referencesXMLBuf.append(value.asString());
                }
            }
        } catch (Exception e) {
            throw new DBExecutionException(
                    "Error while fetching model hierachy - "
                            + model.getModelName(), e);
        }

        String referencesXML = referencesXMLBuf != null ? "<entities>"
                + referencesXMLBuf.toString() + "</entities>" : null;
        return referencesXML;
    }

    /**
     * Create a list of distinct XMLDBModels from the given list that may 
     * contain duplicates.
     *  
     * @param modelNamesList List of models that may contain duplicates.
     * @return List of distinct XMLDBModels.
     */
    private ArrayList<XMLDBModel> _getDistinctModelsList(
            ArrayList<String> modelNamesList) {

        ArrayList<XMLDBModel> finalModelsList = new ArrayList<XMLDBModel>();
        XMLDBModel model;
        HashSet resultModelNames = new HashSet();
        for (String modelName : modelNamesList) {
            if (!resultModelNames.contains(modelName)) {

                model = new XMLDBModel(modelName.substring(modelName
                        .lastIndexOf("/") + 1));

                finalModelsList.add(model);
                resultModelNames.add(modelName);
            }
        }
        return finalModelsList;
    }
    
    /**
     * Return the model from the database as XmlDocument.
     * 
     * @param task GetModelTask that contains the model name.
     * @return The model as XmlDocument.
     * @throws DBExecutionException Thrown if the operation fails.
     */
    private XmlDocument _getModelFromDB(GetModelTask task)
            throws DBExecutionException {
        
        _checkXMLDBConnectionObjects(true, false, false);

        XmlDocument dbModel;
        try {
            
            dbModel = _xmlContainer.getDocument(task.getModelName());
            
        } catch (XmlException e) {

            throw new DBExecutionException(
                    "Failed to execute GetModelsTask"
                            + " - Could not find the model in the database. "
                            + e.getMessage(), e);
        }
        return dbModel;
    }

    /**
     * Retrieve the references inside a model from the reference file in the
     * database.
     * 
     * @param modelName The model name for which the references are required.
     * @return A string representation of all the references in the given model
     * in xml format.
     * @exception DBExecutionException Thrown if there is an execution
     * exception.
     */
    private String _getModelReferences(String modelName)
            throws DBExecutionException {
        String reference = "";

        try {
            
            _checkXMLDBConnectionObjects(true, true, false);

            
            XmlQueryContext xmlContext = _xmlManager.createQueryContext();

            if (xmlContext == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - could not create an xml query context from the xml manager.");
            }

            String query = "doc('dbxml:/" + _xmlContainer.getName()
                    + "/ReferenceFile.ptdbxml')" + "/reference/entity[@name='"
                    + modelName + "']";

            XmlQueryExpression queryExpression = _xmlManager.prepare(query,
                    xmlContext);

            if (queryExpression == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - could not create an xml query expression from the xml manager.");
            }

            XmlResults results = queryExpression.execute(xmlContext);

            if (results != null && results.size() > 0) {

                XmlValue xmlValue = results.next();
                reference = xmlValue.asString();
            }

            return reference;

        } catch (XmlException e) {
            throw new DBExecutionException(
                    "Failed to retrieve the references for the given model - "
                            + e.getMessage(), e);
        }

    }

//    /**
//     * Return the upper level entity node in a model content that is being
//     * passed to it as a string.
//     * 
//     * @param strModelContent The model content as a string in xml format.
//     * @return A string representation of the upper level node.
//     * @exception DBExecutionException Thrown if there is a problem executing
//     * the task.
//     */
//    private static String _getParentEntityNodeAsString(String strModelContent)
//            throws DBExecutionException {
//
//        String parentNode = null;
//
//        try {
//
//            if (strModelContent == null || strModelContent.length() == 0) {
//                throw new DBExecutionException(
//                        "Faild to extract entity node from the xml content - "
//                                + "content sent is empty or null");
//            }
//
//            parentNode = strModelContent.substring(strModelContent
//                    .indexOf("<entity"), strModelContent.indexOf(">") + 1);
//
////            parentNode = parentNode.replace(">", "/>");
//
//        } catch (IndexOutOfBoundsException e) {
//            throw new DBExecutionException(
//                    "Faild to extract entity node from the xml content - "
//                            + e.getMessage(), e);
//        }
//        return parentNode;
//    }

    
    
    /**
     * Populate the base model with the maximal parent hierarchies.
     * 
     * @param model XMLDBModel in the hierarchy.
     * @param dbModel DBModel for the above XMLDBModel.
     * @param childHierarchy List that contains the list if previous models
     * visited in the hierarchy.
     * @param baseModel Base model for which the parent hierarchies need to be
     * populated.
     */

    private void _populateParentList(XMLDBModel model, DBModel dbModel,
            LinkedList<XMLDBModel> childHierarchy, XMLDBModel baseModel) {

        /*
         * If the current model has parents, then
         * add the current model to the list, and for every child
         * call _populateParentList. Remove the current model
         * from the list after all children have been visited.
         *
         *  If the current model has no parents, then
         *  add the current model to the linked list and
         *  add the linked list to the list of parents
         *  in the base model.
         */
        ArrayList<DBModel> parentsList = dbModel._parentsList;
        if (parentsList != null && parentsList.size() > 0) {

            childHierarchy.addFirst(model);
            for (DBModel parentDBModel : parentsList) {

                XMLDBModel parentXMLDBModel = new XMLDBModel(
                        parentDBModel._modelName);

                _populateParentList(parentXMLDBModel, parentDBModel,
                        childHierarchy, baseModel);
            }
            childHierarchy.removeFirst();
        } else {
            LinkedList<XMLDBModel> xmlDBModelParentsList = new LinkedList<XMLDBModel>();
            xmlDBModelParentsList.addAll(childHierarchy);
            xmlDBModelParentsList.addFirst(model);
            baseModel.addParentList(xmlDBModelParentsList);
        }

    }
    
    /**
     * Remove the double quotes from the given value.
     * @param originalString String from which we want to
     * remove double quotes.
     * @return OriginalString without double quotes.
     */
    private String _removeQuotes(String originalString) {
        return originalString.replaceAll("\"", "");
    }
    
    /**
     * Replace the reference tags in a given model with the reference content.
     * @param modelContent The model content.
     * @param referenceModelContent The content that needs to be inserted in the parent model.
     * @param referenceModelName The name of the model being referenced.
     * @return A string representation of the parent model with the reference replaced.
     */
    private String _replaceReferenceWithContent(
            String modelContent, String referenceModelContent, 
            String referenceModelName) {
        
        StringBuffer parentModelContentBuffer = new StringBuffer(modelContent);
        String referenceString = "<entity dbmodelname=\"" + referenceModelName + "\"";
        
        int startPosition = 0;
        boolean isDone = false;
        
        while(!isDone) {
            
            int refIndex = parentModelContentBuffer.indexOf(referenceString, startPosition);
            
            if (refIndex != -1) {
                parentModelContentBuffer.replace(refIndex, 
                        refIndex + referenceString.length(), "<entity");
        
                int refContentIndex = parentModelContentBuffer.indexOf(">", refIndex) + 1;
        
                parentModelContentBuffer.insert(refContentIndex, referenceModelContent);
        
                startPosition = refContentIndex + referenceModelContent.length();
            } else {
                
                isDone = true;
                
            }  
        }

        return parentModelContentBuffer.toString(); 
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The environment for the Oracle Berkeley XMLDatabase that defines the
     * parameters like cache size, locking mechanism, storing mechanism etc.
     * required for creating a database connection.
     */
    private Environment _environment;
    /**
     * Denote whether the database connection is active or not
     */
    private boolean _isConnectionAlive;

    /**
     * Denote whether the database connection is active or not
     */
    private boolean _isTransactionActive;

    /**
     * This object contains the parameters like path, container name etc.
     * required to connect with the database
     */
    private DBConnectionParameters _params;

    /**
     * This is the Oracle Berkeley XML DB Container that is used to add and
     * remove documents.
     */
    private XmlContainer _xmlContainer;

    /**
     * This is the Oracle BerkeleyXML DB manager that is used to execute queries
     * over the opened container
     */
    private XmlManager _xmlManager;

    /**
     * This is a hash map to store the complete models without any references.
     */
    private HashMap<String, String> _xmlModelHerarichyMap;

    /**
     * This is the Oracle Berkeley XML DB Transaction that is used to commit or
     * abort certain transactions. This will be set only if a transaction
     * enabled connection is requested.
     */
    private XmlTransaction _xmlTransaction;

    /**
     * Contain the parents list for a model.
     */
    private class DBModel {
        /**
         * Construct an instance with the given model name.
         */
        DBModel(String modelName) {
            _modelName = modelName;
        }

        /**
         * Add parent to model if the parent is not already present.
         * @param model Parent model to be added to parentslist.
         */
        public void addParent(DBModel model) {
            if (_parentsList == null) {
                _parentsList = new ArrayList<DBModel>();
            }
            if (!_parentsList.contains(model)) {
                _parentsList.add(model);
            }
        }

        /**
         * Name of the model
         */
        String _modelName;
        /**
         * Parents list for the model.
         */
        ArrayList<DBModel> _parentsList;

        /* *//**
         * Match the given DBModel to the current model
         * @param model Model to be compared.
         * @return True if the names are same, false otherwise.
         */
        /*
        public boolean equals(DBModel model) {
         if (model instanceof DBModel) {
             if (((DBModel) model)._modelName.equals(_modelName)) {
                 return true;
             } else {
                 return false;
             }
         }
         return false;
        }

        *//**
         * Return the hash code of the modelName as two models are equal if
         * their model names are equal. So their has codes are also derived from
         * model name.
         * @return Hash code of the model name.
         */
        /*
        public int hashCode() {
         return _modelName.hashCode();
        }*/
    }
}

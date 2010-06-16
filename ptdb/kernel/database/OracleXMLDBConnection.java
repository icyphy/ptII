package ptdb.kernel.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ptdb.common.dto.AttributeSearchTask;
import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.FetchHierarchyTask;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.GraphSearchTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptolemy.data.expr.Variable;
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
            config.setCacheSize(100 * 1024 * 1024); // 50MB
            config.setAllowCreate(true);
            config.setInitializeCache(true);
            config.setTransactional(true);
            config.setInitializeLocking(true);
            config.setInitializeLogging(true);
            config.setErrorStream(System.err);
            /*config.setMaxLockers(1000);
            config.setMaxLocks(1000);
            config.setMaxLockObjects(1000);*/
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
                _xmlTransaction.commit();
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
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException, ModelAlreadyExistException {

        try {

            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the XmlContainer object was not "
                                + "instantiated properly");
            }

            if (_xmlTransaction == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the XmlTransaction object was not "
                                + "instantiated properly");
            }

            if (task == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the CreateModelTask object passed was null");
            }

            XMLDBModel model = task.getXMLDBModel();

            if (model == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the XMLDBModel object passed in the "
                                + "CreateModelTask was null");
            }

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
                
                modelBody = modelBody.substring(modelBody.indexOf("<entity"));

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
            _fetchHierarchyForModel(model);
        }

        return modelsList;
    }

    /**
     * Get the attributes defined from the database.
     * @param task The criteria to get the attribute.
     * @return List of attributes stored in the database.
     * @exception DBExecutionException
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Execute the get models task and return the model requested as XMLDBModel
     * object.
     * @param task The GetModelsTask object that contains the model name.
     * @return Model retrieved from the database.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException {

        try {

            if (task == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the GetModelsTask object passed was null");
            }

            XmlDocument dbModel;
            try {
                dbModel = _xmlContainer.getDocument(task.getModelName());
            } catch (XmlException e) {

                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - Could not find the model in the database"
                                + e.getMessage(), e);
            }

            XMLDBModel completeXMLDBModel = null;

            if (dbModel != null) {

                completeXMLDBModel = new XMLDBModel(dbModel.getName());

                String completeModelBody = "";

                String references = _getModelReferences(dbModel.getName());

                if (references != null && references.length() > 0) {

                    Node modelNode = _parseXML(references);

                    modelNode = modelNode.getChildNodes().item(0);

                    completeModelBody = _buildCompleteModel(modelNode);

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

                XMLDBModel model;
                for (String modelName : matchingModelNamesList) {
                    model = new XMLDBModel();
                    model.setModelName(modelName.substring(modelName
                            .lastIndexOf("/") + 1));
                    finalModelsList.add(model);
                }
                return finalModelsList;
                
            }
        }
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Execute the necessary commands to save/update a model in the database
     * according to the model specification given in the task parameter.
     * 
     * @param Task to be completed. In this case SaveModelTask.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

        try {

            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the XmlContainer object was not "
                                + "instantiated properly");
            }

            if (_xmlTransaction == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the XmlTransaction object was not "
                                + "instantiated properly");
            }

            if (task == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the SaveModelTask object passed was null");
            }

            XMLDBModel xmlDBModel = task.getXMLDBModel();

            if (xmlDBModel == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the XMLDBModel object passed in the "
                                + "SaveModelTask was null");
            }

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
                
                modelBody = modelBody.substring(modelBody.indexOf("<entity"));
                
                currentDbModel.setContent(modelBody);
                _xmlContainer.updateDocument(_xmlTransaction, currentDbModel);
            }

        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute SaveModelTask - "
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
     * @param The node in the reference file that points to the model.
     * @return Model body without references in xml format.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    private String _buildCompleteModel(Node currentNode)
            throws DBExecutionException {

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

            XmlDocument currentDbModel;

            String currentModelContent = "";

            try {

                currentDbModel = _xmlContainer.getDocument(currentModelName);

                if (currentDbModel == null) {

                    throw new DBExecutionException(
                            "Failed to execute GetModelsTask"
                                    + " - Could not find a model with the name "
                                    + currentModelName
                                    + " in the database when"
                                    + " trying to get the references.");

                }

                currentModelContent = currentDbModel.getContentAsString();

            } catch (XmlException e) {

                throw new DBExecutionException(
                        "Failed to execute GetModelsTask - " + e.getMessage(),
                        e);
            }

            if (currentNode.hasChildNodes()) {

                NodeList children = currentNode.getChildNodes();

                for (int i = 0; i < children.getLength(); i++) {

                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        Node child = children.item(i);

                        String childContent = _buildCompleteModel(child);

                        childContent = childContent.substring(childContent
                                .indexOf("<entity"));

                        String childNode = _getParentEntityNodeAsString(childContent);

                        if (childNode != null && childNode.length() > 0) {

                            currentModelContent = currentModelContent
                                    .replaceAll(childNode, childContent);
                        }

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

        attributesQuery.append("$const/@name=\"").append(attribute.getName())
                .append("\"");

        attributesQuery.append(" and ");

        attributesQuery.append("$const/@value[contains(.,").append(
                attribute.getToken().toString()).append(")]");

        attributesQuery.append(" and ");

        attributesQuery.append("$const/@class=\"").append(
                attribute.getClassName()).append("\"");

        return attributesQuery.toString();

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
        String currentNodeName = _getValueForAttribute(currentNode, "name");

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

                currentDBModel.addParent(parentDBModel);
                dBModelsMap.put(parentNodeName, parentDBModel);
            }
        }
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
     * @exception DBExecutionException Thrown if hte query context is not
     * initialized properly.
     */
    private ArrayList<String> _executeSingleAttributeMatch(
            String attributeClause) throws XmlException, DBExecutionException {

        ArrayList<String> modelsList = new ArrayList<String>();

        String attributeSearchQuery = "for $const in collection(\""
                + _params.getContainerName() + "\")/entity/property where "
                + attributeClause + " " + " return base-uri($const)";

        //System.out.println("attributeSearchQuery - " + attributeSearchQuery);

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
     */
    private XMLDBModel _fetchHierarchyForModel(XMLDBModel model)
            throws DBExecutionException {
        /*
         * Fetch references from database reference file.
         */
        String referencesXML = _getParentHierarchiesForModelFromDB(model);
        if (referencesXML != null) {
            /*
             * Create document for the given references XML.
             */
            Document document = (Document) _parseXML(referencesXML);
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
        StringBuffer referencesXML = null;

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

                referencesXML = new StringBuffer();

                XmlValue value;
                while (results.hasNext()) {
                    value = results.next();
                    referencesXML.append(value.asString());

                }

                referencesXML.append("</entities>");
            }
        } catch (Exception e) {
            throw new DBExecutionException(
                    "Error while fetching model hierachy - "
                            + model.getModelName(), e);
        }
        return referencesXML != null ? referencesXML.toString() : null;
    }

    /**
     * Retrieve the references inside a model from the reference file in the
     * database.
     * 
     * @param The model name for which the references are required.
     * @return A string representation of all the references in the given model
     * in xml format.
     * @exception DBExecutionException Thrown if there is an execution
     * exception.
     */
    private String _getModelReferences(String modelName)
            throws DBExecutionException {
        String reference = "";

        try {

            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the XmlContainer object was not instantiated properly");
            }

            if (_xmlManager == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the XmlManager object was not instantiated properly");
            }

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

    /**
     * Return the upper level entity node in a model content that is being
     * passed to it as a string.
     * 
     * @param The model content as a string in xml format.
     * @return A string representation of the upper level node.
     * @exception DBExecutionException Thrown if there is a problem executing
     * the task.
     */
    private static String _getParentEntityNodeAsString(String strModelContent)
            throws DBExecutionException {

        String parentNode = null;

        try {

            if (strModelContent == null || strModelContent.length() == 0) {
                throw new DBExecutionException(
                        "Faild to extract entity node from the xml content - "
                                + "content sent is empty or null");
            }

            parentNode = strModelContent.substring(strModelContent
                    .indexOf("<entity"), strModelContent.indexOf(">") + 1);

            parentNode = parentNode.replace(">", "/>");

        } catch (IndexOutOfBoundsException e) {
            throw new DBExecutionException(
                    "Faild to extract entity node from the xml content - "
                            + e.getMessage(), e);
        }
        return parentNode;
    }

    /**
     * Get the value for the given attribute.
     * 
     * @param currentNode Node for which attribute value needs to be determined.
     * @param attributeName Name of the attribute.
     * @return Return the value for the given attribute. Return null if
     * attribute not present for the given node.
     */
    private String _getValueForAttribute(Node currentNode, String attributeName) {

        NamedNodeMap attributes = currentNode.getAttributes();
        String strCurrentModelName = null;

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {

                Node node = attributes.item(i);
                if (node.getNodeName().equalsIgnoreCase("name")) {
                    strCurrentModelName = node.getNodeValue();
                    break;
                }
            }
        }
        return strCurrentModelName;
    }

    /**
     * Parse the xml string that is passed to it and return the upper node of
     * that xml.
     * 
     * @param The xml string that needs to be parsed
     * @return The upper node for the xml string after parsing it.
     * @exception DBExecutionException Thrown if a parser exceptions was thrown
     */
    private Node _parseXML(String xmlString) throws DBExecutionException {

        if (xmlString == null || xmlString.length() == 0) {
            throw new DBExecutionException("Faild to parse the xml - "
                    + "content sent is empty or null");
        }

        DocumentBuilder docBuilder;

        Node firstNode = null;

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();

        if (docBuilderFactory == null) {
            throw new DBExecutionException(
                    "Faild to parse the xml - "
                            + "could not create a new instance of DocumentBuilderFactory.");
        }

        docBuilderFactory.setIgnoringElementContentWhitespace(true);

        try {

            docBuilder = docBuilderFactory.newDocumentBuilder();

            if (docBuilder == null) {
                throw new DBExecutionException("Faild to parse the xml - "
                        + "could not create a new instance of DocumentBuilder.");
            }

            InputSource inputSource = new InputSource();

            inputSource.setCharacterStream(new StringReader(xmlString));

            firstNode = docBuilder.parse(inputSource);

        } catch (ParserConfigurationException e) {

            throw new DBExecutionException("Failed to parse the model - "
                    + e.getMessage(), e);

        } catch (SAXException e) {
            throw new DBExecutionException("Failed to parse the model - "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new DBExecutionException("Failed to parse the model - "
                    + e.getMessage(), e);
        }

        return firstNode;
    }

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

        /**
         * Match the given DBModel to the current model
         * @param model Model to be compared.
         * @return True if the names are same, false otherwise.
         */
        public boolean equals(Object model) {
            if (model instanceof DBModel) {
                if (((DBModel) model)._modelName.equals(_modelName)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        /**
         * Return the hash code of the modelName as two models are equal if
         * their model names are equal. So their has codes are also derived from
         * model name.
         * @return Hash code of the model name.
         */
        public int hashCode() {
            return _modelName.hashCode();
        }
    }
}

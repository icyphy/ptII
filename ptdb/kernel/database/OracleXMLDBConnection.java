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
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
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

 * Create and manage the actual connection to the Oracle XML database.
 * Manage the transactions for the database.
 *
 * <p>This is a wrapper class that acts as a  Facade to the Oracle Berkeley XML
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
     *  Construct an instance that initializes the environment
     *  and creates a database connection
     *  based on the given parameters.
     *
     * @param dbConnParams Encapsulate the parameters required to
     *                       create a database connection.
     *
     * @exception DBConnectionException If thrown  while creating a
     *                                 connection.
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
            config.setMaxLockers(2000);
            config.setMaxLocks(2000);
            config.setMaxLockObjects(2000);

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
     * Abort the connection and roll back the transaction
     * Invoke in case of errors.
     *
     * @exception DBConnectionException If thrown while aborting
     * transaction in the database.
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
     * @exception DBConnectionException If thrown while committing
     * transaction in the database.
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
     * Commit the transaction and close the connection to
     * the database.
     *
     * <p>If the transaction on this connection is alive/open
     * while closing the connection, the transaction will be aborted.</p>
     *
     * @exception DBConnectionException If thrown while closing
     * database connection.
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
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, CreateModelTask.
     *          This will tell the DB layer to create a new model in the database.
     * @exception DBExecutionException Thrown if there is a problem executing the task.
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException {

        try {

            //check if the xml container was instantiated properly
            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the XmlContainer object was not instantiated properly");
            }

            //check if the xml transaction was instantiated properly
            if (_xmlTransaction == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the XmlTransaction object was not instantiated properly");
            }

            //check if the task passed was created properly.
            if (task == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the CreateModelTask object passed was null");
            }

            //get the XMLDBModel object from the task.
            XMLDBModel model = task.getXMLDBModel();

            //check if the model inside the CreateModelTask was created properly.
            if (model == null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - the XMLDBModel object passed in the CreateModelTask was null");
            }

            //==================================================
            //try to see if the document exists in the database.
            //==================================================

            //get the document from the database
            XmlDocument doc = null;

            try {
                doc = _xmlContainer.getDocument(model.getModelName());
            } catch (XmlException e) {
                //do nothing
            }

            //if the document was retrieved from the database, throw an exception
            if (doc != null) {
                throw new DBExecutionException(
                        "Failed to execute CreateModelTask"
                                + " - The model already exist in the database. Please use the executeSaveModelTask to update the model.");
            }
            // if the document was not found in the database
            else {

                //use the container to store the model by retrieving the model name and model body
                //from the XMLDBModel object and setting that in the container.
                //The container uses the function putDocument with transaction enabled
                //to store the document in the database.
                //This method expects the document name and the content
                _xmlContainer.putDocument(_xmlTransaction,
                        model.getModelName(), model.getModel());

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
     * @exception DBExecutionException If thrown while searching
     * in the database.
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
     * Execute the get models task which basically accesses the  database and returns the model requested
     * as XMLDBModel object
     * @exception DBExecutionException Thrown if there is a problem executing the task.
     * @param task this is the get models task that contain the model name
     * @return XMLDBModel this is the model fetched from the database
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException {

        try {

            //check if the task passed was created properly.
            if (task == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the GetModelsTask object passed was null");
            }

            //get the model from the database based on the name using the container method that returns
            //the document from the database.
            //this is a document fetched from the database using the xml container with transaction enabled
            XmlDocument dbModel;
            try {
                dbModel = _xmlContainer.getDocument(_xmlTransaction, task
                        .getModelName());
            } catch (XmlException e) {

                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - Could not find the model in the database");

            }

            //this is the model that should be returned.
            XMLDBModel completeXMLDBModel = null;

            //if the dbModel is not null, meaning the model exist do some operations on it.
            //if the dbModel is null, simply return null.
            if (dbModel != null) {

                //instantiate the XMLDBModel.
                completeXMLDBModel = new XMLDBModel();

                //get the name from the result and store it in the model
                completeXMLDBModel.setModelName(dbModel.getName());

                //variable to hold the complete model body.
                String strCompleteModelBody = "";

                //get a list of all models referenced in this model from the reference file.
                String strReferences = _getModelReferences(dbModel.getName());

                //if a reference list was retrieved
                if (strReferences != null && strReferences.length() > 0) {

                    //parse the reference string to get a node representation of it.
                    Node modelNode = _parseXML(strReferences);

                    //get the actual model node inside the parsed document.
                    modelNode = modelNode.getChildNodes().item(0);

                    //call the _buildCompleteModel to build the model with all of its references.
                    strCompleteModelBody = _buildCompleteModel(modelNode);

                }

                //if there are no references to the model
                else {
                    //set the complete model body to the model fetched from the database.
                    strCompleteModelBody = dbModel.getContentAsString();
                }

                //set the model body in the model object
                completeXMLDBModel.setModel(strCompleteModelBody);

                //indicate that the model is not a new model and it exists in the database
                completeXMLDBModel.setIsNew(false);

                //parents are not needed thus the model  parents will be set to null
                completeXMLDBModel.setParents(null);

            }

            //return the list of models.
            return completeXMLDBModel;

        } catch (XmlException e) {

            throw new DBExecutionException("Failed to execute GetModelsTask - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Search models that contain the given attributes in the database.
     *
     * @param task Task that contains a list of attributes that
     * need to be searched in the database.
     *
     * @return List of models that contain the attributes;
     * Empty list if the search returns zero results; and
     * Null list if the search is not performed due to empty
     * or invalid search criteria.
     *
     * @exception DBExecutionException If thrown while searching
     * in the database.
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

                    return finalModelsList;
                }
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
     * @exception DBExecutionException If thrown while searching
     * in the database.
     */
    public ArrayList<XMLDBModel> executeGraphSearchTask(GraphSearchTask task)
            throws DBExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Execute the necessary commands to save/update a model in the database according
     * to the model specification given in the task parameter.
     *
     * @param task
     *          The task to be completed.  In this case, SaveModelTask.
     *          This will tell the DB layer to save/update a model already existing in the database.
     * @exception DBExecutionException Thrown if there is a problem executing the task.
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

        try {

            //check if the xml container was instantiated properly
            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the XmlContainer object was not instantiated properly");
            }

            //check if the xml transaction was instantiated properly
            if (_xmlTransaction == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the XmlTransaction object was not instantiated properly");
            }

            //check if task passed was created properly.
            if (task == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the SaveModelTask object passed was null");
            }

            //get the XMLDBModel object from the task.
            XMLDBModel model = task.getXMLDBModel();

            //check if the model inside the CreateModelTask was created properly.
            if (model == null) {
                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the XMLDBModel object passed in the SaveModelTask was null");
            }

            //use the container to get a handle on the document that represents the model.
            XmlDocument currentDbModel = null;

            try {
                currentDbModel = _xmlContainer
                        .getDocument(model.getModelName());
            } catch (XmlException e) {
                //do nothing
            }

            //check if the model is not in the database, throw an exception
            if (currentDbModel == null) {

                throw new DBExecutionException(
                        "Failed to execute SaveModelTask"
                                + " - the model does not exist in the database. Please use executeCreateModelTask instead.");
            }
            //if the model is in the database update it.
            else {
                //set the new content of the model to the document fetched from the database.
                currentDbModel.setContent(model.getModel());

                //update the database with the new changes to the model using the container object.
                //using the updateDocument method with transaction enabled
                //This method expects the document to be updated.
                _xmlContainer.updateDocument(_xmlTransaction, currentDbModel);
            }

        } catch (XmlException e) {
            throw new DBExecutionException("Failed to execute SaveModelTask - "
                    + e.getMessage(), e);
        }

    }

    /**
     * Provide information regarding the state of the internal variables
     * useful for unit testing purposes.
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
     * @param p_currentNode the node in the reference file that points to the model
     * @return String that represents the model body without references
     * @exception DBExecutionException Thrown if there is a problem executing the task.
     */
    private String _buildCompleteModel(Node p_currentNode)
            throws DBExecutionException {

        //if the hash map is null, instantiate it.
        //this step is done only for the first call to this method.
        if (_xmlModelHerarichyMap == null) {
            _xmlModelHerarichyMap = new HashMap<String, String>();
        }

        //get a list of all the attributes in this current node.
        NamedNodeMap attributes = p_currentNode.getAttributes();

        //variable to hold the model name.
        String strCurrentModelName = "";

        //if the attributes are not null
        if (attributes != null) {
            //loop through the attributes to get the name.
            for (int i = 0; i < attributes.getLength(); i++) {
                //get the node that represents the attribute in that list.
                Node node = attributes.item(i);

                //check if the node name is "name"
                if (node.getNodeName().equalsIgnoreCase("name")) {
                    //get the value of the node.
                    strCurrentModelName = node.getNodeValue();

                    //break the loop since we found what we are looking for.
                    break;
                }
            }
        }

        //if a model name was extracted from the node, then proceed
        if (strCurrentModelName != null && strCurrentModelName.length() > 0) {

            //check if the model is in the hash map
            if (_xmlModelHerarichyMap.containsKey(strCurrentModelName)) {
                //return modelContent from the hash map.
                return (String) _xmlModelHerarichyMap.get(strCurrentModelName);
            }

            //check if the xml container was instantiated properly
            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the XmlContainer object was not instantiated properly");
            }

            //variable to hold the current model fetched from the database.
            XmlDocument currentDbModel;

            //variable to store the model content.
            String strCurrentModelContent = "";

            try {

                //use the container to get a handle on the document that represents the model.
                currentDbModel = _xmlContainer.getDocument(strCurrentModelName);

                //if the model is not in the database, throw an exception.
                if (currentDbModel == null) {
                    throw new DBExecutionException(
                            "Failed to execute GetModelsTask"
                                    + " - Could not find a model with the name "
                                    + strCurrentModelName
                                    + " in the database when trying to get the references.");

                }

                //set the content of the current model to the content of the model fetched from the database
                strCurrentModelContent = currentDbModel.getContentAsString();

            } catch (XmlException e) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask - " + e.getMessage(),
                        e);
            }

            //check if the current node has children
            if (p_currentNode.hasChildNodes()) {

                //get the list of children.
                NodeList children = p_currentNode.getChildNodes();

                //loop through the children and get the element nodes
                for (int i = 0; i < children.getLength(); i++) {

                    //check if the child node at this index is an element node
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {

                        //get the node from the list
                        Node child = children.item(i);

                        //call this method recursively to get the content of the child node
                        String strChildContent = _buildCompleteModel(child);

                        //remove the unnecessary xml headers.
                        strChildContent = strChildContent
                                .substring(strChildContent.indexOf("<entity"));

                        //get the string representation of the parent entity in the child.
                        String strChildNode = _getParentEntityNodeAsString(strChildContent);

                        //if the child node was returned properly
                        if (strChildNode != null && strChildNode.length() > 0) {

                            //replace the nodes that references the child in the content of the parent with the child model
                            strCurrentModelContent = strCurrentModelContent
                                    .replaceAll(strChildNode, strChildContent);
                        }

                        //make sure that each xml tag is in one line.
                        //Ptolemy will not recognize the xml file if the tags are in the same line.
                        strCurrentModelContent = strCurrentModelContent
                                .replaceAll(">", ">\n");
                    }
                }
            }

            //add the current model to the hash map (name, content)
            _xmlModelHerarichyMap.put(strCurrentModelName,
                    strCurrentModelContent);

            //return the model content
            return strCurrentModelContent;

        }
        //if the name was not extracted from the node, then return empty string.
        else {
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
     * Close the environment and invoke delete on the container
     * that closes the connection and releases the resources.
     *
     * @exception DBConnectionException If thrown while
     * closing the environment.
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
     * @param attribute Attribute for which the sub-query
     * needs to be created.
     *
     * @return Sub-query for the given attribute.
     * @exception IllegalActionException If thrown whie retrieving
     * attribute data.
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
     * @param dBModelsMap Map that contains all the DBModels
     * as they are created so that their parent lists can be populated.
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
     * Execute the attribute search on the database for
     * the given attribute clause.
     *
     * @param attributeClause Attribute sub-query for which
     * search needs to be done.
     *
     * @return List of models matching the attribute search.
     *
     * @exception XmlException If thrown while executing query.
     */
    private ArrayList<String> _executeSingleAttributeMatch(
            String attributeClause) throws XmlException {

        ArrayList<String> modelsList = new ArrayList<String>();

        String attributeSearchQuery = "for $const in collection(\""
                + _params.getContainerName() + "\")/entity/property where "
                + attributeClause + " " + " return base-uri($const)";

        //System.out.println("attributeSearchQuery - " + attributeSearchQuery);

        XmlQueryContext context = _xmlManager.createQueryContext();
        context.setEvaluationType(XmlQueryContext.Lazy);

        XmlResults results = _xmlManager.query(attributeSearchQuery, context,
                null);
        if (results != null && results.hasNext()) {
            XmlValue value = results.next();
            while (value != null) {
                modelsList.add(value.asString());
                value = results.next();
            }
        }

        return modelsList;
    }

    /**
     * Fetch the parent hierarchies for the given model.
     *
     * @param model The model for which the parent
     * hierarchies are required.
     *
     * @exception DBExecutionException If thrown while
     * fetching model hierarchy.
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
     * Get all the parent entries in the reference file
     * where the given model is a child entity.
     *
     * @param model Model for which the parent references are to be fetched.
     *
     * @return String representation of the matching reference XML entities.
     *
     * @exception DBExecutionException If thrown while fetching parent hierarchies
     * from database.
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
            XmlResults results = _xmlManager.query(fetchHierarchyQuery,
                    context, null);
            /*
             * Create well formed XML for document parser to parse.
             */
            if (results != null && results.hasNext()) {

                referencesXML = new StringBuffer(
                        "<?xml version=\"1.0\" standalone=\"no\"?><entities>");

                XmlValue value = results.next();
                while (value != null) {
                    referencesXML.append(value.asString());
                    value = results.next();
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
     * retrieve the references inside a model from the reference file in the database.
     *
     * @param p_strModelName - the model name for which the references are required.
     * @return String representation of all the references in the given model in xml format.
     * @exception DBExecutionException thrown if there is an execution exception.
     */
    private String _getModelReferences(String p_strModelName)
            throws DBExecutionException {
        String strReference = "";

        try {

            //check if the xml container was instantiated properly
            if (_xmlContainer == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the XmlContainer object was not instantiated properly");
            }

            //check if the xml manager was instantiated properly
            if (_xmlManager == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - the XmlManager object was not instantiated properly");
            }

            //get a query context.
            XmlQueryContext xmlContext = _xmlManager.createQueryContext();

            //check if the xml context was created properly
            if (xmlContext == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - could not create an xml query context from the xml manager.");
            }

            //generate the query.
            String strQuery = "doc('dbxml:/" + _xmlContainer.getName()
                    + "/ReferenceFile.ptdbxml')" + "/reference/entity[@name='"
                    + p_strModelName + "']";

            // prepare the query for execution.
            XmlQueryExpression queryExpression = _xmlManager.prepare(strQuery,
                    xmlContext);

            //check if the xml expression was created properly
            if (queryExpression == null) {
                throw new DBExecutionException(
                        "Failed to execute GetModelsTask"
                                + " - could not create an xml query expression from the xml manager.");
            }

            //execute the query and return the results.
            XmlResults results = queryExpression.execute(xmlContext);

            //if the results are not null and not empty
            if (results != null && results.size() > 0) {
                //get the first item in the results.
                XmlValue result = results.next();

                //get the value of the first item in string format.
                strReference = result.asString();
            }

            //return the result.
            return strReference;

        } catch (XmlException e) {
            throw new DBExecutionException(
                    "Failed to retrieve the references for the given model - "
                            + e.getMessage(), e);
        }

    }

    /**
     * return the upper level entity node in a model content that is being passed to it as a string.
     *
     * @param strModelContent the model content in string fromat.
     * @return String representation of the upper level node.
     * @exception DBExecutionException Thrown if there is a problem executing the task.
     */
    private static String _getParentEntityNodeAsString(String strModelContent)
            throws DBExecutionException {

        //the string result of the node.
        String strNode = null;

        try {

            //if the model content is null or empty
            if (strModelContent == null || strModelContent.length() == 0) {
                throw new DBExecutionException(
                        "Faild to extract entity node from the xml content - "
                                + "content sent is empty or null");
            }

            //extract the first entity node in the content and set it to the strNode variable
            strNode = strModelContent.substring(strModelContent
                    .indexOf("<entity"), strModelContent.indexOf(">") + 1);

            //replace the closing of the entity with the proper closing.
            strNode = strNode.replace(">", "/>");

        } catch (IndexOutOfBoundsException e) {
            throw new DBExecutionException(
                    "Faild to extract entity node from the xml content - "
                            + e.getMessage(), e);
        }
        //return the result.
        return strNode;
    }

    /**
     * Get the value for the given attribute.
     *
     * @param currentNode Node for which attribute value
     * needs to be determined.
     * @param attributeName Name of the attribute.
     * @return Return the value for the given attribute.
     * Return null if attribute not present for the given node.
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
     * Parse the xml string that is passed to it and returns the upper node of that xml.
     *
     * @param p_strXML the xml string that needs to be parsed
     * @return Node - the upper node for the xml string after parsing it.
     * @exception DBExecutionException used as wrapper to the parser exceptions
     */
    private Node _parseXML(String p_strXML) throws DBExecutionException {

        //if the xml to be parsed is null or empty throw an exception
        if (p_strXML == null || p_strXML.length() == 0) {
            throw new DBExecutionException("Faild to parse the xml - "
                    + "content sent is empty or null");
        }

        //document builder object used to construct parse the string.
        DocumentBuilder docBuilder;

        //the top most node in the parsed string.
        Node firstNode = null;

        //a factory document used to create an instance of document builder.
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();

        //check if the document builder factory was created properly.
        if (docBuilderFactory == null) {
            throw new DBExecutionException(
                    "Faild to parse the xml - "
                            + "could not create a new instance of DocumentBuilderFactory.");
        }
        //configure the builder factory to ignore the element content whites paces.
        docBuilderFactory.setIgnoringElementContentWhitespace(true);

        try {

            //get an instance of document builder using the factory.
            docBuilder = docBuilderFactory.newDocumentBuilder();

            //check if the document builder was created properly.
            if (docBuilder == null) {
                throw new DBExecutionException("Faild to parse the xml - "
                        + "could not create a new instance of DocumentBuilder.");
            }

            //create an input source object. This is done because the document builder does not accept string as an input
            //The input source will be as a wrapper to the string.
            InputSource is = new InputSource();

            //set the character stream for the input source to the xml string
            is.setCharacterStream(new StringReader(p_strXML));

            //parse the input source and get the first node in the xml.
            firstNode = docBuilder.parse(is);

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
     * @param childHierarchy List that contains the list if previous models visited
     * in the hierarchy.
     * @param baseModel Base model for which the parent hierarchies
     * need to be populated.
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
     *  The environment for the Oracle Berkeley XMLDatabase that defines the
     *  parameters like cache size, locking mechanism, storing mechanism etc.
     *  required for creating a database connection.
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
     * This is a hash map to store the complete models without any references.
     */
    private HashMap<String, String> _xmlModelHerarichyMap;

    /**
     * This is the Oracle Berkeley XML DB Transaction that is used
     * to commit or abort certain transactions.
     * This will be set only if a transaction enabled
     * connection is requested.
     */
    private XmlTransaction _xmlTransaction;

    /**
     * Contain the parents list for a model.
     */
    private class DBModel {
        /**
         * Construct an instance with
         * the given model name.
         */
        DBModel(String modelName) {
            _modelName = modelName;
        }

        /**
         * Add parent to model if the parent
         * is not already present.
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

//        /**
//         * Match the given DBModel to the current model
//         * based on name.
//         * @param model Model to be compared.
//         * @return True if the names are same, false otherwise.
//         */
//        public boolean equals(DBModel model) {
//            if (model._modelName.equals(_modelName)) {
//                return true;
//            } else {
//                return false;
//            }
//        }
    }
}

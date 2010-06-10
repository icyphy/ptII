package ptdb.kernel.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlTransaction;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

//////////////////////////////////////////////////////////////////////////
////OracleXMLDBConnection
/**

 * Create and manage the actual connection to the Oracle XML database. 
 * Manage the transactions for the database.
 * 
 * <p>This is a wrapper class that acts as a  Facade to the Oracle Berkeley XML 
 * Database interface.</p>
 *     
 * @author Ashwini Bijwe
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
    * @param dbConnParams - Encapsulate the parameters required to 
    *                       create a database connection  
    * @throws DBConnectionException - When the XML DB encounters 
    *                                 an error while creating a 
    *                                 connection
    */
    public OracleXMLDBConnection(DBConnectionParameters dbConnParams)
            throws DBConnectionException {

        try {

            _params = dbConnParams;

            EnvironmentConfig config = new EnvironmentConfig();
            config.setCacheSize(100 * 1024 * 1024); // 50MB
            config.setAllowCreate(true);
            config.setInitializeCache(true);
            config.setTransactional(true);
            config.setInitializeLocking(true);
            config.setInitializeLogging(true);
            config.setErrorStream(System.err);
            File dbFile = new File(_params.getUrl());

            _environment = new Environment(dbFile, config);

            _xmlManager = new XmlManager(_environment, null);

            _xmlContainer = _xmlManager.openContainer(_params
                    .getContainerName());

            if (_params.isTransactionRequired())
                _xmlTransaction = _xmlManager.createTransaction();

            isConnectionAlive = true;

        } catch (FileNotFoundException e) {

            throw new DBConnectionException(
                    "Exception while connecting to the database : "
                            + "Database not found at the given location : "
                            + _params.getUrl(), e);

        } catch (DatabaseException e) {

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
     */
    public void abortConnection() throws DBConnectionException {
        try {
            checkConnectionAlive();
            if (_xmlTransaction != null)
                _xmlTransaction.abort();

            _cleanUp();

        } catch (XmlException e) {

            _cleanUp();
            throw new DBConnectionException(
                    "Database transaction could not be aborted - "
                            + e.getMessage(), e);

        } finally {
            isConnectionAlive = false;
        }

    }

    /** 
     * Commit the transaction and close the connection to 
     * the database.
     * Invoke in case of successful completion of processing.
     */
    public void closeConnection() throws DBConnectionException {
        try {
            checkConnectionAlive();
            if (_xmlTransaction != null)
                _xmlTransaction.commit();

            _cleanUp();

        } catch (XmlException e) {

            _cleanUp();
            throw new DBConnectionException(
                    "Database transaction could not be committed - "
                            + e.getMessage(), e);

        } finally {
            isConnectionAlive = false;
        }
    }
    
    /**
     * Execute the necessary commands to create a new model in the database according
     * to the model specification given in the task parameter
     * 
     * @param task
     *          The task to be completed.  In this case, CreateModelTask. 
     *          This will tell the DB layer to create a new model in the database.
     * @throws DBExecutionException
     */
    public void executeCreateModelTask(CreateModelTask task)
            throws DBExecutionException {
	
	try {
	    
	    //check if the xml container was instantiated properly
	    if(_xmlContainer == null) {
		throw new DBExecutionException("Failed to execute CreateModelTask" + 
	    	   " - the XmlContainer object was not instantiated properly");
	    }
	    
	    //check if the xml transaction was instantiated properly 
	    if(_xmlTransaction == null) {
		throw new DBExecutionException("Failed to execute CreateModelTask" + 
	    	   " - the XmlTransaction object was not instantiated properly");
	    }
	    
	  
	    //check if the task passed was created properly.
	    if(task == null) {
		throw new DBExecutionException("Failed to execute CreateModelTask" + 
			" - the CreateModelTask object passed was null");
	    }
	    
	    //get the XMLDBModel object from the task.
	    XMLDBModel model = task.getXMLDBModel();
	    
	  
	    //check if the model inside the CreateModelTask was created properly.
	    if(model == null) {
		throw new DBExecutionException("Failed to execute CreateModelTask" + 
			" - the XMLDBModel object passed in the CreateModelTask was null");
	    }

	    //==================================================
	    //try to see if the document exists in the database.
	    //==================================================

	    //get the document from the database
	    XmlDocument doc = _xmlContainer.getDocument(model.getModelName());

	    //if the document was retrieved from the database, throw an exception
	    if(doc != null) {	    
	       throw new DBExecutionException("Failed to execute CreateModelTask" + 
	    	   " - The model already exist in the database. Please use the executeSaveModelTask to update the model.");
	    }
	    // if the document was not found in the database
	    else {
	    
		//use the container to store the model by retrieving the model name and model body 
		//from the XMLDBModel object and setting that in the container. 
		//The container uses the function putDocument with transaction enabled 
		//to store the document in the database. 
		//This method expects the document name and the content
		_xmlContainer.putDocument (_xmlTransaction, model.getModelName (), model.getModel());

	    }
        } catch (XmlException e) {
            throw new DBExecutionException(
                    "Failed to execute GetModelsTask - "
                            + e.getMessage(), e);
        }

    }

    /**
     * 
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Execute the get models task which basically accesses the  database and returns the model requested
     * as XMLDBModel object
     * @throws DBExecutionException
     * @param task this is the get models task that contain the model name
     * @return XMLDBModel this is the model fetched from the database	
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException {
			
		
	//get the model from the database based on the name using the container method that returns the document from the database.
	try {
	    
	    //this is a document fetched from the database using the xml container with transaction enabled
	    XmlDocument dbModel = _xmlContainer.getDocument(_xmlTransaction, task.getModelName());
	    
	    //this is the model that should be returned.
	    XMLDBModel completeXMLDBModel = null;
	    
	    //if the dbModel is not null, meaning the model exist do some operations on it.
	    //if the dbModel is null, simply return null.
	    if(dbModel != null) {	

		//instantiate the XMLDBModel.
		completeXMLDBModel = new XMLDBModel();
		
		//get the name from the result and store it in the model
		completeXMLDBModel.setModelName(dbModel.getName());
    	  
		//get the model body from the dbModel using the getContentAsString method    
		//and build the model by resolving the references using 
		//buildCompleteModel() method
		String strCompleteModelBody = dbModel.getContentAsString();//_buildCompleteModel(dbModel.getName()); 

    	  
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
            
            throw new DBExecutionException(
                    "Failed to execute GetModelsTask - "
                            + e.getMessage(), e);
        }

    }
    
    /**
     * Execute the necessary commands to save/update a model in the database according
     * to the model specification given in the task parameter
     * 
     * @param task
     *          The task to be completed.  In this case, SaveModelTask. 
     *          This will tell the DB layer to save/update a model already existing in the database.
     * @throws DBExecutionException
     */
    public void executeSaveModelTask(SaveModelTask task)
            throws DBExecutionException {

	try {

	    //check if the xml container was instantiated properly
	    if(_xmlContainer == null) {
		throw new DBExecutionException("Failed to execute SaveModelTask" + 
			" - the XmlContainer object was not instantiated properly");
	    }
   
	    //check if the xml transaction was instantiated properly 
	    if(_xmlTransaction == null) {
		throw new DBExecutionException("Failed to execute SaveModelTask" + 
			" - the XmlTransaction object was not instantiated properly");
	    }
	    
	  
	    //check if task passed was created properly.
	    if(task == null) {
		throw new DBExecutionException("Failed to execute SaveModelTask" + 
			" - the SaveModelTask object passed was null");
	    }
  
	    //get the XMLDBModel object from the task.
	    XMLDBModel model = task.getXMLDBModel();

	  
	    //check if the model inside the CreateModelTask was created properly.
	    if(model == null) {
		throw new DBExecutionException("Failed to execute SaveModelTask" + 
			" - the XMLDBModel object passed in the CreateModelTask was null");
	    }
	    
	    //use the container to get a handle on the document that represents the model. 
	    XmlDocument currentDbModel = _xmlContainer.getDocument(model.getModelName());

	    //check if the model is not in the database, throw an exception
	    if(currentDbModel == null) {
		
		throw new DBExecutionException("Failed to execute SaveModelTask" + 
		" - the model does not exist in the database. Please use executeCreateModelTask instead.");
	    }
	    //if the model is in the database update it.
	    else {
		//set the new content of the model to the document fetched from the database.
		currentDbModel.setContent(model.getModel());

	    
		//update the database with the new changes to the model using the container object. 
		//using the updateDocument method with transaction enabled
		//This method expects the document to be updated.
		_xmlContainer.updateDocument (_xmlTransaction, currentDbModel);
	    }
	    
        } catch (XmlException e) {
            throw new DBExecutionException(
                    "Failed to execute SaveModelTask - "
                            + e.getMessage(), e);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    
    /**
     * Check if the connection is alive 
     * @throws DBConnectionException - when the connection is not alive
     * 
     */
    private void checkConnectionAlive() throws DBConnectionException {
        if(!isConnectionAlive)
            throw new DBConnectionException("This connection is not alive anymore. It has been closed or aborted.");
    }
    
    
    /**
     * Build the complete model by resolving all the references in it.
     * 
     * @param p_currentNode the node in the reference file that points to the model
     * @return String that represents the model body without references
     * @throws DBExecutionException 
     */
    private String _buildCompleteModel(Node p_currentNode) throws DBExecutionException {
	
	//if the hash map is null, instantiate it.
	//this step is done only for the first call to this method.
	if(_xmlModelHerarichyMap == null)
	{
	    _xmlModelHerarichyMap = new HashMap<String, String>();
	}
	
	//get a list of all the attributes in this current node.
	NamedNodeMap attributes = p_currentNode.getAttributes();
	
	//variable to hold the model name.
	String strCurrentModelName = "";
	
	//loop through the attributes to get the name.
	for(int i = 0; i < attributes.getLength(); i++)
	{
	    //get the node that represents the attribute in that list.
	    Node node = attributes.item(i);
	    
	    //check if the node name is "name"
	    if(node.getNodeName().equalsIgnoreCase("name"))
	    {
		//get the value of the node.
		strCurrentModelName = node.getNodeValue();
		
		//break the loop
		break;
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
	    
	    //use the container to get a handle on the document that represents the model. 
	    XmlDocument currentDbModel;
	    
	  
	    //variable to store the model content.
	    String strCurrentModelContent = "";
	    
	    
            try {
        	
        
        	//get the model content.
        	//this is done to replace the content before returning the model.
	        currentDbModel = _xmlContainer.getDocument(strCurrentModelName);
            
	    
        	    
	        //if the model is not in the database, throw an exception.
        	if(currentDbModel == null) {
        	    throw new DBExecutionException(
        		    "Failed to execute GetModelsTask"
        		    + " - Could not find a model with the name " + strCurrentModelName 
        		    + " in the database when trying to get the references.");
        		
        	    }

        	//set the content of the current model to the content of the model fetched from the database
        	strCurrentModelContent = currentDbModel.getContentAsString();
        	
        	
            } catch (XmlException e) {
        	throw new DBExecutionException(
                        "Failed to execute GetModelsTask - "
                                + e.getMessage(), e);
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
		        
		        //replace the nodes that references the child in the content of the parent with the child model
		        strCurrentModelContent = strCurrentModelContent
		                .replaceAll(strChildNode, strChildContent);

		        //make sure that each xml tag is in one line.
		        strCurrentModelContent = strCurrentModelContent
		                .replaceAll(">", ">\n");
		    }

	        }

	        //add the current model to the hash map (name, content
	        _xmlModelHerarichyMap.put(strCurrentModelName, strCurrentModelContent);

	        //return the model content
	        return strCurrentModelContent;
	    }

	    // if there are no children
	    else {
	        
		//add the current model to the hash map (name, content)
	        _xmlModelHerarichyMap.put(strCurrentModelName, strCurrentModelContent);

	        //return the model content
	        return strCurrentModelContent;
	    }
        }
	//if the name was not extracted from the node, then return empty string.
	else
	{
	    return "";
	}
    }
    
    /**
     * return the upper level entity node in a model content that is being passed to it as a string.
     * 
     * @param strModelContent the model content in string fromat.
     * @return String representation of the upper level node.
     */
    private static String _getParentEntityNodeAsString(String strModelContent) throws DBExecutionException
    {
	//the string result of the node.
	String strNode = null;
	
	try {
	    
	    if(strModelContent == null || strModelContent.length() == 0) {
		throw new DBExecutionException("Faild to extract entity node from the xml content - " +
			"content sent is empty or null");
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
     * Parse the xml string that is passed to it and returns the upper node of that xml.
     * 
     * @param p_strXML the xml string that needs to be parsed
     * @return Node - the upper node for the xml string after parsing it.
     * @throws DBExecutionException used as wrapper the parser exceptions
     */
    private Node _parseXML(String p_strXML) throws DBExecutionException
    {
	//document builder object used to construct parse the string.
	DocumentBuilder docBuilder;
	
	//the top most node in the parsed string.
	Node firstNode = null;
	
	//a factory document used to create an instance of document builder.
	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	
	//configure the builder factory to ignore the element content whites paces.
	docBuilderFactory.setIgnoringElementContentWhitespace(true);
	
	try {
	    
	    //get an instance of document builder using the factory.
	    docBuilder = docBuilderFactory.newDocumentBuilder();
	    
	    //create an input source object. This is done because the document builder does not accept string as an input
	    //The input source will be as a wrapper to the string.
	    InputSource is = new InputSource();
	    //set the character stream for the input source to the xml string 
	    is.setCharacterStream(new StringReader(p_strXML));
	    
	    //parse the input source and get the first node in the xml.
	    firstNode = docBuilder.parse(is);
	        
        } catch (ParserConfigurationException e) {
            
            throw new DBExecutionException(
                    "Failed to parse the model - "
                            + e.getMessage(), e);
            
        } catch (SAXException e) {
            throw new DBExecutionException(
                    "Failed to parse the model - "
                            + e.getMessage(), e);
        } catch (IOException e) {
            throw new DBExecutionException(
                    "Failed to parse the model - "
                            + e.getMessage(), e);
        } 
        
        return firstNode;
    }
    /**
     * Close the environment and invoke delete on the container 
     * that closes the connection and releases the resources 
     * @throws DBConnectionException
     */
    private void _cleanUp() throws DBConnectionException {
        try {
            if (_xmlContainer != null)
                _xmlContainer.delete();

            if (_xmlTransaction != null)
                _xmlTransaction.delete();

            if (_environment != null)
                _environment.close();

        } catch (DatabaseException e) {

            throw new DBConnectionException(
                    "Database transaction could not be committed - "
                            + e.getMessage(), e);

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
     * Denote whether the database connection is active or not
     */
    private boolean isConnectionAlive;
}

/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/*
 *
 */
package ptdb.kernel.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ptdb.common.dto.DBConnectionParameters;
import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;

import com.sleepycat.db.TransactionConfig;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlValue;

///////////////////////////////////////////////////////////////////
//// RebuildReferenceFile

/**
 * Rebuild the reference file by reading all the models in the database.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class RebuildReferenceFile extends OracleXMLDBConnection {

    /** Create an instance by instantiating the database connection.
     * @param dbConnParams Database parameters to create the connection.
     * @exception DBConnectionException
     */
    public RebuildReferenceFile(DBConnectionParameters dbConnParams)
            throws DBConnectionException {
        super(dbConnParams);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Re-create the reference file in the database.
     * @param args Not used
     * @exception IOException If thrown while reading from the console.
     * @exception DBConnectionException If thrown while creating the database
     * connection.
     * @exception DBExecutionException If thrown while executing database queries.
     * @exception XMLDBModelParsingException If thrown while parsing a model.
     */
    public static void main(String[] args) throws IOException,
    DBConnectionException, DBExecutionException,
    XMLDBModelParsingException {

        System.out.println("This process will take some time to complete. "
                + "Are you sure you want to continue?(Y/N)");

        String input = _readInput();

        if ("Y".equalsIgnoreCase(input)) {

            System.out.println("Please avoid using the PtolemyDB application "
                    + "until this process completes.");

            System.out.println("Initializing connection...");

            DBConnectionParameters dbConnectionParameters = DBConnectorFactory
                    .getDBConnectionParameters();
            dbConnectionParameters.setIsTransactionRequired(false);

            RebuildReferenceFile rebuildReferenceFile = new RebuildReferenceFile(
                    dbConnectionParameters);
            rebuildReferenceFile._rebuildReferenceFile();

            System.out.println("Rebuilding process is complete.");
            System.out.println("Thank you for your patience. :)");
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Create the reference string for the given model.
     *
     * @param model Model for which the reference string needs to be made.
     * @return Reference string for the given model.
     * @exception XMLDBModelParsingException If thrown while parsing the model.
     * @exception XmlException If thrown while reading the model from the database.
     */
    private String _buildReferenceString(XMLDBModel model)
            throws XMLDBModelParsingException, XmlException {
        StringBuilder referenceBuilder = new StringBuilder();

        /* Populate the model name and the model id in the given model.*/
        try {
            _populateModelNameAndModelId(model);
        } catch (DBExecutionException e) {
            System.out.println("Warning: Error occurred - " + e.getMessage());
            System.out.println("Skipping model - " + model.getModelName());
            return "";
        }
        /* Return the reference string if found in the local map.*/
        String modelName = model.getModelName();
        String modelId = model.getModelId();

        if (_referencesMap.containsKey(modelName)) {
            return _referencesMap.get(modelName);
        }

        referenceBuilder.append("<entity DBModelId=\"").append(modelId)
        .append("\" name=\"").append(modelName).append("\" >")
        .append("\n");

        /* Retrieve the model content from the database.*/
        GetModelTask task = new GetModelTask(modelName);
        try {

            model = executeGetModelTask(task);

        } catch (DBExecutionException e) {

            System.out.println("Warning: Error occurred - " + e.getMessage());
            System.out.println("Skipping model - " + model.getModelName());
            return "";
        }

        Document documentNode = (Document) Utilities.parseXML(model.getModel());
        /*
         * First level nodes.
         */
        Node topEntityNode = documentNode.getElementsByTagName("entity")
                .item(0);
        NodeList entityList = topEntityNode.getChildNodes();

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
                    String referencedModelName = _getModelNameFromModelId(referencedModelId);
                    referenceBuilder.append(
                            _buildReferenceString(new XMLDBModel(
                                    referencedModelName, referencedModelId)))
                                    .append("\n");
                }
            }
        }

        referenceBuilder.append("</entity>").append("\n");

        String referenceString = referenceBuilder.toString();
        _referencesMap.put(modelName, referenceString);

        return referenceString;
    }

    /** Retrieve and return the list of all models in the database.
     * @return List of models in the database.
     * @exception XmlException If thrown while reading the model list from the
     * database.
     */
    private List<XMLDBModel> _getAllModelNames() throws XmlException {
        ArrayList<String> modelNames = new ArrayList<String>();
        String query = "for $entity in collection (\""
                + _params.getContainerName()
                + "\")/entity return base-uri($entity)";

        XmlQueryContext context = _xmlManager.createQueryContext();
        XmlResults results = _xmlManager.query(query, context, null);
        if (results != null) {
            XmlValue value;
            while (results.hasNext()) {
                value = results.next();
                String modelName = value.asString();
                if (!modelName.endsWith(".ptdbxml")) {
                    modelNames.add(modelName);
                }
            }
        }
        return _getDistinctModelsList(modelNames);
    }

    /** Get the model Id for the given model name.
     * @param modelName Model name for which the model id is required.
     * @return Model Id for the given model name, null if not found.
     * @exception XmlException If thrown while retrieving model id from the
     * database.
     */
    private String _getModelIdFromModelName(String modelName)
            throws XmlException {
        String modelId = null;

        String query = "for $prop in doc (\"" + _params.getContainerName()
                + "/" + modelName
                + "\")/entity/property where $prop/@name = \"DBModelId\" "
                + "return data($prop/@value)";

        XmlQueryContext context = _xmlManager.createQueryContext();
        XmlResults results = _xmlManager.query(query, context, null);
        if (results != null) {
            XmlValue value;
            while (results.hasNext()) {
                value = results.next();
                modelId = value.asString();
            }
        }

        return modelId;

    }

    /** Get the model name for the given model id.
     * @param modelId Model If for which the model name is required.
     * @return Model name for the given model id, null if not found.
     * @exception XmlException
     */
    private String _getModelNameFromModelId(String modelId) throws XmlException {
        String modelName = null;

        String query = "for $prop in collection(\""
                + _params.getContainerName()
                + "\")/entity/property where $prop/@name = \"DBModelId\" "
                + " and $prop/@value = \"" + modelId + "\""
                + " return base-uri($prop)";

        XmlQueryContext context = _xmlManager.createQueryContext();
        XmlResults results = _xmlManager.query(query, context, null);
        if (results != null) {
            XmlValue value;
            while (results.hasNext()) {
                value = results.next();
                modelName = value.asString();
                modelName = modelName.substring(modelName.lastIndexOf('/') + 1);
            }
        }
        return modelName;
    }

    /** Populate the model name and model id in the given model.
     * Either the model id or the model name should be set in the given model.
     * @param xmlDBModel model for which model name or model id is to be
     * fetched.
     * @exception DBExecutionException If both model id and model name are null.
     * If model name fetched from the database is null.
     * If model id fetched from the database is null.
     * @exception XmlException If thrown while reading the model name or model id
     * from the database.
     */
    private void _populateModelNameAndModelId(XMLDBModel xmlDBModel)
            throws DBExecutionException, XmlException {

        if (xmlDBModel.getModelId() == null
                && xmlDBModel.getModelName() == null) {
            throw new DBExecutionException(
                    "Both model name and model Id are null. Need atleast one to populate the references.");
        }

        if (xmlDBModel.getModelId() == null) {
            String modelId = _getModelIdFromModelName(xmlDBModel.getModelName());
            if (modelId == null) {
                throw new DBExecutionException(
                        "Invalid model has no model id - "
                                + xmlDBModel.getModelName());
            }
            xmlDBModel.setModelId(modelId);
        } else if (xmlDBModel.getModelName() == null) {
            String modelName = _getModelNameFromModelId(xmlDBModel.getModelId());
            if (modelName == null) {
                throw new DBExecutionException(
                        "Invalid model has no model name - "
                                + xmlDBModel.getModelId());
            }
            xmlDBModel.setModelName(modelName);
        }
    }

    /**
     * Rebuild the reference file by reading the models.
     * @exception DBExecutionException If thrown while reading data from the
     * database.
     * @exception XMLDBModelParsingException If thrown while parsing the model.
     * @exception DBConnectionException If thrown while connecting to the database.
     */
    private void _rebuildReferenceFile() throws DBExecutionException,
    XMLDBModelParsingException, DBConnectionException {

        try {

            System.out.println("Get list of all models...");
            List<XMLDBModel> allModelsList = _getAllModelNames();

            System.out.println("Processing " + allModelsList.size()
                    + " models.");
            int count = 1;
            for (XMLDBModel model : allModelsList) {
                System.out.println("Processing " + count++ + " of "
                        + allModelsList.size() + " models.");
                _buildReferenceString(model);
            }
            System.out
            .println("Starting transaction to update ReferenceFile...");
            TransactionConfig transactionConfig = new TransactionConfig();
            /*
             * Open the transaction and enable committed reads. All
             * queries performed with this transaction handle will
             * use read committed isolation.
             */
            transactionConfig.setReadCommitted(true);

            _xmlTransaction = _xmlManager.createTransaction(null,
                    transactionConfig);
            _isTransactionActive = true;

            _updateReferenceFile();
            commitConnection();

        } catch (XmlException e) {
            abortConnection();
            throw new DBExecutionException(
                    "Error while rebuilding reference file - " + e.getMessage(),
                    e);
        } finally {
            closeConnection();
        }

    }

    /** Update the reference file with the recreated reference strings.
     * @exception XmlException If thrown while updating the ReferenceFile in the
     * database.
     */
    private void _updateReferenceFile() throws XmlException {

        String query = "for $entity in doc(\"" + _params.getContainerName()
                + "/ReferenceFile.ptdbxml\")/reference/entity "
                + "return delete nodes $entity";

        XmlQueryContext context = _xmlManager.createQueryContext();

        _xmlManager.query(_xmlTransaction, query, context, null);

        for (String references : _referencesMap.values()) {
            query = "insert node " + references + " into doc(\"dbxml:/"
                    + _params.getContainerName()
                    + "/ReferenceFile.ptdbxml\")/reference";

            _xmlManager.query(_xmlTransaction, query, context, null);
        }
    }

    /** Read the user input to decide whether the rebuild references process
     * should be started or not.
     * @return The user input.
     * @exception IOException If thrown while reading input from the console.
     */
    private static String _readInput() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String userInput = null;
        try {
            userInput = br.readLine();
        } finally {
            br.close();
        }

        return userInput;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /* Map to store the built references. */
    private HashMap<String, String> _referencesMap = new HashMap<String, String>();
}

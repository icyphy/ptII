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

package ptdb.common.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.IllegalNameException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// Utilities

/**
 * Provide utility functions for document parsing etc.
 *
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class Utilities {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Check if the given name meet the model name convention.
     *
     * @param name The name to be verified.
     * @return true - if the name is valid.<br>
     *          false - if the name is invalid.
     * @exception IllegalNameException Thrown if the verifying name is invalid.
     */
    public static boolean checkModelName(String name)
            throws IllegalNameException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalNameException("The name cannot be empty!");
        }

        //        if (name.matches("^[A-Za-z0-9]+$")) {
        if (name.matches("^[\\w]+$")) {
            return true;
        } else {
            throw new IllegalNameException("Invalid Name! The name can only "
                    + "contain numbers and letters.");
        }
    }

    /**
     * Check if the given name meet the attribute name convention.
     *
     * @param name The name to be verified.
     * @return true - if the name is valid.<br>
     *          false - if the name is invalid.
     * @exception IllegalNameException Thrown if the verifying name is invalid.
     */
    public static boolean checkAttributeName(String name)
            throws IllegalNameException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalNameException("The name cannot be empty!");
        }

        if (name.matches("^[\\w\\s]+$")) {
            return true;
        } else {
            throw new IllegalNameException("Invalid Name! The name can only "
                    + "contain numbers and letters.");
        }
    }

    /** Check whether the given file exists.
     *
     * @param filePath Path for the file.
     * @return True, if file exists, false otherwise.
     */
    public static boolean checkFileExists(String filePath) {

        File file = new File(filePath);
        return file.exists();
    }

    /**
     * Create new Id by appending a timestamp to the given name.
     * @param name The name that will be used to generate the id.
     * @return An Id that is a combination of the name passed with the time stamp.
     */
    public static String generateId(String name) {

        String id = "";

        Date date = new Date();

        id = name + "_" + date.getTime();

        return id;
    }

    /**
     * Convert the document node to string.
     *
     * @param document Document which needs to be converted to String.
     * @return String for the given document.
     */
    public static String getDocumentXMLString(Document document) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) document
                .getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        String documentContent = lsSerializer.writeToString(document);

        /* Removing the XML tag appended automatically by the parser. */
        if (documentContent.startsWith("<?xml")) {
            int index = documentContent.indexOf("?>") + 2;
            documentContent = documentContent.substring(index);
        }

        return documentContent;
    }

    /**
     * Extra the PTDB model id from a given Ptolemy model.
     *
     * @param model The model to be extracted the id.
     * @return The id of the model in the database.  If that model hasn't been
     * stored in the database yet, return null.
     */
    public static String getIdFromModel(NamedObj model) {
        String id = null;

        if (model != null
                && model.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR) != null) {

            id = ((StringParameter) model
                    .getAttribute(XMLDBModel.DB_MODEL_ID_ATTR)).getExpression();

        }

        return id;
    }

    /**
     * Create the property node string to add to a MOML with
     * the class as StringConstantParameter.
     * @param propertyName Name for the property.
     * @param propertyValue Value for the property.
     * @return Property node string for the given name and value.
     */
    public static String getPropertyString(String propertyName,
            String propertyValue) {

        return "<property name=\"" + propertyName + "\" "
                + "class=\"ptolemy.data.expr.StringConstantParameter\" "
                + "value=\"" + propertyValue + "\"></property>";
    }

    /**
     * Get the value for the given attribute.
     *
     * @param currentNode Node for which attribute value needs to be determined.
     * @param attributeName Name of the attribute.
     * @return Return the value for the given attribute. Return null if
     * attribute not present for the given node.
     */
    public static String getValueForAttribute(Node currentNode,
            String attributeName) {

        NamedNodeMap attributes = currentNode.getAttributes();
        String strCurrentModelName = null;

        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {

                Node node = attributes.item(i);
                if (node.getNodeName().equalsIgnoreCase(attributeName)) {
                    strCurrentModelName = node.getNodeValue();
                    break;
                }
            }
        }
        return strCurrentModelName;
    }

    /**
     * Add a parameter tag called DBModelId to the given model body.
     * @param modelBody The XML model body where the Id parameter will be added.
     * @param modelId The Id of the model that needs to be inserted.
     * @return The resulting modelBody after inserting the Id to it.
     */
    public static String insertIdTagToModelBody(String modelBody, String modelId) {

        String modelIdTag = "<property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" "
                + "class=\"ptolemy.data.expr.StringConstantParameter\" value=\""
                + modelId + "\"></property>";

        StringBuffer modelBodyBuffer = new StringBuffer(modelBody);

        modelBodyBuffer.insert(modelBodyBuffer.indexOf(">") + 1, modelIdTag);

        return modelBodyBuffer.toString();

    }

    /**
     * Intersect the XMLDBModels results from two list, and take the common
     * ones and return them in a new list.
     *
     * @param previousResults The list of the first batch of models.
     * @param currentResults The list of the second batch of models.
     * @return The list of common models from both lists.
     */
    public static ArrayList<XMLDBModel> intersectResults(
            ArrayList<XMLDBModel> previousResults,
            ArrayList<XMLDBModel> currentResults) {

        // If the previous result is empty, just return the results found
        // in this searcher.
        if (previousResults == null || previousResults.size() == 0) {
            return currentResults;
        }

        // If the current result is empty, just return the empty set.
        if (currentResults == null || currentResults.size() == 0) {
            return currentResults;
        }

        Hashtable<String, XMLDBModel> existingModels = new Hashtable<String, XMLDBModel>();

        ArrayList<XMLDBModel> returnedResults = new ArrayList<XMLDBModel>();

        for (Object element : previousResults) {
            XMLDBModel xmldbModel = (XMLDBModel) element;
            existingModels.put(xmldbModel.getModelName(), xmldbModel);
        }

        for (Object element : currentResults) {
            XMLDBModel xmldbModel = (XMLDBModel) element;
            if (existingModels.get(xmldbModel.getModelName()) != null) {
                returnedResults.add(xmldbModel);
            }
        }

        return returnedResults;
    }

    /**
     * Check if the model name exists in the given reference string.
     * @param modelName Model name to check.
     * @param referenceString Reference string to check within.
     * @return True if the model name is present within the given reference
     * string, false otherwise.
     */
    public static boolean modelReferenceExists(String modelName,
            String referenceString) {
        boolean modelExists = false;
        if (modelName != null && referenceString != null) {
            String searchString = "name=\"" + modelName + "\"";
            if (referenceString.indexOf(searchString) != -1) {
                modelExists = true;
            }
        }
        return modelExists;
    }

    /**
     * Parse the xml string that is passed to it and return the upper node of
     * that xml.
     *
     * @param xmlString The xml string that needs to be parsed
     * @return The upper node for the xml string after parsing it.
     * @exception XMLDBModelParsingException Thrown if a parser exceptions was thrown
     */
    public static Node parseXML(String xmlString)
            throws XMLDBModelParsingException {

        if (xmlString == null || xmlString.length() == 0) {
            throw new XMLDBModelParsingException("Failed to parse the xml - "
                    + "content sent is empty or null");
        }

        DocumentBuilder docBuilder;

        Node firstNode = null;

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
                .newInstance();

        if (docBuilderFactory == null) {
            throw new XMLDBModelParsingException(
                    "Faild to parse the xml - "
                            + "could not create a new instance of DocumentBuilderFactory.");
        }

        docBuilderFactory.setIgnoringElementContentWhitespace(true);

        try {

            docBuilder = docBuilderFactory.newDocumentBuilder();

            if (docBuilder == null) {
                throw new XMLDBModelParsingException(
                        "Faild to parse the xml - "
                                + "could not create a new instance of DocumentBuilder.");
            }

            InputSource inputSource = new InputSource();

            inputSource.setCharacterStream(new StringReader(xmlString));

            firstNode = docBuilder.parse(inputSource);

        } catch (ParserConfigurationException e) {

            throw new XMLDBModelParsingException("Failed to parse the model - "
                    + e.getMessage(), e);

        } catch (SAXException e) {
            throw new XMLDBModelParsingException("Failed to parse the model - "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new XMLDBModelParsingException("Failed to parse the model - "
                    + e.getMessage(), e);
        }

        return firstNode;
    }

}

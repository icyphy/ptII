/*
 * 
 */
package ptdb.common.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

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

import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.XMLDBModelParsingException;


///////////////////////////////////////////////////////////////
//// Utilities

/**
 * Provide utility functions for document parsing etc.
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class Utilities {

    //////////////////////////////////////////////////////////////////////
    ////		public variables 				////

    //////////////////////////////////////////////////////////////////////
    ////		public methods 					////
    /**
     * Parse the xml string that is passed to it and return the upper node of
     * that xml.
     * 
     * @param xmlString The xml string that needs to be parsed
     * @return The upper node for the xml string after parsing it.
     * @exception DBExecutionException Thrown if a parser exceptions was thrown
     */
    public static Node parseXML(String xmlString) throws XMLDBModelParsingException {

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
                throw new XMLDBModelParsingException("Faild to parse the xml - "
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

    /**
     * Get the value for the given attribute.
     * 
     * @param currentNode Node for which attribute value needs to be determined.
     * @param attributeName Name of the attribute.
     * @return Return the value for the given attribute. Return null if
     * attribute not present for the given node.
     */
    public static String getValueForAttribute(Node currentNode, String attributeName) {

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
     * Create new Id by appending a timestamp to the given name.
     * @param name The name that will be used to generate the id.
     * @return An Id that is a combination of the name passed with the time stamp.
     */
    public static String generateId(String name) {
        
        String id = "";

        Date date = new Date ();
        
        id = name + "_" + date.getTime();
                
        return id;
    }
    
    
    /**
     * Add a parameter tag called DBModelId to the given model body.
     * @param modelBody The XML model body where the Id parameter will be added.
     * @param modelId The Id of the model that needs to be inserted.
     * @return The resulting modelBody after inserting the Id to it.
     */
    public static String insertIdTagToModelBody(String modelBody, String modelId) {
                
        String modelIdTag = "<property name=\"DBModelId\" "
            +"class=\"ptolemy.data.expr.StringParameter\" value=\"" 
            + modelId + "\"></property>";
        
        StringBuffer modelBodyBuffer = new StringBuffer(modelBody);
      
        modelBodyBuffer.insert(modelBodyBuffer.indexOf(">") + 1, modelIdTag);
        
        return modelBodyBuffer.toString();

    }
    
    /**
     * 
     * @param document
     * @return
     */
    public static String getDocumentXMLString(Document document) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) document.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        return lsSerializer.writeToString(document); 
    }
    //////////////////////////////////////////////////////////////////////
    ////		protected methods 				////

    //////////////////////////////////////////////////////////////////////
    ////		protected variables 				////

    //////////////////////////////////////////////////////////////////////
    ////		private methods 				////

    //////////////////////////////////////////////////////////////////////
    ////		private variables				////

}

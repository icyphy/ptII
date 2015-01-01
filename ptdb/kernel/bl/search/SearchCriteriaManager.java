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
package ptdb.kernel.bl.search;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.SearchCriteriaParseException;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SearchCriteriaManager

/**
 * The business layer class to handle the operations for saving and
 * loading search criteria from the stored file system.
 *
 * @author Alek Wang
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (wenjiaow)
 * @Pt.AcceptedRating red (wenjiaow)
 *
 */
public class SearchCriteriaManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Open the file from the file system, and load the search criteria
     * stored in it.  Return the loaded search criteria back to the caller.
     *
     * @param searchCriteriaFileName The location and file name of the search
     *  criteria to be loaded.
     * @param configuration The configuration of the opening search criteria.
     * @return The search criteria that contains the information stored in the
     *  file to be loaded.
     * @exception SearchCriteriaParseException Thrown if there is any error
     *  during parsing the search criteria file.
     * @exception IOException Thrown if IO errors happened during reading of
     * the search criteria file.
     */
    public static SearchCriteria open(String searchCriteriaFileName,
            Configuration configuration) throws SearchCriteriaParseException,
            IOException {

        SearchCriteria storedSearchCriteria = new SearchCriteria();

        File criteriaFile = new File(searchCriteriaFileName);

        if (!criteriaFile.exists()) {
            throw new FileNotFoundException("The search criteria file "
                    + searchCriteriaFileName + "cannot be found.");

        }

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder;

        try {

            documentBuilder = documentBuilderFactory.newDocumentBuilder();

            Node criteriaNode = documentBuilder.parse(criteriaFile)
                    .getFirstChild();

            NodeList childNodeList = criteriaNode.getChildNodes();

            for (int j = 0; j < childNodeList.getLength(); j++) {
                String nodeName = childNodeList.item(j).getNodeName();

                if (nodeName.equals("pattern")) {

                    // The pattern part exists, fetch the sub string of pattern.
                    String criteriaString = "";
                    FileInputStream fileInputStream = new FileInputStream(
                            criteriaFile);
                    BufferedReader bufferedReader = null;
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(
                                        fileInputStream));

                        StringBuffer criteriaStringBuffer = new StringBuffer("");

                        String line = bufferedReader.readLine();

                        while (line != null) {
                            criteriaStringBuffer.append(line);
                            line = bufferedReader.readLine();
                        }
                        criteriaString = criteriaStringBuffer.toString();
                    } finally {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                    }


                    int startIndex = criteriaString.indexOf("<pattern>");
                    int endIndex = criteriaString.indexOf("</pattern>");

                    String patternMoml = criteriaString.substring(
                            startIndex + 9, endIndex);

                    storedSearchCriteria.setPatternMoML(patternMoml);

                } else if (nodeName.equals("attributes")) {

                    // attributes node exists.
                    ArrayList<Attribute> attributesList = new ArrayList();

                    Node attributesNode = childNodeList.item(j);

                    NodeList attributesNodeList = attributesNode
                            .getChildNodes();

                    for (int i = 0; i < attributesNodeList.getLength(); i++) {

                        if (attributesNodeList.item(i).getNodeName()
                                .equals("attribute")) {
                            Node attributeNode = attributesNodeList.item(i);
                            NamedNodeMap attributeInfoMap = attributeNode
                                    .getAttributes();

                            StringParameter attribute = new StringParameter(
                                    new NamedObj(), attributeInfoMap
                                    .getNamedItem("name")
                                    .getNodeValue());

                            attribute.setToken(attributeInfoMap.getNamedItem(
                                    "value").getNodeValue());

                            attributesList.add(attribute);
                        }

                    }

                    storedSearchCriteria.setAttributes(attributesList);

                } else if (nodeName.equals("modelname")) {

                    // Model name tag exists.
                    Node modelNameNode = childNodeList.item(j);

                    NamedNodeMap modelNameAttributes = modelNameNode
                            .getAttributes();

                    storedSearchCriteria.setModelName(modelNameAttributes
                            .getNamedItem("value").getNodeValue());
                }

            }

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new SearchCriteriaParseException(
                    "Fail to parse the stored search criteria file in "
                            + searchCriteriaFileName, e);
        }

        return storedSearchCriteria;

    }

    /**
     * Save the search criteria to a file in the XML format.
     *
     * @param searchCriteria The DTO containing the search criteria to be
     *  saved.
     * @param searchCriteriaFile The file path and name where the criteria
     *  should be saved.
     * @return true - if the search criteria is saved successful.<br>
     *  False - if the search criteria is not saved successful.
     * @exception IllegalActionException Thrown if the passed attributes'
     * values cannot be obtained.
     * @exception IOException Thrown if error happens during writing the
     *  search criteria information to the file.
     */
    public static boolean save(SearchCriteria searchCriteria,
            String searchCriteriaFile) throws IllegalActionException,
            IOException {

        StringBuffer searchCriteriaStringBuffer = new StringBuffer();

        searchCriteriaStringBuffer.append("<criteria>");

        // Fetch the effigy for the pattern.
        if (searchCriteria.getPatternMoML() != null) {

            String patternMoMl = searchCriteria.getPatternMoML();

            //            int startIndex = patternMoMl.indexOf("<entity");
            //            int endIndex = patternMoMl.lastIndexOf("</entity>");

            searchCriteriaStringBuffer.append("<pattern>");
            //            searchCriteriaStringBuffer.append(patternMoMl.substring(startIndex,
            //                    endIndex + 9));

            searchCriteriaStringBuffer.append(patternMoMl);

            searchCriteriaStringBuffer.append("</pattern>");

        }

        // Fetch the attributes.
        ArrayList<Attribute> attributesList = searchCriteria.getAttributes();

        if (attributesList != null && attributesList.size() > 0) {

            searchCriteriaStringBuffer.append("<attributes>");

            for (Attribute attribute : attributesList) {
                searchCriteriaStringBuffer.append("<attribute name=\""
                        + attribute.getName() + "\" value="
                        + ((StringParameter) attribute).getToken().toString()
                        + " />");
            }

            searchCriteriaStringBuffer.append("</attributes>");
        }

        // Fetch the model name.
        if (searchCriteria.getModelName() != null
                && !searchCriteria.getModelName().trim().isEmpty()) {

            searchCriteriaStringBuffer.append("<modelname value=\""
                    + searchCriteria.getModelName() + "\" />");

        }

        searchCriteriaStringBuffer.append("</criteria>");

        File file = new File(searchCriteriaFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        BufferedWriter bufferedWriter = new BufferedWriter(
                new OutputStreamWriter(fileOutputStream, java.nio.charset.Charset.defaultCharset()));

        try {

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create the new file");
                }
            }

            bufferedWriter.write(searchCriteriaStringBuffer.toString());

            bufferedWriter.flush();

        } finally {

            bufferedWriter.close();

        }

        return true;

    }

}

/* Utilities that manipulate strings using XSLT

 Copyright (c) 2002-2003 The Regents of the University of California.
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.util;

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xalan.serialize.Serializer;
import org.apache.xalan.serialize.SerializerFactory;
import org.w3c.dom.Document;

//////////////////////////////////////////////////////////////////////////
//// XSLTUtilities
/**
A collection of utilities for manipulating strings using XSLT
These utilities do not depend on any other ptolemy.* packages.


@author Christopher Hylands, Haiyang Zheng
@version $Id$
@since Ptolemy II 2.1
*/
public class XSLTUtilities {

    /** Instances of this class cannot be created.
     */
    private XSLTUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Parse a document.
     * @param filename The file name of the xml file to be read in
     * @return the parsed document.
     * @exception Exception Thrown if there is a problem with the
     * transformation.
     */
    public static Document parse(String filename) throws Exception {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(filename));
    }

    /** Given a Document, generate a String.
     *  @param document The document to be converted to a string.
     *  @returns A string representation of the Document.
     *  @exception Exception
     */
    public static String toString(Document document) throws Exception {
	// FIXME: Joern's sample code had this in it, but if we
	// include it, then we get Provider   not found errors 
	//String defaultDBFI =
	//    System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
	//System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
	//		   defaultDBFI == null ? "" : defaultDBFI);

        Properties format = org.apache.xalan.templates.OutputProperties
	    .getDefaultMethodProperties("xml");
        format.setProperty("indent", "yes");
        format.setProperty("{http://xml.apache.org/xslt}indent-amount",
			   "4");
        Serializer serializer = SerializerFactory.getSerializer(format);
        OutputStream outputStream = new ByteArrayOutputStream();
        serializer.setOutputStream(outputStream);
        serializer.asDOMSerializer().serialize(document);
        outputStream.close();
        return outputStream.toString();
    }

    /** Transform a document
     * @param inputDocument The Document to be transformed
     * @param xslFileName The file name of the xsl file to be used.
     * If the file cannot be found, then we look up the file in the classpath.
     * @return a transformed document
     * @exception Thrown if there is a problem with the transformation.
     */
    public static Document transform(Document inputDocument,
            String xslFileName) throws Exception {
        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
	Transformer transformer = null;
	try {
	    transformer = transformerFactory
		.newTransformer(new StreamSource(xslFileName));
	} catch (javax.xml.transform.TransformerConfigurationException ex) {
	    try {
                // We might be in the Swing Event thread, so
                // Thread.currentThread().getContextClassLoader()
                // .getResource(entry) probably will not work.
                Class refClass = Class.forName("ptolemy.util.XSLTUtilities");
                URL entryURL =
		    refClass.getClassLoader().getResource(xslFileName);
		if (entryURL != null) {
		    transformer = transformerFactory
			.newTransformer(new StreamSource(entryURL.toString()));
		} else {
		    throw ex;
		}
	    } catch (Exception ex2) {
		throw ex;
	    }
	}
        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(inputDocument), result);
        return (Document)result.getNode();
    }

    /** Transform a document.
     * @param inputDocument The Document to be transformed
     * @param xslFileNames A list of Strings naming the
     * xsl files to be applied sequentially.
     * @return a transformed document
     * @exception Thrown if there is a problem with the transformation.
     */
    public static Document transform(Document inputDocument,
            List xslFileNames) throws Exception {
        Iterator fileNames = xslFileNames.iterator();
        while (fileNames.hasNext()) {
            String fileName = (String) fileNames.next();
            inputDocument = transform(inputDocument, fileName);
        }
        return inputDocument;
    }

    /** Transform a document.
     * @param xsltFileName The name of the xsl file to be used.
     * @param sourceFileName The name of the file to be read in and
     * transformed.
     * @param resultFileName The name of the file to be generated.
     * @exception Thrown if there is a problem with the transformation.
     */
    public static void transform (String xsltFileName,
				  String sourceFileName,
				  String resultFileName) throws Exception {
        OutputStream resultStream = new FileOutputStream(resultFileName);
        StreamSource source = new StreamSource(sourceFileName);
        StreamResult result = new StreamResult(resultStream);
        TransformerFactory transformerFactory =
	    TransformerFactory.newInstance();
        Transformer transformer = transformerFactory
	    .newTransformer(new StreamSource(xsltFileName));
        transformer.setOutputProperty("indent", "yes");
        transformer.transform(source, result);
        resultStream.flush();
        resultStream.close();
    }
}

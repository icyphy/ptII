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

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;


import org.w3c.dom.Document;

//////////////////////////////////////////////////////////////////////////
//// XSLTUtilities
/**
A collection of utilities for manipulating strings using XSLT
These utilities do not depend on any other ptolemy.* packages.


@author Christopher Hylands
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
     */
    public static Document transform(String filename) throws Exception {
        DocumentBuilderFactory factory
            = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(filename));

    }

    /** Transform a document 
     * @param inputDocument The Document to be transformed
     * @param transformFileName The file name of the xsl file to be used
     * @return a transformed document
     */
    public static Document transform(Document inputDocument,
            String transformFileName) throws Exception {
        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
        Transformer transformer = transformerFactory
            .newTransformer(new StreamSource(transformFileName));
        DOMResult result = new DOMResult();
        transformer.transform(new DOMSource(inputDocument), result);
        return (Document)result.getNode();
    }
}

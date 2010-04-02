/* A validating parser.

 Copyright (c) 2010 The Regents of the University of California.
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

 */
package ptolemy.configs.test;


import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


/**
 A validating xml parser.
 <p>Note that MoML files are not always valid xml because of the 
 &lt;configure&gt; tag.</p>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ValidatingXMLParser extends DefaultHandler {
    public static void main(String[] argv) {
        try {
            parse(argv[0]); 
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public static void parse(String fileName)  throws Throwable {
        DefaultHandler handler = new ValidatingXMLParser();

        // Use the validating parser
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);

        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new File(fileName), handler);
        } catch (SAXParseException ex) {

            System.out.println("\n Error parsing line " +
                ex.getLineNumber() + ", uri " + ex.getSystemId());
            System.out.println("   " + ex.getMessage());
            throw ex;
        }
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================
    public void setDocumentLocator(Locator l) {
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String namespaceURI, String sName, // simple name
        String qName, // qualified name
        Attributes attrs) throws SAXException {
    }

    public void endElement(String namespaceURI, String sName, // simple name
        String qName // qualified name
    ) throws SAXException {
    }

    public void characters(char[] buf, int offset, int len) 
        throws SAXException {
    }

    public void ignorableWhitespace(char[] buf, int offset, int len)
        throws SAXException {
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    public void error(SAXParseException exception) throws SAXParseException {
        warning(exception);
        throw exception;
    }

    public void warning(SAXParseException exception) throws SAXParseException {
        System.out.println("Warning: line " + exception.getLineNumber() +
            ", uri " + exception.getSystemId());
        System.out.println("   " + exception.getMessage());
    }
}

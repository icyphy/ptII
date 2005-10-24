/*
 * Created on Feb 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.backtrack.xmlparser;

import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author tfeng
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XmlHandler implements com.microstar.xml.XmlHandler {
    XmlHandler(ConfigXmlTree tree, String systemId) {
        currentTree = this.tree = tree;
        this.systemId = systemId;
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#startDocument()
     */
    public void startDocument() throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#endDocument()
     */
    public void endDocument() throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    public Object resolveEntity(String publicId, String systemId)
            throws Exception {
        if ((publicId != null) && publicId.equals(MoML_PUBLIC_ID_1)) {
            return new StringReader(MoML_DTD_1);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#startExternalEntity(java.lang.String)
     */
    public void startExternalEntity(String systemId) throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#endExternalEntity(java.lang.String)
     */
    public void endExternalEntity(String systemId) throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#doctypeDecl(java.lang.String, java.lang.String, java.lang.String)
     */
    public void doctypeDecl(String name, String publicId, String systemId)
            throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#attribute(java.lang.String, java.lang.String, boolean)
     */
    public void attribute(String aname, String value, boolean isSpecified)
            throws Exception {
        if (value != null) {
            currentAttrs.put(aname, value);
        }
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#startElement(java.lang.String)
     */
    public void startElement(String elname) throws Exception {
        ConfigXmlTree newtree = new ConfigXmlTree(elname);
        newtree.setParent(currentTree);
        currentTree = newtree;

        Enumeration attrenu = currentAttrs.keys();

        while (attrenu.hasMoreElements()) {
            String attr = (String) attrenu.nextElement();
            currentTree.setAttribute(attr, (String) currentAttrs.get(attr));
        }

        currentAttrs.clear();
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#endElement(java.lang.String)
     */
    public void endElement(String elname) throws Exception {
        currentTree = currentTree.getParent();
        totalElements++;
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#charData(char[], int, int)
     */
    public void charData(char[] ch, int start, int length) throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws Exception {
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#error(java.lang.String, java.lang.String, int, int)
     */
    public void error(String message, String systemId, int line, int column)
            throws Exception {
    }

    public int getTotalElements() {
        return totalElements;
    }

    // Copied from ptolemy.moml.MoMLParser.

    /** The standard MoML DTD, represented as a string.  This is used
     *  to parse MoML data when a compatible PUBLIC DTD is specified.
     *  NOTE: This DTD includes a number of elements that are deprecated.
     *  They are included here for backward compatibility.  See the MoML
     *  chapter of the Ptolemy II design document for a view of the
     *  current (nondeprecated) DTD.
     */
    public static String MoML_DTD_1 = "<!ELEMENT model (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | property | relation | rename | rendition | unlink)*><!ATTLIST model name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT class (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | port | property | relation | rename | rendition | unlink)*><!ATTLIST class name CDATA #REQUIRED extends CDATA #IMPLIED source CDATA #IMPLIED><!ELEMENT configure (#PCDATA)><!ATTLIST configure source CDATA #IMPLIED><!ELEMENT deleteEntity EMPTY><!ATTLIST deleteEntity name CDATA #REQUIRED><!ELEMENT deletePort EMPTY><!ATTLIST deletePort name CDATA #REQUIRED><!ELEMENT deleteProperty EMPTY><!ATTLIST deleteProperty name CDATA #REQUIRED><!ELEMENT deleteRelation EMPTY><!ATTLIST deleteRelation name CDATA #REQUIRED><!ELEMENT director (configure | doc | property)*><!ATTLIST director name CDATA \"director\" class CDATA #REQUIRED><!ELEMENT doc (#PCDATA)><!ATTLIST doc name CDATA \"_doc\"><!ELEMENT entity (class | configure | deleteEntity | deletePort | deleteRelation | director | doc | entity | group | import | input | link | port | property | relation | rename | rendition | unlink)*><!ATTLIST entity name CDATA #REQUIRED class CDATA #IMPLIED source CDATA #IMPLIED><!ELEMENT group ANY><!ATTLIST group name CDATA #IMPLIED><!ELEMENT import EMPTY><!ATTLIST import source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT input EMPTY><!ATTLIST input source CDATA #REQUIRED base CDATA #IMPLIED><!ELEMENT link EMPTY><!ATTLIST link insertAt CDATA #IMPLIED insertInsideAt CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #IMPLIED vertex CDATA #IMPLIED><!ELEMENT location EMPTY><!ATTLIST location value CDATA #REQUIRED><!ELEMENT port (configure | doc | property | rename)*><!ATTLIST port class CDATA #IMPLIED name CDATA #REQUIRED><!ELEMENT property (configure | doc | property | rename)*><!ATTLIST property class CDATA #IMPLIED name CDATA #REQUIRED value CDATA #IMPLIED><!ELEMENT relation (configure | doc | property | rename | vertex)*><!ATTLIST relation name CDATA #REQUIRED class CDATA #IMPLIED><!ELEMENT rename EMPTY><!ATTLIST rename name CDATA #REQUIRED><!ELEMENT rendition (configure | location | property)*><!ATTLIST rendition class CDATA #REQUIRED><!ELEMENT unlink EMPTY><!ATTLIST unlink index CDATA #IMPLIED insideIndex CDATA #IMPLIED port CDATA #REQUIRED relation CDATA #IMPLIED><!ELEMENT vertex (configure | doc | location | property | rename)*><!ATTLIST vertex name CDATA #REQUIRED pathTo CDATA #IMPLIED value CDATA #IMPLIED>";

    // Copied from ptolemy.moml.MoMLParser.

    /** The public ID for version 1 MoML. */
    public static String MoML_PUBLIC_ID_1 = "-//UC Berkeley//DTD MoML 1//EN";

    protected ConfigXmlTree tree;

    protected String systemId;

    protected Hashtable currentAttrs = new Hashtable();

    protected ConfigXmlTree currentTree;

    private int totalElements = 0;
}

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

import ptolemy.moml.MoMLParser;

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
        if (publicId != null && publicId.equals(MoMLParser.MoML_PUBLIC_ID_1)) {
            return new StringReader(MoMLParser.MoML_DTD_1);
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
        if (value != null)
            currentAttrs.put(aname, value);
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
            String attr = (String)attrenu.nextElement();
            currentTree.setAttribute(attr, (String)currentAttrs.get(attr));
        }
        currentAttrs.clear();
    }

    /* (non-Javadoc)
     * @see com.microstar.xml.XmlHandler#endElement(java.lang.String)
     */
    public void endElement(String elname) throws Exception {
        currentTree = currentTree.getParent();
        totalElements ++;
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

    protected ConfigXmlTree tree;

    protected String systemId;

    protected Hashtable currentAttrs = new Hashtable();

    protected ConfigXmlTree currentTree;

    private int totalElements = 0;

}

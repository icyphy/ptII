/*
 * Created on Feb 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.backtrack.xmlparser;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

/**
 * @author tfeng
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XmlOutput {

    public static void outputXmlTree(ConfigXmlTree tree, Writer writer)
            throws IOException {
        tree.startTraverseChildren();
        // DTD header
        writer.write(DTD_HEAD1 + tree.getElementName() + DTD_HEAD2);

        outputXmlSubtree(tree, writer, 0);
    }

    protected static void outputXmlSubtree(ConfigXmlTree tree, Writer writer, int indent)
            throws IOException {
        String indentString = createIndent(indent);
        String elementName = tree.getElementName();

        // Starting tag
        StringBuffer starting = new StringBuffer(indentString);
        starting.append('<');
        starting.append(elementName);
        Enumeration attrs = tree.getAttributeNames();
        while (attrs.hasMoreElements()) {
            String attrName = (String)attrs.nextElement();
            String attrValue = tree.getAttribute(attrName);
            starting.append(' ');
            starting.append(attrName);
            starting.append("=\"");
            starting.append(attrValue);
            starting.append('\"');
        }
        if (tree.isLeaf()) {
            starting.append("/>\n");
            writer.write(starting.toString());
        } else {
            starting.append(">\n");
            writer.write(starting.toString());

            // Write children
            tree.startTraverseChildren();
            while (tree.hasMoreChildren())
                outputXmlSubtree(tree.nextChild(), writer, indent + 2);

            // Ending tag
            writer.write(indentString + "</" + tree.getElementName() + ">\n");
        }
    }

    private static String createIndent(int indent) {
        StringBuffer buffer = new StringBuffer(indent);
        for (int i=0; i<indent; i++)
            buffer.append(' ');
        return buffer.toString();
    }

    public static final String DTD_HEAD1 =
    "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
    "<!DOCTYPE ";
    public static final String DTD_HEAD2 =
    " PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"\n" +
    "    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">\n";

}

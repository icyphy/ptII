/* XML output from its tree representation.

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.xmlparser;

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// XmlOutput
/**
   XML output from its tree representation.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class XmlOutput {
    
    /** Output an XML tree to the writer.
     * 
     *  @param tree The XML tree.
     *  @param writer The writer.
     *  @exception IOException If there is an IO exception during the output.
     */
    public static void outputXmlTree(ConfigXmlTree tree, Writer writer)
            throws IOException {
        tree.startTraverseChildren();

        // DTD header
        writer.write(DTD_HEAD1 + tree.getElementName() + DTD_HEAD2);

        _outputXmlSubtree(tree, writer, 0);
    }

    /** DTD header before the root element name.
     */
    public static final String DTD_HEAD1 =
        "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
        "<!DOCTYPE ";

    /** DTD header after the root element name.
     */
    public static final String DTD_HEAD2 =
        " PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"\n" +
        "    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">\n";

    /** Output the sub-tree to the writer with the specified number of
     *  indentations.
     *  
     *  @param tree The sub-tree.
     *  @param writer The writer.
     *  @param indent The number of indentations.
     *  @exception IOException If there is an IO exception during the output.
     */
    protected static void _outputXmlSubtree(ConfigXmlTree tree, Writer writer,
            int indent) throws IOException {
        String indentString = _createIndent(indent);
        String elementName = tree.getElementName();

        // Starting tag
        StringBuffer starting = new StringBuffer(indentString);
        starting.append('<');
        starting.append(elementName);

        Enumeration attrs = tree.getAttributeNames();

        while (attrs.hasMoreElements()) {
            String attrName = (String) attrs.nextElement();
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

            while (tree.hasMoreChildren()) {
                _outputXmlSubtree(tree.nextChild(), writer, indent + 2);
            }

            // Ending tag
            writer.write(indentString + "</" + tree.getElementName() + ">\n");
        }
    }

    /** Return a string with the specified number of white spaces.
     * 
     *  @param indent The number of white spaces.
     *  @return A string with the specified number of white spaces.
     */
    private static String _createIndent(int indent) {
        StringBuffer buffer = new StringBuffer(indent);

        for (int i = 0; i < indent; i++) {
            buffer.append(' ');
        }

        return buffer.toString();
    }
}

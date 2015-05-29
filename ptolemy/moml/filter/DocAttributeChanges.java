/* Backwards compatibility changes for DocAttribute.

 Copyright (c) 2015 The Regents of the University of California.
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
package ptolemy.moml.filter;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
////DocAttributeChanges

/**
 * A MoML filter to convert StringParameters contained by DocAttribute to
 * StringAttributes.
 * 
 * @author Daniel Crawl
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red ()
 * @Pt.AcceptedRating Red ()
 */
public class DocAttributeChanges extends MoMLFilterSimple {

    /** Change StringParameter attributes insinde DocAttribute to
     *  StringAttribute.
     *  @param container  The container for this attribute.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {

        if (container != null
                && container.getClass().getName()
                        .equals(_docAttributeClassName)
                && attributeName.equals("class") && attributeValue != null
                && attributeValue.equals(_stringParameterClassName)) {
            MoMLParser.setModified(true);
            return _stringAttributeClassName;
        }
        return attributeValue;
    }

    /** In this class, do nothing. */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
    }

    /** Class name of DocAttribute. */
    private final static String _docAttributeClassName = "ptolemy.vergil.basic.DocAttribute";

    /** Class name of StringAttribute. */
    private final static String _stringAttributeClassName = "ptolemy.kernel.util.StringAttribute";

    /** Class name of StringParameter. */
    private final static String _stringParameterClassName = "ptolemy.data.expr.StringParameter";
}

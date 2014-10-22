/* XML handler that generates the library description for backtracking actors.

 Copyright (c) 2005-2014 The Regents of the University of California.
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

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ptolemy.backtrack.util.PathFinder;

import com.microstar.xml.XmlParser;

///////////////////////////////////////////////////////////////////
//// ConfigXmlHandler
/**
 XML handler that generates the library description for backtracking actors.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ConfigXmlHandler extends XmlHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Exclude the specified XML document from the scan.
     *
     *  @param canonicalPath The canonical path of the XML document to be
     *   excluded.
     */
    public void addExcludedFile(String canonicalPath) {
        _excludedFiles.add(canonicalPath);
    }

    /** Exclude the specified XML documents from the scan.
     *
     *  @param canonicalPaths The canonical paths of the XML documents to be
     *   excluded.
     */
    public void addExcludedFiles(Collection<String> canonicalPaths) {
        _excludedFiles.addAll(canonicalPaths);
    }

    /** Handle the end tag of an XML element. This method tests whether the XML
     *  element has a "class" attribute, whose value is a class name in the set
     *  of classes to keep. If so, the element is kept in the transformed XML
     *  document. Otherwise, the element is not output to the transformed XML
     *  document.
     *
     *  @param elementName The name of the XML element.
     *  @exception Exception If the overriding method in the superclass throws an
     *   Exception.
     */
    public void endElement(String elementName) throws Exception {
        boolean keep = _includedClasses == null
        // If null, every element is kept.
                || !getCurrentTree().isLeaf()
                // If not leaf, at least a descendant is kept.
                || (getCurrentTree().hasAttribute("class") && _includedClasses
                        .contains(getCurrentTree().getAttribute("class")));
        // A match in the set.

        if (keep) {
            String className = getCurrentTree().getAttribute("class");

            if (_REMOVED_ELEMENT_SET.contains(elementName)
                    || (className != null)
                    && _REMOVED_CLASS_SET.contains(className)) {
                // Omit this "input" element.
                getCurrentTree().startTraverseChildren();

                while (getCurrentTree().hasMoreChildren()) {
                    getCurrentTree().getParent().addChild(
                            getCurrentTree().nextChild());
                }
            } else {
                getCurrentTree().getParent().addChild(getCurrentTree());
            }
        }

        super.endElement(elementName);
    }

    /** Process the instruction given in the data. This method only handles
     *  the "moml" target type.
     *
     *  @param target The target (the name at the start of the processing
     *   instruction).
     *  @param data The data, if any (the rest of the processing instruction).
     *  @exception Exception If the MoML parser throws an exception.
     */
    public void processingInstruction(String target, String data)
            throws Exception {
        if (target.equals("moml")) {
            StringReader dataReader = new StringReader(data);
            XmlParser newParser = new XmlParser();
            ConfigXmlHandler newHandler = new ConfigXmlHandler(
                    getCurrentTree(), getSystemId(), _includedClasses);
            newHandler.addExcludedFiles(_excludedFiles);
            newParser.setHandler(newHandler);
            newParser.parse(getSystemId(), null, dataReader);
            dataReader.close();
        }
    }

    /** Handle the start tag of an XML element. If the element is an "input"
     *  element, the source referred to is parsed.
     *
     *  @param elementName The name of the XML element.
     *  @exception Exception If the overridden method in the superclass throws
     *   an Exception.
     */
    public void startElement(String elementName) throws Exception {
        super.startElement(elementName);

        if (elementName.equals("input")) {
            String fileName = getCurrentTree().getAttribute("source");

            String newName = null;
            try {
                newName = PathFinder.getPtolemyPath() + fileName;
                File newFile = new File(newName);

                if (!newFile.exists()) {
                    File oldFile = new File(getSystemId());
                    newName = oldFile.getParent() + "/" + fileName;
                    newFile = new File(newName);
                }

                String canonicalPath = newFile.getCanonicalPath();

                if (!_excludedFiles.contains(canonicalPath)
                        && !_parsedFiles.contains(canonicalPath)) {
                    _parsedFiles.add(canonicalPath);
                    ConfigParser subparser = new ConfigParser(getCurrentTree());
                    subparser.addExcludedFiles(_excludedFiles);
                    subparser.addExcludedFiles(_parsedFiles);
                    subparser.parseConfigFile(newName, _includedClasses, false);
                }
            } catch (Exception e) {
                System.err.println("ConfigXmlHandler: failed to parse \""
                        + newName + "\": " + e);
                // By default, do not change the element.
            }
        }
    }

    /** Construct an XML handler.
     *
     *  @param tree The XML tree to be scanned.
     *  @param systemId The system ID representing the location of the original
     *   XML document.
     *  @param includedClasses The classes in the original XML document that
     *   should be transformed in the new XML document.
     */
    ConfigXmlHandler(ConfigXmlTree tree, String systemId,
            Set<String> includedClasses) {
        super(tree, systemId);

        this._includedClasses = includedClasses;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** The class in the original XML document that should be removed when it
     *  is transformed to the library description of backtracking actors.
     */
    private static final String[] _REMOVED_CLASSES = new String[] {
            "ptolemy.kernel.CompositeEntity", "ptolemy.actor.gui.Configuration" };

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The class in the original XML document that should be removed when it
     *  is transformed to the library description of backtracking actors.
     */
    private static Set<String> _REMOVED_CLASS_SET = new HashSet<String>();

    /** The elements in the original XML document that should be removed when it
     *  is transformed to the library description of backtracking actors.
     */
    private static final String[] _REMOVED_ELEMENTS = new String[] {
            "configure", "input" };

    /** The elements in the original XML document that should be removed when it
     *  is transformed to the library description of backtracking actors.
     */
    private static Set<String> _REMOVED_ELEMENT_SET = new HashSet<String>();

    /** The canonical paths of the XML documents that should be excluded from
     *  the parsing.
     */
    private Set<String> _excludedFiles = new HashSet<String>();

    /** The names of the classes that should be kept in the transformed XML
     *  document. If it is null, all classes will be kept.
     */
    private Set<String> _includedClasses;

    /** The canonical paths of the XML documents that have been parsed by this
     *  parser, or by the parsers that parse the ancestor nodes of the current
     *  tree.
     */
    private Set<String> _parsedFiles = new HashSet<String>();

    ///////////////////////////////////////////////////////////////////
    ////                  static class initializer                 ////

    static {
        _REMOVED_ELEMENT_SET.addAll(Arrays.asList(_REMOVED_ELEMENTS));
        _REMOVED_CLASS_SET.addAll(Arrays.asList(_REMOVED_CLASSES));
    }
}

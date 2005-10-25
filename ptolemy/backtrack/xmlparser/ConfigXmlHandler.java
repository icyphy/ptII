/*

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

import ptolemy.backtrack.util.PathFinder;

import com.microstar.xml.XmlParser;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tfeng
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConfigXmlHandler extends XmlHandler {
    ConfigXmlHandler(ConfigXmlTree tree, String systemId, Set includedClasses) {
        super(tree, systemId);

        this.includedClasses = includedClasses;
    }

    public void addExcludedFile(String canonicalPath) {
        _excludedFiles.add(canonicalPath);
    }

    public void addExcludedFiles(Collection canonicalPaths) {
        _excludedFiles.addAll(canonicalPaths);
    }

    public void startElement(String elname) throws Exception {
        super.startElement(elname);

        if (elname.equals("input")) {
            String fileName = currentTree.getAttribute("source");

            try {
                String newName = PathFinder.getPtolemyPath() + fileName;
                File newFile = new File(newName);

                if (!newFile.exists()) {
                    File oldFile = new File(systemId);
                    newName = oldFile.getParent() + "/" + fileName;
                    newFile = new File(newName);
                }

                String canonicalPath = newFile.getCanonicalPath();

                if (!_excludedFiles.contains(canonicalPath)) {
                    ConfigParser subparser = new ConfigParser(currentTree);
                    subparser.addExcludedFiles(_excludedFiles);
                    subparser.parseConfigFile(newName, includedClasses, false);
                }
            } catch (Exception e) {
                // FIXME: For the time being...
            }
        }
    }

    public void endElement(String elname) throws Exception {
        boolean keep = (includedClasses == null) // If null, every element is kept.
                || !currentTree.isLeaf() // If not leaf, at least a descendant is kept.
                || ( // A match in the set.
                currentTree.hasAttribute("class") && includedClasses
                        .contains(currentTree.getAttribute("class")));

        if (keep) {
            String className = currentTree.getAttribute("class");

            if (REMOVE_ELEMENT_SET.contains(elname)
                    || ((className != null) && REMOVE_CLASS_SET
                            .contains(className))) { // Omit this "input" element.
                currentTree.startTraverseChildren();

                while (currentTree.hasMoreChildren()) {
                    currentTree.getParent().addChild(currentTree.nextChild());
                }
            } else {
                currentTree.getParent().addChild(currentTree);
            }
        }

        super.endElement(elname);
    }

    public void processingInstruction(String target, String data)
            throws Exception {
        if (target.equals("moml")) {
            StringReader dataReader = new StringReader(data);
            XmlParser newParser = new XmlParser();
            ConfigXmlHandler newHandler = new ConfigXmlHandler(currentTree,
                    systemId, includedClasses);
            newHandler.addExcludedFiles(_excludedFiles);
            newParser.setHandler(newHandler);
            newParser.parse(systemId, null, dataReader);
            dataReader.close();
        }
    }

    public static final String[] REMOVE_ELEMENTS = new String[] { "configure",
            "input" };

    public static final String[] REMOVE_CLASSES = new String[] {
            "ptolemy.kernel.CompositeEntity", "ptolemy.actor.gui.Configuration" };

    private Set _excludedFiles = new HashSet();

    private static Set REMOVE_ELEMENT_SET = new HashSet();

    private static Set REMOVE_CLASS_SET = new HashSet();

    private Set includedClasses; // If null, every element matches.

    static {
        REMOVE_ELEMENT_SET.addAll(Arrays.asList(REMOVE_ELEMENTS));
        REMOVE_CLASS_SET.addAll(Arrays.asList(REMOVE_CLASSES));
    }
}

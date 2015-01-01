/* Ptolemy XML configuration parser.

 Copyright (c) 2005-2013 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ptolemy.backtrack.util.PathFinder;

import com.microstar.xml.XmlParser;

///////////////////////////////////////////////////////////////////
//// ConfigParser

/**
 Ptolemy XML configuration parser. This parser builds an XML tree in memory
 as it parses a configuration. The tree is either a complete representation
 of the configuration file, or a partial representation that defines only the
 given Java classes. See {@link ConfigXmlHandler} for more information on the
 second usage.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ConfigParser {
    ///////////////////////////////////////////////////////////////////
    ////                        constructors                       ////

    /** Construct a configuration parser with no parent node. The parent node
     *  is default to <tt>null</tt>.
     */
    public ConfigParser() {
        this(new ConfigXmlTree(null));
    }

    /** Construct a configuration parser with a parent node.
     *
     *  @param xmlTree The parent node of the XML tree.
     */
    public ConfigParser(ConfigXmlTree xmlTree) {
        _xmlTree = xmlTree;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an excluded file to the XML handler.
     *
     *  @param canonicalPath The canonical path of the file to be excluded.
     *  @see ConfigXmlHandler#addExcludedFile(String)
     */
    public void addExcludedFile(String canonicalPath) {
        _excludedFiles.add(canonicalPath);
    }

    /** Add a collection of excluded files to the XML handler.
     *
     *  @param canonicalPaths The collection of canonical paths of the files to
     *   be excluded.
     *  @see ConfigXmlHandler#addExcludedFiles(Collection)
     */
    public void addExcludedFiles(Collection<String> canonicalPaths) {
        _excludedFiles.addAll(canonicalPaths);
    }

    /** Add a package prefix to the classes in the XML tree.
     *
     *  @param packagePrefix The package prefix to be added to the head of each
     *   class name.
     *  @param classes The names of affected classes.
     */
    public void addPackagePrefix(String packagePrefix, Set classes) {
        addPackagePrefix(_xmlTree, packagePrefix, classes);
    }

    /** Get the parsed XML tree.
     *
     *  @return The XML tree. It may be <tt>null</tt> if no configuration has
     *   been parsed.
     */
    public ConfigXmlTree getTree() {
        return _xmlTree;
    }

    /** The main function to test the functionality of this class. It parses
     *  the Ptolemy configuration file and extracts the nodes (and their
     *  parents) pertaining to class <tt>ptolemy.actor.lib.Sequence</tt>, and
     *  outputs the result to the standard output.
     *
     *  @param args The command-line arguments (not used).
     *  @exception Exception If error occurs.
     */
    public static void main(String[] args) throws Exception {
        String[] classes = new String[] { "ptolemy.actor.lib.Sequence" };
        Set<String> classSet = new HashSet<String>();
        classSet.addAll(Arrays.asList(classes));

        ConfigParser parser = new ConfigParser();
        parser.parseConfigFile(DEFAULT_SYSTEM_ID, classSet);
        parser.addPackagePrefix("ptolemy.backtrack", classSet);

        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        XmlOutput.outputXmlTree(parser.getTree(), writer);
        writer.close();
    }

    /** Parse a configuration file and build the XML tree below the given
     *  parent node (or <tt>null</tt> if not given). Only the nodes
     *  corresponding to the classes in <tt>includedClasses</tt> and their
     *  parent nodes are created. Other nodes in the XML tree are ignored.
     *  <p>
     *  This method is the same as <tt>parseConfigFile(fileName,
     *  includedClasses, true)</tt>.
     *
     *  @param fileName The name of the configuration file.
     *  @param includedClasses The set of names of classes to be included.
     *  @exception Exception If error occurs.
     *  @see #parseConfigFile(String, Set, boolean)
     */
    public void parseConfigFile(String fileName, Set<String> includedClasses)
            throws Exception {
        parseConfigFile(fileName, includedClasses, true);
    }

    /** Parse a configuration file and build the XML tree below the given
     *  parent node (or <tt>null</tt> if not given). Only the nodes
     *  corresponding to the classes in <tt>includedClasses</tt> and their
     *  parent nodes are created. Other nodes in the XML tree are ignored.
     *
     *  @param fileName The name of the configuration file.
     *  @param includedClasses The set of names of classes to be included.
     *  @param backtrackingElement Whether to set the parent node of the
     *   constructed XML tree to be the "backtrack" node.
     *  @exception Exception If error occurs.
     */
    public void parseConfigFile(String fileName, Set<String> includedClasses,
            boolean backtrackingElement) throws Exception {
        XmlParser parser = new XmlParser();
        BufferedReader br = new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(fileName), java.nio.charset.Charset.defaultCharset()));
        ConfigXmlHandler handler = new ConfigXmlHandler(_xmlTree, fileName,
                includedClasses);
        handler.addExcludedFiles(_excludedFiles);
        parser.setHandler(handler);
        try {
            // This fails to parse gtLibrary because it goes into a loop,
            // and causes a stack overflow, so we catch Throwable and
            // throw an exception.
            parser.parse(fileName, null, br);
        } catch (Throwable throwable) {
            throw new Exception("Failed to parse \"" + fileName + "\"",
                    throwable);
        }
        // Manually add a <group> element to the root of the tree.
        if (backtrackingElement) {
            _xmlTree._setElementName("group");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public fields                      ////

    // FindBugs suggests making these final.

    /** The default path of the default Ptolemy configuration file. It is
     *  usually located at <tt>ptolemy/configs/full/configuration.xml</tt> in
     *  the Ptolemy tree.
     */
    public static final String DEFAULT_SYSTEM_ID = PathFinder.getPtolemyPath()
            + "ptolemy/configs/full/configuration.xml";

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** The recursive function to traverse the XML tree and add a package
     *  prefix to each class name found in the given set.
     *
     *  @param tree The XML tree to be traversed.
     *  @param packagePrefix The package prefix to be added.
     *  @param classes The set of names of affected classes.
     */
    private void addPackagePrefix(ConfigXmlTree tree, String packagePrefix,
            Set classes) {
        String className = tree.getAttribute("class");

        if ((className != null) && classes.contains(className)) {
            tree.setAttribute("class", packagePrefix + "." + className);
        }

        tree.startTraverseChildren();

        while (tree.hasMoreChildren()) {
            addPackagePrefix(tree.nextChild(), packagePrefix, classes);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The set of excluded files.
     */
    private Set<String> _excludedFiles = new HashSet<String>();

    /** The XML tree.
     */
    private ConfigXmlTree _xmlTree;
}

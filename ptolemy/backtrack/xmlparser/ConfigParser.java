/*
 * Created on Feb 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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

/**
 * @author tfeng
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ConfigParser {

    public ConfigParser() {
        this(new ConfigXmlTree(null));
    }

    public ConfigParser(ConfigXmlTree xmlTree) {
        _xmlTree = xmlTree;
    }

    public void addExcludedFile(String canonicalPath) {
        _excludedFiles.add(canonicalPath);
    }
    
    public void addExcludedFiles(Collection canonicalPaths) {
        _excludedFiles.addAll(canonicalPaths);
    }

    public void parseConfigFile(String fileName, Set includedClasses)
            throws Exception {
        parseConfigFile(fileName, includedClasses, true);
    }
    
    public void parseConfigFile(String fileName, Set includedClasses,
            boolean backtrackingElement) throws Exception {
        XmlParser parser = new XmlParser();
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        ConfigXmlHandler handler = new ConfigXmlHandler(_xmlTree, fileName, includedClasses);
        handler.addExcludedFiles(_excludedFiles);
        parser.setHandler(handler);
        parser.parse(fileName, null, br);

        // Manually modify the resulting tree.
        if (backtrackingElement) {
            _xmlTree.setElementName("entity");
            _xmlTree.setAttribute("name", "Backtracking");
            _xmlTree.setAttribute("class", "ptolemy.moml.EntityLibrary");
        }
    }

    public void addPackagePrefix(String packagePrefix, Set classes) {
        addPackagePrefix(_xmlTree, packagePrefix, classes);
    }

    protected void addPackagePrefix(ConfigXmlTree tree, String packagePrefix, Set classes) {
        String className = tree.getAttribute("class");
        if (className != null && classes.contains(className)) {
            tree.setAttribute("class", packagePrefix + "." + className);
        }
        tree.startTraverseChildren();
        while (tree.hasMoreChildren())
            addPackagePrefix(tree.nextChild(), packagePrefix, classes);
    }

    public ConfigXmlTree getTree() {
        return _xmlTree;
    }

    public static void main(String[] args)
            throws Exception {
        String[] classes = new String[]{
            "ptolemy.actor.lib.Sequence"
        };
        Set classSet = new HashSet();
        classSet.addAll(Arrays.asList(classes));

        ConfigParser parser = new ConfigParser();
        parser.parseConfigFile(DEFAULT_SYSTEM_ID, classSet);
        parser.addPackagePrefix("ptolemy.backtrack", classSet);

        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        XmlOutput.outputXmlTree(parser.getTree(), writer);
        writer.close();
    }

    public static String DEFAULT_SYSTEM_ID =
        PathFinder.getPtolemyPath() +
        "ptolemy/configs/full/configuration.xml";

    private ConfigXmlTree _xmlTree;
    
    private Set _excludedFiles = new HashSet();
}

/*
 * Created on Feb 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ptolemy.backtrack.xmlparser;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ptolemy.util.StringUtilities;

import com.microstar.xml.XmlParser;

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

    public void startElement(String elname) throws Exception {
        super.startElement(elname);

        if (elname.equals("input")) {
            String fileName = currentTree.getAttribute("source");
            try {
                String newName = PTOLEMY_PATH + fileName;
                File newFile = new File(newName);
                if (!newFile.exists()) {
                    File oldFile = new File(systemId);
                    newName = oldFile.getParent() + "/" + fileName;
                }

                ConfigParser subparser = new ConfigParser(currentTree);
                subparser.parseConfigFile(newName, includedClasses);
            } catch (Exception e) {
                // FIXME: For the time being...
            }
        }
    }

    public void endElement(String elname) throws Exception {
        boolean keep = includedClasses == null ||   // If null, every element is kept.
                       !currentTree.isLeaf() ||     // If not leaf, at least a descendant is kept.
                       (                            // A match in the set.
                           currentTree.hasAttribute("class") &&
                           includedClasses.contains(currentTree.getAttribute("class"))
                       );
        if (keep) {
            String className = currentTree.getAttribute("class");
            if (REMOVE_ELEMENT_SET.contains(elname) ||
                    (className != null && REMOVE_CLASS_SET.contains(className))) {   // Omit this "input" element.
                currentTree.startTraverseChildren();
                while (currentTree.hasMoreChildren())
                    currentTree.getParent().addChild(currentTree.nextChild());
            } else
                currentTree.getParent().addChild(currentTree);
        }

        super.endElement(elname);
    }

    public void processingInstruction(String target, String data)
            throws Exception {
        if (target.equals("moml")) {
            StringReader dataReader = new StringReader(data);
            XmlParser newParser = new XmlParser();
            ConfigXmlHandler newHandler = new ConfigXmlHandler(currentTree, systemId, includedClasses);
            newParser.setHandler(newHandler);
            newParser.parse(systemId, null, dataReader);
            dataReader.close();
        }
    }

    public static String PTOLEMY_PATH = "../../../";

    public static final String[] REMOVE_ELEMENTS = new String[] {
        "configure",
        "input"
    };

    public static final String[] REMOVE_CLASSES = new String[] {
        "ptolemy.kernel.CompositeEntity",
        "ptolemy.actor.gui.Configuration"
    };

    private static Set REMOVE_ELEMENT_SET = new HashSet();

    private static Set REMOVE_CLASS_SET = new HashSet();

    private Set includedClasses;    // If null, every element matches.

    static {
        REMOVE_ELEMENT_SET.addAll(Arrays.asList(REMOVE_ELEMENTS));
        REMOVE_CLASS_SET.addAll(Arrays.asList(REMOVE_CLASSES));

        String PTII = StringUtilities.getProperty("ptolemy.ptII.dir");
        if (PTII != null)
            PTOLEMY_PATH = PTII + File.separatorChar;
    }

}

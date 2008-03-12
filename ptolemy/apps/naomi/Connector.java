/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

package ptolemy.apps.naomi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.PtolemyPreferences;
import ptolemy.actor.gui.UserActorLibrary;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.VergilErrorHandler;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Connector extends MoMLApplication {

    /**
     * @param basePath
     * @param args
     * @throws Exception
     */
    public Connector(String basePath, String[] args) throws Exception {
        super(basePath, args);
    }

    /**
     * @param args
     * @throws Exception
     */
    public Connector(String[] args) throws Exception {
        super(args);
    }

    public static void main(String[] args) {
        try {
            new Connector(args).processCommand();
        } catch (Throwable t) {
            MessageHandler.error("Command failed", t);
            System.err.print(KernelException.stackTraceToString(t));
            System.exit(1);
        }
    }

    public void processCommand() throws IllegalActionException {
        if (_root == null) {
            throw new IllegalActionException("NAOMI root directory is not "
                    + "specified with -root argument.");
        } else if (!_pathExists(_root)) {
            throw new IllegalActionException("NAOMI root directory \""
                    + _root + "\" does not exist.");
        }

        if (_command == null) {
            throw new IllegalActionException(
                    "No command is specified with the -cmd argument.");
        }

        if (_inModelName == null) {
            throw new IllegalActionException(
                    "No input model is specified with the -in argument.");
        }

        if (_owner == null) {
            throw new IllegalActionException(
                    "No owner is specified with the -owner argument.");
        }

        File interfaceFile = new File(new File(new File(_root, "interfaces"),
                _owner), _owner + "_interface.xml");
        if (!(interfaceFile.exists() && interfaceFile.isFile())) {
            throw new IllegalActionException("Interface file " +
                    interfaceFile.getPath() + " does not exist.");
        }
        _loadInterface(interfaceFile);

        NamedObj model;
        try {
            model = _parseInModel();
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Cannot parse input model.");
        }

        switch (_command) {
        case LIST:
            for (Object attrObject : model.attributeList(Variable.class)) {
                Attribute attr = (Attribute) attrObject;
                for (Object paramObject
                        : attr.attributeList(NaomiParameter.class)) {
                    NaomiParameter naomiParam = (NaomiParameter) paramObject;
                    String attributeName = naomiParam.getAttributeName();
                    boolean load = _inputAttributes.contains(attributeName);
                    boolean save = _outputAttributes.contains(attributeName);
                    String expression = naomiParam.getExpression();
                    if (load && save) {
                        System.out.println("Sync: " + expression);
                    } else if (load) {
                        System.out.println("Load: " + expression);
                    } else if (save) {
                        System.out.println("Save: " + expression);
                    }
                }
            }
            break;

        case SYNC:
            File attributesPath = new File(_root, "attributes");
            if (!_pathExists(attributesPath)) {
                throw new IllegalActionException("Attributes directory \""
                        + attributesPath + "\" does not exist.");
            }
            _loadAttributes(model, attributesPath);
            _saveAttributes(model, attributesPath);
            _outputModel(model);
        }
    }

    public static final String[][] COMMAND_OPTIONS = new String[][] {
        {"-cmd", "<command>"},
        {"    ", "list (list NAOMI attributes)"},
        {"    ", "sync (synchronize NAOMI attributes)"},
        {"-in", "<input model>"},
        {"-out", "<output model>"},
        {"-owner", "<owner>"},
        {"-root", "<NAOMI root directory>"}
    };

    public static final String[][] NAMESPACES = new String[][] {
        {"att", "http://www.atl.lmco.com/naomi/attributes"},
        {"inf", "http://www.atl.lmco.com/naomi/interfaces"},
        {"xsi", "http://www.w3.org/2001/XMLSchema-instance"}
    };

    public static class MappedNamespaceContext implements NamespaceContext {

        public MappedNamespaceContext(Map<String, String> map) {
            _map = map;
        }

        public MappedNamespaceContext(String[][] map) {
            _map = new HashMap<String, String>();
            for (String[] entry : map) {
                _map.put(entry[0], entry[1]);
            }
        }

        public String getNamespaceURI(String prefix) {
            return _map.get(prefix);
        }

        public String getPrefix(String namespaceURI) {
            for (Map.Entry<String, String> entry : _map.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public Iterator<String> getPrefixes(String namespaceURI) {
            List<String> keys = new LinkedList<String>();
            for (Map.Entry<String, String> entry : _map.entrySet()) {
                if (entry.getValue().equals(namespaceURI)) {
                    keys.add(entry.getKey());
                }
            }
            return keys.iterator();
        }

        private Map<String, String> _map;
    }

    public enum Command {
        LIST("list"), SYNC("sync");

        public String toString() {
            return _name;
        }

        Command(String name) {
            _name = name;
        }

        private String _name;
    }

    protected Configuration _createDefaultConfiguration() throws Exception {
        if (_configurationURL == null) {
            _configurationURL = specToURL(_basePath
                    + "/full/configuration.xml");
        }

        // This has the side effects of merging properties from ptII.properties
        Configuration configuration = super._createDefaultConfiguration();

        try {
            configuration = readConfiguration(_configurationURL);
        } catch (Exception ex) {
            throw new Exception("Failed to read configuration '"
                    + _configurationURL + "'", ex);
        }

        // Read the user preferences, if any.
        PtolemyPreferences.setDefaultPreferences(configuration);

        // If _hideUserLibraryAttribute is not present, or is false,
        // call openUserLibrary().  openUserLibrary() will open either the
        // user library or the library named by the _alternateLibraryBuilder.
        Parameter hideUserLibraryAttribute = (Parameter) configuration
                .getAttribute("_hideUserLibrary", Parameter.class);

        if ((hideUserLibraryAttribute == null)
                || hideUserLibraryAttribute.getExpression().equals("false")) {

            // Load the user library.
            try {
                MoMLParser.setErrorHandler(new VergilErrorHandler());
                UserActorLibrary.openUserLibrary(configuration);
            } catch (Exception ex) {
                MessageHandler.error("Failed to display user library.", ex);
            }
        }

        return configuration;
    }

    protected void _loadAttributes(NamedObj model, File attributesPath)
    throws IllegalActionException {
        for (Object attrObject : model.attributeList(Variable.class)) {
            Attribute attr = (Attribute) attrObject;
            for (Object paramObject
                    : attr.attributeList(NaomiParameter.class)) {
                NaomiParameter naomiParam = (NaomiParameter) paramObject;
                String attributeName = naomiParam.getAttributeName();
                if (!_inputAttributes.contains(attributeName)) {
                    continue;
                }
                File attributeFile = new File(attributesPath, attributeName);

                Date attributeDate = naomiParam.getModifiedDate();
                Date fileDate = new Date(attributeFile.lastModified());
                if (!attributeFile.exists() || !attributeFile.isFile()
                        || !attributeDate.before(fileDate)) {
                    continue;
                }

                try {
                    Document document = _parseXML(attributeFile);

                    XPathFactory xpathFactory = XPathFactory.newInstance();
                    XPath xpath = xpathFactory.newXPath();
                    xpath.setNamespaceContext(new MappedNamespaceContext(
                            NAMESPACES));
                    XPathExpression expr = xpath.compile(
                            "/att:attribute/att:value");
                    String value = (String) expr.evaluate(document);
                    System.out.println("Load: " + attributeName + " = " +
                            value);
                    value = StringUtilities.unescapeForXML(value);

                    String moml = "<property name=\"" + attr.getName() + "\" " +
                            "value=\"" + value + "\"/>";
                    MoMLChangeRequest request =
                        new MoMLChangeRequest(this, attr.getContainer(), moml);
                    request.execute();

                    moml = "<property name=\"" + naomiParam.getName() + "\" " +
                            "value=\"" + naomiParam.getAttributeName() + " (" +
                            NaomiParameter.DATE_FORMAT.format(fileDate) +
                            ")\"/>";
                    request = new MoMLChangeRequest(this, attr, moml);
                    request.execute();
                } catch (XPathExpressionException e) {
                    throw new KernelRuntimeException(e, "Unexpected error.");
                }

                break;
            }
        }
    }

    protected void _loadInterface(File file) throws IllegalActionException {
        Document document = _parseXML(file);

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(new MappedNamespaceContext(NAMESPACES));
        XPathExpression expr;

        String[] nodeTypes = new String[] {"input", "output"};
        List<Set<String>> sets = new LinkedList<Set<String>>();
        sets.add(_inputAttributes);
        sets.add(_outputAttributes);
        for (int i = 0; i < nodeTypes.length; i++) {
            try {
                expr = xpath.compile("/inf:interface/inf:" + nodeTypes[i]);
                NodeList nodes = (NodeList) expr.evaluate(document,
                        XPathConstants.NODESET);
                for (int j = 0; j < nodes.getLength(); j++) {
                    Node node = nodes.item(j);
                    String text = node.getTextContent();
                    sets.get(i).add(text);
                }
            } catch (XPathExpressionException e) {
                throw new KernelRuntimeException(e, "Unexpected error.");
            }
        }
    }

    protected void _outputModel(NamedObj model) throws IllegalActionException {
        try {
            PrintStream stream;
            if (_outModelName == null) {
                stream = System.out;
            } else {
                stream = new PrintStream(_outModelName);
            }
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            model.exportMoML(writer);
            writer.flush();
        } catch (Exception e) {
            throw new IllegalActionException(null, e,
                    "Cannot output result model");
        }
    }

    protected void _parseArgs(final String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println(_usage());
            System.exit(0);
        }

        List<String> argsToProcess = new LinkedList<String>();

        for (int i = 0; i < args.length; i++) {
            if (!_parseConnectorArg(args[i])) {
                argsToProcess.add(args[i]);
            }
        }

        if (_isExpectingValue()) {
            throw new IllegalActionException("Missing configuration");
        }

        String[] processedArgs = (String[]) argsToProcess
                .toArray(new String[argsToProcess.size()]);

        super._parseArgs(processedArgs);
    }

    protected boolean _parseConnectorArg(String arg)
    throws IllegalActionException {
        arg = arg.toLowerCase();
        if (arg.startsWith("-")) {
            if (_isExpectingValue()) {
                throw new IllegalActionException("Expecting value for argument "
                        + "\"" + arg + "\"");
            } else if (arg.equals("-cmd") && _command == null) {
                _expectingCommand = true;
                return true;
            } else if (arg.equals("-in") && _inModelName == null) {
                _expectingInModelName = true;
                return true;
            } else if (arg.equals("-out") && _outModelName == null) {
                _expectingOutModelName = true;
                return true;
            } else if (arg.equals("-owner") && _owner == null) {
                _expectingOwner = true;
                return true;
            } else if (arg.equals("-root") && _root == null) {
                _expectingRoot = true;
                return true;
            }
        } else if (_expectingCommand) {
            for (Command cmd : Command.values()) {
                if (arg.equals(cmd.toString())) {
                    _command = cmd;
                    _expectingCommand = false;
                    return true;
                }
            }
            throw new IllegalActionException("Unknown command: " + arg);
        } else if (_expectingInModelName) {
            _inModelName = arg;
            _expectingInModelName = false;
            return true;
        } else if (_expectingOutModelName) {
            _outModelName = arg;
            _expectingOutModelName = false;
            return true;
        } else if (_expectingOwner) {
            _owner = arg;
            _expectingOwner = false;
            return true;
        } else if (_expectingRoot) {
            _root = arg;
            _expectingRoot = false;
            return true;
        }
        return false;
    }

    protected void _saveAttributes(NamedObj model, File attributesPath)
    throws IllegalActionException {

        for (Object attrObject : model.attributeList(Variable.class)) {
            Variable attr = (Variable) attrObject;
            for (Object paramObject
                    : attr.attributeList(NaomiParameter.class)) {
                NaomiParameter naomiParam = (NaomiParameter) paramObject;
                String attributeName = naomiParam.getAttributeName();
                if (!_outputAttributes.contains(attributeName)) {
                    continue;
                }

                File attributeFile = new File(attributesPath, attributeName);

                Date attributeDate = naomiParam.getModifiedDate();
                Date fileDate = new Date(attributeFile.lastModified());
                if (attributeFile.exists() && attributeFile.isFile()
                        && !fileDate.before(attributeDate)) {
                    continue;
                }

                String newValue = attr.getExpression();
                System.out.println("Save: " + attributeName + " = " + newValue);

                try {
                    DocumentBuilderFactory docFactory =
                        DocumentBuilderFactory.newInstance();
                    docFactory.setNamespaceAware(true);
                    DocumentBuilder builder;
                    builder = docFactory.newDocumentBuilder();
                    DOMImplementation impl = builder.getDOMImplementation();
                    Document document = impl.createDocument(NAMESPACES[0][1],
                            "attribute", null);
                    Element root = document.getDocumentElement();
                    Element owner = document.createElementNS(NAMESPACES[0][1],
                            "owner");
                    owner.setTextContent(_owner);
                    root.appendChild(owner);
                    Element value = document.createElementNS(NAMESPACES[0][1],
                            "value");
                    value.setTextContent(StringUtilities.escapeForXML(
                            newValue));
                    root.appendChild(value);
                    Element units = document.createElementNS(NAMESPACES[0][1],
                            "units");
                    root.appendChild(units);
                    Element documentation = document.createElementNS(
                            NAMESPACES[0][1], "documentation");
                    root.appendChild(documentation);

                    FileOutputStream stream =
                        new FileOutputStream(attributeFile);
                    _serializeXML(document, stream);

                    attributeFile.setLastModified(attributeDate.getTime());
                } catch (ParserConfigurationException e) {
                    throw new IllegalActionException(null, e,
                            "Cannot create DocumentBuilder.");
                } catch (FileNotFoundException e) {
                    throw new IllegalActionException(null, e,
                            "Cannot create attribute file: "
                            + attributeFile.getPath());
                }
            }
        }
    }

    protected String _usage() {
        return _configurationUsage(
                "java " + getClass().getName() + " [ options ]",
                COMMAND_OPTIONS, new String[] {});
    }

    private boolean _isExpectingValue() {
        return _expectingInModelName || _expectingOutModelName
                || _expectingCommand || _expectingRoot || _expectingOwner;
    }

    private NamedObj _parseInModel() throws Exception {
        URL inURL = specToURL(_inModelName);
        URL base = inURL;
        MoMLParser parser = new MoMLParser();
        return parser.parse(base, inURL);
    }

    private Document _parseXML(File file) throws IllegalActionException {
        try {
            DocumentBuilderFactory docFactory =
                DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            DocumentBuilder builder = docFactory.newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException e) {
            throw new IllegalActionException(null, e,
                    "Cannot create DocumentBuilder.");
        } catch (SAXException e) {
            throw new IllegalActionException(null, e,
                    "Fail to parse attribute file: " + file);
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Cannot read from attribute file: " + file);
        }
    }

    private boolean _pathExists(File path) {
        return path.exists() && path.isDirectory();
    }

    private boolean _pathExists(String path) {
        return _pathExists(new File(path));
    }

    private void _serializeXML(Document document, OutputStream stream)
    throws IllegalActionException {
        try {
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(stream);
            TransformerFactory transformerFactory =
                TransformerFactory.newInstance();
            Transformer serializer = transformerFactory.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
            stream.flush();
            stream.close();
        } catch (TransformerException e) {
            throw new IllegalActionException(null, e,
                    "Unable to serialize XML stream.");
        } catch (IOException e) {
            throw new IllegalActionException(null, e,
                    "Unable to serialize XML stream.");
        }
    }

    private Command _command;

    private URL _configurationURL;

    private boolean _expectingCommand;

    private boolean _expectingInModelName;

    private boolean _expectingOutModelName;

    private boolean _expectingOwner;

    private boolean _expectingRoot;

    private String _inModelName;

    private Set<String> _inputAttributes = new HashSet<String>();

    private String _outModelName;;

    private Set<String> _outputAttributes = new HashSet<String>();

    private String _owner;

    private String _root;
}

/* Instantiate a Functional Mock-up Unit (FMU).

 Copyright (c) 2011-2012 The Regents of the University of California.
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
package ptolemy.actor.lib.fmi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 * Invoke a Functional Mock-up Interface (FMI) 1.0 Model Exchange 
 * Functional Mock-up Unit (FMU).
 * 
 * <p>Read in a <code>.fmu</code> file named by the 
 * <i>fmuFile</i> parameter.  The <code>.fmu</code> file is a zipped
 * file that contains a file named <code>modelDescription.xml</code>
 * that describes the ports and parameters that are created.
 * At run time, method calls are made to C functions that are
 * included in shared libraries included in the <code>.fmu</code>
 * file.</p>
 * 
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @author Christopher Brooks, Michael Wetter, Edward A. Lee, 
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUImport extends TypedCompositeActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMUImport(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        fmuFile = new FileParameter(this, "fmuFile");
        fmuFile.setExpression("fmuImport.fmu");
    }

    /** The Functional Mock-up Unit (FMU) file.
     *  The FMU file is a zip file that contains a file named "modelDescription.xml"
     *  and any necessary shared libraries.  The file is read when this
     *  actor is instantiated or when the file name changes.  The initial default
     *  value is "fmuImport.fmu".
     */
    public FileParameter fmuFile;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fmuFile</i>, then unzip
     *  the file and load in the .xml file, creating and deleting parameters
     *  as necessary.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *  is <i>fmuFile</i> and the file cannot be opened or there
     *  is a problem creating or destroying the parameters
     *  listed in thile.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fmuFile) {
            try {
                _updateParameters();
            } catch (NameDuplicationException e) {
                throw new IllegalActionException(this, e, "Name duplication");
            }
        }

        super.attributeChanged(attribute);
    }

    /** 
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Create ports and parameters
     * @param domNode The Node representation of the modelDescription.xml file
     * read in from the <i>fmuFile</i> parameter. 
     * @throws IllegalActionException If a port or parameter cannot be created.
     * @throws NameDuplicationException If there already exists a port or
     * parameter with the same name as the port or parameter to be created.
     */
    private void _createPortsAndParameters(Node domNode)
            throws IllegalActionException, NameDuplicationException {
        int type = domNode.getNodeType();
        if (type == Node.ELEMENT_NODE) {
            String name = "";
            String value = "";
            boolean foundParameter = false;
            boolean foundPort = false;
            NamedNodeMap attributes = domNode.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (attribute.getNodeType() == Node.ATTRIBUTE_NODE){
                    if (attribute.getNodeName().equals("name")) {
                        name = attribute.getNodeValue();
                        foundPort = true;
                    }
                    if (attribute.getNodeName().equals("variability")) {
                        name = attribute.getNodeValue();
                        if (attribute.getNodeValue().equals("parameter"))
                            foundPort = false;
                            foundParameter = true;
                    }
                }
                for (Node child = domNode.getFirstChild(); child != null;
                        child = child.getNextSibling()) {
                    if (!child.getNodeName().equals("Real")) {
                        continue;
                    }
                    NamedNodeMap subattributes = child.getAttributes();
                    for (int j = 0; j < subattributes.getLength(); j++) {
                        Node subattribute = subattributes.item(i);
                        if (subattribute != null
                                && subattribute.getNodeName().equals("start")) {
                            value = subattribute.getNodeValue();
                        }
                    }
                }
            }
            if (foundParameter) {
                System.out.println("Creating parameter: " + name + " " + value);
                Parameter parameter = new Parameter(this, name);
                parameter.setExpression(value);
                // Prevent exporting this to MoML unless it has
                // been overridden.
                parameter.setDerivedLevel(1);
                foundParameter = false;
            } else if (foundPort) {
                System.out.println("Creating Port: " + name);
                // FIXME: All output ports?
                TypedIOPort port = new TypedIOPort(this, name, false, true);
                port.setDerivedLevel(1);
            }
        }
    }
    
    /**
     * Indent to the current level in multiples of _basicIndent.
     */
    private void _outputIndentation() {
        // Based on DomEcho.java from
        // http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
        for (int i = 0; i < _indent; i++) {
            System.out.print(_basicIndent);
        }
    }
    
    /** Read in a .xml file and return a Document.
     * 
     * @param xmlFile The .xml file to be read in, typically modelDescription.xml
     * from the file named by the <i>fmuFile</i> parameter.
     * @return A Document Object Model (DOM) suitable for parsing
     * @throws IllegalActionException If the xmlFile cannot be parsed.
     */
    private Document _parseXMLFile(File xmlFile) throws IllegalActionException {
        // Based on DomEcho.java from
        // http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
        System.out.println("FMUImport: parsing " + xmlFile);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // Using factory get an instance of document builder.
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse using builder to get DOM representation of the XML file.
            return db.parse(xmlFile.getCanonicalPath());
        } catch(Exception pce) {
            throw new IllegalActionException(this, pce, "Failed to parse " + xmlFile);
        }
    }
    
    /**
     * Echo common attributes of a DOM2 Node and terminate output with an
     * EOL character.
     * @param n The node.
     */
    private void _printlnCommon (Node n) {
        // Based on DomEcho.java from
        // http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
        System.out.print(" nodeName=\"" + n.getNodeName() + "\"");

        String val = n.getNamespaceURI();
        if (val != null) {
            System.out.print(" uri=\"" + val + "\"");
        }

        val = n.getPrefix();
        if (val != null) {
            System.out.print(" pre=\"" + val + "\"");
        }

        val = n.getLocalName();
        if (val != null) {
            System.out.print(" local=\"" + val + "\"");
        }

        val = n.getNodeValue();
        if (val != null) {
            System.out.print(" nodeValue=");
            if (val.trim().equals("")) {
                // Whitespace
                System.out.print("[WS]");
            } else {
                System.out.print("\"" + n.getNodeValue() + "\"");
            }
        }
        System.out.println();
    }
    
    /**
     * @param  
     */  
    private void _traverseDOM(Node domNode)
            throws IllegalActionException, NameDuplicationException {
        // Based on DomEcho.java from
        // http://docs.oracle.com/javase/tutorial/jaxp/dom/readingXML.html
        // Indent to the current level before printing anything
        _outputIndentation();

        int type = domNode.getNodeType();
        switch (type) {
        case Node.ATTRIBUTE_NODE:
            System.out.print("ATTR:");
            _printlnCommon(domNode);
            break;
        case Node.CDATA_SECTION_NODE:
            System.out.print("CDATA:");
            _printlnCommon(domNode);
            break;
        case Node.COMMENT_NODE:
            System.out.print("COMM:");
            _printlnCommon(domNode);
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            System.out.print("DOC_FRAG:");
            _printlnCommon(domNode);
            break;
        case Node.DOCUMENT_NODE:
            System.out.print("DOC:");
            _printlnCommon(domNode);
            break;
        case Node.DOCUMENT_TYPE_NODE:
            System.out.print("DOC_TYPE:");
            _printlnCommon(domNode);

            // Print entities if any
            NamedNodeMap nodeMap = ((DocumentType)domNode).getEntities();
            _indent += 2;
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Entity entity = (Entity)nodeMap.item(i);
                _traverseDOM(entity);
            }
            _indent -= 2;
            break;
        case Node.ELEMENT_NODE:
            System.out.print("ELEM:");
            _printlnCommon(domNode);

            // Print attributes if any.  Note: element attributes are not
            // children of ELEMENT_NODEs but are properties of their
            // associated ELEMENT_NODE.  For this reason, they are printed
            // with 2x the indent level to indicate this.
            NamedNodeMap atts = domNode.getAttributes();
            _indent += 2;
            for (int i = 0; i < atts.getLength(); i++) {
                Node att = atts.item(i);
                _traverseDOM(att);
            }
            _indent -= 2;
            
            break;
        case Node.ENTITY_NODE:
            System.out.print("ENT:");
            _printlnCommon(domNode);
            break;
        case Node.ENTITY_REFERENCE_NODE:
            System.out.print("ENT_REF:");
            _printlnCommon(domNode);
            break;
        case Node.NOTATION_NODE:
            System.out.print("NOTATION:");
            _printlnCommon(domNode);
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            System.out.print("PROC_INST:");
            _printlnCommon(domNode);
            break;
        case Node.TEXT_NODE:
            System.out.print("TEXT:");
            _printlnCommon(domNode);
            break;
        default:
            System.out.print("UNSUPPORTED NODE: " + type);
            _printlnCommon(domNode);
            break;
        }

        // Print children if any
        _indent++;
        for (Node child = domNode.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            _traverseDOM(child);
            if (child.getNodeName().equals("ScalarVariable")) {
                _createPortsAndParameters(child);
            }
        }
        _indent--;
    }
    
    /** Unzip a file.
     *  Based on http://java.sun.com/developer/technicalArticles/Programming/compression/
     *  @param zipFilename  The file to be unzipped.
     *  @return a list of canonical paths to the files created
     *  @exception IOException if the file cannot be opened, if there are problems reading
     *  the zip file or if there are problems creating the files or directories.
     */
    private List<File> _unzip(String zipFileName) throws IOException {
        // FIXME: Use URLs, not files so that we can work from JarZip files.
        BufferedOutputStream destination = null;
        FileInputStream fileInputStream = new FileInputStream(zipFileName);
        ZipInputStream zipInputStream =
            new ZipInputStream(new BufferedInputStream(fileInputStream));
        ZipEntry entry;
        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        // FIXME: maybe put this in the tmp directory?
        String topDirectory = zipFileName.substring(0, zipFileName.length() - 4);
        List<File> files = new LinkedList<File>();
        try {
            while((entry = zipInputStream.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                String entryName = entry.getName();
                File destinationFile = new File(topDirectory, entryName);
                File destinationParent = destinationFile.getParentFile();
                // If the directory does not exist, create it.
                if (!destinationParent.isDirectory()
                    && !destinationParent.mkdirs()) {
                    throw new IOException("Failed to create \""
                            + destinationParent + "\".");
                }
                // If the entry is not a directory, then write the file.
                if (!entry.isDirectory()) {
                    // Write the files to the disk.
                    FileOutputStream fos = new FileOutputStream(destinationFile); 
                    destination = new BufferedOutputStream(fos, BUFFER);
                    int count;
                    while ((count = zipInputStream.read(data, 0, BUFFER)) 
                            != -1) {
                        destination.write(data, 0, count);
                    }
                    destination.flush();
                    destination.close();
                    files.add(destinationFile);
                }
            }
        } finally {
            if (zipInputStream != null) {
                zipInputStream.close();
            }
        }
        return files;
    }

    /** Update the parameters listed in the modelDescription.xml file
     *  contained in the zipped file named by the <i>fmuFile</i>
     *  parameter
     *  @exception IllegalActionException If the file named by the
     *  <i>fmuFile<i> parameter cannot be unzipped or if there
     *  is a problem deleting any pre=existing parameters or
     *  creating new parameters.
     * @throws NameDuplicationException If a paramater to be created
     * has the same name as a pre-existing parameter.
     */
    private void _updateParameters()
            throws IllegalActionException, NameDuplicationException {
        // Unzip the fmuFile.  We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        List<File> files = null;
        try {
            // FIXME: Use URLs, not files so that we can work from JarZip files.
            
            // Only read the file if the name has changed from the last time we
            // read the file or if the modification time has changed.
            fmuFileName = fmuFile.asFile().getCanonicalPath();
            if (fmuFileName == _fmuFileName) {
                return;
            }
            _fmuFileName = fmuFileName;
            long modificationTime = new File(fmuFileName).lastModified();
            if (_fmuFileModificationTime == modificationTime) {
                return;
            }
            _fmuFileModificationTime = modificationTime;
            files = _unzip(fmuFileName);
            
            // Find the modelDescription.xml file.
            File modelDescriptionFile = null;
            for (File file : files) {
                if (file.getName().endsWith("modelDescription.xml")) {
                    modelDescriptionFile = file;
                    break;
                }
            }
            if (modelDescriptionFile == null) {
                throw new IllegalActionException(this, "File modelDescription.xml is missing "
                        + "from the fmu archive \""
                        + fmuFileName + "\"/");
            }
            // Read the modelDescription.xml file.
            Document modelDescription = _parseXMLFile(modelDescriptionFile);
            
            // Create parameters and ports.
            _traverseDOM(modelDescription);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip \"" + fmuFileName + "\".");
        }
    }

    /** The name of the fmuFile.
     *  The _fmuFileName field is set the first time we read
     *  the file named by the <i>fmuFile</i> parameter.  The
     *  file named by the <i>fmuFile</i> parameter is only read
     *  if the name has changed or if the modification time of 
     *  the file is later than the time the file was last read.
     */
    private String _fmuFileName = null;
    
    /** The modification time of the file named by the
     *  <i>fmuFile</i> parameter the last time the file was read.
     */
    private long _fmuFileModificationTime = -1;
    
    /** Indent level */
    private int _indent = 0;

    /** Indentation will be in multiples of basicIndent  */
    private final String _basicIndent = "  ";

}

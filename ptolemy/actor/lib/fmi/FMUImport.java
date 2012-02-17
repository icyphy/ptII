/* Instantiate a Functional Mock-up Unit (FMU).

 Copyright (c) 2011 The Regents of the University of California.
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
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FMUImport

/**
 Instantiate a Functional Mock-up Unit (FMU).

 @author Christopher Brooks, Michael Wetter, Edward A. Lee, 
 @version $Id$
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
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
     *  and any necessary shared libraries.
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

    /** Unzip a file.
     *  Based on http://java.sun.com/developer/technicalArticles/Programming/compression/
     *  @param zipFilename  The file to be unzipped.
     *  @return a list of canonical paths to the files created
     *  @exception IOException if the file cannot be opened, if there are problems reading
     *  the zip file or if there are problems creating the files or directories.
     */
    private List<File> _unzip(String zipFileName) throws IOException {
        BufferedOutputStream destination = null;
        FileInputStream fileInputStream = new FileInputStream(zipFileName);
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
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
     *  is a problem deleting any preexisting parameters or
     *  creating new parameters.
     * @throws NameDuplicationException 
     */
    private void _updateParameters() throws IllegalActionException, NameDuplicationException {
        // Unzip the fmuFile.  We probably need to do this
        // because we will need to load the shared library later.
        String fmuFileName = null;
        List<File> files = null;
        try {
            fmuFileName = fmuFile.asFile().getCanonicalPath();
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
                throw new IllegalActionException(this, "File modelDescription.xml is missing from the fmu archive.");
            }
            // Read the modelDescription.xml file.
            Document modelDescription = _parseXMLFile(modelDescriptionFile);
            
            // Create parameters and ports.
            _traverseDOM(modelDescription);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to unzip \"" + fmuFileName + "\".");
        }
        System.out.println("FMUImport: created " + files.size() + " files.");
    }
    
    private Document _parseXMLFile(File xmlFile) throws IllegalActionException {
        System.out.println("FMUImport: parsing " + xmlFile);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // Using factory get an instance of document builder.
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse using builder to get DOM representation of the XML file.
            return db.parse(xmlFile.getCanonicalPath());
        }catch(Exception pce) {
            throw new IllegalActionException(this, pce, "Failed to parse " + xmlFile);
        }
    }
    
    /**
     * Echo common attributes of a DOM2 Node and terminate output with an
     * EOL character.
     */
    private void printlnCommon(Node n) {
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
     * Indent to the current level in multiples of basicIndent
     */
    private void outputIndentation() {
        for (int i = 0; i < indent; i++) {
            System.out.print(basicIndent);
        }
    }

    private void _createPortsAndParameters(Node domNode) throws IllegalActionException, NameDuplicationException {
        int type = domNode.getNodeType();
        if (type == Node.ELEMENT_NODE) {
            String name = "";
            String value = "";
            boolean foundParameter = false;
            NamedNodeMap attributes = domNode.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                if (attribute.getNodeType() == Node.ATTRIBUTE_NODE){
                    if (attribute.getNodeName().equals("name")) {
                        name = attribute.getNodeValue();
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
                        if (subattribute.getNodeName().equals("start")) {
                            value = subattribute.getNodeValue();
                        }
                    }
                }
            }
            if (foundParameter) {
                Parameter parameter = new Parameter(this, name);
                parameter.setExpression(value);
                // Prevent exporting this to MoML unless it has
                // been overridden.
                parameter.setDerivedLevel(1);
            }
        }
    }

    private void _traverseDOM(Node domNode) throws IllegalActionException, NameDuplicationException {
        // Indent to the current level before printing anything
        outputIndentation();

        int type = domNode.getNodeType();
        switch (type) {
        case Node.ATTRIBUTE_NODE:
            System.out.print("ATTR:");
            printlnCommon(domNode);
            break;
        case Node.CDATA_SECTION_NODE:
            System.out.print("CDATA:");
            printlnCommon(domNode);
            break;
        case Node.COMMENT_NODE:
            System.out.print("COMM:");
            printlnCommon(domNode);
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            System.out.print("DOC_FRAG:");
            printlnCommon(domNode);
            break;
        case Node.DOCUMENT_NODE:
            System.out.print("DOC:");
            printlnCommon(domNode);
            break;
        case Node.DOCUMENT_TYPE_NODE:
            System.out.print("DOC_TYPE:");
            printlnCommon(domNode);

            // Print entities if any
            NamedNodeMap nodeMap = ((DocumentType)domNode).getEntities();
            indent += 2;
            for (int i = 0; i < nodeMap.getLength(); i++) {
                Entity entity = (Entity)nodeMap.item(i);
                _traverseDOM(entity);
            }
            indent -= 2;
            break;
        case Node.ELEMENT_NODE:
            System.out.print("ELEM:");
            printlnCommon(domNode);

            // Print attributes if any.  Note: element attributes are not
            // children of ELEMENT_NODEs but are properties of their
            // associated ELEMENT_NODE.  For this reason, they are printed
            // with 2x the indent level to indicate this.
            NamedNodeMap atts = domNode.getAttributes();
            indent += 2;
            for (int i = 0; i < atts.getLength(); i++) {
                Node att = atts.item(i);
                _traverseDOM(att);
            }
            indent -= 2;
            
            break;
        case Node.ENTITY_NODE:
            System.out.print("ENT:");
            printlnCommon(domNode);
            break;
        case Node.ENTITY_REFERENCE_NODE:
            System.out.print("ENT_REF:");
            printlnCommon(domNode);
            break;
        case Node.NOTATION_NODE:
            System.out.print("NOTATION:");
            printlnCommon(domNode);
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            System.out.print("PROC_INST:");
            printlnCommon(domNode);
            break;
        case Node.TEXT_NODE:
            System.out.print("TEXT:");
            printlnCommon(domNode);
            break;
        default:
            System.out.print("UNSUPPORTED NODE: " + type);
            printlnCommon(domNode);
            break;
        }

        // Print children if any
        indent++;
        for (Node child = domNode.getFirstChild(); child != null;
             child = child.getNextSibling()) {
            _traverseDOM(child);
            if (child.getNodeName().equals("ScalarVariable")) {
                _createPortsAndParameters(child);
            }
        }
        indent--;
    }
    
    /** Indent level */
    private int indent = 0;

    /** Indentation will be in multiples of basicIndent  */
    private final String basicIndent = "  ";

}

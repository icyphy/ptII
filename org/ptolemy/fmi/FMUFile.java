/* Parse a Functional Mock-up Unit (FMU) file.

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
package org.ptolemy.fmi;

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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


///////////////////////////////////////////////////////////////////
//// FMUFile

/**
 * Parse a Functional Mock-up Interface (FMI) 1.0 Model Exchange 
 * Functional Mock-up Unit (FMU) file.
 * 
 * 
 * <p>FMI documentation may be found at
 * <a href="http://www.modelisar.com/fmi.html">http://www.modelisar.com/fmi.html</a>.
 * </p>
 * 
 * @author Christopher Brooks
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUFile {
    /** Return the name of the shared library from a .fmu file.
     *  @param fmiModelDescription The representation of the model that was read
     *  in by {#parseFMUFile}.
     *  @return The canonical path of the shared library.
     *  @exception IOException If thrown while determining the canonical path of the library.
     */
    public static String fmuSharedLibrary(FMIModelDescription fmiModelDescription) throws IOException {

        // Find the modelDescription.xml file.
        File modelDescriptionFile = null;
        for (File file : fmiModelDescription.files) {
            if (file.getName().endsWith("modelDescription.xml")) {
                modelDescriptionFile = file;
                break;
            }
        }

        if (modelDescriptionFile == null) {
            throw new IOException("The .fmu file does not contain a modelDescription.xml file.");
        }

        // Determine the path to the shared object.
        String topDirectory = modelDescriptionFile.getParent();
        String osName = System.getProperty("os.name").toLowerCase();
        String extension = ".so";
        if (osName.startsWith("mac")) {
            // JModelica seems to use darwin as the binary name
            osName = "darwin";
            // FIXME: OpenModelica uses something different.
            extension = ".dylib";
        } else if (osName.startsWith("Windows")) {
            extension = ".dll";
        }
        String bitWidth = "64";
        if (FMUFile._is32Bit()) {
            bitWidth = "32";
        }
        String library =  topDirectory + File.separator
            + "binaries" + File.separator
            + osName + bitWidth + File.separator
            + fmiModelDescription.modelIdentifier + extension;
        String canonicalPath = new File(library).getCanonicalPath();
        return canonicalPath;
    }

    /** Read in a .fmu file and parse the modelDescription.xml file.
     *  @param fmuFileName the .fmu file
     *  @return An object that represents the structure of the
     *  modelDescriptionFile.xml file.
     *  @exception IOException If the file cannot be unzipped or the
     *  modelDescription.xml file contained by the fmuFileName zip
     *  file cannot be parsed.
     */
    public static FMIModelDescription parseFMUFile(String fmuFileName)
            throws IOException {

        // Unzip the file.
        List<File> files = null;
        try {
            files = _unzip(fmuFileName);
        } catch (IOException ex) {
            throw new IOException("Failed to unzip \""
                    + fmuFileName + "\".", ex);
        }

        // Find the modelDescription.xml file.
        File modelDescriptionFile = null;
        for (File file : files) {
            if (file.getName().endsWith("modelDescription.xml")) {
                modelDescriptionFile = file;
                break;
            }
        }
        if (modelDescriptionFile == null) {
            throw new IOException("File \"modelDescription.xml\" is missing "
                    + "from the fmu archive \""
                    + fmuFileName + "\"/");
        }

        // Read the modelDescription.xml file.
        Document document = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Using factory get an instance of document builder.
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse using builder to get DOM representation of the XML file.
            document = db.parse(modelDescriptionFile.getCanonicalPath());
        } catch(Exception ex) {
            throw new IOException("Failed to parse \"" + modelDescriptionFile + "\".", ex);
        }

        Element root = document.getDocumentElement();

        // Create an object that represents the modelDescription.xml file
        FMIModelDescription fmiModelDescription = new FMIModelDescription();

        // Save the list of files that were extracted for later use.
        fmiModelDescription.files = files;

        // Handle the root attributes
        if (root.hasAttribute("fmiVersion")) {
            fmiModelDescription.fmiVersion = root.getAttribute("fmiVersion");
        }
        if (root.hasAttribute("modelIdentifier")) {
            fmiModelDescription.modelIdentifier = root.getAttribute("modelIdentifier");
        }
        if (root.hasAttribute("modelName")) {
            fmiModelDescription.modelName = root.getAttribute("modelName");
        }
        if (root.hasAttribute("guid")) {
            fmiModelDescription.guid = root.getAttribute("guid");
        }
        // FIXME: Handle numberOfContinuousStates, numberOfEventIndicators etc.
            
        // FIXME: handle typeDefinitions

        // FIXME: handle DefaultExperiment
           
        // FIXME: handle Vendor annotations

        // ModelVariables
        // NodeList is not a list, it only has getLength() and item(). #fail.
        NodeList scalarVariables = document.getElementsByTagName("ScalarVariable");

        for (int i = 0; i < scalarVariables.getLength(); i++) {
            Element element = (Element) scalarVariables.item(i);
            fmiModelDescription.modelVariables.add(new FMIScalarVariable(element));
        }

        return fmiModelDescription;
    }

    /** Return true if this is a 32bit JVM.
     *  @return true if this is a 32bit JVM.
     */
    private static boolean _is32Bit() {
        String dataModelProperty = System.getProperty("sun.arch.data.model");
        // FIXME: it is difficult to detect if we are under a
        // 64bit JVM.  See
        // http://forums.sun.com/thread.jspa?threadID=5306174
        if (dataModelProperty.indexOf("64") != -1) {
            return false;
        } else {
            String javaVmNameProperty = System.getProperty("java.vm.name");
            if (javaVmNameProperty.indexOf("64") != -1) {
                return false;
            }
        }
        return true;
    }

    /** Unzip a file into a temporary directory.
     *  Based on http://java.sun.com/developer/technicalArticles/Programming/compression/.
     *  @param zipFileName  The file to be unzipped.
     *  @return the list of files that were extracted.
     *  @exception IOException if the file cannot be opened, if there are problems reading
     *  the zip file or if there are problems creating the files or directories.
     */
    private static List<File> _unzip(String zipFileName) throws IOException {
        // FIXME: Use URLs, not files so that we can work from JarZip files.
        BufferedOutputStream destination = null;
        FileInputStream fileInputStream = new FileInputStream(zipFileName);
        ZipInputStream zipInputStream =
            new ZipInputStream(new BufferedInputStream(fileInputStream));
        ZipEntry entry;
        final int BUFFER = 2048;
        byte data[] = new byte[BUFFER];

        // Unzip in a temporary directory.
        File topDirectoryFile = File.createTempFile("FMUFile", ".tmp");
        if (!topDirectoryFile.delete()) {
            throw new IOException("Could not delete temporary file "
                    + topDirectoryFile);
        }
        if (!topDirectoryFile.mkdir()) {
            throw new IOException("Could not create directory "
                    + topDirectoryFile);
        }
        topDirectoryFile.deleteOnExit();
        String topDirectory = topDirectoryFile.getCanonicalPath();
        System.out.println("Extracting to " + topDirectory);
        List<File> files = new LinkedList<File>();
        try {
            while((entry = zipInputStream.getNextEntry()) != null) {
                //System.out.println("Extracting: " + entry);
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
}

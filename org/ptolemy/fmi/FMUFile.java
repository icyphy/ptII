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
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jna.NativeLibrary;

///////////////////////////////////////////////////////////////////
//// FMUFile

/**
 * Parse a Functional Mock-up Interface (FMI) 1.0 Functional Mock-up
 * Unit (FMU) file and create a FMIModelDescription for later use.
 *
 * <p>The parseFMUFile() method in this class is the primary entry
 * point into this package.</p>
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
    public static String fmuSharedLibrary(
            FMIModelDescription fmiModelDescription) throws IOException {

        // Find the modelDescription.xml file.
        File modelDescriptionFile = null;
        for (File file : fmiModelDescription.files) {
            if (file.getName().endsWith("modelDescription.xml")) {
                modelDescriptionFile = file;
                break;
            }
        }

        if (modelDescriptionFile == null) {
            throw new IOException(
                    "The .fmu file does not contain a modelDescription.xml file.");
        }

        // Determine the path to the shared object.
        String topDirectory = modelDescriptionFile.getParent();
        String osName = System.getProperty("os.name").toLowerCase(
                Locale.getDefault());
        String extension = ".so";
        if (osName.startsWith("mac")) {
            // JModelica seems to use darwin as the binary name
            osName = "darwin";
            extension = ".dylib";
        } else if (osName.startsWith("windows")) {
            osName = "win";
            extension = ".dll";
        }
        String bitWidth = "64";
        if (FMUFile._is32Bit()) {
            bitWidth = "32";
        }
        String library = topDirectory + File.separator + "binaries"
                + File.separator + osName + bitWidth + File.separator
                + fmiModelDescription.modelIdentifier + extension;
        File canonicalFile = new File(library).getCanonicalFile();
        if (!canonicalFile.exists()) {
            if (osName.startsWith("mac") || osName.startsWith("darwin")) {
                // OpenModelica 1.8.1 uses darwin-x86_64
                osName = "darwin-x86_";
                extension = ".so";
                library = topDirectory + File.separator + "binaries"
                        + File.separator + osName + bitWidth + File.separator
                        + fmiModelDescription.modelIdentifier + extension;
                canonicalFile = new File(library).getCanonicalFile();
            }
        }
        String canonicalPath = canonicalFile.getCanonicalPath();

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
        return FMUFile.parseFMUFile(fmuFileName, false);
    }

    /** Read in a .fmu file and parse the modelDescription.xml file,
     *  optionally ignore problems loading the shared library.
     *  @param fmuFileName the .fmu file
     *  @param ignoreSharedLibraryErrors True if errors that occur
     *  during the loading of the shared library should be ignored.
     *  This is useful for loading FMUs that do not have shared
     *  libraries for the current platform.
     *  @return An object that represents the structure of the
     *  modelDescriptionFile.xml file.
     *  @exception IOException If the file cannot be unzipped or the
     *  modelDescription.xml file contained by the fmuFileName zip
     *  file cannot be parsed.
     */
    public static FMIModelDescription parseFMUFile(String fmuFileName,
            boolean ignoreSharedLibraryErrors) throws IOException {

        // Unzip the file.
        List<File> files = null;
        try {
            files = _unzip(fmuFileName);
        } catch (IOException ex) {
            throw new IOException("Failed to unzip \"" + fmuFileName + "\".",
                    ex);
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
                    + "from the fmu archive \"" + fmuFileName + "\"/");
        }

        // Read the modelDescription.xml file.
        Document document = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Using factory get an instance of document builder.
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse using builder to get DOM representation of the XML file.
            document = db.parse(modelDescriptionFile.getCanonicalPath());
        } catch (Throwable throwable) {
            throw new IOException("Failed to parse \"" + modelDescriptionFile
                    + "\".", throwable);
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
            fmiModelDescription.modelIdentifier = root
                    .getAttribute("modelIdentifier");
        }
        if (root.hasAttribute("modelName")) {
            fmiModelDescription.modelName = root.getAttribute("modelName");
        }
        if (root.hasAttribute("guid")) {
            fmiModelDescription.guid = root.getAttribute("guid");
        }
        if (root.hasAttribute("numberOfContinuousStates")) {
            fmiModelDescription.numberOfContinuousStates = Integer.valueOf(
                    root.getAttribute("numberOfContinuousStates")).intValue();
        }
        if (root.hasAttribute("numberOfEventIndicators")) {
            fmiModelDescription.numberOfEventIndicators = Integer.valueOf(
                    root.getAttribute("numberOfEventIndicators")).intValue();
        }

        // TypeDefinitions
        // NodeList is not a list, it only has getLength() and item(). #fail.
        NodeList types = document.getElementsByTagName("Type");
        int length = types.getLength();
        for (int i = 0; i < length; i++) {
            Element element = (Element) types.item(i);
            String elementTypeName = element.getAttribute("name");
            NodeList children = element.getChildNodes(); // NodeList. Worst.
                                                         // Ever.
            for (int j = 0; j < children.getLength(); j++) {
                Node child = element.getChildNodes().item(j);
                if (child instanceof Element) {
                    Element childElement = (Element) child;
                    String childTypeName = childElement.getNodeName();
                    fmiModelDescription.typeDefinitions.put(elementTypeName,
                            childTypeName);
                }
            }
        }

        // FIXME: handle DefaultExperiment

        // FIXME: handle Vendor annotations

        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
        // Load the shared library
        try {
            fmiModelDescription.nativeLibrary = NativeLibrary
                    .getInstance(sharedLibrary);
        } catch (Throwable throwable) {
            List<String> binariesFiles = new LinkedList<String>();
            for (File file : fmiModelDescription.files) {
                if (file.toString().indexOf("binaries") != -1) {
                    binariesFiles.add(file.toString() + "\n");
                }
            }
            String message = "Failed to load the \"" + sharedLibrary
                    + "\" shared library, which was created "
                    + "by unzipping \"" + fmuFileName
                    + "\". Usually, this is because the .fmu file does "
                    + "not contain a shared library for the current "
                    + "architecture.  The fmu file contained the "
                    + "following files with 'binaries' in the path:\n"
                    + binariesFiles;
            System.out.println(message + "\n Original error:\n " + throwable);
            if (!ignoreSharedLibraryErrors) {
                // Note that Variable.propagate() will handle this error and
                // hide it.
                throw new IOException(message, throwable);
            }
        }

        // ModelVariables
        // NodeList is not a list, it only has getLength() and item(). #fail.
        NodeList scalarVariables = document
                .getElementsByTagName("ScalarVariable");

        for (int i = 0; i < scalarVariables.getLength(); i++) {
            Element element = (Element) scalarVariables.item(i);
            fmiModelDescription.modelVariables.add(new FMIScalarVariable(
                    fmiModelDescription, element));
        }

        // Implementation
        // NodeList is not a list, it only has getLength() and item(). #fail.
        NodeList implementation = document
                .getElementsByTagName("CoSimulation_StandAlone");
        if (implementation.getLength() > 1) {
            System.out.println("Warning, CoSimulation_StandAlone can "
                    + "only have one element, a Capability");
        }
        for (int i = 0; i < implementation.getLength(); i++) {
            Element element = (Element) implementation.item(i);
            NodeList capabilities = element
                    .getElementsByTagName("Capabilities");
            for (int j = 0; j < capabilities.getLength(); j++) {
                Element capabilitiesElement = (Element) capabilities.item(j);
                fmiModelDescription.capabilities = new FMICoSimulationCapabilities(
                        capabilitiesElement);
            }
        }

        // FIXME: handle CoSimulation_Tool

        return fmiModelDescription;
    }

    /** Return true if this is a 32bit JVM.
     *  @return true if this is a 32bit JVM.
     */
    private static boolean _is32Bit() {
        String dataModelProperty = System.getProperty("sun.arch.data.model");
        // FIXME: it is difficult to detect if we are under a
        // 64bit JVM. See
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
        System.out.println("FMUFile: Extracting to " + topDirectory);
        List<File> files = new LinkedList<File>();
        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream = null;
        try {
            fileInputStream = new FileInputStream(zipFileName);
            zipInputStream = new ZipInputStream(new BufferedInputStream(
                    fileInputStream));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // System.out.println("Extracting: " + entry);
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
                    while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
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

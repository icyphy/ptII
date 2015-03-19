/* Parse a Functional Mock-up Unit (FMU) file.

   Copyright (c) 2011-2014 The Regents of the University of California.
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * @author Christopher Brooks, Thierry S. Nouidui
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUFile {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
                if (file.getParent().endsWith("sources")) {
                    System.err.println("Warning, while looking for the shared library, \""
                            + file + "\" was found in a sources/ directory.  "
                            + "This is unusual, typically modelDescription.xml "
                            + "is in the directory above sources/.");
                }
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
                        + File.separator + osName + bitWidth
                        + File.separator
                        + fmiModelDescription.modelIdentifier + extension;
                File canonicalFile2 = new File(library).getCanonicalFile();
                if (canonicalFile2.exists()) {
                    System.out
                            .println("Could not find "
                                    + canonicalFile
                                    + " but "
                                    + canonicalFile2
                                    + "exists.  "
                                    + "This is probably OpenModelica 1.8.1, which uses dwarwin-x86_64");
                    canonicalFile = canonicalFile2;
                } else {
                    // OpenModelica 1.9.2 uses x86_64-apple-darwin13.4.0
                    // FIXME: Invoke uname -r to get the 13.4.0.  To do this, use ProcessBuilder.  See FMUBuild.java
                    osName = "x86_";
                    extension = ".dylib";
                    library = topDirectory + File.separator + "binaries"
                        + File.separator + osName + bitWidth
                        + "-apple-darwin13.4.0"
                        + File.separator
                        + fmiModelDescription.modelIdentifier + extension;
                    File canonicalFile3 = new File(library).getCanonicalFile();
                    if (canonicalFile3.exists()) {
                        System.out
                            .println("Could not find "
                                    + canonicalFile
                                    + " or "
                                    + canonicalFile2
                                    + " but "
                                    + canonicalFile3
                                    + "exists.  "
                                    + "This is probably OpenModelica 1.9.2, which uses x86_64-apple-darwin13.4.0");
                        canonicalFile = canonicalFile3;
                    } else {
                        File binariesDirectory = new File(topDirectory + File.separator + "binaries");
                        if (binariesDirectory.isDirectory()) {
                            library = "";
                            File[] files = binariesDirectory.listFiles();
                            for (File file : files) {
                                if (file.isDirectory()) {
                                    if (file.getName().matches(".*" + bitWidth + "-darwin.*")) {
                                        library = file.getCanonicalFile()
                                            + File.separator
                                            + fmiModelDescription.modelIdentifier + extension;
                                      break;
                                    }
                                }
                            }
                            if (library.length() > 0) {
                                File canonicalFile4 = new File(library).getCanonicalFile();
                                if (canonicalFile4.exists()) {
                                    System.out
                                        .println("Could not find "
                                                + canonicalFile
                                                + " or "
                                                + canonicalFile2
                                                + " or "
                                                + canonicalFile3
                                                + " but "
                                                + canonicalFile4
                                                + "exists.  "
                                                + "This is probably OpenModelica after 1.9.2, which uses x86_64-darwin13.4.0");
                                    canonicalFile = canonicalFile4;
                                } else {
                                    System.out.println(canonicalFile + " does not exist"
                                            + " also tried " + canonicalFile2
                                            + " for OpenModelica 1.8.1"
                                            + " and " + canonicalFile3
                                            + " for OpenModelica 1.9.2."
                                            + " and " + canonicalFile4 + ".");
                                }
                            } else {
                                System.out
                                    .println("Could not find "
                                            + canonicalFile
                                            + " or "
                                            + canonicalFile2
                                            + " or "
                                            + canonicalFile3 + ".");
                            }
                        }
                    }
                }
            }
        }
        String canonicalPath = canonicalFile.getCanonicalPath();

        return canonicalPath;
    }

    /** Read in a .fmu file and parse the modelDescription.xml file.
     *  If the same file has been previously read, then return the
     *  FMIModelDescription from that previous reading.
     *  Note that this does not load the shared library.
     *  That is loaded upon the first attempt to use the procedures in it.
     *  This is important because we want to be able to view
     *  a model that references an FMU even if the FMU does not
     *  support the current platform.
     *  @param fmuFileName the .fmu file
     *  @return An object that represents the structure of the
     *  modelDescriptionFile.xml file.
     *  @exception IOException If the file cannot be unzipped or the
     *  modelDescription.xml file contained by the fmuFileName zip
     *  file cannot be parsed.
     */
    public static FMIModelDescription parseFMUFile(String fmuFileName)
            throws IOException {

        // FIXME: JModelica FMUs can have both CoSimulation and
        // ModelExchange NodeLists, see CoupledClutches.xml.  
        // The caching is indexed on the file name, which means
        // that we cannot parse both a CS fmu and a ME fmu with
        // the same file name.
        FMIModelDescription result = _modelDescriptions.get(fmuFileName);
        if (result != null) {
            return result;
        }

        // Unzip the file.
        List<File> files = null;
        try {
            files = unzip(fmuFileName);
        } catch (IOException ex) {
            // Java 1.5 does not support IOException(String, Throwable).
            // We sometimes compile this with gcj, which is Java 1.5
            IOException exception = new IOException("Failed to unzip \""
                    + fmuFileName + "\".");
            exception.initCause(ex);
            throw exception;
        }

        // Find the modelDescription.xml file.
        File modelDescriptionFile = null;
        String fmuResourceLocation = null;
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith("modelDescription.xml")) {
                modelDescriptionFile = file;
                if (fmuResourceLocation != null) {
                    break;
                }
            }
            if (fileName.endsWith("resources")
                    || fileName.endsWith("resources/")) {
                fmuResourceLocation = file.toURI().toURL().toString();
                if (modelDescriptionFile != null) {
                    break;
                }
            }
        }
        if (modelDescriptionFile == null) {
            throw new IOException("File \"modelDescription.xml\" is missing "
                    + "from the fmu archive \"" + fmuFileName + "\"");
        }
        if (fmuResourceLocation == null) {
            File fmuResourceFile = new File(modelDescriptionFile.getParent(),
                    "resources");
            fmuResourceLocation = fmuResourceFile.toURI().toURL().toString();
            if (!fmuResourceFile.isDirectory()) {
                if (fmuResourceFile.exists()) {
                    if (fmuResourceFile.delete()) {
                        throw new IOException(
                                "Could not delete file \""
                                        + fmuResourceFile
                                        + "\" before creating a directory with the same name.");
                    }
                }
                if (!fmuResourceFile.mkdirs()) {
                    throw new IOException("Could not create directory \""
                            + fmuResourceFile + "\"");
                }
            }
        }

        // Remove any trailing slash.
        if (fmuResourceLocation.endsWith("/")) {
            fmuResourceLocation = fmuResourceLocation.substring(0,
                    fmuResourceLocation.length() - 1);
        }

        if (fmuResourceLocation.indexOf("%20") != -1) {
            System.out
                    .println("FMUFile: The fmuResourceLocation \""
                            + fmuResourceLocation
                            + "\" contains one or more \"%20\"."
                            + " Certain tools have problems with this, so we are converting \"%20\" to space \" \".");
            fmuResourceLocation = fmuResourceLocation.replace("%20", " ");
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
            // Java 1.5 does not support IOException(String, Throwable).
            // We sometimes compile this with gcj, which is Java 1.5
            IOException exception = new IOException("Failed to parse \""
                    + modelDescriptionFile + "\".");
            exception.initCause(throwable);
            throw exception;
        }

        Element root = document.getDocumentElement();

        // Create an object that represents the modelDescription.xml file
        FMIModelDescription fmiModelDescription = new FMIModelDescription();

        // Record this model description in case there is another instance of this FMU.
        _modelDescriptions.put(fmuFileName, fmiModelDescription);

        // Save the list of files that were extracted for later use.
        fmiModelDescription.files = files;

        // Location of the resources/ directory in the zip file;
        fmiModelDescription.fmuResourceLocation = fmuResourceLocation;

        double fmiVersion = 0.0;
        // Handle the root attributes
        if (root.hasAttribute("fmiVersion")) {
            fmiModelDescription.fmiVersion = root.getAttribute("fmiVersion");
            try {
                fmiVersion = Double.parseDouble(fmiModelDescription.fmiVersion);
            } catch (NumberFormatException ex) {
                IOException exception = new IOException("Invalid fmiVersion \""
                        + fmiModelDescription.fmiVersion
                        + "\". Required to be of the form n.m, "
                        + "where n and m are natural numbers.");
                exception.initCause(ex);
                throw exception;
            }

            // Under FMI 1.0, the fmuLocation parameter refers to the location
            // of the fmu.

            // fmiVersion 1.5 is not a legitimate version of the FMI standard, it
            // was used by the Ptolemy project for experimenting with FMI 2.0beta.
            if (fmiVersion < 1.5
                    && fmiModelDescription.fmuResourceLocation
                            .endsWith("resources")) {
                fmiModelDescription.fmuResourceLocation = fmiModelDescription.fmuResourceLocation
                        .substring(
                                0,
                                fmiModelDescription.fmuResourceLocation
                                        .length() - "resources".length() - 1); // +1 is to get rid of the /
            }
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
            fmiModelDescription.numberOfContinuousStates = Integer
                    .parseInt(root.getAttribute("numberOfContinuousStates"));
        }
        if (root.hasAttribute("numberOfEventIndicators")) {
            fmiModelDescription.numberOfEventIndicators = Integer.parseInt(root
                    .getAttribute("numberOfEventIndicators"));
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

        if (fmiVersion < 1.5) {
            // Implementation description in FMI 1.0
            // NodeList is not a list, it only has getLength() and item(). #fail.
            NodeList implementation = document
                    .getElementsByTagName("CoSimulation_StandAlone");
            if (implementation.getLength() > 1) {
                System.out.println("Warning: FMU has more than one element "
                        + "CoSimulation_StandAlone");
            }
            for (int i = 0; i < implementation.getLength(); i++) {
                Element element = (Element) implementation.item(i);
                NodeList capabilities = element
                        .getElementsByTagName("Capabilities");
                for (int j = 0; j < capabilities.getLength(); j++) {
                    Element capabilitiesElement = (Element) capabilities
                            .item(j);
                    fmiModelDescription.cosimulationCapabilities = new FMICoSimulationCapabilities(
                            capabilitiesElement);
                }
            }
            // FIXME: handle CoSimulation_Tool
        } else {
            // Implementation description in FMI 2.0.

            // JModelica FMUs can have both CoSimulation and
            // ModelExchange NodeLists, see CoupledClutches.xml

            // Handle CoSimulation.
            NodeList implementation = document
                    .getElementsByTagName("CoSimulation");
            if (implementation.getLength() > 1) {
                System.out
                        .println("Warning: FMU modelDescription provides more than one CoSimulation element");
            }
            if (implementation.getLength() == 1) {
                Element cosimulation = (Element) implementation.item(0);
                fmiModelDescription.cosimulationCapabilities = new FMI20CoSimulationCapabilities(
                        cosimulation);

                // In FMI 2.0, the modelIdentifier is given in the
                // ModelExchange or Cosimulation element, not in the
                // root element (presumably so that CoSimulation and
                // ModelExchange can use non-conflicting names and
                // hence divergent C implementations.
                if (cosimulation.hasAttribute("modelIdentifier")) {
                    fmiModelDescription.modelIdentifier = cosimulation
                            .getAttribute("modelIdentifier");
                } else {
                    System.out
                            .println("Warning: FMU CoSimulation element is missing a modelIdentifier.");
                }

                // FIXME: We should use the
                // FMICoSimulationCapabilities class and preserve the
                // Object Oriented nature of the modelDescription.xml
                // file.  Adding toplevel fields fmiModelDescription
                // means that we have fields that are present, but not
                // useful.

                if (cosimulation.hasAttribute("canGetAndSetFMUstate")) {
                    fmiModelDescription.canGetAndSetFMUstate = Boolean
                            .parseBoolean(cosimulation
                                    .getAttribute("canGetAndSetFMUstate"));
                }

                // canProvideMaxStepSize and fmiGetMaxStepSize() are IBM/UCB extensions to FMI 2.0.
                if (cosimulation.hasAttribute("canProvideMaxStepSize")) {
                    fmiModelDescription.canProvideMaxStepSize = Boolean
                            .parseBoolean(cosimulation
                                    .getAttribute("canProvideMaxStepSize"));
                }
                
                // handleIntegerTime, extension for FMI-HCS
                // FIXME: (FABIO) These informations are temporarily here, but a new HibridCoSimulation mode
                // must be created
                if (cosimulation.hasAttribute("handleIntegerTime")) {
                    fmiModelDescription.handleIntegerTime = Boolean
                            .parseBoolean(cosimulation
                                    .getAttribute("handleIntegerTime"));
                }
                
                // precision, extension for FMI-HCS
                if (fmiModelDescription.handleIntegerTime == true) {
                	if (cosimulation.hasAttribute("precision")) {
                        fmiModelDescription.precision = Integer
                                .parseInt(cosimulation
                                        .getAttribute("precision"));
                    } else {
                    	System.out
                        .println("Warning: FMU modelDescription provides Integer representation of time, but precision is not specified");
                    }
                }
                
            }

            // Handle ModelExchange.
            implementation = document.getElementsByTagName("ModelExchange");

            // If the field has a ModelExchange tag, then set the modelExchange flag.
            // This is needed by the FMI-2.0 Model Exchange tests in org/ptolemy/fmi.
            if (implementation.getLength() > 0) {
                fmiModelDescription.modelExchange = true;
            }

            if (implementation.getLength() > 1) {
                System.out
                        .println("Warning: FMU modelDescription provides more than one ModelExchange element");
            }
            if (implementation.getLength() == 1) {
                Element modelExchange = (Element) implementation.item(0);
                fmiModelDescription.modelExchangeCapabilities = new FMI20ModelExchangeCapabilities(
                        modelExchange);

                // In FMI 2.0, the modelIdentifier is given in the
                // ModelExchange or Cosimulation element, not in the
                // root element (presumably so that CoSimulation and
                // ModelExchange can use non-conflicting names and
                // hence divergent C implementations.
                if (modelExchange.hasAttribute("modelIdentifier")) {
                    fmiModelDescription.modelIdentifier = modelExchange
                            .getAttribute("modelIdentifier");
                } else {
                    System.out
                            .println("Warning: FMU CoSimulation element is missing a modelIdentifier.");
                }
                // Get the providesDirectionalDerivative attribute if present.
                // FIXME: Dymola has a typo and is using providesDirectionalDerivatives
                // rather than providesDirectionalDerivative.
                if (modelExchange
                        .hasAttribute("providesDirectionalDerivatives")) {
                    fmiModelDescription.providesDirectionalDerivative = Boolean
                            .parseBoolean(modelExchange
                                    .getAttribute("providesDirectionalDerivatives"));
                }
                // FIXME: This should be removed once fix in tools like Dymola 2015.
                // providesDirectionalDerivative is the name specified in the standard.
                // Some tools such as Dymola 2015 have typos which is the reason why we search 
                // for both providesDirectionalDerivatives and providesDirectionalDerivatives.
                if (modelExchange.hasAttribute("providesDirectionalDerivative")) {
                    fmiModelDescription.providesDirectionalDerivative = Boolean
                            .parseBoolean(modelExchange
                                    .getAttribute("providesDirectionalDerivative"));
                }
            }
        }

        // This has to be done after the native libraries have been loaded.
        // FIXME: The above comment contradicts the method comment that this does not load libraries.
        // NodeList is not a list, it only has getLength() and item(). #fail.
        NodeList scalarVariables = document
                .getElementsByTagName("ScalarVariable");

        for (int i = 0; i < scalarVariables.getLength(); i++) {
            Element element = (Element) scalarVariables.item(i);
            fmiModelDescription.modelVariables.add(new FMIScalarVariable(
                    fmiModelDescription, element));
        }

        /*
        for (int j = 0; j < capabilities.getLength(); j++) {
            Element capabilitiesElement = (Element) capabilities
                    .item(j);
            fmiModelDescription.cosimulationCapabilities = new FMICoSimulationCapabilities(
                    capabilitiesElement);
        }*/

        if (fmiVersion >= 2.0) {
            // By default each output has direct dependency from all input ports
            fmiModelDescription.addDefaultInputDependencies();

            // This section might be used to retrieve the information of the
            // directDependency between inputs and outputs
            // NodeList is not a list, it only has getLength() and item(). #fail.
            NodeList structure = document
                    .getElementsByTagName("ModelStructure");
            if (structure.getLength() == 1) {
                NodeList listOffOutputs = document
                        .getElementsByTagName("Outputs");
                Node current = null;

                for (int i = 0; i < listOffOutputs.getLength(); i++) {
                    NodeList unknowVariables = listOffOutputs.item(i)
                            .getChildNodes();
                    for (int j = 0; j < unknowVariables.getLength(); j++) {
                        current = unknowVariables.item(j);
                        if (current.getNodeName().equalsIgnoreCase("Unknown")) {
                            fmiModelDescription.parseDependenciese(current);
                        }
                    }
                }
            }
        }

        if (fmiVersion > 1.5) {
            NodeList structure = document
                    .getElementsByTagName("ModelStructure");
            if (structure.getLength() == 1) {
                // Build a list of output variables
                NodeList listOffOutputs = document
                        .getElementsByTagName("Outputs");
                Node current = null;
                if (listOffOutputs.getLength() == 1) {
                    NodeList unknowVariables = listOffOutputs.item(0)
                            .getChildNodes();
                    for (int i = 0; i < unknowVariables.getLength(); i++) {
                        current = unknowVariables.item(i);
                        if (current.getNodeName().equalsIgnoreCase("Unknown")) {
                            fmiModelDescription.outputs.add(new FMI20Output(
                                    fmiModelDescription, current));
                        }
                    }
                }
                // Build  a list of state derivatives
                NodeList listOffDerivatives = document
                        .getElementsByTagName("Derivatives");
                current = null;
                if (listOffDerivatives.getLength() == 1) {
                    NodeList unknowVariables = listOffDerivatives.item(0)
                            .getChildNodes();
                    for (int i = 0; i < unknowVariables.getLength(); i++) {
                        current = unknowVariables.item(i);
                        if (current.getNodeName().equalsIgnoreCase("Unknown")) {
                            fmiModelDescription.continuousStateDerivatives
                            .add(new FMI20ContinuousStateDerivative(
                                    fmiModelDescription, current));
                        }
                    }
                }

            } else {
                new Exception("Warning: ModelStructure element is missing.")
                .printStackTrace();
            }

            fmiModelDescription.createStateVector();
        }

        return fmiModelDescription;
    }

    /** Unzip a file into a temporary directory.
     *  Based on http://java.sun.com/developer/technicalArticles/Programming/compression/.
     *  @param zipFileName  The file to be unzipped.
     *  @return the list of files that were extracted.
     *  @exception IOException if the file cannot be opened, if there are problems reading
     *  the zip file or if there are problems creating the files or directories.
     */
    public static List<File> unzip(String zipFileName) throws IOException {
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
        File destinationFile = null;
        try {
            fileInputStream = new FileInputStream(zipFileName);
            zipInputStream = new ZipInputStream(new BufferedInputStream(
                    fileInputStream));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // System.out.println("Extracting: " + entry);
                String entryName = entry.getName();
                destinationFile = new File(topDirectory, entryName);
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
                    try {
                        FileOutputStream fos = new FileOutputStream(
                                destinationFile);
                        destination = new BufferedOutputStream(fos, BUFFER);
                        int count;
                        while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
                            destination.write(data, 0, count);
                        }
                        files.add(destinationFile);
                    } finally {
                        if (destination != null) {
                            // Is the flush() really necessary?
                            destination.flush();
                            destination.close();
                            destination = null;
                        }
                    }
                }
            }
        } finally {
            if (destination != null) {
                try {
                    destination.close();
                } catch (IOException ex) {
                    System.out.println("FMUFile.unzip(): Failed to close \""
                            + destinationFile + "\"");
                }
            }
            if (zipInputStream != null) {
                zipInputStream.close();
            }
        }
        return files;
    }

    /** Return true if this is a 32bit JVM.
     *  @return true if this is a 32bit JVM.
     */
    public static boolean getIs32Bit() {
        return _is32Bit();
    }

    /** Return true if this is a 32bit JVM.
     *  @return true if this is a 32bit JVM.
     */
    private static boolean _is32Bit() {
        String dataModelProperty = System.getProperty("sun.arch.data.model");
        // FIXME: it is difficult to detect if we are under a
        // 64bit JVM. See
        // http://forums.sun.com/thread.jspa?threadID=5306174
        if (dataModelProperty == null || dataModelProperty.indexOf("64") != -1) {
            return false;
        } else {
            String javaVmNameProperty = System.getProperty("java.vm.name");
            if (javaVmNameProperty.indexOf("64") != -1) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Record of previously read files. */
    private static Map<String, FMIModelDescription> _modelDescriptions = new HashMap<String, FMIModelDescription>();
}

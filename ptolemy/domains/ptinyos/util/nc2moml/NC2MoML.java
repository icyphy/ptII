/*
 @Copyright (c) 2005-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.domains.ptinyos.util.nc2moml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ListIterator;

import net.tinyos.nesc.dump.NDReader;
import net.tinyos.nesc.dump.xml.Xinterface;
import net.tinyos.nesc.dump.xml.Xnesc;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 Generate a .moml file for each .nc file in the input list.

 Usage:
 <pre>
 java -classpath $PTII ptolemy.domains.ptinyos.util.nc2moml.NC2MoMl \
 &lt;<i>xml input prefix</i>&gt; \
 &lt;<i>xml input suffix</i>&gt; \
 &lt;<i>nc sub prefix</i>&gt; \
 &lt;<i>moml output prefix</i>&gt; \
 <i>long path to file containing list of .nc files using short path</i>
 </pre>

 Example:
 <pre>
 java -classpath $PTII ptolemy.domains.ptinyos.util.nc2moml.NC2MoMl \
 /home/celaine/ptII/vendors/ptinyos/moml \
 .ncxml \
 \'$CLASSPATH\' \
 /home/celaine/ptII/vendors/ptinyos/moml \
 /home/celaine/ptII/vendors/ptinyos/moml/.tempfile
 </pre>

 .tempfile contains:
 <pre>
 tos/lib/Counters/Counter.nc
 tos/lib/Counters/IntToLeds.nc
 tos/lib/Counters/IntToLedsM.nc
 tos/lib/Counters/IntToRfm.nc
 tos/lib/Counters/IntToRfmM.nc
 tos/lib/Counters/RfmToInt.nc
 tos/lib/Counters/RfmToIntM.nc
 tos/lib/Counters/SenseToInt.nc
 </pre>

 Example output for Counter.nc:
 <pre>
 &lt;?xml version="1.0"?&gt;
 &lt;!DOCTYPE plot PUBLIC "-//UC Berkeley//DTD MoML 1//EN" "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;

 &lt;class name="Counter" extends="ptolemy.domains.ptinyos.lib.NCComponent"&gt;
 &lt;property name="source" value="$CLASSPATH/tos/lib/Counters/Counter.nc" /&gt;
 &lt;port name="IntOutput" class="ptolemy.actor.IOPort"&gt;
 &lt;property name="output" /&gt;
 &lt;property name="_showName" class="ptolemy.kernel.util.SingletonAttribute" /&gt;
 &lt;/port&gt;
 &lt;port name="StdControl" class="ptolemy.actor.IOPort"&gt;
 &lt;property name="input" /&gt;
 &lt;property name="_showName" class="ptolemy.kernel.util.SingletonAttribute" /&gt;
 &lt;/port&gt;
 &lt;port name="Timer" class="ptolemy.actor.IOPort"&gt;
 &lt;property name="output" /&gt;
 &lt;property name="_showName" class="ptolemy.kernel.util.SingletonAttribute" /&gt;
 &lt;/port&gt;
 &lt;/class&gt;
 </pre>
 Expects &lt;<i>xml input prefix</i>&gt;
 to contain files with &lt;<i>xml input suffix</i>&gt; containing
 an xml dump of:
 <pre>
 interfaces(file(filename.nc))
 </pre>

 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class NC2MoML {
    /** Generate the .moml file for this nesC component.
     *
     *  @param sourcePath The path to the component source file (.nc).
     *  @param componentName The name of the component (no suffix).
     *  @param outputFile The file to generate.
     */
    public static void generateComponent(String sourcePath,
            String componentName, String outputFile) {
        // Set the name of this class to the name of this nesC component.
        Element root = new Element("class");
        root.setAttribute("name", componentName);
        root.setAttribute("extends", "ptolemy.domains.ptinyos.lib.NCComponent");

        // Set the path to the .nc source file.
        Element source = new Element("property");
        source.setAttribute("name", "source");
        source.setAttribute("value", sourcePath);
        root.addContent(source);

        // Set the doc type.
        DocType plot = new DocType("plot", "-//UC Berkeley//DTD MoML 1//EN",
                "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd");
        Document doc = new Document(root, plot);

        // Get the list of interfaces for this nesC component.
        ListIterator interfaces = Xnesc.interfaceList.listIterator();

        while (interfaces.hasNext()) {
            // Get the next interface for this nesC component.
            Xinterface intf = (Xinterface) interfaces.next();

            // Set the port name and class.
            Element port = new Element("port");
            port.setAttribute("name", intf.name);
            port.setAttribute("class", "ptolemy.actor.IOPort");

            // Set the port type.
            // Note: nesC provides == ptII input
            Element portType = new Element("property");
            String portTypeValue = (intf.provided) ? "input" : "output";
            portType.setAttribute("name", portTypeValue);
            port.addContent(portType);

            // If this is a nesC parameterized interface, then set this as a
            // multiport in ptII.
            if (intf.parameters != null) {
                Element multiport = new Element("property");
                multiport.setAttribute("name", "multiport");
                port.addContent(multiport);
            }

            // Set the port name to be shown in vergil.
            Element showName = new Element("property");
            showName.setAttribute("name", "_showName");
            showName.setAttribute("class",
                    "ptolemy.kernel.util.SingletonAttribute");
            port.addContent(showName);

            // Add the port.
            root.addContent(port);
        }

        // Output the moml code to a file.
        FileOutputStream out = null;
        try {
            // Open the file.
            if (outputFile != null) {
                out = new FileOutputStream(outputFile);
            }

            // Set up the serializer.
            XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
            Format format = serializer.getFormat();
            format.setOmitEncoding(true);
            format.setLineSeparator("\n");
            serializer.setFormat(format);

            // Write, flush, and close the file.
            if (out != null) {
                serializer.output(doc, out);
            } else {
                // If a file was not specified, write to stdout.
                serializer.output(doc, System.out);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    // No need to flush, this will happen in the close().
                    // If we flush, then FindBugs says that the close() might not be called.
                    //out.flush();
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /** Read in .nc xml files, generate .moml files.
     *  @param args A series of command line arguments, see the
     *  class comment for details.
     *  @exception IOException If there is a problem reading or
     *  writing a file.
     */
    public static void main(String[] args) throws IOException {
        // Check to make sure all necessary arguments have been passed
        // to program.
        if (args.length < 5) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.domains.ptinyos.util.nc2moml.NC2MoMl "
                    + "<xml input prefix> " + "<xml input suffix> "
                    + "<nc sub prefix> " + "<moml output prefix> "
                    + "[long path to file containing list of .nc files using "
                    + "short path]");
            return;
        }

        // Extract arguments into variables.
        int index = 0;
        String inputPrefix = args[index++].trim();
        String inputSuffix = args[index++].trim();
        String subPrefix = args[index++].trim();
        String outputPrefix = args[index++].trim();
        String inputFiles = args[index++].trim();

        BufferedReader in = null;

        try {
            // Open the file containing the list of .nc files.
            in = new BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(inputFiles), java.nio.charset.Charset.defaultCharset()));

            String inputFileName;

            // Read each line of the file.
            while ((inputFileName = in.readLine()) != null) {
                // Determine the nesC xml name (with path) of the file.
                String xmlSuffix = inputFileName.replaceFirst("\\.nc$",
                        inputSuffix);
                String xmlInputFile = inputPrefix + _FILESEPARATOR + xmlSuffix;

                // Determine the substituted path to the .nc file.
                String pathToNCFile = subPrefix + _FILESEPARATOR
                        + inputFileName;

                // Determine the component name.
                String[] subdirs = inputFileName.split(_FILESEPARATOR);
                String componentName = subdirs[subdirs.length - 1];
                componentName = componentName.replaceFirst("\\.nc$", "");

                // Determine the .moml name (with path) of the file.
                String momlSuffix = inputFileName.replaceFirst("\\.nc$",
                        "\\.moml");
                String momlOutputFile = outputPrefix + _FILESEPARATOR
                        + momlSuffix;

                try {
                    // Parse the nesC xml file.
                    if (new NDReader().parse(xmlInputFile)) {
                        System.out.println("parse ok: " + xmlInputFile);
                    } else {
                        System.out.println("parse exceptions occurred: "
                                + xmlInputFile);
                    }

                    // Generate the .moml file.
                    try {
                        generateComponent(pathToNCFile, componentName,
                                momlOutputFile);
                    } catch (Exception ex) {
                        System.err.println("Errors while generating \""
                                + momlOutputFile + "\" because of exception: "
                                + ex);
                    }
                } catch (SAXException ex) {
                    System.err.println("No xml reader found for \""
                            + xmlInputFile + "\"");
                } catch (FileNotFoundException ex) {
                    System.err.println("Could not find file \"" + xmlInputFile
                            + "\"");
                } catch (Exception ex) {
                    System.err.println("Did not complete nc2moml for file: \""
                            + xmlInputFile + "\" because of exception: " + ex);
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not open file: \"" + inputFiles);
            System.err.println("\" because of exception: " + ex);
        } finally {
            if (in != null) {
                // Close the input file.
                in.close();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** File separator to use, currently "/". */
    private static String _FILESEPARATOR = "/";
}

/*
 @Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.util.ncapp2moml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import net.tinyos.nesc.dump.NDReader;
import net.tinyos.nesc.dump.xml.WiringNode;
import net.tinyos.nesc.dump.xml.WiringScan;
import net.tinyos.nesc.dump.xml.WiringScanForwards;
import net.tinyos.nesc.dump.xml.Xcomponent;
import net.tinyos.nesc.dump.xml.Xinterface;
import net.tinyos.nesc.dump.xml.Xnesc;
import net.tinyos.nesc.dump.xml.Xwire;
import net.tinyos.nesc.dump.xml.Xwiring;

import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

/**
 Generate a .moml file for each .nc application file in the input list.

 Usage:
 <pre>
 java -classpath $PTII ptolemy.domains.ptinyos.util.ncapp2moml.NCApp2MoML \
 &lt;<i>.nc source prefix</i>&gt; \
 &lt;<i>.nc xml input prefix</i>&gt; \
 &lt;<i>.nc xml input suffix</i>&gt; \
 &lt;<i>opts input suffix</i>&gt; \
 &lt;<i>moml output prefix</i>&gt; \
 <i>long path to file containing list of .nc files using short path</i>
 </pre>

 Example:
 <pre>
 java -classpath $PTII ptolemy.domains.ptinyos.util.ncapp2moml.NCApp2MoML \
 /home/celaine/ptII/vendors/ptinyos/tinyos-1.x \
 /home/celaine/ptII/vendors/ptinyos/moml \
 .ncxml \
 .opts \
 /home/celaine/ptII/vendors/ptinyos/moml \
 /home/celaine/ptII/vendors/ptinyos/moml/.tempfile
 </pre>

 .tempfile contains:
 <pre>
 apps/CntToLeds/CntToLeds.nc
 </pre>

 Example output for CntToLeds.nc:
 <pre>
 &lt;?xml version="1.0"?&gt;
 &lt;!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN" "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd"&gt;

 &lt;entity name="CntToLeds" class="ptolemy.domains.ptinyos.kernel.NCCompositeActor"&gt;
 &lt;property name="PtinyOSDirector" class="ptolemy.domains.ptinyos.kernel.PtinyOSDirector" /&gt;
 &lt;entity name="Counter" class="tos.lib.Counters.Counter" /&gt;
 &lt;entity name="IntToLeds" class="tos.lib.Counters.IntToLeds" /&gt;
 &lt;entity name="TimerC" class="tos.system.TimerC" /&gt;
 &lt;entity name="Main" class="tos.system.Main" /&gt;
 &lt;relation name="relation1" class="ptolemy.actor.IORelation" /&gt;
 &lt;relation name="relation2" class="ptolemy.actor.IORelation" /&gt;
 &lt;relation name="relation3" class="ptolemy.actor.IORelation" /&gt;
 &lt;link port="Counter.IntOutput" relation="relation3" /&gt;
 &lt;link port="Counter.Timer" relation="relation2" /&gt;
 &lt;link port="IntToLeds.StdControl" relation="relation1" /&gt;
 &lt;link port="TimerC.Timer" relation="relation2" /&gt;
 &lt;link port="Counter.StdControl" relation="relation1" /&gt;
 &lt;link port="TimerC.StdControl" relation="relation1" /&gt;
 &lt;link port="IntToLeds.IntOutput" relation="relation3" /&gt;
 &lt;link port="Main.StdControl" relation="relation1" /&gt;
 &lt;/entity&gt;
 </pre>

 Expects &lt;<i>xml input prefix</i>&gt;
 to contain files with &lt;<i>xml input suffix</i>&gt; containing
 an xml dump with the following parameters:
 <pre>
 -fnesc-dump=components(wiring, file(filename.nc)
 -fnesc-dump=referenced(interfaces)
 </pre>

 Example call to ncc:
 <pre>
 ncc '-fnesc-dump=components(wiring, file(/home/celaine/tinyos/tinyos/tinyos-1.x-scratch/apps/CntToLeds/CntToLeds.nc))' '-fnesc-dump=referenced(interfaces)' -fnesc-dumpfile=/home/celaine/ptII/vendors/moml/apps/CntToLeds/CntToLeds.ncxml /home/celaine/tinyos/tinyos/tinyos-1.x-scratch/apps/CntToLeds/CntToLeds.nc -I/home/celaine/tinyos/tinyos/tinyos-1.x-scratch/tos/lib/Counters/
 </pre>

 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class NCApp2MoML {
    /** Store the container of the interface (a component) and path to source file.
     *
     * @param intf interface of the component to be stored
     */
    void saveInterfaceContainer(Xinterface intf) {
        Xcomponent component = (Xcomponent) intf.container;
        _ComponentFile componentFile = new _ComponentFile(component,
                intf.location.filename);
        _componentTable.put(component, componentFile);
    }

    /** Traverse the configuration wiring graph and set up the component and relation data structures.
     *
     * @throws Exception if link is not to an element of type Xinterface.
     */
    void readLinks() {
        // Get the list of interfaces for this nesC component.
        ListIterator interfaces = Xnesc.interfaceList.listIterator();

        while (interfaces.hasNext()) {
            // Get the next interface for this nesC component.
            Xinterface intf = (Xinterface) interfaces.next();
            saveInterfaceContainer(intf);

            WiringNode checkNode = Xwiring.wg.lookup(intf);

            if (!intf.provided) {
                WiringScanForwards from = new WiringScanForwards(checkNode);
                ListIterator out = from.edges();
                WiringScan temp = null;

                while (out.hasNext()) {
                    Xwire e = (Xwire) out.next();
                    temp = from.duplicate();

                    if (temp.follow(e)) {
                        if (temp.node.ep instanceof Xinterface) {
                            _relations.addRelation(intf,
                                    (Xinterface) temp.node.ep);
                        } else {
                            System.err
                                    .println("Error: expected Xinterface in link "
                                            + "from "
                                            + intf
                                            + "to "
                                            + temp.node.ep);
                        }
                    }
                }
            }
        }
    }

    /** Generate the .moml file for this nesC application.
     *
     *  @param componentName The name of the component (no suffix).
     *  @param outputFile The file to generate.
     */
    public void generatePtinyOSModel(String componentName, String outputFile) {
        // Set the name of this class to the name of this nesC component.
        Element root = new Element("entity");
        root.setAttribute("name", componentName);
        root.setAttribute("class",
                "ptolemy.domains.ptinyos.kernel.NCCompositeActor");

        // Set the doc type.
        DocType plot = new DocType("entity", "-//UC Berkeley//DTD MoML 1//EN",
                "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd");
        Document doc = new Document(root, plot);

        Element director = new Element("property");
        director.setAttribute("name", "PtinyOSDirector");
        director.setAttribute("class",
                "ptolemy.domains.ptinyos.kernel.PtinyOSDirector");
        root.addContent(director);

        // Traverse the configuration graph and set up the data structures.
        readLinks();

        // Create xml for each component.
        Enumeration enumeration = _componentTable.elements();

        while (enumeration.hasMoreElements()) {
            _ComponentFile componentFile = (_ComponentFile) enumeration
                    .nextElement();
            Element entity = new Element("entity");
            entity.setAttribute("name", componentFile.getName());
            entity.setAttribute("class", componentFile.getClassName());
            root.addContent(entity);
        }

        // Create xml for each relation.
        for (int i = 1; i <= _relations.currentCount(); i++) {
            Element relation = new Element("relation");
            relation.setAttribute("name", "relation" + i);
            relation.setAttribute("class", "ptolemy.actor.IORelation");
            root.addContent(relation);
        }

        // Create xml for each link.
        Set set = _relationTable.entrySet();
        Iterator iterator = set.iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Element link = new Element("link");
            link.setAttribute("port", entry.getKey().toString());
            link.setAttribute("relation", entry.getValue().toString());
            root.addContent(link);
        }

        // Output the moml code to a file.
        try {
            // Open the file.
            FileOutputStream out = null;

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
                out.flush();
                out.close();
            } else {
                // If a file was not specified, write to stdout.
                serializer.output(doc, System.out);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /** Read in .nc application xml files, generate .moml files.
     *  @param args A series of command line arguments, see the
     *  class comment for details.
     *  @exception IOException If there is a problem reading or
     *  writing a file.
     */
    public static void main(String[] args) throws IOException {
        // Check to make sure all necessary arguments have been passed
        // to program.
        if (args.length < 6) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.domains.ptinyos.util.ncapp2moml.NCApp2MoMl "
                    + "<.nc source prefix> " + "<.nc xml input prefix> "
                    + "<.nc xml input suffix> "
                    + "<opts input suffix"
                    + "<moml output prefix> "
                    + "[long path to file containing list of .nc files using "
                    + "short path]");
            return;
        }

        // Extract arguments into variables.
        int index = 0;
        String ncSourcePrefix = args[index++].trim();
        String inputPrefix = args[index++].trim();
        String inputSuffixNC = args[index++].trim();
        String inputSuffixOpts = args[index++].trim();
        String outputPrefix = args[index++].trim();
        String inputfilelist = args[index++].trim();

        _ncSourcePrefix = ncSourcePrefix;

        try {
            // Open the file containing the list of .nc files.
            BufferedReader in = new BufferedReader(
                    new FileReader(inputfilelist));

            String inputfilename;

            // Read each line of the file.
            while ((inputfilename = in.readLine()) != null) {
                // Determine the nesC xml name (with path) of the file.
                String xmlSuffix = inputfilename.replaceFirst("\\.nc$",
                        inputSuffixNC);
                String xmlInputFile = inputPrefix + _FILESEPARATOR + xmlSuffix;

                // Determine the component name.
                String[] subdirs = inputfilename.split(_FILESEPARATOR);
                String componentName = subdirs[subdirs.length - 1];
                componentName = componentName.replaceFirst("\\.nc$", "");

                // Determine the .moml name (with path) of the file.
                String momlSuffix = inputfilename.replaceFirst("\\.nc$",
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
                        new NCApp2MoML().generatePtinyOSModel(componentName,
                                momlOutputFile);
                    } catch (Exception e) {
                        System.err.println("Errors while generating "
                                + momlOutputFile + " because of exception: "
                                + e);
                    }
                } catch (SAXException e) {
                    System.err
                            .println("No xml reader found for" + xmlInputFile);
                } catch (FileNotFoundException e) {
                    System.err.println("Could not find file " + xmlInputFile);
                } catch (Exception e) {
                    System.err.println("Did not complete harvest for file: "
                            + xmlInputFile + " because of exception: " + e);
                }
            }

            // Close the input file.
            in.close();
        } catch (IOException e) {
            System.err.println("Could not open file: " + inputfilelist);
            System.err.println("because of exception: " + e);
        }
    }

    private class _ComponentFile {
        _ComponentFile(Xcomponent component, String filename) {
            _component = component;
            _filename = filename;
        }

        /** Return the java-style path to the component source file.
         */
        public String getClassName() {
            // Example: /home/celaine/tinyos/tinyos/tinyos-1.x-scratch/tos/system/Main.nc
            // Example: tos/system/Main.nc
            String shortPath = _filename
                    .replaceFirst("^" + _ncSourcePrefix, "");

            // Example: tos/system/Main.nc
            shortPath = shortPath.replaceFirst("^" + "/", "");

            // Example: tos/system/Main
            shortPath = shortPath.replaceFirst(".nc" + "$", "");

            // Example: tos.system.Main
            String javaPath = shortPath.replace('/', '.');

            return javaPath;
        }

        public String getName() {
            return _component.toString();
        }

        public String toString() {
            return _filename + ": " + _component;
        }

        String _filename;

        Xcomponent _component;
    }

    private class _Relations {
        private int currentCount() {
            return _relationCounter;
        }

        private String getNewRelationName() {
            return "relation" + (++_relationCounter);
        }

        private void addRelation(Xinterface from, Xinterface to) {
            if (_relationTable.containsKey(from)) {
                String relationName = (String) _relationTable.get(from);
                _relationTable.put(to, relationName);
            } else {
                String relationName = getNewRelationName();
                _relationTable.put(from, relationName);
                _relationTable.put(to, relationName);
            }
        }

        private int _relationCounter = 0;
    }

    private static String _ncSourcePrefix;

    private _Relations _relations = new _Relations();

    // Contains (key, value) pairs of type (Xcomponent, _ComponentFile).
    private Hashtable _componentTable = new Hashtable();

    // Contains (key, value) pairs of type (Xinterface, relation).
    private Hashtable _relationTable = new Hashtable();

    /** File separator to use, currently "/". */
    private static String _FILESEPARATOR = "/";
}

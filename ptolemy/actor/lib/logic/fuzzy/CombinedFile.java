package ptolemy.actor.lib.logic.fuzzy;

import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
This file parses the XML output from the Trade Space Specification Tool
and creates a MoML XML model of the architecture.

The file prompts the user for an xml file (filename). It produces a MoML XML
file with the name filenameModel.xml 

@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */

public class CombinedFile extends DefaultHandler {

    public Architecture thisArchitecture;
    public Option tempOption;
    boolean startArch;
    boolean startOpt;
    boolean startDim;
    boolean endArch;
    boolean endOpt;
    boolean endDim;
    String outputFileName;

    /*
     * Reads the TSST xml file and produces the MoML xml file
     * */

    private void readCreate() {
        ArrayList componentNames = new ArrayList();
        try {
            BufferedWriter outputStream = new BufferedWriter(new FileWriter(
                    "ptolemy\\actor\\lib\\logic\\fuzzy\\" + outputFileName));// test2.xml"));
            outputStream.write("<?xml version= \"1.0\" standalone=\"no\"?>");
            outputStream.newLine();
            outputStream
            .write("<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\"");
            outputStream.newLine();
            outputStream
            .write("    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">");
            outputStream.newLine();
            outputStream
            .write("<entity name=\"dummy\" class=\"ptolemy.actor.TypedCompositeActor\">");
            outputStream.newLine();
            outputStream
            .write("<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.0.beta\">");
            outputStream.newLine();
            outputStream.write("</property>");
            outputStream.newLine();
            outputStream
            .write("<property name=\"director\" class=\"ptolemy.domains.sdf.kernel.SDFDirector\">");
            outputStream
            .write("     <property name=\"iterations\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">");
            outputStream.newLine();
            outputStream.write("     </property>");
            outputStream.newLine();
            outputStream.write("</property>");
            outputStream.newLine();
            try {
                Option tOption;
                if (thisArchitecture == null) {
                    System.out.println("this architecture is null");
                }
                for (int i = 0; i < thisArchitecture.myOptions.size(); i++) {
                    tOption = (Option) thisArchitecture.myOptions.get(i);
                    componentNames.add(tOption.name + "_"
                            + tOption.relatedDimensions.get(0).toString());
                }
                System.out.println("there are currently "
                        + thisArchitecture.myOptions.size()
                        + " components with this architecture named "
                        + thisArchitecture.getName());
                System.out.println("there were " + componentNames.size()
                        + " components ");

                for (int i = 0; i < componentNames.size(); i++) {
                    outputStream
                    .write("<entity name=\""
                            + componentNames.get(i)
                            + "\" class=\"ptolemy.actor.lib.logic.fuzzy.FuzzyLogic\">");
                    outputStream
                    .write("<property name=\"rulesFileName\" class=\"ptolemy.data.expr.Parameter\" value=\""
                            + ((Option) (thisArchitecture.myOptions
                                    .get(i))).relatedDimensions.get(0)
                                    + ".xml\">");
                    outputStream.write("</property>");
                    outputStream
                    .write("<property name=\"componentType\" class=\"ptolemy.data.expr.Parameter\" value=\""
                            + ((Option) (thisArchitecture.myOptions
                                    .get(i))).name + "\">");
                    outputStream.write("</property>");
                    outputStream.newLine();
                    outputStream
                    .write("<property name=\"init\" class=\"ptolemy.data.expr.Parameter\" value=\"2\">");
                    outputStream.write("</property>");
                    outputStream.newLine();
                    outputStream.write("</entity>");
                }

                outputStream
                .write("<entity name=\"Display\" class=\"ptolemy.actor.lib.gui.Display\">");
                outputStream.newLine();
                outputStream.write("</entity>");
                outputStream.newLine();

                for (int i = 0; i <= componentNames.size(); i++) {
                    outputStream.write(" <relation name=\"relation" + i
                            + "\" class=\"ptolemy.actor.TypedIORelation\">");
                    outputStream.newLine();
                    outputStream
                    .write("<property name=\"width\" class=\"ptolemy.data.expr.Parameter\" value=\"Auto\">");
                    outputStream.newLine();
                    outputStream.write("</property>");
                    outputStream.newLine();
                    outputStream.write("</relation>");
                    outputStream.newLine();
                }

                for (int i = 0; i < componentNames.size() - 1; i++) {

                    outputStream.write("<link port=\"" + componentNames.get(i)
                            + ".output\" relation=\"relation" + i + "\"/>");
                    outputStream.newLine();
                    outputStream.write("<link port=\""
                            + componentNames.get(i + 1)
                            + ".input\" relation=\"relation" + i + "\"/>");
                    outputStream.newLine();
                }
                if (componentNames.size() > 1) {
                    outputStream.write("<link port=\""
                            + componentNames.get(componentNames.size() - 1)
                            + ".output\" relation=\"relation"
                            + (componentNames.size() - 1) + "\"/>");
                    outputStream.newLine();
                    outputStream
                    .write("<link port=\"Display.input\" relation=\"relation"
                            + (componentNames.size() - 1) + "\"/>");
                    outputStream.newLine();
                    outputStream.write("</entity>");
                }
            } finally {
                System.out
                .println("I've closed the output stream. The output file has the name "
                        + outputFileName);
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     *  Construct a combined file object, parse the specified file,
     *  and create the Ptolemy II model
     *  @param filename The name of the XML file to be parsed
     */

    public CombinedFile(String filename) {

        boolean startArch = false;
        boolean startOpt = false;
        boolean startDim = false;
        boolean endArch = false;
        boolean endOpt = false;
        boolean endDim = false;
        outputFileName = filename.replace(".xml", "Model.xml");
        System.out.println("combinedfile constructor called");

        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            CombinedFile handler = new CombinedFile();
            handler.outputFileName = outputFileName;
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);
            FileReader r = new FileReader("ptolemy\\actor\\lib\\logic\\fuzzy\\"
                    + filename);
            xr.parse(new InputSource(r));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     * Private architecture class to recreate a textual representation 
     * of the architecture from a XML file
     * */
    class Architecture {
        String name;
        ArrayList myOptions;

        /*Default Constructor
         * */
        public Architecture() {
            name = new String("dummy");
            myOptions = new ArrayList();
        }
        /*
         * @return
         * */
        public ArrayList getComponents() {
            ArrayList componentNames = new ArrayList();

            Option tOption;
            for (int i = 0; i < this.myOptions.size(); i++) {
                tOption = (Option) myOptions.get(i);
                componentNames.add(tOption.name + "_"
                        + tOption.relatedDimensions.get(0).toString());
            }
            return componentNames;
        }

        public String getName() {
            return name;
        }
    }

    /*
     * Private options class, it stores the different dimensions in an architecture
     * */
    class Option {
        String name;
        ArrayList relatedDimensions;

        public Option() {
            name = new String("dummy");
            relatedDimensions = new ArrayList();
        }

        public String displayName() {
            return name;
        }
    }

    public static void main(String args[]) {
        System.out.println("Enter the name of the XML file containing the TSST XML output");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String fileName = null;

        try {
            fileName = br.readLine();
        } catch (IOException ioe) {
            System.out.println("Error reading file name");
            System.exit(1);
        }

        CombinedFile cF = new CombinedFile(fileName);

    }

    public ptolemy.actor.lib.logic.fuzzy.CombinedFile.Architecture getArchitecture() {
        return thisArchitecture;
    }
    /*
     * Constructs a new CombinedFile Object 
     * */
    public CombinedFile() {
        super();
        thisArchitecture = new Architecture();
        tempOption = new Option();
    }

    // //////////////////////////////////////////////////////////////////
    // Event handlers.
    // //////////////////////////////////////////////////////////////////
    /**
     *      The start Document handler is called at the beginning of an XML document
     * /
    public void startDocument() {
        // super.startDocument();
        System.out.println("Start document");
    }
    /*
     * The end Document handler is called at the end of an XML document
     * */
    public void endDocument() {
        System.out.println("End document");
    }
    /*
     * The start Element handler is called when an new XML element is seen
     * */
    public void startElement(String uri, String name, String qName,
            Attributes atts) {
        if ("".equals(uri)) {
            System.out.println("Start element: " + qName);
            if ("architectureName".equals(qName)) {
                startArch = true;
            } else if ("optionName".equals(qName)) {
                startOpt = true;
            } else if ("associatedDimensions".equals(qName)) {
                startDim = true;
            }
        } else {

            if ("architectureName".equals(name)) {
                startArch = true;
            } else if ("optionName".equals(name)) {
                startOpt = true;
            } else if ("associatedDimensions".equals(name)) {
                startDim = true;
            }
            System.out.println("Start element: {" + uri + "}" + name);
            System.out.println("in else of start element");
        }
    }
    /*
     *The end Element handler is called at the end of and XML element 
     * */
    public void endElement(String uri, String name, String qName) {
        if ("".equals(uri)) {
            if ("gov.nasa.jpl.trades.ui.menu.ExportArchitecture_-ArchitectureExport"
                    .equals(qName)) {
                endArch = true;
                System.out.println("#### in End element");
                System.out.println("For arch " + thisArchitecture.getName()
                        + "there are : " + thisArchitecture.myOptions.size()
                        + " different options.");
                // System.out.println("***architecture name is: "+thisArchitecture.getName());
                System.out.println("they are: ");
                Option tOption;
                for (int i = 0; i < thisArchitecture.myOptions.size(); i++) {
                    tOption = (Option) thisArchitecture.myOptions.get(i);
                    System.out.println(tOption.name + " "
                            + tOption.relatedDimensions.get(0).toString());
                }
                System.out.println("before readCreate in EndElement");
                readCreate();
            } else if ("optionName".equals(qName)) {
                endOpt = true;
            } else if ("associatedDimensions".equals(qName)) {
                endDim = true;
            }
            if (endDim == true) {
                endDim = false;
                thisArchitecture.myOptions.add(tempOption);
                tempOption = new Option();
            }
            if (endOpt == true) {
                endOpt = false;
                int k = tempOption.relatedDimensions.size();
                System.out.println("there are " + k
                        + "dimensions with this option");
            }
            System.out.println("End element: " + qName);

        } else {
            if ("gov.nasa.jpl.trades.ui.menu.ExportArchitecture_-ArchitectureExport"
                    .equals(name)) {
                endArch = true;
            } else if ("optionName".equals(name)) {
                endOpt = true;
            } else if ("associatedDimensions".equals(name)) {
                endDim = true;
            }
            System.out.println("End element:   {" + uri + "}" + name);
        }
    }

    public void characters(char ch[], int start, int length) {
        if (startArch == true) {
            startArch = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            thisArchitecture.name = tempBuff.toString();
            System.out.println("architecture name is " + thisArchitecture.name);
        } else if (startOpt == true) {
            startOpt = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            tempOption.name = tempBuff.toString();
            System.out.println("option has value " + tempOption.name);
        } else if (startDim == true) {
            startDim = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            tempOption.relatedDimensions.add(tempBuff.toString());
            System.out.println("related dimension is: " + tempBuff.toString());
            System.out
            .println("size is " + tempOption.relatedDimensions.size());
        }

        System.out.print("Characters:    \"");
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case '\\':
                System.out.print("\\\\");
                break;
            case '"':
                System.out.print("\\\"");
                break;
            case '\n':
                System.out.print("\\n");
                break;
            case '\r':
                System.out.print("\\r");
                break;
            case '\t':
                System.out.print("\\t");
                break;
            default:
                System.out.print(ch[i]);
            break;
            }
        }
        System.out.print("\"\n");
    }

    /**
     * <p>
     * This indicates that a processing instruction (other than the XML
     * declaration) has been encountered.
     * </p>
     * 
     * @param target
     *            <code>String</code> target of PI
     * @param data
     *            <code>String</code containing all data sent to the PI. This
     *            typically looks like one or more attribute value pairs.
     * @throws <code>SAXException</code> when things go wrong
     */
    public void processingInstruction(String target, String data) {

        System.out.println("Inside processignInstruction:");
        System.out.println("target name is: " + target + " and data value is: "
                + data);
    }


}
/* Parse the XML output from the Trade Space Specification Tool
and create a MoML representation of the architecture model.

 Copyright (c) 2009 The Regents of the University of California.
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

package ptolemy.actor.lib.logic.fuzzy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
Parse the XML output from the Trade Space Specification Tool
and create a MoML representation of the architecture model.
<p>
The Trade Space Specification Tool (TSST) is currently under development
in the Rapid Prototyping group at the NASA Jet Propulsion Laboratory in 
Pasedena CA. The Trade Space Specification tool allows the user to 
evaluate various component combinations(ie. solar power vs. nuclear
power) necessary in space craft design. 

This class prompts the user for a filename which should be an xml file
produced by the Trade Space Specification Tool. This class produces a 
MoML representation of the Architecture model that includes 
instantiations of the FuzzyLogic actor.

If the input file name is <code><i>filename</i>.xml</code>, the output 
file name will be <code><i>filename</i>Model.xml</code>. 

@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */

public class CombinedFile extends DefaultHandler {

    /**
     * Construct a new CombinedFile Object. 
     *
     */
    public CombinedFile() {
        super();
        architecture = new Architecture();
        _option = new Option();
    }

    /**
     *  Construct a combined file object, parse the specified file,
     *  and create the Ptolemy II model.
     *  It is expected that this is run from the path $PTII and it 
     *  prepends the location "ptolemy\\actor\\lib\\logic\\fuzzy\\" to 
     *  the filename
     *  @param filename The name of the XML file to be parsed
     *  @exception Exception If the input file cannot be read or
     *  parsed.
     */
    public CombinedFile(String filename) throws Exception {

        _startArchitecture = false;
        _startOption = false;
        _startDimension = false;
        _endArchitecture = false;
        _endOption = false;
        _endDimension = false;
        _outputFileName = filename.replace(".xml", "Model.xml");
        if(_debugging){
            System.out.println("combinedfile constructor called");
        }

        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        CombinedFile handler = new CombinedFile();
        handler._outputFileName = _outputFileName;
        xmlReader.setContentHandler(handler);
        xmlReader.setErrorHandler(handler);
        FileReader reader = new FileReader("ptolemy\\actor\\lib\\logic" +
                "\\fuzzy\\"+ filename);
        xmlReader.parse(new InputSource(reader));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * A TSST may contain multiple architectures where an architecture 
     * is a possible combination of components.
     * Currently, the MoML representation is limited to a single 
     * architecture.
     */
    public Architecture architecture;



    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* Read the TSST xml file and produce the MoML xml file with the 
     * Ptolemy II model.
     *
     */
    private void readCreate() {
        ArrayList<String> componentNames = new ArrayList<String>();

        String output="";
        output+="<?xml version= \"1.0\" standalone=\"no\"?>";
        output+=newline;
        output+="<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD " +
        "MoML 1//EN\"";
        output+= newline;
        output+="    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/"+
        "MoML_1.dtd\">";
        output+= newline;
        output+="<entity name=\"dummy\" class=\"ptolemy.actor.Typed" +
        "CompositeActor\">";
        output+= newline;
        output+= "<property name=\"_createdBy\" class=\"ptolemy." +
        "kernel.attributes.VersionAttribute\" " +
        "value=\"8.0.beta\">";
        output+= newline;
        output+="</property>";
        output+= newline;
        output+="<property name=\"director\" class=\"ptolemy." +
        "domains.sdf.kernel.SDFDirector\">";
        output+="     <property name=\"iterations\" class=\"ptol" +
        "emy.data.expr.Parameter\" value=\"1\">";
        output+= newline;
        output+="     </property>";
        output+= newline;
        output+= "</property>";
        output+= newline; 
        try {
            Option tOption;
            for (int i = 0; i < architecture.myOptions.size(); i++) {
                tOption = (Option) architecture.myOptions.get(i);
                componentNames.add(tOption.name + "_"
                        + tOption.relatedDimensions.get(0).toString());
            }
            if(_debugging){
                System.out.println("there are currently "
                        + architecture.myOptions.size()
                        + " components with this architecture " 
                        + "named "+ architecture.getName());
                System.out.println("there were " + componentNames.size()
                        + " components ");
            }
            for (int i = 0; i < componentNames.size(); i++) {
                output+= "<entity name=\""
                    + componentNames.get(i)
                    + "\" class=\"ptolemy.actor.lib.logic.fuzzy." +
                    "FuzzyLogic\">";
                output+= "<property name=\"rulesFileName\" " +
                "class=\"ptolemy.data.expr.Parameter\"" +
                " value=\""
                + ((Option) (architecture.myOptions
                        .get(i))).relatedDimensions.get(0)
                        + ".xml\">";
                output+="</property>";
                output+="<property name=\"componentType\" " +
                "class=\"ptolemy.data.expr.Parameter" +
                "\" value=\""
                + ((Option) (architecture.myOptions
                        .get(i))).name + "\">";
                output+= "</property>";
                output+= newline;
                output+= "<property name=\"init\" class=\"ptolemy." +
                "data.expr.Parameter\" value=\"2\">";
                output+= "</property>";
                output+= newline;
                output+="</entity>";
            }

            output+= "<entity name=\"Display\" class=\"ptolemy." +
            "actor.lib.gui.Display\">";
            output+= newline;
            output+= "</entity>";
            output+= newline;

            for (int i = 0; i <= componentNames.size(); i++) {
                output+= " <relation name=\"relation" + i
                + "\" class=\"ptolemy.actor.TypedIORelation\">";
                output+= newline;
                output+= "<property name=\"width\" class=\"ptolemy"+
                ".data.expr.Parameter\" value=\"Auto\">";
                output+= newline;
                output+= "</property>";
                output+= newline;
                output+= "</relation>";
                output+= newline;
            }

            for (int i = 0; i < componentNames.size() - 1; i++) {

                output+= "<link port=\"" + componentNames.get(i)
                + ".output\" relation=\"relation" + i + "\"/>";
                output+= newline;
                output+= "<link port=\""
                    + componentNames.get(i + 1)
                    + ".input\" relation=\"relation" + i + "\"/>";
                output+= newline;
            }
            if (componentNames.size() > 1) {
                output+="<link port=\""
                    + componentNames.get(componentNames.size() - 1)
                    + ".output\" relation=\"relation"
                    + (componentNames.size() - 1) + "\"/>";
                output+= newline;
                output+= "<link port=\"Display.input\" relation=\"" +
                "relation" +
                (componentNames.size() - 1) + "\"/>";
                output+= newline;
                output+="</entity>";
            }
        } finally {
            if(_debugging){
                System.out
                .println("I've closed the output stream. The " +
                        "output file has the name "
                        + _outputFileName);
            }
            BufferedWriter outputStream=null;
            try{
                outputStream = new BufferedWriter(new FileWriter(
                        "ptolemy\\actor\\lib\\logic\\fuzzy\\" +
                        _outputFileName));
                outputStream.write(output);
                outputStream.close();
            } catch (IOException ioe) {
                try{
                    outputStream.close();
                }catch(IOException ioex)
                {
                    if(_debugging){
                        System.out.println("There was an exception " +
                                "when attempting to close the " +
                                "file from" +
                        "the ioexception catch block");
                    }
                }
                ioe.printStackTrace();

            }
        }


    }

    /**
     * Private architecture class to recreate a textual representation 
     * of the architecture from a XML file.
     */
    static class Architecture {
        String name="";
        ArrayList myOptions;

        /*Default Constructor
         * */
        public Architecture() {
            name = "dummy";
            myOptions = new ArrayList<String>();
        }
        /*
         *          * */
        public ArrayList getComponents() {
            ArrayList<String> componentNames = new ArrayList<String>();

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

    /**
     * Private options class, it stores the different dimensions in an
     * architecture.
     *
     */
    static class Option {
        String name;
        ArrayList relatedDimensions;

        public Option() {
            name = "dummy";
            relatedDimensions = new ArrayList();
        }

        public String displayName() {
            return name;
        }
    }

    /**
     * Main method for the class.
     * @param args with inputs to the main method.
     * Inputs are currently not used
     */
    public static void main(String args[]) {
        try {
            System.out.println("Enter the name of the XML file containing the TSST XML output");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String fileName = null;
            fileName = reader.readLine();
            if(fileName!= null){
                CombinedFile cF = new CombinedFile(fileName);

            }

        } catch(Exception ex){
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    /**
     * Return the current architecture.
     * @return current architecture
     */
    public ptolemy.actor.lib.logic.fuzzy.CombinedFile.Architecture getArchitecture() {
        return architecture;
    }


    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////
    /*
     * The end Document handler is called at the end of an XML document
     *
     */
    public void endDocument() {
        System.out.println("End document");
    }

    /*
     * The start Element handler is called when an new XML element is seen
     *
     */
    public void startElement(String uri, String name, String qName,
            Attributes atts) {
        if ("".equals(uri)) {
            if(_debugging){
                System.out.println("Start element: " + qName);
            }
            if ("architectureName".equals(qName)) {
                _startArchitecture = true;
            } else if ("optionName".equals(qName)) {
                _startOption = true;
            } else if ("associatedDimensions".equals(qName)) {
                _startDimension = true;
            }
        } else {

            if ("architectureName".equals(name)) {
                _startArchitecture = true;
            } else if ("optionName".equals(name)) {
                _startOption = true;
            } else if ("associatedDimensions".equals(name)) {
                _startDimension = true;
            }
            if(_debugging){
                System.out.println("Start element: {" + uri + "}" + name);
                System.out.println("in else of start element");
            }
        }
    }

    /*
     * The end Element handler is called at the end of and XML element.
     *
     */
    public void endElement(String uri, String name, String qName) {
        if ("".equals(uri)) {
            if ("gov.nasa.jpl.trades.ui.menu.ExportArchitecture_-ArchitectureExport"
                    .equals(qName)) {
                _endArchitecture = true;
                if(_debugging){
                    System.out.println("#### in End element");
                    System.out.println("For arch " + architecture.getName()
                            + "there are : " + architecture.myOptions.size()
                            + " different options.");
                    // System.out.println("***architecture name is: "+thisArchitecture.getName());
                    System.out.println("they are: ");
                }
                Option tOption;
                for (int i = 0; i < architecture.myOptions.size(); i++) {
                    if(_debugging){
                        tOption = (Option) architecture.myOptions.get(i);
                        System.out.println(tOption.name + " "
                                + tOption.relatedDimensions.get(0).toString());
                    }
                }
                if(_debugging){
                    System.out.println("before readCreate in EndElement");
                }
                readCreate();
            } else if ("optionName".equals(qName)) {
                _endOption = true;
            } else if ("associatedDimensions".equals(qName)) {
                _endDimension = true;
            }
            if (_endDimension == true) {
                _endDimension = false;
                architecture.myOptions.add(_option);
                _option = new Option();
            }
            if (_endOption == true) {
                _endOption = false;

                if(_debugging){
                    int k = _option.relatedDimensions.size();
                    System.out.println("there are " + k
                            + "dimensions with this option");
                }
            }
            if(_debugging){
                System.out.println("End element: " + qName);
            }

        } else {
            if ("gov.nasa.jpl.trades.ui.menu.ExportArchitecture_-ArchitectureExport"
                    .equals(name)) {
                _endArchitecture = true;
            } else if ("optionName".equals(name)) {
                _endOption = true;
            } else if ("associatedDimensions".equals(name)) {
                _endDimension = true;
            }
            if(_debugging){
                System.out.println("End element:   {" + uri + "}" + name);
            }
        }
    }

    public void characters(char ch[], int start, int length) {
        if (_startArchitecture == true) {
            _startArchitecture = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            architecture.name = tempBuff.toString();
            if(_debugging){
                System.out.println("architecture name is " + architecture.name);
            }
        } else if (_startOption == true) {
            _startOption = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            _option.name = tempBuff.toString();
            if(_debugging){
                System.out.println("option has value " + _option.name);
            }
        } else if (_startDimension == true) {
            _startDimension = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            _option.relatedDimensions.add(tempBuff.toString());
            if(_debugging){
                System.out.println("related dimension is: " + tempBuff.toString());
                System.out
                .println("size is " + _option.relatedDimensions.size());
            }
        }
        if(_debugging){
            System.out.print("Characters:    \"");
        }
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case '\\':
                if(_debugging){
                    System.out.print("\\\\");
                }
                break;
            case '"':
                if(_debugging){
                    System.out.print("\\\"");
                }
                break;
            case '\n':
                if(_debugging){
                    System.out.print("\\n");
                }
                break;
            case '\r':
                if(_debugging){
                    System.out.print("\\r");
                }
                break;
            case '\t':
                if(_debugging){
                    System.out.print("\\t");
                }
                break;
            default:
                if(_debugging){
                    System.out.print(ch[i]);
                }
            break;
            }
        }
        if(_debugging){
            System.out.print("\"\n");
        }
    }

    /**
     * This indicates that a processing instruction (other than the XML
     * declaration) has been encountered.
     * @param target <code>String</code> target of PI
     * @param data <code>String</code containing all data sent to the PI. 
     * This typically looks like one or more attribute value pairs.
     * @throws <code>SAXException</code> when things go wrong
     */
    public void processingInstruction(String target, String data) {
        if(_debugging){
            System.out.println("Inside processignInstruction:");
            System.out.println("target name is: " + target + " and data value is: "
                    + data);
        }
    }


    ////////////////////////////////////////////////////////////////////
    ////                         private variables                  ////

    //these variables are used in the class 
    private Option _option;
    private String _outputFileName;
    //these variables are constants used throught the class
    private final boolean _debugging = false;
    /** newline marker*/
    public static final String newline = System.getProperty("line.separator");
    // These flags mark the start and end of architectures, options,
    // and dimensions in the file produced by TSST.
    private boolean _startArchitecture;
    private boolean _startOption;
    private boolean _startDimension;
    private boolean _endArchitecture;
    private boolean _endOption;
    private boolean _endDimension;


}
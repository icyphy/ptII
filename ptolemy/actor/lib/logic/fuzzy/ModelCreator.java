/* Parse the XML output from the Trade Space Specification Tool
and create a MoML representation of the architecture model.

 Copyright (c) 2009-2010 The Regents of the University of California.
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ptolemy.kernel.util.IllegalActionException;


/**
Parse the XML output from the Trade Space Specification Tool
and create a MoML representation of the architecture model.
<p>
The Trade Space Specification Tool (TSST) is currently under development
in the Rapid Prototyping group at the NASA Jet Propulsion Laboratory in
Pasedena CA. The Trade Space Specification tool allows the user to
evaluate various component combinations (i.e. solar power vs. nuclear
power) necessary in space craft design. </p>
<p>
This class is as stand alone application that prompts the user for a
filename which should be an xml file produced by the Trade Space
Specification Tool. This application produces a MoML representation of the
Architecture model that includes instantiations of the
{@link  ptolemy.actor.lib.logic.fuzzy.FuzzyLogic} actor.</p>
<p>
If the input file name is <code><i>filename</i>.xml</code>, the output
file name will be <code><i>filename</i>Model.xml</code>.</p>

@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */

public class ModelCreator extends DefaultHandler {

    /**
     * Construct a new CombinedFile Object.
     */
    public ModelCreator() {
        super();
        _architecture = new Architecture();
        _option = new Option();
    }

    /**
     *  Construct a CombinedFile object, parse the specified file,
     *  and create the Ptolemy II model.
     *  It is expected that this application is run from the $PTII
     *  directory and it prepends the location
     *  "ptolemy/actor/lib/logic/fuzzy/" to the filename.
     *  @param filename The name of the XML file to be parsed
     *  @exception Exception If the input file cannot be read or
     *  parsed.
     */
    public ModelCreator(String filename) throws Exception {
        _startArchitecture = false;
        _startOption = false;
        _startDimension = false;
        _endArchitecture = false;
        _endOption = false;
        _endDimension = false;
        _outputFileName = filename.replace(".xml", "Model.xml");
        if (_debugging) {
            System.out.println("combinedfile constructor called");
        }
        FileReader reader = null;
        try {

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            ModelCreator handler = new ModelCreator();
            handler._outputFileName = _outputFileName;
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            reader = new FileReader("ptolemy/actor/lib/logic"
                    + "/fuzzy/"+ filename);
            xmlReader.parse(new InputSource(reader));

        } catch (Exception ex) {
            throw new IllegalActionException(null, ex, "Failed to parse "
                    + filename + ".");
        } finally{
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex,
                            "Failed to close " + filename + ".");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Called by the SAX parser to report regular characters.
     * @param ch The array containing characters
     * @param start Is the starting point in the character array
     * @param length Is length of the character array
     * */
    public void characters(char ch[], int start, int length) {
        if (_startArchitecture == true) {
            _startArchitecture = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            _architecture.name = tempBuff.toString();
            if (_debugging) {
                System.out.println("architecture name is " + _architecture.name);
            }
        } else if (_startOption == true) {
            _startOption = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            _option._name = tempBuff.toString();
            if (_debugging) {
                System.out.println("option has value " + _option._name);
            }
        } else if (_startDimension == true) {
            _startDimension = false;
            StringBuffer tempBuff = new StringBuffer();
            tempBuff.append(ch, start, length);
            _option._relatedDimensions.add(tempBuff.toString());
            if (_debugging) {
                System.out.println("related dimension is: " + tempBuff.toString());
                System.out
                .println("size is " + _option._relatedDimensions.size());
            }
        }
        if (_debugging) {
            System.out.print("Characters:    \"");
        }
        //Note: Findbugs state that the same code is used for two switch statements
        //however that is not the case
        for (int i = start; i < start + length; i++) {
            switch (ch[i]) {
            case '\\':
                if (_debugging) {
                    System.out.print("\\\\");
                }
                break;
            case '"':
                if (_debugging) {
                    System.out.print("\\\"");
                }
                break;
            case '\n':
                if (_debugging) {
                    System.out.print("\\n");
                }
                break;
            case '\r':
                if (_debugging) {
                    System.out.print("\\r");
                }
                break;
            case '\t':
                if (_debugging) {
                    System.out.print("\\t");
                }
                break;
            default:
                if (_debugging) {
                    System.out.print(ch[i]);
                }
                break;
            }
        }
        if (_debugging) {
            System.out.print("\"\n");
        }
    }

    /**
     * Called once when the SAX driver sees the end of a document,
     * even if errors occurred.
     * */
    public void endDocument() {
    }

    /** Called each time the SAX parser sees the end of an element.
     * @param uri The Namespace Uniform Resource Identifier(URI)
     * @param name Is the elements local name
     * @param qName Is the XML 1.0 name
     * */
    public void endElement(String uri, String name, String qName) {
        if ("".equals(uri)) {
            if ("gov.nasa.jpl.trades.ui.menu.ExportArchitecture_-ArchitectureExport"
                    .equals(qName)) {
                _endArchitecture = true;
                if (_debugging) {
                    System.out.println("#### in End element");
                    System.out.println("For arch " + _architecture.getName()
                            + "there are : " + _architecture.myOptions.size()
                            + " different options.");
                    System.out.println("they are: ");
                }
                Option tOption;
                for (int i = 0; i < _architecture.myOptions.size(); i++) {
                    if (_debugging) {
                        tOption = (Option) _architecture.myOptions.get(i);
                        System.out.println(tOption._name + " "
                                + tOption._relatedDimensions.get(0).toString());
                    }
                }
                if (_debugging) {
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
                _architecture.myOptions.add(_option);
                _option = new Option();
            }
            if (_endOption == true) {
                _endOption = false;

                if (_debugging) {
                    int k = _option._relatedDimensions.size();
                    System.out.println("there are " + k
                            + "dimensions with this option");
                }
            }
            if (_debugging) {
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
            if (_debugging) {
                System.out.println("End element:   {" + uri + "}" + name);
            }
        }
    }

    /**
     * Return the current architecture.
     * @return current architecture
     */
    public Architecture getArchitecture() {
        return _architecture;
    }

    /**
     * This indicates that a processing instruction (other than the XML
     * declaration) has been encountered.
     * @param target <code>String</code> target of PI
     * @param data <code>String</code containing all data sent to the PI.
     * This typically looks like one or more attribute value pairs.
     * @exception <code>SAXException</code> when things go wrong
     */
    public void processingInstruction(String target, String data) {
        if (_debugging) {
            System.out.println("Inside processignInstruction:");
            System.out.println("target name is: " + target + " and data value is: "
                    + data);
        }
    }

    /**
     * Parse the XML output from the Trade Space Specification Tool
     * and create a MoML representation of the architecture model.
     * <p>To run this application:
     * <pre>
     *  java -classpath $PTII ptolemy.actor.lib.logic.fuzzy.CombinedFile TSSTOutput.xml
     * </pre>
     *  where <code>TSSTOutput.xml</code> is the output from the Trade
     *  Space Specification Tool.
     *  Note: This argument is optional. If it is not provided the user
     *  will be prompted for a filename.
     * </p>
     * @param args with inputs to the main method.
     * @exception IllegalActionException If the input file cannot be read or
     *  parsed.
     */
    public static void main(String args[])throws IllegalActionException {
        BufferedReader reader = null;
        String fileName = null;
        try {
            if (args.length < 1) {

                System.out.println("Enter the name of the XML file containing the TSST XML output");
                try {
                    reader = new BufferedReader(new InputStreamReader(System.in));
                    fileName = reader.readLine();
                } catch (IOException ex) {
                    throw new IllegalActionException(null, ex, "Failed to parse input.");
                } finally{
                    if (reader!=null) {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            throw new IllegalActionException(null, ex,
                            "Failed to close buffered reader which reads input.");
                        }
                    }
                }
            }else {
                fileName = args[0];
            }
            if (fileName!= null) {
                //Note: Findbugs correctly detects a deadstore to a local
                // variable. This is acceptable in this case since the work is done
                // in the constructor
                ModelCreator cF = new ModelCreator(fileName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }finally{
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException ex) {

                    throw new IllegalActionException(null, ex,
                            "Failed to close " + fileName + ".");
                }
            }
        }



        System.exit(0);
    }

    /** Called each time the SAX parser sees the beginning of an element.
     * @param uri The Namespace Uniform Resource Identifier(URI)
     * @param name Is the elements local name
     * @param qName Is the XML 1.0 name
     * @param atts  An Attributes object
     * */
    public void startElement(String uri, String name, String qName,
            Attributes atts) {
        // Note: The method names and the parameters match those in the
        // DefaultHandler class
        if ("".equals(uri)) {
            if (_debugging) {
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
            if (_debugging) {
                System.out.println("Start element: {" + uri + "}" + name);
                System.out.println("in else of start element");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    // Customized SAX XML Event handlers.
    ///////////////////////////////////////////////////////////////////
    /** Called once when the SAX driver sees the beginning of a document. */
    public void startDocument() {
    }

    ///////////////////////////////////////////////////////////////////
    ////               protected classes                        ////
    /**
     * Private architecture class to recreate a textual representation
     * of the architecture from a XML file.
     */
    protected class Architecture {
        /** Construct an architecture object. */
        public Architecture() {
            name = "dummy";
            myOptions = new ArrayList<Option>();
        }

        ///////////////////////////////////////////////////////////////////
        ////    public variables                                       ////

        /**Name of the TSST architecture being modeled by this Architecture class.*/
        public String name="";
        /** List of TSST options associated with this architecture.*/
        public ArrayList<Option> myOptions;

        ///////////////////////////////////////////////////////////////////
        ////    public methods                    ////

        /**
         * Return an array list consisting of the components/options
         * in the architecture.
         * @return An array list consisting of the components/options
         * in the architecture.
         * */
        public ArrayList<String> getComponents() {
            ArrayList<String> componentNames = new ArrayList<String>();

            Option option;
            for (int i = 0; i < this.myOptions.size(); i++) {
                option = (Option) myOptions.get(i);
                componentNames.add(option._name + "_"
                        + option._relatedDimensions.get(0).toString());
            }
            return componentNames;
        }

        /** Return the name of the architecture.
         * @return The name of the architecture */
        public String getName() {
            return name;
        }
    }

    /** Store the different dimensions in an architecture. */
    protected class Option {
        /** Construct an option. */
        public Option() {
            _name = "dummy";
            _relatedDimensions = new ArrayList<String>();
        }
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Return the display name for the option.
         * @return The display name for the option. */
        public String displayName() {
            return _name;
        }

        ///////////////////////////////////////////////////////////////////
        ////                        private variables                  ////

        /** Name of the option. */
        private String _name;
        /** List of the dimensions related to this option. */
        private ArrayList _relatedDimensions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     *  Read the TSST xml file and produce the MoML xml file with the
     *  Ptolemy II model. The Ptolemy II model is placed in $PTII/
     *  "ptolemy/actor/lib/logic/fuzzy/".
     */
    private void readCreate() {
        ArrayList<String> componentNames = new ArrayList<String>();
        StringBuffer output=new StringBuffer(
                "<?xml version= \"1.0\" standalone=\"no\"?>"+_eol
                +"<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD "
                +"MoML 1//EN\""+ _eol
                +"    \"http://ptolemy.eecs.berkeley.edu/xml/dtd/"
                +"MoML_1.dtd\">"+ _eol
                +"<entity name=\"dummy\" class=\"ptolemy.actor.Typed"
                +"CompositeActor\">"+_eol
                + "<property name=\"_createdBy\" class=\"ptolemy."
                +"kernel.attributes.VersionAttribute\" "
                +"value=\"8.0.beta\">"+ _eol
                +"</property>"+ _eol
                +"<property name=\"SDF Director\" class=\"ptolemy."
                +"domains.sdf.kernel.SDFDirector\">"
                +"<property name=\"iterations\" class=\"ptolemy."
                +"data.expr.Parameter\" value=\"1\">"
                +"     </property>"+_eol
                +"</property>"+ _eol);

        try {
            Option option;
            for (int i = 0; i < _architecture.myOptions.size(); i++) {
                option = (Option) _architecture.myOptions.get(i);
                componentNames.add(option._name + "_"
                        + option._relatedDimensions.get(0));
            }
            if (_debugging) {
                System.out.println("There are currently "
                        + _architecture.myOptions.size()
                        + " components with this architecture "
                        + "named "+ _architecture.getName());
                System.out.println("There were " + componentNames.size()
                        + " components ");
            }
            for (int i = 0; i < componentNames.size(); i++) {
                output.append("<entity name=\""
                        + componentNames.get(i)
                        + "\" class=\"ptolemy.actor.lib.logic.fuzzy."
                        +"FuzzyLogic\">"
                        +"<property name=\"rulesFileName\" "
                        +"class=\"ptolemy.data.expr.Parameter\""
                        +" value=\""
                        + ((Option) (_architecture.myOptions.get(i))).
                        _relatedDimensions.get(0)
                        + ".xml\">"
                        +"</property>"
                        +"<property name=\"componentType\" "
                        +"class=\"ptolemy.data.expr.Parameter"
                        +"\" value=\""
                        + ((Option) (_architecture.myOptions.get(i))).
                        _name + "\">"
                        +"</property>"+_eol
                        +"</entity>");
            }

            output.append(" <entity name=\"AddSubtract\" class=\"ptolemy.actor." +
                    "lib.AddSubtract\">"+_eol+
            " </entity>");
            output.append("<entity name=\"CumulativeCostDisplay\" class=\"ptolemy."
                    +"actor.lib.gui.Display\">" + _eol
                    +"</entity>"+_eol);

            int relationCount = componentNames.size()+1;
            for (int i = 0; i < relationCount; i++) {
                output.append(" <relation name=\"relation" + i
                        + "\" class=\"ptolemy.actor.TypedIORelation\">"+_eol
                        +"<property name=\"width\" class=\"ptolemy"
                        +".data.expr.Parameter\" value=\"Auto\">"+_eol
                        + "</property>"+ _eol
                        +"</relation>"+ _eol);
            }

            int relationNumber = 0;
            for (int i = 0; i < componentNames.size(); i++) {

                // Cost
                output.append("<link port=\"" + componentNames.get(i)
                        + ".output\" relation=\"relation" + relationNumber
                        +"\"/>"+_eol
                        +"<link port=\""+ "AddSubtract.plus\""
                        +" relation=\"relation" + relationNumber
                        +"\"/>"
                        +_eol);

                relationNumber++;

            }

            if (componentNames.size() > 1) {

                // Cost
                output.append("<link port=\""
                        + "AddSubtract.output\" relation=\"relation"
                        + relationNumber + "\"/>"+ _eol
                        +"<link port=\"CumulativeCostDisplay.input\" relation=\""
                        +"relation" +relationNumber + "\"/>"+ _eol);
                relationNumber++;

                output.append("</entity>");
            }
        } finally {
            if (_debugging) {
                System.out
                .println("I've closed the output stream. The " +
                        "output file has the name "
                        + _outputFileName);
            }
            BufferedWriter outputStream=null;
            try {
                outputStream = new BufferedWriter(new FileWriter(
                        "ptolemy/actor/lib/logic/fuzzy/" +
                        _outputFileName));
                outputStream.write(output.toString());
                outputStream.close();
            } catch (IOException ioe) {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException ioex) {
                    if (_debugging) {
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * A TSST may contain multiple architectures where an architecture
     * is a possible combination of components.
     * Currently, the MoML representation is limited to a single
     * architecture.
     */
    private Architecture _architecture;


    //these variables are used in the class
    private Option _option;
    private String _outputFileName;
    //these variables are constants used throught the class
    private final boolean _debugging = false;
    /** newline marker*/
    private final String _eol = System.getProperty("line.separator");
    // These flags mark the start and end of architectures, options,
    // and dimensions in the file produced by TSST.
    private boolean _startArchitecture;
    private boolean _startOption;
    private boolean _startDimension;
    private boolean _endArchitecture;
    private boolean _endOption;
    private boolean _endDimension;

}


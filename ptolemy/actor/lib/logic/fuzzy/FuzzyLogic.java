/* An actor that implements fuzzy logic operation.

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

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import fuzzy.FuzzyEngine;
import fuzzy.LinguisticVariable;

/**
An actor that implements a fuzzy logic operation.
<p>
Fuzzy Logic is multi-valued logic derived from Fuzzy Set Theory by
Lotfi Zadeh. In fuzzy logic, reasoning is approximate instead of
precise and takes on values between 0 and 1.
</p>
<p>
This actor has three inputs and three outputs. The cost input and
output are of type double. The remaining two inputs are of type
string.  If no input token is available, the actor proceeds with
its defuzzification and produces an output. Defuzzification is the
evaluation of specification provided by the designer for a fuzzy logic
based control system, such as "speed too fast," "speed too slow" and
"speed about right"  for a specific input and is used to determine
a crisp output.A brief overview of fuzzy logic can be found at
<a href="http://www.fuzzy-logic.com/#in_browser">http://www.fuzzy-logic.com/</a>.
</p>

<p>If there is input, the defuzzfied value is added or appended to the
input and produced as output. If there is no input, the defuzzified
value is produced as output. As a result, in a chain of FuzzyLogic
actors the inputs of the first actor need not be connected for the model
to run under the {@link ptolemy.actor.Director}.
</p>
@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */

public class FuzzyLogic extends TypedAtomicActor {
    /**
     *  Construct a fuzzy logic actor and set a default rule file name
     *  and a default component type.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception NameDuplicationException
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public FuzzyLogic(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        rulesFileName = new FileParameter(this, "rulesFileName");
        rulesFileName.setExpression("rules.xml");

        // FIXME: This should be numberOfOutputs.
        numOutputs = new Parameter(this, "numOutputs");
        numOutputs.setExpression("1");

        numInputs = new Parameter(this, "numInputs");
        numInputs.setExpression("1");

        componentType = new Parameter(this, "componentType");
        componentType.setExpression("dummyType");

        inputPorts = new Vector<TypedIOPort>();
        outputPorts = new Vector<TypedIOPort>();

        tempInputPort = new TypedIOPort(this, "input", true, false);
        tempInputPort.setTypeEquals(BaseType.DOUBLE);
        inputPorts.addElement(tempInputPort);

        tempOutputPort = new TypedIOPort(this, "output", false, true);
        tempOutputPort.setTypeEquals(BaseType.DOUBLE);
        outputPorts.addElement(tempOutputPort);
        _numOutputs = 1;
        _numInputs = 1;

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** An input port that contains the level of risk associated
     * with upstream components.
     * In this context, risk is the risk that a failure will
     * occur. The default type of this port is String. The only
     * meaningful values are "high", "medium" and "low".
     */
    //  public TypedIOPort riskInput;

    /** An input port that contains the cost associated
     * with upstream components.
     * The default type of this port is Double.
     */
    // public TypedIOPort costInput;
    public Vector<TypedIOPort> inputPorts;

    /** An input port that contains the mass associated
     * with upstream components.
     * The default type of this port is String. Currently the only
     * meaningful values are "high", "medium" and "low".
     */
    //  public TypedIOPort massInput;

    /** An output port that specifies the level of risk associated
     * with using a component or combination of components.
     * In this context, risk is the risk that a failure will
     * occur. The default type of this port is String. The only
     * meaningful values are "high", "medium" and "low".
     */
    //  public TypedIOPort risk;

    /** An output port that specifies the mass associated
     * with using a component or combination of components.
     * The default type of this port is String. Currently the only
     * meaningful values are "high", "medium" and "low", however they
     * could be changed to doubles in the future.
     */
    //  public TypedIOPort mass;

    /** An output port that specifies the cost associated
     * with using a component or combination of components.
     * The default type of this port is Double.
     */
    //public TypedIOPort cost;
    public Vector<TypedIOPort> outputPorts;

    /**
     * The name of the file containing the xml specification for the component.
     * The default value is a file name <code>rules.xml</code>.
     * The file is expected to be specified in XML/FCL Fuzzy Control Language XML.
     * Details on this schema can be found at
     * <a href="http://www.havana7.com/dotfuzzy/format.aspx#in_browser">http://www.havana7.com/dotfuzzy/format.aspx</a>.
     * <p>
     * The file named by this parameter should have a fuzzify xml element with the same name as
     * the base name of the xml file. For example: The file <code>rules.xml</code> should contain
     *  <pre>
     *  <FUZZIFY NAME="rules">
     *   ...
     *  </FUZZIFY>
     *  </pre>
     *  </p>
     */
    public FileParameter rulesFileName;

    /**
     *  One of the terms listed in the corresponding fuzzify xml element
     *  in the file named by the <i>rulesFileName</i> parameter.
     *  A term specifies the particular type can describes a fuzzy
     *  component.
     *  For example: The file <code>rules.xml</code> should contain
     *  <pre>
     *  <FUZZIFY NAME="rules">
     *    &lt;TERM NAME="Solar" POINTS="1 0 0 2" /&gt;
     *    &lt;TERM NAME="Wind" POINTS="0 0 0 1" /&gt;
     *    &lt;TERM NAME="Default" POINTS="2 0 0 3" /&gt;
     *  </FUZZIFY>
     *  </pre>
     *  For details about the xml specification see {@link #rulesFileName}.
     */
    public Parameter componentType;

    /** The number of Inputs, which defaults to an integer with value 1. */
    public Parameter numInputs;
    // FIXME: This should be numberOfInputs

    /** The number of outputs, which defaults to an integer with value 1. */
    public Parameter numOutputs;

    // FIXME: This should be numberOfOutputs.

    ///////////////////////////////////////////////////////////////////
    ////                         public  methods                  ////

    /** Override the base class to determine which comparison is being
     *  specified.  Read the value of the comparison attribute and set
     *  the cached value appropriately.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the comparison is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numOutputs) {
            int tempCount = 0;
            if (_debugging) {
                _debug("the number of outputs has been changed.");
            }
            tempCount = Integer.parseInt(numOutputs.getExpression().trim());
            if (_debugging) {
                _debug("Number of Outputs specified: " + tempCount);
            }
            if (tempCount > _numOutputs) {
                if (_debugging) {
                    _debug(" need to create more output ports");
                    _debug("tempCount is " + tempCount
                            + " number of Outputs is " + _numOutputs);
                }
                for (int i = _numOutputs; i < tempCount; i++) {
                    try {
                        tempOutputPort = new TypedIOPort(this, "output" + i,
                                false, true);
                        tempOutputPort.setTypeEquals(BaseType.DOUBLE);
                        outputPorts.addElement(tempOutputPort);
                    } catch (NameDuplicationException e) {
                    }
                }
                _numOutputs = tempCount;

            }
        } else if (attribute == numInputs) {
            int tempCount = 0;
            if (_debugging) {
                _debug("the number of inputs has been changed.");
            }
            tempCount = Integer.parseInt(numInputs.getExpression().trim());
            if (_debugging) {
                _debug("Number of Inputs specified: " + tempCount);
            }
            if (tempCount > _numInputs) {
                if (_debugging) {
                    _debug(" need to create more input ports");
                }
                for (int i = _numInputs; i < tempCount; i++) {
                    try {
                        tempInputPort = new TypedIOPort(this, "input" + i,
                                true, false);
                        tempInputPort.setTypeEquals(BaseType.DOUBLE);
                        inputPorts.addElement(tempInputPort);
                    } catch (NameDuplicationException e) {
                    }
                }
                _numInputs = tempCount;
            }

        }

    }

    /*
     * Open the file specified by the rulesFileName Parameter, create an
     * instance of a fuzzy logic engine, and populate the engine based on the
     * information provided in the rules file.
     * @exception IllegalActionException If thrown by the base class.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) {
            _debug("rules file name is :" + rulesFileName.getExpression());
        }

        _fuzzyParser = new FuzzyParser(rulesFileName.asURL().getFile());
        ArrayList<FuzzyLogicVar> tempArray = _fuzzyParser
                .getFuzzyLogicVariableArray();

        _linguisticVariableArray = _fuzzyParser.getLinguisticVariableArray();
        String componentTypeValue = componentType.getExpression();
        FuzzyLogicVar tempvar = tempArray.get(0);
        int commaindex;
        int periodindex;
        String tempTermName;

        periodindex = rulesFileName.getExpression().indexOf('.');
        if (rulesFileName.getExpression().substring(0, periodindex)
                .equalsIgnoreCase(tempvar.name)) {
            for (int j = 0; j < tempvar.termNames.size(); j++) {
                tempTermName = tempvar.termNames.get(j);
                commaindex = tempTermName.indexOf(',');
                if (componentTypeValue.equalsIgnoreCase(tempTermName.substring(
                        0, commaindex))) {
                    _linguisticVariableArray.get(0).setInputValue(
                            Double.parseDouble(tempTermName
                                    .substring(commaindex + 1)));
                }
            }

        } else {
            _linguisticVariableArray.get(0).setInputValue(1.5);
        }

        _fuzzyEngine = new FuzzyEngine();
        for (int i = 0; i < _linguisticVariableArray.size(); i++) {
            _fuzzyEngine.register(_linguisticVariableArray.get(i));
        }
        _rules = _fuzzyParser.getRules();

    }

    /*
     * Evaluate the fuzzy logic rules specified and determine the
     * output for this component. The fuzzy logic rules are specified
     * in {@link #rulesFileName}.
     * @exception IllegalActionException If thrown by the base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("FuzzyLogicFire function called for: "
                    + rulesFileName.getExpression());
        }

        double[] myOutputs = new double[_numOutputs];
        if (_debugging) {
            _debug("number of outputs is : " + _numOutputs);
        }
        Token token = null;

        try {

            // read the values from the input ports
            // set the input values here

            for (int i = 0; i < _numInputs; i++) {
                if (inputPorts.get(i).isOutsideConnected()) {
                    if (inputPorts.get(i).hasToken(0)) {
                        if (_debugging) {
                            _debug("i is " + i);
                        }
                        token = inputPorts.get(i).get(0);
                        //FIXME:  find the right location in the linguistic variable array and set the value
                        _linguisticVariableArray.get(i).setInputValue(
                                Double.parseDouble(token.toString()));

                    }
                }
            }

            for (int i = 0; i < _rules.size(); i++) {
                _fuzzyEngine.evaluateRule(_rules.get(i));
            }

            ArrayList<Integer> indicesToDefuzzify = _fuzzyParser
                    .getIndicesToDefuzzify();

            for (int i = 0; i < indicesToDefuzzify.size(); i++) {
                myOutputs[i] = _linguisticVariableArray.get(
                        indicesToDefuzzify.get(i)).defuzzify();
            }

            if (_debugging) {
                for (int i = 0; i < _numOutputs; i++) {
                    _debug("result " + i + " is: " + myOutputs[i]);
                }
            }

        } catch (Exception ex) {
            throw new IllegalActionException(this, ex, "A problem "
                    + "occurred when attempting to evaluate the "
                    + "rules or to get the linguistic variable "
                    + "for defuzzification.");
        }

        for (int i = 0; i < _numOutputs; i++) {
            outputPorts.get(i).send(0, new DoubleToken(myOutputs[i]));
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FuzzyEngine _fuzzyEngine;
    private ArrayList<String> _rules;
    private ArrayList<LinguisticVariable> _linguisticVariableArray;
    private FuzzyParser _fuzzyParser;
    private int _numOutputs;
    private int _numInputs;
    private TypedIOPort tempInputPort;
    private TypedIOPort tempOutputPort;

    ///////////////////////////////////////////////////////////////////
    ////                       protected classes                    ////
    /** A record type used when parsing a Fuzzy Logic XML file.*/
    protected class FuzzyLogicVar {

        /**
         * Fuzzy Logic Variable Constructor
         */
        FuzzyLogicVar() {
            termNames = new ArrayList<String>();
        }

        /**The name of the fuzzy logic variable */
        String name;
        /*An array of term names. Each string in the termNames array
         * is in the format name,initialvalue  specifying a single
         * string with two values a  name and an initial value for
         * the name*/
        ArrayList<String> termNames;
    }

    /**
     * Parse an XML file containing the fuzzy logic rules.
     */
    protected class FuzzyParser extends DefaultHandler {
        /**
         * Construct a Fuzzy Parser.
         */
        FuzzyParser() {
            initialize();
        }

        /**
         * Construct a Fuzzy Parser.
         * @param filename The name of the xml file containing the fuzzy logic rules
         */
        FuzzyParser(String filename) throws IllegalActionException {

            initialize();
            FileReader r = null;
            try {
                XMLReader xr = XMLReaderFactory.createXMLReader();
                xr.setContentHandler(this);
                xr.setErrorHandler(this);
                r = new FileReader(filename);
                xr.parse(new InputSource(r));

            } catch (Exception ex) {
                throw new IllegalActionException(null, ex, "Failed to parse "
                        + filename + ".");
            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException ex) {
                        //FIXME change null to super.this but something that actually works
                        throw new IllegalActionException(null, ex,
                                "Failed to close " + filename + ".");
                    }
                }
            }
        }

        ////////////////////////////////////////////////////////////////////
        ////                         public methods                   ////

        /** Called by the SAX parser to report regular characters.
         * @param ch The array containing characters
         * @param start Is the starting point in the character array
         * @param length Is length of the character array
         * */
        public void characters(char ch[], int start, int length) {

        }

        /**
         * Called once when the SAX driver sees the end of a document, even if errors occurred.
         * */
        public void endDocument() {
            if (_debugging) {
                _debug("saw the end of the document");
            }
        }

        /** Called each time the SAX parser sees the end of an element.
         * @param uri The Namespace Uniform Resource Identifier(URI)
         * @param name Is the elements local name
         * @param qName Is the XML 1.0 name
         * */
        public void endElement(String uri, String name, String qName) {
            if ("".equals(uri)) {
                if ("FUZZIFY".equals(qName) || "DEFUZZIFY".equals(qName)) {
                    if (_debugging) {
                        _debug("saw end of fuzzify or defuzzify");
                    }
                    _startVar = false;
                    _currentIndex++;
                    if ("FUZZIFY".equals(qName)) {
                        _fuzzyLogicVariableArray.add(_fuzzyVar);
                        _fuzzyVar = new FuzzyLogicVar();
                    }
                }
                if ("RULEBLOCK".equals(qName)) {
                    if (_debugging) {
                        _debug("saw end of ruleblock");
                    }
                }
            } else {
                if (_debugging) {
                    _debug("End element:   {" + uri + "}" + name);
                }
            }
        }

        /** Return an array of fuzzy logic variables read from the xml file.
         * @return Return an array of fuzzy logic variables read from the xml file.*/
        public ArrayList<FuzzyLogicVar> getFuzzyLogicVariableArray() {
            return _fuzzyLogicVariableArray;
        }

        /** Return the array list index of the variable to be defuzzified.
         * @return Return the array list index of the variable to be defuzzified.*/
        public ArrayList<Integer> getIndicesToDefuzzify() {
            return _toDefuzzyify;
        }

        /** Return an array of linguistic variables read from the xml file.
         * @return Return an array of linguistic variables read from the xml file.*/
        public ArrayList<LinguisticVariable> getLinguisticVariableArray() {
            return _linguisticVarArray;
        }

        /** Return a string representation of the rules specified in
         *  the xml file.
         *  @return Return a string representation of the rules specified in
         *  the xml file.
         */
        public ArrayList<String> getRules() {
            return _myRules;
        }

        ////////////////////////////////////////////////////////////////////
        // Customized SAX XML Event handlers.
        ////////////////////////////////////////////////////////////////////
        /**
         * Called once when the SAX driver sees the beginning of a document.
         * */
        public void startDocument() {
            if (_debugging) {
                _debug("saw the start of the document");
            }
        }

        /** Called each time the SAX parser sees the beginning of an element.
         * @param uri The Namespace Uniform Resource Identifier(URI)
         * @param name Is the elements local name
         * @param qName Is the XML 1.0 name
         * @param atts An Attributes object
         * */
        public void startElement(String uri, String name, String qName,
                Attributes atts) {
            // Note: The method names and the parameters match those in the
            // DefaultHandler class
            String tempString;
            if ("".equals(uri)) {

                if ("VAR_INPUT".equals(qName)) {

                    int index = atts.getIndex("NAME");
                    if (_debugging) {
                        _debug(" found an input variable");
                        _debug(atts.getValue(index));
                    }

                }
                if ("VAR_OUTPUT".equals(qName)) {

                    int index = atts.getIndex("NAME");
                    if (_debugging) {
                        _debug(" found an output variable with name ");
                        _debug(atts.getValue(index));
                    }
                }

                if ("FUZZIFY".equals(qName) || "DEFUZZIFY".equals(qName)) {
                    if (_debugging) {
                        _debug("saw start of defuzzyfy or fuzzify");
                    }
                    _startVar = true;
                    if ("DEFUZZIFY".equals(qName)) {
                        _toDefuzzyify.add(_currentIndex);
                    }
                    int index = atts.getIndex("NAME");
                    if ("FUZZIFY".equals(qName)) {
                        _fuzzyVar.name = atts.getValue(index);
                    }
                    _linguisticVarArray.add(new LinguisticVariable(atts
                            .getValue(index)));

                }
                if ("TERM".equals(qName)) {
                    if (_startVar == true) {
                        String localName = atts.getValue(0);
                        StringTokenizer st = new StringTokenizer(
                                atts.getValue(1), " ");

                        double a = Double.valueOf(st.nextToken().trim())
                                .doubleValue();
                        double b = Double.valueOf(st.nextToken().trim())
                                .doubleValue();
                        double c = Double.valueOf(st.nextToken().trim())
                                .doubleValue();
                        double d = Double.valueOf(st.nextToken().trim())
                                .doubleValue();
                        (_linguisticVarArray.get(_currentIndex)).add(localName,
                                a, b, c, d);

                        tempString = localName + "," + (a + d) / 2;
                        _fuzzyVar.termNames.add(tempString);
                    }

                }

                if ("RULE".equals(qName)) {
                    _myRules.add(atts.getValue(1));
                }
            } else {
                if (_debugging) {
                    _debug("Start element: {" + uri + "}" + name);
                }

            }
        }

        ////////////////////////////////////////////////////////////////////
        ///                         private methods                     ////
        private void initialize() {
            _startVar = false;
            _toDefuzzyify = new ArrayList<Integer>();
            _currentIndex = 0;
            _linguisticVarArray = new ArrayList<LinguisticVariable>();
            _myRules = new ArrayList<String>();
            _fuzzyVar = new FuzzyLogicVar();
            _fuzzyLogicVariableArray = new ArrayList<FuzzyLogicVar>();
        }

        ////////////////////////////////////////////////////////////////////
        ////                         private variables                 ////
        private int _currentIndex;
        private boolean _debugging = false;

        private ArrayList<FuzzyLogicVar> _fuzzyLogicVariableArray;
        private FuzzyLogicVar _fuzzyVar;
        private ArrayList<LinguisticVariable> _linguisticVarArray;
        private ArrayList<String> _myRules;
        private boolean _startVar;
        private ArrayList<Integer> _toDefuzzyify;
    }
}

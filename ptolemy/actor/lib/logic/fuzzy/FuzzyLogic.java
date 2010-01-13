/* An actor that implements fuzzy logic operation.

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

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;
import fuzzy.FuzzyEngine;
import fuzzy.LinguisticVariable;


/**
An actor that implements fuzzy logic operation.  It has four inputs and
four outputs.  Neither inputs nor outputs are multiports. The first
input and output are of type double.  The remaining three inputs are
type string.  If no input token is avaliable, the actor proceeds with
its defuzzification and produces an output.

<p>If there is input the defuzzfied value is
added(double)/appended(string) to the input and produced as output. If
there is no input the defuzzified value is produced on the output.

@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 8.1
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */

public class FuzzyLogic extends TypedAtomicActor{ 
    /**
     *  Construct a fuzzy logic actor and set a default rule file name
     *  and a default component type.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @throws NameDuplicationException
     *  @throws IllegalActionException
     */

    public FuzzyLogic(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);

        rulesFileName = new FileParameter(this, "rulesFileName");
        rulesFileName.setExpression("rules.xml");

        componentType = new Parameter(this, "componentType");
        componentType.setExpression("dummyType");

        riskInput = new TypedIOPort(this, "riskInput", true, false);
        riskInput.setMultiport(false);
        riskInput.setTypeEquals(BaseType.STRING);
        costInput = new TypedIOPort(this, "costInput", true, false);
        costInput.setMultiport(false);
        costInput.setTypeEquals(BaseType.DOUBLE);
        massInput = new TypedIOPort(this, "massInput", true, false);
        massInput.setMultiport(false);
        massInput.setTypeEquals(BaseType.STRING);


        risk = new TypedIOPort(this, "risk", false, true);
        risk.setMultiport(false);
        risk.setTypeEquals(BaseType.STRING);
        cost = new TypedIOPort(this, "cost", false, true);
        cost.setMultiport(false);
        cost.setTypeEquals(BaseType.DOUBLE);
        mass = new TypedIOPort(this, "mass", false, true);
        mass.setMultiport(false);
        mass.setTypeEquals(BaseType.STRING);
        fuzzyEngine = null;
        linguisticVarArray = null;
        myParser = null;
        rules = null;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public  methods                  ////

    /*
     * Open the file specified in the rulesFileName Parameter, create an
     * instance of a fuzzy logic engine and populate the engine based on the
     * information provided in the rules file
     */
    public void preinitialize() throws IllegalActionException {
        try {
            new File("ptolemy/actor/lib/logic/fuzzy/"
                    + rulesFileName.getExpression());

            if (_debugging) {
                _debug("rules file name is :" + rulesFileName.getExpression());
            }

            myParser = new FuzzyParser(rulesFileName.getExpression());
            linguisticVarArray = myParser.getLinguisticVariableArray();
            String compType = componentType.getExpression();
            if (rulesFileName.getExpression().equalsIgnoreCase("power")) {
                if (compType.equalsIgnoreCase("wind")) {
                    linguisticVarArray.get(0).setInputValue(0.7);
                } else if (compType.equalsIgnoreCase("solar")) {
                    linguisticVarArray.get(0).setInputValue(1.5);
                } else {
                    linguisticVarArray.get(0).setInputValue(2.5);
                }
            } else if (rulesFileName.getExpression().equalsIgnoreCase(
            "propulsion")) {
                if (compType.equalsIgnoreCase("electrical")) {
                    linguisticVarArray.get(0).setInputValue(0.5);
                } else {
                    linguisticVarArray.get(0).setInputValue(1.5);
                }
            } else if (rulesFileName.getExpression().equalsIgnoreCase(
            "antenna")) {
                if (compType.equalsIgnoreCase("high gain")) {
                    linguisticVarArray.get(0).setInputValue(1.5);
                } else {
                    linguisticVarArray.get(0).setInputValue(0.5);
                }
            } else {
                linguisticVarArray.get(0).setInputValue(1.5);
            }

            fuzzyEngine = new FuzzyEngine();
            for (int i = 0; i < linguisticVarArray.size(); i++) {
                fuzzyEngine.register(linguisticVarArray.get(i));
            }
            rules = myParser.getRules();

        } catch (Exception e) {
            // this may not be the ideal way to do this but lets make due with
            // it for now
            e.printStackTrace();
        }
    }

    /*
     * Evaluates the Fuzzy Logic Rules specified and determines the output for
     * this component
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_debugging) {
            _debug("FuzzyLogicFire function called for: "
                    + rulesFileName.getExpression());
        }

        double result = 0;
        String myRisk = " ";
        String myMass = " ";
        Token dToken = null;

        try {
            for (int i = 0; i < rules.size(); i++) {
                fuzzyEngine.evaluateRule(rules.get(i));
            }

            // here we simply assume that we need to defuzzify the last
            // linguistic variable
            result += linguisticVarArray.get(myParser.getIndexToDefuzzify())
            .defuzzify();


            myRisk = componentType.getExpression()
            + " "
            + rulesFileName.getExpression().substring(0,
                    rulesFileName.getExpression().length() - 4)
                    + " risk is medium."
                    + StringUtilities.getProperty("line.separator");
            ;
            myMass = componentType.getExpression()
            + " "
            + rulesFileName.getExpression().substring(0,
                    rulesFileName.getExpression().length() - 4)
                    + " mass is medium."
                    + StringUtilities.getProperty("line.separator");
            ;
            if (_debugging) {
                _debug("result is: " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (costInput.isOutsideConnected()) {
            if (costInput.hasToken(0)) {
                dToken = costInput.get(0);
                result += Double.valueOf(dToken.toString());

            }
        }

        if (riskInput.isOutsideConnected()) {
            if (riskInput.hasToken(0)) {
                dToken = riskInput.get(0);
                myRisk += dToken.toString();
                while (myRisk.contains("\"")) {
                    myRisk = myRisk.replace('\"', ' ');
                }
            }
        }

        if (massInput.isOutsideConnected()) {
            if (massInput.hasToken(0)) {
                dToken = massInput.get(0);
                myMass += dToken.toString();
                while (myMass.contains("\"")) {
                    myMass = myMass.replace('\"', ' ');
                }
            }

        }

        cost.send(0, new DoubleToken(result));
        risk.send(0, new StringToken(myRisk));
        mass.send(0, new StringToken(myMass));


    }

    /**
     * Clone the actor into the specified workspace. This calls the base class
     * and then sets the <code>init</code> and <code>step</code> public members
     * to the parameters of the new actor.
     * 
     * @param workspace
     *            The workspace for the new object.
     * @return A new actor.
     * @exception CloneNotSupportedException
     *                If a derived class contains an attribute that cannot be
     *                cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FuzzyLogic newObject = (FuzzyLogic) super.clone(workspace);
        // this was copied from RAMP fill in with details later
        return newObject;
    }

    /** An input port that contains the level of risk associated
     * with upstream components.
     * In this context, risk is the risk that a failure will
     * occur. The default type of this port is a String. The only 
     * meaningful values are "high", "medium" and "low". 
     */ 
    public TypedIOPort riskInput;
    /** An input port that contains the cost associated
     * with upstream components.
     * The default type of this port is a double. 
     */ 
    public TypedIOPort costInput;

    /** An input port that contains the cost associated
     * with upstream components.
     * The default type of this port is a String. Currently the only 
     * meaningful values are "high", "medium" and "low", however they
     * could be changed to doubles in the future. 
     */ 
    public TypedIOPort massInput;


    /** An output port that specifies the level of risk associated
     * with using a component or combination of components.
     * In this context, risk is the risk that a failure will
     * occur. The default type of this port is a String. The only 
     * meaningful values are "high", "medium" and "low". 
     */
    public TypedIOPort risk;

    /** An output port that specifies the mass associated
     * with using a component or combination of components.
     * The default type of this port is a String. Currently the only 
     * meaningful values are "high", "medium" and "low", however they
     * could be changed to doubles in the future. 
     */ 
    public TypedIOPort mass;

    /** An out port that specifies the cost associated
     * with using a component or combination of components.
     * The default type of this port is a double. 
     */
    public TypedIOPort cost;

    /**
     * The name of the file containing the xml specification for the component.
     * The default value is a file name <code>rules.xml</code>. 
     * The file is expected to be specified in XML/FCL Fuzzy Control Language XML. 
     * Details on this schema can be found at http://www.havana7.com/dotfuzzy/format.aspx.
     * 
     * The file named by this parameter should have a fuzzify xml element with the same name as 
     * the base name of the xml file. For example: The file <code>rules.xml</code> should contain
     *  <pre>
     *  <FUZZIFY NAME="rules">
     *   ... 
     *  </FUZZIFY>
     *  </pre> 
     *  
     */
    public FileParameter rulesFileName;

    /**
     * One of the terms listed in the corresponding fuzzify xml element
     *  in the file named by the <i>rulesFileName</i> parameter.
     *  For example: The file <code>rules.xml</code> should contain
     *  <pre>
     *  <FUZZIFY NAME="rules">
     *    <TERM NAME="Solar" POINTS="1 0 0 2" />
     *    <TERM NAME="Wind" POINTS="0 0 0 1" />
     *    <TERM NAME="Default" POINTS="2 0 0 3" />
     *  </FUZZIFY>
     *  </pre> 
     *  For details about the xml specification see {@link #rulesFileName}.
     */
    public Parameter componentType;


    public PortParameter inc;
    ////////////////////////////////////////////////////////////////////
    ////                         private variables                  ////
    private FuzzyEngine fuzzyEngine;
    private ArrayList<String> rules;
    private ArrayList<LinguisticVariable> linguisticVarArray;
    private FuzzyParser myParser;
}

/**
This class parses an XML file containing the fuzzy logic rules
 **/
class FuzzyParser extends DefaultHandler {
    private boolean _debugging = false;
    private boolean startVar;
    private int toDefuzzyify;
    private int currentIndex;
    private ArrayList<LinguisticVariable> linguisticVarArray;
    private ArrayList<String> rules;

    FuzzyParser() {
        startVar = false;
        toDefuzzyify = -1;
        currentIndex = 0;
        linguisticVarArray = new ArrayList<LinguisticVariable>();
        rules = new ArrayList<String>();
    }

    FuzzyParser(String filename) {
        startVar = false;
        toDefuzzyify = -1;
        currentIndex = 0;
        linguisticVarArray = new ArrayList<LinguisticVariable>();
        rules = new ArrayList<String>();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            FileReader r = new FileReader("ptolemy/actor/lib/logic/fuzzy/"
                    + filename);
            xr.parse(new InputSource(r));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Returns the array list index of the variable to be defuzzified
     * */
    int getIndexToDefuzzify() {
        return toDefuzzyify;
    }

    /* Returns an array of linguistic vairables read from the xml file
     * 
     * */

    ArrayList<LinguisticVariable> getLinguisticVariableArray() {
        return linguisticVarArray;

    }
    /* Returns a string representation of the rules specified in the xml file
     * */
    ArrayList<String> getRules() {
        return rules;
    }

    // //////////////////////////////////////////////////////////////////
    // Customized XML Event handlers.
    // //////////////////////////////////////////////////////////////////

    public void startDocument() {

    }

    public void endDocument() {

    }

    public void startElement(String uri, String name, String qName,
            Attributes atts) {

        if ("".equals(uri)) {
            if ("FUZZIFY".equals(qName) || "DEFUZZIFY".equals(qName)) {
                startVar = true;
                if ("DEFUZZIFY".equals(qName)) {
                    toDefuzzyify = currentIndex;
                }
                int index = atts.getIndex("NAME");
                linguisticVarArray.add(new LinguisticVariable(atts
                        .getValue(index)));
            }
            if ("TERM".equals(qName)) {
                if (startVar == true) {
                    String localName = atts.getValue(0);
                    StringTokenizer st = new StringTokenizer(atts.getValue(1),
                    " ");

                    double a = Double.valueOf(st.nextToken().trim())
                    .doubleValue();
                    double b = Double.valueOf(st.nextToken().trim())
                    .doubleValue();
                    double c = Double.valueOf(st.nextToken().trim())
                    .doubleValue();
                    double d = Double.valueOf(st.nextToken().trim())
                    .doubleValue();
                    (linguisticVarArray.get(currentIndex)).add(localName, a, b,
                            c, d);
                }
            }

            if ("RULE".equals(qName)) {
                rules.add(atts.getValue(1));
            }
        } else {
            if(_debugging){
                System.out.println("Start element: {" + uri + "}" + name);
            }

        }
    }

    public void endElement(String uri, String name, String qName) {
        if ("".equals(uri)) {
            if ("FUZZIFY".equals(qName) || "DEFUZZIFY".equals(qName)) {
                startVar = false;
                currentIndex++;
            }
            if ("RULEBLOCK".equals(qName)) {
            }
        } else {
            if(_debugging){
                System.out.println("End element:   {" + uri + "}" + name);
            }
        }
    }

    public void characters(char ch[], int start, int length) {

    }
}

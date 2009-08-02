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

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;
import ptolemy.actor.lib.logic.fuzzy.FuzzyEngine.fuzzy.FuzzyEngine;
import ptolemy.actor.lib.logic.fuzzy.FuzzyEngine.fuzzy.LinguisticVariable;



/**
This actor implements fuzzy logic operation.  It has four inputs and four outputs. 
Neither inputs nor outputs are multiports. The first input and output are of type double.
The remaining three inputs are type string.
If no input token is avaliable, the actor proceeds with it's defuzzification
and produces an output.

If there is input the defuzzfied value is added(double)/appended(string) to the input and 
produced as output. If there is no input the defuzzified value is produced on the output.

@author Shanna-Shaye Forbes
@version $Id$
@since Ptolemy II 0.4
@Pt.ProposedRating red (sssf)
@Pt.AcceptedRating red (sssf)
 */

public class FuzzyLogic extends Transformer {
    /**
     *  Construct a fuzzy logic actor and set a default rule file name
     *  and a default component type
     *  @param container The container.
     *  @param name The name of this actor within the container.
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */

    public FuzzyLogic(CompositeEntity container, String name)
    throws NameDuplicationException, IllegalActionException {
        super(container, name);

        rulesFileName = new Parameter(this, "rulesFileName");
        rulesFileName.setExpression("rules.xml");

        componentType = new Parameter(this, "componentType");
        componentType.setExpression("dummyType");

        value = new Parameter(this, "value");

        output.setTypeEquals(BaseType.DOUBLE);
        output.setMultiport(false);

        input.setMultiport(false);
        input.setTypeEquals(BaseType.DOUBLE);

        inRisk = new TypedIOPort(this, "inRisk", true, false);
        inRisk.setMultiport(false);
        inRisk.setTypeEquals(BaseType.STRING);
        inCost = new TypedIOPort(this, "inCost", true, false);
        inCost.setMultiport(false);
        inCost.setTypeEquals(BaseType.STRING);
        inMass = new TypedIOPort(this, "inMass", true, false);
        inMass.setMultiport(false);
        inMass.setTypeEquals(BaseType.STRING);

        risk = new TypedIOPort(this, "risk", false, true);
        risk.setMultiport(false);
        risk.setTypeEquals(BaseType.STRING);
        mass = new TypedIOPort(this, "mass", false, true);
        mass.setMultiport(false);
        mass.setTypeEquals(BaseType.STRING);
        cost = new TypedIOPort(this, "cost", false, true);
        cost.setMultiport(false);
        cost.setTypeEquals(BaseType.STRING);
    }

    /*
     * Open the file specified in the rulesFileName Parameter, create an
     * instance of a fuzzy logic engine and populate the engine based on the
     * information provided in the rules file
     */
    public void preinitialize() throws IllegalActionException {
        try {
            new File("ptolemy/actor/lib/logic/fuzz/"
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
            "propulsion")) {
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
        String myCost = " ";
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
            myCost = componentType.getExpression()
            + " "
            + rulesFileName.getExpression().substring(0,
                    rulesFileName.getExpression().length() - 4)
                    + " cost is medium."
                    + StringUtilities.getProperty("line.separator");
            ;
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
        if (inCost.isOutsideConnected()) {
            if (inCost.hasToken(0)) {
                dToken = inCost.get(0);
                myCost += dToken.toString();
                while (myCost.contains("\"")) {
                    myCost = myCost.replace('\"', ' ');
                }
            }
        }

        if (inRisk.isOutsideConnected()) {
            if (inRisk.hasToken(0)) {
                dToken = inRisk.get(0);
                myRisk += dToken.toString();
                while (myRisk.contains("\"")) {
                    myRisk = myRisk.replace('\"', ' ');
                }
            }
        }

        if (inMass.isOutsideConnected()) {
            if (inMass.hasToken(0)) {
                dToken = inMass.get(0);
                myMass += dToken.toString();
                while (myMass.contains("\"")) {
                    myMass = myMass.replace('\"', ' ');
                }
            }

        }

        if (input == null) {
            if (_debugging) {
                _debug("input is null");
            }
        } else if (input.isOutsideConnected()) {
            if (input.hasToken(0)) {

                dToken = input.get(0);
                if (_debugging) {
                    _debug("input from port has value: " + dToken.toString());
                }
                result += Double.valueOf(dToken.toString());
            }

        }

        myCost = "\"" + myCost + "\"";
        myRisk = "\"" + myRisk + "\"";
        myMass = "\"" + myMass + "\"";
        value.setExpression(myCost);
        System.out.println("cost just output " + value.toString());
        cost.send(0, value.getToken());
        value.setExpression(myRisk);
        System.out.println("risk has data: " + myRisk);
        System.out.println("risk just output" + value.toString());
        risk.send(0, value.getToken());

        value.setExpression(myMass);
        mass.send(0, value.getToken());
        value.setExpression(Double.toString(result));
        output.send(0, value.getToken());
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

    public TypedIOPort inRisk;
    public TypedIOPort inCost;
    public TypedIOPort inMass;

    public TypedIOPort risk;
    public TypedIOPort mass;
    public TypedIOPort cost;

    public Parameter rulesFileName;
    public Parameter componentType;
    public Parameter value;
    public PortParameter inc;
    private FuzzyEngine fuzzyEngine;
    private ArrayList<String> rules;
    private ArrayList<LinguisticVariable> linguisticVarArray;
    private FuzzyParser myParser;
}

/**
This class parses an XML file containing the fuzzy logic rules
 **/
class FuzzyParser extends DefaultHandler {
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
                    + filename.toString());
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
            if ("RULEBLOCK".equals(qName)) {
            }
            if ("RULE".equals(qName)) {
                rules.add(atts.getValue(1));
            }
        } else {
            System.out.println("Start element: {" + uri + "}" + name);

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
            System.out.println("End element:   {" + uri + "}" + name);
        }
    }

    public void characters(char ch[], int start, int length) {
        /*
         * for (int i = start; i < start + length; i++) { switch (ch[i]) { case
         * '\\': System.out.print("\\\\"); break; case '"':
         * System.out.print("\\\""); break; case '\n': System.out.print("\\n");
         * break; case '\r': System.out.print("\\r"); break; case '\t':
         * System.out.print("\\t"); break; default: System.out.print(ch[i]);
         * break; } } System.out.print("\"\n");
         */
    }
}

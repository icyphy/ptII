/* Attribute that defines a concept function with a boolean expression.

 Copyright (c) 2003-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies;

import java.util.LinkedList;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// ConceptFunctionDefinitionAttribute

/** Attribute that defines a concept function with a boolean expression.
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ConceptFunctionDefinitionAttribute extends Attribute {

    /** Construct the ConceptFunctionDefinitionAttribute attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ConceptFunctionDefinitionAttribute(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        if (!(container instanceof OntologySolverModel)) {
            throw new IllegalActionException(this, "Can only instantiate a "
                    + " ConceptFunctionDefinitionAttribute inside of an "
                    + " OntologySolverModel.");
        }

        conceptFunctionName = new StringAttribute(this, "conceptFunctionName");
        conceptFunctionName.setExpression("expressionConceptFunction");

        numberOfArguments = new Parameter(this, "numberOfArguments");
        numberOfArguments.setTypeEquals(BaseType.INT);
        numberOfArguments.setExpression("1");

        // By default do not assume the function is monotonic.
        functionIsMonotonic = new Parameter(this, "functionIsMonotonic");
        functionIsMonotonic.setTypeEquals(BaseType.BOOLEAN);
        functionIsMonotonic.setExpression("false");

        outputRangeOntologyName = new StringParameter(this,
                "outputRangeOntologyName");
        outputRangeOntologyName.setExpression("");

        conceptFunctionExpression = new StringParameter(this,
                "conceptFunctionExpression");
        conceptFunctionExpression.setExpression("");
        TextStyle style = new TextStyle(conceptFunctionExpression, "_style");
        style.height.setExpression("5");
        style.width.setExpression("80");

        _argumentNames = new LinkedList<StringAttribute>();
        _argumentDomainOntologies = new LinkedList<StringParameter>();

        _conceptFunction = null;

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"20\" "
                + "style=\"fill:white\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "f(c0, c1, .. , cn)</text></svg>");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** The string that represents the boolean expression of the concept function. */
    public StringParameter conceptFunctionExpression;

    /** The name to identify the expression concept function defined by this attribute. */
    public StringAttribute conceptFunctionName;

    /** Parameter to select whether the concept function defined by this
     *  attribute must be monotonic.
     */
    public Parameter functionIsMonotonic;

    /** The number of arguments for the concept function. */
    public Parameter numberOfArguments;

    /** The name of the ontology that specifies the range of concepts
     *  for the concept function output.
     */
    public StringParameter outputRangeOntologyName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the attributeChanged method so that if the number of arguments for the
     *  concept function changes, the attribute interface adds or removes fields for those
     *  arguments.
     *  @param attribute The attribute that has been changed.
     *  @throws IllegalActionException If there is a problem changing the attribute.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numberOfArguments) {
            IntToken numArgsToken = (IntToken) numberOfArguments.getToken();
            int numArgs = numArgsToken.intValue();

            // Collect all the existing argument fields before checking if we need
            // to add or remove any.  This is required for when the attribute is
            // loaded from a MoML file.
            _argumentNames.clear();
            for (Object argNameAttribute : attributeList(StringAttribute.class)) {
                if (((StringAttribute) argNameAttribute).getName().startsWith(
                        "arg")) {
                    _argumentNames.add((StringAttribute) argNameAttribute);
                }
            }
            _argumentDomainOntologies.clear();
            for (Object argOntologyParameter : attributeList(StringParameter.class)) {
                if (((StringParameter) argOntologyParameter).getName()
                        .startsWith("arg")) {
                    _argumentDomainOntologies
                            .add((StringParameter) argOntologyParameter);
                }
            }
            int currentArgs = _argumentNames.size();

            // Increase the argument fields when the number of arguments increases.
            if (numArgs > currentArgs) {
                try {
                    for (int i = currentArgs; i < numArgs; i++) {
                        StringAttribute newArg = new StringAttribute(this,
                                "arg" + i + "Name");
                        newArg.setExpression("arg" + i);

                        StringParameter newArgDomainOntology = new StringParameter(
                                this, "arg" + i + "DomainOntologyName");
                        newArgDomainOntology.setExpression("");

                        _argumentNames.add(newArg);
                        _argumentDomainOntologies.add(newArgDomainOntology);
                    }
                } catch (NameDuplicationException nameDupEx) {
                    throw new IllegalActionException(
                            this,
                            nameDupEx,
                            "Error when trying to increase the "
                                    + "number of arguments for "
                                    + this
                                    + ". A new argument parameter has the same name as "
                                    + "an existing argument parameter.");
                }

                // Decrease the argument fields when the number of arguments Decreases.
            } else if (numArgs < currentArgs) {
                try {
                    for (int i = currentArgs; i > numArgs; i--) {
                        StringAttribute deletedArg = _argumentNames
                                .removeLast();
                        deletedArg.setContainer(null);

                        StringParameter deletedArgDomainOntology = _argumentDomainOntologies
                                .removeLast();
                        deletedArgDomainOntology.setContainer(null);
                    }
                } catch (NameDuplicationException nameDupEx) {
                    throw new IllegalActionException(this, nameDupEx,
                            "Error when trying to remove "
                                    + "argument parameters from " + this + ".");
                }
            }

            if (_argumentNames.size() != _argumentDomainOntologies.size()) {
                throw new IllegalActionException(
                        this,
                        "Error when changing the number of arguments for "
                                + this
                                + ". The number of argument names and the number of argument domain ontologies is "
                                + "different, and this should never happen.");
            }
        }

        super.attributeChanged(attribute);
    }

    /** Return the concept function defined by this attribute's expression.
     *  @return The concept function.
     *  @throws IllegalActionException If there is an error updating the conceptFunction.
     */
    public ExpressionConceptFunction getConceptFunction()
            throws IllegalActionException {
        if (_conceptFunction == null
                || workspace().getVersion() != _functionVersion) {
            _updateConceptFunction();
            _functionVersion = workspace().getVersion();
        }

        return _conceptFunction;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Update the concept function with the most recent values of the attribute.
     *  @throws IllegalActionException If there is an error updating the conceptFunction.
     */
    protected void _updateConceptFunction() throws IllegalActionException {
        int numArgs = 0;
        Ontology outputRangeOntology = null;

        IntToken numArgsToken = (IntToken) numberOfArguments.getToken();
        numArgs = numArgsToken.intValue();

        outputRangeOntology = (Ontology) ((CompositeEntity) getContainer())
                .getEntity(outputRangeOntologyName.getExpression());
        if (outputRangeOntology == null) {
            throw new IllegalActionException(this,
                    "The specified outputRangeOntology "
                            + outputRangeOntologyName
                            + " could not be found in the model.");
        }

        Ontology[] argDomainOntologies = new Ontology[numArgs];
        int index = 0;
        for (StringParameter ontologyName : _argumentDomainOntologies) {
            argDomainOntologies[index] = (Ontology) ((CompositeEntity) getContainer())
                    .getEntity(ontologyName.getExpression());
            if (argDomainOntologies[index] == null) {
                throw new IllegalActionException(this,
                        "The specified domain ontology " + ontologyName
                                + " for argument " + index
                                + " could not be found in the model.");
            }
            index++;
        }

        String[] argNameArray = new String[numArgs];
        index = 0;
        for (StringAttribute argName : _argumentNames) {
            argNameArray[index++] = argName.getExpression();
        }

        if (_conceptFunction == null) {
            _conceptFunction = new ExpressionConceptFunction(
                    conceptFunctionName.getExpression(), numArgs,
                    argDomainOntologies, outputRangeOntology, argNameArray,
                    conceptFunctionExpression.getExpression(),
                    (OntologySolverModel) getContainer());
        } else {
            _conceptFunction.updateFunctionParameters(conceptFunctionName
                    .getExpression(), numArgs, argDomainOntologies,
                    outputRangeOntology, argNameArray,
                    conceptFunctionExpression.getExpression(),
                    (OntologySolverModel) getContainer());
        }

        if (((BooleanToken) functionIsMonotonic.getToken()).booleanValue()) {
            if (!_conceptFunction.isMonotonic()) {
                throw new IllegalActionException(this,
                        "The defined concept function is set to "
                                + "be monotonic but it is not.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of argument names for the concept function
     *  that are used in the function expression.
     */
    protected LinkedList<StringAttribute> _argumentNames;

    /** The list of domain ontology names for the concept function
     *  argument list.
     */
    protected LinkedList<StringParameter> _argumentDomainOntologies;

    /** The concept function defined by this attribute. */
    protected ExpressionConceptFunction _conceptFunction;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The workspace version at which the cached expression
     *  concept function was valid.
     */
    private long _functionVersion = -1L;
}

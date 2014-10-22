/* Attribute that defines a concept function with a boolean expression.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
import java.util.List;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ExpressionConceptFunctionDefinitionAttribute

/** Attribute that defines a concept function with a boolean expression.
 *
 *  @see ExpressionConceptFunction
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Green (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ExpressionConceptFunctionDefinitionAttribute extends
        ConceptFunctionDefinitionAttribute {

    /** Construct the ExpressionConceptFunctionDefinitionAttribute attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ExpressionConceptFunctionDefinitionAttribute(
            CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // By default the number of arguments for the concept function is fixed.
        numberOfArgumentsIsFixed = new Parameter(this,
                "numberOfArgumentsIsFixed");
        numberOfArgumentsIsFixed.setTypeEquals(BaseType.BOOLEAN);
        numberOfArgumentsIsFixed.setToken(BooleanToken.TRUE);

        // By default do not assume the function is monotonic.
        constrainFunctionToBeMonotonic = new Parameter(this,
                "constrainFunctionToBeMonotonic");
        constrainFunctionToBeMonotonic.setTypeEquals(BaseType.BOOLEAN);
        constrainFunctionToBeMonotonic.setToken(BooleanToken.FALSE);

        outputRangeOntologyName = new StringParameter(this,
                "outputRangeOntologyName");

        argumentNames = new Parameter(this, "argumentNames");
        argumentNames.setTypeEquals(new ArrayType(BaseType.STRING));

        argumentDomainOntologies = new Parameter(this,
                "argumentDomainOntologies");
        argumentDomainOntologies.setTypeEquals(new ArrayType(BaseType.STRING));

        conceptFunctionExpression = new StringParameter(this,
                "conceptFunctionExpression");
        TextStyle style = new TextStyle(conceptFunctionExpression, "_style");
        style.height.setExpression("5");
        style.width.setExpression("80");
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** The parameter that holds the array of strings that name
     *  the domain ontologies for the arguments for the concept function.
     */
    public Parameter argumentDomainOntologies;

    /** The parameter that holds the array of strings that name
     *  the arguments for the concept function.
     */
    public Parameter argumentNames;

    /** The string that represents the boolean expression of the concept function. */
    public StringParameter conceptFunctionExpression;

    /** Parameter to select whether the concept function defined by this
     *  attribute must be monotonic.
     */
    public Parameter constrainFunctionToBeMonotonic;

    /** Parameter to select whether the number of arguments
     *  for the concept function is fixed.
     */
    public Parameter numberOfArgumentsIsFixed;

    /** The name of the ontology that specifies the range of concepts
     *  for the concept function output.
     */
    public StringParameter outputRangeOntologyName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the concept function defined by this attribute's expression.
     *  @return The concept function.
     *  @exception IllegalActionException If there is an error
     *   creating the conceptFunction.
     */
    @Override
    public ExpressionConceptFunction createConceptFunction()
            throws IllegalActionException {
        if (((BooleanToken) numberOfArgumentsIsFixed.getToken()).booleanValue()) {
            if (((ArrayToken) argumentDomainOntologies.getToken()).length() != ((ArrayToken) argumentNames
                    .getToken()).length()) {
                throw new IllegalActionException(
                        this,
                        "The concept function is specified to have a fixed"
                                + " number of arguments, but the lengths of the arrays"
                                + " for the argument names ("
                                + ((ArrayToken) argumentNames.getToken())
                                        .length()
                                + ") and argument domain ontologies("
                                + ((ArrayToken) argumentDomainOntologies
                                        .getToken()).length()
                                + ") are different.");
            }
        }

        Ontology outputRangeOntology = null;
        outputRangeOntology = (Ontology) ((CompositeEntity) getContainer())
                .getEntity(outputRangeOntologyName.getExpression());
        if (outputRangeOntology == null) {
            throw new IllegalActionException(this,
                    "The specified outputRangeOntology "
                            + outputRangeOntologyName
                            + " could not be found in the model.");
        }

        List<Ontology> argDomainOntologies = new LinkedList<Ontology>();
        Token[] argDomainNames = ((ArrayToken) argumentDomainOntologies
                .getToken()).arrayValue();
        for (Token ontologyName : argDomainNames) {
            Ontology ontology = (Ontology) ((CompositeEntity) getContainer())
                    .getEntity(((StringToken) ontologyName).stringValue());
            if (ontology == null) {
                throw new IllegalActionException(this,
                        "The specified domain ontology " + ontologyName
                                + " for a function argument"
                                + " could not be found in the model.");
            } else {
                argDomainOntologies.add(ontology);
            }
        }

        List<String> argNameList = new LinkedList<String>();
        Token[] argNames = ((ArrayToken) argumentNames.getToken()).arrayValue();
        for (Token argName : argNames) {
            argNameList.add(((StringToken) argName).stringValue());
        }

        ExpressionConceptFunction newConceptFunction = new ExpressionConceptFunction(
                getName(),
                ((BooleanToken) numberOfArgumentsIsFixed.getToken())
                        .booleanValue(), argDomainOntologies,
                outputRangeOntology, argNameList,
                conceptFunctionExpression.getExpression(),
                (OntologySolverModel) getContainer(), null);

        if (((BooleanToken) constrainFunctionToBeMonotonic.getToken())
                .booleanValue()) {
            if (!newConceptFunction.isMonotonic()) {
                throw new IllegalActionException(this,
                        "The defined concept function is constrained to "
                                + "be monotonic but it is not.");
            }
        }

        return newConceptFunction;
    }
}

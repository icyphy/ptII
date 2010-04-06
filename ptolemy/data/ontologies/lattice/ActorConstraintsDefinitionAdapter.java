/* A class that creates lattice-based ontology adapter from
 * a model-based actor constraint definition attribute.
 * 
 * Copyright (c) 2010 The Regents of the University of California. All
 * rights reserved.
 *
 * Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 * 
 */
package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.ExpressionConceptFunction;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ActorConstraintsDefinitionAdapter.java

/**
 * A class that creates lattice-based ontology adapter from
 * a model-based actor constraint definition attribute.
 * 
 * @author Charles Shelton
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cshelton)
 * @Pt.AcceptedRating Red (cshelton)
 */
public class ActorConstraintsDefinitionAdapter extends LatticeOntologyAdapter {

    /** Construct the lattice ontology adapter for the given component
     *  and property lattice.
     *  @param solver The specified lattice-based ontology solver.
     *  @param component The given component.
     *  @param constraintExpressions The list of constraint
     *   expressions for each port or component in the actor.
     *  @exception IllegalActionException Thrown if the adapter cannot be
     *   initialized.
     */
    public ActorConstraintsDefinitionAdapter(LatticeOntologySolver solver,
            Object component, List<StringParameter> constraintExpressions)
            throws IllegalActionException {
        // Don't use default constraints for user-defined actor constraints.
        super(solver, component, false);
        _constraintTermExpressions = constraintExpressions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The string suffix for attribute names that represent
     *  constraint definitions for actor attributes.
     */
    public static final String ATTR_SUFFIX = ActorConstraintsDefinitionAttribute.ATTR_SUFFIX;

    /** String representing an equal to constraint choice. */
    public static final String EQ = ActorConstraintsDefinitionAttribute.EQ;

    /** String representing a greater than or equal to constraint choice. */
    public static final String GTE = ActorConstraintsDefinitionAttribute.GTE;

    /** String representing that the actor port or attribute should be
     *  ignored for the ontology analysis and not have a concept
     *  assigned to it.
     */
    public static final String IGNORE = ActorConstraintsDefinitionAttribute.IGNORE;

    /** String representing a less than or equal to constraint choice. */
    public static final String LTE = ActorConstraintsDefinitionAttribute.LTE;

    /** String representing that the actor port or attribute has no
     *  constraints but should have a concept assigned to it.
     */
    public static final String NO_CONSTRAINTS = ActorConstraintsDefinitionAttribute.NO_CONSTRAINTS;

    /** The string suffix for attribute names that represent
     *  constraint definitions for actor ports.
     */
    public static final String PORT_SUFFIX = ActorConstraintsDefinitionAttribute.PORT_SUFFIX;

    /** String representing the separator character ";" between
     *  constraint expressions in the constraint expression string.
     */
    public static final String SEPARATOR = ActorConstraintsDefinitionAttribute.SEPARATOR;

    /** The length of the attribute and port suffix strings. */
    public static final int SUFFIX_LENGTH = ActorConstraintsDefinitionAttribute.SUFFIX_LENGTH;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the constraints of this component. The constraints are
     *  generated from the expressions passed in from an
     *  ActorConstraintsDefinitionAttribute that allows the user to
     *  define actor constraints in the OntologySolver model.
     *  @return The list of constraints for this component.
     *  @exception IllegalActionException If there is a problem
     *   parsing the constraint expression strings to create the actor
     *   constraints.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        for (StringParameter constraintExpression : _constraintTermExpressions) {
            if (!constraintExpression.getExpression().trim().equals(IGNORE)
                    && !constraintExpression.getExpression().trim().equals(
                            NO_CONSTRAINTS)) {

                String objName = getActorElementName(constraintExpression);
                Object actorElement = null;

                if (constraintExpression.getName().endsWith(PORT_SUFFIX)) {
                    actorElement = ((Entity) getComponent()).getPort(objName);
                } else if (constraintExpression.getName().endsWith(ATTR_SUFFIX)) {
                    actorElement = ((Entity) getComponent())
                            .getAttribute(objName);
                }
                _setConstraints(actorElement, constraintExpression
                        .getExpression());
            }
        }

        return super.constraintList();
    }

    /** Return a list of property-able ports and attributes contained
     *  by the component.  If any of the actor element expressions are
     *  set to IGNORE then they are not added to the list of
     *  property-able objects.
     *  @return The list of property-able ports and attributes.
     */
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();

        // Add all ports that are not set to IGNORE.
        for (StringParameter constraintExpression : _constraintTermExpressions) {
            if (!constraintExpression.getExpression().trim().equals(IGNORE)
                    && constraintExpression.getName().endsWith(PORT_SUFFIX)) {

                String portName = getActorElementName(constraintExpression);
                Port portToAdd = ((Entity) getComponent()).getPort(portName);
                list.add(portToAdd);
            }
        }

        // Add attributes.
        list.addAll(_getPropertyableAttributes());

        return list;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Return the inequality term representing the concept function
     *  defined by the specified string.
     *  @param functionString The string containing the expression for
     *   the concept function.
     *  @return The concept function inequality term that implements
     *   the concept function and contains the correct inequality term
     *   inputs.
     *  @exception IllegalActionException If the string cannot be
     *   correctly parsed and the concept function cannot be created.
     */
    protected ConceptFunctionInequalityTerm _getConceptFunctionTerm(
            Object actorElement, String functionString)
            throws IllegalActionException {
        ArrayList<String> argumentNameList = new ArrayList<String>();
        ArrayList<InequalityTerm> argumentList = new ArrayList<InequalityTerm>();

        for (StringParameter constraintExpression : _constraintTermExpressions) {
            if (!constraintExpression.getExpression().trim().equals(IGNORE)) {
                String actorElementName = getActorElementName(constraintExpression);

                // Have to use a Java regular expression Pattern here
                // because the String.regex() method doesn't match
                // line terminators to the '.' character, so any
                // function strings that contain multiple lines would
                // have been incorrectly parsed.
                int regexOptions = Pattern.DOTALL;
                // Note that "\\b" matches a word boundary.
                Pattern compiledRegex = Pattern.compile(".*\\b"
                        + actorElementName + "\\b.*", regexOptions);
                Matcher regexMatcher = compiledRegex.matcher(functionString);

                if (regexMatcher.matches()) {
                    argumentNameList.add(actorElementName);
                    Object argument = null;

                    if (constraintExpression.getName().endsWith(PORT_SUFFIX)) {
                        argument = ((Entity) getComponent())
                                .getPort(actorElementName);
                    } else if (constraintExpression.getName().endsWith(
                            ATTR_SUFFIX)) {
                        argument = ((Entity) getComponent())
                                .getAttribute(actorElementName);
                    }

                    if (argument == null) {
                        throw new IllegalActionException(
                                "Error parsing actor constraint function: "
                                        + "could not find the argument named "
                                        + actorElementName + " in the actor "
                                        + getComponent() + ".");
                    }
                    argumentList.add(getPropertyTerm(argument));
                }
            }
        }

        String argumentNames[] = new String[argumentNameList.size()];
        System.arraycopy(argumentNameList.toArray(), 0, argumentNames, 0,
                argumentNameList.size());

        InequalityTerm[] argumentTerms = new InequalityTerm[argumentList.size()];
        System.arraycopy(argumentList.toArray(), 0, argumentTerms, 0,
                argumentList.size());

        Ontology functionOntology = getSolver().getOntology();
        Ontology[] domainOntologies = new Ontology[argumentTerms.length];
        for (int i = 0; i < domainOntologies.length; i++) {
            domainOntologies[i] = functionOntology;
        }

        int numArgs = argumentNames.length;

        ConceptFunction function = new ExpressionConceptFunction(
                ((NamedObj) getComponent()).getName() + "_"
                        + ((NamedObj) actorElement).getName()
                        + "_ConstraintFunction", numArgs, domainOntologies,
                functionOntology, argumentNames, functionString,
                (OntologySolverModel) getSolver().getContainedModel());

        ConceptFunctionInequalityTerm functionTerm = new ConceptFunctionInequalityTerm(
                function, argumentTerms);
        return functionTerm;
    }

    /** Get the name of the element contained by the actor (either a
     *  port or an attribute) for which the specified parameter
     *  defines a constraint.
     *  @param expressionParameter The string parameter that defines
     *   the element's constraints.
     *  @return The string name of the element (either a port or an attribute).
     */
    protected String getActorElementName(StringParameter expressionParameter) {
        String elementName = expressionParameter.getName();
        elementName = elementName.substring(0, elementName.length()
                - SUFFIX_LENGTH);
        return elementName;
    }

    /** Return the list of property-able Attributes.  This list is
     *  defined by the expressions for each attribute taken from the
     *  ActorConstraintsDefinitionAttribute.  Any attribute that is
     *  set to IGNORE is not added to the list of property-able
     *  attributes.
     *  @return The list of property-able Attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = new LinkedList<Attribute>();

        // Add all attributes that are not set to IGNORE.
        for (StringParameter constraintExpression : _constraintTermExpressions) {
            if (!constraintExpression.getExpression().trim().equals(IGNORE)
                    && constraintExpression.getName().endsWith(ATTR_SUFFIX)) {

                String attributeName = getActorElementName(constraintExpression);
                Attribute attributeToAdd = ((Entity) getComponent())
                        .getAttribute(attributeName);
                result.add(attributeToAdd);
            }
        }
        return result;
    }

    /** Set the constraints for the actor attribute or port based on
     *  the parsed expression string.
     *  @param actorElement The attribute or port from the actor to be
     *  constrained.
     *  @param constraintExpressionString The expression string that
     *   is parsed to get the constraints for the actor attribute or
     *   port.
     *  @exception IllegalActionException If the constraint cannot be
     *   set due to problems parsing the expression.
     */
    protected void _setConstraints(Object actorElement,
            String constraintExpressionString) throws IllegalActionException {

        // The expression string should contain a set of ";" separated
        // constraint expressions.
        String[] singleConstraintExpressions = constraintExpressionString
                .split(SEPARATOR);

        for (String constraintString : singleConstraintExpressions) {
            constraintString = constraintString.trim();

            String constraintDir = null;
            if (constraintString.startsWith(GTE)) {
                constraintDir = GTE;
                constraintString = constraintString.substring(GTE.length())
                        .trim();
            } else if (constraintString.startsWith(LTE)) {
                constraintDir = LTE;
                constraintString = constraintString.substring(LTE.length())
                        .trim();
            } else if (constraintString.startsWith(EQ)) {
                constraintDir = EQ;
                constraintString = constraintString.substring(EQ.length())
                        .trim();
            } else {
                throw new IllegalActionException(
                        "Cannot set a constraint for the actor "
                                + getComponent() + ". Unrecognized direction: "
                                + constraintDir);
            }

            // First see if the right term is just the name of a Concept
            Object rightTerm = getSolver().getOntology().getEntity(
                    constraintString);
            String objName = null;

            // If the right term was not a concept, see if it is another element
            // in the actor (either a port or an attribute).
            if (rightTerm == null) {
                rightTerm = ((Entity) getComponent()).getPort(constraintString);
                if (rightTerm != null) {
                    objName = ((Port) rightTerm).getName();
                }
            }
            if (rightTerm == null) {
                rightTerm = ((Entity) getComponent())
                        .getAttribute(constraintString);
                if (rightTerm != null) {
                    objName = ((Attribute) rightTerm).getName();
                }
            }

            // If the right term is an attribute or port in the actor,
            // check to make sure it is not set to be ignored for the
            // ontology analysis.
            if (rightTerm != null && !(rightTerm instanceof Concept)) {
                for (StringParameter constraintExpression : _constraintTermExpressions) {
                    if (constraintExpression.getName().substring(
                            0,
                            constraintExpression.getName().length()
                                    - SUFFIX_LENGTH).equals(objName)) {
                        if (constraintExpression.getExpression().trim().equals(
                                IGNORE)) {
                            throw new IllegalActionException(
                                    "Cannot set up a constraint for "
                                            + actorElement
                                            + " in actor "
                                            + getComponent()
                                            + " because the actor element "
                                            + rightTerm
                                            + " is on the RHS of the "
                                            + "constraint  but it is set "
                                            + "to be ignored by the ontology "
                                            + "analysis.");
                        }
                    }
                }
            }

            // If the right term is neither a Concept nor an attribute
            // or port in the actor, then it must be a parseable
            // concept function definition.
            if (rightTerm == null) {
                rightTerm = _getConceptFunctionTerm(actorElement,
                        constraintString);
            }

            if (rightTerm == null) {
                throw new IllegalActionException(
                        "Could not parse constraint expression right term "
                                + "value for the actor " + getComponent()
                                + ". Right term string: " + constraintString);
            }

            if (constraintDir.equals(GTE)) {
                setAtLeast(actorElement, rightTerm);
            } else if (constraintDir.equals(LTE)) {
                setAtMost(actorElement, rightTerm);
            } else if (constraintDir.equals(EQ)) {
                if (rightTerm instanceof Concept) {
                    setEquals(actorElement, (Concept) rightTerm);
                } else {
                    setSameAs(actorElement, rightTerm);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of expressions that represent the constraint
     *  terms for each constraint in the actor.
     */
    protected List<StringParameter> _constraintTermExpressions;
}

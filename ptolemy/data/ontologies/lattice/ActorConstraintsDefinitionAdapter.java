/* A class that creates a lattice-based ontology adapter from
 * a model-based actor constraints definition attribute.
 *
 * Copyright (c) 2010-2014 The Regents of the University of California. All
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

import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.EQ;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.GTE;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.LTE;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.SEPARATOR;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.getActorElementName;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.getConstraintDirAndRHSStrings;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementAPort;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementAnAttribute;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementIgnored;
import static ptolemy.data.ontologies.lattice.ActorConstraintsDefinitionAttribute.isActorElementUnconstrained;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptFunction;
import ptolemy.data.ontologies.ConceptFunctionInequalityTerm;
import ptolemy.data.ontologies.ExpressionConceptFunction;
import ptolemy.data.ontologies.Ontology;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ActorConstraintsDefinitionAdapter.java

/**
 * A class that creates a lattice-based ontology adapter from
 * a model-based actor constraints definition attribute.
 *
 * @see ActorConstraintsDefinitionAttribute
 * @author Charles Shelton
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cshelton)
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
            ComponentEntity component,
            List<StringParameter> constraintExpressions)
                    throws IllegalActionException {
        // Don't use default constraints for user-defined actor constraints.
        super(solver, component, false);
        _constraintTermExpressions = constraintExpressions;
    }

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
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {

        for (StringParameter constraintExpression : _constraintTermExpressions) {
            if (!isActorElementIgnored(constraintExpression)
                    && !isActorElementUnconstrained(constraintExpression)) {

                String objName = getActorElementName(constraintExpression);
                NamedObj actorElement = null;

                if (isActorElementAPort(constraintExpression)) {
                    actorElement = ((ComponentEntity) getComponent())
                            .getPort(objName);
                } else if (isActorElementAnAttribute(constraintExpression)) {
                    actorElement = ((ComponentEntity) getComponent())
                            .getAttribute(objName);
                }
                if (actorElement == null) {
                    throw new InternalErrorException(
                            (getComponent() instanceof NamedObj ? ((NamedObj) getComponent())
                                    : null), null,
                            "Could not find a component or attribute named \""
                                    + objName
                                    + "\".  Thus, actorElement is null?");
                } else {
                    _setConstraints(actorElement,
                            ((StringToken) constraintExpression.getToken())
                                    .stringValue());
                }
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
    @Override
    public List<Object> getPropertyables() {
        List<Object> list = new ArrayList<Object>();

        // Add all ports that are not set to IGNORE.
        for (StringParameter constraintExpression : _constraintTermExpressions) {
            try {
                if (!isActorElementIgnored(constraintExpression)
                        && isActorElementAPort(constraintExpression)) {

                    String portName = getActorElementName(constraintExpression);
                    Port portToAdd = ((ComponentEntity) getComponent())
                            .getPort(portName);
                    list.add(portToAdd);
                }
            } catch (IllegalActionException e) {
                // FIXME: Don't know what to do here since the inherited method getPropertyables()
                // cannot throw an exception.
            }
        }
        // Add attributes.
        list.addAll(_getPropertyableAttributes());

        return list;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the inequality term representing the concept function
     *  defined by the specified string.
     *  @param actorElement The actor element for which this concept function
     *   constraint is being defined.
     *  @param functionString The string containing the expression for
     *   the concept function.
     *  @return The concept function inequality term that implements
     *   the concept function and contains the correct inequality term
     *   inputs.
     *  @exception IllegalActionException If the string cannot be
     *   correctly parsed and the concept function cannot be created.
     */
    protected ConceptFunctionInequalityTerm _getConceptFunctionTerm(
            NamedObj actorElement, String functionString)
                    throws IllegalActionException {
        ArrayList<String> argumentNameList = new ArrayList<String>();
        ArrayList<InequalityTerm> argumentList = new ArrayList<InequalityTerm>();

        // FIXME: This method needs to be modified if we want to support
        // wild cards for functions with variable argument list sizes in the future.

        for (StringParameter constraintExpression : _constraintTermExpressions) {
            if (!isActorElementIgnored(constraintExpression)) {
                String actorElementInExpressionName = getActorElementName(constraintExpression);

                // Have to use a Java regular expression Pattern here
                // because the String.regex() method doesn't match
                // line terminators to the '.' character, so any
                // function strings that contain multiple lines would
                // have been incorrectly parsed.
                int regexOptions = Pattern.DOTALL;
                // Note that "\\b" matches a word boundary.
                Pattern compiledRegex = Pattern.compile(".*\\b"
                        + actorElementInExpressionName + "\\b.*", regexOptions);
                Matcher regexMatcher = compiledRegex.matcher(functionString);

                if (regexMatcher.matches()) {
                    argumentNameList.add(actorElementInExpressionName);
                    Object argument = null;

                    if (isActorElementAPort(constraintExpression)) {
                        argument = ((ComponentEntity) getComponent())
                                .getPort(actorElementInExpressionName);
                    } else if (isActorElementAnAttribute(constraintExpression)) {
                        argument = ((ComponentEntity) getComponent())
                                .getAttribute(actorElementInExpressionName);
                    }

                    if (argument == null) {
                        throw new IllegalActionException(actorElement,
                                "Error parsing actor constraint function: "
                                        + "could not find the argument named "
                                        + actorElementInExpressionName
                                        + " in the actor " + getComponent()
                                        + ".");
                    }

                    /* 10/5/10 Charles Shelton - I don't think it is wrong for a constraint function
                     * to refer to the element being constrained.  It could be used for recursion
                     * or promoting an element to a concept value higher in the lattice than its current
                     * one based on the concept value of another element. So I am commenting out this exception.
                    if (argument.equals(actorElement)) {
                        throw new IllegalActionException(actorElement,
                                "Error parsing actor constraint function in the actor "
                                        + getComponent() + ": "
                                        + "the constraint concept function expression for "
                                        + actorElement.getName() + " cannot refer to itself in the "
                                        + "concept function.");
                    }
                     */
                    argumentList.add(getPropertyTerm(argument));
                }
            }
        }

        InequalityTerm[] argumentTerms = new InequalityTerm[argumentList.size()];
        System.arraycopy(argumentList.toArray(), 0, argumentTerms, 0,
                argumentList.size());

        Ontology functionOntology = getSolver().getOntology();
        List<Ontology> domainOntologies = new ArrayList<Ontology>(
                argumentTerms.length);
        for (InequalityTerm argumentTerm : argumentTerms) {
            domainOntologies.add(functionOntology);
        }

        ConceptFunction function = new ExpressionConceptFunction(
                ((ComponentEntity) getComponent()).getName() + "_"
                        + actorElement.getName() + "_ConstraintFunction", true,
                        domainOntologies, functionOntology, argumentNameList,
                        functionString, (OntologySolverModel) getSolver()
                        .getContainedModel(), actorElement);

        ConceptFunctionInequalityTerm functionTerm = new ConceptFunctionInequalityTerm(
                function, argumentTerms);
        return functionTerm;
    }

    /** Return the list of property-able Attributes.  This list is
     *  defined by the expressions for each attribute taken from the
     *  ActorConstraintsDefinitionAttribute.  Any attribute that is
     *  set to IGNORE is not added to the list of property-able
     *  attributes.
     *  @return The list of property-able Attributes.
     */
    @Override
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = new LinkedList<Attribute>();

        // Add all attributes that are not set to IGNORE.
        for (StringParameter constraintExpression : _constraintTermExpressions) {
            try {
                if (!isActorElementIgnored(constraintExpression)
                        && isActorElementAnAttribute(constraintExpression)) {

                    String attributeName = getActorElementName(constraintExpression);
                    Attribute attributeToAdd = ((ComponentEntity) getComponent())
                            .getAttribute(attributeName);
                    result.add(attributeToAdd);
                }
            } catch (IllegalActionException e) {
                // FIXME: Don't know what to do here since the inherited method
                // _getPropertyableAttributes() cannot throw an exception.
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
    protected void _setConstraints(NamedObj actorElement,
            String constraintExpressionString) throws IllegalActionException {

        // The expression string should contain a set of ";" separated
        // constraint expressions.
        String[] singleConstraintExpressions = constraintExpressionString
                .split(SEPARATOR);

        for (String constraintString : singleConstraintExpressions) {
            constraintString = constraintString.trim();

            List<String> dirAndRHSStrings = getConstraintDirAndRHSStrings(constraintString);
            String constraintDir = null;
            String RHSString = null;
            if (dirAndRHSStrings == null) {
                // If no direction specification is present, do the right thing
                // based on the fixed point type.
                if (getSolver().isLeastFixedPoint()) {
                    constraintDir = ">=";
                } else {
                    constraintDir = "<=";
                }
                RHSString = constraintString;
            } else {
                constraintDir = dirAndRHSStrings.get(0);
                RHSString = dirAndRHSStrings.get(1);
            }

            // First see if the right term is just the name of a Concept
            Object RHSTerm = getSolver().getOntology().getEntity(RHSString);
            String objName = null;

            // If the right term was not a concept, see if it is another element
            // in the actor (either a port or an attribute).
            if (RHSTerm == null) {
                RHSTerm = ((ComponentEntity) getComponent()).getPort(RHSString);
                if (RHSTerm != null) {
                    objName = ((Port) RHSTerm).getName();
                }
            }
            if (RHSTerm == null) {
                RHSTerm = ((ComponentEntity) getComponent())
                        .getAttribute(RHSString);
                if (RHSTerm != null) {
                    objName = ((Attribute) RHSTerm).getName();
                }
            }

            // If the right term is an attribute or port in the actor,
            // check to make sure it is not set to be ignored for the
            // ontology analysis.
            if (RHSTerm != null && !(RHSTerm instanceof Concept)) {
                for (StringParameter constraintExpression : _constraintTermExpressions) {
                    if (getActorElementName(constraintExpression).equals(
                            objName)) {
                        if (isActorElementIgnored(constraintExpression)) {
                            throw new IllegalActionException(actorElement,
                                    "Cannot set up a constraint for "
                                            + actorElement + " in actor "
                                            + getComponent()
                                            + " because the actor element "
                                            + RHSTerm
                                            + " is on the RHS of the "
                                            + "constraint but it is set "
                                            + "to be ignored by the ontology "
                                            + "analysis.");
                        }
                    }
                }
            }

            // If the right term is neither a Concept nor an attribute
            // or port in the actor, then it must be a parseable
            // concept function definition.
            if (RHSTerm == null) {
                RHSTerm = _getConceptFunctionTerm(actorElement, RHSString);
            }

            if (RHSTerm == null) {
                throw new IllegalActionException(actorElement,
                        "Could not parse constraint expression right term "
                                + "value for the actor " + getComponent()
                                + ". Right term string: " + RHSString);
            }

            if (constraintDir.equals(GTE)) {
                setAtLeast(actorElement, RHSTerm);
            } else if (constraintDir.equals(LTE)) {
                if (RHSTerm instanceof ConceptFunctionInequalityTerm) {
                    throw new IllegalActionException(
                            actorElement,
                            "When the constraint is '<=' which"
                                    + " indicates an acceptance criterion and not a consraint for the solver, the"
                                    + " inequality cannot have a monotonic function inequality term"
                                    + " on its RHS.");
                } else {
                    setAtMost(actorElement, RHSTerm);
                }
            } else if (constraintDir.equals(EQ)) {
                if (RHSTerm instanceof Concept) {
                    setEquals(actorElement, (Concept) RHSTerm);
                } else {
                    setSameAs(actorElement, RHSTerm);
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

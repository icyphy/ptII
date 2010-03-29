/* Attribute that defines the lattice ontology solver constraints for an actor.

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
package ptolemy.data.ontologies.lattice;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ptolemy.data.expr.StringParameter;
import ptolemy.data.ontologies.OntologySolverModel;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// ActorConstraintsDefinitionAttribute

/** Attribute that defines the lattice ontology solver constraints for an actor.
 *  
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class ActorConstraintsDefinitionAttribute extends Attribute {

    /** Construct the ActorConstraintsDefinitionAttribute attribute
     *  with the given container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ActorConstraintsDefinitionAttribute(CompositeEntity container,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);

        if (!(container instanceof OntologySolverModel)) {
            throw new IllegalActionException(
                    this,
                    "An ActorConstraintsDefinitionAttribute "
                            + "must be contained by an OntologySolverModel entity.");
        }

        actorClassName = new StringParameter(this, "actorClassName");
        actorClassName.setExpression("");

        foundActorClassName = new StringParameter(this, "foundActorClassName");
        foundActorClassName.setExpression("");
        foundActorClassName.setVisibility(Settable.NONE);
        foundActorClassName.setPersistent(true);

        _constraintTermExpressions = new LinkedList<StringParameter>();

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-40\" width=\"75\" height=\"40\" "
                + "style=\"fill:white\"/>" + "<text x=\"-45\" y=\"-25\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "      Actor\nConstraints</text></svg>");

        SingletonConfigurableAttribute description = (SingletonConfigurableAttribute) this
                .getAttribute("_iconDescription");
        description.setPersistent(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  ports and parameters                     ////

    /** The string that represents the class name of the actor for which
     *  this attribute defines lattice ontology solver constraints.
     */
    public StringParameter actorClassName;

    /** The parameter that contains the last valid actor class name found. */
    public StringParameter foundActorClassName;

    /** The string suffix for attribute names that represent constraint definitions
     *  for actor attributes.
     */
    public static final String ATTR_SUFFIX = "AttrTerm";

    /** String representing an equal to constraint choice. */
    public static final String EQ = "==";

    /** String representing a greater than or equal to constraint choice. */
    public static final String GTE = ">=";

    /** String representing that the actor port or attribute should be
     *  ignored for the ontology analysis and not have a concept assigned to it.
     */
    public static final String IGNORE = "IGNORE_ELEMENT";

    /** String representing a less than or equal to constraint choice. */
    public static final String LTE = "<=";

    /** String representing that the actor port or attribute has no constraints
     *  but should have a concept assigned to it.
     */
    public static final String NO_CONSTRAINTS = "NO_CONSTRAINTS";

    /** The string suffix for attribute names that represent constraint definitions
     *  for actor ports.
     */
    public static final String PORT_SUFFIX = "PortTerm";

    /** String representing the separator character ";" between constraint expressions in
     *  the constraint expression string.
     */
    public static final String SEPARATOR = ";";

    /** The length of the attribute and port suffix strings. */
    public static final int SUFFIX_LENGTH = 8;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the attributeChanged method so that if the actor class name
     *  changes, the attribute interface adds and removes fields for constraints
     *  for the actor's ports and attributes.
     *  @param attribute The attribute that has been changed.
     *  @throws IllegalActionException If there is a problem changing the attribute.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == actorClassName) {

            // Collect all the existing constraint parameter fields before checking if we need
            // to remove them because the actor class name changed. This is necessary
            // for the case when the attribute is loaded from a MoML file.
            _constraintTermExpressions.clear();
            for (Object constraintParameter : attributeList(StringParameter.class)) {
                String parameterName = ((StringParameter) constraintParameter)
                        .getName();
                if (parameterName.endsWith(PORT_SUFFIX)
                        || parameterName.endsWith(ATTR_SUFFIX)) {
                    _constraintTermExpressions
                            .add((StringParameter) constraintParameter);
                }
            }

            if (!actorClassName.getExpression().equals("")
                    && !actorClassName.getExpression().equals(
                            foundActorClassName.getExpression())) {
                try {

                    // Verify that the actorClassName correctly specifies an existing class.
                    Class actorClass = Class.forName(actorClassName
                            .getExpression());
                    try {
                        // Instantiate a temporary actor from this class in order
                        // to get all the port and attribute information.
                        Constructor actorConstructor = actorClass
                                .getConstructor(new Class[] {
                                        CompositeEntity.class, String.class });
                        Object actorInstance = actorConstructor
                                .newInstance(new Object[] {
                                        (CompositeEntity) this.getContainer(),
                                        "tempActor" });

                        // Remove all the old constraint parameters that no longer apply
                        // since the actor class name has changed.
                        for (StringParameter constraintParameter : _constraintTermExpressions) {
                            constraintParameter.setContainer(null);
                        }
                        _constraintTermExpressions.clear();

                        // Create a constraint expression parameter for every port and
                        // attribute in the actor.
                        if (actorInstance instanceof ComponentEntity) {
                            for (Object actorPort : ((ComponentEntity) actorInstance)
                                    .portList()) {
                                StringParameter constraintExpression = new StringParameter(
                                        this, ((Port) actorPort).getName()
                                                + PORT_SUFFIX);
                                _constraintTermExpressions
                                        .add(constraintExpression);
                            }

                            for (Object actorAttribute : ((ComponentEntity) actorInstance)
                                    .attributeList()) {
                                if (!((Attribute) actorAttribute).getName()
                                        .startsWith("_")) {
                                    StringParameter constraintExpression = new StringParameter(
                                            this, ((Attribute) actorAttribute)
                                                    .getName()
                                                    + ATTR_SUFFIX);
                                    _constraintTermExpressions
                                            .add(constraintExpression);
                                }
                            }
                        }

                        // Set the icon for the attribute so that it looks like the actor
                        // for which it defines an OntologyAdapter.

                        // First look for an icon file for the actor.
                        // If no actor icon file is found, use the _iconDescription
                        // attribute.
                        String iconFile = actorClassName.getExpression()
                                .replace('.', '/')
                                + "Icon.xml";
                        URL xmlFile = actorClass.getClassLoader().getResource(
                                iconFile);
                        if (xmlFile != null) {
                            MoMLParser parser = new MoMLParser(this.workspace());
                            parser.setContext(this);
                            parser.parse(xmlFile, xmlFile);
                        }

                        if (xmlFile == null) {
                            ConfigurableAttribute actorIconAttribute = (ConfigurableAttribute) ((ComponentEntity) actorInstance)
                                    .getAttribute("_iconDescription");
                            if (actorIconAttribute != null) {
                                String iconDescription = actorIconAttribute
                                        .getConfigureText();
                                SingletonConfigurableAttribute description = (SingletonConfigurableAttribute) this
                                        .getAttribute("_iconDescription");
                                description.configure(null, null,
                                        iconDescription);
                            }
                        }

                        ((ComponentEntity) actorInstance).setContainer(null);

                        // Set the found actor name parameter so that we know what the previous
                        // actor class was set to the next time attributeChanged is called.
                        foundActorClassName.setExpression(actorClassName
                                .getExpression());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new IllegalActionException(
                                this,
                                ex,
                                "Error when trying to "
                                        + "instantiate the class "
                                        + actorClassName.getExpression()
                                        + " in ActorConstraintsDefinitionAttribute "
                                        + this);
                    }
                } catch (ClassNotFoundException classEx) {
                    throw new IllegalActionException(this, classEx,
                            "Actor class " + actorClassName.getExpression()
                                    + " not found.");
                }
            }
        }

        super.attributeChanged(attribute);
    }

    /** Get the adapter defined by this attribute.
     *  @param component The model component for which the adapter will be created.
     *  @return The ActorConstraintsDefinitionAdapter specified by this attribute.
     *  @throws IllegalActionException If the container model's solver cannot
     *   be found or there is a problem initializing the adapter.
     */
    public ActorConstraintsDefinitionAdapter getAdapter(Object component)
            throws IllegalActionException {
        try {
            if (component.getClass() != Class.forName(actorClassName
                    .getExpression())) {
                throw new IllegalActionException(this, "The component "
                        + component
                        + " passed in for the adapter is not of class "
                        + actorClassName.getExpression() + ".");
            }
        } catch (ClassNotFoundException classEx) {
            throw new IllegalActionException(this, classEx, "Actor class "
                    + actorClassName.getExpression() + " not found.");
        }

        LatticeOntologySolver solver = (LatticeOntologySolver) ((OntologySolverModel) getContainer())
                .getContainerSolver();

        // If the solver is null, throw an exception.
        if (solver == null) {
            throw new IllegalActionException(this, "The OntologySolverModel "
                    + " does not have an associated OntologySolver so no "
                    + " OntologyAdapter can be created.");
        }

        // Get the adapter for the actor.
        return new ActorConstraintsDefinitionAdapter(solver, component,
                _constraintTermExpressions);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The list of expressions that represent the constraint
     *  terms for each constraint in the actor.
     */
    protected List<StringParameter> _constraintTermExpressions;
}

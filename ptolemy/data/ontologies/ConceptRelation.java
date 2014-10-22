/* A relation between concepts in an ontology.

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ConceptRelation

/**
 A relation between concepts in an ontology.

 This class is really only used to connect the FiniteConcepts in the Ontology
 class, which corresponds to the relations that a user draws in the Ontology
 Editor in Vergil.  This information is also then used to generate the
 ordering relations when a ConceptGraph is created.

 @author Edward A. Lee, Ben Lickly
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see FiniteConcept
 @see Ontology
 */
public class ConceptRelation extends ComponentRelation {

    /** Construct a ConceptRelation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This ConceptRelation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  @param container The container.
     *  @param name The name of the ConceptRelation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this ConceptRelation.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public ConceptRelation(Ontology container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct a ConceptRelation in the given workspace with an empty string
     *  as a name.
     *  If the workspace argument is null, use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version of the workspace.
     *  @param workspace The workspace for synchronization and version
     *  tracking.
     *  @exception IllegalActionException If the container is incompatible
     *   with this ConceptRelation.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public ConceptRelation(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** An annotation that describes the transition. If this is non-empty,
     *  then a visual editor will be expected to put this annotation on
     *  or near the transition to document its function. This is a string
     *  that defaults to the empty string. Note that it can reference
     *  variables in scope using the notation $name.
     */
    public StringParameter annotation;

    /** Color in which to render this transition.
     *  The default color is black.
     */
    public ColorAttribute color;

    /** Indicator that this transition should be rendered as a dashed line.
     *  This is a boolean that defaults to false.
     */
    public Parameter dashed;

    /** Attribute the exit angle of a visual rendition.
     *  This parameter contains a DoubleToken, initially with value 0.0.
     *  It must lie between -PI and PI.  Otherwise, it will be truncated
     *  to lie within this range.
     */
    public Parameter exitAngle;

    /** Attribute giving the orientation of a self-loop. This is equal to
     * the tangent at the midpoint (more or less).
     *  This parameter contains a DoubleToken, initially with value 0.0.
     */
    public Parameter gamma;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value of the {@link #annotation} parameter.
     *  @return The value of the annotation parameter, or a
     *   string "ERROR" if the parameter cannot be evaluated.
     */
    public String getLabel() {
        try {
            return annotation.stringValue();
        } catch (IllegalActionException e) {
            return "ERROR";
        }
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of {@link Ontology} or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the argument is not an Ontology or null.
     *  @exception NameDuplicationException If the container already has
     *   a relation with the name of this relation.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null && !(container instanceof Ontology)) {
            throw new IllegalActionException(container, this,
                    "Relation can only be contained by instances of "
                            + "Ontology.");
        }
        super.setContainer(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an exception if the port cannot be linked
     *  to this relation.
     *
     *  @param port The port to be checked to see if the relation
     *  can be attached
     *  @exception IllegalActionException If the port's container
     *   is not a {@link FiniteConcept}.
     */
    @Override
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);

        if (!(port.getContainer() instanceof FiniteConcept)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "ConceptRelation can only connect to instances of Concept.");
        }

        if (numLinks() >= 2) {
            throw new IllegalActionException(this,
                    "ConceptRelation can only connect two Concepts.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the variables of this relation.
     *  @exception IllegalActionException Thrown if the relation's variables
     *   cannot be initialized.
     *  @exception NameDuplicationException Thrown if any of the names of attributes
     *   created for this relation conflict with existing attributes in this
     *   relation.
     */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        annotation = new StringParameter(this, "annotation");
        annotation.setExpression("");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        Variable textHighlightHint = new Variable(annotation, "_textHeightHint");
        textHighlightHint.setExpression("5");
        textHighlightHint.setPersistent(false);

        color = new ColorAttribute(this, "color");
        color.setExpression("{0.0, 0.0, 0.0, 1.0}");

        dashed = new Parameter(this, "dashed");
        dashed.setExpression("false");
        dashed.setTypeEquals(BaseType.BOOLEAN);

        exitAngle = new Parameter(this, "exitAngle");
        exitAngle.setVisibility(Settable.NONE);
        exitAngle.setExpression("0.0");
        exitAngle.setTypeEquals(BaseType.DOUBLE);

        gamma = new Parameter(this, "gamma");
        gamma.setVisibility(Settable.NONE);
        gamma.setExpression("0.0");
        gamma.setTypeEquals(BaseType.DOUBLE);
    }
}

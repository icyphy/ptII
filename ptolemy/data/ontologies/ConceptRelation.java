/* A transition in an FSMActor.

 Copyright (c) 1999-2009 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// ConceptRelation

/**
 A relation between concepts in an ontology.
 
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see Concept
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
    ////                         public methods                    ////

    /** Return a string describing this transition. The string has up to
     *  three lines. The first line is the guard expression, preceded
     *  by "guard: ".  The second line is the <i>outputActions</i> preceded
     *  by the string "output: ". The third line is the
     *  <i>setActions</i> preceded by the string "set: ". If any of these
     *  is missing, then the corresponding line is omitted.
     *  @return A string describing this transition.
     */
    public String getLabel() {
        StringBuffer buffer = new StringBuffer("");

        boolean hasAnnotation = false;
        String text;
        try {
            text = annotation.stringValue();
        } catch (IllegalActionException e) {
            text = "Exception evaluating annotation: " + e.getMessage();
        }
        if (!text.trim().equals("")) {
            hasAnnotation = true;
            buffer.append(text);
        }
        return buffer.toString();
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of Ontology or null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the argument is not an Ontology or null.
     *  @exception NameDuplicationException If the container already has
     *   an relation with the name of this transition.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null) {
            if (!(container instanceof Ontology)) {
                throw new IllegalActionException(container, this,
                        "Transition can only be contained by instances of "
                                + "FSMActor.");
            }
        }
        super.setContainer(container);
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

    /** Attribute the exit angle of a visual rendition.
     *  This parameter contains a DoubleToken, initially with value PI/5.
     *  It must lie between -PI and PI.  Otherwise, it will be truncated
     *  to lie within this range.
     */
    public Parameter exitAngle;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Throw an IllegalActionException if the port cannot be linked
     *  to this transition. A transition has a source state and a
     *  destination state. A transition is only linked to the outgoing
     *  port of its source state and the incoming port of its destination
     *  state.
     *  @exception IllegalActionException If the port cannot be linked
     *   to this transition.
     */
    protected void _checkPort(Port port) throws IllegalActionException {
        super._checkPort(port);

        if (!(port.getContainer() instanceof Concept)) {
            throw new IllegalActionException(this, port.getContainer(),
                    "ConceptRelation can only connect to instances of Concept.");
        }

        if (numLinks() == 0) {
            return;
        }

        if (numLinks() >= 2) {
            throw new IllegalActionException(this,
                    "ConceptRelation can only connect two Concepts.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the variables of this transition. */
    private void _init() throws IllegalActionException,
            NameDuplicationException {
        annotation = new StringParameter(this, "annotation");
        annotation.setExpression("");
        // Add a hint to indicate to the PtolemyQuery class to open with a text style.
        Variable variable = new Variable(annotation, "_textHeightHint");
        variable.setExpression("5");
        variable.setPersistent(false);

        exitAngle = new Parameter(this, "exitAngle");
        exitAngle.setVisibility(Settable.NONE);
        exitAngle.setExpression("0.0");
        exitAngle.setTypeEquals(BaseType.DOUBLE);
    }
}

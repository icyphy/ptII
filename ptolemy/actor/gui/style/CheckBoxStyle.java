/* An attribute for specifying that a parameter is edited with a check box.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui.style;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CheckBoxStyle

/**
 This attribute annotates user settable attributes to specify
 a checkbox style for configuring the containing attribute.
 This style is only valid for boolean valued attributes, so this class
 expects that the container will be an instance of Parameter that contains
 a boolean token.

 @see ptolemy.actor.gui.EditorPaneFactory
 @see ParameterEditorStyle
 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class CheckBoxStyle extends ParameterEditorStyle {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public CheckBoxStyle() {
        super();
    }

    /** Construct an attribute in the given workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will contain the attribute
     *  that is being constructed.
     */
    public CheckBoxStyle(Workspace workspace) {
        // This constructor is needed for Shallow codegen to work.
        super(workspace);
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Parameter.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CheckBoxStyle(Parameter container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  @param param The attribute that this annotates.
     *  @return True if the argument is a parameter that contains a
     *  boolean token.
     */
    @Override
    public boolean acceptable(Settable param) {
        if (!(param instanceof Parameter)) {
            return false;
        }

        try {
            Token current = ((Parameter) param).getToken();

            if (current instanceof BooleanToken) {
                return true;
            } else {
                return false;
            }
        } catch (IllegalActionException ex) {
            return false;
        }
    }

    /** Create a new check box entry in the given query associated with the
     *  parameter containing this style.  The name of the entry is
     *  the name of the parameter.  Attach the parameter to the created entry.
     *
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing parameter
     *  does not contain a boolean token.
     */
    @Override
    public void addEntry(PtolemyQuery query) throws IllegalActionException {
        String name = getContainer().getName();

        if (!(getContainer() instanceof Parameter)) {
            throw new IllegalActionException(getContainer(),
                    "CheckBoxStyle can only be "
                            + "contained by instances of Parameter.");
        }

        Parameter param = (Parameter) getContainer();
        Token current = param.getToken();

        if (!(current instanceof BooleanToken)) {
            throw new IllegalActionException(getContainer(),
                    "CheckBoxStyle can only be "
                            + "used for boolean-valued parameters");
        }

        query.addCheckBox(name, param.getDisplayName(),
                ((BooleanToken) current).booleanValue());
        query.attachParameter(param, name);
    }

    /** Override the base class to check that the container is
     *  an instance of parameter.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute and container
     *   are not in the same workspace, or
     *   the proposed container would result in recursive containment, or
     *   the proposed container is not an instance of Parameter.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        if (container != null && !(container instanceof Parameter)) {
            throw new IllegalActionException(this, container,
                    "CheckBoxStyle can only be contained by a Parameter.");
        }

        super.setContainer(container);
    }
}

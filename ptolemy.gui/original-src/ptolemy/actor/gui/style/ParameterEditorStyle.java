/* An attribute for specifying how a parameter is edited.

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
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ParameterEditorStyle

/**
 This attribute annotates user settable attributes to specify the style
 used for configuring the containing attribute.
 The EditorPaneFactory class uses concrete subclasses
 of this base class as a strategy pattern for creating entries in
 an interactive parameter editor.  This class expects that the container
 will implement the Settable interface.

 @see ptolemy.actor.gui.EditorPaneFactory
 @see Settable
 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public abstract class ParameterEditorStyle extends Attribute {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public ParameterEditorStyle() {
        super();
    }

    /** Construct an attribute in the given workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will contain the attribute
     *  that is being constructed.
     */
    public ParameterEditorStyle(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Settable.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ParameterEditorStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  For instance, a check box style would return true if the
     *  argument was a Parameter that contained a boolean token.
     *  @param param The attribute that this annotates.
     *  @return True if this style is acceptable.
     */
    public abstract boolean acceptable(Settable param);

    /** Create a new entry in the given query associated with the
     *  attribute containing this style.  The name of the entry should be
     *  the name of the attribute.  The attribute should be attached to
     *  the entry so that changes in its value are reflected in the
     *  query.
     *
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing attribute
     *   has a value that cannot be edited using this style.
     */
    public abstract void addEntry(PtolemyQuery query)
            throws IllegalActionException;

    /** Override the base class to first check that the container is
     *  an instance of Settable.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment, or
     *   the proposed container is not an instance of Settable.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        // We want this check, but here is not the place.
        //     if (container != null && !(container instanceof Settable)) {
        //    throw new IllegalActionException(this, container,
        //            "ParameterEditorStyle can only be "
        //            + "contained by Settable.");
        // }
        super.setContainer(container);
    }
}

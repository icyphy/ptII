/* An attribute for specifying that a parameter is edited with an entry line.

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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// LineStyle

/**
 This attribute annotates user settable attributes to specify
 an arbitrary type-in line style for configuring the containing attribute.
 This style can be used with any Settable attribute.

 @see ptolemy.actor.gui.EditorPaneFactory
 @see ParameterEditorStyle
 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class LineStyle extends ParameterEditorStyle {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public LineStyle() {
        super();
    }

    /** Construct an attribute in the given workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will contain the attribute
     *  that is being constructed.
     */
    public LineStyle(Workspace workspace) {
        // This constructor is needed for Shallow codegen to work.
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
    public LineStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  @param param The attribute that this annotates.
     *  @return True.
     */
    @Override
    public boolean acceptable(Settable param) {
        return true;
    }

    /** Create a new type-in line
     *  entry in the given query associated with the
     *  attribute containing this style.  The name of the entry is
     *  the name of the attribute.  Attach the attribute to the created entry.
     *
     *  @param query The query into which to add the entry.
     */
    @Override
    public void addEntry(PtolemyQuery query) {
        Settable container = (Settable) getContainer();
        String name = container.getName();
        String defaultValue = "";
        defaultValue = container.getExpression();
        // Adjust the editability if the container is NOT_EDITABLE
        // and _expertMode is not set.
        if (query.adjustEditable(container, null)) {
            query.addLine(name, container.getDisplayName(), defaultValue,
                    PtolemyQuery.preferredBackgroundColor(container),
                    PtolemyQuery.preferredForegroundColor(container));
        } else {
            // Treat this like a NotEditableLineDisplay
            query.addDisplay(name, container.getDisplayName(), defaultValue);
        }
        query.attachParameter(container, name);
    }
}

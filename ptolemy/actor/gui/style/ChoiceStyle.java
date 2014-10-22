/* An attribute for specifying that a parameter is edited with a combo menu.

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

import java.util.List;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ChoiceStyle

/**
 This attribute annotates user settable attributes to specify
 an uneditable combobox style for configuring the containing attribute.
 An uneditable combobox is restricted to only the values specified as the
 combobox options.  No arbitrary value can be entered.
 For an editable combobox, use EditableChoiceStyle instead.
 The choices that are presented in the combobox
 are given by a set of attributes implementing the Settable interface,
 such as StringAttribute, contained by this style.

 @see EditableChoiceStyle
 @see ptolemy.actor.gui.EditorPaneFactory
 @see ParameterEditorStyle
 @see ptolemy.kernel.util.StringAttribute
 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class ChoiceStyle extends ParameterEditorStyle {
    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public ChoiceStyle() {
        super();
    }

    /** Construct an attribute in the given workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will contain the attribute
     *  that is being constructed.

     */
    public ChoiceStyle(Workspace workspace) {
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
    public ChoiceStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given attribute.
     *  @param param The attribute that this annotates.
     *  @return True if the style contains some attributes representing the
     *   choices.
     */
    @Override
    public boolean acceptable(Settable param) {
        return !attributeList(Settable.class).isEmpty();
    }

    /** Create a new uneditable
     *  combo box entry in the given query associated with the
     *  attribute containing this style.  The name of the entry is
     *  the name of the attribute.  Attach the attribute to the created entry.
     *
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing attribute
     *   has a value that cannot be edited using this style.
     */
    @Override
    public void addEntry(PtolemyQuery query) throws IllegalActionException {
        Settable container = (Settable) getContainer();
        String name = container.getName();
        List paramList = attributeList(Settable.class);
        Settable[] choices = (Settable[]) paramList
                .toArray(new Settable[paramList.size()]);
        String[] values = new String[choices.length];

        for (int i = 0; i < choices.length; i++) {
            values[i] = choices[i].getExpression();
        }

        String defaultChoice = container.getExpression();
        query.addChoice(name, container.getDisplayName(), values,
                defaultChoice, _isEditable,
                PtolemyQuery.preferredBackgroundColor(container),
                PtolemyQuery.preferredForegroundColor(container));
        query.attachParameter(container, name);
    }

    /** Whether or not the combobox is editable.  EditableComboBox changes
     *  this to create an editable combo box.  In this base class the
     *  value is false.
     */
    protected boolean _isEditable = false;
}

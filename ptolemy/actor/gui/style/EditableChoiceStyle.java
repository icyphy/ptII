/* An attribute for specifying that a parameter is edited with a combo menu.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.style;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// EditableChoiceStyle
/**
This attribute annotates user settable attributes to specify
an editable combobox style for configuring the containing attribute.
An editable combobox allows an arbitrary value to be entered in the
combobox.
For an uneditable combobox, use ChoiceStyle instead.
The choices that are presented in the combobox
are given by a set of attributes implementing the Settable interface,
such as StringAttribute, contained by this style.
<p>
This class extends ChoiceStyle only for the purpose of eliminating code
duplication.

@see ChoiceStyle
@see ptolemy.actor.gui.EditorPaneFactory
@see ParameterEditorStyle
@see ptolemy.kernel.util.StringAttribute
@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/

public class EditableChoiceStyle extends ChoiceStyle {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public EditableChoiceStyle() {
        super();
        _isEditable = true;
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
    public EditableChoiceStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _isEditable = true;
    }

    /** Create a new editable
     *  combo box entry in the given query associated with the
     *  attribute containing this style.  The name of the entry is
     *  the name of the attribute.  Attach the attribute to the created entry.
     *
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing attribute
     *   has a value that cannot be edited using this style.
     */
    public void addEntry(PtolemyQuery query) throws IllegalActionException {
        super.addEntry(query);
        _isEditable = true;
    }
}

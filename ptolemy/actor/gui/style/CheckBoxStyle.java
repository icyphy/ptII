/* An attribute for specifying that a parameter is edited with a check box.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.style;

// Ptolemy imports.
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.Query;
import ptolemy.kernel.util.*;
import ptolemy.actor.gui.PtolemyQuery;

// Java imports.

//////////////////////////////////////////////////////////////////////////
//// CheckBoxStyle
/**
This attribute annotates a parameter to suggest an interactive
mechanism for editing that uses a single checkbox.
The EditorPaneFactory class observes the
presence of this attribute to guide construction of an interactive
parameter editor.

@see EditorPaneFactory
@author Edward A. Lee, Steve Neuendorffer
@version $Id$
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

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Parameter.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CheckBoxStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     */
    public boolean accept(Parameter param) {
	try {
	    Token current = param.getToken();
	    if (current instanceof BooleanToken) {
		return true;
	    } else {
		return false;
	    }
	} catch (IllegalActionException ex) {
	    return false;
	}
    }

    /** This method is called by EditorPaneFactory to delegate the
     *  construction of an entry in a Query.  This permits this class
     *  to control how the user modifies the value of the containing
     *  parameter.
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing parameter
     *   has a non-boolean value.
     */
    public void addEntry(PtolemyQuery query) throws IllegalActionException {
        String name = getContainer().getName();
        Parameter param = (Parameter)getContainer();
        Token current = param.getToken();
        if (!(current instanceof BooleanToken)) {
            throw new IllegalActionException(getContainer(),
                    "CheckBoxStyle can only be used for boolean-valued parameters");
        }
        query.addCheckBox(name, name, ((BooleanToken)current).booleanValue());
        query.attachParameter(param, name);
    }
}

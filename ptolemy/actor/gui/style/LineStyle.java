/* An attribute for specifying that a parameter is edited with an entry line.

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
import ptolemy.data.expr.Variable;
import ptolemy.gui.Query;
import ptolemy.kernel.util.*;
import ptolemy.actor.gui.PtolemyQuery;

// Java imports.

//////////////////////////////////////////////////////////////////////////
//// LineStyle
/**
This attribute annotates a parameter to suggest an interactive
mechanism for editing that uses a combobox menu.
The EditorPaneFactory class observes the
presence of this attribute to guide construction of an interactive
parameter editor.

@see EditorPaneFactory
@author Steve Neuendorffer
@version $Id$
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

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Variable.
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
     *  @return True.
     */
    public boolean accept(Variable param) {
	return true;
    }

    /** Create a new entry in the given query with the given name
     *  with this style.  Attach the variable that
     *  contains this style to the created entry.  
     *  This class will create a line entry.
     *  
     *  @param query The query into which to add the entry.
     *  @exception IllegalActionException If the containing variable
     *  has a value that cannot be edited using this style.
     */
    public void addEntry(PtolemyQuery query)
	throws IllegalActionException {
        String name = getContainer().getName();
        Variable param = (Variable)getContainer();
        query.addLine(name, name, param.stringRepresentation());
	query.attachParameter(param, name);
    }
}

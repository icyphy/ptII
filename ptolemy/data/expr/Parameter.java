/* Parameter is another name for a Variable.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLUtilities;

import java.io.Writer;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
Parameter is almost identical to Variable, its base class, with the
only difference being the MoML representation.  The base class has none,
and therefore is not a persistent object.  This class has one.
A second reason for existence of this class is to serve as a
marker for variables that are to made
visible at the user interface level.  By convention, an instance
of NamedObj has a set of attributes, some of which are instances
of the class Parameter.  When a user interface presents these attributes
for editing, it presents only those that are instances of Parameter.
And the parameters, but not any variables, are exported to MoML when
the container object is exported.

@author Neil Smyth, Edward A. Lee, Xiaojun Liu
@version $Id$

@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token

*/

public class Parameter extends Variable {

    /** Construct a parameter in the default workspace with an empty
     *  string as its name. The parameter is added to the list of
     *  objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Parameter() {
        super();
    }

    /** Construct a parameter in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public Parameter(Workspace workspace) {
        super(workspace);
    }

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public Parameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public Parameter(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Write a MoML description of this object.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "attribute"
     *  element, with "name" and "class" (XML) attributes.
     *  The body of the element, between the "&lt;attribute&gt;"
     *  and "&lt;/attribute&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @throws IOException If an I/O error occurs.
     */
    public void exportMoML(Writer output, int depth) throws IOException {
        String value = stringRepresentation();
        String valueTerm = "";
        if(value != null && !value.equals("")) {
            valueTerm = " value=\"" + 
                MoMLUtilities.escapeAttribute(stringRepresentation()) + 
                "\"";
        }

        output.write(_getIndentPrefix(depth)
               + "<"
               + getMoMLElementName()
               + " name=\""
               + getName()
               + "\" class=\""
               + getClass().getName()
               + "\""
               + valueTerm
               + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLElementName() + ">\n");
    }
}

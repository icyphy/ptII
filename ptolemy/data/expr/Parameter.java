/* Parameter is a subclass of Variable with support for strings.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
Parameter extends Variable with additional support for string-valued
variables that makes these friendlier at the user interface level.
In particular, this class supports an annotation that specifies
choices for values.  A user interface can use this to present a
choice dialog that offers the specified values.  This is typically
used when a particular set of choices make sense.  The values can
be any expression, or if used in conjunction with string mode,
any string.
<p>
By default, an instance of Parameter, unlike Variable, is persistent.
<p>
By convention, an instance of NamedObj has a set of attributes,
some of which are visible to users and some of which are not.
When a user interface presents these attributes
for editing, it presents only those that are visible.
By default, an instance of Parameter is visible, as indicated by the fact
that it returns FULL in its getVisibility() method.  This can be overridden
by calling setVisibility().

@author Neil Smyth, Edward A. Lee, Xiaojun Liu
@version $Id$
@since Ptolemy II 0.2

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
        setVisibility(Settable.FULL);
        setPersistent(true);
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
        setVisibility(Settable.FULL);
        setPersistent(true);
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
        setVisibility(Settable.FULL);
        setPersistent(true);
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
        setVisibility(Settable.FULL);
        setPersistent(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a choice.
     *  @param choice A choice to offer to the user.
     *  @see #removeChoice(String)
     */
    public void addChoice(String choice) {
        if (_choices == null) {
            _choices = new ArrayList();
        }
        _choices.add(choice);
    }

    /** Write a MoML description of this object, unless this object is
     *  not persistent. MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name" and "class" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_suppressMoML(depth)) {
            return;
        }
        String value = getExpression();
        String valueTerm = "";
        if (value != null && !value.equals("")) {
            valueTerm = " value=\"" +
                StringUtilities.escapeForXML(value) + "\"";
        }

        output.write(_getIndentPrefix(depth)
                + "<"
                + _elementName
                + " name=\""
                + name
                + "\" class=\""
                + getClassName()
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + _elementName + ">\n");
    }
    
    /** Get choices.
     *  @return An array of choices, or null if there are none.
     *  @see #addChoice(String)
     */
    public String[] getChoices() {
        if (_choices == null || _choices.size() == 0) {
            return null;
        } else {
            return (String [])_choices.toArray(new String[_choices.size()]);
        }
    }
        
    /** Remove all the choices.
     *  @see #removeChoice(String)
     */
    public void removeAllChoices() {
        _choices = null;
    }

    /** Remove a choice.
     *  @param choice A choice to remove from the list offered to the user.
     *  @see #addChoice(String)
     *  @see #removeAllChoices()
     */
    public void removeChoice(String choice) {
        if (_choices != null) {
            _choices.remove(choice);
        }
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The List of choices.
    private List _choices;
}

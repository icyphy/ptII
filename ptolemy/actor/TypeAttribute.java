/* An attribute for defining a data type for a port.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Yellow (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

// FIXME Spurious dependence
import ptolemy.moml.MoMLUtilities;

import java.io.IOException;
import java.io.Writer;

//////////////////////////////////////////////////////////////////////////
//// TypeAttribute
/**
An attribute for defining a data type for a port.
Use setExpression() to define a data type, as in for example,
<pre>
    attribute.setExpression("BaseType.DOUBLE");
</pre>
The class TypedIOPort recognizes when this attribute is inserted
and calls setTypeEquals() to define its type.

@author Edward A. Lee
@version $Id$
@see TypedIOPort
*/
public class TypeAttribute extends Attribute implements Settable {

    /** Construct an attribute with the given name contained by the specified
     *  port. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TypeAttribute(TypedIOPort container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Write a MoML description of this object.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name", "class", and "value" (XML) attributes.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        String value = getExpression();
        String valueTerm = "";
        if(value != null && !value.equals("")) {
            // FIXME: Spurious dependence on the moml package.
            valueTerm = " value=\"" + 
                MoMLUtilities.escapeAttribute(value) + 
                "\"";
        }

        output.write(_getIndentPrefix(depth)
               + "<"
               + getMoMLElementName()
               + " name=\""
               + name
               + "\" class=\""
               + getClass().getName()
               + "\""
               + valueTerm
               + "/>\n");
    }

    /** Get the type designation that has been set by setExpression(),
     *  or null if there is none.
     *  @return The type designation.
     */
    public String getExpression() {
        return _designation;
    }

    /** Get the type class that has been set by setExpression(), or
     *  null if setExpression() has not been called.
     *  @return The type class.
     */
    public Type getType() {
        return _type;
    }

    /** Set the type designation.
     *  @param expression The type designation.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void setExpression(String expression) throws IllegalActionException {
        _designation = expression;
        _type = BaseType.forName(expression);
        if (_type == null) {
            throw new IllegalActionException(this,
            "Cannot find type class: " + expression);
        }
        TypedIOPort container = (TypedIOPort)getContainer();
        if (container != null) {
            container.attributeChanged(this);
        }
    }

    // FIXME: Override setContainer to ensure it's a TypedIOPort.

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The type designation.
    private String _designation;

    // The type.
    private Type _type;
}

/* An attribute for defining the data type of a port.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// PrototypeAttribute
/**
An attribute for defining the data type of a port.
Use setExpression() to define a data type, as in for example,
<pre>
    attribute.setExpression("double");
</pre>
The class TypedIOPort recognizes when this attribute is inserted
and calls setTypeEquals() to define its type. In the above example,
the type of the port is set to double.

The following primitive types are supported: boolean, complex, double,
int, long, and string. For structured types, follow the same syntax as
in expressions. For example:
<pre>
    {double} - double array
    [int]    - int matrix
    {field1 = string, field2 = int} - record with two fields
</pre>

@author Edward A. Lee, Xiaojun Liu
@version $Id$
@see TypedIOPort
@see PtParser
*/
public class PrototypeAttribute extends StringAttribute {

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
    public PrototypeAttribute(TypedIOPort container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the type specified using setExpression(), or
     *  null if setExpression() has not been called.
     *  @return The type class.
     */
    public Type getType() {
        return _type;
    }

    /** Check the type designation by evaluating the prototype expression.
     *  @exception IllegalActionException If the expression is not a valid
     *   type.
     */
    public void validate() throws IllegalActionException {
	ASTPtRootNode tree = _parser.generateParseTree(getExpression());
	Token token = tree.evaluateParseTree();
        _type = token.getType();
        super.validate();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to ensure that the proposed container
     *  is a TypedIOPort.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   TypedIOPort, or if the base class throws it.
     */
    protected void _checkContainer(NamedObj container)
            throws IllegalActionException {
        if (!(container instanceof TypedIOPort) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "PrototypeAttribute can only be contained by instances " +
		    "of TypedIOPort.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The parser for the ptototype string.
    private PtParser _parser = new PtParser();

    // The type.
    private Type _type;
}

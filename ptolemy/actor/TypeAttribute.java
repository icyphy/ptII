/* An attribute for defining a data type for a port.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TypeAttribute
/**
An attribute for defining a data type for a port.
Use setExpression() to define a data type, as in for example,
<pre>
    attribute.setExpression("double");
</pre>
The class TypedIOPort recognizes when this attribute is inserted
and calls setTypeEquals() to define its type. In the above example,
the type of the port is set to double.
<p>
The type can be given by any expression that can be evaluated.
In fact, it would be equally valid to specify the type to be double
using
<pre>
    attribute.setExpression("0.0");
</pre>
The Constants class defines the constant "double" to have the value
0.0, so that instead you may give the type by name.
The Constants class defines for convenience the following
constants: boolean, complex, double, fixedpoint, general,
int, long, matrix, object, scalar, string, and unknown.
The constant "unknown" has a rather special behavior, in that
it sets the type of the port to be unknown, allowing type resolution
to infer it.  The constant "matrix" designates a matrix
without specifying an element type, in contrast to, for example,
"[double]", which specifies a double matrix.
Similarly, the constant "scalar" designates a scalar of any type
(double, int, long, etc.).
The constant "general" designates any type.
<p>
Since the type is given by a "prototype" (an expression with the
appropriate type), any data type that can be given in an expression
can be specified as a type. For structured types, follow the
same syntax as in expressions. For example:
<pre>
    {double} - double array
    [int]    - int matrix
    {field1 = string, field2 = int} - record with two fields
</pre>

@author Edward A. Lee, Xiaojun Liu
@version $Id$
@since Ptolemy II 1.0
@see TypedIOPort
@see ptolemy.data.expr.Constants
*/
public class TypeAttribute extends Parameter {
    
    /** Construct a parameter in the specified workspace with an empty
     *  string as a name.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public TypeAttribute(Workspace workspace) {
        super(workspace);
    }

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
                    "TypeAttribute can only be contained by instances " +
                    "of TypedIOPort.");
        }
    }
}

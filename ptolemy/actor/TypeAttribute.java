/* An attribute for defining a data type for a port.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// TypeAttribute
/**
An attribute for defining a data type for a port.
Use setExpression() to define a data type, as in for example,
<pre>
    attribute.setExpression("double");
</pre>
The class TypedIOPort recognizes when this attribute is inserted
and calls setTypeEquals() to define its type.

@author Edward A. Lee
@version $Id$
@see TypedIOPort
*/
public class TypeAttribute extends StringAttribute {

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

    /** Get the type class that has been set by setExpression(), or
     *  null if setExpression() has not been called.
     *  @return The type class.
     */
    public Type getType() {
        return _type;
    }

    /** Check the type designation using BaseType.forName().
     *  @see BaseType#forName(String)
     *  @exception IllegalActionException If the expression is not a valid
     *   type name.
     */
    public void validate() throws IllegalActionException {
        _type = BaseType.forName(getExpression());
        if (_type == null) {
            throw new IllegalActionException(this,
                    "Cannot find type class: " + getExpression());
        }
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
                    "TypeAttribute can only be contained by instances " +
		    "of TypedIOPort.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The type.
    private Type _type;
}

/* An IOPort with a type.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.actor;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.graph.*;

import java.lang.reflect.*;

//////////////////////////////////////////////////////////////////////////
//// TypedIOPort
/**
An IOPort with a type.  The type is one of the token types in the data
package.  A TypedIOPort has a declared type which can be set by calling
setDeclaredType().  If this method is not called, the declared type is
null, and the type is said to be undeclared.  In this case, the port has
more than one types and the acceptable types are defined by a set of
constraints.<p>

In addition to the declared type, a TypedIOPort also has a resolved type.
If the declared type is not null, the resolved type will be the same as
the declared type; otherwise, the type resolution algorithm will assign
a resolved type according to the type constraints.<p>

A TypedIOPort can only link to instances of TypedIORelation. Derived
classes may further constrain links to a subclass of TypedIORelation.
To do this, they should override the protected methods _link() and
_linkInside() to throw an exception if their arguments are not of the
appropriate type.  Similarly, an TypeIOPort can only be contained by a
class derived from ComponentEntity and implementing the TypedActor
interface.  Subclasses may further constrain the containers by overriding
setContainer().

@author Yuhong Xiong, Lukito Muliadi
@version $Id$
*/

public class TypedIOPort extends IOPort {

    // all the constructors are wrappers of the super class constructors.

    /** Construct a TypedIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public TypedIOPort() {
        super();
    }

    /** Construct a TypedIOPort with a containing actor and a name
     *  that is neither an input nor an output.  The specified container
     *  must implement the TypedActor interface, or an exception will be
     *  thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TypedIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Construct a TypedIOPort with a container and a name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the TypedActor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isinput True if this is to be an input port.
     *  @param isoutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TypedIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput)
            throws IllegalActionException, NameDuplicationException {
	super(container, name, isinput, isoutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must
     *  do this yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  The new port will have the same declared and resolved types
     *  as this one.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new TypedIOPort.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        TypedIOPort newobj = (TypedIOPort)super.clone(ws);
	newobj._typeTerm = null;
	return newobj;
    }

    /** Return the declared type of this port.  The type is represented
     *  by an instance of Class associated with a token type.
     *  If the type is undeclared, returns null.
     *  This method is read-synchronized on the workspace.
     *  @return a Class representing the declared type, or null if the
     *   type is undeclared.
     */
    public Class getDeclaredType() {
	try {
	    workspace().getReadAccess();
	    return _declaredType;
	} finally {
	    workspace().doneReading();
	}
    }

    /** Return the resolved type of this object.  The type is represented
     *  by an instance of Class associated with a token type.
     *  If this port has an undeclared type, and this method is called
     *  before type resolution takes place, this method returns null.
     *  This method is read-synchronized on the workspace.
     *  @return a Class representing the resolved type.
     */
    public Class getResolvedType() {
	try {
	    workspace().getReadAccess();
	    return _resolvedType;
	} finally {
	    workspace().doneReading();
	}
    }

    /** Return an InequalityTerm encapsulating the resolved type of
     *  this port. The InequalityTerm can be used to form type constraints.
     *  If this port has a declared type, the inequality
     *  term represents a type constant; otherwise, it represents a
     *  type variable.
     *  @return An InequalityTerm whose value is the resolved type.
     */
    public InequalityTerm getTypeTerm() {
	if (_typeTerm == null) {
	    _typeTerm = new TypeTerm(this);
	}
	return _typeTerm;
    }

    /** Override the method in the super class to do type checking.
     *  If the type of the specified token is the resolved type of this
     *  TypedIOPort, or the token can be converted to the resolved type
     *  losslessly, the token is sent to all receivers connected to the
     *  specified channel. Otherwise, IllegalActionException is thrown.
     *  Before putting the token into the destination receivers, this
     *  method also finds the resolved type of the input TypedIOPort
     *  containing the receivers, and tests if the token is an instance
     *  of that type. If not, this method will convert the token to the
     *  type of the input port. The conversion is done by calling the
     *  convert() method on an instance of a token with the resolved type
     *  of the input port.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelindex The index of the channel, from 0 to width-1
     *  @param token The token to send
     *  @exception IllegalActionException If the port is not an output,
     *   or if the index is out of range, or if the token to be sent cannot
     *   be converted to the resolved type of this IOPort.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    public void send(int channelindex, Token token)
            throws IllegalActionException, NoRoomException {
	Receiver[][] farRec;
        try {
            workspace().getReadAccess();
            if (!isOutput()) {
                throw new NoRoomException(this,
                        "send: Tokens can only be sent from an output port.");
            }
            if (channelindex >= getWidth() || channelindex < 0) {
                throw new NoRoomException(this,
                        "send: channel index is out of range.");
            }
	    int compare = TypeLattice.compare(token.getClass(), _resolvedType);
	    if (compare == CPO.HIGHER ||
                    compare == CPO.INCOMPARABLE) {
		throw new IllegalArgumentException("Run-time type checking " +
		    "failed. token: " + token.getClass().getName() +
		    ", port: " + getFullName() + ", port type: " +
		    getResolvedType().getName());
	    }

	    // Note that the getRemoteReceivers() method doesn't throw
            // any non-runtime exception.
	    farRec = getRemoteReceivers();
	    if (farRec == null || farRec[channelindex] == null) {
		return;
	    }
        } finally {
            workspace().doneReading();
        }

	try {
            for (int j = 0; j < farRec[channelindex].length; j++) {
	        TypedIOPort port =
                    (TypedIOPort)farRec[channelindex][j].getContainer();
	        Class farType = port.getResolvedType();

		// farType might be "Token", since the base class Token
		// does not have a convert method, the convert method
		// should only be invoked if the token being transported
		// is not an instance of farType.
	        if (farType.isInstance(token)) {
                    farRec[channelindex][j].put(token);
                } else {
		    Object[] arg = new Object[1];
		    arg[0] = token;
		    Method convert = port._getConvertMethod();
		    Token newToken = (Token)convert.invoke(null, arg);
                    farRec[channelindex][j].put(newToken);
                }
            }
        } catch (IllegalAccessException iae) {
	    throw new InternalErrorException("TypedIOPort.send: " +
                    "IllegalAccessException: " + iae.getMessage());
	} catch (InvocationTargetException ite) {
            throw new InternalErrorException("TypedIOPort.send: " +
                    "InvocationTargetException: " + ite.getMessage());
        }
    }

    /** Override the base class to ensure that the proposed container
     *  implements the TypedActor interface (the base class ensures that
     *  the container implements the Actor interface) or null. A null
     *  argument will remove the port from the container.
     *
     *  @param container The proposed container.
     *  @exception IllegalActionException If the proposed container is not a
     *   ComponentEntity, doesn't implement TypedActor, or has no name,
     *   or the port and container are not in the same workspace. Or
     *   it's not null.
     *  @exception NameDuplicationException If the container already has
     *   a port with the name of this port.
     */
    public void setContainer(Entity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof TypedActor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "TypedIOPort can only be contained by objects " +
		    "implementing the TypedActor interface.");
        }
        super.setContainer(container);
    }

    /** Sets the declared type of this object.  The type is represented
     *  by an instance of Class associated with a non-abstract token type,
     *  or null. If the type is null, the type of this port is undeclared.
     *  This method is write-synchronized on the workspace.
     *  @param type an instance of a Class representing a token type.
     *  @exception IllegalArgumentException If the specified type is not
     *   a non-abstract token type, or null.
     */
    // FIXME: this method may want to inform its director about this
    // change.
    public void setDeclaredType(Class c) {
	if (c != null && !(TypeLattice.isInstantiableType(c))) {
	    throw new IllegalArgumentException(
		    "TypedIOPort.setDeclaredType: argument is not " +
		    "a non-abstract token type, or null.");
	}

	try {
	    workspace().getWriteAccess();
	    _declaredType = c;

	    // also set the resolved type,  If _declaredType == null, i.e.,
	    // undeclared, the type resolution algorithm will reset the
	    // _resolvedType.
	    _resolvedType = _declaredType;
	    _convertMethod = null;
	} finally {
	    workspace().doneWriting();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the resolved type of this port.  The type is represented
     *  by an instance of Class that is an element in the type lattice.
     *  This method is write-synchronized on the workspace.
     *  Note that this method should not be used directly. It should
     *  only be used by the type resolution algorithm.
     *  @param type an instance of Class that is an element in the type
     *   lattice.
     */
    protected void _setResolvedType(Class c) {
	try {
	    workspace().getWriteAccess();
	    _resolvedType = c;

	    _convertMethod = null;
	} finally {
	    workspace().doneWriting();
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include
     *  information about the type of this port.
     */
    public static final int TYPE = 4096;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class and in this class.
     *  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  <p>
     *  If the detail argument sets the bit defined by the constant
     *  TYPE, then append to the description a field of the form
     *  "type {declared <i>declaredType</i> resolved <i>resolvedType</i>}".
     *  The declaredType and resolvedType are the names of the tokens
     *  representing the types.
     *  <p>
     *
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }

            if ((detail & TYPE) != 0) {
                if (result.trim().length() > 0) {
                    result += " ";
                }
                result += "type {declared ";
		if (getDeclaredType() == null) {
		    result += "null";
		} else {
		    result += getDeclaredType().getName();
		}
		result += " resolved ";
		if (getResolvedType() == null) {
		    result += "null";
		} else {
		    result += getResolvedType().getName();
		}
		result += "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Override parent method to ensure compatibility of the relation.
     *  <p>
     *  This method should not be used directly.  Use the public version
     *  instead. It is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an TypedIORelation, or the port already linked
     *   to a relation and is not a multiport, or if the relation has width
     *   not exactly one and the port is not a multiport, or the port is
     *   not in the same workspace as the relation.
     */
    protected void _link(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " TypedIOPort requires TypedIORelation.");
        }
        super._link((TypedIORelation)relation);
    }

    /** Override parent method to ensure compatibility of the relation.
     *  <p>
     *  This method should not be used directly.  Use the public version
     *  instead. It is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to on the inside.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an TypedIORelation, or the port already linked
     *   to a relation and is not a multiport, or the relation has width
     *   not exactly one and the port is not a multiport, or the
     *   relation is incompatible with this port, or the port is not
     *   in the same workspace as the relation.
     */
    protected void _linkInside(ComponentRelation relation)
            throws IllegalActionException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " TypedIOPort requires TypedIORelation.");
        }
        super._linkInside((TypedIORelation)relation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    // Return the convert() Method for the resolved type of this port.
    private Method _getConvertMethod() {
	try {
	    if (_convertMethod == null) {
	    	Class[] formal = new Class[1];
	    	formal[0] = Token.class;
	    	_convertMethod = _resolvedType.getMethod("convert", formal);
	    }
	    return _convertMethod;

	} catch (NoSuchMethodException nsme) {
            throw new InternalErrorException("TypedIOPort._getConvertMethod: "
                    + "NoSuchMethodException: " + nsme.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Class _declaredType = null;
    private Class _resolvedType = null;

    private TypeTerm _typeTerm = null;
    private Method _convertMethod = null;
}


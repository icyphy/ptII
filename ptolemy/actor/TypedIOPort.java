/* An IOPort with a type.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
import ptolemy.data.type.*;
import ptolemy.graph.*;

import java.lang.reflect.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// TypedIOPort
/**
An IOPort with a type. This class implements the Typeable interface.
The type is represented by an intance of Type in data.type package.
It can be declared by calling setTypeEquals(). If this method is not
called, or called with a BaseType.NAT argument, the type of this port
will be set by type resolution using the type constraints. The type
constraints on this port can be specified using the methods defined in
the Typeable interface.

This class keeps a list of TypeListeners. Whenever the type
changes, this class will generate an instance of TypeEvent and pass it
to the listeners by calling their typeChanged() method. A TypeListener
register its interest in the type change event of this port by calling
addTypeListener(), and can be removed from the listener list by calling
the removeTypeListener().

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

public class TypedIOPort extends IOPort implements Typeable {

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
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   TypedActor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public TypedIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
	super(container, name, isInput, isOutput);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a type listener to this port. The listener will
     *  be notified of all the type changes.
     *  @param listener The TypeListener to add.
     */
    public void addTypeListener(TypeListener listener) {
	_typeListeners.add(listener);
    }

    /** Clone this port into the specified workspace. The new port is
     *  <i>not</i> added to the directory of that workspace (you must
     *  do this yourself if you want it there).
     *  The result is a new port with no connections and no container.
     *  The new port will have the same type as this one, but will not
     *  have any type listeners and type constraints attached to it.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the
     *   attributes cannot be cloned.
     *  @return A new TypedIOPort.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        TypedIOPort newobj = (TypedIOPort)super.clone(ws);
	// set _declaredType and _resolvedType
	if (_declaredType instanceof StructuredType &&
                !_declaredType.isConstant()) {
	    newobj._declaredType =
                (Type)((StructuredType)_declaredType).clone();
	    newobj._resolvedType = newobj._declaredType;
	}

	newobj._typeTerm = null;
	newobj._typeListeners = new LinkedList();
	newobj._constraints = new LinkedList();
	return newobj;
    }

    /** Return the type of this port.  The type is represented
     *  by an instance of Class associated with a token type.
     *  If the type is not set through setTypeEquals(), and this method
     *  is called before type resolution takes place, this method
     *  returns BaseType.NAT.
     *  This method is read-synchronized on the workspace.
     *  @return An instance of Type.
     */
    public Type getType() {
	try {
	    _workspace.getReadAccess();
	    return _resolvedType;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** Return an InequalityTerm encapsulating the type of
     *  this port. The InequalityTerm can be used to form type constraints.
     *  If the type is set through setTypeEquals(), the inequality
     *  term represents a type constant; otherwise, it represents a
     *  type variable.
     *  @return An InequalityTerm whose value is the type of this port.
     */
    public InequalityTerm getTypeTerm() {
	if (_typeTerm == null) {
	    _typeTerm = new TypeTerm(this);
	}
	return _typeTerm;
    }

    /** Remove a type listener from this port.  If the listener is
     *  not attached to this port, do nothing.
     *  @param listener The TypeListener to be removed.
     */
    public void removeTypeListener(TypeListener listener) {
	_typeListeners.remove(listener);
    }

    /** Send a token to the specified channel, checking the type
     *  and converting the token if necessary.
     *  If the port is not connected to anything, or receivers have not been
     *  created in the remote port, or the channel index is out of
     *  range, or the port is not an output port,
     *  then just silently return.  This behavior makes it
     *  easy to leave output ports unconnected when you are not interested
     *  in the output.
     *  If the type of the specified token is the type of this
     *  port, or the token can be converted to that type
     *  losslessly, the token is sent to all receivers connected to the
     *  specified channel. Otherwise, IllegalActionException is thrown.
     *  Before putting the token into the destination receivers, this
     *  method also checks the type of the remote input port,
     *  and converts the token if necessary.
     *  The conversion is done by calling the
     *  convert() method of the type of the remote input port.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  Since it is possible for a thread to block while executing a put,
     *  it is important that the thread does not hold read access on
     *  the workspace when it is blocked. Thus this method releases
     *  read access on the workspace before calling put.
     *
     *  @param channelindex The index of the channel, from 0 to width-1.
     *  @param token The token to send.
     *  @exception IllegalActionException If the token to be sent cannot
     *   be converted to the type of this port, or if the token is null.
     *  @exception NoRoomException If there is no room in the receiver.
     */
    public void send(int channelindex, Token token)
            throws IllegalActionException, NoRoomException {
        if (token == null) {
            throw new IllegalActionException(this,
                    "Cannot send a null token.");
        }
	Receiver[][] farRec;
        try {
            try {
                _workspace.getReadAccess();
                int compare = TypeLattice.compare(token.getType(),
                        _resolvedType);
                if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                    throw new IllegalArgumentException(
                            "Run-time type checking failed. token type: "
                            + token.getType().toString() + ", port: "
                            + getFullName() + ", port type: " + getType().toString());
                }

                // Note that the getRemoteReceivers() method doesn't throw
                // any non-runtime exception.
                farRec = getRemoteReceivers();
                if (farRec == null || farRec[channelindex] == null) {
                    return;
                }
            } finally {
                _workspace.doneReading();
            }

            for (int j = 0; j < farRec[channelindex].length; j++) {
                TypedIOPort port =
                    (TypedIOPort)farRec[channelindex][j].getContainer();
                Type farType = port.getType();

                if (farType.isEqualTo(token.getType())) {
                    farRec[channelindex][j].put(token);
                } else {
                    Token newToken = farType.convert(token);
                    farRec[channelindex][j].put(newToken);
                }
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            // NOTE: This may occur if the port is not an output port.
            // Ignore...
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

    /** Constrain that the type of this port to be equal to or greater
     *  than the type of the specified Typeable object.
     *  @param less A Typeable object.
     */
    public void setTypeAtLeast(Typeable lesser) {
	Inequality ineq = new Inequality(lesser.getTypeTerm(),
                this.getTypeTerm());
	_constraints.add(ineq);
    }

    /** Constrain that the type of this port to be equal to or greater
     *  than the type represented by the specified InequalityTerm.
     *  @param typeTerm An InequalityTerm.
     */
    public void setTypeAtLeast(InequalityTerm typeTerm) {
	Inequality ineq = new Inequality(typeTerm, this.getTypeTerm());
	_constraints.add(ineq);
    }

    /** Constrain that the type of this port to be equal to or less
     *  than the argument.
     */
    public void setTypeAtMost(Type type) {
	Inequality ineq = new Inequality(this.getTypeTerm(),
                new TypeConstant(type));
	_constraints.add(ineq);
    }

    /** Set the type of this port to the type corresponding to the specified
     *  Class object.
     *  This method is write-synchronized on the workspace.
     *  @param c A Class.
     *  @exception IllegalArgumentException If the specified Class does not
     *   corresponds to a BaseType.
     *  @deprecated Use the method with a Type argument instead.
     */
    public void setTypeEquals(Class c) throws IllegalArgumentException {
	try {
	    Token token = (Token)c.newInstance();
	    setTypeEquals(token.getType());

	} catch (InstantiationException ie) {
	    throw new IllegalArgumentException(
                    "TypedIOPort.setTypeEquals(Class): Cannot create a " +
                    "token from the specified Class object. " + ie.getMessage());
	} catch (IllegalAccessException iae) {
	    throw new IllegalArgumentException(
                    "TypedIOPort.setTypeEquals(Class): Cannot create a " +
                    "token from the specified Class object. " + iae.getMessage());
	}
    }

    /** Set the type of this port. The type is represented by an instance
     *  of Type. If the type is BaseType.NaT, the determination of the type
     *  is left to type resolution.
     *  This method is write-synchronized on the workspace.
     *  @param type A Type.
     */
    public void setTypeEquals(Type type) {
	try {
	    _workspace.getWriteAccess();

	    if (type instanceof BaseType) {
	    	_declaredType = type;
	    } else {
		// new type is StructuredType
		StructuredType typeStruct = (StructuredType)type;

		if (typeStruct.isConstant()) {
		    _declaredType = type;
		} else {
		    // new type is a variable StructuredType.
		    try {
			if (typeStruct.getUser() == null) {
			    typeStruct.setUser(this);
			    _declaredType = type;
			} else {
			    // new type already has user, clone it.
			    StructuredType newType =
				(StructuredType)typeStruct.clone();
			    newType.setUser(this);
			    _declaredType = newType;
			}
		    } catch (IllegalActionException ex) {
			// since the user was null, this should never happen.
			throw new InternalErrorException(
                                "TypedIOPort.setTypeEquals: " + ex.getMessage());
                    } catch (CloneNotSupportedException ex2) {
                        throw new InternalErrorException(
                                "TypedIOPort.setTypeEquals: " +
                                "Cannot clone typeStruct" +
                                ex2.getMessage());
                    }
		}
	    }

	    if (!_resolvedType.isEqualTo(_declaredType)) {
		_notifyTypeListener(_resolvedType, _declaredType);
	    }
	    _resolvedType = _declaredType;

	} finally {
	    _workspace.doneWriting();
	}
    }

    /** Constrain that the type of this port is the same as the type
     *  of the specified Typeable object.
     *  @param equal A Typeable object.
     */
    public void setTypeSameAs(Typeable equal) {
	Inequality ineq = new Inequality(this.getTypeTerm(),
                equal.getTypeTerm());
	_constraints.add(ineq);
	ineq = new Inequality(equal.getTypeTerm(),
                this.getTypeTerm());
	_constraints.add(ineq);
    }

    /** Return the type constraints of this port in the form of a
     *  list of inequalities.
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
	return _constraints;
    }

    /** Return the type constraints of this port in the form of an
     *  enumeration of Inequality.
     *  @return An Enumeration of Inequality.
     *  @see ptolemy.graph.Inequality
     *  @deprecated Use typeConstraintList() instead.
     */
    public Enumeration typeConstraints() {
	return Collections.enumeration(typeConstraintList());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicate that the description(int) method should include
     *  information about the type of this port.
     */
    public static final int TYPE = 4096;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override parent method to ensure compatibility of the relation.
     *  This method is <i>not</i> synchronized on the
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
    protected void _checkLiberalLink(ComponentRelation relation)
            throws IllegalActionException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " TypedIOPort requires TypedIORelation.");
        }
        super._checkLiberalLink((TypedIORelation)relation);
    }

    /** Override parent method to ensure compatibility of the relation.
     *  This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation The relation to link to.
     *  @exception IllegalActionException If this port has no container or
     *   the relation is not an TypedIORelation, or the port already linked
     *   to a relation and is not a multiport, or if the relation has width
     *   not exactly one and the port is not a multiport, or the port is
     *   not in the same workspace as the relation.
     */
    protected void _checkLink(Relation relation)
            throws IllegalActionException {
        if (!(relation instanceof TypedIORelation)) {
            throw new IllegalActionException(this, relation,
                    "Attempt to link to an incompatible relation." +
                    " TypedIOPort requires TypedIORelation.");
        }
        super._checkLink((TypedIORelation)relation);
    }

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
     *  The declared type is the type set through setTypeEquals(). If this
     *  method is not called, the declared type is BaseType.NAT. The resolved
     *  type is the type of this port.  Both types are represented by the names
     *  of the corresponding tokens.
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
            _workspace.getReadAccess();
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
		result += _declaredType.toString();
                result += " resolved ";
		result += getType().toString();
		result += "}";
            }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Notify the type listener about type change.

    private void _notifyTypeListener(Type oldType, Type newType) {

	if (_typeListeners.size() > 0) {
	    TypeEvent event = new TypeEvent(this, oldType, newType);
	    Iterator listeners = _typeListeners.iterator();
	    while (listeners.hasNext()) {
		((TypeListener)listeners.next()).typeChanged(event);
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Type _declaredType = BaseType.NAT;
    private Type _resolvedType = BaseType.NAT;

    private TypeTerm _typeTerm = null;

    // Listeners for type change.
    private List _typeListeners = new LinkedList();

    // type constraints
    private List _constraints = new LinkedList();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class TypeTerm implements InequalityTerm {

	// pass the port reference in the constructor so it can be
	// returned by getAssociatedObject().
	private TypeTerm(TypedIOPort port) {
	    _port = port;
	}

	///////////////////////////////////////////////////////////////
	////                       public inner methods            ////

	/** Disallow the value of this term to be changed.
	 */
	public void fixValue() {
	    _valueFixed = true;
	    Object value = getValue();
	    if (value instanceof StructuredType) {
		((StructuredType)value).fixType();
	    }
	}

	/** Return this TypedIOPort.
	 *  @return A TypedIOPort.
	 */
	public Object getAssociatedObject() {
	    return _port;
	}

	/** Return the type of this TypedIOPort.
	 */
	public Object getValue() {
	    return getType();
        }

        /** Return this TypeTerm in an array if this term represent
	 *  a type variable. This term represents a type variable
	 *  if the type of this port is not set through setTypeEquals().
         *  If the type of this port is set, return an array of size zero.
	 *  @return An array of InequalityTerm.
         */
        public InequalityTerm[] getVariables() {
	    if (isSettable()) {
	    	InequalityTerm[] variable = new InequalityTerm[1];
	    	variable[0] = this;
	    	return variable;
	    }
	    return (new InequalityTerm[0]);
        }

        /** Reset the variable part of this type to the specified type.
	 *  @param e A Type.
         *  @exception IllegalActionException If the type is not settable,
	 *   or the argument is not a Type.
         */
        public void initialize(Object e)
		throws IllegalActionException {
	    if ( !isSettable()) {
	    	throw new IllegalActionException("TypeTerm.initialize: " +
                        "Cannot initialize a constant type.");
	    }

	    if ( !(e instanceof Type)) {
		throw new IllegalActionException("TypeTerm.initialize: " +
                        "The argument is not a Type.");
	    }

	    Type oldType = _resolvedType;
	    if (_declaredType == BaseType.NAT) {
	        _resolvedType = BaseType.NAT;
	    } else {
		// _declaredType is a StructuredType
		((StructuredType)_resolvedType).reset();
	    }

	    if (!oldType.isEqualTo(_resolvedType)) {
		_notifyTypeListener(oldType, _resolvedType);
	    }
        }

        /** Test if the type of this TypedIOPort can be changed.
	 *  The type can be changed if setTypeEquals() is not called,
	 *  or called with a BaseType.NAT argument.
         *  @return True if the type of this TypedIOPort can be changed;
	 *   false otherwise.
         */
        public boolean isSettable() {
	    if (_declaredType.isConstant() || _valueFixed) {
		return false;
	    }
	    return true;
        }

        /** Check whether the current type of this port is acceptable.
         *  A type is acceptable if it represents an instantiable object.
	 *  Any type is acceptable (including non-instantiable types)
	 *  if the associated port is not connected to anything.
         *  @return True if the current type is acceptable.
         */
        public boolean isValueAcceptable() {
            if (getType().isInstantiable()) {
                return true;
            }
            // For a disconnected port, any type is acceptable.
            if (_port.numLinks() == 0) {
                return true;
            }
            return false;
        }

        /** Set the type of this port.
	 *  @parameter e A Type.
         *  @exception IllegalActionException If the argument is not a
	 *   substitution instance of the type of this port.
         */
        public void setValue(Object e) throws IllegalActionException {
	    if ( !isSettable()) {
		throw new IllegalActionException(
                        "TypedIOPort$TypeTerm.setValue: The type is not " +
                        "settable.");
	    }

	    if ( !_declaredType.isSubstitutionInstance((Type)e)) {
		// FIXME: should throw TypeConflictException.
	        throw new IllegalActionException(
                        "TypedIOPort$TypeTerm.setValue: The new type is not a " +
                        "substitution instance of the type of this port. " +
                        "port: " + _port.getFullName() + " portType: " +
                        getValue() + " newType: " + e.toString());
	    }

	    Type oldType = _resolvedType;
	    if (_declaredType == BaseType.NAT) {
		_resolvedType = (Type)e;
	    } else {
		// _declaredType is a StructuredType
		((StructuredType)_resolvedType).updateType((StructuredType)e);
	    }

	    if (!oldType.isEqualTo((Type)e)) {
		_notifyTypeListener(oldType, _resolvedType);
	    }
        }

        /** Override the base class to give a description of the port
         *  and its type.
         *  @return A description of the port and its type.
         */
        public String toString() {
            return "(" + _port.toString() + ", " + getType() + ")";
        }

	/** Allow the value of this term to be changed, if this term is a
	 *  variable.
	 */
	public void unfixValue() {
	    _valueFixed = false;
	    Object value = getValue();
	    if (value instanceof StructuredType) {
		((StructuredType)value).unfixType();
	    }
	}

        ///////////////////////////////////////////////////////////////
        ////                       private inner variable          ////

        private TypedIOPort _port = null;
	private boolean _valueFixed = false;
    }
}

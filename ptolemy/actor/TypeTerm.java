/* An InequalityTerm that encapsulates a type.

 Copyright (c) 1997-1998 The Regents of the University of California.
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
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.graph.*;

//////////////////////////////////////////////////////////////////////////
//// TypeTerm
/**
An InequalityTerm that encapsulate a type.
This class models a term in an inequality constraint for type resolution.
It can be used in two ways. First, it can be used to represent the
resolved type of a TypedIOPort. In this case, the constructor taking a
TypedIOPort argument should be used. If the type of the TypedIOPort is
declared, this type term represents a type constant, otherwise, it
represents a type variable.  Second, this class can be used to model a
type constant not associated with a TypedIOPort. In this case, the
constructor taking a Class argument should be used.
<p>
An Inequality constraint can be formed by specifying two TypeTerms.

@author Yuhong Xiong
@version $Id$
@see ptolemy.actor.TypedIOPort
@see ptolemy.graph.InequalityTerm
@see ptolemy.graph.Inequality
*/

public class TypeTerm implements InequalityTerm {

    /** Construct a TypeTerm whose value is the resolved type of the
     *  specified TypedIOPort.
     *  @param port A TypedIOPort.
     */
    public TypeTerm(TypedIOPort port) {
	_port = port;
    }

    /** Construct a TypeTerm whose value is the specified type constant.
     *  @param type A Class representing a type in the type hierarchy.
     *  @exception IllegalArgumentException If the specified type is not
     *   a token type.
     */
    public TypeTerm(Class type) {
        if ( !TypeLattice.isAType(type)) {
            throw new IllegalArgumentException("TypeTerm: argument is not " +
                    "an acceptable type.");
        }
	_type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the TypedIOPort associated with this term. If this
     *  term is not associated with a TypedIOPort, return null.
     *  @return A TypedIOPort.
     */
    public TypedIOPort getPort() {
	return _port;
    }

    /** Return the type represented by this term. If this term is
     *  associated with a TypedIOPort, the returned type is the
     *  resolved type of that TypedIOPort. If this term is not
     *  associated with a TypedIOPort, the returned type is the type
     *  constant specified in the constructor.
     */
    public Object getValue() {
	if (_port != null) {
	    return _port.getResolvedType();
	} else {
	    return _type;
	}
    }

    /** Return this TypeTerm in an array if this term represent a type
     *  variable. This term represent a type variable only when this
     *  term is associated with a TypedIOPort, and the type of that port
     *  is undeclared.  If this term represent a type constant, return
     *  an array of size zero.
     */
    public InequalityTerm[] getVariables() {
    	if (_port != null) {
	    Class declared = _port.getDeclaredType();
	    if (declared == null) {
	    	InequalityTerm[] variable = new InequalityTerm[1];
	    	variable[0] = this;
	    	return variable;
	    }
	}
	return (new InequalityTerm[0]);
    }

    /** Test if this term represents a type variable.
     *  This term represent a type variable only when this term is
     *  associated with a TypedIOPort, and the type of that port
     *  is undeclared.
     *  @return True if this term represents a type variable; false
     *   otherwise.
     */
    public boolean isSettable() {
    	if (_port != null) {
	    Class declared = _port.getDeclaredType();
	    if (declared == null) {
		return true;
	    }
	}
	return false;
    }

    /** If this term is associated with an undeclared TypedIOPort,
     *  set the resolved type of that TypedIOPort.
     *  @exception If this term is not associated with a TypedIOPort, or the
     *  port has a declared type.
     */
    public void setValue(Object e)
	    throws IllegalActionException {
    	if (_port != null) {
	    Class declared = _port.getDeclaredType();
	    if (declared == null) {
		_port.setResolvedType((Class)e);
		return;
	    }
	}

	throw new IllegalActionException("TypeTerm.setValue: Cannot set "
		    + "the value of a type constant.");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variable                  ////

    private TypedIOPort _port = null;
    private Class _type = null;
}


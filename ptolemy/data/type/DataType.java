/** Type hierarchy of token classes.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.type;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.*;

//////////////////////////////////////////////////////////////////////////
//// DataType
/**
A class representing the basic data type of a token.  This type may be a 
constant type or a variable type that will be resolved by a DataTypeResolver.
The possible values of this data type are represented as members of the
type-safe enumeration DataTypeEnum.  A lattice representing the possible
lossless conversions of this data type can be returned using the
getTypeLattice method.  

@author Steve Neuendorffer, Yuhong Xiong
@version $Id$
@see ptolemy.graph.CPO
*/

public final class DataType extends Type implements InequalityTerm
{
    /** Create a new variable data type object with the same value as 
     *  BOTTOM.
     */
    public DataType() {
        _value = BOTTOM._value;
        _isSettable = true;
    }

    /** Create a new data type object with the same value as 
     *  the given data type.  If the given data type is constant, then this
     *  type will be constant also.  If the given data type is variable,
     *  then this data type will also be variable.
     */
    public DataType(DataType t) {
        _value = t._value;
        _isSettable = t._isSettable;
    }

    /** Create a new constant data type object with a new value.
     *  The value will have the given characteristics, but will not
     *  be a part of the type lattice.  This method is used to create
     *  the values of the lattice and their corresponding constant template
     *  data types.
     */
    private DataType(String name, boolean instantiable) {
        _value = new DataTypeEnum(name, instantiable);
        _isSettable = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the Object associated with this term. If this term is
     *  not associated with a particular Object, or it is not necessary
     *  to obtain the reference of the associated Object, this method
     *  can return <code>null</code>.
     *  @return an Object.
     */
    public Object getAssociatedObject() {
        return null;
    }
    
    /** Return the value of this term.  If this term is a constant,
     *  return that constant; if this term is a variable, return the
     *  current value of that variable; if this term is a function,
     *  return the evaluation of that function based on the current
     *  value of variables in the function.
     *  @return an Object representing an element in the underlying CPO.
     */
    public Object getValue() {
        return _value;
    }

    /** Return an array of variables contained in this term.
     *  If this term is a constant, return an array of size zero;
     *  if this term is a variable, return an array of size one that
     *  contains this variable; if this term is a function, return an
     *  array containing all the variables in the function.
     *  @return an array of InequalityTerms
     */
    public InequalityTerm[] getVariables() {
        InequalityTerm terms[];
        if(isSettable()) {
            terms = new InequalityTerm[1];
            terms[0] = this;
        } else {
            terms = new InequalityTerm[0];
        }
        return terms;
    }

    public boolean isEqualTo(Object t) {
        if (t instanceof DataType) 
            return _value == ((DataType) t)._value;
        else
            return false;
    }
        
    /** Check whether this term can be set to a specific element of the
     *  underlying CPO. Only variable terms are settable, constant
     *  and function terms are not.
     *  @return <code>true</code> if this term is a variable;
     *   <code>false</code> otherwise.
     */
    public boolean isSettable() {
        return _isSettable;
    }

    /** Check whether the current type of this term is acceptable,
     *  and return true if it is.  Normally, a type is acceptable
     *  if it represents an instantiable object.
     *  @return True if the current type is acceptable.
     */
    public boolean isTypeAcceptable() {
        return _value._instantiable;
    }

    /** Set the value of this term to the specified CPO element.
     *  Only terms consisting of a single variable can have their
     *  values set.
     *  @param e an Object representing an element in the
     *   underlying CPO.
     *  @exception IllegalActionException If this term is not a variable.
     */
    public void setValue(Object e) throws IllegalActionException {
        if(!isSettable()) {
            throw new IllegalActionException("Inequality term is not a " +
                    "variable!");
        }
        if(!(e instanceof DataTypeEnum)) {
            throw new InternalErrorException("Object must be a value " + 
                    "compatible with a DataType!");
        }
        _value = (DataTypeEnum) e;
    }    

    /** Return a string representing this type
     */
    public String toString() {
        String s = new String("DataType(");
        s += _value.getName();
        s += ")";
        if(isSettable()) 
            return "Var" + s;
        else 
            return "Const" + s;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public static methods                  ////

    /** Returns the type lattice. This method partially exposes the
     *  underlying object representing the type lattice, so it breaks
     *  information hiding. But this is necessary since the type
     *  resolution mechanism in the actor package needs access to the
     *  underlying lattice.
     *  @return a CPO modeling the type hierarchy.
     */
    public static CPO getTypeLattice() {
	return _typeLattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    
    public static final DataType BOOLEAN = new DataType("BOOLEAN", true);
    public static final DataType BOTTOM = new DataType("BOTTOM", false);
    public static final DataType COMPLEX = new DataType("COMPLEX", true);
    public static final DataType DOUBLE = new DataType("DOUBLE", true);
    public static final DataType INT = new DataType("INT", true);
    public static final DataType LONG = new DataType("LONG", true);
    public static final DataType NUMERICAL = new DataType("NUMERICAL", false);
    public static final DataType OBJECT = new DataType("OBJECT", true);
    public static final DataType RECORD = new DataType("RECORD", false);
    public static final DataType STRING = new DataType("STRING", true);
    public static final DataType TOP = new DataType("TOP", false);


    ///////////////////////////////////////////////////////////////////
    ////                   private static methods                  ////

    // construct the lattice of types.
    private static DirectedAcyclicGraph _setup() {

        DirectedAcyclicGraph _lattice = new DirectedAcyclicGraph();

	_lattice.add(BOOLEAN._value);
	_lattice.add(BOTTOM._value);		// NaT
	_lattice.add(COMPLEX._value);
	_lattice.add(DOUBLE._value);
	_lattice.add(INT._value);
	_lattice.add(LONG._value);
        _lattice.add(NUMERICAL._value);
        _lattice.add(OBJECT._value);
        _lattice.add(RECORD._value);
        _lattice.add(STRING._value);
        _lattice.add(TOP._value);

        _lattice.addEdge(OBJECT._value, TOP._value);
        _lattice.addEdge(BOTTOM._value, OBJECT._value);

        _lattice.addEdge(RECORD._value, TOP._value);
        _lattice.addEdge(BOTTOM._value, RECORD._value);

        _lattice.addEdge(STRING._value, TOP._value);
        _lattice.addEdge(BOOLEAN._value, STRING._value);
        _lattice.addEdge(BOTTOM._value, BOOLEAN._value);

	_lattice.addEdge(NUMERICAL._value, STRING._value);
	_lattice.addEdge(LONG._value, NUMERICAL._value);
	_lattice.addEdge(COMPLEX._value, NUMERICAL._value);
	_lattice.addEdge(DOUBLE._value, COMPLEX._value);
	_lattice.addEdge(INT._value, LONG._value);
	_lattice.addEdge(INT._value, DOUBLE._value);
        _lattice.addEdge(BOTTOM._value, INT._value);

	if ( !_lattice.isLattice()) {
	    throw new InternalErrorException("DataType: The type " +
		"hierarchy is not a lattice.");
	}
	return _lattice;
    }

    /** A type safe enumeration representing the possible types of a
     *  DataType object;
     */
    public class DataTypeEnum {
        private DataTypeEnum(String name, boolean instantiable) {
            _name = name;
            _instantiable = instantiable;
        }

        public String getName() {
            return _name;
        }

        private String _name;
        private boolean _instantiable;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static DirectedAcyclicGraph _typeLattice = _setup();
    
    private DataTypeEnum _value;
    private boolean _isSettable;
}


/** An Interface representing the Type of an object.

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

import ptolemy.graph.InequalityTerm;
import ptolemy.graph.Inequality;	/* Needed for javadoc */ 
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.IllegalActionException;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Type
/**
An interface representing the type of an object.  

@author Steve Neuendorffer
$Id$

*/

public class Type implements InequalityTerm
{
    /** Create a new type with variable datatype, initialized to bottom, and
     *  variable dimensiontype, initialized to bottom.
     */
    public Type() {
        this(new DataType(), new DimensionType());
    }

    /** Create a new type with the same characteristics as the given datatype
     *  and dimension type.
     */
    public Type(DataType datatype, DimensionType dimensiontype) {
        _datatype = new DataType(datatype);
        _dimensiontype = new DimensionType(dimensiontype);
    }

    /** Create a new type with the same characteristics as the given type
     */
    public Type(Type type) {
        this(type._datatype, type._dimensiontype);
    }

    /** Return the Object associated with this term. If this term is
     *  not associated with a particular Object, or it is not necessary
     *  to obtain the reference of the associated Object, this method
     *  can return <code>null</code>.
     *  @return an Object.
     */
    public Object getAssociatedObject() {
        return null;
    }

    public DataType getDataType() {
        return _datatype;
    }

    public DimensionType getDimensionType() {
        return _dimensiontype;
    }
    
    /** Return the value of this term.  If this term is a constant,
     *  return that constant; if this term is a variable, return the
     *  current value of that variable; if this term is a function,
     *  return the evaluation of that function based on the current
     *  value of variables in the function.
     *  @return an Object representing an element in the underlying CPO.
     */
    public Object getValue() {
        return this;
    }

    /** Return an array of variables contained in this term.
     *  If this term is a constant, return an array of size zero;
     *  if this term is a variable, return an array of size one that
     *  contains this variable; if this term is a function, return an
     *  array containing all the variables in the function.
     *  @return an array of InequalityTerms
     */
    public InequalityTerm[] getVariables() {
        // I don't think I really need this, because we aren't actually
        // using InequalitySolver, but here goes.
        InequalityTerm[] terms1 = _datatype.getVariables();
        InequalityTerm[] terms2 = _dimensiontype.getVariables();
        InequalityTerm[] terms =
            new InequalityTerm[terms1.length + terms2.length];
        int i, index = 0;
        for(i = 0; i < terms1.length; i++, index++) 
            terms[index] = terms1[i];
        for(i = 0; i < terms2.length; i++, index++) 
            terms[index] = terms2[i];
        return terms;
    }

    /** Check whether this term can be set to a specific element of the
     *  underlying CPO. Only variable terms are settable, constant
     *  and function terms are not.
     *  @return <code>true</code> if this term is a variable;
     *   <code>false</code> otherwise.
     */
    public boolean isSettable() {
        return _datatype.isSettable() || _dimensiontype.isSettable();
    }

    /** Check whether the current type of this term is acceptable,
     *  and return true if it is.  Normally, a type is acceptable
     *  if it represents an instantiable object.
     *  @return True if the current type is acceptable.
     */
    public boolean isTypeAcceptable() {
        return _datatype.isTypeAcceptable() && _datatype.isTypeAcceptable();
    }

    /** Set the value of this term to the specified CPO element.
     *  Only terms consisting of a single variable can have their
     *  values set.
     *  @param e an Object representing an element in the
     *   underlying CPO.
     *  @exception IllegalActionException If this term is not a variable.
     */
    public void setValue(Object e)
        throws IllegalActionException {
        if(!(e instanceof Type)) throw new InternalErrorException(
                "Object must be an instance of the Type class");
        Type t1 = (Type) e;
        _datatype.setValue(t1._datatype);
        _dimensiontype.setValue(t1._dimensiontype);
    }

    private DataType _datatype;
    private DimensionType _dimensiontype;
}


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
//// Data
/**
A class representing the basic data type of a token. 
A lattice representing the possible
lossless conversions of this data type can be returned using the
getTypeLattice method.  

@author Steve Neuendorffer, Yuhong Xiong
@version $Id$
@see ptolemy.graph.CPO
*/

public final class Data implements TypeValue
{
    /** Create a new constant data type object with a new value.
     *  The value will have the given characteristics, but will not
     *  be a part of the type lattice.  This method is used to create
     *  the values of the lattice and their corresponding constant template
     *  data types.
     */
    private Data(String name, boolean instantiable) {
        _name = name;
        _instantiable = instantiable;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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

    /** Return whether or not this object represents a DataType that is 
     *  instantiable.
     */
    public boolean isInstantiable() {
        return _instantiable;
    }

    /** Return a string representing this type
     */
    public String toString() {
        String s = new String("Data(");
        s += _name;
        s += ")";
        return s;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    public static methods                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    
    public static final Data BOOLEAN = new Data("BOOLEAN", true);
    public static final Data BOTTOM = new Data("BOTTOM", false);
    public static final Data COMPLEX = new Data("COMPLEX", true);
    public static final Data DOUBLE = new Data("DOUBLE", true);
    public static final Data INT = new Data("INT", true);
    public static final Data LONG = new Data("LONG", true);
    public static final Data NUMERICAL = new Data("NUMERICAL", false);
    public static final Data OBJECT = new Data("OBJECT", true);
    public static final Data RECORD = new Data("RECORD", false);
    public static final Data STRING = new Data("STRING", true);
    public static final Data TOP = new Data("TOP", false);


    ///////////////////////////////////////////////////////////////////
    ////                   private static methods                  ////

    // construct the lattice of types.
    private static DirectedAcyclicGraph _setup() {

        DirectedAcyclicGraph _lattice = new DirectedAcyclicGraph();

	_lattice.add(BOOLEAN);
	_lattice.add(BOTTOM);		// NaT
	_lattice.add(COMPLEX);
	_lattice.add(DOUBLE);
	_lattice.add(INT);
	_lattice.add(LONG);
        _lattice.add(NUMERICAL);
        _lattice.add(OBJECT);
        _lattice.add(RECORD);
        _lattice.add(STRING);
        _lattice.add(TOP);

        _lattice.addEdge(OBJECT, TOP);
        _lattice.addEdge(BOTTOM, OBJECT);

        _lattice.addEdge(RECORD, TOP);
        _lattice.addEdge(BOTTOM, RECORD);

        _lattice.addEdge(STRING, TOP);
        _lattice.addEdge(BOOLEAN, STRING);
        _lattice.addEdge(BOTTOM, BOOLEAN);

	_lattice.addEdge(NUMERICAL, STRING);
	_lattice.addEdge(LONG, NUMERICAL);
	_lattice.addEdge(COMPLEX, NUMERICAL);
	_lattice.addEdge(DOUBLE, COMPLEX);
	_lattice.addEdge(INT, LONG);
	_lattice.addEdge(INT, DOUBLE);
        _lattice.addEdge(BOTTOM, INT);

	if ( !_lattice.isLattice()) {
	    throw new InternalErrorException("Data: The type " +
		"hierarchy is not a lattice.");
	}
	return _lattice;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static DirectedAcyclicGraph _typeLattice = _setup();
    
    private String _name;
    private boolean _instantiable;
}


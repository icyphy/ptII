/* An experimental SBF object.

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (kienhuis@eecs.berkeley.edu)
@AcceptedRating Red (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.demo.QR;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

import ptolemy.data.type.BaseType;

//////////////////////////////////////////////////////////////////////////
//// ND_6

/**

This class defines an experimental SBF object that is part of the
compilation of the QR algorithm written in Matlab into a process
network. It is supposed to be generated automatically, but for the
moment it is generated manually. It currently serves as an example of
how SBF objects might look. This implementation is likely to change.

@author Bart Kienhuis
@version $Id$
*/

public class ND_6 extends TypedAtomicActor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct an actor that is an SBF object with the given container
     *  and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ND_6(TypedCompositeActor aContainer, String aName)
            throws IllegalActionException, NameDuplicationException
        {
            super(aContainer, aName);
            // ED_2
            out0 = new TypedIOPort(this, "out0", false, true);

            // ED_6
            out1 = new TypedIOPort(this, "out1", false, true);

            out0.setTypeEquals(BaseType.DOUBLE);
            out1.setTypeEquals(BaseType.DOUBLE);

            // The Type of these Parameter is set by the First
            // Token placed in the parameters when created
            parameter_N = new Parameter(this, "N", new IntToken(6));
            parameter_K = new Parameter(this, "K", new IntToken(10));
            parameter_d = new Parameter(this, "d", new IntToken(0));

            // Fill up R_1 with data.
            r_1.ReadMatrix( "Zeros_64x64" );
        }

    /** Initialize controller and state of the SBF object.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
	super.initialize();

	_t = 0;
	_returnValue = true;

	// Get the correct value from the parameters
	_N = ((IntToken) parameter_N.getToken()).intValue();
	_K = ((IntToken) parameter_K.getToken()).intValue();
	_d = ((IntToken) parameter_d.getToken()).intValue();

	if (_t == 0){
	    _t0 = 1;
	    _t1 = 1;
	    _t2 = 1;
	}

	if (_t2 == 1) {
	    i =  i+1;
	    if (i > UB_i) {
		_t1 = 1;
	    }
	}

	if (_t1 == 1) {
	    j =  j+1;
	    if (j > UB_j) {
		_t0 = 1;
	    }
	}

	if (_t0 == 1) {
	    _t = _t+1;
	    if (_t == 2) {
		_returnValue = false;
	    }
	}

	if (_t0 == 1) {
	    _t0 = 0;
	    j = 1;
	    UB_j = _N;
	}

	if (_t1 == 1) {
	    _t1 = 0;
	    i = j;
	    UB_i = _N;
	}
    }

    /** Fire the SBF object. In terms of an SBF object, this means the
        execution of the <i>binding function</i>. This function
        associates inputs and outputs of the SBF object to a function
        of function repertoire of the SBF object. It does this binding
        on the basis of the content of the state of the SBF object.
        @exception IllegalActionException Not Thrown.
    */
    public void fire() throws IllegalActionException {
	_value = r_1.retrieve( r_1.atKey(j, i) );

	if ( -i + j == 0 ) {
	    out0.broadcast( new DoubleToken( _value ) );
	}

	if ( i - j - 1 >= 0 ) {
	    out1.broadcast( new DoubleToken( _value ) );
	}
    }

    /** Postfire the SBF object. In terms of the SBF object, this
        means the execution of the <i>transition function</i>.  This
        function determines the next state of the SBF object on the
        basis of the current state. As such, this method represents
        the controller that governs the enabling sequence of
        functions. It controls the sequence of in which the SBF object
        moves from the current state to another state.  <p> The
        current implementation doesn't run indefinitely, but
        ends. When the end condition is reached, the method returns
        <i>false</i> to indicate it is done; otherwise <i>true</i> is
        returned.
    */
    public boolean postfire() {


	if (_t == 0){
	    _t0 = 1;
	    _t1 = 1;
	    _t2 = 1;
	}

	if (_t2 == 1) {
	    i =  i+1;
	    if (i > UB_i) {
		_t1 = 1;
	    }
	}

	if (_t1 == 1) {
	    j =  j+1;
	    if (j > UB_j) {
		_t0 = 1;
	    }
	}

	if (_t0 == 1) {
	    _t = _t+1;
	    if (_t == 2) {
		_returnValue = false;
	    }
	}

	if (_t0 == 1) {
	    _t0 = 0;
	    j = 1;
	    UB_j = _N;
	}

	if (_t1 == 1) {
	    _t1 = 0;
	    i = j;
	    UB_i = _N;
	}
	return _returnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // -- Part of the Actor
    /** Output Port Domain ED_1. */
    public TypedIOPort out0;

    /** Output Port Domain ED_11. */
    public TypedIOPort out1;

    /** Number of antennas parameter of the SBF Object. */
    public Parameter parameter_N;

    /** Number of Iterations parameter of the SBF Object. */
    public Parameter parameter_K;

    /** Debug parameter of the SBF Object. */
    public Parameter parameter_d;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void Vectorize(double arg0, double arg1) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // -- Get a private copy of the parameters
    private int _N;
    private int _K;
    private int _d;

    // -- private data from the actors
    private int UB_k;
    private int UB_j;
    private int UB_i;

    private int k;
    private int j;
    private int i;

    private int _t;
    private int _t0;
    private int _t1;
    private int _t2;

    private double _value;

    private ArrayIndex r_1 = new ArrayIndex();

    private boolean _returnValue = true;

    private int _count = 0;


}

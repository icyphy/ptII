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
//// ND_66

/**

This class defines an experimental SBF object that is part of the
compilation of the QR algorithm written in Matlab into a process
network. It is supposed to be generated automatically, but for the
moment it is generated manually. It currently serves as an example of
how SBF objects might look. This implementation is likely to change.

@author Bart Kienhuis
@version $Id$
*/

public class ND_66 extends TypedAtomicActor {

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
    public ND_66(TypedCompositeActor aContainer, String aName)
            throws IllegalActionException, NameDuplicationException
        {
            super(aContainer, aName);
            in0 = new TypedIOPort(this, "in0", true, false);
            in1 = new TypedIOPort(this, "in1", true, false);
            in2 = new TypedIOPort(this, "in2", true, false);
            in3 = new TypedIOPort(this, "in3", true, false);
            in4 = new TypedIOPort(this, "in4", true, false);
            in5 = new TypedIOPort(this, "in5", true, false);

            out0 = new TypedIOPort(this, "out0", false, true);
            out1 = new TypedIOPort(this, "out1", false, true);
            out2 = new TypedIOPort(this, "out2", false, true);
            out3 = new TypedIOPort(this, "out3", false, true);
            out4 = new TypedIOPort(this, "out4", false, true);

            in0.setTypeEquals(BaseType.DOUBLE);
            in1.setTypeEquals(BaseType.DOUBLE);
            in2.setTypeEquals(BaseType.DOUBLE);
            in3.setTypeEquals(BaseType.DOUBLE);
            in4.setTypeEquals(BaseType.DOUBLE);
            in5.setTypeEquals(BaseType.DOUBLE);

            out0.setTypeEquals(BaseType.DOUBLE);
            out1.setTypeEquals(BaseType.DOUBLE);
            out2.setTypeEquals(BaseType.DOUBLE);
            out3.setTypeEquals(BaseType.DOUBLE);
            out4.setTypeEquals(BaseType.DOUBLE);

            // The Type of these Parameter is set by the First
            // Token placed in the parameters when created
            parameter_N = new Parameter(this, "N", new IntToken(6));
            parameter_K = new Parameter(this, "K", new IntToken(10));
            parameter_d = new Parameter(this, "d", new IntToken(0));
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
	    _t3 = 1;
	}

	if (_t3 == 1) {
	    i =  i+1;
	    if (i > UB_i) {
		_t2 = 1;
	    }
	}

	if (_t2 == 1) {
	    j =  j+1;
	    if (j > UB_j) {
		_t1 = 1;
	    }
	}

	if (_t1 == 1) {
	    k =  k+1;
	    if (k > UB_k) {
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
	    k = 1;
	    UB_k = _K;
	}

	if (_t1 == 1) {
	    _t1 = 0;
	    j = 1;
	    UB_j = _N - 1;
	}

	if (_t2 == 1) {
	    _t2 = 0;
	    i = j + 1;
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
	if  ( k - 2 >= 0 ) { // ED_5
	    _argIn0 = ((DoubleToken) in0.get(0)).doubleValue();
	    r_3.store( _argIn0, r_3.atKey(k - 1, j, i) );
	    _argIn0 = r_3.retrieve( r_3.atKey(k - 1, j, i) );
	}

	if  ( k - 1 == 0 ) { // ED_6
	    _argIn0 = ((DoubleToken) in1.get(0)).doubleValue();
	    r_1.store( _argIn0, r_1.atKey(j, i) );
	    _argIn0 = r_1.retrieve( r_1.atKey(j, i) );
	}

	if  ( j - 2 >= 0 ) { // ED_7_in
	    _argIn1 = ((DoubleToken) in2.get(0)).doubleValue();
	    x_3.store( _argIn1, x_3.atKey(k, j-1, i) );
	    _argIn1 = x_3.retrieve( x_3.atKey(k, j-1, i) );
	}

	if  ( j - 1 == 0 ) { // ED_8_in
	    _argIn1 = ((DoubleToken) in3.get(0)).doubleValue();
	    x_1.store( _argIn1, x_1.atKey(k, i) );
	    _argIn1 = x_1.retrieve( x_1.atKey(k, i) );
	}

	if  ( i - j - 2 >= 0 ) { // ED_9_in
	    _argIn2 = ((DoubleToken) in4.get(0)).doubleValue();
	    t_2.store( _argIn2, t_2.atKey(k,j,i-1) );
	    _argIn2 = t_2.retrieve( t_2.atKey(k,j,i-1) );
	}

	if  ( -i + j + 1 == 0 ) { // ED_10_in
	    _argIn2 = ((DoubleToken) in5.get(0)).doubleValue();
	    t_1.store( _argIn2, t_1.atKey(k, j) );
	    _argIn2 = t_1.retrieve( t_1.atKey(k, j) );
	}

	_Rotate( _argIn0, _argIn1, _argIn2 ); // 3 arguments outs

	if ( _K - k - 1 >= 0 ) {
	    out0.broadcast( new DoubleToken( _argOut0 ) );
	}

	if ( -_K + k == 0 ) {
	    out1.broadcast( new DoubleToken( _argOut0 ) );
	}

	if ( -i + j + 1 == 0 ) {
	    out2.broadcast( new DoubleToken( _argOut1 ) );
	}

	if ( i - j - 2 >= 0 ) {
	    out3.broadcast( new DoubleToken( _argOut1 ) );
	}

	if ( _N - i - 1 >= 0 ) {
	    out4.broadcast( new DoubleToken( _argOut2 ) );
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
        _returnValue = true;
	if (_t == 0){
	    _t0 = 1;
	    _t1 = 1;
	    _t2 = 1;
	    _t3 = 1;
	}

	if (_t3 == 1) {
	    i =  i+1;
	    if (i > UB_i) {
		_t2 = 1;
	    }
	}

	if (_t2 == 1) {
	    j =  j+1;
	    if (j > UB_j) {
		_t1 = 1;
	    }
	}

	if (_t1 == 1) {
	    k =  k+1;
	    if (k > UB_k) {
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
	    k = 1;
	    UB_k = _K;
	}

	if (_t1 == 1) {
	    _t1 = 0;
	    j = 1;
	    UB_j = _N - 1;
	}

	if (_t2 == 1) {
	    _t2 = 0;
	    i = j + 1;
	    UB_i = _N;
	}
        return _returnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    // -- Part of the Actor

    /** Input Port Domain ED_5_in. */
    public TypedIOPort in0;

    /** Input Port Domain ED_6_in. */
    public TypedIOPort in1;

    /** Input Port Domain ED_7_in. */
    public TypedIOPort in2;

    /** Input Port Domain ED_8_in. */
    public TypedIOPort in3;

    /** Input Port Domain ED_9_in. */
    public TypedIOPort in4;

    /** Input Port Domain ED_10_in. */
    public TypedIOPort in5;

    /** Output Port Domain ED_5. */
    public TypedIOPort out0;

    /** Output Port Domain ED_12. */
    public TypedIOPort out1;

    /** Output Port Domain ED_13. */
    public TypedIOPort out2;

    /** Output Port Domain ED_17. */
    public TypedIOPort out3;

    /** Output Port Domain ED_9. */
    public TypedIOPort out4;

    /** Number of antennas parameter of the SBF Object. */
    public Parameter parameter_N;

    /** Number of Iterations parameter of the SBF Object. */
    public Parameter parameter_K;

    /** Debug parameter of the SBF Object. */
    public Parameter parameter_d;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _Rotate(double arg0, double arg1, double arg2) {
	_argOut0 = Math.cos(arg2) * arg0   -   Math.sin(arg2) * arg1;
	_argOut1 = Math.sin(arg2) * arg0   +   Math.cos(arg2) * arg1;
	_argOut2 = arg2;
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
    private int _t3;

    private double _argIn0;
    private double _argIn1;
    private double _argIn2;
    private double _argOut0;
    private double _argOut1;
    private double _argOut2;

    private ArrayIndex r_1 = new ArrayIndex();
    private ArrayIndex r_3 = new ArrayIndex();
    private ArrayIndex x_3 = new ArrayIndex();
    private ArrayIndex x_1 = new ArrayIndex();
    private ArrayIndex t_1 = new ArrayIndex();
    private ArrayIndex t_2 = new ArrayIndex();

    private boolean _returnValue = true;

}

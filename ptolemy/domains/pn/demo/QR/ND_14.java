package ptolemy.domains.pn.sbf.lib;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

public class ND_14 extends TypedAtomicActor {

    // -- Part of the Actor
    public TypedIOPort out0; // ED_4
    public TypedIOPort out1; // ED_8
   
   // -- Public interface of the Actor
    public Parameter parameter_N;
    public Parameter parameter_K;
    public Parameter parameter_d;

    // -- Get a private copy of the parameters
    private int _N;
    private int _K;
    private int _d;

    // -- private data from the actors
    private int UB_k;
    private int UB_j;

    private int k;
    private int j;
 
    private int _t;
    private int _t0;
    private int _t1;
    private int _t2;

    private double _value;

    private boolean _returnValue = true;

    private ArrayIndex x_1 = new ArrayIndex();
    
    private int _count = 0;

    public ND_14(TypedCompositeActor aContainer, String aName)
	throws IllegalActionException, NameDuplicationException   
    {
	super(aContainer, aName);
	out0 = new TypedIOPort(this,"out0",false,true);
	out1 = new TypedIOPort(this,"out1",false,true);

        out0.setTypeEquals(BaseType.DOUBLE);
	out1.setTypeEquals(BaseType.DOUBLE);

	// The Type of these Parameter is set by the First
	// Token placed in the parameters when created
	parameter_N = new Parameter(this,"N", new IntToken(6));
	parameter_K = new Parameter(this,"K", new IntToken(10));
	parameter_d = new Parameter(this,"d", new IntToken(0));

	x_1.ReadMatrix( "U_1000x16" );

	System.out.println(" --- Process ND_14 Created -- ");
    }
 
    public void initialize() throws IllegalActionException {	
	super.initialize();

	_t = 0;
	_returnValue = true;

	// Get the correct value from the parameters
	_N = ((IntToken) parameter_N.getToken()).intValue();
	_K = ((IntToken) parameter_K.getToken()).intValue();
	_d = ((IntToken) parameter_d.getToken()).intValue();

	// System.out.println(" -- ND_14 K: " + _K);
	// System.out.println(" -- ND_14 N: " + _N);

	if (_t == 0){
	    _t0 = 1;
	    _t1 = 1;
	    _t2 = 1;
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
	    _t = _t+1;      // t equals 1 means counter is set to all lower bounds.
	    if (_t == 2) { // t equals n means counter finished completed n-1 cycles.
		// sbfQuit();
		// System.out.println(" --- (INIT) FINISHED NS_14 ");
		_returnValue = false;
		//(Thread.currentThread()).suspend();
		// sbfExit();
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
	    UB_j = _N;
	}
	// System.out.println(" -- Init of ND_14 -- ");
    }

    public void fire() throws IllegalActionException {	
	// System.out.println(" -- Firing ND_14 -- ");

	//	double tmp_out0 = ((Double) x_1.get( ArrayIndex.m(k, j) )).doubleValue();
	_value = x_1.retrieve( x_1.atKey(k, j) );

	if ( j - 1 == 0 ) {
	    out0.broadcast( new DoubleToken( _value ) );
	}

	if ( j - 2 >= 0 ) {
	    out1.broadcast( new DoubleToken( _value ) );
	}

    }

    public boolean prefire() {	
	// System.out.println(" -- PRE Firing ND_14 -- ");
	return true;
    }

    // The State Update
    public boolean postfire() {	

	if (_t == 0){
	    _t0 = 1;
	    _t1 = 1;
	    _t2 = 1;
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
	    _t = _t+1;      // t equals 1 means counter is set to all lower bounds.
	    if (_t == 2) { // t equals n means counter finished completed n-1 cycles.
		// sbfQuit();
		// System.out.println(" --- FINISHED NS_14 ");
		// (Thread.currentThread()).suspend();
		_returnValue = false;
		// System.exit(0);
		// sbfExit();
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
	    UB_j = _N;
	}

	// System.out.println(" -- POST Firing ND_14 -- _count " + _count++ );
	//		   + " _t0 " + _t0 + "  _t1 " + _t1);
	// System.out.println(" -- POST Firing ND_14 -- k: " + k + " j: " + j);
	return _returnValue;
    }
    
}

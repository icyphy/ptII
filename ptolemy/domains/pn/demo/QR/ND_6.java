package ptolemy.domains.pn.demo.QR;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

import ptolemy.data.type.BaseType;

public class ND_6 extends TypedAtomicActor {

    // -- Part of the Actor
    public TypedIOPort out0; // ED_1
    public TypedIOPort out1; // ED_11

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

    public ND_6(TypedCompositeActor aContainer, String aName)
	throws IllegalActionException, NameDuplicationException   
    {
	super(aContainer, aName);
	// ED_2
	out0 = new TypedIOPort(this,"out0",false,true);

	// ED_6
	out1 = new TypedIOPort(this,"out1",false,true);


        out0.setTypeEquals(BaseType.DOUBLE);
        out1.setTypeEquals(BaseType.DOUBLE);


	// The Type of these Parameter is set by the First
	// Token placed in the parameters when created
	parameter_N = new Parameter(this,"N", new IntToken(6));
	parameter_K = new Parameter(this,"K", new IntToken(10));
	parameter_d = new Parameter(this,"d", new IntToken(0));

	System.out.println(" --- Process ND_6 Created -- ");
	r_1.ReadMatrix( "Zeros_64x64" );    
    }
 
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
	    _t = _t+1;      // t equals 1 means counter is set to all lower bounds.
	    if (_t == 2) { // t equals n means counter finished completed n-1 cycles.
		// System.out.println(" -- (INIT) FINISHED ND_6 -- ");
		_returnValue = false;
		// sbfExit();
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

	// System.out.println(" -- INIT of ND_6 DONE -- ");
    }

    public void fire() throws IllegalActionException {	
	// System.out.println(" -- Firing ND_6 -- ");

	_value = r_1.retrieve( r_1.atKey(j, i) );

	// System.out.println(" --- retrieved value --- " + _value );
	if ( -i + j == 0 ) {
	    out0.broadcast( new DoubleToken( _value ) );
	}

	if ( i - j - 1 >= 0 ) {
	    out1.broadcast( new DoubleToken( _value ) );
	}
	// System.out.println(" --- DONE WITH Firing ND_6 --- ");
    }

    public boolean prefire() {	
	// System.out.println(" -- PRE Firing ND_6 -- ");
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
	    _t = _t+1;      // t equals 1 means counter is set to all lower bounds.
	    if (_t == 2) { // t equals n means counter finished completed n-1 cycles.
		// System.out.println(" -- FINISHED ND_6 -- ");
		// (Thread.currentThread()).suspend();
		_returnValue = false;
		// System.exit(0);
		// sbfExit();
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


	// System.out.println(" -- POST Firing ND_6 -- _count " + _count++);
	//	   + " _t0 " + _t0 + "  _t1 " + _t1);
	return _returnValue;
    }
 
    private void Vectorize(double arg0, double arg1) {
    }
}

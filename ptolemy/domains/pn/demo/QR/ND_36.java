package ptolemy.domains.pn.demo.QR;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;

public class ND_36 extends TypedAtomicActor {

    // -- Part of the Actor
    public TypedIOPort in0; // ED_1_in
    public TypedIOPort in1; // ED_2_in
    public TypedIOPort in2; // ED_3_in
    public TypedIOPort in3; // ED_4_in

    public TypedIOPort out0; // ED_1
    public TypedIOPort out1; // ED_11
    public TypedIOPort out2; // ED_10

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

    private double _argIn0;
    private double _argIn1;
    private double _argOut0;
    private double _argOut1;
    private double _argOut2;

    private ArrayIndex r_1 = new ArrayIndex();
    private ArrayIndex r_2 = new ArrayIndex();
    private ArrayIndex x_3 = new ArrayIndex();
    private ArrayIndex x_1 = new ArrayIndex();

    private boolean _returnValue = true;

    public ND_36(TypedCompositeActor aContainer, String aName)
	throws IllegalActionException, NameDuplicationException   
    {

	super(aContainer, aName);

        System.out.println(" -- THROUGH CONSTRUCTOR ND_36 -- ");

	in0 = new TypedIOPort(this,"in0",true,false);
	in1 = new TypedIOPort(this,"in1",true,false);
	in2 = new TypedIOPort(this,"in2",true,false);
	in3 = new TypedIOPort(this,"in3",true,false);

	out0 = new TypedIOPort(this,"out0",false,true);
	out1 = new TypedIOPort(this,"out1",false,true);
	out2 = new TypedIOPort(this,"out2",false,true);


        in0.setTypeEquals(BaseType.DOUBLE);
        in1.setTypeEquals(BaseType.DOUBLE);
        in2.setTypeEquals(BaseType.DOUBLE);
        in3.setTypeEquals(BaseType.DOUBLE);
        
	out0.setTypeEquals(BaseType.DOUBLE);
	out1.setTypeEquals(BaseType.DOUBLE);
	out2.setTypeEquals(BaseType.DOUBLE);

	// The Type of these Parameter is set by the First
	// Token placed in the parameters when created
	parameter_N = new Parameter(this,"N", new IntToken(6));
	parameter_K = new Parameter(this,"K", new IntToken(10));
	parameter_d = new Parameter(this,"d", new IntToken(0));

	System.out.println(" --- Process ND_36 Created -- ");
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
		// sbfExit();
		// System.out.println(" -- FINISHED ND_36 -- ");
		_returnValue = false;
		//(Thread.currentThread()).suspend();
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
	// System.out.println(" -- ND_36 INIT DONE -- ");
    }

    public void fire() throws IllegalActionException {	
	// System.out.println(" -- Firing ND_36 -- ");

	if  ( k - 2 >= 0 ) { // ED_1_in
	    _argIn0 = ((DoubleToken) in0.get(0)).doubleValue();
	    r_2.store( _argIn0, r_2.atKey(k - 1, j) );
	    _argIn0 = r_2.retrieve( r_2.atKey(k - 1, j) );
	}

	if  ( k - 1 == 0 ) { //ED_2_in
	    _argIn0 = ((DoubleToken) in1.get(0)).doubleValue();
	    r_1.store( _argIn0, r_1.atKey(j, j) );
	    _argIn0 = r_1.retrieve( r_1.atKey(j, j) );
	}

	if  ( j - 2 >= 0 ) { //ED_3_in
	    _argIn1 = ((DoubleToken) in2.get(0)).doubleValue();
	    x_3.store( _argIn1, x_3.atKey(k, j - 1, j) );
	    _argIn1 = x_3.retrieve( x_3.atKey(k, j - 1, j) );
	}

	if  ( j - 1 == 0 ) { //ED_4_in
	    _argIn1 = ((DoubleToken) in3.get(0)).doubleValue();
	    x_1.store( _argIn1, x_1.atKey(k, j) );
	    _argIn1 = x_1.retrieve( x_1.atKey(k, j) );
	}

	// Vectorize( in0, in1, out0, out1, out2 );
	Vectorize( _argIn0, _argIn1 );

	if ( _K - k - 1 >= 0 ) { //ED_1
	   out0.broadcast( new DoubleToken( _argOut0 ) );
	}

	if ( -_K + k == 0 ) { //ED_11
	    // System.out.println(" ++++ ND_36 SEND TOKEN TO ND_86 ++ ED_11 ++  ");
	    out1.broadcast( new DoubleToken( _argOut0 ) );
	}

	if ( _N - j - 1 >= 0 ) { //ED_10
	    out2.broadcast( new DoubleToken( _argOut2 ) );
	}

    }

    public boolean prefire() {	
	// System.out.println(" -- PRE Firing ND_36 -- ");
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
		// sbfExit();
		System.out.println(" -- FINISHED ND_36 -- ");
		_returnValue = false;
		// (Thread.currentThread()).suspend();
		// System.exit(0);
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

	//System.out.println(" -- POST Firing ND_36 -- _t " + _t 
	//	   + " _t0 " + _t0 + "  _t1 " + _t1);
	return _returnValue;
    }
 
    private void Vectorize(double arg0, double arg1) {
	// System.out.println(" Vectorize IN 0: "  + arg0 +  " 1: " + arg1 );
	_argOut2 = -1*Math.atan2( arg1,arg0 );
	_argOut0 = Math.cos(_argOut2) * arg0   -   Math.sin(_argOut2) * arg1;
	_argOut1 = 0.0;
	// System.out.println(" Vectorize OUT 0: "  + _argOut0 +  " 1: " 
	//		   + _argOut1 + " 2: " + _argOut2);
    }
}

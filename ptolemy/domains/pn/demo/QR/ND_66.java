package ptolemy.domains.pn.demo.QR;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

import ptolemy.data.type.BaseType;

public class ND_66 extends TypedAtomicActor {

    // -- Part of the Actor
    public TypedIOPort in0; // ED_5_in
    public TypedIOPort in1; // ED_6_in
    public TypedIOPort in2; // ED_7_in
    public TypedIOPort in3; // ED_8_in
    public TypedIOPort in4; // ED_9_in
    public TypedIOPort in5; // ED_10_in

    public TypedIOPort out0; // ED_5
    public TypedIOPort out1; // ED_12
    public TypedIOPort out2; // ED_3
    public TypedIOPort out3; // ED_7
    public TypedIOPort out4; // ED_9

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

    public ND_66(TypedCompositeActor aContainer, String aName)
	throws IllegalActionException, NameDuplicationException   
    {
	super(aContainer, aName);
	in0 = new TypedIOPort(this,"in0",true,false);
	in1 = new TypedIOPort(this,"in1",true,false);
        in2 = new TypedIOPort(this,"in2",true,false);
	in3 = new TypedIOPort(this,"in3",true,false);
	in4 = new TypedIOPort(this,"in4",true,false);
 	in5 = new TypedIOPort(this,"in5",true,false);

	out0 = new TypedIOPort(this,"out0",false,true);
	out1 = new TypedIOPort(this,"out1",false,true);
	out2 = new TypedIOPort(this,"out2",false,true);
	out3 = new TypedIOPort(this,"out3",false,true);
	out4 = new TypedIOPort(this,"out4",false,true);

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
	parameter_N = new Parameter(this,"N", new IntToken(6));
	parameter_K = new Parameter(this,"K", new IntToken(10));
	parameter_d = new Parameter(this,"d", new IntToken(0));

	System.out.println(" --- Process ND_66 Created -- ");
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
	    _t = _t+1;      // t equals 1 means counter is set to all lower bounds.
	    if (_t == 2) { // t equals n means counter finished completed n-1 cycles.
		// System.out.println(" -- FINISHED ND_66 -- ");
		_returnValue = false;
		// (Thread.currentThread()).suspend();
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
	    UB_j = _N - 1;
	}

	if (_t2 == 1) {
	    _t2 = 0;
	    i = j + 1;
	    UB_i = _N;
	}
	// System.out.println(" -- ND_66 INIT DONE -- ");
    }

    public void fire() throws IllegalActionException {	
	System.out.println(" -- Firing ND_66 -- ");

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
	    // System.out.println(" -- ED7 ");
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
	    // System.out.println(" -- out0/ED_5 r "+ _argOut0);
	    out0.broadcast( new DoubleToken( _argOut0 ) );
	}

	if ( -_K + k == 0 ) {
	    //System.out.println(" -- out1/ED_12 r " + _argOut0);
	    out1.broadcast( new DoubleToken( _argOut0 ) );
	}

	if ( -i + j + 1 == 0 ) {
	    // System.out.println(" -- out2/ED_3 x " + _argOut1);
	    out2.broadcast( new DoubleToken( _argOut1 ) );
	}

	if ( i - j - 2 >= 0 ) {
	    // System.out.println(" -- out3/ED_7 x " + _argOut1);
	    out3.broadcast( new DoubleToken( _argOut1 ) );
	}

	if ( _N - i - 1 >= 0 ) {
	    // System.out.println(" -- out4/ED_9 t " + _argOut2);
	    out4.broadcast( new DoubleToken( _argOut2 ) );
	}



    }

    public boolean prefire() {	
	// System.out.println(" -- PRE Firing ND_66 -- ");
	return true;
    }

    // The State Update
    public boolean postfire() {	

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
	    _t = _t+1;      // t equals 1 means counter is set to all lower bounds.
	    if (_t == 2) { // t equals n means counter finished completed n-1 cycles.
		// System.out.println(" -- FINISHED ND_66 -- ");
	        _returnValue = false;
		// (Thread.currentThread()).suspend();
		// sbfQuit();
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
	    UB_j = _N - 1;
	}

	if (_t2 == 1) {
	    _t2 = 0;
	    i = j + 1;
	    UB_i = _N;
	}
	
	// System.out.println(" -- POST ND_66 k:" + k + " j:" +j + " i: " + i);

	//	System.out.println(" -- POST Firing ND_66 -- _t " + _t 
	//		   + " _t0 " + _t0 + "  _t1 " + _t1
	//		   + " _t2 " + _t2 + "  _t3 " + _t3 );
	return _returnValue;
    }
 
    private void _Rotate(double arg0, double arg1, double arg2) {
	// System.out.println(" Rotate IN 0: "  + arg0 +  " 1: " + arg1 + " 2:" + arg2);
	_argOut0 = Math.cos(arg2) * arg0   -   Math.sin(arg2) * arg1;
	_argOut1 = Math.sin(arg2) * arg0   +   Math.cos(arg2) * arg1;
	_argOut2 = arg2;

	// System.out.println(" Rotate OUT 0: "  + _argOut0 +  " 1: " 
	//			   + _argOut1 + " 2: " + _argOut2);
    }
}

package ptolemy.domains.pn.sbf.lib;

import java.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.data.expr.Parameter;

import ptolemy.data.type.BaseType;

public class ND_86 extends TypedAtomicActor {

    // -- Part of the Actor
    public TypedIOPort in0; // ED_1_in
    public TypedIOPort in1; // ED_2_in

    public TypedIOPort out0; // ED_2_in

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

    private int k = 0;
    private int j = 0;
    private int i = 0;
 
    private int _t;
    private int _t0;
    private int _t1;
    private int _t2;

    private double _argIn0;

    private ArrayIndex r_2 = new ArrayIndex();
    private ArrayIndex r_3 = new ArrayIndex();
    private ArrayIndex Sink_1 = new ArrayIndex();
 
    private boolean _returnValue = true;

    public ND_86(TypedCompositeActor aContainer, String aName)
	throws IllegalActionException, NameDuplicationException   
    {
	super(aContainer, aName);
	in0 = new TypedIOPort(this,"in0",true,false);
	in1 = new TypedIOPort(this,"in1",true,false);

	out0 = new TypedIOPort(this,"out0",false,true);


        in0.setTypeEquals(BaseType.DOUBLE);
        in1.setTypeEquals(BaseType.DOUBLE);

        out0.setTypeEquals(BaseType.DOUBLE);

	// Declare the Parameters
	// The Type of these Parameter is set by the First
	// Token placed in the parameters when created
	parameter_N = new Parameter(this,"N", new IntToken(6));
	parameter_K = new Parameter(this,"K", new IntToken(10));
	parameter_d = new Parameter(this,"d", new IntToken(0));

	System.out.println(" --- Process ND_86 Created -- ");
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
		// sbfQuit();
		// sbfExit();
		// System.out.println(" -- FINISHED ND_86 -- ");
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

	// System.out.println(" -- ND_86 INIT DONE -- ");
    }

    public void fire() throws IllegalActionException {	
	// System.out.println(" -- Firing ND_86 -- i: " + i + " j: " + j);


	if  ( -i + j == 0 ) { // ED_11_in
	    // System.out.println(" -- Firing ND_86 -- ED_11 ");
	    _argIn0 = ((DoubleToken) in0.get(0)).doubleValue();
	    // System.out.println(" -- RETRIEVED FOR ND_86 -- ED_11 ");
	    r_2.store( _argIn0, r_2.atKey(_K,j) );
	    _argIn0 = r_2.retrieve( r_2.atKey(_K, j) );
	}

 	if  ( i - j - 1 >= 0 ) { // ED_12_in
	    // System.out.println(" -- Firing ND_86 -- ED_12 ");
	    _argIn0 = ((DoubleToken) in1.get(0)).doubleValue();
	    // System.out.println(" -- RETRIEVED FOR ND_86 -- ED_11 ");
	    r_3.store( _argIn0, r_3.atKey(_K,j,i) );
	    _argIn0 = r_3.retrieve( r_3.atKey(_K,j,i) );
	}

	Sink_1.store(_argIn0, Sink_1.atKey(j, i));
	out0.broadcast(new DoubleToken( _argIn0 ));

    }

    public boolean prefire() {	
	// System.out.println(" -- PRE Firing ND_86 -- ");
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
		// sbfQuit();
		// sbfExit();
		//System.out.println(" -- FINISHED ND_86 -- ");
		Sink_1.WriteMatrix("Rout");
		_returnValue = false;
		//terminate();
		//(Thread.currentThread()).stop();
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

	// System.out.println(" -- POST ND_86 i: " + i + " j: " + j);
	//System.out.println(" -- POST Firing ND_86 -- _t " + _t 
	//		   + " _t0 " + _t0 + "  _t1 " + _t1);
	return _returnValue;
    }
 
    private void Vectorize(double arg0, double arg1) {
    }
}

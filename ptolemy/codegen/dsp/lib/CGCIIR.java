/* IIR, CGC domain: CGCIIR.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCIIR.pl by ptlang
 */
/*
  Copyright (c) 1990-1997 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.lib;

import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCIIR
/**
   An infinite impulse response (IIR) filter implemented in a direct form II
   realization.  The transfer function is of the form H(z) = G*N(1/z)/D(1/z),
   where N() and D() are polynomials.  The parameter "gain" specifies G, and
   the floating-point arrays "numerator" and "denominator" specify N() and D(),
   respectively.  Both arrays start with the constant terms of the polynomial
   and decrease in powers of z (increase in powers of 1/z).  Note that the
   constant term of D is not omitted, as is common in other programs that
   assume that it has been normalized to unity.
   <p>
   <p>
   This star implements an infinite impulse response filter of arbitrary order
   in a direct form II [1] realization.
   The parameters of the star specify <i>H</i>(<i>z</i>), the <i>Z</i>-transform of an
   impulse response <i>h</i>(<i>n</i>).
   The output of the star is the convolution of the input with <i>h</i>(<i>n</i>).
   <p>
   Note that the numerical finite precision noise increases with the filter order.
   To minimize this distortion, it is often desirable to expand the filter
   into a parallel or cascade form.
   <h3>References</h3>
   <p>[1]  
   A. V. Oppenheim and R. W. Schafer, <i>Discrete-Time Signal Processing</i>,
   Prentice-Hall: Englewood Cliffs, NJ, 1989.

   @Author Kennard White, Yu Kee Lim
   @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCIIR.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCIIR extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCIIR(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        signalIn = new ClassicPort(this, "signalIn", true, false);
        signalIn.setTypeEquals(BaseType.DOUBLE);
        signalOut = new ClassicPort(this, "signalOut", false, true);
        signalOut.setTypeEquals(BaseType.DOUBLE);

        // Filter gain FloatState
        gain = new Parameter(this, "gain");
        gain.setExpression("1");

        // Numerator coefficients FloatArrayState
        numerator = new Parameter(this, "numerator");
        numerator.setExpression("{.5 .25 .1}");

        // Denominator coefficients FloatArrayState
        denominator = new Parameter(this, "denominator");
        denominator.setExpression("{1 .5 .3}");

        // State FloatArrayState
        state = new Parameter(this, "state");
        state.setExpression("{0}");

        /*     //# line 77 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCIIR.pl"
               numState = 0;
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * signalIn of type double.
     */
    public ClassicPort signalIn;

    /**
     * signalOut of type double.
     */
    public ClassicPort signalOut;

    /**
     *  Filter gain parameter with initial value "1".
     */
    public Parameter gain;

    /**
     *  Numerator coefficients parameter with initial value ".5 .25 .1".
     */
    public Parameter numerator;

    /**
     *  Denominator coefficients parameter with initial value "1 .5 .3".
     */
    public Parameter denominator;

    /**
     *  State parameter with initial value "0".
     */
    public Parameter state;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 82 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCIIR.pl"
        int numNumer = numerator.size();
	int numDenom = denominator.size();
	double b0, scaleDenom, scaleNumer;
	numState = max(numNumer,numDenom); 

	// Set up scaling to distribute the gain through the numerator,
	// and scale both numer and denom to make b0=1
	if ( numDenom < 1 ) {
	    b0 = 1.0;
	} else {
	    if ( (b0 = denominator[0]) == 0.0 ) {
		// FIXME: should sanity-check b0 more thoroughly
		// (e.g., shouldn't even be close to zero)
		Error::abortRun(*this, 
                        "Must have non-zero leading coefficient in the denominator");
		return;
	    }
	}
	scaleDenom = 1.0 / b0;
	scaleNumer = scaleDenom * double(gain);

	// Set up the state vector.  The state vector includes
	// both the delay states and the coefficients in the appropriate
	// order:
	// S(0) A(0) -1 S(1) A(1) -B(1) ... S(n-1) A(n-1) -B(n-1) Sn An -Bn
	// state[0] and state[2] are never referenced
	state.resize(numState*3);
	for ( int i=0; i < numState; i++) {
	    state[i*3+0] = 0;
	    state[i*3+1] = i < numNumer ? scaleNumer * numerator[i] : 0;
	    state[i*3+2] = i < numDenom ? scaleDenom * -denominator[i] : 0;
	}
    }

    /**
     */
    public void  generateFireCode() {
        //# line 145 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCIIR.pl"
        if ( numState == 1 ) addCode(feedThrough);
	else addCode(iir);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String feedThrough = 
    "	/* No state for the IIR filter: just scale the input */\n"
    + "	$ref(signalOut) = $ref(state,1) * $ref(signalIn);\n";

    public String iir = 
    "	/*\n"
    + "	   v[0] is the current state variable, v[1] is the current numerator\n"
    + "	   coefficient, and v[2] is the currrent denominator coefficient\n"
    + "	 */\n"
    + "	double* v = & $ref(state,3);\n"
    + "	double* stateEnd = (double *) $ref(state) + $size(state);\n"
    + "	double s0 = $ref(signalIn);\n"
    + "	double  s, y;\n"
    + "\n"
    + "	s = *v++;\n"
    + "	y = s * *v++;\n"
    + "	s0 += s * *v++;\n"
    + "\n"
    + "	for ( ; v < stateEnd; ) {\n"
    + "	    double sTmp = *v;\n"
    + "	    *v++ = s;\n"
    + "	    s = sTmp;\n"
    + "	    y += s * *v++;\n"
    + "	    s0 += s * *v++;\n"
    + "	}\n"
    + "	$ref(state,3) = s0;\n"
    + "	y += s0 * $ref(state,1);\n"
    + "	$ref(signalOut) = y;\n";
}

/* FIRFix, CGC domain: CGCFIRFix.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCFIRFix.pl by ptlang
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
//// CGCFIRFix
/**
A finite impulse response (FIR) filter with fixed-point capabilities.
The fixed-point coefficients are specified by the "taps" parameter.
The default coefficients give an 8th-order, linear phase lowpass filter.
To read coefficients from a file, replace the default coefficients
with "fileName", preferably specifying a complete path.
Polyphase multirate filtering is also supported.
<p>
<p>
This star implements a finite-impulse response filter with multirate capability.
<a name="filter, FIR"></a>
<a name="FIR filter"></a>
The default coefficients correspond to an eighth-order, equiripple,
linear-phase, lowpass filter which has a 3 dB cutoff frequency at
approximately 1<i>/</i>3 of the Nyquist frequency.
The default precision on these coefficients is a total of 24 bits.
The number of decimal bits is chosen as the minimum number of bits
needed to represent the number in fixed-point.
One bit is reserved for the sign, and the rest are fractional bits.
During computation of filter outputs, the precision of the filter taps
is converted to the precision contained in the "TapsPrecision" parameter.
<p>
To load the filter coefficients from a file, simply replace the default
coefficients with the string "<i>filename</i>".
It is advisable to use an absolute path name as part of the file name,
especially if you are using the graphical interface.
This will allow the FIR filter to work as expected regardless of
the directory in which the ptolemy process actually runs.
It is best to use tilde characters in the filename to reference them to
the home directory of the user.
This way, future file system reorganizations will have minimal effect.
<p>
When the <i>decimation</i> (<i>interpolation</i>)
state is different from unity, the filter behaves exactly
as it were followed (preceded) by a DownSample (UpSample) star.
However, the implementation is much more efficient than
it would be using UpSample and DownSample stars because
a polyphase structure is used internally, thereby avoiding the
unnecessary memory locations and multiplication by zero.
Arbitrary sample rate conversions by rational factors can
be accomplished in this way.
<p>
To design a filter for a multirate system, simply assume that the
sample rate is the product of the interpolation parameter and
the input sample rate, or equivalently, the product of the decimation
parameter and the output sample rate.
<a name="multirate filter design"></a>
<a name="filter design, multirate"></a>
<a name="filter, multirate"></a>
In particular, considerable care must be taken to avoid aliasing.
Specifically, if the input sample rate is <i>f</i>,
then the filter stopband should begin before <i>f/</i>2.
If the interpolation ratio is <i>i</i>, then <i>f/</i>2 is a fraction 1<i>/</i>(2<i> i</i>)
of the sample rate at which you must design your filter.
<p>
The <i>decimationPhase</i> parameter is somewhat subtle.
It is exactly equivalent the phase parameter of the DownSample star.
Its interpretation is as follows; when decimating,
samples are conceptually discarded (although a polyphase structure
does not actually compute the discarded samples).
If you are decimating by a factor of three, then you will select
one of every three outputs, with one of three possible phases.
When decimationPhase is zero (the default),
the latest (most recent) samples are the ones selected.
The decimationPhase must be strictly less than
the decimation ratio.
<p>
For more information about polyphase filters, see [1-2].
<a name="Harris, F. J."></a>
<a name="Vaidyanathan, P. P."></a>
<h3>References</h3>
<p>[1]  
F. J. Harris,
``Multirate FIR Filters for Interpolating and Desampling'', in
<i>Handbook of Digital Signal Processing</i>, Academic Press, 1987.
<p>[2]  
P. P. Vaidyanathan,
``Multirate Digital Filters, Filter Banks, Polyphase
Networks, and Applications: A Tutorial'',
<i>Proc. of the IEEE</i>, vol. 78, no. 1, pp. 56-93, Jan. 1990.
@see ptolemy.domains.cgc.stars.FIRCx
@see ptolemy.domains.cgc.stars.Biquad
@see ptolemy.domains.cgc.stars.UpSample
@see ptolemy.domains.cgc.stars.DownSample

 @Author Edward A. Lee, Alireza Khazeni, J.Weiss
 @Version $Id$, based on version 1.12 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCFIRFix.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFIRFix extends CGCFix {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFIRFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        signalIn = new ClassicPort(this, "signalIn", true, false);
        signalIn.setTypeEquals(BaseType.FIX);
        signalOut = new ClassicPort(this, "signalOut", false, true);
        signalOut.setTypeEquals(BaseType.FIX);

        // Filter tap values. FixArrayState
        taps = new Parameter(this, "taps");
        taps.setExpression("-.040609 -.001628 .17853 .37665 .37665 .17853 -.001628 -.040609");

        // Decimation ratio. IntState
        decimation = new Parameter(this, "decimation");
        decimation.setExpression("1");

        // Downsampler phase. IntState
        decimationPhase = new Parameter(this, "decimationPhase");
        decimationPhase.setExpression("0");

        // Interpolation ratio. IntState
        interpolation = new Parameter(this, "interpolation");
        interpolation.setExpression("1");

        // Flag indicating whether or not to use the arriving particles as they are:\nYES keeps the same precision, and NO casts them to the precision specified\nby the parameter \"InputPrecision\". IntState
        ArrivingPrecision = new Parameter(this, "ArrivingPrecision");
        ArrivingPrecision.setExpression("YES");

        // Precision of the input in bits.\nThe input particles are only cast to this precision if the parameter\n\"ArrivingPrecision\" is set to NO. PrecisionState
        InputPrecision = new Parameter(this, "InputPrecision");
        InputPrecision.setExpression("4.14");

        // Precision of the taps in bits. PrecisionState
        TapPrecision = new Parameter(this, "TapPrecision");
        TapPrecision.setExpression("4.14");

        // Precision of the accumulation in bits. PrecisionState
        AccumulationPrecision = new Parameter(this, "AccumulationPrecision");
        AccumulationPrecision.setExpression("4.14");

        // Precision of the output in bits. PrecisionState
        OutputPrecision = new Parameter(this, "OutputPrecision");
        OutputPrecision.setExpression("4.14");

        // phaseLength IntState
        phaseLength = new Parameter(this, "phaseLength");
        phaseLength.setExpression("0");

        // tapSize IntState
        tapSize = new Parameter(this, "tapSize");
        tapSize.setExpression("0");

        // tap FixState
        tap = new Parameter(this, "tap");
        tap.setExpression("0.0");

        // Accum FixState
        Accum = new Parameter(this, "Accum");
        Accum.setExpression("0.0");

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * signalIn of type fix.
     */
    public ClassicPort signalIn;

    /**
     * signalOut of type fix.
     */
    public ClassicPort signalOut;

    /**
     *  Filter tap values. parameter with initial value "-.040609 -.001628 .17853 .37665 .37665 .17853 -.001628 -.040609".
     */
     public Parameter taps;

    /**
     *  Decimation ratio. parameter with initial value "1".
     */
     public Parameter decimation;

    /**
     *  Downsampler phase. parameter with initial value "0".
     */
     public Parameter decimationPhase;

    /**
     *  Interpolation ratio. parameter with initial value "1".
     */
     public Parameter interpolation;

    /**
     *  Flag indicating whether or not to use the arriving particles as they are:
YES keeps the same precision, and NO casts them to the precision specified
by the parameter "InputPrecision". parameter with initial value "YES".
     */
     public Parameter ArrivingPrecision;

    /**
     *  Precision of the input in bits.
The input particles are only cast to this precision if the parameter
"ArrivingPrecision" is set to NO. parameter with initial value "4.14".
     */
     public Parameter InputPrecision;

    /**
     *  Precision of the taps in bits. parameter with initial value "4.14".
     */
     public Parameter TapPrecision;

    /**
     *  Precision of the accumulation in bits. parameter with initial value "4.14".
     */
     public Parameter AccumulationPrecision;

    /**
     *  Precision of the output in bits. parameter with initial value "4.14".
     */
     public Parameter OutputPrecision;

    /**
     *  phaseLength parameter with initial value "0".
     */
     public Parameter phaseLength;

    /**
     *  tapSize parameter with initial value "0".
     */
     public Parameter tapSize;

    /**
     *  tap parameter with initial value "0.0".
     */
     public Parameter tap;

    /**
     *  Accum parameter with initial value "0.0".
     */
     public Parameter Accum;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 196 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFIRFix.pl"
CGCFix::setup();

		int d = decimation;
		int i = interpolation;
		int dP = decimationPhase;
		signalIn.setSDFParams(d, d+1+(taps.size()/i));
		signalOut.setSDFParams(i, i-1);
		if (dP >= d) {
			Error::abortRun (*this, ": decimationPhase too large");
			return;
		}
		// The phaseLength is ceiling(taps.size/interpolation)
		// It is a protected instance variable.
		int temp = taps.size() / i;
		tapSize = taps.size();
		if ((taps.size() % i) != 0)  temp++;
		phaseLength = temp;

		// Set the precision on the fixed-point variables

		if (!int(ArrivingPrecision))
			signalIn.setPrecision(InputPrecision);
		signalOut.setPrecision(OutputPrecision);

		tap.setPrecision(TapPrecision);
		Accum.setPrecision(AccumulationPrecision);
     }

    /**
     */
    public void  generateFireCode() {
        //# line 234 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFIRFix.pl"
addCode(bodyDecl);
                CGCFix::clearOverflow();
 
	{ StringList _str_; _str_ << 
"	/* phase keeps track of which phase of the filter coefficients is used.\n"
"	   Starting phase depends on the decimationPhase state. */\n"
"	phase = $val(decimation) - $val(decimationPhase) - 1;   \n"

	 addCode(_str_); }
	
	{ StringList _str_; _str_ << 
"	/* Iterate once for each input consumed */\n"
"	for (inC = 1; inC <= $val(decimation) ; inC++) {\n"

	 addCode(_str_); }

	{ StringList _str_; _str_ << 
"		/* Produce however many outputs are required for each \n"
"		   input consumed */\n"
"		while (phase < $val(interpolation)) {\n"

	 addCode(_str_); }

	{ StringList _str_; _str_ << 
"			FIX_SetToZero($ref(Accum));\n"

	 addCode(_str_); }

	{ StringList _str_; _str_ << 
"			/* Compute the inner product. */\n"
"			for (i = 0; i < $val(phaseLength); i++) {\n"
"				tapsIndex = i * $val(interpolation) + phase;\n"
"				if (tapsIndex >= $val(tapSize))\n"
"			    		FIX_SetToZero($ref(tap));\n"
"				else\n"
"			 		FIX_Assign($ref(tap),$ref(taps,tapsIndex));\n"

	 addCode(_str_); }

	{ StringList _str_; _str_ << 
"				inPos = $val(decimation) - inC + i;\n"

	 addCode(_str_); }
                                CGCFix::clearOverflow();
	{ StringList _str_; _str_ << 
"				FIX_MulAdd($ref(Accum), $ref(tap),$ref(signalIn,inPos));\n"

	 addCode(_str_); }
                                CGCFix::checkOverflow();
	{ StringList _str_; _str_ << 
"			}\n"

	 addCode(_str_); }
                        CGCFix::clearOverflow();
	{ StringList _str_; _str_ << 
"			FIX_Assign($ref(signalOut,outCount),$ref(Accum));\n"

	 addCode(_str_); }
                        CGCFix::checkOverflow();
	{ StringList _str_; _str_ << 
"			outCount--;;\n"
"			phase += $val(decimation);\n"
"		}\n"
"		phase -= $val(interpolation);\n"
"	}"

	 addCode(_str_); }

     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String bodyDecl = 
        "                int phase, tapsIndex, inC, i;\n"
        + "                int outCount = $val(interpolation) - 1;\n"
        + "	        int inPos;\n";
}

/* FIR, CGC domain: CGCFIR.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCFIR.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty provisions.
 */
package ptolemy.domains.sdf.cgc.dsp.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCFIR
/**
A Finite Impulse Response (FIR) filter.
Coefficients are in the "taps" state variable.
Default coefficients give an 8th order, linear phase lowpass
filter. To read coefficients from a file, replace the default
coefficients with "fileName".
<p>
<p>
This star implements a finite-impulse response filter with multirate capability.
The default coefficients correspond to an eighth order, equiripple,
linear-phase, lowpass filter.  The 3dB cutoff frequency at about 1/3
of the Nyquist frequency.  To load filter coefficients from a file,
simply replace the default coefficients with the string "&lt;filename".
<p>
It is advisable to use an absolute path name as part of the file name,
especially if you are using the graphical interface.
This will allow the FIR filter to work as expected regardless of
the directory in which the ptolemy process actually runs.
It is best to use tilde's in the filename to reference them to user's
home directory.  This way, future filesystem reorganizations
will have minimal effect.
<p>
When the <i>decimation</i> (<i>interpolation</i>)
state is different from unity, the filter behaves exactly
as it were followed (preceded) by a DownSample (UpSample) star.
However, the implementation is much more efficient than
it would be using UpSample and DownSample stars;
a polyphase structure is used internally, avoiding unnecessary use
of memory and unnecessary multiplication by zero.
Arbitrary sample-rate conversions by rational factors can
be accomplished this way.
<p>
To design a filter for a multirate system, simply assume the
sample rate is the product of the interpolation parameter and
the input sample rate, or equivalently, the product of the decimation
parameter and the output sample rate.
In particular, considerable care must be taken to avoid aliasing.
Specifically, if the input sample rate is f,
then the filter stopband should begin before f/2.
If the interpolation ratio is i, then f/2 is a fraction 1/2i
of the sample rate at which you must design your filter.
<p>
The <i>decimationPhase</i> parameter is somewhat subtle.
It is exactly equivalent the phase parameter of the DownSample star.
Its interpretation is as follows; when decimating,
samples are conceptually discarded (although a polyphase structure
does not actually compute the discarded samples).
If you are decimating by a factor of three, then you will select
one of every three outputs, with three possible phases.
When decimationPhase is zero (the default),
the latest (most recent) samples are the ones selected.
The decimationPhase must be strictly less than
the decimation ratio.
<p>
For more information about polyphase filters, see F. J. Harris,
"Multirate FIR Filters for Interpolating and Desampling", in
<i>Handbook of Digital Signal Processing</i>, Academic Press, 1987.

 @Author Soonhoi Ha
 @Version $Id$, based on version 1.14 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCFIR.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFIR extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFIR(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        signalIn = new TypedIOPort(this, "signalIn", true, false);
        signalIn.setTypeEquals(BaseType.DOUBLE);
        signalOut = new TypedIOPort(this, "signalOut", false, true);
        signalOut.setTypeEquals(BaseType.DOUBLE);

        // Filter tap values.
        taps = new Parameter(this, "taps");
        taps.setExpression("-.040609 -.001628 .17853 .37665 .37665 .17853 -.001628 -.040609");

        // Decimation ratio.
        decimation = new Parameter(this, "decimation");
        decimation.setExpression("1");

        // Downsampler phase.
        decimationPhase = new Parameter(this, "decimationPhase");
        decimationPhase.setExpression("0");

        // Interpolation ratio.
        interpolation = new Parameter(this, "interpolation");
        interpolation.setExpression("1");

        // phaseLength
        phaseLength = new Parameter(this, "phaseLength");
        phaseLength.setExpression("0");

        // tapSize
        tapSize = new Parameter(this, "tapSize");
        tapSize.setExpression("0");

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * signalIn of type double.
     */
    public TypedIOPort signalIn;

    /**
     * signalOut of type double.
     */
    public TypedIOPort signalOut;

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
     *  phaseLength parameter with initial value "0".
     */
     public Parameter phaseLength;

    /**
     *  tapSize parameter with initial value "0".
     */
     public Parameter tapSize;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 177 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFIR.pl"
        //int x = taps.size();
        //		int i = interpolation;
        //		int d = decimation;
        //		if (x % i != 0) x = x/i + 1;
        //		else x = x/i;
        //		int y = i/d;
        //		if (i % d != 0) y++;
        //		/* count of elementary operations */
        //		return 1 + y * (6 + x * 6);
        // Dummy return value.
        return 0;
     }

    /**
     */
    public void  setup() {
        //# line 119 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFIR.pl"
        //int d = decimation;
        //		int i = interpolation;
        //		int dP = decimationPhase;
        //		signalIn.setSDFParams(d, d+1+(taps.size()/i));
        //		signalOut.setSDFParams(i, i-1);
        //		if (dP >= d) {
        //			Error::abortRun (*this, ": decimationPhase too large");
        //			return;
        //		}
        //		// The phaseLength is ceiling(taps.size/interpolation)
        //		// It is a protected instance variable.
        //		int temp = taps.size() / i;
        //		tapSize = taps.size();
        //		if ((taps.size() % i) != 0)  temp++;
        //		phaseLength = temp;
     }

    /**
     */
    public void  go() {
        //# line 173 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFIR.pl"
        //addCode(bodyDecl);
        //		addCode(body);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String bodyDecl = 
        "	int phase, tapsIndex, inC, i;\n"
        + "	int outCount = $val(interpolation) - 1;\n"
        + "	int inPos;\n"
        + "	double out, tap;\n";

    public String body = 
        "	/* phase keeps track of which phase of the filter coefficients is used.\n"
        + "	   Starting phase depends on the decimationPhase state. */\n"
        + "	phase = $val(decimation) - $val(decimationPhase) - 1;   \n"
        + "	\n"
        + "	/* Iterate once for each input consumed */\n"
        + "	for (inC = 1; inC <= $val(decimation) ; inC++) {\n"
        + "\n"
        + "		/* Produce however many outputs are required for each \n"
        + "		   input consumed */\n"
        + "		while (phase < $val(interpolation)) {\n"
        + "			out = 0.0;\n"
        + "\n"
        + "			/* Compute the inner product. */\n"
        + "			for (i = 0; i < $val(phaseLength); i++) {\n"
        + "				tapsIndex = i * $val(interpolation) + phase;\n"
        + "				if (tapsIndex >= $val(tapSize))\n"
        + "			    		tap = 0.0;\n"
        + "				else\n"
        + "			 		tap = $ref2(taps,tapsIndex);\n"
        + "				inPos = $val(decimation) - inC + i;\n"
        + "				out += tap * $ref2(signalIn,inPos);\n"
        + "			}\n"
        + "			$ref2(signalOut,outCount) = out;\n"
        + "			outCount--;;\n"
        + "			phase += $val(decimation);\n"
        + "		}\n"
        + "		phase -= $val(interpolation);\n"
        + "	}\n";
}

/* FFTCx, CGC domain: CGCFFTCx.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl by ptlang
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
//// CGCFFTCx
/**
Complex Fast Fourier transform.
Parameter "order" (default 8) is the log, base 2, of the transform size.
Parameter "size" (default 256) is the number of samples read ie. size is less than or equal to 2 ^order.
Parameter "direction" (default 1) is 1 for forward, -1 for inverse FFT.]
<p>
A number of input samples given by the parameter <i>size</i> will
be consumed at the input, zero-padded if necessary to make 2<i> <sup>order</sup></i>
samples, and transformed using a fast Fourier transform algorithm.
<a name="FFT, complex"></a>
<a name="fast Fourier transform, complex"></a>
<a name="Fourier transform, fast, complex"></a>
If <i>direction</i> is 1, then the forward Fourier transform is computed.
If <i>direction</i> is -1, then the inverse Fourier transform is computed.
<p>
Note a single firing of this star consumes <i>size</i> inputs
and produces 2<i> <sup>order</sup></i> outputs.
This must be taken into account when determining for how many iterations
to run a universe.
For example, to compute just one FFT, only one iteration should be run.
<p>
<b>Bugs</b>: the routine currently used (from Gabriel) recomputes trig
<a name="Gabriel"></a>
functions for each term, instead of using a table.  Instead,
<code>ComplexFFT::start()</code> should compute a table of appropriate size to save
time.  This has no effect, obviously, if only one transform
is performed.
Code is modified from SDFComplexFFT star, which was originated from Gabriel.

 @Author S. Ha
 @Version $Id$, based on version 1.14 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFFTCx extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFFTCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.COMPLEX);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        // Log base 2 of the transform size.
        order = new Parameter(this, "order");
        order.setExpression("8");

        // Number of input samples to read.
        size = new Parameter(this, "size");
        size.setExpression("256");

        // = 1 for forward, = -1 for inverse.
        direction = new Parameter(this, "direction");
        direction.setExpression("1");

        // localData
        localData = new Parameter(this, "localData");
        localData.setExpression("0");

        // fftSize
        fftSize = new Parameter(this, "fftSize");
        fftSize.setExpression("256");

/*     //# line 84 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl"
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type complex.
     */
    public TypedIOPort input;

    /**
     * output of type complex.
     */
    public TypedIOPort output;

    /**
     *  Log base 2 of the transform size. parameter with initial value "8".
     */
     public Parameter order;

    /**
     *  Number of input samples to read. parameter with initial value "256".
     */
     public Parameter size;

    /**
     *  = 1 for forward, = -1 for inverse. parameter with initial value "1".
     */
     public Parameter direction;

    /**
     *  localData parameter with initial value "0".
     */
     public Parameter localData;

    /**
     *  fftSize parameter with initial value "256".
     */
     public Parameter fftSize;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 216 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl"
        //return  1000; 	/* just say large number for now */
        // Dummy return value.
        return 0;
     }

    /**
     */
    public void  initCode() {
        //# line 176 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl"
        //// add fft routine
        //		addProcedure(fftRoutine, "fft_rif");
     }

    /**
     */
    public void  setup() {
        //# line 163 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl"
        //int temp = 1 << int(order);
        //		if (temp < int(size)) {
        //			Error::abortRun(*this, "2^order must be >= size");
        //			return;
        //		} 
        //
        //		localData.resize(temp * 2);
        //		input.setSDFParams (int(size), int(size)-1);
        //		output.setSDFParams (temp, temp-1);
        //		fftSize = temp;
     }

    /**
     */
    public void  go() {
        //# line 207 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCFFTCx.pl"
        //addCode(loadCode);
        //
        //		// generate output data.  If inverse, we must scale the result.
        //		if (int(direction) != 1)
        //			addCode(scaleOut);
        //
        //		addCode(outData);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String fftRoutine = 
        "\n"
        + "/*\n"
        + " * This fft routine is from ~gabriel/src/filters/fft/fft.c;\n"
        + " * I am unsure of the original source.  The file contains no\n"
        + " * copyright notice or description.\n"
        + " * The declaration is changed to the prototype form but the\n"
        + " * function body is unchanged.  (J. T. Buck)\n"
        + " */\n"
        + "\n"
        + "#define SWAP(a, b) tempr=(a); (a)=(b); (b)=tempr\n"
        + "\n"
        + "/*\n"
        + " * Replace data by its discrete Fourier transform, if isign is\n"
        + " * input as 1, or by its inverse discrete Fourier transform, if \n"
        + " * \"isign\" is input as -1.  \"data'\"is a complex array of length \"nn\",\n"
        + " * input as a real array data[0..2*nn-1]. \"nn\" MUST be an integer\n"
        + " * power of 2 (this is not checked for!?)\n"
        + " */\n"
        + "\n"
        + "static void fft_rif(data, nn, isign)\n"
        + "double* data;\n"
        + "int nn, isign;\n"
        + "{\n"
        + "	int	n;\n"
        + "	int	mmax;\n"
        + "	int	m, j, istep, i;\n"
        + "	double	wtemp, wr, wpr, wpi, wi, theta;\n"
        + "	double	tempr, tempi;\n"
        + "\n"
        + "	data--;\n"
        + "	n = nn << 1;\n"
        + "	j = 1;\n"
        + "\n"
        + "	for (i = 1; i < n; i += 2) {\n"
        + "		if(j > i) {\n"
        + "			SWAP(data[j], data[i]);\n"
        + "			SWAP(data[j+1], data[i+1]);\n"
        + "		}\n"
        + "		m= n >> 1;\n"
        + "		while (m >= 2 && j >m) {\n"
        + "			j -= m;\n"
        + "			m >>= 1;\n"
        + "		}\n"
        + "		j += m;\n"
        + "	}\n"
        + "	mmax = 2;\n"
        + "	while (n > mmax) {\n"
        + "		istep = 2*mmax;\n"
        + "		theta = -6.28318530717959/(isign*mmax);\n"
        + "		wtemp = sin(0.5*theta);\n"
        + "		wpr = -2.0*wtemp*wtemp;\n"
        + "		wpi = sin(theta);\n"
        + "		wr = 1.0;\n"
        + "		wi = 0.0;\n"
        + "		for (m = 1; m < mmax; m += 2) {\n"
        + "			for (i = m; i < n; i += istep) {\n"
        + "				j = i + mmax;\n"
        + "				tempr = wr*data[j] - wi*data[j+1];\n"
        + "				tempi = wr*data[j+1] + wi*data[j];\n"
        + "				data[j] = data[i] - tempr;\n"
        + "				data[j+1] = data[i+1] - tempi;\n"
        + "				data[i] += tempr;\n"
        + "				data[i+1] += tempi;\n"
        + "			}\n"
        + "			wr = (wtemp=wr)*wpr - wi*wpi+wr;\n"
        + "			wi = wi*wpr + wtemp*wpi + wi;\n"
        + "		}\n"
        + "		mmax = istep;\n"
        + "	}\n"
        + "}\n"
        + "\n";

    public String loadCode = 
        "	int i, j = 0;\n"
        + "	for (i = $val(size) - 1; i >= 0; i--) {\n"
        + "		$ref(localData,j++) = $ref(input,i).real;\n"
        + "		$ref(localData,j++) = $ref(input,i).imag;\n"
        + "	}\n"
        + "        for (i = $val(size) ; i < $val(fftSize) ; i ++) {\n"
        + "		$ref(localData)[j++] = 0.0;\n"
        + "		$ref(localData)[j++] = 0.0;\n"
        + "	}		\n"
        + "	fft_rif ($ref(localData),$val(fftSize), $val(direction));\n";

    public String scaleOut = 
        "	for (i = 0; i < 2*$val(fftSize); i++)\n"
        + "		$ref(localData,i) /= $val(fftSize);\n";

    public String outData = 
        "	j = 0;\n"
        + "	for (i = $val(fftSize) - 1; i >= 0; i--) {\n"
        + "		$ref(output,i).real = $ref(localData,j++);\n"
        + "		$ref(output,i).imag = $ref(localData,j++);\n"
        + "	}\n";
}

/* Window, CGC domain: CGCWindow.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCWindow.pl by ptlang
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
//// CGCWindow
/**
   Generates standard window functions:
   Rectangle, Bartlett, Hanning, Hamming, Kaiser, Blackman, and SteepBlackman.
   <p>
   <p>
   This star produces on its output values that are samples of a standard
   windowing function.  The window function to be sampled is determined by
   the <i>name</i> string parameter.  Possible values are: <b>Rectangle</b>,
   <b>Bartlett</b>, <b>Hanning</b>, <b>Hamming</b>, <b>Blackman</b>, and
   <b>SteepBlackman</b>.  Upper and lower case characters in the names are
   equivalent.
   <p>
   The parameter <i>length</i> is the length of the window to produce.  Note
   that most windows functions have zero value at the first and last sample.
   The parameter <i>period</i> specifies the period of the output signal:
   the window will be zero-padded if required.  A <i>period</i> of 0
   means a period equal to <i>length</i>.  A negative period will
   produce only one window, and then outputs zero for all later samples.
   A period of less than window length will be equivalent to a period of
   window length (i.e, period=0).
   <p>
   One period of samples are produced on every firing.

   @Author Brian L. Evans Contributor(s): Jose Luis Pino and Kennard White
   @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCWindow.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCWindow extends CGCWaveForm {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCWindow(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Name of the window function to generate:\nRectangle, Bartlett, Hanning, Hamming, Kaiser, Blackman, or SteepBlackman. StringState
        name = new Parameter(this, "name");
        name.setExpression("Hanning");

        // Length of the window function to produce. IntState
        length = new Parameter(this, "length");
        length.setExpression("256");

        // An array of numeric parameters for the window.\nFor the Kaiser window, the first entry in this state is taken as the\nbeta parameter which is proportional to the stopband attenuation of\nthe window. FloatArrayState
        WindowParameters = new Parameter(this, "WindowParameters");
        WindowParameters.setExpression("{0}");

        /*     //# line 78 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCWindow.pl"
               value.setAttributes(A_NONSETTABLE|A_CONSTANT);
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  Name of the window function to generate:
     Rectangle, Bartlett, Hanning, Hamming, Kaiser, Blackman, or SteepBlackman. parameter with initial value "Hanning".
    */
    public Parameter name;

    /**
     *  Length of the window function to produce. parameter with initial value "256".
     */
    public Parameter length;

    /**
     *  An array of numeric parameters for the window.
     For the Kaiser window, the first entry in this state is taken as the
     beta parameter which is proportional to the stopband attenuation of
     the window. parameter with initial value "0".
    */
    public Parameter WindowParameters;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 84 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCWindow.pl"
        const char* wn = name;
        int winType = Ptdsp_WindowNumber(wn);
        if (winType == PTDSP_WINDOW_TYPE_NULL) {
            Error::abortRun(*this, "Unknown window name ", wn);
            return;
        }

        // Don't want to risk divide by zero
        int realLen = int(length);
        if ( realLen < 4 ) {
            Error::abortRun(*this, "Window length is too small ",
                    "(should be greater than 3)");
            return;
        }

        double* windowTaps = new double[realLen];
        int validWindow = Ptdsp_Window(windowTaps, realLen, winType,
                (double *) WindowParameters);
        if (! validWindow) {
            delete [] windowTaps;
            Error::abortRun(*this, "Could not compute the taps for a ",
                    wn, " window: Ptdsp_Window failed.");
            return;
        }

        value.resize(int(length));
        window.initialize();		// initialize to a null string
        for (int i = 0; i < realLen; i++) {
            window << windowTaps[i] << " ";
        }
        delete [] windowTaps;

        value.setInitValue(window);
        value.initialize();
        CGCWaveForm::setup();
    }
}

/* RaisedCosine, CGC domain: CGCRaisedCosine.java file generated from /users/ptolemy/src/domains/cgc/dsp/stars/CGCRaisedCosine.pl by ptlang
 */
/*
  Copyright (c) 1990-2005 The Regents of the University of California.
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
//// CGCRaisedCosine
/**
   An FIR filter with a magnitude frequency response that is shaped
   like the standard raised cosine or square-root raised cosine
   used in digital communications.
   <p>
   See the SDFRaisedCos star.

   @Author Joseph T. Buck
   @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/dsp/stars/CGCRaisedCosine.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRaisedCosine extends CGCFIR {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRaisedCosine(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Number of taps IntState
        length = new Parameter(this, "length");
        length.setExpression("64");

        // Distance from center to first zero crossing IntState
        symbol_interval = new Parameter(this, "symbol_interval");
        symbol_interval.setExpression("16");

        // Excess bandwidth, between 0 and 1 FloatState
        excessBW = new Parameter(this, "excessBW");
        excessBW.setExpression("1.0");

        // If YES, use square-root raised cosine pulse IntState
        square_root = new Parameter(this, "square_root");
        square_root.setExpression("NO");

        /*     //# line 49 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCRaisedCosine.pl"
        // taps are no longer constant or settable
        taps.clearAttributes(A_CONSTANT|A_SETTABLE);
        // fix interpolation default
        interpolation.setInitValue("16");
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  Number of taps parameter with initial value "64".
     */
    public Parameter length;

    /**
     *  Distance from center to first zero crossing parameter with initial value "16".
     */
    public Parameter symbol_interval;

    /**
     *  Excess bandwidth, between 0 and 1 parameter with initial value "1.0".
     */
    public Parameter excessBW;

    /**
     *  If YES, use square-root raised cosine pulse parameter with initial value "NO".
     */
    public Parameter square_root;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() {
        //# line 55 "/users/ptolemy/src/domains/cgc/dsp/stars/CGCRaisedCosine.pl"
        if (double(excessBW) < 0.0)
            Error::abortRun(*this, "Invalid excess bandwidth");
        if (int(symbol_interval) <= 0)
            Error::abortRun(*this, "Invalid symbol interval");
        taps.resize(length);
        int center = int(length)/2;
        for (int i = 0; i < int(length); i++) {
            if (int(square_root))
                taps[i] = Ptdsp_SqrtRaisedCosine(i - center,
                        int(symbol_interval), excessBW);
            else
                taps[i] = Ptdsp_RaisedCosine(i - center,
                        int(symbol_interval), excessBW);
        }
        CGCFIR :: setup();
    }
}

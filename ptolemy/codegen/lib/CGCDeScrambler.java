/* DeScrambler, CGC domain: CGCDeScrambler.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDeScrambler.pl by ptlang
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
//// CGCDeScrambler
/**
Descramble the input bit sequence using a feedback shift register.
The taps of the feedback shift register are given by the "polynomial"
parameter.  This is a self-synchronizing descrambler that will exactly
reverse the operation of the Scrambler star if the polynomials are the same.
The low order bit of the polynomial should always be set. For more information,
see the documentation for the SDF Scrambler star and Lee and Messerschmitt,
Digital Communication, Second Edition, Kluwer Academic Publishers, 1994,
pp 595-603.
<p>
<a name="feedback shift register"></a>
<a name="pseudo-random sequence"></a>
<a name="PN sequence"></a>
<a name="primitive polynomial"></a>
<a name="maximal length feedback shift register"></a>

 @Author E. A. Lee
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCDeScrambler.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDeScrambler extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDeScrambler(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // generator polynomial for the maximal length shift register IntState
        polynomial = new Parameter(this, "polynomial");
        polynomial.setExpression("0440001");

        // the shift register IntState
        shiftReg = new Parameter(this, "shiftReg");
        shiftReg.setExpression("0");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
bit sequence in (zero or nonzero)
     */
    public ClassicPort input;

    /**
bit sequence out (zero or one)
     */
    public ClassicPort output;

    /**
     *  generator polynomial for the maximal length shift register parameter with initial value "0440001".
     */
     public Parameter polynomial;

    /**
     *  the shift register parameter with initial value "0".
     */
     public Parameter shiftReg;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

// Should check that generator polynomial does not exceed 31 bits. How?
          // To avoid sign extension problems, the hob must be zero
          if (((IntToken)((polynomial).getToken())).intValue() < 0) {
            throw new IllegalActionException(this,"Sorry, polynomials of order higher than 31 are not supported");
            return;
          }
          if (!(((IntToken)((polynomial).getToken())).intValue() & 1)) {
            throw new IllegalActionException(this,"The low-order bit of the polynomial is not set. Input will have no effect");
          }
     }

    /**
     */
    public void  generateFireCode() {

addCode(descramble);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String descramble =
        "          int reg, masked, parity;\n"
        + "          reg = $ref(shiftReg) << 1;\n"
        + "          /* put the input in the low order bit */\n"
        + "          reg += ($ref(input) != 0);\n"
        + "          masked = $val(polynomial) & reg;\n"
        + "          /* Now we need to find the parity of \"masked\". */\n"
        + "          parity = 0;\n"
        + "          /* Calculate the parity of the masked word */\n"
        + "          while (masked > 0) {\n"
        + "            parity = parity ^ (masked & 1);\n"
        + "            masked = masked >> 1;\n"
        + "          }\n"
        + "          $ref(output) = parity;\n"
        + "          $ref(shiftReg) = reg;\n";
}

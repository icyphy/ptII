/* Scrambler, CGC domain: CGCScrambler.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCScrambler.pl by ptlang
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
//// CGCScrambler
/**
Scramble the input bit sequence using a feedback shift register.
The taps of the feedback shift register are given by the "polynomial"
parameter, which should be a positive integer.  The n-th bit of this
integer indicates whether the n-th tap of the delay line is fed back.
The low-order bit is called the 0-th bit, and should always be set.
The next low-order bit indicates whether the output of the first delay
should be fed back, etc.  The default "polynomial" is an octal number
defining the V.22bis scrambler. For an explanation of scramblers, see
Lee and Messerschmitt, Digital Communication, Second Edition,
Kluwer Academic Publishers, 1994, pp 595-603.
<p>
<a name="feedback shift register"></a>
<a name="pseudo-random sequence"></a>
<a name="PN sequence"></a>
In scramblers based on feedback shift registers,
all the bits to be fed back are exclusive-ored
together (i.e., their parity is computed), and the result is exclusive-ored
with the input bit.  This result is produced at the output and shifted
into the delay line.
With proper choice of polynomial, the resulting output appears highly random
even if the input is highly non-random (e.g., all zeros or all ones).
<p>
If the polynomial is a <i>primitive polynomial</i>, then the feedback shift
register is a so-called <i>maximal length feedback shift register</i>.
<a name="primitive polynomial"></a>
<a name="maximal length feedback shift register"></a>
This means that with a constant input, the output will be sequence
with period 2<i> <sup>N</sup>  -  </i>1, where <i>N</i> is the order of the polynomial
(the length of the shift register).  This is the longest possible sequence.
Moreover, within this period, the sequence will appear to be white,
in that a computed autocorrelation will be very nearly an impulse.
Thus, the scrambler with a constant input can be very effectively used
to generate a pseudo-random bit sequence.
<p>
The maximal-length feedback shift register with constant input will
pass through 2<i> <sup>N</sup>  -  </i>1 states before returning to a state it has
been in before.  This is one short of the 2<i> <sup>N</sup></i> states that a register
with <i>N</i> bits can take on.  This one missing state, in fact, is a <i>lock-up</i>
state, in that if the input is an appropriate constant, the scrambler will
cease to produce random-looking output, and will output a constant.
For example, if the input is all zeros, and the initial state of the
scrambler is zero, then the outputs will be all zero, hardly random.
This is easily avoided by initializing the scrambler to some non-zero state.
<p>
The <i>polynomial</i> must be carefully chosen. It must represent a
<i>primitive polynomial</i>, which is one that cannot be factored into two
(nontrivial) polynomials with binary coefficients.  See Lee and Messerschmitt
for more details.  For convenience, we give here a set of primitive polynomials
(expressed as octal numbers so that they are easily translated into taps
on shift register).  All of these will result in maximal-length pseudo-random
sequences if the input is constant and lock-up is avoided:
<pre>
order    polynomial
2        07
3        013
4        023
5        045
6        0103
7        0211
8        0435
9        01021
10       02011
11       04005
12       010123
13       020033
14       042103
15       0100003
16       0210013
17       0400011
18       01000201
19       02000047
20       04000011
21       010000005
22       020000003
23       040000041
24       0100000207
25       0200000011
26       0400000107
27       01000000047
28       02000000011
29       04000000005
30       010040000007
</pre>
<p>
The leading zero in the polynomial indicates an octal number.  Note also
that reversing the order of the bits in any of these numbers will also result
in a primitive polynomial.  Thus, the default value for the polynomial parameter
is 0440001 in octal, or "100 100 000 000 000 001" in binary.  Reversing these bits
we get "100 000 000 000 001 001" in binary, or 0400011 in octal.  This latter number
is the one listed above as the primitive polynomial of order 17.
The order is simply the index the highest order non-zero in the polynomial,
where the low-order bit has index zero.
<p>
Since the polynomial and the feedback shift register are both implemented
using type "int", the order of the polynomial is limited by the size of the "int"
data type.  For simplicity and portability, the polynomial is also not allowed
to be interpretable as a negative integer, so the sign bit cannot be used.
Thus, if "int" is a 32 bit word, then the highest order polynomial allowed is
30 (recall that indexing for the order starts at zero, and we cannot use the sign bit).
Since many machines today have 32-bit integers, we give the primitive polynomials
above only up to order 30.

 @Author E. A. Lee
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCScrambler.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCScrambler extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCScrambler(CompositeEntity container, String name)
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
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

// Should check that generator polynomial does not exceed 31 bits. How?
          // To avoid sign extension problems, the hob must be zero
          if (((IntToken)((polynomial).getToken())).intValue() < 0) {
            throw new IllegalActionException(this,"Sorry, the polynomial must be a positive integer.");
            return;
          }
     }

    /**
     */
    public void  generateFireCode() {

addCode(scramble);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String scramble =
        "          int reg, masked, parity;\n"
        + "          reg = $ref(shiftReg) << 1;\n"
        + "          masked = $val(polynomial) & reg;\n"
        + "          /* Now we need to find the parity of \"masked\". */\n"
        + "          parity = 0;\n"
        + "          /* Calculate the parity of the masked word */\n"
        + "          while (masked > 0) {\n"
        + "            parity = parity ^ (masked & 1);\n"
        + "            masked = masked >> 1;\n"
        + "          }\n"
        + "          /* Exclusive-or with the input */\n"
        + "          parity = parity ^ ($ref(input) != 0);\n"
        + "          $ref(output) = parity;\n"
        + "          /* Put the parity bit into the shift register */\n"
        + "          $ref(shiftReg) = reg + parity;\n";
}

/* BitsToInt, CGC domain: CGCBitsToInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCBitsToInt.pl by ptlang
*/
/*
Copyright (c) 1990-1996 The Regents of the University of California.
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
//// CGCBitsToInt
/**
The integer input sequence is interpreted as a bit stream in which any nonzero
value is interpreted as to mean a 'one' bit.
This star consumes "nBits" successive bits from the input,
packs them into an integer, and outputs the resulting integer.
The first received bit becomes the most significant bit of the output.
If "nBits" is smaller than the wordsize minus one, then the
output integer will always be non-negative.
<p>


 @Author Jose Luis Pino
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCBitsToInt.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCBitsToInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCBitsToInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // number of bits read per execution IntState
        nBits = new Parameter(this, "nBits");
        nBits.setExpression("4");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type int.
     */
    public ClassicPort input;

    /**
     * output of type int.
     */
    public ClassicPort output;

    /**
     *  number of bits read per execution parameter with initial value "4".
     */
     public Parameter nBits;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((nBits).getToken())).intValue()*2;
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

/* Need (int) cast on sizeof to eliminate gcc warning */
	if (((IntToken)((nBits).getToken())).intValue() > /*sizeof(int)*/ 16 *8) {
StringBuffer message = new StringBuffer("nBits needs to be less than");
	    message.append(/*(unsigned int)(sizeof(int)*/ ( 16 *8));
	    throw new IllegalActionException(this,message);
	    return;
	}
	if (((IntToken)((nBits).getToken())).intValue() < 0) {
	    throw new IllegalActionException(this, "nBits must be positive");
	    return;
	}
	input.setSDFParams(((IntToken)((nBits).getToken())).intValue(),((IntToken)((nBits).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

addCode(readNwrite);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String readNwrite =
        "	int i;\n"
        + "	int word = 0;\n"
        + "	for (i=$val(nBits)-1;i>=0;i--) {\n"
        + "	    /* shift new bits into the shift register */\n"
        + "	    word <<= 1;\n"
        + "	    if ($ref(input,i)) word += 1;\n"
        + "	}\n"
        + "	$ref(output) = word;\n";
}

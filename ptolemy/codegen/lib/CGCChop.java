/* Chop, CGC domain: CGCChop.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCChop.pl by ptlang
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
//// CGCChop
/**
On each execution, this star reads a block of "nread" samples (default 128)
and writes "nwrite" of these samples (default 64), skipping the first
"offset" samples (default 0).  It is an error if nwrite + offset > nread.
If nwrite > nread, then the output consists of overlapping windows,
and hence "offset" must be negative.
<p>
This star reads a block of particles of any type, and writes a block
of particles that somehow overlaps the input block.
The number of input particles consumed is given by <i>nread</i>,
and the number of output particles produced is given by <i>nwrite</i>.
The <i>offset</i> parameter (default 0) specifies where the output
block should start, relative to the beginning of the input block.
To avoid trying to read samples that have not yet been consumed,
it is required that <i>nwrite  +  offset  &lt;</i>=<i>  nread</i>.
Hence, if <i>nwrite  &gt;  nread</i>, <i>offset</i> must be negative,
and the output consists of overlapping blocks input particles.

 @Author S. Ha
 @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCChop.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCChop extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCChop(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);

        // Number of particles read. IntState
        nread = new Parameter(this, "nread");
        nread.setExpression("128");

        // Number of particles written. IntState
        nwrite = new Parameter(this, "nwrite");
        nwrite.setExpression("64");

        // Position of output block relative to input block. IntState
        offset = new Parameter(this, "offset");
        offset.setExpression("0");

        // If offset > 0, specify whether to use previously read inputs\n(otherwise use zeros). IntState
        use_past_inputs = new Parameter(this, "use_past_inputs");
        use_past_inputs.setExpression("YES");

        // Lower limit on the indexing from the input to the output buffer IntState
        hiLim = new Parameter(this, "hiLim");
        hiLim.setExpression("0");

        // Current index into the output buffer IntState
        inidx = new Parameter(this, "inidx");
        inidx.setExpression("0");

        // Lower limit on the indexing from the input to the output buffer IntState
        loLim = new Parameter(this, "loLim");
        loLim.setExpression("0");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type anytype.
     */
    public ClassicPort input;

    /**
     * output of type anytype.
     */
    public ClassicPort output;

    /**
     *  Number of particles read. parameter with initial value "128".
     */
     public Parameter nread;

    /**
     *  Number of particles written. parameter with initial value "64".
     */
     public Parameter nwrite;

    /**
     *  Position of output block relative to input block. parameter with initial value "0".
     */
     public Parameter offset;

    /**
     *  If offset > 0, specify whether to use previously read inputs
(otherwise use zeros). parameter with initial value "YES".
     */
     public Parameter use_past_inputs;

    /**
     *  Lower limit on the indexing from the input to the output buffer parameter with initial value "0".
     */
     public Parameter hiLim;

    /**
     *  Current index into the output buffer parameter with initial value "0".
     */
     public Parameter inidx;

    /**
     *  Lower limit on the indexing from the input to the output buffer parameter with initial value "0".
     */
     public Parameter loLim;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

computeRange();
		return ((IntToken)((nwrite).getToken())).intValue() + 2 * (((IntToken)((hiLim).getToken())).intValue() - ((IntToken)((loLim).getToken())).intValue());
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (((IntToken)((nread).getToken())).intValue() <= 0) {
                    throw new IllegalActionException(this, "The number of samples to read ",
                                    "must be positive");
                    return;
                }
                if (((IntToken)((nwrite).getToken())).intValue() <= 0) {
                    throw new IllegalActionException(this, "The number of samples to write ",
                                    "must be positive");
                    return;
                }

                if (((IntToken)((use_past_inputs).getToken())).intValue() == 1) // FIXME use_past_inputs should be a Boolean
		    input.setSDFParams(((IntToken)((nread).getToken())).intValue(),((IntToken)((nread).getToken())).intValue()+((IntToken)((offset).getToken())).intValue()-1);
		else
		    input.setSDFParams(((IntToken)((nread).getToken())).intValue(),((IntToken)((nread).getToken())).intValue()-1);

		output.setSDFParams(((IntToken)((nwrite).getToken())).intValue(),((IntToken)((nwrite).getToken())).intValue()-1);
		computeRange();
     }

    /**
     */
    public void  generateFireCode() {

StringBuffer moreDeclarations = new StringBuffer("\tint ");
		moreDeclarations  + "hiLim = " + ((IntToken)((hiLim).getToken())).intValue()  + ", "
				  + "inputIndex = " + ((IntToken)((inidx).getToken())).intValue()  + ", "
				.append("loLim = " + ((IntToken)((loLim).getToken())).intValue()  + ";\n");

		addCode(decl);
		addCode(moreDeclarations);
		if (input.resolvedType() == COMPLEX)
		  addCode(complexOut);
		else
		  addCode(nonComplexOut);
     }

    /**
     */
    protected void computeRange () {

// Compute the range of output indexes that come from inputs
                // This method is called in the setup() method for the Chop
                // star, and in the go method for ChopVarOffset because
                // it resets the offset parameter
                hiLim = ((IntToken)((nwrite).getToken())).intValue() - ((IntToken)((offset).getToken())).intValue() - 1;
                if (((IntToken)((hiLim).getToken())).intValue() >= ((IntToken)((nwrite).getToken())).intValue()) hiLim = ((IntToken)((nwrite).getToken())).intValue() - 1;
                else if (((IntToken)((use_past_inputs).getToken())).intValue() == 1) // FIXME use_past_inputs should be a Boolean hiLim = ((IntToken)((nwrite).getToken())).intValue() - 1;

                loLim = ((IntToken)((nwrite).getToken())).intValue() - ((IntToken)((nread).getToken())).intValue() - ((IntToken)((offset).getToken())).intValue();
                if (((IntToken)((loLim).getToken())).intValue() < 0) loLim = 0;

                inidx = ((IntToken)((nread).getToken())).intValue() - ((IntToken)((nwrite).getToken())).intValue() + ((IntToken)((offset).getToken())).intValue();
                if (((IntToken)((inidx).getToken())).intValue() < 0) inidx = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String decl =
        "	int i;\n";

    public String nonComplexOut =
        "	for (i = 0; i < $val(nwrite); i++) {\n"
        + "	    if (i > hiLim || i < loLim) {\n"
        + "		$ref(output,i) = 0;\n"
        + "	    }\n"
        + "	    else {\n"
        + "		$ref(output,i) = $ref(input,inputIndex);\n"
        + "		inputIndex++;\n"
        + "            }\n"
        + "        }\n";

    public String complexOut =
        "	for (i = 0; i < $val(nwrite); i++) {\n"
        + "	    if (i > hiLim || i < loLim) {\n"
        + "		$ref(output,i).real = 0;\n"
        + "		$ref(output,i).imag = 0;\n"
        + "	    }\n"
        + "	    else {\n"
        + "		$ref(output,i) = $ref(input,inputIndex);\n"
        + "		inputIndex++;\n"
        + "            }\n"
        + "        }\n";
}

/* Delay, CGC domain: CGCDelay.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCDelay.pl by ptlang
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
//// CGCDelay
/**
Bulk delay.

 @Author T. M. Parks
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCDelay.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCDelay extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCDelay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Number of delay samples. IntState
        delay = new Parameter(this, "delay");
        delay.setExpression("1");

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public ClassicPort input;

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  Number of delay samples. parameter with initial value "1".
     */
     public Parameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {

if (!(int) delay) return;
	addDeclaration(declarations);
	addCode(init);
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (!(int) delay) forkInit(input,output);
     }

    /**
     */
    public void  generateFireCode() {

if (!(int) delay) return;
	addCode(main);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String declarations =
        "	/* static so that buffer will be initialized to zero */\n"
        + "	double $starSymbol(buffer)[$val(delay)];\n"
        + "	int $starSymbol(index);\n";

    public String init =
        "	$starSymbol(index) = 0;\n"
        + "    {\n"
        + "	int i;\n"
        + "	for (i = 0 ; i < $val(delay) ; i++)\n"
        + "	    $starSymbol(buffer)[i] = 0;\n"
        + "    }\n";

    public String main =
        "	$ref(output) = $starSymbol(buffer)[$starSymbol(index)];\n"
        + "	$starSymbol(buffer)[$starSymbol(index)] = $ref(input);\n"
        + "	if ( ++$starSymbol(index) >= $val(delay) )\n"
        + "	    $starSymbol(index) -= $val(delay);\n";
}

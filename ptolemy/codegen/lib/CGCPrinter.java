/* Printer, CGC domain: CGCPrinter.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCPrinter.pl by ptlang
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
//// CGCPrinter
/**
Prints out one sample from each input port per line
If "fileName" is not equal to "cout" (the default) or "stdout", it
specifies the filename to write to.
<p>
This star prints its input, which may be int or float type.
There may be multiple inputs: all inputs are printed together on
the same line, separated by tabs.

 @Author E. A. Lee, Kennard
 @Version $Id$, based on version 1.18 of /users/ptolemy/src/domains/cgc/stars/CGCPrinter.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCPrinter extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCPrinter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        // Filename for output. StringState
        fileName = new Parameter(this, "fileName");
        fileName.setExpression("cout");

        // index for multi input trace. IntState
        index = new Parameter(this, "index");
        index.setExpression("1");

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
     *  Filename for output. parameter with initial value "cout".
     */
     public Parameter fileName;

    /**
     *  index for multi input trace. parameter with initial value "1".
     */
     public Parameter index;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return 6;	/* unreliable data */
     }

    /**
     */
    public void  wrapup() {

if (fileOutput)
		addCode("\tfclose($starSymbol(fp)); \n");
     }

    /**
     */
    public void  generatePreinitializeCode() {

Stringfn = fileName;
	    fileOutput = ! ( fn==null
	      || strcmp(fn, "cout")==0 || strcmp(fn, "stdout")==0
	      || strcmp(fn, "<cout>")==0 || strcmp(fn, "<stdout>")==0);
	    if (fileOutput) {
		StringBuffer s = new StringBuffer();
		s.append("    FILE* $starSymbol(fp);");
		addDeclaration(s);
		addInclude("<stdio.h>");
		addCode(openfile);
	    }
     }

    /**
     */
    public void  generateFireCode() {

for (int i = 1; i <= input.numberPorts(); i++) {
		index = i;
		if (fileOutput) {
			addCode(
"\tfprintf($starSymbol(fp),\"%f\\t\", (double) ($ref(input#index)));\n");
		} else {
			addCode(
"\tprintf(\"%f\\t\", (double) ($ref(input#index)));\n");
		}
	    }
	    if (fileOutput) {
		addCode("\tfprintf($starSymbol(fp),\"\\n\"); \n");
	    } else {
		addCode("\tprintf(\"\\n\"); \n");
	   }
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String openfile =
        "    if (!($starSymbol(fp)=fopen(\"$val(fileName)\",\"w\"))) {\n"
        + "	fprintf(stderr,\"ERROR: cannot open output file for Printer star.\\n\");\n"
        + "    	exit(1);\n"
        + "    }\n";
}

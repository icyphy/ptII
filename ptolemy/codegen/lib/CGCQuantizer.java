/* Quantizer, CGC domain: CGCQuantizer.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCQuantizer.pl by ptlang
*/
/*
Copyright (c) 1996-2005 The Regents of the University of California.
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
//// CGCQuantizer
/**
This star quantizes the input value to the nearest output value
in the given codebook.  The nearest value is found by a full search
of the codebook, so the star will be significantly slower than
either Quant or LinQuantIdx.  The absolute value of the difference
is used as a distance measure.

 @Author Bilung Lee, Yu Kee Lim
 @Version $Id$, based on version 1.2 of /users/ptolemy/src/domains/cgc/stars/CGCQuantizer.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCQuantizer extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCQuantizer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
        outIndex = new ClassicPort(this, "outIndex", false, true);
        outIndex.setTypeEquals(BaseType.INT);

        // Possible output values FloatArrayState
        floatCodebook = new Parameter(this, "floatCodebook");
        floatCodebook.setExpression("{0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0}");

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
Closest value in the codebook
     */
    public ClassicPort output;

    /**
Index of the closest value in the codebook
     */
    public ClassicPort outIndex;

    /**
     *  Possible output values parameter with initial value "0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0".
     */
     public Parameter floatCodebook;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<math.h>");
     }

    /**
     */
    public void  generateFireCode() {

addCode(quantizer);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String quantizer =
        "    double in = (double) $ref(input);\n"
        + "    int size = $size(floatCodebook);\n"
        + "\n"
        + "    int index = 0;\n"
        + "    double distance = fabs(in - $ref(floatCodebook,0));\n"
        + "\n"
        + "    double sum = 0;\n"
        + "    int i;\n"
        + "\n"
        + "    for (i=1; i<size; i++) {\n"
        + "      sum = fabs(in - $ref(floatCodebook,i));\n"
        + "      if (sum < distance) {\n"
        + "        index = i;\n"
        + "        distance =sum;\n"
        + "      }\n"
        + "    } \n"
        + "\n"
        + "    $ref(output) = $ref(floatCodebook,index);\n"
        + "    $ref(outIndex) = index;\n";
}

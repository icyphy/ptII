/* Mux, CGC domain: CGCMux.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMux.pl by ptlang
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
//// CGCMux
/**
Multiplexes any number of inputs onto one output stream.
B particles are consumed on each input, where B is the blockSize.
But only one of these blocks of particles is copied to the output.
The one copied is determined by the "control" input.
Integers from 0 through N-1 are accepted at the "control" input,
where N is the number of inputs.  If the control input is outside
this range, an error is signaled.
<p>
<a name="multiplex"></a>

 @Author S. Ha
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCMux.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMux extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMux(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        control = new ClassicPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);

        // Number of particles in a block. IntState
        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("1");

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
     * control of type int.
     */
    public ClassicPort control;

    /**
     * output of type anytype.
     */
    public ClassicPort output;

    /**
     *  Number of particles in a block. parameter with initial value "1".
     */
     public Parameter blockSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((blockSize).getToken())).intValue() + 3;
     }

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<stdio.h>");
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if ( ((IntToken)((blockSize).getToken())).intValue() < 1 ) {
                         throw new IllegalActionException(this, "blockSize must be positive");
                        return;
                }
                output.setSDFParams(((IntToken)((blockSize).getToken())).intValue(),((IntToken)((blockSize).getToken())).intValue()-1);
                input.setSDFParams(((IntToken)((blockSize).getToken())).intValue(),((IntToken)((blockSize).getToken())).intValue()-1);
     }

    /**
     */
    public void  generateFireCode() {

addCode(init);
                addCode(switchStatement);
                addCode("\t{\n");
                // control value i means port number i+1
                for (int i = 0; i < input.numberPorts(); i++) {
                        addCode(copydata(i,i+1));
                }
                addCode(badPortNum);
                addCode("\t}\n");
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String init =
        "        int n = $ref(control);\n"
        + "        int j = $val(blockSize);\n";

    public String switchStatement =
        "        switch(n)\n";

    public String copydata (int i, int portnum) {
        return
        "            case " + i + ":\n"
        + "                while (j--) {\n"
        + "                        $ref(output,j) = $ref(input#" + portnum + ",j);\n"
        + "                }\n"
        + "                break;\n";
    }

    public String badPortNum =
        "            default:\n"
        + "                fprintf(stderr, \"invalid control input %d\", n);\n";
}

/* SGIAudioOut, CGC domain: CGCSGIAudioOut.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSGIAudioOut.pl by ptlang
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
//// CGCSGIAudioOut
/**
Put samples into audio output port.

 @Author T. M. Parks
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCSGIAudioOut.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSGIAudioOut extends CGCSGIAudio {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSGIAudioOut(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        left = new ClassicPort(this, "left", true, false);
        left.setTypeEquals(BaseType.DOUBLE);
        right = new ClassicPort(this, "right", true, false);
        right.setTypeEquals(BaseType.DOUBLE);

/*
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Left channel.
     */
    public ClassicPort left;

    /**
Right channel.
     */
    public ClassicPort right;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {

super.initCode();
        addCode(open);
     }

    /**
     */
    public void  generateFireCode() {

addCode(write);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String open =
        "        $starSymbol(port) = ALopenport(\"$starSymbol(port)\", \"w\", $starSymbol(config));\n";

    public String write =
        "        {\n"
        + "            short buffer[2];\n"
        + "\n"
        + "            buffer[0] = (short)($ref(left) * 32768.0);\n"
        + "            buffer[1] = (short)($ref(right)* 32768.0);\n"
        + "            ALwritesamps($starSymbol(port), buffer, 2);\n"
        + "        }\n";
}

/* SGIAudio, CGC domain: CGCSGIAudio.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSGIAudio.pl by ptlang
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
//// CGCSGIAudio
/**
Base class for reading and writing SGI audio ports.

 @Author T. M. Parks
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCSGIAudio.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSGIAudio extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSGIAudio(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  wrapup() {
        
addCode(close); 
     }

    /**
     */
    public void  generatePreinitializeCode() {
        
addInclude("<audio.h>");
	addGlobal(declare);
	addCode(setup); 
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String declare = 
        "	ALport $starSymbol(port);\n"
        + "	ALconfig $starSymbol(config);\n";

    public String setup = 
        "	$starSymbol(config) = ALnewconfig();\n"
        + "	ALsetwidth($starSymbol(config), AL_SAMPLE_16);\n"
        + "	ALsetchannels($starSymbol(config), AL_STEREO);\n"
        + "	ALsetqueuesize($starSymbol(config), 0x1000);\n";

    public String close = 
        "	ALcloseport($starSymbol(port));\n"
        + "	ALfreeconfig($starSymbol(config));\n";
}

/* MonoOut, CGC domain: CGCMonoOut.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMonoOut.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
All rights reserved.
See the file $PTOLEMY/copyright for copyright notice,
limitation of liability, and disclaimer of warranty
provisions.
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
//// CGCMonoOut
/**
Writes mono(1 channel) data with either linear16 or ulaw8 encoding to
a file given by "fileName". The file can be the audio port /dev/audio,
if supported by the workstation.
<p>
This code is based on the description of the audio driver which can
be obtained by looking at the man page for audio.
The ulaw algorithm is based on the description of the T1 system found
in: Simon Haykin, "Communication Systems," section 8.2 (Wiley 1983).

 @Author T. M. Parks and Sunil Bhave
 @Version $Id$, based on version 1.16 of /users/ptolemy/src/domains/cgc/stars/CGCMonoOut.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMonoOut extends CGCAudioBase {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMonoOut(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);

        // If non-negative, the number of samples ahead that can computed. IntState
        aheadLimit = new Parameter(this, "aheadLimit");
        aheadLimit.setExpression("-1");

/*
channels.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
    channels.setInitValue(1);
    balance.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
    balance.setInitValue(0.0);
    encodingType.setInitValue("ulaw8");
    portType.setInitValue("speaker");
    sampleRate.setInitValue(8000);
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Input ranges from -1.0 to 1.0
     */
    public ClassicPort input;

    /**
     *  If non-negative, the number of samples ahead that can computed. parameter with initial value "-1".
     */
     public Parameter aheadLimit;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return ((IntToken)((blockSize).getToken())).intValue()*14;
     }

    /**
     */
    public void  wrapup() {

super.wrapup();
     }

    /**
     */
    public void  generatePreinitializeCode() {

super.initCode();

    /* variable for the sync codeblock below and its initialization */
    if ((int)aheadLimit >= 0 )
       {
	addDeclaration(syncCounter);
	addCode("$starSymbol(count) = 0; ");
       }
    /* Declare buffer type and size depending on the encoding */
    if (encodingType.equalsIgnoreCase("linear16")) {
      addDeclaration(declarations("short", ((IntToken)((blockSize).getToken())).intValue()/2));
    } else {
      addDeclaration(declarations("unsigned char", ((IntToken)((blockSize).getToken())).intValue()));
      addModuleFromLibrary("ptdspMuLaw", "src/utils/libptdsp", "ptdsp");
    }
    /* Open file for writing data */
    addCode(openFileForWriting);
    /* Setting the audio driver if the output file is /dev/audio */
    if (strcasecmp(fileName, "/dev/audio") == 0)
      {
	/* audio_setup : to set encodingType, sampleRate and channels */
	addCode("$sharedSymbol(CGCAudioBase,audio_setup)($starSymbol(file), $ref(encodingType), $ref(sampleRate), $ref(channels)); ");
	/* audio_control : to set portType, volume and balance */
	addCode("$sharedSymbol(CGCAudioBase,audio_control)($starSymbol(file), $ref(portType), $ref(volume), $ref(balance), 0); ");
	addCode("$sharedSymbol(CGCAudioBase,audio_balance)($starSymbol(file), $ref(balance)); ");
      }
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (encodingType.equalsIgnoreCase("linear16")) {
      input.setSDFParams(int(blockSize/2), int(blockSize/2)-1);
    } else {
      input.setSDFParams(((IntToken)((blockSize).getToken())).intValue(), ((IntToken)((blockSize).getToken())).intValue()-1);
    }
     }

    /**
     */
    public void  generateFireCode() {

if (encodingType.equalsIgnoreCase("linear16")) {
	addCode(convertLinear);
    } else {
	addCode(convertUlaw);
    }
    if ((int)aheadLimit >= 0 ) {
	if (encodingType.equalsIgnoreCase("linear16")) {
	    addCode(syncLinear16);
    	} else {
	    addCode(syncUlaw);
    	}
	}
    addCode(setbufptr);
    addCode(write);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String setbufptr =
        "    $starSymbol(bufferptr) = $starSymbol(buffer);\n";

    public String convertLinear =
        "{\n"
        + "    /* convert floating-point to 16-bit linear */\n"
        + "    int i;\n"
        + "    for (i=0; i <($val(blockSize)/2); i++) {\n"
        + "      /* Convert from floating point [-1.0,1.0] to 16-bit sample */\n"
        + "     $starSymbol(buffer)[$val(blockSize)/2-1-i] \n"
        + "	= (short)($ref(input, i)*32767.0);\n"
        + "    }\n"
        + "  }";

    public String convertUlaw =
        "{\n"
        + "    /* convert floating-point to 8-bit u-law encoding */\n"
        + "    int i;\n"
        + "    for (i = 0; i < $val(blockSize); i++) {\n"
        + "      /* Convert from floating point [-1.0,1.0] to 16-bit sample */\n"
        + "      short sample16 = (short)($ref(input,i) * 32767.0);\n"
        + "      /* Convert 16-bit sample to PCM mu-law */\n"
        + "      $starSymbol(buffer)[$val(blockSize)-1-i] = \n"
        + "	      Ptdsp_LinearToPCMMuLaw(sample16);\n"
        + "    }\n"
        + "  }";
}

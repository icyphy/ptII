/* MonoIn, CGC domain: CGCMonoIn.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMonoIn.pl by ptlang
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
//// CGCMonoIn
/**
Reads mono(1 channel) data with either linear16 or ulaw8 encoding from
a file given by "fileName". The file can be the audio port /dev/audio,
if supported by the workstation.
<p>
This code is based on the description of the audio driver which can
be obtained by looking at the man page for audio.
The ulaw algorithm is based on the description of the T1 system found
in: Simon Haykin, "Communication Systems," section 8.2 (Wiley 1983).

 @Author T. M. Parks and Sunil Bhave
 @Version $Id$, based on version 1.10 of /users/ptolemy/src/domains/cgc/stars/CGCMonoIn.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMonoIn extends CGCAudioBase {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMonoIn(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

/*
channels.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
    channels.setInitValue(1);
    balance.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
    balance.setInitValue(0.0);
    encodingType.setInitValue("ulaw8");
    sampleRate.setInitValue(8000);
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Output ranges from -1.0 to 1.0
     */
    public ClassicPort output;

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
    /* Declare buffer type and size depending on the encoding */
    if (encodingType.equalsIgnoreCase("linear16")) {
      addDeclaration(declarations("short", ((IntToken)((blockSize).getToken())).intValue()/2));
    }
    else {
      addDeclaration(declarations("unsigned char", ((IntToken)((blockSize).getToken())).intValue()));
      addModuleFromLibrary("ptdspMuLaw", "src/utils/libptdsp", "ptdsp");
    }
    /* Open file for reading data */
    addCode(openFileForReading);
    if (strcasecmp(fileName, "/dev/audio") == 0)
      {
	/* audio_setup : to set encodingType, sampleRate and channels */
	addCode("$sharedSymbol(CGCAudioBase,audio_setup)($starSymbol(file), $ref(encodingType), $ref(sampleRate), $ref(channels)); ");
	/* audio_control : to set portType, volume and balance */
	addCode("$sharedSymbol(CGCAudioBase,audio_control)($starSymbol(file), $ref(portType), $ref(volume), $ref(balance), 1); ");
	addCode("$sharedSymbol(CGCAudioBase,audio_balance)($starSymbol(file), $ref(balance)); ");
      }
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

if (encodingType.equalsIgnoreCase("ulaw8")) {
      output.setSDFParams(((IntToken)((blockSize).getToken())).intValue(), ((IntToken)((blockSize).getToken())).intValue()-1);
    }
    else {
      output.setSDFParams(int(blockSize/2), int(blockSize/2)-1);
    }
     }

    /**
     */
    public void  generateFireCode() {

addCode(setbufptr);
    addCode(read);
    if (encodingType.equalsIgnoreCase("linear16")) {
	addCode(convertLinear);
    } else {
	addCode(convertUlaw);
    }
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String convertLinear =
        "{\n"
        + "     /* convert from linear16 to floating point */\n"
        + "     int i;\n"
        + "     for (i=0; i <($val(blockSize)/2); i++) {\n"
        + "       /* Convert the 16-bit sample to a floating point [-1.0,1.0] */\n"
        + "	$ref(output,i) = $starSymbol(buffer)[i] /32768.0;\n"
        + "     }\n"
        + "  }";

    public String convertUlaw =
        "{\n"
        + "     /* convert from ulaw to floating point */\n"
        + "     for (i = 0; i < $val(blockSize); i++) {\n"
        + "       /* Read mu-law PCM sample from buffer */\n"
        + "       int ulawbyte = $starSymbol(buffer)[$val(blockSize)-1-i];\n"
        + "       /* Convert mu-law PCM sample to a 16-bit sample */\n"
        + "       int sample16 = Ptdsp_PCMMuLawToLinear(ulawbyte);\n"
        + "       /* Convert the 16-bit sample to a floating point [-1.0,1.0] */\n"
        + "       $ref(output,i) = ((double)sample16) / 32768.0;\n"
        + "     }\n"
        + "  }";

    public String setbufptr =
        "    $starSymbol(bufferptr) = $starSymbol(buffer);\n";
}

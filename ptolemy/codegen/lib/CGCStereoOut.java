/* StereoOut, CGC domain: CGCStereoOut.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCStereoOut.pl by ptlang
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
//// CGCStereoOut
/**
   Writes Compact Disc audio format from a file given by "fileName". The file
   can be the audio port /dev/audio, if supported by the workstation.
   The data written is linear 16 bit encoded and stereo(2 channel) format.
   <p>
   This code is based on the description of the audio driver which can
   be obtained by looking at the man page for audio.

   @Author Sunil Bhave
   @Version $Id$, based on version 1.23 of /users/ptolemy/src/domains/cgc/stars/CGCStereoOut.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCStereoOut extends CGCAudioBase {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCStereoOut(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        left = new ClassicPort(this, "left", true, false);
        left.setTypeEquals(BaseType.DOUBLE);
        right = new ClassicPort(this, "right", true, false);
        right.setTypeEquals(BaseType.DOUBLE);

        // If non-negative, the number of samples ahead that can computed. IntState
        aheadLimit = new Parameter(this, "aheadLimit");
        aheadLimit.setExpression("-1");

        // If set to 1, the star receives 2 samples of data per\n             firing IntState
        homogeneous = new Parameter(this, "homogeneous");
        homogeneous.setExpression("0");

        /*
          encodingType.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
          encodingType.setInitValue("linear16");
          channels.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
          channels.setInitValue(2);
          portType.setInitValue("line_out");
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
       Left channel input. Range: -1.0 to 1.0
    */
    public ClassicPort left;

    /**
       Right channel input Range: -1.0 to 1.0
    */
    public ClassicPort right;

    /**
     *  If non-negative, the number of samples ahead that can computed. parameter with initial value "-1".
     */
    public Parameter aheadLimit;

    /**
     *  If set to 1, the star receives 2 samples of data per
     firing parameter with initial value "0".
    */
    public Parameter homogeneous;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

        return ((IntToken)((blockSize).getToken())).intValue()*28;
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

        /* variable for the sync codeblock below */
        if ((int)aheadLimit >= 0 ) {
            addDeclaration(syncCounter);
            addCode("$starSymbol(count) = 0; ");
        }
        /* Declare "buffer" to be of type short and blockSize/2 bytes */
        addDeclaration(declarations("short", ((IntToken)((blockSize).getToken())).intValue()/2));
        /* Open file for writing data */
        addCode(openFileForWriting);
        /* Set the audio driver if file is "/dev/audio" */
        if (strcasecmp(fileName, "/dev/audio") == 0)
            {
                /* audio_setup : to set encodingType, sampleRate and channels */
                StringBuffer setupParameters = new StringBuffer("$sharedSymbol(CGCAudioBase,audio_setup)");
                setupParameters   + "($starSymbol(file), "
                    + "\"" + encodingType  + "\", "
                        <<  sampleRate  + ", "
                    .append( channels    + ");\n");

                addCode(setupParameters);
                /* audio_control : to set portType, volume and balance */
                StringBuffer controlParameters = new StringBuffer("$sharedSymbol(CGCAudioBase,audio_control)");
                controlParameters  + "($starSymbol(file), "
                    + "\"" + portType  + "\", "
                        <<  volume  + ", "
                        <<  balance  + ", "
                    .append("0);\n");
                addCode(controlParameters);
            }
    }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

        if (homogeneous == 1) {
            left.setSDFParams(1);
            right.setSDFParams(1);
        }
        else {
            left.setSDFParams(int(blockSize/4), int(blockSize/4)-1);
            right.setSDFParams(int(blockSize/4), int(blockSize/4)-1);
        }
    }

    /**
     */
    public void  generateFireCode() {

        if (homogeneous == 1) {
            addCode(homogeneousConvert);
            addCode("if ($starSymbol(counter) == ($val(blockSize)/4)) {\n");
            addCode(setbufptr);
            addCode(write);
            addCode("$starSymbol(counter) = 0; \n");
            addCode("}\n");
        }
        else {
            addCode(convert);
            if ((int)aheadLimit >= 0 ) addCode(syncLinear16);
            addCode(setbufptr);
            addCode(write);
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String homogeneousConvert =
    "    /* Take data from Input and put it in buffer */\n"
    + "    /* this code writes 2 samples per star firing to the write buffer */\n"
    + "    {\n"
    + "      int j;\n"
    + "      j = 2*$starSymbol(counter);\n"
    + "      $starSymbol(buffer)[j] = (short)($ref(left)*32767.0);\n"
    + "      $starSymbol(buffer)[j+1] = (short)($ref(right)*32767.0);\n"
    + "      $starSymbol(counter)++;\n"
    + "    }\n";

    public String convert =
    "    /* Take data from Input and put it in buffer */\n"
    + "    /* Data in buffer is alternate left and right channels */\n"
    + "    {\n"
    + "      int i, j;\n"
    + "      for (i = 0; i < ($val(blockSize)/4); i ++) {\n"
    + "        j = i*2;\n"
    + "        $starSymbol(buffer)[j] = \n"
    + "          (short)($ref(left,($val(blockSize)/4) -1 - i)*32768.0);\n"
    + "        $starSymbol(buffer)[j+1] = \n"
    + "          (short)($ref(right,($ref(blockSize)/4) - 1 - i)*32768.0);\n"
    + "      }\n"
    + "    }\n";

    public String setbufptr =
    "    $starSymbol(bufferptr) = $starSymbol(buffer);\n";
}

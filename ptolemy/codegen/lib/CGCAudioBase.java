/* AudioBase, CGC domain: CGCAudioBase.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCAudioBase.pl by ptlang
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
//// CGCAudioBase
/**
   Base star for reading and writing audio data.

   @Author Sunil Bhave
   @Version $Id$, based on version 1.36 of /users/ptolemy/src/domains/cgc/stars/CGCAudioBase.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCAudioBase extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCAudioBase(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // File for reading/writing data. If the file is \n             something other than /dev/audio, the file should \n             be in Sun audio format. If the file is in another\n             format, use conversion program available on web eg. \n             SOX - Sound Exchange http://www.spies.com/sox StringState
        fileName = new Parameter(this, "fileName");
        fileName.setExpression("/dev/audio");

        // Number of samples to read or write. IntState
        blockSize = new Parameter(this, "blockSize");
        blockSize.setExpression("16");

        // Encoding type of data being read from port. \"linear16\"\n               or \" ulaw8 \" StringState
        encodingType = new Parameter(this, "encodingType");
        encodingType.setExpression("linear16");

        // Number of samples per second. IntState
        sampleRate = new Parameter(this, "sampleRate");
        sampleRate.setExpression("44100");

        // The volume of the audio. range 0 to 10 FloatState
        volume = new Parameter(this, "volume");
        volume.setExpression("0.8");

        // Audio i/o port: line_in, line_out, cd, microphone, speaker. StringState
        portType = new Parameter(this, "portType");
        portType.setExpression("line_in");

        // Balance of audio. range -10 to 10 FloatState
        balance = new Parameter(this, "balance");
        balance.setExpression("0.0");

        // number of interleaved channels: mono = 1, stereo = 2 IntState
        channels = new Parameter(this, "channels");
        channels.setExpression("2");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  File for reading/writing data. If the file is
     something other than /dev/audio, the file should
     be in Sun audio format. If the file is in another
     format, use conversion program available on web eg.
     SOX - Sound Exchange http://www.spies.com/sox parameter with initial value "/dev/audio".
    */
    public Parameter fileName;

    /**
     *  Number of samples to read or write. parameter with initial value "16".
     */
    public Parameter blockSize;

    /**
     *  Encoding type of data being read from port. "linear16"
     or " ulaw8 " parameter with initial value "linear16".
    */
    public Parameter encodingType;

    /**
     *  Number of samples per second. parameter with initial value "44100".
     */
    public Parameter sampleRate;

    /**
     *  The volume of the audio. range 0 to 10 parameter with initial value "0.8".
     */
    public Parameter volume;

    /**
     *  Audio i/o port: line_in, line_out, cd, microphone, speaker. parameter with initial value "line_in".
     */
    public Parameter portType;

    /**
     *  Balance of audio. range -10 to 10 parameter with initial value "0.0".
     */
    public Parameter balance;

    /**
     *  number of interleaved channels: mono = 1, stereo = 2 parameter with initial value "2".
     */
    public Parameter channels;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public void  wrapup() {

        addCode(closeFile);
    }

    /**
     */
    public void  generatePreinitializeCode() {

        addInclude("<stdio.h>");
        /* Define strcmp function */
        addInclude("<string.h>");
        /* Define read and write functions */
        addInclude("<unistd.h>");
        /* Define the open function and O_RDWR constant */
        addInclude("<fcntl.h>");
        addInclude("<sys/ioctl.h>");
        /* Define the ceil function */
        addInclude("<math.h>");
        /* Define audio driver : HACK: This is Sun Specific */
        addInclude("<sys/audioio.h>");
        /* Define the SunSound Struct for audio file header*/
        addGlobal(globalDec, "globals");
        addGlobal(globals);
        addProcedure(audio_setupDef,   "CGCAudioBase_audio_setup");
        addProcedure(audio_controlDef, "CGCAudioBase_audio_control");
        addProcedure(audio_gainDef,    "CGCAudioBase_audio_gain");
        addProcedure(audio_balanceDef, "CGCAudioBase_audio_balance");
        addCode("$starSymbol(counter) = 0; \n");
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String globalDec =
    "      /* Struct that contains the header information */\n"
    + "      /* for Sun audio files */\n"
    + "      typedef struct sound_struct {\n"
    + "        int magic;               /* magic number SND_MAGIC */\n"
    + "        int dataLocation;        /* offset or pointer to the data */\n"
    + "        int dataSize;            /* number of bytes of data */\n"
    + "        int dataFormat;          /* the data format code */\n"
    + "        int samplingRate;        /* the sampling rate */\n"
    + "        int channelCount;        /* the number of channels */\n"
    + "        char info[4];            /* optional text information */\n"
    + "      } SndSoundStruct;\n";

    public String globals =
    "      int $starSymbol(file);      \n";

    public String declarations (String      datatype, int size) {
        return
            "      " + datatype + " $starSymbol(buffer)[" + size + "];\n"
            + "      " + datatype + " *$starSymbol(bufferptr);\n"
            + "      SndSoundStruct $starSymbol(header);\n"
            + "      int $starSymbol(counter);\n";
    }

    public String openFileForReading =
    "      /* Open file for reading */\n"
    + "      /* If the file is something other than /dev/audio, open the  */\n"
    + "      /* file and strip of the header */\n"
    + "      if (($starSymbol(file) = open(\"$val(fileName)\",O_RDONLY,0666)) == -1)\n"
    + "        {\n"
    + "          perror(\"$val(fileName): Error opening read-file, exiting . . .\");\n"
    + "          exit(1);\n"
    + "        }        \n"
    + "      /* To remove the header from the audio file */\n"
    + "      if ((strcasecmp(\"$val(fileName)\", \"/dev/audio\")) != 0)\n"
    + "        {\n"
    + "          read($starSymbol(file), (&$starSymbol(header)), 28);\n"
    + "          /* check whether file is in Sun audio format */\n"
    + "          if ($starSymbol(header).magic != 0x2e736e64)\n"
    + "            {\n"
    + "              perror(\"$val(fileName): File not in Sun audio format. Please refer to the star Profile.  exiting . . .\");\n"
    + "              exit(1);\n"
    + "            }\n"
    + "          /* set the corresponding defstates using info in the header */\n"
    + "          /* this can be useful if the user is not sure of the */\n"
    + "          /* data encoding in the file */\n"
    + "          if ($starSymbol(header).dataFormat == 3)\n"
    + "            $ref(encodingType) = \"linear16\";\n"
    + "          else\n"
    + "            $ref(encodingType) = \"ulaw8\";\n"
    + "          $ref(sampleRate) = $starSymbol(header).samplingRate;\n"
    + "          $ref(channels) = $starSymbol(header).channelCount;\n"
    + "        }\n";

    public String openFileForWriting =
    "      /* Open file for writing */\n"
    + "      /* If the file is something other than /dev/audio, open the */\n"
    + "      /* and add in the header at the top */\n"
    + "      /* the values for the fields will be taken from the parameters */\n"
    + "      if (($starSymbol(file) = open(\"$val(fileName)\",O_WRONLY|O_CREAT,0666)) == -1)\n"
    + "        {\n"
    + "          perror(\"$val(fileName): Error opening write-file, exiting. . .\");\n"
    + "          exit(1);\n"
    + "        }        \n"
    + "      /* Attach the header to the generated audio file */\n"
    + "      if ((strcasecmp(\"$val(fileName)\", \"/dev/audio\")) != 0)\n"
    + "        { \n"
    + "          /* magic is a magic number used to identify the structure */\n"
    + "          /* as a SNDSoundStruct */\n"
    + "          $starSymbol(header).magic = 0x2e736e64;\n"
    + "          /* offset to the first byte of sound data */\n"
    + "          $starSymbol(header).dataLocation = 28;\n"
    + "          /* DataSize should equal size of audio file */\n"
    + "          $starSymbol(header).dataSize = $val(blockSize);\n"
    + "          if (strcasecmp(\"$val(encodingType)\", \"linear16\") == 0) {\n"
    + "            /* linear16 encoding  = 3 */\n"
    + "            $starSymbol(header).dataFormat = 3;\n"
    + "          }\n"
    + "          else {\n"
    + "            /* ulaw8 encoding = 1 */\n"
    + "            $starSymbol(header).dataFormat = 1;\n"
    + "          }\n"
    + "          $starSymbol(header).samplingRate = $val(sampleRate);\n"
    + "          $starSymbol(header).channelCount = $val(channels);\n"
    + "\n"
    + "          write($starSymbol(file),(&$starSymbol(header)), 28);\n"
    + "        }\n";

    public String read =
    "      /* Read blockSize bytes of data from the file. If it returns\n"
    + "         zero, assume end of file and break - this will break out\n"
    + "          of the execution loop in with the TclTkTarget and the TychoTarget\n"
    + "        */\n"
    + "      if ( read($starSymbol(file), $starSymbol(bufferptr),\n"
    + "                $val(blockSize)) <= 0 ) {\n"
    + "            break;\n"
    + "        }\n";

    public String write =
    "      /* Write blockSize bytes to file */\n"
    + "      if (write($starSymbol(file), $starSymbol(bufferptr), $val(blockSize)) != $val(blockSize))\n"
    + "        {\n"
    + "          perror(\"$val(fileName): Error writing to file. exiting . . .\");\n"
    + "          exit(1);\n"
    + "        }\n";

    public String closeFile =
    "      /* Close file */\n"
    + "      if (close($starSymbol(file)) != 0) {\n"
    + "        perror(\"$val(fileName): Error closing file. exiting. . . \");\n"
    + "        exit(1);\n"
    + "      }\n";

    public String audio_setupDef =
    "      /*\n"
    + "        Set the encoding parameters of the audio device.\n"
    + "\n"
    + "        encodingType and precision:linear16 or ulaw8\n"
    + "        sampleRate: samples per second\n"
    + "        channels: stereo = 2, mono = 1\n"
    + "\n"
    + "        NOTE: ulaw8 will force the channels parameter to 1.\n"
    + "\n"
    + "        NOTE: Both input and output must have the same values of\n"
    + "        these parameters. If there are two stars that call this\n"
    + "        routine with different parameters, then the parameters\n"
    + "        corresponding to the most recent call will be used!\n"
    + "        */\n"
    + "      static void $sharedSymbol(CGCAudioBase,audio_setup)\n"
    + "        (int fd, char* encodingType, int sampleRate, int channels) {\n"
    + "        audio_info_t info;\n"
    + "\n"
    + "        /* Initialize the control struct */\n"
    + "        AUDIO_INITINFO(&info);\n"
    + "\n"
    + "        /* Set the type of encoding and precision for record and play */\n"
    + "        if (strcasecmp(encodingType, \"linear16\") == 0) {\n"
    + "          info.record.encoding = AUDIO_ENCODING_LINEAR;\n"
    + "          info.record.precision = 16;\n"
    + "          info.play.encoding = AUDIO_ENCODING_LINEAR;\n"
    + "          info.record.precision = 16;\n"
    + "\n"
    + "        } else if (strcasecmp(encodingType, \"ulaw8\") == 0) {\n"
    + "          info.record.encoding = AUDIO_ENCODING_ULAW;\n"
    + "          info.record.precision = 8;\n"
    + "          info.play.encoding = AUDIO_ENCODING_ULAW;\n"
    + "          info.record.precision = 8;\n"
    + "\n"
    + "          /* Force channels to 1 */\n"
    + "          channels = 1;\n"
    + "\n"
    + "        } else {\n"
    + "          perror(\"Audio encoding parameter must be \\\"linear16\\\" or \\\"ulaw8\\\". exiting . . .\");\n"
    + "          exit(1);\n"
    + "        }\n"
    + "\n"
    + "        /* Set the number of channels and sample rate */\n"
    + "        info.record.channels = channels;\n"
    + "        info.record.sample_rate = sampleRate;\n"
    + "        info.play.channels = channels;\n"
    + "        info.play.sample_rate = sampleRate;\n"
    + "\n"
    + "        /* Set the device parameters */\n"
    + "        ioctl(fd, AUDIO_SETINFO, (caddr_t)(&info));\n"
    + "\n"
    + "      }\n";

    public String audio_controlDef =
    "      /*\n"
    + "        Set the audio device controls like gain and balance.\n"
    + "        These parameters can be set once the audio is already running.\n"
    + "\n"
    + "        portType: for input: \"line_in\", \"cd\", \"microphone\"\n"
    + "                  for output: \"line_out\", \"speaker\"\n"
    + "        volume: range 0.0 - 1.0 (double)\n"
    + "        balance: range -1.0 to 1.0 (double)\n"
    + "        recordFlag: record = 1, play = 0\n"
    + "        */\n"
    + "      static void $sharedSymbol(CGCAudioBase,audio_control)\n"
    + "        (int fd, char* portType, double volume, double balance, \n"
    + "         int recordFlag) {\n"
    + "        audio_info_t info;\n"
    + "        audio_prinfo_t *audioStatusPtr;\n"
    + "\n"
    + "        /* Initialize the control struct */\n"
    + "        AUDIO_INITINFO(&info);\n"
    + "\n"
    + "        /* Pointer to audio status data structure to be modified */\n"
    + "        audioStatusPtr = recordFlag ? (&info.record) : (&info.play);\n"
    + "\n"
    + "        /* Set the port */\n"
    + "        /* FIXME: Should print a non-modal error message if bad value */\n"
    + "        if (recordFlag) {\n"
    + "          if (strcasecmp(portType, \"line_in\") == 0)\n"
    + "            audioStatusPtr->port = AUDIO_LINE_IN;\n"
    + "          else if (strcasecmp(portType, \"cd\") == 0)\n"
    + "            audioStatusPtr->port = AUDIO_INTERNAL_CD_IN;\n"
    + "          else if (strcasecmp(portType, \"microphone\") == 0)\n"
    + "            audioStatusPtr->port = AUDIO_MICROPHONE;\n"
    + "        }\n"
    + "        else {\n"
    + "          if (strcasecmp(portType, \"line_out\") == 0)\n"
    + "            audioStatusPtr->port = AUDIO_LINE_OUT;\n"
    + "          else\n"
    + "            audioStatusPtr->port = AUDIO_SPEAKER;\n"
    + "        }\n"
    + "\n"
    + "        audioStatusPtr->gain = (int)(AUDIO_MAX_GAIN * volume);\n"
    + "        audioStatusPtr->balance = (int)(AUDIO_MID_BALANCE * (balance +\n"
    + "                                                            1.0));\n"
    + "\n"
    + "        /* Set the device parameters */\n"
    + "        ioctl(fd, AUDIO_SETINFO, (caddr_t)(&info));\n"
    + "      }\n";

    public String audio_gainDef =
    "      /*\n"
    + "        Set the audio gain. Faster than audio_control if just setting\n"
    + "        gain.\n"
    + "\n"
    + "        volume: range 0.0 - 1.0 (double)\n"
    + "        recordFlag: record = 1, play = 0\n"
    + "        */\n"
    + "      static void $sharedSymbol(CGCAudioBase,audio_gain)\n"
    + "        (int fd, double volume, int recordFlag) {\n"
    + "        audio_info_t info;\n"
    + "        audio_prinfo_t *audioStatusPtr;\n"
    + "\n"
    + "        /* Initialize the control struct */\n"
    + "        AUDIO_INITINFO(&info);\n"
    + "        /* Pointer to audio status data structure to be modified */\n"
    + "        audioStatusPtr = recordFlag ? (&info.record) : (&info.play);\n"
    + "        /* Set gain value */\n"
    + "        audioStatusPtr->gain = (int)(AUDIO_MAX_GAIN * volume);\n"
    + "        /* Set the device parameters */\n"
    + "        ioctl(fd, AUDIO_SETINFO, (caddr_t)(&info));\n"
    + "      }\n";

    public String audio_balanceDef =
    "      /*\n"
    + "        Set the audio balance. Faster than audio_control if just setting\n"
    + "        gain.\n"
    + "\n"
    + "        balance: range -1.0 to 1.0 (double)\n"
    + "        */\n"
    + "      static void $sharedSymbol(CGCAudioBase,audio_balance)\n"
    + "        (int fd, double balance) {\n"
    + "        audio_info_t info;\n"
    + "\n"
    + "        /* Initialize the control struct */\n"
    + "        AUDIO_INITINFO(&info);\n"
    + "        /* Set balance value */\n"
    + "        info.play.balance = (int)(AUDIO_MID_BALANCE * (balance + 1.0));\n"
    + "        /* Set the device parameters */\n"
    + "        ioctl(fd, AUDIO_SETINFO, (caddr_t)(&info));\n"
    + "      }\n";

    public String syncCounter =
    "      /* Hack for Sun only */\n"
    + "      unsigned $starSymbol(count);\n";

    public String syncUlaw =
    "      {\n"
    + "        /* Hack for Sun only */\n"
    + "        /* Wait for samples to drain */\n"
    + "        audio_info_t info;\n"
    + "        /* Wait for some samples to drain */\n"
    + "        do {\n"
    + "          /* the cast below is to prevent warnings */\n"
    + "          ioctl($starSymbol(file), AUDIO_GETINFO, (caddr_t)(&info));\n"
    + "        } while ((int) ($starSymbol(count) - info.play.samples) > \n"
    + "                 $val(aheadLimit));\n"
    + "        $starSymbol(count) += $val(blockSize);\n"
    + "      }\n";

    public String syncLinear16 =
    "      {\n"
    + "        /* Hack for Sun only */\n"
    + "        /* Wait for samples to drain */\n"
    + "        audio_info_t info;\n"
    + "        /* Wait for some samples to drain */\n"
    + "        do {\n"
    + "          /* the cast below is to prevent warnings */\n"
    + "          ioctl($starSymbol(file), AUDIO_GETINFO, (caddr_t)(&info));\n"
    + "        } while ((int) ($starSymbol(count) - info.play.samples) > \n"
    + "                 $val(aheadLimit));\n"
    + "        $starSymbol(count) += $val(blockSize)/2;\n"
    + "      }\n";
}

/* WaveForm, CGC domain: CGCWaveForm.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCWaveForm.pl by ptlang
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
//// CGCWaveForm
/**
   Output a waveform as specified by the array state "value" (default "1 -1").
   You can get periodic signals with any period, and can halt a simulation
   at the end of the given waveform.  The following table summarizes the
   capabilities:

   haltAtEnd   periodic   period    operation
   ------------------------------------------------------------------------
   NO          YES        0         The period is the length of the waveform
   NO          YES        N>0       The period is N
   NO          NO         anything  Output the waveform once, then zeros
   YES         anything   anything  Stop after outputting the waveform once

   The first line of the table gives the default settings.
   <p>
   This star may be used to read a file by simply setting "value" to
   something of the form "&lt; filename".  The file will be read completely
   and its contents stored in an array.  The size of the array is currently
   limited to 20,000 samples.  To read longer files, use the
   <tt>ReadFile</tt>
   star.  This latter star reads one sample at a time, and hence also
   uses less storage.
   <a name="file read"></a>
   <a name="waveform from file"></a>
   <a name="reading from a file"></a>
   <a name="halting a simulation"></a>
   <a name="simulation, halting"></a>

   @Author S. Ha
   @Version $Id$, based on version 1.10 of /users/ptolemy/src/domains/cgc/stars/CGCWaveForm.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCWaveForm extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCWaveForm(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // One period of the output waveform. FloatArrayState
        value = new Parameter(this, "value");
        value.setExpression("{1 -1}");

        // Halt the run at the end of the given data. IntState
        haltAtEnd = new Parameter(this, "haltAtEnd");
        haltAtEnd.setExpression("NO");

        // Output is periodic if \"YES\" (nonzero). IntState
        periodic = new Parameter(this, "periodic");
        periodic.setExpression("YES");

        // If greater than zero, gives the period of the waveform. IntState
        period = new Parameter(this, "period");
        period.setExpression("0");

        // pos IntState
        pos = new Parameter(this, "pos");
        pos.setExpression("0");

        // size IntState
        size = new Parameter(this, "size");
        size.setExpression("0");

        /*
         */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  One period of the output waveform. parameter with initial value "1 -1".
     */
    public Parameter value;

    /**
     *  Halt the run at the end of the given data. parameter with initial value "NO".
     */
    public Parameter haltAtEnd;

    /**
     *  Output is periodic if "YES" (nonzero). parameter with initial value "YES".
     */
    public Parameter periodic;

    /**
     *  If greater than zero, gives the period of the waveform. parameter with initial value "0".
     */
    public Parameter period;

    /**
     *  pos parameter with initial value "0".
     */
    public Parameter pos;

    /**
     *  size parameter with initial value "0".
     */
    public Parameter size;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

        return 11;  /* worst case number */
    }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

        pos = 0;
        size = value.size();
    }

    /**
     */
    public void  generateFireCode() {

        addCode(body);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String body =
    "	if ($val(haltAtEnd) && ($ref(pos) >= $val(size)))\n"
    + "		break;\n"
    + "	else if ($ref(pos) >= $val(size)) {\n"
    + "		$ref(output) = 0.0;\n"
    + "		$ref(pos)++;\n"
    + "	} else {\n"
    + "		$ref(output) = $ref(value)[$ref(pos)++];\n"
    + "	}\n"
    + "	if ($val(periodic))\n"
    + "	    if ($val(period) <= 0 && $ref(pos) >= $val(size)) \n"
    + "		$ref(pos) = 0;\n"
    + "	    else if ($val(period) > 0 && $ref(pos) >= $val(period)) \n"
    + "		$ref(pos) = 0;\n";
}

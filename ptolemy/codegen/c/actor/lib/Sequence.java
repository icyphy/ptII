/* WaveForm, CGC domain: CGCWaveForm.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCWaveForm.pl by ptlang
 */
/*
  Copyright (c) 1990-1996 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCSequence
/**
   Output a sequence as specified by the array "values" (default "-1, 0, 1").
   You can get either a repeated (infinite) sequence  or 
   non-repeated (finite) sequence.


   <p>
   This star may be used to read a file by simply setting "value" to
   something of the form "&lt; filename".  The file will be read completely
   and its contents stored in an array.  The size of the array is currently
   limited to 20,000 samples.  To read longer files, use the 
   <tt>ReadFile</tt>
   star.  This latter star reads one sample at a time, and hence also
   uses less storage.

   @author Man-Kit (Jackie) Leung
   @since Ptolemy II 5.0 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class Sequence extends CCodeGeneratorHelper {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Sequence(ptolemy.actor.lib.Sequence actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    public void  generateFireCode(StringBuffer stream) {
        stream.append(_codeBlock);
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    protected String _codeBlock = 
    "	if ($ref(pos) >= $size(values)){\n"
    + "		break;\n"              
    + "	} else if ($ref(enable)) {\n"
    + "		$ref(output) = $ref(value)[$ref(pos)++];\n"
    + "	}\n"
    + "	if ($val(repeat))\n"
    + "	    if($ref(pos) >= $val(size)) \n"
    + "		$ref(pos) = 0;\n";
    
}


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
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCSequence
/**
   Output a the result of subtracting or adding the values of the input ports


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
public class AddSubtract extends CCodeGeneratorHelper {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    
    
    public AddSubtract(ptolemy.actor.lib.AddSubtract actor) {
        super(actor);
        _actor = actor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    public void  generateFireCode(StringBuffer stream) throws IllegalActionException {
        _generateCodeblocks();
        stream.append(_codeBlock);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////
    /*
     * This function is called to generate the codeblock
     */
    private void _generateCodeblocks() throws IllegalActionException {
        StringBuffer tmpStream = new StringBuffer();
        tmpStream.append("$ref(output) = ");
        Token sum = null;
        for (int i = 0; i < _actor.plus.getWidth(); i++) {
            if (_actor.plus.hasToken(i)) {
                if (sum == null) {
                    tmpStream.append("input#" + i);
                }
                else { 
                    tmpStream.append(" + input#" + i);
                }
            }
        }
        for (int i = 0; i < _actor.minus.getWidth(); i++) {
            if (_actor.minus.hasToken(i)) {
                Token in = _actor.minus.get(i);
                if (sum == null) {
                    tmpStream.append("0");
                }
                tmpStream.append(" - input#" + i);
            }
        }
        _codeBlock = tmpStream.toString() + "\n";        
    }

    
    /*
     * This is a temporary holder to store the actor,
     * so we can use it to generate the codeblock from
     * information contained in the actor
     */
    private ptolemy.actor.lib.AddSubtract _actor;

    
    protected String _codeBlock;
}

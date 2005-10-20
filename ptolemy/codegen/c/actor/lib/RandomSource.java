/* A helper class for ptolemy.actor.lib.RandomSource

Copyright (c) 1997-2005 The Regents of the University of California.
All rights reserved.
Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

PT_COPYRIGHT_VERSION_2
COPYRIGHTENDKEY

*/
package ptolemy.codegen.c.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.LongToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// RandomSource
/**
 A helper class for ptolemy.actor.lib.RandomSource.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
*/
public abstract class RandomSource extends CCodeGeneratorHelper {
    /** Constructor method for the RandomSource helper.
     *  @param actor the associated actor.
     */
    public RandomSource(ptolemy.actor.lib.RandomSource actor) {
       super(actor);
    }

    /** Generate fire code.
     *  @param code the given buffer to append the code to.
     *  @exception IllegalActionException
     */
    public void generateFireCode(StringBuffer code)
           throws IllegalActionException {
        
        super.generateFireCode(code);
        
        _generateRandomNumber(code);
    }
    
    /** Generate the code for initializing the random number generator 
     *  with the seed, if it has been given.  A seed of zero is interpreted 
     *  to mean that no seed is specified.  In such cases, a seed based on 
     *  the current time and this instance of a RandomSource is used to be 
     *  fairly sure that two identical sequences will not be returned.
     *  @return The initialize code of this actor.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());
        
        ptolemy.actor.lib.RandomSource actor =
            (ptolemy.actor.lib.RandomSource) getComponent();
        
        long sd = ((LongToken) (actor.seed.getToken())).longValue();

        if (sd != (long) 0) {
            code.append("    $actorSymbol(seed) = " + sd + ";\n");
        } else {
            code.append("    $actorSymbol(seed) = (time(0) + " +  actor.hashCode() + ");\n");
        }        
        return processCode(code.toString());
    }
    
    /** Generate the preinitialize code. 
     *  @return The preinitialize code of this actor.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());
        code.append("unsigned int $actorSymbol(seed);\n");
        return processCode(code.toString());
    }
    
    /** Get the files needed by the code generated for the RandomSource actor.
     *  @return A set of strings that are names of the files
     *  needed by the code generated for the RandomSource actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("\"stdlib.h\"");
        files.add("\"time.h\"");
        return files;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate code for producing a new random number.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected abstract void _generateRandomNumber(StringBuffer code)
            throws IllegalActionException;
    
}

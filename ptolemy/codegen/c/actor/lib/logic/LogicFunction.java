/* A helper class for ptolemy.actor.lib.logic.LogicFunction

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
package ptolemy.codegen.c.actor.lib.logic;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// LogicFunction

/**
 A helper class for ptolemy.actor.lib.logic.LogicFunction.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
*/
public class LogicFunction extends CCodeGeneratorHelper {
    /** Constructor method for the LogicFunction helper.
     *  @param actor the associated actor.
     */
    public LogicFunction(ptolemy.actor.lib.logic.LogicFunction actor) {
       super(actor);
    }

    /** Generate fire code.
     *  @param code the given buffer to append the code to.
     *  @exception IllegalActionException
     */
    public void generateFireCode(StringBuffer code)
           throws IllegalActionException {
        
        super.generateFireCode(code);
        ptolemy.actor.lib.logic.LogicFunction actor =
            (ptolemy.actor.lib.logic.LogicFunction) getComponent();
        
        String function = actor.function.getExpression();
        
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append("\n    ");
        codeBuffer.append("$ref(output) = ");

        if (function.equals("nand") || function.equals("nor") || function.equals("xnor")) {
            codeBuffer.append("!");   
        }
        codeBuffer.append("((");
        for (int i = 0; i < actor.input.getWidth(); i++) {
            if (function.equals("xor") || function.equals("xnor")) {
                codeBuffer.append("($ref(input#" + i + ")? 1: 0)");
            } else {
                codeBuffer.append("$ref(input#" + i + ")");
            }
                
            if (i < (actor.input.getWidth() - 1)) {
                if (function.equals("and") || function.equals("nand")) {
                    codeBuffer.append(" && ");
                } else if (function.equals("or") || function.equals("nor")) {
                    codeBuffer.append(" || "); 
                } else if (function.equals("xor") || function.equals("xnor")) {
                    codeBuffer.append(" + "); 
                }
            }
        }    
        codeBuffer.append(")");
        if (function.equals("xor") || function.equals("xnor")) {
            codeBuffer.append("%2");   
        }
        codeBuffer.append(");\n");
        code.append(processCode(codeBuffer.toString()));
    }
}

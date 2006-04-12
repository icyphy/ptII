/* A helper class for ptolemy.actor.lib.AddSubtract

 Copyright (c) 2006 The Regents of the University of California.
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

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A helper class for ptolemy.actor.lib.AddSubtract.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit) TODO: need $ref(something#i) to specify variables for different channels
 @Pt.AcceptedRating Red (eal)
 */
public class AddSubtract extends CCodeGeneratorHelper {
    /**
     * Constructor method for the AddSubtract helper.
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.AddSubtract actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method generate code that loops through each
     * INPUT [multi-ports] and combine (add or subtract) them.
     * The result code is put into the given code buffer
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.AddSubtract actor = 
            (ptolemy.actor.lib.AddSubtract) getComponent();

        Type type = actor.output.getType(); 
        boolean minusOnly = actor.plus.getWidth() == 0;

        ArrayList args = new ArrayList();
        args.add(new Integer(0));
        
        if (type == BaseType.STRING) {
            _codeStream.appendCodeBlock("StringPreFireBlock");
            for (int i = 0; i < actor.plus.getWidth(); i++) {
                args.set(0, new Integer(i));
                _codeStream.appendCodeBlock("StringLengthBlock", args);
            }
            _codeStream.appendCodeBlock("StringAllocBlock");
        } else {
            String blockType = isPrimitive(type) ? "" : "Token";
            String blockPort = (minusOnly) ? "Minus" : "";
            
            _codeStream.appendCodeBlock(
                    blockType + blockPort + "PreFireBlock");
        }

        String blockType = isPrimitive(type) ? codeGenType(type) : "Token";

        for (int i = 1; i < actor.plus.getWidth(); i++) {
            args.set(0, new Integer(i));
            _codeStream.appendCodeBlock(blockType + "AddBlock", args);
        }

        for (int i = minusOnly ? 1 : 0; i < actor.minus.getWidth(); i++) {
            args.set(0, new Integer(i));
            _codeStream.appendCodeBlock(blockType + "MinusBlock", args);
        }

        if (!isPrimitive(type)) {
            _codeStream.appendCodeBlock("TokenPostFireBlock");
        }
        
        return processCode(_codeStream.toString());
    }
}

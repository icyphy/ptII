/* A helper class for ptolemy.actor.lib.MultiplyDivide

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
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 A helper class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)  TODO:  $ref(something#i) to specify variables for different channels would make the code cleaner
 @Pt.AcceptedRating Red (cxh)
 */
public class MultiplyDivide extends CCodeGeneratorHelper {
    /**
     * Constructor method for the MultiplyDivide helper.
     * @param actor the associated actor
     */
    public MultiplyDivide(ptolemy.actor.lib.MultiplyDivide actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generate code that loops through each
     * INPUT [multi-ports] and combine (multiply or divide) them.
     * The result code is put into the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        Type type = actor.output.getType();
        boolean divideOnly = actor.multiply.getWidth() == 0;

        ArrayList args = new ArrayList();
        args.add(new Integer(0));

        String blockType = isPrimitive(type) ? "" : "Token";

        if (!divideOnly) {
            _codeStream.appendCodeBlock("SetNumeratorBlock");
        } else {
            _codeStream.appendCodeBlock(blockType + "SetNumeratorOneBlock");
        }

        if (actor.divide.getWidth() > 0) {
            _codeStream.appendCodeBlock("SetDenominatorBlock");
        }

        for (int i = 1; i < actor.multiply.getWidth(); i++) {
            args.set(0, new Integer(i));
            _codeStream.appendCodeBlock(blockType + "MultiplyBlock", args);
        }

        for (int i = 1; i < actor.divide.getWidth(); i++) {
            args.set(0, new Integer(i));
            _codeStream.appendCodeBlock(blockType + "DivideBlock", args);
        }

        if (actor.divide.getWidth() == 0) {
            _codeStream.appendCodeBlock("NumeratorOutputBlock");
        } else {
            _codeStream.appendCodeBlock(blockType + "OutputBlock");
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from MultiplyDivide.c,
     * replace macros with their values and append the processed code
     * block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        ArrayList args = new ArrayList();

        Type type = actor.output.getType();
        args.add(cType(type));
        _codeStream.appendCodeBlock("preinitBlock", args);

        return processCode(_codeStream.toString());
    }
}

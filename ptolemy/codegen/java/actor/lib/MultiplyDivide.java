/* A helper class for ptolemy.actor.lib.MultiplyDivide

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.codegen.java.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 A helper class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class MultiplyDivide extends JavaCodeGeneratorHelper {
    /**
     * Constructor method for the MultiplyDivide helper.
     * @param actor the associated actor
     */
    public MultiplyDivide(ptolemy.actor.lib.MultiplyDivide actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generates code that loops through each
     * input [multi-ports] and combines (add or subtract) them.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        String outputType = codeGenType(actor.output.getType());
        String multiplyType = codeGenType(actor.multiply.getType());
        String divideType = codeGenType(actor.divide.getType());

        boolean divideOnly = !actor.multiply.isOutsideConnected();

        ArrayList args = new ArrayList();

        ArrayList initArgs = new ArrayList();
        if (divideOnly) {
            initArgs.add(divideType);
        } else {
            initArgs.add(multiplyType);
            initArgs.add(outputType);
        }
        _codeStream.appendCodeBlock(divideOnly ? "divideOnlyInitProduct"
                : "initProduct", initArgs);

        args.add("");
        args.add(outputType);
        args.add(multiplyType);

        for (int i = 1; i < actor.multiply.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            _codeStream.appendCodeBlock("multiplyBlock", args);
        }

        for (int i = divideOnly ? 1 : 0; i < actor.divide.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            args.set(2, divideType);
            _codeStream.appendCodeBlock("divideBlock", args);
        }
        _codeStream.appendCodeBlock("outputBlock");

        return processCode(_codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from MultiplyDivide.c,
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        ArrayList args = new ArrayList();

        Type type = actor.output.getType();
        args.add(targetType(type));

        if (_codeStream.isEmpty()) {
            _codeStream.append(_eol
                    + _codeGenerator.comment("preinitialize "
                            + generateSimpleName(getComponent())));
        }

        _codeStream.appendCodeBlock("preinitBlock", args);

        return processCode(_codeStream.toString());
    }
}

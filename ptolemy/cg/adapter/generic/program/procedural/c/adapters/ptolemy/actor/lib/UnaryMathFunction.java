/*
 @Copyright (c) 2005-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.Set;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * An adapter class for ptolemy.actor.lib.UnaryMathFunction.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (mankit)
 */
public class UnaryMathFunction extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct the UnaryMathFunction adapter.
     * @param actor the associated actor
     */
    public UnaryMathFunction(ptolemy.actor.lib.UnaryMathFunction actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>expBlock</code>, <code>logBlock</code>,
     * <code>signBlock</code>, <code>squareBlock</code>, or
     * <code>sqrtBlock</code> from UnaryMathFunction.c depending on the
     * function parameter specified, replaces macros with their values
     * and returns the the processed code block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());
        ptolemy.actor.lib.UnaryMathFunction actor = (ptolemy.actor.lib.UnaryMathFunction) getComponent();

        String function = actor.function.getExpression();
        String codeBlockName = function.equals("exp") ? "expBlock" : function
                .equals("log") ? "logBlock"
                : function.equals("sign") ? "signBlock" : function
                        .equals("square") ? "squareBlock" : "sqrtBlock";
        code.append(_generateBlockByName(codeBlockName));

        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * UnaryMathFunction actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the UnaryMathFunction actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }
}

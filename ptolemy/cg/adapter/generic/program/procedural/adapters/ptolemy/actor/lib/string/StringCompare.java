/* A code generation adapter class for actor.lib.string.StringCompare

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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.string;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.string.StringCompare.

 @author Christopher Brooks, based on codegen StringCompare by Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class StringCompare extends NamedProgramCodeGeneratorAdapter {

    /**
     * Construct the StringCompare helper.
     * @param actor The associated actor.
     */
    public StringCompare(ptolemy.actor.lib.string.StringCompare actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>sinBlock</code>, <code>cosBlock</code>,
     * <code>tanBlock</code>, <code>asinBlock</code>, <code>acosBlock</code>,
     * or <code>atanBlock</code> from TrigFunction.c depending on the function
     * parameter specified, replaces macros with their values and appends
     * the processed code block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.string.StringCompare actor = (ptolemy.actor.lib.string.StringCompare) getComponent();

        String function = actor.function.getExpression();
        String ignoreCase = ((BooleanToken) actor.ignoreCase.getToken())
                .booleanValue() ? "ignoreCase" : "dontIgnoreCase";
        String codeBlockName = ignoreCase
                + (function.equals("equals") ? "EqualsBlock" : function
                        .equals("startsWith") ? "StartsWithBlock" : function
                                .equals("endsWith") ? "EndsWithBlock" : "ContainsBlock");
        ArrayList<String> args = new ArrayList<String>();
        code.append(getTemplateParser().generateBlockCode(codeBlockName, args));

        return code.toString();
    }
}

/* A adapter class for ptolemy.domains.de.lib.MostRecent

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib;

import java.util.LinkedList;
import java.util.Locale;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// MostRecent

/**
 * A adapter class for ptolemy.domains.de.lib.MostRecent.
 *
 * @author William Lucas
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */
public class MostRecent extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a MostRecent adapter.
     * @param actor the associated actor
     */
    public MostRecent(ptolemy.domains.de.lib.MostRecent actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * A function which returns the generated code from the C template
     * initialization method.
     * @return A string representing the Initialize C code for this actor
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        LinkedList args = new LinkedList();
        int inputWidth = ((ptolemy.domains.de.lib.MostRecent) getComponent()).input
                .getWidth();
        args.add(Integer.toString(inputWidth));
        Parameter initialValue = ((ptolemy.domains.de.lib.MostRecent) getComponent()).initialValue;
        String type = initialValue.getType().toString();
        type = type.substring(0, 1).toUpperCase(Locale.getDefault())
                + type.substring(1);
        args.add(type);

        Token initialValueToken = initialValue.getToken();
        if (initialValueToken instanceof DoubleToken) {
            double tokenDouble;
            tokenDouble = ((DoubleToken) initialValueToken).doubleValue();
            args.add(Double.toString(tokenDouble));
        } else if (initialValueToken instanceof IntToken) {
            int tokenInt;
            tokenInt = ((IntToken) initialValueToken).intValue();
            args.add(Integer.toString(tokenInt));
        } else if (initialValueToken instanceof BooleanToken) {
            boolean tokenBool;
            tokenBool = ((BooleanToken) initialValueToken).booleanValue();
            args.add(Boolean.toString(tokenBool));
        } else if (type.compareTo("Unknown") == 0) {
            args.clear();
            codeStream.appendCodeBlock("absentInitBlock", args);
            return processCode(codeStream.toString());
        } else {
            throw new IllegalActionException("Token type at MostRecent : "
                    + type + " not supported yet.");
        }

        codeStream.appendCodeBlock("initBlock", args);
        return processCode(codeStream.toString());
    }

    /**
     * A function which returns the generated code from the C template
     * preFire method.
     * @return A string representing the preFire C code for this actor
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generatePrefireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        ptolemy.domains.de.lib.MostRecent actor = (ptolemy.domains.de.lib.MostRecent) getComponent();

        codeStream.appendCodeBlock("beginPreFireBlock", args);

        if (actor.trigger.isOutsideConnected()) {
            for (int j = 0; j < actor.trigger.getWidth(); j++) {
                args.clear();
                args.add(Integer.toString(j));
                codeStream.appendCodeBlock("preFireLoopBlock", args);
            }
        }

        args.clear();
        codeStream.appendCodeBlock("endPreFireBlock", args);

        return processCode(codeStream.toString());
    }

    /**
     * A function which returns the generated code from the C template
     * wrapup method.
     * @return A string representing the wrapup C code for this actor
     * @exception IllegalActionException If illegal macro names are found.
     */
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        return processCode(codeStream.getCodeBlock("wrapupBlock"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Generate fire code.
     * The method generates code that is executed when the <i>input</i> has a Token
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        LinkedList args = new LinkedList();
        ptolemy.domains.de.lib.MostRecent actor = (ptolemy.domains.de.lib.MostRecent) getComponent();

        int inputWidth = actor.input.getWidth();
        int outputWidth = actor.output.getWidth();
        int triggerWidth = actor.trigger.getWidth();
        int commonWidth = Math.min(inputWidth, outputWidth);

        args.add(Integer.toString(inputWidth));
        args.add(Integer.toString(outputWidth));
        args.add(Integer.toString(triggerWidth));

        codeStream.appendCodeBlock("InitFireBlock", args);

        for (int i = 0; i < commonWidth; i++) {
            args.clear();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("inputChannelLoopFireBlock", args);
        }

        for (int i = commonWidth; i < inputWidth; i++) {
            args.clear();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("throwTokensLoopFireBlock", args);
        }

        for (int i = 0; i < triggerWidth; i++) {
            args.clear();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("triggerLoopFireBlock", args);
        }

        args.clear();
        codeStream.appendCodeBlock("ifTriggeredFireBlock", args);

        for (int i = 0; i < commonWidth; i++) {
            args.clear();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("ifTriggeredLoopFireBlock", args);
        }

        args.clear();
        codeStream.appendCodeBlock("endIfTriggeredFireBlock", args);

        return processCode(codeStream.toString());
    }
}

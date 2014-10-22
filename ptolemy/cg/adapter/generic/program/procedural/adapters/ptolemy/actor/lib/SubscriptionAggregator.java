/*  A adapter class for ptolemy.actor.lib.SubscriptionAggregator
 @Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 *  An adapter class for ptolemy.actor.lib.SubscriptionAggregator.
 *
 * @see ptolemy.actor.lib.SubscriptionAggregator
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 *
 */
public class SubscriptionAggregator extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the SubscriptionAggregator helper.
     * @param actor the associated actor
     */
    public SubscriptionAggregator(ptolemy.actor.lib.SubscriptionAggregator actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Subscriber.c and
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.SubscriptionAggregator actor = (ptolemy.actor.lib.SubscriptionAggregator) getComponent();

        CodeStream codeStream = _templateParser.getCodeStream();

        if (actor.input.isOutsideConnected()) {
            codeStream.appendCodeBlock("fireBlock0", false);
            ArrayList<String> args = new ArrayList<String>();
            args.add("0");
            for (int i = 1; i < actor.input.getWidth(); i++) {
                args.set(0, Integer.toString(i));
                if (actor.operation.stringValue().equals("add")) {
                    codeStream.appendCodeBlock("fireBlockAdd", args);
                } else if (actor.operation.stringValue().equals("multiply")) {
                    codeStream.appendCodeBlock("fireBlockMultiply", args);
                } else {
                    throw new IllegalActionException(
                            "SubscriptionAggregator operation '"
                                    + actor.operation + "' not supported");
                }
            }
            codeStream.appendCodeBlock("fireBlock2", false);
        }
        return processCode(codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from SubscriptionAggregator.c
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.SubscriptionAggregator actor = (ptolemy.actor.lib.SubscriptionAggregator) getComponent();

        CodeStream codeStream = _templateParser.getCodeStream();

        ArrayList<String> args = new ArrayList<String>();

        Type type = actor.output.getType();
        args.add(targetType(type));

        if (codeStream.isEmpty()) {
            codeStream.append(_eol
                    + getCodeGenerator().comment(
                            "preinitialize "
                                    + generateSimpleName(getComponent())));
        }

        codeStream.appendCodeBlock("preinitBlock", args);

        return processCode(codeStream.toString());
    }
}

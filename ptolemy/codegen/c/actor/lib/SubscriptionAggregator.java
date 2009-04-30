/*
 @Copyright (c) 2007-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * Generate C code for an actor that produces an output token on
 * on each firing with a value that is equal to the absolute value of
 * the input.
 *
 * @see ptolemy.actor.lib.SubscriptionAggregator
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 *
 */
public class SubscriptionAggregator extends CCodeGeneratorHelper {
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
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.SubscriptionAggregator actor = (ptolemy.actor.lib.SubscriptionAggregator) getComponent();

        if (actor.input.isOutsideConnected()) {
            _codeStream.appendCodeBlock("fireBlock0", false);
            ArrayList args = new ArrayList();
            args.add(Integer.valueOf(0));
            for (int i = 1; i < actor.input.getWidth(); i++) {
                args.set(0, Integer.valueOf(i));
                if (actor.operation.stringValue().equals("add")) {
                    _codeStream.appendCodeBlock("fireBlockAdd", args);
                } else if (actor.operation.stringValue().equals("multiply")) {
                    _codeStream.appendCodeBlock("fireBlockMultiply", args);
                } else {
                    throw new IllegalActionException(
                            "SubscriptionAggregator operation '"
                                    + actor.operation + "' not supported");
                }
            }
            _codeStream.appendCodeBlock("fireBlock2", false);
        }
        return processCode(_codeStream.toString());
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
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.SubscriptionAggregator actor = (ptolemy.actor.lib.SubscriptionAggregator) getComponent();

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

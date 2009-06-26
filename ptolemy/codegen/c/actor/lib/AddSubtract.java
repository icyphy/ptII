/* A helper class for ptolemy.actor.lib.AddSubtract

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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A helper class for ptolemy.actor.lib.AddSubtract.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (mankit) Pending FIXME in AddSubtract.c: need to deallocate Tokens
 @Pt.AcceptedRating Yellow (cxh)
 */
public class AddSubtract extends CCodeGeneratorHelper {
    /**
     * Construct an AddSubtract helper.
     * @param actor the associated actor
     */
    public AddSubtract(ptolemy.actor.lib.AddSubtract actor) {
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
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();

        String outputType = codeGenType(actor.output.getType());
        String plusType = codeGenType(actor.plus.getType());
        String minusType = codeGenType(actor.minus.getType());

        boolean minusOnly = !actor.plus.isOutsideConnected();

        ArrayList args = new ArrayList();

        ArrayList initArgs = new ArrayList();
        if (minusOnly) {
            initArgs.add(minusType);
        } else {
            initArgs.add(plusType);
            initArgs.add(outputType);
        }

        if (minusOnly) {
            code.append(_generateBlockCode("minusOnlyInitSum", initArgs));
        } else if (plusType.equals(outputType)) {
            code.append(_generateBlockCode("initSum"));
        } else {
            code.append(_generateBlockCode("convertAndInitSum", initArgs));
        }

        args.add("");
        args.add(outputType);
        args.add(plusType);

        for (int i = 1; i < actor.plus.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            _codeStream.appendCodeBlock("plusBlock", args);
        }

        for (int i = minusOnly ? 1 : 0; i < actor.minus.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            args.set(2, minusType);
            _codeStream.appendCodeBlock("minusBlock", args);
        }
        if (actor.output.isOutsideConnected()
                && actor.output.numberOfSinks() > 0) {
            // If the AddSubtract is in a Composite and the output is connected
            // to a port that is not connected, then don't generate code
            // for the output.  See test/auto/CompositeWithUnconnectedPort.xml
            _codeStream.appendCodeBlock("outputBlock");
        }
        return processCode(_codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from AddSubtract.c,
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();

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

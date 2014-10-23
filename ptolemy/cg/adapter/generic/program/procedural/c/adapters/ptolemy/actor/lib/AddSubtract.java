/* A adapter class for ptolemy.actor.lib.AddSubtract

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A adapter class for ptolemy.actor.lib.AddSubtract.

 @author Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (rodiers)
 @Pt.AcceptedRating Red (rodiers)
 */
public class AddSubtract
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.AddSubtract {
    /**
     *  Construct a AddSubtract adapter.
     *  @param actor The given ptolemy.actor.lib.AddSubtract actor.
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
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        ptolemy.actor.lib.AddSubtract actor = (ptolemy.actor.lib.AddSubtract) getComponent();

        String outputType = null;
        String plusType = null;
        String minusType = null;

        try {
            outputType = getCodeGenerator().codeGenType(actor.output.getType());
            plusType = getCodeGenerator().codeGenType(actor.plus.getType());
            minusType = getCodeGenerator().codeGenType(actor.minus.getType());
        } catch (Throwable throwable) {
            throw new IllegalActionException(
                    actor,
                    throwable,
                    "Failed to get the type of a port, perhaps the type needs to be explicitly set from the UI?");
        }

        ArrayList<String> args = new ArrayList<String>();

        CodeStream codeStream = _templateParser.getCodeStream();

        ArrayList initArgs = new ArrayList<String>();

        boolean minusOnly = !actor.plus.isOutsideConnected();
        if (minusOnly) {
            initArgs.add(minusType);
            codeStream.appendCodeBlock("minusOnlyInitSum", initArgs);
        } else {
            Type type = actor.output.getType();
            if (!getCodeGenerator().isPrimitive(type)) {
                initArgs.add("$tokenFunc($new("
                        + getCodeGenerator().codeGenType(type)
                        + "(1,1,0))::zero())");
            } else {
                // FIXME: this seems wrong, why doesn't zero work here?
                //$PTII/bin/ptcg -language java ./adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/test/auto/AddSubtract.xml
                if (type == BaseType.BOOLEAN) {
                    initArgs.add("false");
                } else if (type == BaseType.STRING) {
                    initArgs.add("\"\"");
                } else {
                    initArgs.add("0");
                }
            }
            getTemplateParser().generateBlockCode("initSum", initArgs);
        }
        args.add("");
        args.add(outputType);
        args.add(plusType);

        codeStream.append(_eol + "bool triggered = false;" + _eol);

        for (int i = 0; i < actor.plus.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            codeStream.appendCodeBlock("plusBlock", args);
        }

        for (int i = 0; i < actor.minus.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            args.set(2, minusType);
            codeStream.appendCodeBlock("minusBlock", args);
        }
        if (actor.output.isOutsideConnected()
                && actor.output.numberOfSinks() > 0) {
            // If the AddSubtract is in a Composite and the output is connected
            // to a port that is not connected, then don't generate code
            // for the output.  See test/auto/CompositeWithUnconnectedPort.xml
            codeStream.appendCodeBlock("outputBlock");
        }
        return processCode(codeStream.toString());
    }
}

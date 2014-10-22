/* A adapter class for ptolemy.actor.lib.AbsoluteValue

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AbsoluteValue

/**
 A adapter class for ptolemy.actor.lib.AbsoluteValue.

 @author Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class AbsoluteValue extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct a AbsoluteValue adapter.
     *  @param actor The given ptolemy.actor.lib.AbsoluteValue actor.
     */
    public AbsoluteValue(ptolemy.actor.lib.AbsoluteValue actor) {
        super(actor);
    }

    /**
     * Generate fire code for the AbsoluteValue actor.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.AbsoluteValue actor = (ptolemy.actor.lib.AbsoluteValue) getComponent();

        String type = getCodeGenerator().codeGenType(actor.input.getType());
        if (getCodeGenerator().isPrimitive(type)) {
            type = "Primitive";
        } else if (!type.equals("Complex")) {
            throw new IllegalActionException(
                    actor.output,
                    "Only primitive types "
                            + "and Complex numbers may have their absolute value taken, "
                            + "the type was " + type);
        } else {
            // FIXME: This is how we tell the system that we are using Complex.
            _templateParser.addNewTypesUsed("Complex");
        }

        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.appendCodeBlock(type + "FireBlock");
        return processCode(codeStream.toString());
    }

}

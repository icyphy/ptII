/* A adapter class for ptolemy.domains.sdf.lib.SampleDelay

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.ArrayToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// SampleDelay

/**
 A adapter class for SampleDelay.

 @author Martin Schoeberl, Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mschoebe)
 @Pt.AcceptedRating Red (mschoebe)
 */
public class SampleDelay extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct a SampleDelay adapter.
     *  @param actor The given ptolemy.domains.sdf.lib.SampleDelay actor.
     */
    public SampleDelay(ptolemy.domains.sdf.lib.SampleDelay actor) {
        super(actor);
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from SampleDealy.j,
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    //     @Override
    //     public String generatePreinitializeCode() throws IllegalActionException {
    //         super.generatePreinitializeCode();

    //         ptolemy.domains.sdf.lib.SampleDelay actor = (ptolemy.domains.sdf.lib.SampleDelay) getComponent();

    //         ArrayList<String> args = new ArrayList<String>();

    //         Type type = actor.input.getType();
    //         args.add(targetType(type));

    //         CodeStream codeStream = _templateParser.getCodeStream();

    //         if (codeStream.isEmpty()) {
    //             codeStream.append(_eol
    //                     + getCodeGenerator().comment(
    //                             "preinitialize " + getComponent().getName()));
    //         }

    //         codeStream.appendCodeBlock("preinitBlock", args);

    //         return processCode(codeStream.toString());
    //     }

    /** Generate the initialize code for the SampleDelay actor by
     *  declaring the initial values of the sink channels of the
     *  output port of the SampleDelay actor.
     *  @return The generated initialize code for the SampleDelay actor.
     *  @exception IllegalActionException If the base class throws it,
     *   or if the initial
     *   outputs of the SampleDelay actor is not defined.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.domains.sdf.lib.SampleDelay actor = (ptolemy.domains.sdf.lib.SampleDelay) getComponent();

        int length = ((ArrayToken) actor.initialOutputs.getToken()).length();

        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.append(_eol
                + getCodeGenerator().comment(
                        "initialize " + generateSimpleName(getComponent())));

        ArrayList<String> args = new ArrayList<String>();
        args.add("");
        for (int i = 0; i < length; i++) {
            args.set(0, Integer.toString(i));
            codeStream.appendCodeBlock("initTokens", args);
        }

        return processCode(codeStream.toString());
    }
}

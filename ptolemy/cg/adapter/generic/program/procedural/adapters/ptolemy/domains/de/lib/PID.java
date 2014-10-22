/* A adapter class for ptolemy.domains.de.lib.Register

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.de.lib;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Register

/**
 A adapter class for ptolemy.domains.de.lib.PID.

 @author Jeff C. Jensen
@version $Id$
@since Ptolemy II 10.0
 */
public class PID extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a PID adapter.
     * @param actor the associated actor
     */
    public PID(ptolemy.domains.de.lib.PID actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generates code that loops through each input [multi-port]
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        CodeStream codeStream = _templateParser.getCodeStream();

        /*ptolemy.domains.de.lib.Register actor = (ptolemy.domains.de.lib.Register) getComponent();
        int commonWidth = Math.min(actor.input.getWidth(), actor.output
                .getWidth());
        ArrayList<String> templateArgs = new ArrayList<String>();
        String initialValueBlock;

        //Generate preinit block; this block differs depending
        // on whether or not an initial value has been set.
        if (actor.initialValue.getToken() != null) {
            initialValueBlock = "preinitBlock_hasInitialValue";
        } else {
            initialValueBlock = "preinitBlock_noInitialValue";
        }
        templateArgs.add("");
        for (int channel = 0; channel < commonWidth; channel++) {
            templateArgs.set(0, Integer.valueOf(channel).toString());
            codeStream.appendCodeBlock(initialValueBlock, templateArgs);
        }

        //Generate trigger block; if a trigger is received, output
        // the stored value for every channel
        for (int channel = 0; channel < commonWidth; channel++) {
            templateArgs.set(0, Integer.valueOf(channel).toString());
            codeStream.appendCodeBlock("triggerBlock", templateArgs);
        }

        //Generate update (input) block; if a new value is received,
        // store the value and trigger the old value (if not already
        // for this firing)
        for (int channel = 0; channel < commonWidth; channel++) {
            templateArgs.set(0, Integer.valueOf(channel).toString());
            codeStream.appendCodeBlock("updateValueBlock", templateArgs);
        }
         */

        return processCode(codeStream.toString());
    }
}

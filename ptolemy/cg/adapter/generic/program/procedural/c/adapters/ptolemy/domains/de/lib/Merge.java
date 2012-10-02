/* An adapter class for ptolemy.domains.de.lib.Merge

 Copyright (c) 2006-2012 The Regents of the University of California.
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

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
A adapter class for ptolemy.domains.de.lib.Merge.

@author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
*/
public class Merge extends NamedProgramCodeGeneratorAdapter {

    /**
     *  Construct a Merge adapter.
     *  @param actor The given ptolemy.domains.de.lib.Merge actor.
     */
    public Merge(ptolemy.domains.de.lib.Merge actor) {
        super(actor);
    }

    /**
     * Construct the fire block.
     */
    public String generateFireCode() throws IllegalActionException {

        ptolemy.domains.de.lib.Merge actor = (ptolemy.domains.de.lib.Merge) getComponent();
        ArrayList<String> args = new ArrayList<String>();
        CodeStream codeStream = _templateParser.getCodeStream();
        args.add("");
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i).toString());
            codeStream.appendCodeBlock("mergeBlock", args);
        }
        return processCode(codeStream.toString());
    }

    /** Return a string that represents the source time.
     *  @param timeVariable The variable to be set in the generated code
     *  @return A string sets the timeVariable to the timestamp of the
     *  last input channel.
     *  @exception IllegalActionException If there is a problme
     *  getting the width of the input.
     */
    public String getSourceTimeString(String timeVariable)
            throws IllegalActionException {
        String name = CodeGeneratorAdapter.generateName((NamedObj) _component);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < ((ptolemy.domains.de.lib.Merge) _component).input
                .getWidth(); i++) {
            // This seems wrong, it overwrites timeVariable for each EventHead_ that
            // is non-null.
            result.append("if (Event_Head_" + name + "_input[" + i
                    + "] != NULL) {\n" + timeVariable + " = &Event_Head_"
                    + name + "_input[" + i + "]->tag.timestamp;\n" + "}\n");
        }
        return result.toString();
    }
}

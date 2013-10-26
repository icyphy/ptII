/* A adapter class for ptolemy.domains.de.lib.Merge.
 @Copyright (c) 2005-2013 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.de.lib;

import java.util.LinkedList;

import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////Merge

/**
 * A adapter class for ptolemy.domains.de.lib.Merge.
 *
 * @author William Lucas, Based on Merge.java by Patricia Derler
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (wlc)
 * @Pt.AcceptedRating Red (wlc)
 */
public class Merge extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the Merge adapter.
     * @param actor the associated actor
     */
    public Merge(ptolemy.domains.de.lib.Merge actor) {
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
    public String generateInitializeCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();

        LinkedList args = new LinkedList();
        Parameter discardEvents = ((ptolemy.domains.de.lib.Merge) getComponent()).discardEvents;
        boolean value = ((BooleanToken) discardEvents.getToken())
                .booleanValue();
        args.add(Boolean.toString(value));

        codeStream.appendCodeBlock("initBlock", args);
        return processCode(codeStream.toString());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Generate the fire code of the current actor.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        LinkedList args = new LinkedList();

        ptolemy.domains.de.lib.Merge actor = (ptolemy.domains.de.lib.Merge) getComponent();

        code.append(getTemplateParser().generateBlockCode("fireBeginBlock",
                args));
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.clear();
            args.add(Integer.toString(i));
            code.append(getTemplateParser().generateBlockCode("fireLoopBlock",
                    args));
        }

        return code.toString();
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

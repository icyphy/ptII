/* An adapter class for ptolemy.domains.de.lib.TimeGap

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
package ptolemy.cg.adapter.generic.program.procedural.c.xmos.adapters.ptolemy.actor.lib;

import java.util.ArrayList;
import java.util.List;

import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TimeGap

/**
 A adapter class for ptolemy.domains.de.lib.TimeGap.

 @author Jeff C. Jensen
@version $Id$
@since Ptolemy II 10.0
 */
public class MultiplyDivide
        extends
        ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib.MultiplyDivide {
    /**
     *  Construct a TimeGap adapter.
     *  @param actor The given ptolemy.actor.lib.TimeGap actor.
     */
    public MultiplyDivide(ptolemy.actor.lib.MultiplyDivide actor) {
        super(actor);
    }

    @Override
    protected String _generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.clear();
        List<String> args = new ArrayList<String>();
        StringBuffer computeResults = new StringBuffer();

        for (int i = 0; i < ((ptolemy.actor.lib.MultiplyDivide) _component).multiply
                .getWidth(); i++) {
            computeResults.append("if ($hasToken(multiply#" + i + ")) {\n"
                    + "    result = result * $get(multiply#" + i + ");\n}");
        }
        for (int i = 0; i < ((ptolemy.actor.lib.MultiplyDivide) _component).divide
                .getWidth(); i++) {
            computeResults.append("if ($hasToken(divide#" + i + ")) {\n"
                    + "    result = result / $get(divide#" + i + ");\n}");
        }
        args.add(computeResults.toString());

        codeStream.appendCodeBlock("fireBlock", args);
        return processCode(codeStream.toString());
    }

    /** Return a string that represents the source time.
     *  @param timeVariable The name of the time variable to be set.
     *  @return A string sets the timeVariable to the timestamp of the
     *  last input channel.
     *  @exception IllegalActionException If there is a problem getting the width of the divide
     *  or multiply ports.
     */
    @Override
    public String getSourceTimeString(String timeVariable)
            throws IllegalActionException {
        String name = CodeGeneratorAdapter.generateName((NamedObj) _component);
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < ((ptolemy.actor.lib.MultiplyDivide) _component).divide
                .getWidth(); i++) {
            // This seems wrong, it overwrites timeVariable for each EventHead_ that
            // is non-null.
            result.append("if (Event_Head_" + name + "_divide[" + i
                    + "] != NULL) {\n" + timeVariable + " = &Event_Head_"
                    + name + "_divide[" + i + "]->tag.timestamp;\n" + "}\n");
        }
        for (int i = 0; i < ((ptolemy.actor.lib.MultiplyDivide) _component).multiply
                .getWidth(); i++) {
            result.append("if (Event_Head_" + name + "_multiply[" + i
                    + "] != NULL) {\n" + timeVariable + " = &Event_Head_"
                    + name + "_multiply[" + i + "]->tag.timestamp;\n" + "}\n");
        }
        return result.toString();
    }

}

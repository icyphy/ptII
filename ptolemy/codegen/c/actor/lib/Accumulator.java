/* A helper class for ptolemy.actor.lib.Accumulator

 Copyright (c) 2005-2006 The Regents of the University of California.
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
 * A helper class for ptolemy.actor.lib.Accumulator.
 *
 * @author Man-Kit Leung, Gang Zhou
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (mankit) TODO: Needs to work with String, ArrayToken.  Similar to Ramp
 * @Pt.AcceptedRating Red (zgang)
 */
public class Accumulator extends CCodeGeneratorHelper {
    /**
     * Construct an Accumulator helper.
     * @param actor the associated actor.
     */
    public Accumulator(ptolemy.actor.lib.Accumulator actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Accumulator.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();
        if (actor.reset.getWidth() > 0) {
            code.append(_generateBlockCode("initReset"));
            for (int i = 1; i < actor.input.getWidth(); i++) {
                ArrayList args = new ArrayList();
                args.add(new Integer(i));
                code.append(_generateBlockCode("readReset", args));
            }
            code.append(_generateBlockCode("initSum"));
        }

        for (int i = 0; i < actor.input.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(new Integer(i));
            code.append(_generateBlockCode("readInput", args));
        }

        code.append(_generateBlockCode("sendBlock"));
        return code.toString();
    }

    /** Generate the preinitialize code. 
     *  @return The preinitialize code.
     *  @exception IllegalActionException
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.actor.lib.Accumulator actor = (ptolemy.actor.lib.Accumulator) getComponent();
        Type type = actor.input.getType();
        code.append("static " + type.toString() + " $actorSymbol(sum);\n");

        if (actor.reset.getWidth() > 0) {
            code.append("static unsigned char $actorSymbol(resetTemp);\n");
        }

        return processCode(code.toString());
    }
}

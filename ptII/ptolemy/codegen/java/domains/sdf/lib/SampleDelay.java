/* A code generation helper class for ptolemy.domains.sdf.lib.SampleDelay

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.codegen.java.domains.sdf.lib;

import java.util.ArrayList;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.data.ArrayToken;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// SampleDelay

/**
 A code generation helper class for ptolemy.domains.sdf.lib.SampleDelay.

 @author Ye Zhou
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class SampleDelay extends JavaCodeGeneratorHelper {
    /** Construct a helper with the given
     *  ptolemy.domains.sdf.lib.SampleDelay actor.
     *  @param actor The given ptolemy.domains.sdf.lib.SampleDelay actor.
     */
    public SampleDelay(ptolemy.domains.sdf.lib.SampleDelay actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the initialize code for the SampleDelay actor by
     *  declaring the initial values of the sink channels of the
     *  output port of the SampleDelay actor.
     *  @return The generated initialize code for the SampleDelay actor.
     *  @exception IllegalActionException If the base class throws it,
     *   or if the initial
     *   outputs of the SampleDelay actor is not defined.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.domains.sdf.lib.SampleDelay actor = (ptolemy.domains.sdf.lib.SampleDelay) getComponent();

        int length = ((ArrayToken) actor.initialOutputs.getToken()).length();

        _codeStream.append(_eol
                + _codeGenerator.comment("initialize "
                        + generateSimpleName(getComponent())));

        ArrayList args = new ArrayList();
        args.add("");
        for (int i = 0; i < length; i++) {
            /* Token element =*/((ArrayToken) actor.initialOutputs.getToken())
                    .getElement(i);

            args.set(0, Integer.valueOf(i));
            _codeStream.appendCodeBlock("initTokens", args);
        }

        return processCode(_codeStream.toString());
    }
}

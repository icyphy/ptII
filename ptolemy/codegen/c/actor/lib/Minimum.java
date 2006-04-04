/* A helper class for ptolemy.actor.lib.Minimum
 @Copyright (c) 2005-2006 The Regents of the University of California.

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
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.Minimum.
 *
 * @author Man-Kit Leung, Gang Zhou
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (mankit)
 */
public class Minimum extends CCodeGeneratorHelper {
    /**
     * Construct a Minimum helper.
     * @param actor the associated actor
     */
    public Minimum(ptolemy.actor.lib.Minimum actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Minimum.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Minimum actor = (ptolemy.actor.lib.Minimum) getComponent();

        _codeStream.appendCodeBlock("fireInitBlock");
        // FIXME: we need to resolve other ScalarTokens like Complex.
        for (int i = 1; i < actor.input.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(new Integer(i));
            _codeStream.appendCodeBlock("findBlock", args);
        }
        for (int i = 0; i < actor.minimumValue.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(new Integer(i));
            _codeStream.appendCodeBlock("sendBlock1", args);
        }
        for (int i = 0; i < actor.channelNumber.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(new Integer(i));
            _codeStream.appendCodeBlock("sendBlock2", args);
        }
        return processCode(_codeStream.toString());
    }
}

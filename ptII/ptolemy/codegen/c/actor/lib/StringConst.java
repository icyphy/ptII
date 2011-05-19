/* A helper class for actor.lib.StringConst

 @Copyright (c) 2005-2009 The Regents of the University of California.
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

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.StringConst.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class StringConst extends CCodeGeneratorHelper {
    /**
     * Constructor the StringConst helper.
     * @param actor the associated actor.
     */
    public StringConst(ptolemy.actor.lib.StringConst actor) {
        super(actor);
    }

    /** Generate the initialize code. Declare the variable state.
     *  @return The initialize code.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        ptolemy.actor.lib.StringConst actor = (ptolemy.actor.lib.StringConst) getComponent();
        if (actor.output.isOutsideConnected()
                && actor.output.numberOfSinks() > 0) {
            // If the actor is in a Composite and the output is connected
            // to a port that is not connected, then don't generate code
            // for the output.  See test/auto/StringConstComposite.xml
            _codeStream.appendCodeBlock("initMaybeBlock");
        }
        return processCode(_codeStream.toString());
    }
}

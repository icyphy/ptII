/* An adapter class for ptolemy.actor.lib.LimitedFiringSource

 Copyright (c) 2010-2011 The Regents of the University of California.
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

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.IntToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// LimitedFiringSource

/**
 An adapter class for ptolemy.actor.lib.LimitedFiringSource.

 @author Christopher Brooks, based on codege LimitedFiringSource by Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class LimitedFiringSource extends NamedProgramCodeGeneratorAdapter {

    /** Construct a LimitedFiringSource adapter.
     *  @param actor the associated actor
     */
    public LimitedFiringSource(ptolemy.actor.lib.LimitedFiringSource actor) {
        super(actor);
    }

    /**
     * Generate postfire code if the firingCountLimit parameter is non-zero.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePostfireCode());

        ptolemy.actor.lib.LimitedFiringSource actor = (ptolemy.actor.lib.LimitedFiringSource) getComponent();

        if (((IntToken) actor.firingCountLimit.getToken()).intValue() > 0) {
            ArrayList<String> args = new ArrayList<String>();
            code.append(getTemplateParser().generateBlockCode(
                    "postfireFiringCountLimitBlock", args));
        }
        return code.toString();
    }

    /**
     * Generate preinitialize code if the firingCountLimit parameter is non-zero.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer(super.generatePostfireCode());

        ptolemy.actor.lib.LimitedFiringSource actor = (ptolemy.actor.lib.LimitedFiringSource) getComponent();

        if (((IntToken) actor.firingCountLimit.getToken()).intValue() > 0) {
            ArrayList<String> args = new ArrayList<String>();
            code.append(getTemplateParser().generateBlockCode(
                    "preinitializeFiringCountLimitBlock", args));
        }
        return code.toString();
    }
}

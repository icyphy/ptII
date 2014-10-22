/* Record an initial token and then output that initial token during initialize(), then pass through.

 @Copyright (c) 2011-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */

package ptolemy.domains.sdf.lib;

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *
 * Record an initial token and then output that initial token during
 * initialize(), then pass through.
 *
 * <p>The BootstrapSampleDelay works in the same way as a regular
 * sample delay actor, but with the added feature that it records the
 * initial value passed into it and will send that value out as the
 * initial value on the next run.</p>
 *
 * <p>Frequently, sample delays are placed as dependency loop-breakers
 * so that a model can run, but their default value of {0} is
 * undesirable. This is commonly fixed by examining the first value
 * passed into the sample delay and either setting the sample delay's
 * starting value to this value; or recording that value in a
 * parameter within the same container as the sample delay and setting
 * the sample delay's starting value to reference the parameter.</p>
 *
 * <p>The Bootstrap sample delay internalizes the above solutions and
 * furthermore needs no additional manual upkeep should starting
 * values change.
 *
 * @author Jason Smith, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 *
 */
public class BootstrapSampleDelay extends SampleDelay {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BootstrapSampleDelay(final CompositeEntity container,
            final String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
    }

    /** During the first iteration, read exactly one input token, update
     *  the initialOutputs for a future run and send the token to the output.
     *  @exception IllegalActionException If the get() or send() methods
     *   of the ports throw it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (once) {
            final Token message = input.get(0);
            output.send(0, message);
            initialOutputs.setExpression("{" + message.toString() + "}");
            initialOutputs.setPersistent(true);
            once = false;
        } else {
            super.fire();
        }
    }

    /**
     * Reset the state for the next run.
     * @exception IllegalActionException If thrown by a base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        once = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean once = true;
}

/* A simple sink actor that consumes and discards input tokens and
 then calls System.exit() in wrapup.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Exit

/**
 A simple sink actor that consumes and discards input tokens and
 then calls System.exit() in wrapup.

 @see Stop
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Exit extends Sink {
    /** Construct an actor with an input multiport.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Exit(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume and discard at most one token from each input channel.
     *  @exception IllegalActionException If accessing the input throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                input.get(i);
            }
        }
    }

    /** Exit the Java process by calling System.exit() after all the
     *  wrapup() methods have been called.
     *  If the ptolemy.ptII.exitAfterWrapup property is <b>not</b>
     *  set, then when wrapup() is almost finished, we call System.exit()
     *  after all the wrapup() methods have been called.
     *  If the ptolemy.ptII.exitAfterWrapup property is set, then
     *  we throw an Exception.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        getManager().exitAfterWrapup();
    }
}

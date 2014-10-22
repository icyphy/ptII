/* A timed actor that outputs the simulated physical time .

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import ptolemy.actor.lib.TimedSource;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// GetCurrentMicrostep

/**
Produce an output token on each firing with a value that is
the current microstep, also called the current index,
of superdense time. This can only be used with
directors that implement SuperdenseTimeDirector.

@author Jia Zou
@version $Id$
@since Ptolemy II 8.0
@see ptolemy.actor.SuperdenseTimeDirector
@deprecated Use actor.lib.GetCurrentMicrostep instead.
@Pt.ProposedRating Yellow (jiazou)
@Pt.AcceptedRating Red
 */
@Deprecated
public class GetCurrentMicrostep extends TimedSource {
    /** Construct an actor with the given container and name.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GetCurrentMicrostep(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set the type constraints.
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send the simulated physical time to the output, which is the
     *  currentTime of the enclosing DE director.
     *  @exception IllegalActionException If send() throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (!(getDirector() instanceof DEDirector)) {
            throw new IllegalActionException(this,
                    "Actor can only work with DE or PTIDES directors.");
        }
        DEDirector director = (DEDirector) getDirector();

        output.send(0, new IntToken(director.getMicrostep()));

        super.fire();
    }

}

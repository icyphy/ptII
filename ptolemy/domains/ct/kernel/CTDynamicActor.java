/* Interface of dynamic actors in the CT domain.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// CTDynamicActor
/**
Interface of dynamic actors in the CT domain. Dynamic actors are actors
that have one or more integrators in their I/O relations.
Typically, integrators, analog filters, and anything that can be written
as<BR>
dx/dt = f(x, u, t)<BR>
y = g(x, u, t)<BR>
are dynamic actors.
<P>
There is one method defined in this interface, which is
emitTentativeOutput(). The tentative output is the output of the actor
after each iteration (but before the states are updated). They are
emitted for exciting the event generating and the output schedule.
The tentative output is the output if the current integration step is
acceptable. It may not be the "real" output, if there is an "missed" event
reported by some event detectors.
@author Jie Liu
@version $Id$
*/
public interface CTDynamicActor extends Actor{
    /** Emit the tentative outputs.
     *  @exception IllegalActionException If the data transfer is not
     *       completed.
     */
    public void emitTentativeOutputs() throws IllegalActionException;
}

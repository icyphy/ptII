/* Interface of dynamic actors in the CT domain.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// CTDynamicActor
/**
Interface of dynamic actors in the CT domain. Dynamic actors are actors
that have states and will emit their states at the beginning of an execution.
Typically, integrators and analog filters are dynamic actors. 
There is one method defined in this interface, which is
emitPotentialStates(). The potential state is the (new) state after 
each iteration (but before postfire). They are emitted for exciting the
the event generating schedule and the output schedule. The potential 
states may not be the "real" states, if there is an "missed" event 
reported during the firing of the event generation schedule.
@author Jie Liu
@version $Id$
*/
public interface CTDynamicActor {
    /** Emit the potential state.
     *  @exception IllegalActionException If the data transfer is not
     *       completed.
     */
    public void emitPotentialStates() throws IllegalActionException;
}

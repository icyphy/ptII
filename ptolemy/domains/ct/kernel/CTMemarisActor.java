/* An interface for actors that can remembers their state.

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
@ProposedRating Red (liuj@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// CTMemarisActor
/**
An interface for actors that can remember their states. The states
of the actor can be used for rolling back the simulation when needed.
This ability is essential when embedding CT subsystem inside an
event based system.
<P>
The interface defines two methods, saveStates() and restoreStates().
If the saveStates() method is called, the current states of the actor, 
for example local variables, should be remembered. When the 
restoreStates() is called, the saved states should be restored. 
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public interface CTMemarisActor {
    
    /** Restore the saved States. If there's no saved states, throws
     *  an exception.
     *  @exception IllegalActionException If there were no saved state.
     */
    public void restoreStates() throws IllegalActionException ;

    /** Save the current state of the actor.
     */
    public void saveStates();

}

/* ZenoDelay is an extension of ListenFeedBackDelay with an overridden
getDelay() method that approximates a Zeno condition.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@AcceptedRating Red (davisj@eecs.berkeley.edu)
@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.dde.demo.LocalZeno;

import ptolemy.domains.dde.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.dde.kernel.NullToken; // For Javadoc

//////////////////////////////////////////////////////////////////////////
//// ZenoDelay
/**
ZenoDelay is an extension of ListenFeedBackDelay with an overridden
getDelay() method that approximates a Zeno condition.

@author John S. Davis II
@version $Id$
@see ptolemy.domains.dde.kernel.NullToken
*/
public class ZenoDelay extends ListenFeedBackDelay {

    /** Construct a ZenoDelay actor with no container and a name
     *  that is an empty string.
     */
    public ZenoDelay()
            throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a ZenoDelay actor with the specified workspace
     *  and no name.
     * @param workspace The workspace for this ZenoDelay actor.
     */
    public ZenoDelay(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
	super(workspace);
    }

    /** Construct a ZenoDelay actor with the specified container
     *  and name.
     * @param container The container of this actor.
     * @param name The name of this actor.
     * @exception IllegalActionException If the constructor of the
     *  superclass throws an IllegalActionException.
     * @exception NameDuplicationException If the constructor of the
     *  superclass throws a NameDuplicationException .
     */
    public ZenoDelay(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** While the current time is less then 50.0, return the delay
     *  value as specified in the super class. After the current
     *  time has exceeded 50, return a delay value of 0.001 for
     *  1000 firings. After the 1000 firings are complete, resume
     *  returning the super class delay value.
     * @return The delay value depending upon whether time has
     *  exceeded 50.0.
     */
    public double getDelay() {
	if( _cntr < 1000 ) {
	    if( getCurrentTime() < 50.0 ) {
		return super.getDelay();
	    } else {
		_cntr++;
		return 0.001;
	    }
	}
	return super.getDelay();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _cntr = 0;

}

/* 

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

@ProposedRating Red (bilung@eecs.berkeley.edu)
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.process;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// MultiBranchActor
/**

@author John S. Davis II
@version $Id$
@see BranchController
*/
public class MultiBranchActor extends CompositeActor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void fire() throws IllegalActionException {
        try {
            _workspace.getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

	    ((ProcessDirector)getDirector()).transferBoundaryData();

            // Note that this is assured of firing the local director,
            // not the executive director, because this is opaque.
            getDirector().fire();

        } finally {
            _workspace.doneReading();
        }
    }

    /** 
     */
    public void initialize() throws IllegalActionException {
	Director director = getDirector();
	if( !isOpaque() ) {
	    throw new IllegalActionException(this, "Error: " +
		    "MultiBranchActors must be opaque.");
	} else if( !(director instanceof ProcessDirector) ) {
	    throw new IllegalActionException(this, "Error: " +
		    "The local director of a MultiBranchActor " +
		    "must be an instance of " +
		    "ptolemy/actor/process/ProcessDirector.");
	}
	super.initialize();
    }

}




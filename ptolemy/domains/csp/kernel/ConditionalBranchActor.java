/* Interface for the actor that contain a ConditionalBranchControl.

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

package ptolemy.domains.csp.kernel;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// ConditionalBranchActor
/**
ConditionalBranchActor is an actor that contain a controller to manage
the conditional branches for performing conditional communication within
CSP domain.

@author Bilung Lee
@version $Id$
@see ConditionalBranch
@see ConditionalBranchController
*/
public interface ConditionalBranchActor extends Actor {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the the conditional branch controller of this actor.
     *  @return The conditional branch controller.
     */
    public ConditionalBranchController getConditionalBranchController();
}

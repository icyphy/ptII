/* An Actor that follows dataflow semantics.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;


//////////////////////////////////////////////////////////////////////////
//// DataflowActor
/**
A Dataflow Actor is a actor that follows dataflow semantics.   
At any time it may be queried for the number of tokens that 
it will produce or consume on any of its ports during its next firing.

@author Stephen Neuendorffer
@version $Id$

@see ptolemy.domains.sdf.kernel.SDFAtomicActor
@see ptolemy.domains.sdf.kernel.SDFCompositeActor
@see ptolemy.actors.CompositeActor
@see ptolemy.actors.IOPort
*/
interface DataflowActor {


    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @return The number of tokens produced on the port.
     */
    public int getTokenProductionRate(IOPort p) 
        throws IllegalActionException;


    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  @return The number of tokens consumed on the port.
     */
    public int getTokenConsumptionRate(IOPort p) 
        throws IllegalActionException;

}




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
     *  @throw IllegalActionException if port is not contained in this actor,
     *  or is not an input port.
     *  @return The number of tokens consumed on the port.
     */
    public int getTokenConsumptionRate(IOPort p) 
        throws IllegalActionException;


    /** Get the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   
     *
     *  @throw IllegalActionException if port is not contained in this actor.
     *  or is not an output port.
     *  @return The number of tokens produced on the port.
     */
    public int getTokenProductionRate(IOPort p) 
        throws IllegalActionException;

    /** Set the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.   This will generally
     *  be called in an AtomicActor to define it's behavior.   It may also
     *  be called in an opaque CompositeActor to place a non-dataflow domain
     *  inside of a dataflow domain.  (In this case the CompositeActor cannot
     *  determine the rate by scheduling the contained domain, and it must be
     *  explicitly declared.)
     *
     *  @throw IllegalActionException if port is not contained in this actor,
     *  or is not an input port.
     *  @return The number of tokens consumed on the port.
     */
            /*
    public void setTokenConsumptionRate(IOPort p, int count) 
        throws IllegalActionException;
        */

    /** Set the number of tokens that are produced or consumed 
     *  on the designated port of this Actor.  This will generally
     *  be called in an AtomicActor to define it's behavior.   It may also
     *  be called in an opaque CompositeActor to place a non-dataflow domain
     *  inside of a dataflow domain.  (In this case the CompositeActor cannot
     *  determine the rate by scheduling the contained domain, and it must be
     *  explicitly declared.) 
     *
     *  @throw IllegalActionException if port is not contained in this actor,
     *  or is not an output port.
     *  @return The number of tokens produced on the port.
     */
            /*    public void setTokenProductionRate(IOPort p, int count) 
        throws IllegalActionException;
        */
            /* public int getTokenConsumptionDelay(IOPort p)
            throws IllegalActionException;

    public int getTokenProductionDelay(IOPort p)
            throws IllegalActionException;

    public void setTokenConsumptionDelay(IOPort p, int count)
            throws IllegalActionException;

    public void setTokenProductionDelay(IOPort p, int count)
            throws IllegalActionException;
            */
}




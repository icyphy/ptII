/* Interface for transparent directors for CT sub-systems.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

//////////////////////////////////////////////////////////////////////////
//// CTTransparentDirector
/**
Interface for CT transparent directors. Transparent directors in the CT
domain can transfer its internal step size control information to the
executive director. It defines methods to support the step size control
queries by the executive CTDirector, such that after the internal
CT subsystem finishes one integration step, its step size control information
will be accessible by the outside CT director.
<P>
Implementations of this interface are typically contained by CTCompositeActors.

@see CTCompositeActor
@author  Jie Liu
@version $Id$
@since Ptolemy II 0.3

*/
public interface CTTransparentDirector {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Implementations of this method should return
     *  true if there is an event at current time.
     *  @return True if there is an event at current time.
     */
    public boolean hasCurrentEvent();

    /** Implementations of this method should return
     *  true if the current integration step is accurate from
     *  this director's point of view.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate();

    /** Implementations of this method should return
     *  the predicted next step size if this step is accurate.
     *  @return The predicted step size.
     */
    public double predictedStepSize();

    /** Implementations of this method should return
     *  the refined step size if this step is not accurate.
     *  @return The refined step size.
     */
    public double refinedStepSize();

}


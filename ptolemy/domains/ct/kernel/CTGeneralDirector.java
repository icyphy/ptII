/* Interface for general directors for CT systems.

Copyright (c) 1999-2004 The Regents of the University of California.
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

import ptolemy.actor.util.Time;


//////////////////////////////////////////////////////////////////////////
//// CTGeneralDirector
/**
   Interface for CT directors. This interface defines the methods for lower 
   level CT directors to access information of the top level CT director.    

   @see CTDirector
   @see CTTransparentDirector
   @author  Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)

*/
public interface CTGeneralDirector {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current step size used by the solver.
     *  @return The current step size.
     */
    public double getCurrentStepSize();

    /** Return the enclosing CT general director of this director, or null if
     *  this director is at the top level or the enclosing director is
     *  not a CT general director.
     *
     *  @return The enclosing CT general director of this director, if there 
     *  is any.
     */
    public CTGeneralDirector getEnclosingCTGeneralDirector();

    /** Return the current execution phase.
     * @return The the current execution phase.
     */
    public CTExecutionPhase getExecutionPhase();
    
    /** Return the current time the current iteration starts.
     * @return The time the current iteration starts.
     */
    public Time getIterationBeginTime();
    
    /** Return the ODE solver.
     *  @return The default ODE solver associated with this director.
     */
    public ODESolver getODESolver();
    
    /** Return true if the current execution phase is a discrete one. 
     *  @return true if the current execution phase is a discrete one.
     */
    public boolean isDiscretePhase();
}


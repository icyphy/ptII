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

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CTGeneralDirector
/**
   Interface for CT transparent directors. Transparent directors in the CT
   domain can transfer its internal step size control information to the
   executive director. It defines methods to support the step size control
   queries by the executive CTDirector, such that after the internal
   CT subsystem finishes one integration step, its step size control information
   will be accessible by the outside CT director.
   <P>
   Implementations of this interface are typically contained by CTCompositeActors.

   @see CTGeneralDirector
   @author  Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Yellow (liuj)
   @Pt.AcceptedRating Yellow (chf)

*/
public interface CTGeneralDirector {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public double getCurrentStepSize();

    /** Return the ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getODESolver();

    /**
     * @return
     */
    public boolean isDiscretePhase();

    /**
     * @return
     */
    public boolean isPureDiscretePhase();

    /**
     * @return
     */
    public boolean isWaveformGeneratingPhase();

    /**
     * @return
     */
    public boolean isEventGeneratingPhase();
    
    public boolean isCreatingIterationStartingStatesPhase();

    public boolean isSolvingStatesPhase();
    public boolean isProducingOutputsPhase();    
    public boolean isUpdatingContinuousStatesPhase();
    public boolean isPrefiringDynamicActorsPhase();
    public boolean isFiringEventGeneratorsPhase();
    public boolean isFiringDynamicActorsPhase();
    public boolean isFiringStateTransitionActorsPhase();
}


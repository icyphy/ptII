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

   @see CTDirector, CTTransparentDirector
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

    /** Return the ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getODESolver();
    
    /** Return true if the current execution phase deals with 
     *  generating the starting states for the following continuous phase
     *  execution. 
     *  @return true if the current execution phase deals with
     *  generating the starting states for the following continuous phase
     *  execution.
     */
    public boolean isCreatingIterationStartingStatesPhase();

    /** Return true if the current execution phase is a discrete one. 
     *  @return true if the current execution phase is a discrete one.
     */
    public boolean isDiscretePhase();

    /** Return true if the current execution phase deals with 
     *  event generator actors only. Event generator actors are 
     *  those actors transform continuous signals into discrete signals.   
     *  @return true if the current execution phase deals with
     *  event generator actors only.
     */
    public boolean isEventGeneratingPhase();

    /** Return true if the current execution phase deals with 
     *  firing dynamic actors only.  
     *  @return true if the current execution phase deals with
     *  firing dynamic actors only.  
     */
    public boolean isFiringDynamicActorsPhase();

    /** Return true if the current execution phase deals with 
     *  firing event generator actors only.  
     *  @return true if the current execution phase deals with
     *  firing event generator actors only.  
     */
    public boolean isFiringEventGeneratorsPhase();

    /** Return true if the current execution phase deals with 
     *  firing state transition actors only.  
     *  @return true if the current execution phase deals with
     *  firing state transition actors only.  
     */
    public boolean isFiringStateTransitionActorsPhase();

    /** Return true if the current execution phase deals with 
     *  prefiring dynamic actors only.  
     *  @return true if the current execution phase deals with
     *  prefiring dynamic actors only.  
     */
    public boolean isPrefiringDynamicActorsPhase();

    /** Return true if the current execution phase deals with 
     *  producing outputs of output actors only.  
     *  @return true if the current execution phase deals with
     *  producing outputs of output actors only.  
     */
    public boolean isProducingOutputsPhase();    

    /** Return true if the current execution phase deals with 
     *  pure discrete actors only. Pure discrete actors are those actors
     *  transform discrete signals into discrete signals.   
     *  @return true if the current execution phase deals with
     *  pure discrete actors only.
     */
    public boolean isPureDiscretePhase();

    /** Return true if the current execution phase deals with 
     *  solving states of stateful actors only.  
     *  @return true if the current execution phase deals with
     *  solving states of stateful actors only.
     */
    public boolean isSolvingStatesPhase();

    /** Return true if the current execution phase deals with 
     *  updating the states of continuous actors only.  
     *  @return true if the current execution phase deals with
     *  updating the states of continuous actors only.  
     */
    public boolean isUpdatingContinuousStatesPhase();

    /** Return true if the current execution phase deals with 
     *  waveform generator actors only. Waveform generator actors are 
     *  those actors transform discrete signals into continuous signals.   
     *  @return true if the current execution phase deals with
     *  waveform generator actors only.
     */
    public boolean isWaveformGeneratingPhase();

    /** Return the current time the current iteration starts.
     * @return The time the current iteration starts.
     */
    public Time getIterationBeginTime();
}


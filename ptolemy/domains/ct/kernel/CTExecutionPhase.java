/* A type-safe enumeration of possible execution phases of a CT director.

Copyright (c) 1998-2004 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// CTExecutionPhase
/** A type-safe enumeration of possible execution phases of a CT director.

   @author  Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public final class CTExecutionPhase {

    /** A private constructor.
     */
    private CTExecutionPhase(String executionPhaseName) {
        _executionPhaseName = executionPhaseName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return the string representation of this object.
     *  @return the string representation of this object.
     */
    public String toString() {
        return _executionPhaseName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** This final static memeber indicates the director is creating
     *  the starting states for the following continuous phase execution. 
     */
    public final static CTExecutionPhase CREATINGSTARTINGSTATES_PHASE 
        = new CTExecutionPhase("CREATINGSTARTINGSTATES_PHASE");

    /** This final static memeber indicates the director is firing dynamic
     *  actors. 
     */
    public final static CTExecutionPhase FIRINGDYNAMICACTORS_PHASE 
        = new CTExecutionPhase("FIRINGDYNAMICACTORS_PHASE");

    /** This final static memeber indicates the director is firing event
     *  generators.
     */
    public final static CTExecutionPhase FIRINGEVENTGENERATORS_PHASE 
        = new CTExecutionPhase("FIRINGEVENTGENERATORS_PHASE");

    /** This final static memeber indicates the director is firing
     *  purely discrete actors. 
     */
    public final static CTExecutionPhase FIRINGPURELYDISCRETE_PHASE 
        = new CTExecutionPhase("FIRINGPURELYDISCRETE_PHASE");

    /** This final static memeber indicates the director is firing state 
     *  transition actors. 
     */
    public final static CTExecutionPhase FIRINGSTATETRANSITIONACTORS_PHASE 
        = new CTExecutionPhase("FIRINGSTATETRANSITIONACTORS_PHASE");

    /** This final static memeber indicates the director is generating
     *  discrete events by iterating event generators. 
     */
    public final static CTExecutionPhase GENERATINGEVENTS_PHASE 
        = new CTExecutionPhase("GENERATINGEVENTS_PHASE");

    /** This final static memeber indicates the director is generating
     *  continuous waveforms by iterating waveform generators. 
     */
    public final static CTExecutionPhase GENERATINGWAVEFORMS_PHASE 
        = new CTExecutionPhase("GENERATINGWAVEFORMS_PHASE");

    /** This final static memeber indicates the director is postfiring event
     *  generators.
     */
    public final static CTExecutionPhase POSTFIRINGEVENTGENERATORS_PHASE
        = new CTExecutionPhase("POSTFIRINGEVENTGENERATORS_PHASE");

    /** This final static memeber indicates the director is prefiring
     *  dynamic actors. 
     */
    public final static CTExecutionPhase PREFIRINGDYNAMICACTORS_PHASE 
        = new CTExecutionPhase("PREFIRINGDYNAMICACTORS_PHASE");

    /** This final static memeber indicates the director is producing
     *  outputs. 
     */
    public final static CTExecutionPhase PRODUCINGOUTPUTS_PHASE 
        = new CTExecutionPhase("PRODUCINGOUTPUTS_PHASE");

    /** This final static memeber indicates the director is solving
     *  the states of dynamic actors. 
     */
    public final static CTExecutionPhase SOLVINGSTATES_PHASE 
        = new CTExecutionPhase("SOLVINGSTATES_PHASE");

    /** This final static memeber indicates the director is not in any 
     *  of the above phases.
     */
    public final static CTExecutionPhase UNKNOWN_PHASE 
        = new CTExecutionPhase("UNKNOWN_PHASE");

    /** This final static memeber indicates the director is updating
     *  (postfiring) all the continuous actors. 
     */
    public final static CTExecutionPhase UPDATINGCONTINUOUSSTATES_PHASE 
        = new CTExecutionPhase("UPDATINGCONTINUOUSSTATES_PHASE");

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // private name of execution phase.
    private String _executionPhaseName;
}

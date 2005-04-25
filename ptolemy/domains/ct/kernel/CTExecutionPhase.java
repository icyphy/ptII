/* A type-safe enumeration of all possible execution phases of a CT director.

Copyright (c) 2004-2005 The Regents of the University of California.
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

/** A type-safe enumeration of all possible execution phases of a CT director.

@author  Haiyang Zheng
@version $Id$
@since Ptolemy II 4.1
@Pt.ProposedRating Green (hyzheng)
@Pt.AcceptedRating Green (hyzheng)
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

    /** This final static member indicates the director is firing dynamic
     *  actors.
     */
    public final static CTExecutionPhase FIRING_DYNAMIC_ACTORS_PHASE = new CTExecutionPhase(
            "FIRING_DYNAMIC_ACTORS_PHASE");

    /** This final static member indicates the director is firing event
     *  generators.
     */
    public final static CTExecutionPhase FIRING_EVENT_GENERATORS_PHASE = new CTExecutionPhase(
            "FIRING_EVENT_GENERATORS_PHASE");

    /** This final static member indicates the director is firing state
     *  transition actors.
     */
    public final static CTExecutionPhase FIRING_STATE_TRANSITION_ACTORS_PHASE = new CTExecutionPhase(
            "FIRING_STATE_TRANSITION_ACTORS_PHASE");

    /** This final static member indicates the director is generating
     *  discrete events by iterating event generators.
     */
    public final static CTExecutionPhase GENERATING_EVENTS_PHASE = new CTExecutionPhase(
            "GENERATING_EVENTS_PHASE");

    /** This final static member indicates the director is generating
     *  continuous waveforms by iterating waveform generators.
     */
    public final static CTExecutionPhase GENERATING_WAVEFORMS_PHASE = new CTExecutionPhase(
            "GENERATING_WAVEFORMS_PHASE");

    /** This final static member indicates the director is iterating
     *  purely discrete actors.
     */
    public static final CTExecutionPhase ITERATING_PURELY_DISCRETE_ACTORS_PHASE = new CTExecutionPhase(
            "ITERATING_PURELY_DISCRETE_ACTORS_PHASE");

    /** This final static member indicates the director is postfiring event
     *  generators.
     */
    public final static CTExecutionPhase POSTFIRING_EVENT_GENERATORS_PHASE = new CTExecutionPhase(
            "POSTFIRING_EVENT_GENERATORS_PHASE");

    /** This final static member indicates the director is prefiring
     *  dynamic actors.
     */
    public final static CTExecutionPhase PREFIRING_DYNAMIC_ACTORS_PHASE = new CTExecutionPhase(
            "PREFIRING_DYNAMIC_ACTORS_PHASE");

    /** This final static member indicates the director is producing
     *  outputs via firing purely continuous actors.
     */
    public final static CTExecutionPhase PRODUCING_OUTPUTS_PHASE = new CTExecutionPhase(
            "PRODUCING_OUTPUTS_PHASE");

    /** This final static member indicates the director is not in any
     *  of the well-defined phases.
     */
    public final static CTExecutionPhase UNKNOWN_PHASE = new CTExecutionPhase(
            "UNKNOWN_PHASE");

    /** This final static member indicates the director is updating
     *  (postfiring) all the continuous actors.
     */
    public final static CTExecutionPhase UPDATING_CONTINUOUS_STATES_PHASE = new CTExecutionPhase(
            "UPDATING_CONTINUOUS_STATES_PHASE");

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // private name of execution phase.
    private String _executionPhaseName;
}

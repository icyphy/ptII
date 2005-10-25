/* Interface for general directors for CT systems.

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

import ptolemy.actor.util.Time;

//////////////////////////////////////////////////////////////////////////
//// CTGeneralDirector

/**
 Interface that defines the methods for lower level CT directors in hierarchy
 to query information from the upper level CT directors. The information
 includes the current step size for integration, the current execution phase,
 the beginning time of the current integration, and the current solver.
 All CT directors must implement this interface.
 <p>
 The {@link CTDirector} class and {@link CTTransparentDirector} interface
 directly implements this interface. The CTDirector class focuses on how to
 solve ordinary differential equations. The CTTransparentDirector interface
 defines the methods for upper level CT directors in hierarchy to query
 step size control information from the lower level CT directors.

 @author  Haiyang Zheng
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)

 */
public interface CTGeneralDirector {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current step size used by this director.
     *  @return The current step size used by this director.
     */
    public double getCurrentStepSize();

    /** Return the error tolerance, used by state and output step size
     *  actors.
     *  @return The error tolerance.
     */
    public double getErrorTolerance();

    /** Return the executive CT general director of this director, or null if
     *  this director is at the top level or the executive director is
     *  not a CT general director.
     *  @return The executive CT general director of this director, if there
     *  is any.
     */
    public CTGeneralDirector getExecutiveCTGeneralDirector();

    /** Return the current execution phase.
     *  @return The the current execution phase.
     */
    public CTExecutionPhase getExecutionPhase();

    /** Return the time the current iteration starts from.
     *  @return The time the current iteration starts from.
     */
    public Time getIterationBeginTime();

    /** Return the class name for the ODE solver.
     *  @return The class name for the ODE solver.
     */
    public String getODESolverClassName();

    /** Return true if the current phase of execution is a discrete one.
     *  @return true if the current phase of execution is a discrete one.
     */
    public boolean isDiscretePhase();
}

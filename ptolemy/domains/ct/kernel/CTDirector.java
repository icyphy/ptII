/* An base class for directors of Continuous time simulation.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// CTDirector
/** 

@author Jie Liu
@version $Id$
@see ptolemy.actor.Director
*/
public interface CTDirector extends Nameable{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current ODESolver. 
     *  @return The current CTODESolver
     */
    public ODESolver getCurrentODESolver();

    /** Return the current step size. In a fixed step size method this is 
     *  is the value set by setParam("initialStepSize"). For a variable step
     *  size method, the step size is controlled by the algorithm.
     *  @return the current step size.
     */
    public double getCurrentStepSize();

    /** Return the currentTime.
     *  @return the currentTime.
     */
    public double getCurrentTime(); 

    /** Return the initial step size, as in the parameter.
     */
    public double getInitialStepSize();

    /** Return the time accuracy such that two time stamp within this
     *  accuracy is considered identical.
     *  @return The time accuracy.
     */
    public double getTimeAccuracy();


    /** Return the next break point of the waveform. A break point of 
     *  the waveform is the time point at which the derivative of the
     *  waveform is not continuous. The returned value is the the smallest
     *  time of the next break point registed by the actors. If no actor
     *  set this time after resetNextJumpTime() is called, then return the
     *  simulation stop time.
     *
     *  @return The next jump time.
     */
    public double nearestBreakPoint() ;

    /** Regist the next time point when a discontinuity (jump) occurs.
     *  This discontinuity may result in changing ODE solver in
     *  some derived directors. If the time is smaller than the
     *  currentTime then throw an IllegalActionException. Otherwise,
     *  the next jump time is set to the minimum of the remembered
     *  next jump time and the specified time.
     *
     *  @param The next time a jump occurs.
     *  @exception IllegalActionException If the time is smaller than
     *        the current time.
     */
    public void registBreakPoint(double nextjumptime);

    /** Reset the next jump time to the simulation stop time. This
     *  method is suggested to be called at each prefire() stage
     *  of the director. After that each actor may regist a smaller
     *  next jump time at their prefire() stage. The actual next
     *  jump time is the minimum of them.
     */
    public void resetBreakPoints();

    /** Set the current ODE solver. The solver's container will be
     *  set to this director.
     *  The ODE solver will handle the algorithms in integrators.
     *
     *  @param solver The ODE solver for this director.
     *  @exception IllegalActionException If the solver is not compatible
     *        with the director. Not thrown here
     */
    public void setCurrentODESolver(ODESolver solver)
            throws IllegalActionException ;

    /** Set the current step size. This variable is very import during
     *  the simulation and can not be changed in the middle of an
     *  iteration.
     *  @param curstepsize The step size used for currentStepSize().
     */
    public void setCurrentStepSize(double curstepsize);

    /** Set the current simulation time. All the actors directed by this
     *  director will share this global time. 
     *  @param tnow The current time.
     */
    public void setCurrentTime(double tnow);

}

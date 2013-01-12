/* Interface for actors that control integration step sizes.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
package ptolemy.actor.continuous;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ContinuousStepSizeController

/**
 Interface for actors and directors that control integration step sizes.
 This interface should be implemented by components that discover
 breakpoints during an integration step (such as level-crossing
 detectors) and by integrators.
 <P>
 Actors can affect the integration step size in two ways. The first one
 is by introducing predictable breakpoints. To request a breakpoint
 at time <i>t</i>, an actor can call the fireAt()
 method of ContinuousDirector with a time argument <i>t</i>,
 the director will treat <i>t</i> as a breakpoint. Actors that only
 introduce predictable breakpoints need not implement this interface.
 The director guarantees that no step size will be use that is large
 enough to step over <i>t</i>.
 <P>
 The second way of controlling step size is through checking the accuracy
 after each integration step. We treat an integration step as accurate if
 the numerical integration error is less than the error tolerance and
 there are no (unpredicted) breakpoints within this step.
 Actors that use this mechanism need to implement this interface.
 At the end of each integration step, each actor that implements this
 interface will be asked whether this step is accurate by calling its
 isStepSizeAccurate() method. If this method returns false, then all
 actors that implement this interface will be asked to suggest a
 refined step size (by calling refinedStepSize()). The integration step
 will be repeated with the smallest of these suggestions.
 <p>
 If all actors that implement this interface find the integration
 step accurate, then they will be asked for a suggested next step size
 (by calling suggestedStepSize()).
 The smallest of these suggested step sizes will be used for the next
 integration step.
 <P>
 If there are no step size control actors in a model, the step size
 is controlled by the director.  Most (or possibly all) CT directors
 will leave the default step size at its initial value and only deviate
 from these steps when there is a predictable breakpoint that does not
 coincide with one of these steps.

 @author  Jie Liu, Haiyang Zheng, Edward A. Lee
 @version $Id: ContinuousStepSizeController.java 57040 2010-01-27 20:52:32Z cxh $
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (eal)
 */
public interface ContinuousStepSizeController {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
   

    /** Implementations of this method should return
     *  true if the current integration step size
     *  is sufficiently small for this actor to give accurate
     *  results.
     *  @return True if the current step is accurate.
     */
    public boolean isStepSizeAccurate();

    /** Implementations of this method should return
     *  the suggested next step size. If the current integration
     *  step is accurate, each actor will be asked for its suggestion
     *  for the next step size. If the actor that implements this interface
     *  does not care what the next step size is, it should
     *  return java.lang.Double.MAX_VALUE.
     *  @return The suggested next step size.
     *  @exception IllegalActionException If an actor suggests an illegal step size.
     */
    public double suggestedStepSize() throws IllegalActionException;

    /** Implementations of this method should return
     *  the suggested refined step size for restarting the current integration.
     *  If any actor returns false when isStepSizeAccurate() is called,
     *  then this method will be called on all actors that implement this
     *  interface. The minimum of their returned value will be the new step size.
     *  If the actor does not need a smaller step size, then
     *  this method should return the current step size.
     *  @return The suggested refined step size.
     *  @exception IllegalActionException If the step size cannot be further refined.
     */
    public double refinedStepSize() throws IllegalActionException;
}

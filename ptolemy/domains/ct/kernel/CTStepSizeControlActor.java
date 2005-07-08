/* Interface for actors that control integration step sizes.

 Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// CTStepSizeControlActor

/**
 Interface for actors that control integration step sizes for handling
 unpredictable breakpoints or controlling local truncation error when
 resolving states. Typically, actors that implement this interface are
 event detectors and dynamic actors.
 <P>
 Actors can affect the integration step size in two ways. The first one
 is by introducing predictable breakpoints. For example, when the fireAt()
 method of CTDirector is called with an argument <i>t</i>,
 the CTDirector will treat <i>t</i> as a breakpoint. Actors that only
 introduce predictable breakpoints need not implement this interface.
 <P>
 The second way of controlling step size is through checking the accuracy 
 after each integration step. We treat an integration step accurate if 
 the numerical integration error is less than the error tolerance and
 there is no (unpredictable) breakpoints within this step.
 Actors that use this mechanism need to implement this interface.
 At the end of each integration step, each CTStepSizeControlActor
 will be asked whether this step is accurate by calling its
 isOutputAccurate() or isStateAccurate() method. If either method returns
 false, that actor will then be asked to suggest a refined step size. If
 there are more than one actor finds that this step is not accurate, then the
 smallest of the suggested steps size will be used by the director to restart
 the integration step.
 <p>
 If all step size control actors find the integration step accurate,
 then they will be asked for a (predicted) next step size.
 The smallest predicted next step size will be used for the next
 integration step.
 <P>
 If there are no step size control actors in a model, the step size
 is controlled by the director.  Most (or possibly all) CT directors
 will leave the default step size at its initial value and only deviate
 from these steps when there is a predictable breakpoint that does not
 coincide with one of these steps.
 
 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 */
public interface CTStepSizeControlActor extends Actor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Implementations of this method should return
     *  true if this actor declares the current integration step 
     *  is accurate w.r.t. its output values.
     *  @return True if the current step is accurate.
     */
    public boolean isOutputAccurate();

    /** Implementations of this method should return
     *  true if this actor declares the current integration step 
     *  is accurate w.r.t. its current states.
     *  @return True if the current step is accurate.
     */
    public boolean isStateAccurate();

    /** Implementations of this method should return
     *  the predicted next step size. If the current integration
     *  step is accurate, the actor will be asked for the prediction
     *  of the next step size. If the actor that implement this interface
     *  does not know how to predict the next step size, it should
     *  return java.lang.Double.MAX_VALUE.
     *  @return The predicted next step size.
     */
    public double predictedStepSize();

    /** Implementations of this method should return
     *  the refined step size for restarting the current integration.
     *  If this actor returns false when calling the isOutputAccurate() or
     *  isStateAccurate() method, then it will be asked for a refined step size.
     *  If the actor does not want to restart the current integration
     *  step, this method should return the current step size of the
     *  director.
     *  @return The refined step size.
     */
    public double refinedStepSize();
}

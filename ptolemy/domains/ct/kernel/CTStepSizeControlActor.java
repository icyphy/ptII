/* Interface for actors that controls integration step sizes.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Green (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;

//////////////////////////////////////////////////////////////////////////
//// CTStepSizeControlActor
/** 
Interface for actors that controls integration step sizes. Typically, 
the step size of an integration algorithm is determined by the initial
step size parameter, the local truncation error at the integrators, 
the discontinuiyy of actor bahaviours, etc. All actors that want to 
effect the integration step size should implement this interface.
<P>
Three methods are defined in this interface, isThisStepAcceptable(),
refinedStepSize(), and predictedStepSize(). At the end of each integration
step, the CTStepSizeControlActors will be asked whether this step is
acceptable. If one of the actor is not satisfied, then it will be asked
for a refined step size. The integration step will be restarted with 
this refined step size. If all the step-size-control actors are satisfied,
they will be asked for the (predicted) next step size.
@author  Jie Liu
@version $ID$
*/
public interface CTStepSizeControlActor extends Actor{
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Return true if the current integration step is successful. 
     *  Actors that implement this interface will interprete "successful"
     *  themselves. For example, for integrators, "successful" could 
     *  mean that the local truncation error is small enough; for 
     *  event detectors, "successful" could mean that there is not event
     *  missed during the current integration step.
     *  @return True if the current integration step is acceptable.
     */
    public boolean isThisStepSuccessful();

    /** Return the predicted next step size. If the current integration 
     *  step is successful, the actor will be asked for the prediction 
     *  of the next step size. If the actor that implement this interface
     *  does not know how to predict the next step size, it should 
     *  return java.lang.Double.MAX_VALUE.
     *  @return The predicted next step size.
     */
    public double predictedStepSize();
    
    /** Return the refined step size for restarting current step. If the 
     *  current integration step size is not successful, the actor will
     *  be asked for a refined step size. The current integation step 
     *  will be restarted with the minimum of all returned values.
     *  If the actor does not want to restart the current integration 
     *  step, this method should return the current step size.
     *  @return The refined current step size.
     */
    public double refinedStepSize();
}

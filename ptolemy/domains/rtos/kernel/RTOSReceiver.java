/* Real Time Operating System Receiver

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.rtos.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// RTOSReceiver
/**
The receiver for the RTOS domain. This receiver extends the DEReciever.
However, instead of attaching a time stamp to the received token before
sending it to the event queue, it attaches the priority of the token.
The priority of a received token is determined in the following order:
<UL>
<li> If the container of the receiver has a parameter named <i>priority</i>
then the priority equals to the value of the parameter.
<li> If the container of the receiver does not have a <i>priority</i>
parameter, but the actor, which contains the container of this receiver,
has a <i>priority</i> parameter, then the priority equals to the value
of the <i>priority<i> of the actor.
<li> If neither the container nor the container of the container of this
reciever has the <i>priority</i> parameter, then the priority of the
token is the default priority, which is 5.0.

@author Edward A. Lee, Jie Liu
@version $Id$
*/
public class RTOSReceiver extends DEReceiver {


    ////////////////////////////////////////////////////////////////////////
    ////                     public methods                             ////

    /** Put a token into this receiver. Note that
     *  this token does not become immediately available to the get() method.
     *  Instead, the token is queued with the director, and the director
     *  must put the token back into this receiver using the _triggerEvent()
     *  protected method in order for the token to become available to
     *  the get() method.  By default, this token will be enqueued by
     *  the director with the default priority -- 5.0.
     *  However, by setting a <i>priority</i> parameter to the container
     *  of this receiver, or the container's container, 
     *  you can enqueue the event with any priority.
     *  This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @param token The token to be put.
     */
    public synchronized void put(Token token) {
        try {
            RTOSDirector dir = (RTOSDirector)getDirector();
            IOPort port = getContainer();
            Parameter priority = (Parameter)port.getAttribute("priority");
            if (priority == null) {
                priority = (Parameter)((NamedObj)port.getContainer()).getAttribute("priority");
            }
            double priorityValue = 5.0;
            if (priority != null) {
                priorityValue = ((DoubleToken)priority.getToken()).doubleValue();
            }
            dir._enqueueEvent(this, token, priorityValue);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(ex.toString());
        }
    }

    /** Override the put(token, time) method of the super class, and ignore
     *  the time argument. Only the priority of a token is of interests,
     *  and the priority is obtained from parameters of the container
     *  or container's container.
     *  This method is synchronized since the actor may not
     *  execute in the same thread as the director.
     *  @param token The token to be put.
     *  @param time Ignored.
     */
    public synchronized void put(Token token, double time)
            throws IllegalActionException{
       put(token);
   }
}

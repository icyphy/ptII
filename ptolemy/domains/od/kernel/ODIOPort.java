/* A timed input/output port used in the OD domain.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Red (davisj@eecs.berkeley.edu)

*/

package ptolemy.domains.od.kernel;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ODIOPort
/** 
A timed input/output port used in the OD domain.

FIXME: There are some critical semantic differences between actors that
       use ODIOPorts and polymorphic ports which use regular IOPorts.
       The key difference is that IOPort.send() sets the timestamp of the
       token to the rcvrTime of the receiving actor's receiver. ODIOPort.send()
       sets the timestamp of the token to the minimum rcvrTime of the
       sending actor. FIXME: Do I have this right???
       
       What I should do is set the timestamp of the token to the current
       time of the sending actor.


@author John S. Davis II
@version @(#)ODIOPort.java	1.5	11/16/98
*/
public class ODIOPort extends IOPort {

    /** 
     */
    public ODIOPort() {
        super();
    }
    
    /** 
     */
    public ODIOPort(ComponentEntity container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    /** 
     */
    public ODIOPort(ComponentEntity container, String name,
            boolean isinput, boolean isoutput) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isinput, isoutput);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast a token to all connected receivers. The time stamp 
     *  associated with this token will be the current time of the actor
     *  which contains this port.
     */
    public void broadcast(Token token) 
            throws IllegalActionException, NoRoomException {
        broadcast( token, ((ODActor)getContainer()).getCurrentTime() );
    }
            
    /** Broadcast a token to all connected receivers. The time stamp 
     *  associated with this token will be the current time of the 
     *  actor which contains this port plus the specified delay.
     */
    public void broadcast(Token token, double delay) 
            throws IllegalActionException, NoRoomException {
        if( delay < -1.0 ) {
            throw new IllegalActionException( this, "Negative delay "
                    + "values are not allowed.");
        }
        try {
            workspace().getReadAccess();
            if (!isOutput()) {
                throw new IllegalActionException(this,
                        "broadcast: Tokens can only be sent from an " +
                        "output port.");
            }
            Receiver fr[][] = getRemoteReceivers();
            if(fr == null) {
                return;
            }

            for (int j = 0; j < fr.length; j++) {
                double currentTime = ((ODActor)getContainer()).getCurrentTime();
                send(j, token, currentTime + delay);
            }
        } finally {
            workspace().doneReading();
        }
    }
   
    /** FIXME
     */
    public int getPriority() {
        return _priority;
    }
    
    /** Note that the priority of a port only impacts the priority of the
     *  port's contained receivers in a relative sense. 
     *  FIXME
     */
    public void setPriority(int priority) {
        _priority = priority;
    }
    
    /** Send a token to the receiver specified by the output port
     *  and channel number.
     */
    public void send(int channel, Token token)
            throws InvalidStateException, IllegalActionException  {
        send( channel, token, ((ODActor)getContainer()).getCurrentTime() );
    }
            
    /** Send a token to the receiver specified by the output port
     *  and channel number.
     */
    public void send(int channelindex, Token token, double delay)
            throws InvalidStateException, IllegalActionException  {
        if( delay < -1.0 ) {
            throw new IllegalActionException( this, "Negative delay "
                    + "values are not allowed.");
	}
        Receiver[][] farRec;
        try {
            workspace().getReadAccess();
            if (!isOutput()) {
                throw new IllegalActionException(this,
                        "send: Tokens can only be sent from an "+
                        "output port.");
            }
            if (channelindex >= getWidth() || channelindex < 0) {
                throw new IllegalActionException(this,
                        "send: channel index is out of range.");
            }
            // Note that the getRemoteReceivers() method doesn't throw
            // any non-runtime exception.
            farRec = getRemoteReceivers();
            if (farRec == null || farRec[channelindex] == null) {
                return;
            }
            
            System.out.println("\nAbout to call ODReceiver.put() within " +
                    "ODIOPort.send()."); 
            for (int j = 0; j < farRec[channelindex].length; j++) {
                double currentTime = ((ODActor)getContainer()).getCurrentTime(); 
                ((ODReceiver)farRec[channelindex][j]).put(
                        token, currentTime + delay);
            }
        } finally {
            workspace().doneReading();
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////
    
    // The higher the integer, the higher the priority.
    private int _priority = 0;

}





















/* An aggregation consisting of a Token, a time stamp and destination Receiver.

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

import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// Event
/**
An Event is an aggregation consisting of a Token, a time stamp and 
destination Receiver. Both the token and destination receiver are 
allowed to have null values. This is particularly useful in situations
where the specification of the destination receiver may be considered 
redundant.

@author John S. Davis II
@version @(#)Event.java	1.3	11/02/98
@see ptolemy.actors.Token
*/

public class Event {

    /** Construct an Event with a token and time stamp.
     */
    public Event(Token token, double time) {
        _token = token;
        _timeStamp = time;
    }

    /** Construct an Event with a token, a time stamp and a 
     *  destination receiver.
     */
    public Event(Token token, double time, Receiver receiver) {
        this(token, time);
        _receiver = receiver;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the destination receiver of this event.
     *  @return Receiver The destination receiver of this event.
     */
    public Receiver getReceiver() {
        return _receiver;
    }

    /** Return the time stamp of this event.
     *  @return double The time stamp of this event.
     */
    public double getTime() {
        return _timeStamp;
    }

    /** Return the token of this event.
     *  @return Token The token of this event.
     */
    public Token getToken() {
        return _token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    double _timeStamp = 0.0;
    Token _token = null;
    Receiver _receiver = null;
}

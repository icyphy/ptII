/* A Token with a time stamp and destination Receiver.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.od.kernel;

import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// Event
/**
An Event is a Token that has associated with it a time stamp and
a destination Receiver.

@author John S. Davis II
@version @(#)Event.java	1.3	11/02/98
@see ptolemy.actors.Token
*/
public class Event {

    /** 
     */
    public Event(Token token, double time) {
        _token = token;
        _timeStamp = time;
    }

    /** 
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

    Token _token;
    double _timeStamp = 0.0;
    Receiver _receiver;
}

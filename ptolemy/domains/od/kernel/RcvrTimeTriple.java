/* A data structure for storing a receiver along with its rcvrTime and priority.

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

//////////////////////////////////////////////////////////////////////////
//// RcvrTimeTriple
/**
A data structure for storing a receiver along with its rcvrTime and priority.

@author John S. Davis II 
@version @(#)RcvrTimeTriple.java	1.9	11/17/98

*/
public class RcvrTimeTriple extends NamedObj {

    /** 
     */
    public RcvrTimeTriple(TimedQueueReceiver rcvr, double rcvrTime, 
            int priority ) {
        super();
        _rcvr = rcvr;
        try {
            setTime(rcvrTime); 
            setPriority(priority);
        } catch( IllegalActionException e) {
            System.out.println("This is very bad!!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public TimedQueueReceiver getReceiver() {
        return _rcvr;
    }

    /** 
     */
    public int getPriority() {
        return _priority;
    }

    /** 
     */
    public void setPriority( int priority ) {
        _priority = priority;
    }

    /** 
     */
    public double getTime() {
        return _rcvrTime;
    }

    /**  FIXME: Do I need this method?
     */
    public void setTime( double time ) throws IllegalActionException {
        if( time < _rcvrTime ) {
            throw new IllegalActionException( getContainer(), 
                    "Rcvr times must be monotonically non-decreasing.");
        }
        _rcvrTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private TimedQueueReceiver _rcvr;
    private double _rcvrTime = 0.0;
    private int _priority;
}

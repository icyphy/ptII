/* The sort keys for sorting events in the global sorted queue.

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
*/

package ptolemy.domains.de.kernel;


//////////////////////////////////////////////////////////////////////////
//// DESortKey
/**
An event in Discrete Event domain is modeled as an instance of Token and
an instance of DESortKey. A sort key is a wrapper for an instance of 
double and an instance of integer. The sort keys are used by the global 
event queue to sort incoming events according to their time stamps.

@author Lukito Muliadi
@version $Id$
@see DECQComparator
*/
public class DESortKey {

    /** Construct a DESortKey object with the given time stamp and fine level.
     *  Time stamp is a double quantity indicating the time when the event
     *  takes place. Fine level is an integer quantity indicating the depth 
     *  in the topology useful for scheduling simultaneous events.
     * 
     * @param timeStamp the time when the event occurs.
     * @param fineLevel depth in the topology useful for scheduling.
     * 
     */	
    public DESortKey(double timeStamp, int fineLevel) {
        _timeStamp = timeStamp;
        _fineLevel = fineLevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public double timeStamp() {
        return _timeStamp;
        
    }
    public int fineLevel() {
        return _fineLevel;
    }

    public DESortKey increment(double timeStamp, int fineLevel) {
        return new DESortKey(_timeStamp+timeStamp, _fineLevel+fineLevel);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    
    // FIXME: change double to Number ?
    // _timeStamp 
    private double _timeStamp;
    // _fineLevel
    private int _fineLevel;

}



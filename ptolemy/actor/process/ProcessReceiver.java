/* Interface for receivers in process domains.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (mudit@eecs.berkeley.edu)
@AcceptedRating Yellow

*/

package ptolemy.actor.process;

import ptolemy.kernel.util.*;
import ptolemy.actor.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ProcessReceiver
/**
Interface for receivers in the process oriented domains.
It adds methods to the Receiver interface for setting flags that
indicate whether a termination of the simulation has been requested.

In process oriented domains, simulations are normally ended on the
detection of a deadlock. During a deadlock, processes or the
corresponding threads are normally waiting on a call to some
methods (for reading or writing) on a receiver.
To terminate or end the simulation, these methods should
either return or throw an exception to inform the processes that they
should terminate themselves. For this a method requestFinish() is defined.
This method would set a local flag in the receivers and wake up all the
processes waiting on some call to the receiver. On waking up these
processes would see that the termination flag set and behave accordingly.
A sample implementation is <BR>
<Code>
public synchronized void requestFinish() {
    _terminate = true;
    notifyAll();
}
</code>
<p>

@author Neil Smyth, Mudit Goel, John S. Davis II
@version $Id$

*/
public interface ProcessReceiver extends Receiver {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the local flags of this receiver. Use this method when
     *  restarting execution.
     */
    public void reset();

    /** Set a local flag requesting that the simulation be finished.
     */
    public void requestFinish();
}

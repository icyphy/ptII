/* An exception thrown when a mutation fails.

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

@ProposedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.event;

import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// TopologyChangeFailedException
/**
Thrown when a topology change fails.
Generally, if a topology change fails, then the public fields
<code>failedEvent</code> and <code>thrownException</code>
will contain the mutation event that could not be invoked,
and the exception that it threw. If the mutation was
attempted to be rolled-back to put the graph back into
a consistent state and <i>that</i> failed, then
<code>failedEventOnUndo</code> and <code>thrownExceptionOnUndo</code>
will also be set.

@author John Reekie
@version $Id$
@see Topology
*/
public class TopologyChangeFailedException extends KernelException {

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                    ////

    /** The mutation event that failed.
     */
    public TopologyEvent failedEvent;

    /** The mutation event that failed when a rollback was attempted.
     */
    public TopologyEvent failedEventOnUndo;

    /** The exception that was thrown by the failed mutation event.
     */
    public Exception thrownException;

    /** The exception that was thrown by the failed undo mutation event.
     */
    public Exception thrownExceptionOnUndo;

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a new TopologyChangeFailedException containing the event
     * which failed and the exception it threw.
     */
    public TopologyChangeFailedException (
            TopologyEvent failedEvent,
            Exception thrownException) {
        this.failedEvent = failedEvent;
        this.thrownException = thrownException;
    }
				  
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
}

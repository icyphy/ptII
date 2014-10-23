/* A ProcessReceiver is an interface for receivers in the process oriented
 domains.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.process;

import ptolemy.actor.Receiver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
//// ProcessReceiver

/**
 A ProcessReceiver is an interface for receivers in the process oriented
 domains. It adds methods to the Receiver interface for setting flags that
 indicate whether a termination of the simulation has been requested.
 In addition, methods are available to accommodate hierarchical
 heterogeneity via composite actors.
 <P>
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
 <P>
 <P>
 To accommodate hierarchical heterogeneity, an instantiation of
 ProcessReceiver must be able to determine its topological location
 with respect to boundary ports. A boundary port is an opaque port
 that is contained by a composite actor. This ability is enforced
 with the isConnectedToBoundary(), isConnectedToBoundaryOutside(),
 isConnectedToBoundaryInside(), isInsideBoundary() and isOutsideBoundary()
 methods. For convenience, the BoundaryDetector class is available to
 to simplify the implementation of these methods.
 <P>
 Blocking reads and writes are accommodated via the get(Branch)
 and put(Token, Branch) methods. In cases where a branch attempts
 to get data from or put data into a process receiver, it calls,
 respectively, these methods by passing itself as an argument.
 The process receiver then knows to register any blocks with
 the branch rather than with a director as is occurs in non-composite
 cases.
 <P>
 Note that it is not necessary for an implementation of ProcessReceiver to
 be used in the ports of an opaque composite actor. It is perfectly fine
 for a ProcessReceiver implementation to be used in the ports of an atomic
 actor. In such cases the get() and put() methods are called without the
 use of a branch object. If blocking reads or writes occur they are
 registered with the controlling director without the need for a branch
 or branch controller.


 @author Neil Smyth, Mudit Goel, John S. Davis II
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (mudit)
 @Pt.AcceptedRating Yellow (mudit)
 @see ptolemy.actor.process.BoundaryDetector

 */
public interface ProcessReceiver extends Receiver {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  <P>
     *  It is suggested that this method be implemented using
     *  the BoundaryDetector class although such an implementation
     *  is not necessary.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     *  @see BoundaryDetector
     */
    public boolean isConnectedToBoundary() throws IllegalActionException;

    /** Return true if this receiver is connected to the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the inside of a boundary port, then return true; otherwise
     *  return false.
     *  <P>
     *  It is suggested that this method be implemented using
     *  the BoundaryDetector class although such an implementation
     *  is not necessary.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     * @exception InvalidStateException
     *  @see BoundaryDetector
     */
    public boolean isConnectedToBoundaryInside() throws InvalidStateException,
    IllegalActionException;

    /** Return true if this receiver is connected to the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is connected
     *  to the outside of a boundary port, then return true; otherwise
     *  return false.
     *  <P>
     *  It is suggested that this method be implemented using
     *  the BoundaryDetector class although such an implementation
     *  is not necessary.
     *  @return True if this receiver is contained on the outside of
     *   a boundary port; return false otherwise.
     * @exception IllegalActionException
     *  @see BoundaryDetector
     */
    public boolean isConnectedToBoundaryOutside() throws IllegalActionException;

    /** Return true if this receiver is a consumer receiver. A process
     *  receiver is a consumer receiver if it is connected to a
     *  boundary port.
     *  @return True if this is a consumer receiver; return
     *   false otherwise.
     * @exception IllegalActionException
     */
    public boolean isConsumerReceiver() throws IllegalActionException;

    /** Return true if this receiver is contained on the inside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the inside of a boundary port then return true; otherwise
     *  return false.
     *  <P>
     *  It is suggested that this method be implemented using
     *  the BoundaryDetector class although such an implementation
     *  is not necessary.
     *  @return True if this receiver is contained on the inside of
     *   a boundary port; return false otherwise.
     *  @see BoundaryDetector
     */
    public boolean isInsideBoundary();

    /** Return true if this receiver is contained on the outside of a
     *  boundary port. A boundary port is an opaque port that is
     *  contained by a composite actor. If this receiver is contained
     *  on the outside of a boundary port then return true; otherwise
     *  return false.
     *  <P>
     *  It is suggested that this method be implemented using
     *  the BoundaryDetector class although such an implementation
     *  is not necessary.
     *  @return True if this receiver is contained on the outside of
     *   a boundary port; return false otherwise.
     *  @see BoundaryDetector
     */
    public boolean isOutsideBoundary();

    /** Return true if this receiver is a producer receiver. A process
     *  receiver is a producer receiver if it is contained on the
     *  inside or outside of a boundary port.
     *  @return True if this is a producer receiver; return false
     *   otherwise.
     */
    public boolean isProducerReceiver();

    /** Determine whether this receiver is read blocked.
     *  @return True if this receiver is read blocked and
     *   false otherwise.
     */
    public boolean isReadBlocked();

    /** Determine whether this receiver is write blocked.
     *  @return True if this receiver is write blocked and
     *   false otherwise.
     */
    public boolean isWriteBlocked();

    /** Set a local flag requesting that the simulation be finished.
     */
    public void requestFinish();

    /** Reset the local flags of this receiver. Use this method when
     *  restarting execution.
     */
    @Override
    public void reset();
}

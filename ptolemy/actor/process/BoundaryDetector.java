/* A BoundaryDetector determines the topological relationship of a Receiver
 with respect to boundary ports.

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

import java.util.Iterator;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;

///////////////////////////////////////////////////////////////////
//// BoundaryDetector

/**
 A BoundaryDetector determines the topological relationship of a Receiver
 with respect to boundary ports.

 @author John S. Davis II
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (davisj)
 @Pt.AcceptedRating Red (davisj)

 */
public class BoundaryDetector {
    /** Construct a BoundaryDetector with the specified containing
     *  receiver.
     *  @param receiver The receiver containing this BoundaryDetector.
     */
    public BoundaryDetector(Receiver receiver) {
        _receiver = receiver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the receiver containing this boundary detector
     *  is connected to a boundary port. A boundary port is an opaque
     *  port that is contained by a composite actor. If the containing
     *  receiver is connected to a boundary port, then return true;
     *  otherwise return false.
     *  This method is not synchronized so the caller should be.
     * @return True if the containing receiver is connected to
     *  boundary port; return false otherwise.
     * @exception IllegalActionException
     */
    public boolean isConnectedToBoundary() throws IllegalActionException {
        if (_connectedBoundaryCacheIsOn) {
            return _isConnectedBoundaryValue;
        } else {
            IOPort contPort = _receiver.getContainer();

            if (contPort == null) {
                _connectedBoundaryCacheIsOn = false;
                _isConnectedBoundaryValue = false;
                return _isConnectedBoundaryValue;
            }

            ComponentEntity contEntity = (ComponentEntity) contPort
                    .getContainer();
            IOPort connectedPort = null;
            ComponentEntity connectedEntity = null;

            Iterator ports = contPort.connectedPortList().iterator();

            while (ports.hasNext()) {
                connectedPort = (IOPort) ports.next();
                connectedEntity = (ComponentEntity) connectedPort
                        .getContainer();

                if (connectedEntity == contEntity.getContainer()
                        && connectedPort.isInput() && connectedPort.isOpaque()) {
                    // The port container of this receiver is
                    // connected to the inside of a boundary port.
                    // Now determine if this receiver's channel is
                    // connected to the boundary port.
                    Receiver[][] receivers = connectedPort.deepGetReceivers();

                    for (Receiver[] receiver : receivers) {
                        for (int j = 0; j < receiver.length; j++) {
                            if (_receiver == receiver[j]) {
                                _connectedBoundaryCacheIsOn = true;
                                _isConnectedBoundaryValue = true;
                                return true;
                            }
                        }
                    }
                } else if (connectedPort.isOpaque()
                        && !connectedEntity.isAtomic()
                        && connectedPort.isOutput()) {
                    // The port container of this receiver is
                    // connected to the outside of a boundary port.
                    // Now determine if this receiver's channel is
                    // connected to the boundary port.
                    Receiver[][] receivers = connectedPort.getRemoteReceivers();

                    for (Receiver[] receiver : receivers) {
                        for (int j = 0; j < receiver.length; j++) {
                            if (_receiver == receiver[j]) {
                                _connectedBoundaryCacheIsOn = true;
                                _isConnectedBoundaryValue = true;
                                return true;
                            }
                        }
                    }
                }
            }

            _connectedBoundaryCacheIsOn = true;
            _isConnectedBoundaryValue = false;
            return _isConnectedBoundaryValue;
        }
    }

    /** Return true if the receiver containing this boundary detector
     *  is connected to the inside of an input boundary port; return
     *  false otherwise. A boundary port is an opaque port that is
     *  contained by a composite actor.
     *  This method is not synchronized so the caller should be.
     * @return True if the containing receiver is connected to the
     *  inside of a boundary port; return false otherwise.
     * @exception IllegalActionException
     * @exception InvalidStateException
     */
    public boolean isConnectedToBoundaryInside() throws InvalidStateException,
    IllegalActionException {
        if (_connectedInsideOfBoundaryCacheIsOn) {
            return _isConnectedInsideOfBoundaryValue;
        } else {
            IOPort contPort = _receiver.getContainer();

            if (contPort == null) {
                _connectedInsideOfBoundaryCacheIsOn = false;
                _isConnectedInsideOfBoundaryValue = false;
                return _isConnectedInsideOfBoundaryValue;
            }

            ComponentEntity contEntity = (ComponentEntity) contPort
                    .getContainer();
            IOPort connectedPort = null;
            ComponentEntity connectedEntity = null;

            Iterator ports = contPort.connectedPortList().iterator();

            while (ports.hasNext()) {
                connectedPort = (IOPort) ports.next();
                connectedEntity = (ComponentEntity) connectedPort
                        .getContainer();

                if (connectedEntity == contEntity.getContainer()
                        && connectedPort.isInput() && connectedPort.isOpaque()) {
                    // The port container of this receiver is
                    // connected to the inside of a boundary port.
                    // Now determine if this receiver's channel is
                    // connected to the boundary port.
                    Receiver[][] receivers = connectedPort.deepGetReceivers();

                    for (Receiver[] receiver : receivers) {
                        for (int j = 0; j < receiver.length; j++) {
                            if (_receiver == receiver[j]) {
                                _connectedInsideOfBoundaryCacheIsOn = true;
                                _isConnectedInsideOfBoundaryValue = true;
                                return true;
                            }
                        }
                    }
                }
            }

            _connectedInsideOfBoundaryCacheIsOn = true;
            _isConnectedInsideOfBoundaryValue = false;
            return _isConnectedInsideOfBoundaryValue;
        }
    }

    /** Return true if the receiver containing this boundary detector
     *  is connected to the outside of an output boundary port; return
     *  false otherwise. A boundary port is an opaque port that is
     *  contained by a composite actor. If the receiver containing
     *  this boundary detector is contained on the inside of a boundary
     *  port, then return false.
     *  This method is not synchronized so the caller should be.
     * @return True if the containing receiver is connected to the
     *  outside of a boundary port; return false otherwise.
     * @exception IllegalActionException
     */
    public boolean isConnectedToBoundaryOutside() throws IllegalActionException {
        if (_connectedOutsideOfBoundaryCacheIsOn) {
            return _isConnectedOutsideOfBoundaryValue;
        } else {
            IOPort contPort = _receiver.getContainer();

            if (contPort == null) {
                _connectedOutsideOfBoundaryCacheIsOn = false;
                _isConnectedOutsideOfBoundaryValue = false;
                return _isConnectedOutsideOfBoundaryValue;
            }

            Iterator ports = contPort.connectedPortList().iterator();

            while (ports.hasNext()) {
                IOPort connectedPort = (IOPort) ports.next();
                ComponentEntity connectedEntity = (ComponentEntity) connectedPort
                        .getContainer();

                if (connectedPort.isOpaque() && !connectedEntity.isAtomic()
                        && connectedPort.isOutput()) {
                    // The port container of this receiver is
                    // connected to the outside of a boundary port.
                    // Now determine if this receiver's channel is
                    // connected to the boundary port.
                    Receiver[][] receivers = connectedPort.getRemoteReceivers();

                    for (Receiver[] receiver : receivers) {
                        for (int j = 0; j < receiver.length; j++) {
                            if (_receiver == receiver[j]) {
                                _connectedOutsideOfBoundaryCacheIsOn = true;
                                _isConnectedOutsideOfBoundaryValue = true;
                                return true;
                            }
                        }
                    }
                }
            }

            _connectedOutsideOfBoundaryCacheIsOn = true;
            _isConnectedOutsideOfBoundaryValue = false;
            return _isConnectedOutsideOfBoundaryValue;
        }
    }

    /** Return true if the receiver containing this boundary detector
     *  is contained on the inside of a boundary port. A boundary port
     *  is an opaque port that is contained by a composite actor. If
     *  the containing receiver is contained on the inside of a
     *  boundary port then return true; otherwise return false. This
     *  method is not synchronized so the caller should be.
     *  @return True if the containing receiver is contained on the
     *  inside of a boundary port; return false otherwise.
     */
    public boolean isInsideBoundary() {
        if (_insideBoundaryCacheIsOn) {
            return _isInsideBoundaryValue;
        } else {
            IOPort innerPort = _receiver.getContainer();

            if (innerPort == null) {
                _insideBoundaryCacheIsOn = false;
                _isInsideBoundaryValue = false;
                return _isInsideBoundaryValue;
            }

            ComponentEntity innerEntity = (ComponentEntity) innerPort
                    .getContainer();

            if (innerEntity != null && !innerEntity.isAtomic()
                    && innerPort.isOpaque()) {
                // The containing receiver is contained by the port
                // of a composite actor.
                if (innerPort.isOutput() && !innerPort.isInput()) {
                    _isInsideBoundaryValue = true;
                } else if (!innerPort.isOutput() && innerPort.isInput()) {
                    _isInsideBoundaryValue = false;
                } else if (!innerPort.isOutput() && !innerPort.isInput()) {
                    _isInsideBoundaryValue = false;
                } else {
                    // CONCERN: The following only works if the port
                    // is not both an input and output.
                    throw new IllegalArgumentException("A port that "
                            + "is both an input and output can not be "
                            + "properly dealt with by "
                            + "PNQueueReceiver.isInsideBoundary");
                }

                _insideBoundaryCacheIsOn = true;
                return _isInsideBoundaryValue;
            }

            _insideBoundaryCacheIsOn = true;
            _isInsideBoundaryValue = false;
            return _isInsideBoundaryValue;
        }
    }

    /** Return true if the receiver containing this boundary detector
     *  is contained on the outside of a boundary port. A boundary
     *  port is an opaque port that is contained by a composite actor.
     *  If the containing receiver is contained on the outside of a
     *  boundary port then return true; otherwise return false. This
     *  method is not synchronized so the caller should be.
     * @return True if the containing receiver is contained on the
     *  outside of a boundary port; return false otherwise.
     */
    public boolean isOutsideBoundary() {
        if (_outsideBoundaryCacheIsOn) {
            return _isInsideBoundaryValue;
        } else {
            IOPort innerPort = _receiver.getContainer();

            if (innerPort == null) {
                _outsideBoundaryCacheIsOn = false;
                _isOutsideBoundaryValue = false;
                return _isOutsideBoundaryValue;
            }

            ComponentEntity innerEntity = (ComponentEntity) innerPort
                    .getContainer();

            if (innerEntity != null && !innerEntity.isAtomic()
                    && innerPort.isOpaque()) {
                // The containing receiver is contained by the port
                // of a composite actor.
                if (innerPort.isOutput() && !innerPort.isInput()) {
                    _isOutsideBoundaryValue = false;
                } else if (!innerPort.isOutput() && innerPort.isInput()) {
                    _isOutsideBoundaryValue = true;
                } else if (!innerPort.isOutput() && !innerPort.isInput()) {
                    _isOutsideBoundaryValue = false;
                } else {
                    // CONCERN: The following only works if the port
                    // is not both an input and output.
                    throw new IllegalArgumentException("A port that "
                            + "is both an input and output can not be "
                            + "properly dealt with by "
                            + "PNQueueReceiver.isInsideBoundary");
                }

                _outsideBoundaryCacheIsOn = true;
                return _isOutsideBoundaryValue;
            }

            _outsideBoundaryCacheIsOn = true;
            _isOutsideBoundaryValue = false;
            return _isOutsideBoundaryValue;
        }
    }

    /** Reset the cache variables in boundary detector.
     */
    public void reset() {
        _insideBoundaryCacheIsOn = false;
        _isInsideBoundaryValue = false;
        _outsideBoundaryCacheIsOn = false;
        _isOutsideBoundaryValue = false;
        _connectedBoundaryCacheIsOn = false;
        _isConnectedBoundaryValue = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private Receiver _receiver;

    private boolean _insideBoundaryCacheIsOn = false;

    private boolean _isInsideBoundaryValue = false;

    private boolean _outsideBoundaryCacheIsOn = false;

    private boolean _isOutsideBoundaryValue = false;

    private boolean _connectedBoundaryCacheIsOn = false;

    private boolean _isConnectedBoundaryValue = false;

    private boolean _connectedInsideOfBoundaryCacheIsOn = false;

    private boolean _isConnectedInsideOfBoundaryValue = false;

    private boolean _connectedOutsideOfBoundaryCacheIsOn = false;

    private boolean _isConnectedOutsideOfBoundaryValue = false;
}

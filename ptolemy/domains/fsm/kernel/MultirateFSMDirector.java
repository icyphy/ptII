/* A MultirateFSMDirector governs the execution of a modal model.

Copyright (c) 1999-2005 The Regents of the University of California.
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

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.DFUtilities;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * @author zhouye
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MultirateFSMDirector extends FSMDirector {

    /**
     *
     */
    public MultirateFSMDirector() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param workspace
     */
    public MultirateFSMDirector(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public MultirateFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    /////////////////////////////////////////////////////////////////////
    ////                    public methods                           ////

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Return true if data are transferred from the input port of
     *  the container to the connected ports of the controller and
     *  of the current refinement actor.
     *  <p>
     *  This method will transfer all of the available tokens on each
     *  input channel. The port argument must be an opaque input port.
     *  If any channel of the input port has no data, then that
     *  channel is ignored. Any token left not consumed in the ports
     *  to which data are transferred is discarded.
     *  @param port The input port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    public boolean transferInputs(IOPort port)
            throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean transferred = false;
        // The receivers of the current refinement that receive data
        // from "port."
        Receiver[][] insideReceivers = _currentLocalReceivers(port);

        int rate = DFUtilities.getTokenConsumptionRate(port);
        for (int i = 0; i < port.getWidth(); i++) {
            // For each channel
            try {
                if (insideReceivers != null
                        && insideReceivers[i] != null) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        // Since we only transfer number of tokens declared by
                        // the port rate, we should be safe to clear the receivers.
                        // Maybe we should move this step to prefire() or postfire(),
                        // as in FSMDirector.
                        insideReceivers[i][j].clear();
                        /*
                        while (insideReceivers[i][j].hasToken()) {
                            // clear tokens.
                            // FIXME: This could be a problem for Giotto, etc.
                            // as get() method in Giotto does not remove the
                            // token from the receiver.
                            insideReceivers[i][j].get();
                        }*/
                    }
                    // Transfer number of tokens at most the declared port rate.
                    // Note: we don't throw exception if there are fewer tokens
                    // available. The prefire() method of the refinement simply
                    // return false.
                    for (int k = 0; k < rate; k++) {
                        if (port.hasToken(i)) {
                            ptolemy.data.Token t = port.get(i);
                            port.sendInside(i, t);
                        }
                    }
                    // Successfully transferred data, so return true.
                    transferred = true;
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " + ex);
            }
        }
        return transferred;
    }

    /** Transfer data from an output port of the current refinement actor
     *  to the ports it is connected to on the outside. This method differs
     *  from the base class method in that this method will transfer <i>k</i>
     *  tokens in the receivers, where <i>k</i> is the port rate if it is
     *  declared by the port. If the port rate is not declared, this method
     *  behaves like the base class method and will transfer at most one token.
     *  This behavior is required to handle the case of multi-rate actors.
     *  The port argument must be an opaque output port.
     *  @exception IllegalActionException If the port is not an opaque
     *  output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */

    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "HDFFSMDirector: transferOutputs():" +
                    "  port argument is not an opaque output port.");
        }
        boolean transferred = false;
        int rate = DFUtilities.getRate(port);
        Receiver[][] insideReceivers = port.getInsideReceivers();
        for (int i = 0; i < port.getWidth(); i ++) {
            if (insideReceivers != null && insideReceivers[i] != null) {
                for (int k = 0; k < rate; k ++) {
                    // Only transfer number of tokens declared by the port
                    // rate. Throw exception if there are not enough tokens.
                    try {
                        ptolemy.data.Token t = port.getInside(i);
                        port.send(i, t);
                    } catch (NoTokenException ex) {
                        throw new InternalErrorException(
                                "Director.transferOutputs: " +
                                "Not enough tokens for port "
                                + port.getName() + " " + ex);
                    }
                }
            }
            transferred = true;
        }
        return transferred;
    }

}

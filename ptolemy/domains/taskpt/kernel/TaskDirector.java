/* A director for controlling tasks in the taskpt domain.

 Copyright (c) 2010-2014 The Regents of the University of California.
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
package ptolemy.domains.taskpt.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.domains.taskpt.lib.Memory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TaskDirector

/** A director for controlling a task in the taskpt domain. This director
 * only accepts tokens of type <i>PtrToken</i> on its ports connected to upstream
 * actors in the higher level composite. Pure output ports are ignored by this
 * director.
 *
 * <p>When transferring data from higher level composites, the director does not pass
 * the incoming tokens directly. If the port is a pure input port,
 * tokens are read from the address specified in the <i>PtrToken</i>
 * from the shared memory controlled by the higher level director and are passed
 * to the downstream actors inside. If the port is an input/output port, the data
 * send from the actors inside are stored in the shared memory of the higher level
 * director after all the actors controlled by this director have finished execution.</p>
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 *
 * @see ptolemy.domains.taskpt.lib.Task
 */
public class TaskDirector extends TaskPtDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The director will have a default scheduler of type
     *  SequenceScheduler. In addition to invoking the base class constructor
     *  the memory is initialized.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException Thrown if the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException Thrown if the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public TaskDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Read data from the shared memory from the location specified by the
     * incoming token and broadcast the data to the actors inside.
     *
     * @param port The port to be inspected.
     * @return True, if input is transferred or it is also an output port that
     * specifies the address, where the output of the actors inside has to be
     * written.
     *
     * @exception IllegalActionException Thrown if the width of the port is not exactly one,
     * the input is not of type PtrToken, no memory is found where to write the data,
     * it is not an output port or the port is not opaque.
     */
    @Override
    protected boolean _transferInputs(IOPort port)
            throws IllegalActionException {

        if (_debugging) {
            _debug("Calling transferInputs on port: " + port.getFullName());
        }

        // Do not transfer inputs, if the port is also an output port.
        // In this case the port contains the address for writing the output.
        if (port.isOutput()) {

            // remove present tokens on outputs
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    while (port.hasNewTokenInside(i)) {
                        port.getInside(i);
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                }
            }
            return true;
        }

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an opaque"
                            + "input port.");
        }

        boolean wasTransferred = false;

        int width = port.getWidth();

        // accept only one channel coming in.
        if (width != 1) {
            throw new IllegalActionException(this, port,
                    "Only input ports with width 1 are supported by this director.");
        }

        int insideWidth = port.getWidthInside();

        try {
            if (port.hasToken(0)) {
                Token t = port.get(0);
                if (t instanceof PtrToken) {

                    PtrToken ptr = (PtrToken) t;

                    Memory mem = getThreadMemory();
                    if (mem == null) {
                        throw new IllegalActionException("no memory found.");
                    }

                    int addr = ptr.getAddress();
                    int size = ptr.getSize();
                    for (int j = 0; j < size; ++j) {
                        Token token = mem.read(addr + j);
                        for (int i = 0; i < insideWidth; ++i) {
                            port.sendInside(i, token);
                        }
                        _debug("Transferring input token " + token
                                + " from address " + (addr + j));
                    }

                } else {
                    throw new IllegalActionException(this, port,
                            "Wrong type of token. PtrToken required.");
                }
                wasTransferred = true;
            }

        } catch (NoTokenException ex) {
            // this shouldn't happen.
            throw new InternalErrorException(this, ex, null);
        }
        return wasTransferred;
    }

    /** Write the outputs from the inside to the address specified by the PtrToken
     * present outside.
     *
     * @param port The port to be inspected.
     * @return True, if an output is transferred.
     *
     * @exception IllegalActionException Thrown if the port is not an input/output port,
     * there is no PtrToken present specifying the address where to write data,
     * no memory is found where to write the data, it is not an output port or
     * the port is not opaque.
     */
    @Override
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        boolean result = false;

        // get the ptr where to write the output
        PtrToken ptr = null;
        if (port.isInput()) {
            if (port.hasToken(0)) {
                Token in = port.get(0);
                if (in instanceof PtrToken) {
                    ptr = (PtrToken) in;
                }
            }
        }
        if (ptr == null) {
            throw new IllegalActionException(this, port,
                    "Failed to transfer outputs.");
        }
        int addr = ptr.getAddress();
        int size = ptr.getSize();
        int offset = 0;

        Memory mem = getThreadMemory();
        if (mem == null) {
            throw new IllegalActionException("no memory found.");
        }

        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                while (port.hasNewTokenInside(i)) {
                    Token t = port.getInside(i);

                    if (offset < size) {
                        mem.write(addr + offset, t);
                        if (_debugging) {
                            _debug("Writing token " + t
                                    + " present on inside channel " + i
                                    + " to address " + (addr + offset));
                        }
                        offset++;
                    }
                }

                result = true;

            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private Memory getThreadMemory() {
        Memory memory = null;
        Director director = ((Actor) getContainer().getContainer())
                .getDirector();
        if (director instanceof TaskPtDirector) {
            memory = ((TaskPtDirector) director).getMemory();
        }
        return memory;
    }

}

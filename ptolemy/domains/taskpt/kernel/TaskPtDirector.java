/* A director for controlling threads and tasks in the taskpt domain.

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

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.domains.sequence.gui.VisualSequenceDirector;
import ptolemy.domains.sequence.kernel.ControlActor;
import ptolemy.domains.sequence.kernel.SequenceAttribute;
import ptolemy.domains.taskpt.lib.Memory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TaskPtDirector

/** A director for controlling tasks and threads in the taskpt domain.
 *
 * <p>A TaskPtDirector has a shared memory that can be accessed by the actors
 * that this director is controlling. The memory is local to this
 * director (and not accessible for higher level entities).</p>
 *
 * For more details on this MoC see
 * Torsten Limberg, Bastian Ristau, and Gerhard Fettweis.
 * <a href="http://dx.doi.org/10.1007/978-3-540-70550-5_9">
 * A Real-Time Programming Model for Heterogeneous MPSoCs</a>, Proc. of SAMOS VIII.
 *
 * @author Bastian Ristau
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating red (ristau)
 * @Pt.AcceptedRating red (ristau)
 */
public abstract class TaskPtDirector extends VisualSequenceDirector {

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
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public TaskPtDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the memory assigned to this director.
     *
     * @return The memory assigned to this director.
     */
    public Memory getMemory() {
        return _memory;
    }

    /** Initialize the model controlled by this director.
     *
     *  @exception IllegalActionException Thrown if the initialize() method of
     *  one of the associated actors throws it, or if there is no
     *  scheduler.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _setInitialValues();
    }

    /** Use a QueueReceiver model for this director.
     * @return A new QueueReceiver.
     */
    @Override
    public Receiver newReceiver() {
        return new QueueReceiver();
    }

    /** Return false if the system has finished executing. In addition to
     * calling the to invoking the base class method clear receivers and
     * the memory, if the system has finished executing.
     *
     *  @return True if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException Thrown if the iterations parameter
     *  does not contain a legal value.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        if (!result) {
            _clearReceivers();
            _memory.clear();
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize the shared memory for the actors and set the
     * iterations for this director to one.
     *
     *  @exception IllegalActionException Thrown if there is a problem instantiating
     *   the director's parameters.
     *  @exception NameDuplicationException Thrown if there is a problem instantiating
     *   the director's parameters.
     */
    @Override
    protected void _init() throws IllegalActionException,
            NameDuplicationException {
        super._init();
        if (_memory == null) {
            _memory = new Memory();
        }
        iterations.setExpression("1");
    }

    /** Transfer at most one data token from the given input port of
     *  the container to the ports it is connected to on the inside.
     *  This method delegates the operation to the IOPort, so that the
     *  subclass of IOPort, TypedIOPort, can override this method to
     *  perform run-time type conversion.
     *
     *  @param port The port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException Thrown, if the port is not an opaque
     *   input port.
     *  @see IOPort#transferInputs
     */
    @Override
    protected boolean _transferInputs(IOPort port)
            throws IllegalActionException {
        // FIXME: Currently also PtrTokens are directly transferred. This
        // will lead to an error as this director has its own memory. If
        // the address specified in the passed PtrToken is read or written inside,
        // it will not read/write to the intended location.
        //
        // In case of a present
        // Token of type PtrToken, transfer the Tokens this PtrToken is pointing to
        // instead. If the port is an input/output port and PtrTokens are
        // present, do nothing (because it is an address for the output tokens send
        // from inside).
        return super._transferInputs(port);
    }

    /** Transfer at most one data token from the given output port of
     *  the container to the ports it is connected to on the outside.
     *  @param port The port to transfer tokens from.
     *  @return True if the port has an inside token that was successfully
     *  transferred.  Otherwise return false (or throw an exception).
     *  @exception IllegalActionException Thrown, if the port is not an opaque
     *   output port.
     */
    @Override
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        // FIXME: Currently also PtrTokens are directly transferred. This
        // will lead to an error as this director has its own memory. If
        // the address specified in the passed PtrToken is read or written outside,
        // it will not read/write to the intended location.
        //
        // In case of a present
        // Token of type PtrToken, transfer the Tokens this PtrToken is pointing to
        // instead. If the port is an input/output port and PtrTokens are
        // present from outside, write data to the memory of the higher level director.
        return super._transferOutputs(port);
    }

    /** Set the initial Tokens present on the inputs of the actors. In
     * this case do not set any initial tokens. Remove already present tokens.
     *
     * @exception IllegalActionException Thrown if already present tokens cannot be
     * removed.
     */
    protected void _setInitialValues() throws IllegalActionException {
        // remove initial values
        // FIXME: Currently the values are set in SequencedModelDirector
        // within the initialize() method. It will be a better idea to
        // move the initialization into a method and
        // override that in this class with a method that does nothing.
        _clearReceivers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _clearReceivers() throws IllegalActionException {
        for (SequenceAttribute attribute : _sequencedList) {
            Entity actorEntity = (Entity) attribute.getContainer();

            if (!(actorEntity instanceof ControlActor)) {
                Iterator ports = ((Actor) actorEntity).inputPortList()
                        .iterator();
                while (ports.hasNext()) {
                    IOPort port = (IOPort) ports.next();
                    int width = port.getWidth();
                    for (int i = 0; i < width; ++i) {
                        if (port.hasToken(i)) {
                            port.get(i);
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Memory _memory;

}

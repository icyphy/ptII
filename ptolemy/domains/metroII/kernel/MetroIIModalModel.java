/* MetroIIModalModel extends ModalModel to support enclosing MetroFSMDirector.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

import java.util.Iterator;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroModalModel

/**
 * MetroIIModalModel extends ModalModel to support enclosing MetroIIFSMDirector.
 *
 * @author Liangpeng Guo.
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIIModalModel extends ModalModel implements GetFirable {

    /**
     * Constructs a modal model in the specified workspace with no container and
     * an empty string as a name. You can then change the name with setName().
     * If the workspace argument is null, then use the default workspace.
     *
     * @param workspace
     *            The workspace that will list the actor.
     * @exception IllegalActionException
     *                If the name has a period in it, or the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container already contains an entity with the
     *                specified name.
     */
    public MetroIIModalModel(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /**
     * Constructs a modal model with a name and a container. The container
     * argument must not be null, or a NullPointerException will be thrown.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the container is incompatible with this actor.
     * @exception NameDuplicationException
     *                If the name coincides with an actor already in the
     *                container.
     */
    public MetroIIModalModel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /**
     * Returns the iterator for the caller function of getfire().
     *
     * @return iterator the iterator for the caller function of getfire().
     */
    @Override
    public YieldAdapterIterable<Iterable<Event.Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    @Override
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                                    throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    /**
     * getfire() should be identical to getfire() of MetroCompositeActor.
     *
     * If this actor is opaque, transfer any data from the input ports of this
     * composite to the ports connected on the inside, and then invoke the
     * fire() method of its local director. The transfer is accomplished by
     * calling the transferInputs() method of the local director (the exact
     * behavior of which depends on the domain). If the actor is not opaque,
     * throw an exception. This method is read-synchronized on the workspace, so
     * the fire() method of the director need not be (assuming it is only called
     * from here). After the fire() method of the director returns, send any
     * output data created by calling the local director's transferOutputs
     * method.
     *
     */
    @Override
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        try {

            if (_debugging) {
                _debug("Calling fire()");
            }

            try {
                _workspace.getReadAccess();

                // First invoke piggybacked methods.
                if (_piggybacks != null) {
                    // Invoke the fire() method of each piggyback.
                    for (Executable piggyback : _piggybacks) {
                        piggyback.fire();
                    }
                }
                if (_derivedPiggybacks != null) {
                    // Invoke the fire() method of each piggyback.
                    for (Executable piggyback : _derivedPiggybacks) {
                        piggyback.fire();
                    }
                }

                if (!isOpaque()) {
                    throw new IllegalActionException(this,
                            "Cannot fire a non-opaque actor.");
                }

                _transferPortParameterInputs();

                Director _director = getDirector();
                // Use the local director to transfer inputs from
                // everything that is not a port parameter.
                // The director will also update the schedule in
                // the process, if necessary.
                for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
                        .hasNext() && !_stopRequested;) {
                    IOPort p = (IOPort) inputPorts.next();

                    if (!(p instanceof ParameterPort)) {
                        _director.transferInputs(p);
                    }
                }

                if (_stopRequested) {
                    return;
                }

                // _director.fire();
                if (_director instanceof GetFirable) {
                    ((GetFirable) _director).getfire(resultHandler);
                } else {
                    _director.fire();
                }

                if (_stopRequested) {
                    return;
                }

                // Use the local director to transfer outputs.
                _director.transferOutputs();
            } finally {
                _workspace.doneReading();
            }

            if (_debugging) {
                _debug("Called fire()");
            }
        } catch (IllegalActionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Updates the expression with 'MetroIIFSMDirector' instead of 'FSMDirector'
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        directorClass
        .setExpression("ptolemy.domains.metroII.kernel.MetroIIFSMDirector");
    }

}

/* A TDL Actor used in the TDL domain.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

package ptolemy.domains.tdl.kernel;

import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.data.Token;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.modal.RefinementPort;
import ptolemy.graph.Node;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * A TDL Actor used in the TDL domain.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 */
public class TDLActor extends FSMActor {

    /**
     * Construct an FSMActor in the default workspace with an empty string as
     * its name. Add the actor to the workspace directory. Increment the version
     * number of the workspace.
     */
    public TDLActor() {
        super();
    }

    /**
     * Construct an FSMActor in the specified workspace with an empty string as
     * its name. You can then change the name with setName(). If the workspace
     * argument is null, then use the default workspace. Add the actor to the
     * workspace directory. Increment the version number of the workspace.
     *
     * @param workspace
     *            The workspace that will list the actor.
     */
    public TDLActor(Workspace workspace) {
        super(workspace);
    }

    /**
     * Create an FSMActor in the specified container with the specified name.
     * The name must be unique within the container or an exception is thrown.
     * The container argument must not be null, or a NullPointerException will
     * be thrown.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of this actor within the container.
     * @exception IllegalActionException
     *                If the entity cannot be contained by the proposed
     *                container.
     * @exception NameDuplicationException
     *                If the name coincides with an entity already in the
     *                container.
     */
    public TDLActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create a new Relation. This relation should not be a Transition but a TDL
     * transition.
     */
    @Override
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();

            // Director director = getDirector();
            TDLTransition tr = new TDLTransition(this, name);
            return tr;
        } finally {
            workspace().doneWriting();
        }
    }

    /**
     * Read input values on the given port.
     *
     * @param node
     *            Node containing the TDLAction for reading the input.
     * @param p
     *            Port that should be read.
     * @param modePeriod
     *            Current mode period.
     * @return True if input was read.
     * @exception IllegalActionException
     *             If
     */
    public boolean readInput(Node node, IOPort p, long modePeriod)
            throws IllegalActionException {
        // TODO for all channels
        List<IOPort> ports = p.deepInsidePortList();
        for (IOPort port : ports) {
            if (port instanceof RefinementPort) {
                if (inputIsSafeToProcess(port)) {
                    for (int i = 0; i < p.getWidth(); i++) {
                        if (i < p.getWidthInside()) {
                            while (p.hasToken(i)) {
                                Token t = p.get(i);
                                if (_debugging) {
                                    _debug(getName(),
                                            "transferring input from "
                                                    + p.getName());
                                }
                                p.sendInside(i, t);
                                return true;
                            }
                        } else {
                            // No inside connection to transfer tokens to.
                            // In this case, consume one input token if there is one.
                            if (_debugging) {
                                _debug(getName(), "Dropping single input from "
                                        + port.getName());
                            }
                            while (p.hasToken(i)) {
                                p.get(i);
                                return true;
                            }
                        }
                    }
                    super._readInputs(port, 0);
                }
            }
        }
        return false;
    }

    /**
     * Return true if the next input on the given port is safe to process. An
     * input is safe to process if the model timestamp of the event minus the
     * minimum delay on the port is smaller than or equal to real time.
     * Otherwise, events with smaller timestamps could appear on this port which
     * has to be avoided.
     *
     * @param port
     *            Port for which the next event should be tested for being safe
     *            to process.
     * @return True if the event is safe to process.
     * @exception IllegalActionException
     *             If the minimum delay cannot be computed.
     */
    public boolean inputIsSafeToProcess(IOPort port)
            throws IllegalActionException {
        //Time modelTime = getDirector().getModelTime();

        Director executiveDirector = ((CompositeActor) getExecutiveDirector()
                .getContainer()).getExecutiveDirector();

        Director topLevelDirector = ((CompositeActor) executiveDirector
                .getContainer()).getExecutiveDirector();

        //Time physicalTime = null;
        if (topLevelDirector != null) {
            /*physicalTime =*/topLevelDirector.getModelTime();
        } else {
            /*physicalTime =*/executiveDirector.getModelTime();
        }
        //        TDLCausalityInterface causalityInterface = (TDLCausalityInterface) ((CompositeActor) ((TDLModule) getContainer())
        //                .getContainer()).getCausalityInterface();
        //        Dependency minimumDelay = causalityInterface.getMinimumDelay(port);
        //        if (minimumDelay instanceof RealDependency) {
        //            boolean isSafe = ((RealDependency) minimumDelay).value() == Double.MAX_VALUE
        //                    || modelTime.subtract(
        //                            ((RealDependency) minimumDelay).value()).compareTo(
        //                            physicalTime) <= 0;
        //            return isSafe;
        //        } else {
        //            return true;
        //        }
        return true;
    }

}

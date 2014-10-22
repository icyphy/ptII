/*
@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

import ptolemy.domains.modal.modal.Refinement;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * A TDL refinement is used to define the implementation of a TDL mode. The only
 * reason for not using the Refinement class is because the Ports used in a
 * refinement should be used are TDLRefinementPort.
 *
 * @author Patricia Derler
@version $Id$
@since Ptolemy II 8.0
 *
 */
public class TDLRefinement extends Refinement {

    /**
     * Construct a TDL refinement.
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
    public TDLRefinement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name. We override that here.
        setClassName("ptolemy.domains.tt.tdl.kernel.TDLRefinement");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Create a new port with the specified name in the container of this
     * refinement, which in turn creates a port in this refinement all other
     * refinements, and the controller. This method is write-synchronized on the
     * workspace.
     *
     * @param name
     *            The name to assign to the newly created port.
     * @return The new port.
     * @exception NameDuplicationException
     *                If the entity already has a port with the specified name.
     */
    @Override
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            if (_mirrorDisable || getContainer() == null) {
                // Have already called newPort() in the container.
                // This time, process the request.
                TDLRefinementPort port = new TDLRefinementPort(this, name);

                // NOTE: Changed RefinementPort so mirroring
                // is enabled by default. This means mirroring
                // will occur during MoML parsing, but this
                // is harmless. EAL 12/04.
                // port._mirrorDisable = false;
                // Create the appropriate links.
                TDLModule container = (TDLModule) getContainer();

                if (container != null) {
                    String relationName = name + "Relation";
                    Relation relation = container.getRelation(relationName);

                    if (relation == null) {
                        relation = container.newRelation(relationName);

                        Port containerPort = container.getPort(name);
                        containerPort.link(relation);
                    }

                    port.link(relation);
                }

                return port;
            } else {
                _mirrorDisable = true;
                ((TDLModule) getContainer()).newPort(name);
                return getPort(name);
            }
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "Refinement.newPort: Internal error: " + ex.getMessage());
        } finally {
            _mirrorDisable = false;
            _workspace.doneWriting();
        }
    }
}

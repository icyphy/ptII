/* Extension of TypedIORelation for distributed environments.

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.distributed.actor;

import java.util.HashMap;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DistributedTypedIORelation

/**
 Extension of TypedIORelation for distributed environments. It overrides the
 deepReceivers method that returns the connected receivers to this relation.
 In this case, the relation only contains (is connected to) one
 DistributedReceiver in charge of forwarding tokens to the distributed
 services that are connected.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)

 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.distributed.actor.DistributedReceiver
 */
public class DistributedTypedIORelation extends TypedIORelation {
    /** Construct a relation in the default workspace with an empty string
     *  as its name. Add the relation to the directory of the workspace.
     */
    public DistributedTypedIORelation() {
        super();
        init();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the relation to the workspace directory.
     *
     *  @param workspace The workspace that will list the relation.
     */
    public DistributedTypedIORelation(Workspace workspace) {
        super(workspace);
        init();
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the relation.
     *  @exception IllegalActionException If the container is incompatible
     *   with this relation.
     *  @exception NameDuplicationException If the name coincides with
     *   a relation already in the container.
     */
    public DistributedTypedIORelation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the receivers of all input ports linked to this
     *  relation.
     *
     *  @param except The port to exclude.
     *  @return The receivers associated with this relation.
     */
    @Override
    public Receiver[][] deepReceivers(IOPort except) {
        if (VERBOSE) {
            System.out.println("> DistributedTypedIORelation.deepReceivers()");
        }

        return intermediateReceiver;
    }

    /** Specify the servicesReceiversListMap for the internal
     *  DistributedReceiver.
     *
     *  @param servicesReceiversListMap for the internal DistributedReceiver.
     */
    public void setServicesReceiversListMap(HashMap servicesReceiversListMap) {
        if (VERBOSE) {
            System.out.println("> DistributedTypedIORelation."
                    + "setRemoteReceivers()");
        }

        ((DistributedReceiver) intermediateReceiver[0][0])
                .setServicesReceiversListMap(servicesReceiversListMap);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private   methods                 ////

    /** Creates a DistributedReceiver and assigns it to the intermediateReceiver
     *  data structure. The container of the receiver is set to connectedPort.
     */
    private void init() {
        DistributedReceiver receiver = new DistributedReceiver();

        try {
            receiver.setContainer(connectedPort);
        } catch (IllegalActionException e) {
            KernelException.stackTraceToString(e);
        }

        intermediateReceiver[0][0] = receiver;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private   variables               ////

    /** Activates debugging information. */
    private boolean VERBOSE = false;

    /** The port that the DistributedReceiver contained is connected to. */
    private TypedIOPort connectedPort = new TypedIOPort();

    /** Bidimensional array of Receiver to be returned by deepReceivers. */
    private Receiver[][] intermediateReceiver = new Receiver[1][1];
}

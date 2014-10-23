/* A TypedCompositeActor that creates multiple instances of itself
 during the preinitialize phase of model execution.

 Copyright (c) 2003-2014 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.lib.hoc;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

// Note: the (at least) single-space is needed in the javadoc below to
// protect emacs' comment text formatting from a "{@link..." appearing
// at the start of a line and disabling paragraph reformatting and
// line-wrap (zk)
///////////////////////////////////////////////////////////////////
//// MultiInstanceComposite

/**
 A {@link ptolemy.actor.TypedCompositeActor} that creates multiple
 instances of itself during the preinitialize phase of model execution.<p>

 A MultiInstanceComposite actor may be used to instantiate {@link
 #nInstances} identical processing blocks within a model. This actor
 (the "master") creates {@link #nInstances}&nbsp;-&nbsp;1 additional
 instances (clones) of itself during the {@link #preinitialize()} phase
 of model execution and destroys these additional instances during model
 {@link #wrapup()}. MultiInstanceComposite <em>must be opaque</em> (have
 a director), so that its Actor interface methods (preinitialize(), ...,
 wrapup()) are invoked during model initialization. Each instance may
 refer to its {@link #instance} [0..{@link #nInstances}-1] parameter
 which is set automatically by the master if it needs to know its
 instance number.<p>

 MultiInstanceComposite <em>input</em> ports must not be multiports (for
 now) and may be connected to multiports or regular ports.  During
 preinitialize(), the master MultiInstanceComposite determines how its
 input ports are connected, and creates additional relations in its
 container (the model it is embedded in) to connect the input ports of
 its clones (instances) to the same output port if that port is a
 multiport.  If that output port is a regular port, the clone's input
 port is linked to the already existing relation between that output
 port and the master's input port.  MultiInstanceComposite
 <em>output</em> ports must not be multiports (for now) and must be
 connected to input multiports. The master MultiInstanceComposite
 creates additional relations to connect the output ports of its clones
 to the input port. Finally, after all these connections are made, the
 master's preinitialize() calls preinitialize() of the clones.<p>

 From here on until wrapup(), nothing special happens. Type resolution
 occurs on all instances in the modified model, so does initialize() and
 the computation of schedules by directors of the master and clones.<p>

 During model wrapup(), the master MultiContextComposite deletes any
 relations created, unlinks any ports if needed, and deletes the clones
 it created. To re-synchronize vergil's model graph, an empty
 ChangeRequest is also queued with the Manager.<p>

 Actor parameters inside MultiInstanceComposite may refer to parameters
 of the container model. This presents a problem during cloning() and
 wrapup() where the container model's parameters are not in scope during
 the clone's validateSettables() (unless the MultiInstanceComposite is
 built as a moml class having its own set of parameters). This problem
 is for now solved by providing a temporary scope copy using a
 ScopeExtendingAttribute for the cloning() and wrapup() phases of the
 clones.<p>

 @author Zoltan Kemenczy, Sean Simmons, Research In Motion Limited
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (zkemenczy)
 @Pt.AcceptedRating Red (cxh)
 */
public class MultiInstanceComposite extends TypedCompositeActor {
    /** Construct a MultiInstanceComposite actor in the specified
     *  workspace with no container and an empty string as a name.
     *  @param workspace The workspace of this object.
     */
    public MultiInstanceComposite(Workspace workspace) {
        super(workspace);
        _construct();
    }

    /** Construct a MultiInstanceComposite actor with the given container
     *  and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MultiInstanceComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _construct();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The total number of instances to instantiate including instance
     * 0 (the master copy).
     */
    public Parameter nInstances;

    /** The index of this instance. */
    public Parameter instance;

    /** If true, show the clones. */
    public Parameter showClones;

    /** Clone a "master copy" of this actor into the specified workspace
     *  - note that this is not used for creating the additional
     *  instances.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MultiInstanceComposite newObject = (MultiInstanceComposite) super
                .clone(workspace);
        newObject._isMasterCopy = _isMasterCopy;
        return newObject;
    }

    /** Call the base class to perform standard preinitialize(), and, if
     * this is the master copy, proceed to create {@link #nInstances}-1
     * additional copies, and link them to the same input/output ports
     * this master is connected to.
     *
     * @exception IllegalActionException If cloning the additional
     * copies fails, or if any ports are not connected to multiports.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (!_isMasterCopy) {
            //All initialization happens in the master.
            return;
        }
        super.preinitialize();

        // Master only from here on
        if (getDirector() == null || getDirector().getContainer() != this) {
            throw new IllegalActionException(this, getFullName()
                    + "No director.");
        }
        // Get write permission on the workspace.
        try {
            _workspace.getWriteAccess();

            int N = ((IntToken) nInstances.getToken()).intValue();

            // Make sure instance is correct (ignore any user errors :)
            instance.setToken(new IntToken(0));

            TypedCompositeActor container = (TypedCompositeActor) getContainer();

            // We first remove the superfluous clones
            while (_clones.size() > N - 1) {
                MultiInstanceComposite clone = _clones.get(N - 1);
                Iterator<?> ports = clone.portList().iterator();
                while (ports.hasNext()) {
                    TypedIOPort port = (TypedIOPort) ports.next();
                    Iterator<?> relations = port.linkedRelationList()
                            .iterator();
                    while (relations.hasNext()) {
                        TypedIORelation relation = (TypedIORelation) relations
                                .next();

                        // Use a different criterion to delete relation
                        // since the old one wouldn't work any more.
                        // Added by Gang Zhou.
                        TypedIOPort mirrorPort = (TypedIOPort) getPort(port
                                .getName());

                        if (!port.isDeeplyConnected(mirrorPort)) {
                            //if (relation.linkedPortList().size() <= 2) {
                            // Delete the relation that was created in
                            // preinitialize()
                            try {
                                if (_debugging) {
                                    _debug("Deleting " + relation.getFullName());
                                }
                                relation.setContainer(null);
                            } catch (NameDuplicationException ex) {
                                throw new InternalErrorException(ex);
                            }
                        } else {
                            // Unlink the clone's port from the relation
                            if (_debugging) {
                                _debug("Unlinking " + port.getFullName()
                                        + " from " + relation.getFullName());
                            }
                            port.unlink(relation);
                        }
                    }
                }

                // Now delete the clone itself
                try {
                    if (_debugging) {
                        _debug("Deleting " + clone.getFullName());
                    }
                    clone.setContainer(null);
                } catch (NameDuplicationException ex) {
                    throw new InternalErrorException(ex);
                }
                _clones.remove(N - 1);
            }

            // Initialize the clones
            for (MultiInstanceComposite clone : _clones) {
                clone._preinitClone();
            }

            // Now instantiate the clones and connect them to the model
            for (int i = _clones.size() + 1; i < N; i++) {
                MultiInstanceComposite clone = null;

                try {
                    clone = (MultiInstanceComposite) _cloneClone(container
                            .workspace());
                } catch (CloneNotSupportedException ex) {
                    throw new IllegalActionException(this, ex, "Clone failed.");
                }

                try {
                    // See if we should draw the clone.
                    if (((BooleanToken) showClones.getToken()).booleanValue()) {
                        // Draw the clone beneath the master's location.
                        Location location = (Location) clone
                                .getAttribute("_location");
                        if (location != null) {
                            double coords[] = location.getLocation();
                            coords[1] += 60 * i;
                            location.setLocation(coords);
                        }
                    } else {
                        // Hide the clone.
                        try {
                            new Attribute(clone, "_hide");
                        } catch (KernelException e) {
                            // This should not occur.  Ignore if it does
                            // since the only downside is that the actor is
                            // rendered.
                        }
                    }

                    clone.setName(getName() + "_" + i);
                    clone.setContainer(container);
                    clone.validateSettables();

                    if (_debugging) {
                        _debug("Cloned: " + clone.getFullName());
                    }

                    // Clone all attached relations and link to same
                    // ports as the originals
                    Iterator<?> ports = portList().iterator();

                    while (ports.hasNext()) {
                        TypedIOPort port = (TypedIOPort) ports.next();
                        TypedIOPort newPort = (TypedIOPort) clone.getPort(port
                                .getName());
                        List<?> relations = port.linkedRelationList();
                        if (relations == null || relations.size() < 1) {
                            continue;
                        }
                        if (relations.size() > 1) {
                            throw new IllegalActionException(port,
                                    "Can be linked to one relation only");
                        }

                        TypedIORelation relation = (TypedIORelation) relations
                                .get(0);
                        TypedIORelation oldRelation = relation;

                        // Iterate through other ports that are connected to this port.
                        // If a connected port is a multiport, then we create
                        // a new relation to connect the clone's newPort
                        // to that multiport. Otherwise, we use the
                        // relation above to link newPort.
                        Iterator<?> otherPorts = relation.linkedPortList(port)
                                .iterator();

                        // Added by Gang Zhou. If a port is connected to
                        // multiple other ports (through a single relation),
                        // only one relation should be created.
                        boolean isRelationCreated = false;
                        boolean isPortLinked = false;

                        while (otherPorts.hasNext()) {
                            TypedIOPort otherPort = (TypedIOPort) otherPorts
                                    .next();

                            if (port.isOutput() && !otherPort.isMultiport()) {
                                throw new IllegalActionException(
                                        this,
                                        getFullName()
                                        + ".preinitialize(): "
                                        + "output port "
                                        + port.getName()
                                        + "must be connected to a multi-port");
                            }

                            // Modified by Gang Zhou so that the port can
                            // be connected to the otherPort either from inside
                            // or from outside.
                            boolean isInsideLinked = otherPort
                                    .isInsideGroupLinked(oldRelation);

                            if (port.isInput()
                                    && (!isInsideLinked && otherPort.isOutput() || isInsideLinked
                                            && otherPort.isInput())
                                            || port.isOutput()
                                            && (!isInsideLinked && otherPort.isInput() || isInsideLinked
                                                    && otherPort.isOutput())) {
                                if (otherPort.isMultiport()) {
                                    if (!isRelationCreated) {
                                        relation = new TypedIORelation(
                                                container, "r_" + getName()
                                                + "_" + i + "_"
                                                + port.getName());
                                        relation.setPersistent(false);
                                        isRelationCreated = true;

                                        if (_debugging) {
                                            _debug(port.getFullName()
                                                    + ": created relation "
                                                    + relation.getFullName());
                                        }
                                    }

                                    otherPort.link(relation);
                                }

                                if (!isPortLinked) {
                                    newPort.link(relation);
                                    isPortLinked = true;

                                    if (_debugging) {
                                        _debug(newPort.getFullName()
                                                + ": linked to "
                                                + relation.getFullName());
                                    }
                                }
                            }
                        }
                    }

                    // Let the clone know which instance it is
                    clone.instance.setToken(new IntToken(i));
                } catch (NameDuplicationException ex) {
                    throw new IllegalActionException(this, ex,
                            "couldn't clone/create");
                }

                // The clone is preinitialized only if it has just been
                // created, otherwise the current director schedule will
                // initialize it.
                clone._preinitClone();
                _clones.add(clone);
            }
        } finally {
            _workspace.doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Clone to create a copy of the master copy. */
    private Object _cloneClone(Workspace workspace)
            throws CloneNotSupportedException {
        MultiInstanceComposite newObject = (MultiInstanceComposite) super
                .clone(workspace);
        newObject._isMasterCopy = false;
        // The following is necessary in case an exception occurs
        // during execution because then wrapup might not properly complete.
        newObject.setPersistent(false);

        return newObject;
    }

    private void _construct() {
        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        setClassName("ptolemy.actor.lib.hoc.MultiInstanceComposite");

        try {
            nInstances = new Parameter(this, "nInstances", new IntToken(1));
            instance = new Parameter(this, "instance", new IntToken(0));
            showClones = new Parameter(this, "showClones", new BooleanToken(
                    false));
            showClones.setTypeEquals(BaseType.BOOLEAN);
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Problem setting up instances or nInstances parameter");
        }

        _isMasterCopy = true;
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-10\" width=\"60\" "
                + "height=\"40\" style=\"fill:red\"/>\n"
                + "<rect x=\"-18\" y=\"-8\" width=\"56\" "
                + "height=\"36\" style=\"fill:lightgrey\"/>\n"
                + "<rect x=\"-25\" y=\"-15\" width=\"60\" "
                + "height=\"40\" style=\"fill:red\"/>\n"
                + "<rect x=\"-23\" y=\"-13\" width=\"56\" "
                + "height=\"36\" style=\"fill:lightgrey\"/>\n"
                + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
                + "height=\"40\" style=\"fill:red\"/>\n"
                + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
                + "height=\"36\" style=\"fill:lightgrey\"/>\n"
                + "<rect x=\"-15\" y=\"-10\" width=\"10\" height=\"8\" "
                + "style=\"fill:white\"/>\n"
                + "<rect x=\"-15\" y=\"2\" width=\"10\" height=\"8\" "
                + "style=\"fill:white\"/>\n"
                + "<rect x=\"5\" y=\"-4\" width=\"10\" height=\"8\" "
                + "style=\"fill:white\"/>\n"
                + "<line x1=\"-5\" y1=\"-6\" x2=\"0\" y2=\"-6\"/>"
                + "<line x1=\"-5\" y1=\"6\" x2=\"0\" y2=\"6\"/>"
                + "<line x1=\"0\" y1=\"-6\" x2=\"0\" y2=\"6\"/>"
                + "<line x1=\"0\" y1=\"0\" x2=\"5\" y2=\"0\"/>" + "</svg>\n");
    }

    private void _preinitClone() throws IllegalActionException {
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private List<MultiInstanceComposite> _clones = new LinkedList<MultiInstanceComposite>();

    private boolean _isMasterCopy = false;

    //private String _scopeExtendingAttributeName = "_micScopeExtender";
}

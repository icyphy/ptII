/* Mulitple Instance Composite

 Copyright (c) 1998-2003 The Regents of the University of California and
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

@ProposedRating Red (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.hoc;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ScopeExtendingAttribute;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;

import java.util.Iterator;
import java.util.List;

// Note: the (at least) single-space is needed in the javadoc below to
// protect emacs' comment text formatting from a "{@link..." appearing
// at the start of a line and disabling paragraph reformatting and
// line-wrap (zk)

//////////////////////////////////////////////////////////////////////////
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
 */
public class MultiInstanceComposite extends TypedCompositeActor {

    /** Construct a MultiInstanceComposite actor in the specified
     *  workspace with no container and an empty string as a name.
     */
    public MultiInstanceComposite(Workspace workspace) {
        super(workspace);
        _initialize();
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
        _initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The total number of instances to instantiate including instance
     * 0 (the master copy).
     */
    public Parameter nInstances;

    /** The index of this instance. */
    public Parameter instance;

    /** Clone a "master copy" of this actor into the specified workspace
     *  - note that this is not used for creating the additional
     *  instances.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        MultiInstanceComposite newObject =
            (MultiInstanceComposite)super.clone(workspace);
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
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) _debug(getFullName()+".preinitialize()");

        if (!_isMasterCopy) return;
        // Master only from here on

        if (getDirector() == null || getDirector().getContainer() != this) {
            throw new IllegalActionException(this,
                    getFullName() + "must have a director, see javadoc.");
        }
        int N = ((IntToken)nInstances.getToken()).intValue();
        // Make sure instance is correct (ignore any user errors :)
        instance.setToken(new IntToken(0));

        TypedCompositeActor container = (TypedCompositeActor)getContainer();

        // In case actors in this MultiInstanceComposite refer to
        // parameters of the container of this composite, their cloning
        // would fail unless we extended the scope to include the
        // parameters of this composite's container...
        _addScopeExtendingAttribute();

        // Now instantiate the clones and connect them to the model
        for (int i = 1; i < N; i++) {
            MultiInstanceComposite clone = (MultiInstanceComposite)
                container.getEntity(getName() + "_" + i);
            if (clone == null) {
                try {
                    clone = (MultiInstanceComposite)
                        _cloneClone(container.workspace());
                } catch (CloneNotSupportedException ex) {
                    throw new IllegalActionException(this,
                            "couldn't _cloneClone(): " + ex.toString());
                }
                try {
                    clone.setName(getName() + "_" + i);
                    clone.setContainer(container);
                    if (_debugging)
                        _debug("Cloned: "+clone.getFullName());
                    // Clone all attached relations and link to same
                    // ports as the originals
                    Iterator ports = portList().iterator();
                    while (ports.hasNext()) {
                        TypedIOPort port = (TypedIOPort)ports.next();
                        TypedIOPort newPort = (TypedIOPort)
                            clone.getPort(port.getName());
                        List relations = port.linkedRelationList();
                        if (relations.size() > 1) {
                            throw new IllegalActionException(this,
                                    getFullName() + ".preinitialize(): port "
                                    + port.getName() + " can be linked to "
                                    + "one relation only");
                        }
                        if (relations.size() < 1) {
                            continue;
                        }
                        TypedIORelation relation = (TypedIORelation)
                            relations.get(0);

                        // Iterate through other actors ports in the
                        // container that are connected to port. If a
                        // connected port is a multiport, then we create
                        // a new relation to connect the clone's newPort
                        // to that multiport. Otherwise, we use the
                        // relation above to link newPort.
                        Iterator otherPorts =
                            relation.linkedPortList(port).iterator();
                        while (otherPorts.hasNext()) {
                            TypedIOPort otherPort = (TypedIOPort)
                                otherPorts.next();
                            if (port.isOutput() &&
                                    !otherPort.isMultiport()) {
                                throw new IllegalActionException(this,
                                    getFullName() + ".preinitialize(): "
                                    + "output port "+ port.getName()
                                    + "must be connected to a multi-port");
                            }
                            if ((port.isInput() && otherPort.isOutput()) ||
                                    (port.isOutput() && otherPort.isInput())) {
                                if (otherPort.isMultiport()) {
                                    relation = new TypedIORelation
                                        (container,"r_" + getName() + "_" +
                                                i + "_" + port.getName());
                                    otherPort.link(relation);
                                    if (_debugging)
                                        _debug(port.getFullName()+
                                                ": created relation "+
                                                relation.getFullName());
                                }
                                newPort.link(relation);
                                if (_debugging)
                                    _debug(newPort.getFullName()+
                                            ": linked to "+
                                            relation.getFullName());
                                container.connectionsChanged(otherPort);
                                clone.connectionsChanged(newPort);
                            }
                        }
                    }
                    // Let the clone know which instance it is
                    clone.instance.setToken(new IntToken(i));
                    // Remove the clone's copy of the scopeExtender attribute
                    clone._removeScopeExtendingAttribute();

                } catch (NameDuplicationException ex) {
                    throw new IllegalActionException(this, ex,
                            "couldn't clone/create");
                }
                // The clone is preinitialized only if it has just been
                // created, otherwise the current director schedule will
                // initialize it.
                clone.preinitialize();
            }
        }
    }

    /** Call the base class to perform standard wrapup() functions, and,
     * if this is the master copy, delete the clones of this actor
     * created during {@link #preinitialize()}.
     * @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_debugging) _debug(getFullName()+".wrapup()");
        if (!_isMasterCopy) return;

        // Note: wrapup() is called on any normal or abnormal run
        // termination. When embedded in a class that has already been
        // removed from its container, nContext may be eventually
        // referring to an undefined expression, so don't try getToken()
        // on it.
        TypedCompositeActor container = (TypedCompositeActor)getContainer();
        if (container == null)
            return;
        int i = 1;
        MultiInstanceComposite clone = (MultiInstanceComposite)
            container.getEntity(getName() + "_" + i);
        while (clone != null) {
            Iterator ports = clone.portList().iterator();
            while (ports.hasNext()) {
                TypedIOPort port = (TypedIOPort)ports.next();
                Iterator relations = port.linkedRelationList().iterator();
                while (relations.hasNext()) {
                    TypedIORelation  relation = (TypedIORelation)
                        relations.next();
                    if (relation.linkedPortList().size() <= 2) {
                        // Delete the relation that was created in
                        // preinitialize()
                        try {
                            if (_debugging)
                                _debug("Deleting "+relation.getFullName());
                            relation.setContainer(null);
                        } catch (NameDuplicationException ex) {};
                    } else {
                        // Unlink the clone's port from the relation
                        if (_debugging)
                            _debug("Unlinking "+port.getFullName()+" from "+
                                    relation.getFullName());
                        port.unlink(relation);
                    }
                }
            }
            // Now delete the clone itself
            try {
                if (_debugging)
                    _debug("Deleting "+clone.getFullName());
                clone._addScopeExtendingAttribute();
                clone.setContainer(null);
            } catch (NameDuplicationException ex) {};
            // Next
            clone = (MultiInstanceComposite)
                container.getEntity(getName() + "_" + ++i);
        }
        _removeScopeExtendingAttribute();
        // Queue a dummy change request that will cause a vergil model
        // graph update
        StringBuffer buffer = new StringBuffer();
        buffer.append("<group>\n");
        buffer.append("</group>");
        MoMLChangeRequest request =
            new MoMLChangeRequest(this, container, buffer.toString());
        request.setPersistent(false);
        requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    private void _addScopeExtendingAttribute()
            throws IllegalActionException {
        try {
            ScopeExtendingAttribute scopeExtender = (ScopeExtendingAttribute)
                getAttribute(_scopeExtendingAttributeName);
            if (scopeExtender != null) {
                scopeExtender.setContainer(null); // old
            }
            scopeExtender = new ScopeExtendingAttribute
                (this, _scopeExtendingAttributeName);
            Iterator scopeVariables = nInstances.getScope().
                elementList().iterator();
            while (scopeVariables.hasNext()) {
                Variable variable = (Variable)scopeVariables.next();
                new Variable(scopeExtender, variable.getName(),
                        variable.getToken());
            }
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem adding scope extending attribute");
        }
    }

    /** Clone to create a copy of the master copy. */
    private Object _cloneClone(Workspace workspace)
            throws CloneNotSupportedException {
        MultiInstanceComposite newObject =
            (MultiInstanceComposite)super.clone(workspace);
        newObject._isMasterCopy = false;
        return newObject;
    }

    private void _initialize() {
        // The base class identifies the class name as TypedCompositeActor
        // irrespective of the actual class name.  We override that here.
        getMoMLInfo().className = "ptolemy.actor.lib.hoc.MultiInstanceComposite";
        try {
            nInstances = new Parameter(this, "nInstances", new IntToken(1));
            instance = new Parameter(this, "instance", new IntToken(0));
        } catch (Exception ex) {
            throw new InternalErrorException(this, ex,
                    "Problem setting up instances or nInstances parameter");
        }
        _isMasterCopy = true;
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" width=\"60\" " +
                "height=\"40\" style=\"fill:red\"/>\n" +
                "<rect x=\"-28\" y=\"-18\" width=\"56\" " +
                "height=\"36\" style=\"fill:lightgrey\"/>\n" +
                "<rect x=\"-18\" y=\"-14\" width=\"40\" " +
                "height=\"24\" style=\"fill:white\"/>\n" +
                "<rect x=\"-20\" y=\"-12\" width=\"40\" " +
                "height=\"24\" style=\"fill:white\"/>\n" +
                "<rect x=\"-22\" y=\"-10\" width=\"40\" " +
                "height=\"24\" style=\"fill:white\"/>\n" +
                "</svg>\n");
    }

    private void _removeScopeExtendingAttribute()
            throws IllegalActionException {
        try {
            ScopeExtendingAttribute scopeExtender = (ScopeExtendingAttribute)
                getAttribute(_scopeExtendingAttributeName);
            if (scopeExtender != null)
                scopeExtender.setContainer(null);
        } catch (NameDuplicationException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem removing scope extending attribute");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _isMasterCopy = false;
    private String _scopeExtendingAttributeName = "_micScopeExtender";
}

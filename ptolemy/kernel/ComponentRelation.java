/* Relation supporting hierarchy.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.kernel;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ComponentRelation
/** 
Relation supporting hierarchy.  Specifically, methods are added for
defining a container and for performing deep traversals of a graph.
Most importantly, however, instances of this class refuse to link
to ports that are not instances of ComponenPort.  Thus, this class
ensures that ComponentPort instances are only connected to other
ComponentPort instances.

@author Edward A. Lee
@version $Id$
*/
public class ComponentRelation extends Relation {

    /** Construct a relation in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public ComponentRelation() {
         super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the relation.
     */
    public ComponentRelation(Workspace workspace) {
	super(workspace, "");
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the relation.
     *  @exception NameDuplicationException Name coincides with
     *   a relation already in the container.
     */	
    public ComponentRelation(CompositeEntity container, String name) 
             throws NameDuplicationException {
        super(container.workspace(), name);
        try {
            container._addRelation(this);
        } catch (IllegalActionException ex) {
            // Ignore -- always has a name.
        }
        // "super" call above puts this on the workspace list.
        workspace().remove(this);
        _setContainer(container);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Deeply enumerate the ports linked to this relation. Look through
     *  all transparent ports and return only non transparent ports (those
     *  with no inside links).
     *  This method is synchronized on the workspace.
     *  @return An enumeration of ComponentPorts.
     */	
    public Enumeration deepGetLinkedPorts() {
        synchronized(workspace()) {
            if (_deeplinkedportsversion == workspace().getVersion()) {
                // Cache is valid.  Use it.
                return _deeplinkedports.elements();
            }
            Enumeration nearports = getLinkedPorts();
            LinkedList result = new LinkedList();
            
            while( nearports.hasMoreElements() ) {
                ComponentPort port = (ComponentPort)nearports.nextElement();
                if (port._outside(this.getContainer())) {
                    // Transparent port above me.
                    result.appendElements(port.deepGetConnectedPorts());
                } else {
                    // Port below me, may be transparent.
                    result.appendElements(port.deepGetInsidePorts());
                }
            }
            _deeplinkedports = result;
            _deeplinkedportsversion = workspace().getVersion();
            return _deeplinkedports.elements();
        }
    }

    /** Get the container entity.
     *  This method is synchronized on the workspace.
     *  @return An instance of CompositeEntity.
     */
    public Nameable getContainer() {
        synchronized(workspace()) {
            return _container;
        }
    }

    /** Specify the container entity, adding the relation to the list 
     *  of relations in the container.  If the container already contains
     *  a relation with the same name, then throw an exception and do not make
     *  any changes.  Similarly, if the container is not in the same
     *  workspace as this relation, throw an exception.
     *  If this relation already has a container, remove it
     *  from that container first.  Otherwise, remove it from
     *  the list of objects in the workspace. If the argument is null, then
     *  unlink the ports from the relation, remove it from
     *  its container, and add it to the list of objects in the workspace.
     *  If the relation is already contained by the container, do nothing.
     *  This method is synchronized on the
     *  workspace and increments its version number.
     *  @param container The proposed container.
     *  @exception IllegalActionException
     *   This entity and container are not in the same workspace..
     *  @exception NameDuplicationException Name collides with a name already
     *   on the contents list of the container.
     */	
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (container != null && workspace() != container.workspace()) {
            throw new IllegalActionException(this, container,
                    "Cannot set container because workspaces are different.");
        }
        synchronized(workspace()) {
            CompositeEntity prevcontainer = (CompositeEntity)getContainer();
            if (prevcontainer == container) return;
            // Do this first, because it may throw an exception.
            if (container != null) {
                container._addRelation(this);
                if (prevcontainer == null) {
                    workspace().remove(this);
                }
            }
            _setContainer(container);
            if (container == null) {
                // Ignore exceptions, which mean the object is already
                // on the workspace list.
                try {
                    workspace().add(this);
                } catch (IllegalActionException ex) {}
            }

            if (prevcontainer != null) {
                prevcontainer._removeRelation(this);
            }
            if (container == null) {
                unlinkAll();
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Return a reference to the local port list.  Throw an exception if
     *  the specified port is not a ComponentPort.
     *  NOTE : This method has been made protected for the sole purpose
     *  of connecting Ports to Relations (see Port.link(Relation)). It
     *  should NOT be accessed by any other method.
     *  @param port The port to link to.
     *  @exception IllegalActionException Incompatible port
     */
    protected CrossRefList _getPortList (Port port) 
            throws IllegalActionException {
        if (!(port instanceof ComponentPort)) {
            throw new IllegalActionException(this, port,
                    "ComponentRelation can only link to a ComponentPort.");
        }
        return super._getPortList(port);
    }

    /** Set the container without any effort to maintain consistency
     *  (i.e. nothing is done to ensure that the container includes the
     *  relation in its list of relations, nor that the relation is removed from
     *  the relation list of the previous container).
     *  If the previous container is null and the
     *  new one non-null, remove the relation from the list of objects in the
     *  workspace.  If the new container is null, then add the relation to
     *  the list of objects in the workspace.
     *  This method is synchronized on the
     *  workspace, and increments its version number.
     *  It assumes the workspace of the container is the same as that
     *  of this relation, but this is not checked.  The caller should check.
     *  Use the public version to to ensure consistency.
     *  @param container
     */	
    protected void _setContainer(CompositeEntity container) {
        synchronized(workspace()) {
            _container = container;
            workspace().incrVersion();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The entity that contains this entity.
    private CompositeEntity _container;

    // A cache of the deeply linked ports, and the version used to
    // construct it.
    private LinkedList _deeplinkedports;
    private long _deeplinkedportsversion = -1;
}

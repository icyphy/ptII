/* An actor containing a PetriNet composite actor.

 Copyright (c) 2001 The Regents of the University of California.
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
@ProposedRating Red (yukewang@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/


package ptolemy.domains.petrinet.kernel;


import ptolemy.actor.TypedActor;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.Token;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PetrinetActor
/**
A Petri net Actor

<p>This is the basic unit of the hierarchical PetriNet component. It contains
ports, places, transitions, and hierarchical petrinet components.

<p>The current version restricts the ports to be uniformly connected to
Places or transitions in one direction. It is not allowed to have the
ports to connect to places and transitions in the same input or output
direction.

<p>It is also assumed that a transition is connected to places and a place
is connected to transitions eventually in the hierarchy.

<p>However, the system does not check for such restrictions.

The flat Peti Net model is defined as follows.
<br>            place ----> transition ----> place
<br>A hierarchical Petri Net model is defined as follows:
<br>            place ----> transition ----> place
<br>            place ----> (ports) ----> transition ---> (ports) ----> place

<br>where (ports) means it could be 0 or any finite number of
different directional ports, ---> means one or more marked or unmarked arcs.

<p>In this current implementation, it is restricted that all the inputs/outputs
to a port are either all places or all transitions plus possible ports.

<p>Multiple arcs are allowed for each connection. Each arc is counted
as default weight 1, unless otherwise specified.


@author  Yuke Wang
@version $Id$ */
public class PetriNetActor extends TypedCompositeActor  {

    /** Construct a PetriNetActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public PetriNetActor() {
        super();
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    /** Construct a PetriNetActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public PetriNetActor(Workspace workspace) {
	super(workspace);
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    /** Create a PetriNetActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PetriNetActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new actor.
     *  @return A new PetriNetActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.

     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PetriNetActor newObject = (PetriNetActor)super.clone(workspace);
        return newObject;
    }

    /** This method calls the firing method of the director, which
     *  fires one enabled transition of this PetriNetActor.
     *  It is assumed that the top level of the hierarchy is a
     *  PetiNetDirector.
     *  @exception IllegalActionException If
     *  fireHierarchicalPetriNet throws exception.
     *
     */
    public void fire() throws IllegalActionException {
        Nameable container = getContainer();
        _debug("PetriNetActor.fire, the actors is"
                + container.getFullName() + "  " + getFullName());
        TypedCompositeActor pn = (TypedCompositeActor) this;
        PetriNetDirector director = (PetriNetDirector) getDirector();

        director.fireHierarchicalPetriNet(pn);

    }

    /** If any of the components are Transitions and are testReady,
     *  return true, otherwise return false.
     *
     *  Find all the transitions contained in the PetriNetActor.
     *  the transitions can be deeply contained....
     *  We will check all the deeply contained transitions and
     *  see which one is ready to fire.
     *  If there is one transition ready to fire, then the container
     *  PetriNetActor is ready to fire.
     *  @exception IllegalActionException If testReadyTransition
     *  throws exception.
     *  @return true or false, a PetriNetActor is ready to fire or not.
     */
    public boolean prefire() throws IllegalActionException {

        PetriNetDirector director = (PetriNetDirector) getDirector();
        TypedCompositeActor pnActor = (TypedCompositeActor) this;

        LinkedList componentList = director.findTransitions(pnActor);
        Iterator components = componentList.iterator();
        while (components.hasNext()) {
            Nameable componentActor = (Nameable) components.next();
            if (componentActor instanceof TypedCompositeActor) {
                TypedCompositeActor transitionComponent 
                                 = (TypedCompositeActor) componentActor;
		if(director.testReadyTransition(transitionComponent)) {
                    return true;
                }
            }
        }
        return false;
    }

 
}

/*               LinkedList componentList = _findTransitions(pnActor);
                Iterator components = componentList.iterator();
                while (components.hasNext()) {

                    Nameable component1 = (Nameable) components.next();
                    if (component1 instanceof TypedCompositeActor) {
                        TypedCompositeActor transitionComponent 
                                 = (TypedCompositeActor) component1;
		        if(_testReadyTransition(transitionComponent))
                            readyComponentList.add(transitionComponent);
                    }
                }

*/

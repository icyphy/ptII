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

import ptolemy.kernel.CompositeEntity;
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

import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
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


import ptolemy.graph.Inequality;

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

THis is the basic unit of the hierarchical PetriNet component. It contains
ports, places, transitions, and hierarchical petrinet components.
The current version restricts the ports to be uniformly connected to
Places or transitions in one direction. It is not allowed to have the
ports to connect to places and transitions in the same input or output
direction.

It is also assumed that a transition is connected to places and a place
is connected to transitions eventually in the hierarchy.

However, the system does not check for such restrictions.

The flat Peti Net model is defined as follows.
            place ----> transition ----> place
A hierarchical Petri Net model is defined as follows:
            place ----> transition ----> place
            place ----> (ports) ----> transition ---> (ports) ----> place
where (ports) means it could be 0 or any finite number of different directional
ports, ---> means one or more marked or unmarked arcs.

In this current implementation, it is restricted that all the inputs/outputs
to a port are either all places or all transitions plus possible ports.

Multiple arcs are allowed for each connection. Each arc is counted as default
weight 1, unless otherwise specified.


@author  Yuke Wang
@version $Id$
*/
public class PetriNetActor extends TypedCompositeActor  {

    public PetriNetActor() {
        super();
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    public PetriNetActor(Workspace workspace) {
	super(workspace);
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    public PetriNetActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }




    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PetriNetActor newObject = (PetriNetActor)super.clone(workspace);
        return newObject;
    }


    /**   It is assumed that the top level of the hierarchy is a PetiNetDirector.
     *
     */

    public void fire() throws IllegalActionException {
        Nameable container = getContainer();
        System.out.println("inside the _PetriNetActor.fire, the actors is"
                + container.getFullName() + "  " + getFullName());
        TypedCompositeActor pn = (TypedCompositeActor) this;
        PetriNetDirector director = (PetriNetDirector) getDirector();

        director.fireHierarchicalPetriNet(pn);

    }

    /** find all the transitions contained in the PetriNetActor.
     *  the transitions can be deeply contained....
     *  we will check all the deeply contained transitions and
     *  see which one is ready to fire.
     *  If there is one transition ready to fire, then the container
     *  PetriNetActor is ready to fire.
     */

    public boolean prefire() throws IllegalActionException {
        System.out.println("inside the PetriNetActor.prefire, the actors is"
                +  getFullName() );
        TypedCompositeActor pn = (TypedCompositeActor) this;
        PetriNetDirector director = (PetriNetDirector) getDirector();

        Iterator components = deepEntityList().iterator();
        while (components.hasNext()) {
            Nameable component = (Nameable) components.next();
            if (component instanceof Transition) {
                Transition transitionComponent = (Transition) component;
                boolean t = director.testReadyTransition(transitionComponent);
                if(t)
                    return true;
            }
        }
        return false;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


}

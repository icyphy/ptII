/* An actor containing a PetriNet composite actor.

 Copyright (c) 2001-2003 The Regents of the University of California.
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


import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PetrinetActor
/**
A Petri net Actor

<p> As defined in the PetriNetDirector.java, a PetriNetActor is a
directed and weighted graph <i>G = (V, E) </i> containing three kinds
of nodes: Places <i>p_i</i>, Transitions <i>t_i</i>, and PetriNetActors
<i>PA_i</i>, i.e., <i> V = {p_i} union {t_i} union {PA_i} </i>,
where each <i>PA_i</i> itself is again defined as a PetriNetActor.
Each node of <i>V</i> is called a <i>component</i> of the
PetriNetActor <i>G</i>.

A PetriNetActor is implemented as an extension of TypedCompositeActor.
The current file contains two main methods: fire() and prefire().
More details of PetriNetActor can be found in PetriNetDirector.java.

@author  Yuke Wang
@version $Id$
@since Ptolemy II 2.0
 */
public class PetriNetActor extends TypedCompositeActor  {

    /** Construct a PetriNetActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public PetriNetActor() {
        super();
        setClassName("ptolemy.domains.petrinet.kernel.PetriNetActor");
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
        setClassName("ptolemy.domains.petrinet.kernel.PetriNetActor");
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
        setClassName("ptolemy.domains.petrinet.kernel.PetriNetActor");
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

    /** This method fires the PetriNetActor by calling the firing method
     *  of the director. It is assumed that the top level of the hierarchy
     *  is a PetriNetDirector.
     *  @exception IllegalActionException If director.fire() throws exception.
     *
     *
     */
    public void fire() throws IllegalActionException {

        System.out.println("PetriNetActor.fire, the actors is "
                + "  " + getFullName()  + " Container is "
                + getContainer().getFullName());


        PetriNetDirector director = (PetriNetDirector) getDirector();
        director.fire();

    }

    /** This method tests whether the PetriNetActor or its component
     *  contains any enabled Transitions or not. If any of the components
     *  is enabled, the method returns true, otherwise returns false.
     *
     *  @exception IllegalActionException If testReadyTransition
     *  throws exception.
     *  @return true or false, a PetriNetActor is ready to fire or not.
     */
    public boolean prefire() throws IllegalActionException {

        PetriNetDirector director = (PetriNetDirector) getDirector();
        TypedCompositeActor actor = (TypedCompositeActor) this;

        LinkedList componentList = director.findTransitions(actor);
        Iterator components = componentList.iterator();
        while (components.hasNext()) {
            Nameable componentActor = (Nameable) components.next();
            if (componentActor instanceof TypedCompositeActor) {
                TypedCompositeActor transitionComponent
                    = (TypedCompositeActor) componentActor;
                if (director.isTransitionReady(transitionComponent)) {
                    return true;
                }
            }
        }
        return false;
    }


}

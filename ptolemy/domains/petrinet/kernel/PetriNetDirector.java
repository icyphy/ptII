/* Petri net director.

 Copyright (c) 2001-2014 The Regents of the University of California.
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


 ***************************************************************

 PetriNet Director Modifications

 Copyright (c) 2010-2014 The University of Florida

 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF FLORIDA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF FLORIDA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF FLORIDA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 FLORIDA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
 */

package ptolemy.domains.petrinet.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.SharedParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// PetriNetDirector

/**
 * This implementation of a PetriNet Director is an extension of the PetriNet
 * Director created by Wang and Lee located in the experimental domain.  This
 * director supports an iteration count that can be used to stop execution,
 * even if transitions are ready to fire.  The director also supports a
 * PetriNetDisplay actor that prints token counts of all the Places (at each
 * iteration) after execution is finished.  The documentation for the original
 * Wang and Lee director is provided below and includes a definition of PetriNet.
 *
 * <p>
 * This domain implements the basic Petri Net model where Places and Transitions
 * form a bipartite graph and enabled Transitions can fire randomly. It also
 * allows Transitions to be replaced by any other Actors in Ptolemy. It
 * implements two forms of Hierarchical and compositional Petri nets. The first
 * form of hierarchical and compositional Petri net semantics comes from the
 * fact that a Transition can contain a sub-Petri-net which is invisible to the
 * director of the container of the Transition. The second form of hierarchical
 * and compositional Petri net semantics comes from a new Actor called
 * PetriNetActor which is a collection of Places and Transitions, and those
 * Places and Transitions are visible to the director of the container of the
 * PetriNetActor. The users can choose which form of models to use, and/or mix
 * them together.
 *
 * <p>
 * The basic Petri net is a directed, weighted, bipartite graph consisting of
 * two kinds of nodes, called <i>Places</i> and <i>Transitions</i>, where arcs
 * are either from a Place to a Transition or from a Transition to a Place. In
 * graphical representation, Places are drawn as circles, Transitions as bars or
 * boxes. Arcs are labeled with their <i>weights</i> (positive integers).
 * Labels of unity weight are usually omitted. Multiple arcs can exist between a
 * Place and a Transition. A <i>marking</i> assigns to each Place <i>p</i> an
 * nonnegative integer <i>k</i>, we say that <i>p</i> is <i>marked with k
 * tokens</i>.
 *
 * <p>
 * Please note here the term <i>token</i> is used differently from the general
 * Ptolemy term <i>token</i>. Here a <i>token</i> is really the integer 1.
 * <i>k tokens</i> is represented by the integer <i>k</i>.
 *
 * <p>
 * A Transition <i>t</i> is said to be <i>enabled</i> if each input Place <i>p</i>
 * of <i>t</i> is marked with at least the sum of <i>w(p, t)</i> tokens, where
 * <i>w(p, t)</i> are the weights of the arcs from <i>p</i> to <i>t</i>.
 *
 * <p>
 * An enabled Transition may or may not fire. When there are multiple enabled
 * Transitions, any of them can fire randomly. A firing of an enabled Transition
 * <i>t</i> removes <i>w(p, t)</i> tokens from each input Place <i>p</i> of
 * <i>t</i>, and adds <i>w(t, p)</i> tokens to each output Place <i>p</i> of
 * <i>t</i>, where <i>w(t, p) and w(p, t) </i> are the weights of the arcs from
 * and to the Transition respectively.
 *
 * <p>
 * A Transition without any input Place is called a <i>source Transition</i>,
 * and one without any output Place is called a <i>sink Transition</i>. Note
 * that a source Transition is unconditionally enabled, and that the firing of a
 * sink Transition consumes tokens, but does not produce any.
 *
 * <p>
 * Many variations of Petri net exist in the literature including: hierarchical
 * Petri nets, colored Petri nets, timed Petri nets, fuzzy Petri nets,
 * stochastic Petri nets, compositional Petri nets, and many of the
 * combinations.
 *
 * <p>
 * As pointed out earlier, in Ptolemy we implement the basic Petri net model
 * plus two kinds of hierarchical and compositional Petri nets. This is made
 * possible by defining the PetriNetActor. The PetriNetActor is a directed and
 * weighted graph just like the basic Petri Net model. However, a PetriNetActor
 * graph <i>G = (V, E) </i> contains three kinds of nodes: Places <i>p_i</i>,
 * Transitions <i>t_i</i>, and PetriNetActors <i>PA_i</i>, i.e., <i> V = {p_i}
 * union {t_i} union {PA_i} </i>, where each <i>PA_i</i> itself is again
 * defined as a PetriNetActor. Places are assigned with non-negative integer
 * markings. The default marking is 0. A Place is implemented by the atomic
 * actor Place. A PetriNetActor is a TypedCompositeActor contains Places,
 * Transitions and/or PetriNetActors.
 *
 * <p>
 * Each node of <i>V</i> is called a <i>component</i> of the PetriNetActor
 * <i>G</i>. Therefore the vertex set <i>V</i> of <i>G</i> is also called the
 * <i>component set</i> of the PetriNetActor <i>G</i>. The concept of
 * <i>component</i> is a key difference between the basic Petri net model and
 * the hierarchical Petri net model defined here. In Ptolemy term, a component
 * is an element in the entityList of the PetriNetActor. A PetriNetActor
 * consists of components. A component can be a Place, a Transition, and a
 * PetriNetActor component. A component can be enabled and fires if it is a
 * Transition or it is a PetriNetActor component that contains other
 * Transitions. When the firing method _fireHierarchicalPetriNetOnce() fires, it
 * chooses one component to fire.
 *
 * <p>
 * The definition of PetriNetActor defines one form of hierarchical and
 * compositional Petri nets. It defines a hierarchical Petri net since the
 * PetriNetActor <i>G</i> can contain other PetriNetActors <i>PA_i</i> as
 * components. It defines a compositional Petri net since two PetriNetActors
 * <i>PA_1 and PA_2 </i> of <i>V</i> can be connected through their ports to
 * form a bigger Petri net <i>G</i>.
 *
 * <p>
 * The second form of Hierarchical and compositional Petri net comes from the
 * fact that a Transition can be any TypedCompositeActor in Ptolemy domains,
 * which can have its own firing director. The content of the Transition is
 * invisible to the director of the container of the Transition. Therefore it is
 * possible to have a Transition contains other Places and Transitions and has a
 * PetriNetDirector as the local director for the Transition.
 *
 * <p>
 * The <i>set of Transitions</i> of the PetriNetActor <i>G</i>, or the
 * Transition set of <i>G</i>, is defined to be the union of the Transitions
 * <i>t_i</i> with the sets of Transitions of each PetriNetActor component
 * <i>PA_i</i>. A member of the Transition set of <i>G</i> is therefore
 * contained in <i>G</i> itself in which case the Transition is also a
 * component of <i>G</i>, or it is contained in some PetriNetActor component
 * <i>PA_i</i>. Therefore a Transition is a different concept from a Component
 * in PetriNetActor graph. The method findTransitions() returns the Transition
 * set of <i>G</i>.
 *
 * <p>
 * A component has ports through which connections to other components are made.
 * A Place or a Transition each has one input port and one output port, where
 * multiple connections can be made. In our model, a PetriNetActor component can
 * have multiple ports. A PetriNetActor component <i>PA_j</i> can be connected
 * to Places <i>p_i</i>, Transitions <i>t_i</i>, or other PetriNetActor
 * components <i>PA_i</i> through ports. A Place <i>p_i</i> can be connected
 * to Transitions <i>t_i</i>, or to ports of PetriNetActor components <i>PA_i</i>.
 * A Transition <i>t_i</i> can be connected to Places <i>p_i</i> or to ports
 * of PetriNetActor components <i>PA_i</i>.
 *
 * <p>
 * One restriction is that a port of a PetriNetActor component <i>PA_i</i> is
 * either connected to Places or to Transitions, but not both. Another
 * restriction is that a Place (Transition) is not allowed to be connected to
 * another Place (Transition) through ports. Though no verification of these two
 * conditions is checked, any violation of these two conditions will be reported
 * by appropriate methods during the execution.
 *
 * <p>
 * Multiple arcs can exist between any two components. The arcs can be marked by
 * an nonnegative integer as their weights. Weight 1 can be omitted. The method
 * _getWeightNumber(arc) obtains the weight assigned to the arc. If no weight is
 * assigned, the default weight is 1.
 *
 * <p>
 * For a Transition <i>t</i>, all Places <i>p</i> that can reach <i>t</i>
 * through ports or without ports are the input Places of <i>t</i>. All Places
 * that can be reached from <i>t</i> through ports or without ports are the
 * output Places of <i>t</i>. Given a Transition <i>t</i>, the methods
 * _findBackwardConnectedPlaces() and _findForwardConnectedPlaces() find the
 * input and output Places of the Transition respectively.
 *
 * <p>
 * A Transition <i>t</i> is enabled or ready in the PetriNetActor if for each
 * input Place <i>p</i> of <i>t</i>, the marking of <i>p</i> is bigger than
 * the sum of the weights of all arcs connecting <i>p</i> to <i>t</i>. The
 * method isTransitionReady(transition) tests whether the given Transition is
 * enabled/ready or not.
 *
 * <p>
 * If a Transition <i>t</i> is enabled and <i>t</i> is contained in a
 * PetriNetActor component <i>PA_i</i>, then the PetriNetActor component
 * <i>PA_i</i> is also an enabled component. On the other hand, for any
 * PetriNetActor component <i>PA_i</i>, if it contains an enabled Transition,
 * then the PetriNetActor component <i>PA_i</i> is enabled. The method
 * PetriNetActor.prefire() tests whether there is any enabled Transitions
 * contained in the PetriNetActor component.
 *
 * <p>
 * An enabled Transition may or may not fire. For the given PetriNetActor <i>G</i>,
 * all its enabled components including Transitions <i>t_i</i> and
 * PetriNetActor components <i>PA_i</i> are collected together in a list
 * returned by _readyComponentList(). Suppose the list has <i>n</i> components
 * of <i>t_i</i> and <i>PA_i</i>, each component has <i>1/n</i> probability
 * to be chosen to fire. The method _fireHierarchicalPetriNetOnce() chooses one
 * component from the list to fire.
 *
 * <p>
 * If an enabled Transition is chosen to fire, the method fireTransition() is
 * called to fire the Transition and update the input and output Places of the
 * Transition. The firing of the Transition is determined by its own director,
 * if there is one, otherwise no action is needed. For each input Place of the
 * Transition, its marking has to be decreased by the weight of each arc
 * connecting the Place to the Transition. For each output Place, the marking
 * will be increased by the weight of each arc connecting the Transition to the
 * Place.
 *
 * <p>
 * If a PetriNetActor component <i>PA_i</i> is chosen to fire, the director
 * then recursively repeats the same procedure for <i>PA_i</i> as for the top
 * level PetriNetActor <i>G</i>.
 *
 * <p>
 * Finally, the firing of the hierarchical Petri net is continued until there is
 * no more Transitions and components to fire, or it goes to infinite loop.
 * Currently no action is taken for infinite loops.
 *
 * <p>
 * Other form of firing sequence can be defined as well. We could randomly fire
 * all the deeply contained Transitions. We could randomly fire the components
 * in each hierarchy.
 *
 * [1] T. Murata, "Petri nets: properties, analysis and applications",
 * Proceedings of the IEEE, VOl. 77, NO. 4, April 1989, pp. 541-579. [2] J. L.
 * Peterson, "Petri Net Theory and the modeling of systems", Prentice Hall,
 * 1981.
 *
 * @author Yuke Wang, Edward A. Lee and modified by Zach Ezzell
 * @version $Id$
 * @since Ptolemy II 8.1
 *
 */

public class PetriNetDirector extends Director {

    /**
     * Construct a new Petri net director.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of the director.
     * @exception IllegalActionException
     *                If the name has a period in it, or the director is not
     *                compatible with the specified container.
     * @exception NameDuplicationException
     *                If the container already contains an entity with the
     *                specified name.
     */

    public PetriNetDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * This parameter represents the maximum number of times the PetriNet
     * should fire.
     */
    Parameter iterations;

    /** If true, this parameter specifies that the random number
     *  generator should be reset on each run of the model (in
     *  the initialize() method). It is a boolean that defaults
     *  to false. This is a shared parameter, meaning that changing
     *  it somewhere in the model causes it to be changed everywhere
     *  in the model.
     */
    public SharedParameter resetOnEachRun;

    /** The seed that controls the random number generator that
     *  determines which component is fired.
     *
     *  <p>This is a shared parameter, meaning that all instances of
     *  PetriNetDirector or derived classes in the same model share the
     *  same value.  This parameter is used for testing so that a
     *  model has predictable results and can be compared against
     *  known good results.</p>
     *
     *  <p>A seed of zero is interpreted to mean that no seed is
     *  specified, which means that each execution of the model could
     *  result in distinct data. For the value 0, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that with
     *  extremely high probability, two distinct directors will have
     *  distinct seeds.  However, current time may not have enough
     *  resolution to ensure that two subsequent executions of the
     *  same model have distinct seeds. For a value other than zero,
     *  the seed is set to that value plus the hashCode() of the full
     *  name of the director. This means that with high probability,
     *  two distinct director will have distinct, but repeatable
     *  seeds.</p>
     *
     *  <p>This parameter contains a LongToken, initially with value
     *  0.</p>
     */
    public SharedParameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>seed</i>
     *  then create the base random number generator.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Copied from actor.lib.RandomSource
        if (attribute == seed) {
            long seedValue = ((LongToken) seed.getToken()).longValue();

            if (seedValue != _generatorSeed) {
                _needNewGenerator = true;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new director
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        // Based on ptolemy.actor.lib.RandomSource.
        // We need a clone(Workspace) method so that Actor Oriented Classes
        // work.
        PetriNetDirector newObject = (PetriNetDirector) super.clone(workspace);

        newObject.iterations = (Parameter) newObject.getAttribute("iterations");

        // It is too soon to generate the new generator because
        // all clones will have the same actor name, which results
        // in the same seed.
        newObject._needNewGenerator = true;

        return newObject;
    }

    /**
     * Find all Transitions of the given container, i.e., the
     * Transition set of the container, which is supposed to be a
     * PetriNetActor.  A Transition can be contained in the top level
     * PetriNetActor, or its PetriNetActor components. This method
     * searches for Transitions recursively for each PetriNetActor
     * component.
     *
     * @param container
     *            The container where the Transitions are contained.
     * @return the list of Transitions contained by the container.
     */
    public LinkedList findTransitions(TypedCompositeActor container) {
        Iterator components = container.entityList().iterator();
        LinkedList temporaryList = new LinkedList();
        while (components.hasNext()) {
            Nameable component = (Nameable) components.next();
            if (component instanceof Place) {
                // Don't do anything for Place
            } else if (component instanceof PetriNetActor) {
                TypedCompositeActor componentActor = (TypedCompositeActor) component;
                LinkedList newComponentList = findTransitions(componentActor);
                temporaryList.addAll(newComponentList);
            } else {
                temporaryList.add(component);
            }
        }
        return temporaryList;
    }

    /**
     * Fire enabled components of the PetriNetActor by calling the
     * method _fireHierarchicalPetriNetOnce(), one at a time until
     * there is no more enabled components to fire. The enabled
     * component can be an enabled Transition or an enabled
     * PetriNetActor component. It is the job of the method
     * _fireHierarchicalPetriNetOnce() to find all enabled components
     * if there is any, to choose which enabled component to fire, and
     * to update markings of related Places when a component fires.
     *
     * <p>A description of the firing is sent to any actors that implement
     * the {@link PetriNetDisplayer} interface.  If this director has a debug
     * listener, then the description is also sent to those listeners.</p>
     *
     * @exception IllegalActionException If the method
     * _fireHierarchicalPetriNetOnce() throws exceptions, which can
     * happen if the method isTransitionReady() or fireTransition()
     * throws exceptions.
     */
    @Override
    public void fire() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof TypedCompositeActor) {
            StringBuffer description = new StringBuffer();
            int time = 0;
            int placeCount = 0;
            Iterator actors;
            String[] names;
            TypedCompositeActor petriContainer = (TypedCompositeActor) container;
            actors = petriContainer.entityList().iterator();
            LinkedList placeList = new LinkedList();
            while (actors.hasNext()) {
                Nameable component = (Nameable) actors.next();
                if (component instanceof Place) {
                    placeCount++;
                    placeList.add(component);
                }
            }
            if (placeCount > 0) {
                Collections.sort(placeList, (Place) placeList.get(0));
            }
            names = new String[placeCount];
            int i = 0;
            Iterator cPlace = placeList.iterator();
            while (cPlace.hasNext()) {
                Place p = (Place) cPlace.next();
                names[i] = p.getName();
                if (names[i].length() == 1) {
                    names[i] += "  ";
                } else if (names[i].length() == 2) {
                    names[i] += " ";
                }
                i++;
            }

            for (String name : names) {
                description.append(name + " ");
            }
            description.append("\n");
            cPlace = placeList.iterator();
            i = 0;
            while (cPlace.hasNext()) {
                Place p = (Place) cPlace.next();
                _addMarkingToDescription(description, p.getMarking(), i, names);
                i++;
            }
            description.append("\n");
            boolean test = _fireHierarchicalPetriNetOnce(petriContainer);
            time++;
            int iter = ((IntToken) iterations.getToken()).intValue();
            if (iter >= 0) {
                while (test) {
                    if (iter == 0 || time < iter) {
                        _debug("" + iter + " " + time);
                        cPlace = placeList.iterator();
                        i = 0;
                        while (cPlace.hasNext()) {
                            Place p = (Place) cPlace.next();

                            _addMarkingToDescription(description,
                                    p.getMarking(), i, names);
                            i++;
                        }
                        description.append("\n");
                        test = _fireHierarchicalPetriNetOnce(petriContainer);
                        time++;
                    } else {
                        test = false;
                    }
                }
            }

            if (_debugging) {
                _debug("fire(): " + description.toString());
            }

            // Notify PetriNetDisplayer actors
            actors = petriContainer.entityList().iterator();
            while (actors.hasNext()) {
                Nameable component = (Nameable) actors.next();
                if (component instanceof PetriNetDisplayer) {
                    ((PetriNetDisplayer) component).setText(description
                            .toString());
                    ((PetriNetDisplayer) component).openDisplay();

                }
            }
        }
    }

    /**
     * Fire an enabled Transition.

     * <p>The transition argument to this method must be an enabled
     * Transition. If the given Transition is Opaque, then it fires
     * the Transition first, otherwise no action is taken for the
     * Transition; the method then updates the markings of the input
     * Places and output Places of the Transition. The update of the
     * marking is done one relation at a time. The input Places and
     * output Places are found by the methods
     * _findForwardConnectedPlaces() and
     * _findBackwardConnectedPlaces() respectively.</p>
     *
     * @param transition
     *            The transition to be fired.
     * @exception IllegalActionException
     *                If the method _getWeightNumber() throws an exception.
     */
    public void fireTransition(TypedCompositeActor transition)
            throws IllegalActionException {

        if (_debugging) {
            _debug(transition.getFullName() + " is firing");
        }
        if (transition.isOpaque()) {
            transition.fire();
        }
        LinkedList newRelationList = new LinkedList();
        Iterator outputPorts = transition.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outPort = (IOPort) outputPorts.next();
            newRelationList.addAll(outPort.linkedRelationList());
        }
        LinkedList temporaryDestinationPortList = new LinkedList();
        while (newRelationList.size() > 0) {
            IORelation weights = (IORelation) newRelationList.getFirst();
            if (weights != null) {
                Iterator weightPorts = weights.linkedDestinationPortList()
                        .iterator();
                while (weightPorts.hasNext()) {
                    IOPort weightPort = (IOPort) weightPorts.next();
                    if (!temporaryDestinationPortList.contains(weightPort)) {
                        temporaryDestinationPortList.add(weightPort);
                        Nameable weightPlace = weightPort.getContainer();
                        if (weightPlace instanceof PetriNetActor) {
                            if (weightPort.isOutput()) {
                                newRelationList.addAll(weightPort
                                        .linkedRelationList());
                            } else if (weightPort.isInput()) {
                                newRelationList.addAll(weightPort
                                        .insideRelationList());
                            }
                        } else if (weightPlace instanceof Place) {
                            // Don't do anything for Place
                        }
                    }
                }
                int weightNumber = _getWeightNumber(weights);
                LinkedList forwardConnectedPlaces = _findForwardConnectedPlaces(weights);
                Iterator forwardConnectedPlace = forwardConnectedPlaces
                        .iterator();
                int itemCount = 0;
                while (forwardConnectedPlace.hasNext()) {
                    Place forwardPlace = (Place) forwardConnectedPlace.next();
                    itemCount++;
                    int oldToken = forwardPlace.getMarking();
                    forwardPlace.increaseMarking(weightNumber);
                    if (_debugging) {
                        _debug("              the " + itemCount + " place is "
                                + forwardPlace.getFullName() + " old  "
                                + oldToken + " new "
                                + forwardPlace.getMarking());
                    }
                }
            }
            newRelationList.remove(weights);
        }
        LinkedList backRelationList = new LinkedList();
        Iterator inputPorts = transition.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inPort = (IOPort) inputPorts.next();
            backRelationList.addAll(inPort.linkedRelationList());
        }
        LinkedList temporarySourcePortList = new LinkedList();
        while (backRelationList.size() > 0) {
            IORelation weights = (IORelation) backRelationList.getFirst();
            if (weights != null) {
                Iterator weightPorts = weights.linkedSourcePortList()
                        .iterator();
                while (weightPorts.hasNext()) {
                    IOPort weightPort = (IOPort) weightPorts.next();
                    if (!temporarySourcePortList.contains(weightPort)) {
                        temporarySourcePortList.add(weightPort);
                        Nameable weightPlace = weightPort.getContainer();

                        if (weightPlace instanceof PetriNetActor) {
                            if (weightPort.isOutput()) {
                                backRelationList.addAll(weightPort
                                        .insideRelationList());
                            } else if (weightPort.isInput()) {
                                backRelationList.addAll(weightPort
                                        .linkedRelationList());
                            }
                        }
                    }
                }
                int weightNumber = _getWeightNumber(weights);
                LinkedList updatePlace = _findBackwardConnectedPlaces(weights);
                Iterator pointer = updatePlace.iterator();
                int backPlaceCount = 0;
                while (pointer.hasNext()) {
                    Place item = (Place) pointer.next();
                    backPlaceCount++;
                    int oldMarking = item.getMarking();
                    item.decreaseMarking(weightNumber);
                    if (_debugging) {
                        _debug("                        the " + backPlaceCount
                                + " place  is " + item.getFullName() + " old "
                                + oldMarking + " new  " + item.getMarking());
                    }
                    if (item.getMarking() < 0) {
                        break;
                    }
                }
            }
            backRelationList.remove(weights);
        }
    }

    /**
     * Test whether a given Transition is enabled or not.

     * <p>A Transition is enabled if for each of the input Places, the
     * marking of the Place is bigger than the sum of weights of edges
     * connecting the Place to the Transition. The Transition itself
     * is any TypedCompositeActor. The Transition can be a component
     * of a PetriNetActor, or it is contained in some PetriNetActor
     * component.</p>
     *
     * <p>This is one of the key methods for hierarchical Petri Nets. It
     * is equivalent to the prefire() method for a Transition. The
     * method first finds all the input Places of the Transition by
     * calling the method _findBackwardConnectedPlaces(), and sets the
     * temporary marking of the Places equal to the real marking; then
     * it enumerates all the arcs connecting Places to the Transition
     * and decreases the temporaryMarking of the Places reachable from
     * the arc. If after all arcs have been enumerated and the
     * temporaryMarking of all input Places are greater than 0, then
     * the Transition is ready to fire, otherwise it is not ready to
     * fire. The reason that we use a temporaryMarking here is to keep
     * the initialMarking of the places unchanged when we test a
     * Transition is ready or not.</p>
     *
     * @param transition
     *            Transition to be tested to be enabled or not.
     * @return true or false The tested transition is ready to fire or not.
     * @exception IllegalActionException
     *                If the method "_getWeightNumber" throws exceptions, which
     *                can happen if the arcs are not assigned to some other
     *                value other than integers.
     */
    public boolean isTransitionReady(TypedCompositeActor transition)
            throws IllegalActionException {
        boolean readyToFire = true;
        LinkedList placeList = _findBackwardConnectedPlaces(transition);
        Iterator placeLists = placeList.iterator();
        while (placeLists.hasNext()) {
            Place place = (Place) placeLists.next();
            place.setTemporaryMarking(place.getMarking());
        }
        LinkedList newRelationList = new LinkedList();
        Iterator inputPorts = transition.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inPort = (IOPort) inputPorts.next();
            newRelationList.addAll(inPort.linkedRelationList());
        }
        LinkedList temporarySourcePortList = new LinkedList();
        while (newRelationList.size() > 0) {
            IORelation weights = (IORelation) newRelationList.getFirst();
            if (weights != null) {
                Iterator weightPorts = weights.linkedSourcePortList()
                        .iterator();
                while (weightPorts.hasNext()) {
                    IOPort weightPort = (IOPort) weightPorts.next();

                    if (!temporarySourcePortList.contains(weightPort)) {
                        temporarySourcePortList.add(weightPort);

                        Nameable weightPlace = weightPort.getContainer();

                        if (weightPlace instanceof PetriNetActor) {
                            if (weightPort.isOutput()) {
                                newRelationList.addAll(weightPort
                                        .insideRelationList());
                            } else if (weightPort.isInput()) {
                                newRelationList.addAll(weightPort
                                        .linkedRelationList());
                            }
                        }
                    }
                }
                int weightNumber = _getWeightNumber(weights);
                LinkedList updatePlace = _findBackwardConnectedPlaces(weights);
                Iterator places = updatePlace.iterator();
                while (places.hasNext()) {
                    Place place = (Place) places.next();
                    place.decreaseTemporaryMarking(weightNumber);
                    if (place.getTemporaryMarking() < 0) {
                        return false;
                    }
                }
            }

            newRelationList.remove(weights);
        }

        return readyToFire;
    }

    /**This method preinitializes the actors associated with this
     * director.
     *
     * @exception IllegalActionException
     *              Exception is thrown if superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        if (_random == null
                || ((BooleanToken) resetOnEachRun.getToken()).booleanValue()) {
            _createGenerator();
        }

        Nameable container = getContainer();
        if (container instanceof TypedCompositeActor) {
            Iterator rList = ((TypedCompositeActor) container).relationList()
                    .iterator();
            while (rList.hasNext()) {
                ComponentRelation r = (ComponentRelation) rList.next();
                Attribute tAttribute = r.getAttribute("width");
                if (tAttribute == null) {
                    return;
                } else if (tAttribute instanceof Variable) {
                    Variable widthAttribute = (Variable) tAttribute;
                    Token widthToken = widthAttribute.getToken();
                    if (widthToken instanceof ScalarToken) {
                        ScalarToken widthScalarToken = (ScalarToken) widthToken;
                        if (widthScalarToken.intValue() < 0) {
                            IntToken newToken = new IntToken(1);
                            widthAttribute.setToken(newToken);
                        }
                    }
                }
            }
        }
        super.preinitialize();
    }

    /**
     * Return false, indicating that the director does not wish to be scheduled
     * for another iteration. FIXME: This is provisional since there is
     * currently no way to stop the execution of a Petri net model, so we just
     * run once.
     *
     * @return False.
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * This method is a helper method that adds a marking count to the
     * output StringBuffer.
     *
     * @param original
     *         The StringBuffer to add the marking count to.
     * @param marking
     *          The marking count to add to the String.
     * @param index
     *          The index into the placeNames array of the place corresponding
     *          to the marker count;
     * @param placeNames
     *          The array of place names.
     */
    private void _addMarkingToDescription(StringBuffer original, int marking,
            int index, String[] placeNames) {
        int mWidth = 1;
        if (marking > 9 && marking < 100) {
            mWidth = 2;
        } else if (marking > 99) {
            mWidth = 3;
        }
        original.append(marking);
        int nameWidth = placeNames[index].length();
        for (int i = 0; i < nameWidth - mWidth + 1; i++) {
            original.append(" ");
        }
    }

    //    /**
    //     * This method is a helper method that adds white space to a string
    //     *
    //     * @param amount
    //     *            The amount of white space to add to the output string.
    //     *
    //     * @return Return the new output string with white space added.
    //     */
    //    private String _createWhiteSpace(int amount) {
    //        String output = "";
    //        for (int i = 0; i < amount; i++) {
    //            output += " ";
    //        }
    //        return output;
    //    }
    //
    /** Create the random number generator using current parameter values.
     *  @exception IllegalActionException If thrown while reading the
     *  seed Token.
     */
    private void _createGenerator() throws IllegalActionException {
        // From actor.lib.RandomSource
        long seedValue = ((LongToken) seed.getToken()).longValue();
        _generatorSeed = seedValue;

        if (seedValue == 0L) {
            seedValue = System.currentTimeMillis() + hashCode();
        } else {
            seedValue = seedValue + getFullName().hashCode();
        }
        _random = new Random(seedValue);
        _needNewGenerator = false;
    }

    /**
     * This method gets the weight assigned to the given relation. The current
     * hierarchical Petri Net allows multiple arcs connecting Places,
     * transitions, and ports. Each arc can have an attribute "weight", or
     * without such attribute. The default is assumed to be weight 1. This
     * default weight can be changed into other weight if necessary.
     *
     * @param weights
     *            An arc in the PetriNetActor.
     * @return The weight associated with the relation, default is 1.
     * @exception IllegalActionException
     *                If attribute.getToken or token.intValue throws an
     *                exception, which may happen if the weight assigned to the
     *                relation is not an integer.
     */
    private int _getWeightNumber(IORelation weights)
            throws IllegalActionException {
        Attribute temporaryAttribute = weights.getAttribute("Weight");
        if (temporaryAttribute == null) {
            return 1;
        } else if (temporaryAttribute instanceof Variable) {
            Variable weightAttribute = (Variable) temporaryAttribute;
            Token weightToken = weightAttribute.getToken();
            if (weightToken instanceof ScalarToken) {
                ScalarToken weightScalarToken = (ScalarToken) weightToken;
                return weightScalarToken.intValue();
            }
            return 0;
        } else {
            return 0;
        }
    }

    /**
     * For each relation, this method finds all the affected Places in the
     * backward direction, i.e., the input Places. Those Places determine
     * whether a Transition is ready to fire or not. If ready, the firing
     * Transition has to update the tokens in all these Places. The algorithm
     * used in this method is the breadth first search of the graph.
     *
     * @param weight
     *            The arc connecting transition input to ports or Places.
     * @return List The Places control the transition at the end of the weight
     *         is ready to fire or not.
     */
    private LinkedList _findBackwardConnectedPlaces(IORelation weight) {
        LinkedList newRelationList = new LinkedList();
        newRelationList.add(weight);
        LinkedList temporarySourcePortList = new LinkedList();
        LinkedList temporaryPlaceList = new LinkedList();
        while (newRelationList.size() > 0) {
            IORelation weights = (IORelation) newRelationList.getFirst();
            Iterator weightPorts = weights.linkedSourcePortList().iterator();
            while (weightPorts.hasNext()) {
                IOPort weightPort = (IOPort) weightPorts.next();
                if (!temporarySourcePortList.contains(weightPort)) {
                    temporarySourcePortList.add(weightPort);
                    Nameable weightPlace = weightPort.getContainer();
                    if (weightPlace instanceof PetriNetActor) {
                        if (weightPort.isOutput()) {
                            newRelationList.addAll(weightPort
                                    .insideRelationList());
                        } else if (weightPort.isInput()) {
                            newRelationList.addAll(weightPort
                                    .linkedRelationList());
                        }
                    } else if (weightPlace instanceof Place) {
                        temporaryPlaceList.add(weightPlace);
                    } else {
                        _debug("-------found no place/PetriNetActor  "
                                + weightPort.getFullName());
                    }
                }
            }
            newRelationList.remove(weights);
        }
        return temporaryPlaceList;
    }

    /**
     * This method finds all the Places that determines whether a transition is
     * enabled or not. It starts to trace each input relation of the transition
     * and finds each of the Place connected to the relation. This allows
     * duplicated copies of the same Place.It unites all the connected Places to
     * each relation.
     *
     * @param transition
     *            A Transition of concern.
     * @return List Returns all the backward connected Places to the transition.
     */
    private LinkedList _findBackwardConnectedPlaces(
            TypedCompositeActor transition) {
        LinkedList newRelationList = new LinkedList();
        Iterator inputPorts = transition.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inPort = (IOPort) inputPorts.next();
            newRelationList.addAll(inPort.linkedRelationList());
        }
        LinkedList temporaryPlaceList = new LinkedList();
        while (newRelationList.size() > 0) {
            IORelation weights = (IORelation) newRelationList.getFirst();
            temporaryPlaceList.addAll(_findBackwardConnectedPlaces(weights));
            newRelationList.remove(weights);
        }
        return temporaryPlaceList;
    }

    /**
     * This method finds the forward connected Places or output Places for a
     * given relation. This is equivalent to find all Places reachable for this
     * relation. This method is needed when we update the tokens in Places
     * connected to a firing Transition.
     *
     * We can not use the method deeplyConnectedPortList() due to the
     * duplication of ports in the list.
     *
     * @param weight
     *            The arc connecting transition output to ports or Places.
     * @return List The output Places needed to be updated if the transition
     *         connected to the weight fires.
     */
    private LinkedList _findForwardConnectedPlaces(IORelation weight) {
        LinkedList newRelationList = new LinkedList();
        newRelationList.add(weight);
        LinkedList temporaryDestinationPortList = new LinkedList();
        LinkedList temporaryPlaceList = new LinkedList();
        while (newRelationList.size() > 0) {
            IORelation weights = (IORelation) newRelationList.getFirst();
            Iterator weightPorts = weights.linkedDestinationPortList()
                    .iterator();
            while (weightPorts.hasNext()) {
                IOPort weightPort = (IOPort) weightPorts.next();
                if (!temporaryDestinationPortList.contains(weightPort)) {
                    temporaryDestinationPortList.add(weightPort);
                    Nameable weightPlace = weightPort.getContainer();
                    if (weightPlace instanceof PetriNetActor) {
                        if (weightPort.isOutput()) {
                            newRelationList.addAll(weightPort
                                    .linkedRelationList());
                        } else if (weightPort.isInput()) {
                            newRelationList.addAll(weightPort
                                    .insideRelationList());
                        }
                    } else if (weightPlace instanceof Place) {
                        temporaryPlaceList.add(weightPlace);
                    } else {
                        _debug("------found no place/PetriNetActor"
                                + weightPort.getFullName());
                    }
                }
            }
            newRelationList.remove(weights);
        }
        return temporaryPlaceList;
    }

    /**
     * Test whether a PetriNetActor can be fired or not, and
     * fires the PetriNetActor once if it can be fired. The method
     * first finds all the enabled components returned by
     * _readyComponentList(); then it randomly chooses one component
     * to fire. If the chosen component is a PetriNetActor, this
     * method is called recursively to fire the chosen PetriNetActor
     * component; otherwise the chosen component must be a Transition
     * represented by any TypedCompositeActor, and this method calls
     * the method fireTransition() to fire the Transition.
     *
     * @param container
     *            The container of the hierarchical Petri net.
     * @return true or false The PetriNetActor container can be fired or not.
     * @exception IllegalActionException
     *                If _readyComponentList() or fireTransition() throws an
     *                exception.
     */
    private boolean _fireHierarchicalPetriNetOnce(TypedCompositeActor container)
            throws IllegalActionException {
        List components = _readyComponentList(container);
        int componentCount = components.size();
        if (componentCount == 0) {
            return false;
        } else if (componentCount > 0) {
            if (_debugging) {
                _debug(componentCount + " transitions ready");
            }
            if (_needNewGenerator) {
                _createGenerator();
            }
            Collections.shuffle(components, _random);
            for (int i = 0; i < components.size(); i++) {
                if (components.get(i) instanceof TypedCompositeActor) {
                    TypedCompositeActor realTransition = (TypedCompositeActor) components
                            .get(i);
                    if (isTransitionReady(realTransition)) {
                        fireTransition(realTransition);
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Initialize the Director parameters.
     *
     * @exception IllegalActionException
     *              If setting the type or expression of the iterations
     *              parameter throws an exception.
     *
     * @exception NameDuplicationException
     *              If the parameter name for iterations already exists.
     */
    private void _initParameters() throws IllegalActionException,
    NameDuplicationException {
        iterations = new Parameter(this, "iterations");
        iterations.setExpression("0");
        iterations.setTypeEquals(BaseType.INT);

        seed = new SharedParameter(this, "seed", PetriNetDirector.class, "0L");
        seed.setTypeEquals(BaseType.LONG);

        resetOnEachRun = new SharedParameter(this, "resetOnEachRun",
                PetriNetDirector.class, "false");
        resetOnEachRun.setTypeEquals(BaseType.BOOLEAN);

    }

    /**
     * Return all the enabled components in a container.
     *
     * <p>The firing method will choose one component from this list
     * randomly to fire. A Transition is enabled if
     * isTransitionReady() returns true on testing the transition. A
     * PetriNetActor is an enabled component if it contains an enabled
     * Transition, which is tested by the method
     * petriNetActor.prefire().</p>
     *
     * @param container
     *            Test how many components are ready to fire in the container.
     * @return Return all the ready to fire components in the container.
     * @exception IllegalActionException
     *                If isTransitionReady() or PetriNetActor.prefire() throws
     *                exception.
     */
    private List _readyComponentList(TypedCompositeActor container)
            throws IllegalActionException {
        Iterator actors = container.entityList().iterator();
        LinkedList readyComponentList = new LinkedList();
        while (actors.hasNext()) {
            Nameable component = (Nameable) actors.next();
            if (component instanceof TypedCompositeActor) {
                TypedCompositeActor componentTransition = (TypedCompositeActor) component;
                if (isTransitionReady(componentTransition)) {
                    readyComponentList.add(componentTransition);
                }
            }
        }
        return readyComponentList;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current value of the seed parameter. */
    private long _generatorSeed = 0L;

    /** Indicator that a new generator is needed. */
    private boolean _needNewGenerator = true;

    /** The Random object, used to shuffle transitions. */
    private Random _random;
}

/* An Interface Automaton.

 Copyright (c) 1999-2001 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

import java.util.HashMap;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// InterfaceAutomaton
/**
This class models an Interface Automaton. Interface automata is an automata
model defined by Luca de Alfaro in the paper "Interface Automata". 
An InterfaceAutomaton contains a set of states and
InterfaceAutomatonTransitions. There are three kinds transitions:
input transition, output transition, and internal transitions.
<p>
(FIXME: Are interface automata that are fired required to be deterministic?
or just randomly choose a transition.)

When an InterfaceAutomaton is fired, the outgoing transitions of the current
state are examined. An IllegalActionException is thrown if there is more than
one enabled transition. If there is exactly one enabled transition then it is
taken.
<p>
An InterfaceAutomaton enters its initial state during initialization. The
name of the initial state is specified by the <i>initialStateName</i> string
attribute.
<p>
An InterfaceAutomaton contains a set of variables for the input ports that
can be referenced in the labels of input transitions.
<p>

@author Yuhong Xiong, Xiaojun Liu and Edward A. Lee
@version $Id$
@see State
@see InterfaceAutomatonTransition
*/
public class InterfaceAutomaton extends FSMActor {

    /** Construct an InterfaceAutomaton in the default workspace with an
     *  empty string as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public InterfaceAutomaton() {
        super();
    }

    /** Construct an InterfaceAutomaton in the specified workspace with an
     *  empty string as its name. The name can be changed later with
     *  setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public InterfaceAutomaton(Workspace workspace) {
        super(workspace);
    }

    /** Create an InterfaceAutomaton in the specified container with the
     *  specified name. The name must be unique within the container or an
     *  exception is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this automaton within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public InterfaceAutomaton(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new InterfaceAutomaton that is the composition of the
     *  specified InterfaceAutomaton and this one.
     *  @param automaton An InterfaceAutomaton to compose with this one.
     *  @return An InterfaceAutomaton that is the composition.
     *  @exception IllegalActionException If this automaton is not composable
     *   with the argument.
     */
    public InterfaceAutomaton compose(InterfaceAutomaton automaton)
                throws IllegalActionException {
        // First computes the product automaton, then prunes the illegal
        // states.
        InterfaceAutomaton product = _computeProduct(automaton);


        return null;
    }

    // FIXME: do we allow non-determinism? how to handle it?
    /** Set the values of input variables. Choose the enabled transition
     *  among the outgoing transitions of the current state. Throw an
     *  exception if there is more than one transition enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Create a new instance of InterfaceAutomatonTransition with the
     *  specified name in this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return An InterfaceAutomatonTransition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            InterfaceAutomatonTransition transition =
                    new InterfaceAutomatonTransition(this, name);
            return transition;
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add an InterfaceAutomatonTransition to this InterfaceAutomaton.
     *  This method should not be used directly.  Call the setContainer()
     *  method of the transition instead. This method does not set the
     *  container of the transition to refer to this container. This method
     *  is <i>not</i> synchronized on the workspace, so the caller should be.
     *
     *  @param relation The InterfaceAutomatonTransition to contain.
     *  @exception IllegalActionException If the transition has no name, or
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof InterfaceAutomatonTransition)) {
            throw new IllegalActionException(this, relation,
                    "InterfaceAutomaton can only contain instances of "
                    + "InterfaceAutomatonTransition.");
        }
        super._addRelation(relation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Compute the product of this autmaton and the argument.
    // Use frontier exploration. The frontier is represented by a HashMap
    // frontier. The key is the name of the state in the product, the value
    // is a Triple: procudeState, stateInThis, stateInArgument. The keys
    // are used to easily check if a product state is in the frontier.
    //
    // init: set the product to empty
    //       product = frontier = (this.initialState x automaton.initialSate, 
    //                             this.initialState, automaton.initialState)
    // iterate: pick a currentState from frontie
    //          for each transition from the currentState:
    //              (1) check composability;
    //              (2) if destination state not in product, add it to
    //                  both the product and the frontier
    //              (3) add the transition to the product.
    //          remove currentState from frontier
    // end: when frontier is empty
    //
    // The name of the states in the product is formed by the 
    // <nameInThisAutomaton>_&_<nameInArgumentAutomaton>
    //
    private InterfaceAutomaton _computeProduct(InterfaceAutomaton automaton)
            throws IllegalActionException {
        try {
            // init
            InterfaceAutomaton product = new InterfaceAutomaton();
            HashMap frontier = new HashMap();

            State currentStateThis = this.getInitialState();
            State currentStateArgument = automaton.getInitialState();
            String name = currentStateThis.getName() + "_&_"
                              + currentStateArgument.getName();
            // set container to null now so it is not in the product automaton
            State currentState = new State(null, name);
            Triple triple = new Triple(currentState, currentStateThis,
                                       currentStateArgument);
            frontier.put(name, triple);

            // iterate
            while ( !frontier.isEmpty()) {
                // pick a value from frontier. It seems that there isn't an
                // easy way to pick an arbitrary entry from a HashMap, except
                // through Iterator
                Iterator iterator = frontier.keySet().iterator();
                name = (String)iterator.next();
                triple = (Triple)frontier.get(name);

                // extend frontier from state in this automaton
                currentStateThis = triple._stateInThis;
                ComponentPort outPort = currentStateThis.outgoingPort;
                Iterator transitions = outPort.linkedRelationList().iterator();
                while (transitions.hasNext()) {
                    InterfaceAutomatonTransition transition =
                            (InterfaceAutomatonTransition)transitions.next();
                    // check composability
                    int transitionType = transition.getTransitionType();
                    if (transitionType ==
                            InterfaceAutomatonTransition.INPUT_TRANSITION) {


                    }
                    else if (transitionType ==
                            InterfaceAutomatonTransition.OUTPUT_TRANSITION) {


                    } else if (transitionType ==
                            InterfaceAutomatonTransition.INTERNAL_TRANSITION) {


                    } else {
                        throw new InternalErrorException(
                            "InterfaceAutomaton._computeProduct: unrecognized "
                            + "transition type.");
                    }
                }
            }

            return null;
        } catch (NameDuplicationException exception) {
            // FIXME: this can actually happen, although extremly unlikely.
            // Eg. this automaton has states "a" and "b_&_c", the argument
            // has "a_&_b" and "c". Do we need to worry about this?
            throw new InternalErrorException(
                "InterfaceAutomaton._computeProduct: name in product "
		+ "automaton clashes: " + exception.getMessage());
	}
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    ///////////////////////////////////////////////////////////////////
    ////                            inner class                    ////
    private class Triple {
        private Triple(State productState, State stateInThis,
                                                State stateInArgument) {
            _productState = productState;
            _stateInThis = stateInThis;
            _stateInArgument = stateInArgument;
        }

        private State _productState = null;
        private State _stateInThis = null;
        private State _stateInArgument = null;
    }
}

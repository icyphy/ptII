/* A DiscreteState is a state in Hybrid Automaton.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.apps.hsif.lib;

import java.util.LinkedList;

public class DiscreteState {

  public DiscreteState (String DSName){
    _name = DSName;
 }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

  /** Return the name of the DiscreteState.
   */
  public String getName() {
    return _name;
  }

  /** Return the invariants of the DiscreteState.
   */
  public LinkedList getInvariants() {
    return _invariants;
  }

  /** Return the algEquations of the DiscreteState.
   */
  public LinkedList getAlgEquations() {
    return _algEquations;
  }

  /** Return the flowEquations of the DiscreteState.
   */
  public LinkedList getFlowEquations() {
    return _flowEquations;
  }

  /** Return the actions of the DiscreteState.
   */
  public LinkedList getActions() {
    return _actions;
  }

  /** Add an invariant to the invariants of the DiscreteState.
   */
  public void addInvariant(Invariant invariant) {
    _invariants.add(invariant);
  }

  /** Add an algEquation to the algEquations of the DiscreteState.
   */
  public void addAlgEquation(AlgEquation algEquation) {
    _algEquations.add(algEquation);
  }

  /** Add a flowEquation to the flowEquations of the DiscreteState.
   */
  public void addFlowEquation(FlowEquation flowEquation) {
    _flowEquations.add(flowEquation);
  }

  /** Add a action to the actions of the DiscreteState.
   */
  public void addAction(Action action) {
    _actions.add(action);
  }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                   ////

  private String _name = "";

  private LinkedList _invariants = new LinkedList();
  private LinkedList _algEquations = new LinkedList();
  private LinkedList _flowEquations = new LinkedList();
  private LinkedList _actions = new LinkedList();
}
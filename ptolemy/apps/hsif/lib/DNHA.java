/* A DNHA is a dynamic network of hybrid automata.

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

public class DNHA {

  /** Constructor for DNHA
   *  @param DNHAname The name for DNHA.
   */
  public DNHA (String DNHAName){
    _name = DNHAName;
 }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

  /** Return the name of the DNHA.
   */
  public String getName() {
    return _name;
  }

  /** Return the variables of the DNHA.
   */
  public LinkedList getVariables() {
    return _variables;
  }

  /** Return the parameters of the DNHA.
   */
  public LinkedList getParameters() {
    return _parameters;
  }

  /** Return the constraints of the DNHA.
   */
  public LinkedList getConstraints() {
    return _constraints;
  }

  /** Return the channels of the DNHA.
   */
  public LinkedList getChannels() {
    return _channels;
  }

  /** Return the hybrid automata of the DNHA.
   */
  public LinkedList getHybridAutomata() {
    return _hybridAutomata;
  }

  /** Add a variable to the variables of the DNHA.
   */
  public void addVariable(Variable variable) {
    _variables.add(variable);
  }

  /** Add a parameter to the parameters of the DNHA.
   */
  public void addParameter(Parameter parameter) {
    _parameters.add(parameter);
  }

  /** Add a channel to the channels of the DNHA.
   */
  public void addChannel(Channel channel) {
    _channels.add(channel);
  }

  /** Add a constraint to the constraints of the DNHA.
   */
  public void addConstraint(Constraint constraint) {
    _constraints.add(constraint);
  }

  /** Add a hybrid automaton to the hybrid automata of the DNHA.
   */
  public void addHybridAutomaton(HybridAutomaton hybridAutomaton) {
    _hybridAutomata.add(hybridAutomaton);
  }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

  private String _name = "";

  private LinkedList _variables = new LinkedList();
  private LinkedList _parameters = new LinkedList();
  private LinkedList _constraints = new LinkedList();
  private LinkedList _channels = new LinkedList();
  private LinkedList _hybridAutomata = new LinkedList();
}
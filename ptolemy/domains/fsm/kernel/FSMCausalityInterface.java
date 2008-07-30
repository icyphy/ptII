/* Causality interface for FSM actors.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
////FSMCausalityInterface

/**
This class infers the causality interface of an FSMActor by checking
the guards and actions of the transitions. If any transition in the
model has an output action that writes to a port and a guard that
references an input port, then there is a direct dependency of
that output on that input. Otherwise, there is no dependency.
Note that this is a conservative analysis in that it may indicate
a dependency when there is none. For example, if all outgoing
transitions from a state produce the same output value, and 
a transition is always taken, then irrespective of the guards,
the output has no dependency on the inputs.  A precise analysis,
however, is much more difficult (probably undecidable).
<p>
All input ports that affect the state (i.e. that are mentioned in
any guard) must be in an equivalence class. Otherwise, we cannot
reliably make a decision about what the next state is. In addition,
if any input in a refinement affects an output, that input must
also be in this equivalence class. Otherwise, the scheduler
will assume there is no relationship between these inputs and
could provide an event that triggers a state transition in an
earlier firing than an event that triggers an output from the
current refinement.

@author Edward A. Lee
@version $Id: FSMCausalityInterface.java 47513 2007-12-07 06:32:21Z cxh $
@since Ptolemy II 7.2
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (eal)
*/
public class FSMCausalityInterface extends CausalityInterfaceForComposites {
   
   /** Construct a causality interface for the specified actor.
    *  @param actor The actor for which this is a causality interface.
    *   This is required to be an instance of CompositeEntity.
    *  @param defaultDependency The default dependency of an output
    *   port on an input port.
    */
   public FSMCausalityInterface(
           Actor actor, Dependency defaultDependency) 
           throws IllegalArgumentException {
       super(actor, defaultDependency);
       if (!(actor instanceof FSMActor)) {
           throw new IllegalArgumentException(
                   "Cannot create an instance of " +
                   "FSMCausalityInterface for " +
                   actor.getFullName()
                   +", which is not an FSMActor.");
       }
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   /** Return the dependency between the specified input port
    *  and the specified output port.  This is done by checking
    *  the guards and actions of all the transitions.
    *  When called for the first time since a change in the model
    *  structure, this method performs the complete analysis of
    *  the FSM and caches the result. Subsequent calls just
    *  look up the result.
    *  @param input The input port.
    *  @param output The output port, or null to update the
    *   dependencies (and record equivalence classes) without
    *   requiring there to be an output port.
    *  @return The dependency between the specified input port
    *   and the specified output port, or null if a null output
    *   is port specified.
    *  @exception IllegalActionException If a guard expression cannot be parsed.
    */
   public Dependency getDependency(IOPort input, IOPort output)
           throws IllegalActionException {
       // Cast is safe because this is checked in the constructor
       FSMActor actor = (FSMActor)_actor;
       
       // If the dependency is not up-to-date, then update it.
       long workspaceVersion = actor.workspace().getVersion();
       if (_dependencyVersion != workspaceVersion) {
           // Need to update dependencies. The cached version
           // is obsolete.
           boolean stateDependentCausality = ((BooleanToken)
                   actor.stateDependentCausality.getToken())
                   .booleanValue();
           try {
               actor.workspace().getReadAccess();
               _reverseDependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
               _forwardDependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
               
               // Initialize the equivalence classes to contain each input port.
               _equivalenceClasses = new HashMap<IOPort,Set<IOPort>>();
               List<IOPort> actorInputs = _actor.inputPortList();
               for (IOPort actorInput : actorInputs) {
                   Set<IOPort> equivalences = new HashSet<IOPort>();
                   equivalences.add(actorInput);
                   _equivalenceClasses.put(actorInput, equivalences);
               }
               
               // Iterate over all the transitions or just the transitions
               // of the current state.
               Collection<Transition> transitions;
               if (!stateDependentCausality) {
                   transitions = actor.relationList();
               } else {
                   State currentState = actor.currentState();
                   transitions = currentState.outgoingPort.linkedRelationList();
               }
               for (Transition transition : transitions) {
                   // Collect all the output ports that are written to on this transition.
                   Set<IOPort> outputs = new HashSet<IOPort>();
                   
                   // Look only at the "choice" actions because "commit" actions
                   // do not execute until postfire(), and hence do not imply
                   // an input/output dependency.
                   List<AbstractActionsAttribute> actions = transition.choiceActionList();
                   for (AbstractActionsAttribute action : actions) {
                       List<String> names = action.getDestinationNameList();
                       for (String name : names) {
                           NamedObj destination = action.getDestination(name);
                           if (destination instanceof IOPort
                                   && ((IOPort)destination).isOutput()) {
                               // Found an output that is written to.
                               outputs.add((IOPort)destination);
                           }
                       }
                   }

                   // Now handle the guard expression, finding
                   // all referenced input ports.
                   Set<IOPort> inputs = new HashSet<IOPort>();
                   String guard = transition.getGuardExpression();
                   // The guard expression may be empty (in the ERG domain,
                   // for example). Continue if this is the case.
                   if (guard.trim().equals("")) {
                       continue;
                   }
                   // Parse the guard expression.
                   PtParser parser = new PtParser();
                   try {
                       ASTPtRootNode guardParseTree = parser.generateParseTree(guard);
                       ParseTreeFreeVariableCollector collector = new ParseTreeFreeVariableCollector();
                       Set<String> freeVariables = collector.collectFreeVariables(guardParseTree);
                       for (String freeVariable : freeVariables) {
                           // Reach into the FSMActor to get the port.
                           IOPort port = (IOPort)actor._identifierToPort.get(freeVariable);
                           if (port != null && port.isInput()) {
                               // Found a reference to an input port in the guard.
                               inputs.add(port);
                           }
                       }
                   } catch (IllegalActionException ex) {
                       throw new IllegalActionException(actor, ex,
                               "Failed to parse guard expression \""
                               + guard + "\"");
                   }
                   if (inputs.isEmpty()) {
                       continue;
                   }
                   // If any guard references more than one input, then
                   // those inputs need to be in the same equivalence class.
                   if (inputs.size() > 1) {
                       Set<IOPort> merge = new HashSet<IOPort>(inputs);
                       for (IOPort actorInput : inputs) {
                           // Add all input ports that are equivalence classes with
                           // these input ports.
                           Set<IOPort> equivalences = _equivalenceClasses.get(actorInput);
                           merge.addAll(equivalences);
                       }
                       for (IOPort actorInput : merge) {
                           _equivalenceClasses.put(actorInput, merge);
                       }
                   }
                   // Set dependencies of all the found output
                   // ports on all the found input ports.
                   for (IOPort writtenOutput : outputs) {
                       Map<IOPort,Dependency> outputMap = _reverseDependencies.get(writtenOutput);
                       if (outputMap == null) {
                           outputMap = new HashMap<IOPort,Dependency>();
                           _reverseDependencies.put(writtenOutput, outputMap);
                       }
                       for (IOPort readInput : inputs) {
                           outputMap.put(readInput, _defaultDependency.oTimesIdentity());
                           
                           // Now handle the forward dependencies.
                           Map<IOPort,Dependency> inputMap = _forwardDependencies.get(readInput);
                           if (inputMap == null) {
                               inputMap = new HashMap<IOPort,Dependency>();
                               _forwardDependencies.put(readInput, inputMap);
                           }
                           inputMap.put(writtenOutput, _defaultDependency.oTimesIdentity());
                       }
                   }
               }
           } finally {
               actor.workspace().doneReading();
           }
           _dependencyVersion = workspaceVersion;
       }
       if (output == null) {
           return null;
       }
       Map<IOPort,Dependency> inputMap = _forwardDependencies.get(input);
       if (inputMap != null) {
           Dependency result = inputMap.get(output);
           if (result != null) {
               return result;
           }
       }
       // If there is no recorded dependency, then reply
       // with the additive identity (which indicates no
       // dependency).
       return _defaultDependency.oPlusIdentity();
   }
}
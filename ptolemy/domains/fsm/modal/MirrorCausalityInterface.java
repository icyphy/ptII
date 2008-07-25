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
package ptolemy.domains.fsm.modal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Dependency;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// MirrorCausalityInterface

/**
This class infers a causality interface from causality interfaces provided
in the constructor and in the {@link #composeWith} method. For each of these
interfaces, this interface finds ports in its own actor that match the names
of those for the specified interfaces, and constructs dependencies that
are oPlus compositions of the dependencies in the specified interfaces for
ports with the same names.

@author Edward A. Lee
@version $Id: MirrorCausalityInterface.java 47513 2007-12-07 06:32:21Z cxh $
@since Ptolemy II 7.2
@Pt.ProposedRating Yellow (eal)
@Pt.AcceptedRating Red (eal)
*/
public class MirrorCausalityInterface extends CausalityInterfaceForComposites {
   
   /** Construct a causality interface that mirrors the specified causality
    *  interface.
    *  @param actor The actor for which this is an interface.
    *  @param causality The interface to mirror.
    */
   public MirrorCausalityInterface(Actor actor, CausalityInterface causality)
           throws IllegalArgumentException {
       super(actor, causality.getDefaultDependency());
       _composedInterfaces.add(causality);
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   /** Add the specified causality interface for the specified actor.
    *  @param causality The interface to compose with the one specified
    *   in the constructor.
    */
   public void composeWith(CausalityInterface causality) {
       _composedInterfaces.add(causality);
   }

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
       // If the dependency is not up-to-date, then update it.
       long workspaceVersion = ((NamedObj)_actor).workspace().getVersion();
       if (_dependencyVersion != workspaceVersion) {
           // Need to update dependencies. The cached version
           // is obsolete.
           try {
               ((NamedObj)_actor).workspace().getReadAccess();
               _reverseDependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
               _forwardDependencies = new HashMap<IOPort,Map<IOPort,Dependency>>();
               _equivalenceClasses = new HashMap<IOPort,Set<IOPort>>();
               // Initialize equivalence classes for each input port.
               List<IOPort> actorInputs = _actor.inputPortList();
               for (IOPort actorInput : actorInputs) {
                   Set<IOPort> equivalences = new HashSet<IOPort>();
                   equivalences.add(actorInput);
                   _equivalenceClasses.put(actorInput, equivalences);
                   // FIXME
               }
               // Iterate over all the associated interfaces.
               for (CausalityInterface causality : _composedInterfaces) {
                   List<IOPort> mirrorInputs = causality.getActor().inputPortList();
                   for (IOPort mirrorInput : mirrorInputs) {
                       Port localInput = ((Entity)_actor).getPort(mirrorInput.getName());
                       if (!(localInput instanceof IOPort)) {
                           throw new IllegalActionException(_actor, mirrorInput.getContainer(),
                                   "No matching port with name " + mirrorInput.getName());
                       }
                       Map<IOPort,Dependency> forwardMap = _forwardDependencies.get((IOPort)localInput);
                       if (forwardMap == null) {
                           forwardMap = new HashMap<IOPort,Dependency>();
                           _forwardDependencies.put((IOPort)localInput, forwardMap);
                       }
                       for (IOPort dependentOutput : causality.dependentPorts(mirrorInput)) {
                           Port localOutput = ((Entity)_actor).getPort(dependentOutput.getName());
                           if (!(localOutput instanceof IOPort)) {
                               throw new IllegalActionException(_actor, mirrorInput.getContainer(),
                                       "No matching port with name " + mirrorInput.getName());
                           }
                           Dependency dependency = causality.getDependency(mirrorInput, dependentOutput);
                           forwardMap.put((IOPort)localOutput, dependency);
                           // Now handle the reverse dependencies.
                           Map<IOPort,Dependency> backwardMap = _reverseDependencies.get((IOPort)localOutput);
                           if (backwardMap == null) {
                               backwardMap = new HashMap<IOPort,Dependency>();
                               _reverseDependencies.put((IOPort)localOutput, backwardMap);
                           }
                           backwardMap.put((IOPort)localInput, dependency);
                       }
                       // Next do equivalence classes.
                       // The following cannot be null... initialized above.
                       Set<IOPort> equivalences = _equivalenceClasses.get(localInput);
                       Collection<IOPort> mirrorEquivalents = causality.equivalentPorts(mirrorInput);
                       for (IOPort equivalent : mirrorEquivalents) {
                           Port localEquivalent = ((Entity)_actor).getPort(equivalent.getName());
                           if (!(localEquivalent instanceof IOPort)) {
                               throw new IllegalActionException(_actor, mirrorInput.getContainer(),
                                       "No matching port with name " + equivalent.getName());
                           }
                           mirrorEquivalents.add((IOPort)localEquivalent);
                       }
                   }
               }
           } finally {
               ((NamedObj)_actor).workspace().doneReading();
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
   
   ///////////////////////////////////////////////////////////////////
   ////                         private fields                    ////

   /** The set of causality interfaces that this one composes. */
   private Set<CausalityInterface> _composedInterfaces = new HashSet<CausalityInterface>();
}
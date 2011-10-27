/* This director simulates the execution of the Ptides programming model
   on multi-core platforms.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.domains.ptides.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.SuperdenseDependency;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  This director simulates the execution of the Ptides programming model
 *  on multi-core platforms. The goal is to provide a framework for evaluation
 *  of different multi-core execution strategies.
 *
 *  @author Michael Zimmer
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mzimmer)
 *  @Pt.AcceptedRating Red (mzimmer)
 *
 */
public class PtidesMulticoreDirector extends PtidesPreemptiveEDFDirector {    

    public PtidesMulticoreDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    public void initialize() throws IllegalActionException {
        
        super.initialize();
        _calculateSuperdenseDependenices();
        
    }
    
    /** Calculate the superdense dependency (minimum model time delay) between
     * a source input port and the input port group of a destination input 
     * port. The Floyd-Warshall algorithm is used to calculate the minimum 
     * model time delay paths. 
     * @throws IllegalActionException */
    //TODO: Code assumes code generation is at atomic actor level.
    //TODO: multiports?
    protected void _calculateSuperdenseDependenices() 
            throws IllegalActionException {
       
        // For debugging.
        NamedObj dir = getContainer();
        
        if (!(getContainer() instanceof TypedCompositeActor)) {
            throw new IllegalActionException(getContainer(), getContainer() +
                    "is not a TypedCompositeActor");
        }

        // Initialize HashMap.
        _superdenseDependencyPair = 
            new HashMap<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>>();
        
        // Create a list for all input ports. A List is needed since Set does 
        // not make any guarantees on iteration order.
        List<TypedIOPort> inputPorts = new ArrayList<TypedIOPort>();
        
        // Calculate superdense dependency from each input port of an
        // actor to the input ports of immediate predecessor actors.
        // This builds a directed graph.
        for(Actor actor : (List<Actor>)((TypedCompositeActor) 
                getContainer()).deepEntityList()) {
            
            CausalityInterface actorCausality = actor.getCausalityInterface();
            for(TypedIOPort inputPort: 
                    (List<TypedIOPort>)(actor.inputPortList())) {
                
                // Initialize nested HashMap.
                _superdenseDependencyPair.put(inputPort, 
                        new HashMap<TypedIOPort,SuperdenseDependency>());
                
                // Add to list.
                inputPorts.add(inputPort);
                
                // Set dependency with self.
                _putSuperdenseDependencyPair(inputPort, inputPort, 
                        SuperdenseDependency.OTIMES_IDENTITY);
    
                for(TypedIOPort outputPort: 
                        (List<TypedIOPort>)(actor.outputPortList())) {
                    // Superdense dependency between input port and output
                    // port of current actor.
                    SuperdenseDependency minDelay = 
                        (SuperdenseDependency) actorCausality.getDependency(
                        inputPort, outputPort);
                    // Only if dependency exists...
                    if(!minDelay.equals(
                            SuperdenseDependency.OPLUS_IDENTITY)) {
                        // Set input port pair for all connected ports.
                        // Assumes no delay from connections.
                        for(TypedIOPort connectedPort: 
                                (List<TypedIOPort>)
                                outputPort.connectedPortList()) {
                            // Exclude ports which aren't input ports.
                            //TODO: is this the right check?
                            if(connectedPort.isInput()) {
                                /*
                                _debug(inputPort.getName(dir) + "->" + 
                                            connectedPort.getName(dir) + ": " +
                                            minDelay.timeValue() + " (" + 
                                            minDelay.indexValue() + ")");
                                */
                                _putSuperdenseDependencyPair(inputPort, 
                                        connectedPort, minDelay);
                            }
                        }
                    } else {
                        //_debug("no dependency");
                    }
                }
            }
        }
        
        // Floyd-Warshall algorithm. This finds the minimum model time delay
        // between all input ports.
        for(TypedIOPort k : inputPorts) {
            for(TypedIOPort i : inputPorts) {
                for(TypedIOPort j : inputPorts) {
                    SuperdenseDependency ij, ik, kj;
                    ij = _getSuperdenseDependencyPair(i, j);
                    ik = _getSuperdenseDependencyPair(i, k);
                    kj = _getSuperdenseDependencyPair(k, j);
                    // Check if i->k->j is better than i->j.
                    if(ij.compareTo(ik.oTimes(kj)) == 
                            SuperdenseDependency.GREATER_THAN) {
                        _putSuperdenseDependencyPair(i, j, 
                                (SuperdenseDependency) ik.oTimes(kj));
                    }
                }
            }
        }
        
        // Currently have the superdense dependency from each source input 
        // port to every destination input port, not the input port group
        // of the destination input port. Find input port groups and fix this.
        for(Actor actor : (List<Actor>)((TypedCompositeActor) 
                getContainer()).deepEntityList()) {
            
            CausalityInterface actorCausality = actor.getCausalityInterface();

            for(TypedIOPort outputPort: 
                    (List<TypedIOPort>)(actor.outputPortList())) {      
                
                Set<TypedIOPort> inputPortGroup = new HashSet<TypedIOPort>();
    
                for(TypedIOPort inputPort: 
                        (List<TypedIOPort>)(actor.inputPortList())) {
                    // Superdense dependency between input port and output
                    // port of current actor.
                    SuperdenseDependency minDelay = 
                        (SuperdenseDependency) actorCausality.getDependency(
                        inputPort, outputPort);
                    // Only if dependency exists...
                    if(!minDelay.equals(
                            SuperdenseDependency.OPLUS_IDENTITY)) {                     
                        inputPortGroup.add(inputPort);   
                    } 
                }
                
                // Go through all source input ports and find the superdense
                // dependency to the current destination input port group of
                // this actor.
                // FIXME: If an actor has non-disjoint input port groups, then
                // the value stored may depend on the order in which input
                // port groups are visited.
                for(TypedIOPort srcPort : inputPorts) {
                    SuperdenseDependency min = 
                        SuperdenseDependency.OPLUS_IDENTITY;
                    for(TypedIOPort destPort : inputPortGroup) {
                        min = (SuperdenseDependency) min.oPlus(
                                    _getSuperdenseDependencyPair(
                                            srcPort, destPort));
                    }
                    if(!min.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
                        for(TypedIOPort destPort : inputPortGroup) {
                            _putSuperdenseDependencyPair(
                                    srcPort, destPort, min);
                        }
                    }
                }
            }
        }

        if(_debugging) {
            StringBuffer buf = new StringBuffer();
            buf.append("\t");
            for(TypedIOPort srcPort : inputPorts) {
                buf.append(srcPort.getName(dir) + "\t");
            }
            _debug(buf.toString());
            for(TypedIOPort srcPort : inputPorts) {
                buf = new StringBuffer();
                buf.append(srcPort.getName(dir) + "\t");
                for(TypedIOPort destPort : inputPorts) {
                    buf.append(_getSuperdenseDependencyPair(srcPort, destPort)
                            .timeValue() + "(" +
                            _getSuperdenseDependencyPair(srcPort, destPort)
                            .indexValue() + ")\t"); 
                }
                _debug(buf.toString());
            }
        }
        
    }
    
    
    /** Store the superdense dependency between pairs of input ports using 
     * nested Maps. Providing the source input as a key will return a Map 
     * value, where the destination input port can be used as a key to return 
     * the superdense dependency. 
     */
    private Map<TypedIOPort, Map<TypedIOPort,SuperdenseDependency>> 
            _superdenseDependencyPair;
    
    /** Store a superdense dependency between a source input port and the
     * input port group of a destination input port.
     * 
     * @param src The source input port.
     * @param dest The destination input port.
     * @param dep The superdense dependency.
     */
    protected void _putSuperdenseDependencyPair(TypedIOPort src, 
            TypedIOPort dest, SuperdenseDependency dep) {
        if(!dep.equals(SuperdenseDependency.OPLUS_IDENTITY)) {
            _superdenseDependencyPair.get(src).put(dest, dep);
        }
    }
    
    /** Return the superdense dependency between a source input port and the
     * input port group of a destination input port.
     * 
     * @param src Source input port.
     * @param dest Destination input port.
     * @return Superdense dependency.
     */
    protected SuperdenseDependency _getSuperdenseDependencyPair(
            TypedIOPort src, TypedIOPort dest) {
        if(_superdenseDependencyPair.containsKey(src) &&
                _superdenseDependencyPair.get(src).containsKey(dest)) {
            return _superdenseDependencyPair.get(src).get(dest);
        } else {
            return SuperdenseDependency.OPLUS_IDENTITY;
        }
    }
}

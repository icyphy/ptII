/* A FunctionDependency is an abstract class that describes the function
dependency relation between the inputs and outputs of an actor.

 Copyright (c) 2003 The Regents of the University of California.
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

package ptolemy.actor;

import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.util.NamedObj;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependency
/** A FunctionDependency is an abstract class that describes the function 
dependence relation between the inputs and outputs of an actor. 
<p> It contains a ports-graph including the ports of both
the container and the contained actors (if any). The detailed
implementation of how to contruct the graph is undefined but left to sub classes.
<p>
A pair of ports, input and output, are declared dependent if the current value 
of output depends on the current value of the input. Otherwise, it is independent.
So, for TimedDelay, the input and output are independent, while for AddSubtract,
the output port is dependent on both the plus and minus inputs. Another instance 
of independent input and output pair may happen in a composite actor, where the
input and output are not related. 
<p>
To check if the ports graph has cycles, use the <i>getCycleNodes</i> method. The 
method returns an array of IOPorts in cycles. If there is no cycle, the returned
array is empty.

@see FunctionDependencyOfAtomicActor
@see FunctionDependencyOfCompositeActor
@see ptolemy.domains.de.kernel.DEEDirector
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public abstract class FunctionDependency {

    /** Construct a FunctionDependency object for the given container.
     *  @param container The container has this FunctionDependency object.
     */
    public FunctionDependency (Actor container) {
        _container = container;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a cycle loop in the ports graph of this FunctionDependency
     *  object, return the nodes (ports) in the cycle loop. If there are 
     *  multiple cycles, all the nodes will be returned. If there is no 
     *  cycle, an empty array is returned. The type of the returned nodes is IOPort. 
     *  <p>
     *  The validity of the FunctionDependency object is checked at the beginning
     *  of this method.
     *  @return An array contains the IOPorts in the cycles.
     *  @see #validate()
     */
    public Object[] getCycleNodes() {
        validate();
        return _directedGraph.cycleNodes();
    } 
    
    /** Return an abstract ports graph reflecting the function dependency
     *  information. The ports graph includes only the container ports but not 
     *  those of contained actors. This information is usually used by 
     *  the director of the container, which contains the container of this 
     *  FunctionDependency object. For atomic actors, there is no difference between
     *  this method and the <i>getDetailedPortsGraph</i> method.
     *  <p>
     *  The validity of the FunctionDependency object is checked at the beginning
     *  of this method.
     *  @return a detailed ports graph reflecting the function dependency
     *  information that includes the internal ports.
     *  @see #getDetailedPortsGraph
     */
    public DirectedGraph getAbstractPortsGraph() {
        // There is no difference between the getAbstractPortsGraph
        // and getDetailedPortsGraph methods.
        if (_container instanceof AtomicActor) {
            _abstractPortsGraph = _directedGraph;
            return getDetailedPortsGraph();
        }
        validate();
        // construct a new directed graph
        _abstractPortsGraph = new DirectedGraph();

        // add inputs and outputs of the container to the graph.
        Iterator inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            _abstractPortsGraph.addNodeWeight(inputs.next());
        }
        Iterator outputs = _container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            _abstractPortsGraph.addNodeWeight(outputs.next());
        }        
        
        // connect the input to output if there is a dependency between them
        inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            Collection reachableOutputs = 
                _directedGraph.reachableNodes(_directedGraph.node(inputPort));
            outputs = _container.outputPortList().listIterator();
            while (outputs.hasNext()) {
                IOPort outputPort = (IOPort) outputs.next();
                if (reachableOutputs.contains(_directedGraph.node(outputPort))) {
                    _abstractPortsGraph.addEdge(inputPort, outputPort);
//                    _abstractPortsGraph.addEdge(inputPort, outputPort, 
//                        new Integer(1));
                }
            }
        }

        return _abstractPortsGraph;
    }

    /** Return a detailed ports graph reflecting the function dependency
     *  information. The ports graph includes both the container ports and those 
     *  of contained actors (if any). This information is usually used by the 
     *  local director of the container of this FunctionDependency object. 
     *  <p>
     *  The validity of the FunctionDependency object is checked at the beginning
     *  of this method.
     *  @return a detailed ports graph reflecting the input output dependency
     *  information that includes the internal ports.
     *  @see #getAbstractPortsGraph
     */
    public DirectedGraph getDetailedPortsGraph() {
        validate();
        return _directedGraph;
    }
    
    /** Get the independent outputs of the given input port.
     *  @param inputPort The given input port.
     *  @return a set of output ports that are independent of the input.
     */
    //  FIXME: This will be removed when the DEDirector redesign finishes.
    public Set getIndependentOutputPorts(IOPort inputPort) {
        validate();
        Set independentOutputPorts = new HashSet();
        Collection reachableOutputs = 
            _directedGraph.reachableNodes(_directedGraph.node(inputPort));
        Iterator outputs = _container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            IOPort outputPort = (IOPort) outputs.next();
            if (!reachableOutputs.contains(_directedGraph.node(outputPort))) {
                independentOutputPorts.add(outputPort);
            }
        }
        return independentOutputPorts;
    }
 
    /** Get the input ports on which the given output port is dependent.
     *  @param outputPort The given output port.
     *  @return a set of input ports.
     */
    public Set getDependentInputPorts(IOPort outputPort) {
        validate();
        Set dependentInputs = new HashSet();
        if (_abstractPortsGraph == null) {
            // force to construct an abstractPotrsGraph
            getAbstractPortsGraph();
        }
        Object[] backReachableInputs = 
            _abstractPortsGraph.backwardReachableNodes(outputPort);
        int arrayLength = backReachableInputs.length;
        for (int i = 0; i < arrayLength; i++) {
            dependentInputs.add(backReachableInputs[i]);
        }
        return dependentInputs;
    }

    /** Make the FunctionDependency object invalid. Note that the 
     *  FunctionDependency
     *  object is used to help a director to construct a valid schedule. 
     *  When a model changes, e.g. the topology change, the director has
     *  to reconstruct the FunctionDependency object and schedule. So 
     *  this method 
     *  is so called to force a reconstruction.
     *  @see ptolemy.domains.de.kernel.DEDirector
     */        
    public void invalidate() {
        _directedGraphValid = -1;
    }

    /** Check the validity of the FunctionDependency object. If it is invalid,
     *  reconstruct it. Otherwise, do nothing.
     */        
    public void validate() {
        long workspaceVersion = 
            ((NamedObj)_container).workspace().getVersion();
        if (_directedGraphValid != workspaceVersion) {
            _constructDirectedGraph();
            _directedGraphValid = workspaceVersion;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.  
     *  This method is left undefined and the sub classes provide
     *  the detailed implementation.
     */
    protected abstract void _constructDirectedGraph(); 


    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    // The container of this IODependnecy object.
    protected Actor _container;
    // The directed graph of the input and output ports.
    protected DirectedGraph _directedGraph;

    ///////////////////////////////////////////////////////////////////
    ////                      private methods                      ////

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    private DirectedGraph _abstractPortsGraph;
    // The validity flag of this attribute.
    private long _directedGraphValid = -1;
}

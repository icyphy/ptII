/* An instance of IODependencyOfFSMActor describes the input-output 
dependence information of an FSM actor.

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

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// IODependencyOfFSMActor
/** An instance of IODependencyOfFSMActor describes the input-output 
dependence information of an FSM actor.

@see IODependency
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependencyOfFSMActor extends IODependency {

    /** Construct an IODependency in the given container. 
     *  @param container The container has this IODependency object.
     */
    public IODependencyOfFSMActor(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.  
     */
    protected void _constructDirectedGraph() {

        // clear the nodes and edges in the directed graph
        _directedGraph = new DirectedGraph();
    
        // First, include all the ports as nodes in the graph.
    
        // get all the inputs and outputs of the container
        Iterator inputs = _container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            _directedGraph.addNodeWeight(inputs.next());
        }
        Iterator outputs = _container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            _directedGraph.addNodeWeight(outputs.next());
        }
    
        // FIXME: In the current implementation, we assume all the outputs
        // are not dependent on the inputs.

    }
}

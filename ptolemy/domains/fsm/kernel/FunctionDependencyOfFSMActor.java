/* An instance of FunctionDependencyOfFSMActor describes the function
dependency information of an FSM actor.

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

package ptolemy.domains.fsm.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.FunctionDependency;
import ptolemy.graph.DirectedGraph;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfFSMActor
/** An instance of FunctionDependencyOfFSMActor describes the function
dependence relation of an FSM actor. It contains a ports graph
including the container ports only.
<p>
For an FSM actor, all the input ports and output ports are independent.

@see FunctionDependency
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class FunctionDependencyOfFSMActor extends FunctionDependency {

    /** Construct a FunctionDependencyOfFSMActor in the given container.
     *  @param container The container has this FunctionDependency object.
     */
    public FunctionDependencyOfFSMActor(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.
     */
    protected void _constructDirectedGraph() {

        // get a new directed graph
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
    }
}

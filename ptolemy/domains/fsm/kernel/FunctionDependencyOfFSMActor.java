/* An instance of FunctionDependencyOfFSMActor describes the function
   dependency information of an FSM actor.

   Copyright (c) 2003-2004 The Regents of the University of California.
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

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.FunctionDependency;
import ptolemy.actor.IOPort;
import ptolemy.graph.DirectedGraph;

//////////////////////////////////////////////////////////////////////////
//// FunctionDependencyOfFSMActor
/** An instance of FunctionDependencyOfFSMActor describes the function
    dependence relation of an FSM actor. Both the abstract and detailed 
    port graph include the container ports only, and they are the same.
    <p>
    For an FSM actor, all the input ports and output ports are dependent.

    @see FunctionDependency
    @author Haiyang Zheng
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (hyzheng)
    @Pt.AcceptedRating Red (hyzheng)
*/
public class FunctionDependencyOfFSMActor extends FunctionDependency {

    /** Construct a FunctionDependencyOfFSMActor in the given container.
     *  @param container The container has this FunctionDependency object.
     */
    public FunctionDependencyOfFSMActor(Actor container) {
        super(container);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Construct an abstract port graph from a detailed port graph by 
        excluding the internal ports. For FSM actor, the abstract
        graph is the same with the detailed graph.
     */
    protected void _constructAbstractPortGraph() {
        //DirectedGraph abstractPortGraph = getAbstractPortGraph();
        //abstractPortGraph = getDetailedPortGraph();
        _abstractPortGraph = _detailedPortGraph;
    }
    
    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.
     */
    protected void _constructDetailedPortGraph() {
        // local variables setup
        FSMActor container = (FSMActor)getContainer();
        //DirectedGraph detailedPortGraph = getDetailedPortGraph();
        DirectedGraph detailedPortGraph = _detailedPortGraph;

        // Construct a fully connected ports graph
        // that the inputs and outputs are all directly dependent.

        Iterator inputs = container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            Iterator outputs = container.outputPortList().listIterator();
            while (outputs.hasNext()) {
                // connected the inputs and outputs
                detailedPortGraph.addEdge(inputPort, outputs.next());
            }
        }
    }
}

/* Conversion of Ptolemy II models to generic weighted graphs.

 Copyright (c) 2001-2003 The University of Maryland. All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

// In theory, this class could be in ptolemy.graph, but that would
// set up a two way dependency between ptolemy.actor and ptolemy.graph.

package ptolemy.actor;

import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Graph;
import ptolemy.graph.Node;
import ptolemy.graph.Edge;

import java.util.HashMap;
import java.util.Iterator;
import java.lang.String;


///////////////////////////////////////////////////////////////////////
//// GraphReader
/** This class provides methods for converting Ptolemy II models
into generic graph representations. Portions of
this code are based on examples from [1].

<p>
References<br>
[1] J. Davis et al., <em>Heterogeneous
concurrent modeling and design in Java</em>, Technical report,
Electronics Research Laboratory, University of California at Berkeley,
March 2001.

@author Shuvra S. Bhattacharyya, Chia-Jui Hsu
@version $Id$
@since Ptolemy II 2.0
*/
public class GraphReader {

    /** Construct a new graph reader.
     */
    public GraphReader() {
        _actorMap = new HashMap();
    }

    /** Convert the model represented by a CompositeActor into a directed,
     *  weighted graph. Each node of the weighted graph will have as its
     *  weight the Ptolemy II AtomicActor that the node represents,
     *  and each edge will have as its weight the input port associated
     *  with the connection that the edge represents. These conventions
     *  for assigning node and edge weights can be changed in
     *  specialized graph reader classes by overriding the
     *  {@link ptolemy.actor.GraphReader#_computeNodeWeight(AtomicActor)} and
     *  {@link ptolemy.actor.GraphReader#_computeEdgeWeight(IOPort sourcePort, 
     *  IOPort sinkPort)}
     *  methods.
     *  This method will convert low level CompositeActor as a node.
     *  @param compositeActor The composite actor to convert.
     *  @return the directed, weighted graph.
     *  @exception RuntimeException If the deep entity list of the
     *  composite actor contains an entry that is not an AtomicActor.
     */
    public Graph convert(CompositeActor compositeActor) {
        
        // Instantiate an empty graph.
        Graph graph = _initializeGraph(compositeActor);

        // Add all deeply-contained actors to the graph
        Iterator actors = compositeActor.deepEntityList().iterator();
        while (actors.hasNext()) {
            Object entity = actors.next();
            if(entity instanceof AtomicActor || 
                    entity instanceof CompositeActor) {
                Actor actor = (Actor)entity;
                Node newNode = graph.addNodeWeight(_computeNodeWeight(actor));
                _actorMap.put(actor, newNode);
            } else {
                throw new RuntimeException("Unsupported deep entity type: "
                        + entity.getClass().getName()
                        + " (value = " + entity + ")");
            }
        }

        // Convert each connection in the model to a graph edge
        actors = compositeActor.deepEntityList().iterator();
        while (actors.hasNext()) {
            Actor source = (Actor)(actors.next());

            // Connect the current actor to each of its sinks
            Iterator outPorts = source.outputPortList().iterator();
            while (outPorts.hasNext()) {
                IOPort outPort = (IOPort)(outPorts.next());
                Iterator inPorts =
                    outPort.deepConnectedInPortList().iterator();
                while (inPorts.hasNext()) {
                    IOPort inPort = (IOPort)(inPorts.next());
                    Actor sink = (Actor)(inPort.getContainer());
                    if (graph.containsNode((Node)(_actorMap.get(sink)))) {
                        if (_debug) System.out.println("Adding edge from "
                                + source + " to " + sink);

                        graph.addEdge((Node)(_actorMap.get(source)),
                                (Node)(_actorMap.get(sink)),
                                _computeEdgeWeight(outPort, inPort));
                    }
                }
            }
        }

        // Perform global graph transformations.
        _transformTopology(graph);

        // Return the filled-in Graph.
        return graph;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Determine the weight to be assigned to the weighted graph edge that
     *  represents a given connection in a Ptolemy II model.
     *  This method returns the input port as the edge weight.
     *  This method should be overridden by derived
     *  classes that have different edge weighting conventions.
     *  @param sourcePort the output port of the connection associated with
     *  the edge.
     *  @param sinkPort the input port of the connection associated with the
     *  edge
     *  @return the weight of the edge.
     */
    protected Object _computeEdgeWeight(IOPort sourcePort, IOPort sinkPort) {
        return sinkPort;
    }

    /** Determine the weight to be assigned to the weighted graph node that
     *  represents a given actor. This method returns the actor itself as the
     *  weight. This method should be overridden by derived
     *  classes that have different node weighting conventions.
     *  @param actor the actor whose node weight is to be determined.
     *  @return the weight of the node.
     */
    protected Object _computeNodeWeight(Actor actor) {
        return actor;
    }

    /** Instantiate and initialize a graph just prior to filling it
     *  in from a given Ptolemy II model. This is a pre-processing
     *  step to the conversion process that can be specialized
     *  based on the type of graph that is being read (e.g.,
     *  the method can be overridden to allocate a specialized Graph
     *  object). In this
     *  base class, we simply instantiate an empty Graph and return it.
     *  @param compositeActor the Ptolemy II model that will be converted.
     *  @return the empty graph that is to hold the converted model.
     */
    protected Graph _initializeGraph(CompositeActor compositeActor) {
        return new DirectedGraph();
    }

    /** Perform post-processing on the entire graph to complete the
     *  conversion. This method should be overridden by derived classes
     *  to implement specialized conversion aspects that operate
     *  at a global level (i.e., beyond the level of individual nodes and
     *  edges).
     *  @param graph the graph.
     */
    protected void _transformTopology(Graph graph) {        
        if (_debug) {
            System.out.println("A dump of the graph before global "
                    + "transformation:\n" + graph.toString() + "\n");
        }
        
        //write transform strategy here.        
        
        if (_debug) {
            System.out.println("A dump of the graph after global "
                    + "transformation:\n" + graph.toString() + "\n");
        }
    }
    
    /** Set debug mode and let the class display conversion information.
     * @param debug True will turn on debug mode, false will turn off debug
     * mode.
     */
    protected void _setDebug(boolean  debug) {
        _debug = debug;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag for turning local debugging output on and off.
    private static boolean _debug = false;

    // Map from actors to the generic graph nodes that represent them.
    // Keys are instances of AtomicActor, and values are instances of Node.
    private HashMap _actorMap;

}

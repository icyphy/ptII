/* An instance of IODependenceOfAtomicActor is an attribute of an atomic actor 
containing the input-output dependence information.

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
import ptolemy.graph.Edge;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// IODependenceOfAtomicActor
/** An instance of IODependenceOfAtomicActor is an attribute containing
the input-output dependence information of an atomic actor. The attribute is 
constructed in the preinitialize method of the atomic actors. This class
extends IODependence.
<p>
For atomic actors, by default, all the input ports and output ports are
directly dependent. If the atomic actor explicitly declares that some input
and output ports are not directly dependent, use the 
<i>removeDependence(input, output)</i> method to remove the direct dependence 
of a pair, input port and output port. 
<p>
This attribute is not persistent by default, so it will not be exported
into a MoML representation of the model.

@see IODependence
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependenceOfAtomicActor extends IODependence {

    /** Construct an IODependence attribute in the given container 
     *  with the given name. The container argument must not be null, 
     *  or a NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  Resolve the IODependence of input and output ports. Set this
     *  attribute nonpersistent.
     *
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the attribute is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public IODependenceOfAtomicActor(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Remove the dependence between the input and output ports.
     * 
     *  @param inputPort An input port.
     *  @param outputPort An output Port.
     */
    public void removeDependence(IOPort inputPort, IOPort outputPort)
        throws IllegalActionException {
        validate();
        Object[] incidentEdgeArray = 
            _dg.incidentEdges(_dg.node(inputPort)).toArray();
        for (int i = 0; i < incidentEdgeArray.length; i++) {
            Edge edge = (Edge)(incidentEdgeArray[i]);
            if (edge.sink().equals(_dg.node(outputPort))) {
                _dg.removeEdge(edge);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.  
     *  The directed graph is returned.
     */
    protected void _constructDirectedGraph() 
        throws IllegalActionException, NameDuplicationException {

        AtomicActor container = (AtomicActor)getContainer();       

        // clear the nodes and edges in the directed graph
        _dg = new DirectedGraph();
    
        // First, include all the ports as nodes in the graph.
    
        // get all the inputs and outputs of the container
        Iterator inputs = container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            // 'add' replaced with 'addNodeWeight' since the former
            // has been deprecated.  The change here should have no
            // effect since .add had already been defined as a call
            // to .addNodeWeight -winthrop
            _dg.addNodeWeight(inputs.next());
        }
    
        Iterator outputs = container.outputPortList().listIterator();
        while (outputs.hasNext()) {
            _dg.addNodeWeight(outputs.next());
        }
    
        // If the container is an atomic actor, 
        // the IODependence attribute has a default behavior
        // that the inputs and outputs are all directly dependent.
        // If any special properties are necessary, e.g. TimedDelay
        // for DE models, they will be given by the designers.
        // The reason for this is that we do not do code analysis,
        // and we can not tell the details.

        inputs = container.inputPortList().listIterator();
        while (inputs.hasNext()) {
            IOPort inputPort = (IOPort) inputs.next();
            outputs = container.outputPortList().listIterator();
            while (outputs.hasNext()) {
                // connected the inputs and outputs
                _dg.addEdge(inputPort, outputs.next());
            }
        }
        
        // if the atomic actor declares some dependencies do not
        // exist, remove them. 
        // Note: this method calls the removeDependence(input, output)
        // method defined above.
        container.removeDependencies(); 
        //System.out.println("after removing edges:\n " + _dg.toString());
    }
}

/* An instance of IODependenceOfFSMActor is an attribute of a fsm actor 
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

import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// IODependenceOfFSMActor
/** An instance of IODependenceOfFSMActor is an attribute containing
the input-output dependence information of a fsm actor. 
<p>
This attribute is not persistent by default, so it will not be exported
into a MoML representation of the model.

@see IODependence
@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
*/
public class IODependenceOfFSMActor extends IODependence {

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
    public IODependenceOfFSMActor(Entity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Construct a directed graph with the nodes representing input and
     *  output ports, and directed edges representing dependencies.  
     *  The directed graph is returned.
     */
    protected void _constructDirectedGraph() 
        throws IllegalActionException, NameDuplicationException {

        FSMActor container = (FSMActor)getContainer();       

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
    
        // FIXME: In the current implementation, we assume all the outputs
        // are not directly dependent on the inputs.

    }
}

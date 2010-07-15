/* Syntactic Graph for syntactic representations.

Copyright (c) 2010 The Regents of the University of California.
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

package ptolemy.cg.lib;

import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

import ptolemy.cg.lib.SyntacticNode;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////SyntacticGraph

/** Represent ptolemy networks with Syntactic Graphs.
*   Syntactic Graphs represent ptolemy networks in a manner that
*   can be translated to a syntactical representation. In this 
*   representative graph, representative nodes can be organized 
*   as parallel compositions of nodes in series with feedback drawn
*   around the entire graph. 
*   
*   @author Chris Shaver
*   @version $Id$
*   @since
*   @Pt.ProposedRating red (shaver)
*   @Pt.AcceptedRating red 
*/
public class SyntacticGraph extends CompositeEntity {

    /** Create new instance of SyntacticGraph with no container. */
    public SyntacticGraph() {
        super();
        _nodes = new LinkedList<SyntacticNode>();
        _columns = new LinkedList<LinkedList<SyntacticNode>>();
        _representingNodes = new HashMap();
        
        _feedbackRemoved = false;
        _madeBijective = false;
        _canAdd = true;
        _pureCount = 0;
    }

    /** Create new instance of SyntacticGraph in a given workspace.
     *  @param workspace
     */
    public SyntacticGraph(Workspace workspace) {
        super(workspace);
        
        _nodes = new LinkedList<SyntacticNode>();
        _columns = new LinkedList<LinkedList<SyntacticNode>>();
        _representingNodes = new HashMap();
        
        _feedbackRemoved = false;
        _madeBijective = false;
        _canAdd = true;
        _pureCount = 0;
    }

    /** Construct an instance of SyntacticGraph in a given container.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public SyntacticGraph(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _nodes = new LinkedList<SyntacticNode>();
        _columns = new LinkedList<LinkedList<SyntacticNode>>();
        _representingNodes = new HashMap();
        
        _feedbackRemoved = false;
        _madeBijective = false;
        _canAdd = true;
        _pureCount = 0;
    }
    
    /** Add a SyntacticNode to the Graph. 
     *  Nodes are either purely syntactic nodes or nodes that
     *  represent Entities.
     *  @param node Node to add to the Graph.
     *  @throws IllegalActionException on attempts to add nodes
     *  after certain transformations are done to the graph.
     */
    public void addNode(SyntacticNode node) 
        throws IllegalActionException, NameDuplicationException {
        
        // Disallow adding nodes after certain transformations are done.
        if (!_canAdd) {
            throw new IllegalActionException(this, "cannot add more nodes");
        }
        
        // Get connections and reconstruct them in the syntactic graph.
        List<SyntacticPort> inputs = node.getInputs(); 
        for (SyntacticPort port : inputs) {
            // Terminate empty ports syntactically
            if (port.isEmpty()) {
                SyntacticNode interm = new SyntacticNode(this, "term_" + _pureCount + "_" + port.getName());
                ++_pureCount;
                interm.setSyntactic(0, 1);
                
                try {
                    SyntacticPort inport = (SyntacticPort) interm.getOutputs().get(0);
                    connect(inport, port);
                    _nodes.add(interm);
                }
                
                catch (IndexOutOfBoundsException ex) {}
            }
            
            else {
                
                
                
            }
            
        }
        
        // Add node
        _nodes.add(node);
        
        // Map to nodes from represented Entities
        Entity ent = node.getRepresented();
        if (ent != null) {
            _representingNodes.put(ent, node);
        }
    }
    
    /** Determine whether feedback has been removed.
     *  Feedback is transformed into named pairs of initial and
     *  terminal syntactic nodes.
     *  @return Whether feedback has been removed.
     * */
    public boolean isFeedbackRemoved() {
        return _feedbackRemoved;
    }
    
    /** Determine whether the graph has been made completely bijective.
     *  Multiple relations are either removed in the nodes by repeating 
     *  multivalent ports as series of single ports, or removed in the
     *  graph by representing splits and merges as purely syntactic nodes.
     *  @return Whether graph has been made bijective.
     * */
    public boolean isBijective() {
        return _madeBijective;
    }
    
    public String description(String prefix, String suffix) {
        String desc = 
            prefix + "Graph: " + getName() + suffix;
        
        if (_feedbackRemoved) {
            desc += prefix + "Feedback has been removed." + suffix;
        }
        
        if (_madeBijective) {
            desc += prefix + "Graph has been made bijective." + suffix;
        }
            
        desc += prefix + "Nodes: {" + suffix;
        for (SyntacticNode node : _nodes) {
            desc += node.description(prefix + "| | | ", suffix);
        }
        desc += prefix + "}" + suffix;
        
        return desc;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** List of nodes in the Graph. */
    private LinkedList<SyntacticNode> _nodes;
    
    /** Conjunctive columns of nodes. Used for parallel composition */
    private LinkedList<LinkedList<SyntacticNode>> _columns;

    /** Map between Entities and representative SyntaticNodes */
    private HashMap<Entity, SyntacticNode> _representingNodes;
    
    /** Characteristics of Graph. */
    private boolean _feedbackRemoved;
    private boolean _madeBijective;
    private boolean _canAdd;
    
    private int _pureCount;
    
}

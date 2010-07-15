/* Nodes for syntactic graph representations.

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

import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import ptolemy.actor.IOPort;
import ptolemy.cg.lib.SyntacticPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SyntacticNode

/** Represent nodes in the context of SyntacticGraphs.
 *  Existing and generated elements of models are represented
 *  and their ports are represented syntactically as enumerated 
 *  lists of input and output ports. This enumeration is used 
 *  for combinatorial composition in syntaxes with unnamed ports. 
 *  <p>
 *  Inheriting classes should use protected methods for
 *  constructing representation from existing elements. 
 *  <p>
 *  @author Chris Shaver
 *  @version $Id$
 *  @since
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red 
 *
 */
public class SyntacticNode extends ComponentEntity {

    /** Create new instance of SyntacticNode with no connections. */
    public SyntacticNode() {
        super();
        
        _represented = null;
        _isRepresented = false;
        _isRepresentative = true;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _inref = new HashMap();
        _outref = new HashMap();
        _numIns = 0;
        _numOuts = 0;
    }

    /** Create new instance of SyntacticNode with no connections.
     *  @param workspace
     */
    public SyntacticNode(Workspace workspace) {
        super(workspace);
        
        _represented = null;
        _isRepresented = false;
        _isRepresentative = true;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _inref = new HashMap();
        _outref = new HashMap();
        _numIns = 0;
        _numOuts = 0;
    }
    
    /** Construct an entity with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public SyntacticNode(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _represented = null;
        _isRepresented = false;
        _isRepresentative = true;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _inref = new HashMap();
        _outref = new HashMap();
        _numIns = 0;
        _numOuts = 0;
    }
    

    /** Represent an Entity and its ports for use in a SyntacticGraph.
     *  @param entity The Entity to be represented with the SyntacticNode 
     *  @return if the representation is total
     */
    public boolean representEntity(Entity entity)
        throws IllegalActionException, NameDuplicationException {
        
        // Check to make sure node is blank
        if (_isRepresented) {
            throw new IllegalActionException(this, "Already is being used.");
        }
    
        // Represented as an Entity attaching reference.
        _represented = entity;
        _isRepresented = true;
        _isRepresentative = true;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        
        // Is a total representation (does not leave out ports)
        boolean totalr = true;
        
        List<Port> ports = entity.portList();
        for (Port ep : ports) {
            // Only case at the present
            if (ep instanceof IOPort) {
                IOPort ioep = (IOPort)ep;
                int width = ioep.getWidth();
                String epname = ioep.getName();
                
                // If unconnected treat as single port
                // for multiply connected ports make several
                // ports connected in parallel
                if (width < 1) width = 1;
                
                if (ioep.isInput()) {
                    SyntacticPort iport = new SyntacticPort(this, ep, true, "in_ref_" + epname);
                    _inref.put(ep, _numIns);
                    
                    for (int n = 0; n < width; ++n, ++_numIns) _inputs.add(iport);    
                    _isInitial = false;
                }
                
                // If port is both create both
                if (ioep.isOutput()) {
                    SyntacticPort oport = new SyntacticPort(this, ep, false, "out_ref_" + epname);
                    _outref.put(ep, _numOuts);
                    
                    for (int n = 0; n < width; ++n, ++_numOuts) _outputs.add(oport);
                    _isTerminal = false;
                }
            }
            
            else continue;
         
        }
        
        _isIsolated = _isInitial && _isTerminal;
        
        return totalr;
    }
    
    /** Set node as purely syntactic, not representing any Entity.
     *  @param inputs Number of inputs.
     *  @param outputs Number of outputs.
     * */
    public void setSyntactic(int inputs, int outputs)
        throws IllegalActionException, NameDuplicationException {
        _isRepresentative = false;
        _isInitial = inputs == 0;
        _isTerminal = outputs == 0;
        _isIsolated = _isInitial && _isTerminal;
        
        String name = this.getName(); 
        for (int n = 0; n < inputs; ++n, ++_numIns) {
            SyntacticPort port = new SyntacticPort(this, null, true, "in_" + n + name);
            _inputs.add(port);
        }
        
        for (int n = 0; n < outputs; ++n, ++_numOuts) {
            SyntacticPort port = new SyntacticPort(this, null, false, "out_" + n + name);
            _outputs.add(port);
        }
    }
    
    
    /** Get the list of ordered inputs.
     *  @return The list of inputs.   
     */
    public List getInputs() {
        return _inputs;
    }
    
    /** Get the list of ordered outputs.
     *  @return The list of outputs.   
     */
    public List getOutputs() {
        return _outputs;
    }
    
    /** Determine whether node is initial, having no inputs.
     *  @return Whether node is initial
     */
    public boolean isInitial() {
        return _isInitial;
    }
    
    /** Determine whether node is terminal, having no outputs.
     *  @return Whether node is terminal
     */
    public boolean isTerminal() {
        return _isTerminal;
    }
    
    /** Determine whether node is isolated from the network.
     *  @return Whether node is isolated
     */
    public boolean isIsolated() {
        return _isIsolated;
    }
    
    /** Get the Entity syntactically represented by the node.
     *  @return Represented Entity.
     */
    public Entity getRepresented() {
        return _represented;
    }
    
    /** Determine whether node represents an Entity or is purely syntactic.
     *  A purely syntactic node represents a relationship in the Ptolemy 
     *  network as a SyntacticNode that can be expressed in syntax as an
     *  special atomic element rather than as a combinator.  
     *  @return Whether node is a representation.
     */
    public boolean isRepresentative() {
        return _isRepresentative;
    }

    public String description(String prefix, String suffix) {
        String desc = prefix + "Node: " + getName() + " {" + suffix;
        String indent = "....";
        
        if (_isRepresentative && _isRepresented) {
            desc += prefix + indent + "Representing: " + _represented.getName() + suffix;
        }
        else {
            desc += prefix + indent + "Pure syntactic node" + suffix;
        }
        
        desc +=
            prefix + indent + "Initial: " + _isInitial + suffix +
            prefix + indent + "Terminal: " + _isTerminal + suffix +
            prefix + indent + "Isolated: " + _isIsolated + suffix;
        
        desc += prefix + indent + "inputs: {" + suffix;
        for (Port port : _inputs) {
            desc += prefix + indent + "...." + port.getName() + suffix;
        }
        desc += prefix + indent + "}" + suffix;
        
        desc += prefix + indent + "outputs: {" + suffix;
        for (Port port : _outputs) {
            desc += prefix + indent + "...." + port.getName() + suffix;
        }
        desc += prefix + "}" + suffix;
        
        return desc;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Get the SyntacticPort corresponding to a channel of a represented 
     *  input port.
     *  @param port Port represented.
     *  @param offset Offset channel of port, 0 if single port.
     *  @return corresponding SyntacticPort in node.
     * */
    protected SyntacticPort _mapInputPort(Port port, int offset) {
        Integer base = _inref.get(port);
        if (base == null) return null;
        
        SyntacticPort sport = null;
        try {
            sport = _inputs.get(base.intValue() + offset);
        }
        
        catch (IndexOutOfBoundsException ex) {}
        
        return sport;
    }
    
    
    /** Get the SyntacticPort corresponding to a channel of a represented 
     *  output port.
     *  @param port Port represented.
     *  @param offset Offset channel of port, 0 if single port.
     *  @return corresponding SyntacticPort in node.
     * */
    protected SyntacticPort _mapOutputPort(Port port, int offset) {
        Integer base = _outref.get(port);
        if (base == null) return null;
        
        SyntacticPort sport = null;
        try {
            sport = _outputs.get(base.intValue() + offset);
        }
        
        catch (IndexOutOfBoundsException ex) {}
        
        return sport;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    // The following port lists refer to ports in the super.
    // Adding ports has been overridden above to add references 
    // to these structures. 

    /** List of references to ports syntactically marked as inputs. */
    protected LinkedList<SyntacticPort> _inputs;
    
    /** List of references to ports syntactically marked as outputs. */
    protected LinkedList<SyntacticPort> _outputs;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** Map to base port indexes (offsets are used for multiports) */
    private HashMap<Port, Integer> _inref;
    private HashMap<Port, Integer> _outref;
    
    /** Represented Entity */
    private Entity _represented;
    private boolean _isRepresented;
    private boolean _isRepresentative;
    
    /** Characteristics of node. */
    private boolean _isInitial;
    private boolean _isTerminal;
    private boolean _isIsolated;

    /** Internal counts of ports */
    private int _numIns;
    private int _numOuts;
    
}


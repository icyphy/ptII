/* Nodes for syntactic graph representations.

Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.cg.lib.syntactic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
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
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red
 *
 */
public class SyntacticNode extends ComponentEntity implements SyntacticTerm {

    /** Create new instance of SyntacticNode with no connections. */
    public SyntacticNode() {
        super();

        _represented = null;
        _exteriorPort = null;
        _isRepresented = false;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        _nodeType = NodeType.UNKNOWN;
        _visited = false;
        _marked = false;
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _inref = new HashMap();
        _outref = new HashMap();
        _numIns = 0;
        _numOuts = 0;
        _permutation = null;
        _label = "";
    }

    /** Create new instance of SyntacticNode with no connections.
     *  @param workspace Workspace in which to create the node.
     */
    public SyntacticNode(Workspace workspace) {
        super(workspace);

        _represented = null;
        _exteriorPort = null;
        _isRepresented = false;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        _nodeType = NodeType.UNKNOWN;
        _visited = false;
        _marked = false;
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _inref = new HashMap();
        _outref = new HashMap();
        _numIns = 0;
        _numOuts = 0;
        _permutation = null;
        _label = "";
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
        _exteriorPort = null;
        _isRepresented = false;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;
        _nodeType = NodeType.UNKNOWN;
        _visited = false;
        _marked = false;
        _inputs = new LinkedList();
        _outputs = new LinkedList();
        _inref = new HashMap();
        _outref = new HashMap();
        _numIns = 0;
        _numOuts = 0;
        _permutation = null;
        _label = "";
    }

    /** Represent an Entity and its ports for use in a SyntacticGraph.
     *  @param entity The Entity to be represented with the SyntacticNode
     *  @return if the representation is total.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public boolean representEntity(Entity entity)
            throws IllegalActionException, NameDuplicationException {

        // Check to make sure node is blank
        if (_isRepresented) {
            throw new IllegalActionException(this, "Already is being used.");
        }

        // Represented as an Entity attaching reference.
        _represented = entity;
        _exteriorPort = null;
        _isRepresented = true;
        _isInitial = true;
        _isTerminal = true;
        _isIsolated = true;

        _nodeType = NodeType.REPRESENTATIVE;

        // Is a total representation (does not leave out ports)
        boolean totalr = true;

        List<Port> ports = entity.portList();
        for (Port ep : ports) {

            // Only case at the present
            if (ep instanceof IOPort) {
                IOPort ioep = (IOPort) ep;
                int width = ioep.getWidth();
                String epname = ioep.getName();

                // If unconnected treat as single port
                // for multiply connected ports make several
                // ports connected in parallel
                if (width < 1) {
                    width = 1;
                }

                if (ioep.isInput()) {
                    _inref.put(ep, _numIns);

                    for (int n = 0; n < width; ++n, ++_numIns) {
                        SyntacticPort iport = new SyntacticPort(this, ep, true,
                                "in_ref_" + n + "_" + epname);
                        iport.setChannel(n);
                        _inputs.add(iport);
                        StringAttribute cardinal = new StringAttribute(iport,
                                "_cardinal");
                        cardinal.setExpression("WEST");
                    }
                    _isInitial = false;
                }

                // If port is both create both
                if (ioep.isOutput()) {
                    _outref.put(ep, _numOuts);

                    for (int n = 0; n < width; ++n, ++_numOuts) {
                        SyntacticPort oport = new SyntacticPort(this, ep,
                                false, "out_ref_" + n + "_" + epname);
                        oport.setChannel(n);
                        _outputs.add(oport);
                        StringAttribute cardinal = new StringAttribute(oport,
                                "_cardinal");
                        cardinal.setExpression("EAST");
                    }
                    _isTerminal = false;
                }
            } else {
                continue;
            }

        }

        _isIsolated = _isInitial && _isTerminal;
        _attachText("_iconDescription", _representativeIcon);

        return totalr;
    }

    /** Add a port node with a directionality.
     *  Given port is represented by constructed SyntacticPorts
     *  then added to the node in this form. The number of
     *  ports added from the given port is the width of the
     *  given port. Each channel is represented by a SyntacticPort
     *  and pointed back to the given Port.
     *
     *  @param port Port to add
     *  @param isin True if input, False if output.
     *  @return number of SyntacticPorts added (= width).
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public int addPorts(Port port, boolean isin) throws IllegalActionException,
            NameDuplicationException {
        String prefix = isin ? "in_" : "out_";
        List<SyntacticPort> portset = isin ? _inputs : _outputs;
        String cardinality = isin ? "WEST" : "EAST";

        int width = SyntacticPort.portWidth(port);
        int index = portset.size();

        (isin ? _inref : _outref).put(port, index);

        for (int n = index; n < width + index; ++n, ++_numOuts) {
            SyntacticPort rport = new SyntacticPort(this, port, isin, prefix
                    + n);
            rport.setChannel(n);
            portset.add(rport);
            StringAttribute cardinal = new StringAttribute(rport, "_cardinal");
            cardinal.setExpression(cardinality);
        }

        if (isin) {
            _numIns += width;
        } else {
            _numOuts += width;
        }

        return width;
    }

    /** Represent an exterior port with a purely syntactic Node.
     *
     *  @param port The port to represent on the node.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void representExteriorPort(Port port) throws IllegalActionException,
            NameDuplicationException {

        if (port instanceof IOPort) {
            IOPort ioport = (IOPort) port;
            int width = 1;//ioport.getWidthInside();
            _isIsolated = false;
            if (ioport.isInput()) {
                for (int n = 0; n < width; ++n) {
                    SyntacticPort oport = new SyntacticPort(this, ioport,
                            false, "out_" + n + "_external_in");
                    oport.setChannel(n);
                    _outputs.add(oport);
                    _outref.put(port, n);
                    StringAttribute cardinal = new StringAttribute(oport,
                            "_cardinal");
                    cardinal.setExpression("EAST");
                    _attachText("_iconDescription", _inputIcon);
                }
                _exteriorPort = port;
                _isInitial = true;
                _isTerminal = false;
                ++_numOuts;

                _nodeType = NodeType.INPUT;
            } else if (ioport.isOutput()) {
                for (int n = 0; n < width; ++n) {
                    SyntacticPort iport = new SyntacticPort(this, ioport, true,
                            "in_" + n + "_external_out");
                    iport.setChannel(n);
                    _inputs.add(iport);
                    _inref.put(port, n);
                    StringAttribute cardinal = new StringAttribute(iport,
                            "_cardinal");
                    cardinal.setExpression("WEST");
                    _attachText("_iconDescription", _outputIcon);
                }
                _exteriorPort = port;
                _isTerminal = true;
                _isInitial = false;
                ++_numIns;

                _nodeType = NodeType.OUTPUT;
            }
            _isIsolated = _isInitial && _isTerminal;
        }

    }

    /** Set node as purely syntactic, not representing any Entity.
     *  @param inputs Number of inputs.
     *  @param outputs Number of outputs.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     * */
    public void setSyntactic(int inputs, int outputs)
            throws IllegalActionException, NameDuplicationException {
        _isInitial = inputs == 0;
        _isTerminal = outputs == 0;
        _isIsolated = _isInitial && _isTerminal;

        String name = this.getName();
        for (int n = 0; n < inputs; ++n, ++_numIns) {
            SyntacticPort port = new SyntacticPort(this, null, true, "in_" + n
                    + "_" + name);
            _inputs.add(port);
            StringAttribute cardinal = new StringAttribute(port, "_cardinal");
            cardinal.setExpression("WEST");
        }

        for (int n = 0; n < outputs; ++n, ++_numOuts) {
            SyntacticPort port = new SyntacticPort(this, null, false, "out_"
                    + n + "_" + name);
            _outputs.add(port);
            StringAttribute cardinal = new StringAttribute(port, "_cardinal");
            cardinal.setExpression("EAST");
        }
    }

    /** Set node as an identity.
     *
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void setIdentity() throws IllegalActionException,
            NameDuplicationException {
        setSyntactic(1, 1);
        _nodeType = NodeType.IDENTITY;
        _attachText("_iconDescription", _identityIcon);
    }

    /** Set node as a feedback node in specified direction, true being feed out.
     *  @param direction The direction to make the feedback node.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void setFeedback(boolean direction) throws IllegalActionException,
            NameDuplicationException {
        if (direction) {
            setSyntactic(1, 0);
            _attachText("_iconDescription", _sendIcon);
            _nodeType = NodeType.SEND;
        } else {
            setSyntactic(0, 1);
            _attachText("_iconDescription", _returnIcon);
            _nodeType = NodeType.RECEIVE;
        }
    }

    /** Set node as a mediator node in specified direction.
     *  The true direction splits, the false merges.
     *  @param direction The direction to make the mediator node.
     *  @param valence The amount of endpoints on the opposite side.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void setMediator(boolean direction, int valence)
            throws IllegalActionException, NameDuplicationException {
        if (valence < 2) {
            throw new IllegalActionException(this,
                    "Cannot mediate less than 2 valence.");
        }

        if (direction) {
            setSyntactic(1, valence);
            _nodeType = NodeType.SPLIT;
        } else {
            setSyntactic(valence, 1);
            _nodeType = NodeType.MERGE;
        }

        _attachText("_iconDescription", _makeMediatorIcon());
    }

    /** Set node as a initial or terminal node in specified direction, true being out.
     *  @param direction The direction to make the cap node.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void setCap(boolean direction) throws IllegalActionException,
            NameDuplicationException {
        if (direction) {
            setSyntactic(1, 0);
        } else {
            setSyntactic(0, 1);
        }
        _attachText("_iconDescription", _capIcon);
        _nodeType = NodeType.CAP;
    }

    /** Set node as a bijective permutation node with a specified permutation.
     *  @param permutation The permutation to represent as the node.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void setPermutation(int permutation[])
            throws IllegalActionException, NameDuplicationException {
        int plen = permutation.length;
        setSyntactic(plen, plen);
        _attachText("_iconDescription", _makePermutationIcon(permutation));
        _permutation = permutation.clone();
        _nodeType = NodeType.PERMUTATION;
    }

    /** Get the connected node from a given port.
     *  If the graph is not made bijective this gives the first.
     *  @param port The port to look out from.
     *  @return An immediately connected node.
     */
    public SyntacticNode getConnectedNode(SyntacticPort port) {
        SyntacticPort rport = port.getConnectedPort();
        if (rport == null) {
            return null;
        }

        NamedObj rent = rport.getContainer();
        if (!(rent instanceof SyntacticNode)) {
            return null;
        }

        return (SyntacticNode) rent;
    }

    /** Get a unique list of connected nodes from a given list of ports.
     * @param ports The list of ports to look out from.
     * @return A set list of immediately connected nodes.
     */
    public List<SyntacticNode> getConnectedNode(List<SyntacticPort> ports) {
        LinkedList<SyntacticNode> outstream = new LinkedList();
        for (SyntacticPort port : ports) {
            SyntacticNode rnode = getConnectedNode(port);
            if (rnode != null && !outstream.contains(rnode)) {
                outstream.add(rnode);
            }
        }

        return outstream;
    }

    /** Get a list of nodes immediately downstream from outgoing connections.
     *  @return A list of immediately downstream nodes.
     */
    public List<SyntacticNode> getDownstreamNodes() {
        return getConnectedNode(getOutputs());
    }

    /** Get a list of nodes immediately upstream from outgoing connections.
     *  @return A list of immediately upstream nodes.
     */
    public List<SyntacticNode> getUpstreamNodes() {
        return getConnectedNode(getInputs());
    }

    /** Recalculate properties of node. */
    public void remark() {
        _isInitial = _inputs.size() == 0;
        _isTerminal = _outputs.size() == 0;
        _isIsolated = _isInitial && _isTerminal;
    }

    /** Set the visited marker.
     *  This marker is used for algorithms that visit nodes.
     *  @param b State of the visited flag.
     */
    public void setVisited(boolean b) {
        _visited = b;
    }

    /** Set the marked marker.
     *  This marker is used for algorithms that traverse nodes.
     *  @param b State of the marked flag.
     */
    public void setMarked(boolean b) {
        _marked = b;
    }

    /** Set the label used to lexically represent the node.
     *  @param label Label to set for node.
     *  @see #getLabel
     */
    public void setLabel(String label) {
        _label = label;
    }

    /** Set the location of the node in layout.
     *  @param x The x-coordinate
     *  @param y The y-coordinate
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void setLocation(double x, double y) throws IllegalActionException,
            NameDuplicationException {
        Location location = (Location) getAttribute("_location");
        if (location == null) {
            location = new Location(this, "_location");
        }

        if (isPermutation()) {
            int permlen = 1;
            if (_permutation != null) {
                permlen = _permutation.length;
            }
            y = permlen * 35.0;
        }

        double coords[] = { x, y };
        location.setLocation(coords);
    }

    /** Get the vertical offset for visually representing node.
     *  @return vertical offset down from baseline.
     */
    public double getLayoutVerticalSpace() {
        if (isPermutation()) {
            return 0.0;
        }
        if (isIdentity()) {
            return 50.0;
        }
        return 100.0;
    }

    /** Get string identifier (non-unique) for node.
     *  @return identifier string.
     */
    public String getIdentifier() {
        StringBuffer id = new StringBuffer();
        if (_nodeType == NodeType.PERMUTATION) {
            if (_permutation == null) {
                //id = "";
            } else if (_permutation.length == 0) {
                id.append("[]");
            } else {
                id.append("[" + (_permutation[0] + 1));
                for (int n = 1; n < _permutation.length; ++n) {
                    id.append(" " + (_permutation[n] + 1));
                }
                id.append("]");
            }
        } else if (_nodeType == NodeType.SPLIT) {
            id.append("[ < " + _outputs.size() + " ]");
        } else if (_nodeType == NodeType.MERGE) {
            id.append("[ " + _inputs.size() + " > ]");
        } else if (isCap()) {
            id.append("T");
        } else if (isIncoming()) {
            id.append("in");
        } else if (isOutgoing()) {
            id.append("out");
        } else if (isRepresentative()) {
            id.append(_label);
        }

        return id.toString();
    }

    /** Get whether the node has been visited.
     *  @return value of the visited flag.
     */
    public boolean isVisited() {
        return _visited;
    }

    /** Get whether the node has been marked.
     *  @return value of the marked flag.
     */
    public boolean isMarked() {
        return _marked;
    }

    /** Get index represented by the first channel of given port.
     *  @param port The Port represented in the node.
     *  @return base index object or null if not found.
     */
    public Integer outputPortIndex(Port port) {
        Integer index = _outref.get(port);
        return index;
    }

    /** Get index represented by the first channel of given port.
     *  @param port The Port represented in the node.
     *  @return base index object or null if not found.
     */
    public Integer inputPortIndex(Port port) {
        Integer index = _inref.get(port);
        return index;
    }

    /** Get index represented by the first channel of given port.
     *  @param port The Port represented in the node.
     *  @return base index object or null if not found.
     */
    @Override
    public Integer outputIndex(SyntacticPort port) {
        int index = _outputs.indexOf(port);
        return index < 0 ? null : index;
    }

    /** Get index represented by the first channel of given port.
     *  @param port The Port represented in the node.
     *  @return base index object or null if not found.
     */
    @Override
    public Integer inputIndex(SyntacticPort port) {
        int index = _inputs.indexOf(port);
        return index < 0 ? null : index;
    }

    /** Get the list of ordered inputs.
     *  @return The list of inputs.
     */
    @Override
    public List<SyntacticPort> getInputs() {
        return _inputs;
    }

    /** Get the list of ordered outputs.
     *  @return The list of outputs.
     */
    @Override
    public List<SyntacticPort> getOutputs() {
        return _outputs;
    }

    /** Get the number of inputs to the node.
     *  For input nodes this returns 1 for the input it
     *  represents to the downstream node.
     *  @return number of inputs.
     */
    @Override
    public int sizeInputs() {
        if (_nodeType.isIncoming()) {
            return 1;
        } else {
            return _inputs == null ? 0 : _inputs.size();
        }
    }

    /** Get the number of outputs to the node.
     *  For output nodes this returns 1 for the output it
     *  represents to the upstream node.
     *  @return number of outputs.
     */
    @Override
    public int sizeOutputs() {
        if (_nodeType.isOutgoing()) {
            return 1;
        } else {
            return _outputs == null ? 0 : _outputs.size();
        }
    }

    /** Get the Syntactic rank of the node.
     *  @return rank of the node.
     */
    @Override
    public SyntacticRank rank() {
        return new SyntacticRank(sizeOutputs(), 0, sizeInputs(), 0);
    }

    /** Generate a string signifying the boundary of a node.
     *  The boundary is the number of inputs and outputs.
     *  @return string representing boundary of node.
     */
    public String boundaryCode() {
        return "" + sizeInputs() + " --> " + sizeOutputs();
    }

    /** Get the first input or return null.
     *  @return first input or null.
     */
    public SyntacticPort getFirstInput() {
        if (_inputs.size() == 0) {
            return null;
        }
        return _inputs.getFirst();
    }

    /** Get the first output or return null.
     *  @return first output or null.
     */
    public SyntacticPort getFirstOutput() {
        if (_outputs.size() == 0) {
            return null;
        }
        return _outputs.getFirst();
    }

    /** Get the relative order of the node by types.
     *  ... which order?
     *  @return the order of the node.
     */
    @Override
    public int getOrder() {
        return _nodeType.getOrder();
    }

    /** Determine whether node represents feedback.
     *  @return Whether node is a feedback node
     */
    public boolean isFeedback() {
        return _nodeType.isFeedback();
    }

    /** Determine whether node represents an identity.
     *  @return Whether node is identity
     */
    public boolean isIdentity() {
        return _nodeType == NodeType.IDENTITY;
    }

    /** Determine whether node represents an exterior port.
     *  @return Whether node is exterior
     */
    public boolean isExterior() {
        return _nodeType.isExterior();
    }

    /** Determine whether node represents a mediator.
     *  @return Whether node is a mediator
     */
    public boolean isMediator() {
        return _nodeType.isMediator();
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

    /** Determine whether node is a cap.
     *  @return Whether node is a cap
     */
    public boolean isCap() {
        return _nodeType == NodeType.CAP;
    }

    /** Determine whether node is a permutation.
     *  @return Whether node is a permutation
     */
    public boolean isPermutation() {
        return _nodeType == NodeType.PERMUTATION;
    }

    /** Determine whether node is incoming to the expression.
     *  @return Whether node is incoming
     */
    public boolean isIncoming() {
        return _nodeType.isIncoming();
    }

    /** Determine whether node is outgoing to the expression.
     *  @return Whether node is outgoing
     */
    public boolean isOutgoing() {
        return _nodeType.isOutgoing();
    }

    /** Determine whether or not code should be generated.
     *  If false, code should be suppressed such is the case
     *  for identity nodes.
     *  @return whether code should be generated.
     */
    @Override
    public boolean hasCode() {
        return !isIdentity();
    }

    /** Get the Entity syntactically represented by the node.
     *  @return Represented Entity.
     */
    public Entity getRepresented() {
        return _represented;
    }

    /** Get the exterior Port syntactically represented by the node.
     *  @return Represented Port.
     */
    public Port getExteriorPort() {
        return _exteriorPort;
    }

    /** Get the NodeType.
     *  @return Represented NodeType.
     */
    public NodeType getNodeType() {
        return _nodeType;
    }

    /** Get the label of the node.
     *  @return label of the node.
     *  @see #setLabel
     */
    public String getLabel() {
        return _label;
    }

    /** Determine whether node represents an Entity or is purely syntactic.
     *  A purely syntactic node represents a relationship in the Ptolemy
     *  network as a SyntacticNode that can be expressed in syntax as an
     *  special atomic element rather than as a combinator.
     *  @return Whether node is a representation.
     */
    public boolean isRepresentative() {
        return _nodeType == NodeType.REPRESENTATIVE;
    }

    /** Generate code for node.
     *  @return code representation of node.
     */
    @Override
    public String generateCode() {
        return getIdentifier();
    };

    /** Print description of Node.
     * @param prefix Line prefix for embedding description
     * @param suffix Line suffix for embedding description
     * @return Description of Node.
     */
    public String description(String prefix, String suffix) {
        StringBuffer desc = new StringBuffer(prefix + "Node: " + getName()
                + " {" + suffix);
        String indent = "....";

        if (isRepresentative() && _isRepresented) {
            desc.append(prefix + indent + "Representing: "
                    + _represented.getName() + suffix);
        } else {
            desc.append(prefix + indent + "Pure syntactic node" + suffix);
        }

        desc.append(prefix + indent + "Initial: " + _isInitial + suffix
                + prefix + indent + "Terminal: " + _isTerminal + suffix
                + prefix + indent + "Isolated: " + _isIsolated + suffix);

        desc.append(prefix + indent + "inputs: {" + suffix);
        for (Port port : _inputs) {
            desc.append(prefix + indent + "...." + port.getName() + suffix);
        }
        desc.append(prefix + indent + "}" + suffix);

        desc.append(prefix + indent + "outputs: {" + suffix);
        for (Port port : _outputs) {
            desc.append(prefix + indent + "...." + port.getName() + suffix);
        }
        desc.append(prefix + "}" + suffix);

        return desc.toString();
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
        if (base == null) {
            return null;
        }

        SyntacticPort sport = null;
        try {
            sport = _inputs.get(base.intValue() + offset);
        }

        catch (IndexOutOfBoundsException ex) {
        }

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
        if (base == null) {
            return null;
        }

        SyntacticPort sport = null;
        try {
            sport = _outputs.get(base.intValue() + offset);
        }

        catch (IndexOutOfBoundsException ex) {
        }

        return sport;
    }

    /** Generate icon for permutation node.
     *  @param permutation Array of permutation values.
     *  @return svg code for generated icon.
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    protected String _makePermutationIcon(int permutation[])
            throws IllegalActionException, NameDuplicationException {

        int permlen = permutation.length;
        int pheight = permlen * 35;
        StringBuffer svgIcon = new StringBuffer("<svg>\n"
                + "<rect x=\"-20\" y=\"" + -pheight
                + "\" width=\"40\" height=\"" + 2 * pheight
                + "\" style=\"fill:red\"/>\n");

        for (int n = 0; n < permlen; ++n) {
            int m = permutation[n];
            svgIcon.append("<line x1=\"-18\" y1=\"" + (n * 70 + 35 - pheight)
                    + "\" x2=\"18\" y2=\"" + (m * 70 + 35 - pheight)
                    + "\" />\n");
        }

        svgIcon.append("</svg>\n");

        // Try and set position of ports
        Token t = new DoubleToken(6.0);

        for (SyntacticPort port : _inputs) {
            new Variable(port, "_portSpread", t);
        }
        for (SyntacticPort port : _outputs) {
            new Variable(port, "_portSpread", t);
        }

        return svgIcon.toString();
    }

    /** Generate icon for mediator node.
     *  @return svg code for generated icon.
     */
    protected String _makeMediatorIcon() {
        StringBuffer svgIcon = new StringBuffer("<svg>\n");
        int ins = _inputs.size();
        int outs = _outputs.size();

        if (ins == 1 && outs > 1) {
            svgIcon.append("<polygon points=\"20,-20 -20,0 20,20\" style=\"fill:red\"/>\n");
            double vinc = 30.0 / (outs - 1);
            for (int n = 0; n < outs; ++n) {
                svgIcon.append("<line x1=\"-18\" y1=\"0\" x2=\"18\" y2=\""
                        + (n * vinc - 15.0) + "\" />\n");
            }
        } else if (outs == 1 && ins > 1) {
            svgIcon.append("<polygon points=\"-10,-20 10,0 -10,20\" style=\"fill:red\"/>\n");
            double vinc = 30.0 / (ins - 1);
            for (int n = 0; n < ins; ++n) {
                svgIcon.append("<line x1=\"-18\" y1=\"" + (n * vinc - 15.0)
                        + "\" x2=\"18\" y2=\"0\" />\n");
            }
        } else {
            svgIcon.append("<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:green\"/>\n");
        }

        svgIcon.append("</svg>\n");
        return svgIcon.toString();
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
    ////                         private variables                 ////

    /** Map to base input port indexes (offsets are used for multiports) */
    private HashMap<Port, Integer> _inref;

    /** Map to base output port indexes (offsets are used for multiports) */
    private HashMap<Port, Integer> _outref;

    /** Represented Entity */
    private Entity _represented;

    /** Represented exterior Port */
    private Port _exteriorPort;

    /** true if representational and pointing to an Entity */
    private boolean _isRepresented;

    /** true if node is initial */
    private boolean _isInitial;

    /** true if node is terminal */
    private boolean _isTerminal;

    /** true if node is isolated */
    private boolean _isIsolated;

    /** Type of node */
    private NodeType _nodeType;

    /** Node label */
    private String _label;

    /** Internal markers for iteration. */
    private boolean _visited;

    /** Internal marker for iteration. */
    private boolean _marked;

    /** Internal count of input ports */
    private int _numIns;

    /** Internal count of output ports */
    private int _numOuts;

    /** Permutation if node is a permutation node. */
    private int _permutation[];

    /** Icon for representative nodes */
    private static String _representativeIcon = "<svg>\n"
            + "<rect x=\"-30\" y=\"-20\" width=\"60\" height=\"40\" style=\"fill:white\"/>\n"
            + "<polygon points=\"-10,-10 -10,10 10,10 10,-10\" style=\"fill:orange\"/>\n"
            + "</svg>\n";

    /** Icon for identity nodes */
    private static String _identityIcon = "<svg>\n"
            + "<rect x=\"-30\" y=\"-10\" width=\"60\" height=\"20\" style=\"fill:none\"/>\n"
            + "<polygon points=\"-10,-10 20,0 -10,10\" style=\"fill:blue\"/>\n"
            + "</svg>\n";

    /** Icon for input nodes */
    private static String _inputIcon = "<svg>\n"
            + "<polygon points=\"-10,-20 10,0 -10,20\" style=\"fill:red\"/>\n"
            + "</svg>\n";

    /** Icon for output nodes */
    private static String _outputIcon = "<svg>\n"
            + "<polygon points=\"10,-20 -10,0 10,20\" style=\"fill:red\"/>\n"
            + "</svg>\n";

    /** Icon for feedback return nodes */
    private static String _returnIcon = "<svg>\n"
            + "<polygon points=\"-10,-20 10,0 -10,20\" style=\"fill:gray\"/>\n"
            + "</svg>\n";

    /** Icon for feedback send nodes */
    private static String _sendIcon = "<svg>\n"
            + "<polygon points=\"10,-20 -10,0 10,20\" style=\"fill:gray\"/>\n"
            + "</svg>\n";

    /** Icon for cap nodes */
    private static String _capIcon = "<svg>\n"
            + "<rect x=\"-20\" y=\"-20\" width=\"40\" height=\"40\" style=\"fill:green\"/>\n"
            + "</svg>\n";

    ///////////////////////////////////////////////////////////////////
    ////                         internal classes                  ////

    /** Internal enum representing the types of nodes and
     *  how they are ordered, compared, and categorized.
     */
    public enum NodeType {
        /** Permutation node. */
        PERMUTATION(0),

        /** Representative node. */
        REPRESENTATIVE(1),

        /** Split mediator. */
        SPLIT(3),

        /** Merge mediator. */
        MERGE(3),

        /** Cap node. */
        CAP(4),

        /** Feedback send. */
        SEND(5),

        /** Feedback receive. */
        RECEIVE(5),

        /** Input node. */
        INPUT(6),

        /** Output node. */
        OUTPUT(6),

        /** Identity node. */
        IDENTITY(7),

        /** Other node type. */
        UNKNOWN(100);

        /** Constant sort order of the node. */
        private final int order;

        /** Make a new node type enum with a given order.
         *  @param sort order of node.
         */
        private NodeType(int order) {
            this.order = order;
        }

        /** Decide whether type is unknown.
         *  @return whether type is unknown.
         */
        public boolean isUnknown() {
            return this == UNKNOWN;
        }

        /** Decide whether type is an input or output.
         *  @return whether type is input or output.
         */
        public boolean isExterior() {
            return this == INPUT || this == OUTPUT;
        }

        /** Decide whether type is feedback in or out.
         *  @return whether type is feedback in or out.
         */
        public boolean isFeedback() {
            return this == SEND || this == RECEIVE;
        }

        /** Decide whether type is a purely syntactic one.
         *  @return whether type is a purely syntactic one.
         */
        public boolean isPure() {
            return this == IDENTITY || this == CAP || this == PERMUTATION
                    || isMediator() || isFeedback();
        }

        /** Decide whether type is a mediator.
         *  @return whether type is a mediator.
         */
        public boolean isMediator() {
            return this == SPLIT || this == MERGE;
        }

        /** Decide whether type is incoming.
         *  True if an input or feedback input.
         *  @return whether type is incoming.
         */
        public boolean isIncoming() {
            return this == INPUT || this == RECEIVE;
        }

        /** Decide whether type is outgoing.
         *  True if an output or feedback output.
         *  @return whether type is outgoing.
         */
        public boolean isOutgoing() {
            return this == OUTPUT || this == SEND;
        }

        /** Sort order of type.
         *  @return sort order of type.
         */
        public int getOrder() {
            return order;
        }

    };

}

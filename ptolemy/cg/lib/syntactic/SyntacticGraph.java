/* Syntactic Graph for syntactic representations.

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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
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
 *   @since Ptolemy II 10.0
 *   @Pt.ProposedRating red (shaver)
 *   @Pt.AcceptedRating red
 */
public class SyntacticGraph extends CompositeEntity {

    /** Create new instance of SyntacticGraph with no container. */
    public SyntacticGraph() {
        super();
        _nodes = new LinkedList<SyntacticNode>();
        _mediators = new LinkedList<SyntacticNode>();
        _feedIns = new LinkedList<SyntacticNode>();
        _feedOuts = new LinkedList<SyntacticNode>();
        _representors = new LinkedList();
        _series = new SyntacticSeries();
        _contraction = null;
        _representedModel = null;
        _exprName = new SyntacticName();

        _representingNodes = new HashMap();
        _exinNodes = new HashMap();
        _exoutNodes = new HashMap();
        _labelsToNodes = new HashMap();
        _nodesToLabels = new HashMap();
        _representees = new HashMap();

        _feedbackRemoved = false;
        _madeBijective = false;
        _canAdd = true;
        _pureCount = 0;
        _labelCount = 0;
    }

    /** Create new instance of SyntacticGraph in a given workspace.
     *  @param workspace Workspace in which to create the syntactic graph.
     */
    public SyntacticGraph(Workspace workspace) {
        super(workspace);
        _nodes = new LinkedList<SyntacticNode>();
        _mediators = new LinkedList<SyntacticNode>();
        _feedIns = new LinkedList<SyntacticNode>();
        _feedOuts = new LinkedList<SyntacticNode>();
        _series = new SyntacticSeries();
        _contraction = null;
        _representedModel = null;
        _exprName = new SyntacticName();
        _representors = new LinkedList();

        _representingNodes = new HashMap();
        _exinNodes = new HashMap();
        _exoutNodes = new HashMap();
        _labelsToNodes = new HashMap();
        _nodesToLabels = new HashMap();
        _representees = new HashMap();

        _feedbackRemoved = false;
        _madeBijective = false;
        _canAdd = true;
        _pureCount = 0;
        _labelCount = 0;
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
        _mediators = new LinkedList<SyntacticNode>();
        _feedIns = new LinkedList<SyntacticNode>();
        _feedOuts = new LinkedList<SyntacticNode>();
        _series = new SyntacticSeries();
        _representors = new LinkedList();
        _contraction = null;
        _representedModel = null;
        _exprName = new SyntacticName();

        _representingNodes = new HashMap();
        _exinNodes = new HashMap();
        _exoutNodes = new HashMap();
        _labelsToNodes = new HashMap();
        _nodesToLabels = new HashMap();
        _representees = new HashMap();

        _feedbackRemoved = false;
        _madeBijective = false;
        _canAdd = true;
        _pureCount = 0;
        _labelCount = 0;
    }

    /** Build the syntactic graph from the given model.
     *  Model is constructed by going through the total set of
     *  steps necessary to establish a representative graph:
     *  <pre>
     *      -- add entities to graph,
     *      -- make graph bijective,
     *      -- remove feedback loops,
     *      -- order graph topologically,
     *      -- insert permutation operators,
     *      -- add layout information to display
     *         generated graph.
     *  </pre>
     * @param model Model to represent with graph.
     * @return whether graph was successively produced.
     * @exception IllegalActionException If thrown while adding a node, making Bijective, removing feedback, setting the structure, inserting permutations or laying out the graph
     * @exception NameDuplicationException If thrown while adding a node, making Bijective, removing feedback, setting the structure, inserting permutations or laying out the graph
     */
    public boolean build(CompositeEntity model) throws IllegalActionException,
            NameDuplicationException {
        _representedModel = model;

        List<ComponentEntity> ents = model.entityList();
        for (ComponentEntity ent : ents) {
            addNode(ent);
            /*
            System.out.print("Entity: " + ent.getName() + "\n");
            List<Port> ports = (List<Port>)ent.portList();
            for (Port port : ports) {
                SyntacticPort.IOType type = SyntacticPort.portType(port, model);
                Integer width = SyntacticPort.portWidth(port);
                System.out.print("    Port: " + port.getName() + "(" + type + ": " + width + ") => \n");
                List<Port> rports = (List<Port>)port.connectedPortList();
                for (Port rport : rports) {
                    SyntacticPort.IOType rtype = SyntacticPort.portType(rport, model);
                    Integer rwidth = SyntacticPort.portWidth(rport);
                    System.out.print("      -> " + rport.getName() + "(" + rtype + ": " + rwidth + ")\n");
                }

            }
             */
        }

        makeBijective();
        removeFeedback();
        structure();
        insertPermutations();
        layoutGraph();

        return true;
    }

    /** Add entity to the syntactic graph wrapping with syntactic node.
     *  Beginning of an alternative. Not currently used.
     *
     *  @param entity Entity to add to the graph.
     *  @exception IllegalActionException If thrown while creating a new SyntacticNode or adding an entity.
     *  @exception NameDuplicationException If thrown while creating a new SyntacticNode or adding an entity.
     */
    public void addNode2(Entity entity) throws IllegalActionException,
            NameDuplicationException {
        int repcount = _representors.size();
        SyntacticNode node = new SyntacticNode(this, "rep_" + repcount);
        node.representEntity(entity);
        _representors.add(node);
        _representees.put(entity, node);
    }

    /** Add entity to the syntactic graph wrapping with syntactic node.
     *
     *  @param entity Entity to add to the graph.
     *  @exception IllegalActionException If thrown while creating a new SyntacticNode or adding an entity.
     *  @exception NameDuplicationException If thrown while creating a new SyntacticNode or adding an entity.
     */
    public void addNode(Entity entity) throws IllegalActionException,
    NameDuplicationException {

        int repcount = _representors.size();
        SyntacticNode node = new SyntacticNode(this, "rep_" + repcount);
        node.representEntity(entity);
        _representors.add(node);
        _representees.put(entity, node);
        addNode(node);
    }

    /** Add a SyntacticNode to the Graph.
     *  Nodes are either purely syntactic nodes or nodes that
     *  represent Entities.
     *
     *  @param node Node to add to the Graph.
     *  @exception IllegalActionException on attempts to add nodes
     *  after certain transformations are done to the graph.
     *  @exception NameDuplicationException if duplicate names are used.
     */
    public void addNode(SyntacticNode node) throws IllegalActionException,
    NameDuplicationException {

        // Disallow adding nodes after certain transformations are done.
        if (!_canAdd) {
            throw new IllegalActionException(this, "cannot add more nodes");
        }

        // Get connections and reconstruct them in the syntactic graph.
        List<SyntacticPort> inputs = node.getInputs();
        for (SyntacticPort port : inputs) {
            // Terminate empty ports syntactically
            if (port.isEmpty()) {
                SyntacticNode interm = new SyntacticNode(this, "term_"
                        + _pureCount);
                ++_pureCount;
                interm.setCap(false);

                SyntacticPort inport = interm.getFirstOutput();
                if (inport != null) {
                    _makeConnection(inport, port);
                }
                _nodes.add(interm);
                addLabelFromNode(interm);
            }

            // Handle connections on ports
            else if (port.isRepresentative()) {
                Port rport = port.getRepresentedPort();
                int rdex = port.getChannel();
                List<Port> connectedPorts = rport.connectedPortList();

                // For each connected port find out whether it connects to the given
                // channel specified by the representation and which channel it is coming from.
                for (Port cport : connectedPorts) {
                    NamedObj centity_ob = cport.getContainer();
                    if (!(centity_ob instanceof Entity)) {
                        continue;
                    }

                    Entity centity = (Entity) centity_ob;
                    if (centity instanceof CompositeEntity
                            && centity == _representedModel) {
                        if (!(cport instanceof IOPort && ((IOPort) cport)
                                .isInput())) {
                            continue;
                        }

                        SyntacticNode exin = new SyntacticNode(this, "exin_"
                                + _pureCount);
                        ++_pureCount;
                        exin.representExteriorPort(cport);
                        _addExteriorNode(exin);

                        SyntacticPort outport = exin.getFirstOutput();
                        if (outport != null) {
                            _makeConnection(outport, port);
                        }
                        continue;
                    }

                    if (!_representingNodes.containsKey(centity)) {
                        continue;
                    }
                    SyntacticNode cnode = _representingNodes.get(centity);

                    Integer portBase = cnode.outputPortIndex(cport);
                    if (portBase == null) {
                        continue;
                    }

                    // cport --> rport established as relationship, try casting both to IOPorts
                    Integer cdex = _getOutputChannel(cport, rport, rdex);
                    if (cdex == null) {
                        continue;
                    }

                    // Found cport to connect, get representative port
                    List<SyntacticPort> rep_cports = cnode.getOutputs();
                    if (rep_cports.size() > portBase + cdex) {
                        _makeConnection(rep_cports.get(portBase + cdex), port);
                    }
                }

            }

        }

        // Get connections and reconstruct them in the syntactic graph.
        List<SyntacticPort> outputs = node.getOutputs();
        for (SyntacticPort port : outputs) {

            // Terminate empty ports syntactically
            if (port.isEmpty()) {
                SyntacticNode outterm = new SyntacticNode(this, "init_"
                        + _pureCount);
                ++_pureCount;
                outterm.setCap(true);

                SyntacticPort outport = outterm.getFirstInput();
                if (outport != null) {
                    _makeConnection(port, outport);
                }
                _nodes.add(outterm);
                addLabelFromNode(outterm);
            }

            // Handle connections on ports
            else if (port.isRepresentative()) {
                Port rport = port.getRepresentedPort();
                int rdex = port.getChannel();
                List<Port> connectedPorts = rport.connectedPortList();

                // For each connected port find out whether it connects to the given
                // channel specified by the representation and which channel it is coming from.
                for (Port cport : connectedPorts) {
                    NamedObj centity_ob = cport.getContainer();
                    if (!(centity_ob instanceof Entity)) {
                        continue;
                    }

                    Entity centity = (Entity) centity_ob;
                    if (centity instanceof CompositeEntity
                            && centity == _representedModel) {
                        if (!(cport instanceof IOPort && ((IOPort) cport)
                                .isOutput())) {
                            continue;
                        }

                        SyntacticNode exout = new SyntacticNode(this, "exout_"
                                + _pureCount);
                        ++_pureCount;
                        exout.representExteriorPort(cport);
                        _addExteriorNode(exout);

                        SyntacticPort inport = exout.getFirstInput();
                        if (inport != null) {
                            _makeConnection(port, inport);
                        }
                        continue;
                    }

                    if (!_representingNodes.containsKey(centity)) {
                        continue;
                    }
                    SyntacticNode cnode = _representingNodes.get(centity);

                    Integer portBase = cnode.inputPortIndex(cport);
                    if (portBase == null) {
                        continue;
                    }

                    // cport --> rport established as relationship, try casting both to IOPorts
                    Integer cdex = _getInputChannel(rport, rdex, cport);
                    if (cdex == null) {
                        continue;
                    }

                    // Found cport to connect, get representative port
                    List<SyntacticPort> rep_cports = cnode.getInputs();
                    if (rep_cports.size() > portBase + cdex) {
                        _makeConnection(port, rep_cports.get(portBase + cdex));
                    }
                }

            }

        }

        _addRepresentativeNode(node);
    }

    /** Make SyntacticGraph bijective by adding pure nodes.
     *  Pure nodes are added to mediate multiply connected nodes.
     *
     *  @exception IllegalActionException If thrown while creating a
     *  SyntacticNode, setting a Mediator, removing a connection or
     *  adding a connection.
     *  @exception NameDuplicationException If thrown while creating a
     *  SyntacticNode, setting a Mediator, removing a connection or
     *  adding a connection.
     */
    public void makeBijective() throws IllegalActionException,
            NameDuplicationException {
        if (_nodes == null) {
            System.out.print("Node-list _nodes.\n");
        }

        for (SyntacticNode node : _nodes) {
            if (node == null) {
                continue;
            }
            for (SyntacticPort port : node.getInputs()) {
                LinkedList<Port> r_outs = new LinkedList(
                        port.connectedPortList());

                // Find multiply connected ports
                int r_outn = r_outs.size();
                if (r_outn <= 1) {
                    continue;
                } else {
                    //System.out.print("Multiple node with " + r_outn + " connections.\n");
                    //continue;
                }

                SyntacticNode mediator = new SyntacticNode(this, "med_"
                        + _pureCount + "_" + port.getName());
                ++_pureCount;
                mediator.setMediator(false, r_outn);

                try {
                    _removeConnection(port);

                    // Connect individual ports
                    for (int n = 0; n < r_outn; ++n) {
                        SyntacticPort inport = mediator.getInputs().get(n);
                        SyntacticPort outport = (SyntacticPort) r_outs.get(n);

                        // Clear port if only connection left
                        if (port.connectedPortList().size() <= 1) {
                            _removeConnection(outport);
                        }

                        _makeConnection(outport, inport);
                    }

                    // Reconnect port from new syntactic node
                    SyntacticPort outport = mediator.getOutputs().get(0);
                    _makeConnection(outport, port);

                    _mediators.add(mediator);
                    addLabelFromNode(mediator);
                }

                catch (IndexOutOfBoundsException ex) {
                }
            }

            for (SyntacticPort port : node.getOutputs()) {
                LinkedList<Port> r_ins = new LinkedList(
                        port.connectedPortList());

                // Find multiply connected ports
                int r_inn = r_ins.size();
                if (r_inn <= 1) {
                    continue;
                } else {
                    //System.out.print("Multiple node with " + r_inn + " connections.\n");
                    //continue;
                }

                SyntacticNode mediator = new SyntacticNode(this, "med_"
                        + _pureCount + "_" + port.getName());
                ++_pureCount;
                mediator.setMediator(true, r_inn);

                try {
                    _removeConnection(port);

                    // Connect individual ports
                    for (int n = 0; n < r_inn; ++n) {
                        SyntacticPort outport = mediator.getOutputs().get(n);
                        SyntacticPort inport = (SyntacticPort) r_ins.get(n);

                        // Clear port if only connection left
                        if (port.connectedPortList().size() <= 1) {
                            _removeConnection(inport);
                        }

                        _makeConnection(inport, outport);
                    }

                    // Reconnect port from new syntactic node
                    SyntacticPort inport = mediator.getInputs().get(0);
                    _makeConnection(inport, port);

                    _mediators.add(mediator);
                    addLabelFromNode(mediator);
                }

                catch (IndexOutOfBoundsException ex) {
                }
            }
        }

        _madeBijective = true;

        // add all mediators
        _nodes.addAll(_mediators);
    }

    /** Remove feedback from graph and draw connections out to periphery.
     *  This can only be done when the graph is bijective, though this
     *  might still work with more constraints given a multiply connected
     *  graph.
     *
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void removeFeedback() throws IllegalActionException,
    NameDuplicationException {
        Collection<SyntacticNode> rootSet = _getRootSet();
        if (rootSet.size() == 0) {
            // Here I should preload a node at random?
            System.out
            .print("The graph is compact. A root node is being chosen.\n");
            if (_nodes.isEmpty()) {
                return;
            }

            // Loading the first node in the set as the root.
            rootSet.add(_nodes.getFirst());
        }

        _clearVisited();
        _clearMarked();

        Stack<SyntacticNode> depth = new Stack();
        for (SyntacticNode node : rootSet) {
            depth.push(node);
            while (!depth.isEmpty()) {
                SyntacticNode tnode = depth.peek();
                if (tnode.isVisited()) {
                    tnode.setMarked(false);
                    depth.pop();
                } else {
                    tnode.setMarked(true);
                    for (SyntacticPort tport : tnode.getOutputs()) {
                        SyntacticPort rport = tport.getConnectedPort();
                        if (rport == null) {
                            continue;
                        }

                        SyntacticNode rnode = tnode.getConnectedNode(tport);
                        if (rnode == null) {
                            continue;
                        }

                        if (rnode.isMarked()) {
                            // Feedback between tport -> rport

                            SyntacticNode outFb = new SyntacticNode(this,
                                    "fb_out_" + _pureCount);
                            SyntacticNode inFb = new SyntacticNode(this,
                                    "fb_in_" + _pureCount);
                            _pureCount += 2;

                            outFb.setFeedback(true);
                            inFb.setFeedback(false);

                            try {
                                SyntacticPort outPort = outFb.getInputs()
                                        .get(0);
                                SyntacticPort inPort = inFb.getOutputs().get(0);

                                _removeConnection(tport);
                                _removeConnection(rport);

                                _makeConnection(inPort, rport);
                                _makeConnection(tport, outPort);
                            }

                            catch (IndexOutOfBoundsException ex) {
                            }

                            outFb.setVisited(true);
                            inFb.setVisited(true);

                            _feedIns.add(inFb);
                            _nodes.add(inFb);
                            addLabelFromNode(inFb);

                            _feedOuts.add(outFb);
                            _nodes.add(outFb);
                            addLabelFromNode(outFb);

                            //System.out.print("Found feedback \'" +
                            //        tnode.getName() + "::" + tport.getName() + " -> " +
                            //        rnode.getName() + "::" + rport.getName() + "\'\n");
                            continue;
                        }

                        if (rnode.isVisited()) {
                            continue;
                        }

                        // rnode unvisited
                        depth.push(rnode);
                    }

                    tnode.setVisited(true);
                }
            }
        }

        _feedbackRemoved = true;

    }

    /** Structure the graph into columns.
     *  This transformation imposes a partial order on the acyclic graph.
     *
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void structure() throws IllegalActionException,
    NameDuplicationException {

        if (!(_feedbackRemoved && _madeBijective)) {
            System.out
            .print("Feedback must be removed and bijection should be established.\n");
            return;
        }

        Collection<SyntacticNode> rootSet = _getRootSet();
        if (rootSet.isEmpty()) {
            System.out.print("The graph is without boundaries.\n");
            return;
        }

        _clearVisited();
        _series.clear();

        boolean nodesLeft = true;
        SyntacticColumn current = new SyntacticColumn();
        for (SyntacticNode node : rootSet) {
            node.setVisited(true);
            current.add(node);
        }

        current.sort();
        _series.add(current);
        while (nodesLeft) {
            boolean isTerminalColumn = true;
            boolean hasTerminals = false;

            SyntacticColumn column = new SyntacticColumn();
            for (SyntacticPort port : current.getOutputs()) {
                SyntacticPort rport = port.getConnectedPort();
                if (rport == null) {
                    continue;
                }

                SyntacticNode rnode = rport.getNode();
                if (rnode == null || rnode.isVisited()) {
                    continue;
                }

                if (current.doesFollow(rnode)) {
                    rnode.setVisited(true);
                    column.add(rnode);

                    if (!rnode.isTerminal()) {
                        isTerminalColumn = false;
                    } else {
                        hasTerminals = true;
                    }
                } else {
                    SyntacticNode identity = new SyntacticNode(this, "id_"
                            + _pureCount);
                    ++_pureCount;

                    identity.setIdentity();
                    identity.setVisited(true);

                    try {
                        _removeConnection(port);
                        _removeConnection(rport);

                        SyntacticPort idIn = identity.getInputs().get(0);
                        _makeConnection(port, idIn);

                        SyntacticPort idOut = identity.getOutputs().get(0);
                        _makeConnection(idOut, rport);
                    }

                    catch (IndexOutOfBoundsException ex) {
                    }

                    identity.setVisited(true);
                    column.add(identity);

                    isTerminalColumn = false;
                }
            }

            if (column.isEmpty()) {
                nodesLeft = false;
            } else {
                if (hasTerminals && !isTerminalColumn) {
                    LinkedList<SyntacticNode> pushout = new LinkedList();
                    LinkedList<SyntacticNode> removal = new LinkedList();
                    for (SyntacticTerm term : column) {
                        if (!(term instanceof SyntacticNode)) {
                            continue;
                        }

                        SyntacticNode node = (SyntacticNode) term;
                        if (!node.isTerminal()) {
                            continue;
                        }

                        for (SyntacticPort port : node.getInputs()) {
                            SyntacticPort rport = port.getConnectedPort();
                            if (rport == null) {
                                continue;
                            }

                            SyntacticNode identity = new SyntacticNode(this,
                                    "id_" + _pureCount);
                            ++_pureCount;

                            identity.setIdentity();
                            identity.setVisited(true);
                            port.unlinkAll();

                            try {
                                _removeConnection(port);
                                _removeConnection(rport);

                                SyntacticPort idIn = identity.getInputs()
                                        .get(0);
                                _makeConnection(rport, idIn);

                                SyntacticPort idOut = identity.getOutputs()
                                        .get(0);
                                _makeConnection(idOut, port);
                            }

                            catch (IndexOutOfBoundsException ex) {
                            }

                            identity.setVisited(true);
                            pushout.add(identity);

                            node.setVisited(false);
                            removal.add(node);
                        }
                    }

                    column.removeAll(removal);
                    column.addAll(pushout);
                }

                column.sort();
                _series.add(column);
                current = column;
            }
        }

    }

    /** Insert permutation objects between columns.
     *
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void insertPermutations() throws IllegalActionException,
    NameDuplicationException {
        if (_series.size() < 2) {
            return;
        }

        ListIterator<SyntacticTerm> colIt = _series.listIterator();
        SyntacticTerm scol = colIt.next();
        while (colIt.hasNext()) {
            SyntacticColumn permcol = new SyntacticColumn();
            colIt.add(permcol);

            SyntacticTerm rcol = colIt.next();
            List<SyntacticPort> sports = scol.getOutputs();
            List<SyntacticPort> rports = rcol.getInputs();

            boolean isordered = true;
            int nperm = sports.size();
            int permutation[] = new int[nperm];
            for (int n = 0; n < nperm; ++n) {
                SyntacticPort sport = sports.get(n);
                SyntacticPort rport = sport.getConnectedPort();
                if (rport == null) {
                    continue;
                }

                Integer rdex = rcol.inputIndex(rport);
                if (rdex == null) {
                    continue;
                }

                permutation[n] = rdex;
                if (n != rdex) {
                    isordered = false;
                }
            }

            if (!isordered) {
                SyntacticNode permnode = new SyntacticNode(this, "perm_"
                        + _pureCount);
                ++_pureCount;
                permnode.setPermutation(permutation);

                List<SyntacticPort> iports = permnode.getInputs();
                List<SyntacticPort> oports = permnode.getOutputs();
                int osize = oports.size();
                if (osize != iports.size()) {
                    return;
                }

                for (int n = 0; n < nperm; ++n) {
                    int rdex = permutation[n];
                    if (rdex >= osize) {
                        continue;
                    }

                    SyntacticPort sport = sports.get(n);
                    SyntacticPort rport = rports.get(rdex);
                    SyntacticPort iport = permnode.getInputs().get(n);
                    SyntacticPort oport = permnode.getOutputs().get(rdex);

                    _removeConnection(sport);
                    _removeConnection(rport);
                    _makeConnection(sport, iport);
                    _makeConnection(oport, rport);
                }

                permcol.add(permnode);
                _addExteriorNode(permnode);
            }
            scol = rcol;
        }

        _exprName.bind(_series);
        _exprName.setName("Expression");
    }

    /** Layout graph for display in columns.
     *
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public void layoutGraph() throws IllegalActionException,
    NameDuplicationException {
        double colpos = 10.0, coldepth = 10.0;
        for (SyntacticTerm termIt : _series) {
            coldepth = 10.0;
            if (!(termIt instanceof SyntacticColumn)) {
                continue;
            }

            SyntacticColumn colIt = (SyntacticColumn) termIt;
            if (colIt.isEmpty()) {
                continue;
            }

            for (SyntacticTerm term : colIt) {
                if (!(term instanceof SyntacticNode)) {
                    continue;
                }

                SyntacticNode node = (SyntacticNode) term;
                node.setLocation(colpos, coldepth);
                coldepth += node.getLayoutVerticalSpace();

                node.setDisplayName(node.getIdentifier());
            }

            colpos += 180.0;
        }
    }

    /** Generate code for model represented by graph.
     *
     *  @return code generated from syntactic graph.
     */
    public String generateCode() {
        StringBuffer code = new StringBuffer();
        for (SyntacticNode node : _nodes) {
            if (node.isRepresentative()) {
                code.append("" + getLabelFromNode(node) + " = \t"
                        + node.getRepresented().getName() + "\n");
            }
        }
        code.append("\n");

        int nfeeds = _feedIns.size();

        SyntacticTerm expression = _series;
        if (nfeeds != 0) {
            _contraction = new SyntacticContraction(nfeeds);
            _contraction.setKernel(_series);
            expression = _contraction;
        }

        _exprName.bind(expression);
        _exprName.setName("Expr_1");

        return code.toString() + _exprName.generateDefinitionCode() + "\n";
    }

    /** Determine whether feedback has been removed.
     *  Feedback is transformed into named pairs of initial and
     *  terminal syntactic nodes.
     *  @return Whether feedback has been removed.
     */
    public boolean isFeedbackRemoved() {
        return _feedbackRemoved;
    }

    /** Determine whether the graph has been made completely bijective.
     *  Multiple relations are either removed in the nodes by repeating
     *  multivalent ports as series of single ports, or removed in the
     *  graph by representing splits and merges as purely syntactic nodes.
     *  @return Whether graph has been made bijective.
     */
    public boolean isBijective() {
        return _madeBijective;
    }

    /** Print description of Syntactic Graph.
     * @param prefix Line prefix for embedding description
     * @param suffix Line suffix for embedding description
     * @return Description of Syntactic Graph.
     */
    public String description(String prefix, String suffix) {
        String desc = prefix + "Graph: " + getName() + suffix;

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

        desc += prefix + "Exterior Ins: {" + suffix;
        for (SyntacticNode node : _exinNodes.values()) {
            desc += node.description(prefix + "| | | ", suffix);
        }
        desc += prefix + "}" + suffix;

        desc += prefix + "Exterior Outs: {" + suffix;
        for (SyntacticNode node : _exoutNodes.values()) {
            desc += node.description(prefix + "| | | ", suffix);
        }
        desc += prefix + "}" + suffix;

        desc += prefix + suffix;
        desc += prefix + "Columns ::::" + suffix;
        for (SyntacticTerm cterm : _series) {
            if (!(cterm instanceof SyntacticColumn)) {
                continue;
            }

            SyntacticColumn column = (SyntacticColumn) cterm;
            desc += prefix + prefix + "Column ::" + suffix;
            for (SyntacticTerm term : column) {
                if (!(term instanceof SyntacticNode)) {
                    continue;
                }

                SyntacticNode node = (SyntacticNode) term;
                desc += node.description(prefix + prefix + "......", suffix);
            }
        }

        return desc;
    }

    /** Get the node associated with a given label or null.
     *  @param label The label given for a node.
     *  @return The node associated with the given label or null.
     */
    public SyntacticNode getNodeFromLabel(String label) {
        if (!_labelsToNodes.containsKey(label)) {
            return null;
        }
        return _labelsToNodes.get(label);
    }

    /** Get the label associated with a give node.
     *  @param node The node give for a label.
     *  @return The label associated with the given node or null.
     */
    public String getLabelFromNode(SyntacticNode node) {
        if (!_nodesToLabels.containsKey(node)) {
            return null;
        }
        return _nodesToLabels.get(node);
    }

    /** Add a unique label to given node or return its current label.
     *  @param node The node to give a label to.
     *  @return the label given to the node or already attached to it.
     */
    public String addLabelFromNode(SyntacticNode node) {
        if (_nodesToLabels.containsKey(node)) {
            return _nodesToLabels.get(node);
        } else {
            String prefix = "U";
            if (node.isPermutation()) {
                prefix = "P";
            } else if (node.isCap()) {
                prefix = "C";
            } else if (node.isExterior() && node.isInitial()) {
                prefix = "I";
            } else if (node.isExterior() && node.isTerminal()) {
                prefix = "O";
            } else if (node.isFeedback() && node.isInitial()) {
                prefix = "Re";
            } else if (node.isFeedback() && node.isTerminal()) {
                prefix = "Sn";
            } else if (node.isIdentity()) {
                prefix = "_";
            } else if (node.isRepresentative()) {
                prefix = "E";
            } else {
                prefix = "M";
            }

            String label = prefix + _labelCount;
            _nodesToLabels.put(node, label);
            _labelsToNodes.put(label, node);
            node.setLabel(label);
            ++_labelCount;

            return label;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         static methods                    ////

    /** Join a list of strings with a given infix.
     *  @param stringList List of strings.
     *  @param infix Infix to join strings with.
     *  @return String containing joined strings.
     */
    public static String stringJoin(List<String> stringList, String infix) {
        StringBuffer results = new StringBuffer();
        ListIterator<String> strings = stringList.listIterator();
        if (strings.hasNext()) {
            results.append(strings.next());
            while (strings.hasNext()) {
                results.append(infix + strings.next());
            }
        }
        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add representative SyntacticNode to graph and associated maps.
     *  If the node is not representative the node will not be added to
     *  the map between entities and nodes.
     *
     *  @param node The representative node to add.
     */
    protected void _addRepresentativeNode(SyntacticNode node) {
        // Add node
        _nodes.add(node);
        addLabelFromNode(node);

        // Map to nodes from represented Entities
        Entity ent = node.getRepresented();
        if (ent != null) {
            _representingNodes.put(ent, node);
        }
    }

    /** Add node that represents an exterior port.
     *  If the node is does not have an exterior port it will not be
     *  added to the map between ports and nodes.
     *
     *  @param node The representative node to add.
     */
    protected void _addExteriorNode(SyntacticNode node) {
        // Add node
        _nodes.add(node);
        addLabelFromNode(node);
    }

    /** Make connection between SyntacticPorts in Syntactic Nodes.
     *
     * @param out The output port to connect.
     * @param in The input port to connect.
     * @exception IllegalActionException
     */
    protected void _makeConnection(SyntacticPort out, SyntacticPort in)
            throws IllegalActionException {
        connect(out, in);
        System.out.print("" + out.getContainer().getName() + "::"
                + out.getName() + " => " + in.getContainer().getName() + "::"
                + in.getName() + "\n");
    }

    /** Remove connection from SyntacticPorts in Syntactic Nodes.
     *
     *  @param port The port to disconnect.
     *  @exception IllegalActionException
     */
    protected void _removeConnection(SyntacticPort port)
            throws IllegalActionException {
        port.unlinkAll();
    }

    /** Get the input channel connected to in a given port from a given output port and channel.
     *  If no connection is found null is returned.
     *
     *  @param oport The output port sending on ochan.
     *  @param ochan The output channel for port oport.
     *  @param iport The input port whose channel is to be determined.
     *  @return input channel on iport or null.
     *  @exception IllegalActionException
     */
    protected Integer _getInputChannel(Port oport, int ochan, Port iport)
            throws IllegalActionException {
        if (!(oport instanceof IOPort && iport instanceof IOPort)) {
            return null;
        }

        Receiver[][] orecvs = ((IOPort) oport).getRemoteReceivers();
        Receiver[][] irecvs = ((IOPort) iport).getReceivers();

        if (ochan >= orecvs.length) {
            return null;
        }
        Receiver[] orecv = orecvs[ochan];
        Receiver foundr = null;

        for (Receiver r : orecv) {
            if (r.getContainer() == iport) {
                foundr = r;
                break;
            }
        }

        if (foundr == null) {
            return null;
        }

        Integer ichan = null;
        outer: for (int chan = 0; chan < irecvs.length; ++chan) {
            for (Receiver r : irecvs[chan]) {
                if (r == foundr) {
                    ichan = chan;
                    break outer;
                }
            }
        }

        return ichan;
    }

    /** Get the output channel connected to in a given port from a given input port and channel.
     *  If no connection is found null is returned.
     *
     *  @param oport The output port sending on ochan.
     *  @param iport The input port whose channel is to be determined.
     *  @param ichan The input channel for port iport.
     *  @return output channel on oport or null.
     *  @exception IllegalActionException
     */
    protected Integer _getOutputChannel(Port oport, Port iport, int ichan)
            throws IllegalActionException {
        if (!(oport instanceof IOPort && iport instanceof IOPort)) {
            return null;
        }

        Receiver[][] orecvs = ((IOPort) oport).getRemoteReceivers();
        Receiver[][] irecvs = ((IOPort) iport).getReceivers();

        if (ichan >= irecvs.length) {
            return null;
        }
        Receiver[] irecv = irecvs[ichan];
        Receiver foundr = null;
        Integer ochan = null;

        outer: for (int chan = 0; chan < orecvs.length; ++chan) {
            for (Receiver r : orecvs[chan]) {
                if (r.getContainer() == iport) {
                    foundr = r;
                    ochan = chan;
                    break outer;
                }
            }
        }

        if (foundr == null) {
            return null;
        }

        boolean matchr = false;
        for (Receiver r : irecv) {
            if (r == foundr) {
                matchr = true;
                break;
            }
        }

        if (!matchr) {
            return null;
        }

        return ochan;
    }

    /** Get the set of initial nodes as a Collection.
     *
     *  @return A Collection containing the initial nodes in the graph.
     */
    protected Collection<SyntacticNode> _getRootSet() {
        Collection<SyntacticNode> rootSet = new LinkedList();
        for (SyntacticNode node : _nodes) {
            if (node.isInitial()) {
                rootSet.add(node);
            }
        }

        return rootSet;
    }

    /** Clear all the visited flags in the nodes.
     *  This is used for visiting algorithms.
     */
    protected void _clearVisited() {
        for (SyntacticNode node : _nodes) {
            node.setVisited(false);
        }
    }

    /** Clear all the visited flags in the nodes.
     *  This is used for visiting algorithms.
     */
    protected void _clearMarked() {
        for (SyntacticNode node : _nodes) {
            node.setMarked(false);
        }
    }

    /** Get list of input ports for model.
     *  @return List of input ports.
     */
    protected LinkedList<SyntacticNode> _getInputs() {
        LinkedList<SyntacticNode> innodes = new LinkedList();
        if (_series.size() < 1) {
            return innodes;
        }

        SyntacticTerm sterm = _series.getFirst();
        if (!(sterm instanceof SyntacticColumn)) {
            return innodes;
        }

        for (SyntacticTerm term : (SyntacticColumn) sterm) {
            if (!(term instanceof SyntacticNode)) {
                continue;
            }

            SyntacticNode node = (SyntacticNode) term;
            if (node.isIncoming()) {
                innodes.add(node);
            }
        }

        return innodes;
    }

    /** Get list of output ports for model.
     *  @return List of output ports.
     */
    protected LinkedList<SyntacticNode> _getOutputs() {
        LinkedList<SyntacticNode> outnodes = new LinkedList();
        if (_series.size() < 1) {
            return outnodes;
        }

        SyntacticTerm sterm = _series.getLast();
        if (!(sterm instanceof SyntacticColumn)) {
            return outnodes;
        }

        for (SyntacticTerm term : (SyntacticColumn) sterm) {
            if (!(term instanceof SyntacticNode)) {
                continue;
            }

            SyntacticNode node = (SyntacticNode) term;
            if (node.isOutgoing()) {
                outnodes.add(node);
            }
        }

        return outnodes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private LinkedList<SyntacticNode> _representors;
    private HashMap<Entity, SyntacticNode> _representees;
    private CompositeEntity _representedModel;

    /** List of nodes in the Graph. */
    private LinkedList<SyntacticNode> _nodes;

    /** List of mediator nodes in the Graph. */
    private LinkedList<SyntacticNode> _mediators;

    /** List of feedback inputs. */
    private LinkedList<SyntacticNode> _feedIns;

    /** List of feedback output. */
    private LinkedList<SyntacticNode> _feedOuts;

    /** Series of parallel groups representing the acyclic component
     *  of the Syntactic Graph.
     */
    private SyntacticSeries _series;

    /** Contraction term forming a feedback loop of the acyclic component
     *  if feedback exists in the model. */
    private SyntacticContraction _contraction;

    /** Expression name bound to expression representing the Syntactic Graph. */
    private SyntacticName _exprName;

    /** Map between Entities and representative SyntaticNodes. */
    private HashMap<Entity, SyntacticNode> _representingNodes;

    /** Map between Composite input Ports and representing nodes. */
    private HashMap<Port, SyntacticNode> _exinNodes;

    /** Map between Composite output Ports and representing nodes. */
    private HashMap<Port, SyntacticNode> _exoutNodes;

    /** Map set between labels and nodes. */
    private HashMap<String, SyntacticNode> _labelsToNodes;

    /** Map set between nodes and labels. */
    private HashMap<SyntacticNode, String> _nodesToLabels;

    /** Number of labels (used for uniqueness). */
    private int _labelCount;

    /** True if feedback has been removed. */
    private boolean _feedbackRemoved;

    /** True if graph has been made bijective. */
    private boolean _madeBijective;

    /** True if nodes can still be added (preprocessing). */
    private boolean _canAdd;

    /** Count of non-representational nodes. */
    private int _pureCount;

}

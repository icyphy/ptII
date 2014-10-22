/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.graph.layout;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import diva.graph.GraphModel;

/**
 * A simple layout which places nodes on a grid using a cost function.
 * It performs the following simple-minded algorithm:
 *
 * <ol>
 *
 * <li> defines a grid based on the number of nodes in the graph and
 *   randomly assigns the nodes to vertices on the grid.
 *
 * <li> randomly swaps nodes on the grid and picks a good settling
 *  point based on a cost function which favors short edges over
 *  long ones, straight edges over diagonal ones, and generally
 *  tries not to put edges through nodes.
 *
 * <li> distorts the grid based on the relative sizes of the nodes,
 *   and finally places the nodes according to this distortion.  *
 *
 * </ol>
 *
 * This class is implemented as a large template method, so it should
 * be relatively easy to extend or modify.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class GridAnnealingLayout extends AbstractGlobalLayout {
    /**
     * The penalty for each unit of height in an edge.
     */
    private static final double HEIGHT_FACTOR = 1;

    /**
     * The penalty for each unit of width in an edge.
     */
    private static final double WIDTH_FACTOR = 1;

    /**
     * The penalty for elbow-shaped edges, i.e ones
     * which have height and width.
     */
    private static final double ELBOW_PENALTY = 10;

    /**
     * The penalty for overlaps with other edges in the graph.
     */
    private static final double EDGE_OVERLAP_PENALTY = 30;

    /**
     * The penalty for tees, where a node lands ON another
     * edge that is not connected to it.
     */
    private static final double TEE_PENALTY = 30;

    /**
     * The penalty for crossings with other edges in the graph.
     */
    private static final double CROSSING_PENALTY = 10;

    /**
     * The random number generator used in choosing which
     * nodes to swap.
     */
    protected Random _random = new Random(System.currentTimeMillis());

    /**
     * The original graph that is passed in by the user on
     * which the layout is applied.
     */
    protected Object _graph;

    /**
     * The grid width.
     */
    protected int _gw;

    /**
     * The grid height.
     */
    protected int _gh;

    /**
     * The current grid configuration as the algorithm progresses.
     */
    protected Object[][] _grid;

    /**
     * The best grid configuration so far as the algorithm progresses.
     */
    protected Object[][] _minGrid;

    /**
     * The relative cost of the best grid configuration so far as the
     * algorithm progresses.
     */
    protected double _minCost;

    /**
     * A mapping from nodes to their corresponding logical grid
     * positions, stored as integer arrays of length 2.
     */
    protected HashMap _map;

    /**
     * A sparseness measure for the layout.
     *
     * @see #setSparseness(double)
     */
    protected double _sparseness = 1.0;

    /**
     * The number of iterations to cool over.
     */
    protected int _numIters = 5;

    /**
     * The number of moves per iteration.
     */
    protected int _numMoves = 10;

    /**
     * The cooling constant.
     */
    protected double _cool = .95;

    public GridAnnealingLayout(LayoutTarget target) {
        super(target);
    }

    /**
     * Perform simulated annealing using the cost function.
     */
    private void anneal() {
        double curCost = _minCost = 0;
        double prob = 1.0;
        snapMin();

        for (int i = 0; i < _numIters; i++) {
            for (int j = 0; j < _numMoves; j++) {
                prob *= _cool;

                int x1 = (int) (_random.nextDouble() * _gw);
                int y1 = (int) (_random.nextDouble() * _gh);
                int x2 = (int) (_random.nextDouble() * _gw);
                int y2 = (int) (_random.nextDouble() * _gh);
                Object node1 = _grid[x1][y1];
                Object node2 = _grid[x2][y2];

                if (node1 != node2) {
                    double startCost = nodeCost(node1) + nodeCost(node2);

                    if (node1 == null) {
                        setXY(node2, x1, y1);
                    } else if (node2 == null) {
                        setXY(node1, x2, y2);
                    } else {
                        swap(node1, node2);
                    }

                    double endCost = nodeCost(node1) + nodeCost(node2);
                    double deltaCost = endCost - startCost;
                    curCost += deltaCost;

                    //   debug(">>> DELTA: " + deltaCost);
                    if (curCost < _minCost) {
                        _minCost = curCost;
                        snapMin();
                    }

                    // see if we should put the nodes back
                    // in their previous positions
                    if (deltaCost > 0 && _random.nextDouble() > prob) {
                        curCost -= deltaCost;

                        if (node1 == null) {
                            setXY(node2, x2, y2);
                        } else if (node2 == null) {
                            setXY(node1, x1, y1);
                        } else {
                            swap(node1, node2);
                        }
                    }
                }
            }
        }

        //  debug(" MIN_COST = " + _minCost + " ======================");
    }

    /**
     * Assert the given condition and throw a runtime exception with
     * the given string if the assertion fails.
     */
    private final void ASSERT(boolean b, String err) throws RuntimeException {
        if (!b) {
            throw new RuntimeException(err);
        }
    }

    /**
     * Assign layout positions to the nodes in the graph based on the
     * logical grid positions determined in the annealing process.
     */
    private void assignLayout() {
        Rectangle2D dim = getLayoutTarget().getViewport(_graph);

        //debug("DIM: " + dim);
        //debug("gw=" + _gw);
        //debug("gh=" + _gh);
        double[] placeX = new double[_gw];

        for (int x = 0; x < _gw; x++) {
            placeX[x] = (0.5 + x) * dim.getWidth() / _gw + dim.getX();
        }

        double[] placeY = new double[_gh];

        for (int y = 0; y < _gh; y++) {
            placeY[y] = (0.5 + y) * dim.getHeight() / _gh + dim.getY();
        }

        for (int i = 0; i < _gw; i++) {
            for (int j = 0; j < _gh; j++) {
                ASSERT(_minGrid != null, "Null min grid!");

                Object node = _minGrid[i][j];

                if (node != null) {
                    double x;
                    double y;
                    x = placeX[i];
                    y = placeY[j];
                    LayoutUtilities.placeNoReroute(getLayoutTarget(), node, x,
                            y);

                    //debug("Assigning: " + x + ", " + y + " to node " + n);
                }
            }
        }

        LayoutUtilities.routeVisibleEdges(_graph, getLayoutTarget());
    }

    /**
     * Cleanup the data structures used in the layout so that they are
     * empty next time the layout is called.
     */
    private void cleanupStructures() {
        _map = null;
        _grid = null;
        _minGrid = null;
        _minCost = Double.MAX_VALUE;
        _gw = -1;
        _gh = -1;
    }

    /**
     * Debugging output.
     */
    //private void debug(String s) {
    //    System.err.println(s);
    //}
    /**
     * Return the absolute cost of an individual edge.  By default the
     * cost function is:
     *
     * <pre>
     *  EDGE_COST(e) = [
     *        HEIGHT(e) +
     *        WIDTH(e) +
     *        ELBOW_PENALTY(e) +
     *        EDGE_OVERLAP_PENALTY * num_overlap(e) +
     *        CROSSING_PENALTY * num_crossing(e)
     *  ]
     * </pre>
     */
    protected double edgeCost(Object edge) {
        GraphModel model = getLayoutTarget().getGraphModel();
        Object tail = model.getTail(edge);
        Object head = model.getHead(edge);

        head = _getParentInGraph(head);
        tail = _getParentInGraph(tail);

        if (head == null || tail == null) {
            return 0;
        }

        int[] ptail = getXY(tail);
        int[] phead = getXY(head);
        double heightCost = HEIGHT_FACTOR * Math.abs(phead[1] - ptail[1]);
        double widthCost = WIDTH_FACTOR * Math.abs(phead[0] - ptail[0]);
        double elbowCost = heightCost == 0 || widthCost == 0 ? 0
                : ELBOW_PENALTY;
        double overlapCost = numOverlaps(edge, _graph) * EDGE_OVERLAP_PENALTY;
        double crossingCost = numCrossings(edge, _graph) * CROSSING_PENALTY;
        return heightCost + widthCost + elbowCost + overlapCost + crossingCost;
    }

    /**
     * @see #setCoolingFactor(double)
     */
    public double getCoolingFactor() {
        return _cool;
    }

    /**
     * @see #setIterationCount(int)
     */
    public int getIterationCount(int cnt) {
        return _numIters;
    }

    /**
     * @see #setMoveCount(int)
     */
    public int getMoveCount(int cnt) {
        return _numIters;
    }

    /**
     * Return the sparseness value of this layout; the default value
     * is 1.0.
     *
     * @see #setSparseness(double)
     */
    public double getSparseness() {
        return _sparseness;
    }

    /**
     * Return the logical X, Y positions of the given
     * node as an integer array of length 2.
     */
    protected int[] getXY(Object node) {
        return (int[]) _map.get(node);
    }

    /**
     * Initialize the grid and randomly assign nodes to vertices of
     * the grid.  The grid initialization is based on the aspect ratio
     * of the viewport in which the layout is being performed.  In
     * particular the following algorithm is used:
     *
     * <pre>
     *      GH = H/W * sqrt(N) * SPARSENESS
     * </pre>
     *
     * Where H and W are the height and width of the viewport, N is
     * the number of nodes in the graph, and SPARSENESS is some
     * measure of the sparseness of the layout.  A SPARSENESS of 1
     * will mean that the graph is tightly packed, and the packing
     * amount decreases linearly with the SPARSENESS value.
     *
     * @see #setSparseness(double)
     */
    protected void initGrid() {
        GraphModel model = getLayoutTarget().getGraphModel();
        int nodeCount = model.getNodeCount(_graph);

        if (nodeCount > 0) {
            Rectangle2D dim = getLayoutTarget().getViewport(_graph);
            double aspect = dim.getHeight() / dim.getWidth();
            double gh = Math.sqrt(nodeCount * aspect) * _sparseness;

            //debug("aspect=" + aspect);
            //debug("gh=" + gh);
            // Fix the number of vertical nodes.
            _gh = (int) Math.ceil(gh);

            // Infer the number of horizontal nodes.
            _gw = nodeCount / _gh;

            while (_gh * _gw < nodeCount) {
                _gw++;
            }

            _grid = new Object[_gw][_gh];

            Iterator nodes = model.nodes(_graph);

            for (int x = 0; x < _gw; x++) {
                for (int y = 0; y < _gh; y++) {
                    if (!nodes.hasNext()) {
                        break;
                    }

                    setXY(nodes.next(), x, y);
                }
            }
        }
    }

    /**
     * Perform the annealing layout algorithm on the given graph
     * in the context of the given layout target.
     */
    @Override
    public void layout(Object composite) {
        LayoutTarget target = getLayoutTarget();
        _graph = composite;
        _map = new HashMap();

        if (target.getGraphModel().getNodeCount(_graph) > 0) {
            initGrid();

            // Avoid exceptions if the graph is not visible.
            if (_gh == 0 || _gw == 0) {
                return;
            }

            anneal();
            assignLayout();
            cleanupStructures();
        }
    }

    /**
     * Return the absolute cost of an individual node.  By default the
     * cost function is:
     *
     * <pre>
     *  NODE_COST(n) = SUM [ EDGE_COST(n.edge(i)) ] +
     *                 TEE_PENALTY * num_tee(g)
     * </pre>
     */
    protected double nodeCost(Object node) {
        LayoutTarget target = getLayoutTarget();
        GraphModel model = target.getGraphModel();

        if (node == null) {
            return 0;
        }

        int cost = 0;

        for (Iterator i = model.inEdges(node); i.hasNext();) {
            cost += edgeCost(i.next());
        }

        for (Iterator i = model.outEdges(node); i.hasNext();) {
            cost += edgeCost(i.next());
        }

        double teeCost = numTees(_graph) * TEE_PENALTY;
        cost += teeCost;

        return cost;
    }

    /**
     * Return the number of crossings between this edge and other
     * edges in the graph.
     */
    protected final int numCrossings(Object inEdge, Object composite) {
        int num = 0;
        GraphModel model = getLayoutTarget().getGraphModel();
        Object inTail = model.getTail(inEdge);
        Object inHead = model.getHead(inEdge);

        inHead = _getParentInGraph(inHead);
        inTail = _getParentInGraph(inTail);

        if (inHead == null || inTail == null) {
            return 0;
        }

        int[] inTailPt = getXY(inTail);
        int[] inHeadPt = getXY(inHead);

        for (Iterator i = model.nodes(composite); i.hasNext();) {
            Object node = i.next();

            for (Iterator j = model.outEdges(node); j.hasNext();) {
                Object edge = j.next();
                Object tail = model.getTail(edge);
                Object head = model.getHead(edge);

                if (tail == null || head == null || tail == inTail
                        || tail == inHead || head == inTail || head == inHead) {
                    //these cannot cross
                    continue;
                }

                int[] tailPt = getXY(tail);
                int[] headPt = getXY(head);

                if (Line2D.linesIntersect(inTailPt[0], inTailPt[1],
                        inHeadPt[0], inHeadPt[1], tailPt[0], tailPt[1],
                        headPt[0], headPt[1])) {
                    num++;
                }
            }
        }

        //        debug("\tEdge crossings: " + in + ", " + num);
        return num;
    }

    /**
     * Return the number of instances of nodes that are being placed
     * on top of edges that they are not connected to.
     */
    protected final int numTees(Object composite) {
        return 0; //XXX
    }

    /**
     * Return the number of overlaps between this edge and other edges
     * in the graph.  For now, simply test to see if the lines are
     * horizontal or vertical and overlap with each other.
     */
    protected final int numOverlaps(Object inEdge, Object composite) {
        int num = 0;
        GraphModel model = getLayoutTarget().getGraphModel();
        Object inTail = model.getTail(inEdge);
        Object inHead = model.getHead(inEdge);

        inHead = _getParentInGraph(inHead);
        inTail = _getParentInGraph(inTail);

        if (inHead == null || inTail == null) {
            return 0;
        }

        int[] inTailPt = getXY(inTail);
        int[] inHeadPt = getXY(inHead);

        for (int which = 0; which < 2; which++) {
            if (inTailPt[which] == inHeadPt[which]) {
                for (Iterator i = model.nodes(composite); i.hasNext();) {
                    Object node = i.next();

                    for (Iterator j = model.outEdges(node); j.hasNext();) {
                        Object edge = j.next();
                        Object tail = model.getTail(edge);
                        Object head = model.getHead(edge);
                        head = _getParentInGraph(head);
                        tail = _getParentInGraph(tail);

                        if (head != null && tail != null) {
                            int[] tailPt = getXY(tail);
                            int[] headPt = getXY(head);

                            if (tailPt[which] == headPt[which]
                                    && tailPt[which] == inTailPt[which]) {
                                //int other = which + (1 % 2);

                                // test to see if the "other" coordinate is
                                // *between* the two points.
                                num++;
                            }
                        }
                    }
                }

                break;
            }
        }

        //        debug("\tEdge overlaps: " + in + ", " + num);
        return num;
    }

    /**
     * Set the cooling factor to be a value greater than 0 and less
     * than or equal to 1.  The cooling factor determines how quickly
     * the annealing "settles"; the lower the factor, the faster the
     * annealing settles.  The Default value is .95.
     */
    public void setCoolingFactor(double val) {
        if (val <= 0 || val > 1) {
            String err = "Cooling factor must be greater than 0 and less or equal to 1: "
                    + val;
            throw new IllegalArgumentException(err);
        }

        _cool = val;
    }

    /**
     * Set the number of iterations to cool over.  Default value is 100.
     */
    public void setIterationCount(int cnt) {
        _numIters = cnt;
    }

    /**
     * Set the number of moves per iteration.  Default value is 10.
     */
    public void setMoveCount(int cnt) {
        _numIters = cnt;
    }

    /**
     * Set the sparseness of this layout.  A sparseness of 1.0 will
     * mean that the graph is tightly packed, and the packing amount
     * decreases linearly with the SPARSENESS value.
     */
    public void setSparseness(double val) {
        if (val < 1.0) {
            String err = "Illegal sparseness value: " + val;
            throw new IllegalArgumentException(err);
        }

        _sparseness = val;
    }

    /**
     * Set the logical X, Y positions of the given node.
     */
    protected void setXY(Object node, int x, int y) {
        int[] pos = (int[]) _map.get(node);

        if (pos == null) {
            pos = new int[2];
            _map.put(node, pos);
        }

        _grid[x][y] = node;
        pos[0] = x;
        pos[1] = y;
    }

    /**
     * Take a snapshot of the minimum cost arrangement of the nodes,
     * so that we can backtrack to it later.
     */
    private void snapMin() {
        if (_minGrid == null) {
            _minGrid = new Object[_gw][_gh];
        }

        for (int x = 0; x < _gw; x++) {
            for (int y = 0; y < _gh; y++) {
                _minGrid[x][y] = _grid[x][y];
            }
        }
    }

    /**
     * Swap the two nodes' grid positions.
     */
    private void swap(Object node1, Object node2) {
        //        debug("SWAP: " + node1 + ", " + node2);
        int xtmp;

        //        debug("SWAP: " + node1 + ", " + node2);
        int ytmp;
        int[] xy1 = getXY(node1);
        int[] xy2 = getXY(node2);
        xtmp = xy1[0];
        ytmp = xy1[1];
        setXY(node1, xy2[0], xy2[1]);
        setXY(node2, xtmp, ytmp);
    }

    // Unfortunately, the head and/or tail of the edge may not
    // be directly contained in the graph.  In this case, we need to
    // figure out which of their parents IS in the graph and calculate the
    // cost of that instead.
    private Object _getParentInGraph(Object node) {
        GraphModel model = getLayoutTarget().getGraphModel();

        while (node != null && !model.containsNode(_graph, node)) {
            Object parent = model.getParent(node);

            if (model.isNode(parent)) {
                node = parent;
            } else {
                node = null;
            }
        }

        return node;
    }
}

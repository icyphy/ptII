/*
 Copyright (c) 1998-2001 The Regents of the University of California
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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import diva.graph.GraphModel;
import diva.graph.GraphUtilities;
import diva.graph.basic.BasicGraphModel;
import diva.util.ArrayIterator;

/**
 * A level-based  layout algorithm originally implemented
 * by Ulfar Erlingsson at Cornell/RPI and modified to fit into
 * this system.<p>
 *
 * The algorithm is structured in the following way:
 *
 * <ul>
 *
 *   <li> Copy the original graph.  The copy will be augmented with
 *        dummy nodes, edges, etc.  The method which performs the copy
 *        can be overridden in a subclass so that the the layout can
 *        be customized.  For example, one might wish to create dummy
 *        edges from a composite node, which represent edges from
 *        subnodes of the composite node to subnodes in other
 *        composite nodes in the graph (e.g.  if you are laying out a
 *        circuit schematic, with composite nodes representing
 *        components and subnodes representing pins on the
 *        components).
 *
 *   <li> Perform the levelizing layout on the graph copy.  This
 *        process consists of several steps:
 *        <ul>
 *            <li> Calculate the levels of the nodes
 *                 in the graph.
 *            <li> Add dummy nodes on edges which span
 *                 multiple levels in the graph.
 *            <li> Perform a sorting on each level
 *                 in the graph based on some cost
 *                 function (not yet implemented).
 *            <li> Assign a position based on the level
 *                 and sorting order of the node.
 *        </ul>
 *
 *   <li> Copy the layout results from the graph copy back into the
 *        original graph, ignoring dummy nodes.  This should also be
 *        overridden if the copy process was overridden (described above).
 *
 * </ul>
 *
 * TODO:
 * <ul>
 *   <li> Break cycles in the graph.
 *   <li> Implement barycentric layout (currently commented out).
 * </ul>
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 * @rating Red
 */
public class LevelLayout extends AbstractGlobalLayout {
    /**
     * Layout the graph in levels from top to bottom.
     *
     * @see #setOrientation(int)
     */
    public static final int VERTICAL = 0;

    /**
     * Layout the graph in levels from left to right.
     *
     * @see #setOrientation(int)
     */
    public static final int HORIZONTAL = 1;

    /**
     * _levelData contains the layout information such as the number
     * of levels in the graph.  Users can make queries from this object.
     */
    //    private LevelData _levelData = null;

    /**
     * Keep track of the orientation; vertical by
     * default.
     */
    protected int _orientation = VERTICAL;

    /**
     * The graph implementation of the graph copy, for adding
     * dummy nodes/edges to the graph copy.
     */
    private BasicGraphModel _local = null;

    /**
     * A flag to determine whether or not we want to randomize the
     * placement of the nodes.
     */
    private boolean _randomizedPlacement = true;

    /**
     * Construct a new levelizing layout with a vertical orientation.
     */
    public LevelLayout(LayoutTarget target) {
        super(target);
        _local = new BasicGraphModel();
    }

    /**
     * Copy the given graph and make the nodes/edges in the copied
     * graph point to the nodes/edges in the original.
     */
    protected Object copyComposite(Object origComposite) {
        GraphModel model = getLayoutTarget().getGraphModel();
        Object copyComposite = _local.createComposite(null);
        HashMap map = new HashMap();

        for (Iterator i = model.nodes(origComposite); i.hasNext(); ) {
            Object origNode = i.next();
            if (getLayoutTarget().isNodeVisible(origNode)) {
                Rectangle2D r = getLayoutTarget().getBounds(origNode);
                LevelInfo inf = new LevelInfo();
                inf.origNode = origNode;
                inf.x = r.getX();
                inf.y = r.getY();
                inf.width = r.getWidth();
                inf.height = r.getHeight();
                Object copyNode = _local.createNode(inf);
                _local.addNode(this, copyNode, copyComposite);
                map.put(origNode, copyNode);
            }
        }

        for (Iterator i = model.nodes(origComposite); i.hasNext(); ) {
            Object origTail = i.next();
            for (Iterator j = model.outEdges(origTail); j.hasNext(); ) {
                Object origEdge = j.next();
                Object origHead = model.getHead(origEdge);

                if (origHead != null) {
                    Object copyTail = map.get(origTail);
                    Object copyHead = map.get(origHead);
                    if (copyHead != null && copyTail != null) {
                        Object copyEdge = _local.createEdge(origEdge);
                        _local.setEdgeTail(this, copyEdge, copyTail);
                        _local.setEdgeHead(this, copyEdge, copyHead);
                    }
                }
            }
        }
        return copyComposite;
    }


    /**
     * Take the layout generated by the core layout algorithm and copy
     * it back into the view of the original composite passed in by the
     * user.
     */
    protected void copyLayout(Object origComposite, Object copyComposite) {
        GraphModel model = getLayoutTarget().getGraphModel();
        for (Iterator ns = _local.nodes(copyComposite); ns.hasNext(); ) {
            Object copyNode = ns.next();
            LevelInfo inf = getLevelInfo(copyNode);
            ASSERT(inf != null, "null inf");
            if (inf.origNode != null) {
                Rectangle2D r = getLayoutTarget().getBounds(inf.origNode);
                ASSERT(r != null, "null rect");
                getLayoutTarget().translate(inf.origNode, inf.x-r.getX(), inf.y-r.getY());
            }
        }
        LayoutUtilities.routeVisibleEdges(origComposite, getLayoutTarget());
    }

    /**
     * Return the local graph model.
     */
    public BasicGraphModel getLocalGraphModel() {
        return _local;
    }

    /**
     * Return the orientation in which the graph is to be laid out,
     * either VERTICAL or HORIZONTAL.
     */
    public int getOrientation() {
        return _orientation;
    }

    /**
     * Return whether or not placement will be randomized.
     */
    public boolean getRandomizedPlacement() {
        return _randomizedPlacement;
    }

    /**
     * Perform the levelizing layout on the given composite in the given
     * target environment.  It operates on a copy of the composite and
     * then copies the layout results back into the original view (the
     * given layout target).
     */
    public void layout(Object composite) {
        LevelData levelData = calculateLayout(composite);
        if (levelData != null) {
            applyLayout(levelData, composite);
        }
        /*
          _origComposite = g;
          _target = t;

          if (g.getNodeCount() > 0) {
          _levelData._copyGraph = copyComposite(g, t);

          //              if (isCyclic(_copyGraph)) {
          //                  String err = "Unable to perform levelizing layout on cyclic composites";
          //                  throw new IllegalArgumentException(err);
          //              }
          breakCycles(_levelData._copyGraph);
          //doLayout();
          copyLayout(_levelData._copyGraph, t);

          cleanupStructures();
          }
        */
    }

    /**
     * This method performs levelizing layout on the given composite.  It
     * figures out the node levels, but doesn't actually layout the
     * composite in the target environment yet.  The level information can
     * be accessed through the returned LevelData.  This information
     * can be used to size the viewport.  The following are the
     * operations performed in this method:
     * <ul>
     * <li>Make a copy of the original composite. All operations are
     * performed on the copy of the composite.</li>
     * <li>Break the cycles in the composite if there are any.</li>
     * <li>Add dummies to edges that span multiple levels in the
     * composite.</li>
     * <li>Assign level numbers to the nodes in the composite.  This
     * creates the _levels data structure which provides access
     * to all the nodes in each level.</li>
     * </li>
     * To apply this layout to the target environment, call applyLayout
     * with the returned LevelData.
     */
    public LevelData calculateLayout(Object composite) {
        GraphModel model = getLayoutTarget().getGraphModel();
        if (model.getNodeCount(composite) > 0) {
            LevelData levelData = new LevelData(getLayoutTarget(), composite);
            levelData._copyGraph = copyComposite(composite);
            breakCycles(levelData._copyGraph, _local);
            //Assign level numbers to the nodes in the composite.
            computeLevels(levelData);

            //POST all nodes have level greater than
            //     their incoming nodes
            for (Iterator i = _local.nodes(levelData._copyGraph);
                i.hasNext(); ) {
                Object node = i.next();
                int lvl = getLevel(node);
                for (Iterator j = GraphUtilities.inNodes(node, _local); j.hasNext(); ) {
                    Object n2 = j.next();
                    int lvl2 = getLevel(n2);
                    ASSERT(lvl2 < lvl, "Level order error " + node + ", " + n2);
                }
            }
            ASSERT(LayoutUtilities.checkContainment(levelData._copyGraph, _local),
                    "Inconsistent post-computeLevels");

            //Add dummies to edges that span multiple levels in the
            //graph.
            addDummies(levelData);

            //POST all nodes have level one greater
            //     than their incoming nodes
            //POST all dummy nodes have one in-edge and
            //     one out-edge
            for (Iterator i = _local.nodes(levelData._copyGraph);
                i.hasNext(); ) {
                Object node = i.next();
                int lvl = getLevel(node);
                for (Iterator j = GraphUtilities.inNodes(node, _local);
                    j.hasNext(); ) {
                    Object n2 = j.next();
                    int lvl2 = getLevel(n2);
                    ASSERT((lvl2 == lvl-1), "Level equality error " + node + ", " + n2);
                }
            }
            for (Iterator i = _local.nodes(levelData._copyGraph);
                i.hasNext(); ) {
                Object node = i.next();
                if (isDummy(node)) {
                    Iterator outs = _local.outEdges(node);
                    ASSERT(outs.hasNext(), "Dummy w/ no out-edges");
                    outs.next();
                    ASSERT(!outs.hasNext(), "Dummy w/ multiple out edges");

                    Iterator ins = _local.inEdges(node);
                    ASSERT(ins.hasNext(), "Dummy w/ no in edges");
                    ins.next();
                    ASSERT(!ins.hasNext(), "Dummy w/ multiple in edges");
                }
            }
            ASSERT(LayoutUtilities.checkContainment(levelData._copyGraph, _local),
                    "Inconsistent post-addDummies");

            //Create the _levels data structure which provides
            //convenient access to all the nodes in each level.
            makeLevels(levelData);

            //POST no levels are empty, and for each
            //     node in a level it's level is appropriate
            for (int i  = 1; i < levelData._levels.length; i++) {
                ArrayList nodes = levelData._levels[i];
                ASSERT((nodes.size() != 0), "Empty level " + i);
            }
            ASSERT(LayoutUtilities.checkContainment(levelData._copyGraph, _local),
                    "Inconsistent post-makeLevels");
            return levelData;
        }
        else {
            return null;
        }
    }

    /**
     * Place the nodes in the target environment according to their
     * levels and sorting order which are specified in levelData.
     * By default, the dummy nodes are used while doing the layout.
     * This method should be called after calculateLayout(t, g)
     * which returns the levelData used by this method.
     */
    public void applyLayout(LevelData levelData, Object g) {
        applyLayout(levelData, g, true);
        /*
          Rectangle2D r = t.getViewport(g);
          placeNodes(levelData, r);
          copyLayout(levelData._origGraph, levelData._copyGraph, t);
        */
    }

    /**
     * Place the nodes in the target environment according to their
     * levels and sorting order which are specified in levelData.
     * If "useDummies" is false, the dummy nodes are not used
     * in the layout which produces a more compact layout:
     * nodes in the same level may overlap and edges may
     * cross over nodes.  If "useDummies" is true, the dummy
     * nodes are used in the layout.
     * This method should be called after calculateLayout(t, g)
     * which returns the levelData used by this method.
     */
    public void applyLayout(LevelData levelData, Object g,
            boolean useDummies) {
        Rectangle2D r = getLayoutTarget().getViewport(g);
        placeNodes(levelData, r, useDummies);
        copyLayout(levelData._origGraph, levelData._copyGraph);
    }

    /**
     * Set the orientation in which the graph is to be laid out,
     * either VERTICAL or HORIZONTAL.
     */
    public void setOrientation(int o) {
        if ((o != VERTICAL) && (o != HORIZONTAL)) {
            String err = "Orientation must be either VERTICAL or HORIZONTAL";
            throw new IllegalArgumentException(err);
        }
        _orientation = o;
    }

    /**
     * Set whether or not placement will be randomized.
     */
    public void setRandomizedPlacement(boolean flag) {
        _randomizedPlacement = flag;
    }

    public class LevelData {
        /**
         * The layout target that is passed in by the user and
         * used to assign the layout to the view.
         */
        protected LayoutTarget _target;

        /**
         * The original graph that is passed in by the user on
         * which the layout is eventually being assigned.
         */
        protected Object _origGraph;

        /**
         * The local graph copy of the user's graph, to which
         * dummy nodes/edges are added, and on which the actual
         * layout is first performed before these values are
         * copied back into the user's graph.
         */
        protected Object _copyGraph;

        /**
         * A variable that is used to keep track of the maximum
         * level in the graph, starting at -1 and then incremented
         * as the level-finding algorithm is applied.
         */
        protected int _maxLevel = -1;

        /**
         * A simple data structure to keep track of the levels.
         * This is an array of array lists.  Each array list represents
         * a level in the graph and contains references to nodes in
         * the graph.
         */
        protected ArrayList _levels[] = null;

        /**
         * A meta-node which is a dummy node that is added to the graph
         * with edges to every other node in the graph in order to make it
         * easier to perform a topological sort.
         */
        protected Object _meta = null;

        public LevelData(LayoutTarget t, Object composite) {
            _target = t;
            _origGraph = composite;
        }

        public int getLevelCount() {
            return _levels.length;
        }

        /**
         * Each level contains a list of nodes that are in that
         * level (level width).  This includes dummy nodes which
         * are used as place holders in layout.  If 'withDummy'
         * is true, the method returns the most number of nodes including
         * dummy nodes in a level.  Otherwise, the method
         * returns the most number of real nodes in a level.
         */
        public int getMaxLevelWidth(boolean withDummy) {
            int max = -1;
            if (withDummy) {
                for (int i=0; i< getLevelCount(); i++) {
                    ArrayList list = _levels[i];
                    if (list.size() > max) {
                        max = list.size();
                    }
                }
            }
            else {
                for (int i=0; i< getLevelCount(); i++) {
                    ArrayList list = _levels[i];
                    if (list.size() > max) {
                        int ct = 0;
                        for (Iterator iter = list.iterator(); iter.hasNext();) {
                            Object n = iter.next();
                            if (!isDummy(n)) {
                                ct++;
                            }
                        }
                        max = Math.max(ct, max);
                    }
                }
            }
            return max;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private methods follow
    ///////////////////////////////////////////////////////////////////////////////////////

    /**
     * Assert the given condition and throw a runtime exception with
     * the given string if the assertion fails.
     */
    private void ASSERT(boolean b, String err) throws RuntimeException {
        if (!b) {
            throw new RuntimeException(err);
        }
    }

    /**
     * Perform the levelizing layout on the local copy of the graph, according
     * to the algorithm outlined in the class description.
     */
    /*
      private void doLayout() {
      //Assign level numbers to the nodes in the graph.
      computeLevels();

      //POST all nodes have level greater than
      //     their incoming nodes
      for (Iterator i = _levelData._copyGraph.nodes(); i.hasNext(); ) {
      Node n = (Node)i.next();
      int lvl = getLevel(n);
      for (Iterator j = GraphUtilities.inNodes(n); j.hasNext(); ) {
      Node n2 = (Node)j.next();
      int lvl2 = getLevel(n2);
      ASSERT(lvl2 < lvl, "Level order error " + n + ", " + n2);
      }
      }
      ASSERT(LayoutUtilities.checkContainment(_levelData._copyGraph, _target),
      "Inconsistent post-computeLevels");

      //Add dummies to edges that span multiple levels in the
      //graph.
      addDummies();

      //POST all nodes have level one greater
      //     than their incoming nodes
      //POST all dummy nodes have one in-edge and
      //     one out-edge
      for (Iterator i = _levelData._copyGraph.nodes(); i.hasNext(); ) {
      Node n = (Node)i.next();
      int lvl = getLevel(n);
      for (Iterator j = GraphUtilities.inNodes(n); j.hasNext(); ) {
      Node n2 = (Node)j.next();
      int lvl2 = getLevel(n2);
      ASSERT((lvl2 == lvl-1), "Level equality error " + n + ", " + n2);
      }
      }
      for (Iterator i = _levelData._copyGraph.nodes(); i.hasNext(); ) {
      Node n = (Node)i.next();
      if (isDummy(n)) {
      Iterator outs = n.outEdges();
      ASSERT(outs.hasNext(), "Dummy w/ no out-edges");
      outs.next();
      ASSERT(!outs.hasNext(), "Dummy w/ multiple out edges");

      Iterator ins = n.inEdges();
      ASSERT(ins.hasNext(), "Dummy w/ no in edges");
      ins.next();
      ASSERT(!ins.hasNext(), "Dummy w/ multiple in edges");
      }
      }
      ASSERT(LayoutUtilities.checkContainment(_levelData._copyGraph, _target),
      "Inconsistent post-addDummies");

      //Create the _levels data structure which provides
      //convenient access to all the nodes in each level.
      makeLevels();

      //POST no levels are empty, and for each
      //     node in a level it's level is appropriate
      for (int i  = 1; i < _levelData._levels.length; i++) {
      ArrayList nodes = _levelData._levels[i];
      ASSERT((nodes.size() != 0), "Empty level " + i);
      }
      ASSERT(LayoutUtilities.checkContainment(_levelData._copyGraph, _target),
      "Inconsistent post-makeLevels");

      //Place the nodes in the viewport according to their
      //levels and sorting order (note: sorting is not yet
      //implemented).
      Rectangle2D r = _target.getViewport(_origGraph);
      placeNodes(_levelData, r);

      //POST no post per se because this step does not
      //     modify graph topology.
      }
    */

    /**
     * Inefficient check for cycles in a graph.
     *
     private boolean isCyclic(Graph g) {
     for (Iterator i = g.nodes(); i.hasNext(); ) {
     Node root = (Node)i.next();
     setAllVisited(g, false);
     if (checkCyclic(root)) {
     return true;
     }
     }
     return false;
     }
    */

    /**
     * Inefficient algorithm to break cycles in
     * the graph.
     */
    private void breakCycles(Object composite, GraphModel model) {
        boolean hasCycles = true;

        while (hasCycles) {
            hasCycles = false;
            for (Iterator i = model.nodes(composite); i.hasNext(); ) {
                Object root = i.next();
                setAllVisited(composite, false);
                if (checkAndBreak(null, root)) {
                    hasCycles = true;
                    break;
                }
            }
        }
    }

    /**
     * Return true if a cycle was broken.
     */
    private boolean checkAndBreak(Object edge, Object node) {
        ASSERT(node != null, "null tail: " + node);

        if (isVisited(node)) {
            ASSERT(edge != null, "null incoming edge: " + node);
            //debug("BROKEN CYCLE AT: " + n);
            Object head = _local.getHead(edge);
            Object tail = _local.getTail(edge);
            if (head == tail) {
                // destroy the self loop.
                _local.setEdgeHead(this, edge, null);
                _local.setEdgeTail(this, edge, null);
            } else {
                //reverse the edge
                _local.setEdgeHead(this, edge, tail);
                _local.setEdgeTail(this, edge, head);
            }
            return true;
        }

        setVisited(node,true);
        for (Iterator i = _local.outEdges(node); i.hasNext(); ) {
            Object outEdge = i.next();
            Object outNode = _local.getHead(outEdge);
            ASSERT(outNode != null, "null head: " + edge);
            if (checkAndBreak(outEdge, outNode)) {
                return true;
            }
        }
        setVisited(node, false);
        return false;
    }



    /**
     * Perform DFS from the given node, checking for
     * cycles.
     *
     private boolean checkCyclic(Object node) {
     ASSERT(n != null, "null tail: " + n);

     if (n.isVisited()) {
     //          debug("CYCLE AT: " + n);
     return true;
     }
     n.setVisited(true);
     for (Iterator i = n.outEdges(); i.hasNext(); ) {
     Edge e = (Edge)i.next();
     Node out = e.getHead();
     ASSERT(out != null, "null head: " + e);
     if (checkCyclic(out)) {
     return true;
     }
     }
     n.setVisited(false);
     return false;
     }
    */

    /**
     * Add dummy nodes between nodes along edges that span multiple
     * levels.  For example:
     *
     * <pre>
     *
     *   o from            o from          o from
     *   |            |                  | <------ original edge
     *   |            |                  o dum1
     *   |    ==>   |            ==>          |          ==> ...
     *   |            o dum1          o dum2
     *   | <- e --> |                  |
     *  o to            o to          o to
     *
     * </pre>
     */
    private void addDummies(LevelData levelData) {
        ArrayList dummies = new ArrayList();
        for (Iterator nodes = _local.nodes(levelData._copyGraph);
            nodes.hasNext();) {
            Object to = nodes.next();
            if (isDummy(to)) { continue; }
            LevelInfo nlinfo = getLevelInfo(to);

            for (Iterator in = _local.inEdges(to); in.hasNext();) {
                Object edge = in.next();
                if (isDummy(_local.getTail(edge))) { continue; }
                while (getLevel(to) > getLevel(_local.getTail(edge))+1 ) {
                    //                    debug("Creating dummy between " + e.getTail() + " & " + e.getHead());

                    //dummy gets stuck between e.tail and e.head
                    LevelInfo dumInfo = new LevelInfo();
                    Object dummy = _local.createNode(dumInfo);
                    dumInfo.level = getLevel(_local.getTail(edge))+1;
                    // XXX postpone until later!  this is a
                    // hack to avoid concurrent modification
                    // exception.... =(
                    // _impl.addNode(this, dummy, _levelData._copyGraph);
                    dummies.add(dummy);
                    _local.setEdgeHead(this, edge, dummy);
                    edge = _local.createEdge(null);
                    _local.setEdgeTail(this, edge, dummy);
                    _local.setEdgeHead(this, edge, to);
                }
            }
        }

        //avoid concurrent modification exception...
        for (Iterator i = dummies.iterator(); i.hasNext(); ) {
            _local.addNode(this, i.next(), levelData._copyGraph);
        }
    }

    /**
     * Debugging output to standard err.
     */
    private void debug(String s) {
        System.err.println(s);
    }


    /**
     * Get the level of <i>n</i> in the graph.
     * Requires that all nodes have LevelInfo attributes.
     */
    private void makeLevels(LevelData levelData) {
        levelData._maxLevel = -1;
        Object maxNode = null;
        int level;

        //find the topmost node
        for (Iterator i = _local.nodes(levelData._copyGraph); i.hasNext();) {
            Object node = i.next();
            if ((level = getLevel(node)) > levelData._maxLevel) {
                levelData._maxLevel = level;
                maxNode = node;
            }
        }

        //        debug("max = " + maxNode);

        //create some buckets to store the nodes
        levelData._levels = new ArrayList[levelData._maxLevel+1];
        for (int i = 0; i < levelData._maxLevel+1; i++) {
            levelData._levels[i] = new ArrayList();
        }

        //clear all the nodes
        setAllVisited(levelData._copyGraph, false);

        initialOrderNodes(levelData, maxNode);

        //debug
        //          for (int i = 0; i < _levelData._levels.length; i++) {
        //              ArrayList l = _levelData._levels[i];
        //              IteratorUtil.printElements("Level " + i + ":", l.iterator());
        //          }
    }

    /**
     * Assign an initial ordering to the nodes.  This starts with the
     * "maximum" node (i.e.  a node in the maximum level) and
     * traverses its predecessors, adding them all to the level
     * buckets.  Then it adds all the other unmarked nodes.
     */
    private void initialOrderNodes(LevelData levelData, Object maxNode) {
        addSubGraphReverseDFS(levelData, maxNode);

        for (Iterator i = _local.nodes(levelData._copyGraph); i.hasNext();) {
            Object node = i.next();
            if (!isVisited(node)) { /**FIXXX**/
                addSubGraphReverseDFS(levelData, node);
            }
        }
    }


    /**
     * Add this node and all of its parent nodes to the levels array
     * in a reverse DFS.
     */
    private void addSubGraphReverseDFS(LevelData levelData, Object node) {
        setVisited(node, true);
        for (Iterator ins = GraphUtilities.inNodes(node, _local); ins.hasNext();) {
            Object in = ins.next();
            ASSERT((in != null), "NULL found, n = " + node);
            if (!isVisited(in)) {
                addSubGraphReverseDFS(levelData, in);
            }
        }
        levelData._levels[getLevel(node)].add(node);
    }

    /**
     * Place the nodes in the graph, based on the previous level
     * calculations and the order of the nodes in the _levels array.
     * Because we can make the placement either horiz. we need to be
     * clever to share code.  The algorithm is written as if it were
     * vertical placement, and in the horizontal case, different
     * values are used.
     */
    private void placeNodes(LevelData levelData, Rectangle2D vp,
            boolean useDummies) {
        //        debug("vp = " + vp);

        //XXX this whole thing is a hack.  there
        //    really should be no empty levels.
        //    fix is elsewhere...
        int nonEmptyLevels = 0;
        for (Iterator i = new ArrayIterator(levelData._levels); i.hasNext(); ) {
            ArrayList nodes = (ArrayList)i.next();
            if (nodes.size()>0) nonEmptyLevels++;
        }
        nonEmptyLevels = (int)Math.max(1, nonEmptyLevels);

        if (getOrientation() == VERTICAL) {
            double ystep, y;
            ystep = vp.getHeight() / nonEmptyLevels;
            y = vp.getY() + ystep / 2;
            //            int lnum=0;
            for (Iterator i = new ArrayIterator(levelData._levels); i.hasNext(); ) {
                ArrayList nodes = (ArrayList)i.next();
                int levelWidth;
                if (useDummies) {
                    levelWidth = nodes.size();
                }
                else{
                    // HH, use the number of real nodes (no dummies) to
                    // determine the step size in the x direction.
                    levelWidth = 0;
                    for (Iterator j = nodes.iterator(); j.hasNext();) {
                        Object n = j.next();
                        if (!isDummy(n)) {
                            levelWidth++;
                        }
                    }
                    //                    System.out.println("Level " + lnum + ": " + nodes.size() + " nodes, real nodes: " + levelWidth);
                    //                    lnum++;
                }
                double xstep, x;
                xstep = vp.getWidth() / levelWidth;

                x = vp.getX() + xstep / 2;
                if (nodes.size() == 0) {
                    continue;  //XXX why do we have an empty level???
                }

                for (Iterator ns = nodes.iterator(); ns.hasNext();) {
                    Object node = ns.next();
                    if (!isDummy(node)) {
                        placeNode(node, x, y);
                    }
                    x += xstep;
                }
                y += ystep;
            }
        }
        else {
            double xstep, x;
            xstep = vp.getWidth() / nonEmptyLevels;
            x = vp.getX() + xstep / 2;

            for (Iterator i = new ArrayIterator(levelData._levels); i.hasNext(); ) {
                ArrayList nodes = (ArrayList)i.next();
                int levelWidth;
                if (useDummies) {
                    levelWidth = nodes.size();
                }
                else{
                    // HH, use the number of real nodes (no dummies) to
                    // determine the step size in the x direction.
                    levelWidth = 0;
                    for (Iterator j = nodes.iterator(); j.hasNext();) {
                        Object n = j.next();
                        if (!isDummy(n)) {
                            levelWidth++;
                        }
                    }
                    //                    System.out.println("Level " + lnum + ": " + nodes.size() + " nodes, real nodes: " + levelWidth);
                    //                    lnum++;
                }
                double ystep, y;
                ystep = vp.getHeight() / levelWidth;
                y = vp.getY() + ystep / 2;
                if (nodes.size() == 0) {
                    continue;  //XXX why do we have an empty level???
                }

                for (Iterator ns = nodes.iterator(); ns.hasNext();) {
                    Object node = ns.next();
                    if (!isDummy(node)) {
                        LevelInfo inf = getLevelInfo(node);
                        placeNode(node, x, y);
                    }
                    y += ystep;
                }
                x += xstep;
            }
        }
    }

    // add random perturbation for now.
    private void placeNode(Object node, double x, double y) {
        LevelInfo inf = getLevelInfo(node);
        //       debug("placing: " + inf.origNode + "(" + x + ", " + y + ")");
        if (_randomizedPlacement) {
            x += Math.random()*.25*inf.width;
            y += Math.random()*.25*inf.height;
        }
        inf.x = x-inf.width/2;
        inf.y = y-inf.height/2;
    }


    //==================================================================
    // UTILITY FUNCTIONS HERE
    //==================================================================


    private double getX(LevelData levelData, Object node) {
        return levelData._target.getBounds(node).getX();
    }

    private LevelInfo getLevelInfo(Object node) {
        return (LevelInfo) _local.getSemanticObject(node);
    }

    /**
     * Get the level of <i>n</i> in the graph.  Requires that node has
     * LevelInfo attribute.
     */
    private int getLevel(Object node) {
        return getLevelInfo(node).level;
    }

    /**
     * Set the level of <i>n</i> in the graph.
     * Requires that node has LevelInfo attribute.
     */
    private void setLevel(Object node, int l) {
        getLevelInfo(node).level = l;
    }

    /**
     * Get the level of <i>n</i> in the graph.
     * Requires that node has LevelInfo attribute.
     */
    private int getUsage(Object node) {
        return getLevelInfo(node).usage;
    }

    /**
     * Return whether or not the given node
     * is a dummy.
     */
    private boolean isDummy(Object node) {
        LevelInfo inf = getLevelInfo(node);
        return (inf.origNode == null);
    }

    /**
     * Set the level of <i>n</i> in the graph.
     * Requires that node has LevelInfo attribute.
     */
    private void setUsage(Object node, int val) {
        getLevelInfo(node).usage = val;
    }


    /**
     * Topologically sort from the given node and
     * place the results in the given array list.
     */
    private void topoSort(Object node, ArrayList topo) {
        setVisited(node, true);
        for (Iterator i = GraphUtilities.inNodes(node, _local); i.hasNext();) {
            Object n2 = i.next();
            if (!isVisited(n2)) {
                topoSort(n2, topo);
            }
        }
        topo.add(node);
    }

    /**
     * Create a meta-node and add it to the graph, connecting
     * it to each existing node in the graph.
     */
    private void makeMeta(LevelData levelData) {
        levelData._meta = _local.createNode(new LevelInfo());

        for (Iterator ns = _local.nodes(levelData._copyGraph); ns.hasNext(); ) {
            Object node = ns.next();
            Object edge = _local.createEdge(null);
            _local.setEdgeTail(this, edge, node);
            _local.setEdgeHead(this, edge, levelData._meta);
        }

        //avoid self-loop
        _local.addNode(this, levelData._meta, levelData._copyGraph);
    }


    /**
     * Remove the meta-node from the graph.
     */
    private void removeMeta(LevelData levelData) {
        try {
            GraphUtilities.purgeNode(this, levelData._meta, _local); /**FIXXX**/
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
        levelData._meta = null;
    }

    /**
     * The semantic object of each node in the graph copy that is
     * being laid out.
     */
    public static class LevelInfo {
        public Object origNode = null;
        public double barycenter;
        public int level = -1;
        public int usage = Integer.MAX_VALUE;
        public double x;
        public double y;
        public double width;
        public double height;
        public boolean visited = false;
    }

    public void setVisited(Object node, boolean val) {
        getLevelInfo(node).visited = val;
    }

    public void setAllVisited(Object composite, boolean val) {
        for (Iterator i = _local.nodes(composite); i.hasNext(); ) {
            setVisited(i.next(), val);
        }
    }

    public boolean isVisited(Object node) {
        return getLevelInfo(node).visited;
    }


    /**
     * Topological sort of graph and then set level numbers
     * for the nodes.
     */
    private void computeLevels(LevelData levelData) {
        setAllVisited(levelData._copyGraph, false);

        // Topological sort
        makeMeta(levelData);
        ArrayList topo = new ArrayList();
        topoSort(levelData._meta, topo);
        //DEBUG
        //IteratorUtil.printElements("TOPOLOGICAL SORT:", topo.iterator());

        /*
         * Find maximum level, which is 1 + the maximum
         * of any of your predecessors.
         *
         *         A
         *       / | \
         *      B  |  D
         *      |\ |  |
         *      |  C  |
         *       \ | /
         *       meta
         */
        int maxLevel = 0;
        for (Iterator i = topo.iterator(); i.hasNext();) {
            int level = 0;
            Object node = i.next();
            for (Iterator ins = GraphUtilities.inNodes(node, _local); ins.hasNext();) {
                Object in = ins.next();
                level = Math.max(level, getLevel(in)+1);
            }
            //            debug("INITIAL: " + getLevelInfo(n).origNode + ", " + level);
            //              for (Iterator ins = GraphUtilities.inNodes(n); ins.hasNext();) {
            //                  Node in = (Node)ins.next();
            //                  debug("\tin: " + getLevelInfo(in).origNode + ", " + getLevel(in));
            //              }
            setLevel(node, level);
            maxLevel = Math.max(maxLevel, level);
        }

        /* Find maximum usage, which is the maximum
         * level of any of your predecessors - 1.
         *
         *         A
         *       / | \
         *      B  |  |
         *      |\ |  |
         *      |  C  D
         *       \ | /
         *       meta
         */
        for (int i = topo.size()-1; i >= 0; i--) {
            Object node = (topo.get(i));
            int usage = maxLevel;

            if (!_local.outEdges(node).hasNext()) {
                usage = getLevel(node);
            }
            for (Iterator outs = GraphUtilities.outNodes(node, _local); outs.hasNext(); ) {
                //there was an XXX here?
                Object out = outs.next();
                usage = Math.min(usage, getUsage(out)-1);
            }
            setUsage(node, usage);
        }


        // Assign level number based on usage.
        for (Iterator i = topo.iterator(); i.hasNext();) {
            Object node = i.next();
            setLevel(node, getUsage(node));
            //debug("LEVEL: " + getLevelInfo(n).origNode + ", " + getUsage(n));
        }

        removeMeta(levelData);
    }
}
















/*
  ///////////////////////////////////////////////////////////////////////
  // Under construction
  ///////////////////////////////////////////////////////////////////////



  /**
   * Do insertion sort on the level based on the barycenters,
   * then reorder
   *
   private final void sortLevel(ArrayList nodes) {
   Object []ns = nodes.toArray();
   Arrays.sort(ns, new BarycentricComparator());
   nodes.clear();
   for (Iterator i = new ArrayIterator(ns); i.hasNext(); ) {
   nodes.add(i.next());
   }

   /*
     int len = nodes.size();
     for (int i = 1; i < len; i++) {
     Node n1 = (Node) nodes.get(i);
     double bc = barycenter(n1);
     int j;
     for (j = i; j > 0; j--) {
     Node n2 = (Node)nodes.get(j-1);
     if (bc >= getBarycenter(n2)) break;
     nodes.add(j, n2);
     }
     nodes.add(j, n1);
     }*
     }


     private final void orderLevel( ArrayList nodes, double l, double y,
     boolean doin, boolean doout ) {
     int levelcnt = nodes.size();
     for (Iterator e = nodes.iterator(); e.hasNext();) {
     Node n = (Node) e.next();
     computeBarycenter(n, doin, doout);
     }
     sortLevel( nodes );
     //XXX placeLevel( l, y, nodes );
     }

     // Do downwards barycentering on first pass, upwards on second, then average
     private final void orderNodes( double l, int op ) {
     boolean doup = ((op & 0x1) == 1);
     boolean doin = (op > 5 || !doup);
     boolean doout = (op > 5 || doup);
     double ystep = (_maxLevel>0) ? (_target.getViewport(_origGraph).getHeight()/_maxLevel) : 0.0;
     if (doup ) {
     double y = 0.0;
     for ( int i = 0; i <= _maxLevel; ++i ) {                // Going upwards
     ArrayList nodes = _levels[i];
     orderLevel( nodes, l, y, doin, doout );
     y += ystep;
     }
     }
     else {
     double y = l;
     for ( int i = _maxLevel; i >= 0; --i ) {                // Going downwards
     ArrayList nodes = _levels[i];
     orderLevel( nodes, l, y, doin, doout );
     y -= ystep;
     }
     }
     }

     protected final void straightenDummy(Node n) {
     Node tail = n.getInNode(0);
     Node head = n.getOutNode(0);
     double avg = (n.getX() + tail.getX() + head.getX()) / 3;
     n.setX(avg);
     }

     private final int xmarginSize = 10;
     protected synchronized final void straightenLayout( double l ) {
     double ystep = l/(_maxLevel+1);
     double y = 0.0;
     for (int i = 0; i <= _maxLevel; i++) {
     ArrayList nodes = _levels[i];
     for (Iterator e = nodes.iterator(); e.hasNext(); ) {
     Node n = (Node)e.next();
     if (n instanceof DummyNode) {
     straightenDummy(n);
     }
     }

     for (int j = 1; j < nodes.size(); j++) {
     Node n = (Node)nodes.get(j);
     Node prev = (Node)nodes.get( j-1 );
     double prevright = prev.getX() + prev.getW()/2 + xmarginSize;
     double thisleft =  n.getX() - n.getW()/2 - xmarginSize;
     double overlap = prevright - thisleft;
     if (overlap > 0 ) {
     prev.setX(prev.getX() - overlap/2);
     n.setX(n.getX() + overlap/2);
     }
     n.setY(y);
     }
     y += ystep;
     }
     }



     protected int _operation = 0;
     protected final int _Order = 100;
     private final void Embed() {
     double L = _bb.globals.L();
     _bb.setArea( 0, 0, L, L );
     if (_operation < _Order ) {
     orderNodes( L, _operation );
     }
     else {
     straightenLayout( L );
     }
     _bb.Update();
     ++_operation;
     _bb.globals.Temp( (double)_operation );
     }

     private void computeBarycenter(Node n, boolean doin, boolean doout) {
     double insum = 0.0;
     double outsum = 0.0;
     int insize = 0;
     int outsize = 0;

     if (doin) {
     for (Iterator e = GraphUtilities.inNodes(n); e.hasNext();) {
     insize++;
     insum += getX((Node)e.next());
     }
     if (insize == 0) {
     insize = 1;
     insum = getX(n);
     }
     }

     if (doout ) {
     for (Iterator e = GraphUtilities.outNodes(n); e.hasNext();) {
     outsize++;
     outsum += getX(n);
     }
     if (outsize == 0) {
     outsize = 1;
     outsum = getX(n);
     }
     }

     double barycenter;
     if (doin && doout ) {
     barycenter = (insum+outsum)/(insize+outsize);
     }
     else if (doin) {
     barycenter = insum/insize;
     }
     else if (doout) {
     barycenter = outsum/outsize;
     }
     else {
     barycenter = getX(n);
     }

     LevelInfo info = getLevelInfo(n);
     info.barycenter = barycenter;
     }


     private double getBarycenter(Node n) {
     return getLevelInfo(n).barycenter;
     }



     private Filter _defaultIgnoreFilter = new Filter() {
     public boolean accept(Object o) {
     if (o instanceof Node) {
     Node n = (Node)o;
     return (n == null) || !_target.isNodeVisible(n);
     }
     return false;
     }
     };

     private Filter _ignoreFilter = _defaultIgnoreFilter;

     public void setIgnoreFilter(Filter f) {
     if (f == null) {
     _ignoreFilter = _defaultIgnoreFilter;
     }
     _ignoreFilter = new OrFilter(f, _defaultIgnoreFilter);
     }

     private boolean ignoreNode(Node n) {
     return _ignoreFilter.accept(n);
     }

     private boolean ignoreEdge(Edge e) {
     return (ignoreNode(e.getHead()) || ignoreNode(e.getTail()));
     }
*/





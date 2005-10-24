/*

Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.jhdl.soot;

import soot.toolkits.graph.Block;

import ptolemy.graph.*;
import ptolemy.kernel.util.IllegalActionException;

import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// DominatorHashMap

/**
 * This class determines the dominators of each Block within a CFG.
 * The key of each entry in this hashMap is a node within a
 * DirectedAcyclicCFG. The Values of the HashMap are Vectors that contain
 * references to dominating nodes. <p>
 *
 * This class will also compute the immediate dominator for each
 * Node in the graph. <p>
 *
 * Note that this class can determine the post dominators (and
 * immediate post dominators) instead of the
 * dominators if necessary.
 *
 @author Mike Wirthlin
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
*/
public class DominatorHashMap extends HashMap {
    /**
     * @param g A control-flow graph of the method of interest
     * @param postDominates If true, a HashMap of the post dominators
     * will be created. If false, a HashMap of the dominators will
     * be created.
     **/
    public DominatorHashMap(DirectedAcyclicCFG g, boolean postDominates)
        throws IllegalActionException {
        super(g.nodeCount());
        _graph = g;
        _postDominates = postDominates;

        _computeDominators();
        _computeImmediateDominators();
    }

    /**
     * Determine the dominators of the CFG. The postDominates parameter
     * is false (i.e. compute dominators, not post dominators).
     **/
    public DominatorHashMap(DirectedAcyclicCFG g) throws IllegalActionException {
        this(g, false);
    }

    /**
     * Returns a Vector containing all dominators of Node n.
     **/
    public List getDominators(Node n) {
        return (Vector) get(n);
    }

    /** Returns true if Node d dominates Node n **/
    public boolean dominates(Node d, Node n) {
        List dominates = getDominators(n);

        if (dominates.contains(d)) {
            return true;
        } else {
            return false;
        }
    }

    /** Returns the immediate dominator of Node n **/
    public Node getImmediateDominator(Node n) {
        Object o = _immediateDominators.get(n);

        if (o != null) {
            return (Node) o;
        } else {
            return null;
        }
    }

    public HashMap immediateDominators() {
        return _immediateDominators;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        // debug printing
        for (Iterator i = keySet().iterator(); i.hasNext();) {
            Node n = (Node) i.next();
            sb.append(_graph.nodeString(n) + ":");

            Vector v = (Vector) get(n);

            for (Iterator j = v.iterator(); j.hasNext();) {
                Node d = (Node) j.next();
                sb.append(_graph.nodeString(d) + " ");
            }

            sb.append("\r\n");
        }

        return sb.toString();
    }

    // p. 184 of Muchnick. Returns a HashMap with Nodes as the key
    // and Nodes as the values.
    public String immediateDominatorsString() {
        StringBuffer sb = new StringBuffer();

        for (Iterator i = _immediateDominators.keySet().iterator();
                    i.hasNext();) {
            Node n = (Node) i.next();
            sb.append(_graph.nodeString(n) + "=");

            Object o = _immediateDominators.get(n);

            //              if (o==null)
            //                  sb.append("none");
            //              else
            //                  sb.append(_graph.nodeString((Node)o));
            if (o instanceof Node) {
                sb.append(_graph.nodeString((Node) o));
            } else {
                sb.append("None");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    // p. 184 of Muchnick. Returns a HashMap with Nodes as the key
    // and Nodes as the values.
    protected HashMap _computeImmediateDominators() {
        // Clone this object, but remove self dominators
        HashMap tmp = new HashMap(size());

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            Node n = (Node) i.next();
            Vector v = (Vector) get(n);
            Vector u = (Vector) v.clone();
            u.remove(n);
            tmp.put(n, u);
        }

        for (Iterator i = keySet().iterator(); i.hasNext();) {
            Node n = (Node) i.next();

            if (n == _root) {
                continue;
            }

            Vector v = (Vector) tmp.get(n);
            Vector remove = new Vector(v.size());

            for (Iterator j = v.iterator(); j.hasNext();) {
                Node s = (Node) j.next();
                Vector sv = (Vector) tmp.get(s);

                for (Iterator k = v.iterator(); k.hasNext();) {
                    Node t = (Node) k.next();

                    if (t == s) {
                        continue;
                    }

                    if (sv.contains(t)) {
                        remove.add(t);
                    }
                }
            }

            v.removeAll(remove);
        }

        for (Iterator i = tmp.keySet().iterator(); i.hasNext();) {
            Node n = (Node) i.next();
            Vector v = (Vector) tmp.get(n);
            Node t = null;

            if (v.size() > 0) {
                t = (Node) v.get(0);
            }

            tmp.put(n, t);
        }

        _immediateDominators = tmp;
        return _immediateDominators;
    }

    // see page 671 in DragonBook
    protected void _computeDominators() throws IllegalActionException {
        // Sort the graph Nodes
        if (_postDominates) {
            _root = _graph.sink();
        } else {
            _root = _graph.source();
        }

        Collection sortedNodes = _graph.topologicalSort(_graph.nodes());

        if (_postDominates) {
            sortedNodes = _reverseList(sortedNodes);
        }

        int graphSize = _graph.nodeCount();

        // Create a Dominator Vector for each node in graph
        for (Iterator i = _graph.nodes().iterator(); i.hasNext();) {
            Node n = (Node) i.next();
            Vector d = null;

            if (n == _root) {
                d = new Vector(1);
                d.add(_root);
            } else {
                d = new Vector(graphSize);

                // Inititally, assume all nodes dominate current node
                for (Iterator j = _graph.nodes().iterator(); j.hasNext();) {
                    d.add((Node) j.next());
                }
            }

            // Place initalized Vector into the hashMap
            put(n, d);
        }

        boolean changed = false;

        do {
            changed = false;

            // Fastest if you start at top of graph
            //for (int i=0;i<sortedNodes.length;i++) {
            //Node n = (Node) sortedNodes[i];
            for (Iterator i = sortedNodes.iterator(); i.hasNext();) {
                Node n = (Node) i.next();

                if (n == _root) {
                    continue;
                }

                //                    System.out.println("Dominators for block "+
                //                                       ((Block)n.getWeight()).getIndexInMethod());
                Vector nDominators = (Vector) get(n);

                // Loop through predecessors of n
                Vector intersection = null;
                Collection predecessors;

                if (_postDominates) {
                    predecessors = _graph.successors(n);
                } else {
                    predecessors = _graph.predecessors(n);
                }

                for (Iterator j = predecessors.iterator(); j.hasNext();) {
                    Node p = (Node) j.next();
                    Vector pDominators = (Vector) get(p);

                    if (intersection == null) {
                        // If intersection vector is null, initialize it
                        // with the dominators of p
                        intersection = new Vector(pDominators.size());
                        intersection.addAll(pDominators);
                    } else {
                        // compute the intersection with p
                        Vector remove = new Vector(intersection.size());

                        for (Iterator k = intersection.iterator(); k.hasNext();) {
                            Object o = k.next();

                            if (!(pDominators.contains(o))) {
                                remove.add(o);
                            }
                        }

                        // remove elements that need removing
                        for (Iterator k = remove.iterator(); k.hasNext();) {
                            intersection.remove(k.next());
                        }
                    }
                }

                // Vector intersection now contains the intersection of
                // the dominators of all predecessors.
                // Add itself.
                intersection.add(n);

                if (intersection.size() < nDominators.size()) {
                    changed = true;
                    put(n, intersection);
                }
            }
        } while (changed);

        // Trim size of dominator vectors (extra space no longer needed)
        for (Iterator i = _graph.nodes().iterator(); i.hasNext();) {
            Object o = i.next();
            Vector v = (Vector) get(o);
            v.trimToSize();
        }
    }

    /*
      protected Object[] _reverseList(Object [] ol) {
      int len = ol.length;
      Object[] o = new Object[len];
      for (int i=0;i<ol.length;i++)
      o[i]=ol[len-i-1];
      return o;
      }
    */
    protected Collection _reverseList(Collection l) {
        Vector v = new Vector(l.size());

        for (Iterator i = l.iterator(); i.hasNext();) {
            v.add(0, i.next());
        }

        return v;
    }

    public static void main(String[] args) {
        DirectedAcyclicCFG _cfg = DirectedAcyclicCFG._main(args);

        try {
            DominatorHashMap dhm = new DominatorHashMap(_cfg);
            System.out.println("Dominators\n" + dhm);

            DominatorHashMap post = new DominatorHashMap(_cfg, true);
            System.out.println("Post Dominators\n" + post);
            System.out.println("Immediate Dominators");
            System.out.println(dhm.immediateDominatorsString());
            System.out.println("Immediate Post Dominators");
            System.out.println(post.immediateDominatorsString());
        } catch (IllegalActionException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    protected DirectedAcyclicCFG _graph;
    protected boolean _postDominates;
    protected Node _root;
    protected HashMap _immediateDominators;
}

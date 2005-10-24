/* Hash a mutable directed graph to dotty notation.

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
package ptolemy.copernicus.jhdl.util;

import soot.toolkits.graph.Block;

import ptolemy.graph.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// SynthesisToDotty

/**
   Convert a Soot DirectedGraph to dotty notation.
   @author Michael Wirthlin
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class SynthesisToDotty extends GraphToDotty {
    /**
     * Return a string which contains the DirectedGraph in dotty form
     * @param ename Title of the graph
     */
    public String convert(Object graph, String ename) {
        DirectedGraph g = (DirectedGraph) graph;
        int count = 0;
        subCount = 0;

        HashMap hm = new HashMap();
        HashMap blockToVertex = new HashMap();
        StringBuffer sb = new StringBuffer();

        sb.append("//Dotfile created by SynthesisToDotty\r\n");
        sb.append("digraph " + ename + " {\r\n");
        sb.append("\tcompound=true;\r\n");
        sb.append("\t// Vertices\r\n");

        for (Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
            Node source = (Node) nodes.next();
            String name;

            if (source.getWeight() instanceof SuperBlock) {
                SuperBlock b = (SuperBlock) source.getWeight();
                name = "cluster_" + count;
                sb.append("\t" + subGraph(b, name));
                blockToVertex.put(source, bigHack);

                if (bigHack == null) {
                    sb.append("\t\"" + name + "\" [label=\""
                        + b.getBlock().toShortString() + "\"];\r\n");
                }
            } else {
                name = "v" + count;
                sb.append("\t\"" + name + "\"");

                if (source.hasWeight()) {
                    sb.append(" [label=\""
                        + convertSpecialsToEscapes(source.getWeight().toString())
                        + "\"]");
                }

                sb.append(";\r\n");
            }

            hm.put(source, name);
            count++;
        }

        sb.append("\t// Edges\r\n");

        for (Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
            Node source = (Node) nodes.next();

            for (Iterator succs = g.outputEdges(source).iterator();
                        succs.hasNext();) {
                Edge edge = (Edge) succs.next();
                Node dest = edge.sink();

                boolean sourceIsSB = (source.getWeight() instanceof SuperBlock)
                            && (blockToVertex.get(source) != null);
                boolean destIsSB = (dest.getWeight() instanceof SuperBlock)
                            && (blockToVertex.get(dest) != null);

                if (sourceIsSB) {
                    sb.append("\t" + blockToVertex.get(source));
                } else {
                    sb.append("\t" + hm.get(source));
                }

                sb.append(" -> ");

                if (destIsSB) {
                    sb.append(blockToVertex.get(dest));
                } else {
                    sb.append(hm.get(dest));
                }

                sb.append(" [");

                if (sourceIsSB) {
                    sb.append(" ltail=" + hm.get(source));
                }

                if (sourceIsSB && destIsSB) {
                    sb.append(",");
                }

                if (destIsSB) {
                    sb.append(" lhead=" + hm.get(dest));
                }

                if (edge.hasWeight()) {
                    if (sourceIsSB || destIsSB) {
                        sb.append(",");
                    }

                    sb.append(" label=\""
                        + convertSpecialsToEscapes(edge.getWeight().toString())
                        + "\"");
                }

                sb.append("];\r\n");
            }
        }

        sb.append("}\r\n");
        return sb.toString();
    }

    protected String subGraph(SuperBlock b, String cluster_num) {
        HashMap hm = new HashMap();
        StringBuffer sb = new StringBuffer();
        DirectedGraph g = b.getGraph();
        Block bl = b.getBlock();

        bigHack = null;

        sb.append("subgraph " + cluster_num + " {\r\n");
        sb.append("\t\tlabel=\"" + bl.toShortString() + "\";\r\n");
        sb.append("\t\t// Vertices\r\n");

        for (Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
            Node source = (Node) nodes.next();
            String name = "s" + subCount++;

            if (bigHack == null) {
                bigHack = name;
            }

            sb.append("\t\t\"" + name + "\"");

            if (source.hasWeight()) {
                sb.append(" [label=\""
                    + convertSpecialsToEscapes(source.getWeight().toString())
                    + "\"]");
            }

            sb.append(";\r\n");
            hm.put(source, name);
        }

        sb.append("\t\t// Edges\r\n");

        for (Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
            Node source = (Node) nodes.next();

            for (Iterator succs = g.outputEdges(source).iterator();
                        succs.hasNext();) {
                Edge edge = (Edge) succs.next();
                Node dest = edge.sink();
                sb.append("\t\t\"" + hm.get(source) + "\" -> \"" + hm.get(dest)
                    + "\"");

                if (edge.hasWeight()) {
                    sb.append(" [label=\""
                        + convertSpecialsToEscapes(edge.getWeight().toString())
                        + "\"]");
                }

                sb.append(";\r\n");
            }
        }

        sb.append("\t}\r\n");
        return sb.toString();
    }

    private String bigHack;
    private int subCount;
}

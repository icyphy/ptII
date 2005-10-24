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
package ptolemy.copernicus.jhdl.util;

import ptolemy.graph.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// PtDirectedGraphToDotty

/**
   Convert a Soot DirectedGraph to dotty notation.
   @author Michael Wirthlin
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class PtDirectedGraphToDotty extends GraphToDotty {
    /**
     * Return a string which contains the DirectedGraph in dotty form
     * @param ename Title of the graph
     */
    public String convert(Object graph, String ename) {
        DirectedGraph g = (DirectedGraph) graph;
        int count = 0;
        HashMap hm = new HashMap();
        StringBuffer sb = new StringBuffer();
        sb.append(header("PtDirectedGraphToDotty", ename));

        for (Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
            Node source = (Node) nodes.next();
            String name = "v" + count++;
            sb.append("\t\"" + name + "\"");

            if (source.hasWeight()) {
                sb.append(" [label=\""
                    + convertSpecialsToEscapes(source.getWeight().toString())
                    + "\"]");
            }

            sb.append(";\r\n");
            hm.put(source, name);
        }

        sb.append("\t// Edges\r\n");

        for (Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
            Node source = (Node) nodes.next();

            for (Iterator succs = g.outputEdges(source).iterator();
                        succs.hasNext();) {
                Edge edge = (Edge) succs.next();
                Node dest = edge.sink();
                sb.append("\t\"" + hm.get(source) + "\" -> \"" + hm.get(dest)
                    + "\"");

                if (edge.hasWeight()) {
                    sb.append(" [label=\""
                        + convertSpecialsToEscapes(edge.getWeight().toString())
                        + "\"]");
                }

                sb.append(";\r\n");
            }
        }

        sb.append("}\r\n");
        return sb.toString();
    }
}

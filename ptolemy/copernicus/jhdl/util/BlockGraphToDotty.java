/*
 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.util;

import soot.jimple.*;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.Block;
import java.util.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// BlockGraphToDotty
/**
Convert a Soot BlockGraph to dotty notation.
@author Michael Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class BlockGraphToDotty extends GraphToDotty {

    /**
     * Return a string which contains the BlockGraph in dotty form
     * @param ename Title of the graph
     */
    public String convert(Object graph, String ename){
        BlockGraph g = (BlockGraph)graph;

        //Code copied from buildMapForBlock() in Block.java
        //Method is private so I couldn't get at it without
        //just copying it over...  :-/
        Map m = new HashMap();
        List basicBlocks = g.getBlocks();
        Iterator it = basicBlocks.iterator();
        while (it.hasNext()) {
            Block currentBlock = (Block) it.next();
            m.put(currentBlock.getHead(),  "Block " + (new Integer(currentBlock.getIndexInMethod()).toString()));
        }

        int count=0;
        HashMap hm=new HashMap();
        StringBuffer sb = new StringBuffer();
        sb.append("//Dotfile created by HashMutableToDotty\r\n");
        sb.append("digraph "+ename+" {\r\n");
        sb.append("\t// Vertices\r\n");
        for (Iterator nodes = g.iterator();nodes.hasNext();) {
            Block source = (Block)nodes.next();
            String name="v" + count++;
            sb.append("\t\""+name+"\" [label=\""
                      // soot 2.0 
                      //+convertSpecialsToEscapes(source.toString(m))
                      +convertSpecialsToEscapes(source.toString())
                    +"\"];"+MYEOL);
            hm.put(source, name);
        }
        sb.append("\t// Edges\r\n");
        for (Iterator nodes=g.iterator(); nodes.hasNext();){
            Block source = (Block)nodes.next();
            boolean endsWithIf = source.getTail() instanceof IfStmt;

            //System.err.println(source.toShortString());

            for (Iterator succs = g.getSuccsOf(source).iterator(); succs.hasNext();) {
                Block dest= (Block)succs.next();
                sb.append("\t\""+hm.get(source)+"\" -> \""+hm.get(dest)+"\"");
                if (endsWithIf){
                    sb.append(" [\"label\"=");
                    if (((IfStmt)source.getTail()).getTargetBox().getUnit() == dest.getHead()){
                        sb.append("\"true\"");
                    } else {
                        sb.append("\"false\"");
                    }
                    sb.append("]");
                }
                sb.append(";"+MYEOL);
            }
        }
        sb.append("}\r\n");
        return sb.toString();
    }
}

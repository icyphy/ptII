/* Hash a mutable directed graph to dotty notation.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

import ptolemy.graph.*;
import java.util.Iterator;
import java.util.HashMap;

//////////////////////////////////////////////////////////////////////////
//// PtDirectedGraphToDotty
/**
Convert a Soot DirectedGraph to dotty notation.
@author Michael Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class PtDirectedGraphToDotty {

    /**
     * Return a string which contains the DirectedGraph in dotty form
     */
    public static String convert(DirectedGraph g){
	return convert(g, "NoTitle");
    }

    /**
     * Return a string which contains the DirectedGraph in dotty form
     * @param ename Title of the graph
     */
    public static String convert(DirectedGraph g, String ename){
	int count=0;
	HashMap hm=new HashMap();
	StringBuffer sb = new StringBuffer();
	sb.append("//Dotfile created by PtDirectedGraphToDotty\r\n");
	sb.append("digraph "+ename+" {\r\n");
	sb.append("\t// Vertices\r\n");
	for(Iterator nodes = g.nodes().iterator(); nodes.hasNext();) {
	    Node source = (Node)nodes.next();
	    String name="v" + count++;
	    sb.append("\t\""+name+"\"");

	    if (source.hasWeight()){
		sb.append(" [label=\""
			  +convertSpecialsToEscapes(source.weight().toString())
			  +"\"]");
	    }
	    sb.append(";\r\n");
	    hm.put(source, name);
	}
	sb.append("\t// Edges\r\n");
	for (Iterator nodes=g.nodes().iterator(); nodes.hasNext();){
	    Node source = (Node)nodes.next();
	    for(Iterator succs = g.outputEdges(source).iterator(); succs.hasNext();) {
		Edge edge= (Edge)succs.next();		
		Node dest= edge.sink();
		sb.append("\t\""+hm.get(source)+"\" -> \""+hm.get(dest)+"\"");
		if (edge.hasWeight()){
		    sb.append(" [label=\""+
			      convertSpecialsToEscapes(edge.weight().toString())
			      +"\"]");
		}
		sb.append(";\r\n");
	    }
	}
	sb.append("}\r\n");
	return sb.toString();
    }

    /**
     * Converts all the special characters in <code>str</code> (like newlines
     * and quotes) to escape sequences (like \n)
     *
     * Courtesy of Nathan Kitchen
     */
    public static String convertSpecialsToEscapes(String str) {
	StringBuffer strBuf = new StringBuffer();
	for (int i = 0; i < str.length(); i++) {
	    char c = str.charAt(i);
	    switch (c) {
	    case '\n':
		strBuf.append("\\n");
		break;
	    case '\t':
		strBuf.append("\\t");
		break;
	    case '\r':
		strBuf.append("\\r");
		break;
	    case '\"':
		strBuf.append("\\\"");
		break;
	    case '\'':
		strBuf.append("\\\'");
		break;
	    case '\b':
		strBuf.append("\\b");
		break;
	    case '\f':
		strBuf.append("\\f");
		break;
	    case '\\':
		strBuf.append("\\\\");
		break;
	    default:
		strBuf.append(c);
	    }
	}
	return strBuf.toString();
    }

}

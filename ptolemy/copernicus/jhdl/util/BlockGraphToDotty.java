package ptolemy.copernicus.jhdl.util;

import soot.jimple.*;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.Block;
import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// BlockGraphToDotty
/**
Convert a Soot BlockGraph to dotty notation.
@author Michael Wirthlin
@version $Id$
@since Ptolemy II 2.0
*/
public class BlockGraphToDotty {

    /**
     * Return a string which contains the BlockGraph in dotty form
     */
    public static String convert(BlockGraph g){
	return convert(g, "NoTitle");
    }

    /**
     * Return a string which contains the BlockGraph in dotty form
     * @param ename Title of the graph
     */
    public static String convert(BlockGraph g, String ename){

	//Code copied from buildMapForBlock() in Block.java
	//Method is private so I couldn't get at it without
	//just copying it over...  :-/
	Map m = new HashMap();
        List basicBlocks = g.getBlocks();
        Iterator it = basicBlocks.iterator();
        while(it.hasNext()) {
            Block currentBlock = (Block) it.next();
            m.put(currentBlock.getHead(),  "Block " + (new Integer(currentBlock.getIndexInMethod()).toString()));
        }     

	int count=0;
	HashMap hm=new HashMap();
	StringBuffer sb = new StringBuffer();
	sb.append("//Dotfile created by HashMutableToDotty\r\n");
	sb.append("digraph "+ename+" {\r\n");
	sb.append("\t// Vertices\r\n");
	for(Iterator nodes = g.iterator();nodes.hasNext();) {
	    Block source = (Block)nodes.next();
	    String name="v" + count++;
	    sb.append("\t\""+name+"\" [label=\""
		      +convertSpecialsToEscapes(source.toString(m))
		      +"\"];\r\n");
	    hm.put(source, name);
	}
	sb.append("\t// Edges\r\n");
	for (Iterator nodes=g.iterator(); nodes.hasNext();){
	    Block source = (Block)nodes.next();
	    boolean endsWithIf = source.getTail() instanceof IfStmt;

	    //System.err.println(source.toShortString());
	    
	    for(Iterator succs = g.getSuccsOf(source).iterator(); succs.hasNext();) {
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

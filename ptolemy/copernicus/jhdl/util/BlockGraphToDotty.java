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
		      +"\"];"+MYEOL);
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
		sb.append(";"+MYEOL);
	    }
	}
	sb.append("}\r\n");
	return sb.toString();
    }

    public static void writeDotFile(String basename, BlockGraph g) {
	String filename = validFileName(basename) + ".dot";
	System.out.println("Writing "+filename);
	try {
	    FileWriter dotFile=new FileWriter(filename);
	    dotFile.write(convert(g,basename));
	    dotFile.close();
	} catch (IOException e){
	    System.out.println(e);
	}
    }

}

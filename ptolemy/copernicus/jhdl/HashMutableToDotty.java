package ptolemy.copernicus.jhdl;

import soot.toolkits.graph.*;
import java.util.*;

public class HashMutableToDotty {
  public static String convert(HashMutableDirectedGraph g, String ename){
    int count=0;
    HashMap hm=new HashMap();
    StringBuffer sb = new StringBuffer();
    sb.append("//Dotfile created by HashMutableToDotty\r\n");
    sb.append("digraph "+ename+" {\r\n");
    sb.append("\t// Vertices\r\n");
    for (Iterator nodes = g.getNodes().iterator();nodes.hasNext();) {
      Object source = nodes.next();
      String name="v" + count++;
      sb.append("\t\""+name+"\" [label=\""+source+"\"];\r\n");
      hm.put(source, name);
    }
    sb.append("\t// Edges\r\n");
    for (Iterator nodes=g.getNodes().iterator(); nodes.hasNext();){
      Object source = nodes.next();
      for (Iterator succs = g.getSuccsOf(source).iterator(); succs.hasNext();) {
	Object dest= succs.next();
	sb.append("\t\""+hm.get(source)+"\" -> \""+hm.get(dest)+"\";\r\n");
      }
    }
    sb.append("}\r\n");
    return sb.toString();
  }
}

package ptolemy.lang.java;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import ptolemy.lang.*;

class Main {
  public static void main(String[] args) {
    int numArgs = args.length;
    int fileStart = 0;
    boolean debug = false;

    if (numArgs >= 1) {
       debug = args[0].equals("-d");
       if (debug) {
          fileStart++;
       }
    }

    if (numArgs < 1) {
       System.out.println(
        "usage : ptolemy.lang.java.Main [-d] f1.java [f2.java ...]");
    }

    ApplicationUtility.enableTrace = debug;
    
    for (int f = fileStart; f < numArgs; f++) {
        StaticResolution.load(args[f], true);
        
        ApplicationUtility.trace(">declResolution on " + args[f]);
        StaticResolution.declResolution();
    }

    LinkedList unitList = StaticResolution.fullyResolvedFiles;
    
    Set nodeSet = NumberNodeVisitor.numberNodes(unitList);
    NumberDeclVisitor.numberDecls(unitList);
    
    Iterator unitItr = unitList.iterator();

    while (unitItr.hasNext()) {
        CompileUnitNode ast = (CompileUnitNode) unitItr.next();
        System.out.println(ast.toString());
    }
  }
}

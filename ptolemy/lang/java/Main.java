package ptolemy.lang.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

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
    
    Map nodeMap = NumberNodeVisitor.numberNodes(unitList);
    NumberDeclVisitor.numberDecls(unitList);
    
    Iterator unitItr = unitList.iterator();

    while (unitItr.hasNext()) {
        CompileUnitNode ast = (CompileUnitNode) unitItr.next();
        System.out.println(ast.toString());
    }   
    
    BufferedReader reader = null;
    
    reader = new BufferedReader(new InputStreamReader(System.in));    
    
    int nodeNumber;
    String lineString;  
      
    do {
       System.out.print("Enter node number to inspect (-1 to quit) : ");          
       
       try {
         lineString = reader.readLine();
       } catch (IOException e) {
         throw new RuntimeException("io error");
       }
         
       nodeNumber = Integer.parseInt(lineString);         
       
       if (nodeNumber != -1) {                          
          PropertyMap node = (PropertyMap) nodeMap.get(new Integer(nodeNumber));                    
          Interrogator.interrogate(node, reader);          
       }                           
    } while (nodeNumber != -1);      
  } 
}

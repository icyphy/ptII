/*
Main program to do static semantic analysis on one or more Java source
files. Allows inspection of the abstract syntax trees after compilation.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/  

package ptolemy.lang.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

/** Main program to do static semantic analysis on one or more Java source
 *  files. Allows inspection of the abstract syntax trees after compilation.
 *
 *  @author Jeff Tsay
 */
public class Main {

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
    
    LinkedList units = new LinkedList();
    for (int f = fileStart; f < numArgs; f++) {
        units.add(StaticResolution.load(args[f], 2));
    }
    
    Map nodeMap = NumberNodeVisitor.numberNodes(units);
    NumberDeclVisitor.numberDecls(units);
    
    Iterator unitItr = units.iterator();

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

/*
An application that parses a Java program, then reconstructs the source
program using the AST. This is used for testing purposes.

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

import ptolemy.lang.*;

public class RegenerateCode {
  public static void main(String[] args) {
    int files = args.length;
    int fileStart = 0;
    boolean debug = false;

    if (files >= 1) {
       debug = args[0].equals("-d");
       if (debug) {
          fileStart++;
          files--;
       }
    }

    if (files < 1) {
       System.out.println("usage : ptolemy.lang.java.RegenerateCode [-d] f1.java [f2.java ...]");
    }

    ApplicationUtility.enableTrace = debug;

    for (int f = 0; f < files; f++) {
        JavaParser p = new JavaParser();

        try {
          p.init(args[f + fileStart]);

        } catch (Exception e) {
          System.err.println("error opening input file " + args[f + fileStart]);
          System.err.println(e.toString());
        }

        //p.yydebug = debug;

        p.yyparse();

        CompileUnitNode ast = p.getAST();

        String codeString = (String) ast.accept(new JavaCodeGenerator());

        System.out.println("// Regenerated file : " + args[f + fileStart]);
        System.out.println(codeString);
    }
  }
}
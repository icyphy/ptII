/*
An application that writes a skeleton file with extension "j" after
parsing an input Java file.

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

import java.io.FileOutputStream;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

public class Skeleton {
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
       System.out.println("usage : ptolemy.lang.Skeleton [-d] f1.java [f2.java ...]");
    }


    for (int f = 0; f < files; f++) {
        JavaParser p = new JavaParser();

        try {
          p.init(args[f + fileStart]);

        } catch (Exception e) {
          System.err.println("error opening input file " + args[f + fileStart]);
          System.err.println(e.toString());
        }

        p.yydebug = debug;

        p.yyparse();

        CompileUnitNode ast = p.getAST();

        ast.accept(new SkeletonVisitor());

        String outCode = (String) ast.accept(new JavaCodeGenerator());

        try {
          String outFileName = args[f + fileStart];
          outFileName = outFileName.substring(0, outFileName.lastIndexOf('.'));
          outFileName += ".jskel";
          FileOutputStream outFile = null;

          outFile = new FileOutputStream(outFileName);

          outFile.write(outCode.getBytes());
        } catch (Exception e) {
          System.err.println("error opening output file "
           + args[f + fileStart]);
          System.err.println(e.toString());
        }
    }
  }
}

/*
An application that writes a skeleton file with extension "jskel" after
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
import java.io.IOException;

import ptolemy.lang.StringManip;
import ptolemy.lang.java.nodetypes.CompileUnitNode;

/** An application that writes a skeleton file with extension "jskel" after
 *  parsing an input Java file.
 *
 *  @author Jeff Tsay
 */
public class Skeleton {
    public static void main(String[] args) {
        int files = args.length;

        if (files < 1) {
            System.out.println("usage : ptolemy.lang.java.Skeleton " +
                    "[-d] [-i] f1.java [f2.java ...]");
            return;
        }

        _parseArgs(args);

        for (int f = _fileStart; f < files; f++) {

            CompileUnitNode ast = null;
            String filename = args[f];

            try {
                ast = JavaParserManip.parse(filename, _debug);
            } catch (Exception e) {
                System.err.println("error opening or parsing input file " + 
                        filename);
                e.printStackTrace();
            }

            if (_eliminateImports) {
                ast = StaticResolution.load(ast, 0);
            }

            ast.accept(new SkeletonVisitor(), null);

            if (_eliminateImports) {
                ast.accept(new FindExtraImportsVisitor(true), null);
            }

            String outCode = (String) ast.accept(new JavaCodeGenerator(),
                    null);

            String outFileName = StringManip.partBeforeLast(filename, '.') +
                ".jskel";

            try {
                FileOutputStream outFile = new FileOutputStream(outFileName);
                outFile.write(outCode.getBytes());
                outFile.close();
            } catch (IOException e) {
                System.err.println("error opening/writing/closing output file "
                        + outFileName);
                e.printStackTrace();
            }
        }
    }

    protected static boolean _parseArg(String arg) {
        _fileStart++;
        if (arg.equals("-d")) {
            _debug = true;
        } else if (arg.equals("-i")) {
            _eliminateImports = true;
        } else {
            _fileStart--; // restore fileStart to previous value
            return false; // no more options possible

        }
        return true; // more options possible
    }

    protected static void _parseArgs(String[] args) {
        int i = 0;
        int length = args.length;
        boolean moreOptions;

        do {
            moreOptions = _parseArg(args[i]);
            i++;
        } while (moreOptions && (i < length));
    }

    /** The index at which the first file to skeletonize is found in
     *  the arguments array.
     */
    protected static int _fileStart = 0;

    protected static boolean _debug = false;

    /** True if the user wants to eliminate unnecessary import statements
     *  in the skeleton output file. This takes a much longer time.
     */
    protected static boolean _eliminateImports = false;
}

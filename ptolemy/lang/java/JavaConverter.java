/*
A class that parses Java source files, and converts them
using the resulting abstract syntax trees, and a given code generator, or 
list of code generation passes.

Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

*/

package ptolemy.lang.java;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.CompileUnitNode;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import ptolemy.lang.java.StaticResolution;





//////////////////////////////////////////////////////////////////////////
//// JavaConverter
/** A class that parses Java source files, and converts them
 *  using the resulting abstract syntax trees, and a code generator, or 
 *  list of visitors (code generation passes), that is supplied
 *  in the class constructor. If no code generator is specified in
 *  the constructor then use the Java code generator. 
 *  This class is used for testing purposes. It can also be used
 *  to drive applications for converting Java source files 
 *  into other languages or formats.
 *
 *  @author Jeff Tsay, Shuvra S. Bhattacharyya 
 *  @version $Id$
 */
public class JavaConverter implements JavaStaticSemanticConstants {

    /**
     * Use the specified visitor as a single-pass code generator.
     *
     * @param visitor The visitor to use during code generation. 
     */
    public JavaConverter(JavaVisitor visitor) {
        _passList = new LinkedList();
        _passList.add(visitor);  
    }
    
    /**
     * Use the specified list of visitors as a sequence of code
     * generation passes.
     *
     * @param passList The list of passes to use during code generation. 
     */
    public JavaConverter(LinkedList passList) {
        Iterator passes = passList.iterator();
        while(passes.hasNext()) {
            if (!(passes.next() instanceof JavaVisitor)) {
                // FIXME: insert exception
            }
        }
        _passList = passList;
    }
   
     
    /**
     * Use the Java code generator to generate the output.
     */
    public JavaConverter() {
         _passList = new LinkedList();
         _passList.add(new JavaCodeGenerator());
    }

    /**
     *  Parse a list of one or more Java source files, and
     *  generate code using the resulting abstract syntax trees
     *  and the code generator pass(es) with which this
     *  object has been configured. If the list of file names
     *  is empty, the method silently does nothing, and returns
     *  an empty list.
     *
     * @param args The Java source files that are to be converted.
     * @return The return values returned by each pass on each file.
     * The pass return values for the first file are first in the list,
     * followed by the pass return values for the second file, and so on.  
     */
    public LinkedList convert(String[] args) {
        int files = args.length;
        int fileStart = 0;

        // The list of pass return results.
        LinkedList passResults = new LinkedList();

        for (int f = 0; f < files; f++) {
            JavaParser javaParser = new JavaParser();

            try {
                javaParser.init(args[f + fileStart]);

            } catch (Exception e) {
                System.err.println("error opening input file " + args[f + fileStart]);
                System.err.println(e.toString());
            }

            // Parse the Java input, and construct the abstract syntax tree.
            javaParser.yydebug = _verbose;
            javaParser.yyparse();
            CompileUnitNode ast = javaParser.getAST();

            if (_verbose) {
                System.out.println("Input Java parsed. The AST follows.");
                System.out.println(ast.toString());
            }

            ast.setProperty(IDENT_KEY, args[f + fileStart]);

            if (_performStaticResolution) {
                // Perform all three passes of static resolution
                if (_verbose) {
                    System.out.println("Beginning static resolution");
                }
                StaticResolution.loadCompileUnit(ast, 2);
                if (_verbose) {
                    System.out.println("Static resolution complete. The new AST:");
                    System.out.println(ast.toString());
                }
            }

            // Invoke the specified passes on the abstract syntax tree.
            Iterator passes = _passList.iterator();
            String resultString = new String();
            int passNumber = 1;
            Object passResult = null;
            while(passes.hasNext()) {
                Object pass = passes.next();
                if (_verbose) {
                    System.out.print("Starting Pass #" + passNumber + ": ");
                    System.out.println(pass.getClass().getName());
                }
                passResult = ast.accept((JavaVisitor)pass);
                if (_verbose) {
                    System.out.println("The AST after Pass #" + passNumber + ":");
                    System.out.println(ast.toString());
                }
                if (passResult == null) {
                    passResults.add(NullValue.instance);
                } else {
                    passResults.add(passResult);
                }
                if (_verbose && (passResult!=null)) {
                    System.out.println("The result returned from Pass #"
                            + passNumber + ":");
                    System.out.println(passResult.toString()); 
                }
                passNumber++;
            }


        }
        return passResults;
    }

    /**
     * Configure the output format and static resolution flag of
     * code generation. No configuration is required if
     * the default behavior (no verbose output and no static resolution)
     * is desired.
     * @param verbose Indicates whether or not verbose output should
     * be generated. Verbose output includes debugging output from
     * the Java parser, and resulting abstract syntax trees and
     * return values from all code generation passes.
     * @param performStaticResolution Indicates whether or not
     * static resolution should be performed prior to invoking the
     * given list of code generation passes.
     */ 
    public void configure(boolean verbose, boolean performStaticResolution) {
        _verbose = verbose;
        _performStaticResolution = performStaticResolution;
    }

    // The "back-end" code generator used to convert the input Java. 
    private LinkedList _passList;

    // Indicates whether or not verbose output format should be
    // used. This includes diagnostic output, and abstract syntax tree displays 
    // after each code generation pass.
    private boolean _verbose = false;

    // Indicates whether or not static resolution should be peformed
    // prior to invoking the given list of code generation passes.
    private boolean _performStaticResolution = false;
}

/*

An application that converts a Java class into  C source files (an
"interface header" file, a .h file and a .c file) that implement the class.

Copyright (c) 2001-2003 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.io.IOException;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

//////////////////////////////////////////////////////////////////////////
//// JavaToC
/** An application that converts a Java class (from a class file) into  C
   source files (a .h file and a .c file) that implement the class.
   The C conversion capability is highly experimental and rudimentary
   at this point, with only a limited set of Java language features
   supported. We are actively extending the set of supported features.

   @author Shuvra S. Bhattacharyya, Ankush Varma
   @version $Id$
   @since Ptolemy II 2.0
*/

public class JavaToC {

    // Private constructor to prevent instantiation of the class.
    private JavaToC() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a class name, convert the specified class to C (.c and
     *  .h files).
     *  @param classPath The classpath to use during the conversion.
     *  @param className The name of the class to translate.
     *  @param generateSingleClass Indicates whether (true) or not (false).
     *  @param verbose Whether it should output standard messages during
     *  compilation.
     *  "single class mode" should be used during the conversion
     *  (see {@link Context#getSingleClassMode()} for details).
     */
    public static void convert(String classPath, String className,
            String compileMode, boolean verbose) throws IOException {

        boolean generateSingleClass = compileMode.equals("singleClass");

        if (verbose) {
            System.out.println("JavaToC.convert(): classpath is: "
                    + classPath);
            System.out.println("Single class flag is: " + generateSingleClass);
        }

        // Initialize code generation.
        Scene.v().reset();
        Scene.v().setSootClassPath(classPath);
        Scene.v().loadClassAndSupport(className);
        RequiredFileGenerator RFG = new RequiredFileGenerator();
        OverriddenMethodGenerator.init();

        if (!generateSingleClass) {
            RFG.init(classPath, className);
        }

        HeaderFileGenerator hGenerator = new HeaderFileGenerator();
        CodeFileGenerator cGenerator = new CodeFileGenerator();
        InterfaceFileGenerator iGenerator = new InterfaceFileGenerator();

        /** Generate a main function unconditionally. Even if the
         *  class does not have a main method, such a file is useful
         *  for debugging and testing purposes.
         */
        MainFileGenerator mGenerator = new MainFileGenerator();

        if (generateSingleClass) {
            cGenerator.setSingleClassMode();
            hGenerator.setSingleClassMode();
        }

        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Generate the "interface header" file.
        String code = iGenerator.generate(sootClass);
        FileHandler.write(CNames.sanitize(className)
                + InterfaceFileGenerator.interfaceFileNameSuffix(), code);

        // Generate the .h file.
        code = hGenerator.generate(sootClass);
        FileHandler.write(CNames.sanitize(className) + ".h", code);

        // Generate the .c file.
        code = cGenerator.generate(sootClass);
        FileHandler.write(CNames.sanitize(className) + ".c", code);

        if (!generateSingleClass) {
            // Generate other required files.
            RFG.generateTransitiveClosureOf(classPath,
                            className, compileMode, verbose);

            // Generate the makefile.
            MakeFileGenerator.generateMakeFile(classPath, className);

            // Generate the file containing the wrapper for the main
            // method.
            code = mGenerator.generate(sootClass);
            FileHandler.write(CNames.sanitize(className) + "_main.c", code);

        }
    }


    /** Entry point for the JavaToC application. See {@link JavaToC} for
     *  instructions on usage.
     *  @param args Application arguments.
     */
    public static void main(String[] args) throws IOException {

        String classPath = new String(args[0]);
        String className = new String();

        //_test(classPath);

        // Default flags.
        String compileMode = new String("full");
        boolean verbose = false;

        // Actual flags.
        for(int i = 1;i<args.length; i++) {
            if (args[i].startsWith("-")) {
                // Its a flag.
                if     (args[i].equals("-v")) verbose = true;
                else if(args[i].equals("-q")) verbose = false;
                else if(args[i].equals("-singleClass"))
                    compileMode = new String("singleClass");
                else if(args[i].equals("-headersOnly"))
                    compileMode = new String("headersOnly");
                else if(args[i].equals("-full"))
                    compileMode = new String ("full");
                else if(args[i].equals("-h")) {
                    showHelp();
                    System.exit(0);
                }
                else if(args[i].equals("-lib")) {
                    if (i<args.length-1) {
                        i++;
                        System.setProperty("j2c_lib", args[i]);
                    }
                    else {
                        System.err.println(
                            "Must specify library directory for -lib option");
                    }
                }

            }
            else {
                // Its the name of a class to convert.
                className=args[i];
                convert(classPath, className, compileMode, verbose);
            }
        }

        // If no className specified
        if(className.equals("")) showHelp();

    }

    /** Prints out the help message on usage of this class and command-line
     *  arguments.
     */
    public static void showHelp() {
        System.out.println( "USAGE: java "
                + " javatoc classPath [flags] [-lib <library>] className1"
                + " [flags][className2]...\n");
        System.out.println( "Compile mode flags: "
                + "[-singleClass], [-headersOnly], [-full]");
        System.out.println( "Verbose mode flags: "
                + "[-v] for verbose, [-q] for quiet.");

        System.out.println( "help flags        : [-h] to see this message");
        System.out.println( "\nLater flags override earlier ones.");
    }

}

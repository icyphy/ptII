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

import soot.Scene;
import soot.SootClass;

import java.io.IOException;

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
     */
    public static void convert(String classPath, String className)
            throws IOException {

        boolean generateSingleClass = Options.v().get("compileMode")
            .equals("singleClass");
        boolean verbose = Options.v().getBoolean("verbose");

        if (verbose) {
            System.out.println("JavaToC.convert(): classpath is: "
                    + classPath);
            System.out.println("Single class flag is: " + generateSingleClass);
        }

        // Initialize code generation.
        Scene.v().setSootClassPath(classPath);
        Scene.v().loadClassAndSupport(className);
        RequiredFileGenerator RFG = new RequiredFileGenerator();
        OverriddenMethodGenerator.init();

        if (!generateSingleClass) {
            RFG.init(classPath, className);
        }

        HeaderFileGenerator hGenerator = new HeaderFileGenerator();
        CodeFileGenerator cGenerator = new CodeFileGenerator();
        StubFileGenerator sGenerator = new StubFileGenerator();

        if (generateSingleClass) {
            cGenerator.setSingleClassMode();
            hGenerator.setSingleClassMode();
        }

        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Generate the "interface header" file.
        String code = sGenerator.generate(sootClass);
        FileHandler.write(CNames.sanitize(className)
                + StubFileGenerator.stubFileNameSuffix(), code);

        // Generate the .h file.
        code = hGenerator.generate(sootClass);
        FileHandler.write(CNames.sanitize(className) + ".h", code);

        // Generate the .c file.
        code = cGenerator.generate(sootClass);
        FileHandler.write(CNames.sanitize(className) + ".c", code);

        if (!generateSingleClass) {
            // Generate other required files.
            RequiredFileGenerator
                    .generateTransitiveClosureOf(classPath, className);

            // Generate the makefile.
            MakeFileGenerator.generateMakeFile(classPath, className);

            // Generate the file containing the wrapper for the main
            // method.
            MainFileGenerator mGenerator = new MainFileGenerator();
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

        // Default flags.
        String compileMode = new String("full");
        boolean verbose = false;

        // Actual flags.
        for (int i = 1;i<args.length; i++) {
            if (args[i].startsWith("-")) {
                // Its a flag.

                // Call for help.
                if (args[i].equals("-h")) {
                    showHelp();
                    System.exit(0);
                }
                // Check for possible options.
                else if (Options.isValidFlag(args[i])) {
                    if (i<args.length-1) {
                        i++;
                        // Strip the leading "-" and note the option.
                        Options.v().put(args[i-1].substring(1), args[i]);
                    }
                    else {
                        System.err.println(
                                "Invalid command-line format.");
                    }
                }
            }
            else {
                // Its the name of a class to convert.
                className=args[i];

                // Autodetection of garbage collector.
                // If the gcDir option is specified, check that it exists.
                // If it does not exist, set it to "" in order to turn off
                // garbage collection.
                String gcDir = Options.v().get("gcDir");
                if (!gcDir.equals("")) {
                    if (!FileHandler.exists(gcDir)) {
                        Options.v().put("gcDir", "");
                    }
                }

                // Process the class.
                convert(classPath, className);
            }
        }

        // If no className specified
        if (className.equals("")) showHelp();

    }

    /** Prints out the help message on usage of this class and command-line
     *  arguments.
     */
    public static void showHelp() {
        System.out.println( "USAGE: java "
                + " javatoc classPath [flags] [value] [flag] [value] "
                + "... className"
                + " [flag][value] ... [flag] [value] [className2]...\n");
        System.out.println( "Command-line flags and their possible values\n"
                + "verbose (true/false)\n"
                + "compileMode (singleClass/headersOnly/full) \n"
                + "lib (path to library directory).");

        System.out.println( "help flags        : [-h] to see this message");
        System.out.println( "\nLater flags override earlier ones.");
    }

}

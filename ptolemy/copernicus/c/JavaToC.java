/*
An application that converts a Java class into  C
source files (a .h file and a .c file) that implement
the class.

Copyright (c) 2001 The University of Maryland.
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

import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;

import soot.Scene;
import soot.SootClass;

//////////////////////////////////////////////////////////////////////////
//// JavaToC
/** An application that converts a Java class (from a class file) into  C
 *  source files (a .h file and a .c file) that implement the class.
 *  The application takes two arguments, and an optional third argument.
 *  The first argument specifies the classpath to use during the Java to C
 *  translation; the second argument specifies the name of the class to translate;
 *  and the optional third argument can be used to turn on "single class
 *  mode" translation (see {@link Context#getSingleClassMode()} for details
 *  on this translation mode). To turn on single class mode, the third
 *  argument should be set to "-singleClass."
 *  <p>
 *  The C conversion capability is highly experimental and rudimentary
 *  at this point, with only a limited set of Java language features
 *  supported. We are actively extending the set of supported features.
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 */
public class JavaToC {

    // Private constructor to prevent instantiation of this class.
    private JavaToC() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Entry point for the JavaToC application. See {@link JavaToC} for
     *  instructions on usage.
     *  @param args application arguments.
     */
    public static void main(String[] args) throws Exception {

        // Initialize code generation
        String usage = "Usage: java ptolemy.lang.copernicus.c.JavaToC classpath "
               + " classname [-singleClass]";
        if ((args.length < 2) || (args.length > 3)) {
            throw new RuntimeException(usage);
        }
        Scene.v().setSootClassPath(args[0]);
        String className = args[1]; 
        HeaderFileGenerator hGenerator = new HeaderFileGenerator();
        CodeFileGenerator cGenerator = new CodeFileGenerator();
        if (args.length == 3) {      
            if (args[2].equals("-singleClass")) {
                cGenerator.setSingleClassMode();
                hGenerator.setSingleClassMode();
            } else {
                throw new RuntimeException(usage);
            }
        }
        Scene.v().loadClassAndSupport(className);
        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Generate the .h file.
        String code = hGenerator.generate(sootClass);
        PrintWriter out;
        if ((out = new PrintWriter(new FileOutputStream(className + ".h"))) == null) 
            throw new IOException("Could not create .h file.");
        out.println(code.toString());
        out.close();
        
        // Generate the .c file.
        code = cGenerator.generate(sootClass);
        if ((out = new PrintWriter(new FileOutputStream(className + ".c"))) == null) 
            throw new IOException("Could not create .h file.");
        out.println(code.toString());
        out.close();
    }
}

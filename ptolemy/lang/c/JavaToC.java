/*
An application that parses a Java source file, and outputs
an equivalent source file in C.

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

package ptolemy.lang.c;

import ptolemy.lang.java.RegenerateCode; 
import ptolemy.lang.NullValue;
import ptolemy.lang.java.ResolveNameVisitor;
import java.util.LinkedList;
import java.util.Iterator;
import ptolemy.lang.java.PackageResolutionVisitor;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;



//////////////////////////////////////////////////////////////////////////
//// JavaToC
/** An application that parses a Java source file, and then constructs 
 *  an equivalent C source file using static resolution on the resulting AST, 
 *  followed by the Ptolemy II C code generator "back end."
 *  The application takes a Java source file as its sole argument.
 *
 *  This class is used to test the Ptolemy II C code generation functionality.
 *  It can also be used as a standalone translator of arbitrary Java
 *  source files into equivalent C source files.
 *
 *  @author Shuvra S. Bhattacharyya
 *  @version $Id$
 */
public class JavaToC {
    public static void main(String[] args) throws Exception {

        // Check validity of the application's argument.
        if (args.length != 1) {
            throw new Exception("JavaToC expects exactly one argument");
        }
        else if (!args[0].endsWith(".java")) {
            throw new Exception("The specified file name must end with '.java'.");
        }

        // Set up the code generator as follows:
        // Pass 1:  Compute indentation levels for code constructs
        // Pass 2:  Generate a header file (i.e., a .h file)
        // Pass 3:  Generate equivalent C code (i.e., a .c file)
        LinkedList passList = new LinkedList();
        passList.add(new IndentationVisitor());
        passList.add(new HeaderFileGenerator());
        passList.add(new CCodeGenerator()); 
        RegenerateCode regenerator = new RegenerateCode(passList);

        // Configure the code generator to display verbose output,
        // and perform static resolution.
        regenerator.configure(true, true);

        // Generate C code from the Java files specified on the command line.
        LinkedList passResultList = regenerator.regenerate(args);


        // Write out the generated .c file.
        Object generatedCode;
        String baseName = args[0].substring(0, args[0].lastIndexOf('.'));
        if ((generatedCode = passResultList.removeLast()) == null) {
            throw new Exception("Generation of .c file has failed.");
        }
        PrintWriter out = new PrintWriter(
                new FileOutputStream(baseName + ".c")); 
        if (out==null) {
            throw new IOException("Could not create .c file.");
        }
        out.println(generatedCode.toString());
        out.close();

        // Write out the generated .h file.
        if ((generatedCode = passResultList.removeLast()) == null) {
            throw new Exception("Generation of .h file has failed.");
        }
        out = new PrintWriter(
                new FileOutputStream(baseName + ".h")); 
        if (out==null) {
            throw new IOException("Could not create .h file.");
        }
        out.println(generatedCode.toString());
        out.close();

    }
}

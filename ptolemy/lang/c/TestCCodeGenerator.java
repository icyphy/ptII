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

import ptolemy.lang.java.MyRegenerateCode; 
import ptolemy.lang.java.ResolveNameVisitor;
import java.util.LinkedList;
import ptolemy.lang.java.PackageResolutionVisitor;


//////////////////////////////////////////////////////////////////////////
//// TestCCodeGenerator
/** An application that parses a Java source file, and then constructs 
 *  an equivalent C source file using static resolution on the resulting AST, 
 *  followed by the Ptolemy II C code generator "back end."
 *
 *  This class is used for testing purposes.
 *  It can also be used as a standalone translator of arbitrary Java
 *  source files into equivalent C source files.
 *
 *  @author Shuvra S. Bhattacharyya
 */
public class TestCCodeGenerator {
    public static void main(String[] args) {

        // Set up the code generator as follows:
        // Pass 1:  Compute indentation levels for code constructs
        // Pass 2:  Generate equivalent C code
        LinkedList passList = new LinkedList();
        passList.add(new IndentationVisitor());
        passList.add(new CCodeGenerator()); 
        MyRegenerateCode regenerator = new MyRegenerateCode(passList);

        // Configure the code generator to display verbose output,
        // and perform static resolution.
        regenerator.configure(true, true);

        // Generate C code from the Java files specified on the command line.
        regenerator.regenerate(args);
    }
}

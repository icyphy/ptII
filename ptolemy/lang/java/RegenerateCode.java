/*
An application that parses a Java source file, and regenerates
equivalent Java source code from the resulting abstract syntax tree.

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

package ptolemy.lang.java;

import java.util.LinkedList;
import java.util.Iterator;
import java.lang.RuntimeException;



//////////////////////////////////////////////////////////////////////////
//// RegenerateCode
/** 
 *  An application that parses one or more Java source files, and regenerates
 *  equivalent Java source code from the resulting abstract syntax tree.
 *  This application is used for testing purposes.
 *
 *  @author Jeff Tsay, Shuvra S. Bhattacharyya
 *  @version $Id$
 */
public class RegenerateCode {

    /** Parse one or more Java source files, regenerate the
     *  Java code using the Java code generator, and output
     *  the regenerated code to standard output.
     *  @param args The filename(s) of the Java source file(s) to be
     *  regenerated
     */
    public static void main(String[] args) {

        JavaConverter converter = new JavaConverter(new JavaCodeGenerator());

        // Parse the specified Java source files, and regenerate
        // the associated code.
        LinkedList passResultList = converter.convert(args);

        Iterator passResults = passResultList.iterator();
        int sourceFileIndex = 0;
        while (passResults.hasNext()) {
            if (sourceFileIndex >= args.length) {
                throw new RuntimeException("Number of pass results exceeds "
                    + "number of source files.\n");
            }
            else {
                System.out.println("\n\n/* Regenerated source code for '" 
                        + args[sourceFileIndex++] + "' follows. */\n\n");
                Object passResult = passResults.next();
                if (passResult == null) 
                    System.out.println("NULL CODE GENERATION RESULT"); 
                else 
                    System.out.println(passResult.toString());
            }
        }

    }
}

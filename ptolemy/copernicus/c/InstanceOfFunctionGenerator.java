/* A class that generates code that performs lookup operations to implement
 the "instanceof" operator.

 Copyright (c) 2003-2005 The University of Maryland.
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

 */
package ptolemy.copernicus.c;

import java.util.Iterator;

import soot.SootClass;

//////////////////////////////////////////////////////////////////////////
//// InstanceOfFunctionGenerator

/**
 A class that generates code that performs lookup operations to implement
 the "instanceof" operator. It generates a function for each class C that
 takes the argument as the hashNumber corresponding to some class/interface
 A and returns 1 if C is an instance of A.

 @author Ankush Varma
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ankush)
 @Pt.AcceptedRating Red (ssb)
 */
public class InstanceOfFunctionGenerator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code for a method that looks up the number corresponding
     * to the given interface/class in a table and returns 1 if this class
     * is an instance of that interface/class, 0 otherwise. Essentially, it
     * returns whether the source class can be cast as a given
     * class/interface.
     * @param source The class for which such a method needs to be
     * generated.
     * @return The code for the method.
     */
    public String generate(SootClass source) {
        StringBuffer code = new StringBuffer();

        code.append(_generateMethodDeclaration(source));
        code.append("{\n");

        code.append(_indent(1) + "switch (checkIndex)\n" + _indent(1) + "{\n");

        // If its this class itself
        code.append(_indent(2) + _comment(source.getName()));
        code.append(_indent(2) + "case " + CNames.hashNumberOf(source) + ": "
                + "return 1;\n\n");

        // Directly implemented interfaces.
        Iterator interfaces = source.getInterfaces().iterator();

        while (interfaces.hasNext()) {
            SootClass thisInterface = (SootClass) interfaces.next();
            code.append(_indent(2) + _comment(thisInterface.toString()));
            code.append(_indent(2) + "case "
                    + CNames.hashNumberOf(thisInterface) + ": "
                    + "return 1;\n\n");
        }

        code.append(_indent(2) + "default: return ");

        if (source.hasSuperclass()) {
            code.append("((PCCG_CLASS_PTR)thisClass->superclass)->instanceOf"
                    + "((PCCG_CLASS_PTR)thisClass->superclass, checkIndex);\n");
        } else {
            code.append("0;\n");
        }

        code.append(_indent(1) + "}\n"); // End switch

        code.append("}\n\n");
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Wraps a given string in comment delimiters.
    private String _comment(String code) {
        return Utilities.comment(code);
    }

    // Generate the declaration for the lookup method.
    private String _generateMethodDeclaration(SootClass source) {
        StringBuffer code = new StringBuffer();
        code.append(_comment("Method that provides \"instanceof\" lookups for "
                + source.getName())
                + _comment("Returns 1 if the given number corresponds "
                        + "to an implemented interface or a superclass."));

        String methodName = "instanceOf";
        code.append("static short " + methodName + "(PCCG_CLASS_PTR "
                + "thisClass, long int checkIndex)\n");
        return code.toString();
    }

    // Call the standard utility indent method.
    private String _indent(int level) {
        return Utilities.indent(level);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
}

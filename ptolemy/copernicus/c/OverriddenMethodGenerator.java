/*
A class that handles generation and management of Java methods that are
over-ridden by pre-defined C code.

Copyright (c) 2002 The University of Maryland.
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

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ankush@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import java.util.Iterator;
import soot.SootMethod;
import soot.Type;


/**
    A class that handles generation and management of Java methods that are
    over-ridden by pre-defined C code. The class allows conventional java
    methods to be replaced with pre-defined C code. This may be done for
    platform-specificness, correctness or performance considerations.

    @author Ankush Varma 
    @version $Id$
*/

public class OverriddenMethodGenerator {

    /** The directory containing the location of the bodies of overridden
     * methods.
     */
    public static String overriddenBodyLib = "../runtime/over_bodies/";

    /** Checks if the method is overridden.
     *
     * @return True if the method is overridden.
     */
    public static boolean isOverridden(SootMethod method) {
        // A method is overridden if the corresponding file for its body
        // does not exist in overriddenBodyLib.
        if (FileHandler.exists(overriddenBodyLib
                + CNames.functionNameOf(method) + ".c")) {
            return true;
        }
        else {
            return false;
        }
    }

    /** Returns the code for a given overridden method.
     *
     *  @param method The method for which C code is needed.
     *  @return The code for the method.
     */
    public static String getCode(SootMethod method) {
        StringBuffer code = new StringBuffer("/* Function that implements "
                + "method " + method.getSubSignature() + "*/\n");

        code.append(_getHeaderCode(method) + "{\n");
        code.append(_getBodyCode(method) + "}\n");

        return code.toString();

    }

    /** Returns the code for the header of the overridden method. This is
     * almost identical to
     * NativeMethodGenerator._getStubHeader(SootMethod).
     *
     *  @return The code for the header of the method.
     */
    private static String _getHeaderCode(SootMethod method) {

        StringBuffer code = new StringBuffer();
        Type returnType = method.getReturnType();
        int numParameters = method.getParameterCount();


        code.append(CNames.typeNameOf(returnType) + " ");
        code.append(CNames.functionNameOf(method));

        code.append("( ");

        // The first parameter is an instance of the class the
        // method belongs to, if the method is non-static.
        if (!method.isStatic()) {
            code.append(CNames.instanceNameOf(method.getDeclaringClass())
                    + " instance");

            // Put a comma if there are more parameters.
            if (numParameters > 0) {
                code.append(", ");
            }
        }

        Iterator i = method.getParameterTypes().iterator();
        int parameterIndex = 0;
        Type parameterType;
        while (i.hasNext()) {
            parameterType = (Type)i.next();
            code.append(CNames.typeNameOf(parameterType));
            code.append(" p" + parameterIndex);
            // The dummy names of the parameters are p0, p1 ... etc.
            if (parameterIndex < numParameters -1) {
                code.append(", ");// Separators.
            }
            parameterIndex++;
        }
        code.append(")");

        return code.toString();
    }


    /** Returns the code for the body of a given overridden method.
     *
     *  @return The code for the body of the method.
     */
    private static String _getBodyCode(SootMethod method) {
        // We're putting 4 leading spaces for indentation.
        String indent = "    ";
        StringBuffer code = new StringBuffer();
        code.append(indent + "/* OVERRIDDEN METHOD */\n");
        code.append(indent + "#include \""
                + overriddenBodyLib
                + CNames.functionNameOf(method) + ".c\"");
        code.append("\n");

        return code.toString();
    }
}

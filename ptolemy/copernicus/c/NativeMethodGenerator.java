/*

A class that handles generation and management of native methods.

Copyright (c) 2002-2003 The University of Maryland.
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

import soot.RefLikeType;
import soot.SootMethod;
import soot.Type;

import java.io.File;
import java.util.Iterator;


/** A class that handles generation and management of native methods. It
    uses stub files.  A "stub" file for a native method contains an
    appropriate definition and returns data of the appropriate type, but
    does not encapsulate any functionality. It is there to allow the code
    to be compiled.

    The approach taken is that when a native method's code is asked for, it
    returns the pre-defined C code for the method from a library. However,
    if the body of the method has not been defined, then it simply
    generates a stub for the method.

    The native method code is stored in a directory (nativeLib). However,
    this does not contain any body code for the methods. The body codes for
    methods are stored in the directory nativeBodyLib. If a method's body
    code exists in nativeBodyLib, then its code in nativeLib will just
    have a #include for the file containing the body code. If the method's
    body code does not exist in nativeBodyLib, then its code in nativeLib
    will be a stub.

    @author Ankush Varma
    @version $Id$
*/
public class NativeMethodGenerator {

    /** The location of the native library methods.
     */
    public static final String nativeLib = "natives/";



    /** Return the name of the file where the C code for a native method
     *  should be.
     *  @param method The method.
     *  @return The name of the file.
     */
    public static String fileContainingCodeFor(SootMethod method) {
        String fileName = CNames.functionNameOf(method)
            + ".c";

        return fileName;
    }


    /** Generates a stub file for the given native method.
     *  @param method The method for which a stub is needed.
     */
    public static void generateStub(SootMethod method) {
        if (!method.isNative()) {
            System.err.println(
                    "NativeMethodGenerator.generateStub(SootMethod):"
                    + "\n\tWARNING: "
                    + method.toString()
                    + " is not native.\n");
        }

        // Leading Comment.
        StringBuffer code = new StringBuffer(
                "/* PCCG: Function that implements native method\n"
                + method.getSignature() + "\n"
                + "*/\n");

        code.append(_getStubHeader(method) + " {\n");

        String cReturnType = CNames.typeNameOf(method.getReturnType());

        // Add a #include if the method body exists.
        if (FileHandler.exists(Options.v().get("runtimeDir")
                + "/native_bodies/"
                + fileContainingCodeFor(method))) {

            code.append(_indent(1) + "#include \"native_bodies/"
                    + fileContainingCodeFor(method)
                    + "\"\n");
        }
        // Otherwise declare and return a dummy variable of the appropriate
        // type if the method is not void.
        else {
            code.append(_indent(1) + "/* DUMMY METHOD STUB */\n");

            if (!cReturnType.equals("void")) {
                code.append(_indent(1) + cReturnType + " dummy;\n");
                Type returnType = method.getReturnType();

                if (returnType instanceof RefLikeType) {
                    code.append(_indent(1) + "dummy = NULL;\n");
                }
                else {
                    code.append(_indent(1) + "dummy = 0;\n");
                }

                code.append(_indent(1) + "return dummy;\n");
            }
        }

        // Close the function.
        code.append("}\n");

        // Create the directory for the native library if none exists.
        if (!FileHandler.exists(nativeLib)) {
            File nativesDir = new File(nativeLib);
            nativesDir.mkdirs();
        }

        // Write out to the File with the appropriate name.
        // Make sure an existing file is never overwritten.
        String fileName =  nativeLib + fileContainingCodeFor(method);
        if (!FileHandler.exists(fileName)) {
            FileHandler.write(fileName, code.toString());
        }
    }


    /** Reads the corresponding file for the native method code and returns
     * it. The file containing the method code may or may not be a stub.
     * If the file does not exist, a stub file is generated.
     *
     *  @param method The Native method for which the code is needed.
     *  @return The Code for the given method.
     */
    public static String getCode(SootMethod method) {
        // File where the code for this method should be.
        String fileName = nativeLib + fileContainingCodeFor(method);
        String code;

        if (!FileHandler.exists(fileName)) {
            generateStub(method);
        }

        code = FileHandler.readStringFromFile(fileName);

        return code;
    }

    /** Returns the location of the hand-coded native library method bodies.
     */
    public static String getNativeBodyLib() {
        return  Options.v().get("runtimeDir") + "/native_bodies/";
    }


    /** Returns the first line of the stub for the given method.
     *  @param method The method for which the stub header is needed.
     *  @return The code for the head of the stub.
     */
    private static String _getStubHeader(SootMethod method) {

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
            code.append(" n" + parameterIndex);
            // The dummy names of the parameters are n0, n1 ... etc.

            if (parameterIndex < numParameters -1) {
                code.append(", ");// Separators.
            }

            parameterIndex++;
        }
        code.append(")");

        return code.toString();
    }



    /** Return a string that generates an indentation string (a sequence
     *  of spaces) for the given indentation level. Each indentation
     *  level unit is four characters wide.
     *  @param level The indentation level.
     *  @return The indentation string that corresponds to the given
     *  indentation level.
     */
    private static String _indent(int level) {
        return Utilities.indent(level);
    }


}

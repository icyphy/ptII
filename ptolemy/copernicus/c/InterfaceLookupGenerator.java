/* A class that generates code that performs lookup operations for
  disambiguation of interfaces.

 Copyright (c) 2003 The University of Maryland.
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

@ProposedRating Red (<your email address>)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.SootClass;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// InterfaceLookupGenerator
/**
A class that generates code that performs lookup operations for
disambiguation of interfaces.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/
public class InterfaceLookupGenerator {

    /** Default Constructor.
     */
    public InterfaceLookupGenerator() {
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Generate code for a method that looks up interface method calls in
     * a table and calls the correct function accordingly.
     * @param source The class for which such a method needs to be
     * generated.
     * @return The code for the method.
     */
    public String generate(SootClass source) {
        StringBuffer code = new StringBuffer();
        StringBuffer header = new StringBuffer();
        // Nothing needs to be done if the class implements no interfaces.
        if (AnalysisUtilities.getAllInterfacesOf(source).size() <= 0) {
            return code.toString();
        }
        // Nothing needs to be done if no methods may need to be looked up.
        HashMap interfaceMethodsMap = getLookupMethods(source);

        if (interfaceMethodsMap.size() == 0) {
            return code.toString();
        }

        int indentLevel = 1;

        code.append(_generateMethodDeclaration(source));
        code.append("{\n");

        code.append(_indent(indentLevel) + "switch (methodIndex)\n");
        code.append(_indent(indentLevel++) + "{\n");

        Iterator interfaceMethods = interfaceMethodsMap.keySet().iterator();
        while (interfaceMethods.hasNext()){
            SootMethod interfaceMethod = (SootMethod)interfaceMethods.next();

            // The corresponding actual method.
            SootMethod actualMethod = (SootMethod)interfaceMethodsMap
                .get(interfaceMethod);

            if (RequiredFileGenerator.isRequired(actualMethod)
                    // We don't need to map abstract methods.
                    && !actualMethod.isAbstract()) {

                code.append(_indent(indentLevel)
                        + _comment(interfaceMethod.toString()));

                code.append(_indent(indentLevel) + "case "
                        + CNames.hashNumberOf(interfaceMethod) + ": "
                        + "return (void*) &"
                        + CNames.functionNameOf(actualMethod)
                        + ";\n\n");
            }
        }
        code.append(_indent(indentLevel) + "default: return NULL;\n");

        code.append(_indent(--indentLevel) + "}\n");

        code.append("}\n\n");
        return code.toString();
    }


    /** Returns the list of all methods that may need to be looked up.
     * Provides a mapping from the interface's method to the corresponding
     * method in the class that implements it.
     * @param source The class for which this set is to be generated.
     * @return The set of all methods that may need to be looked up.
     */
    public static HashMap getLookupMethods(SootClass source) {
        HashMap interfaceMethodMap = new HashMap();
        Iterator interfaces = AnalysisUtilities.getAllInterfacesOf(source)
            .iterator();


        while (interfaces.hasNext()){
            SootClass thisInterface = (SootClass)interfaces.next();
            Iterator methods = thisInterface.getMethods().iterator();

            while (methods.hasNext()){
                // The method in the interface.
                SootMethod method = (SootMethod) methods.next();

                // Find out whether this method is supported. Its supported
                // if the source either declares or inherits this method.
                if (source.declaresMethod(method.getSubSignature())) {
                    interfaceMethodMap.put(method,
                            source.getMethod(method.getSubSignature()));
                }
                else {
                    Iterator inheritedMethods = MethodListGenerator
                        .getInheritedMethods(source).iterator();
                    while (inheritedMethods.hasNext()) {
                        SootMethod inheritedMethod =
                            (SootMethod)inheritedMethods.next();
                        if (inheritedMethod.getSubSignature().equals(
                                method.getSubSignature())) {
                            interfaceMethodMap.put(method,inheritedMethod);
                        }
                    }
                }

            }
        }
        return interfaceMethodMap;
    }

    /** Finds out whether a class needs a lookup function.
     * @param source The class.
     * @return True if the class has at least one required method (@see
     * RequiredFileGenerator.isRequired) that is defined in an
     * interface implemented by the class.
     */
    public static boolean needsLookupFunction(SootClass source) {
        if (AnalysisUtilities.getAllInterfacesOf(source).size() <= 0) {
            return false;
        }
        // If no methods neeed to be looked up.
        if (getLookupMethods(source).size() == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    // Wraps a given string in comment delimiters.
    private String _comment(String code) {
        return Utilities.comment(code);
    }

    // Generate the declaration for the lookup method.
    private String _generateMethodDeclaration(SootClass source) {
        StringBuffer code = new StringBuffer();
        code.append(_comment("Method that provides interface lookups for "
                + source.getName())
                + _comment("Returns a pointer to the correct function"));
        String methodName = CNames.interfaceLookupNameOf(source);
        code.append("static void* " + methodName + "(long int methodIndex)\n");
        return code.toString();
    }

    // Call the standard utility indent method.
    private String _indent(int level) {
        return Utilities.indent(level);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

}

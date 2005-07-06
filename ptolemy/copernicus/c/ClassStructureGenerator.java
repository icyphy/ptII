/*  A file that generates code for the C structure corresponding to a
    class.

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.ArrayType;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;


//////////////////////////////////////////////////////////////////////////
//// ClassStructureGenerator

/**
   A file that generates code for the C structure corresponding to a class.

   @author Ankush Varma
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (<your email address>)
   @Pt.AcceptedRating Red (ssb)
*/
public class ClassStructureGenerator extends CodeGenerator {
    /** Default constructor.
     * @param context The context to use for the generated code.
     */
    public ClassStructureGenerator(Context context) {
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the structure corresponding to a class.
     * @param source The class.
     * @return The code for the C structure corresponding to this class.
     */
    public String generate(SootClass source) {
        StringBuffer code = new StringBuffer();
        String className = source.getName();

        // Structure that implements the class.
        code.append(_comment("Structure that implements Class " + className));
        code.append("struct " + CNames.classNameOf(source) + "{\n\n");

        // These are fields that are declared first because they must be
        // present in every class.
        code.append(_generateCommonFields(source));

        // Generate the method table. Constructors are included since they
        // operate on class instances.
        if (Context.getSingleClassMode()) {
            _context.setDisableImports();
        }

        String inheritedMethods = _generateMethodPointers(MethodListGenerator
                .getInheritedMethods(source),
                "Inherited/overridden methods");
        _context.clearDisableImports();

        String introducedMethods = _generateMethodPointers(MethodListGenerator
                .getNewMethods(source),
                "New public and protected methods")
            + _generateMethodPointers(MethodListGenerator.getConstructors(
                                              source), "Constructors")
            + _generateMethodPointers(MethodListGenerator.getPrivateMethods(
                                              source), "Private methods");

        if ((Context.getSingleClassMode() || inheritedMethods.equals(""))
                && introducedMethods.equals("")) {
            code.append(_comment("Empty method table"));
        } else {
            code.append(_indent(1) + "struct {\n");
            code.append(inheritedMethods + introducedMethods);
            code.append("\n" + _indent(1) + "} methods;\n");
        }

        // Generate class variables i.e. static fields.
        String staticFields = _generateStaticFields(source);

        if (!staticFields.equals("")) {
            code.append("\n" + _indent(1) + "struct {\n\n");
            code.append("\n" + _indent(2) + "/* Class variables */\n");
            code.append(staticFields);
            code.append("\n" + _indent(1) + "} classvars;\n");
        }

        // Terminator for declared type for the class as a whole.
        code.append("\n};\n\n");

        return code.toString();
    }

    /** Get the set of Array instances required by the generated class.
     * @return The set of array instances in the local context.
     */
    public HashSet getArrayInstances() {
        return _context.getArrayInstances();
    }

    /** Get the map of types required by the generated class.
     * @return The _requiredTypeMap structure created while generating this
     * class.
     */
    public HashMap getRequiredTypeMap() {
        return _requiredTypeMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////

    /** Generate the data is is needed by all classes.
     * @param source The class for which these fields are to be generated.
     * @return The code corresponding to fields in the class structure that
     * correspond to all classes.
     */
    private String _generateCommonFields(SootClass source) {
        StringBuffer code = new StringBuffer();

        // Pointer to a C string containing the name of the class.
        code.append(_indent(1) + _comment("The name of this class."));
        code.append(_indent(1) + "char* name;\n\n");

        // The size of a instances of this class. This needed for cloning
        // objects of this class.
        code.append(_indent(1)
                + _comment("The memory needed by instances of this class."));
        code.append(_indent(1) + "long instance_size;\n\n");

        // Pointer to superclass structure.
        code.append(_indent(1));

        if (source.hasSuperclass() && !Context.getSingleClassMode()
                && RequiredFileGenerator.isRequired(
                        source.getSuperclass())) {
            code.append(_comment("Pointer to superclass structure"));
            code.append(_indent(1));
            code.append(CNames.classNameOf(source.getSuperclass()));
            code.append(" ");
        } else {
            code.append(_comment("Placeholder for pointer to superclass"
                                + " structure"));
            code.append(_indent(1));
            code.append("void *");
        }

        code.append(CNames.superclassPointerName() + ";\n\n");

        // Pointer to array class. This is initialized to null here,
        // and set appropriately when the array class is created at
        // run-time (array classes are created on demand).
        // This is declared as a pointer to the struct that implements
        // java.lang.Object so that access to common members of the
        // structure (across all classes) is facilitated.
        // The pointer declaration is commented out if we are in single
        // class mode.
        code.append(_indent(1) + _comment("Pointer to array class"));

        if (Context.getSingleClassMode()) {
            code.append(_indent(1) + _openComment);
        }

        code.append(_indent(1)
                + CNames.classNameOf(Scene.v().getSootClass("java.lang.Object"))
                + " array_class;\n");

        if (Context.getSingleClassMode()) {
            code.append(_indent(1) + _closeComment);
        }

        code.append("\n");

        // Generate lookup method for diambiguation of interface calls.
        code.append(_indent(1) + _comment("Interface lookup function."));
        code.append(_indent(1) + "void* (*lookup)(long int);\n\n");

        // Generate function that resolves the "instanceof" operator.
        code.append(_indent(1)
                + _comment("Function for handling the "
                        + "\"instanceof\" operator."));
        code.append(_indent(1) + "short (*instanceOf)"
                + "(PCCG_CLASS_PTR, long int);\n\n");

        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Generate a C declaration that corresponds to a class field.
    private String _generateField(SootField field) {
        if (RequiredFileGenerator.isRequired(field)) {
            StringBuffer fieldCode = new StringBuffer(_indent(1));
            fieldCode.append(CNames.typeNameOf(field.getType()) + " ");
            _updateRequiredTypes(field.getType());

            if (field.getType() instanceof ArrayType) {
                _context.addArrayInstance(CNames.typeNameOf(field.getType()));
            }

            fieldCode.append(CNames.fieldNameOf(field));
            fieldCode.append(";\n");

            return fieldCode.toString();
        } else {
            return new String();
        }
    }

    /** Given a list of Java methods, generate code that declares function
     *  pointers corresponding to the non-static methods in the list.
     *  The format of a method pointer declaration is as follows:
     *  Do not generate pointers for static methods.
     *
     *  functionReturnType (*functionName)(paramOneType, paramTwoType, ...);
     *
     *  is inserted at the beginning, before the methods are declared.
     *  The comment is omitted if no code is produced by this method
     *  (that is, there are no non-static methods to generate pointers for).
     *
     *  @param methodList The list of methods.
     *  @param comment A comment to insert in the generated code. This comment
     *  @return Function pointer code for specified Java methods.
     */
    private String _generateMethodPointers(List methodList, String comment) {
        StringBuffer methodCode = new StringBuffer();
        final String indent = _indent(2);
        Iterator methods = methodList.iterator();
        int insertedMethods = 0;

        while (methods.hasNext()) {
            SootMethod method = (SootMethod) (methods.next());

            if (!method.isStatic() && _isDeclarable(method)) {
                if (insertedMethods == 0) {
                    methodCode.append("\n" + indent + _comment(comment));

                    // If importing of referenced include files in
                    // disabled, then place the method table in
                    // comments.
                    if (_context.getDisableImports()) {
                        methodCode.append(_indent(2) + _openComment);
                    }
                }

                methodCode.append(indent);

                methodCode.append(CNames.typeNameOf(method.getReturnType()));

                methodCode.append(" (*");
                methodCode.append(CNames.methodNameOf(method));
                methodCode.append(")(");
                methodCode.append(_generateParameterTypeList(method));
                methodCode.append(");\n");

                // The return type is required.
                _updateRequiredTypes(method.getReturnType());

                // The type of the class that declares this method.
                Type declaringClassType = method.getDeclaringClass().getType();

                if (RequiredFileGenerator.isRequired(declaringClassType)) {
                    _updateRequiredTypes(declaringClassType);
                }

                insertedMethods++;

                // Add the method's return type to the context as a
                // required type if it is an array.
                if (method.getReturnType() instanceof ArrayType) {
                    _context.addArrayInstance(CNames.typeNameOf(
                                                      method.getReturnType()));
                }
            }
        }

        if ((insertedMethods > 0) && _context.getDisableImports()) {
            methodCode.append(_indent(2) + _closeComment);
        }

        return methodCode.toString();
    }

    // Generate static field declarations.
    private String _generateStaticFields(SootClass source) {
        StringBuffer fieldCode = new StringBuffer();
        Iterator fields = source.getFields().iterator();
        int insertedFields = 0;

        while (fields.hasNext()) {
            SootField field = (SootField) (fields.next());

            if (Modifier.isStatic(field.getModifiers())
                    && RequiredFileGenerator.isRequired(field)) {
                fieldCode.append(_indent(1) + _generateField(field));
                insertedFields++;
            }
        }

        return fieldCode.toString();
    }

    /** Checks whether a given method should be declared in the class
     * structure. A method should be declared if it is a required method,
     * or if the corresponding method in any superclass is required.
     * @param method The method to check.
     * @return True if it should be declared.
     */
    private boolean _isDeclarable(SootMethod method) {
        if (RequiredFileGenerator.isRequired(method)) {
            return true;
        }

        SootClass source = method.getDeclaringClass();

        while (source.hasSuperclass()) {
            source = source.getSuperclass();

            if (source.declaresMethod(method.getSubSignature())) {
                SootMethod m = source.getMethod(method.getSubSignature());

                if (RequiredFileGenerator.isRequired(m)) {
                    return true;
                }
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    // Characters that end a comment.
    private static final String _commentEnd = "*/";

    // Characters that start a comment.
    private static final String _commentStart = "/*";

    // The end of a comment for generated code that is to be
    // commented-out.
    private static final String _closeComment =
    "**********************************" + _commentEnd + "\n";

    // The beginning of a comment for generated code that is to be
    // The beginning of a comment for generated code that is to be
    // commented-out.
    private static final String _openComment = _commentStart
    + "**********************************\n";
}

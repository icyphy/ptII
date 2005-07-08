/*
 A class that determines names of various entities to use for
 C code generation.

 Copyright (c) 2001-2005 The University of Maryland.
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

 */
package ptolemy.copernicus.c;

import java.util.HashMap;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.NullType;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;

/** A class that determines names of various entities to use for C code
 generation.

 @author Shuvra S. Bhattacharyya, Ankush Varma
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (ssb)
 @Pt.AcceptedRating Red (ssb)
 */
public class CNames {
    // Private constructor to prevent instantiation of this class.
    private CNames() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine the C name for the class-specific structure type that
     *  implements a Soot class. The class-specific structure has
     *  type "struct {@link #classNameOf(SootClass)}". Additionally,
     *  the identifier {@link #classNameOf(SootClass)} (i.e., without
     *  the struct qualifier) is defined in the generated code to be a
     *  pointer type that points to the class-specific structure.
     *  @param source The class.
     *  @return The C name for the class-specific structure type.
     */
    public static String classNameOf(SootClass source) {
        return ("C" + instanceNameOf(source));
    }

    /** Returns the C filename corresponding to a class.
     *  @param className The name of a class.
     *  @return The C fileName corresponding to this class.
     */
    public static String classNameToFileName(String className) {
        if (isSystemClass(className)) {
            return (Options.v().get("lib") + "/" + _sanitize(className)
                    .replace('.', '/'));
        } else {
            return (_sanitize(className).replace('.', '/'));
        }
    }

    /** Determine the C name for the class-specific structure variable that
     *  implements a Soot class. The type of this structure is
     *  the type pointed to by the pointer type {@link #classNameOf(SootClass)}.
     *  @param source The class.
     *  @return The C name for the class-specific structure variable.
     */
    public static String classStructureNameOf(SootClass source) {
        return ("V" + instanceNameOf(source));
    }

    /** Clear the set of local variable names.
     */
    public static void clearLocalNames() {
        _localMap = new HashMap();
    }

    /** Return the name associated with a field in a Soot class.
     *  @param field The field.
     *  @return The name.
     */
    public static String fieldNameOf(SootField field) {
        String name;

        if ((name = (String) (_nameMap.get(field))) == null) {
            // Hash the type signature to avoid naming conflicts associated
            // with names that are longer than the number of significant
            // characters in a C identifier.
            Integer prefixCode = new Integer(field.getSubSignature().hashCode());
            name = _sanitize("f" + prefixCode + "_" + field.getName());
            _nameMap.put(field, name);
        }

        return name;
    }

    /** Return the name of the C function that implements a given Soot method.
     *  @param method The method.
     *  @return The function name.
     */
    public static String functionNameOf(SootMethod method) {
        String name;

        if ((name = (String) (_functionMap.get(method))) == null) {
            if (method.isNative()) {
                /*
                 name = ((method.getDeclaringClass().getName()) + "_" +
                 method.getName()).replace('.', '_');
                 */
                String prefixBase = method.getDeclaringClass().getName()
                        + method.getSubSignature();
                Integer prefixCode = new Integer(prefixBase.hashCode());
                name = _sanitize("n" + prefixCode + "_" + method.getName());

                //f for function,  n for native
            } else {
                // Hash the class name + type signature combination to
                // avoid naming conflicts.
                String prefixBase = method.getDeclaringClass().getName()
                        + method.getSubSignature();
                Integer prefixCode = new Integer(prefixBase.hashCode());
                name = _sanitize("f" + prefixCode + "_" + method.getName());
            }

            _functionMap.put(method, name);
        }

        return name;
    }

    /** Return a number representing the hash code for this method.
     *  @param method The SootMethod for which we want the hash number.
     *  @return The number representing this hash.
     */
    public static int hashNumberOf(SootMethod method) {
        String prefixBase = method.getDeclaringClass().getName()
                + method.getSubSignature();
        return prefixBase.hashCode();
    }

    /** Return a number representing the hashCode for this class.
     * @param source The class.
     * @return The hashCode.
     */
    public static int hashNumberOf(SootClass source) {
        return source.toString().hashCode();
    }

    /** Return the include file name for a given class.
     *  @param source The class.
     *  @return The include file name.
     */
    public static String includeFileNameOf(SootClass source) {
        return _sanitize(source.getName().replace('.', '/')) + ".h";
    }

    /** Given a class, return the name of the function that implements
     *  initialization of the class, including all functionality in the
     *  static initializer for the class (if it exists), and all
     *  class-level initialization required on the C data structures that
     *  implement the class.
     *  When called, this function must be passed the address of the
     *  variable given by {@link #classStructureNameOf}.
     *  @param source The class.
     *  @return The function name.
     */
    public static String initializerNameOf(SootClass source) {
        String name;

        if ((name = (String) (_initializerMap.get(source))) == null) {
            final String suffix = "_init";
            String base = instanceNameOf(source) + suffix;
            Integer prefixCode = new Integer(base.hashCode());
            name = _sanitize("f" + prefixCode + suffix);
            _initializerMap.put(source, name);
        }

        return name;
    }

    /** Determine the C name for the instance-specific structure type that
     *  implements a Soot class. The instance-specific structure has
     *  type "struct {@link #instanceNameOf(SootClass)}". Additionally,
     *  the identifier {@link #instanceNameOf(SootClass)} (i.e., without
     *  the struct qualifier) is defined in the generated code to be a pointer
     *  type that points to the class-specific structure.
     *  @param source The Soot class.
     *  @return The C name for the instance-specific structure type.
     */
    public static String instanceNameOf(SootClass source) {
        if (_nameMap.containsKey(source)) {
            return (String) (_nameMap.get(source));
        } else {
            return _instanceNameOf(source);
        }
    }

    /** Return the C name of the method that performs lookups to
     * disambiguate interface references.
     * @param source The class for which this method needs to be generated.
     * @return The name of the lookup method.
     */
    public static String interfaceLookupNameOf(SootClass source) {
        // Same for all classes.
        return _sanitize("lookup");
    }

    /** Returns whether a given class is a System class.
     *  @param className A class.
     *  @return True if the given class is a System class.
     */
    public static boolean isSystemClass(String className) {
        if ((className.startsWith("java.")) || (className.startsWith("sun."))
                || (className.startsWith("org."))
                || (className.startsWith("com."))
                || (className.startsWith("javax."))) {
            return (true);
        } else {
            return (false);
        }
    }

    /** Return the name of a local.
     *  @param local The local.
     *  @return The name.
     */
    public static String localNameOf(Local local) {
        String name;

        if ((name = (String) (_localMap.get(local))) == null) {
            name = _sanitize("L" + local.getName());
            _localMap.put(local, name);
        }

        return name;
    }

    /** Return the name of the C structure member that represents
     *  a given Soot method. The identifier returned by this method
     *  is a member of the structure that implements the associated class
     *  (see {@link #classNameOf(SootClass)}).
     *  @param method The Soot method.
     *  @return The name.
     */
    public static String methodNameOf(SootMethod method) {
        String name;

        if ((name = (String) (_nameMap.get(method))) == null) {
            // Hash the type signature to avoid naming conflicts for overloaded
            // methods.
            Integer prefixCode = new Integer(method.getSubSignature()
                    .hashCode());
            name = _sanitize("m" + prefixCode + "_" + method.getName());
            _nameMap.put(method, name);
        }

        return name;
    }

    /** Return a version of a string with all $, <, >, or - characters
     *  replaced by _.
     *
     *  @param name The String to be converted.
     *  @return The sanitized version of this string.
     */
    public static String sanitize(String name) {
        return _sanitize(name);
    }

    /** Initialize C name generation. This method must be called once before any
     *  other method in this class is called.
     */
    public static void setup() {
        _functionMap = new HashMap();
        _initializerMap = new HashMap();
        _localMap = new HashMap();
        _nameMap = new HashMap();
    }

    /** Return the name of the class structure member that points to the
     *  superclass structure.
     *  Each structure that implements a class has as a member a pointer
     *  to the superclass. This method returns the name of this pointer
     *  member.
     *  @return The name of the pointer member.
     */
    public static String superclassPointerName() {
        return "superclass";
    }

    /** Determine the C name associated with a Soot type. For RefType types,
     *  the C name returned is the name of the instance-specific data structure.
     *  To obtain the name of the class-specific data structure associated with
     *  a RefType, see {@link #instanceNameOf(SootClass)}.
     *  @param type The type.
     *  @return The C name.
     */
    public static String typeNameOf(Type type) {
        String name = null;

        if (type instanceof RefType) {
            SootClass source = ((RefType) type).getSootClass();

            // Makes sure no references are made to non-required types that
            // are declarable in the class structure.
            if (RequiredFileGenerator.isRequired(source)) {
                name = instanceNameOf(source);
            } else {
                name = "void*";
            }
        } else if (type instanceof ArrayType) {
            name = "iA" + ((ArrayType) type).numDimensions + "_"
                    + typeNameOf(((ArrayType) type).baseType);
        }

        if (type instanceof BooleanType) {
            name = "short";
        } else if (type instanceof ByteType) {
            name = "char";
        } else if (type instanceof CharType) {
            name = "char";
        } else if (type instanceof DoubleType) {
            name = "double";
        } else if (type instanceof FloatType) {
            name = "float";
        } else if (type instanceof IntType) {
            name = "long";
        } else if (type instanceof LongType) {
            name = "long";
        } else if (type instanceof NullType) {
            name = "void*";
        } else if (type instanceof ShortType) {
            name = "short";
        } else if (type instanceof VoidType) {
            name = "void";
        } else {
            new RuntimeException("Unsupported Soot type '"
                    + type.getClass().getName() + "'");
        }

        return name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public constants                  ////

    /** The name of the runtime function or macro to be used for
     *  allocating an array.
     */
    public static final String arrayAllocateFunction = "pccg_array_allocate";

    /** The prefix of array class descriptors that correspond
     *  to primitive types. For example, if we concatenate "int"
     *  to this prefix, we get the run-time struct that represents
     *  the class of arrays of integers.
     */
    public static final String arrayClassPrefix = "PCCG_ARRAY_";

    /** The name of the runtime function or macro to be used for
     *  determining the length of an array.
     */
    public static final String arrayLengthFunction = "PCCG_ARRAY_LENGTH";

    /** The name of the runtime function or macro to be used for
     *  computing array references.
     */
    public static final String arrayReferenceFunction = "PCCG_ARRAY_ACCESS";

    /** The name of the runtime function or macro to be used for
     *  implementing the Java instanceof operator.
     */
    public static final String instanceOfFunction = "PCCG_instanceof";

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Derive a unique name for a class that is to be used as the
    //  name of the user-defined C type that implements instances of
    //  the class.
    private static String _instanceNameOf(SootClass source) {
        String CClassName;

        // Classes that are not required just become void*. This is so that
        // they can be used as arguments of methods that are declared in
        // the class structure, but not used.
        if (!RequiredFileGenerator.isRequired(source)) {
            CClassName = "void*";
        } else {
            String name = source.getName();

            // The choice of 'i' as the first letter stands for "instance."
            String className = (name.indexOf(".") < 0) ? name : name
                    .substring(name.lastIndexOf(".") + 1);
            Integer prefixCode = new Integer(name.hashCode());
            CClassName = _sanitize("i" + prefixCode.toString() + "_"
                    + className);
        }

        _nameMap.put(source, CClassName);
        return CClassName;
    }

    // Sanitize a name to be valid a C identifier.
    private static String _sanitize(String name) {
        return name.replace('-', '0').replace('<', '_').replace('>', '_')
                .replace('$', '_');
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //  Map from a Java method to the name of the C function that implements
    //  the method. Keys are of type SootMethod. Values are of type String.
    private static HashMap _functionMap;

    //  Map from a class to the name of the function that implements
    //  instance-specific initialization for the class.
    //  Keys are of type SootClass.
    //  Values are of type String.
    private static HashMap _initializerMap;

    //  Map from a local to the identifier that represents the local in the
    //  generated C code. Keys are of type Local.  Values are of type String.
    private static HashMap _localMap;

    //  Map for names associated with the C structures that implement classes
    //  and class instances. Specifically, this map provides the names in the
    //  generated C code that correspond to class instances, instance fields,
    //  and class methods.
    //  Keys are of type SootClass, SootField, or SootMethod.
    //  Values are of type String.
    private static HashMap _nameMap;
}

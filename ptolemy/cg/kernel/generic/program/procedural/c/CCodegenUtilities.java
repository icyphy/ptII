/* A utility class containing methods for c code generation.

 Copyright (c) 2007-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */

package ptolemy.cg.kernel.generic.program.procedural.c;

import java.util.Locale;

/**
A utility class used to simplify creating c templates in EmbeddedCActors.

@author Teale Fristoe
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating red (tbf)
@Pt.AcceptedRating
 */
public class CCodegenUtilities {

    /** Return a code block to include a file.
     *  @param file The name of the file.
     *  @param constant The name of the constant to check to see if the file
     *          has already been included.
     *  @return A block of codegen code to include a file.
     */
    public static String getIncludeBlock(String file, String constant) {
        String code = "#ifndefine " + constant + "\n" + "#define " + constant
                + "\n" + "#include \"" + file + "\"\n" + "#endif\n";
        return code;
    }

    /** Return a code block to delete a jni local reference.
     * @param reference The reference to delete.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to delete a jni local reference.
     */
    public static String jniDeleteLocalRef(String reference, boolean targetCpp) {
        if (targetCpp) {
            return "env->DeleteLocalRef(" + reference + ")";
        } else {
            return "(*env)->DeleteLocalRef(env, " + reference + ")";
        }
    }

    /** Return a code block to find a jni class.
     * @param className The name of the class to find.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to find a jni class.
     */
    public static String jniFindClass(String className, boolean targetCpp) {
        if (targetCpp) {
            return "env->FindClass(\"" + className + "\")";
        } else {
            return "(*env)->FindClass(env, \"" + className + "\")";
        }
    }

    /** Return a code block to get the elements of a jni array.
     * @param type The type, with a capital first letter, of the array elements.
     * @param arrayName The name of the jni array.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to get elements from a jni array.
     */
    public static String jniGetArrayElements(String type, String arrayName,
            boolean targetCpp) {
        if (targetCpp) {
            return "env->Get" + type + "ArrayElements((j"
                    + type.toLowerCase(Locale.getDefault()) + "Array)"
                    + arrayName + ", NULL)";
        } else {
            return "(*env)->Get" + type + "ArrayElements(env, " + arrayName
                    + ", NULL)";
        }
    }

    /** Return a code block to get the jni id of a Java method.
     * @param jniClass The Java class whose method to find.
     * @param name The name of the method to find.
     * @param signature The signature of the method to find.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to get the jni id of a Java method.
     * <pre>
     *      cid = (*env)-&gt;GetMethodID(env, stringClass,
     *                          "&lt;init&gt;", "([C)V");
     * </pre>
     */
    public static String jniGetMethodID(String jniClass, String name,
            String signature, boolean targetCpp) {
        if (targetCpp) {
            return "env->GetMethodID(" + jniClass + ", \"" + name + "\", \""
                    + signature + "\")";
        } else {
            return "(*env)->GetMethodID(env, " + jniClass + ", \"" + name
                    + "\", \"" + signature + "\")";
        }
    }

    /** Return a code block to get an element from a jni array.
     * @param arrayName The name of the jni array.
     * @param index The index in the jni array to find the element.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to get an element from a jni array.
     */
    public static String jniGetObjectArrayElement(String arrayName,
            String index, boolean targetCpp) {
        if (targetCpp) {
            return "env->GetObjectArrayElement(" + arrayName + ", " + index
                    + ")";
        } else {
            return "(*env)->GetObjectArrayElement(env, " + arrayName + ", "
                    + index + ")";
        }
    }

    /** Return a code block to create a new jni array.
     * @param type The type of the array.
     * @param size The number of elements of the array.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to create a new jni element.
     */
    public static String jniNewArray(String type, String size, boolean targetCpp) {
        if (targetCpp) {
            return "env->New" + type + "Array(" + size + ")";
        } else {
            return "(*env)->New" + type + "Array(env, " + size + ")";
        }
    }

    /** Return a code block to create a new Java object using jni.
     * @param objectType The type of the object.
     * @param methodID The jni id of the object's constructor.
     * @param arguments A list of arguments to the constructor.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to create a new Java object.
     */
    public static String jniNewObject(String objectType, String methodID,
            String[] arguments, boolean targetCpp) {
        String returnVal = "";
        if (targetCpp) {
            returnVal = "env->NewObject(";
        } else {
            returnVal = "(*env)->NewObject(env, ";
        }
        returnVal = returnVal.concat(objectType + ", " + methodID);
        for (String argument : arguments) {
            returnVal = returnVal.concat(", " + argument);
        }
        returnVal = returnVal.concat(")");
        return returnVal;
    }

    /** Return a code block to create a new jni object array.
     * @param size The number of elements of the array.
     * @param objectType The type of object in the array.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to create a new jni object array.
     */
    public static String jniNewObjectArray(String size, String objectType,
            boolean targetCpp) {
        if (targetCpp) {
            return "env->NewObjectArray(" + size + ", " + objectType
                    + ", NULL)";
        } else {
            return "(*env)->NewObjectArray(env, " + size + ", " + objectType
                    + ", NULL)";
        }
    }

    /** Return a code block to release elements from a jni array.
     * @param type The type of object in the array.
     * @param arrayName The name of the array.
     * @param elementsPointer The pointer to the element to remove.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to release elements from a jni array.
     */
    public static String jniReleaseArrayElements(String type, String arrayName,
            String elementsPointer, boolean targetCpp) {
        if (targetCpp) {
            return "env->Release" + type + "ArrayElements((j"
                    + type.toLowerCase(Locale.getDefault()) + "Array)"
                    + arrayName + ", " + elementsPointer + ", 0)";
        } else {
            return "(*env)->Release" + type + "ArrayElements(env, " + arrayName
                    + ", " + elementsPointer + ", 0)";
        }
    }

    /** Return a code block to set elements in a jniarray.
     * @param type The type of the array.
     * @param arrayName The name of the array.
     * @param index The index of the first element to set.
     * @param length The number of elements to set.
     * @param valuePointer The value to set the elements.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to set selements in a jni array.
     */
    public static String jniSetArrayRegion(String type, String arrayName,
            String index, String length, String valuePointer, boolean targetCpp) {
        if (targetCpp) {
            return "env->Set" + type + "ArrayRegion(" + arrayName + ", "
                    + index + ", " + length + ", " + valuePointer + ")";
        } else {
            return "(*env)->Set" + type + "ArrayRegion(env, " + arrayName
                    + ", " + index + ", " + length + ", " + valuePointer + ")";
        }
    }

    /** Return a code block to set an element in a jni object array.
     * @param arrayName The name of the object array.
     * @param index The index of the element to set.
     * @param value The value to set the element.
     * @param targetCpp Boolean indicating whether the target language is C or C++.
     * @return A string containing code to set an element in a jni object array.
     */
    public static String jniSetObjectArrayElement(String arrayName,
            String index, String value, boolean targetCpp) {
        if (targetCpp) {
            return "env->SetObjectArrayElement(" + arrayName + ", " + index
                    + ", " + value + ")";
        } else {
            return "(*env)->SetObjectArrayElement(env, " + arrayName + ", "
                    + index + ", " + value + ")";
        }
    }
}

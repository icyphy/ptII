package ptolemy.actor.lib.jjs;

/* Utilities for common procedures for interaction with Nashorn.

Copyright (c) 2014-2015 The Regents of the University of California.
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

import java.util.Map;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/** A collection of utilities for common procedures in the interaction between
 *  Java and JavaScript code through Nashorn.
 *
 * @author Marten Lohstroh
 * @see ScriptObjectMirror
 */
public class JSUtils {

    /** Attempt to safely cast the given object into a given class.
     *  If the object is null, then the cast is considered unsafe. Additionally,
     *  if the argument allowEmpty is set to false, then only consider the cast
     *  safe if the object is not empty. If the cast is safe, return the object,
     *  return null otherwise. Note that no ClassCastException is thrown by
     *  this method. If the cast fails, it returns null.
     *
     * @param obj The object to be cast.
     * @param objClass The class to cast the object to.
     * @param allowEmpty Whether empty objects are desired.
     * @return The object if the cast is successful, null otherwise.
     */
    public static <T> T castSafely(Object obj, Class<T> objClass,
            boolean allowEmpty) {
        try {
            if (obj == null) {
                return null;
            } else {

                T coerced = objClass.cast(obj);
                if (!allowEmpty) {

                    if (coerced instanceof Map && ((Map) coerced).isEmpty()) {
                        return null;
                    }
                    if (coerced.getClass().isArray()
                            && ((Object[]) coerced).length == 0) {
                        return null;
                    }
                    if (coerced instanceof String
                            && ((String) coerced).trim().equals("")) {
                        return null;
                    } else {
                        return coerced;
                    }
                } else {
                    return coerced;
                }
            }
        } catch (ClassCastException e) {
            return null;
        }
    }

    /** Test whether a given object can be safely cast into a given class.
     *  If the object is null, then the cast is considered unsafe.
     *
     *  If the cast is safe, return true, return false otherwise.
     *  Note that no ClassCastException is thrown by this method.
     *  If the cast fails, it returns false.
     *
     * @param obj The object to be cast.
     * @param objClass The class to cast the object to.
     * @return True if the cast is successful, false otherwise.
     */
    public static <T> boolean isSafe(Object obj, Class<T> objClass) {
        return checkSafety(obj, objClass, true);
    }

    /** Test whether a given object can be safely cast into a given class.
     *  If the object is null, then the cast is considered unsafe. Moreover,
     *  only if the object is not empty the cast is considered safe.
     *
     *  If the cast is safe, return true, return false otherwise.
     *  Note that no ClassCastException is thrown by this method.
     *  If the cast fails, it returns false.
     *
     * @param obj The object to be cast.
     * @param objClass The class to cast the object to.
     * @return True if the cast is successful, false otherwise.
     */
    public static <T> boolean isSafeNotEmpty(Object obj, Class<T> objClass) {
        return checkSafety(obj, objClass, false);
    }

    /** Test whether a given object can be safely cast into a given class.
    *  If the object is null, then the cast is considered unsafe. Additionally,
    *  if the argument allowEmpty is set to false, then only consider the cast
    *  safe if the object is not empty. If the cast is safe, return true,
    *  return false otherwise. Note that no ClassCastException is thrown by
    *  this method. If the cast fails, it returns false.
    *
    * @param obj The object to be cast.
    * @param objClass The class to cast the object to.
    * @param allowEmpty Whether empty objects are desired.
    * @return True if the cast is successful, false otherwise.
    */
    public static <T> boolean checkSafety(Object obj, Class<T> objClass,
            boolean allowEmpty) {
        if (castSafely(obj, objClass, allowEmpty) == null) {
            return false;
        } else {
            return true;
        }
    }
}

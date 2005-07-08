/*

 A class that maintains context information for C code generation.

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


 */
package ptolemy.copernicus.c;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/** A class that maintains context information for C code generation.

 @author Shuvra S. Bhattacharyya
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (ssb)
 @Pt.AcceptedRating Red (ssb)
 */
public class Context {
    /** Construct an empty context. */
    public Context() {
        _includeFileSet = new HashSet();
        _stringConstantMap = new HashMap();
        _stringConstantCount = 0;
        _arrayInstanceSet = new HashSet();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an array instance to the set of array instances in the context.
     *  @param instanceName The name of the array instance.
     */
    public void addArrayInstance(String instanceName) {
        _arrayInstanceSet.add(instanceName);
    }

    /** Add an entire collection of array instances.
     * @param instances The collection of array instances.
     */
    public void addArrayInstances(Collection instances) {
        _arrayInstanceSet.addAll(instances);
    }

    /** Add an include file to the set of include files in the context.
     *  File name delimiters (double quotes or angle brackets), and the .h
     *  suffix, must be included in the argument.
     *  @param fileName The name of the include file.
     */
    public void addIncludeFile(String fileName) {
        if (!_includeFileSet.contains(fileName)) {
            _includeFileSet.add(fileName);
        }
    }

    /** Reset the context to be empty. All information in the current context
     *  is discared.
     */
    public void clear() {
        _includeFileSet.clear();
        _stringConstantMap = new HashMap();
        _stringConstantCount = 0;
        _arrayInstanceSet.clear();
    }

    /** Enable importing of referenced include files.
     */
    public void clearDisableImports() {
        _disableImports = false;
    }

    /** Return an the set of array Instance names in the context.
     *  Each element is a String representing the name of the
     *  array instance.
     *  @return The set of array Instances
     */
    public HashSet getArrayInstances() {
        return _arrayInstanceSet;
    }

    /** Return true if and only if importing of referenced include files
     *  is presently disabled.
     *  @return True if and only if importing is disabled.
     */
    public boolean getDisableImports() {
        return _disableImports;
    }

    /** Return the C identifier that corresponds to a string constant in this
     *  context.
     *  @param constant The string constant.
     *  @return The C identifier.
     */
    public String getIdentifier(String constant) {
        return (String) (_stringConstantMap.get(constant));
    }

    /** Return an Iterator over the set of include files in the context.
     *  Each element in the Iterator is a String representing an include
     *  file name.
     *  Each such file name includes appropriate file name delimiters
     *  (double quotes or angle brackets), and the .h suffix.
     *  @return The Iterator.
     */
    public Iterator getIncludeFiles() {
        return _includeFileSet.iterator();
    }

    /** Return true if and only if single class mode translation is
     *  presently enabled. In single class mode, inherited methods
     *  and fields are ignored, which can greatly reduce the number of
     *  references to other classes. Single class mode is used primarily
     *  for diagnostic purposes, and for rapid testing of new code.
     *  @return True if and only if single class mode translation is enabled.
     */
    public static boolean getSingleClassMode() {
        return ((String) Options.v().get("compileMode")).equals("singleClass");
    }

    /** Return an Iterator over the set of string constants in the context.
     *  @return An Iterator over the set of string constants.
     */
    public Iterator getStringConstants() {
        return _stringConstantMap.keySet().iterator();
    }

    /** Add a new string constant to the pool of string constants if the
     *  string does not already exist in the pool. Return the C identifier
     *  for the string constant.
     *  @param value The string constant.
     *  @return The C identifier.
     */
    public String newStringConstant(String value) {
        String name;

        if ((name = (String) (_stringConstantMap.get(value))) == null) {
            name = new String("PCCG__string" + _stringConstantCount++);
            _stringConstantMap.put(value, name);
        }

        return name;
    }

    /** Disable importing of referenced include files.
     */
    public void setDisableImports() {
        _disableImports = true;
    }

    /** Turn on (enable) single class mode translation
     *  (see {@link #getSingleClassMode()}).
     */
    public static void setSingleClassMode() {
        Options.v().put("compileMode", "singleClass");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Flag that indicates whether or not importing of referenced include files
    // is disabled.
    private boolean _disableImports;

    // The set of system and runtime include files that need to be included
    // in the generated code. The include files stored in this set must
    // be delimited with double quotes or angle brackets, and must contain
    // the .h suffix.
    private HashSet _includeFileSet;

    // Count of the number of string constants that are currently in the
    // pool of string constants.
    private int _stringConstantCount;

    // The pool of string constants (literals) for the generated code.
    // Keys in this map are the string constant values
    // (the literals to be used),
    // and the values in the map are the C identifiers to use when referencing
    // the strings in the generated code. For each string constant, a static
    // string object is created in the generated code.
    private HashMap _stringConstantMap;

    //The set of array instances that need to be typedef'd
    private HashSet _arrayInstanceSet;
}

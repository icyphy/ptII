/*
  A class that extracts ordered lists of method declarations
  with an ordering convention that facilitates translation
  of methods into function pointers (e.g., for C code generation).

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

import soot.SootClass;
import soot.SootMethod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/** A class that extracts ordered lists of method declarations
    with an ordering convention that facilitates translation
    of methods into function pointers (e.g., for C code generation).
    <p>
    Specifically, the class extracts the class initializer method
    (if present); the list of constructors;
    the list of all new public
    and protected methods that are defined
    for a given class (i.e., new method definitions, excluding definitions
    that override methods in superclasses); the list of
    inherited methods; and the list of
    private methods.  For sub-classes, overridden methods in the
    inherited methods list are replaced with the overriding definitions.
    Thus, the inherited methods list is the list of 'active' definitions
    in the current scope whose declarations originated in superclasses.
    <p>
    For example, consider the following class/method combinations:
    <p>
    class C1(base class), public methods m1, m2, m3; private method p1
    <br>
    class C2(extends C1), public methods m4, m5(overrides m2)
    <br>
    class C3 (extends C2), public methods m6, m7; private methods p1, p2
    <p>
    Then the inherited methods list generated for C3 is
    <p>
    m1, m5, m3, m4;
    <p>
    the private list generated for C3 is
    <p>
    p1, p2;
    <p>
    the inherited methods list generated for C2 is
    <p>
    m1, m5, m3;
    <p>
    and the new methods list generated for C2 is
    <p>
    m4.
    <p>
    If function pointers are declared according to these orderings
    in the translated type definitions associated with C1, C2, and C3,
    then virtual functions can be implemented correctly.
    <p>
    The lists constructed by this class are lists of method declarations.
    That is, each element is of type SootMethod.

    @author Shuvra S. Bhattacharyya
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (ssb)
    @Pt.AcceptedRating Red (ssb)

*/
public class MethodListGenerator {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a class, return the class initializer method if it exists,
     *  or return null if the class does not have a class initializer method.
     *  @param source The class.
     *  @return The class initializer method.
     *  @since Ptolemy II 2.0
     */
    public static SootMethod getClassInitializer(SootClass source) {
        if (!_classInitializerMap.containsKey(source)) {
            _generate(source);
        }

        return (SootMethod) _classInitializerMap.get(source);
    }

    /** Return the list of constructors for a given class.
     *  @param source The class.
     *  @return The list of constructors.
     */
    public static LinkedList getConstructors(SootClass source) {
        if (!_constructorListMap.containsKey(source)) {
            _generate(source);
        }

        return (LinkedList) _constructorListMap.get(source);
    }

    /** Return the list of inherited methods for a given class.
     *  @param source The class.
     *  @return The list of inherited methods.
     */
    public static LinkedList getInheritedMethods(SootClass source) {
        if (!_inheritedListMap.containsKey(source)) {
            _generate(source);
        }

        return (LinkedList) _inheritedListMap.get(source);
    }

    /** Return the list of new methods for a given class.
     *  @param source The class.
     *  @return The list of new methods.
     */
    public static LinkedList getNewMethods(SootClass source) {
        if (!_newListMap.containsKey(source)) {
            _generate(source);
        }

        return (LinkedList) _newListMap.get(source);
    }

    /** Return the list of private methods for a given class.
     *  @param source The class.
     *  @return The list of private methods.
     */
    public static LinkedList getPrivateMethods(SootClass source) {
        if (!_privateListMap.containsKey(source)) {
            _generate(source);
        }

        return (LinkedList) _privateListMap.get(source);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a class, construct the lists
     *  of constructors, inherited methods, new public and protected methods,
     *  and private methods. Recursively include methods from
     *  superclasses in the inherited methods list.
     *  @param source The class.
     */
    protected static void _generate(SootClass source) {
        LinkedList constructorList = new LinkedList();
        LinkedList newList = new LinkedList();
        LinkedList privateList = new LinkedList();
        LinkedList inheritedList = new LinkedList();

        if (source.hasSuperclass()) {
            SootClass superClass = source.getSuperclass();
            inheritedList.addAll(getInheritedMethods(superClass));
            inheritedList.addAll(getNewMethods(superClass));
        }

        _classInitializerMap.put(source, null);

        Iterator methods = source.getMethods().iterator();

        while (methods.hasNext()) {
            SootMethod method = (SootMethod) (methods.next());

            String name;

            if ((name = method.getName()).indexOf('<') != -1) {
                if (name.indexOf("clinit") != -1) {
                    // Static initializer for the class.
                    // Assume that there is at most one such initializer.
                    SootMethod previousEntry = (SootMethod) (_classInitializerMap
                            .get(source));

                    if (previousEntry == null) {
                        _classInitializerMap.put(source, method);
                    } else if (previousEntry != method) {
                        throw new RuntimeException(
                                "More than one class initializer "
                                + "method found for " + source.getName() + ":\n"
                                + previousEntry.getSubSignature() + ", and "
                                + method.getSubSignature() + ".\n");
                    }
                } else if (name.indexOf("init") != -1) {
                    // (Non-static) class constructor.
                    constructorList.add(method);
                } else {
                    // Unrecognized method with name that contains '<'
                    throw new RuntimeException(
                            "Unknown type of special method: "
                            + method.getSubSignature() + " in class "
                            + source.getName());
                }
            } else {
                if (method.isPrivate()) {
                    privateList.add(method);
                } else {
                    Iterator inheritedMethods = inheritedList.iterator();
                    int inheritedMethodIndex = 0;
                    boolean found = false;

                    while (inheritedMethods.hasNext() && !found) {
                        SootMethod inheritedMethod = (SootMethod) (inheritedMethods
                                .next());

                        if (method.getSubSignature().equals(inheritedMethod
                                    .getSubSignature())) {
                            found = true;
                        } else {
                            inheritedMethodIndex++;
                        }
                    }

                    if (found) {
                        // The method overrides a previously defined method
                        inheritedList.set(inheritedMethodIndex, method);
                    } else {
                        // New methods that are not required should be
                        // discarded. If the corresponding method in a
                        // subclass is required, it should be "new" for
                        // that subclass. This applies when pruning is
                        // enabled.
                        if (RequiredFileGenerator.isRequired(method)) {
                            newList.add(method);
                        }
                    }
                }
            }
        }

        // JDK1.4.1 seems to have a number of methods that are inherited
        // from interfaces, but not implemented in the class. These methods
        // need to be inherited too.
        // First we create a list of the subSignatures of all inherited
        // methods.
        /*
          HashSet subSignatures = new HashSet();
          Iterator inheritedMethods = (Iterator)inheritedList.listIterator();
          while (inheritedMethods.hasNext()) {
          SootMethod inheritedMethod = (SootMethod)inheritedMethods.next();
          subSignatures.add(inheritedMethod.getSubSignature());
          }
          // Iterate over all the interfaces this class implements.
          // FIXME: There must be a faster implementation than a
          // triple-nested loop.
          // FIXME: Methods end up duplicated. Need to take care of that.
          Iterator interfaces = source.getInterfaces().iterator();
          while (interfaces.hasNext()) {
          SootClass thisInterface = (SootClass)interfaces.next();

          // Iterate over each method in each interface.
          Iterator interfaceMethods = thisInterface.getMethods().iterator();
          while (interfaceMethods.hasNext()) {
          SootMethod thisMethod = (SootMethod)interfaceMethods.next();
          // Prevent the method from being listed as "inherited" if
          // its declared by this class itself, or if its
          // subSignature is already present.
          String thisSubsignature = thisMethod.getSubSignature();
          if (!source.declaresMethod(thisSubsignature)
          && !subSignatures.contains(thisSubsignature)) {
          inheritedList.addLast(thisMethod);
          subSignatures.add(thisSubsignature);
          }
          }
          }
        */
        _constructorListMap.put(source, constructorList);
        _inheritedListMap.put(source, inheritedList);
        _newListMap.put(source, newList);
        _privateListMap.put(source, privateList);
    }

    // Private constructor to prevent instantiation of the class.
    private MethodListGenerator() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Mapping from classes to lists of class initializer methods.
    // The keys are of type SootClass, and the values are either null
    // (indicating the absence of a class initializer method), or
    // of type SootMethod.
    private static HashMap _classInitializerMap = new HashMap();

    // Mapping from classes to lists of constructors.
    // The keys are of type SootClass, and the values are of type LinkedList.
    // Each element of each LinkedList is a SootMethod.
    private static HashMap _constructorListMap = new HashMap();

    // Mapping from classes to lists of inherited methods.
    // The keys are of type SootClass, and the values are of type LinkedList.
    // Each element of each LinkedList is a SootMethod.
    private static HashMap _inheritedListMap = new HashMap();

    // Mapping from classes to lists of new methods.
    // The keys are of type SootClass, and the values are of type LinkedList.
    // Each element of each LinkedList is a SootMethod.
    private static HashMap _newListMap = new HashMap();

    // Mapping from classes to lists of private methods.
    // The keys are of type SootClass, and the values are of type LinkedList.
    // Each element of each LinkedList is a SootMethod.
    private static HashMap _privateListMap = new HashMap();
}

/* This class provides utilities for analyzing classes, methods and fields.

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

@ProposedRating Red (ankush@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

package ptolemy.copernicus.c;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// AnalysisUtilities
/**
This class provides utilities for analyzing classes, methods and fields.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/
public class AnalysisUtilities{
    /** Returns the set of classes needed by a field. These are the
     * declaring class of the field, and the type class (if any)  of the field.
     * @param field The field.
     * @return The set of classes required to define the field.
     */
    public static LinkedList classesRequiredBy(SootField field) {
        LinkedList classes = new LinkedList();

        classes.add(field.getDeclaringClass());

        Type type = field.getType();
        if (type instanceof ArrayType) {
            type = ((ArrayType)type).getElementType();
        }

        if (type instanceof RefType) {
            SootClass source = ((RefType)type).getSootClass();
            if (!classes.contains(source)) {
                classes.add(source);
            }
        }

        return classes;
    }

    /** Return the set of interfaces, and all superInterfaces thereof
     * implemented in a class or any of its superclasses. This is the set
     * of all interfaces the class may be cast as.
     * @param source The class to analyze.
     * @return The set of interfaces the class may be cast as.
     */
    public static HashSet getAllInterfacesOf(SootClass source) {
        HashSet interfaceSet = new HashSet();

        while (source.hasSuperclass()) {
            interfaceSet.addAll(getSuperInterfacesOf(source));
            source = source.getSuperclass();
        }

        return interfaceSet;
    }


    /** Returns the list of classes in the arguments to a given method.
     * Also takes into account the return type of the method.
     * @param method The method to be analyzed.
     * @return The list of classes corresponding to the types of the
     * arguments to this method.
     */
    public static HashSet getArgumentClasses(SootMethod method) {
        HashSet classes = new HashSet();
        HashSet typeSet = new HashSet(method.getParameterTypes());
        typeSet.add(method.getReturnType());

        Iterator types = typeSet.iterator();
        while (types.hasNext()) {
            Type type = (Type)types.next();

            if (type instanceof ArrayType) {
                type = ((ArrayType)type).getElementType();
            }

            if (type instanceof RefType) {
                SootClass source = ((RefType)type).getSootClass();
                if (!classes.contains(source)) {
                    classes.add(source);
                }
            }
        }

        return classes;
    }

    /** Return the set of classes corresponding to the types of local variables
     * in the body of a given method.
     * @param method The method to analyze.
     * @return The set of classes corresponding to the types of local variables
     * in the body of this method.
     */
    public static HashSet getLocalTypeClasses(SootMethod method) {
        HashSet classes = new HashSet();

        if (method.isConcrete()
                && !OverriddenMethodGenerator.isOverridden(method)) {

            Iterator locals = method.retrieveActiveBody().getLocals()
                .iterator();

            while (locals.hasNext()) {
                Local local = (Local) locals.next();
                if (local.getType() instanceof RefType) {
                    RefType type = (RefType)local.getType();
                    classes.add(type.getSootClass());
                }
            }
        }

        return classes;
    }

    /** Return the list of interfaces, and all superInterfaces of these
     * interfaces implemented by a given class.
     * @param source The class to analyze.
     * @return The list of interfaces it can support.
     */
    public static HashSet getSuperInterfacesOf(SootClass source) {
        HashSet interfaceSet = new HashSet();
        LinkedList gray = new LinkedList();

        gray.addAll(source.getInterfaces());

        while (!gray.isEmpty()) {
            SootClass s = (SootClass)gray.getFirst();
            if (s.isInterface()) {
                Iterator classes = s.getInterfaces().iterator();

                while (classes.hasNext()) {
                    SootClass superclass = (SootClass) classes.next();
                    if (! interfaceSet.contains(superclass)
                            && !gray.contains(superclass)) {
                        gray.addLast(superclass);
                    }
                }

            }

            interfaceSet.add(s);
            gray.remove(s);
        }

        return interfaceSet;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                       protected fields                    ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

}

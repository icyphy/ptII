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

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import soot.Type;
import soot.RefType;
import soot.ArrayType;

import soot.Unit;

import soot.jimple.Stmt;
import soot.jimple.FieldRef;

import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;

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
     * @param The set of classes required to define the field.
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

    /** Returns the set of fields called by a given method.
     * @param method The method to be analyzed.
     * @return The set of fields accessed by this method.
     */
    public static HashSet getFieldsAccessedBy(SootMethod method) {
        HashSet fields = new HashSet();
        if (method.isConcrete()
                && (!OverriddenMethodGenerator.isOverridden(method))) {
            Iterator units = method.retrieveActiveBody()
                    .getUnits().iterator();
            while (units.hasNext()) {
                Unit unit = (Unit)units.next();
                if (unit instanceof Stmt) {
                    if (((Stmt)unit).containsFieldRef()) {
                        FieldRef fieldRef = (FieldRef)((Stmt)unit)
                                .getFieldRef();
                        SootField field = fieldRef.getField();
                        fields.add(field);
                    }
                }
            }
        }
        return fields;
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

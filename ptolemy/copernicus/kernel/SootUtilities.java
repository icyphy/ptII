/* Utilities to use with Soot

 Copyright (c) 2001 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.kernel;

import soot.Body;
import soot.Hierarchy;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/*
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
*/
//////////////////////////////////////////////////////////////////////////
//// SootUtilities
/**
This class consists of static utility methods for use with Soot

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class SootUtilities {

    // Merge the given class with its super class.  All of the methods and
    // fields of the super class will be added to the given class, if
    // they do not already exist.  Methods existing in both the given class
    // and the super class will be merged by inlining the super class method.
    public static void foldClass(SootClass theClass) {
        SootClass superClass = theClass.getSuperclass();
        // System.out.println("folding " + theClass + " into " + superClass);
        Scene.v().setActiveHierarchy(new Hierarchy());

        // Copy the interface declarations
        theClass.getInterfaces().addAll(superClass.getInterfaces());

        // Copy the field declarations.
        _copyAndReplaceFields(theClass, superClass);

        // Now create new methods in the given class for methods that
        // exist in the super class, but not in the given class.
        // Invoke the super class.
        for(Iterator methods = superClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod oldMethod = (SootMethod)methods.next();
            if(theClass.declaresMethod(oldMethod.getSubSignature()))
                continue;
            SootMethod newMethod = new SootMethod(oldMethod.getName(),
                    oldMethod.getParameterTypes(),
                    oldMethod.getReturnType(),
                    oldMethod.getModifiers());
            theClass.addMethod(newMethod);
            JimpleBody newBody = Jimple.v().newBody(newMethod);
            newMethod.setActiveBody(newBody);
            newBody.insertIdentityStmts();
            //System.out.println("method = " + newMethod);
            //System.out.println("oldMethod = " + oldMethod);
            // Call the super method
            Chain units = newBody.getUnits();
            // get a list of the locals that reference
            // the parameters of the
            // constructor.
            List parameterList = new ArrayList();
            parameterList.addAll(newBody.getLocals());

            // Invoke the method...
            // handling static and void methods differently
            if(oldMethod.getReturnType() == VoidType.v()) {
                InvokeExpr invokeExpr;
                if(newMethod.isStatic()) {
                    invokeExpr = Jimple.v().newStaticInvokeExpr(
                            oldMethod, parameterList);
                } else {
                    Local thisLocal = newBody.getThisLocal();
                    parameterList.remove(thisLocal);
                    invokeExpr = Jimple.v().newVirtualInvokeExpr(
                            thisLocal, oldMethod, parameterList);
                }
                units.add(Jimple.v().newInvokeStmt(invokeExpr));
                // return void
                units.add(Jimple.v().newReturnVoidStmt());
            } else {
                InvokeExpr invokeExpr;
                // Create a new local for the return value.
                Local returnValueLocal = Jimple.v().newLocal("returnValue",
                        oldMethod.getReturnType());
                newBody.getLocals().add(returnValueLocal);
                if(newMethod.isStatic()) {
                    invokeExpr = Jimple.v().newStaticInvokeExpr(
                            oldMethod, parameterList);

                } else {
                    Local thisLocal = newBody.getThisLocal();
                    parameterList.remove(thisLocal);
                    invokeExpr = Jimple.v().newVirtualInvokeExpr(
                            thisLocal, oldMethod, parameterList);
                }
                units.add(Jimple.v().newAssignStmt(
                        returnValueLocal, invokeExpr));
                // return the value
                units.add(Jimple.v().newReturnStmt(returnValueLocal));
            }
        }

        // Loop through all the methods again, this time looking for
        // method invocations on the old superClass...  Inline these calls.
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod newMethod = (SootMethod)methods.next();
            // System.out.println("newmethod = " + newMethod.getSignature());

            Body newBody = newMethod.retrieveActiveBody();

            // use a snapshotIterator since we are going to be manipulating
            // the statments.
            Iterator j = newBody.getUnits().snapshotIterator();
            while(j.hasNext()) {
                Stmt stmt = (Stmt)j.next();
                if(stmt.containsInvokeExpr()) {
                    InvokeExpr invoke = (InvokeExpr)stmt.getInvokeExpr();
                    if(invoke.getMethod().getDeclaringClass() == superClass) {
                        //System.out.println("inlining " + invoke.getMethod());
                        // Force the body of the thing we are inlining to be
                        // loaded
                        invoke.getMethod().retrieveActiveBody();
                        SiteInliner.inlineSite(invoke.getMethod(),
                                stmt, newMethod);
                    }
                }
            }
        }

        // And now replace any remaining references to the super class.
        changeTypesInMethods(theClass, superClass, theClass);

        theClass.setSuperclass(superClass.getSuperclass());
    }

    public static SootClass copyClass(SootClass oldClass,
            String newClassName) {
	//System.out.println("SootClass.copyClass(" + oldClass + ", "
	//		   + newClassName + ")");
        // Create the new Class
        SootClass newClass = new SootClass(newClassName,
                    Modifier.PUBLIC);
	try {
	    Scene.v().addClass(newClass);
        } catch (RuntimeException runtime) {
	    throw new RuntimeException("Perhaps you are calling the same "
				       + "transform twice?: " + runtime);
	}
        // Set the Superclass.
        newClass.setSuperclass(oldClass.getSuperclass());

        // Copy the interface declarations
        newClass.getInterfaces().addAll(oldClass.getInterfaces());

        // Copy the fields.
        _copyAndReplaceFields(newClass, oldClass);

        // Copy the methods.
        Iterator methods = oldClass.getMethods().iterator();
        while(methods.hasNext()) {
            SootMethod oldMethod = (SootMethod)methods.next();

            SootMethod newMethod = new SootMethod(oldMethod.getName(),
                    oldMethod.getParameterTypes(),
                    oldMethod.getReturnType(),
                    oldMethod.getModifiers(),
                    oldMethod.getExceptions());
            newClass.addMethod(newMethod);
            JimpleBody body = Jimple.v().newBody(newMethod);
            body.importBodyContentsFrom(oldMethod.retrieveActiveBody());
            newMethod.setActiveBody(body);
        }

        changeTypesOfFields(newClass, oldClass, newClass);
        changeTypesInMethods(newClass, oldClass, newClass);
        return newClass;
    }

    /** Copy all the fields into the given class from the given old class.
     *  Replace fields of type oldClass with fields of type newClass.
     */
    private static void _copyAndReplaceFields(SootClass newClass,
            SootClass oldClass) {
        Iterator fields = oldClass.getFields().iterator();
        while(fields.hasNext()) {
            SootField oldField = (SootField)fields.next();
            SootField newField = new SootField(oldField.getName(),
                    oldField.getType(),
                    oldField.getModifiers());
            newClass.addField(newField);
        }
    }

    /** Search through all the fields in the given class and if the
     *  field is of class oldClass, then change it to newClass.
     *  @param theClass The class containing fields to modify.
     *  @param oldClass The class to replace.
     *  @param newClass The new class.
     */
    public static void changeTypesOfFields(SootClass theClass,
            SootClass oldClass, SootClass newClass) {
        Iterator fields = theClass.getFields().iterator();
        while(fields.hasNext()) {
            SootField oldField = (SootField)fields.next();
            Type type = oldField.getType();
            //  System.out.println("field with type " + type);
            if(type instanceof RefType &&
                    ((RefType)type).getSootClass() == oldClass) {
                oldField.setType(RefType.v(newClass));
            }
        }
    }

    /** Search through all the methods in the given class and change
     *  all references to the old class to references to the new class.
     *  This includes field references, type casts, this references,
     *  new object instantiations and method invocations.
     *  @param theClass The class containing methods to modify.
     *  @param oldClass The class to replace.
     *  @param newClass The new class.
     */
    public static void changeTypesInMethods(
            SootClass theClass, SootClass oldClass, SootClass newClass) {
        //  System.out.println("fixing references on " + theClass);
        //  System.out.println("replacing " + oldClass + " with " + newClass);
        for(Iterator methods = theClass.getMethods().iterator();
            methods.hasNext();) {
            SootMethod newMethod = (SootMethod)methods.next();
            //   System.out.println("newmethod = " + newMethod.getSignature());

            Type returnType = newMethod.getReturnType();
            if(returnType instanceof RefType &&
                    ((RefType)returnType).getSootClass() == oldClass) {
                newMethod.setReturnType(RefType.v(newClass));
            }
            List paramTypes = new LinkedList();
            for(Iterator oldParamTypes =
                    newMethod.getParameterTypes().iterator();
                oldParamTypes.hasNext();) {
                Type type = (Type)oldParamTypes.next();
                if(type instanceof RefType &&
                        ((RefType)type).getSootClass() == oldClass) {
                    paramTypes.add(RefType.v(newClass));
                } else {
                    paramTypes.add(type);
                }
            }
            newMethod.setParameterTypes(paramTypes);

            Body newBody = newMethod.retrieveActiveBody();

            Iterator j =  newBody.getUnits().iterator();
            while(j.hasNext()) {
                Unit unit = (Unit)j.next();
                Iterator boxes = unit.getUseAndDefBoxes().iterator();
                while(boxes.hasNext()) {
                    ValueBox box = (ValueBox)boxes.next();
                    Value value = box.getValue();

                    if(value instanceof FieldRef) {
                        // Fix references to fields
                        FieldRef r = (FieldRef)value;
                        if(r.getField().getDeclaringClass() == oldClass) {
                            r.setField(newClass.getFieldByName(
                                    r.getField().getName()));
                            //   System.out.println("fieldRef = " +
                            //              box.getValue());
                        } else if(r.getField().getDeclaringClass().getName().startsWith(oldClass.getName())) {
                            SootClass changeClass =
                                _getInnerClassCopy(oldClass,
                                        r.getField().getDeclaringClass(),
                                        newClass);
                            r.setField(changeClass.getFieldByName(
                                    r.getField().getName()));
                        }
                    } else if(value instanceof CastExpr) {
                        // Fix casts
                        CastExpr r = (CastExpr)value;
                        Type type = r.getType();
                        if(type instanceof RefType) {
                            SootClass refClass =
                                ((RefType)type).getSootClass();
                            if(refClass == oldClass) {
                                r.setCastType(RefType.v(newClass));
                                //    System.out.println("newValue = " +
                                //        box.getValue());
                            } else if(refClass.getName().startsWith(
                                    oldClass.getName())) {
                                SootClass changeClass =
                                    _getInnerClassCopy(oldClass,
                                        refClass, newClass);
                                r.setCastType(RefType.v(changeClass));
                            }
                        }
                    } else if(value instanceof ThisRef) {
                        // Fix references to 'this'
                        ThisRef r = (ThisRef)value;
                        Type type = r.getType();
                        if(type instanceof RefType &&
                                ((RefType)type).getSootClass() == oldClass) {
                            box.setValue(Jimple.v().newThisRef(
                                    RefType.v(newClass)));
                        }
                    } else if(value instanceof ParameterRef) {
                        // Fix references to a parameter
                        ParameterRef r = (ParameterRef)value;
                        Type type = r.getType();
                        if(type instanceof RefType &&
                                ((RefType)type).getSootClass() == oldClass) {
                            box.setValue(Jimple.v().newParameterRef(
                                    RefType.v(newClass), r.getIndex()));
                        }
                    } else if(value instanceof InvokeExpr) {
                        // Fix up the method invokes.
                        InvokeExpr r = (InvokeExpr)value;
                        if(r.getMethod().getDeclaringClass() == oldClass) {
                            r.setMethod(newClass.getMethod(
                                    r.getMethod().getSubSignature()));
                        } else if(r.getMethod().getDeclaringClass().getName().startsWith(oldClass.getName())) {
                            SootClass changeClass =
                                _getInnerClassCopy(oldClass,
                                        r.getMethod().getDeclaringClass(),
                                        newClass);
                            r.setMethod(changeClass.getMethod(
                                    r.getMethod().getSubSignature()));
                        }
                    } else if(value instanceof NewExpr) {
                    // Fix up the object creations.
                        NewExpr r = (NewExpr)value;
                        if(r.getBaseType().getSootClass() == oldClass) {
                            r.setBaseType(RefType.v(newClass));
                            //   System.out.println("newValue = " +
                            //           box.getValue());
                        } else if(r.getBaseType().getSootClass().getName().startsWith(oldClass.getName())) {
                            SootClass changeClass =
                                _getInnerClassCopy(oldClass,
                                        r.getBaseType().getSootClass(),
                                        newClass);
                            r.setBaseType(RefType.v(changeClass));
                        }
                    }
                    //    System.out.println("value = " + value);
                    //   System.out.println("class = " +
                    //            value.getClass().getName());
                }
                //   System.out.println("unit = " + unit);
            }
        }
    }

    private static SootClass _getInnerClassCopy(
            SootClass oldOuterClass, SootClass oldInnerClass,
            SootClass newOuterClass) {

        String oldInnerClassName = oldInnerClass.getName();
        String oldInnerClassSpecifier = oldInnerClassName.substring(
                oldOuterClass.getName().length());
        //System.out.println("oldInnerClassSpecifier = " +
        //        oldInnerClassSpecifier);
        String newInnerClassName = newOuterClass.getName() +
            oldInnerClassSpecifier;
        SootClass newInnerClass;

        if(Scene.v().containsClass(newInnerClassName)) {
            newInnerClass = Scene.v().getSootClass(newInnerClassName);
        } else {
            oldInnerClass.setLibraryClass();
            //   System.out.println("copying "+ oldInnerClass +
            //           " to " + newInnerClassName);
            newInnerClass = copyClass(oldInnerClass, newInnerClassName);
            newInnerClass.setApplicationClass();
        }

        changeTypesOfFields(newInnerClass, oldOuterClass, newOuterClass);
        changeTypesInMethods(newInnerClass, oldOuterClass, newOuterClass);
        return newInnerClass;
    }

    // Get the method with the given name in the given class
    // (or one of its super classes).
    public static SootMethod searchForMethodByName(SootClass theClass,
            String name) {
        while(theClass != null) {
            if(theClass.declaresMethodByName(name)) {
                return theClass.getMethodByName(name);
            }
            theClass = theClass.getSuperclass();
            theClass.setLibraryClass();
        }
        throw new RuntimeException("Method " + name + " not found in class "
                + theClass);
    }

    // Get the method in the given class that has the given name and will
    // accept the given argument list.
    public static SootMethod getMatchingMethod(SootClass theClass,
            String name, List args) {
        boolean found = false;
        SootMethod foundMethod = null;

        Iterator methods = theClass.getMethods().iterator();

        while(methods.hasNext()) {
            SootMethod method = (SootMethod) methods.next();

            if(method.getName().equals(name) &&
                    args.size() == method.getParameterCount()) {
                Iterator parameterTypes =
                    method.getParameterTypes().iterator();
                Iterator arguments = args.iterator();
                boolean isEqual = true;
                while(parameterTypes.hasNext()) {
                    Type parameterType = (Type)parameterTypes.next();
                    Local argument = (Local)arguments.next();
                    Type argumentType = argument.getType();
                    if(argumentType != parameterType) {
                        // This is inefficient.  Full type merging is
                        // expensive and unnecessary.
                        isEqual = (parameterType == argumentType.merge(
                                parameterType, Scene.v()));
                    }
                    if(!isEqual) break;
                }
                if(isEqual && found)
                    throw new RuntimeException("ambiguous method");
                else {
                    found = true;
                    foundMethod = method;
                    break;
                }
            }
        }
        return foundMethod;
    }
}

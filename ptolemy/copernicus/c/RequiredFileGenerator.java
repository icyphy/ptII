/*
A class that generates the other required files in the
transitive closure.

Copyright (c) 2001-2003 The University of Maryland.
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
@AcceptedRating Red (ankush@eng.umd.edu)
*/



package ptolemy.copernicus.c;

import java.util.Iterator;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Collection;

import soot.Unit;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.Value;
import soot.Trap;

import soot.jimple.InvokeStmt;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;

import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;

/** A class that generates the other required files in the
    transitive closure.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/

public class RequiredFileGenerator {
    /** Initialize the Object.
     */
    public void RequiredFileGenerator() {
        _requiredMethods = new HashSet();
        _requiredClasses = new HashSet();
        _strictlyRequiredClasses = new HashSet();
    }

    /** Generate the .h files for all classes in the transitive closure of
     * the given class, and the .c files for required classes only. A class
     * is considered "required" if it contains at least one method that is
     * in the transitive closure of methods called in the main class.
     *  @param classPath The classPath.
     *  @param className The main class.
     *  @param compileMode The compilation mode.
     *  @param verbose Whether routine messages are to be generated.
     *  @exception IOException If  file I/O errors occur.
     */
    public static void generateTransitiveClosureOf(String classPath,
            String className, String compileMode, boolean verbose)
            throws IOException {
        if (!compileMode.equals("singleClass")) {
            Scene.v().setSootClassPath(classPath);
            Scene.v().loadClassAndSupport(className);

            // Generate headers for everything in the transitive closure.
            Iterator j = getRequiredClasses().iterator();
            while(j.hasNext()) {
                String nextClassName=((SootClass)j.next()).getName();
                _generateHeaders(classPath, nextClassName, compileMode,
                            verbose);
            }


            // Generate only the .c files for everything in the transitive
            // closure.
            Iterator i = getRequiredClasses().iterator();
            while (i.hasNext()) {
                String nextClassName=((SootClass)i.next()).getName();

                if (verbose) {
                    System.out.println(nextClassName);
                }

                _generateC(classPath, nextClassName, compileMode, verbose);
            }


         }
    }

    /** Returns the set of all required classes.
     *  @return The set of all required Classes.
     */
    public static Collection getRequiredClasses() {
        if (_requiredClasses == null) {
            return new HashSet();
        } else {
            return (Collection) _requiredClasses;
        }
    }

    /** Returns the set of classes that must be initialized by clinit
     *  methods, initialization functions etc.
     *
     *  @return The set of classes that must be initialized.
     */
    public static Collection getStrictlyRequiredClasses() {
        if (_strictlyRequiredClasses == null) {
            return new HashSet();
        } else {
            return (Collection) _strictlyRequiredClasses;
        }
    }

    /** Initialize and compute the required classes and methods.
     *  @param classPath The classpath.
     *  @param className The name of the class which we will take as the root
     *  from which others are called.
     */
    public void init(String classPath, String className) {
        _compute(classPath, className);

    }


    /** Returns whether a given method is required or not. Since we are
     *  generating a static library of all methods, they are
     *  all considered required. Thus this will always return true.
     *
     *  @param methodName Any method.
     *  @return True if it is a required method.
     */
    public static boolean isRequiredMethod(SootMethod method) {

        // Always returns true because we want a static library.
        return true;
        /*
        if (_requiredMethods != null) {
            return _requiredMethods.contains(method);
        }
        else {
            // It goes here if it initialization(_compute()) was not
            // done.
            return true;
        }
        */
    }

    public static HashSet generateUserClasses(StringBuffer code) {
        HashSet libSources = new HashSet();

        // Generate all source files for user classes.
        Iterator i = RequiredFileGenerator.getRequiredClasses().iterator();
        while (i.hasNext()) {
            SootClass nextClass = (SootClass)i.next();

            String name = MakeFileGenerator.classNameToMakeFileName(
                    nextClass.getName());

            // Go over each name. If it is not a system class, add it to
            // "sources" else add it to libSources.
            if (!CNames.isSystemClass(nextClass.getName())) {
                code.append("\t" + name + ".c\\\n");
            }
            else {
                libSources.add(name);
            }
        }
        return libSources;
    }

    /** Calculate which classes and methods are really needed.
        @param classPath The classpath.
        @param className The name of the class.
     */
    private static void _compute(String classPath, String className) {

        // Initialize the scene and other variables.
        _requiredMethods = new HashSet();
        _requiredClasses = new HashSet();
        _strictlyRequiredClasses = new HashSet();

        Scene.v().setSootClassPath(classPath);
        Scene.v().loadClassAndSupport(className);

        Iterator i = Scene.v().getClasses().iterator();

        String nextClassName = new String();

        while (i.hasNext())
        {
            _requiredClasses.add(i.next());
        }


        Scene.v().setMainClass(Scene.v().getSootClass(className));


        // Note all methods in the main class as required methods.
         Iterator mainMethods =
                 Scene.v().getMainClass().getMethods().iterator();

        // Note all methods possibly called by any of the main methods as
        // required methods.
         while (mainMethods.hasNext()) {
             SootMethod thisMethod = (SootMethod)mainMethods.next();
             _requiredMethods.add(thisMethod);
         }

         _growRequiredTree();

    }

    /** Gets all fields called directly by a given method.
     * @param method The method for which we want the target methods.
     * @return The collection of all methods called by this method.
     */
    private static Collection _fieldsUsedBy(SootMethod method) {
        // Set of all the called methods.
        HashSet fields = new HashSet();

        // FIXME: What about native methods?
        if(method.isConcrete()) {
            method.getDeclaringClass().setApplicationClass();
            // Iterate over all the units and see which ones use fields.
            Iterator units = ((JimpleBody)method.retrieveActiveBody()).
                    getUnits().iterator();

            while (units.hasNext()) {
                Unit unit = (Unit)units.next();
                if (unit instanceof Stmt) {
                    if (((Stmt)unit).containsFieldRef()) {
                        fields.add(((FieldRef)((Stmt)unit).
                                getFieldRef()).getField());
                    }
                }
            }
        }

        return ((Collection) fields);
    }


    /** Generate the C code for the given class.
        @param classPath
        @param className The name of the class.
        @param compileMode The compilation mode.
        @param verbose Whether routine messages should be generated.
     */
    private static void _generateC(String classPath, String className,
            String compileMode, boolean verbose) {

        // Initialize code generation.
        Scene.v().setSootClassPath(classPath);
        CodeFileGenerator cGenerator = new CodeFileGenerator();

        String code;

        cGenerator.clearSingleClassMode();

        Scene.v().loadClassAndSupport(className);
        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Make changes in the filename.
        String fileName = new String();
        fileName = CNames.classNameToFileName(className);

        // Generate the .c file.
        if (compileMode.equals("full")) {
            if (FileHandler.exists(fileName+".c")) {
                if(verbose) System.out.println( "\texists:"+fileName+".c");
            }
            else
            {
                code = cGenerator.generate(sootClass);
                FileHandler.write(fileName+".c", code);
                if(verbose) System.out.println( "\tcreated: "
                    +fileName+".c");
            }
        }
    }

    /** Generate the .h and "interface header" Files required for a given
     * class if they do not already exist.
     *  @param classPath The class path.
     *  @param className The class for which the files should be generated.
     *  @param compileMode The compilation mode.
     *  @param verbose Whether routine messages should be generated.
     */
    private static void _generateHeaders(String classPath, String className,
            String compileMode, boolean verbose) throws IOException {

        // Initialize code generation.
        Scene.v().setSootClassPath(classPath);

        HeaderFileGenerator hGenerator      = new HeaderFileGenerator();
        InterfaceFileGenerator iGenerator   = new InterfaceFileGenerator();

        String code;

        hGenerator.clearSingleClassMode();
        iGenerator.clearSingleClassMode();

        Scene.v().loadClassAndSupport(className);
        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Make changes in the filename.
        String fileName = new String();
        fileName = CNames.classNameToFileName(className);

        // Create any parent directories, if required.
        if (fileName.lastIndexOf('/')>0) {
        // The file requires some directories.
            if(verbose) {
                System.out.println(className);
            }

            File dummyFile = new File(fileName.substring(0,
                                        fileName.lastIndexOf('/')));
            dummyFile.mkdirs();
        }

        // Generate the interface header file.
        if (FileHandler.exists(fileName
                    + InterfaceFileGenerator.interfaceFileNameSuffix())) {
            if(verbose) System.out.println( "\texists: " + fileName
                + InterfaceFileGenerator.interfaceFileNameSuffix());
        }
        else {
            code = iGenerator.generate(sootClass);
            String name = fileName
                    + InterfaceFileGenerator.interfaceFileNameSuffix();

            if (! FileHandler.exists(name)) {
                FileHandler.write(name, code);
            }

            if(verbose) {
                System.out.println( "\tcreated: " + name);
            }
        }


        // Generate the .h file.
        if(FileHandler.exists(fileName+".h")) {
            if(verbose) {
                System.out.println( "\texists: " + fileName + ".h");
            }
        }
        else {
            code = hGenerator.generate(sootClass);
            FileHandler.write(fileName+".h", code);

            if(verbose) {
                System.out.println( "\tcreated: " + fileName + ".h");
            }
        }
    }

    /** Add one more "layer" of methods and classes in the
     *  breadth-first-search for required method and classes. If a class
     *  is required, its "clinit" and "init" methods are all required. If a
     *  method is required, its class is automatically required.
     *
     */
    private static void _growRequiredTree() {
        // We will recurse if any new classes or methods are added.
        boolean newMethodsAdded = false;
        boolean newClassesAdded = false;

        // Note that we only use iterators over clones. This is to prevent
        // ConcurrentModificationException from being thrown.
        // Iterate over all the classes and add their "clinit" methods to
        // the set of required methods.
        Iterator requiredClasses = ((HashSet)_strictlyRequiredClasses.clone())
                .iterator();


        while (requiredClasses.hasNext()) {
             SootClass thisClass = (SootClass)requiredClasses.next();

             if (thisClass.declaresMethod("void <clinit>()")) {
                 SootMethod initMethod =
                        thisClass.getMethod("void <clinit>()");

                if (!_requiredMethods.contains(initMethod)) {
                    newMethodsAdded = true;
                    _requiredMethods.add(initMethod);
                }
             }

             // The superclass of each class is also a required class.
             /*
             if (thisClass.hasSuperclass()) {
                 SootClass superclass = thisClass.getSuperclass();
                 if (!_strictlyRequiredClasses.contains(superclass)) {
                     _strictlyRequiredClasses.add(superclass);
                     newClassesAdded = true;
                 }
             }
             */

             // All methods of a class that correspond to methods declared
             // in interfaces are required.
             /*
             Iterator interfaces = thisClass.getInterfaces().iterator();
             while (interfaces.hasNext()) {
                 SootClass thisInterface = (SootClass)interfaces.next();

                 Iterator interfaceMethods = thisInterface.getMethods()
                        .iterator();

                 while (interfaceMethods.hasNext()) {
                    SootMethod interfaceMethod = (SootMethod)interfaceMethods
                            .next();

                    // To prevent non-concrete methods from being declared
                    // as required.
                    try {
                        _requiredMethods.add(thisClass
                                .getMethod(interfaceMethod.getSubSignature()));
                    }
                    catch (RuntimeException e) {
                    }
                 }
             }
             */
        }



        // For each method in the required set, find all the methods it
        // calls. Add these to the required set. Also find the fields it
        // accesses. All classes containing these fields are required.
        // FIXME: This is a correct, but non-optimal algorithm.
        // We clone to prevent ConcurrentModificationException.
        Iterator requiredMethodsIterator = ((HashSet)_requiredMethods.clone()).
                iterator();

        while (requiredMethodsIterator.hasNext()) {
            SootMethod thisMethod = (SootMethod)requiredMethodsIterator.next();

            // Iterate over all methods called by this method.
            Iterator targets = _methodsCalledBy(thisMethod).iterator();
            while (targets.hasNext()) {
                SootMethod targetMethod = (SootMethod)targets.next();
                if (!_requiredMethods.contains(targetMethod)) {
                    newMethodsAdded = true;
                    _requiredMethods.add(targetMethod);
                }
            }

            // Iterate over all exceptions thrown by or in  this method.
            // All classes corresponding to those exceptions are added.
            if (thisMethod.isConcrete()) {
                Iterator traps = thisMethod.retrieveActiveBody()
                        .getTraps().iterator();

                while (traps.hasNext()) {
                    _strictlyRequiredClasses.add((SootClass)
                            ((Trap)traps.next()).getException());
                }
            }

            // Iterate over all fields used by this method.
            Iterator fields = _fieldsUsedBy(thisMethod).iterator();
            while (fields.hasNext()) {
                SootClass declaringClass =
                        ((SootField)fields.next()).getDeclaringClass();

                if(!_strictlyRequiredClasses.contains(declaringClass)) {
                    newClassesAdded = true;
                    _strictlyRequiredClasses.add(declaringClass);
                }
            }

        }

        // The set of required classes is all classes that declare at least
        // one required method or field. We've already taken care of the
        // fields. Here we take care of the methods.
        requiredMethodsIterator = ((HashSet)_requiredMethods.clone())
                .iterator();
        while (requiredMethodsIterator.hasNext()) {
            SootMethod thisMethod =
                    (SootMethod)requiredMethodsIterator.next();

            SootClass declaringClass = thisMethod.getDeclaringClass();

            if (!_strictlyRequiredClasses.contains(declaringClass)) {
                newClassesAdded = true;
                _strictlyRequiredClasses.add(declaringClass);
            }
        }



        // If this call to _growRequiredTree caused any changes, another call to
        // _growRequiredTree is required.
        if (newMethodsAdded || newClassesAdded) {
            _growRequiredTree();
        }

    }

    /** Gets all methods called directly by a given method. Causes changes
     *  in Scene.v(), requiring it to be re-initialized if used after this
     *  method.
     *
     * @param method The method for which we want the target methods.
     * @return The collection of all methods called by this method.
     * FIXME: Does this need to be made more general?
     */
    private static Collection _methodsCalledBy(SootMethod method) {
        // Set of all the called methods.
        HashSet targets = new HashSet();

        /*
        Scene.v().loadClassAndSupport(method.getDeclaringClass().getName());
        Scene.v().setMainClass(method.getDeclaringClass());
        ClassHierarchyAnalysis analyser = new ClassHierarchyAnalysis();
        Scene.v().setActiveInvokeGraph(analyser.newInvokeGraph());

        Iterator methodTargets = Scene.v().getActiveInvokeGraph()
                .getTransitiveTargetsOf(method).iterator();
        while (methodTargets.hasNext()) {
            targets.add(methodTargets.next());
        }
        */

        try {
            method.getDeclaringClass().setApplicationClass();


            // Iterate over all the units and see which ones call methods.
            Iterator units = ((JimpleBody)method.retrieveActiveBody()).
                    getUnits().iterator();

            while (units.hasNext()) {
                Unit unit = (Unit)units.next();

                if (unit instanceof InvokeStmt) {
                        InvokeExpr invokeExpr = (InvokeExpr)(((InvokeStmt)unit)
                                .getInvokeExpr());

                        targets.add(invokeExpr.getMethod());

                }
                // The unit may not be an invoke statement by itself. It
                // may have an RHS thats an invoke statement. For example,
                // a = b().  DefinitionStmt includes both AssignStmt and
                // IdentityStmt.
                else if ((unit instanceof AssignStmt)) {
                    Value rightOp = ((AssignStmt)unit).getRightOp();

                    if (rightOp instanceof InvokeExpr) {
                        targets.add(((InvokeExpr)rightOp).getMethod());
                    }
                }
                else if (unit instanceof IdentityStmt) {
                    Value rightOp = ((IdentityStmt)unit).getRightOp();

                    if (rightOp instanceof InvokeExpr) {
                        targets.add(((InvokeExpr)rightOp).getMethod());

                    }
                }
            }
        }
        // In case some method cannot be analysed (for example some
        // non-concrete methods).
        catch (Exception e) {
        }

        return ((Collection) targets);

    }

    private static HashSet _requiredMethods;
    private static HashSet _requiredClasses;
    private static HashSet _strictlyRequiredClasses;

}


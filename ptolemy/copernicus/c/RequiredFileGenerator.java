/*
A class that generates the other required files in the
transitive closure.

Copyright (c) 2001 The University of Maryland.
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

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.InvokeGraph;


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
    }

    /** Initialize and compute the required classes and methods.
     *  @param classPath The classpath.
     *  @param className The name of the class which we will take as the root
     *  from which others are called.
     */
    public void init(String classPath, String className) {
        _compute(classPath, className);
    }


    /** Generate the .h files for all classes in the transitive closure of the
     *  given class, and the .c files for required classes only. A class is
     *  considered "required" if it contains atleast one method that is in the
     *  transitive closure of methods called in the main class.
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
            _compute(classPath, className);

            Scene.v().setSootClassPath(classPath);
            Scene.v().loadClassAndSupport(className);

            // Generate headers for everything in the transitive closure.

            Iterator j = Scene.v().getClasses().iterator();
            while(j.hasNext()) {
                String nextClassName=((SootClass)j.next()).getName();
                _generateHeaders(classPath, nextClassName, compileMode,
                    verbose);
                //_generateC(classPath, nextClassName, compileMode, verbose);
            }


            // Generate only the required .c files.

            Iterator i = _requiredClasses.iterator();
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
    public static Collection getRequiredClasses(String classPath, String
            className) {
        _compute(classPath, className);
        return (Collection) _requiredClasses;
    }

    /** Returns whether a given method is required or not.
        @param methodName Any method.
        @return True if it is a required method.
        A method is "required" if it is part of the active invoke graph.
        If the RequiredFileGenerator was not initialized, it'll always
        return true. All methods in the main class are automatically required.
    */
    public static boolean isRequiredMethod(SootMethod meth) {
        if (_requiredMethods != null) {
            return _requiredMethods.contains(meth);
        }
        else {
            // It goes here if it initialization(_compute()) was not
            // done.
            return true;
        }
    }


    /** Calculate which classes and methods are really needed.
        @param classPath The classpath.
        @param className The name of the class.
     */
    private static void _compute(String classPath, String className) {

         // Initialize the scene and other variables.
         _requiredMethods = new HashSet();
         _requiredClasses = new HashSet();
         Scene.v().setSootClassPath(classPath);
         Scene.v().loadClassAndSupport(className);
         Scene.v().setMainClass(Scene.v().getSootClass(className));
         ClassHierarchyAnalysis analyser = new ClassHierarchyAnalysis();
         Scene.v().setActiveInvokeGraph(analyser.newInvokeGraph());

        // Note all methods in the main class as required methods.
         Iterator mainMethodsIter =
                 Scene.v().getMainClass().getMethods().iterator();

        // Note all methods possibly called by any of the main methods as
        // required methods.
         while (mainMethodsIter.hasNext()) {
             SootMethod thisMethod = (SootMethod)mainMethodsIter.next();
             List targetList =
                 Scene.v().getActiveInvokeGraph(
                     ).getTransitiveTargetsOf(thisMethod);

             Iterator i = targetList.iterator();
             while (i.hasNext()) {
                 SootMethod newMethod = (SootMethod)i.next();
                 _requiredMethods.add(newMethod);
             }
             _requiredMethods.add(thisMethod);
         }

         // Add all required runtime methods to the list of required
         // methods.

         SootClass stringClass = Scene.v().
                loadClassAndSupport("java.lang.String");
         SootMethod initStringWithCharArray = stringClass.getMethod(
                "void <init>(char[])");
         _requiredMethods.add(initStringWithCharArray);


         // The set of required classes is all classes that declare atleast
         // one required method.
         Iterator requiredMethodsIter = _requiredMethods.iterator();

         while (requiredMethodsIter.hasNext()) {
             SootMethod thisMethod =
                     (SootMethod)requiredMethodsIter.next();

             _requiredClasses.add(thisMethod.getDeclaringClass());
         }

         // Make sure the "clinit" method is noted as required, if it
         // exists

         Iterator requiredClassesIter = _requiredClasses.iterator();
         while (requiredClassesIter.hasNext()) {
             SootClass thisClass = (SootClass)requiredClassesIter.next();

             if (thisClass.declaresMethod("void <clinit>()")) {
                 SootMethod initMethod =
                        thisClass.getMethod("void <clinit>()");
                 _requiredMethods.add(initMethod);
             }
         }
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
            else {
                code = cGenerator.generate(sootClass);
                FileHandler.write(fileName+".c", code);
                if(verbose) System.out.println( "\tcreated: "
                    +fileName+".c");
            }
        }
    }

    /** Generate the .h and "interface header" Files required for a given class.
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
            if(verbose) System.out.println(className);
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
            FileHandler.write(fileName
                    + InterfaceFileGenerator.interfaceFileNameSuffix(), code);
            if(verbose) System.out.println( "\tcreated: " + fileName
                    + InterfaceFileGenerator.interfaceFileNameSuffix());
        }


        // Generate the .h file.
        if(FileHandler.exists(fileName+".h")) {
            if(verbose) System.out.println( "\texists: " + fileName + ".h");
        }
        else {
            code = hGenerator.generate(sootClass);
            FileHandler.write(fileName+".h", code);
            if(verbose) System.out.println( "\tcreated: " + fileName + ".h");
        }
    }

    private static HashSet _requiredMethods;
    private static HashSet _requiredClasses;

}


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


/**
A class that generates the other required files in the
transitive closure.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/

public class RequiredFileGenerator {
    /**
     * dummy constructor
     */
    public void RequiredFileGenerator() {
            // dummy constructor

        }

    /**
     * Generate the .h files for all classes in the transitive closure of the
     * given class and the .c files for required classes only.
     * @param classPath The classPath.
     * @param className The class for which the transitive closure is to be
     * generated.
     * @param compileMode The compilation mode.
     * @param verbose Whether routine messages are to be generated.
     * @throws IOException On file I/O errors.
     */
    public static void generateTransitiveClosureOf(String classPath,
            String className, String compileMode, boolean verbose) 
            throws IOException {
        if (!compileMode.equals("singleClass")) {
            _compute(classPath, className);

            Scene.v().setSootClassPath(classPath);
            Scene.v().loadClassAndSupport(className);

            //generate headers for everything in the transitive closure
            Iterator j = Scene.v().getClasses().iterator();
            while(j.hasNext()) {
                String nextClassName=((SootClass)j.next()).getName();
                _generateHeaders(classPath, nextClassName, compileMode,
                    verbose);
            }

            //generate only the required .c files
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


    /**
        @return The names of all required Classes
     */
    public static Collection getRequiredClasses(String classPath, String
            className) {
        _compute(classPath, className);
        return (Collection) _requiredClasses;
    }


    /**
     * @param className The name of a class.
     * @return The C fileName corresponding to this class.
     */
    public static String classNameToFileName(String className) {
        if (isSystemClass(className)) {
            return(System.getProperty("j2c_lib")
                + "/" + className.replace('.', '/'));
        }
        else {
            return(className);
        }
    }

    /**
     * @param className A class.
     * @return True if the given class is a System class.
     */
    public static boolean isSystemClass(String className) {

        if ((className.startsWith("java."))||
                (className.startsWith("sun."))) {
            return (true);
        }
        else {
            return(false);
        }
    }


    /** Calculate which classes and methods are really needed
        @param classPath
        @param className
      */
    protected static void _compute(String classPath, String className) {
         _requiredMethods = new HashSet();
         _requiredClasses = new HashSet();
         Scene.v().setSootClassPath(classPath);
         Scene.v().loadClassAndSupport(className);
         Scene.v().setMainClass(Scene.v().getSootClass(className));
         ClassHierarchyAnalysis analyser = new ClassHierarchyAnalysis();
         Scene.v().setActiveInvokeGraph(analyser.newInvokeGraph());

         Iterator mainMethodsIter =
                 Scene.v().getMainClass().getMethods().iterator();

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
         }

         Iterator requiredMethodsIter = _requiredMethods.iterator();
         while (requiredMethodsIter.hasNext()) {

             SootMethod thisMethod =
                     (SootMethod)requiredMethodsIter.next();

             _requiredClasses.add(thisMethod.getDeclaringClass());
         }
    }

    /** Generate the C code for the given class.
        @param classPath
        @param className The name of the class.
        @param compileMode The compilation mode.
        @param verbose Whether routine messages should be generated.
    */
    protected static void _generateC(String classPath, String className,
            String compileMode, boolean verbose) {

        // Initialize code generation
        Scene.v().setSootClassPath(classPath);
        CodeFileGenerator cGenerator = new CodeFileGenerator();

        String code;

        cGenerator.clearSingleClassMode();

        Scene.v().loadClassAndSupport(className);
        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        //Make changes in the filename
        String fileName = new String();
        fileName = classNameToFileName(className);

        // Generate the .c file.
        if (compileMode.equals("full")) {
            if (FileHandler.exists(fileName+".c")) {
                if(verbose) System.out.println( "\texists:"+fileName+".c");
            }
            else {
                code = cGenerator.generate(sootClass);
                FileHandler.write(fileName+".c",code);
                if(verbose) System.out.println( "\tcreated: "
                    +fileName+".c");
            }
        }
    }

    /**
     * generate the .h and _i.h Files required for a given class
     * @param classPath The class path.
     * @param className The class for which the files should be generated.
     * @param compileMode The compilation mode.
     * @param verbose Whether routine messages should be generated.
     */
    protected static void _generateHeaders(String classPath, String className,
            String compileMode, boolean verbose) throws IOException {

        // Initialize code generation
        Scene.v().setSootClassPath(classPath);

        HeaderFileGenerator hGenerator      = new HeaderFileGenerator();
        InterfaceFileGenerator iGenerator   = new InterfaceFileGenerator();

        String code;

        hGenerator.clearSingleClassMode();
        iGenerator.clearSingleClassMode();

        Scene.v().loadClassAndSupport(className);
        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        //Make changes in the filename
        String fileName = new String();
        fileName = classNameToFileName(className);

        //create any parent directories
        if (fileName.lastIndexOf('/')>0) {
        //the file requires some directories
            if(verbose) System.out.println(className);
            File dummyFile = new File(fileName.substring(0,
                                        fileName.lastIndexOf('/')));
            dummyFile.mkdirs();
        }

        // Generate the _i.h file.
        if (FileHandler.exists(fileName+"_i.h")) {
            if(verbose) System.out.println( "\texists: "+fileName+"_i.h");
        }
        else {
            code = iGenerator.generate(sootClass);
            FileHandler.write(fileName+"_i.h",code);
            if(verbose) System.out.println( "\tcreated: "+fileName+"_i.h");
        }


        // Generate the .h file.
        if(FileHandler.exists(fileName+".h")) {
            if(verbose) System.out.println( "\texists: "+fileName+".h");
        }
        else {
            code = hGenerator.generate(sootClass);
            FileHandler.write(fileName+".h",code);
            if(verbose) System.out.println( "\tcreated: "+fileName+".h");
        }
    }

    protected static HashSet _requiredMethods;
    protected static HashSet _requiredClasses;

}


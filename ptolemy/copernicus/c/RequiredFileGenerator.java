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

import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/** A class that generates the other required files in the
    transitive closure.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0
*/

public class RequiredFileGenerator {

    /** Generate the .h files for all classes in the transitive closure of
     * the given class, and the .c files for required classes only. A class
     * is considered "required" if it contains at least one method that is
     * in the transitive closure of methods called in the main class.
     *  @param classPath The classPath.
     *  @param className The main class.
     *  @exception IOException If  file I/O errors occur.
     */
    public static void generateTransitiveClosureOf(String classPath,
            String className)
            throws IOException {

        String compileMode = Options.v().get("compileMode");
        boolean verbose = Options.v().getBoolean("verbose");

        if (!compileMode.equals("singleClass")) {
            // Generate headers for only required classes.
            Iterator j = getRequiredClasses().iterator();
            //Iterator j = Scene.v().getClasses().iterator();
            while (j.hasNext()) {
                String nextClassName=((SootClass)j.next()).getName();
                _generateHeaders(classPath, nextClassName);
            }


            // Generate only the .c files for everything in the transitive
            // closure.
            Iterator i = getRequiredClasses().iterator();
            while (i.hasNext()) {
                String nextClassName=((SootClass)i.next()).getName();

                if (verbose) {
                    System.out.println(nextClassName);
                }

                _generateC(classPath, nextClassName);
            }


        }
    }


    /** Appends the list of C files corresponding to user classes to a
     * given StringBuffer and returns the list of C files corresponding to
     * library files. This is used by the MakeFileGenerator.
     * @param code The StringBuffer to which the C filenames of user
     * classes are to be added.
     * @return The list of C filesnames of required library files.
     */
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

    /** Returns the set of all required classes.
     *  @return The set of all required Classes.
     */
    public static Collection getRequiredClasses() {
        return (Collection) _requiredClasses;
    }

    /** Initialize and compute the required classes and methods.
     *  @param classPath The classpath.
     *  @param className The name of the class which we will take as the root
     *  from which others are called.
     */
    public void init(String classPath, String className) {
        _pruneLevel = Options.v().getInt("pruneLevel");

        switch (_pruneLevel) {
        case 0 :
            _pruneLevel0(classPath, className);
            break;
        case 1 :
            _pruneLevel1(classPath, className);
            break;
        default :
            throw new RuntimeException("Level " + _pruneLevel
                    + " pruning not supported!");
        }


    }

    /** Returns whether a given class is required or not.
     *  @param source Any class.
     *  @return True if it is a required class.
     */
    public static boolean isRequired(SootClass source) {
        if (Options.v().get("compileMode").equals("full")
                && (_pruneLevel > 0)) {
            return _requiredClasses.contains(source);
        }
        else {
            return true;
        }
    }

    /** Returns whether a given field is required or not.
     *  @param field Any field.
     *  @return True if it is a required field.
     */
    public static boolean isRequired(SootField field) {
        if (Options.v().get("compileMode").equals("full")
                && (_pruneLevel >0) ) {
            return _requiredFields.contains(field);
        }
        else {
            return true;
        }
    }

    /** Returns whether a given method is required or not.
     *
     *  @param method Any method.
     *  @return True if it is a required method.
     */
    public static boolean isRequired(SootMethod method) {
        if (Options.v().get("compileMode").equals("full")
                && (_pruneLevel >0) ) {
            return _requiredMethods.contains(method);
        }
        else {
            return true;
        }
    }

    /** Returns whether a given Type is required or not. A type is required
     * if it is not a RefType, or its corresponding class is not required.
     * @param type The type to be checked.
     * @return True if the type is required.
     */
    public static boolean isRequired(Type type) {
        if (type instanceof RefType) {
            SootClass sootClass = ((RefType)type).getSootClass();
            if (isRequired(sootClass)) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return true;
        }
    }

    /** Calculate which classes and methods are really needed when a
        prune-level 1 analysis is called.
        @param classPath The classpath.
        @param className The name of the class.
    */
    private static void _pruneLevel0(String classPath, String className) {
        _requiredClasses = new HashSet(Scene.v().getClasses());
    }

    /** Calculate which classes and methods are really needed when a
        prune-level 1 analysis is called.
        @param classPath The classpath.
        @param className The name of the class.
    */
    private static void _pruneLevel1(String classPath, String className) {
        SootClass source = Scene.v().getSootClass(className);


        CallGraphPruner pruner = new CallGraphPruner(source);
        _requiredMethods = pruner.getReachableMethods();
        _requiredClasses = pruner.getReachableClasses();
        _requiredFields = pruner.getReachableFields();
    }

    /** Generate the C code for the given class.
        @param classPath
        @param className The name of the class.
        @param compileMode The compilation mode.
        @param verbose Whether routine messages should be generated.
    */
    private static void _generateC(String classPath, String className) {

        // Initialize code generation.
        Scene.v().setSootClassPath(classPath);
        CodeFileGenerator cGenerator = new CodeFileGenerator();

        String code;
        boolean verbose = Options.v().getBoolean("verbose");

        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Make changes in the filename.
        String fileName = new String();
        fileName = CNames.classNameToFileName(className);

        // Generate the .c file.
        if (Options.v().get("compileMode").equals("full")) {
            if (verbose) {
                if (FileHandler.exists(fileName+".c")) {
                    System.out.println( "\texists:"+fileName+".c");
                }
                else {
                    System.out.println( "\tcreating: " +fileName+".c");

                }
            }
            code = cGenerator.generate(sootClass);
            FileHandler.write(fileName+".c", code);
        }
    }

    /** Generate the .h and "stub header" Files required for a given
     * class if they do not already exist.
     *  @param classPath The class path.
     *  @param className The class for which the files should be generated.
     *  @param compileMode The compilation mode.
     *  @param verbose Whether routine messages should be generated.
     */
    private static void _generateHeaders(String classPath, String className)
            throws IOException {

        // Initialize code generation.
        Scene.v().setSootClassPath(classPath);

        HeaderFileGenerator hGenerator;
        StubFileGenerator sGenerator = new StubFileGenerator();

        String code;

        Scene.v().loadClassAndSupport(className);
        SootClass sootClass = Scene.v().getSootClass(className);
        CNames.setup();

        // Make changes in the filename.
        String fileName = new String();
        fileName = CNames.classNameToFileName(className);

        boolean verbose = Options.v().getBoolean("verbose");
        // Create any parent directories, if required.
        if (fileName.lastIndexOf('/')>0) {
            // The file requires some directories.
            if (verbose) {
                System.out.println(className);
            }

            File dummyFile = new File(fileName.substring(0,
                    fileName.lastIndexOf('/')));
            dummyFile.mkdirs();
        }


        // Generate the stub header file.
        if (FileHandler.exists(fileName
                + StubFileGenerator.stubFileNameSuffix())) {
            code = sGenerator.generate(sootClass);
            String name = fileName
                + StubFileGenerator.stubFileNameSuffix();

            FileHandler.write(name, code);

            if (verbose) System.out.println( "\texists: " + fileName
                    + StubFileGenerator.stubFileNameSuffix());
        }
        else {
            code = sGenerator.generate(sootClass);
            String name = fileName
                + StubFileGenerator.stubFileNameSuffix();

            FileHandler.write(name, code);

            if (verbose) {
                System.out.println( "\tcreated: " + name);
            }
        }


        // Generate the .h file.
        if (FileHandler.exists(fileName+".h")) {
            hGenerator = new HeaderFileGenerator();
            code = hGenerator.generate(sootClass);
            FileHandler.write(fileName+".h", code);


            if (verbose) {
                System.out.println( "\texists: " + fileName + ".h");
            }
        }
        else {
            hGenerator = new HeaderFileGenerator();
            code = hGenerator.generate(sootClass);
            FileHandler.write(fileName+".h", code);

            if (verbose) {
                System.out.println( "\tcreated: " + fileName + ".h");
            }
        }
    }

    private static HashSet _requiredMethods = new HashSet();
    private static HashSet _requiredClasses = new HashSet();
    private static HashSet _requiredFields = new HashSet();

    // What level of pruning is required.
    private static int _pruneLevel;
}


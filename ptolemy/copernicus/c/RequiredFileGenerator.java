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

import soot.Scene;
import soot.SootClass;


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
     * generate the c files for all classes in the transitive closure of the
     * given class.
     * @param classPath The classPath.
     * @param className The class for which the transitive closure is to be
     * generated.
     * @param compileMode The compilation mode.
     * @param verbose Whether routine messages are to be generated.
     */
    public static void generateTransitiveClosureOf(String classPath,
    String className, String compileMode, boolean verbose) throws IOException {

            if (!compileMode.equals("singleClass")) {
                Scene.v().setSootClassPath(classPath);
                Scene.v().loadClassAndSupport(className);

                Iterator i = Scene.v().getClasses().iterator();
                i.next();//Skip the first class

                String nextClassName = new String();

                while (i.hasNext()) {
                    nextClassName=((SootClass)i.next()).getName();
                    generateCode(classPath, nextClassName,
                        compileMode, verbose);
                }
            }


    }

    /**
     * generate all the code Files required for a given class
     * @param classPath The class path.
     * @param className The class for which the files should be generated.
     * @param compileMode The compilation mode.
     * @param verbose Whether routine messages should be generated.
     */
    public static void generateCode(String classPath, String className,
        String compileMode, boolean verbose) throws IOException {

            // Initialize code generation
            Scene.v().setSootClassPath(classPath);

            HeaderFileGenerator hGenerator      = new HeaderFileGenerator();
            CodeFileGenerator cGenerator        = new CodeFileGenerator();
            InterfaceFileGenerator iGenerator   = new InterfaceFileGenerator();

            String code;

            cGenerator.clearSingleClassMode();
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
     * @param className The name of a class.
     * @return The C fileName corresponding to this class.
     */
    public static String classNameToFileName(String className) {
        if (isSystemClass(className)) return(System.getProperty("j2c_lib")+"/"
                    +className.replace('.', '/'));
        else return(className);
    }

    /**
     * @param className A class.
     * @return True if the given class is a System class.
     */
    public static boolean isSystemClass(String className) {

        if ((className.startsWith("java."))||
            (className.startsWith("sun."))) return (true);
        else return(false);
    }


}


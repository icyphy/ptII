/*

A class that generates a makefile for a given class.

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
@AcceptedRating Red (ankush@eng.umd.edu)

*/

package ptolemy.copernicus.c;

import java.util.HashSet;
import java.util.Iterator;

/** A class that generates the makefile for the given class. The generated file
    has the name (class).make.

    @author Ankush Varma
    @version $Id$
    @since Ptolemy II 2.0
*/

public class MakeFileGenerator {
    /** Dummy constructor
     */
    public MakeFileGenerator() {
    }

    /** Finds the filename corresponding to this class.
     *  @param className The name of the class.
     *  @return The corresponding filename as it should be written to the
     *  makeFile.
     */
    public static String classNameToMakeFileName(String className) {
        StringBuffer name = new StringBuffer(
                CNames.classNameToFileName(className));
        return name.toString();
    }

    /** Create the MakeFile.
     *  @param classPath The classPath.
     *  @param className The class for which the Makefile is to be generated.
     *  The makefile will have the name <i>className</i>.make.
     */
    public static void generateMakeFile(String classPath, String className) {
        // Check for special targets.
        if (Options.v().get("target").equals("C6000")) {
            MakefileGenerator_C6000.generateMakeFile(classPath, className);
            return;
        }

        // Garbage collection.
        String gcDir = Options.v().get("gcDir");
        boolean gc = !(gcDir.equals(""));
        StringBuffer code = new StringBuffer();
        boolean isStatic = true;

        if (java.io.File.pathSeparatorChar != ';') {
            // We are not under windows, so use dynamic linking
            isStatic = false;
        }

        code.append("#Standard variables\n");
        code.append("PTII = ../../../..\n");

        code.append("THIS = " + className + ".make\n");
        code.append("RUNTIME = " + Options.v().get("runtimeDir") + "\n");
        code.append("NATIVE_BODIES ="
                + NativeMethodGenerator.getNativeBodyLib() + "\n");
        // Overridden bodies.
        code.append("OVER_BODIES = "
                + OverriddenMethodGenerator.getOverriddenBodyLib() + "\n");
        // Java-to-C library.
        code.append("LIB = " + Options.v().get("lib")
                + "\n");
        code.append("LIB_FILE = $(LIB)/j2c_lib.a\n");
        // Garbage collector
        if (gc) {
            code.append("\n# Garbage Collector.\n");
            code.append("GC_LIB = $(PTII)/lib/libgc.a\n");
            code.append("# Uncomment the next line for dynamic linking\n");
            if (isStatic) {
                code.append("#");
            }
            // FIXME: cd to $(PTII) so that we don't end up with a relative
            // path in the ld path under Solaris which will cause problems
            // when we move the executable around
            code.append("GC_LIB = -Wl,-R`cd $(PTII);"
                    + " pwd`/lib -L$(PTII)/lib -lgc\n");
            code.append("GC_DIR = " + gcDir + "\n\n");
        }

        code.append("STATIC= -static");
        code.append("# Uncomment the next line for dynamic linking\n");
        if (isStatic) {
            code.append("#");
        }
        code.append("STATIC=\n");

        // The -g flag is for gdb debugging.
        //code.append("CFLAGS = -O2 -static -s -Wall -pedantic -I .");
        code.append("CFLAGS = -g $(STATIC) -Wall -pedantic -I .");

        if (gc) {
            code.append(" -DGC");
        }
        else {
            code.append(" -UGC");
        }
        code.append("\n");

        code.append("DEPEND = gcc -MM -I $(RUNTIME) -I $(LIB) "
                + "-I $(NATIVE_BODIES) -I $(OVER_BODIES) -I .");
        if (gc) {
            code.append(" -I $(GC_DIR)");
        }
        code.append("\n\n");

        // Get names of all .c files in the transitive closure.
        code.append("SOURCES = $(RUNTIME)/pccg_runtime.c "
                + "$(RUNTIME)/pccg_array.c $(RUNTIME)/strings.c\\\n"
                + "\t"
                + CNames.classNameToFileName(className) + "_main.c\\\n");

        HashSet libSources = RequiredFileGenerator.generateUserClasses(code);

        // Generate all the source files for system(library) classes.
        code.append("\n\nLIB_SOURCES = ");
        Iterator i = libSources.iterator();
        while (i.hasNext()) {
            code.append("\t" + (String)i.next() + ".c\\\n");
        }

        code.append("\n");// Takes care of blank line for last "\".

        // Definitions for various kinds of files.
        code.append("\nOBJECTS = $(SOURCES:.c=.o)\n");
        code.append(  "HEADERS = $(SOURCES:.c=.h)\n");
        code.append( "IHEADERS = $(SOURCES:.c="
                + StubFileGenerator.stubFileNameSuffix() + ")\n");

        code.append("\nLIB_OBJECTS = $(LIB_SOURCES:.c=.o)\n");
        code.append(  "LIB_HEADERS = $(LIB_SOURCES:.c=.h)\n");
        code.append( "LIB_IHEADERS = $(LIB_SOURCES:.c="
                + StubFileGenerator.stubFileNameSuffix() + ")\n");

        // Main Target.
        code.append("\n"+ className
                    + ".exe : depend $(OBJECTS) $(LIB_FILE)\n");

        code.append("\tgcc $(CFLAGS) $(OBJECTS) $(LIB_FILE) ");
        if (gc) {
            code.append("$(GC_LIB) ");
        }
        code.append("-o "+ className +".exe\n");

        // Conversion from .c to .o
        code.append(".c.o:\n");
        code.append("\tgcc $(CFLAGS) -c  -I $(RUNTIME) -I $(LIB) "
                + "-I $(NATIVE_BODIES)");
        if (gc) {
            code.append(" -I $(GC_DIR)");
        }
        code.append(" $< -o $@ \n\n");



        // Library generation.
        code.append("$(LIB_FILE): $(LIB_OBJECTS)\n");
        code.append("\tar cr $(LIB_FILE) $(LIB_OBJECTS)\n");
        code.append("\tranlib $(LIB_FILE)\n");

        // Other targets.
        code.append("\n.PHONY:depend\n\n");
        code.append("depend:\n");
        code.append("\t$(DEPEND) $(SOURCES)>makefile.tmp;\\\n");
        code.append("\tcat $(THIS) makefile.tmp>"
                + className + ".mk;\\\n");
        code.append("\trm makefile.tmp;\n");
        code.append("\tmv " + className + ".mk $(THIS)\n");
        code.append("\n");

        code.append("clean:\n");
        code.append("\trm -f $(OBJECTS)\n"
                + "\trm -f $(LIB_OBJECTS)\n"
                + "\trm -f $(LIB_FILE)\n");
        code.append("\n");

        code.append("# DO NOT DELETE THIS LINE "
                + " -- make depend depends on it.\n\n");

        FileHandler.write(className + ".make", code.toString());

    }

}



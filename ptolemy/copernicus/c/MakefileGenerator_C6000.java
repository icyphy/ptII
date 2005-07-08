/* This class generates a makefile specific to the TMS320C6xxx.

 Copyright (c) 2003-2005 The University of Maryland.
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

 */
package ptolemy.copernicus.c;

import java.util.HashSet;
import java.util.Iterator;

import soot.Scene;
import soot.SootClass;

//////////////////////////////////////////////////////////////////////////
//// MakefileGenerator_C6000

/**
 This class generates a makefile specific to the TMS320C6xxx.

 @author Ankush Varma
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ankush)
 @Pt.AcceptedRating Red (ssb)
 */
public class MakefileGenerator_C6000 extends MakeFileGenerator {
    /** Create the MakeFile.
     *  @param classPath The classPath.
     *  @param className The class for which the Makefile is to be generated.
     *  The makefile will have the name <i>className</i>.make.
     */
    public static void generateMakeFile(String classPath, String className) {
        SootClass source = Scene.v().getSootClass(className);
        String code = setupVariables(source) + specifyRules()
                + specifyFiles(source) + specifyTargets(source);

        FileHandler.write(className + ".make", code);
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
    private static String setupVariables(SootClass source) {
        StringBuffer code = new StringBuffer();
        code.append("#Standard variables.\n");

        // Runtime directory.
        code.append("RUNTIME = ../runtime\n");

        // Native Bodies.
        code.append("NATIVE_BODIES ="
                + NativeMethodGenerator.getNativeBodyLib() + "\n");

        // Overridden bodies.
        code.append("OVER_BODIES = "
                + OverriddenMethodGenerator.getOverriddenBodyLib() + "\n");

        // Java-to-C library.
        code.append("LIB = " + Options.v().get("lib") + "\n");

        // TI Directory
        code.append("TI_DIR  := C:/ti\n");

        // C6000 compiler, assembler, and linker.
        code.append("\n#  C6000 compiler, assembler, and linker.\n");
        code.append("CC62 = cl6x\n");
        code.append("AS62 = asm6x\n");
        code.append("LD62 = lnk6x\n");

        // Options
        code.append("\n#Options\n");
        code.append("CC62OPTS = -qq -i. -i../runtime -i$(LIB) "
                + "-i$(NATIVE_BODIES) -i$(OVER_BODIES)\n");
        code.append("AS62OPTS = -g\n");
        code.append("LD62OPTS = -c -stack 0x400 -heap 0x400 -q\n");
        code.append("LIBS     = -l$(TI_DIR)/c6000/cgtools/lib/rts6700.lib\n");

        code.append("\n");
        return code.toString();
    }

    private static String specifyFiles(SootClass source) {
        StringBuffer code = new StringBuffer();

        // Get names of all .c files in the transitive closure.
        code.append("SOURCES = $(RUNTIME)/pccg_runtime.c "
                + "$(RUNTIME)/pccg_array.c $(RUNTIME)/strings.c\\\n" + "\t"
                + source.getName() + "_main.c\\\n");

        HashSet libSources = RequiredFileGenerator.generateUserClasses(code);

        // Generate all the source files for system(library) classes.
        code.append("\n\nLIB_SOURCES = ");

        Iterator i = libSources.iterator();

        while (i.hasNext()) {
            code.append("\t" + (String) i.next() + ".c\\\n");
        }

        code.append("\n"); // Takes care of blank line for last "\".

        code.append("PROG = " + source.getName() + "_main\n");
        code.append("CMDS = generic.cmd\n");
        code.append("OBJS = $(SOURCES:.c=.obj) $(LIB_SOURCES:.c=.obj)\n");

        code.append("\n");

        return code.toString();
    }

    private static String specifyRules() {
        StringBuffer code = new StringBuffer();
        code.append("#Rules\n");

        // Note that all object files will be created in current directory.
        code.append("%.obj : %.c\n");
        code.append("\tcl6x $(CC62FLAGS) $(CC62OPTS) $<\n\n");

        code.append("%.out : %.obj\n");
        code.append("\t$(LD62) $(LD62FLAGS) $(LD62OPTS) -o $@ $< *.obj "
                + "$(LIBS) $(CMDS)\n");

        code.append("\n");
        return code.toString();
    }

    private static String specifyTargets(SootClass source) {
        StringBuffer code = new StringBuffer();
        code.append("#Targets\n");
        code.append("$(PROG).out: $(OBJS)\n");

        code.append("\nclean:\n");
        code.append("\trm *.obj *.out\n");

        code.append("\n");
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
}

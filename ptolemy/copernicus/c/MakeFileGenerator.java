/* A class that generates a makefile for a given class.

Copyright (c) 2002 The University of Maryland.
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
import java.util.LinkedList;
import java.util.Collection;

import soot.Scene;
import soot.SootClass;

/** A class that generates the makefile for the given class. The generated file
    has the name (class).make.

    @author Ankush Varma
    @version $Id$
    @since Ptolemy II 2.0
*/

public class MakeFileGenerator {
    /** Dummy constructor
     */
    public void MakeFileGenerator() {
    }

    /** Create the MakeFile.
     *  @param classPath The classPath.
     *  @param className The class for which the Makefile is to be generated.
     *  The makefile will have the name <i>className</i>.make.
     */
    public static void generateMakeFile(String classPath, String className) {
        StringBuffer code = new StringBuffer();

        code.append("#Standard variables\n");
        code.append("RUNTIME = ../runtime\n");
        code.append("LIB = " + System.getProperty("j2c_lib","/j2c_lib")
                + "\n");

        // The -g3 flag is for gdb debugging.
        code.append("CFLAGS = -Wall -pedantic -g3\n");
        code.append("DEPEND = gcc -MM -I $(RUNTIME) -I $(LIB)\n\n");

        code.append("THIS = " + className + ".make\n");

        // Get names of all .c files in the transitive closure.
        code.append("SOURCES = $(RUNTIME)/pccg_runtime.c "
                + "$(RUNTIME)/pccg_array.c $(RUNTIME)/strings.c\\\n"
                + "\t" + className + "_main.c\\\n");

        Iterator i = RequiredFileGenerator.getRequiredClasses(classPath,
                className).iterator();

        while (i.hasNext()) {
            String name = _classNameToMakeFileName(
                ((SootClass)i.next()).getName());

            // A name with a "$" in it represents an inner class.
            // FIXME: We don't want to compile inner classes for now.
            if (name.indexOf("$") == -1) {
                code.append("\t" + name + ".c\\\n");
            }
        }

        code.append("\n");// Takes care of blank line for last "\".


        code.append("\nOBJECTS = $(SOURCES:.c=.o)\n");
        code.append(  "HEADERS = $(SOURCES:.c=.h)\n");
        code.append( "IHEADERS = $(SOURCES:.c="
                + InterfaceFileGenerator.interfaceFileNameSuffix() + ")\n");

        code.append(className + ".exe : $(OBJECTS)\n");
        code.append("\tgcc $(OBJECTS) -o "+ className +".exe\n");

        code.append(".c.o:\n");
        code.append("\tgcc -c $(CFLAGS) -I $(RUNTIME) -I $(LIB) $< -o $@ "
                + "2>err.txt\n\n");

        code.append(".PHONY:depend\n\n");
        code.append("depend:\n");
        code.append("\t$(DEPEND) $(SOURCES)>makefile.tmp;\\\n");
        code.append("\tcat $(THIS) makefile.tmp>"
                + className + ".mk;\\\n");
        code.append("\trm makefile.tmp;\n");
        code.append("\n");

        code.append("clean:\n");
        code.append("\trm $(OBJECTS);\n");

        code.append("# DO NOT DELETE THIS LINE "
                    + " -- make depend depends on it.\n\n");

        FileHandler.write(className + ".make", code.toString());

    }

    /** Finds the filename corresponding to this class and replaces
     *  "$" with "$$" for compatibility with the <i>make</i> utility.
     *  @param className The name of the class.
     *  @return The corresponding filename as it should be written to the
     *  makeFile.
     */
    protected static String _classNameToMakeFileName(String className) {
        StringBuffer name = new StringBuffer(
            CNames.classNameToFileName(className));

        // Replace "$" with "$$"so that the make utility interprets names
        // correctly.
        for(int j = 0;j<name.length();j++) {
            if (name.charAt(j) == '$') {
                name.insert(j,"$");
                j++;
            }
        }

        return name.toString();
    }
}



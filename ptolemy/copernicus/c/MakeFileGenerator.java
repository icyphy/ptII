/* a class that generates a makefile for the given class

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
import java.util.LinkedList;

import soot.Scene;
import soot.SootClass;

/* A class that generates a makefile for the given class

   @author Ankush Varma
   @version $Id$
*/

public class MakeFileGenerator
{
    public void MakeFileGenerator()
    {
        //dummy constructor
    }

    public static void generateMakeFile(String classPath, String className)
    {
        StringBuffer code = new StringBuffer();

        code.append("#Standard variables\n");
        code.append("RUNTIME = ../runtime\n");
        code.append("LIB = "+System.getProperty("j2c_lib","/j2c_lib")+"\n");
        code.append("CFLAGS = -Wall -pedantic\n");
        code.append("DEPEND = gcc -MM -I $(RUNTIME) -I $(LIB)\n\n");

        code.append("THIS = "+className+".make\n");


        //get names of all .c files in the transitive closure
        Iterator i = _classNameList(classPath,className).iterator();
        code.append("SOURCES = $(RUNTIME)/runtime.c $(RUNTIME)/array.c\\\n");
        while (i.hasNext())
        {
            String name = _classNameToMakeFileName((String)i.next());
            code.append("\t"+name+".c\\\n");
        }
        code.append("\n");//takes care of blank line for last "\"


        code.append("\nOBJECTS = $(SOURCES:.c=.o)\n");
        code.append(  "HEADERS = $(SOURCES:.c=.h)\n");
        code.append( "IHEADERS = $(SOURCES:.c=.i.h)\n");

        code.append(className+".exe : $(OBJECTS)\n");
        code.append("\tgcc $(OBJECTS)\n");

        code.append("makefile: $(THIS)\n");
        code.append("\tmake depend\n\n");

        code.append(".c.o:\n");
        code.append("\tgcc -c $(CFLAGS) -I $(RUNTIME) -I $(LIB) $<\n\n");

        code.append("depend:\n");
        code.append("\t$(DEPEND) $(SOURCES)>makefile.tmp;\\\n");
        code.append("\tcat $(THIS) makefile.tmp>makefile;\\\n");
        code.append("\trm makefile.tmp;\n");
        code.append("\n");

        code.append("clean:\n");
        code.append("\trm $(OBJECTS);\n");

        code.append("# DO NOT DELETE THIS LINE "
                    + " -- make depend depends on it.\n\n");

        FileHandler.write(className+".make",code.toString());

    }

protected static LinkedList _classNameList(String classPath, String className)
    //returns a list of the names of all classses in the transitive closure
    {
        LinkedList names = new LinkedList();

        Scene.v().setSootClassPath(classPath);
        Scene.v().loadClassAndSupport(className);

        Iterator i = Scene.v().getClasses().iterator();
        while (i.hasNext())
        {
            SootClass thisClass = (SootClass)i.next();
            names.add(thisClass.getName());
        }

        return names;

    }

// finds filename corrseponding to class and replaces
// "$" with "$$" for compatibility
protected static String _classNameToMakeFileName(String className)
    {
        StringBuffer name = new StringBuffer(
            RequiredFileGenerator.classNameToFileName(className));

        for(int j=0;j<name.length();j++)
        {
            if (name.charAt(j)=='$')
            {
                name.insert(j,"$");
                j++;
            }
            //replace "$" with "$$"
            //so that makefile interprets names correctly
        }

        return name.toString();
    }
}



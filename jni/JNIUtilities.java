/** A graph editor frame for Ptolemy models that use the JNI interface.

Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/

package jni;

import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.StringUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


//////////////////////////////////////////////////////////////////////////
//// JNIUtilities
/**
A collection of utilities for generating Java Native Interface classes

For information about how to create shared libraries under Windows using
Cygwin, see
<a href="http://cygwin.com/cygwin-ug-net/dll.html" target="_top"><code>http://cygwin.com/cygwin-ug-net/dll.html</code></a>

<p>For information about using JNI with Cygwin, see
 <a href="http://www.inonit.com/cygwin/jni/helloWorld/c.html" target="_top"><code>http://www.inonit.com/cygwin/jni/helloWorld/c.html</code></a>

<h2>How to call an actor that calls C code that returns a double</h2>
In this example, we call a C method called <code>meaningOfLife()</code>
that returns a double.
<ol>

<li> Create the C file called <code>meaningOfLife.c</code> that contains
<pre>
// Return the answer to "the meaning of life, the universe, and everything"
double meaningOfLife() {
  return 42.0;
}
</pre>

<li> Create <code>meaningOfLife.h</code> that contains
<pre>
extern "C" double meaningOfLife();
</pre>

<li> Compile and create the shared library.  Under Windows, we 
create a <code>.dll</code>
<pre>
gcc -shared -o meaningOfLife.dll meaningOfLife.c
</pre>

<li> Start up Vergil with the JNI interface enabled.
<pre>
$PTII/bin/vergil -jni
</pre>
Note that the <code>-jni</code> option may go away in the future
if we merge the jni facility into the main tree

<li> Create a new model with File -> New -> Graph Editor
<li> Drag in the JNIActor from the jni folder
<li> Right click on the actor and select Configure Arguments.
<li> Fill in the form as follows
<dl>
<dt><code><b>Name:</b></code>
<dd><code>output</code>
<dt><code><b>C or C++ type:</b></code>
<dd><code>double</code>
<dt><code><b>Kind:</b></code>
<dd><code>return</code>
</dl>

<li> Select Ok and then Commit to close
the argument configurer
<li> Right Click on the actor to edit
the parameters
<dl>
<dt><code><b>Native Function Name:</b></code>
<dd><code>"meaningOfLife"</code>
<dt><code>Native Library Name<b></b></code>
<dd><code>"meaningOfLife"</code>
<dt><code><b>DLLs Directory</b></code>
<dd><code>""</code>
</dl>
<li> Select Commit to close the 
Edit parameters window
<li> Select JNI from the menu and
then select Generate C Interface
<li> Save the model.
<br>Because of an apparent bug, it is necessary
to save the model for the port we just created to appear
<li> Add a Display actor from the Source folder in the 
Actor Library and connect the input of the Display actor
to the output of the meaningOfLife Actor
<li> Because of a bug in the Ptolemy interface to the JNI
actor, you must specify the type of the output of the meaningOfLife
actor by right clicking on the actor and selecting
Configure Ports and then entering double for
output type
<li> Drag in a SDF director and set the number of iterations to 1.
<li> Select Run
</ol>
@author Vincent Arnould (vincent.arnould@thalesgroup.com), contributor Christopher Hylands
@version $Id$
@since Ptolemy II 2.2
*/
public class JNIUtilities {

    /** Instances of this class cannot be created.
     */
    private JNIUtilities() {
    }

    public static void generateJNI(CompositeEntity model,
            GenericJNIActor actor)
            throws Exception {
        // V‰rification qu'il y a un return, place le return en void sinon
        if (actor.getArgumentReturn() == null) {
            MessageHandler.warning(
                    "No return was configured,\n so we set it as void");
            actor.addArgumentReturn();
        }

        //Cr‰ation des ports
        Iterator relations = model.relationList().iterator();
        while(relations.hasNext()) {
            ((ptolemy.kernel.ComponentRelation)(relations.next())).unlinkAll();
        }
        actor.removeAllPorts();
        actor.createPorts();

        //Nommage de l'acteur en fonction de ses parameters
        String nativeFuncName = "";
        String libName = "";
        try {
            nativeFuncName =
                (((StringToken) ((Parameter) actor
                        .getAttribute("Native Function Name"))
                        .getToken())
                        .toString());
            libName =
                ((StringToken) ((Parameter) actor
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();

        } catch (Exception ex) {
	    throw new IllegalActionException(actor, ex, 
					 "no library or function name !");

        }
        libName =
            libName.substring(1, libName.length() - 1);
        nativeFuncName =
            nativeFuncName.substring(1, nativeFuncName.length() - 1);

        try {
            actor.setName(libName + "I" + nativeFuncName);
        } catch (NameDuplicationException ex) {
	    throw new IllegalActionException(actor, ex, 
					 "Unable to rename JNIActor '"
					 + actor.getName()
					 + "': \n"
					 + "An JNI Actor already exists !\n");
        }

        //render the graph within its controller
        // FIXME: when this method was in ThalesGraphFrame, we rerendered.
        //_controller.rerender();

        //actor.notifyAll();

        //Cr‰ation du fichier Java
        File javaFile = _exportJavaInterfaceFile(actor);

        // FIXME: if we are running under Vergil, we should use
        // the graphical exec code in Copernicus so that the user
        // can see the execution.  If we are running without a UI.
        // We should call exec().
        //Compilation java de la classe interface:
        String cmd[] = new String[4];
        cmd[0] = "javac";
        cmd[1] = "-classpath";
        cmd[2] = "%PTII%";
        cmd[3] =
            "jni/jni"
            + libName
            + "/Jni"
            + actor.getName()
            + ".java";

        Runtime r = Runtime.getRuntime();

	for(int i = 0; i < cmd.length; i++) {
	    System.out.print(cmd[i] + " ");
	}
	System.out.println("");

        Process compil = r.exec(cmd);

        //Generation du fichier entete
        cmd = new String[4];
        cmd[0] = "javah";
        cmd[1] = "-d";
        cmd[2] = "jni/jni" + libName;
        cmd[3] =
            "jni.jni"
            + libName
            + ".Jni"
            + actor.getName();
        Runtime r2 = Runtime.getRuntime();

        //waiting for the compilation to be done
        compil.waitFor();
	for(int i = 0; i < cmd.length; i++) {
	    System.out.print(cmd[i] + " ");
	}
	System.out.println("");
        Process header = r2.exec(cmd);

        //Cr‰ation du fichier C
        File cFile = _exportCInterfaceFile(actor);

        //compile Cfile
        header.waitFor();

        /*Process make = r3.exec(cmd);
        make.waitFor();*/
        _exportDSP(actor);
	_exportMakefile(actor);

        cmd = new String[5];
        cmd[0] = "make";
	cmd[1] = "-C";
        cmd[2] = "jni/jni" + libName;
        cmd[3] = "-f";
        cmd[4] = "Jnijni" + libName + ".mk";

        Runtime r3 = Runtime.getRuntime();

	for(int i = 0; i < cmd.length; i++) {
	    System.out.print(cmd[i] + " ");
	}
	System.out.println("");

        Process make = r3.exec(cmd);
	make.waitFor();
	System.out.println("Done");
    }

    /** Create the interface Java File.
     */
    protected static File _exportJavaInterfaceFile(GenericJNIActor actor) 
	throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();
        String nativeFuncName = "";
        String returnJType = "";
        String returnName = "";
        String libName = "";
        String interlibName = "";
        String interFuncName = "";
        String returnJType2 = "";
        try {
            nativeFuncName =
                (((StringToken) ((Parameter) actor
                        .getAttribute("Native Function Name"))
                        .getToken())
                        .toString());
            libName =
                ((StringToken) ((Parameter) actor
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();
        } catch (IllegalActionException ex) {
	    throw new IllegalActionException(actor, ex, 
					 "no library or function name !");
        }

        interlibName = "jni" + libName.substring(
                1, libName.length() - 1);
        interFuncName =
            "jni" + nativeFuncName.substring(
                    1, nativeFuncName.length() - 1);

        List argumentsList = actor.argumentsList();
        Iterator arguments = argumentsList.iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (!(argument == null) && argument.isReturn()) {
                returnJType = argument.getJType();
                returnName = argument.getName();
                break;
            }
        }

        if (returnJType.equals("void"))
            returnJType2 = "";
        else
            returnJType2 = returnJType;

        results.append(
		       "package jni."
		       + interlibName
		       + ";\n"
		       + "\n\n\n"
		       + "/* The class that interface the native function call\n"
		       + " * @author autogenerated file - by V. Arnould.TRT\n"
		       + " */\n"
		       + "public class Jni"
		       + actor.getName()
		       + " { \n\n"
		       + "/* The default constructor\n"
		       + " * @author autogenerated - by V. Arnould.TRT\n"
		       + " */\n"
		       + "public Jni"
		       + actor.getName()
		       + "(){}\n\n"
		       + "/* The loading of the native library\n"
		       + " */\n"
		       + "static {\n"
		       + "System.loadLibrary(\"Jni"
		       + interlibName
		       + "\");\n }\n\n"
		       + "public native "
		       + returnJType
		       + " "
		       + interFuncName
		       + "("
            + _getArgsInWithJType(actor, ",")
            + _virgule(
                    _getArgsInWithJType(actor, ","),
                    _getArgsInOutWithJType(actor, ","))
            + _getArgsInOutWithJType(actor, ",")
            + _virgule(
                    _getArgsInWithJType(actor, ",")+
                    _getArgsInOutWithJType(actor, ","),
                    _getArgsOutWithJType(actor, ","))
            + _getArgsOutWithJType(actor, ",")
            + ") throws SecurityException;\n\n"
            + "/* Send the result of the native library."
            + " This method is called by"
            + " the native code\n"
            + " * @author autogenerated - by V. Arnould.TRT\n"
            + " */\n"
            + " public void "
            + "sendResults("
            + _getArgsOutWithJType(actor, ",")
            + _virgule(
                    _getArgsOutWithJType(actor, ","),
                    _getArgsInOutWithJType(actor, ","))
            + _getArgsInOutWithJType(actor, ",")
            + ") {\n");

        arguments = _getArgsOut(actor).iterator();
        while (arguments.hasNext()) {
            String name = ((Argument) arguments.next()).getName();
            results.append( "_" + name + " = " + name + ";\n");
        }

        arguments = _getArgsInOut(actor).iterator();
        while (arguments.hasNext()) {
            String name = ((Argument) arguments.next()).getName();
            results.append( "_" + name + " = " + name + ";\n");
        }
	results.append("\n}\n\n");
        results.append(
            "/* Call the native function inthe native library. This method is "
            + "called by the GenericJNIActor on fire()\n"
            + " * @author autogenerated - by V. Arnould.TRT\n"
            + " */\n"
            + " public "
            + returnJType
            + " fire("
            + _getArgsInWithJType(actor, ",")
            + _virgule(_getArgsInWithJType(actor, ","),
                    _getArgsInOutWithJType(actor, ","))
            + _getArgsInOutWithJType(actor, ",")
            + _virgule(
                    _getArgsInOutWithJType(actor, ","),
                    _getArgsOutWithJType(actor, ","))
            + _getArgsOutWithJType(actor, ",")
            + ") {\n\n");

        //if there is a returned value
        Argument argRet = (Argument) actor.getArgumentReturn();
        if(!(argRet.getJType().equals("void"))) {
                results.append( "_" + argRet.getName() + " = "
                    + interFuncName
                    + "("
                    + _getArgsIn(actor, ",")
                    + _virgule( _getArgsIn(actor, ","),
                            _getArgsInOut(actor, ","))
                    + _getArgsInOut(actor, ",")
                    + _virgule( _getArgsInOut(actor, ","),
                            _getArgsOut(actor, ","))
                    + _getArgsOut(actor, ",")
                    + ");"
                    + "\n");

                results.append("return"
                    + " _" + argRet.getName() + ";\n"+ "}\n");
	} else {
                results.append( interFuncName
                    + "("
                    + _getArgsIn(actor, ",")
                    + _virgule( _getArgsIn(actor, ","),
                            _getArgsInOut(actor, ","))
                    + _getArgsInOut(actor, ",")
                    + _virgule( _getArgsInOut(actor, ","),
                            _getArgsOut(actor, ","))
                    + _getArgsOut(actor, ",")
                    + ");"
                    + "\n }\n");
	}

        results.append( "///////////// public fields\n" + "\n");

        //out
        arguments = _getArgsOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            results.append( "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n");
        }
        //inout
        arguments = _getArgsInOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            results.append( "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n");
        }

        //in
        arguments = _getArgsIn(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            results.append( "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n");
        }
        //return
        if(!(argRet.getJType().equals("void")))
            results.append( "public " + argRet.getJType() + " " + "_"
                + argRet.getName() + ";\n");

        results.append( "\n}");
        File dir = new File(System.getProperty("user.dir")
                + "/jni/" + interlibName);
        //Creation du repertoire
        try {
            dir.mkdirs();
        } catch (NullPointerException ex) {
	    throw new IllegalActionException(null, ex, "No directory '"
					 + dir + "'");
        }

        File javaFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/Jni"
                    + actor.getName()
                    + ".java");

	FileWriter writer = new FileWriter(javaFile);
	writer.write(results.toString());
	writer.close();

        return javaFile;
    }

    /** Create the interface C  File.
     *
     */
    protected static File _exportCInterfaceFile(GenericJNIActor actor)
	throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();
        String nativeFuncName = "";
        String returnType = "";
        String returnName = "";

        String dllDir = "";
        String libName = "";
        try {
            nativeFuncName =
                (((StringToken) ((Parameter) actor
                          .getAttribute("Native Function Name"))
                        .getToken())
                        .toString());
            libName =
                ((StringToken) ((Parameter) actor
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();
            dllDir =
                ((StringToken) ((Parameter) actor
                        .getAttribute("DLLs Directory"))
                        .getToken())
                .toString();

        } catch (Exception ex) {
	    throw new IllegalActionException(actor, ex, 
					 "no library or function name !");
        }
        String interlibName =
            "jni" + libName.substring(1, libName.length() - 1);
        String interFuncName =
            "jni" + nativeFuncName.substring(1,
                    nativeFuncName.length() - 1);
        List argumentsList = actor.argumentsList();
        Iterator arguments = argumentsList.iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (!(argument == null) && argument.isReturn()) {
                returnType = argument.getCType();
                returnName = argument.getName();
                break;
            }
        }

        String returnJNIType = "";
        arguments = _getArgs(actor, false, false, true).iterator();
        if (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            returnJNIType = argument.getJNIType();
        }

        String returnJType2;
        if (returnType.equals("void")) {
            returnJType2 = "";
        } else {
            returnJType2 = returnType + " ";
	}
        results.append("#include \"jni.h\" \n"
            // le fichier entete de la librairie existante
	    // Don't use io stream it results in compile time errors under
	    // gcc with cygwin
            +"/* #include <iostream> */\n "
            + "#include \"" + System.getProperty("user.dir")
            + "/" + dllDir.substring(1,
                    dllDir.length()-1) + "/"
            + libName.substring(1, libName.length() - 1)
            + ".h\"\n"
            // le fichier entete g‰n‰r‰ par javah
            +"#include \"jni_"
            + interlibName
            + "_Jni"
            + actor.getName()
            + ".h\"\n"
            + "/*******************************************************\n"
            + "*********************"
            + actor.getName()
            + "_"
            + interFuncName
            + "******************\n"
            + "*****************************************************/\n\n"
            + "//Declaration de la fonction existante\n"
            + "//extern \"C\" "
            + returnType
            + " "
            + nativeFuncName.substring(1, nativeFuncName.length() - 1)
            + "("
            + _getArgsInWithCType(actor, ",")
            + _virgule( _getArgsInWithCType(actor, ","),
                    _getArgsInOut(actor, ","))
            + _getArgsInOutWithCType(actor, ",")
            + _virgule(_getArgsInOut(actor, ","),
                    _getArgsOutWithCType(actor, ","))
            + _getArgsOutWithCType(actor, ",")
            + ");\n\n"
            + "JNIEXPORT "
            + actor.getArgumentReturn().getJNIType()
            + " JNICALL Java_jni_"
            + interlibName
            + "_Jni"
            + actor.getName()
            + "_"
            + interFuncName
            + "(\nJNIEnv *env, jobject jobj "
            + _virgule(_getArgsInWithJNIType(actor, ",")) 
	    + _getArgsInWithJNIType(actor, ",")
            + _virgule(_getArgsInWithJNIType(actor, ","),
                    _getArgsInOutWithCType(actor, ","))
            + _getArgsInOutWithJNIType(actor, ",")
            + _virgule(_getArgsInOutWithJNIType(actor, ","),
                    _getArgsOutWithCType(actor, ","))
            + _getArgsOutWithJNIType(actor, ",")
            + ")\n"
            + "{\n"
            + _indent1 + "// Declaration des sorties\n");

        arguments = _getArgsOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            results.append(argument.getJNIType() + " "
			    + argument.getName() + ";\n");
        }

	results.append("\n"
		       + _indent1 + "// structure en sortie de la fonction C\n"
		       + _indent1 + "//target_location_struct *target_location = "
		       + "new target_location_struct;\n\n"
		       + _indent1 + "// appel de la librairie existante\n\n");

        //For the array inout and in
        arguments = _getArgsInOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            String typ = arg.getJType();
            if (typ.endsWith("[]")) {
                    typ = typ.substring(0, typ.length()-2);
                    results.append( _indent1 + "j" + typ  + "Array *"
                        + arg.getName() + "_1 =" +
                        "(j" + typ + "Array*)env->Get"
                        + typ.substring(0, 1).toUpperCase()
                        + typ.substring(1, typ.length())  + "ArrayElements("
                        + arg.getName() + ",JNI_FALSE);\n");
                }
        }


        arguments = _getArgsIn(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            String typ = arg.getJType();
            if (typ.endsWith("[]")) {
                    typ = typ.substring(0, typ.length()-2);
                    results.append( _indent1 + "j" + typ  + "Array *"
			+ arg.getName() + "_1 =" +
                        "(j" + typ + "Array*)env->Get"
			+ typ.substring(0, 1).toUpperCase()
                        + typ.substring(1, typ.length())  + "ArrayElements("
                        + arg.getName() + ",JNI_FALSE);\n"
                        + "//if((long *)in_1 == (long *)0 || (long *)inout_1 == (long *)0)n" +
                        "//{\n" + "//std::cout << \"warning : the matrix is empty !\""
                        + " << std::endl;\n" +
                        "//}\n");
                }
        }


        //if they is a return
        if (!returnJNIType.equals("void")) {
	    results.append(returnJNIType
			   + " _" + returnName
			   + " = (" + returnJNIType + ")");
	}

        //native function call

	results.append(
		       nativeFuncName.substring(1, nativeFuncName.length() - 1)
		       + "("
		       + _getArgsWithCTypeCast(actor, true, false, false, ",")
		       + _virgule( _getArgsInWithCType(actor, ","), _getArgsInOut(actor, ","))
		       + _getArgsWithCTypeCast(actor, true, true, false, ",")
		       + _virgule( _getArgsInOutWithCType(actor, ","), _getArgsOut(actor, ","))
		       + _getArgsWithCTypeCast(actor, false, true, false, ",")
		       + ");\n\n");

        //Release memory in native side
        //for in
        arguments = _getArgsIn(actor).iterator();
        while(arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            String typ = arg.getJType();
            if(typ.endsWith("[]")) {
                    typ = typ.substring(0, typ.length()-2);
                    results.append( "env->Release"+ typ.substring(0, 1).toUpperCase() +
                        typ.substring(1, typ.length())  + "ArrayElements(" +
                        arg.getName() + ",(" + arg.getC2Type() + ")" + arg.getName() + "_1,0);\n");
	    }
        }
        //for inout
        arguments = _getArgsInOut(actor).iterator();
        while(arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            String typ = arg.getJType();
            if(typ.endsWith("[]")) {
                    typ = typ.substring(0, typ.length()-2);
                    results.append("env->Release"
				   + typ.substring(0, 1).toUpperCase()
				   + typ.substring(1, typ.length())
				   + "ArrayElements("
				   + arg.getName() + ",(" + arg.getC2Type()
				   + ")" + arg.getName() + "_1,0);\n");
	    }
        }

        if (!returnJNIType.equals("void")) {
            results.append( _indent1 + "if (_" + returnName + " == 0) {\n"
			    + _indent2 + "//std::cout << \"warning : return = 0\" << std::endl;\n"
			    + "\n}\n");
        }

        if (!(_getArgsOut(actor, "").equals("")
                    && _getArgsInOut(actor, "").equals(""))) {
	    results.append("                // envoi des sorties dans l'environnement JAVA / PTOLEMY II\n"
                + "jclass cls = env->GetObjectClass(jobj);\n"
                + "jmethodID mid = env->GetMethodID(cls,\"sendResults\",\""
                + _signatureSendResults(actor)
                + "\");\n"
                + "if (mid == 0) {\n"
                + "std::cout << \"Can't find sendResults method\";\n");

            if (!returnJNIType.equals("void")) {
                results.append( _indent1 + "return _" + returnName + ";\n}\n");
	    } else {
                results.append( "return;\n}\n");
	    }
            results.append( "env->CallVoidMethod(jobj,mid," + _getArgsOut(actor, ",")
                //warning : ici on suppose qu'il y a toujours au - 1 argument Out !! TBFix
                +_virgule( _getArgsOut(actor, ","), _getArgsInOut(actor, ","))
                + _getArgsInOut(actor, ",")
                + ");\n"
                + "if (env->ExceptionOccurred()) {\n"
                + "std::cout << \"Can't get back results!!\" << std::endl;\n"
                + "env->ExceptionDescribe();\n"
                + "env->ExceptionClear();\n"
                + "}\n"
                + "env->ExceptionClear();\n\n");
        }

        results.append(_indent1 + "// M‰nage\n");

        /*arguments = _getArgsOut(actor).iterator();
          while (arguments.hasNext()) {
          Argument arg = (Argument) arguments.next();
          results.append( "delete " + " " + arg.getName() + ";\n";
          }
          arguments = _getArgsInOut(actor).iterator();
          while (arguments.hasNext()) {
          Argument arg = (Argument) arguments.next();
          results.append( "delete " + " " + arg.getName() + ";\n";
          }*/

        if (!returnJNIType.equals("void")) {
            results.append(_indent1 + "return _" + returnName + ";\n}\n");
        } else {
            results.append(_indent1 + "return;\n}\n");
	}

        //exporting the file
        File cFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/jni"
                    + actor.getName()
                    + ".cpp");
	FileWriter writer = new FileWriter(cFile);
	writer.write(results.toString());
	writer.close();
	return cFile;
    }

    /** Export the Visual Studio project.
     */
    protected static void _exportDSP(GenericJNIActor actor)
	throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();
        String libName = "";
        String dllDir = "";
        try {
            libName =
                ((StringToken) ((Parameter) actor
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();
            dllDir =
                ((StringToken) ((Parameter) actor.getAttribute("DLLs Directory"))
                        .getToken())
                .toString();
        } catch (Exception ex) {
	    throw new IllegalActionException(actor, ex, 
					 "no library or function name !");
        }
        libName = libName.substring(1, libName.length() - 1);
        String interlibName = "jni" + libName;
        results.append(
            "# Microsoft Developer Studio Project File - Name=\""
            + interlibName
            + "\" - Package Owner=<4>\r\n"
            + "# Microsoft Developer Studio Generated Build File,"
            + " Format Version 6.00\r\n"
            + "# ** DO NOT EDIT **\r\n"
            + "# TARGTYPE \"Win32 (x86) Dynamic-Link Library\" 0x0102\r\n"
            + "CFG="
            + interlibName
            + " - Win32 Debug\r\n"
            + "!MESSAGE This is not a valid makefile. To build this project"
            + " using NMAKE,\r\n"
            + "!MESSAGE use the Export Makefile command and run\r\n"
            + "!MESSAGE \r\n"
            + "!MESSAGE NMAKE /f \""
            + interlibName
            + ".mak\".\r\n"
            + "!MESSAGE \r\n"
            + "!MESSAGE You can specify a configuration when running NMAKE\r\n"
            + "!MESSAGE by defining the macro CFG on the command line. For example:\r\n"
            + "!MESSAGE \r\n"
            + "!MESSAGE NMAKE /f \""
            + interlibName
            + ".mak\" CFG=\""
            + interlibName
            + " - Win32 Debug\"\r\n"
            + "!MESSAGE \r\n"
            + "!MESSAGE Possible choices for configuration are:\r\n"
            + "!MESSAGE \r\n"
            + "!MESSAGE \""
            + interlibName
            + " - Win32 Release\" (based on \"Win32 (x86) Dynamic-Link Library\")\r\n"
            + "!MESSAGE \""
            + interlibName
            + " - Win32 Debug\" (based on \"Win32 (x86) Dynamic-Link Library\")\r\n"
            + "!MESSAGE \r\n\r\n"
            + "# Begin Project\r\n"
            + "# PROP AllowPerConfigDependencies 0\r\n"
            + "# PROP Scc_ProjName \"\" \r\n"
            + "# PROP Scc_LocalPath \"\" \r\n"
            + "CPP=cl.exe\r\n"
            + "MTL=midl.exe\r\n"
            + "RSC=rc.exe\r\n\r\n"
            + "!IF  \"$(CFG)\" == \""
            + interlibName
            + "- Win32 Release\"\r\n\r\n"
            + "# PROP BASE Use_MFC 0\r\n"
            + "# PROP BASE Use_Debug_Libraries 0\r\n"
            + "# PROP BASE Output_Dir \"Release\"\r\n"
            + "# PROP BASE Intermediate_Dir \"Release\"\r\n"
            + "# PROP BASE Target_Dir \"\" \r\n"
            + "# PROP Use_MFC 0\r\n"
            + "# PROP Use_Debug_Libraries 0\r\n"
            + "# PROP Output_Dir \"" + System.getProperty("user.dir")
            + "\\" + dllDir.substring(1, dllDir.length())
            + "\r\n"
            + "# PROP Intermediate_Dir \"Release\"\r\n"
            + "# PROP Target_Dir \"\" \r\n"
            + "# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D \"WIN32\""
            + " /D \"NDEBUG\" /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
            + " /D \""
            + interlibName.toUpperCase()
            + "_EXPORTS\" /YX /FD /c\r\n"
            + "# ADD CPP /nologo /MT /W3 /GX /O2 /D \"WIN32\" /D \"NDEBUG\""
            + " /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
            + " /D \""
            + interlibName.toUpperCase()
            + "_EXPORTS\" /YX /FD /c /I "
            + "\"d:/users/arnould/jdk1.4.1/include/\" "
            + "/I "
            + "\"d:/users/arnould/jdk1.4.1/include/win32/\" \r\n"
            + "# ADD BASE MTL /nologo /D \"NDEBUG\" /mktyplib203 /win32\r\n"
            + "# ADD MTL /nologo /D \"NDEBUG\" /mktyplib203 /win32\r\n"
            + "# ADD BASE RSC /l 0x40c /d \"NDEBUG\"\r\n"
            + "# ADD RSC /l 0x40c /d \"NDEBUG\"\r\n"
            + "BSC32=bscmake.exe\r\n"
            + "# ADD BASE BSC32 /nologo\r\n"
            + "# ADD BSC32 /nologo\r\n"
            + "LINK32=link.exe\r\n"
            + "# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib"
            + " comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid"
            + ".lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386\r\n"
            + "# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib"
            + " advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib"
            + " /nologo /dll /machine:I386\r\n\r\n"
            + "!ELSEIF  \"$(CFG)\" == \""
            + interlibName
            + " - Win32 Debug\"\r\n\r\n"
            + "# PROP BASE Use_MFC 0\r\n"
            + "# PROP BASE Use_Debug_Libraries 1\r\n"
            + "# PROP BASE Output_Dir \"Debug\"\r\n"
            + "# PROP BASE Intermediate_Dir \"Debug\"\r\n"
            + "# PROP BASE Target_Dir \"\"\r\n"
            + "# PROP Use_MFC 0\r\n"
            + "# PROP Use_Debug_Libraries 1\r\n"
            + "# PROP Output_Dir \"" + System.getProperty("user.dir")
            + "\\" + dllDir.substring(1, dllDir.length())
            + "\r\n"
            + "# PROP Intermediate_Dir \"Debug\"\r\n"
            + "# PROP Ignore_Export_Lib 0\r\n"
            + "# PROP Target_Dir \"\"\r\n"
            + "# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D \"WIN32\""
            + " /D \"_DEBUG\" /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
            + " /D \""
            + interlibName.toUpperCase()
            + "_EXPORTS\" /YX /FD /GZ /c\r\n"
            + "# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od  /D \"WIN32\""
            + " /D \"_DEBUG\" /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
            + " /D \""
            + interlibName.toUpperCase()
            + "_EXPORTS\" /YX /FD /GZ /c /I "
            + "\"d:/users/arnould/jdk1.4.1/include/\" "
            + "/I "
            + "\"d:/users/arnould/jdk1.4.1/include/win32/\" \r\n"
            + "# ADD BASE MTL /nologo /D \"_DEBUG\" /mktyplib203 /win32\r\n"
            + "# ADD MTL /nologo /D \"_DEBUG\" /mktyplib203 /win32\r\n"
            + "# ADD BASE RSC /l 0x40c /d \"_DEBUG\"\r\n"
            + "# ADD RSC /l 0x40c /d \"_DEBUG\"\r\n"
            + "BSC32=bscmake.exe\r\n"
            + "# ADD BASE BSC32 /nologo\r\n"
            + "# ADD BSC32 /nologo\r\n"
            + "LINK32=link.exe\r\n"
            + "# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32"
            + ".lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib"
            + " odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept\r\n"
            + "# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib"
            + " advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32"
            + ".lib /nologo /dll /debug /machine:I386 /pdbtype:sept /libpath:\".\"\r\n"
            + "# SUBTRACT LINK32 /profile /map /nodefaultlib\r\n\r\n"
            + "!ENDIF \r\n\r\n"
            + "# Begin Target\r\n\r\n"
            + "# Name \""
            + interlibName
            + " - Win32 Release\"\r\n"
            + "# Name \""
            + interlibName
            + " - Win32 Debug\"\r\n"
            + "# Begin Group \"Source Files\"\r\n\r\n"
            + "# PROP Default_Filter \"cpp;c;cxx;rc;def;r;odl;idl;hpj;bat\"\r\n"
            + "# Begin Source File\r\n\r\n"
            + "SOURCE=.\\jni"
            + actor.getName()
            + ".cpp\r\n"
            + "# End Source File\r\n"
            + "# End Group\r\n"
            + "# Begin Group \"Header Files\"\r\n\r\n"
            + "# PROP Default_Filter \"h;hpp;hxx;hm;inl\"\r\n"
            + "# Begin Source File\r\n\r\n"
            + "SOURCE=.\\jni_jni"
            + libName
            + "_Jni"
            + actor.getName()
            + ".h\r\n"
            + "# End Source File\r\n"
            + "# Begin Source File\r\n\r\n"
            + "SOURCE=\"" + System.getProperty("user.dir")
            + "\\" + dllDir.substring(1, dllDir.length()-1) + "\\"
            + libName
            + ".h\"\r\n"
            + "# End Source File\r\n"
            + "# End Group\r\n"
            + "# Begin Group \"Resource Files\"\r\n\r\n"
            + "# PROP Default_Filter \"ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe\"\r\n"
            + "# Begin Source File\r\n\r\n"
            + "SOURCE=\"" + System.getProperty("user.dir")
            + "\\" + dllDir.substring(1, dllDir.length()-1) + "\\"
            + libName
            + ".dll\"\r\n"
            + "# End Source File\r\n"
            + "# Begin Source File\r\n\r\n"
            + "SOURCE=\"" + System.getProperty("user.dir")
            + "\\" + dllDir.substring(1, dllDir.length()-1) + "\\"
            + libName
            + ".lib\"\r\n"
            + "# End Source File\r\n"
            + "# End Group\r\n"
            + "# End Target\r\n"
            + "# End Project\r\n");
        File dspFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/Jni"
                    + interlibName
                    + ".dsp");
	FileWriter writer = new FileWriter(dspFile);
	writer.write(results.toString());
	writer.close();
    }


    /** Export a makefile */
    protected static void _exportMakefile(GenericJNIActor actor)
	throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();
        String libName = "";
        String dllDir = "";
        try {
            libName =
                ((StringToken) ((Parameter) actor
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();
            dllDir =
                ((StringToken) ((Parameter) actor.getAttribute("DLLs Directory"))
                        .getToken())
                .toString();
        } catch (Exception ex) {
	    throw new IllegalActionException(actor, ex, 
					 "no library or function name !");
        }
        libName = libName.substring(1, libName.length() - 1);
        String interlibName = "jni" + libName;
	String libraryPath = dllDir;
	if (libraryPath.equals("")) {
	    libraryPath = ".";
	}
        results
	    .append("# Makefile automatically generated for JNI\n"
		    + "ROOT =\t\t" 
		    + StringUtilities.getProperty("ptolemy.ptII.dir") + "\n\n"
                    + "# Get configuration info\n"
		    + "CONFIG =\t$(ROOT)/mk/ptII.mk\n" 
		    + "include $(CONFIG)\n\n"
		    + interlibName + ":\n"
		    + "\t$(PTCC) \\\n"
		    + "\t\t-I$(PTJAVA_DIR)/include \\\n"
		    + "\t\t-I$(PTJAVA_DIR)/include/$(PTJNI_ARCHITECTURE) \\\n"
		    + "\t\t-fno-exceptions \\\n"
		    + "\t\t-Wl,--add-stdcall-alias -shared \\n"
		    + "\t\t-L" + libraryPath + " -l" + libName + " \\\n"
		    + "\t\t -o " + libName
		    + ".$(PTJNI_SHAREDLIBRARY_SUFFIX) \\\n"
		    + "\t\tjni" + actor.getName() + ".cpp\n\n"
                    + "# Get the rest of the rules\n"
		    + "include $(ROOT)/mk/ptcommon.mk\n"
		    );

        File makeFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/Jni"
                    + interlibName
                    + ".mk");
	FileWriter writer = new FileWriter(makeFile);
	writer.write(results.toString());
	writer.close();
    }

    /** Get the args belonging to this entity.
     * @return a vector of in arguments.
     */
    protected static Vector _getArgs(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn) {
        Vector ret = new Vector();
        List inArgs = actor.argumentsList();
        Iterator ite = inArgs.iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (arg != null
                    && arg.isInput() == isInput
                    && arg.isOutput() == isOutput
                    && arg.isReturn() == isReturn) {
                ret.add((Object) arg);
            }
        }
        return ret;
    }

    /** Get the args belonging to this entity.
     *  @return the name of each in arguments.
     */
    protected static String _getArgs(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(actor, isInput, isOutput, isReturn).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (arg != null) {
                if (ret == "")
                    ret = arg.getName();
                else
                    ret = ret + separator + arg.getName();
            }
        }
        return ret;
    }

    /** Get the args belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected static String _getArgsWithCType(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(actor, isInput, isOutput, isReturn).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (arg != null) {
                if (ret == "")
                    ret = arg.getCType() + " " + arg.getName();
                else
                    ret = ret + ", " + arg.getCType() + " " + arg.getName();
            }
        }
        return ret;
    }

    /** Get the args belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected static String _getArgsWithCTypeCast(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(actor, isInput, isOutput, isReturn).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            String add = "";
            if(arg.getJType().endsWith("[]")) add = "_1";
            if (arg != null) {
                if (ret == "")
                    ret = " (" + arg.getC2Type() + ")" + arg.getName() + add;
                else
                    ret =
                        ret
                        + separator
                        + " ("
                        + arg.getC2Type()
                        + ")"
                        + arg.getName() + add;
            }
        }
        return ret;
    }

    /** Get the args belonging to this entity with their java type.
     *  @return the java type and name of each in arguments
     */
    protected static String _getArgsWithJType(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(actor, isInput, isOutput, isReturn).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (ret == "")
                ret = arg.getJType() + " " + arg.getName();
            else
                ret = ret + ", " + arg.getJType() + " " + arg.getName();
        }

        return ret;

    }

    /** Get the args In belonging to this entity with their JNI type, excluding the out arguments.
     *  @return the JNI type and name of each in arguments.
     */
    protected static String _getArgsWithJNIType(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(actor, isInput, isOutput, isReturn).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (ret == "")
                ret = arg.getJNIType() + " " + arg.getName();
            else
                ret = ret + ", " + arg.getJNIType() + " " + arg.getName();
        }
        return ret;
    }

    /** Get the args InOut belonging to this entity.
     *  @return a vector of inout arguments.
     */
    protected static Vector _getArgsInOut(GenericJNIActor actor) {
        return _getArgs(actor, true, true, false);
    }

    /** Get the args InOut belonging to this entity.
     *  @return a vector of inout arguments.
     */
    protected static String _getArgsInOut(GenericJNIActor actor, String separator) {
        return _getArgs(actor, true, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments, excluding the in arguments.
     */
    protected static String _getArgsInOutWithJType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithJType(actor, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected static String _getArgsInOutWithCType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithCType(actor, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type, excluding the out arguments.
     *  @return the JNI type and name of each in arguments.
     */
    protected static String _getArgsInOutWithJNIType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithJNIType(actor, true, true, false, separator);
    }

    /** Get the args Out belonging to this entity.
     *  @return a vector of out arguments, excluding the in arguments.
     */
    protected static Vector _getArgsOut(GenericJNIActor actor) {
        return _getArgs(actor, false, true, false);
    }

    /** Get the args out name belonging to this entity.
     *  @return the name of the out arguments, excluding the in arguments.
     */
    protected static String _getArgsOut(GenericJNIActor actor,
            String separator) {
        return _getArgs(actor, false, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments,
     *  excluding the in arguments.
     */
    protected static String _getArgsOutWithJType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithJType(actor, false, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their c type.
     *  @return the c type of the out arguments.
     */
    protected static String _getArgsOutWithCType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithCType(actor, false, true, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type,
     *   excluding the out arguments.
     *  @return the JNI type and name of each in arguments, excluding
     *  the in arguments.
     */
    protected static String _getArgsOutWithJNIType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithJNIType(actor, false, true, false, separator);
    }

    /** Get the args In belonging to this entity.
     *  @return a vector of out arguments, excluding the in arguments.
     */
    protected static Vector _getArgsIn(GenericJNIActor actor) {
        return _getArgs(actor, true, false, false);
    }

    /** Get the args In name belonging to this entity.
     *  @return the name of the out arguments, excluding the in arguments.
     */
    protected static String _getArgsIn(GenericJNIActor actor, String separator) {
        return _getArgs(actor, true, false, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments,
     *  excluding the in arguments.
     */
    protected static String _getArgsInWithJType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithJType(actor, true, false, false, separator);
    }

    /** Get the args Out belonging to this entity with their c type.
     *  @return the c type of the out arguments.
     */
    protected static String _getArgsInWithCType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithCType(actor, true, false, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type,
     *  excluding the out arguments.
     *  @return the JNI type and name of each in arguments, excluding
     *  the in arguments.
     */

    protected static String _getArgsInWithJNIType(
            GenericJNIActor actor,
            String separator) {
        return _getArgsWithJNIType(actor, true, false, false, separator);
    }

    /** Get the return argument belonging to this entity with its JNI type.
     *  @return the JNI type of the return argument.
     */
    protected static String _getArgReturnWithJNIType(GenericJNIActor actor) {
        return _getArgsWithJNIType(actor, false, false, true, "");
    }

    /**
     *  @return the signature of the interface fonction.
     */
    protected static String _signatureSendResults(GenericJNIActor actor) {
        String returnValue = "(";

        //out
        Iterator ite = _getArgsOut(actor).iterator();
        while (ite.hasNext()) {
            String typ = ((Argument) ite.next()).getJType();
            returnValue = returnValue + _signature(typ);
        }
        //in out
        ite = _getArgsInOut(actor).iterator();
        while (ite.hasNext()) {
            String typ = ((Argument) ite.next()).getJType();
            returnValue = returnValue + _signature(typ);
        }

        returnValue = returnValue + ")V";

        return returnValue;
    }

    /**
     *  @return the signature of the interface fonction.
     */
    protected static String _signature(String typ) {
        String ret = "";
        if (typ.endsWith("[]"))
            ret = "[";

        if (typ.equals("boolean")||typ.startsWith("boolean"))
            ret = ret + "Z";
        else if (typ.equals("byte")||typ.startsWith("byte"))
            ret = ret + "B";
        else if (typ.equals("char")||typ.startsWith("char"))
            ret = ret + "C";
        else if (typ.equals("short")||typ.startsWith("short"))
            ret = ret + "S";
        else if (typ.equals("int")||typ.startsWith("int"))
            ret = ret + "I";
        else if (typ.equals("long")||typ.startsWith("long"))
            ret = ret + "J";
        else if (typ.equals("float")||typ.startsWith("float"))
            ret = ret + "F";
        else if (typ.equals("double")||typ.startsWith("double"))
            ret = ret + "D";
        else if (typ.equals("Object")||typ.startsWith("Object"))
            ret = ret + "L";
        return ret;
    }

    /** Test the given string to know if a comma is needed
        @param string the string to test
        @return "" if str is null, "," else
    */
    private static String _virgule(String string) {
        if (string.equals("")) {
            return "";
        }
        return ",";
    }

    /** Test the given string to know if a comma is needed
        @param string1 the first string to test
        @param string2 the second string to test
        @return "," if str != null && str2 != null, else return ""
    */
    private static String _virgule(String string1, String string2) {
        if (string1.equals("")) {
            return "";
        } else if (!string1.equals("")) {
            return ",";
        }
        return "";
    }

    private static String _indent1 = StringUtilities.getIndentPrefix(1);
    private static String _indent2 = StringUtilities.getIndentPrefix(2);
}

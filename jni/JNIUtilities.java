/** A graph editor frame for Ptolemy models that use the JNI interface.

Copyright (c) 2003 The Regents of the University of California.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ptolemy.actor.Actor;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.StreamExec;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;


//////////////////////////////////////////////////////////////////////////
//// JNIUtilities
/**
A collection of utilities for generating Java Native Interface (JNI) classes.

<p>For information about JNI, see
<a href="http://java.sun.com/docs/books/tutorial/native1.1/concepts/index.html"><code>http://java.sun.com/docs/books/tutorial/native1.1/concepts/index.html</code></a>

<p>For information about how to create shared libraries under Windows using
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

<li> Set the CLASSPATH to include the current directory
<pre>
CLASSPATH=.
export CLASSPATH
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
<dt><code><b>libraryDirectory</b></code>
<dd><code>""</code>
<dt><code><b>nativeFunction:</b></code>
<dd><code>"meaningOfLife"</code>
<dt><code><b>nativeLibrary</b></code>
<dd><code>"meaningOfLife"</code>

</dl>
<li> Select Commit to close the Edit Parameters window
<li> Select JNI from the menu and
then select Generate C Interface
<li> FIXME: Copy the dlls into a directory that is in the path
<pre>
cp meaningOfLife.dll $PTII/bin
cp jni/jnimeaningOfLife/JnijnimeaningOfLife.dll $PTII/bin
</pre>
<li> Save the model.
<br>FIXME: Because of an apparent bug, it is necessary
to save the model for the port we just created to appear
<li> Add a Display actor from the Sink folder in the
Actor Library and connect the input of the Display actor
to the output of the meaningOfLife Actor
<li> FIXME: Because of a bug in the Ptolemy interface to the JNI
actor, you must specify the type of the output of the meaningOfLife
actor by right clicking on the actor and selecting
Configure Ports and then entering double for
output type
<li> Drag in a SDF director and set the number of iterations to 1.
<li> Select Run
<li> The answer to "the meaning of life, the universe, and everything"
should appear in the Display actor.
</ol>
If you get an error like:
<pre>
/cygdrive/c/Program Files/j2sdk1.4.1_01/include/win32/jni_md.h:16: syntax error before `;'
</pre>
Then see
<a href="http://www.xraylith.wisc.edu/~khan/software/gnu-win32/README.jni.txt" target="_top"><code>http://www.xraylith.wisc.edu/~khan/software/gnu-win32/README.jni.txt</code></a>
<br>You might need to edit jni_md.h, the above URL says
<blockquote>
GCC doesn't have a __int64 built-in, and this patch basically uses
"long long" instead.

<ol>
<li> Edit the file <jdk_root>/include/win32/jni_md.h, Where <jdk_root>
   is the installation root (eg., c:/jdk1.1.7A).

<li> Replace the segment:
</ol>

<pre>
    typedef long jint;
    typedef __int64 jlong;
</pre>
with:
<pre>
    typedef long jint;
    #ifdef __GNUC__
    typedef long long jlong;
    #else
    typedef __int64 jlong;
    #endif
    typedef signed char jbyte;
</pre>
<blockquote>




@author Vincent Arnould (vincent.arnould@thalesgroup.com), contributor Christopher Hylands
@version $Id$
@since Ptolemy II 2.3
*/
public class JNIUtilities {

    /** Instances of this class cannot be created.
     */
    private JNIUtilities() {
        // We could try to have non static fields for the parameter values
        // but that would make things tricky since we want to have
        // a method that takes a model which will generate JNI
        // code for all the GenericJNIActors in a model.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a model, generate JNI files for all GenericJNIActors.
     *  @param model The model to generate JNI files for any
     *  contained GenericJNIActors.
     *  @return true if a GenericJNIActor was found.
     *  @exception Exception If there was a problem creating the JNI files.
     */
    public static boolean generateJNI(CompositeEntity model)
            throws Exception {
        boolean success = false;
        if (model instanceof Actor) {
            List actorsList = model.allAtomicEntityList();

            Iterator actors = actorsList.iterator();
            while (actors.hasNext()) {
                Object actor = actors.next();

                if (actor instanceof GenericJNIActor) {
                    JNIUtilities.generateJNI(model,
                            (GenericJNIActor) actor);
                }
            }
            success = true;
        }
        return success;
    }

    /** Generate JNI files for one actor in a model
     */
    public static void generateJNI(CompositeEntity model,
            GenericJNIActor actor)
            throws Exception {
        System.out.println("Generating JNI for " + actor.getName());

        // V‰rification qu'il y a un return, place le return en void sinon
        if (actor.getArgumentReturn() == null) {
            MessageHandler.warning(
                    "No return was configured,\n so we set it as void");
            actor.addArgumentReturn();
        }

        //Cr‰ation des ports
        Iterator relations = model.relationList().iterator();
        while (relations.hasNext()) {
            ((ptolemy.kernel.ComponentRelation)(relations.next())).unlinkAll();
        }
        actor.removeAllPorts();
        actor.createPorts();

        // Name of the actor and function
        String nativeFunction = _getNativeFunction(actor);
        String nativeLibrary = getNativeLibrary(actor);

        // Rename the actor.
        String newName = nativeLibrary + "I"
            + nativeFunction;

        // About the renaming, Edward wrote:
        //
        // Why rename an actor <library name>I<function name>?  I
        // guess you probably would really like to use "."  instead of
        // "I", but unfortunately, Ptolemy doesn't allow this.
        // Perhaps "-" would be easier to read?  Or "_"?  Or " "?


        // Vincent responded with:
        //
        // The "_" is used by JNI to construct the name of the native
        // interface function :
        // "Java_packageName_subPackagesNames_ClassName_nativeFunctionName"
        // where the Class is the one containing the native function
        // call : so no way to use "_".  The " " and "-" are also not
        // usable because JNI doesn't support native function name
        // with such characters.


        if (!actor.getName().startsWith(newName)) {
            // If the name of the actor does not already start with
            // the new name, then set the name.
            // This would be the case if we had a model that had
            // multiple GenericJNIActors in it and we had already
            // created the JNI files for those actors.
            try {
                actor.setName(newName);
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(actor, ex,
                        "Unable to rename GenericJNIActor '"
                        + actor.getName()
                        + "' to '"
                        + newName
                        + "': \n"
                        + "An JNI Actor already exists!\n");
            }
        }

        //render the graph within its controller
        // FIXME: when this method was in ThalesGraphFrame, we rerendered.
        //_controller.rerender();

        //actor.notifyAll();

        String interNativeLibrary = _getInterNativeLibrary(actor);
        String destinationDirectory =
            System.getProperty("user.dir") + "/jni/"
            + nativeLibrary;


        //Cr‰ation du fichier Java
        _exportJavaInterfaceFile(actor, destinationDirectory);

        // FIXME: if we are running under Vergil, we should use
        // the graphical exec code in Copernicus so that the user
        // can see the execution.  If we are running without a UI.
        // We should call exec().

        //Compilation java de la classe interface:


        //Cr‰ation du fichier C
        _exportCInterfaceFile(actor, destinationDirectory);
        _exportDSP(actor, destinationDirectory);
        _exportMakefile(actor, destinationDirectory);

        List execCommands = new LinkedList();

        // Create the .class file.
        execCommands.add("javac -classpath \""
                + StringUtilities.getProperty("ptolemy.ptII.dir")
                + "\" jni/" + nativeLibrary
                + "/Jni"
                + actor.getName()
                + ".java");
        // Create the .h file.
        execCommands.add("javah -d jni/" + nativeLibrary
                + " jni." + nativeLibrary
                + ".Jni"
                + actor.getName());

        // Create the shared library.
        execCommands.add("make -C jni/" + nativeLibrary + " -f "
                + "Jni" + interNativeLibrary + ".mk");


        StreamExec javaExec = new StreamExec();
        javaExec.setCommands(execCommands);
        javaExec.start();

    }

    // Return the value of the nativeLibrary argument with the double
    // quotes stripped off.
    public static String getNativeLibrary(GenericJNIActor actor)
            throws IllegalActionException {
        String nativeLibrary =
            (((StringToken) ((Parameter) actor
                    .getAttribute("nativeLibrary"))
                    .getToken())
                    .toString());
        return nativeLibrary.substring(1, nativeLibrary.length() - 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the JNI C file.
     *  @param actor Actor to generate a JNI C file for.
     *  @param destinationDirectory Directory to create the file in.
     */
    protected static File _exportCInterfaceFile(GenericJNIActor actor,
            String destinationDirectory)
            throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();

        String libraryDirectory = _getLibraryDirectory(actor);
        String nativeFunction = _getNativeFunction(actor);
        String nativeLibrary = getNativeLibrary(actor);

        String interNativeFunction = _getInterNativeFunction(actor);

        String returnType = "";
        String returnName = "";
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
        arguments = _getArguments(actor, false, false, true).iterator();
        if (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            returnJNIType = argument.getJNIType();
        }

        results.append("#include \"jni.h\" \n"
                // le fichier entete de la librairie existante
                // Don't use io stream it results in compile time errors under
                // gcc with cygwin
                +"/* #include <iostream> */\n"
                + "#include \"" + System.getProperty("user.dir")
                + "/" + libraryDirectory + "/"
                + nativeLibrary
                + ".h\"\n"
                // le fichier entete g‰n‰r‰ par javah
                +"#include \"jni_"
                + nativeLibrary
                + "_Jni"
                + actor.getName()
                + ".h\"\n"
                + "/*******************************************************\n"
                + "*********************"
                + actor.getName()
                + "_"
                + interNativeFunction
                + "******************\n"
                + "*****************************************************/\n\n"
                + "//Declaration of the preexisting function\n"
                + "//extern \"C\" "
                + returnType
                + " "
                + nativeFunction
                + "("
                + _getArgumentsInWithCType(actor, ",")
                + _virgule( _getArgumentsInWithCType(actor, ","),
                        _getArgumentsInOut(actor, ","))
                + _getArgumentsInOutWithCType(actor, ",")
                + _virgule(_getArgumentsInOut(actor, ","),
                        _getArgumentsOutWithCType(actor, ","))
                + _getArgumentsOutWithCType(actor, ",")
                + ");\n\n"
                + "JNIEXPORT "
                + actor.getArgumentReturn().getJNIType()
                + " JNICALL Java_jni_"
                + nativeLibrary
                + "_Jni"
                + actor.getName()
                + "_"
                + interNativeFunction
                + "(\nJNIEnv *env, jobject jobj "
                + _virgule(_getArgumentsInWithJNIType(actor, ","))
                + _getArgumentsInWithJNIType(actor, ",")
                + _virgule(_getArgumentsInWithJNIType(actor, ","),
                        _getArgumentsInOutWithCType(actor, ","))
                + _getArgumentsInOutWithJNIType(actor, ",")
                + _virgule(_getArgumentsInOutWithJNIType(actor, ","),
                        _getArgumentsOutWithCType(actor, ","))
                + _getArgumentsOutWithJNIType(actor, ",")
                + ")\n"
                + "{\n"
                + _indent1 + "// Declaration des sorties\n");

        arguments = _getArgumentsOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            results.append(argument.getJNIType() + " "
                    + argument.getName() + ";\n");
        }

        results.append("\n"
                + _indent1 + "// structure and  exit of the C function\n"
                + _indent1 + "//target_location_struct *target_location = "
                + "new target_location_struct;\n\n"
                + _indent1 + "// appel de la librairie existante\n\n");

        //For the array inout and in
        arguments = _getArgumentsInOut(actor).iterator();
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


        arguments = _getArgumentsIn(actor).iterator();
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
                        + "//if ((long *)in_1 == (long *)0 || (long *)inout_1 == (long *)0)n" +
                        "//{\n" + "//std::cout << \"warning : the matrix is empty !\""
                        + " << std::endl;\n" +
                        "//}\n");
            }
        }


        //if they is a return
        if (!returnJNIType.equals("void")) {
            results.append(_indent1 + returnJNIType
                    + " _" + returnName
                    + " = (" + returnJNIType + ")");
        }

        //native function call

        results.append(
                nativeFunction
                + "("
                + _getArgumentsWithCTypeCast(actor, true, false, false, ",")
                + _virgule(_getArgumentsInWithCType(actor, ","),
                        _getArgumentsInOut(actor, ","))
                + _getArgumentsWithCTypeCast(actor, true, true, false, ",")
                + _virgule(_getArgumentsInOutWithCType(actor, ","),
                        _getArgumentsOut(actor, ","))
                + _getArgumentsWithCTypeCast(actor, false, true, false, ",")
                + ");\n\n");

        //Release memory in native side
        //for in
        arguments = _getArgumentsIn(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            String typ = arg.getJType();
            if (typ.endsWith("[]")) {
                typ = typ.substring(0, typ.length()-2);
                results.append( "env->Release"
                        + typ.substring(0, 1).toUpperCase()
                        + typ.substring(1, typ.length())
                        + "ArrayElements("
                        + arg.getName() + ", (" + arg.getC2TypeHack() + ")"
                        + arg.getName() + "_1,0);\n");
            }
        }
        //for inout
        arguments = _getArgumentsInOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            String typ = arg.getJType();
            if (typ.endsWith("[]")) {
                typ = typ.substring(0, typ.length()-2);
                results.append("env->Release"
                        + typ.substring(0, 1).toUpperCase()
                        + typ.substring(1, typ.length())
                        + "ArrayElements("
                        + arg.getName() + ",(" + arg.getC2TypeHack()
                        + ")" + arg.getName() + "_1,0);\n");
            }
        }

        if (!returnJNIType.equals("void")) {
            results.append( _indent1 + "if (_" + returnName + " == 0) {\n"
                    + _indent2 + "//std::cout << \"warning : return = "
                    + "0\" << std::endl;\n"
                    + _indent1 + "}\n");
        }

        if (!(_getArgumentsOut(actor, "").equals("")
                && _getArgumentsInOut(actor, "").equals(""))) {
            results.append(_indent1 + "// envoi des sorties dans l'environnement JAVA / PTOLEMY II\n"
                    + _indent1 + "jclass cls = env->GetObjectClass(jobj);\n"
                    + _indent1 + "jmethodID mid = env->GetMethodID(cls, "
                    + "\"sendResults\", \""
                    + _signatureSendResults(actor)
                    + "\");\n"
                    + _indent1 + "if (mid == 0) {\n"
                    + _indent2 + "//std::cout << \"Can't find sendResults method\";\n"
                    + _indent2 + "printf(\"Can't find sendResults method\");\n");

            if (!returnJNIType.equals("void")) {
                results.append(_indent2 + "return _" + returnName + ";\n}\n");
            } else {
                results.append(_indent2 + "return;\n}\n");
            }
            results.append(_indent1 + "env->CallVoidMethod(jobj,mid," + _getArgumentsOut(actor, ",")
                    //warning : ici on suppose qu'il y a toujours au - 1 argument Out !! TBFix
                    +_virgule( _getArgumentsOut(actor, ","), _getArgumentsInOut(actor, ","))
                    + _getArgumentsInOut(actor, ",")
                    + ");\n"
                    + _indent1 + "if (env->ExceptionOccurred()) {\n"
                    + _indent2 + "//std::cout << \"Can't get back results!!\" << std::endl;\n"
                    + _indent2 + "printf(\"Can't get back results!!\");\n"
                    + _indent2 + "env->ExceptionDescribe();\n"
                    + _indent2 + "env->ExceptionClear();\n"
                    + _indent1 + "}\n"
                    + _indent1 + "env->ExceptionClear();\n\n");
        }

        results.append(_indent1 + "// M‰nage\n");

        /*arguments = _getArgumentsOut(actor).iterator();
          while (arguments.hasNext()) {
          Argument arg = (Argument) arguments.next();
          results.append( "delete " + " " + arg.getName() + ";\n";
          }
          arguments = _getArgumentsInOut(actor).iterator();
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
            new File(destinationDirectory
                    + "/jni"
                    + actor.getName()
                    + ".cpp");
        FileWriter writer = new FileWriter(cFile);
        writer.write(results.toString());
        writer.close();
        return cFile;
    }

    /** Export the Visual Studio project.
     *  @param actor Actor to generate a JNI Java file for.
     *  @param destinationDirectory Directory to create the file in.
     */
    protected static void _exportDSP(GenericJNIActor actor,
            String destinationDirectory)
            throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();

        String libraryDirectory = _getLibraryDirectory(actor);
        String nativeLibrary = getNativeLibrary(actor);

        String interNativeLibrary = _getInterNativeLibrary(actor);
        results.append(
                "# Microsoft Developer Studio Project File - Name=\""
                + interNativeLibrary
                + "\" - Package Owner=<4>\r\n"
                + "# Microsoft Developer Studio Generated Build File,"
                + " Format Version 6.00\r\n"
                + "# ** DO NOT EDIT **\r\n"
                + "# TARGTYPE \"Win32 (x86) Dynamic-Link Library\" 0x0102\r\n"
                + "CFG="
                + interNativeLibrary
                + " - Win32 Debug\r\n"
                + "!MESSAGE This is not a valid makefile. To build this project"
                + " using NMAKE,\r\n"
                + "!MESSAGE use the Export Makefile command and run\r\n"
                + "!MESSAGE \r\n"
                + "!MESSAGE NMAKE /f \""
                + interNativeLibrary
                + ".mak\".\r\n"
                + "!MESSAGE \r\n"
                + "!MESSAGE You can specify a configuration when running NMAKE\r\n"
                + "!MESSAGE by defining the macro CFG on the command line. For example:\r\n"
                + "!MESSAGE \r\n"
                + "!MESSAGE NMAKE /f \""
                + interNativeLibrary
                + ".mak\" CFG=\""
                + interNativeLibrary
                + " - Win32 Debug\"\r\n"
                + "!MESSAGE \r\n"
                + "!MESSAGE Possible choices for configuration are:\r\n"
                + "!MESSAGE \r\n"
                + "!MESSAGE \""
                + interNativeLibrary
                + " - Win32 Release\" (based on \"Win32 (x86) Dynamic-Link Library\")\r\n"
                + "!MESSAGE \""
                + interNativeLibrary
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
                + interNativeLibrary
                + "- Win32 Release\"\r\n\r\n"
                + "# PROP BASE Use_MFC 0\r\n"
                + "# PROP BASE Use_Debug_Libraries 0\r\n"
                + "# PROP BASE Output_Dir \"Release\"\r\n"
                + "# PROP BASE Intermediate_Dir \"Release\"\r\n"
                + "# PROP BASE Target_Dir \"\" \r\n"
                + "# PROP Use_MFC 0\r\n"
                + "# PROP Use_Debug_Libraries 0\r\n"
                + "# PROP Output_Dir \"" + System.getProperty("user.dir")
                + "\\" + libraryDirectory
                + "\r\n"
                + "# PROP Intermediate_Dir \"Release\"\r\n"
                + "# PROP Target_Dir \"\" \r\n"
                + "# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D \"WIN32\""
                + " /D \"NDEBUG\" /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
                + " /D \""
                + interNativeLibrary.toUpperCase()
                + "_EXPORTS\" /YX /FD /c\r\n"
                + "# ADD CPP /nologo /MT /W3 /GX /O2 /D \"WIN32\" /D \"NDEBUG\""
                + " /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
                + " /D \""
                + interNativeLibrary.toUpperCase()
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
                + interNativeLibrary
                + " - Win32 Debug\"\r\n\r\n"
                + "# PROP BASE Use_MFC 0\r\n"
                + "# PROP BASE Use_Debug_Libraries 1\r\n"
                + "# PROP BASE Output_Dir \"Debug\"\r\n"
                + "# PROP BASE Intermediate_Dir \"Debug\"\r\n"
                + "# PROP BASE Target_Dir \"\"\r\n"
                + "# PROP Use_MFC 0\r\n"
                + "# PROP Use_Debug_Libraries 1\r\n"
                + "# PROP Output_Dir \"" + System.getProperty("user.dir")
                + "\\" + libraryDirectory
                + "\r\n"
                + "# PROP Intermediate_Dir \"Debug\"\r\n"
                + "# PROP Ignore_Export_Lib 0\r\n"
                + "# PROP Target_Dir \"\"\r\n"
                + "# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D \"WIN32\""
                + " /D \"_DEBUG\" /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
                + " /D \""
                + interNativeLibrary.toUpperCase()
                + "_EXPORTS\" /YX /FD /GZ /c\r\n"
                + "# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od  /D \"WIN32\""
                + " /D \"_DEBUG\" /D \"_WINDOWS\" /D \"_MBCS\" /D \"_USRDLL\""
                + " /D \""
                + interNativeLibrary.toUpperCase()
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
                + interNativeLibrary
                + " - Win32 Release\"\r\n"
                + "# Name \""
                + interNativeLibrary
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
                + nativeLibrary
                + "_Jni"
                + actor.getName()
                + ".h\r\n"
                + "# End Source File\r\n"
                + "# Begin Source File\r\n\r\n"
                + "SOURCE=\"" + System.getProperty("user.dir")
                + "\\" + libraryDirectory + "\\"
                + nativeLibrary
                + ".h\"\r\n"
                + "# End Source File\r\n"
                + "# End Group\r\n"
                + "# Begin Group \"Resource Files\"\r\n\r\n"
                + "# PROP Default_Filter \"ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe\"\r\n"
                + "# Begin Source File\r\n\r\n"
                + "SOURCE=\"" + System.getProperty("user.dir")
                + "\\" + libraryDirectory + "\\"
                + nativeLibrary
                + ".dll\"\r\n"
                + "# End Source File\r\n"
                + "# Begin Source File\r\n\r\n"
                + "SOURCE=\"" + System.getProperty("user.dir")
                + "\\" + libraryDirectory + "\\"
                + nativeLibrary
                + ".lib\"\r\n"
                + "# End Source File\r\n"
                + "# End Group\r\n"
                + "# End Target\r\n"
                + "# End Project\r\n");
        File dspFile =
            new File(destinationDirectory
                    + "/Jni"
                    + interNativeLibrary
                    + ".dsp");
        FileWriter writer = new FileWriter(dspFile);
        writer.write(results.toString());
        writer.close();
    }


    /** Create the JNI Java file.
     *  @param actor Actor to generate a JNI Java file for.
     *  @param destinationDirectory Directory to create the file in.
     */
    protected static File _exportJavaInterfaceFile(GenericJNIActor actor,
            String destinationDirectory)
            throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();

        String nativeLibrary = getNativeLibrary(actor);

        String interNativeLibrary = _getInterNativeLibrary(actor);
        String interNativeFunction = _getInterNativeFunction(actor);

        String returnJType = "";
        List argumentsList = actor.argumentsList();
        Iterator arguments = argumentsList.iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (!(argument == null) && argument.isReturn()) {
                returnJType = argument.getJType();
                break;
            }
        }

        results.append("package jni."
                + nativeLibrary + ";\n\n"
                + "import ptolemy.data.expr.UtilityFunctions;\n"
                + "import java.io.File;\n"
                + "\n\n\n"
                + "/* The class that interface the native function "
                + "call\n"
                + " * @author autogenerated file - by V. Arnould.TRT\n"
                + " */\n"
                + "public class Jni"
                + actor.getName()
                + " { \n\n"
                + _indent1 + "/* The default constructor\n"
                + _indent1 + " * @author autogenerated - "
                + "by V. Arnould.TRT\n"
                + _indent1 + " */\n"
                + _indent1 + "public Jni"
                + actor.getName()
                + "(){}\n\n"
                + _indent1 + "/* The loading of the native library\n"
                + _indent1 + " */\n"
                + _indent1 + "static {\n"
                + _indent2 + "String library = \"jni\""
                + "+ File.separator + \"" + nativeLibrary + "\""
                + "+ File.separator + \"Jni" + interNativeLibrary + "\";\n"
                + _indent2 + "try {\n"
                + _indent3 + "UtilityFunctions.loadLibrary("
                + "library);\n"
                + _indent2 + "} catch (java.lang.UnsatisfiedLinkError ex) {\n"
                + _indent3 + "System.out.println(\"Warning, UtilityFunctions.loadLibrary('\" + library + \"' failed\");\n"
                + _indent3 + "ex.printStackTrace();\n"
                + _indent3 + "UtilityFunctions.loadLibrary(\"Jni"
                + interNativeLibrary + "\");\n"
                + _indent2 + "}\n"
                + _indent1 + "}\n\n"
                + _indent1 + "public native "
                + returnJType
                + " "
                + interNativeFunction
                + "("
                + _getArgumentsInWithJType(actor, ",")
                + _virgule(
                        _getArgumentsInWithJType(actor, ","),
                        _getArgumentsInOutWithJType(actor, ","))
                + _getArgumentsInOutWithJType(actor, ",")
                + _virgule(
                        _getArgumentsInWithJType(actor, ",")+
                        _getArgumentsInOutWithJType(actor, ","),
                        _getArgumentsOutWithJType(actor, ","))
                + "/*foo*/"
                + _getArgumentsOutWithJType(actor, ",")
                + ") throws SecurityException;\n\n"
                + _indent1 + "/** Send the result of the native library.\n"
                + _indent1 + " *  This method is called by the native code\n"
                + _indent1 + " *  @author autogenerated - by V. Arnould.TRT\n"
                + _indent1 + " */\n"
                + _indent1 + "public void sendResults("
                + _getArgumentsOutWithJType(actor, ",")
                + _virgule(
                        _getArgumentsOutWithJType(actor, ","),
                        _getArgumentsInOutWithJType(actor, ","))
                + _getArgumentsInOutWithJType(actor, ",")
                + ") {\n");

        arguments = _getArgumentsOut(actor).iterator();
        while (arguments.hasNext()) {
            String name = ((Argument) arguments.next()).getName();
            results.append( _indent1 + "_" + name + " = " + name + ";\n");
        }

        arguments = _getArgumentsInOut(actor).iterator();
        while (arguments.hasNext()) {
            String name = ((Argument) arguments.next()).getName();
            results.append( _indent1 + "_" + name + " = " + name + ";\n");
        }
        results.append("\n"
                + _indent1 + "}\n\n");
        results.append(
                _indent1 + "/** Call the native function in the native library.\n"
                + _indent1 + " *  This method is called by the GenericJNIActor on fire()\n"
                + _indent1 + " *  @author autogenerated - by V. Arnould.TRT\n"
                + _indent1 + " */\n"
                + _indent1 + " public "
                + returnJType
                + " fire("
                + _getArgumentsInWithJType(actor, ",")
                + _virgule(_getArgumentsInWithJType(actor, ","),
                        _getArgumentsInOutWithJType(actor, ","))
                + _getArgumentsInOutWithJType(actor, ",")
                + _virgule(
                        _getArgumentsInOutWithJType(actor, ","),
                        _getArgumentsOutWithJType(actor, ","))
                + _getArgumentsOutWithJType(actor, ",")
                + ") {\n\n");

        //if there is a returned value
        Argument argRet = (Argument) actor.getArgumentReturn();
        if (!(argRet.getJType().equals("void"))) {
            results.append( _indent2 + "_" + argRet.getName() + " = "
                    + interNativeFunction
                    + "("
                    + _getArgumentsIn(actor, ",")
                    + _virgule( _getArgumentsIn(actor, ","),
                            _getArgumentsInOut(actor, ","))
                    + _getArgumentsInOut(actor, ",")
                    + _virgule( _getArgumentsInOut(actor, ","),
                            _getArgumentsOut(actor, ","))
                    + _getArgumentsOut(actor, ",")
                    + ");"
                    + "\n");

            results.append(_indent2 + "return"
                    + " _" + argRet.getName() + ";\n" + _indent1 + "}\n");
        } else {
            results.append( interNativeFunction
                    + "("
                    + _getArgumentsIn(actor, ",")
                    + _virgule( _getArgumentsIn(actor, ","),
                            _getArgumentsInOut(actor, ","))
                    + _getArgumentsInOut(actor, ",")
                    + _virgule( _getArgumentsInOut(actor, ","),
                            _getArgumentsOut(actor, ","))
                    + _getArgumentsOut(actor, ",")
                    + ");"
                    + "\n }\n");
        }

        results.append("\n"
                + _indent1 + "///////////// public fields\n" + "\n");

        //out
        arguments = _getArgumentsOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            results.append(_indent1 + "public " + arg.getJType() + " "
                    + "_" + arg.getName() + ";\n");
        }
        //inout
        arguments = _getArgumentsInOut(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            results.append(_indent1 + "public " + arg.getJType() + " "
                    + "_" + arg.getName() + ";\n");
        }

        //in
        arguments = _getArgumentsIn(actor).iterator();
        while (arguments.hasNext()) {
            Argument arg = (Argument) arguments.next();
            results.append(_indent1 + "public " + arg.getJType() + " "
                    + "_" + arg.getName() + ";\n");
        }
        //return
        if (!(argRet.getJType().equals("void")))
            results.append(_indent1 + "public " + argRet.getJType() + " " + "_"
                    + argRet.getName() + ";\n");

        results.append( "\n}");
        File dir = new File(destinationDirectory);
        //Creation du repertoire
        try {
            dir.mkdirs();
        } catch (NullPointerException ex) {
            throw new IllegalActionException(null, ex, "No directory '"
                    + dir + "'");
        }

        File javaFile =
            new File(destinationDirectory
                    + "/Jni"
                    + actor.getName()
                    + ".java");

        FileWriter writer = new FileWriter(javaFile);
        writer.write(results.toString());
        writer.close();

        return javaFile;
    }

    /** Export a makefile
     *  @param actor Actor to generate a makefile for
     *  @param destinationDirectory Directory to create the file in.
     */
    protected static void _exportMakefile(GenericJNIActor actor,
            String destinationDirectory)
            throws IllegalActionException, IOException {
        StringBuffer results = new StringBuffer();

        String libraryDirectory = _getLibraryDirectory(actor);
        String nativeLibrary = getNativeLibrary(actor);

        String interNativeLibrary = _getInterNativeLibrary( actor);
        String libraryPath = libraryDirectory;
        if (libraryPath.equals("\"\"")) {
            libraryPath = ".";
        }
        //FIXME: this is a HACK, libraryPath should not be set like this
        libraryPath = "../..";
        results
            .append("# Makefile automatically generated for JNI\n"
                    + "ROOT =\t\t"
                    + StringUtilities.getProperty("ptolemy.ptII.dir") + "\n\n"
                    + "# Get configuration info\n"
                    + "CONFIG =\t$(ROOT)/mk/ptII.mk\n"
                    + "include $(CONFIG)\n\n"
                    + "SHAREDLIBRARY ="
                    + "$(PTJNI_SHAREDLIBRARY_PREFIX)Jni"
                    + interNativeLibrary
                    + ".$(PTJNI_SHAREDLIBRARY_SUFFIX)\n"
                    + "$(SHAREDLIBRARY):\n"
                    + "\t\"$(PTCC)\" \\\n"
                    + "\t\t\"-I$(PTJAVA_DIR)/include\" \\\n"
                    + "\t\t\"-I$(PTJAVA_DIR)/include/$(PTJNI_ARCHITECTURE)\" \\\n"
                    + "\t\t-fno-exceptions \\\n"
                    + "\t\t-shared $(PTJNI_SHAREDLIBRARY_LDFLAG) \\\n"
                    + "\t\t-L" + libraryPath + " -l" + nativeLibrary + " \\\n"
                    + "\t\t -o $@ \\\n"
                    + "\t\tjni" + actor.getName() + ".cpp\n\n"
                    + "# Get the rest of the rules\n"
                    + "include $(ROOT)/mk/ptcommon.mk\n"
                    );

        File makeFile =
            new File(destinationDirectory
                    + "/Jni"
                    + interNativeLibrary
                    + ".mk");
        FileWriter writer = new FileWriter(makeFile);
        writer.write(results.toString());
        writer.close();
    }

    /** Get the args belonging to this entity.
     * @return a vector of in arguments.
     */
    protected static Vector _getArguments(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn) {
        Vector returnValue = new Vector();
        Iterator arguments = actor.argumentsList().iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (argument != null
                    && argument.isInput() == isInput
                    && argument.isOutput() == isOutput
                    && argument.isReturn() == isReturn) {
                returnValue.add((Object) argument);
            }
        }
        return returnValue;
    }

    /** Get the args belonging to this entity.
     *  @return the name of each in arguments.
     */
    protected static String _getArguments(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        StringBuffer returnValue = new StringBuffer();
        Iterator arguments =
            _getArguments(actor, isInput, isOutput, isReturn).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (argument != null) {
                if (returnValue.length() > 0) {
                    returnValue.append(separator);
                }
                returnValue.append(argument.getName());
            }
        }
        return returnValue.toString();
    }

    /** Get the args In belonging to this entity.
     *  @return a vector of out arguments, excluding the in arguments.
     */
    protected static Vector _getArgumentsIn(GenericJNIActor actor) {
        return _getArguments(actor, true, false, false);
    }

    /** Get the args In name belonging to this entity.
     *  @return the name of the out arguments, excluding the in arguments.
     */
    protected static String _getArgumentsIn(GenericJNIActor actor,
            String separator) {
        return _getArguments(actor, true, false, false, separator);
    }

    /** Get the args InOut belonging to this entity.
     *  @return a vector of inout arguments.
     */
    protected static Vector _getArgumentsInOut(GenericJNIActor actor) {
        return _getArguments(actor, true, true, false);
    }

    /** Get the args InOut belonging to this entity.
     *  @return a vector of inout arguments.
     */
    protected static String _getArgumentsInOut(GenericJNIActor actor,
            String separator) {
        return _getArguments(actor, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected static String _getArgumentsInOutWithCType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithCType(actor, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type,
     *  excluding the out arguments.
     *  @return the JNI type and name of each in arguments.
     */
    protected static String _getArgumentsInOutWithJNIType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithJNIType(actor, true, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments,
     *  excluding the in arguments.
     */
    protected static String _getArgumentsInOutWithJType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithJType(actor, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their c type.
     *  @return the c type of the out arguments.
     */
    protected static String _getArgumentsInWithCType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithCType(actor, true, false, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type,
     *  excluding the out arguments.
     *  @return the JNI type and name of each in arguments, excluding
     *  the in arguments.
     */

    protected static String _getArgumentsInWithJNIType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithJNIType(actor, true, false, false, separator);
    }

    /** Get the args In belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments,
     *  excluding the in arguments.
     */
    protected static String _getArgumentsInWithJType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithJType(actor, true, false, false, separator);
    }


    /** Get the args Out belonging to this entity.
     *  @return a vector of out arguments, excluding the in arguments.
     */
    protected static Vector _getArgumentsOut(GenericJNIActor actor) {
        return _getArguments(actor, false, true, false);
    }

    /** Get the args out name belonging to this entity.
     *  @return the name of the out arguments, excluding the in arguments.
     */
    protected static String _getArgumentsOut(GenericJNIActor actor,
            String separator) {
        return _getArguments(actor, false, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their c type.
     *  @return the c type of the out arguments.
     */
    protected static String _getArgumentsOutWithCType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithCType(actor, false, true, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type,
     *   excluding the out arguments.
     *  @return the JNI type and name of each in arguments, excluding
     *  the in arguments.
     */
    protected static String _getArgumentsOutWithJNIType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithJNIType(actor, false, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments,
     *  excluding the in arguments.
     */
    protected static String _getArgumentsOutWithJType(
            GenericJNIActor actor,
            String separator) {
        return _getArgumentsWithJType(actor, false, true, false, separator);
    }

    /** Get the args belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected static String _getArgumentsWithCType(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        StringBuffer returnValue = new StringBuffer();
        Iterator arguments =
            _getArguments(actor, isInput, isOutput, isReturn).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (argument != null) {
                if (returnValue.length() > 0) {
                    returnValue.append(separator);
                }
                returnValue.append(argument.getCType() + " "
                        + argument.getName());
            }
        }
        return returnValue.toString();
    }

    /** Get the arguments belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected static String _getArgumentsWithCTypeCast(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        StringBuffer returnValue = new StringBuffer();
        Iterator arguments =
            _getArguments(actor, isInput, isOutput, isReturn).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            String add = "";
            if (argument.getJType().endsWith("[]")) {
                add = "_1";
            }
            if (argument != null) {
                if (returnValue.length() > 0) {
                    returnValue.append(separator);
                }
                returnValue.append(" (" + argument.getC2Type() + ")"
                        + argument.getName() + add);
            }
        }
        return returnValue.toString();
    }

    /** Get the arguments In belonging to this entity with their JNI
     *  type, excluding the out arguments.
     *  @return the JNI type and name of each in arguments.
     */
    protected static String _getArgumentsWithJNIType(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        StringBuffer returnValue = new StringBuffer();
        Iterator arguments =
            _getArguments(actor, isInput, isOutput, isReturn).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (returnValue.length() > 0) {
                returnValue.append(separator);
            }
            returnValue.append(argument.getJNIType() + " "
                    + argument.getName());
        }
        return returnValue.toString();
    }

    /** Get the arguments belonging to this entity with their java type.
     *  @return the java type and name of each in arguments
     */
    protected static String _getArgumentsWithJType(
            GenericJNIActor actor,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        StringBuffer returnValue = new StringBuffer();
        Iterator arguments =
            _getArguments(actor, isInput, isOutput, isReturn).iterator();
        while (arguments.hasNext()) {
            Argument argument = (Argument) arguments.next();
            if (returnValue.length() > 0) {
                returnValue.append(separator);
            }
            returnValue.append(argument.getJType() + " "
                    + argument.getName());
        }
        return returnValue.toString();
    }

    /**
     *  @return the signature of the interface function.
     */
    protected static String _signature(String typ) {
        StringBuffer returnValue = new StringBuffer();
        if (typ.endsWith("[]")) {
            returnValue.append("[");
        }

        if (typ.equals("boolean") || typ.startsWith("boolean")) {
            returnValue.append("Z");
        } else if (typ.equals("byte") || typ.startsWith("byte")) {
            returnValue.append("B");
        } else if (typ.equals("char") || typ.startsWith("char")) {
            returnValue.append("C");
        } else if (typ.equals("short") || typ.startsWith("short")) {
            returnValue.append("S");
        } else if (typ.equals("int") || typ.startsWith("int")) {
            returnValue.append("I");
        } else if (typ.equals("long") || typ.startsWith("long")) {
            returnValue.append("J");
        } else if (typ.equals("float") || typ.startsWith("float")) {
            returnValue.append("F");
        } else if (typ.equals("double") || typ.startsWith("double")) {
            returnValue.append("D");
        } else if (typ.equals("Object") || typ.startsWith("Object")) {
            returnValue.append("L");
        }

        return returnValue.toString();
    }

    /**
     *  @return the signature of the interface function.
     */
    protected static String _signatureSendResults(GenericJNIActor actor) {
        String returnValue = "(";

        //out
        Iterator arguments = _getArgumentsOut(actor).iterator();
        while (arguments.hasNext()) {
            String typ = ((Argument) arguments.next()).getJType();
            returnValue = returnValue + _signature(typ);
        }
        //in out
        arguments = _getArgumentsInOut(actor).iterator();
        while (arguments.hasNext()) {
            String typ = ((Argument) arguments.next()).getJType();
            returnValue = returnValue + _signature(typ);
        }

        returnValue = returnValue + ")V";

        return returnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private method                    ////

    private static String _getInterNativeFunction(GenericJNIActor actor)
            throws IllegalActionException {
        //return "jni" + _getNativeFunction(actor);
        return "jni" + actor.getName();
    }

    private static String _getInterNativeLibrary(GenericJNIActor actor)
            throws IllegalActionException {
        //return "jni" + getNativeLibrary(actor);
        return "jni" + actor.getName();
    }

    // Return the value of the libraryDirectory argument with the double
    // quotes stripped off.
    private static String _getLibraryDirectory(GenericJNIActor actor)
            throws IllegalActionException {
        String libraryDirectory = (((StringToken) ((Parameter) actor
                .getAttribute("libraryDirectory"))
                .getToken())
                .toString());
        return libraryDirectory.substring(1, libraryDirectory.length() - 1);
    }

    // Return the value of the nativeFunction argument with the double
    // quotes stripped off.
    private static String _getNativeFunction(GenericJNIActor actor)
            throws IllegalActionException {
        String nativeFunction =
            (((StringToken) ((Parameter) actor
                    .getAttribute("nativeFunction"))
                    .getToken())
                    .toString());
        return nativeFunction.substring(1, nativeFunction.length() - 1);
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
        } else if (!string1.equals("") && !string2.equals("")) {
            return ",";
        }
        return "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // String to use to indent 1 level
    private static String _indent1 = StringUtilities.getIndentPrefix(1);
    // String to use to indent 2 level
    private static String _indent2 = StringUtilities.getIndentPrefix(2);
    // String to use to indent 3 level
    private static String _indent3 = StringUtilities.getIndentPrefix(3);
}

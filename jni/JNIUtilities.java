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

import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


//////////////////////////////////////////////////////////////////////////
//// JNIUtilities
/**
A collection of utilities for generating Java Native Interface classes

For information about how to create shared libraries under Windows using
Cygwin, see <a href="http://cygwin.com/cygwin-ug-net/dll.html"><code>http://cygwin.com/cygwin-ug-net/dll.html</code></a>

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
            MessageHandler.error(
                    "no lib or func name ! : ",
                    ex);
        }
        libName =
            libName.substring(1, libName.length() - 1);
        nativeFuncName =
            nativeFuncName.substring(1, nativeFuncName.length() - 1);

        try {
            actor.setName(libName + "I" + nativeFuncName);
        } catch (NameDuplicationException ex) {
            /*MessageHandler.error(
              "Unable to rename JNIActor "
              + actor.getName()
              + " : \n An JNI Actor already exists !\n",
              ex);*/
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
        Process header = r2.exec(cmd);

        //Cr‰ation du fichier C
        File cFile = _exportCInterfaceFile(actor);

        //compile Cfile
        header.waitFor();
        /*Process make = r3.exec(cmd);
        make.waitFor();*/
        _exportDSP(actor);
    }

    /** Create the interface Java File.
     */
    protected static File _exportJavaInterfaceFile(GenericJNIActor actor) {
        String str;
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
            MessageHandler.error("no lib or func name ! : ", ex);
        }

        interlibName = "jni" + libName.substring(
                1, libName.length() - 1);
        interFuncName =
            "jni" + nativeFuncName.substring(
                    1, nativeFuncName.length() - 1);

        List argList = actor.argumentsList();
        Iterator ite = argList.iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (!(arg == null) && arg.isReturn()) {
                returnJType = arg.getJType();
                returnName = arg.getName();
                break;
            }
        }

        if (returnJType.equals("void"))
            returnJType2 = "";
        else
            returnJType2 = returnJType;

        str =
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
            + virgule(
                    _getArgsInWithJType(actor, ","),
                    _getArgsInOutWithJType(actor, ","))
            + _getArgsInOutWithJType(actor, ",")
            + virgule(
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
            + virgule(
                    _getArgsOutWithJType(actor, ","),
                    _getArgsInOutWithJType(actor, ","))
            + _getArgsInOutWithJType(actor, ",")
            + ") {\n";

        ite = _getArgsOut(actor).iterator();
        while (ite.hasNext()) {
            String name = ((Argument) ite.next()).getName();
            str = str + "_" + name + " = " + name + ";\n";
        }

        ite = _getArgsInOut(actor).iterator();
        while (ite.hasNext()) {
            String name = ((Argument) ite.next()).getName();
            str = str + "_" + name + " = " + name + ";\n";
        }
        str =
            str
            + "\n}\n\n";
        str =
            str
            + "/* Call the native function inthe native library. This method is "
            + "called by the GenericJNIActor on fire()\n"
            + " * @author autogenerated - by V. Arnould.TRT\n"
            + " */\n"
            + " public "
            + returnJType
            + " fire("
            + _getArgsInWithJType(actor, ",")
            + virgule(_getArgsInWithJType(actor, ","),
                    _getArgsInOutWithJType(actor, ","))
            + _getArgsInOutWithJType(actor, ",")
            + virgule(
                    _getArgsInOutWithJType(actor, ","),
                    _getArgsOutWithJType(actor, ","))
            + _getArgsOutWithJType(actor, ",")
            + ") {\n\n";

        //if there is a returned value
        Argument argRet = (Argument) actor.getArgumentReturn();
        if(!(argRet.getJType().equals("void")))
            {
                str = str + "_" + argRet.getName() + " = "
                    + interFuncName
                    + "("
                    + _getArgsIn(actor, ",")
                    + virgule( _getArgsIn(actor, ","),
                            _getArgsInOut(actor, ","))
                    + _getArgsInOut(actor, ",")
                    + virgule( _getArgsInOut(actor, ","),
                            _getArgsOut(actor, ","))
                    + _getArgsOut(actor, ",")
                    + ");"
                    + "\n ";

                str = str + "return"
                    + " _" + argRet.getName() + ";\n"+ "}\n";
            }
        else
            {
                str = str + interFuncName
                    + "("
                    + _getArgsIn(actor, ",")
                    + virgule( _getArgsIn(actor, ","),
                            _getArgsInOut(actor, ","))
                    + _getArgsInOut(actor, ",")
                    + virgule( _getArgsInOut(actor, ","),
                            _getArgsOut(actor, ","))
                    + _getArgsOut(actor, ",")
                    + ");"
                    + "\n }\n";
            }

        str = str + "///////////// public fields\n" + "\n";

        //out
        ite = _getArgsOut(actor).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            str = str + "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n";
        }
        //inout
        ite = _getArgsInOut(actor).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            str = str + "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n";
        }

        //in
        ite = _getArgsIn(actor).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            str = str + "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n";
        }
        //return
        if(!(argRet.getJType().equals("void")))
            str = str + "public " + argRet.getJType() + " " + "_"
                + argRet.getName() + ";\n";

        str = str + "\n}";
        File dir = new File(System.getProperty("user.dir")
                + "/jni/" + interlibName);
        //Creation du repertoire
        try {
            dir.mkdirs();
        } catch (NullPointerException ex) {
            MessageHandler.error("No directory! : ", ex);
        }

        File javaFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/Jni"
                    + actor.getName()
                    + ".java");

        try {
            FileWriter fileStr = new FileWriter(javaFile);
            fileStr.write(str);
            fileStr.close();
        } catch (Exception ex) {
            MessageHandler.error("No file ! : ", ex);
        }
        return javaFile;
    }

    /** Create the interface C  File.
     *
     */
    protected static File _exportCInterfaceFile(GenericJNIActor actor) {
        String str;
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
            MessageHandler.error("no lib or func name ! : ", ex);
        }
        String interlibName =
            "jni" + libName.substring(1, libName.length() - 1);
        String interFuncName =
            "jni" + nativeFuncName.substring(1,
                    nativeFuncName.length() - 1);
        List argList = actor.argumentsList();
        Iterator ite = argList.iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            if (!(arg == null) && arg.isReturn()) {
                returnType = arg.getCType();
                returnName = arg.getName();
                break;
            }
        }

        String returnJNIType = "";
        ite = _getArgs(actor, false, false, true).iterator();
        if (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            returnJNIType = arg.getJNIType();
        }

        String returnJType2;
        if (returnType.equals("void"))
            returnJType2 = "";
        else
            returnJType2 = returnType + " ";
        str = "#include \"jni.h\" \n"
            // le fichier entete de la librairie existante
            +"#include <iostream>\n"
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
            + virgule( _getArgsInWithCType(actor, ","),
                    _getArgsInOut(actor, ","))
            + _getArgsInOutWithCType(actor, ",")
            + virgule(_getArgsInOut(actor, ","),
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
            + "(\nJNIEnv *env, jobject jobj, "
            + _getArgsInWithJNIType(actor, ",")
            + virgule(_getArgsInWithJNIType(actor, ","),
                    _getArgsInOutWithCType(actor, ","))
            + _getArgsInOutWithJNIType(actor, ",")
            + virgule(_getArgsInOutWithJNIType(actor, ","),
                    _getArgsOutWithCType(actor, ","))
            + _getArgsOutWithJNIType(actor, ",")
            + ")\n"
            + "{\n"
            + "                // Declaration des sorties\n";

        ite = _getArgsOut(actor).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            str = str + arg.getJNIType() + " "
                + arg.getName() + ";\n";
        }

        str =
            str
            + "\n"
            + "                // structure en sortie de la fonction C\n"
            + "//target_location_struct *target_location = "
            + "new target_location_struct;\n\n"
            + "                // appel de la librairie existante\n\n";

        //For the array inout and in
        ite = _getArgsInOut(actor).iterator();
        while(ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            String typ = arg.getJType();
            if(typ.endsWith("[]"))
                {
                    typ = typ.substring(0, typ.length()-2);
                    str = str + "j" + typ  + "Array *"
                        + arg.getName() + "_1 =" +
                        "(j" + typ + "Array*)env->Get"
                        + typ.substring(0, 1).toUpperCase()
                        + typ.substring(1, typ.length())  + "ArrayElements("
                        + arg.getName() + ",JNI_FALSE);\n";
                }
        }


        ite = _getArgsIn(actor).iterator();
        while(ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            String typ = arg.getJType();
            if(typ.endsWith("[]"))
                {
                    typ = typ.substring(0, typ.length()-2);
                    str = str + "j" + typ  + "Array *" + arg.getName() + "_1 =" +
                        "(j" + typ + "Array*)env->Get" + typ.substring(0, 1).toUpperCase()
                        + typ.substring(1, typ.length())  + "ArrayElements("
                        + arg.getName() + ",JNI_FALSE);\n"
                        + "//if((long *)in_1 == (long *)0 || (long *)inout_1 == (long *)0)n" +
                        "//{\n" + "//std::cout << \"warning : the matrix is empty !\""
                        + " << std::endl;\n" +
                        "//}\n";
                }
        }


        //if they is a return
        if (!returnJNIType.equals("void"))
            str =
                str
                + returnJNIType
                + " _"
                + returnName
                + " = ("
                + returnJNIType
                + ")";

        //native function call

        str =
            str
            + nativeFuncName.substring(1, nativeFuncName.length() - 1)
            + "("
            + _getArgsWithCTypeCast(actor, true, false, false, ",")
            + virgule( _getArgsInWithCType(actor, ","), _getArgsInOut(actor, ","))
            + _getArgsWithCTypeCast(actor, true, true, false, ",")
            + virgule( _getArgsInOutWithCType(actor, ","), _getArgsOut(actor, ","))
            + _getArgsWithCTypeCast(actor, false, true, false, ",")
            + ");\n\n";

        //Release memory in native side
        //for in
        ite = _getArgsIn(actor).iterator();
        while(ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            String typ = arg.getJType();
            if(typ.endsWith("[]"))
                {
                    typ = typ.substring(0, typ.length()-2);
                    str = str + "env->Release"+ typ.substring(0, 1).toUpperCase() +
                        typ.substring(1, typ.length())  + "ArrayElements(" +
                        arg.getName() + ",(" + arg.getC2Type() + ")" + arg.getName() + "_1,0);\n";
                }
        }
        //for inout
        ite = _getArgsInOut(actor).iterator();
        while(ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            String typ = arg.getJType();
            if(typ.endsWith("[]"))
                {
                    typ = typ.substring(0, typ.length()-2);
                    str = str + "env->Release"+ typ.substring(0, 1).toUpperCase() +
                        typ.substring(1, typ.length()) + "ArrayElements(" +
                        arg.getName() + ",(" + arg.getC2Type() + ")" + arg.getName() + "_1,0);\n";
                }
        }

        if (!returnJNIType.equals("void")) {
            str =
                str
                + "if(_"
                + returnName
                + " == 0) {\n"
                + "std::cout << \"warning : return = 0\" << std::endl;\n"
                + "\n}\n";
        }

        if (!(_getArgsOut(actor, "").equals("")
                    && _getArgsInOut(actor, "").equals(""))) {
            str =
                str
                + "                // envoi des sorties dans l'environnement JAVA / PTOLEMY II\n"
                + "jclass cls = env->GetObjectClass(jobj);\n"
                + "jmethodID mid = env->GetMethodID(cls,\"sendResults\",\""
                + _signatureSendResults(actor)
                + "\");\n"
                + "if (mid == 0) {\n"
                + "std::cout << \"Can't find sendResults method\";\n";

            if (!returnJNIType.equals("void"))
                str = str + "return _" + returnName + ";\n}\n";
            else
                str = str + "return;\n}\n";

            str = str + "env->CallVoidMethod(jobj,mid," + _getArgsOut(actor, ",")
                //warning : ici on suppose qu'il y a toujours au - 1 argument Out !! TBFix
                +virgule( _getArgsOut(actor, ","), _getArgsInOut(actor, ","))
                + _getArgsInOut(actor, ",")
                + ");\n"
                + "if (env->ExceptionOccurred()) {\n"
                + "std::cout << \"Can't get back results!!\" << std::endl;\n"
                + "env->ExceptionDescribe();\n"
                + "env->ExceptionClear();\n"
                + "}\n"
                + "env->ExceptionClear();\n\n";
        }

        str = str + "                // M‰nage\n";

        /*ite = _getArgsOut(actor).iterator();
          while (ite.hasNext()) {
          Argument arg = (Argument) ite.next();
          str = str + "delete " + " " + arg.getName() + ";\n";
          }
          ite = _getArgsInOut(actor).iterator();
          while (ite.hasNext()) {
          Argument arg = (Argument) ite.next();
          str = str + "delete " + " " + arg.getName() + ";\n";
          }*/

        if (!returnJNIType.equals("void"))
            str = str + "return _" + returnName + ";\n}\n";
        else
            str = str + "return;\n}\n";

        //exporting the file
        File cFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/jni"
                    + actor.getName()
                    + ".cpp");
        try {
            FileWriter fileStr = new FileWriter(cFile);
            fileStr.write(str);
            fileStr.close();
            return cFile;
        } catch (Exception ex) {
            MessageHandler.error("Not able to write the file ! : ", ex);
        }
        return cFile;
    }

    /** Export the Visual Studio project.
     */
    protected static void _exportDSP(GenericJNIActor actor) {
        String str;
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
            MessageHandler.error("No lib name ! : ", ex);
        }
        libName = libName.substring(1, libName.length() - 1);
        String interlibName = "jni" + libName;
        str =
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
            + "# End Project\r\n";
        File dspFile =
            new File(
                    System.getProperty("user.dir") + "/jni/"
                    + interlibName
                    + "/Jni"
                    + interlibName
                    + ".dsp");
        try {
            FileWriter fileStr = new FileWriter(dspFile);
            fileStr.write(str);
            fileStr.close();
        } catch (Exception ex) {
            MessageHandler.error("Not able to write the file ! : ", ex);
        }
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
        @param str the string to test
        @return "" if str is null, "," else
    */
    private static String virgule(String str) {
        if (str.equals("")) {
            return "";
        }
        return ",";
    }

    /** Test the given string to know if a comma is needed
        @param str the first string to test
        @param str2 the second string to test
        @return "," if str != null && str2 != null, else return ""
    */
    private static String virgule(String str, String str2) {
        if (str.equals("")) {
            return "";
        } else if (!str2.equals("")) {
            return ",";
        }
        return "";
    }

}

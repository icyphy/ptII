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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;

import diva.graph.GraphPane;

import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;

import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.DebugListenerTableau;

import ptolemy.gui.CancelException;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NamedList;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphTableau;
import ptolemy.vergil.actor.ActorEditorGraphController;

//////////////////////////////////////////////////////////////////////////
//// ThalesGraphFrame
/**
   This is a graph editor frame for ptolemy models that use the JNI interface.
   Given a composite entity and an instance of ThalesGraphTableau,
   it creates an editor and populates the menus and toolbar.
   This overrides the base class to associate with the editor the JNI interface.
   @see EditorGraphController
   @author  Steve Neuendorffer, Vincent Arnould
   @contributor Edward A. Lee
   @version $Id$
   @see BasicGraphFrame.java,v 1.40 2001/12/05 03:01:29 cxh Exp $
*/
public class ThalesGraphFrame extends BasicGraphFrame {

    /** Construct a frame associated with the specified Ptolemy II model.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  This is typically done by calling show() on the controlling tableau.
     *  @see Tableau#show()
     *  @param entity The model to put in this frame.
     *  @param tableau The tableau responsible for this frame.
     */
    public ThalesGraphFrame(
            CompositeEntity entity,
            ActorGraphTableau tableau) {
        super(entity, tableau);
        //super._addMenus();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the menus that are used by this frame.
     *  It is essential that _createGraphPane() be called before this.
     */
    protected void _addMenus() {
        super._addMenus();
        // Add any commands to graph menu and toolbar
        // that the controller
        // wants in the graph menu and toolbar.
        _graphMenu.addSeparator();
        _controller.addToMenuAndToolbar(_graphMenu, _toolbar);

        // Add debug menu.
        JMenuItem[] debugMenuItems =
            {
                new JMenuItem("Listen to Director",
                        KeyEvent.VK_L),
                new JMenuItem("Animate Execution",
                        KeyEvent.VK_A),
                new JMenuItem("Stop Animating",
                        KeyEvent.VK_S),
            };

        //TRT Add JNI Menu
        _graphMenu.addSeparator();

        JMenuItem[] jniMenuItems =
            { new JMenuItem("Generate C Interface",
                    KeyEvent.VK_G)
            };
        //TRT end

        // NOTE: This has to be initialized here rather than
        // statically because this method is called
        // by the constructor
        // of the base class, and static initializers have not yet
        // been run.
        _debugMenu = new JMenu("Debug");
        _debugMenu.setMnemonic(KeyEvent.VK_D);
        DebugMenuListener debugMenuListener =
            new DebugMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < debugMenuItems.length; i++) {
            debugMenuItems[i].setActionCommand(
                    debugMenuItems[i].getText());
            debugMenuItems[i].addActionListener(
                    debugMenuListener);
            _debugMenu.add(debugMenuItems[i]);
        }
        _menubar.add(_debugMenu);

        //TRT begin changes

        _jniMenu = new JMenu("JNI");
        _jniMenu.setMnemonic(KeyEvent.VK_J);
        JNIMenuListener jniMenuListener = new JNIMenuListener();
        // Set the action command and listener for each menu item.
        for (int i = 0; i < jniMenuItems.length; i++) {
            jniMenuItems[i].setActionCommand(
                    jniMenuItems[i].getText());
            jniMenuItems[i].addActionListener(
                    jniMenuListener);
            _jniMenu.add(jniMenuItems[i]);
        }
        _menubar.add(_jniMenu);
        //TRT end

    }

    /** Create a new graph pane. Note that this method is called in
     *  constructor of the base class, so it must be
     *  careful to not reference
     *  local variables that may not have yet been created.
     */
    protected GraphPane _createGraphPane() {

        _controller = new ActorEditorGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);
        final ActorGraphModel graphModel = new ActorGraphModel(
                getModel());
        return new GraphPane(_controller, graphModel);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc =
                getClass().getClassLoader().getResource(
                        "ptolemy/configs/doc/vergilGraphEditorHelp.htm");
            getConfiguration().openModel(
                    null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    //TRT
    /** Create the interface Java File.
     */
    protected File _exportJavaInterfaceFile(GenericJNIActor act) {
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
                (((StringToken) ((Parameter) act
                          .getAttribute("Native Function Name"))
                        .getToken())
                        .toString());
            libName =
                ((StringToken) ((Parameter) act
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

        List argList = act.argList();
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
            + act.getName()
            + " { \n\n"
            + "/* The default constructor\n"
            + " * @author autogenerated - by V. Arnould.TRT\n"
            + " */\n"
            + "public Jni"
            + act.getName()
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
            + _getArgsInWithJType(act, ",")
            + virgule(
                    _getArgsInWithJType(act, ","),
                    _getArgsInOutWithJType(act, ","))
            + _getArgsInOutWithJType(act, ",")
            + virgule(
                    _getArgsInWithJType(act, ",")+
                    _getArgsInOutWithJType(act, ","),
                    _getArgsOutWithJType(act, ","))
            + _getArgsOutWithJType(act, ",")
            + ") throws SecurityException;\n\n"
            + "/* Send the result of the native library."
            + " This method is called by"
            + " the native code\n"
            + " * @author autogenerated - by V. Arnould.TRT\n"
            + " */\n"
            + " public void "
            + "sendResults("
            + _getArgsOutWithJType(act, ",")
            + virgule(
                    _getArgsOutWithJType(act, ","),
                    _getArgsInOutWithJType(act, ","))
            + _getArgsInOutWithJType(act, ",")
            + ") {\n";

        ite = _getArgsOut(act).iterator();
        while (ite.hasNext()) {
            String name = ((Argument) ite.next()).getName();
            str = str + "_" + name + " = " + name + ";\n";
        }

        ite = _getArgsInOut(act).iterator();
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
            + _getArgsInWithJType(act, ",")
            + virgule(_getArgsInWithJType(act, ","),
                    _getArgsInOutWithJType(act, ","))
            + _getArgsInOutWithJType(act, ",")
            + virgule(
                    _getArgsInOutWithJType(act, ","),
                    _getArgsOutWithJType(act, ","))
            + _getArgsOutWithJType(act, ",")
            + ") {\n\n";

        //if there is a returned value
        Argument argRet = (Argument) act.getArgumentReturn();
        if(!(argRet.getJType().equals("void")))
            {
                str = str + "_" + argRet.getName() + " = "
                    + interFuncName
                    + "("
                    + _getArgsIn(act, ",")
                    + virgule( _getArgsIn(act, ","),
                            _getArgsInOut(act, ","))
                    + _getArgsInOut(act, ",")
                    + virgule( _getArgsInOut(act, ","),
                            _getArgsOut(act, ","))
                    + _getArgsOut(act, ",")
                    + ");"
                    + "\n ";

                str = str + "return"
                    + " _" + argRet.getName() + ";\n"+ "}\n";
            }
        else
            {
                str = str + interFuncName
                    + "("
                    + _getArgsIn(act, ",")
                    + virgule( _getArgsIn(act, ","),
                            _getArgsInOut(act, ","))
                    + _getArgsInOut(act, ",")
                    + virgule( _getArgsInOut(act, ","),
                            _getArgsOut(act, ","))
                    + _getArgsOut(act, ",")
                    + ");"
                    + "\n }\n";
            }

        str = str + "///////////// public fields\n" + "\n";

        //out
        ite = _getArgsOut(act).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            str = str + "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n";
        }
        //inout
        ite = _getArgsInOut(act).iterator();
        while (ite.hasNext()) {
            Argument arg = (Argument) ite.next();
            str = str + "public " + arg.getJType() + " "
                + "_" + arg.getName() + ";\n";
        }

        //in
        ite = _getArgsIn(act).iterator();
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
                    + act.getName()
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
    protected File _exportCInterfaceFile(GenericJNIActor act) {
        String str;
        String nativeFuncName = "";
        String returnType = "";
        String returnName = "";

        String dllDir = "";
        String libName = "";
        try {
            nativeFuncName =
                (((StringToken) ((Parameter) act
                          .getAttribute("Native Function Name"))
                        .getToken())
                        .toString());
            libName =
                ((StringToken) ((Parameter) act
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();
            dllDir =
                ((StringToken) ((Parameter) act
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
        List argList = act.argList();
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
        ite = _getArgs(act, false, false, true).iterator();
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
            + act.getName()
            + ".h\"\n"
            + "/*******************************************************\n"
            + "*********************"
            + act.getName()
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
            + _getArgsInWithCType(act, ",")
            + virgule( _getArgsInWithCType(act, ","),
                    _getArgsInOut(act, ","))
            + _getArgsInOutWithCType(act, ",")
            + virgule(_getArgsInOut(act, ","),
                    _getArgsOutWithCType(act, ","))
            + _getArgsOutWithCType(act, ",")
            + ");\n\n"
            + "JNIEXPORT "
            + act.getArgumentReturn().getJNIType()
            + " JNICALL Java_jni_"
            + interlibName
            + "_Jni"
            + act.getName()
            + "_"
            + interFuncName
            + "(\nJNIEnv *env, jobject jobj, "
            + _getArgsInWithJNIType(act, ",")
            + virgule(_getArgsInWithJNIType(act, ","),
                    _getArgsInOutWithCType(act, ","))
            + _getArgsInOutWithJNIType(act, ",")
            + virgule(_getArgsInOutWithJNIType(act, ","),
                    _getArgsOutWithCType(act, ","))
            + _getArgsOutWithJNIType(act, ",")
            + ")\n"
            + "{\n"
            + "                // Declaration des sorties\n";

        ite = _getArgsOut(act).iterator();
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
        ite = _getArgsInOut(act).iterator();
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


        ite = _getArgsIn(act).iterator();
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
            + _getArgsWithCTypeCast(act, true, false, false, ",")
            + virgule( _getArgsInWithCType(act, ","), _getArgsInOut(act, ","))
            + _getArgsWithCTypeCast(act, true, true, false, ",")
            + virgule( _getArgsInOutWithCType(act, ","), _getArgsOut(act, ","))
            + _getArgsWithCTypeCast(act, false, true, false, ",")
            + ");\n\n";

        //Release memory in native side
        //for in
        ite = _getArgsIn(act).iterator();
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
        ite = _getArgsInOut(act).iterator();
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

        if (!(_getArgsOut(act, "").equals("")
                    && _getArgsInOut(act, "").equals(""))) {
            str =
                str
                + "                // envoi des sorties dans l'environnement JAVA / PTOLEMY II\n"
                + "jclass cls = env->GetObjectClass(jobj);\n"
                + "jmethodID mid = env->GetMethodID(cls,\"sendResults\",\""
                + _signatureSendResults(act)
                + "\");\n"
                + "if (mid == 0) {\n"
                + "std::cout << \"Can't find sendResults method\";\n";

            if (!returnJNIType.equals("void"))
                str = str + "return _" + returnName + ";\n}\n";
            else
                str = str + "return;\n}\n";

            str = str + "env->CallVoidMethod(jobj,mid," + _getArgsOut(act, ",")
                //warning : ici on suppose qu'il y a toujours au - 1 argument Out !! TBFix
                +virgule( _getArgsOut(act, ","), _getArgsInOut(act, ","))
                + _getArgsInOut(act, ",")
                + ");\n"
                + "if (env->ExceptionOccurred()) {\n"
                + "std::cout << \"Can't get back results!!\" << std::endl;\n"
                + "env->ExceptionDescribe();\n"
                + "env->ExceptionClear();\n"
                + "}\n"
                + "env->ExceptionClear();\n\n";
        }

        str = str + "                // M‰nage\n";

        /*ite = _getArgsOut(act).iterator();
          while (ite.hasNext()) {
          Argument arg = (Argument) ite.next();
          str = str + "delete " + " " + arg.getName() + ";\n";
          }
          ite = _getArgsInOut(act).iterator();
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
                    + act.getName()
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
    protected void _exportDSP(GenericJNIActor act) {
        String str;
        String libName = "";
        String dllDir = "";
        try {
            libName =
                ((StringToken) ((Parameter) act
                        .getAttribute("Native Library Name"))
                        .getToken())
                .toString();
            dllDir =
                ((StringToken) ((Parameter) act.getAttribute("DLLs Directory"))
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
            + act.getName()
            + ".cpp\r\n"
            + "# End Source File\r\n"
            + "# End Group\r\n"
            + "# Begin Group \"Header Files\"\r\n\r\n"
            + "# PROP Default_Filter \"h;hpp;hxx;hm;inl\"\r\n"
            + "# Begin Source File\r\n\r\n"
            + "SOURCE=.\\jni_jni"
            + libName
            + "_Jni"
            + act.getName()
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
    protected Vector _getArgs(
            GenericJNIActor act,
            boolean isInput,
            boolean isOutput,
            boolean isReturn) {
        Vector ret = new Vector();
        List inArgs = act.argList();
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
    protected String _getArgs(
            GenericJNIActor act,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(act, isInput, isOutput, isReturn).iterator();
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
    protected String _getArgsWithCType(
            GenericJNIActor act,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(act, isInput, isOutput, isReturn).iterator();
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
    protected String _getArgsWithCTypeCast(
            GenericJNIActor act,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(act, isInput, isOutput, isReturn).iterator();
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
    protected String _getArgsWithJType(
            GenericJNIActor act,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(act, isInput, isOutput, isReturn).iterator();
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
    protected String _getArgsWithJNIType(
            GenericJNIActor act,
            boolean isInput,
            boolean isOutput,
            boolean isReturn,
            String separator) {
        String ret = "";
        Iterator ite = _getArgs(act, isInput, isOutput, isReturn).iterator();
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
    protected Vector _getArgsInOut(GenericJNIActor act) {
        return _getArgs(act, true, true, false);
    }

    /** Get the args InOut belonging to this entity.
     *  @return a vector of inout arguments.
     */
    protected String _getArgsInOut(GenericJNIActor act, String separator) {
        return _getArgs(act, true, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments, excluding the in arguments.
     */
    protected String _getArgsInOutWithJType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithJType(act, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their c type.
     *  @return the c type and name of each in arguments.
     */
    protected String _getArgsInOutWithCType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithCType(act, true, true, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type, excluding the out arguments.
     *  @return the JNI type and name of each in arguments.
     */
    protected String _getArgsInOutWithJNIType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithJNIType(act, true, true, false, separator);
    }

    /** Get the args Out belonging to this entity.
     *  @return a vector of out arguments, excluding the in arguments.
     */
    protected Vector _getArgsOut(GenericJNIActor act) {
        return _getArgs(act, false, true, false);
    }

    /** Get the args out name belonging to this entity.
     *  @return the name of the out arguments, excluding the in arguments.
     */
    protected String _getArgsOut(GenericJNIActor act, String separator) {
        return _getArgs(act, false, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments, excluding the in arguments.
     */
    protected String _getArgsOutWithJType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithJType(act, false, true, false, separator);
    }

    /** Get the args Out belonging to this entity with their c type.
     *  @return the c type of the out arguments.
     */
    protected String _getArgsOutWithCType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithCType(act, false, true, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type, excluding the out arguments.
     *  @return the JNI type and name of each in arguments, excluding the in arguments.
     */
    protected String _getArgsOutWithJNIType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithJNIType(act, false, true, false, separator);
    }

    /** Get the args In belonging to this entity.
     *  @return a vector of out arguments, excluding the in arguments.
     */
    protected Vector _getArgsIn(GenericJNIActor act) {
        return _getArgs(act, true, false, false);
    }

    /** Get the args In name belonging to this entity.
     *  @return the name of the out arguments, excluding the in arguments.
     */
    protected String _getArgsIn(GenericJNIActor act, String separator) {
        return _getArgs(act, true, false, false, separator);
    }

    /** Get the args Out belonging to this entity with their java type.
     *  @return the name and the java type of the out arguments, excluding the in arguments.
     */
    protected String _getArgsInWithJType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithJType(act, true, false, false, separator);
    }

    /** Get the args Out belonging to this entity with their c type.
     *  @return the c type of the out arguments.
     */
    protected String _getArgsInWithCType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithCType(act, true, false, false, separator);
    }

    /** Get the args In belonging to this entity with their JNI type, excluding the out arguments.
     *  @return the JNI type and name of each in arguments, excluding the in arguments.
     */
    protected String _getArgsInWithJNIType(
            GenericJNIActor act,
            String separator) {
        return _getArgsWithJNIType(act, true, false, false, separator);
    }

    /** Get the return argument belonging to this entity with its JNI type.
     *  @return the JNI type of the return argument.
     */
    protected String _getArgReturnWithJNIType(GenericJNIActor act) {
        return _getArgsWithJNIType(act, false, false, true, "");
    }

    /**
     *  @return the signature of the interface fonction.
     */
    protected String _signatureSendResults(GenericJNIActor act) {
        String ret = "(";
        //out
        Iterator ite = this._getArgsOut(act).iterator();
        while (ite.hasNext()) {
            String typ = ((Argument) ite.next()).getJType();
            ret = ret + _signature(typ);
        }
        //in out
        ite = this._getArgsInOut(act).iterator();
        while (ite.hasNext()) {
            String typ = ((Argument) ite.next()).getJType();
            ret = ret + _signature(typ);
        }

        ret = ret + ")V";

        return ret;
    }

    /**
     *  @return the signature of the interface fonction.
     */
    protected String _signature(String typ) {
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

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /** Listener for jni menu commands.
     */
    public class JNIMenuListener implements ActionListener {

        /** React to a menu command.
         */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Generate C Interface")) {
                    NamedObj model = getModel();
                    boolean success = false;
                    if (model instanceof Actor) {
                        List actorsList =
                            ((CompositeEntity) getModel())
                            .allAtomicEntityList();
                        Iterator ite = actorsList.iterator();

                        while (ite.hasNext()) {
                            Object act = ite.next();

                            if (act instanceof GenericJNIActor) {
                                //V‰rification qu'il y a un return, place le return en void sinon
                                GenericJNIActor arg = (GenericJNIActor) act;
                                Argument ret = arg.getArgumentReturn();
                                if (ret == null) {
                                    MessageHandler.warning(
                                            "No return was configured,\n so we set it as void");
                                    arg.addArgumentReturn();
                                }
                                //Cr‰ation des ports
                                Iterator reIte =
                                    ((CompositeEntity) getModel()).relationList().iterator();
                                while(reIte.hasNext())
                                    {
                                        ((ptolemy.kernel.ComponentRelation)(reIte.next())).unlinkAll();
                                    }
                                arg.removeAllPorts();
                                arg.createPorts();

                                //Nommage de l'acteur en fonction de ses parameters
                                String nativeFuncName = "";
                                String libName = "";
                                try {
                                    nativeFuncName =
                                        (((StringToken) ((Parameter) arg
                                                  .getAttribute("Native Function Name"))
                                                .getToken())
                                                .toString());
                                    libName =
                                        ((StringToken) ((Parameter) arg
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
                                    nativeFuncName.substring(
                                            1,
                                            nativeFuncName.length() - 1);

                                try {
                                    arg.setName(libName + "I" + nativeFuncName);
                                } catch (NameDuplicationException ex) {
                                    /*MessageHandler.error(
                                      "Unable to rename JNIActor "
                                      + arg.getName()
                                      + " : \n An JNI Actor already exists !\n",
                                      ex);*/
                                }

                                //render the graph within its controller
                                _controller.rerender();
                                //arg.notifyAll();

                                //Cr‰ation du fichier Java
                                File javaFile = _exportJavaInterfaceFile(arg);

                                //Compilation java de la classe interface:
                                String cmd[] = new String[4];
                                cmd[0] = "javac";
                                cmd[1] = "-classpath";
                                cmd[2] = "%PTII%";
                                cmd[3] =
                                    "jni/jni"
                                    + libName
                                    + "/Jni"
                                    + arg.getName()
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
                                    + arg.getName();
                                Runtime r2 = Runtime.getRuntime();

                                //waiting for the compilation to be done
                                compil.waitFor();
                                Process header = r2.exec(cmd);

                                //Cr‰ation du fichier C
                                File cFile = _exportCInterfaceFile(arg);

                                //compile Cfile
                                header.waitFor();
                                /*Process make = r3.exec(cmd);
                                  make.waitFor();*/
                                _exportDSP(arg);


                            }
                        }
                        success = true;
                    }

                    if (!success) {
                        MessageHandler.error("No JNIActor to interface to!");
                    }
                }
            } catch (Exception ex) {

                MessageHandler.error("Failed to create C interface : " + ex);

            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Jni menu for this frame.
     */
    protected JMenu _jniMenu;
    //TRT end

    /** Debug menu for this frame.
     */
    protected JMenu _debugMenu;

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    // NOTE: The following class is very similar to the inner class
    // in FSMGraphFrame.  Is there some way to merge these?
    // There seem to be enough differences that this may be hard.

    /** Listener for debug menu commands.
     */
    public class DebugMenuListener implements ActionListener {

        /** React to a menu command.
         */
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();
            try {
                if (actionCommand.equals("Listen to Director"))
                    {
                        NamedObj model = getModel();
                        boolean success = false;
                        if (model instanceof Actor) {
                            Director director = ((Actor) model).getDirector();
                            if (director != null) {
                                Effigy effigy =
                                    (Effigy) getTableau().getContainer();
                                // Create a new text effigy inside this one.
                                Effigy textEffigy =
                                    new TextEffigy(
                                            effigy,
                                            effigy.uniqueName("debug listener"));
                                DebugListenerTableau tableau =
                                    new DebugListenerTableau(
                                            textEffigy,
                                            textEffigy.uniqueName("debugListener"));
                                tableau.setDebuggable(director);
                                success = true;
                            }
                        }
                        if (!success) {
                            MessageHandler.error("No director to listen to!");
                        }
                    } else if (actionCommand.equals("Animate Execution")) {
                        // To support animation, add a listener to the
                        // first director found above in the hierarchy.
                        // NOTE: This doesn't properly support all
                        // hierarchy.  Insides of transparent composite
                        // actors do not get animated if they are classes
                        // rather than instances.
                        NamedObj model = getModel();
                        if (model instanceof Actor) {
                            // Dialog to ask for a delay time.
                            Query query = new Query();
                            query.addLine(
                                    "delay",
                                    "Time (in ms) to hold highlight",
                                    Long.toString(_lastDelayTime));
                            ComponentDialog dialog = new ComponentDialog(
                                    //TRT
                                    ThalesGraphFrame.this,
                                    //TRT end
                                    "Delay for Animation", query);
                            if (dialog.buttonPressed().equals("OK")) {
                                try {
                                    _lastDelayTime =
                                        Long.parseLong(
                                                query.getStringValue("delay"));
                                    _controller.setAnimationDelay(_lastDelayTime);
                                    Director director =
                                        ((Actor) model).getDirector();
                                    while (director == null
                                            && model instanceof Actor) {
                                        model = (NamedObj) model.getContainer();
                                        if (model instanceof Actor) {
                                            director =
                                                ((Actor) model).getDirector();
                                        }
                                    }
                                    if (director != null
                                            && _listeningTo != director) {
                                        if (_listeningTo != null) {
                                            _listeningTo.removeDebugListener(
                                                    _controller);
                                        }
                                        director.addDebugListener(_controller);
                                        _listeningTo = director;
                                    }
                                } catch (NumberFormatException ex) {
                                    MessageHandler.error(
                                            "Invalid time, which is required "
                                            + "to be an integer",
                                            ex);
                                }
                            } else {
                                MessageHandler.error(
                                        "Cannot find the director. Possibly this "
                                        + "is because this is a class, not an "
                                        + "instance.");
                            }
                        } else {
                            MessageHandler.error(
                                    "Model is not an actor. Cannot animate.");
                        }
                    } else if (actionCommand.equals("Stop Animating")) {
                        if (_listeningTo != null) {
                            _listeningTo.removeDebugListener(_controller);
                            _controller.clearAnimation();
                            _listeningTo = null;
                        }
                    }
            } catch (KernelException ex) {
                try {
                    MessageHandler.warning(
                            "Failed to create debug listener: " + ex);
                } catch (CancelException exception) {
                }
            }
        }
        private Director _listeningTo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //TRT
    /** The controller
     */
    private ActorEditorGraphController _controller;
    //TRT end

    /** The delay time specified that last time animation was set.
     */
    private long _lastDelayTime = 0;

    /** Test the given string to know if a comma is needed
        @param str the string to test
        @return "" if str is null, "," else
    */
    private String virgule(String str) {
        if (str.equals(""))
            return "";
        else
            return ",";
    }

    /** Test the given string to know if a comma is needed
        @param str the first string to test
        @param str2 the second string to test
        @return "," if str != null && str2 != null, else return ""
    */
    private String virgule(String str, String str2) {
        if (str.equals(""))
            return "";
        else if (!str2.equals(""))
            return ",";
        return "";
    }
}

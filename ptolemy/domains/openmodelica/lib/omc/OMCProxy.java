/* OMCProxy is the glue between OpenModelica Compiler(OMC) and Modelica Development Tooling (MDT).
 *
 * This file is part of Modelica Development Tooling.
 * The Modelica Development Tooling (MDT) software is
 * distributed under the conditions specified below.
 *
 * Copyright (c) 2012-2013,
 * Programming Environment Laboratory (PELAB),
 * Department of Computer and getInformation Science (IDA),
 * Linkoping University (LiU).
 *
 * All rights reserved.
 *
 * (The new BSD license, see also
 *  http://www.opensource.org/licenses/bsd-license.php)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * * Neither the name of Authors nor the name of Linkopings University nor
 *   the names of its contributors may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 */

package ptolemy.domains.openmodelica.lib.omc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.omg.CORBA.ORB;

import ptolemy.domains.openmodelica.kernel.OpenModelicaDirector;
import ptolemy.domains.openmodelica.lib.compiler.CompilerResult;
import ptolemy.domains.openmodelica.lib.compiler.ConnectException;
import ptolemy.domains.openmodelica.lib.compiler.IModelicaCompiler;
import ptolemy.domains.openmodelica.lib.omc.corba.OmcCommunication;
import ptolemy.domains.openmodelica.lib.omc.corba.OmcCommunicationHelper;

/**    
  The OMCProxy is the glue between the OpenModelica Compiler(OMC) and Modelica Development Tooling (MDT).
  It uses the interactive API of OMC to get Information of classes and load them into OMC.
  <p>
  @author Mana Mirzaei, Based on OMCProxy by Adrian Pop, Elmir Jagudin, Andreas Remar
  @version $Id$
  @since Ptolemy II 9.1
  @Pt.ProposedRating Red (cxh)
  @Pt.AcceptedRating Red (cxh)
 */
public class OMCProxy implements IModelicaCompiler {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Environmental variables which should be used for running the result of buildModel.*/
    public static String[] environmentalVariables = null;

    /** Check if we've setup the communication with OpenModelica Compiler(OMC). */
    public boolean hasInitialized = false;

    /** The CORBA object for OpenModelica Compiler(OMC) communication.*/
    public OmcCommunication omcc;

    /** Different types of Operating System(OS).*/
    public static enum osType {
        UNIX, WINDOWS, MAC
    }

    /**The copy of workingDirectory for having public access.*/
    public static File workDir = null;

    /** 
     *  OpenModelica Compiler(OMC) thread runs the OMC binary file by taking environmental variables
     *  OPENMODELICAHOME,OPENMODELICALIBRARY and working directory.
     */
    class OMCThread extends Thread {
        /** Construct an OpenModelica compiler thread
         *  with a new Thread object <i>OpenModelica Interactive Compiler Thread</i>.
         */
        public OMCThread() {
            super("OpenModelica Interactive Compiler Thread");
        }

        public void run() {
            File tmp[] = null;
            try {
                tmp = _getOmcBinaryPaths();
            } catch (ConnectException e) {
                e = new ConnectException("Unable to get the omc binary path!");
                e.printStackTrace();
                _couldNotStartOMC = true;
                hasInitialized = false;
                return;
            }

            File omcBinary = tmp[0];
            final File workingDirectory = tmp[1];
            Process proc = null;
            String command[] = { omcBinary.getAbsolutePath(),
                    "+c=" + _corbaSession, "+d=interactiveCorba" };

            ArrayList<String> both = new ArrayList<String>(command.length);
            Collections.addAll(both, command);

            String cmd[] = new String[both.size()];
            int nonNull = 0;
            for (int i = 0; i < both.size(); i++) {
                String str = both.get(i);
                if (str != null) {
                    cmd[nonNull] = str;
                    nonNull++;
                }
            }

            StringBuffer bufferCMD = new StringBuffer();
            for (int i = 0; i < nonNull; i++) {
                bufferCMD.append(cmd[i] + " ");
            }
            String fullCMD = bufferCMD.toString();
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Running command: " + fullCMD);
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Setting working directory to: "
                            + workingDirectory.getAbsolutePath());

            try {
                if (System.getenv("OPENMODELICAHOME") == null) {
                    Map<String, String> environmentalVariablesMap = System
                            .getenv();
                    Set<Entry<String, String>> entrySet = environmentalVariablesMap
                            .entrySet();
                    Collection<String> lst = new ArrayList<String>();
                    String x = "OPENMODELICAHOME="
                            + omcBinary.getParentFile().getParentFile()
                                    .getAbsolutePath();
                    lst.add(x);

                    if (System.getenv("OPENMODELICALIBRARY") == null) {
                        String y = "OPENMODELICALIBRARY="
                                + omcBinary.getParentFile().getParentFile()
                                        .getAbsolutePath() + "/lib/omlibrary";
                        lst.add(y);
                    }

                    Iterator<Entry<String, String>> i = entrySet.iterator();
                    while (i.hasNext()) {
                        Entry<String, String> z = i.next();
                        lst.add(z.getKey() + "=" + z.getValue());
                    }
                    environmentalVariables = lst
                            .toArray(new String[lst.size()]);
                }
                //FIXME
                proc = Runtime.getRuntime().exec(cmd, environmentalVariables,
                        workingDirectory);

                //Copy the content of workingDirectory for having public access.
                workDir = workingDirectory;
            } catch (IOException e) {
                OpenModelicaDirector.getOMCLogger().getInfo(
                        "Failed to run command: " + fullCMD);
                e = new IOException("Failed to run command: " + fullCMD);
                e.printStackTrace();

                _couldNotStartOMC = true;
                hasInitialized = false;
                return;
            }
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "Command run successfully.");
            OpenModelicaDirector
                    .getOMCLogger()
                    .getInfo(
                            "Waiting for OMC CORBA object reference to appear on disk.");

            OpenModelicaDirector.getOMCLogger().getInfo(
                    "OMC object reference found.");

            try {
                proc.waitFor();
            } catch (InterruptedException e) {
                OpenModelicaDirector
                        .getOMCLogger()
                        .getSever(
                                "OpenModelica compiler interrupted:"
                                        + e.getMessage()
                                        + (proc == null ? " process was null, Perhaps it was not initialized."
                                                : " process exited with code "
                                                        + proc.exitValue()));

                e = new InterruptedException(
                        "OpenModelica compiler interrupted:"
                                + e.getMessage()
                                + (proc == null ? " process was null, Perhaps it was not initialized."
                                        : " process exited with code "
                                                + proc.exitValue()));
                e.printStackTrace();

                hasInitialized = false;
                return;
            }

            if (proc != null) {
                if (OpenModelicaDirector.getOMCLogger() != null) {
                    OpenModelicaDirector.getOMCLogger().getInfo(
                            "OpenModelica compiler exited with code: "
                                    + proc.exitValue());
                } else {
                    new Exception(
                            "OpenModelicaDirector.getOMCLogger was null! OpenModelica subprocess exited with code "
                                    + proc.exitValue()).printStackTrace();
                }
            }

            hasInitialized = false;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                   ////

    /**  Build the Modelica model.
     *  @param className Main class of the model
     *  @return CompilerResult The result of sendExpression("command") method.
     *  @exception ConnectException If buildModel command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult buildModel(String className) throws ConnectException {
        CompilerResult result = sendCommand("buildModel(" + className + ")");
        return result;
    }

    /** Fetch the type of Operating System(OS).
     * @return osType The name of the operating system(OS). 
     */
    public static osType getOs() {
        String osName = System.getProperty("os.name");
        if (osName.contains("Linux")) {
            return osType.UNIX;
        } else if (osName.contains("Windows")) {
            return osType.WINDOWS;
        } else if (osName.contains("Mac")) {
            return osType.MAC;
        } else
            return osType.UNIX;
    }

    /**Initialize the communication with the OpenModelica compiler(OMC).
     * @exception ConnectException If we're unable to start communicating with
     * the server.
     */
    public synchronized void init() throws ConnectException {

        _os = getOs();

        // Set time as _corbaSession.
        String strDate = "";
        Date date = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        strDate = timeFormat.format(date);
        _corbaSession = strDate;

        // Check if an OMC server is already started. 
        File f = new File(_getPathToObject());
        String stringifiedObjectReference = null;
        if (!f.exists()) {

            // If a server is not already started, start it.
            OpenModelicaDirector.getOMCLogger().getInfo(
                    "No OMC object reference found, starting server.");
            _startServer();
        } else {
            OpenModelicaDirector
                    .getOMCLogger()
                    .getInfo(
                            "Old OMC CORBA object reference present, assuming OMC is running.");
        }
        stringifiedObjectReference = _readObjectFromFile();
        _setupOmcc(stringifiedObjectReference);
        hasInitialized = true;
    }

    /** Check if there is an error in the return value of sendCommand("command") method and
     *  fetch the error-information of current run.
     *  @param retval The string returned by the OpenModelica Compiler(OMC).
     *  @return Checks If the string is actually an error.
     */
    public boolean isError(String retval) {
        if (retval == null) {
            return false;
        }

        // See if there are parse errors.
        // An empty list {} also denotes error.
        return retval.toLowerCase().contains("error");
    }

    /** Load Modelica models from the file.
     *  @param fname The file name.
     *  @return CompilerResult The result of sendExpression("command") method.
     *  @exception ConnectException If loadFileInteractiveQualified command couldn't
     *  be sent to the OMC.
     */
    public CompilerResult loadFile(String fname) throws ConnectException {
        CompilerResult result = sendCommand("loadFileInteractiveQualified(\""
                + fname + "\")");
        return result;
    }

    /** Send a command to the OpenModelica Compiler(OMC) and fetches the string result.
     *  @param command The command which should be sent to the OMC.
     *  @return CompilerResult The result of sendExpression("command").
     *  @exception ConnectException If commands couldn't be sent to the OMC.
     */
    public CompilerResult sendCommand(String command) throws ConnectException {

        String error = null;
        String[] retval = { "" };

        if (_couldNotStartOMC) {
            return CompilerResult.makeResult(retval, error);
        }

        if (_numberOfErrors > _showMaxErrors) {
            return CompilerResult.makeResult(retval, error);
        }

        // Trim the start and end spaces.
        command = command.trim();

        if (hasInitialized == false) {
            init();
        }

        try {

            // Fetch the error string from OpenModelica Compiler(OMC). 
            // This should be called after an "Error"
            // is received or whenever the queue of errors are emptied.

            retval[0] = omcc.sendExpression(command);

            if (!command.equalsIgnoreCase("quit()")) {
                error = omcc.sendExpression("getErrorString()");
            }

            // Make sure the error string is not empty.
            if (error != null && error.length() > 2) {
                error = error.trim();
                error = error.substring(1, error.length() - 1);
            } else {
                error = null;
            }

            return CompilerResult.makeResult(retval, error);

        } catch (org.omg.CORBA.COMM_FAILURE x) {
            _numberOfErrors++;

            // Lose connection to OMC(OpenModelica Compiler).
            throw new ConnectException(
                    "Couldn't send command to the OpenModelica Compiler. Tried sending: "
                            + command);
            //FIXME how to add this to stacktrace
        }
    }

    /** Leave and quit OpenModelica environment.
     *  @exception ConnectException If quit command couldn't
     *  be sent to OMC.
     */
    public void quit() throws ConnectException {

        if (hasInitialized = true) {
            sendCommand("quit()");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /** Find the OpenModelica Compiler(OMC) executable file by using path variables.
     @parameter executableName The name of the executable file
     @return File The omc executable file
     */
    private File _findExecutableOnPath(String executableName) {
        String systemPath = System.getenv("PATH");

        // Try path with small letters.
        if (systemPath == null) {
            systemPath = System.getenv("path");
        }
        String[] pathDirs = systemPath.split(File.pathSeparator);

        File fullyQualifiedExecutable = null;
        for (String pathDir : pathDirs) {
            File file = new File(pathDir, executableName);
            if (file.isFile()) {
                fullyQualifiedExecutable = file;
                break;
            }
        }
        return fullyQualifiedExecutable;
    }

    /** Determine the path to the
     omc binary that user (probably) wants to use and the working
     directory of where that binary (most likely) should be started in.
     This will returns for example 'c:\openmodelica132\omc.exe'
     or '/usr/local/share/openmodelica/omc' depending on
     such factors as: OS type, environmental Variables settings,
     where the first matching binary found.
     @return full path to the omc binary and the working folder.
     @exception ConnectException If OPENMODELICAHOME is not set 
     and we could not find binary file in the path.
     */
    private File[] _getOmcBinaryPaths() throws ConnectException {
        String binaryName = "omc";

        if (_os == osType.WINDOWS) {
            binaryName += ".exe";
        }

        File omcBinary = null;
        File omcWorkingDirectory = null;
        File openModelicaHomeDirectory = null;

        // User specified that standard path to OMC(OpenModelica Compiler) should be used.
        // Try to determine OMC(OpenModelica Compiler) path via the OPENMODELICAHOME and
        // by checking in it's various subdirectory for OMC(OpenModelica Compiler) binary file. 
        OpenModelicaDirector
                .getOMCLogger()
                .getInfo(
                        "Using OPENMODELICAHOME environmental variable to find omc-binary");

        // Standard path to OMC(OpenModelica Compiler) binary is encoded in OPENMODELICAHOME
        // variable. 
        String openModelicaHome = System.getenv("OPENMODELICAHOME");
        if (openModelicaHome == null) {
            OpenModelicaDirector
                    .getOMCLogger()
                    .getInfo(
                            "OPENMODELICAHOME environmental variable is NULL, trying the PATH variable");
            File omc = _findExecutableOnPath(binaryName);
            if (omc != null) {
                OpenModelicaDirector.getOMCLogger().getInfo(
                        "Found omc executable in the path here: "
                                + omc.getAbsolutePath());

                openModelicaHome = omc.getParentFile().getParentFile()
                        .getAbsolutePath();
            } else {
                final String m = "Environmental variable OPENMODELICAHOME is not set and we could not find: "
                        + binaryName + " in the PATH";
                OpenModelicaDirector.getOMCLogger().getInfo(m);
                throw new ConnectException(m);
            }
        }

        openModelicaHomeDirectory = new File(openModelicaHome);

        // The subdirectories where OMC(OpenModelica Compiler) binary is located. 
        // adrpo 2012-06-12 It does not support the old ways! "/omc" and "Compiler/omc".
        String[] subdirs = { "bin" };

        for (String subdir : subdirs) {
            String path = openModelicaHomeDirectory.getAbsolutePath()
                    + File.separator;
            path += subdir.equals("") ? binaryName : subdir + File.separator
                    + binaryName;

            File file = new File(path);

            if (file.exists()) {
                omcBinary = file;
                OpenModelicaDirector.getOMCLogger().getInfo(
                        "Using omc-binary at '" + omcBinary.getAbsolutePath()
                                + "'");
                break;
            } else {

                OpenModelicaDirector.getOMCLogger().getInfo(
                        "No omc binary at: [" + path + "]");

            }
        }

        if (omcBinary == null) {
            OpenModelicaDirector
                    .getOMCLogger()
                    .getInfo(
                            "Could not find omc-binary on the OPENMODELICAHOME path or in the PATH variable");
            throw new ConnectException(
                    "Unable to start the OpenModelica Compiler, binary not found");
        }
        //FIXME
        /* if (System.getenv("USER") != null)
            omcWorkingDirectory = new File(System.getProperty("java.io.tmpdir")
                    + System.getenv("USER") + "\\" + "OpenModelica" + "\\");
        else
            omcWorkingDirectory = new File(System.getProperty("java.io.tmpdir")
                    + "nobody" + "\\" + "OpenModelica" + "\\");*/
        omcWorkingDirectory = new File(System.getProperty("java.io.tmpdir"));

        String workingDirectory = "Using working directory '"
                + omcWorkingDirectory.getAbsolutePath() + "'";

        OpenModelicaDirector.getOMCLogger().getInfo(workingDirectory);

        // Print out the working directory because it is not obvious on the Mac.
        System.out.println("OMCProxy: " + omcWorkingDirectory);

        return new File[] { omcBinary, omcWorkingDirectory };
    }

    /** Return the path to the OMC CORBA object that is stored on a disk.*/
    private String _getPathToObject() {

        String fileName = null;
        String username = System.getenv("USER");
        String temp = System.getProperty("java.io.tmpdir");
        //FIXME
        /*
        switch (_os) {
        case UNIX:
            if (username == null)
                username = "nobody";

            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                fileName = temp + "/" + username + "/" + "OpenModelica"
                        + "/openmodelica." + username + ".objid";
            } else {
                fileName = temp + "/" + username + "/" + "OpenModelica"
                        + "/openmodelica." + username + ".objid" + "."
                        + _corbaSession;
            }
            break;
        case WINDOWS:
            if (username == null)
                username = "nobody";

            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                fileName = temp + username + "\\" + "OpenModelica" + "\\"
                        + "openmodelica.objid";
            } else {
                fileName = temp + username + "\\" + "OpenModelica" + "\\"
                        + "openmodelica.objid" + "." + _corbaSession;
            }
            break;
        case MAC:
            if (username == null)
                username = "nobody";

            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                fileName = temp + "/" + username + "/" + "OpenModelica" + "/"
                        + "openmodelica." + username + ".objid";
            } else {
                fileName = temp + "/" + username + "/" + "OpenModelica" + "/"
                        + "openmodelica." + username + ".objid" + "."
                        + _corbaSession;
            }
            break;
        }*/

        switch (OMCProxy.getOs()) {

        // Add _corbaSession to the end of OMC CORBA object reference name to make it unique.
        case UNIX:
            if (username == null) {
                username = "nobody";
            }
            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                fileName = temp + "/openmodelica." + username + ".objid";
            } else {
                fileName = temp + "/openmodelica." + username + ".objid" + "."
                        + _corbaSession;
            }
            break;
        case WINDOWS:
            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                fileName = temp + "openmodelica.objid";
            } else {
                fileName = temp + "openmodelica.objid" + "." + _corbaSession;
            }
            break;
        case MAC:
            String macUsername = System.getenv("USER");
            if (macUsername == null) {
                macUsername = "nobody";
            }
            if (_corbaSession == null || _corbaSession.equalsIgnoreCase("")) {
                fileName = temp + "/openmodelica." + macUsername + ".objid";
            } else {
                fileName = temp + "/openmodelica." + macUsername + ".objid"
                        + "." + _corbaSession;
            }
            break;
        }

        OpenModelicaDirector.getOMCLogger().getInfo(
                "Will look for OMC object reference in '" + fileName + "'.");

        return fileName;
    }

    /** Read the OMC CORBA object reference from a file on a disk.
        @return The object reference as a String.
     */
    private String _readObjectFromFile() {

        String path = _getPathToObject();
        File f = new File(path);
        String stringifiedObjectReference = null;

        BufferedReader br = null;

        try {
            FileReader fr = new FileReader(f);
            br = new BufferedReader(fr);
            stringifiedObjectReference = br.readLine();
        } catch (FileNotFoundException e) {
            e = new FileNotFoundException(
                    "The OpenModelica Compiler CORBA object path '" + path
                            + "' does not exist!");
            e.printStackTrace();
        } catch (IOException e) {
            e = new IOException(
                    "Unable to read OpenModelica Compiler CORBA object from '"
                            + path + "'");
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e = new IOException(
                        "Very weird error indeed, IOException when closing BufferedReader for file '"
                                + path + "'.");
                e.printStackTrace();
            }
        }

        return stringifiedObjectReference;
    }

    /** Initialize an ORB, convert the stringified OpenModelica Compiler(OMC) object to a real
     CORBA object and then narrow that object to an OmcCommunication object.
     */
    private synchronized void _setupOmcc(String stringifiedObjectReference) {

        String args[] = { null };

        // Set the CORBA read timeout to a larger value as we send huge amounts of data
        // from OMC(OpenModelica Compiler) to MDT(Modelica Development Tooling).
        System.setProperty("com.sun.CORBA.transport.ORBTCPReadTimeouts",
                "1:60000:300:1");

        ORB orb;
        orb = ORB.init(args, null);

        // Convert string to CORBA object. 
        org.omg.CORBA.Object obj = orb
                .string_to_object(stringifiedObjectReference);

        // Convert object to OmcCommunication object. 
        omcc = OmcCommunicationHelper.narrow(obj);
    }

    /** Start the OpenModelica Compiler(OMC) server by starting OMCThread.
     @exception ConnectException If OPENMODELICAHOME is not set 
     and we could not find binary file in the path.
     */
    private synchronized void _startServer() throws ConnectException {

        if (!_fOMCThreadHasBeenScheduled) {

            if (_fOMCThread == null) {
                _fOMCThread = new OMCThread();
            }
            _fOMCThread.start();
            _fOMCThreadHasBeenScheduled = true;

            synchronized (_fOMCThread) {
                try {
                    _fOMCThread.wait(10000);
                } catch (InterruptedException e) {
                }
            }
        }
        synchronized (_fOMCThread) {
            _fOMCThread.notify();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Initialize _corbaSession.
    private String _corbaSession = null;

    // Indicate if we give up on running OpenModelica Compiler(OMC) as it is unable to start. 
    private boolean _couldNotStartOMC = false;

    // This object is used for starting OpenModelica Compiler(OMC)'s thread. 
    private OMCThread _fOMCThread = null;

    // Flag which indicates whether the server should start or not. 
    private boolean _fOMCThreadHasBeenScheduled = false;

    // Initialize the number of errors.
    private int _numberOfErrors = 0;

    // The Operating System(OS) we are running on.
    private osType _os;

    // Maximum number of compiler errors to display. 
    private int _showMaxErrors = 10;
}

// Actor that calls a simulation program of a dynamic system that is coupled to Ptolemy II

/*
 ********************************************************************
Copyright Notice
----------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy). All rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov

NOTICE.  This software was developed under partial funding from the U.S.
Department of Energy.  As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly.  Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.


Modified BSD License agreement
------------------------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.
   3. Neither the name of the University of California, Lawrence
      Berkeley National Laboratory, U.S. Dept. of Energy nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

 ********************************************************************
 */

package lbnl.actor.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import lbnl.actor.lib.net.Server;
import lbnl.util.ClientProcess;
import lbnl.util.WarningWindow;
import lbnl.util.XMLWriter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.lib.SDFTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

/**
 * Actor that calls a simulation program of a dynamic system
 * that is coupled to Ptolemy II. At the start of the simulation,
 * this actor fires a system command that is defined by the parameter
 * <code>programName</code> with arguments <code>programArguments</code>.
 * It then initiates a socket connection and uses the socket to
 * exchange data with the external simulation program each time
 * the actor is fired.
 *
 * @author Michael Wetter
 * @version $Id$
 * @since Ptolemy II 8.0
 *
 */
public class Simulator extends SDFTransformer {

    /** Constructs an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Simulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.DOUBLE_MATRIX);
        input.setMultiport(false);
        output.setTypeEquals(BaseType.DOUBLE_MATRIX);
        output.setMultiport(false);

        programName = new FileParameter(this, "programName");
        new Parameter(programName, "allowFiles", BooleanToken.TRUE);
        new Parameter(programName, "allowDirectories", BooleanToken.FALSE);

        programArguments = new Parameter(this, "programArguments");
        programArguments.setTypeEquals(BaseType.STRING);
        programArguments.setExpression("");

        workingDirectory = new FileParameter(this, "workingDirectory");
        new Parameter(workingDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(workingDirectory, "allowDirectories", BooleanToken.TRUE);
        workingDirectory.setExpression(".");

        simulationLogFile = new FileParameter(this, "simulationLogFile");
        simulationLogFile.setTypeEquals(BaseType.STRING);
        simulationLogFile.setExpression("simulation.log");
        new Parameter(simulationLogFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(simulationLogFile, "allowDirectories", BooleanToken.FALSE);

        socketTimeout = new Parameter(this, "socketTimeout");
        socketTimeout.setDisplayName("socketTimeout [milliseconds]");
        socketTimeout.setExpression("5000");
        socketTimeout.setTypeEquals(BaseType.INT);

        showConsoleWindow = new Parameter(this, "showConsoleWindow");
        showConsoleWindow.setTypeEquals(BaseType.BOOLEAN);
        showConsoleWindow.setToken(BooleanToken.TRUE);

        // expert settings
        socketPortNumber = new Parameter(this, "socketPortNumber");
        socketPortNumber
        .setDisplayName("socketPortNumber (used if non-negative)");
        socketPortNumber.setExpression("-1");
        socketPortNumber.setTypeEquals(BaseType.INT);
        socketPortNumber.setVisibility(Settable.EXPERT);

        socketConfigurationFile = new FileParameter(this,
                "socketConfigurationFile");
        socketConfigurationFile.setTypeEquals(BaseType.STRING);
        socketConfigurationFile.setExpression("socket.cfg");
        new Parameter(socketConfigurationFile, "allowFiles", BooleanToken.TRUE);
        new Parameter(socketConfigurationFile, "allowDirectories",
                BooleanToken.FALSE);
        socketConfigurationFile.setVisibility(Settable.EXPERT);

        // we produce one (DOUBLE_MATRIX) token as the initial output
        output_tokenInitProduction.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Simulator newObject = (Simulator) super.clone(workspace);

        newObject.programArguments = (Parameter) newObject
                .getAttribute("programArguments");
        newObject.programName = (FileParameter) newObject
                .getAttribute("programName");
        newObject.socketPortNumber = (Parameter) newObject
                .getAttribute("socketPortNumber");
        newObject.simulationLogFile = (FileParameter) newObject
                .getAttribute("simulationLogFile");
        newObject.socketConfigurationFile = (FileParameter) newObject
                .getAttribute("socketConfigurationFile");
        newObject.socketTimeout = (Parameter) newObject
                .getAttribute("socketTimeout");
        newObject.workingDirectory = (FileParameter) newObject
                .getAttribute("workingDirectory");
        newObject.showConsoleWindow = (Parameter) newObject
                .getAttribute("showConsoleWindow");

        return newObject;
    }

    /** Send the input token to the client program and send the
     * output from the client program to the output port.
     *
     *  @exception IllegalActionException If the simulation time between Ptolemy
     *    and the client program is not synchronized.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        // Check the input port for a token.
        if (input.hasToken(0)) {
            if (server.getClientFlag() == 0) {
                // If clientflag is non-zero, do not read anymore
                _writeToServer();
                // before the read happens, the client program will advance one time step
                _readFromServer();
                if (server.getClientFlag() == 0) {
                    // Get values sent by simulator
                    double[] dblRea = server.getDoubleArray();
                    outTok = new DoubleMatrixToken(dblRea, dblRea.length, 1);

                    // Make sure that simulation times are synchronized
                    final double simTimRea = server
                            .getSimulationTimeReadFromClient();
                    final double simTim = getDirector().getModelTime()
                            .getDoubleValue();
                    if (firstFire) {
                        firstFire = false;
                    } else {
                        if (Math.abs(simTimRea - simTimReaPre
                                - (simTim - simTimPre)) > 0.0001) {
                            final String em = "Simulation time of "
                                    + this.getFullName()
                                    + " is not synchronized." + LS
                                    + "Time step in Ptolemy = "
                                    + (simTim - simTimPre) + LS
                                    + "Time step in client  = "
                                    + (simTimRea - simTimReaPre) + LS
                                    + "Time in client = " + simTimRea;
                            throw new IllegalActionException(this, em);
                        }
                    }
                    // Store simulation time
                    simTimReaPre = simTimRea;
                    simTimPre = simTim;
                }

            } else { // Either client is down or this is the first time step.
                if (clientTerminated) {
                    // Client terminated in last call, but Ptolemy keeps doing a
                    // (at least one) more time step. Hence we issue a warning.
                    // Start a new thread for the warning window so that the simulation can continue.
                    if (warWin == null) {
                        if (!isHeadless) {
                            warWin = new Thread(new WarningWindow(
                                    terminationMessage));
                            warWin.start();
                        }
                        System.err.println("*** " + terminationMessage);
                    }
                }
                // Consume token
                input.get(0);
                final double simTimRea = server
                        .getSimulationTimeReadFromClient();
                final double simTim = getDirector().getModelTime()
                        .getDoubleValue();
                // Store simulation time
                simTimReaPre = simTimRea;
                simTimPre = simTim;

            }
        }
        //////////////////////////////////////////////////////
        // send output token
        output.send(0, outTok);
    }

    /** Output the first token during initialize.
     *  @exception IllegalActionException If there the client flag
     *  is non-zero, or the double array returned by the server
     *  is null.
     */
    protected void _outputInitToken() throws IllegalActionException {
        double[] dblRea = server.getDoubleArray();
        if (server.getClientFlag() == 1) {
            final String em = "Actor "
                    + this.getFullName()
                    + ": "
                    + LS
                    + "When trying to read from server, at time "
                    + getDirector().getModelTime().getDoubleValue()
                    + ","
                    + "client sent flag "
                    + server.getClientFlag()
                    + ","
                    + LS
                    + "which indicates that it reached the end of its simulation."
                    + LS
                    + "This should not happen during the initialization of this actor.";
            throw new IllegalActionException(em);
        } else if (server.getClientFlag() != 0) {
            final String em = "Actor " + this.getFullName() + ": " + LS
                    + "When trying to read from server, at time "
                    + getDirector().getModelTime().getDoubleValue() + ","
                    + "client sent flag " + server.getClientFlag() + "," + LS
                    + "which indicates a problem in the client.";
            throw new IllegalActionException(em);
        }
        if (dblRea == null) {
            final String em = "Actor "
                    + this.getFullName()
                    + ": "
                    + LS
                    + "When trying to read from server, obtained 'null' at time "
                    + getDirector().getModelTime().getDoubleValue();
            throw new IllegalActionException(em);
        }
        outTok = new DoubleMatrixToken(dblRea, dblRea.length, 1);
        output.send(0, outTok);
    }

    /** Write the data to the server instance, which will send it to
     * the client program.
     *
     * @exception IllegalActionException If there was an error when
     * writing to the server.
     */
    protected void _writeToServer() throws IllegalActionException {

        //////////////////////////////////////////////////////
        // Write data to server
        dblWri = _getDoubleArray(input.get(0));
        try {
            //                                       Thread.sleep(1000); // in milliseconds
            server.write(0, tokTim, dblWri);
        } catch (IOException e) {
            String em = "Error while writing to client: " + LS + e.getMessage();
            throw new IllegalActionException(this, em);
        }
        // get tokens' time stamp. This time will be written to the
        // client in the next time step, this time step read from the client
        // the output which will be sent to clients in the next time step
        // as inputs
        tokTim = getDirector().getModelTime().getDoubleValue();
    }

    /** Read the data from the server instance, which will read it
     * from the client program.
     *
     * @exception IllegalActionException If there was an error when
     * reading from the server.
     */
    protected void _readFromServer() throws IllegalActionException {
        //////////////////////////////////////////////////////
        // Read data from server
        try {
            //                Thread.sleep(100); // in milliseconds
            server.read();

            final int serFla = server.getClientFlag();
            if (serFla < 0) {
                String em = "Error: Client " + this.getFullName()
                        + " terminated communication by sending flag = "
                        + serFla + " at time "
                        + getDirector().getModelTime().getDoubleValue() + ","
                        + LS;
                // Add specifics of error message.
                switch (serFla) {
                case -10:
                    em += "which indicates a problem in the client during its initialization.";
                    break;
                case -20:
                    em += "which indicates a problem in the client during its time integration.";
                    break;
                default: // used for -1 and other (undefined) flags
                    em += "which indicates a problem in the client.";
                    break;
                }
                throw new IllegalActionException(this, em);
            }

            if (serFla > 0) {
                // Client reached its final time. If this is also the last
                // step from Ptolemy, then we don't want to issue a warning.
                // Hence, we store the information, and if there is one more
                // step from Ptolemy, then we issue a warning.
                clientTerminated = true;
                terminationMessage = "Warning: "
                        + this.getFullName()
                        + " terminated communication by sending flag = "
                        + serFla
                        + " at time "
                        + getDirector().getModelTime().getDoubleValue()
                        + "."
                        + LS
                        + "Simulation will continue without updated values from client program.";
            }
        } catch (java.net.SocketTimeoutException e) {
            String em = "SocketTimeoutException while reading from client in "
                    + this.getFullName()
                    + ": "
                    + LS
                    + e.getMessage()
                    + "."
                    + LS
                    + "Try to increase the value of the parameter 'socketTimeout'."
                    + LS
                    + "It could be that the client \""
                    + programName.getExpression()
                    + "\" is not executing properly.  From the command line, "
                    + "try running:"
                    + LS
                    + "  "
                    + programName.getExpression()
                    + " "
                    + programArguments.getExpression()
                    + LS
                    + "You should see something like:"
                    + LS
                    + "  Simulation model has time step       60"
                    + LS
                    + "  Error: Failed to obtain socket file descriptor. sockfd=-1."
                    + LS
                    + "The error message is expected because Ptolemy is not "
                    + "present."
                    + LS
                    + "Also, make sure that the directory that contains"
                    + LS
                    + "\"bcvtb.dll\" (on Windows), \"libbcvtb.so\" (on Linux) or"
                    + LS
                    + "\"libbcvtb.dylib\" (on Mac OS X) is on your"
                    + "PATH, LD_LIBRARY_PATH or DYLD_LIBRARY_PATH for Windows, "
                    + LS
                    + "Linux and Mac OS X respectively."
                    + LS
                    + "That directory contains the shared library used by the simulator.";
            try {
                server.close();
            } catch (IOException e2) {
            }
            // Check the exit value of the subprocess
            em += "\nClient subprocess exit value (should be 0): ";
            try {
                // If the subprocess is still running, then we may
                // get an exception here.  See Process.exitValue().
                em += cliPro.exitValue();
            } catch (Throwable throwable) {
                em += "<<Unknown: " + throwable.getMessage();
            }
            ; // do nothing here
            throw new IllegalActionException(this, e, em);
        } catch (IOException e) {
            String em = "IOException while reading from server.";
            try {
                server.close();
            } catch (IOException e2) {
            }
            ; // do nothing here
            // If the client sent a termination flag, then clientTerminated=true
            // In this case, the client may have closed the socket connection, and
            // hence we don't throw an IOException, but rather issue a warning
            // in case that Ptolemy proceeds with its iterations.
            // Without the check (!clientTerminated), an IOException is thrown
            // on Windows (but not on Mac or Linux) from the actor that connects
            // to EnergyPlus.
            if (!clientTerminated) {
                throw new IllegalActionException(this, e, em);
            }
        }
    }

    /** Initializes the data members and checks if the parameters of
     * the actor are valid.
     *
     * @exception IllegalActionException If the parameters of the
     *     actor are invalid, or if the file with the socket
     *     information cannot be written to disk.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Flag that indicates whether the client terminated,
        // and thread for the warning window that is used in such a situation
        clientTerminated = false;
        terminationMessage = "";
        warWin = null;

        // Check if we run in headless mode
        isHeadless = StringUtilities.getProperty("ptolemy.ptII.isHeadless")
                .equals("true");
        // Working directory
        worDir = Simulator.resolveDirectory(getContainer(),
                cutQuotationMarks(workingDirectory.getExpression()));

        // Verify that directory exist
        if (!new File(worDir).isDirectory()) {
            String em = "Error: Working directory does not exist." + LS
                    + "Working directory is set to: '" + worDir + "'" + LS
                    + "Check configuration of '" + this.getFullName() + "'.";
            throw new IllegalActionException(this, em);
        }
        // Verify that working directory is not used by any other Simulator actor.
        // Otherwise, they overwrite each others socket.cfg file, and possibly the output files.
        // Note that we check the return value for null and for the actor name. This
        // is because if Ptolemy II stops due to an exception (such as because of a
        // wrong connection), then the wrapup method is not called, in which case
        // the map still contains the name of this actor and its output directory.
        final String otherEntry = _simulatorWorkingDirs.putIfAbsent(worDir,
                this.getFullName());
        if (!(otherEntry == null || otherEntry.equals(this.getFullName()))) {
            String em = "Error: Working directory '"
                    + worDir
                    + "'"
                    + LS
                    + "is used by the following Simulator actors:"
                    + LS
                    + otherEntry
                    + LS
                    + this.getFullName()
                    + LS
                    + "Each Simulator actor needs to have its own working directory."
                    + LS
                    + "You need to change the value of the parameter workingDirectory"
                    + LS + "in any of these actors.";
            throw new IllegalActionException(this, em);
        }

        // Command that starts the simulation
        final String simCon = socketConfigurationFile.stringValue();
        // Assign BSD port number
        porNo = Integer.parseInt(socketPortNumber.getExpression());
        //////////////////////////////////////////////////////////////
        // Instantiate server for IPC
        try {
            // time out in milliseconds
            final int timOutMilSec = Integer.parseInt(socketTimeout
                    .getExpression());
            if (timOutMilSec <= 0) {
                final String em = "Parameter for socket time out must be positive."
                        + LS + "Received " + timOutMilSec + " milliseconds";
                throw new IllegalActionException(this, em);
            }
            if (porNo < 0) {
                server = new Server(timOutMilSec); // server uses any free port number
            } else {
                server = new Server(porNo, timOutMilSec);
            }
            // get port number
            porNo = server.getLocalPort();
        } catch (IOException e) {
            // try to close server unless it is still a null pointer
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e2) {
                }
            }
            // throw original exception
            throw new IllegalActionException(this, e, e.getMessage());
        }
        //////////////////////////////////////////////////////////////
        // Write xml file for client
        XMLWriter xmlWri = new XMLWriter(worDir, simCon, porNo);
        try {
            xmlWri.write();
        } catch (FileNotFoundException e) {
            String em = "FileNotFoundException when trying to write '"
                    + new File(worDir, simCon).getAbsolutePath() + "'.";
            throw new IllegalActionException(this, e, em);
        } catch (IOException e) {
            throw new IllegalActionException(this, e, e.toString());
        }
        //////////////////////////////////////////////////////////////
        // Start the simulation
        _startSimulation();
    }

    /** Resolve the command string.
     *
     *  This method replaces $CLASSPATH, relative file names and adds .exe
     *  to the command (under Windows)
     *
     *  @param programName Name of program that starts the simulation.
     *  @return The command line string.
     *  @exception IllegalActionException If the simulation process arguments
     *                           are invalid.
     */
    public static String resolveCommandName(final File programName)
            throws IllegalActionException {
        File commandFile = programName;

        // If we are under Windows, look for the .exe
        if (System.getProperty("os.name").startsWith("Windows")) {
            File winComFil = new File(commandFile.toString() + ".exe");
            if (winComFil.exists()) {
                commandFile = winComFil;
            }
        }

        // Remove the path if the argument points to a directory.
        // This fixes the problem if the argument is matlab, and the
        // user runs vergil from a directory that has a subdirectory
        // called matlab.
        if (commandFile.isDirectory()) {
            return commandFile.getName();
        }

        String comArg = commandFile.toString();
        // Get the absolute file name. Otherwise, invoking a command
        // like ../cclient will not work
        commandFile = new File(comArg);
        if (commandFile.exists() && !commandFile.isDirectory()) {
            try {
                comArg = commandFile.getCanonicalPath();
            } catch (IOException exc) {
                String em = "Error: Could not get canonical path for '"
                        + comArg + "'.";
                throw new IllegalActionException(em);
            }
        } else {
            comArg = commandFile.getName();
        }

        return comArg;
    }

    /** Resolve the working string.
     *
     *  This method adds the path of the MoML file to its argument if
     *  the argument is a relative directory.
     *
     *  @param namedObj A named object, typically the container of the model
     *  @param dir The directory to be resolved.
     *  @return The resolved working string.
     *  @exception IllegalActionException If an attribute is found with the name "_uri"
     *           that is not an instance of the URIAttribute class
     */
    public static String resolveDirectory(final NamedObj namedObj,
            final String dir) throws IllegalActionException {
        if (new File(dir).isAbsolute()) {
            return dir;
        }
        String child = dir;
        if (child.length() == 0) {
            child = ".";
        }
        final URI modelURI = URIAttribute.getModelURI(namedObj);
        // If we are running from jar files, then modelURI could be
        // non-null, but modelURI.getPath() could be null.
        String parent = "";
        if (modelURI != null && modelURI.getPath() != null) {
            parent = new File(modelURI.getPath()).getParent();
        } else {
            String property = "java.io.tmpdir";
            parent = StringUtilities.getProperty(property);

            System.out.println("Could not get the path of the URIAttribute of "
                    + namedObj.getFullName() + ".  Using the value of the "
                    + property + " property instead. (" + parent + ").");
        }
        final File file = new File(parent, child);
        child = file.getPath();
        return child;
    }

    /** Start the simulation program.
     *
     *  @exception IllegalActionException If the simulation process arguments
     *                           are invalid.
     */
    protected void _startSimulation() throws IllegalActionException {
        //////////////////////////////////////////////////////////////
        // Construct the argument list for the process builder
        List<String> com = new ArrayList<String>();

        // Process the program name
        // Maybe the user specified $CLASSPATH/lbnl/demo/CRoom/client
        com.add(Simulator.resolveCommandName(programName.asFile()));

        // Process program arguments
        final String argLin = cutQuotationMarks(programArguments
                .getExpression());

        StringTokenizer st = new StringTokenizer(argLin);
        while (st.hasMoreTokens()) {
            com.add(st.nextToken());
        }
        // Close the window that contains the console output.
        // This is needed if a simulation is started multiple times.
        // Otherwise, each new run would make a new window.
        if (cliPro != null) {
            cliPro.disposeWindow();
        }

        cliPro = new ClientProcess(this.getFullName());
        cliPro.redirectErrorStream(true);
        cliPro.setProcessArguments(com, worDir);
        // Check if we run in headless mode
        final boolean showConsole = ((BooleanToken) showConsoleWindow
                .getToken()).booleanValue();
        cliPro.showConsoleWindow(showConsole && !isHeadless);

        // Set simulation log file.
        // The call to System.gc() is required on Windows: If this actor is called multiple times
        // on Windows using vmware fusion and vmware workstation, then the simulation log file
        // exists but cannot be deleted. Calling System.gc() releases the resources which allows
        // Java to delete the file. See also http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6266377
        // This error does not happen on Linux and on Mac OS X.
        System.gc();

        File slf = simulationLogFile.asFile();
        try {
            if (slf.exists()) {
                if (slf.delete()) {
                    if (slf.exists()) {
                        throw new Exception("Cannot delete file.");
                    }
                }
            }
            if (!slf.createNewFile()) {
                throw new Exception("Cannot create file.");
            }
            // make sure we can write new file
            if (!slf.canWrite()) {
                throw new Exception("Cannot write to file.");
            }
        } catch (Exception e) {
            String em = "Error: Cannot write to simulation log file." + LS
                    + "Simulation log file is '" + slf.getAbsolutePath() + "'"
                    + LS + "Check configuration of '" + this.getFullName()
                    + "'.";
            throw new IllegalActionException(this, e, em);
        }
        cliPro.setSimulationLogFile(slf);
        cliPro.run();
        if (!cliPro.processStarted()) {
            String em = "Error: Simulation process did not start." + LS
                    + cliPro.getErrorMessage() + LS
                    + "Check configuration of '" + this.getFullName() + "'.";
            throw new IllegalActionException(this, em);
        }
    }

    /** Initialize state variables.
     *
     *  @exception IllegalActionException If the parent class throws it or
     *                              if the server socket cannot be opened
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        tokTim = getDirector().getModelTime().getDoubleValue();
        firstFire = true;

        //////////////////////////////////////////////////////////////
        // New code since 2008-01-05
        // Send initial output token. See also domains/sdf/lib/SampleDelay.java
        _readFromServer();

        _outputInitToken();
    }

    /** Closes sockets and shuts down the simulator.
     *
     *  @exception IllegalActionException if the base class throws it or
     *        if an I/O error occurs when closing the socket.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // Remove the entry for the working directory. Otherwise,
        // Ptolemy II needs to be closed to erase the static map.
        if (worDir != null) {
            _simulatorWorkingDirs.remove(worDir);
        }
        try {
            // Send signal to the client, indicating that we are done
            // with the time stepping.  This allows the client to
            // terminate gracefully.

            // Server can be null if we are exporting to JNLP.
            if (server != null) {
                server.write(1, tokTim, dblWri);
                // Close the server.
                server.close();
            }
        } catch (IOException e) {
            if (!clientTerminated) {
                throw new IllegalActionException(this, e, e.getMessage());
            }
        }
        if (!isHeadless) {
            // Reset position of window that shows console output so
            // that for the next simulation, the window will be placed
            // on top of the screen again
            ClientProcess.resetWindowLocation();
        }
    }

    /** Cut the leading and terminating quotation marks if present.
     *
     *@param str The string.
     *@return The string with leading and terminating quotation marks removed if present
     */
    public static String cutQuotationMarks(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        } else {
            return str;
        }
    }

    /** Get a double array from the Token.
     *
     * @param t the token which must be a type that can be converted to an ArrayToken
     * @return the double[] array with the elements of the Token
     * @exception IllegalActionException If the base class throws it.
     */
    protected double[] _getDoubleArray(ptolemy.data.Token t)
            throws IllegalActionException {
        final DoubleMatrixToken arrTok = (DoubleMatrixToken) t;
        final int n = arrTok.getRowCount();
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            final DoubleToken scaTok = (DoubleToken) arrTok.getElementAsToken(
                    i, 0);
            ret[i] = scaTok.doubleValue();
            if (Double.isNaN(ret[i])) {
                final String em = "Actor " + this.getFullName() + ": " + LS
                        + "Token number " + i + " is NaN at time "
                        + getDirector().getModelTime().getDoubleValue();
                throw new IllegalActionException(this, em);
            }
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////
    /** Arguments of program that starts the simulation. */
    public Parameter programArguments;

    /** Name of program that starts the simulation. */
    public FileParameter programName;

    /** Port number for BSD socket (used if non-negative). */
    public Parameter socketPortNumber;

    /** File name to which this actor writes the simulation log. */
    public FileParameter simulationLogFile;

    /** File name to which this actor writes the socket configuration. */
    public FileParameter socketConfigurationFile;

    /** Socket time out in milliseconds. */
    public Parameter socketTimeout;

    /** Working directory of the simulation. */
    public FileParameter workingDirectory;

    /** If <i>true</i> (the default), a window will be created that
        shows the console output. */
    public Parameter showConsoleWindow;

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Double values that were written to the socket. */
    protected double[] dblWri;

    /** Thread that runs the simulation. */
    protected ClientProcess cliPro;

    /** Port number that is actually used for BSD socket. */
    protected int porNo;

    /** Server used for data exchange. */
    protected Server server;

    /** Working directory of the subprocess. */
    protected String worDir;

    /** Output tokens. */
    protected DoubleMatrixToken outTok;

    /** Ptolemy's time at the last call of the fire method. */
    protected double simTimPre;

    /** Time read from the simulation program at the last call of the fire method. */
    protected double simTimReaPre;

    /** Flag, set to true when the clients terminates the communication. */
    protected boolean clientTerminated;

    /** Thread that is used if a warning window need to be shown. */
    protected Thread warWin;

    /** Message that will be displayed in the warning window when the client terminated,
        but Ptolemy continues with the simulation. */
    protected String terminationMessage;

    /** Flag, set the <code>true</code> if Ptolemy is run without any graphical
     *  interface.
     *
     * If <code>isHeadless=true</code>, this actor will not open any windows for
     * reporting outputs or warnings.
     */
    protected boolean isHeadless;

    /** Flag that is true during the first firing of this actor/. */
    protected boolean firstFire;

    /** System dependent line separator. */
    protected final static String LS = System.getProperty("line.separator");

    /** Time of token that will be written to the client.

        This is equal to the Ptolemy time minus one time step, because at time t_k,
        a client gets the output of other clients at t_{k-1}, which allows the client to
        compute the states and outputs at t_k
     */
    protected double tokTim;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    /** Map that contains the working directory and the name of the
        Simulator actor. This map is used to enforce that each Simulator
        actor uses its own working directory.
     */
    private static ConcurrentHashMap<String, String> _simulatorWorkingDirs = new ConcurrentHashMap<String, String>();
}

// Actor that calls a system command.

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import lbnl.util.ClientProcess;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

/**
  Actor that calls a system command.
  This actor fires a system command that is defined by the parameter
  <code>programName</code> with arguments <code>programArguments</code>.
  It waits unit the command terminates. The output of the actor
  are the exit flag, the standard output and standard error
  of the command

  <p>The parameters <code>programName</code> and <code>programArguments</code>
  can have references to the port name that will be replaced by the token
  value of the respective port. For example, a user may add an input port
  called <code>inPort</code> and enter as the <code>programArguments</code>
  the string <code>1 $inPort 3</code>. Then, if at the current firing, the
  input port is <code>inPort=2</code>, then the program argument will
  be <code>1 2 3</code>. In addition to port names, you can also use
  the variable <code>$time</code> and <code>$iteration</code>, which
  will be replaced by the current simulation time and the iteration count.

  To use this class, instantiate it, then add ports (instances
  of TypedIOPort).  In vergil, you can add ports by right clicking on
  the icon and selecting "Configure Ports".  In MoML you can add
  ports by just including ports of class TypedIOPort, set to be
  inputs, as in the following example:</p>

 <pre>
 &lt;entity name="exp" class="lbnl.actor.lib.SystemCommand"&gt;
 &lt;port name="in" class="ptolemy.actor.TypedIOPort"&gt;
 &lt;property name="input"/&gt;
 &lt;/port&gt;
 &lt;/entity&gt;
 </pre>

 *
 * @author Michael Wetter
 * @version $Id$
 * @since Ptolemy II 8.0
 *
 */
public class SystemCommand extends TypedAtomicActor {

    /** Constructs an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SystemCommand(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        exitValue = new TypedIOPort(this, "exitValue", false, true);
        output = new TypedIOPort(this, "output", false, true);
        error = new TypedIOPort(this, "error", false, true);

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

        showConsoleWindow = new Parameter(this, "showConsoleWindow");
        showConsoleWindow.setTypeEquals(BaseType.BOOLEAN);
        showConsoleWindow.setToken(BooleanToken.TRUE);

        output.setTypeEquals(BaseType.STRING);
        error.setTypeEquals(BaseType.STRING);
        exitValue.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The port that outputs the standard output stream of the program. */
    public TypedIOPort output;

    /** The port that outputs the standard error stream of the program. */
    public TypedIOPort error;

    /** The port that outputs the exit value of the program. */
    public TypedIOPort exitValue;

    /** Name of program that starts the simulation. */
    public FileParameter programName;

    /** File name to which this actor writes the simulation log. */
    public FileParameter simulationLogFile;

    /** Working directory of the simulation. */
    public FileParameter workingDirectory;

    /** If <i>true</i> (the default), a window will be created that
        shows the console output. */
    public Parameter showConsoleWindow;

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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SystemCommand newObject = (SystemCommand) super.clone(workspace);

        newObject.programArguments = (Parameter) newObject
                .getAttribute("programArguments");
        newObject.programName = (FileParameter) newObject
                .getAttribute("programName");
        newObject.simulationLogFile = (FileParameter) newObject
                .getAttribute("simulationLogFile");
        newObject.workingDirectory = (FileParameter) newObject
                .getAttribute("workingDirectory");
        newObject.showConsoleWindow = (Parameter) newObject
                .getAttribute("showConsoleWindow");

        newObject._iterationCount = 1;
        newObject._tokenMap = null;

        return newObject;
    }

    /** Read the input token, update the program name and program arguments,
     *  start the program and wait unit it terminates. Then, send the program's
     *  exit value, the standard output and the standard error stream to
     *  the output ports.
     *
     *@exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            // FIXME: Handle multiports
            if (port.isOutsideConnected()) {
                if (port.hasToken(0)) {
                    Token inputToken = port.get(0);
                    _tokenMap.put(port.getName(), inputToken);
                } else {
                    throw new IllegalActionException(this, "Input port "
                            + port.getName() + " has no data.");
                }
            }
        }
        _startSimulation();
        try {
            final int exiVal = cliPro.waitFor();
            exitValue.send(0, new IntToken(exiVal));
            output.send(0, new StringToken(cliPro.getStandardOutput()));
            error.send(0, new StringToken(cliPro.getStandardError()));
        } catch (InterruptedException e) {
            String em = "Error: System command has been interrupted.";
            throw new IllegalActionException(this, e, em);
        }
    }

    /** Initializes the data members and checks if the parameters of
     * the actor are valid.
     *
     * @exception IllegalActionException If the parameters of the
     *     actor are invalid.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();

        // Check if we run in headless mode
        isHeadless = StringUtilities.getProperty("ptolemy.ptII.isHeadless")
                .equals("true");

        // Working directory
        String workDire = cutQuotationMarks(workingDirectory.getExpression());
        // If directory is not set, set it to current directory.
        if (workDire.length() == 0) {
            workDire = ".";
        }
        // Verify that directory exist
        if (!new File(workDire).isDirectory()) {
            String em = "Error: Working directory does not exist." + LS
                    + "Working directory is set to: '" + workDire + "'" + LS
                    + "Check configuration of '" + this.getFullName() + "'.";
            throw new IllegalActionException(this, em);
        }
        _initializeSimulation();
        _tokenMap = new HashMap<String, Token>();
    }

    /** Initializes the simulation program.
     *
     *  @exception IllegalActionException If the simulation process arguments
     *                           are invalid.
     */
    private void _initializeSimulation() throws IllegalActionException {
        //////////////////////////////////////////////////////////////
        worDir = Simulator.resolveDirectory(getContainer(),
                cutQuotationMarks(workingDirectory.getExpression()));

        //////////////////////////////////////////////////////////////
        // Initialize the simulation process
        // Get the command as a File in case it has $CLASSPATH in it.
        File commandFile = programName.asFile();
        final String comArg;
        if (commandFile.exists()) {
            // Maybe the user specified $CLASSPATH/lbnl/demo/CRoom/client
            comArg = commandFile.toString();
        } else {
            // If we are under Windows, look for the .exe
            commandFile = new File(commandFile.toString() + ".exe");
            if (commandFile.exists()) {
                // Maybe the user specified $CLASSPATH/lbnl/demo/CRoom/client
                comArg = commandFile.toString();
            } else {
                // Maybe the user specfied "matlab"
                comArg = programName.getExpression();
            }
        }
        final String argLin = cutQuotationMarks(programArguments
                .getExpression());
        commandList = new ArrayList<String>();
        /* mwetter:
           Disabled section. Otherwise, C:\Program Files\xyz is parsed to
                    two tokens ("C:\Program" and "Files\xyz") in which case
                    the process builder would try to launch C:\Program
          StringTokenizer st = new StringTokenizer(comArg);
          while (st.hasMoreTokens()) {
            commandList.add(st.nextToken());
        }
        */
        commandList.add(comArg);
        StringTokenizer st = new StringTokenizer(argLin);
        while (st.hasMoreTokens()) {
            commandList.add(st.nextToken());
        }
        // Close the window that contains the console output.
        // This is needed if a simulation is started multiple times.
        // Otherwise, each new run would make a new window.
        if (cliPro != null) {
            cliPro.disposeWindow();
        }
        cliPro = new ClientProcess(this.getFullName());
        final boolean showConsole = ((BooleanToken) showConsoleWindow
                .getToken()).booleanValue();
        cliPro.showConsoleWindow(showConsole && !isHeadless);
    }

    /** Starts the simulation program.
     *
     *@exception IllegalActionException If the simulation process arguments
     *                                  are invalid.
     */
    private void _startSimulation() throws IllegalActionException {
        ArrayList<String> com = new ArrayList<String>();
        // Iterate over the list of command to replace all references to input port names.
        // Reference take the form $portName where portName is the name of the port.
        // We call cutQuotationMarks(String) to remove the quotation marks that the Tokens
        // may have. Otherwise, an entry programArguments=$fileName may be parsed to
        // programArguments="$fileName" and commands such as cat $fileName may not find
        // the file on Linux.
        for (Object element : commandList) {
            String comIte = (String) element;
            for (Map.Entry<String, Token> e : _tokenMap.entrySet()) {
                final String fin = '$' + e.getKey();
                while (comIte.contains(fin)) {
                    comIte = comIte.replace(fin, cutQuotationMarks(e.getValue()
                            .toString()));
                }
            }
            // Replace $time and $iteration
            String fin = "$time";
            while (comIte.contains(fin)) {
                comIte = comIte.replace(fin, cutQuotationMarks(getDirector()
                        .getModelTime().toString()));
            }
            fin = "$iteration";
            while (comIte.contains(fin)) {
                comIte = comIte.replace(fin, Integer.toString(_iterationCount)
                        .toString());
            }
            com.add(comIte);
        }
        ///////////////////////////////
        // Resolve the command in case it is a relative file name or in case
        // it has CLASSPATH or relative file names in it.
        com.set(0, Simulator.resolveCommandName(new File(com.get(0))));

        // Set process arguments
        cliPro.setProcessArguments(com, worDir);

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
        // Run the simulation
        cliPro.run();

        if (!cliPro.processStarted()) {
            String em = "Error: Simulation process did not start." + LS
                    + cliPro.getErrorMessage() + LS
                    + "Check configuration of '" + this.getFullName() + "'.";
            throw new IllegalActionException(this, em);
        }
    }

    /** Initialize actor.
     *
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (getPort("time") != null) {
            throw new IllegalActionException(
                    this,
                    "This actor has a port named \"time\", "
                            + "which will not be read, instead the "
                            + "reserved system variable \"time\" will be read. "
                            + "Delete the \"time\" port to avoid this message.");
        }
        if (getPort("iteration") != null) {
            throw new IllegalActionException(
                    this,
                    "This actor has a port named \"iteration\", "
                            + "which will not be read, instead the "
                            + "reserved system variable \"iteration\" will be read. "
                            + "Delete the \"iteration\" port to avoid this message.");
        }
        _iterationCount = 1;
    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;

        // This actor never requests termination.
        return true;
    }

    /** Prefire this actor.  Return false if an input port has no
     *  data, otherwise return true.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();

            // FIXME: Handle multiports
            if (port.isOutsideConnected()) {
                if (!port.hasToken(0)) {
                    return false;
                }
            }
        }
        return super.prefire();
    }

    /** Wraps up the base class.
     *
     *  @exception IllegalActionException if the base class throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // Reset position of window that shows console output so that for the next
        // simulation, the window will be placed on top of the screen again
        ClientProcess.resetWindowLocation();
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////
    /** Arguments of program that starts the simulation. */
    protected Parameter programArguments;

    /** Thread that runs the simulation. */
    protected ClientProcess cliPro;

    /** List with the command and the arguments.
     *
     * This list is stored as it may contain references to input ports that will be
     * substituted prior to the simulation
     */
    protected ArrayList<String> commandList;

    /** Working directory of the subprocess. */
    protected String worDir;

    /** Flag, set the <code>true</code> if Ptolemy is run without any graphical
     *  interface
     *
     * If <code>isHeadless=true</code>, this actor will not open any windows for
     * reporting outputs or warnings.
     */
    protected boolean isHeadless;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _iterationCount = 1;

    private HashMap<String, Token> _tokenMap;

    /** System dependent line separator */
    private final static String LS = System.getProperty("line.separator");
}

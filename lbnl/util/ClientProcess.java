// Communicate with a client process.

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

package lbnl.util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import ptolemy.kernel.util.IllegalActionException;

/**
 * Communicate with a client process.
 *
 * @author Michael Wetter
 * @version $Id$
 * @since Ptolemy II 8.0
 *
 */
public class ClientProcess extends Thread {

    private final static String LS = System.getProperty("line.separator");

    /** Create a ClientProcess.
     *  @param modelName name of the model, used for display only
     **/
    public ClientProcess(final String modelName) {
        super();
        proSta = false;
        errMes = null;
        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        modNam = modelName;
        showConsoleWindow = true;
    }

    /** Set a flag that determines whether the console window will be displayed.
     *  @param showWindow Set to false to avoid the console window to be shown
     */
    public void showConsoleWindow(boolean showWindow) {
        showConsoleWindow = showWindow;
    }

    /** Disposes the window that displays the console output. */
    public void disposeWindow() {
        if (stdFra != null) {
            stdFra.dispose();
        }
    }

    /** Redirects the standard error stream to the standard output stream.
     *  @param flag if true, redirects the standard error stream to the standard output stream
     */
    public void redirectErrorStream(boolean flag) {
        redErrStr = flag;
    }

    /** Sets the simulation log file.
     *  @param simLogFil The log file.
     */
    public void setSimulationLogFile(File simLogFil) {
        logFil = new File(simLogFil.getAbsolutePath());
        logToSysOut = true;
        // Delete log file if it exists
        if (logFil.exists() && !logFil.delete()) {
            throw new RuntimeException("Cannot delete \""
                    + logFil.getAbsolutePath() + "\"");
        }
    }

    /** Runs the process. */
    @Override
    public void run() {
        ProcessBuilder pb = new ProcessBuilder(cmdArr);
        try {
            proSta = false;
            pb.directory(worDir);
            pb.redirectErrorStream(redErrStr);
            // FIXME: should we call simPro.exitValue() and destroy()
            simPro = pb.start();
            proSta = true;
            priStdOut = new PrintOutput(cmdArr.get(0));
            priStdErr = new PrintStderr();
            priStdOut.start();
            priStdErr.start();
        } catch (SecurityException exc) {
            proSta = false;
            errMes = "Error when starting external process."
                    + LS
                    + "You may not have the permission to execute this command."
                    + LS + "Error message           : " + exc.getMessage() + LS
                    + "Current directory       : " + worDir + LS
                    + "ProcessBuilder arguments: " + pb.command().toString();
        } catch (java.io.IOException exc) {
            proSta = false;
            errMes = "Error when starting external process." + LS
                    + "Error message           : " + exc.getMessage() + LS
                    + "Current directory       : " + worDir + LS
                    + "ProcessBuilder arguments: " + pb.command().toString();
        }
    }

    /** Return true if the process started without throwing an exception.
     *  @return true if the process started without throwing an exception.
     */
    public boolean processStarted() {
        return proSta;
    }

    /** Return the error message if <code>proSta=true</code> or a null pointer otherwise.
     *  @return the error message if <code>proSta=true</code> or a null pointer otherwise.
     */
    public String getErrorMessage() {
        return errMes;
    }

    /** Causes the current thread to wait, if necessary, until the process represented
     * by this Process object has terminated. This method returns immediately if the
     * subprocess has already terminated. If the subprocess has not yet terminated,
     * the calling thread will be blocked until the subprocess exits.
     *
     * @return the exit value of the process. By convention, 0 indicates normal termination.
     * @exception InterruptedException if the current thread is interrupted by another thread
     *           while it is waiting, then the wait is ended and an InterruptedException is thrown.
     */
    public int waitFor() throws InterruptedException {
        this.join();
        priStdOut.join();
        priStdErr.join();
        // Call waitFor() to make sure that the process exited.
        // Without this call, the exception
        // java.lang.IllegalThreadStateException: process hasn't exited
        //            at java.lang.UNIXProcess.exitValue(UNIXProcess.java)
        // is occasionally thrown
        return simPro.waitFor();
    }

    /** Get the standard output of the process.
     *  @return the standard output of the process
     */
    public String getStandardOutput() {
        return stdOut.toString();
    }

    /** Get the standard error of the process.
     *  @return the standard error of the process
     */
    public String getStandardError() {
        return stdErr.toString();
    }

    /** Get the exit value of the process.
     * @return the exit value of the process
     */
    public int exitValue() {
        return simPro.exitValue();
    }

    /** Reset the position of the window that shows the console output.
     *
     * This function is typically called by actors in the <i>wrapUp()</i> method
     * so that for the next simulation, the window will be placed at the
     * same position again as in the previous simulation
     */
    public static void resetWindowLocation() {
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit()
                .getScreenSize();
        // Set the window to the bottom left corner, where it gets less in the way
        // compared to the top left corner.

        // Move window up on Mac and Windows so that it does not overlap with taskbar
        final String osName = System.getProperty("os.name").toLowerCase(
                Locale.getDefault());
        int dLocY = 0;
        if (osName.indexOf("windows") > -1) {
            dLocY = 20;
        }
        if (osName.indexOf("mac") > -1) {
            dLocY = 20;
        }

        locY = Math.max(0, screenSize.height - dY - dLocY);
    }

    /** Frame that contains the console output of the simulation. */
    protected JFrame stdFra;

    /** Text area that contains the console output of the simulation. */
    protected JTextArea stdAre;

    /** Scroll pane  that contains the text area for the console output. */

    protected JScrollPane stdScrPan;

    /** Y location of window that displays the console output.
     *
     * This data member is static so that windows can be placed above each
     * other if multiple simulations are used
     */
    public static int locY = -1;

    /** Default height of window. */
    protected static final int dY = 200;

    ///////////////////////////////////////////////////////////////////
    /** Inner class to print any output of the process to the console. */
    private class PrintOutput extends Thread {

        /** Construct an object that starts a simulation
         *  @param programName Name of the program, used for output display only.
         *
         */
        public PrintOutput(final String programName) {
            stdOut = new StringBuilder();
            if (showConsoleWindow && stdFra == null) {
                stdFra = new JFrame("Output of " + modNam);
                JFrame.setDefaultLookAndFeelDecorated(true);
                stdFra.setSize(600, dY);
                stdAre = new JTextArea();
                stdScrPan = new JScrollPane(stdAre);
                if (showConsoleWindow) {
                    // If locY < 0, then this is the first call to any instance of
                    // ClientProcess, hence we reset the window position.
                    if (locY < 0) {
                        resetWindowLocation();
                    }

                    stdFra.setLocation(30, locY);
                    // Move the location up so that the window of another simulation
                    // does not overlap
                    // Move window up on Windows so that it does not overlap with taskbar
                    final String osName = System.getProperty("os.name")
                            .toLowerCase(Locale.getDefault());
                    if (osName.indexOf("linux") > -1) {
                        locY -= dY + 22;
                    } else if (osName.indexOf("mac") > -1) {
                        locY -= dY + 22;
                    } else {
                        locY -= dY;
                    }

                    stdAre.setEditable(false);
                    stdScrPan
                    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                    stdFra.add(stdScrPan);
                    stdFra.setVisible(true);
                }

            }
        }

        /** Runs the process. */
        @Override
        public void run() {
            if (simPro == null) {
                return;
            }
            InputStream is = simPro.getInputStream();
            InputStreamReader isr = null;
            PrintWriter pwLogFil = null;
            PrintWriter pwSysOut = null;
            BufferedReader br = null;
            BufferedWriter bufWri = null;
            FileWriter filWri = null;
            try {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                pwSysOut = new PrintWriter(System.out);
                try {
                    filWri = new FileWriter(logFil);
                    bufWri = new BufferedWriter(filWri);
                    pwLogFil = new PrintWriter(bufWri);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    pwLogFil = new PrintWriter(System.err);
                }

                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        if (logToSysOut) {
                            pwSysOut.println(line);
                            pwSysOut.flush();
                        }
                        pwLogFil.println(line);
                        pwLogFil.flush();
                        stdOut.append(line + LS);
                        if (showConsoleWindow) {
                            stdAre.append(line + LS);
                            //scroll to bottom of text area
                            stdAre.scrollRectToVisible(new Rectangle(0, stdAre
                                    .getHeight() - 2, 1, 1));
                        }
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            } finally {
                // FindBugs suggests closing these, but
                // is that ok if simPro has not terminated?
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
                if (pwLogFil != null) {
                    try {
                        // FIXME: is this ok if pwLogFil is System.err?
                        pwLogFil.close();
                        bufWri.close();
                        filWri.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (pwSysOut != null) {
                    try {
                        // FIXME: is this ok if pwSysOut is System.out?
                        pwSysOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    /** Inner class to print any stderr of the process to the console. */
    private class PrintStderr extends Thread {
        public PrintStderr() {
            stdErr = new StringBuilder();
        }

        /** Runs the process. */
        @Override
        public void run() {
            if (simPro == null) {
                return;
            }
            InputStream is = simPro.getErrorStream();
            InputStreamReader isr = null;
            PrintWriter pwLogFil = null;
            PrintWriter pwSysOut = null;
            BufferedReader br = null;
            BufferedWriter bufWri = null;
            FileWriter filWri = null;
            try {
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                pwSysOut = new PrintWriter(System.err);
                try {
                    filWri = new FileWriter(logFil);
                    bufWri = new BufferedWriter(filWri);
                    pwLogFil = new PrintWriter(bufWri);
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    pwLogFil = new PrintWriter(System.err);
                }

                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        System.out.println("Error: " + line);
                        if (logToSysOut) {
                            pwSysOut.println(line);
                            pwSysOut.flush();
                        }
                        pwLogFil.println(line);
                        pwLogFil.flush();
                        stdErr.append(line);
                    }
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            } finally {
                // FindBugs suggests closing these, but
                // is that ok if simPro has not terminated?
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
                if (pwLogFil != null) {
                    try {
                        // FIXME: is this ok if pwLogFil is System.err?
                        pwLogFil.close();
                        bufWri.close();
                        filWri.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (pwSysOut != null) {
                    try {
                        // FIXME: is this ok if pwSysOut is System.out?
                        pwSysOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    /** Set the process arguments.
     * @param cmdarray array containing the command to call and its arguments.
     * @param dir the working directory of the subprocess.
     * @exception IllegalActionException if the canonical path name of the program file
     *                                  cannot be obtained.
     */
    public void setProcessArguments(List<String> cmdarray, String dir)
            throws IllegalActionException {
        cmdArr = new ArrayList<String>();
        // Note: Earlier versions resolved the path name. This has been moved
        // to the calling program, as the calling program already implements
        // some file and path name checking.
        for (int i = 0; i < cmdarray.size(); i++) {
            cmdArr.add(cmdarray.get(i));
        }

        if (dir.equalsIgnoreCase(".")) {
            worDir = new File(System.getProperty("user.dir"));
        } else if (dir.startsWith("./")) {
            worDir = new File(System.getProperty("user.dir") + dir.substring(1));
        } else {
            worDir = new File(dir);
            if (!worDir.isAbsolute()) {
                worDir = new File(System.getProperty("user.dir")
                        + File.separator + dir);
            }
        }
    }

    /** Array containing the command to call and its arguments. */
    protected List<String> cmdArr;

    /** Working directory of the subprocess, or null if the subprocess
     * should inherit the working directory of the current process.
     */
    protected File worDir;

    /** Log file to which simulation output will be written. */
    protected File logFil;

    /** Flag, if <code>true</code>, then the output will be written to System.out. */
    protected boolean logToSysOut;

    /** Process for the simulation. */
    protected Process simPro;

    /** Name of the model. */
    protected String modNam;

    /** Flag, if true, then the console output will be displayed in a JFrame. */
    protected boolean showConsoleWindow;

    /** Flag that is set to <code>true</code> if the process started without throwing an exception. */
    protected boolean proSta;

    /** Error message if <code>proSta=true</code> or null pointer otherwise. */
    protected String errMes;

    /** String that contains the standard output stream. */
    protected StringBuilder stdOut;

    /** String that contains the standard error stream. */
    protected StringBuilder stdErr;

    /** The thread that captures the standard output stream. */
    protected PrintOutput priStdOut;

    /** The thread that captures the standard error stream. */
    protected PrintStderr priStdErr;

    /** Flag, if true, redirects the standard error stream to the standard output stream. */
    protected boolean redErrStr;

    /** Main method for testing.
     *  @param args  Commands to pass to the client process.
     *  @exception IllegalActionException if the canonical path name
     *  of the program file cannot be obtained.
     */
    public static void main(String args[]) throws IllegalActionException {
        ClientProcess c = new ClientProcess("Test");
        List<String> com = new ArrayList<String>();
        for (String arg : args) {
            com.add(arg);
        }
        c.setProcessArguments(com, ".");
        c.run();
    }
}

/* A helper class for the shell accessor module.

@Copyright (c) 2015-2017 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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


 */

package ptolemy.actor.lib.jjs.modules.shell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

///////////////////////////////////////////////////////////////////
//// ShellHelper

/**
 * A helper class for the shell accessor module. It provides functionality
 * to invoke a command and control <i>stdin</i> and <i>stdout</i>. It forks
 * off a process that executes the specified command.
 *
 * A reader thread listens to the process' output and forwards them via the
 * EventEmitter subsystem (<i>'message'</i> event) to the <i>shell.js</i>
 * module in Nashorn.
 *
 * A wait thread waits until the process exits and emits a <i>'close'</i> event
 * containing the process' exit value.
 *
 * @author Armin Wasicek
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (bilung)
 */
public class ShellHelper {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Factory method to create a new shell.
     *  @param scriptObjectMirror The JavaScript instance invoking the shell.
     *  @param command The command to be executed.
     *  @return a new shell.
     */
    public static ShellHelper createShell(ScriptObjectMirror scriptObjectMirror,
            String command) {
        return new ShellHelper(scriptObjectMirror, command);
    }

    /** Write to the stdin of the process.
     *  @param input The data to be sent to the process.
     *  @exception IOException If the string cannot be written.
     */
    public synchronized void write(String input) throws IOException {
        if (out != null && process.isAlive()) {
            out.write(input);
            out.newLine();
            out.flush();
        }
    }

    /** Start the process and the reader thread. Call
     *   this after initialization and all callbacks have
     *   been installed.
     *   TODO: get the exit value of the process.
     */
    public void start() {
        if (startProcess()) {
            startReader();
            startWaitForProcess();
        }
    }

    /** Kill the process and the reader thread.
     *  @exception IOException Not thrown in this base class.
     */
    public void wrapup() throws IOException {
        //        if (in!=null)  {
        //            in.close();
        //        }
        //        if (out!=null) {
        //            out.close();
        //        }
        if ((readerThread != null) && (readerThread.isAlive())) {
            _readerThreadRunning = false;
        }
        _readerThreadRunning = false;
        try {
            if (readerThread != null) {
                readerThread.join(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        readerThread = null;

        if ((process != null) && (process.isAlive())) {
            process.destroyForcibly();
        }
        process = null;

        try {
            if (waitForProcessThread != null) {
                waitForProcessThread.join(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitForProcessThread = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private constructors                  ////

    /** Private constructor for ShellHelper for calling a shell command
     *  @param currentObj The JavaScript instance of the script that this helps.
     *  @param cmd The command to be called.
     */
    private ShellHelper(ScriptObjectMirror currentObj, String command) {
        _currentObj = currentObj;
        this.command = command;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Builds and starts the command in a process.
     *  @return Returns true, if the process has started and the pipes
     *  are initialized.
     */
    private boolean startProcess() {
        processBuilder = new ProcessBuilder("sh", "-c", command);
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(Redirect.PIPE);
        processBuilder.redirectInput(Redirect.PIPE);
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            //this._currentObj.eval("exports.error('"+e.getMessage()+"');");
            //FIXME Process cannot be started. What to do?
            e.printStackTrace();
            return false;
        }
        // FIXME: FindBugs says that out is being accessed without a lock here.
        out = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(process.getInputStream(),
                StandardCharsets.UTF_8));
        return true;
    }

    /** Starts the reader thread to read from the process' stdout
     * asynchronously.
     */
    private void startReader() {
        readerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    String line = null;
                    try {
                        line = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (line != null) {
                        _currentObj.callMember("emit", "message", line);
                    } else {
                        _readerThreadRunning = false;
                    }
                } while (_readerThreadRunning);
            }
        });
        readerThread.setName("ShellHelper-reader");
        readerThread.start();
    }

    /** Starts the thread to wait for the process' exit and collects the exit value. */
    private void startWaitForProcess() {
        waitForProcessThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Wait to get exit value
                try {
                    int exitValue = process.waitFor();
                    _currentObj.callMember("emit", "close", exitValue);
                    //                    System.out.println("\n\nExit Value is " + exitValue);
                    //                    System.out.flush();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        waitForProcessThread.setName("ShellHelper-waiter");
        waitForProcessThread.start();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private fields                        ////

    /** The current instance of the JavaScript module. */
    private ScriptObjectMirror _currentObj;

    /** The variable used to build the process. */
    private ProcessBuilder processBuilder;

    /** The reader to connect to the process' stdout.*/
    private BufferedReader in;

    /** The reader to connect to the process' stdin.*/
    private BufferedWriter out;

    /** The instance of the invoked command.*/
    private Process process;

    /** A thread to read asynchronously from the process' stdout. */
    private Thread readerThread;

    /** A thread to wait for the process and read its exit value. */
    private Thread waitForProcessThread;

    /** Controls the reader thread. */
    private boolean _readerThreadRunning = true;

    /** A copy of the command that's invoked. */
    private String command;

}

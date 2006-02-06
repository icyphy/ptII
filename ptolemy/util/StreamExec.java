/* Run a list of commands.

 Copyright (c) 2003-2005 The Regents of the University of California.
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
 */
package ptolemy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

/** Execute commands in a subprocess.  This class does not use swing,
 for a graphical interface, see {@link ptolemy.gui.JTextAreaExec}.

 <p>Sample usage:
 <pre> 
        List execCommands = new LinkedList();
        execCommands.add("date");
        execCommands.add("sleep 3");
        execCommands.add("date");
        execCommands.add("notACommand");

        final StreamExec exec = new StreamExec();
        exec.setCommands(execCommands);

        exec.start();
  </pre>


 <p>Loosely based on Example1.java from
 http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html
 <p>See also
 http://developer.java.sun.com/developer/qow/archive/135/index.jsp
 and
 http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html

 @see ptolemy.gui.JTextAreaExec

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class StreamExec implements ExecuteCommands {

    /** Create a StreamExec. */
    public StreamExec() {
        // Does nothing?
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Cancel any running commands. */
    public void cancel() {
        //_worker.interrupt();
        if (_process != null) {
            _process.destroy();
        }
    }

    /** Clear the text area, status bar and progress bar. */
    public void clear() {
        updateStatusBar("");
        _updateProgressBar(0);
    }

    /** Return the return code of the last subprocess that was executed.
     *  @return the return code of the last subprocess that was executed.
     */
    public int getLastSubprocessReturnCode() {
        return _subprocessReturnCode;
    }

    /** Set the list of commands.
     *  @param commands A list of Strings, where each element is a command.
     */
    public void setCommands(List commands) {
        _commands = commands;
    }

    /** Set the working directory of the subprocess.
     *  @param workingDirectory The working directory of the
     *  subprocess.  If this argument is null, then the subprocess is
     *  exectued in the working directory of the current process.
     */
    public void setWorkingDirectory(File workingDirectory) {
        _workingDirectory = workingDirectory;
    }

    /** Start running the commands. */
    public void start() {
        String returnValue = _executeCommands();
        updateStatusBar(returnValue);
        stdout(returnValue);
    }

    /** Append the text message to stderr.  A derived class could
     *  append to a StringBuffer.  @link{JTextAreaExec} appends to a
     *  JTextArea. The output automatically gets a trailing newline
     *  appended.
     *  @param text The text to append to stdandard error.
     */
    public void stderr(final String text) {
        System.err.println(text);
        System.err.flush();
    }

    /** Append the text message to the output.  A derived class could
     *  append to a StringBuffer.  @link{JTextAreaExec} appends to a
     *  JTextArea.
     *  The output automatically gets a trailing newline appended.
     *  @param text The text to append to standard out.
     */
    public void stdout(final String text) {
        System.out.println(text);
        System.out.flush();
    }

    /** Set the text of the status bar.  In this base class, do
     *  nothing, derived classes may update a status bar.
     *  @param text The text with which the status bar is updated.
     */
    public void updateStatusBar(final String text) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the maximum of the progress bar.  In this base class, do
     *  nothing, derived classes may update the size of the progress bar.
     *  @param size The maximum size of the progress bar.
     */
    protected void _setProgressBarMaximum(int size) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Execute the commands in the list.  Update the output with
     * the command being run and the output.
     */
    private String _executeCommands() {
        try {
            Runtime runtime = Runtime.getRuntime();

            try {
                if (_process != null) {
                    _process.destroy();
                }

                _setProgressBarMaximum(_commands.size());

                int commandCount = 0;
                Iterator commands = _commands.iterator();

                while (commands.hasNext()) {
                    _updateProgressBar(++commandCount);

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    // Preprocess by removing lines that begin with '#'
                    // and converting substrings that begin and end
                    // with double quotes into one array element.
                    final String[] commandTokens = StringUtilities
                            .tokenizeForExec((String) commands.next());

                    if (_workingDirectory != null) {
                        stdout("In \"" + _workingDirectory
                                + "\", about to execute:\n");
                    } else {
                        stdout("About to execute:\n");
                    }


                    StringBuffer statusCommand = new StringBuffer();

                    for (int i = 0; i < commandTokens.length; i++) {
                        stdout("        " + commandTokens[i]);

                        // Accumulate the first 50 chars for use in
                        // the status buffer.
                        if (statusCommand.length() < 50) {
                            if (statusCommand.length() > 0) {
                                statusCommand.append(" ");
                            }

                            statusCommand.append(commandTokens[i]);
                        }
                    }

                    if (statusCommand.length() >= 50) {
                        statusCommand.append(" . . .");
                    }

                    updateStatusBar("Executing: " + statusCommand.toString());

                    // 2nd arg is null, meaning no environment changes.
                    _process = runtime.exec(commandTokens, null,
                            _workingDirectory);

                    // Set up a Thread to read in any error messages
                    _StreamReaderThread errorGobbler = new _StreamReaderThread(
                            _process.getErrorStream(),  this);

                    // Set up a Thread to read in any output messages
                    _StreamReaderThread outputGobbler = new _StreamReaderThread(
                            _process.getInputStream(),  this);

                    // Start up the Threads
                    errorGobbler.start();
                    outputGobbler.start();

                    try {
                        _subprocessReturnCode = _process.waitFor();

                        synchronized (this) {
                            _process = null;
                        }

                        if (_subprocessReturnCode != 0) {
                            break;
                        }
                    } catch (InterruptedException interrupted) {
                        stderr("InterruptedException: " + interrupted);
                        throw interrupted;
                    }
                }
            } catch (final IOException io) {
                stderr("IOException: " + io);
            }
        } catch (InterruptedException e) {
            _process.destroy();
            _updateProgressBar(0);
            return "Interrupted"; // SwingWorker.get() returns this
        }

        return "All Done"; // or this
    }

    /** Update the progress bar.  In this base class, do nothing.
     *  @i The current location of the progress bar.
     */
    private void _updateProgressBar(final int i) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // Private class that reads a stream in a thread and updates the
    // JTextArea.
    private class _StreamReaderThread extends Thread {
        _StreamReaderThread(InputStream inputStream, StreamExec streamExec) {
            _inputStream = inputStream;
            _streamExec = streamExec;
        }

        // Read lines from the _inputStream and output them.
        public void run() {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        _inputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    _streamExec.stdout( /*_streamType + ">" +*/
                    line);
                }
            } catch (IOException ioe) {
                _streamExec.stderr("IOException: " + ioe);
            }
        }

        // Stream to read from.
        private InputStream _inputStream;

        private StreamExec _streamExec;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of command to be executed.  Each entry in the list is
     *  a String.  It might be better to have each element of the list
     *  be an String [] so that the shell can interpret each word in
     *  the command.
     */
    private List _commands;

    /** The Process that we are running. */
    private Process _process;

    /** The return code of the last Runtime.exec() command. */
    private int _subprocessReturnCode;

    /** The working directory of the subprocess.  If null, then 
     *  the subprocess is executed in the working directory of the current
     *  process.
     */
    private File _workingDirectory;
}

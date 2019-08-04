/* Run a list of commands.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Execute commands in a subprocess and send the results to stderr and stdout.
 <p>As an alternative to this class, see
 {@link ptolemy.gui.JTextAreaExec}, which uses Swing; and
 {@link ptolemy.util.StringBufferExec}, which writes to a StringBuffer.

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
 <a href="http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html">http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html</a>
 <p>See also
 <a href="http://developer.java.sun.com/developer/qow/archive/135/index.jsp">http://developer.java.sun.com/developer/qow/archive/135/index.jsp</a> <i>(1/11: Broken)</i>
 and
 <a href="http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html">http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a>.</p>

 @see ptolemy.gui.JTextAreaExec
 @see ptolemy.util.StringBufferExec

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 5.2
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

    /** Append to the path of the subprocess.  If directoryName is already
     *  in the path, then it is not appended.
     *  @param directoryName The name of the directory to append to the path.
     */
    @Override
    public void appendToPath(String directoryName) {
        // FIXME: Code Duplication from JTextAreaExec.java
        if (_debug) {
            stdout("StreamExec.appendToPath(): " + directoryName + "\n");
        }

        // Might be Path, might be PATH
        String keyPath = "PATH";
        String path = getenv(keyPath);
        if (path == null) {
            path = getenv("Path");
            if (path != null) {
                keyPath = "Path";
            }
            if (_debug) {
                stdout("StreamExec.appendToPath() Path: " + path + "\n");
            }
        } else {
            if (_debug) {
                stdout("StreamExec.appendToPath() PATH: " + path + "\n");
            }
        }

        if (path == null
                || path.indexOf(File.pathSeparatorChar + directoryName
                        + File.pathSeparatorChar) == -1) {
            if (_debug) {
                stdout("StreamExec.appendToPath() updating\n");
            }
            _envp = StreamExec.updateEnvironment(keyPath,
                    File.pathSeparatorChar + directoryName
                    + File.pathSeparatorChar);

            if (_debug) {
                // For debugging
                for (String element : _envp) {
                    stdout("StreamExec.appendToPath() " + element);
                }
            }
        }
    }

    /** Cancel any running commands. */
    @Override
    public void cancel() {
        //_worker.interrupt();
        if (_process != null) {
            _process.destroy();
        }
    }

    /** Clear the text area, status bar and progress bar. */
    @Override
    public void clear() {
        updateStatusBar("");
        _updateProgressBar(0);
    }

    /** Get the value of the environment of the subprocess.
     *  @param key The key for which to search.
     *  @return The value of the key.  If the key is not set, then
     *  null is returned.  If appendToPath() has been called, and
     *  the then the environment for the subprocess is checked, which
     *  might be different than the environment for the current process
     *  because appendToPath() was called.  Note that that key is searched
     *  for in a case-insensitive mode.
     */
    @Override
    public String getenv(String key) {
        // FIXME: Code Duplication from JTextAreaExec.java
        if (_envp == null) {
            // Sigh.  System.getEnv("PATH") and System.getEnv("Path")
            // will return the same thing, even though the variable
            // is Path.  Updating PATH is wrong, the subprocess will
            // not see the change.  So, we search the env for a direct
            // match
            Map<String, String> environmentMap = System.getenv();
            return environmentMap.get(key);
        }
        for (String element : _envp) {
            if (key.regionMatches(false /*ignoreCase*/, 0, element, 0,
                    key.length())) {
                return element.substring(key.length() + 1, element.length());
            }
        }
        return null;
    }

    /** Return the value of the pattern log.
     *  <p>Calling this method resets the log of previously received
     *  matches.</p>
     *  @return Any strings sent that match the value of the pattern.
     *  The matches for stdout are returned first, then the matches
     *  for stdout.
     *  @see #setPattern(String)
     *  @see #stdout(String)
     */
    public String getPatternLog() {
        String patternOutLog = _patternOutLog.toString();
        String patternErrorLog = _patternErrorLog.toString();
        _patternOutLog = new StringBuffer();
        _patternErrorLog = new StringBuffer();
        return patternOutLog + patternErrorLog;
    }

    /** Return the return code of the last subprocess that was executed.
     *  @return the return code of the last subprocess that was executed.
     */
    @Override
    public int getLastSubprocessReturnCode() {
        return _subprocessReturnCode;
    }

    /** Set the list of commands.
     *  @param commands A list of Strings, where each element is a command.
     */
    @Override
    public void setCommands(List commands) {
        _commands = commands;
    }

    /** Set the pattern that is used to search data sent to stdout.
     *  <p>If the value of the pattern argument is non-null, then
     *  each time {@link #stdout(String)} is called, the value of
     *  the argument to stdout is compared with the pattern
     *  regex.  If there is a match, then the value is appended
     *  to a StringBuffer that whose value may be obtained with
     *  the {@link #getPatternLog()} method.</p>
     *  <p>Calling this method resets the log of previously received
     *  matches.</p>
     *  @param pattern The pattern used.
     *  @see #getPatternLog()
     */
    public void setPattern(String pattern) {
        _pattern = Pattern.compile(pattern);
        _patternOutLog = new StringBuffer();
        _patternErrorLog = new StringBuffer();
    }

    /** Determine whether the last subprocess is waited for or not.
     *  @param waitForLastSubprocess True if the {@link #start()}
     *  method should wait for the last subprocess to finish.
     */
    public void setWaitForLastSubprocess(boolean waitForLastSubprocess) {
        _waitForLastSubprocess = waitForLastSubprocess;
    }

    /** Set the working directory of the subprocess.
     *  @param workingDirectory The working directory of the
     *  subprocess.  If this argument is null, then the subprocess is
     *  executed in the working directory of the current process.
     */
    @Override
    public void setWorkingDirectory(File workingDirectory) {
        _workingDirectory = workingDirectory;
    }

    /** Start running the commands.
     *  By default, the start() method returns after the last subprocess
     *  finishes. See {@link #setWaitForLastSubprocess(boolean)}.
     */
    @Override
    public void start() {
        String returnValue = _executeCommands();
        updateStatusBar(returnValue);
        stdout(returnValue);
    }

    /** Append the text message to stderr.  A derived class could
     *  append to a StringBuffer.  {@link ptolemy.gui.JTextAreaExec} appends to a
     *  JTextArea. The output automatically gets a trailing newline
     *  appended.
     *  @param text The text to append to standard error.
     */
    @Override
    public void stderr(final String text) {
        if (_pattern != null && _pattern.matcher(text).matches()) {
            _patternErrorLog.append(text + _eol);
        }
        System.err.println(text);
        System.err.flush();
    }

    /** Append the text message to the output.  A derived class could
     *  append to a StringBuffer.  {@link ptolemy.gui.JTextAreaExec} appends to a
     *  JTextArea.
     *  The output automatically gets a trailing newline appended.
     *  <p>If {@link #setPattern(String)} has been called with a
     *  non-null argument, then any text that matches the pattern
     *  regex will be appended to a log file.  The log file
     *  may be read with {@link #getPatternLog()}.</p>
     *  @param text The text to append to standard out.
     */
    @Override
    public void stdout(final String text) {
        if (_pattern != null && _pattern.matcher(text).matches()) {
            _patternOutLog.append(text + _eol);
        }
        System.out.println(text);
        System.out.flush();
    }

    /** Update the environment and return the results.
     *  Read the environment for the current process, append the value
     *  of the value parameter to the environment variable named by
     *  the key parameter.
     *  @param key The environment variable to be updated.
     *  @param value The value to append
     *  @return An array of strings that consists of the subprocess
     *  environment variable names and values in the form
     *  <code>name=value</code> with the environment variable
     *  named by the key parameter updated to include the value
     *  of the value parameter.
     */
    public static String[] updateEnvironment(String key, String value) {
        // This is static so that we can share it among
        // ptolemy.util.StreamExec
        // StringBufferExec, which extends Stream Exec
        // and
        // ptolemy.gui.JTextAreaExec, which extends JPanel

        Map<String, String> env = new HashMap(System.getenv());

        env.put(key, value + env.get(key));
        String[] envp = new String[env.size()];

        int i = 0;
        Iterator entries = env.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            envp[i++] = entry.getKey() + "=" + entry.getValue();
            // System.out.println("StreamExec(): " + envp[i-1]);
        }
        return envp;
    }

    /** Set the text of the status bar.  In this base class, do
     *  nothing, derived classes may update a status bar.
     *  @param text The text with which the status bar is updated.
     */
    @Override
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
                                + "\", about to execute:");
                    } else {
                        stdout("About to execute:");
                    }

                    StringBuffer commandBuffer = new StringBuffer();
                    StringBuffer statusCommand = new StringBuffer();
                    for (String commandToken : commandTokens) {
                        if (commandBuffer.length() > 1) {
                            commandBuffer.append(" ");
                        }
                        commandBuffer.append(commandToken);

                        // Accumulate the first 50 chars for use in
                        // the status buffer.
                        if (statusCommand.length() < 50) {
                            if (statusCommand.length() > 0) {
                                statusCommand.append(" ");
                            }

                            statusCommand.append(commandToken);
                        }
                    }
                    stdout("    "
                            + StringUtilities.split(commandBuffer.toString()));
                    if (statusCommand.length() >= 50) {
                        statusCommand.append(" . . .");
                    }

                    updateStatusBar("Executing: " + statusCommand.toString());

                    // If _envp is null, then no environment changes.
                    _process = runtime.exec(commandTokens, _envp,
                            _workingDirectory);

                    // Set up a Thread to read in any error messages
                    _StreamReaderThread errorGobbler = new _StreamReaderThread(
                            _process.getErrorStream(), this);

                    // Set up a Thread to read in any output messages
                    _StreamReaderThread outputGobbler = new _StreamReaderThread(
                            _process.getInputStream(), this);

                    // Start up the Threads
                    errorGobbler.start();
                    outputGobbler.start();

                    if (commands.hasNext() || _waitForLastSubprocess) {
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
     *  @param i The current location of the progress bar.
     */
    private void _updateProgressBar(final int i) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    /** Private class that reads a stream in a thread and updates the
     *  the StreamExec.
     */
    private static class _StreamReaderThread extends Thread {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct a StreamReaderThread.
         *  @param inputStream the stream from which to read.
         *  @param streamExec The StreamExec to be written.
         */
        _StreamReaderThread(InputStream inputStream, StreamExec streamExec) {
            _inputStream = inputStream;
            _streamExec = streamExec;
        }

        /** Read lines from the _inputStream and output them. */
        @Override
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

        /** Stream from which to read. */
        private InputStream _inputStream;

        /** StreamExec which is written. */
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

    private final boolean _debug = false;

    /** End of line character. */
    private static final String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** The environment, which is an array of Strings of the form
     *  <code>name=value</code>.  If this variable is null, then
     *  the environment of the calling process is used.
     */
    private String[] _envp;

    /** The regex pattern used to match against the output of
     *  the subprocess.
     */
    private Pattern _pattern;

    /** The StringBuffer that contains matches to calls to stdout(). */
    private StringBuffer _patternErrorLog;

    /** The StringBuffer that contains matches to calls to stderr(). */
    private StringBuffer _patternOutLog;

    /** The Process that we are running. */
    private Process _process;

    /** The return code of the last Runtime.exec() command. */
    private int _subprocessReturnCode;

    /** True if we should wait for the last subprocess. */
    private boolean _waitForLastSubprocess = true;

    /** The working directory of the subprocess.  If null, then
     *  the subprocess is executed in the working directory of the current
     *  process.
     */
    private File _workingDirectory;
}

/* A JTextArea that takes a list of commands and runs them.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;

import ptolemy.util.ExecuteCommands;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

/** Execute commands in a subprocess and display them in a JTextArea.

 <p>As an alternative to this class, see
 {@link ptolemy.util.StringBufferExec}, which writes to a StringBuffer,
 and
 {@link ptolemy.util.StreamExec}, which writes to stderr and stdout.

 <p>Loosely based on Example1.java from
 <a href="http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html">http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html</a>
 <p>See also
 <a href="http://developer.java.sun.com/developer/qow/archive/135/index.jsp">http://developer.java.sun.com/developer/qow/archive/135/index.jsp</a> <i>(1/11: Broken)</i>
 and
 <a href="http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html">http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a>.</p>

 @see ptolemy.util.StringBufferExec
 @see ptolemy.util.StreamExec

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)

 */
@SuppressWarnings("serial")
public class JTextAreaExec extends JPanel implements ExecuteCommands {
    /** Create the JTextArea, progress bar, status text field and
     *  optionally Start, Cancel and Clear buttons.
     *
     *  @param name A String containing the name to label the JTextArea
     *  with.
     *  @param showButtons True if the Start, Cancel and Clear buttons
     *  should be  made visible.
     */
    public JTextAreaExec(String name, boolean showButtons) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        _jTextArea = new JTextArea("", 20, 100);
        _jTextArea.setEditable(false);

        JScrollPane jScrollPane = new JScrollPane(_jTextArea);
        add(jScrollPane);

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black), name));

        _progressBar = new JProgressBar();

        _startButton = new JButton("Start");
        _startButton.addActionListener(_startListener);
        _enableStartButton();

        _cancelButton = new JButton("Cancel");
        _cancelButton.addActionListener(_interruptListener);
        _cancelButton.setEnabled(false);

        _clearButton = new JButton("Clear");
        _clearButton.addActionListener(_clearListener);
        _clearButton.setEnabled(true);

        Border spaceBelow = BorderFactory.createEmptyBorder(0, 0, 5, 0);

        if (showButtons) {
            JComponent buttonBox = new JPanel();
            buttonBox.add(_startButton);
            buttonBox.add(_cancelButton);
            buttonBox.add(_clearButton);

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(buttonBox);
            buttonBox.setBorder(spaceBelow);
            _statusBar = new JLabel("Click Start to begin",
                    SwingConstants.CENTER);
        } else {
            _statusBar = new JLabel("", SwingConstants.CENTER);
        }

        add(_progressBar);
        add(_statusBar);
        _statusBar.setAlignmentX(CENTER_ALIGNMENT);

        Border progressBarBorder = _progressBar.getBorder();
        _progressBar.setBorder(BorderFactory.createCompoundBorder(spaceBelow,
                progressBarBorder));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the text message to the JTextArea and include a trailing
     *  newline.
     *  @param text The text message to be appended.
     */
    public void appendJTextArea(final String text) {
        Runnable doAppendJTextArea = new Runnable() {
            @Override
            public void run() {
                // Oddly, we can just use '\n' here,
                // we do not need to call
                // System.getProperties("line.separator")
                _jTextArea.append(text + '\n');

                // Scroll down as we generate text.
                _jTextArea.setCaretPosition(_jTextArea.getText().length());
            }
        };

        SwingUtilities.invokeLater(doAppendJTextArea);
    }

    /** Append to the path of the subprocess.  If directoryName is already
     *  in the path, then it is not appended.
     *  @param directoryName The name of the directory to append to the path.
     */
    @Override
    public void appendToPath(String directoryName) {
        if (_debug) {
            stdout("JTextArea.appendToPath(): " + directoryName + "\n");
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
                stdout("JTextArea.appendToPath() Path: " + path + "\n");
            }
        } else {
            if (_debug) {
                stdout("JTextArea.appendToPath() PATH: " + path + "\n");
            }
        }

        if (path == null
                || path.indexOf(File.pathSeparatorChar + directoryName
                        + File.pathSeparatorChar) == -1) {
            if (_debug) {
                stdout("JTextArea.appendToPath() updating\n");
            }
            _envp = StreamExec.updateEnvironment(keyPath,
                    File.pathSeparatorChar + directoryName
                    + File.pathSeparatorChar);

            if (_debug) {
                // For debugging
                for (String element : _envp) {
                    stdout("JTextArea.appendToPath() " + element);
                }
            }
        }
    }

    /** Cancel any running commands. */
    @Override
    public void cancel() {
        _cancelButton.doClick();
    }

    /** Clear the text area, status bar and progress bar. */
    @Override
    public void clear() {
        _clearButton.doClick();
        updateStatusBar("");
        _updateProgressBar(0);
    }

    /** Get the value of the environment of the subprocess.
     *  @param key  The environment variable.
     *  @return The value of the key.  If the key is not set, then
     *  null is returned.  If appendToPath() has been called, and
     *  the then the environment for the subprocess is checked, which
     *  might be different than the environment for the current process
     *  because appendToPath() was called.  Note that that key is searched
     *  for in a case-insensitive mode.
     */
    @Override
    public String getenv(String key) {
        // FIXME: Code Duplication from StreamExec.java
        if (_envp == null) {

            // Sigh.  System.getEnv("PATH") and System.getEnv("Path")
            // will return the same thing, even though the variable
            // is Path.  Updating PATH is wrong, the subprocess will
            // not see the change.  So, we search the env for a direct
            // match
            Map<String, String> environmentMap = System.getenv();

            if (_debug) {
                stdout("JTextArea.getenv(" + key + "), _envp null, returning: "
                        + environmentMap.get(key));
            }

            return environmentMap.get(key);
        }
        for (String element : _envp) {
            String envpKey = element.substring(0, element.indexOf("="));
            if (key.length() == envpKey.length()
                    && key.regionMatches(false /*ignoreCase*/, 0, envpKey, 0,
                            envpKey.length())) {

                if (_debug) {
                    stdout("JTextArea.getenv("
                            + key
                            + "), \""
                            + envpKey
                            + "\"\n\t_envp not null, returning: "
                            + element.substring(key.length() + 1,
                                    element.length()));
                }
                return element.substring(key.length() + 1, element.length());
            }
        }
        return null;
    }

    /** Return the return code of the last subprocess that was executed.
     *  @return the return code of the last subprocess that was executed.
     */
    @Override
    public int getLastSubprocessReturnCode() {
        return _subprocessReturnCode;
    }

    /** Return the Start button.
     *  This method is used to get the Start button so we can
     *  set the focus to it.
     *  @return the Start button.
     */
    public JButton getStartButton() {
        return _startButton;
    }

    /** Main method used for testing.
     *  To run a simple test, use:
     *  <pre>
     *        java -classpath $PTII ptolemy.gui.JTextAreaExec
     *  </pre>
     *  @param args The command line arguments, currently ignored.
     */
    public static void main(String[] args) {
        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                @Override
                public void run() {
                    try {
                        JFrame jFrame = new JFrame("JTextAreaExec Example");
                        WindowListener windowListener = new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                StringUtilities.exit(0);
                            }
                        };

                        jFrame.addWindowListener(windowListener);

                        List execCommands = new LinkedList();
                        execCommands.add("date");
                        execCommands.add("sleep 5");
                        execCommands.add("date");
                        execCommands.add("javac");

                        final JTextAreaExec exec = new JTextAreaExec(
                                "JTextAreaExec Tester", true);
                        exec.setCommands(execCommands);
                        jFrame.getContentPane().add(exec);
                        jFrame.pack();
                        jFrame.setVisible(true);

                        exec.getStartButton().requestFocus();
                        exec.start();
                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                        ex.printStackTrace();
                    }
                }
            };
            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }
    }

    /** Set the list of commands.
     *  @param commands a List of Strings, where each element is a command.
     */
    @Override
    public void setCommands(List commands) {
        _commands = commands;
        _enableStartButton();
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

    /** Start running the commands. */
    @Override
    public void start() {
        _startButton.doClick();
    }

    /** Append the text message to stderr.
     *  The output automatically gets a trailing newline appended.
     *  @param text The text to append to standard error.
     */
    @Override
    public void stderr(final String text) {
        appendJTextArea(text);
    }

    /** Append the text message to the output.
     *  The output automatically gets a trailing newline appended.
     *  @param text The text to append to standard out.
     */
    @Override
    public void stdout(final String text) {
        appendJTextArea(text);
    }

    /** Update the status area with the text message.
     *  @param text The text with which the status area is updated.
     */
    @Override
    public void updateStatusBar(final String text) {
        Runnable doUpdateStatusBar = new Runnable() {
            @Override
            public void run() {
                _statusBar.setText(text);
                _jTextArea.append(text + '\n');
            }
        };

        SwingUtilities.invokeLater(doUpdateStatusBar);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Enable the Start button if there are any commands in the list.
    private void _enableStartButton() {
        if (_commands != null && _commands.size() > 0) {
            _startButton.setEnabled(true);
        } else {
            _startButton.setEnabled(false);
        }
    }

    // Execute the commands in the list.  Update the JTextArea with
    // the command being run and the output.  Update the progress bar
    // and the status bar.
    private Object _executeCommands() {
        // FIXME: Exec, KeyStoreActor, JTextAreaExec have duplicate code
        try {
            Runtime runtime = Runtime.getRuntime();

            try {
                if (_process != null) {
                    _process.destroy();
                }

                _progressBar.setMaximum(_commands.size());

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

                    if (commandTokens.length < 1) {
                        stdout("Warning, an empty string was passed as a command.");
                        continue;
                    }
                    stdout("In \"" + _workingDirectory
                            + "\", about to execute:\n");

                    StringBuffer statusCommand = new StringBuffer();

                    for (String commandToken : commandTokens) {
                        appendJTextArea("        " + commandToken);

                        // Accumulate the first 50 chars for use in
                        // the status buffer.
                        if (statusCommand.length() < 50) {
                            if (statusCommand.length() > 0) {
                                statusCommand.append(" ");
                            }

                            statusCommand.append(commandToken);
                        }
                    }

                    if (statusCommand.length() >= 50) {
                        statusCommand.append(" . . .");
                    }

                    _statusBar
                    .setText("Executing: " + statusCommand.toString());

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

                    try {
                        _subprocessReturnCode = _process.waitFor();

                        if (_subprocessReturnCode != 0) {
                            // FIXME: If we get a segfault in the subprocess, then it
                            // would be nice to get the error in the display.  However,
                            // there is no data to be found in the process error stream?
                            appendJTextArea("Warning, process returned "
                                    + _subprocessReturnCode);

                            synchronized (this) {
                                _process = null;
                            }
                            break;
                        }

                        synchronized (this) {
                            _process = null;
                        }

                    } catch (InterruptedException interrupted) {
                        appendJTextArea("InterruptedException: " + interrupted);
                        throw interrupted;
                    }
                }

                appendJTextArea("All Done.");
            } catch (final IOException io) {
                appendJTextArea("IOException: " + io);
            }
        } catch (InterruptedException e) {
            _process.destroy();
            _updateProgressBar(0);
            return "Interrupted"; // SwingWorker.get() returns this
        }

        return "All Done"; // or this
    }

    // This action listener, called by the Clear button, clears
    // the text area
    private ActionListener _clearListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            Runnable doAppendJTextArea = new Runnable() {
                @Override
                public void run() {
                    _jTextArea.setText(null);
                }
            };

            SwingUtilities.invokeLater(doAppendJTextArea);
        }
    };

    // This action listener, called by the Cancel button, interrupts
    // the _worker thread which is running this._executeCommands().
    // Note that the _executeCommands() method handles
    // InterruptedExceptions cleanly.
    private ActionListener _interruptListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            _cancelButton.setEnabled(false);
            appendJTextArea("Cancel button was pressed");
            _worker.cancel(true);
            _process.destroy();
            _enableStartButton();
        }
    };

    // This action listener, called by the Start button, effectively
    // forks the thread that does the work.
    private ActionListener _startListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent event) {
            _startButton.setEnabled(false);
            _cancelButton.setEnabled(true);
            _statusBar.setText("Working...");

            _worker = new SwingWorker<Object, Void>() {
                @Override
                public Object doInBackground() {
                    return _executeCommands();
                }

                @Override
                public void done() {
                    _enableStartButton();
                    _cancelButton.setEnabled(false);
                    _updateProgressBar(0);
                    try {
                        _statusBar.setText(get(1, TimeUnit.SECONDS).toString());
                    } catch (CancellationException ex) {
                        _statusBar.setText("Cancelled.");
                    } catch (ExecutionException ex1) {
                        _statusBar
                        .setText("The computation threw an exception: "
                                + ex1.getCause());
                    } catch (InterruptedException ex2) {
                        _statusBar
                        .setText("The worker thread was interrupted while waiting, which is probably not a problem.");
                    } catch (TimeoutException ex3) {
                        _statusBar
                        .setText("The wait to get the execution result timed out, which is unusual, but probably not a problem.");
                    }
                }
            };
            _worker.execute();
        }
    };

    // When the _worker needs to update the GUI we do so by queuing a
    // Runnable for the event dispatching thread with
    // SwingUtilities.invokeLater().  In this case we're just changing
    // the value of the progress bar.
    private void _updateProgressBar(final int i) {
        Runnable doSetProgressBarValue = new Runnable() {
            @Override
            public void run() {
                //_jTextArea.append(Integer.valueOf(i).toString());
                _progressBar.setValue(i);
            }
        };

        SwingUtilities.invokeLater(doSetProgressBarValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // Private class that reads a stream in a thread and updates the
    // JTextArea.
    private static class _StreamReaderThread extends Thread {
        _StreamReaderThread(InputStream inputStream, JTextAreaExec jTextAreaExec) {
            _inputStream = inputStream;
            _jTextAreaExec = jTextAreaExec;
        }

        // Read lines from the _inputStream and output them to the
        // JTextArea.
        @Override
        public void run() {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        _inputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    _jTextAreaExec.appendJTextArea(line);
                }
            } catch (IOException ioe) {
                _jTextAreaExec.appendJTextArea("IOException: " + ioe);
            }
        }

        // Stream to read from.
        private InputStream _inputStream;

        // Description of the Stream that we print, usually "OUTPUT" or "ERROR"
        //private String _streamType;

        // JTextArea to update
        private JTextAreaExec _jTextAreaExec;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Cancel Button. */
    private JButton _cancelButton;

    /** The Clear Button. */
    private JButton _clearButton;

    /** The list of command to be executed.  Each entry in the list is
     * a String.  It might be better to have each element of the list
     * be an String [] so that the shell can interpret each word in
     * the command.
     */
    private List _commands = null;

    private final boolean _debug = false;

    /** The environment, which is an array of Strings of the form
     *  <code>name=value</code>.  If this variable is null, then
     *  the environment of the calling process is used.
     */
    private String[] _envp;

    /** JTextArea to write the command and the output of the command. */
    private JTextArea _jTextArea;

    /** The Process that we are running. */
    private Process _process;

    /** The return code of the last Runtime.exec() command. */
    private int _subprocessReturnCode;

    /** Progress bar where the length of the bar is the total number
     * of commands being run.
     */
    private JProgressBar _progressBar;

    /** Label at the bottom that provides feedback as to what is happening. */
    private JLabel _statusBar;

    /** The Start Button. */
    private JButton _startButton;

    /** SwingWorker that actually does the work. */
    private SwingWorker _worker;

    /** The working directory of the subprocess.  If null, then
     *  the subprocess is executed in the working directory of the current
     *  process.
     */
    private File _workingDirectory;
}

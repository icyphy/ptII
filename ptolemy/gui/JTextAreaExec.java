/* A JTextArea that takes a list of commands and runs them.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.gui;

import ptolemy.util.StringUtilities;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/** Execute commands in a subprocess and display them in a JTextArea.

<p>Loosely based on Example1.java from
http://java.sun.com/products/jfc/tsc/articles/threads/threads2.html
<p>See also
http://developer.java.sun.com/developer/qow/archive/135/index.jsp
and
http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
 */
public class JTextAreaExec extends JPanel {

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
                BorderFactory.createLineBorder(Color.black),
                name));

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
            _statusBar = new JLabel("Click Start to begin", JLabel.CENTER);
        } else {
            _statusBar = new JLabel("", JLabel.CENTER);
        }

        add(_progressBar);
        add(_statusBar);
        _statusBar.setAlignmentX(CENTER_ALIGNMENT);

        Border progressBarBorder = _progressBar.getBorder();
        _progressBar.setBorder(BorderFactory.createCompoundBorder(
                spaceBelow,
                progressBarBorder));
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append the text message to the JTextArea and include a trailing
     *  newline.
     */
    public void appendJTextArea(final String text) {
        Runnable doAppendJTextArea = new Runnable() {
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

    /** Cancel any running commands. */
    public void cancel() {
        _cancelButton.doClick();
    }

    /** Clear the text area, status bar and progress bar. */
    public void clear() {
        _clearButton.doClick();
        updateStatusBar("");
        _updateProgressBar(0);
    }

    /** Main method used for testing.
     *  To run a simple test, use:
     *  <pre>
     *        java -classpath $PTII ptolemy.gui.JTextAreaExec
     *  </pre>
     */
    public static void main(String [] args) {
        JFrame jFrame = new JFrame("JTextAreaExec Example");
        WindowListener windowListener = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            };
        jFrame.addWindowListener(windowListener);

        List execCommands = new LinkedList();
        execCommands.add("date");
        execCommands.add("sleep 5");
        execCommands.add("date");
        execCommands.add("javac");

        final JTextAreaExec exec =
            new JTextAreaExec("JTextAreaExec Tester", true);
        exec.setCommands(execCommands);
        jFrame.getContentPane().add(exec);
        jFrame.pack();
        jFrame.show();

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    //We can't do this until now
                    exec.getStartButton().requestFocus();
                    exec.start();
                }
            });

    }

    /** Return the Start button.
     *  This method is used to get the Start button so we can
     *  set the focus to it.
     */
    public JButton getStartButton() {

        return _startButton;
    }

    /** Set the list of commands. */
    public void setCommands(List commands) {
        _commands = commands;
        _enableStartButton();
    }

    /** Start running the commands. */
    public void start() {
        _startButton.doClick();
    }

    /** Update the status area with the text message.*/
    public void updateStatusBar(final String text) {
        Runnable doUpdateStatusBar = new Runnable() {
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
        try {
            Runtime runtime = Runtime.getRuntime();
            try {

                if (_process != null) _process.destroy();
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
                    final String [] commandTokens =
                        StringUtilities
                        .tokenizeForExec((String)commands.next());

                    appendJTextArea("About to execute:\n");
                    StringBuffer statusCommand = new StringBuffer();
                    for (int i = 0; i < commandTokens.length; i++) {
                        appendJTextArea("        " + commandTokens[i]);

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
                    _statusBar.setText("Executing: "
                            + statusCommand.toString());

                    _process = runtime.exec(commandTokens);

                    // Set up a Thread to read in any error messages
                    _StreamReaderThread errorGobbler = new
                        _StreamReaderThread(_process.getErrorStream(),
                                "ERROR", this);

                    // Set up a Thread to read in any output messages
                    _StreamReaderThread outputGobbler = new
                        _StreamReaderThread(_process.getInputStream(),
                                "OUTPUT", this);

                    // Start up the Threads
                    errorGobbler.start();
                    outputGobbler.start();


                    try {
                        int processReturnCode = _process.waitFor();
                        synchronized(this) {
                            _process = null;
                        }
                        if (processReturnCode != 0) break;
                    } catch (InterruptedException interrupted) {
                        appendJTextArea("InterruptedException: "
                                + interrupted);
                        throw interrupted;
                    }
                }
                appendJTextArea("All Done.");
            } catch (final IOException io) {
                appendJTextArea("IOException: " + io);
            }
        }
        catch (InterruptedException e) {
            _process.destroy();
            _updateProgressBar(0);
            return "Interrupted";  // SwingWorker.get() returns this
        }
        return "All Done";         // or this
    }

    // This action listener, called by the Clear button, clears
    // the text area
    private ActionListener _clearListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Runnable doAppendJTextArea = new Runnable() {
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
            public void actionPerformed(ActionEvent event) {
                _cancelButton.setEnabled(false);
                appendJTextArea("Cancel button was pressed");
                _worker.interrupt();
                _process.destroy();
                _enableStartButton();
            }
        };

    // This action listener, called by the Start button, effectively
    // forks the thread that does the work.
    private ActionListener _startListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                _startButton.setEnabled(false);
                _cancelButton.setEnabled(true);
                _statusBar.setText("Working...");

                /* Invoking start() on the SwingWorker causes a new Thread
                 * to be created that will call construct(), and then
                 * finished().  Note that finished() is called even if
                 * the _worker is interrupted because we catch the
                 * InterruptedException in _executeCommands().
                 */
                _worker = new SwingWorker() {
                        public Object construct() {
                            return _executeCommands();
                        }
                        public void finished() {
                            _enableStartButton();
                            _cancelButton.setEnabled(false);
                            _updateProgressBar(0);
                            _statusBar.setText(get().toString());
                        }
                    };
                _worker.start();
            }
        };


    // When the _worker needs to update the GUI we do so by queuing a
    // Runnable for the event dispatching thread with
    // SwingUtilities.invokeLater().  In this case we're just changing
    // the value of the progress bar.
    private void _updateProgressBar(final int i) {
        Runnable doSetProgressBarValue = new Runnable() {
                public void run() {
                    //_jTextArea.append(new Integer(i).toString());
                    _progressBar.setValue(i);
                }
            };
        SwingUtilities.invokeLater(doSetProgressBarValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Private class that reads a stream in a thread and updates the
    // JTextArea.
    private class _StreamReaderThread extends Thread {

        _StreamReaderThread(InputStream inputStream, String streamType,
                JTextAreaExec jTextAreaExec) {
            _inputStream = inputStream;
            _streamType = streamType;
            _jTextAreaExec = jTextAreaExec;
        }

        // Read lines from the _inputStream and output them to the
        // JTextArea.
        public void run() {
            try {
                InputStreamReader inputStreamReader =
                    new InputStreamReader(_inputStream);
                BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);
                String line = null;
                while ( (line = bufferedReader.readLine()) != null)
                    _jTextAreaExec.appendJTextArea(/*_streamType + ">" +*/ line);
            } catch (IOException ioe) {
                _jTextAreaExec.appendJTextArea("IOException: " + ioe);
            }
        }

        // Stream to read from.
        private InputStream _inputStream;
        // Description of the Stream that we print, usually "OUTPUT" or "ERROR"
        private String _streamType;
        // JTextArea to update
        private JTextAreaExec _jTextAreaExec;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Cancel Button.
    private JButton _cancelButton;

    // The Clear Button.
    private JButton _clearButton;

    // The list of command to be executed.  Each entry in the list is
    // a String.  It might be better to have each element of the list
    // be an String [] so that the shell can interpret each word in
    // the command.
    private List _commands = null;

    // JTextArea to write the command and the output of the command.
    private JTextArea _jTextArea;

    // The Process that we are running.
    private Process _process;

    // Progress bar where the length of the bar is the total number
    // of commands being run.
    private JProgressBar _progressBar;

    // Label at the bottom that provides feedback as to what is happening.
    private JLabel _statusBar;

    // The Start Button.
    private JButton _startButton;

    // SwingWorker that actually does the work.
    private SwingWorker _worker;
}


/* A tableau for evaluating Exec expression interactively.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.gui.exec;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// ExecShellTableau

/**
 A tableau that provides a Exec Shell for interacting with the Bash shell.

 @author Christopher Hylands and Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ExecShellTableau extends Tableau implements ShellInterpreter {
    /** Create a new tableau.
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public ExecShellTableau(ExecShellEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        frame = new ExecShellFrame(this);
        setFrame(frame);

        try {
            _interpreter = Runtime.getRuntime().exec("bash -i");
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to create Process");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    @Override
    public String evaluateCommand(String command) throws Exception {
        _executeCommand(command);

        // FIXME: this is _so_ wrong
        return "";
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True
     */
    @Override
    public boolean isCommandComplete(String command) {
        return true;
    }

    /** Append the text message to text area.
     *  The output automatically gets a trailing newline appended.
     *  @param text The test to be appended.
     */
    public void stderr( /*final*/String text) {
        frame.shellTextArea.appendJTextArea(text + "\n");
    }

    /** Append the text message to the text area.
     *  The output automatically gets a trailing newline appended.
     *  @param text The test to be appended.
     */
    public void stdout(final String text) {
        frame.shellTextArea.appendJTextArea(text + "\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The frame in which text is written. */
    public ExecShellFrame frame;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The Exec interpreter
    // FIXME: Perhaps the interpreter should be in its own thread?
    private Process _interpreter;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of ExecShellTableau.
     */
    @SuppressWarnings("serial")
    public static class ExecShellFrame extends TableauFrame {
        // FindBugs suggested refactoring this into a static class.

        /** Construct a frame to display the ExecShell window.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param execShellTableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public ExecShellFrame(ExecShellTableau execShellTableau)
                throws IllegalActionException, NameDuplicationException {
            super(execShellTableau);

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

            shellTextArea = new ShellTextArea();
            shellTextArea.setInterpreter(execShellTableau);
            shellTextArea.mainPrompt = "% ";
            component.add(shellTextArea);
            getContentPane().add(component, BorderLayout.CENTER);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public variables                  ////

        /** The text area tableau used for input and output. */
        public ShellTextArea shellTextArea;

        ///////////////////////////////////////////////////////////////////
        ////                         protected methods                 ////
        @Override
        protected void _help() {
            try {
                URL doc = getClass().getClassLoader().getResource(
                        "ptolemy/actor/gui/ptjacl/help.htm");
                getConfiguration().openModel(null, doc, doc.toExternalForm());
            } catch (Exception ex) {
                System.out.println("ExecShellTableau._help(): " + ex);
                _about();
            }
        }
    }

    /** A factory that creates a control panel to display a Exec Shell.
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a new instance of ExecShellTableau in the specified
         *  effigy. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *  @param effigy The model effigy.
         *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            // NOTE: Can create any number of tableaux within the same
            // effigy.  Is this what we want?
            if (effigy instanceof ExecShellEffigy) {
                return new ExecShellTableau((ExecShellEffigy) effigy,
                        "ExecShellTableau");
            } else {
                return null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Execute the command.  Update the output with
    // the command being run and the output.
    private String _executeCommand(String command) {
        if (command == null || command.length() == 0) {
            return "";
        }

        try {
            Runtime runtime = Runtime.getRuntime();

            try {
                //if (_process != null) {
                //    _process.destroy();
                //}
                // Preprocess by removing lines that begin with '#'
                // and converting substrings that begin and end
                // with double quotes into one array element.
                final String[] commandTokens = StringUtilities
                        .tokenizeForExec(command);

                //stdout("About to execute:\n");
                StringBuffer statusCommand = new StringBuffer();

                for (String commandToken : commandTokens) {
                    //stdout("        " + commandTokens[i]);
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

                //updateStatusBar("Executing: "
                //        + statusCommand.toString());
                _interpreter = runtime.exec(commandTokens);

                // Set up a Thread to read in any error messages
                _StreamReaderThread errorGobbler = new _StreamReaderThread(
                        _interpreter.getErrorStream(), this);

                // Set up a Thread to read in any output messages
                _StreamReaderThread outputGobbler = new _StreamReaderThread(
                        _interpreter.getInputStream(), this);

                // Start up the Threads
                errorGobbler.start();
                outputGobbler.start();

                try {
                    /*int processReturnCode = */_interpreter.waitFor();

                    synchronized (this) {
                        _interpreter = null;
                    }

                    //if (processReturnCode != 0) break;
                } catch (InterruptedException interrupted) {
                    stderr("InterruptedException: " + interrupted);
                    throw interrupted;
                }
            } catch (final IOException io) {
                stderr("IOException: "
                        + ptolemy.kernel.util.KernelException
                        .stackTraceToString(io));
            }
        } catch (InterruptedException e) {
            //_interpreter.destroy();
            return "Interrupted"; // SwingWorker.get() returns this
        }

        return "All Done"; // or this
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    // Private class that reads a stream in a thread and updates the
    // JTextArea.
    private static class _StreamReaderThread extends Thread {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        _StreamReaderThread(InputStream inputStream,
                ExecShellTableau execShellTableau) {
            _inputStream = inputStream;
            _execShellTableau = execShellTableau;
        }

        // Read lines from the _inputStream and output them.
        @Override
        public void run() {
            try {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        _inputStream);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    _execShellTableau.stdout( /*_streamType + ">" +*/
                            line);
                }
            } catch (IOException ioe) {
                _execShellTableau.stderr("IOException: " + ioe);
            }
        }

        // Stream to read from.
        private InputStream _inputStream;

        private ExecShellTableau _execShellTableau;
    }
}

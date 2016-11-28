/* A text area for shell-style interactions.

 Copyright (c) 1998-2016 The Regents of the University of California.
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
package ptolemy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// UserDialog

/**
 A panel with two text areas, one for user input, and one in which
 to display responses.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class UserDialog extends JPanel {
    /** Create a new instance with no initial message.
     */
    public UserDialog() {
        this(null);
    }

    /** Create a new instance with the specified initial message.
     *  @param initialMessage The initial message.
     */
    public UserDialog(String initialMessage) {
        // Graphics
        super(new BorderLayout());
        _initialMessage = initialMessage;

        // Create a one-line input text area.
        // FIXME: Size needs to be configurable.
        _userInputTextArea = new JTextArea("", 1, 80);
        // Event handling
        _userInputTextArea.addKeyListener(new ShellKeyListener());
        add(_userInputTextArea, BorderLayout.PAGE_START);

        // Now create the responses text area.
        // FIXME: Size needs to be configurable.
        _responseTextArea = new JTextArea("", 20, 80);
        _responseTextArea.setEditable(false);
        _jScrollPane = new JScrollPane(_responseTextArea);
        add(_jScrollPane, BorderLayout.PAGE_END);

        // Set the fonts.
        _userInputTextArea.setFont(new Font("Monospaced", 0, 14));
        _responseTextArea.setFont(new Font("Monospaced", 0, 14));

        _userInputTextArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black), ""));
        _responseTextArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black), ""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to output the first prompt.
     *  We need to do this here because we can't write to
     *  the TextArea until the peer has been created.
     */
    @Override
    public void addNotify() {
        super.addNotify();
        initialize(_initialMessage);
    }

    /** Append the specified text to the response text area with a newline at the end.
     *  The text will actually be appended in the swing thread, not immediately.
     *  This method immediately returns.
     *  @param text The text to append to the text area.
     */
    public void appendText(final String text) {
        Runnable doAppendJTextArea = new Runnable() {
            @Override
            public void run() {
                _responseTextArea.append(text + "\n");
                // Scroll down as we generate text.
                _responseTextArea.setCaretPosition(_responseTextArea.getText().length());
            }
        };
        SwingUtilities.invokeLater(doAppendJTextArea);
    }

    /** Get the interpreter that has been registered with setInterpreter().
     *  @return The interpreter, or null if none has been set.
     *  @see #setInterpreter(ShellInterpreter)
     */
    public ShellInterpreter getInterpreter() {
        return _interpreter;
    }

    /** Initialize the text area with the given starting message.
     *  This is just like appendText(), except that it clears the display first.
     *  @param initialMessage The initial message.
     */
    public void initialize(final String initialMessage) {
        if (_responseTextArea == null) {
            _initialMessage = initialMessage;
        } else {
            _initialMessage = null;
            Runnable doInitialize = new Runnable() {
                @Override
                public void run() {
                    _userInputTextArea.setText("");
                    _userInputTextArea.setCaretPosition(0);
                    if (initialMessage != null) {
                        _responseTextArea.setText(initialMessage + "\n");
                    } else {
                        _responseTextArea.setText("");
                    }
                }
            };
            SwingUtilities.invokeLater(doInitialize);
        }
    }

    /** Main method used for testing. To run a simple test, use:
     *  <pre>
     *        java -classpath $PTII ptolemy.gui.UserDialog
     *  </pre>
     *  @param args Currently ignored.
     */
    public static void main(String[] args) {
        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                @Override
                public void run() {
                    try {
                        JFrame jFrame = new JFrame("UserDialog Example");
                        WindowListener windowListener = new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                StringUtilities.exit(0);
                            }
                        };

                        jFrame.addWindowListener(windowListener);

                        final UserDialog exec = new UserDialog();
                        jFrame.getContentPane().add(exec);
                        jFrame.pack();
                        jFrame.setVisible(true);
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

    /** Set the interpreter.
     *  @param interpreter The interpreter.
     *  @see #getInterpreter()
     */
    public void setInterpreter(ShellInterpreter interpreter) {
        _interpreter = interpreter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Evaluate the command.
    // NOTE: This must be called in the swing event thread.
    private void _evalCommand() {
        String command = _userInputTextArea.getText();

        if (_interpreter != null) {
            // Process it.
            // Clear the command text area.
            _userInputTextArea.setText("");
            appendText(command);

            String result;

            try {
                result = _interpreter.evaluateCommand(command);
                if (result != null && !result.trim().equals("")) {
                    appendText(result);
                }
            } catch (Throwable e) {
                appendText("ERROR: " + e.toString());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The scroll pane containing the output text. */
    private JScrollPane _jScrollPane;

    // The initial message, if there is one.
    private String _initialMessage = null;

    // The interpreter.
    private ShellInterpreter _interpreter;

    // The TextArea widget for responses.
    private JTextArea _responseTextArea;

    // The TextArea widget for user input.
    private JTextArea _userInputTextArea;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // The key listener
    private class ShellKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent keyEvent) {
            // Process keys
            switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                // Consume the keypress so it is not displayed.
                keyEvent.consume();
                _evalCommand();
                break;
            }
        }
    }
}

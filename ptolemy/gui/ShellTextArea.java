/* A text area for shell-style interactions.

Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ShellTextArea

/**
A text area supporting shell-style interactions.

@author John Reekie, Christopher Hylands, Edward A. Lee
@version $Id$
*/
public class ShellTextArea extends JPanel {

    /** Create a new instance with no initial message.
     */
    public ShellTextArea () {
        this(null);
    }

    /** Create a new instance with the specified initial message.
     *  @param initialMessage The initial message.
     */
    public ShellTextArea (String initialMessage) {
        // Graphics
        super(new BorderLayout());
        _initialMessage = initialMessage;
        // FIXME: Size needs to be configurable.
        _jTextArea = new JTextArea("", 20, 80);
        // FIXME: Large font for demo. Font needs to be configurable.
        // _jTextArea.setFont(new Font("DialogInput", 0, 24));
        JScrollPane jScrollPane = new JScrollPane(_jTextArea);
        add(jScrollPane);

        setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createLineBorder(Color.black), ""));

        // Event handling
        _jTextArea.addKeyListener(new ShellKeyListener());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to output the first prompt.
     *  We need to do this here because we can't write to
     *  the TextArea until the peer has been created.
     */
    public void addNotify () {
        super.addNotify();
        initialize(_initialMessage);
    }

    /** Append the specified text to the JTextArea and
     *  update the prompt cursor.  The text will actually be appended
     *  in the swing thread, not immediately.  This method immediately
     *  returns.
     *  @param text The text to append to the text area.
     */
    public void appendJTextArea(final String text) {
        Runnable doAppendJTextArea = new Runnable() {
                public void run() {
                    _jTextArea.append(text);
                    // Scroll down as we generate text.
                    _jTextArea.setCaretPosition(_jTextArea.getText().length());
                    // To prevent _promptCursor from being
                    // updated before the JTextArea is actually updated,
                    // this needs to be inside the Runnable.
                    _promptCursor += text.length();
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

    /** Clear the JTextArea and reset the prompt cursor.
     *  The clearing is done in the swing thread, not immediately.
     *  This method immediately returns.
     */
    public void clearJTextArea() {
        Runnable doClearJTextArea = new Runnable() {
                public void run() {
                    _jTextArea.setText("");
                    _jTextArea.setCaretPosition(0);
                    _promptCursor = 0;
                }
            };
        SwingUtilities.invokeLater(doClearJTextArea);
    }

    /** Initialize the text area with the given starting message,
     *  followed by a prompt. If the argument is null or the empty
     *  string, then only a prompt is shown.
     *  @param initialMessage The initial message.
     */
    public void initialize(String initialMessage) {
        if (_jTextArea == null) {
            _initialMessage = initialMessage;
        } else {
            _initialMessage = null;
            clearJTextArea();
            if (initialMessage != null && !initialMessage.equals("")) {
                appendJTextArea(initialMessage + "\n" + mainPrompt);
            } else {
                appendJTextArea(mainPrompt);
            }
        }
    }

    /** Main method used for testing. To run a simple test, use:
     *  <pre>
     *        java -classpath $PTII ptolemy.gui.ShellTextArea
     *  </pre>
     */
    public static void main(String [] args) {
        JFrame jFrame = new JFrame("ShellTextArea Example");
        WindowListener windowListener = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            };
        jFrame.addWindowListener(windowListener);

        final ShellTextArea exec = new ShellTextArea();
        jFrame.getContentPane().add(exec);
        jFrame.pack();
        jFrame.show();
    }

    /** Replace a range in the JTextArea.
     */
    public void replaceRangeJTextArea(final String text,
            final int start,
            final int end) {
        Runnable doReplaceRangeJTextArea = new Runnable() {
                public void run() {
                    _jTextArea.replaceRange(text, start, end);
                }
            };
        SwingUtilities.invokeLater(doReplaceRangeJTextArea);
    }

    /** Return the result of a command evaluation.  This method is used
     *  when it is impractical to insist on the result being returned by
     *  evaluateCommand() of a ShellInterpreter.  For example, computing
     *  the result may take a while.
     *  @param result The result to return.
     */
    public void returnResult(final String result) {
        // Make the text area editable again.
        Runnable doMakeEditable = new Runnable() {
                public void run() {
                    setEditable(true);
                    String toPrint = result + "\n" + mainPrompt;
                    appendJTextArea(toPrint);
                }
            };
        SwingUtilities.invokeLater(doMakeEditable);
    }

    /** Set the associated text area editable (with a true argument)
     *  or not editable (with a false argument).  This should be called
     *  in the swing event thread.
     *  @param editable True to make the text area editable, false to
     *   make it uneditable.
     */
    public void setEditable(boolean editable) {
        _jTextArea.setEditable(editable);
    }

    /** Set the interpreter.
     *  @param interpreter The interpreter.
     *  @see #getInterpreter()
     */
    public void setInterpreter(ShellInterpreter interpreter) {
        _interpreter = interpreter;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Main prompt. */
    public String mainPrompt = ">> ";

    /** Prompt to use on continuation lines. */
    public String contPrompt = "";

    /** Size of the history to keep. */
    public int historyLength = 20;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Evaluate the command so far, if possible, printing
    // a continuation prompt if not.
    // NOTE: This must be called in the swing event thread.
    private void _evalCommand () {
        String newtext = _jTextArea.getText().substring(_promptCursor);
        _promptCursor += newtext.length();
        if (_commandBuffer.length() > 0) {
            _commandBuffer.append("\n");
        }
        _commandBuffer.append(newtext);
        String command = _commandBuffer.toString();

        if (_interpreter == null) {
            appendJTextArea("\n" + mainPrompt);
        } else {
            if (_interpreter.isCommandComplete(command)) {
                // Process it
                appendJTextArea("\n");
                Cursor oldCursor = _jTextArea.getCursor();
                _jTextArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                String result;
                try {
                    result = _interpreter.evaluateCommand(command);
                } catch (RuntimeException e) {
                    // RuntimeException are due to bugs in the expression
                    // evaluation code, so we make the stack trace available.
                    MessageHandler.error("Failed to evaluate expression", e);
                    result = "Internal error evaluating expression.";
                    throw e;
                } catch (Exception e) {
                    result = e.getMessage();
                    // NOTE: Not ideal here to print the stack trace, but
                    // if we don't, it will be invisible, which makes
                    // debugging hard.
                    // e.printStackTrace();
                }
                if (result != null) {
                    if (result.trim().equals("")) {
                        appendJTextArea(mainPrompt);
                    } else {
                        appendJTextArea(result + "\n" + mainPrompt);
                    }
                } else {
                    // Result is incomplete.
                    // Make the text uneditable to prevent further input
                    // until returnResult() is called.
                    // NOTE: We are assuming this called in the swing thread.
                    setEditable(false);
                }
                _commandBuffer.setLength(0);
                _jTextArea.setCursor(oldCursor);
                _updateHistory(command);
            } else {
                appendJTextArea("\n" + contPrompt);
            }
        }
    }

    // Replace the command with an entry from the history.
    private void _nextCommand () {
        String text;
        if (_historyCursor == 0) {
            text = "";
        } else {
            _historyCursor --;
            text = (String)_historyCommands.elementAt(
                    _historyCommands.size() - _historyCursor - 1);
        }
        replaceRangeJTextArea(
                text, _promptCursor, _jTextArea.getText().length());
    }

    // Replace the command with an entry from the history.
    private void _previousCommand () {
        String text;
        if (_historyCursor == _historyCommands.size()) {
            return;
        } else {
            _historyCursor ++;
            text = (String)_historyCommands.elementAt(
                    _historyCommands.size() - _historyCursor);
        }
        replaceRangeJTextArea(
                text, _promptCursor, _jTextArea.getText().length());
    }

    // Update the command history.
    private void _updateHistory (String command) {
        _historyCursor = 0;
        if (_historyCommands.size() == historyLength) {
            _historyCommands.removeElementAt(0);
        }
        _historyCommands.addElement(command);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The command input
    private StringBuffer _commandBuffer = new StringBuffer();

    // The TextArea widget for displaying commands and results
    private JTextArea _jTextArea;

    // Cursor, showing where last prompt or result ended.
    private int _promptCursor = 0;

    // History
    private int _historyCursor = 0;
    private Vector _historyCommands = new Vector();

    // The initial message, if there is one.
    private String _initialMessage = null;

    // The interpreter.
    private ShellInterpreter _interpreter;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // The key listener
    private class ShellKeyListener extends KeyAdapter {
        public void keyTyped (KeyEvent keyEvent) {
            switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_UNDEFINED:
                if (keyEvent.getKeyChar() == '\b') {
                    if (_jTextArea.getCaretPosition() == _promptCursor) {
                        keyEvent.consume(); // don't backspace over prompt!
                    }
                }
                break;

            case KeyEvent.VK_BACK_SPACE:
                if (_jTextArea.getCaretPosition() == _promptCursor) {
                    keyEvent.consume(); // don't backspace over prompt!
                }
                break;
            default:
            }
        }

        public void keyReleased (KeyEvent keyEvent) {
            switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_BACK_SPACE:
                if (_jTextArea.getCaretPosition() == _promptCursor) {
                    keyEvent.consume(); // don't backspace over prompt!
                }
                break;
            default:
            }
        }

        public void keyPressed (KeyEvent keyEvent) {
            if (!_jTextArea.isEditable()) {
                // NOTE: This doesn't seem to always work.
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            // Process keys
            switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                keyEvent.consume();
                _evalCommand();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (_jTextArea.getCaretPosition() <= _promptCursor) {
                    // FIXME: Consuming the event is useless...
                    // The backspace still occurs.  Why?  Java bug?
                    keyEvent.consume(); // don't backspace over prompt!
                }
                break;
            case KeyEvent.VK_LEFT:
                if (_jTextArea.getCaretPosition() == _promptCursor) {
                    keyEvent.consume();
                }
                break;
            case KeyEvent.VK_UP:
                _previousCommand();
                keyEvent.consume();
                break;
            case KeyEvent.VK_DOWN:
                _nextCommand();
                keyEvent.consume();
                break;
            case KeyEvent.VK_HOME:
                _jTextArea.setCaretPosition(_promptCursor);
                keyEvent.consume();
                break;
            default:
                switch (keyEvent.getModifiers()) {
                case InputEvent.CTRL_MASK:
                    switch (keyEvent.getKeyCode()) {
                    case KeyEvent.VK_A:
                        _jTextArea.setCaretPosition(_promptCursor);
                        keyEvent.consume();
                        break;
                    case KeyEvent.VK_N:
                        _nextCommand();
                        keyEvent.consume();
                        break;
                    case KeyEvent.VK_P:
                        _previousCommand();
                        keyEvent.consume();
                        break;
                    default:
                    }
                    break;
                default:
                    // Otherwise we got a regular character.
                    // Don't consume it, and TextArea will
                    // take care of displaying it.
                }
            }
        }
    }
}

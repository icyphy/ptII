/* JTextAreaTclShell: a simple Tcl shell for Jacl

Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.actor.gui.ptjacl;

import tcl.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/**
 A Simple Tcl Shell embedded in a JPanel.

 <p>FIXME: We should have a common base class that handles typing
 commands at an interpreter.

 @author John Reekie, Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 */
public class JTextAreaTclShell extends JPanel {

    /** Create a new Shell instance
     */
    public JTextAreaTclShell () {
	// Graphics
	super(new BorderLayout());
	_jTextArea = new JTextArea("", 10, 40);
	JScrollPane jScrollPane = new JScrollPane(_jTextArea);
	add(jScrollPane);

	setBorder(BorderFactory
		  .createTitledBorder(BorderFactory
				      .createLineBorder(Color.black),
				      ""));

	// Event handling
	_jTextArea.addKeyListener(new ShellKeyListener());

	// Initialize the interpreter.
	try {
	    // FIXME: Perhaps the interpreter should be in its own thread?
	    _tclInterp.setVar("panelShell",
			      ReflectObject.newInstance(_tclInterp,
							JTextAreaTclShell.class,
							this), 0);
	    _tclInterp.eval("proc puts {s} {"
			    + "global panelShell; "
			    + "$panelShell appendJTextArea $s\\n}");
	}
	catch (TclException e) {
	    System.out.println(_tclInterp.getResult());
	}
    }

    /** Override this to initialize the prompt. We need to do
     *	because we can't write to the TextArea until the peer
     * has been created.
     */
    public void addNotify () {
	// FIXME: is this method necessary?
	super.addNotify();
	appendJTextArea(mainPrompt);
    }

    /** Append the text to the JTextArea, update the prompt cursor.
     */
    public void appendJTextArea(final String text) {
        Runnable doAppendJTextArea = new Runnable() {
            public void run() {
		_jTextArea.append(text);
		// Scroll down as we generate text.
		_jTextArea.setCaretPosition(_jTextArea.getText().length());
            }
        };
        SwingUtilities.invokeLater(doAppendJTextArea);
	// FIXME: There could be problems here with _promptCursor being
	// updated before the JTextArea is actually updated
	_promptCursor += text.length();
    }

    /** Main method used for testing.
     *  To run a simple test, use:
     *  <pre>
     *	java -classpath $PTII:$PTII/lib/ptjacl.jar ptolemy.actor.gui.ptjacl.JTextAreaTclShell
     *  </pre>
     */
    public static void main(String [] args) {
        JFrame jFrame = new JFrame("JTextAreaTclShell Example");
        WindowListener windowListener = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
        };
        jFrame.addWindowListener(windowListener);

        final JTextAreaTclShell exec =
	    new JTextAreaTclShell();
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Evaluate the command so far, if possible, printing
    // a continuation prompt if not.
    private void _evalCommand () {
	String newtext = _jTextArea.getText().substring(_promptCursor);
	_promptCursor += newtext.length();
	if (_commandBuffer.length() > 0) {
	    _commandBuffer.append("\n");
	}
	_commandBuffer.append(newtext);
	String command = _commandBuffer.toString();

	if (_tclInterp.commandComplete(command)) {
	    // Process it
	    appendJTextArea("\n");
	    Cursor oldCursor = _jTextArea.getCursor();
	    _jTextArea.setCursor(new Cursor(Cursor.WAIT_CURSOR));
	    try {
		_tclInterp.eval(command);
	    }
	    catch (TclException e) {
		// ignore
	    }
	    String result = _tclInterp.getResult().toString();
	    if (result.length() > 0) {
		appendJTextArea(result + "\n" + mainPrompt);
	    } else {
		appendJTextArea(mainPrompt);
	    }
	    _commandBuffer.setLength(0);
	    _commandCursor = _promptCursor;
	    _jTextArea.setCursor(oldCursor);
	    _updateHistory(command);
	} else {
	    appendJTextArea("\n" + contPrompt);
	}
    }

    // Update the command history
    private void _updateHistory (String command) {
	_historyCursor = 0;
	if (_historyCommands.size() == historyLength) {
	    _historyCommands.removeElementAt(0);
	}
	_historyCommands.addElement(command);
    }

  // Replace the command with an entry from the history
  private void _nextCommand () {
    String text;
    if (_historyCursor == 0) {
      text = "";
    } else {
      _historyCursor --;
      text = (String)_historyCommands.elementAt(
           _historyCommands.size() - _historyCursor - 1);
    }
    replaceRangeJTextArea(text, _commandCursor, _jTextArea.getText().length());
  }

  private void _previousCommand () {
    String text;
    if (_historyCursor == _historyCommands.size()) {
      return;
    } else {
      _historyCursor ++;
      text = (String)_historyCommands.elementAt(
            _historyCommands.size() - _historyCursor);
    }
    replaceRangeJTextArea(text, _commandCursor, _jTextArea.getText().length());
  }

  // The key listener
  class ShellKeyListener extends KeyAdapter {
    public void keyPressed (KeyEvent keyEvent) {
      // Process keys
      switch (keyEvent.getKeyCode()) {
      case KeyEvent.VK_ENTER:
	keyEvent.consume();
	_evalCommand();
	break;
      case KeyEvent.VK_BACK_SPACE:
	if (_jTextArea.getCaretPosition() == _promptCursor) {
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
            // Otherwise we got a regular character. Don't consume it,
            // and TextArea will take care of displaying it.
        }
      }
    }
  }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // Prompts
    public String mainPrompt = "% ";
    public String contPrompt = "> ";

    public int historyLength = 10;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The TextArea widget for displaying commands and results
    private JTextArea _jTextArea;

    // Cursors
    private int _promptCursor = 0;
    private int _commandCursor = 0;

    // History
    private int _historyCursor = 0;
    private Vector _historyCommands = new Vector();

    // Platform dependent newline
    private static String newline = System.getProperty("line.separator");

    // The command input
    private StringBuffer _commandBuffer = new StringBuffer();

    // The Tcl interpreter
    private Interp _tclInterp = new Interp();
}

/* Helper for the textDisplay JavaScript module.

   Copyright (c) 2017-2018 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs.modules.textDisplay;

import java.lang.ref.WeakReference;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.actor.lib.jjs.JavaScript;
import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

/** Helper for the textDisplay JavaScript module.
 *  This causes a window to open that is used to display the text.
 *  The window can be resized and repositioned and the size and position will be remembered.
 *
 *  @author Edward A. Lee, based on DisplayJavaSE.
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class TextDisplayHelper extends HelperBase {

    /** Create a text display with no title.
     *  @param actor The JavaScript actor associated with this helper.
     *  @param currentObj The JavaScript object that this is helping (a TextDisplay).
     */
    public TextDisplayHelper(Object actor, ScriptObjectMirror currentObj) {
        this(actor, currentObj, null);
    }

    /** Create a text display with the specified title.
     *  @param actor The JavaScript actor associated with this helper.
     *  @param currentObj The JavaScript object that this is helping (a TextDisplay).
     *  @param title A title to associate with the display.
     */
    public TextDisplayHelper(Object actor, ScriptObjectMirror currentObj,
            final String title) {
        super(actor, currentObj);

        // This has to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
            @Override
            public void run() {
                _createOrShowWindow(title);
            }
        };
        // The following will fail if we are executing headless.
        // Catch and send to standard output.
        try {
            Top.deferIfNecessary(doDisplay);
        } catch(Throwable ex) {
            // Ignore, assuming other invocations of deferIfNecessary will fail similarly.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append to any text already displayed starting on a new line.
     *  @param text The text to be displayed.
     */
    public void appendText(final String text) {
        // Display probably to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
            @Override
            public void run() {
                _append(text);
            }
        };
        try {
            Top.deferIfNecessary(doDisplay);
        } catch(Throwable ex) {
            // Print to standard out.
            System.out.println("TextDisplay: " + text);
        }
    }

    /** Display text.
     *  @param text The text to be displayed.
     */
    public void displayText(String text) {
        // Display probably to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
            @Override
            public void run() {
                _display(text);
            }
        };
        try {
            Top.deferIfNecessary(doDisplay);
        } catch(Throwable ex) {
            // Print to standard out.
            System.out.println("TextDisplay: " + text);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The text area in which the data will be displayed. */
    public transient JTextArea textArea;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _append(String text) {
        // _frame can be null if we have no graphical display.
        if (_frame == null) {
            System.out.println("TextDisplayHelper: " + text);
            return;
        }
        String currentText = _frame.text.getText();
        if (currentText != null && currentText.length() > 0) {
            _frame.text.append("\n");
        }
        _frame.text.append(text);
    }

    private void _createOrShowWindow(String title) {
        if (textArea == null) {
            // Place the text area in its own frame.
            // Need an effigy and a tableau so that menu ops work properly.

            Effigy containerEffigy = Configuration
                    .findEffigy(_actor.toplevel());

            try {
                if (containerEffigy == null) {
                    // If we have no container Effigy, then we are probably running
                    // without a graphical display, or with MoMLSimpleApplication, which
                    // does pop up a window.
                    return;
                }
                TextEffigy textEffigy = TextEffigy
                        .newTextEffigy(containerEffigy, "");

                // The default identifier is "Unnamed", which is no good for
                // two reasons: Wrong title bar label, and it causes a save-as
                // to destroy the original window.

                textEffigy.identifier.setExpression(_actor.getFullName());

                _tableau = new DisplayWindowTableau(_actor, textEffigy, title);
                _frame = _tableau.frame.get();

                if (_frame != null) {
                    // Require a vertical scrollbar always so that we don't get a horizontal
                    // scrollbar when it appears.
                    JScrollPane pane = _frame.getScrollPane();
                    if (pane != null) {
                        pane.setVerticalScrollBarPolicy(
                                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                    }

                    textArea = _frame.text;

                    // FIXME: Any way to get these in as parameters?
                    textArea.setRows(10);
                    textArea.setColumns(40);

                    _actor.setFrame(_frame);
                    _frame.pack();
                }
            } catch (Exception ex) {
                MessageHandler.error("Error opening window for text display.",
                        ex);
            }
        } else {
            // Erase previous text.
            textArea.setText("");
        }

        if (_frame != null) {
            // show() used to override manual placement by calling pack.
            // No more.
            _frame.setVisible(true);
            _frame.toFront();
        }
    }

    private void _display(String text) {
        // _frame can be null if we have no graphical display.
        if (_frame == null) {
            System.out.println("TextDisplayHelper: " + text);
            return;
        }
        _frame.text.setText(text);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The text editor frame. */
    private TextEditor _frame;

    /** The version of TextEditorTableau that creates a Display window. */
    private DisplayWindowTableau _tableau;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of TextEditorTableau that creates DisplayWindow.
     */
    private static class DisplayWindowTableau extends Tableau {
        // FindBugs suggested refactoring this into a static class.

        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param actor The JavaScript actor associated with this tableau.
         *  @param container The container.
         *  @param title The title.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public DisplayWindowTableau(JavaScript actor, TextEffigy container,
                String title)
                throws IllegalActionException, NameDuplicationException {
            super(container, "tableau");

            TextEditor editor = new TextEditor(title, null, actor);
            frame = new WeakReference<TextEditor>(editor);

            // Also need to set the title of this Tableau.
            setTitle(title);

            // Make sure that the effigy and the text area use the same
            // Document (so that they contain the same data).
            editor.text.setDocument(container.getDocument());
            setFrame(editor);
            editor.setTableau(this);
        }

        public WeakReference<TextEditor> frame;
    }
}

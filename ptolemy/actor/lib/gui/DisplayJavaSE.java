/* An AWT and Swing implementation of the the DisplayInterface
 that displays input data in a text area on the screen.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */

package ptolemy.actor.lib.gui;

import java.awt.Color;
import java.awt.Container;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import ptolemy.actor.gui.AbstractPlaceableJavaSE;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.injection.PortableContainer;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// DisplayJavaSE

/**
<p>
DisplayJavaSE is the implementation of the DisplayInterface that uses AWT and Swing
classes.  Values of the tokens arriving on the input channels in a
text area on the screen.  Each input token is written on a
separate line.  The input type can be of any type.
Thus, string-valued tokens can be used to
generate arbitrary textual output, at one token per line.
</p><p>
Note that because of complexities in Swing, if you resize the display
window, then, unlike the plotters, the new size will not be persistent.
That is, if you save the model and then re-open it, the new size is
forgotten.  To control the size, you should set the <i>rowsDisplayed</i>
and <i>columnsDisplayed</i> parameters.
</p><p>
Note that this actor internally uses JTextArea, a Java Swing object
that is known to consume large amounts of memory. It is not advisable
to use this actor to log large output streams.</p>

@author Yuhong Xiong, Edward A. Lee Contributors: Ishwinder Singh
@version $Id$
@since Ptolemy II 10.0
 */

public class DisplayJavaSE extends AbstractPlaceableJavaSE implements
DisplayInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Free up memory when closing.
     *  This is executed in the Swing event thread.
     */
    @Override
    public void cleanUp() {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                _tableau = null;

                if (_scrollPane != null) {
                    _scrollPane.removeAll();
                    _scrollPane = null;
                }
                if (textArea != null) {
                    textArea.removeAll();
                    textArea = null;
                }
                _frame = null;
                DisplayJavaSE.super.cleanUp();
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Append the string value of the token to the text area
     *  on the screen.  Each value is terminated with a newline
     *  character. This is executed in the Swing event thread.
     *  @param value The string to be displayed
     */
    @Override
    public void display(final String value) {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                if (textArea == null) {
                    return;
                }

                textArea.append(value);

                // Append a newline character.
                if (value.length() > 0 || !_display._isSuppressBlankLines) {
                    textArea.append("\n");
                }

                // Regrettably, the default in swing is that the top
                // of the textArea is visible, not the most recent text.
                // So we have to manually move the scrollbar.
                // The (undocumented) way to do this is to set the
                // caret position (despite the fact that the caret
                // is already where want it).
                try {
                    int lineOffset = textArea.getLineStartOffset(textArea
                            .getLineCount() - 1);
                    textArea.setCaretPosition(lineOffset);
                } catch (BadLocationException ex) {
                    // Ignore ... worst case is that the scrollbar
                    // doesn't move.
                }
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Return the object of the containing text area.
     *  @return the text area.
     */
    @Override
    public Object getTextArea() {
        return textArea;
    }

    /** Set the number of rows for the text area.
     * @param displayActor The display actor to be initialized.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    @Override
    public void init(Display displayActor) throws IllegalActionException,
    NameDuplicationException {
        _display = displayActor;
        super.init(displayActor);
    }

    /** Open the display window if it has not been opened.
     *  @exception IllegalActionException If there is a problem creating
     *  the effigy and tableau.
     *  This is executed in the Swing event thread.
     */
    @Override
    public void openWindow() throws IllegalActionException {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                if (textArea == null) {
                    // No container has been specified for display.
                    // Place the text area in its own frame.
                    // Need an effigy and a tableau so that menu ops work properly.

                    Effigy containerEffigy = Configuration.findEffigy(_display
                            .toplevel());

                    try {
                        if (containerEffigy == null) {
                            throw new IllegalActionException(
                                    "Cannot find effigy for top level: "
                                            + _display.toplevel().getFullName());
                        }
                        TextEffigy textEffigy = TextEffigy.newTextEffigy(
                                containerEffigy, "");

                        // The default identifier is "Unnamed", which is no good for
                        // two reasons: Wrong title bar label, and it causes a save-as
                        // to destroy the original window.

                        textEffigy.identifier.setExpression(_display
                                .getFullName());

                        _tableau = new DisplayWindowTableau(_display,
                                textEffigy, "tableau");
                        _frame = _tableau.frame.get();

                        // Require a vertical scrollbar always so that we don't get a horizontal
                        // scrollbar when it appears.
                        JScrollPane pane = ((TextEditor) _frame)
                                .getScrollPane();
                        if (pane != null) {
                            pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                        }

                        textArea = ((TextEditor) _frame).text;

                        int numRows = ((IntToken) _display.rowsDisplayed
                                .getToken()).intValue();
                        textArea.setRows(numRows);

                        int numColumns = ((IntToken) _display.columnsDisplayed
                                .getToken()).intValue();

                        textArea.setColumns(numColumns);
                        setFrame(_frame);
                        _frame.pack();
                    } catch (Exception ex) {
                        MessageHandler.error(
                                "Error opening window for Display actor.", ex);
                    }
                } else {
                    // Erase previous text.
                    textArea.setText(null);
                }

                if (_frame != null) {
                    // show() used to override manual placement by calling pack.
                    // No more.
                    _frame.setVisible(true);
                    _frame.toFront();
                }
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of JTextArea will be added to that container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of JTextArea will be placed in its own frame.
     *  The text area is also placed in its own frame if this method
     *  is called with a null argument.
     *  The background of the text area is set equal to that of the container
     *  (unless it is null).
     *  This is executed in the Swing event thread.
     *  @param portableContainer The container into which to place the
     *   text area, or null to specify that there is no current
     *   container.
     */
    @Override
    public void place(final PortableContainer portableContainer) {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                Container container = (Container) (portableContainer != null ? portableContainer
                        .getPlatformContainer() : null);
                if (container == null) {
                    // Reset everything.
                    // NOTE: _remove() doesn't work here.  Why?
                    if (_frame != null) {
                        if (_frame instanceof Top) {
                            Top top = (Top) _frame;
                            if (!top.isDisposed()) {
                                top.dispose();
                            }
                        } else {
                            _frame.dispose();
                        }
                    }

                    _frame = null;
                    _scrollPane = null;
                    textArea = null;
                    return;
                }

                textArea = new JTextArea();
                _scrollPane = new JScrollPane(textArea);

                // java.awt.Component.setBackground(color) says that
                // if the color "parameter is null then this component
                // will inherit the  background color of its parent."
                _scrollPane.setBackground(null);
                _scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
                _scrollPane.setViewportBorder(new LineBorder(Color.black));

                // Always have a vertical scrollbar so that we don't get a horizontal scrollbar when it appers.
                _scrollPane
                        .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                container.add(_scrollPane);
                textArea.setBackground(Color.white);

                String titleSpec;
                try {
                    titleSpec = _display.title.stringValue();
                } catch (IllegalActionException e) {
                    titleSpec = "Error in title: " + e.getMessage();
                }

                if (!titleSpec.trim().equals("")) {
                    _scrollPane.setBorder(BorderFactory
                            .createTitledBorder(titleSpec));
                }

                try {
                    int numRows = ((IntToken) _display.rowsDisplayed.getToken())
                            .intValue();
                    textArea.setRows(numRows);

                    int numColumns = ((IntToken) _display.columnsDisplayed
                            .getToken()).intValue();
                    textArea.setColumns(numColumns);

                } catch (IllegalActionException ex) {
                    // Ignore, and use default number of rows.
                }

                // Make sure the text is not editable.
                textArea.setEditable(false);
                _awtContainer = container;
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Remove the display from the current container, if there is one.
     *  This is executed in the Swing thread later.
     */
    @Override
    public void remove() {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                if (textArea != null) {
                    if (_awtContainer != null && _scrollPane != null) {
                        _awtContainer.remove(_scrollPane);
                        _awtContainer.invalidate();
                        _awtContainer.repaint();
                    } else if (_frame != null) {
                        _frame.dispose();
                    }
                }
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Set the desired number of columns of the textArea, if there is one.
     *  This is executed in the Swing event thread.
     *  @param numberOfColumns The new value of the attribute.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */
    @Override
    public void setColumns(final int numberOfColumns)
            throws IllegalActionException {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                if (textArea != null) {
                    // Unset any previously set size.
                    try {
                        _paneSize.setToken((Token) null);
                    } catch (IllegalActionException e) {
                        MessageHandler
                        .error("Unexpected error: Unable to unset previous pane size.",
                                e);
                    }
                    setFrame(_frame);

                    textArea.setColumns(numberOfColumns);

                    if (_frame != null) {
                        _frame.pack();
                        _frame.setVisible(true);
                    }
                }
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Set the desired number of rows of the textArea, if there is one.
     *  This is executed in the Swing event thread.
     *  @param numberOfRows The new value of the attribute.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */
    @Override
    public void setRows(final int numberOfRows) throws IllegalActionException {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                if (textArea != null) {
                    // Unset any previously set size.
                    try {
                        _paneSize.setToken((Token) null);
                    } catch (IllegalActionException e) {
                        MessageHandler
                        .error("Unexpected error: Unable to unset previous pane size.",
                                e);
                    }
                    setFrame(_frame);

                    textArea.setRows(numberOfRows);

                    if (_frame != null) {
                        _frame.pack();
                        _frame.setVisible(true);
                    }
                }
            }
        };
        Top.deferIfNecessary(doIt);
    }

    /** Set the title of the window.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of
     *  the Display window will be updated to the value of the name
     *  parameter.</p>
     *  This is executed in the Swing event thread.
     * @param stringValue The title to be set.
     * @exception IllegalActionException If the title cannot be set.
     */
    @Override
    public void setTitle(final String stringValue)
            throws IllegalActionException {
        Runnable doIt = new Runnable() {
            @Override
            public void run() {
                if (_tableau != null) {
                    try {
                        if (_display.title.stringValue().trim().equals("")) {
                            _tableau.setTitle(stringValue);
                        }
                    } catch (IllegalActionException e) {
                        _tableau.setTitle("Error getting title");
                    }
                }
            }
        };
        Top.deferIfNecessary(doIt);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** The text area in which the data will be displayed. */
    public transient JTextArea textArea;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The AWT Container */
    private Container _awtContainer;

    /** Reference to the Display actor */
    private Display _display;

    /** The version of TextEditorTableau that creates a Display window. */
    private DisplayWindowTableau _tableau;

    /** The scroll pane. */
    private JScrollPane _scrollPane;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of TextEditorTableau that creates DisplayWindow.
     */
    private static class DisplayWindowTableau extends Tableau {
        // FindBugs suggested refactoring this into a static class.

        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param display The Display actor associated with this tableau.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public DisplayWindowTableau(Display display, TextEffigy container,
                String name) throws IllegalActionException,
                NameDuplicationException {
            super(container, name);

            String title = display.title.stringValue();

            if (title.trim().equals("")) {
                title = display.getFullName();
            }

            TextEditor editor = new TextEditor(title, null, display);
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

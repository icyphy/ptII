/* An actor that displays input data in a text area on the screen.

 @Copyright (c) 1998-2010 The Regents of the University of California.
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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.AbstractPlaceableActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Display

/**
 <p>
 Display the values of the tokens arriving on the input channels in a
 text area on the screen.  Each input token is written on a
 separate line.  The input type can be of any type.
 If the input happens to be a StringToken,
 then the surrounding quotation marks are stripped before printing
 the value of the token.  Thus, string-valued tokens can be used to
 generate arbitrary textual output, at one token per line.
 Tokens are read from the input only in
 the postfire() method, to allow them to settle in domains where they
 converge to a fixed point.
 </p><p>
 This actor has a <i>suppressBlankLines</i> parameter, whose default value
 is false. If this parameter is configured to be true, this actor does not
 put a blank line in the display.
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

 @author  Yuhong Xiong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (yuhong)
 @Pt.AcceptedRating Yellow (vogel)
 */
public class Display extends AbstractPlaceableActor {
    /** Construct an actor with an input multiport of type GENERAL.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Display(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setExpression("10");
        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setExpression("40");

        suppressBlankLines = new Parameter(this, "suppressBlankLines");
        suppressBlankLines.setTypeEquals(BaseType.BOOLEAN);
        suppressBlankLines.setToken(BooleanToken.FALSE);

        title = new StringParameter(this, "title");
        title.setExpression("");

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-15\" " + "width=\"40\" height=\"30\" "
                + "style=\"fill:lightGrey\"/>\n" + "<rect x=\"-15\" y=\"-10\" "
                + "width=\"30\" height=\"20\" " + "style=\"fill:white\"/>\n"
                + "<line x1=\"-13\" y1=\"-6\" x2=\"-4\" y2=\"-6\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"-2\" x2=\"0\" y2=\"-2\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"2\" x2=\"-8\" y2=\"2\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"6\" x2=\"4\" y2=\"6\" "
                + "style=\"stroke:grey\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    /** The input port, which is a multiport.
     */
    public TypedIOPort input;

    /** The vertical size of the display, in rows. This contains an
     *  integer, and defaults to 10.
     */
    public Parameter rowsDisplayed;

    /** The flag indicating whether this display actor suppress
     *  blank lines. The default value is false.
     */
    public Parameter suppressBlankLines;

    /** The text area in which the data will be displayed. */
    public transient JTextArea textArea;

    /** The title to put on top. Note that the value of the title
     *  overrides the value of the name of the actor or the display
     *  name of the actor.
     */
    public StringParameter title;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>rowsDisplayed</i>, then set
     *  the desired number of rows of the textArea, if there is one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>rowsDisplayed</i> and its value is not positive.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == rowsDisplayed) {
            int numRows = ((IntToken) rowsDisplayed.getToken()).intValue();

            if (numRows <= 0) {
                throw new IllegalActionException(this,
                        "rowsDisplayed: requires a positive value.");
            }

            if (numRows != _previousNumRows) {
                _previousNumRows = numRows;

                if (textArea != null) {
                    // Unset any previously set size.
                    _paneSize.setToken((Token) null);
                    setFrame(_frame);

                    textArea.setRows(numRows);

                    if (_frame != null) {
                        _frame.pack();
                        _frame.setVisible(true);
                    }
                }
            }
        } else if (attribute == columnsDisplayed) {
            int numColumns = ((IntToken) columnsDisplayed.getToken())
                    .intValue();

            if (numColumns <= 0) {
                throw new IllegalActionException(this,
                        "columnsDisplayed: requires a positive value.");
            }

            if (numColumns != _previousNumColumns) {
                _previousNumColumns = numColumns;

                if (textArea != null) {
                    // Unset any previously set size.
                    _paneSize.setToken((Token) null);
                    setFrame(_frame);

                    textArea.setColumns(numColumns);

                    if (_frame != null) {
                        _frame.pack();
                        _frame.setVisible(true);
                    }
                }
            }
        } else if (attribute == suppressBlankLines) {
            _suppressBlankLines = ((BooleanToken) suppressBlankLines.getToken())
                    .booleanValue();
        } else if (attribute == title) {
            if (_tableau != null) {
                _tableau.setTitle(title.stringValue());
            }
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the textArea public variable to null.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Display newObject = (Display) super.clone(workspace);
        newObject.textArea = null;
        return newObject;
    }

    /** Get the background color.
     *  @return The background color of the text area.
     *  @see #setBackground(Color)
     */
    public Color getBackground() {
        return textArea.getBackground();
    }

    /** Initialize this display.  If place() has not been called
     *  with a container into which to place the display, then create a
     *  new frame into which to put it.
     *  @exception IllegalActionException If the parent class throws it,
     *   or if the numRows or numColumns parameters are incorrect, or
     *   if there is no effigy for the top level container, or if a problem
     *   occurs creating the effigy and tableau.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _initialized = false;
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of JTextArea will be added to that container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of JTextArea will be placed in its own frame.
     *  The text area is also placed in its own frame if this method
     *  is called with a null argument.
     *  The background of the text area is set equal to that of the container
     *  (unless it is null).
     *
     *  @param container The container into which to place the text area, or
     *   null to specify that there is no current container.
     */
    public void place(Container container) {
        _container = container;

        if (_container == null) {
            // Reset everything.
            // NOTE: _remove() doesn't work here.  Why?
            if (_frame != null) {
                _frame.dispose();
                /* experimental: replaces _frame.dispose();
                if (_frame instanceof Top) {
                    Top top = (Top) _frame;
                    if (!top.isDisposed()) {
                        top.dispose();
                    }
                } else {
                    _frame.dispose();
                }
                */
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

        _container.add(_scrollPane);
        textArea.setBackground(Color.white);

        String titleSpec;
        try {
            titleSpec = title.stringValue();
        } catch (IllegalActionException e) {
            titleSpec = "Error in title: " + e.getMessage();
        }

        if (!titleSpec.trim().equals("")) {
            _scrollPane.setBorder(BorderFactory.createTitledBorder(titleSpec));
        }

        try {
            int numRows = ((IntToken) rowsDisplayed.getToken()).intValue();
            textArea.setRows(numRows);

            int numColumns = ((IntToken) columnsDisplayed.getToken())
                    .intValue();
            textArea.setColumns(numColumns);

            // Note that in an applet, you may see problems where
            // the text area is obscured by the horizontal scroll
            // bar.  The solution is to make the applet wider
            // or specify a smaller number of columns to display.
            // The ct CarTracking demo will exhibit this bug
            // if the applet is too narrow.
        } catch (IllegalActionException ex) {
            // Ignore, and use default number of rows.
        }

        // Make sure the text is not editable.
        textArea.setEditable(false);
    }

    /** Read at most one token from each input channel and display its
     *  string value on the screen.  Each value is terminated
     *  with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        int width = input.getWidth();

        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);

                if (!_initialized) {
                    _initialized = true;
                    _openWindow();
                }

                // If the window has been deleted, read the rest of the inputs.
                if (textArea == null) {
                    continue;
                }

                // FIXME: There is a race condition here.
                // textArea can be set to null during execution of this method
                // if another thread closes the display window.

                // The toString() method yields a string that can be parsed back
                // in the expression language to get the original token.
                // However, if the token is a StringToken, that probably is
                // not what we want. So we treat StringToken separately.
                String value = token.toString();
                if (token instanceof StringToken) {
                    value = ((StringToken) token).stringValue();
                }

                textArea.append(value);

                // Append a newline character.
                if (value.length() > 0 || !_suppressBlankLines) {
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
            } else if (!_suppressBlankLines && textArea != null) {
                // There is no input token on this channel, so we
                // output a blank line.
                textArea.append("\n");
            }
        }
        // If we have a Const -> Display SDF model with iterations set
        // to 0, then stopping the model by hitting the stop button
        // was taking between 2 and 17 seconds (average over 11 runs, 7.2 seconds)
        // If we have a Thread.yield() here, then the time is between
        // 1.3 and 3.5 seconds ( average over 10 runs, 2.5 seconds)
        Thread.yield();

        return super.postfire();
    }

    /** Set the background.
     *  @param background The background color.
     *  @see #getBackground()
     */
    public void setBackground(Color background) {
        if (_frame != null) {
            _frame.setBackground(background);
        }
    }

    /** Override the base class to remove the display from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        Nameable previousContainer = getContainer();
        super.setContainer(container);

        if ((container != previousContainer) && (previousContainer != null)) {
            _remove();
        }
    }

    /** Set a name to present to the user.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of the
     *  Display window will be updated to the value of the name parameter.</p>
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    public void setDisplayName(String name) {
        super.setDisplayName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        _setTitle(name);
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of the
     *  Display window will be updated to the value of the name parameter.</p>
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period
     *   or if the object is a derived object and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #getName()
     *  @see #getName(NamedObj)
     *  @see #title
     */
    public void setName(String name) throws IllegalActionException,
            NameDuplicationException {
        super.setName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        _setTitle(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Free up memory when closing. */
    protected void cleanUp() {
        _tableau = null;
        place(null);
        /* experimental: replaces place(null);
        if (_scrollPane != null) {
            _scrollPane.removeAll();
            _scrollPane = null;
        }
        if (textArea != null) {
            textArea.removeAll();
            textArea = null;
        }
        _frame = null;
        */
        super.cleanUp();
    }

    /** Open the display window if it has not been opened.
     *  @exception IllegalActionException If there is a problem creating
     *  the effigy and tableau.
     */
    protected void _openWindow() throws IllegalActionException {
        if (textArea == null) {
            // No container has been specified for display.
            // Place the text area in its own frame.
            // Need an effigy and a tableau so that menu ops work properly.
            Effigy containerEffigy = Configuration.findEffigy(toplevel());

            if (containerEffigy == null) {
                throw new IllegalActionException(this,
                        "Cannot find effigy for top level: "
                                + toplevel().getFullName());
            }

            try {
                TextEffigy textEffigy = TextEffigy.newTextEffigy(
                        containerEffigy, "");

                // The default identifier is "Unnamed", which is no good for
                // two reasons: Wrong title bar label, and it causes a save-as
                // to destroy the original window.
                textEffigy.identifier.setExpression(getFullName());

                _tableau = new DisplayWindowTableau(this, textEffigy, "tableau");
                _frame = _tableau.frame.get();
            } catch (Exception ex) {
                throw new IllegalActionException(this, null, ex,
                        "Error creating effigy and tableau");
            }

            textArea = ((TextEditor) _frame).text;

            int numRows = ((IntToken) rowsDisplayed.getToken()).intValue();
            textArea.setRows(numRows);

            int numColumns = ((IntToken) columnsDisplayed.getToken())
                    .intValue();
            textArea.setColumns(numColumns);
            setFrame(_frame);
            _frame.pack();
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

        /*
         int tab = ((IntToken)tabSize.getToken()).intValue();
         // NOTE: As of jdk 1.3beta the following is ignored.
         textArea.setTabSize(tab);
         */
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (textArea != null) {
                    if ((_container != null) && (_scrollPane != null)) {
                        _container.remove(_scrollPane);
                        _container.invalidate();
                        _container.repaint();
                    } else if (_frame != null) {
                        _frame.dispose();
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** Indicator that the display window has been opened. */
    protected boolean _initialized = false;

    /** The flag indicating whether the blank lines will be suppressed. */
    protected boolean _suppressBlankLines = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the title of this window.
     *  <p>If the <i>title</i> parameter is set to the empty string,
     *  and the Display window has been rendered, then the title of the
     *  Display window will be updated to the value of the name parameter.</p>
     */
    private void _setTitle(String name) {
        if (_tableau != null) {
            try {
                if (title.stringValue().trim().equals("")) {
                    _tableau.setTitle(name);
                }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(this, ex,
                        "Failed to get the value of the title parameter.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The container for the text display, if there is one.
    private Container _container;

    // Record of previous columns.
    private int _previousNumColumns = 0;

    // Record of previous rows.
    private int _previousNumRows = 0;

    // The scroll pane.
    private JScrollPane _scrollPane;

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

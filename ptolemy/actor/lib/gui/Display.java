/* An actor that displays input data in a text area on the screen.

@Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.awt.Color;
import java.awt.Container;
import java.io.IOException;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;

//////////////////////////////////////////////////////////////////////////
//// Display
/**
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
<p>
Note that because of complexities in Swing, if you resize the display
window, then, unlike the plotters, the new size will not be persistent.
That is, if you save the model and then re-open it, the new size is
forgotten.  To control the size, you should set the <i>rowsDisplayed</i>
and <i>columnsDisplayed</i> parameters.

@author  Yuhong Xiong, Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class Display extends Sink implements Placeable {

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
        // Set the type of the input port.
        input.setTypeEquals(BaseType.GENERAL);

        rowsDisplayed = new Parameter(this, "rowsDisplayed");
        rowsDisplayed.setExpression("10");
        columnsDisplayed = new Parameter(this, "columnsDisplayed");
        columnsDisplayed.setExpression("40");

        title = new StringAttribute(this, "title");
        title.setExpression("");

        _windowProperties = new WindowPropertiesAttribute(
                this, "_windowProperties");

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-15\" "
                + "width=\"40\" height=\"30\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<rect x=\"-15\" y=\"-10\" "
                + "width=\"30\" height=\"20\" "
                + "style=\"fill:white\"/>\n"
                + "<line x1=\"-13\" y1=\"-6\" x2=\"-4\" y2=\"-6\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"-2\" x2=\"0\" y2=\"-2\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"2\" x2=\"-8\" y2=\"2\" "
                + "style=\"stroke:grey\"/>\n"
                + "<line x1=\"-13\" y1=\"6\" x2=\"4\" y2=\"6\" "
                + "style=\"stroke:grey\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The horizontal size of the display, in columns. This contains
     *  an integer, and defaults to 40.
     */
    public Parameter columnsDisplayed;

    /** The vertical size of the display, in rows. This contains an
     *  integer, and defaults to 10.
     */
    public Parameter rowsDisplayed;

    /** The text area in which the data will be displayed. */
    public transient JTextArea textArea;

    /** The title to put on top. */
    public StringAttribute title;

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
        // NOTE: Do not react to changes in _windowProperties.
        // Those properties are only used when originally opening a window.
        if (attribute == rowsDisplayed) {
            int numRows = ((IntToken)rowsDisplayed.getToken()).intValue();
            if (numRows <= 0) {
                throw new IllegalActionException(this,
                        "rowsDisplayed: requires a positive value.");
            }
            if (numRows != _previousNumRows) {
                _previousNumRows = numRows;
                if (textArea != null) {
                    textArea.setRows(numRows);
                    if (_frame != null) {
                        _frame.pack();
                        _frame.show();
                    }
                }
            }
        } else if (attribute == columnsDisplayed) {
            int numColumns =
                ((IntToken)columnsDisplayed.getToken()).intValue();
            if (numColumns <= 0) {
                throw new IllegalActionException(this,
                        "columnsDisplayed: requires a positive value.");
            }
            if (numColumns != _previousNumColumns) {
                _previousNumColumns = numColumns;
                if (textArea != null) {
                    textArea.setColumns(numColumns);
                    if (_frame != null) {
                        _frame.show();
                    }
                }
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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Display newObject = (Display)super.clone(workspace);
        newObject.textArea = null;
        return newObject;
    }

    /** Get the background color.
     *  @return The background color of the text area.
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
                DisplayWindowTableau tableau = new DisplayWindowTableau(
                        textEffigy, "tableau");
                _frame = tableau.frame;
            } catch (Exception ex) {
                throw new IllegalActionException(this, null, ex,
                        "Error creating effigy and tableau");
            }
            textArea = _frame.text;
            int numRows =
                ((IntToken)rowsDisplayed.getToken()).intValue();
            textArea.setRows(numRows);
            int numColumns =
                ((IntToken)columnsDisplayed.getToken()).intValue();
            textArea.setColumns(numColumns);
            _windowProperties.setProperties(_frame);
            _frame.pack();
        } else {
            // Erase previous text.
            textArea.setText(null);
        }
        if (_frame != null) {
            // show() used to override manual placement by calling pack.
            // No more.
            _frame.show();
            _frame.toFront();
        }
        /*
          int tab = ((IntToken)tabSize.getToken()).intValue();
          // NOTE: As of jdk 1.3beta the following is ignored.
          textArea.setTabSize(tab);
        */
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
            if (_frame != null) _frame.dispose();
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
        String titleSpec = title.getExpression();
        if (!titleSpec.trim().equals("")) {
            _scrollPane.setBorder(
                    BorderFactory.createTitledBorder(titleSpec));
        }
        try {
            int numRows =
                ((IntToken)rowsDisplayed.getToken()).intValue();
            textArea.setRows(numRows);
            int numColumns =
                ((IntToken)columnsDisplayed.getToken()).intValue();
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
                // If the window has been deleted, read the rest of the inputs.
                if (textArea == null) continue;
                String value = token.toString();
                // If the value is a pure string, strip the quotation marks.
                if ((value.length() > 1) && value.startsWith("\"") &&
                        value.endsWith("\"")) {
                    value = value.substring(1, value.length()-1);
                }
                textArea.append(value);

                // Append a newline character.
                if (width > i + 1) textArea.append("\n");

                // Regrettably, the default in swing is that the top
                // of the textArea is visible, not the most recent text.
                // So we have to manually move the scrollbar.
                // The (undocumented) way to do this is to set the
                // caret position (despite the fact that the caret
                // is already where want it).
                try {
                    int lineOffset = textArea
                        .getLineStartOffset(textArea.getLineCount() - 1);
                    textArea.setCaretPosition(lineOffset);
                } catch (BadLocationException ex) {
                    // Ignore ... worst case is that the scrollbar
                    // doesn't move.
                }
            }
        }
        if (textArea != null) {
            textArea.append("\n");
        }
        return super.postfire();
    }

    /** Set the background */
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
        if (container != previousContainer && previousContainer != null) {
            _remove();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object. This
     *  overrides the base class to make sure that the current frame
     *  properties, if there is a frame, are recorded.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // Make sure that the current position of the frame, if any,
        // is up to date.
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);
        }
        super._exportMoMLContents(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the display from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (textArea != null) {
                        if (_container != null && _scrollPane != null) {
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
    ////                         private members                   ////

    // The container for the text display, if there is one.
    private Container _container;

    // The frame into which to put the text widget, if any.
    private TextEditor _frame;

    // Record of previous columns.
    private int _previousNumColumns = 0;

    // Record of previous rows.
    private int _previousNumRows = 0;

    // The scroll pane.
    private JScrollPane _scrollPane;

    // A specification for the window properties of the frame.
    private WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of TextEditor that removes its association with the
     *  Display upon closing, and also records the size of the display.
     */
    private class DisplayWindow extends TextEditor {

        /** Construct an empty text editor with the specified title.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  @param title The title to put in the title bar.
         */
        public DisplayWindow(String title) {
            super(title);
        }

        /** Close the window.  This overrides the base class to remove
         *  the association with the Display and to record window properties.
         *  @return True.
         */
        protected boolean _close() {
            // Record the window properties before closing.
            if (_frame != null) {
                _windowProperties.setProperties(_frame);
            }
            super._close();
            place(null);
            return true;
        }
    }

    /** Version of TextEditorTableau that creates DisplayWindow.
     */
    private class DisplayWindowTableau extends Tableau {

        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public DisplayWindowTableau(TextEffigy container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            String title = Display.this.title.getExpression();
            if (title.trim().equals("")) {
                title = Display.this.getFullName();
            }
            frame = new DisplayWindow(title);
            // Also need to set the title of this Tableau.
            setTitle(title);
            // Make sure that the effigy and the text area use the same
            // Document (so that they contain the same data).
            frame.text.setDocument(container.getDocument());
            setFrame(frame);
            frame.setTableau(this);
        }

        public DisplayWindow frame;
    }
}

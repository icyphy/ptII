/* An actor that displays input data in a text area on the screen.

@Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.kernel.util.*;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Sink;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;


/**
Display the values of the tokens arriving on the input channels
in a text area on the screen.  The input on each firing is written
on a separate line.  If there is more than one input channel,
then the displays are separated by tabs.
The input type is GENERAL, meaning that any token is acceptable.
Tokens are read from the input only in the postfire() method, to
allow them to settle in domains where they converge to a fixed point.

@author  Yuhong Xiong, Edward A. Lee
@version $Id$
 */
public class Display extends Sink implements Placeable, SequenceActor {

    /** Construct an actor with an input multiport of type GENERAL.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Display(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Set the type of the input port.
        input.setTypeEquals(BaseType.GENERAL);

        rowsDisplayed = new Parameter(this, "rowsDisplayed", new IntToken(10));

        // NOTE: As of jdk 1.3-beta, the setTabSize() method of
        // JTextArea does not work (it is ignored), so the tabSize
        // parameter has no effect.
        // tabSize = new Parameter(this, "tabSize", new IntToken(8));
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The vertical size of the display, in rows. This contains an
     *  an integer, and defaults to 10.
     */
    public Parameter rowsDisplayed;

    /** The number of characters to expand tabs to. This contains an
     *  an integer, and defaults to 8.
     */
    // public Parameter tabSize;

    /** The text area in which the data will be displayed. */
    public JTextArea textArea;

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
            int numRows = ((IntToken)rowsDisplayed.getToken()).intValue();
            if (numRows <= 0) {
                throw new IllegalActionException(this,
                        "rowsDisplayed: requires a positive value.");
            }
            if (textArea != null) {
                textArea.setRows(numRows);
            }
        }
        /* NOTE: Does not work...
           else if (attribute == tabSize) {
           int tab = ((IntToken)tabSize.getToken()).intValue();
           if(tab <= 0) {
           throw new IllegalActionException(this,
           "tabSize: requires a positive value.");
           }
           if (textArea != null) {
           // NOTE: As of jdk 1.3beta the following is ignored.
           textArea.setTabSize(tab);
           }
           }
        */
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the textArea public variable to null.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Display newobj = (Display)super.clone(workspace);
        newobj.textArea = null;
        newobj.rowsDisplayed = (Parameter)newobj.getAttribute("rowsDisplayed");
        // newobj.tabSize = (Parameter)newobj.getAttribute("tabSize");
        return newobj;
    }

    /** Create a text area on the screen, if necessary, or clear the
     *  previously existing text area. If a graphical container has
     *  not been specified, place the text area into its own frame.
     *  Otherwise, place it in the specified container.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (textArea == null) {
            place(_container);
        } else {
            // Erase previous text.
            textArea.setText(null);
        }
        if (_frame != null) {
            _frame.setVisible(true);
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
     *  @param container The container into which to place the text area.
     */
    public void place(Container container) {
        _container = container;
        if (_container == null) {
            System.out.println("Container is null");
            // place the text area in its own frame.
            // FIXME: This probably needs to be a PtolemyFrame, when one
            // exists, so that the close button is dealt with, etc.
            JFrame _frame = new JFrame(getFullName());
            textArea = new JTextArea();
            _scrollPane = new JScrollPane(textArea);
            _frame.getContentPane().add(_scrollPane);
        } else {
            textArea = new JTextArea();
            _scrollPane = new JScrollPane(textArea);
            _container.add(_scrollPane);
            textArea.setBackground(Color.white);
            try {
                int numRows = ((IntToken)rowsDisplayed.getToken()).intValue();
                textArea.setRows(numRows);
            } catch (IllegalActionException ex) {
                // Ignore, and use default number of rows.
            }
            _scrollPane.setBackground(_container.getBackground());
            _scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
            _scrollPane.setViewportBorder(new LineBorder(Color.black));
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
                String value = token.toString();
                textArea.append(value);

                // Append a tab character.
                if (width > i + 1) textArea.append("\t");

                // Regrettably, the default in swing is that the top
                // of the textarea is visible, not the most recent text.
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
        textArea.append("\n");
        return super.postfire();
    }

    /** Override the base class to make sure the end of the text is visible.
     */
    public void wrapup() {
        if ( _scrollPane != null ) {
            JScrollBar bar = _scrollPane.getVerticalScrollBar();
            if (bar != null) {
                bar.setValue(bar.getMaximum() - bar.getVisibleAmount());
            }
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected members                   ////

    protected JScrollPane _scrollPane;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Container _container;

    // The frame into which to put the text widget, if any.
    private JFrame _frame;
}

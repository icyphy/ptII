/* An actor that displays input data in a text area on the screen.

@Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.text.BadLocationException;


/** Display the values of the tokens arriving on the input channels
 *  in a text area on the screen.
 *  <p>
 *  The input type is Token, meaning that any token is acceptable.
 *
 *  @author  Yuhong Xiong, Edward A. Lee
 *  @version $Id$
 */
public class Print extends TypedAtomicActor implements Placeable {

    public Print(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type Token. */
    public TypedIOPort input;

    /** The text area in which the data will be displayed. */
    public JTextArea textArea;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            Print newobj = (Print)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            textArea = null;
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read at most one token from each input channel and display its
     *  string value on the screen.  Each value is terminated
     *  with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);
                String value = token.stringValue();
                textArea.append(value + "\n");

                // Regrettably, the default in swing is that the top
                // of the textarea is visible, not the most recent text.
                // So we have to manually move the scrollbar.
                // The (undocumented) way to do this is to set the
                // caret position (despite the fact that the caret
                // is already where want it).
                try {
                    int lineOffset =
                    textArea.getLineEndOffset(textArea.getLineCount() - 1);
                    textArea.setCaretPosition(lineOffset);
                } catch (BadLocationException ex) {
                    // Ignore ... worst case is that the scrollbar
                    // doesn't move.
                }
            }
        }
    }

    /** Create a text area on the screen, if necessary, or clear the
     *  previously existing text area.
     *  If a grapical container has not been specified,
     *  place the text area into
     *  its own frame.  Otherwise, place it in the specified container.
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
    }

    /** Specify the container in which the data should be displayed.
     *  An instance of JTextArea will be added to that container.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, an instance of TextArea will be placed in its own frame.
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
            textArea.setBackground(_container.getBackground());
        }
        // Make sure the text is not editable.
        textArea.setEditable(false);
    }

    /** Override the base class to make sure the end of the text is visible.
     */
    public void wrapup() {
        JScrollBar bar = _scrollPane.getVerticalScrollBar();
        if (bar != null) {
            bar.setValue(bar.getMaximum() - bar.getVisibleAmount());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private Container _container;
    private JScrollPane _scrollPane;

    // The frame into which to put the text widget, if any.
    private JFrame _frame;
}

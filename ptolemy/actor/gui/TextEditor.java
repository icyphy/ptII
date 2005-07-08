/* Top-level window containing a simple text editor or viewer.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
// FIXME: To do:
//  - Fix printing.
package ptolemy.actor.gui;

// Java imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

//////////////////////////////////////////////////////////////////////////
//// TextEditor

/**

 TextEditor is a top-level window containing a simple text editor or viewer.
 You can access the public member text to set the text, get the text,
 or set the number of rows or columns.
 After creating this, it is necessary to call show() for it to appear.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class TextEditor extends TableauFrame implements DocumentListener {
    /** Construct an empty text editor with no name.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     */
    public TextEditor() {
        this("Unnamed");
    }

    /** Construct an empty text editor with the specified title.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     */
    public TextEditor(String title) {
        this(title, null);
    }

    /** Construct an empty text editor with the specified title and
     *  document.  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     */
    public TextEditor(String title, Document document) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(null, null);
        setTitle(title);

        text = new JTextArea(document);

        // Since the document may have been null, request it...
        document = text.getDocument();
        document.addDocumentListener(this);
        _scrollPane = new JScrollPane(text);

        getContentPane().add(_scrollPane, BorderLayout.CENTER);
        _initialSaveAsFileName = "data.txt";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The text area. */
    public JTextArea text;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to notification that an attribute or set of attributes
     *  changed.
     */
    public void changedUpdate(DocumentEvent e) {
        // Do nothing... We don't care about attributes.
    }

    /** Get the background color.
     *  @return The background color of the scroll pane.
     */
    public Color getBackground() {
        return _scrollPane.getBackground();
    }

    /** React to notification that there was an insert into the document.
     */
    public void insertUpdate(DocumentEvent e) {
        setModified(true);
    }

    /** React to notification that there was a removal from the document.
     */
    public void removeUpdate(DocumentEvent e) {
        setModified(true);
    }

    /** Scroll as necessary so that the last line is visible.
     */
    public void scrollToEnd() {
        // Song and dance to scroll to the new line.
        text.scrollRectToVisible(new Rectangle(new Point(0, text.getHeight())));
    }

    /** Set background color.  This overrides the base class to set the
     *  background of contained scroll pane and text area.
     *  @param background The background color.
     */
    public void setBackground(Color background) {
        super.setBackground(background);

        // This seems to be called in a base class constructor, before
        // this variable has been set. Hence the test against null.
        if (_scrollPane != null) {
            _scrollPane.setBackground(background);
        }

        if (text != null) {
            // NOTE: Should the background always be white?
            text.setBackground(background);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Clear the current contents.  First, check to see whether
     *  the contents have been modified, and if so, then prompt the user
     *  to save them.  A return value of false
     *  indicates that the user has canceled the action.
     *  @return False if the user cancels the clear.
     */
    protected boolean _clear() {
        if (super._clear()) {
            text.setText("");
            return true;
        } else {
            return false;
        }
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        // FIXME: Give instructions for the editor here.
        _about();
    }

    /** Print the contents.
     */
    protected void _print() {
        // FIXME: What should we print?
        super._print();
    }

    // FIXME: Listen for window closing.
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The scroll pane containing the text area. */
    protected JScrollPane _scrollPane;
}

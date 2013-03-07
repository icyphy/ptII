/* Top-level window containing a simple text editor or viewer.

 Copyright (c) 1998-2013 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.gui.UndoListener;

///////////////////////////////////////////////////////////////////
//// TextEditor

/**

 A top-level window containing a simple text editor or viewer.
 You can access the public member text to set the text, get the text,
 or set the number of rows or columns.
 After creating this, it is necessary to call show() for it to appear.

 @author Edward A. Lee, contributors: Christopher Brooks, Ben Leinfelder
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class TextEditor extends TableauFrame implements DocumentListener,
        Printable {
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
        this(title, document, (Placeable) null);
    }

    /** Construct an empty text editor with the specified title and
     *  document and associated placeable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     *  @param placeable The associated placeable.
     */
    public TextEditor(String title, Document document, Placeable placeable) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(null, null, placeable);
        _init(title, document);
    }

    /** Construct an empty text editor with the specified title and
     *  document and associated poratalbeplaceable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     *  @param portablePlaceable The associated PortablePlaceable.
     */
    public TextEditor(String title, Document document,
            PortablePlaceable portablePlaceable) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(null, null, portablePlaceable);
        _init(title, document);
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
     *  If _scrollPane is null, then null is returned.
     *  @see #setBackground(Color)
     */
    public Color getBackground() {
        // Under Java 1.7 on the Mac, the _scrollbar is sometimes null.
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5574
        if (_scrollPane != null) {
            return _scrollPane.getBackground();
        } else {
            return null;
        }
    }

    /** React to notification that there was an insert into the document.
     */
    public void insertUpdate(DocumentEvent e) {
        setModified(true);
    }

    /** Print the text to a printer, which is represented by the
     *  specified graphics object.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @return PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format, int index)
            throws PrinterException {
        if (graphics == null) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;

        // Loosely based on
        // http://forum.java.sun.com/thread.jspa?threadID=217823&messageID=2361189
        // Found it unwise to use the TextArea font's size,
        // We area just printing text so use a a font size that will
        // be generally useful.
        graphics2D.setFont(getFont().deriveFont(9.0f));

        double bottomMargin = format.getHeight() - format.getImageableHeight()
                - format.getImageableY();

        double lineHeight = graphics2D.getFontMetrics().getHeight()
                - (graphics2D.getFontMetrics().getLeading() / 2);

        int linesPerPage = (int) Math.floor((format.getHeight()
                - format.getImageableY() - bottomMargin)
                / lineHeight);

        int startLine = linesPerPage * index;

        if (startLine > text.getLineCount()) {
            return NO_SUCH_PAGE;
        }

        //int pageCount = (text.getLineCount()/linesPerPage) + 1;
        int endLine = startLine + linesPerPage;
        int linePosition = (int) Math.ceil(format.getImageableY() + lineHeight);

        for (int line = startLine; line < endLine; line++) {
            try {
                String linetext = text.getText(
                        text.getLineStartOffset(line),
                        text.getLineEndOffset(line)
                                - text.getLineStartOffset(line));
                graphics2D.drawString(linetext, (int) format.getImageableX(),
                        linePosition);
            } catch (BadLocationException e) {
                // Ignore. Never a bad location.
            }

            linePosition += lineHeight;
            if (linePosition > format.getHeight() - bottomMargin) {
                break;
            }
        }

        return PAGE_EXISTS;

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
     *  @see #getBackground()
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

    /** Dispose of this frame.
     *     Override this dispose() method to unattach any listeners that may keep
     *  this model from getting garbage collected.  This method invokes the
     *  dispose() method of the superclass,
     *  {@link ptolemy.actor.gui.TableauFrame}.
     */
    public void dispose() {
        if (_debugClosing) {
            System.out.println("TextEditor.dispose() : " + this.getName());
        }

        super.dispose();
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

    /** Initializes an empty text editor with the specified title and
     *  document and associated placeable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *
     *  @param title The title to put in the title bar.
     *  @param document The document containing text.
     */
    protected void _init(String title, Document document) {
        setTitle(title);

        text = new JTextArea(document);

        // Since the document may have been null, request it...
        document = text.getDocument();
        document.addDocumentListener(this);
        _scrollPane = new JScrollPane(text);

        getContentPane().add(_scrollPane, BorderLayout.CENTER);
        _initialSaveAsFileName = "data.txt";

        // Set the undo listener, with default key mappings.
        text.getDocument().addUndoableEditListener(new UndoListener(text));
    }

    /** Query the user for a filename, save the model to that file,
     *  and open a new window to view the model.
     *  This overrides the base class to use the ".txt" extension.
     *  @return True if the save succeeds.
     */
    protected boolean _saveAs() {
        return _saveAs(".txt");
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

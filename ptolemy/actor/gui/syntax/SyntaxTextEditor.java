/* Top-level window containing a simple text editor or viewer.

 Copyright (c) 1998-2016 The Regents of the University of California.
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
package ptolemy.actor.gui.syntax;

import java.awt.BorderLayout;

import javax.swing.JTextArea;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.gui.UndoListener;

///////////////////////////////////////////////////////////////////
//// SyntaxTextEditor

/**

 A top-level window containing a text editor or viewer that understands
 the syntax of various languages. To get syntax-directed editing,
 construct this object with an instance of org.fife.ui.rsyntaxtextarea.RSyntaxDocument
 passed in as a constructor argument.
 You can access the public member text to set the text, get the text,
 or set the number of rows or columns.
 After creating this, it is necessary to call show() for it to appear.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class SyntaxTextEditor extends TextEditor {

    /** Construct an empty text editor with no name.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     */
    public SyntaxTextEditor() {
        this("Unnamed");
    }

    /** Construct an empty text editor with the specified title.
     *  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     */
    public SyntaxTextEditor(String title) {
        this(title, null);
    }

    /** Construct an empty text editor with the specified title and
     *  document.  After constructing this, it is necessary
     *  to call setVisible(true) to make the frame appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     */
    public SyntaxTextEditor(String title, Document document) {
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
    public SyntaxTextEditor(String title, Document document,
            Placeable placeable) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(title, document, placeable);
    }

    /** Construct an empty text editor with the specified title and
     *  document and associated PortablePlaceable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *  @param title The title to put in the title bar.
     *  @param document The document containing text, or null if none.
     *  @param portablePlaceable The associated PortablePlaceable.
     */
    public SyntaxTextEditor(String title, Document document,
            PortablePlaceable portablePlaceable) {
        // NOTE: Create with no status bar, since we have no use for it now.
        super(null, null, portablePlaceable);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initializes an empty text editor with the specified title and
     *  document and associated placeable.  After constructing this,
     *  it is necessary to call setVisible(true) to make the frame
     *  appear.
     *
     *  @param title The title to put in the title bar.
     *  @param document The document containing text.
     */
    @Override
    protected void _init(String title, Document document) {
        setTitle(title);

        if (document instanceof RSyntaxDocument) {
            text = new RSyntaxTextArea((RSyntaxDocument) document);
            // The default tab size is odd: 5.
            text.setTabSize(4);
            text.setCaretPosition(0);
            // ((RSyntaxTextArea)text).addHyperlinkListener(this);
            text.requestFocusInWindow();
            ((RSyntaxTextArea) text).setMarkOccurrences(true);
            ((RSyntaxTextArea) text).setCodeFoldingEnabled(true);
            ((RSyntaxTextArea) text).setClearWhitespaceLinesEnabled(false);
        } else if (document != null) {
            text = new JTextArea(document);
        } else {
            text = new JTextArea();
        }

        // Since the document may have been null, request it...
        document = text.getDocument();
        document.addDocumentListener(this);
        _scrollPane = new RTextScrollPane(text, true);

        // To get bookmarking, do this:
        //        Gutter gutter = ((RTextScrollPane)_scrollPane).getGutter();
        //        gutter.setBookmarkingEnabled(true);
        //        URL url = getClass().getClassLoader().getResource("img/bookmark.png");
        //        gutter.setBookmarkIcon(new ImageIcon(url));
        // Will need to copy the img/bookmark.png from the rsyntaxtextarea_demo_2.5.1_Source dir.

        ErrorStrip errorStrip = new ErrorStrip((RSyntaxTextArea) text);
        getContentPane().add(errorStrip, BorderLayout.LINE_END);

        getContentPane().add(_scrollPane, BorderLayout.CENTER);
        _initialSaveAsFileName = "data.txt";

        // Set the undo listener, with default key mappings.
        _undo = new UndoListener(text);
        text.getDocument().addUndoableEditListener(_undo);
    }
}

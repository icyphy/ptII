/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.tutorial;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import diva.gui.AbstractView;

/**
 * A simple MDI text editor view.  FIXME
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class TextView extends AbstractView {
    private JEditorPane _editorPane;
    private JScrollPane _scrollPane;
    public TextView(TextDocument doc) {
        super(doc);
    }
    public TextDocument getTextDocument() {
        return (TextDocument)getDocument();
    }
    public JComponent getComponent() {
        if(_scrollPane == null) {
            TextDocument td = (TextDocument)getDocument();
            _editorPane = new JEditorPane();
            _editorPane.setText(td.getText());
            // Get notified every time text is changed in the component to update
            // our text document.  The "Document" here is a
            // javax.swing.text.Document.  Don't get confused!
            _editorPane.getDocument().addDocumentListener(new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        getTextDocument().setText(_editorPane.getText());
                    }
                    public void insertUpdate(DocumentEvent e) {
                        getTextDocument().setText(_editorPane.getText());
                    }
                    public void removeUpdate(DocumentEvent e) {
                        getTextDocument().setText(_editorPane.getText());
                    }
                });
            _scrollPane = new JScrollPane(_editorPane);
        }
        return _scrollPane;
    }
    public String getTitle() {
        return getDocument().getTitle();
    }
    public String getShortTitle() {
        return getTitle();
    }
}


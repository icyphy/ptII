/* Standardize tab spaces utilities.

Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2007-2009 The Regents of the University of California.
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
*/
package ptolemy.backtrack.eclipse.plugin.actions.codestyle;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;

//////////////////////////////////////////////////////////////////////////
//// StandardizeTabSpacesUtility

/**
 * Standardize tab spaces utilities.
 *
 * @author Thomas Huining Feng
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (tfeng)
 * @Pt.AcceptedRating Red (tfeng)
 */
public class StandardizeTabsSpacesUtility {

    public static void standardize(IEditorPart editor) {
        if (!(editor instanceof JavaEditor)) {
            return;
        }

        JavaEditor textEditor = (JavaEditor) editor;
        int caretPosition = textEditor.getViewer().getSelectedRange().x;

        IDocument document = textEditor.getDocumentProvider().getDocument(
                textEditor.getEditorInput());
        StringBuffer buffer = new StringBuffer(document.get());

        // Replace tabs with spaces
        for (int i = 0; i >= 0;) {
            i = buffer.indexOf("\t", i);
            if (i >= 0) {
                try {
                    document.replace(i, 1, "    ");
                    buffer.replace(i, i + 1, "    ");
                    if (i < caretPosition) {
                        caretPosition += 3;
                    }
                    i += 3;
                } catch (BadLocationException e) {
                    OutputConsole.outputError(e.getMessage());
                }
                i++;
            }
        }

        // Remove trailing spaces
        boolean finished = false;
        for (int eolPos = 0; !finished;) {
            char chr = '\0';

            int bufLength = buffer.length();
            int eolSize = 1;
            for (; eolPos < bufLength; eolPos++) {
                chr = buffer.charAt(eolPos);
                if (chr == '\n' || chr == '\r') {
                    break;
                }
            }

            if (eolPos == bufLength) {
                finished = true;
            } else {
                if (chr == '\r' && eolPos + 1 < bufLength
                        && buffer.charAt(eolPos + 1) == '\n') {
                    eolSize++;
                    eolPos++;
                }
            }

            int spaceStart = eolPos - eolSize;
            for (; spaceStart >= 0; spaceStart--) {
                if (buffer.charAt(spaceStart) != ' ') {
                    break;
                }
            }
            if (spaceStart + eolSize < eolPos) {
                try {
                    document.replace(spaceStart + 1, eolPos
                            - (spaceStart + eolSize), "");
                    buffer.replace(spaceStart + 1, eolPos - eolSize + 1, "");
                    if (eolPos < caretPosition) {
                        caretPosition -= eolPos - (spaceStart + eolSize);
                    } else if (spaceStart < caretPosition
                            && eolPos >= caretPosition) {
                        caretPosition -= caretPosition - (spaceStart + 1);
                    }
                    eolPos = spaceStart + eolSize;
                } catch (BadLocationException e) {
                    OutputConsole.outputError(e.getMessage());
                }
            }
            eolPos++;
        }

        // Add a new line to the end of file
        int docLength = document.getLength();
        try {
            if (docLength == 0
                    || (!document.get(docLength - 1, 1).equals("\n") && !document
                            .get(docLength - 1, 1).equals("\r"))) {
                document.replace(docLength, 0, "\n");
            }
        } catch (BadLocationException e) {
            OutputConsole.outputError(e.getMessage());
        }

        textEditor.getViewer().setSelectedRange(caretPosition, 0);
    }
}

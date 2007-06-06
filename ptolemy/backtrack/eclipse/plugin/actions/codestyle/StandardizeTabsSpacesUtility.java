package ptolemy.backtrack.eclipse.plugin.actions.codestyle;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;

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
        int caretOffset = 0, updateOffset = 0;
        
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
        caretOffset = 0;
        updateOffset = 0;
        boolean finished = false;
        for (int i = 0; !finished;) {
            i = buffer.indexOf("\n", i);
            if (i < 0) {
                i = buffer.length();
                finished = true;
            }
            if (i >= 0) {
                int k = i - 1;
                int endOfLineLength = 1;
                if (k >= 0 && buffer.charAt(k) == '\r') {
                    k--;
                    endOfLineLength++;
                }
                for (; k >= 0; k--) {
                    if (buffer.charAt(k) != ' ') {
                        break;
                    }
                }
                if (k + endOfLineLength < i) {
                    try {
                        document.replace(k + 1 + updateOffset,
                                i - (k + endOfLineLength), "");
                        if (i < caretPosition) {
                            caretOffset -= i - (k + endOfLineLength);
                        } else if (k < caretPosition && i >= caretPosition) {
                            caretOffset -= caretPosition - (k + 1);
                        }
                        updateOffset += k + endOfLineLength - i;
                    } catch (BadLocationException e) {
                        OutputConsole.outputError(e.getMessage());
                    }
                }
                i++;
            }
        }
        caretPosition = caretPosition + caretOffset;
        
        // Add a new line to the end of file
        int length = document.getLength();
        try {
            if (length == 0 || (!document.get(length - 1, 1).equals("\n")
                    && !document.get(length - 1, 1).equals("\r"))) {
                document.replace(length, 0, "\n");
            }
        } catch (BadLocationException e) {
            OutputConsole.outputError(e.getMessage());
        }
        
        textEditor.getViewer().setSelectedRange(caretPosition, 0);
    }
}

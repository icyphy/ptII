package ptolemy.backtrack.eclipse.plugin.actions.codestyle;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;

import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;

public class RemoveTabsUtility {

    public static void removeTabs(IEditorPart editor) {
        if (!(editor instanceof JavaEditor)) {
            return;
        }
        
        JavaEditor textEditor = (JavaEditor) editor;
        int caretPosition = textEditor.getViewer().getSelectedRange().x;
        
        IDocument document = textEditor.getDocumentProvider().getDocument(
                textEditor.getEditorInput());
        String content = document.get();
        int caretOffset = 0, updateOffset = 0;
        
        for (int i = 0; i >= 0;) {
            i = content.indexOf('\t', i);
            if (i >= 0) {
                try {
                    document.replace(i + updateOffset, 1, "    ");
                } catch (BadLocationException e) {
                    OutputConsole.outputError(e.getMessage());
                }
                if (i < caretPosition) {
                    caretOffset += 3;
                }
                i++;
                updateOffset += 3;
            }
        }
        if (!content.endsWith("\n")) {
            try {
                document.replace(document.getLength(), 0, "\n");
            } catch (BadLocationException e) {
                OutputConsole.outputError(e.getMessage());
            }
        }
        textEditor.getViewer().setSelectedRange(caretPosition + caretOffset, 0);
    }
}

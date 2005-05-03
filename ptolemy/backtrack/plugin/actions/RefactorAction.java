package ptolemy.backtrack.plugin.actions;

import java.io.PrintStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.console.MessageConsoleStream;

import ptolemy.backtrack.ast.Transformer;
import ptolemy.backtrack.plugin.EclipsePlugin;
import ptolemy.backtrack.plugin.console.OutputConsole;
import ptolemy.backtrack.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.plugin.util.Environment;

/**
 * Our sample action implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IWorkbenchWindowActionDelegate
 */
public class RefactorAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow window;
    /**
     * The constructor.
     */
    public RefactorAction() {
    }

    /**
     * The action has been activated. The argument of the
     * method represents the 'real' action sitting
     * in the workbench UI.
     * @see IWorkbenchWindowActionDelegate#run
     */
    public void run(IAction action) {
        if (!Environment.setupTransformerArguments(window.getShell(),
                true, false))
            return;
        
        String sourceList = Environment.getSourceList(window.getShell());
        if (sourceList == null)
            return;

        PrintStream oldSystemErr = System.err;
        OutputConsole console = EclipsePlugin.getDefault().getConsole();
        console.clearConsole();
        console.show();
        MessageConsoleStream outputStream =
            console.newMessageStream();
        outputStream.setColor(new Color(null, 0, 0, 255));
        MessageConsoleStream errorStream =
            console.newMessageStream();
        errorStream.setColor(new Color(null, 255, 0, 0));
        
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        String extraClassPaths = store.getString(
                            PreferenceConstants.BACKTRACK_EXTRA_CLASSPATHS);
        
        new TransformThread(
                new String[]{
                        "-classpath",
                        extraClassPaths,
                        "@" + sourceList
                },
                new AsyncPrintStream(outputStream),
                new AsyncPrintStream(errorStream)
        ).start();
    }

    /**
     * Selection in the workbench has been changed. We 
     * can change the state of the 'real' action here
     * if we want, but this can only happen after 
     * the delegate has been created.
     * @see IWorkbenchWindowActionDelegate#selectionChanged
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * We can use this method to dispose of any system
     * resources we previously allocated.
     * @see IWorkbenchWindowActionDelegate#dispose
     */
    public void dispose() {
    }

    /**
     * We will cache window object in order to
     * be able to provide parent shell for the message dialog.
     * @see IWorkbenchWindowActionDelegate#init
     */
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }
    
    private class TransformThread extends Thread {
        
        TransformThread(String[] args, PrintStream output, PrintStream error) {
            _args = args;
            _output = output;
            _error = error;
        }
        
        public void run() {
            PrintStream oldErr = null;
            if (_output != null) {
                oldErr = System.err;
                System.setErr(_output);
            }
            try {
                Transformer.main(_args);
                if (_error != null)
                    _error.println("Transformation finished.");
            } catch (Exception e) {
                if (_error != null)
                    e.printStackTrace(_error);
                EclipsePlugin.getStandardDisplay().asyncExec(
                        new ErrorDialogRunnable(e));
            } finally {
                if (oldErr != null)
                    System.setErr(oldErr);
                IPreferenceStore store = EclipsePlugin.getDefault()
                        .getPreferenceStore();
                try {
                    IContainer container = Environment.getAffectedFolder();
                    container.refreshLocal(
                            IResource.DEPTH_INFINITE, null);
                } catch (Exception e) {
                    OutputConsole.outputError(e.getMessage());
                }
            }
        }
        
        private String[] _args;
        
        private PrintStream _error;
        
        private PrintStream _output;
    }
    
    private class AsyncPrintStream extends PrintStream {
        
        AsyncPrintStream(MessageConsoleStream stream) {
            super(stream, true);
            _stream = stream;
        }
        
        public void println(String s) {
            _display.syncExec(new PrintRunnable(s));
        }
        
        private void _println(String s) {
            _stream.println(s);
        }
        
        private Display _display = EclipsePlugin.getStandardDisplay();
        
        private MessageConsoleStream _stream;
        
        private class PrintRunnable implements Runnable {
            
            PrintRunnable(String s) {
                _s = s;
            }
            
            public void run() {
                _println(_s);
            }
            
            private String _s;
        }
    }
    
    private class ErrorDialogRunnable implements Runnable {
        ErrorDialogRunnable(Throwable t) {
            _t = t;
        }

        public void run() {
            MessageDialog.openError(window.getShell(),
                        "Error in Refactoring",
                        _t.getMessage());
        }
        
        private Throwable _t;
    }
}
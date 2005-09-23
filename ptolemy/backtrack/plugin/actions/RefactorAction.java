/* 

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.plugin.actions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
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
import ptolemy.backtrack.util.Strings;

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
        IDocument document = console.getDocument();
        try {
            document.replace(0, document.getLength(), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        console.show();
        MessageConsoleStream outputStream =
            console.newMessageStream();
        outputStream.setColor(new Color(null, 0, 0, 255));
        MessageConsoleStream errorStream =
            console.newMessageStream();
        errorStream.setColor(new Color(null, 255, 0, 0));
        
        IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workspace.getProjects();
        String[] ptojectPaths = new String[projects.length];
        for (int i = 0; i < projects.length; i++)
            ptojectPaths[0] = projects[i].getLocation().toOSString();
        
        String extraClassPaths = ptojectPaths.length > 0 ?
                Strings.encodeFileNames(ptojectPaths) + File.pathSeparator :
                "";
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        extraClassPaths = extraClassPaths + store.getString(
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
            // Just construct with a dummy output stream (never used).
            super(new ByteArrayOutputStream(), true);
            _stream = stream;
        }
        
        public boolean checkError() {
            return false;
        }
        
        public void close() {
        }
        
        public void flush() {
            _display.syncExec(new PrintRunnable(null, PrintRunnable.FLUSH));
        }
        
        public void write(int b) {
            print((char)b);
        }
        
        public void write(byte[] buffer, int offset, int length) {
            print(new String(buffer, offset, length));
        }
        
        public void print(boolean b) {
            print(Boolean.toString(b));
        }
        
        public void print(char c) {
            print(Character.toString(c));
        }
        
        public void print(int i) {
            print(Integer.toString(i));
        }
        
        public void print(long l) {
            print(Long.toString(l));
        }
        
        public void print(float f) {
            print(Float.toString(f));
        }
        
        public void print(double d) {
            print(Double.toString(d));
        }
        
        public void print(char[] s) {
            print(new String(s));
        }
        
        public void print(String s) {
            _display.syncExec(new PrintRunnable(s, PrintRunnable.PRINT));
        }
        
        public void print(Object obj) {
            print(obj.toString());
        }
        
        public void println() {
            println("");
        }
        
        public void println(boolean b) {
            println(Boolean.toString(b));
        }
        
        public void println(char c) {
            println(Character.toString(c));
        }
        
        public void println(int i) {
            println(Integer.toString(i));
        }
        
        public void println(long l) {
            println(Long.toString(l));
        }
        
        public void println(float f) {
            println(Float.toString(f));
        }
        
        public void println(double d) {
            println(Double.toString(d));
        }
        
        public void println(char[] s) {
            println(new String(s));
        }
        
        public void println(String s) {
            _display.syncExec(new PrintRunnable(s, PrintRunnable.PRINTLN));
        }
        
        public void println(Object o) {
            println(o.toString());
        }
        
        private Display _display = EclipsePlugin.getStandardDisplay();
        
        private MessageConsoleStream _stream;
        
        private class PrintRunnable implements Runnable {
            
            static final int FLUSH   = 0;
            static final int PRINT   = 1;
            static final int PRINTLN = 2;
            
            PrintRunnable(String s, int operation) {
                _s = s;
                _operation = operation;
            }
            
            public void run() {
                switch (_operation) {
                case PRINT:
                    _stream.print(_s);
                case PRINTLN:
                    _stream.println(_s);
                }
                
                // Flush every time, does not work for 3.0.
                /*try {
                    _stream.flush();
                } catch (IOException e) {
                }*/
            }
            
            private String _s;
            
            private int _operation;
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

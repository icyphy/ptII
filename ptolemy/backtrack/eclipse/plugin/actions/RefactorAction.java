/* Action to trigger Java source batch transformation.

@Copyright (c) 1998-2013 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY



 */
package ptolemy.backtrack.eclipse.plugin.actions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

import ptolemy.backtrack.eclipse.ast.Transformer;
import ptolemy.backtrack.eclipse.plugin.EclipsePlugin;
import ptolemy.backtrack.eclipse.plugin.console.OutputConsole;
import ptolemy.backtrack.eclipse.plugin.preferences.PreferenceConstants;
import ptolemy.backtrack.eclipse.plugin.util.Environment;
import ptolemy.backtrack.util.Strings;

///////////////////////////////////////////////////////////////////
//// RefactorAction
/**
 Action to trigger Java source batch transformation. When triggered, the
 source transformer will be invoked to transform all the source files listed
 in the preferences, and place the generated class in the correct packages.

 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RefactorAction implements IWorkbenchWindowActionDelegate {

    /** Dispose of system resources allocated for this actions.
     */
    public void dispose() {
    }

    /** Initialize the action with a window as its parent.
     *
     *  @param window The parent window.
     */
    public void init(IWorkbenchWindow window) {
        _window = window;
    }

    /** Activate the action and transform the listed Java source files.
     *
     *  @param action The action proxy (not used in this method).
     */
    public void run(IAction action) {
        if (!Environment.setupTransformerArguments(_window.getShell(), true,
                false)) {
            return;
        }

        String sourceList = Environment.getSourceList(_window.getShell());

        if (sourceList == null) {
            return;
        }

        OutputConsole console = EclipsePlugin.getDefault().getConsole();
        IDocument document = console.getDocument();

        // Clear the document.
        try {
            document.replace(0, document.getLength(), "");
        } catch (Exception e) {
            EclipsePlugin.getStandardDisplay().asyncExec(
                    new ErrorDialogRunnable(e));
        }

        console.show();

        MessageConsoleStream outputStream = console.newMessageStream();
        outputStream.setColor(new Color(null, 0, 0, 255));

        MessageConsoleStream errorStream = console.newMessageStream();
        errorStream.setColor(new Color(null, 255, 0, 0));

        IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projects = workspace.getProjects();
        String[] ptojectPaths = new String[projects.length];

        for (int i = 0; i < projects.length; i++) {
            ptojectPaths[0] = projects[i].getLocation().toOSString();
        }

        String extraClassPaths = (ptojectPaths.length > 0) ? (Strings
                .encodeFileNames(ptojectPaths) + File.pathSeparator) : "";
        IPreferenceStore store = EclipsePlugin.getDefault()
                .getPreferenceStore();
        extraClassPaths = extraClassPaths
                + store
                        .getString(PreferenceConstants.BACKTRACK_EXTRA_CLASSPATHS);

        new TransformThread(new String[] { "-classpath", extraClassPaths,
                "@" + sourceList }, new AsyncPrintStream(outputStream),
                new AsyncPrintStream(errorStream)).start();
    }

    /** Handle the change of selection.
     *
     *  @param action The action proxy (not used in this method).
     *  @param selection The new selection (not used in this method).
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /** The parent window.
     */
    private IWorkbenchWindow _window;

    ///////////////////////////////////////////////////////////////////
    //// AsyncPrintStream

    /**
     The subclass of PrintStream that asynchronously sends the output to the
     Eclipse console.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class AsyncPrintStream extends PrintStream {

        /** Check whether there is an IO error. This method always returns
         *  false.
         *
         *  @return true if there is an error; false, otherwise.
         */
        public boolean checkError() {
            return false;
        }

        /** Close the print stream.
         */
        public void close() {
        }

        /** Flush the output. This is asynchronously done in the background.
         */
        public void flush() {
            _display.syncExec(new PrintRunnable(null, PrintRunnable.FLUSH));
        }

        /** Print the string representation of an object to the stream.
         *
         *  @param object The object.
         */
        public void print(Object object) {
            print(object.toString());
        }

        /** Print a string to the stream.
         *
         *  @param s The string to write.
         */
        public void print(String s) {
            _display.syncExec(new PrintRunnable(s, PrintRunnable.PRINT));
        }

        /** Print a boolean value to the stream.
         *
         *  @param b The boolean value.
         */
        public void print(boolean b) {
            print(Boolean.toString(b));
        }

        /** Print a char to the stream.
         *
         *  @param c The char to write.
         */
        public void print(char c) {
            print(Character.toString(c));
        }

        /** Print an array of chars to the stream.
         *
         *  @param s The array of chars to write.
         */
        public void print(char[] s) {
            print(new String(s));
        }

        /** Print a double value to the stream.
         *
         *  @param d The double value to write.
         */
        public void print(double d) {
            print(Double.toString(d));
        }

        /** Print a float value to the stream.
         *
         *  @param f The float value to write.
         */
        public void print(float f) {
            print(Float.toString(f));
        }

        /** Print an integer to the stream.
         *
         *  @param i The integer to write.
         */
        public void print(int i) {
            print(Integer.toString(i));
        }

        /** Print a long value to the stream.
         *
         *  @param l The long value to write.
         */
        public void print(long l) {
            print(Long.toString(l));
        }

        /** Print an empty line to the stream.
         */
        public void println() {
            println("");
        }

        /** Print the string representation of an object to the stream, and
         *  append a new line character.
         *
         *  @param object The object.
         */
        public void println(Object object) {
            println(object.toString());
        }

        /** Print a string to the stream, and append a new line character.
         *
         *  @param s The string to write.
         */
        public void println(String s) {
            _display.syncExec(new PrintRunnable(s, PrintRunnable.PRINTLN));
        }

        /** Print a boolean value to the stream, and append a new line
         *  character.
         *
         *  @param b The boolean value.
         */
        public void println(boolean b) {
            println(Boolean.toString(b));
        }

        /** Print a char to the stream, and append a new line character.
         *
         *  @param c The char to write.
         */
        public void println(char c) {
            println(Character.toString(c));
        }

        /** Print an array of chars to the stream, and append a new line
         *  character.
         *
         *  @param s The array of chars to write.
         */
        public void println(char[] s) {
            println(new String(s));
        }

        /** Print a double value to the stream, and append a new line character.
         *
         *  @param d The double value to write.
         */
        public void println(double d) {
            println(Double.toString(d));
        }

        /** Print a float value to the stream, and append a new line character.
         *
         *  @param f The float value to write.
         */
        public void println(float f) {
            println(Float.toString(f));
        }

        /** Print an integer to the stream, and append a new line character.
         *
         *  @param i The integer to write.
         */
        public void println(int i) {
            println(Integer.toString(i));
        }

        /** Print a long value to the stream, and append a new line character.
         *
         *  @param l The long value to write.
         */
        public void println(long l) {
            println(Long.toString(l));
        }

        /** Write a part of the byte buffer to the stream.
         *
         *  @param buffer The buffer.
         *  @param offset The starting offset.
         *  @param length The length.
         */
        public void write(byte[] buffer, int offset, int length) {
            print(new String(buffer, offset, length));
        }

        /** Write a char in the integer to the stream.
         *
         *  @param i The integer representation of the char.
         */
        public void write(int i) {
            print((char) i);
        }

        /** Construct a stream that asynchronously sends the output to the given
         *  stream of the Eclipse console.
         *
         *  @param stream The stream of the Eclipse console.
         */
        AsyncPrintStream(MessageConsoleStream stream) {
            // Just construct with a dummy output stream for the superclass,
            // which is never used.
            super(new ByteArrayOutputStream(), true);
            _stream = stream;
        }

        /** Eclipse's standard display object.
         */
        private Display _display = EclipsePlugin.getStandardDisplay();

        /** The stream of the Eclipse console.
         */
        private MessageConsoleStream _stream;

        //////////////////////////////////////////////////////////////////////////
        //// PrintRunnable

        /**
         The runnable object to execute asynchronously with the Eclipse GUI
         thread. It produces output to the Eclipse console.

         @author Thomas Feng
         @version $Id$
         @since Ptolemy II 5.1
         @Pt.ProposedRating Red (tfeng)
         @Pt.AcceptedRating Red (tfeng)
         */
        private class PrintRunnable implements Runnable {

            /** Print the output.
             */
            public void run() {
                switch (_operation) {
                case PRINT:
                    _stream.print(_s);

                case PRINTLN:
                    _stream.println(_s);
                }

                // Flush every time, does not work with Eclipse 3.0.

                try {
                    _stream.flush();
                } catch (IOException e) {
                }
            }

            /** The operation to flush the output.
             */
            public static final int FLUSH = 0;

            /** The operation to print a string.
             */
            public static final int PRINT = 1;

            /** The operation to print a string with a new line character.
             */
            public static final int PRINTLN = 2;

            /** Construct a runnable to print the output to the Eclipse console.
             *
             *  @param s The string to print.
             *  @param operation The operation ({@link #FLUSH}, {@link #PRINT}
             *   or {@link #PRINTLN}).
             */
            PrintRunnable(String s, int operation) {
                _s = s;
                _operation = operation;
            }

            /** The operation to perform.
             */
            private int _operation;

            /** The string to print.
             */
            private String _s;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// ErrorDialogRunnable

    /**
     The runnable object to asynchronously pop up an error dialog.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class ErrorDialogRunnable implements Runnable {

        /** Open the error dialog.
         */
        public void run() {
            MessageDialog.openError(_window.getShell(), "Error in Refactoring",
                    _t.getMessage());
        }

        /** Construct an error dialog with a cause.
         *
         *  @param t The cause of the error.
         */
        ErrorDialogRunnable(Throwable t) {
            _t = t;
        }

        /** The cause of the error.
         */
        private Throwable _t;
    }

    ///////////////////////////////////////////////////////////////////
    //// TransformThread

    /**
     The thread to execute the transformation.

     @author Thomas Feng
     @version $Id$
     @since Ptolemy II 5.1
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class TransformThread extends Thread {

        /** Execute the transformation in the thread.
         */
        public void run() {
            PrintStream oldErr = null;

            if (_output != null) {
                oldErr = System.err;
                System.setErr(_output);
            }

            try {
                Transformer.main(_args);

                if (_error != null) {
                    _error.println("Transformation finished.");
                }
            } catch (Exception e) {
                if (_error != null) {
                    e.printStackTrace(_error);
                }

                EclipsePlugin.getStandardDisplay().asyncExec(
                        new ErrorDialogRunnable(e));
            } finally {
                // IPreferenceStore store = EclipsePlugin.getDefault()
                //         .getPreferenceStore();

                try {
                    IContainer container = Environment.getAffectedFolder();
                    container.refreshLocal(IResource.DEPTH_INFINITE, null);
                } catch (Exception e) {
                    e.printStackTrace(_error);
                }
                _output.flush();
                _error.flush();

                if (oldErr != null) {
                    System.setErr(oldErr);
                }
            }
        }

        /** Construct a thread to execute the transformation.
         *
         *  @param args The command-line arguments to the transformer's main
         *   function ({@link Transformer#main(String[])}).
         *  @param output The stream where the normal console output is sent, or
         *   null if the output is ignored.
         *  @param error The stream where the error messages are sent, or null
         *   if the error messages are ignored.
         */
        TransformThread(String[] args, PrintStream output, PrintStream error) {
            _args = args;
            _output = output;
            _error = error;
        }

        /** The command-line arguments to the transformer's main function
         *  ({@link Transformer#main(String[])}).
         */
        private String[] _args;

        /** The stream where the error messages are sent.
         */
        private PrintStream _error;

        /** The stream where the normal console output is sent.
         */
        private PrintStream _output;
    }
}

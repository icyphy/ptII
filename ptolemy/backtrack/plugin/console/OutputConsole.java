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
package ptolemy.backtrack.plugin.console;

import org.eclipse.swt.graphics.Color;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.internal.console.ConsoleManager;

import ptolemy.backtrack.plugin.EclipsePlugin;

//////////////////////////////////////////////////////////////////////////
//// OutputConsole

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class OutputConsole extends MessageConsole implements IConsoleListener {
    public OutputConsole() {
        super(
                "Ptolemy II Backtracking",
                EclipsePlugin
                        .getImageDescriptor("ptolemy/backtrack/plugin/images/ptolemy.gif"));
        register();
    }

    public void register() {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
        IConsole[] existing = manager.getConsoles();
        boolean exists = false;

        for (int i = 0; i < existing.length; i++) {
            if (existing[i] == this) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            manager.addConsoles(new IConsole[] { this });
        }
    }

    public void unregister() {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
        manager.removeConsoles(new IConsole[] { this });
    }

    public void consolesAdded(IConsole[] consoles) {
    }

    public void consolesRemoved(IConsole[] consoles) {
    }

    public void show() {
        IConsoleManager manager = ConsolePlugin.getDefault()
                .getConsoleManager();
        manager.showConsoleView(this);
    }

    public static void outputError(String message) {
        outputMessage(message, new Color(null, 255, 0, 0));
    }

    public static void outputMessage(String message) {
        outputMessage(message, new Color(null, 0, 0, 255));
    }

    public static void outputMessage(String message, Color color) {
        EclipsePlugin.getStandardDisplay().syncExec(
                new OutputMessageThread(message, color));
    }

    private static class OutputMessageThread implements Runnable {
        OutputMessageThread(String message, Color color) {
            _message = message;
            _color = color;
        }

        public void run() {
            OutputConsole console = EclipsePlugin.getDefault().getConsole();
            MessageConsoleStream outputStream = console.newMessageStream();
            outputStream.setColor(_color);
            console.show();
            outputStream.print(_message + "\n");
        }

        private String _message;

        private Color _color;
    }

    private IConsoleManager _consoleManager = ConsolePlugin.getDefault()
            .getConsoleManager();
}

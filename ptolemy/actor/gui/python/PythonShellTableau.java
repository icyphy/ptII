/* A tableau for evaluating Python expression interactively.

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.gui.python;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// PythonShellTableau

/**
   A tableau that provides an interactive shell for evaluating Python expressions.
   @author Christopher Hylands and Edward A. Lee
   @version $Id$
   @since Ptolemy II 3.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class PythonShellTableau extends Tableau implements ShellInterpreter {
    /** Create a new tableau.
     *  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public PythonShellTableau(PythonShellEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        PythonShellFrame frame = new PythonShellFrame(this);
        setFrame(frame);

        // FIXME: this would be a good place to read in init.py
        //        _interpreter.execfile(ptolemy.data.expr.UtilityFunctions
        //                              .findFile("ptolemy/actor/gui/python/init.py");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    public String evaluateCommand(String command) throws Exception {
        try {
            PyObject results = _interpreter.eval(command);
            return results.toString();
        } catch (Throwable throwable) {
            return throwable.toString();
        }
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True.
     */
    public boolean isCommandComplete(String command) {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The parameter used for evaluation.
    // FIXME: Perhaps the interpreter should be in its own thread?
    private PythonInterpreter _interpreter = new PythonInterpreter();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of PythonShellTableau.
     */
    public class PythonShellFrame extends TableauFrame {
        /** Construct a frame to display the PythonShell window.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public PythonShellFrame(Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
            super(tableau);

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

            ShellTextArea shellPanel = new ShellTextArea();
            shellPanel.setInterpreter(PythonShellTableau.this);
            component.add(shellPanel);
            getContentPane().add(component, BorderLayout.CENTER);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         protected methods                 ////
        protected void _help() {
            try {
                URL doc = getClass().getClassLoader().getResource("ptolemy/actor/gui/python/help.htm");
                getConfiguration().openModel(null, doc, doc.toExternalForm());
            } catch (Exception ex) {
                System.out.println("PythonShellTableau._help(): " + ex);
                _about();
            }
        }
    }

    /** A factory that creates a control panel to display a Tcl Shell
     */
    public static class Factory extends TableauFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this attribute.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public Factory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Create a new instance of PythonShellTableau in the specified
         *  effigy. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *  @param effigy The model effigy.
         *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        public Tableau createTableau(Effigy effigy) throws Exception {
            // NOTE: Can create any number of tableaux within the same
            // effigy.  Is this what we want?
            if (effigy instanceof PythonShellEffigy) {
                return new PythonShellTableau((PythonShellEffigy) effigy,
                        "PythonShellTableau");
            } else {
                return null;
            }
        }
    }
}

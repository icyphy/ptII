/* A tableau for evaluating Tcl expression interactively.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.actor.gui.ptjacl;

import java.awt.BorderLayout;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TclException;

///////////////////////////////////////////////////////////////////
//// TclShellTableau

/**
 A tableau that provides a Tcl Shell for interacting with Ptjacl,
 a 100% Java implementation of Tcl.

 @author Christopher Hylands and Edward A. Lee
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TclShellTableau extends Tableau implements ShellInterpreter {
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
    public TclShellTableau(TclShellEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        TclShellFrame frame = new TclShellFrame(this);
        setFrame(frame);

        try {
            //
            _tclInterp.setVar("panelShell", ReflectObject.newInstance(
                    _tclInterp, ShellTextArea.class, frame.shellTextArea), 0);
            _tclInterp.eval("proc puts {s} {" + "global panelShell; "
                    + "$panelShell appendJTextArea $s\\n}");

            // FIXME: what about user initializations in ~/.tclrc?
            // Source Ptolemy specific initializations.
            _tclInterp
            .eval("if [catch {source [java::call ptolemy.data.expr.UtilityFunctions findFile \"ptolemy/actor/gui/ptjacl/init.tcl\"]} errMsg ] { puts $errorInfo};");
        } catch (TclException ex) {
            throw new IllegalActionException(this, ex,
                    "Could not initialize the " + "tcl interpreter:\n"
                            + _tclInterp.getResult().toString());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    @Override
    public String evaluateCommand(String command) throws Exception {
        try {
            _tclInterp.eval(command);
            return _tclInterp.getResult().toString();
        } catch (TclException ex) {
            return _tclInterp.getVar("errorInfo", null, 0).toString();
        }
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True if the command is complete.
     */
    @Override
    public boolean isCommandComplete(String command) {
        return Interp.commandComplete(command);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The Tcl interpreter
    // FIXME: Perhaps the interpreter should be in its own thread?
    private Interp _tclInterp = new Interp();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of TclShellTableau.
     */
    @SuppressWarnings("serial")
    public static class TclShellFrame extends TableauFrame {
        // FindBugs suggested refactoring this into a static class.

        /** Construct a frame to display the TclShell window.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param tclShellTableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public TclShellFrame(TclShellTableau tclShellTableau)
                throws IllegalActionException, NameDuplicationException {
            super(tclShellTableau);

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

            shellTextArea = new ShellTextArea();
            shellTextArea.setInterpreter(tclShellTableau);
            shellTextArea.mainPrompt = "% ";
            component.add(shellTextArea);
            getContentPane().add(component, BorderLayout.CENTER);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public variables                  ////

        /** The text area tableau used for input and output. */
        public ShellTextArea shellTextArea;

        ///////////////////////////////////////////////////////////////////
        ////                         protected methods                 ////
        @Override
        protected void _help() {
            try {
                URL doc = getClass().getClassLoader().getResource(
                        "ptolemy/actor/gui/ptjacl/help.htm");
                getConfiguration().openModel(null, doc, doc.toExternalForm());
            } catch (Exception ex) {
                System.out.println("TclShellTableau._help(): " + ex);
                _about();
            }
        }
    }

    /** A factory that creates a control panel to display a Tcl Shell.
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

        /** Create a new instance of TclShellTableau in the specified
         *  effigy. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *  @param effigy The model effigy.
         *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
         */
        @Override
        public Tableau createTableau(Effigy effigy) throws Exception {
            // NOTE: Can create any number of tableaux within the same
            // effigy.  Is this what we want?
            if (effigy instanceof TclShellEffigy) {
                return new TclShellTableau((TclShellEffigy) effigy,
                        "TclShellTableau");
            } else {
                return null;
            }
        }
    }
}

/* A tableau for interacting with Ptjacl, the 100% Java implementation of Tcl.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.ptjacl;

import tcl.lang.*;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFactory;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.util.KernelRuntimeException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Panel;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// TclShellTableau
/**
A tableau that provides a Tcl Shell for interacting with Ptjacl,
a 100% Java implementation of Tcl

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class TclShellTableau extends Tableau implements ShellInterpreter {

    /** Create a new Tcl Shell Tableau for use with Tcl commands.
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
    public TclShellTableau(PtolemyEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        NamedObj model = container.getModel();

	TclShellFrame frame = new TclShellFrame((CompositeEntity)model, this);
	setFrame(frame);

	try {
	    // FIXME: Perhaps the interpreter should be in its own thread?
	    _tclInterp.setVar("panelShell",
                    ReflectObject.newInstance(_tclInterp,
                    ShellTextArea.class,
                    this), 0);
	    _tclInterp.eval("proc puts {s} {"
                    + "global panelShell; "
                    + "$panelShell appendJTextArea $s\\n}");
	} catch (TclException e) {
            // FIXME: Should perhaps throw an exception here?
	    System.out.println(_tclInterp.getResult());
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    public String evaluateCommand(String command) throws Exception {
        _tclInterp.eval(command);
        return _tclInterp.getResult().toString();
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True if the command is complete.
     */
    public boolean isCommandComplete(String command) {
        return _tclInterp.commandComplete(command);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The Tcl interpreter
    private Interp _tclInterp = new Interp();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by an instance of TclShellTableau.
     */
    public class TclShellFrame extends PtolemyFrame {

	/** Construct a frame to display the TclShell window.
	 *  After constructing this, it is necessary
	 *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
	 *  @param model The model to put in this frame, or null if none.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
	 */
	public TclShellFrame(final CompositeEntity model, Tableau tableau)
                throws IllegalActionException, NameDuplicationException {
	    super(model, tableau);

            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));

	    ShellTextArea tclShellPanel = new ShellTextArea();
            tclShellPanel.setInterpreter(TclShellTableau.this);
	    component.add(tclShellPanel);
            getContentPane().add(component, BorderLayout.CENTER);
	}

	///////////////////////////////////////////////////////////////////
	////                         protected methods                 ////

	protected void _help() {
	    try {
		URL doc = getClass().getClassLoader().getResource(
                        "doc/coding/tcljava.htm");
		getConfiguration().openModel(null, doc, doc.toExternalForm());
	    } catch (Exception ex) {
		System.out.println("TclShellTableau._help(): " + ex);
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

	/** Create a new instance of TclShellTableau in the specified
         *  effigy. If the specified effigy is not an
         *  instance of PtolemyEffigy, then do not create a tableau
         *  and return null. It is the responsibility of callers of
         *  this method to check the return value and call show().
         *
	 *  @param effigy The model effigy.
	 *  @return A new control panel tableau if the effigy is
         *    a PtolemyEffigy, or null otherwise.
         *  @exception Exception If the factory should be able to create a
         *   tableau for the effigy, but something goes wrong.
	 */
	public Tableau createTableau(Effigy effigy) throws Exception {
	    if (effigy instanceof PtolemyEffigy) {
                // First see whether the effigy already contains a tableau
                TclShellTableau tableau =
                    (TclShellTableau)effigy.getEntity("TclShellTableau");
                if (tableau == null) {
		    try {
			tableau = new TclShellTableau(
				      (PtolemyEffigy)effigy,
				      "TclShellTableau");
		    } catch (NoClassDefFoundError noClassDefFoundError) {
			// Catch the error here.
			KernelRuntimeException kernelRuntimeException =
			    new KernelRuntimeException(this, null,
                                    noClassDefFoundError, null);
			// MessageHandler.error() does not take an Error
			// argument as the second argument, so we create
			// a KernelRuntimeException.
                        MessageHandler.error("Cannot create TclShellTableau. "
					     + "Perhaps $PTII/lib/ptjacl.jar "
					     + "is not in your path?: ",
					     kernelRuntimeException);

		    }
                }
                return tableau;
	    } else {
		return null;
	    }
	}
    }
}

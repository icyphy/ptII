/* An interactive shell that reads and writes strings.

@Copyright (c) 1998-2003 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.gui;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ExpressionShellEffigy;
import ptolemy.actor.gui.ExpressionShellFrame;
import ptolemy.actor.gui.ExpressionShellTableau;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.ShellTextArea;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.awt.Container;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// InteractiveShell
/**
This actor creates a command shell on the screen, sending commands
that are typed by the user to its output port, and reporting strings
received at its input by displaying them.  Each time it fires, it
reads the input, displays it, then displays a command prompt
(which by default is "&gt;&gt"), and waits for a command to be
typed.  The command is terminated by an enter or return character,
which then results in the command being produced on the output.
In a typical use of this actor, it will be preceded by a SampleDelay
actor which will provide an initial welcome message or instructions.
The output will then be routed to some subsystem for processing,
and the result will be fed back to the input.
<p>
Note that because of complexities in Swing, if you resize the display
window, then, unlike the plotters, the new size will not be persistent.
That is, if you save the model and then re-open it, the new size is
forgotten.  The position, however, is persistent.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class InteractiveShell extends TypedAtomicActor
         implements Placeable, ShellInterpreter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public InteractiveShell(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);
                
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
        
        prompt = new PortParameter(this, "prompt",
                new StringToken(">>"));
        // Make command be a StringParameter (no surrounding double quotes).
        prompt.setTypeEquals(BaseType.STRING);
        prompt.setStringMode(true);

        _windowProperties = new WindowPropertiesAttribute(
                this, "_windowProperties");
                
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-20\" y=\"-20\" "
                + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n"
                + "<rect x=\"-14\" y=\"-14\" "
                + "width=\"28\" height=\"28\" "
                + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-10,-10, -5,-5, -10,0\" "
                + "style=\"stroke:black\"/>\n"
                + "<polyline points=\"-7,-10, -2,-5, -7,0\" "
                + "style=\"stroke:black\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public TypedIOPort input;
    
    /** The output port. */
    public TypedIOPort output;
    
    /** The prompt.
     * The initial default is the string >>.   
     * Double quotes are not necessary.
     */  
    public PortParameter prompt;

    /** The shell window object. */
    public ShellTextArea shell;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        InteractiveShell newObject = (InteractiveShell)super.clone(workspace);
        newObject.shell = null;
        newObject._container = null;
        newObject._frame = null;
        return newObject;
    }
    
    /** Evaluate the specified command.
     *  @param command The command.
     *  @return The return value of the command, or null if there is none.
     *  @exception Exception If something goes wrong processing the command.
     */
    public String evaluateCommand(String command) throws Exception {
        // NOTE: This method is typically called in the swing event thread.
        // Be careful to avoid locking up the UI.
        setOutput(command);
        // Return null to indicate that the command evaluation is not
        // complete.  This results in disabling editing on the text
        // widget until returnResult() is called on it, which happens
        // the next time fire() is called.
        return null;
    }
    
    /** Read and display the input, then
     *  wait for user input and produce the user data on the output.
     *  If the user input is "quit" or "exit", then set a flag that causes
     *  postfire() to return false.
     *  @exception IllegalActionException If producing the output
     *   causes an exception.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        prompt.update();
        shell.mainPrompt = ((StringToken)prompt.getToken()).stringValue();

        if (input.numberOfSources() > 0 && input.hasToken(0)) {
            String value = ((StringToken)input.get(0)).stringValue();
            // If window has been dismissed, there is nothing more to do.
            if (shell == null) return;
            if (_firstTime) {
                _firstTime = false;
                shell.initialize(value);
            } else {
                shell.returnResult(value);
            }
        }
        shell.setEditable(true);
        // FIXME: What to type if there is no input connected.
        String userCommand = getOutput();
        if (userCommand.trim().equalsIgnoreCase("quit")
                || userCommand.trim().equalsIgnoreCase("exit")) {
            _returnFalseInPostfire = true;
        }
        output.broadcast(new StringToken(userCommand));
    }
      
    /** Get the output string to be sent. This does not
     *  return until a value is entered on the shell by the user.
     *  @return The output string to be sent.
     */
    public synchronized String getOutput() {
        while (_outputValues.size() < 1 && !_stopRequested) {
            try {
                // NOTE: Do not call wait on this object directly!
                // If another thread tries to get write access to the
                // workspace, it will deadlock!  This method releases
                // all read accesses on the workspace before doing the
                // wait.
                workspace().wait(this);
            } catch (InterruptedException ex) {
            }
        }
        if (_stopRequested) {
            return("");
        } else {
            return((String)_outputValues.remove(0));
        }
    }
    
    /** If the shell has not already been created, create it.
     *  Then wait for user input and produce it on the output.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (shell == null) {
            // No container has been specified for the shell.
            // Place the shell in its own frame.
            // Need an effigy and a tableau so that menu ops work properly.
            Effigy containerEffigy = Configuration.findEffigy(toplevel());
            if (containerEffigy == null) {
                throw new IllegalActionException(this,
                        "Cannot find effigy for top level: "
                        + toplevel().getFullName());
            }
            try {
                ExpressionShellEffigy shellEffigy = new ExpressionShellEffigy(
                        containerEffigy, containerEffigy.uniqueName("shell"));
                // The default identifier is "Unnamed", which is no good for
                // two reasons: Wrong title bar label, and it causes a save-as
                // to destroy the original window.
                shellEffigy.identifier.setExpression(getFullName());
                ShellTableau tableau = new ShellTableau(
                        shellEffigy, "tableau");
                _frame = tableau.frame;
                shell = tableau.shell;
                shell.setInterpreter(this);
                // Prevent editing until the first firing.
                shell.setEditable(false);
            } catch (Exception ex) {
                throw new IllegalActionException(this, null, ex,
                        "Error creating effigy and tableau");
            }
            _windowProperties.setProperties(_frame);
            _frame.pack();
        } else {
            shell.clearJTextArea();
        }
        if (_frame != null) {
            // show() used to override manual placement by calling pack.
            // No more.
            _frame.show();
            _frame.toFront();
        }
        _firstTime = true;
        _returnFalseInPostfire = false;
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True.
     */
    public boolean isCommandComplete(String command) {
        return true;
    }
    
    /** Specify the container into which this shell should be placed.
     *  This method needs to be called before the first call to initialize().
     *  Otherwise, the shell will be placed in its own frame.
     *  The background of the plot is set equal to that of the container
     *  (unless it is null).
     *  @param container The container into which to place the shell, or
     *   null to specify that a new shell should be created.
     */
    public void place(Container container) {
        _container = container;
        
        if (_container == null) {
            // Dissociate with any container.
            // NOTE: _remove() doesn't work here.  Why?
            if (_frame != null) _frame.dispose();
            _frame = null;
            shell = null;
            return;
        }
        shell = new ShellTextArea();
        shell.setInterpreter(this);
        shell.clearJTextArea();
        shell.setEditable(false);

        _container.add(shell);
        // java.awt.Component.setBackground(color) says that
        // if the color "parameter is null then this component
        // will inherit the  background color of its parent."
        shell.setBackground(null);
    }
    
    /** Override the base class to return false if the user has typed
     *  "quit" or "exit".
     *  @return False if the user has typed "quit" or "exit".
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_returnFalseInPostfire) {
            return false;
        }
        return super.postfire();
    }

    /** Override the base class to remove the shell from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        Nameable previousContainer = getContainer();
        super.setContainer(container);
        if (container != previousContainer && previousContainer != null) {
            _remove();
        }
    }
    
    /** Specify an output string to be sent. This method
     *  appends the specified string to a queue. Strings
     *  are retrieved from the queue by getOutput().
     *  @see #getOutput()
     *  @param value An output string to be sent.
     */
    public synchronized void setOutput(String value) {
        _outputValues.add(value);
        notifyAll();
    }

    /** Override the base class to call notifyAll() to get out of
     *  any waiting.
     */
    public void stop() {
        super.stop();
        synchronized(this) {
            notifyAll();
        }
    }
    
    /** Override the base class to make the shell uneditable.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_returnFalseInPostfire && _frame != null) {
            _frame.dispose();
            _frame = null;
            shell = null;
        } else if (shell != null) {
            shell.setEditable(false);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object. This
     *  overrides the base class to make sure that the current frame
     *  properties, if there is a frame, are recorded.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        // Make sure that the current position of the frame, if any,
        // is up to date.
        if (_frame != null) {
            _windowProperties.recordProperties(_frame);
        }
        super._exportMoMLContents(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Container into which this plot should be placed. */
    private Container _container;
    
    /** Indicator of the first time through. */
    private boolean _firstTime = true;

    /** Frame into which plot is placed, if any. */
    private TableauFrame _frame;
    
    /** The list of strings to send to the output. */
    private List _outputValues = new LinkedList();
    
    /** Flag indicating that "exit" or "quit" has been entered. */
    private boolean _returnFalseInPostfire = false;
        
    // A specification for the window properties of the frame.
    private WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the shell from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (shell != null) {
                        if (_container != null) {
                            _container.remove(shell);
                            _container.invalidate();
                            _container.repaint();
                        } else if (_frame != null) {
                            _frame.dispose();
                        }
                    }
                }
            });
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of ExpressionShellTableau that records the size of
     *  the display when it is closed.
     */
    public class ShellTableau extends ExpressionShellTableau {
        
        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public ShellTableau(ExpressionShellEffigy container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            frame = new ShellFrame(this);
            setFrame(frame);
            frame.setTableau(this);
        }
    }
    
    /** The frame that is created by an instance of ShellTableau.
     */
    public class ShellFrame extends ExpressionShellFrame {

        /** Construct a frame to display the ExpressionShell window.
         *  Override the base class to handle window closing.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear.
         *  This is typically accomplished by calling show() on
         *  enclosing tableau.
         *  @param tableau The tableau responsible for this frame.
         *  @exception IllegalActionException If the model rejects the
         *   configuration attribute.
         *  @exception NameDuplicationException If a name collision occurs.
         */
        public ShellFrame(ExpressionShellTableau tableau)
                throws IllegalActionException, NameDuplicationException {
            super(tableau);
        }
                
        /** Overrides the base class to record
         *  the size and location of the frame.
         *  @return False if the user cancels on a save query.
         */
        protected boolean _close() {
            if (_frame != null) {
                _windowProperties.setProperties(_frame);
            }
            // Return value can be ignored since there is no issue of saving.
            super._close();
            place(null);
            return true;
        }
    }
}

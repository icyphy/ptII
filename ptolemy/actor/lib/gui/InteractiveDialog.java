/* An interactive shell that reads and writes strings.

 @Copyright (c) 1998-2016 The Regents of the University of California.
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
 */
package ptolemy.actor.lib.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ExpressionShellEffigy;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.TypeConstant;
import ptolemy.graph.Inequality;
import ptolemy.gui.ShellInterpreter;
import ptolemy.gui.UserDialog;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// InteractiveDialog

/**
 <p>
 This actor creates a window on the screen with a command entry box
 and a results display box. When the user types a command and terminates
 it with a return, this actor will emit an output with the value of that
 command and also display the command in the results display box.
 Any input that it receives it displays in the results display box.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class InteractiveDialog extends TypedAtomicActor implements Placeable,
        ShellInterpreter, UsesInvokeAndWait {
    
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public InteractiveDialog(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        terminateWithNewline = new Parameter(this, "terminateWithNewline");
        terminateWithNewline.setTypeEquals(BaseType.BOOLEAN);
        terminateWithNewline.setExpression("false");

        input = new TypedIOPort(this, "input", true, false);
        // Parameter to get Vergil to label the fileOrURL port.
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

        _windowProperties = new WindowPropertiesAttribute(this,
                "_windowProperties");
        // Note that we have to force this to be persistent because
        // there is no real mechanism for the value of the properties
        // to be updated when the window is moved or resized. By
        // making it persistent, when the model is saved, the
        // attribute will determine the current size and position
        // of the window and save it.
        _windowProperties.setPersistent(true);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-20\" y=\"-20\" " + "width=\"40\" height=\"40\" "
                + "style=\"fill:lightGrey\"/>\n" + "<rect x=\"-14\" y=\"-14\" "
                + "width=\"28\" height=\"28\" " + "style=\"fill:white\"/>\n"
                + "<polyline points=\"-10,-10, -5,-5, -10,0\" "
                + "style=\"stroke:black\"/>\n"
                + "<polyline points=\"-7,-10, -2,-5, -7,0\" "
                + "style=\"stroke:black\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. By default, this has undeclared type.
     *  If backward type inference is enabled, then it has type general.
     *  In either case, it can receive any data type. If it receives
     *  token of type string, it strips off the surrounding double
     *  quotes before displaying the value.
     */
    public TypedIOPort input;
    
    /** If true, append a newline to each output string.
     *  This is a boolean that defaults to false.
     */
    public Parameter terminateWithNewline;
    
    /** The output port. */
    public TypedIOPort output;

    /** The dialog window object. */
    public UserDialog userDialog;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        InteractiveDialog newObject = (InteractiveDialog) super.clone(workspace);
        newObject.userDialog = null;
        newObject._container = null;
        newObject._frame = null;

        // Findbugs:
        //  [M M IS] Inconsistent synchronization [IS2_INCONSISTENT_SYNC]
        // Actually this is not a problem since the object is
        // being created and hence nobody else has access to it.
        newObject._outputValues = new LinkedList<String>();

        try {
            Attribute old = newObject.getAttribute("_windowProperties");
            if (old != null) {
                old.setContainer(null);
            }
            newObject._windowProperties = new WindowPropertiesAttribute(
                    newObject, "_windowProperties");
            newObject._windowProperties.setPersistent(true);
        } catch (Exception ex) {
            // CloneNotSupportedException does not have a constructor
            // that takes a cause argument, so we use initCause
            CloneNotSupportedException throwable = new CloneNotSupportedException();
            throwable.initCause(ex);
            throw throwable;
        }
        return newObject;
    }

    /** Record the specified command and request a firing to send it to the
     *  output.
     *  @param command The command.
     *  @return Null to indicate that the command evaluation is not complete.
     *  @exception Exception If something goes wrong processing the command.
     */
    @Override
    public String evaluateCommand(String command) throws Exception {
        // NOTE: This method is typically called in the swing event thread.
        // Be careful to avoid locking up the UI.
        synchronized(this) {
            _outputValues.add(command);
        }
        // Request a firing.
        getDirector().fireAtCurrentTime(this);

        // Return null to indicate that the command evaluation is not
        // complete.
        return null;
    }

    /** Read and display any input, then if a new command is available,
     *  display it and produce it on the output.
     *  @exception IllegalActionException If producing the output
     *   causes an exception.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // If window has been dismissed, there is nothing more to do.
        if (userDialog == null) {
            return;
        }

        String value = null;
        while (input.numberOfSources() > 0 && input.hasToken(0)) {
            Token inputToken = input.get(0);
            if (inputToken instanceof StringToken) {
                // To get the value without surrounding quotation marks.
                value = ((StringToken) inputToken).stringValue();
            } else {
                value = inputToken.toString();
            }
        }
        if (value != null) {
            if (_firstTime) {
                _firstTime = false;
                userDialog.initialize(value);
            } else {
                userDialog.appendText(value);
            }
        }
        
        synchronized(this) {
            // For some reason, getExpression() returns an escaped string, "\\n",
            // so I need to fix that here.
            boolean terminate = ((BooleanToken)terminateWithNewline.getToken()).booleanValue();
            String format = "%s" + (terminate? "\n" : "");
            for (String command: _outputValues) {
                String formatted = String.format(format, command);
                output.broadcast(new StringToken(formatted));
            }
            _outputValues.clear();
        }
    }

    /** If the shell has not already been created, create it.
     *  Then wait for user input and produce it on the output.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        Runnable doInitialize = new Runnable() {
            @Override
            public void run() {

                if (userDialog == null) {
                    // No container has been specified for the shell.
                    // Place the shell in its own frame.
                    // Need an effigy and a tableau so that menu ops work properly.
                    Effigy containerEffigy = Configuration.findEffigy(toplevel());

                    if (containerEffigy == null) {
                        MessageHandler.error("Cannot find effigy for top level: "
                                + toplevel().getFullName());
                        return;
                    }

                    try {
                        // Similar enough: use ExpressionShellEffigy.
                        ExpressionShellEffigy shellEffigy = new ExpressionShellEffigy(
                                containerEffigy,
                                containerEffigy.uniqueName("interactiveDialog"));

                        // The default identifier is "Unnamed", which is no good for
                        // two reasons: Wrong title bar label, and it causes a save-as
                        // to destroy the original window.
                        shellEffigy.identifier.setExpression(getFullName());

                        _tableau = new DialogTableau(shellEffigy, "tableau");
                        _frame = _tableau.frame;
                        userDialog = _tableau.dialog;
                        userDialog.setInterpreter(InteractiveDialog.this);
                    } catch (Exception ex) {
                        MessageHandler.error(
                                "Error creating effigy and tableau "
                                        + InteractiveDialog.this.getFullName(),
                                        ex);
                        return;
                    }

                    _windowProperties.setProperties(_frame);
                    _frame.pack();
                } else {
                    // Clear the display.
                    userDialog.initialize("");
                }

                if (_frame != null) {
                    // show() used to override manual placement by calling pack.
                    // No more.
                    _frame.show();
                    _frame.toFront();
                }
            }
        };
        try {
            if (!SwingUtilities.isEventDispatchThread()) {
                // Block initialization until the window is created.
                SwingUtilities.invokeAndWait(doInitialize);
            } else {
                // Exporting HTML for
                // ptolemy/actor/lib/hoc/demo/ThreadedComposite/ConcurrentChat.xml
                // ends up running this in the Swing event dispatch
                // thread.
                doInitialize.run();
            }
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Failed to initialize.");
        }

        _firstTime = true;
    }

    /** Return true if the specified command is complete (ready
     *  to be interpreted).
     *  @param command The command.
     *  @return True.
     */
    @Override
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
    @Override
    public void place(Container container) {
        _container = container;

        if (_container == null) {
            // Dissociate with any container.
            // NOTE: _remove() doesn't work here.  Why?
            if (_frame != null) {
                _frame.dispose();
            }

            _frame = null;
            userDialog = null;
            return;
        }

        userDialog = new UserDialog();
        userDialog.setInterpreter(this);

        _container.add(userDialog);

        // java.awt.Component.setBackground(color) says that
        // if the color "parameter is null then this component
        // will inherit the  background color of its parent."
        userDialog.setBackground(null);
    }

    /** Override the base class to remove the shell from its graphical
     *  container if the argument is null.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the base class throws it.
     *  @exception NameDuplicationException If the base class throws it.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        Nameable previousContainer = getContainer();
        super.setContainer(container);

        if (container != previousContainer && previousContainer != null) {
            _remove();
        }
    }

    /** Set a name to present to the user.
     *  <p>If the Plot window has been rendered, then the title of the
     *  Plot window will be updated to the value of the name parameter.</p>
     *  @param name A name to present to the user.
     *  @see #getDisplayName()
     */
    @Override
    public void setDisplayName(String name) {
        super.setDisplayName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        if (_tableau != null) {
            _tableau.setTitle(name);
        }
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  <p>If the Plot window has been rendered, then the title of the
     *  Plot window will be updated to the value of the name parameter.</p>
     *  @param name The new name.
     *  @exception IllegalActionException If the name contains a period
     *   or if the object is a derived object and the name argument does
     *   not match the current name.
     *  @exception NameDuplicationException Not thrown in this base class.
     *   May be thrown by derived classes if the container already contains
     *   an object with this name.
     *  @see #getName()
     *  @see #getName(NamedObj)
     */
    @Override
    public void setName(String name) throws IllegalActionException,
    NameDuplicationException {
        super.setName(name);
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4302
        if (_tableau != null) {
            _tableau.setTitle(name);
        }
    }

    /** Override the base class to call notifyAll() to get out of
     *  any waiting.
     */
    @Override
    public void stop() {
        synchronized (this) {
            super.stop();
            notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the input port greater than or equal to
     *  <code>BaseType.GENERAL</code> in case backward type inference is
     *  enabled and the input port has no type declared.
     *
     *  @return A set of inequalities.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        HashSet<Inequality> result = new HashSet<Inequality>();
        if (isBackwardTypeInferenceEnabled()
                && input.getTypeTerm().isSettable()) {
            result.add(new Inequality(new TypeConstant(BaseType.GENERAL), input
                    .getTypeTerm()));
        }
        return result;
    }

    /** Write a MoML description of the contents of this object. This
     *  overrides the base class to make sure that the current frame
     *  properties, if there is a frame, are recorded.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    @Override
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
    private List<String> _outputValues = new LinkedList<String>();

    /** The version of ExpressionShellTableau that creates a Shell window. */
    private DialogTableau _tableau;

    // A specification for the window properties of the frame.
    private WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Remove the shell from the current container, if there is one.
     */
    private void _remove() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (userDialog != null) {
                    if (_container != null) {
                        _container.remove(userDialog);
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
    public class DialogTableau extends Tableau {
        /** Construct a new tableau for the model represented by the
         *  given effigy.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container does not accept
         *   this entity (this should not occur).
         *  @exception NameDuplicationException If the name coincides with an
         *   attribute already in the container.
         */
        public DialogTableau(ExpressionShellEffigy container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            dialog = new UserDialog();
            dialog.setInterpreter(InteractiveDialog.this);
            frame = new DialogFrame(this);
            frame.setTableau(this);
            setFrame(frame);
        }

        /** The frame */
        public DialogFrame frame;

        /** The UserDialog. */
        public UserDialog dialog;
    }

    /** The frame that is created by an instance of ShellTableau.
     */
    @SuppressWarnings("serial")
    public class DialogFrame extends TableauFrame {
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
        public DialogFrame(DialogTableau tableau)
                throws IllegalActionException, NameDuplicationException {
            super(tableau);
            
            JPanel component = new JPanel();
            component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
            component.add(tableau.dialog);
            getContentPane().add(component, BorderLayout.CENTER);
        }

        /** Overrides the base class to record
         *  the size and location of the frame.
         *  @return False if the user cancels on a save query.
         */
        @Override
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

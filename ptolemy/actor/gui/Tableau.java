/* An object that represents a graphical view of a ptolemy model.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// Tableau
/**
A tableau is a visual representation of a Ptolemy II model in a top-level
window.  This class represents such a top level window.  The top-level
is always a frame, which is a window with a border and title bar. The
window itself is specified by the setFrame() method, and accessed by
the getFrame() method.  An instance of this class will be contained
by the instance of Effigy that represents the model that is depicted
in the top-level window.
<p>
By convention, the constructor for a tableau does not (necessarily)
make the associated frame visible.  To do that, call show().

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see Effigy
*/
public class Tableau extends CompositeEntity {

    /** Construct a tableau in the specified workspace.
     *  @param workspace The workspace.
     *  @exception IllegalActionException If an error occurs creating
     *   the size attribute (should not occur).
     *  @exception NameDuplicationException If the base class has already
     *   created an attribute with name "size" (should not occur).
     */
    public Tableau(Workspace workspace)
        throws IllegalActionException, NameDuplicationException {
        super(workspace);

        size = new SizeAttribute(this, "size");
    }

    /** Construct a tableau with the given name and container.
     *  @param container The container.
     *  @param name The name of the tableau.
     *  @exception IllegalActionException If the tableau cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Tableau(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        size = new SizeAttribute(this, "size");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public parameters                 ////

    /** A specification for the size of the frame.
     */
    public SizeAttribute size;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>size</i> parameter, and a
     *  frame has been specified with setFrame(), then set the size
     *  of the frame.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the size
     *   specification is not correctly formatted, or if the base
     *   class throws it.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == size && _frame != null) {
            size.setSize(_frame);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace. This calls the
     *  base class and then sets the associated frame to null.
     *  Thus, the resulting tableau has no frame associated with it.
     *  @param workspace The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
        throws CloneNotSupportedException {
        Tableau newObject = (Tableau) super.clone(workspace);
        newObject._frame = null;
        return newObject;
    }

    /** Close this tableau by calling dispose() on the associated
     *  frame, or if the associated frame is an instance of TableauFrame,
     *  by calling _close() on it.
     *  @return False if the user cancels on a save query.
     */
    public boolean close() {
        JFrame frame = getFrame();
        if (frame instanceof TableauFrame) {
            // NOTE: Calling a protected method, but this class is in the
            // same package.
            if (!((TableauFrame) frame)._close())
                return false;
        } else if (
            (this instanceof DialogTableau)
                && (frame instanceof PortConfigurerDialog)) {
            if (!((PortConfigurerDialog) frame).close()) {
                return false;
            }
        } else if (frame != null) {
            frame.dispose();
        }
        return true;
    }

    /** Return the top-level window that implements the display of
     *  this tableau.
     *  @return A top-level window.
     */
    public JFrame getFrame() {
        return _frame;
    }

    /** Return the title of this tableau.  Subclasses can override this to
     *  provide a better description of themselves for use in the title.
     *  This base class returns the value set by a call to setTitle(),
     *  if it has been called. If not, then it returns an identifier
     *  of the effigy containing this tableau, or the string "Unnamed"
     *  if there is no such identifier.
     *  The title is used as the title of the top-level window in
     *  the setFrame() method.
     *  @return The title to put on the window.
     */
    public String getTitle() {
        if (_title == null) {
            Effigy effigy = (Effigy) getContainer();
            // Abbreviate the title to 80 chars for use on the Mac.
            return StringUtilities.abbreviate(
                effigy.identifier.getExpression());
        } else {
            return _title;
        }
    }

    /** Return true if the tableau is editable. This base class returns
     *  whatever value has been set by setEditable(), or <i>true</i> if
     *  none has been specified.
     *  @see #setEditable(boolean)
     *  @return True if the tableau is editable.
     */
    public boolean isEditable() {
        return _editable;
    }

    /** Return true if this tableau is a master, which means that
     *  if that if its window is closed, then all other windows associated
     *  with the model are also closed.
     *  @return True if the tableau is a master.
     */
    public boolean isMaster() {
        return _master;
    }

    /** Override the base class so that if the argument is null and the
     *  window is a master, then all other windows associated with the
     *  container are closed and the model is removed from the ModelDirectory.
     *  If this window is not a master, but after removing it there are
     *  no more windows associated with the model, then also remove it
     *  from the ModelDirectory.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If the proposed container is not
     *   an instance of Effigy, or if this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(CompositeEntity container)
        throws IllegalActionException, NameDuplicationException {
        if (container == null) {
            Effigy oldContainer = (Effigy) getContainer();
            super.setContainer(container);
            // Blow away the frame.
            if (_frame != null) {
                // Note that we call hide instead of dispose . . .
                // The windowListener set in setFrame()
                // will trigger dispose() to get called.
                _frame.hide();
            }

            if (isMaster() && oldContainer != null) {
                // Window is a master.  Close the model which will close all
                // other tableaux.
                oldContainer.setContainer(null);
            }
        } else if (container instanceof Effigy) {
            super.setContainer(container);
        } else {
            throw new IllegalActionException(
                this,
                container,
                "The container can only be set to an " + "instance of Effigy");
        }
    }

    /** Make the tableau editable or uneditable.  Notice that this does
     *  not change whether the effigy is modifiable, so other tableaux
     *  on the same effigy may still modify the associated file.
     *  Derived class will usually need to override this method to
     *  set whether their associated interfaces are editable or not.
     *  They should call this superclass method so that isEditable()
     *  returns the value specified here.
     *  @see #isEditable()
     *  @param flag False to make the tableau uneditable.
     */
    public void setEditable(boolean flag) {
        _editable = flag;
    }

    /** Set the top-level window associated with this tableau.
     *  @param frame The top-level window associated with the tableau.
     *  @exception IllegalActionException If the frame is not acceptable
     *   (not thrown in this base class).
     */
    public void setFrame(JFrame frame) throws IllegalActionException {
        _frame = frame;

        if (_frame != null) {
            size.setSize(_frame);
        }

        // Truncate the name so that dialogs under Web Start on the Mac
        // work better.
        frame.setTitle(getTitle());

        // Set up a listener for window closing events.
        frame.addWindowListener(new WindowAdapter() {
            // This is invoked if the window
            // is disposed by the _close() method of Top.
            public void windowClosed(WindowEvent e) {
                try {
                    (Tableau.this).setContainer(null);
                } catch (KernelException ex) {
                    try {
                        MessageHandler.warning("Cannot remove tableau: " + ex);
                    } catch (CancelException exception) {
                    }
                }
            }
            // NOTE: We do not want to do the same in windowClosing()
            // because this will override saving if modified as implemented
            // in Top.
        });
    }

    /** Specify whether the window associated with this tableau
     *  is a master, which means that if that window is closed, then
     *  all windows associated with the model are closed.
     *  @param flag If true, makes the window a master.
     */
    public void setMaster(boolean flag) {
        _master = flag;
    }

    /** Set the title of this tableau, changing the title of the
     *  associated top-level window.  Call this with a null argument
     *  to use the identifier of the containing effigy as a title.
     *  @param title The title to put on the window.
     */
    public void setTitle(String title) {
        _title = StringUtilities.abbreviate(title);
        if (_frame != null) {
            _frame.setTitle(getTitle());
        }
    }

    /** Make this tableau visible by calling setVisible(true), and
     *  raising or deiconifying its window.
     *  If no frame has been set, then do nothing.
     */
    public void show() {
        JFrame frame = getFrame();
        if (frame != null) {
            if (!frame.isVisible()) {
                size.setSize(frame);
                // NOTE: This used to override the location that might
                // have been set in the _windowProperties attribute.
                /*
                  if (frame instanceof Top) {
                  ((Top)frame).centerOnScreen();
                  }
                */
                frame.pack();
                frame.setVisible(true);
                // NOTE: The above calls Component.show()...
                // We used to override Top.show() (Top extends JFrame)
                // to call pack(), but this had the unfortunate side
                // effect of overriding manual placement of windows.
                // However, due to some weirdness in Swing or the AWT,
                // calling pack before setVisible() does not have the
                // same effect as calling pack() within show().
                // Calling it after, however, is not sufficient.
                // We have to call it both before and after! Ugh!
                // If this call to pack is put before the call to
                // setVisible(true), then the HTML welcome window
                // when Vergil starts up is the wrong size.
                frame.pack();
            }
            // Deiconify the window.
            frame.setState(Frame.NORMAL);
            frame.toFront();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Flag indicating whether the tableau is editable. */
    private boolean _editable;

    /** The frame that the tableau is shown in.
     */
    private JFrame _frame;

    /** True if this tableau is a master tableau.  Default value is false.
     */
    private boolean _master = false;

    /** The title set by setTitle(). */
    private String _title;
}

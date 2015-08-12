/* An attribute representing the size of a component.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;

import ptolemy.data.IntMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SizeAttribute

/**
 This attribute stores the width and height of a graphical component.
 The token in this attribute is an IntMatrixToken containing a matrix
 of dimension 1x2, containing the width and the height, in that order.
 By default, this attribute has visibility NONE, so the user will not
 see it in parameter editing dialogs.
 <p>
 Note that this attribute is typically used to record the size of
 a component within a frame (a top-level window). To record the size
 and position of the frame, use WindowPropertiesAttribute.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 @see WindowPropertiesAttribute
 */
public class SizeAttribute extends Parameter implements ComponentListener {
    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SizeAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.NONE);

        // The following line, if uncommented, results in
        // icons that are defined in an external file always being
        // exported along with the model that defines them.  This bloats
        // the MoML files with information that is not needed. I suspect
        // the line was put there because it wasn't clear that you need
        // to invoke File->Save in a submodel if you want the location
        // and position of the submodel window to be saved. It is not
        // sufficient to invoke save just at the top level.
        // setPersistent(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the attribute into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new attribute.
     *  @param workspace The workspace for the new attribute
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SizeAttribute newObject = (SizeAttribute) super.clone(workspace);
        newObject._listeningTo = null;
        return newObject;
    }

    /** Do nothing. This method is
     *  invoked when the component has been made invisible.
     *  @param event The component event.
     */
    @Override
    public void componentHidden(ComponentEvent event) {
    }

    /** Do nothing. This method is
     *  invoked when the component's position changes.
     *  @param event The component event.
     */
    @Override
    public void componentMoved(ComponentEvent event) {
    }

    /** Record the new size. This method is
     *  invoked when the component's size changes.
     *  @param event The component event.
     */
    @Override
    public void componentResized(ComponentEvent event) {
        // FIXME: Due to Swing's lame approach to sizes,
        // the size that is reported by this event is actually,
        // apparently, a random number, the off by just enough
        // from the actual size to cause scroll bars to be
        // squished and the progress bar to not be shown.
        // So we don't record the size.
        // recordSize(_listeningTo);
    }

    /** Do nothing. This method is
     *  invoked when the component has been made visible.
     *  @param event The component event.
     */
    @Override
    public void componentShown(ComponentEvent event) {
    }

    /** Set the value of the attribute to match those of the specified
     *  component.
     *  @param component The component whose size is to be recorded.
     */
    public void recordSize(Component component) {
        try {
            Rectangle bounds = component.getBounds();
            setToken("[" + bounds.width + ", " + bounds.height + "]");
            // Not clear why the following is needed, but if it isn't there,
            // then window properties may not be recorded.
            propagateValue();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("Can't set bounds value!");
        }
    }

    /** Set the size of the specified component to match the
     *  current value of the attribute.  If the value of the attribute
     *  has not been set, then do nothing.  If it has not already
     *  done so, this method also registers with the component
     *  as a listener for component events like resizing.
     *  @param component The component whose size is to be set.
     *  @return True if successful.
     */
    public boolean setSize(Component component) {
        if (component == null) {
            if (_listeningTo != null) {
                _listeningTo.removeComponentListener(this);
                _listeningTo = null;
            }
            return true;
        }
        if (_listeningTo != component) {
            if (_listeningTo != null) {
                _listeningTo.removeComponentListener(this);
            }

            component.addComponentListener(this);
            _listeningTo = component;
        }

        try {
            IntMatrixToken token = (IntMatrixToken) getToken();

            if (token != null) {
                int width = token.getElementAt(0, 0);
                int height = token.getElementAt(0, 1);

                // NOTE: As usual with swing, it's not obvious what the
                // right way to do this is. The following seems to work,
                // found by trial and error.  Even then, the layout
                // manager feels free to override it.
                Dimension dimension = new Dimension(width, height);
                component.setSize(dimension);

                // NOTE: If it's a JComponent, the setSize() is
                // insufficient to set the size (you will have to ask
                // Sun why this is so).  We also have to do the
                // following.
                if (component instanceof JComponent) {
                    ((JComponent) component).setPreferredSize(dimension);
                    ((JComponent) component).setMinimumSize(dimension);
                }
            } else {
                // Unset the size.
                if (component instanceof JComponent) {
                    ((JComponent) component).setPreferredSize(null);
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The component we are listening to. */
    private Component _listeningTo;
    // FIXME: should the above we a weak reference like what we have
    // for WindowPropertiesAttribute?

}

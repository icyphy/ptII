/* Display a java.awt.Image

@Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TokenEffigy;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ImageToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.media.Picture;


//////////////////////////////////////////////////////////////////////////
//// ImageDisplay

/**
   Display an image on the screen using the ptolemy.media.Picture
   class.  For a sequence of images that are all the same size, this class
   will continually update the picture with new data.   If the size of the
   input image changes, then a new Picture object is created.  This class
   will only accept an ImageToken on its input.

   @author James Yeh, Edward A. Lee
   @version $Id$
   @since Ptolemy II 3.0
   @Pt.ProposedRating Red
   @Pt.AcceptedRating Red
*/
public class ImageDisplay extends Sink implements Placeable {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageDisplay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: This is required to be an ImageToken, but
        // we don't see to have that class.
        input.setTypeEquals(BaseType.OBJECT);

        _frame = null;
        _container = null;

        _windowProperties = new WindowPropertiesAttribute(this,
                "_windowProperties");

        _pictureSize = new SizeAttribute(this, "_pictureSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then removes association with graphical objects
     *  belonging to the original class.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ImageDisplay newObject = (ImageDisplay) super.clone(workspace);

        newObject._container = null;
        newObject._frame = null;

        return newObject;
    }

    /** Get the background.
     *  @return The background color.
     *  @see #setBackground()
     */
    public Color getBackground() {
        return _container.getBackground();
    }

    /** Initialize this actor.
     *  If place has not been called, then create a frame to display the
     *  image in.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        // This has to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
                public void run() {
                    _createOrShowWindow();
                }
            };

        SwingUtilities.invokeLater(doDisplay);
    }

    /** Set the container that this actor should display data in.  If place
     * is not called, then the actor will create its own frame for display.
     */
    public void place(Container container) {
        // If there was a previous container that doesn't match this one,
        // remove the pane from it.
        if ((_container != null) && (_picture != null)) {
            _container.remove(_picture);
            _container = null;
        }

        if (_frame != null) {
            _frame.dispose();
            _frame = null;
        }

        _container = container;

        if (container == null) {
            // Reset everything.
            if (_tableau != null) {
                // This will have the side effect of removing the effigy
                // from the directory if there are no more tableaux in it.
                try {
                    _tableau.setContainer(null);
                } catch (KernelException ex) {
                    throw new InternalErrorException(ex);
                }
            }

            _tableau = null;
            _effigy = null;
            _picture = null;
            _oldXSize = 0;
            _oldYSize = 0;

            return;
        }

        if (_picture == null) {
            // Create the pane.
            _picture = new Picture(_oldXSize, _oldYSize);
        }

        // Place the pane in supplied container.
        _container.add(_picture, BorderLayout.CENTER);
    }

    /** Consume a token from the <i>input</i> port
     *  and display the token as an image.  If a token is not available,
     *  do nothing.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            final Token in = input.get(0);

            // Display probably to be done in the Swing event thread.
            Runnable doDisplay = new Runnable() {
                    public void run() {
                        _display(in);
                    }
                };

            SwingUtilities.invokeLater(doDisplay);
        }

        return super.postfire();
    }

    /** Set the background.
     *  @param background The background color.
     *  @see #getBackground()
     */
    public void setBackground(Color background) {
        _container.setBackground(background);
    }

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display
     */
    protected void _display(Token in) {
        if (!(in instanceof ImageToken)) {
            throw new InternalErrorException(
                    "Input is not an ImageToken. It is: " + in);
        }

        // See also ptolemy/actor/lib/image/ImageTableau.java
        if (_frame != null) {
            List tokens = new LinkedList();
            tokens.add(in);

            try {
                _effigy.setTokens(tokens);
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        } else if (_picture != null) {
            Image image = ((ImageToken) in).asAWTImage();
            int xSize = image.getWidth(null);
            int ySize = image.getHeight(null);

            // If the size has changed, have to recreate the Picture object.
            if ((_oldXSize != xSize) || (_oldYSize != ySize)) {
                if (_debugging) {
                    _debug("Image size has changed.");
                }

                _oldXSize = xSize;
                _oldYSize = ySize;

                Container container = _picture.getParent();
        
                if (_picture != null) {
                    container.remove(_picture);
                }

                _picture = new Picture(xSize, ySize);
                _picture.setImage(image);
                _picture.setBackground(null);
                container.add("Center", _picture);
                container.validate();
                container.invalidate();
                container.repaint();
                container.doLayout();

                Container c = container.getParent();

                while (c.getParent() != null) {
                    c.invalidate();
                    c.validate();
                    c = c.getParent();

                    if (c instanceof JFrame) {
                        ((JFrame) c).pack();
                    }
                }
            } else {
                _picture.setImage(((ImageToken) in).asAWTImage());
                _picture.displayImage();
                _picture.repaint();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create or show the top-level window, unless there already is a
     *  container. This must be called in the Swing event thread.
     */
    private void _createOrShowWindow() {
        if (_container == null) {
            // No current container for the pane.
            // Need an effigy and a tableau so that menu ops work properly.
            if (_tableau == null) {
                Effigy containerEffigy = Configuration.findEffigy(toplevel());

                if (containerEffigy == null) {
                    throw new InternalErrorException(
                            "Cannot find effigy for top level: "
                            + toplevel().getFullName());
                }

                try {
                    _effigy = new TokenEffigy(containerEffigy,
                            containerEffigy.uniqueName("imageEffigy"));

                    // The default identifier is "Unnamed", which is
                    // no good for two reasons: Wrong title bar label,
                    // and it causes a save-as to destroy the original window.
                    _effigy.identifier.setExpression(getFullName());

                    _frame = new ImageWindow();

                    _tableau = new ImageTableau(_effigy, "tokenTableau",
                            _frame, _oldXSize, _oldYSize);
                    _tableau.setTitle(getName());
                    _frame.setTableau(_tableau);
                    _windowProperties.setProperties(_frame);

                    // Regrettably, since setSize() in swing doesn't actually
                    // set the size of the frame, we have to also set the
                    // size of the internal component.
                    Component[] components = _frame.getContentPane()
                        .getComponents();

                    if (components.length > 0) {
                        _pictureSize.setSize(components[0]);
                    }

                    _tableau.show();
                } catch (Exception ex) {
                    throw new InternalErrorException(ex);
                }
            } else {
                // Erase previous image.
                _effigy.clear();

                if (_frame != null) {
                    // Do not use show() as it overrides manual placement.
                    _frame.toFront();
                }
            }
        }

        if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The container for the image display, set by calling place() */
    protected Container _container;

    /** The effigy for the image data. */
    protected TokenEffigy _effigy;

    /** The frame, if one is used. */
    protected ImageWindow _frame;

    /** The horizontal size of the previous image. */
    protected int _oldXSize = 0;

    /** The vertical size of the previous image. */
    protected int _oldYSize = 0;

    /** The picture panel. */
    protected Picture _picture;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A specification of the size of the picture if it's in its own window.
     */ 
    private SizeAttribute _pictureSize;

    /** The tableau with the display, if any. */
    private ImageTableau _tableau;

    /** A specification for the window properties of the frame. */
    private WindowPropertiesAttribute _windowProperties;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Version of TableauFrame that removes its association with the
     *  ImageDisplay upon closing, and also records the size of the display.
     */
    protected class ImageWindow extends TableauFrame {
        /** Construct an empty window.
         *  After constructing this, it is necessary
         *  to call setVisible(true) to make the frame appear
         *  and setTableau() to associate it with a tableau.
         */
        public ImageWindow() {
            // The null second argument prevents a status bar.
            super(null, null);
        }

        /** Close the window.  This overrides the base class to remove
         *  the association with the ImageDisplay actor and to record window
         *  properties.
         *  @return True.
         */
        protected boolean _close() {
            // Record the window properties before closing.
            _windowProperties.recordProperties(this);

            // Regrettably, have to also record the size of the contents
            // because in Swing, setSize() methods do not set the size.
            // Only the first component size is recorded.
            Component[] components = getContentPane().getComponents();

            if (components.length > 0) {
                _pictureSize.recordSize(components[0]);
            }

            super._close();
            place(null);
            return true;
        }
    }
}

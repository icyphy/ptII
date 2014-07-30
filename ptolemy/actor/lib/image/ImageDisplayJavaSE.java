/* An AWT and Swing implementation of the the ImageDisplayInterface
 that displays a java.awt.Image.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.gui.AbstractPlaceableJavaSE;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TokenEffigy;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.data.ImageToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.media.Picture;

///////////////////////////////////////////////////////////////////
////ImageDisplayJavaSE

/**
<p>
ImageDisplayJavaSE is the implementation of the ImageDisplayInterface that uses AWT and Swing
classes.</p>

@author Jianwu Wang, Based on code by James Yeh, Edward A. Lee
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating
@Pt.AcceptedRating
 */

public class ImageDisplayJavaSE extends AbstractPlaceableJavaSE implements
ImageDisplayInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Free up memory when closing.
     */
    @Override
    public void cleanUp() {
        _tableau = null;
    }

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display
     */
    @Override
    public void display(final Token in) {
        // Display probably to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
            @Override
            public void run() {
                _display(in);
            }
        };

        SwingUtilities.invokeLater(doDisplay);
    }

    /** Get the background.
     *  @return The background color.
     *  @see #setBackground(Color)
     */
    @Override
    public Color getBackground() {
        return _container.getBackground();
    }

    /**
     * Get the image's frame.
     * @return the image's frame.
     * @see #setFrame(Object)
     */
    @Override
    public Object getFrame() {
        return _imageWindowFrame;
    }

    /**
     * Get the platform dependent picture that contains the image.
     * @return the platform dependent container.
     * @see #setPicture(Object)
     */
    @Override
    public Object getPicture() {
        return _picture;
    }

    /**
     * Get the platform dependent container that contains the image.
     * @return the platform dependent container.
     * @see #setPlatformContainer(Object)
     */
    @Override
    public Object getPlatformContainer() {
        return _container;
    }

    /**
     * Get the image tableau.
     * @return the image tableau.
     */
    @Override
    public Object getTableau() {
        return _tableau;
    }

    /** Initialize an object.
     * @param imageDisplayActor The object to be initialized.
     * @exception IllegalActionException If the entity cannot be contained
     * by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * actor with this name.
     */
    @Override
    public void init(ImageDisplay imageDisplayActor)
            throws IllegalActionException, NameDuplicationException {
        _display = imageDisplayActor;
        super.init(imageDisplayActor);
    }

    /**
     * Initialize the effigy of the image.
     * @exception IllegalActionException If there is a problem initializing the effigy
     */
    @Override
    public void initializeEffigy() throws IllegalActionException {
        // This has to be done in the Swing event thread.
        Runnable doDisplay = new Runnable() {
            @Override
            public void run() {
                _createOrShowWindow();
            }
        };

        SwingUtilities.invokeLater(doDisplay);
    }

    /**
     * Initialize the effigy of the plotter.
     * @exception IllegalActionException If there is a problem initializing the effigy
     */
    @Override
    public void initWindowAndSizeProperties() throws IllegalActionException,
    NameDuplicationException {
        _windowProperties = (WindowPropertiesAttribute) _display.getAttribute(
                "_windowProperties", WindowPropertiesAttribute.class);
        if (_windowProperties == null) {
            _windowProperties = new WindowPropertiesAttribute(_display,
                    "_windowProperties");
            // Note that we have to force this to be persistent because
            // there is no real mechanism for the value of the properties
            // to be updated when the window is moved or resized. By
            // making it persistent, when the model is saved, the
            // attribute will determine the current size and position
            // of the window and save it.
            _windowProperties.setPersistent(true);
        }
        _pictureSize = (SizeAttribute) _display.getAttribute("_pictureSize",
                SizeAttribute.class);
        if (_pictureSize == null) {
            _pictureSize = new SizeAttribute(_display, "_pictureSize");
            _pictureSize.setPersistent(true);
        }
    }

    /**
     * Remove the plot from the frame if the container is null.
     */
    @Override
    public void placeContainer(Container container) {
        // If there was a previous container that doesn't match this one,
        // remove the pane from it.
        if (_container != null && _picture != null) {
            _container.remove(_picture);
            _container = null;
        }

        if (_imageWindowFrame != null) {
            _imageWindowFrame.dispose();
            _imageWindowFrame = null;
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

    /** Set the background.
     *  @param background The background color.
     *  @see #getBackground()
     */
    @Override
    public void setBackground(Color background) {
        _container.setBackground(background);
    }

    /**
     * Set the frame of the image.
     * @param frame The frame to set.
     * @see #getFrame()
     */
    @Override
    public void setFrame(Object frame) {
        if (_imageWindowFrame != null) {
            _imageWindowFrame.removeWindowListener(_windowClosingAdapter);
        }

        if (frame == null) {
            _imageWindowFrame = null;
            return;
        }

        _imageWindowFrame = (ImageWindow) frame;

        _windowClosingAdapter = new WindowClosingAdapter();
        _imageWindowFrame.addWindowListener(_windowClosingAdapter);

        _windowProperties.setProperties(_imageWindowFrame);
    }

    /**
     * Set the platform dependent picture of the image.
     * The container can be AWT container or Android view.
     * @param picture The picture
     * @see #getPicture()
     */
    @Override
    public void setPicture(Object picture) {
        _picture = (Picture) picture;
    }

    /**
     * Set the platform dependent container of the image.
     * The container can be AWT container or Android view.
     * @param container the platform dependent container.
     * @see #getPlatformContainer()
     */
    @Override
    public void setPlatformContainer(Object container) {
        _container = (Container) container;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /** The container for the image display, set by calling place(). */
    protected Container _container;

    /** The effigy for the image data. */
    protected TokenEffigy _effigy;

    /** The frame, if one is used. */
    protected ImageWindow _imageWindowFrame;

    /** The picture panel. */
    protected Picture _picture;

    /** The horizontal size of the previous image. */
    protected int _oldXSize = 0;

    /** The vertical size of the previous image. */
    protected int _oldYSize = 0;

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
                Effigy containerEffigy = Configuration.findEffigy(_display
                        .toplevel());

                if (containerEffigy == null) {
                    throw new InternalErrorException(
                            "Cannot find effigy for top level: "
                                    + _display.toplevel().getFullName());
                }

                try {
                    _effigy = new TokenEffigy(containerEffigy,
                            containerEffigy.uniqueName("imageEffigy"));

                    // The default identifier is "Unnamed", which is
                    // no good for two reasons: Wrong title bar label,
                    // and it causes a save-as to destroy the original window.
                    _effigy.identifier.setExpression(_display.getFullName());

                    _imageWindowFrame = new ImageWindow();

                    _tableau = new ImageTableau(_effigy, "tokenTableau",
                            _imageWindowFrame, _oldXSize, _oldYSize);
                    _tableau.setTitle(_display.getName());
                    _imageWindowFrame.setTableau(_tableau);
                    _windowProperties.setProperties(_imageWindowFrame);

                    // Regrettably, since setSize() in swing doesn't actually
                    // set the size of the frame, we have to also set the
                    // size of the internal component.
                    Component[] components = _imageWindowFrame.getContentPane()
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

                if (_imageWindowFrame != null) {
                    // Do not use show() as it overrides manual placement.
                    _imageWindowFrame.toFront();
                }
            }
        }

        if (_imageWindowFrame != null) {
            _imageWindowFrame.setVisible(true);
            _imageWindowFrame.toFront();
        }
    }

    /** Display the specified token. This must be called in the Swing
     *  event thread.
     *  @param in The token to display
     */
    private void _display(Token in) {
        if (!(in instanceof ImageToken)) {
            throw new InternalErrorException(
                    "Input is not an ImageToken. It is: " + in);
        }

        // See also ptolemy/actor/lib/image/ImageTableau.java
        if (_imageWindowFrame != null) {
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
            if (_oldXSize != xSize || _oldYSize != ySize) {
                _oldXSize = xSize;
                _oldYSize = ySize;

                Container container = _picture.getParent();

                container.remove(_picture);

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
    ////                         private variables                 ////
    /** Reference to the ImageDisplay actor */
    private ImageDisplay _display;

    /** The tableau with the display, if any. */
    private ImageTableau _tableau;

    /** A specification of the size of the picture if it's in its own window.
     */
    private SizeAttribute _pictureSize;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    /** Version of TableauFrame that removes its association with the
     *  ImageDisplay upon closing, and also records the size of the display.
     */
    @SuppressWarnings("serial")
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
        @Override
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
            _display.place(null);
            return true;
        }
    }

    /** Listener for windowClosing action. */
    class WindowClosingAdapter extends
    AbstractPlaceableJavaSE.WindowClosingAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            _display.cleanUp();
        }
    }
}

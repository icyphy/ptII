/* An icon that displays a specified java.awt.Image.

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
package ptolemy.vergil.icon;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.FileUtilities;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.ImageFigure;
import diva.gui.toolbox.FigureIcon;

///////////////////////////////////////////////////////////////////
//// ImageIcon

/**
 An icon that displays a specified java.awt.Image.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ImageIcon extends DynamicEditorIcon implements ImageObserver {
    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ImageIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ImageIcon newObject = (ImageIcon) super.clone(workspace);
        newObject._image = null;
        newObject._scaledImage = null;
        newObject._scalePercentage = 0.0;
        newObject._scalePercentageImplemented = -1.0;
        return newObject;
    }

    /** Create a new default background figure, which is scaled image,
     *  if it has been set, or a default image if not.
     *  This must be called in the Swing thread, or a concurrent
     *  modification exception could occur.
     *  @return A figure representing the specified shape.
     */
    @Override
    public Figure createBackgroundFigure() {
        // NOTE: This gets called every time that the graph gets
        // repainted, which seems excessive to me.  This will happen
        // every time there is a modification to the model that is
        // carried out by a ChangeRequest.
        // The Diva graph package implements a model-view-controller
        // architecture, which implies that this needs to return a new
        // figure each time it is called.  The reason is that the figure
        // may go into a different view, and transformations may be applied
        // to that figure in that view.  However, this class needs to be
        // able to update that figure when setShape() is called.  Hence,
        // this class keeps a list of all the figures it has created.
        // The references to these figures, however, have to be weak
        // references, so that this class does not interfere with garbage
        // collection of the figure when the view is destroyed.
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (_scaledImage == null) {
            // NOTE: This default has to be an ImageFigure, since it
            // will later have its image set. Create a default image.
            try {
                // Use nameToURL so this works in WebStart.
                URL url = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/vergil/icon/PtolemyIISmall.gif",
                        null, getClass().getClassLoader());
                _scaledImage = _image = tk.getImage(url);
                setImage(_scaledImage);
                tk.prepareImage(_scaledImage, -1, -1, this);
            } catch (IOException ex) {
                // Ignore, we can't find the icon.
            }
        }

        ImageFigure newFigure = null;
        // Make sure the image is fully loaded before we create the
        // images. This prevents flashing.
        if (_scalePercentage == _scalePercentageImplemented
                && (tk.checkImage(_scaledImage, 43, 33, this) & ImageObserver.ALLBITS) != 0) {
            // Current image is fully loaded.
            newFigure = new ImageFigure(_scaledImage);
        } else {
            // If the image is not fully loaded, use an empty
            // image. The image will be set in the imageUpdate() method.
            newFigure = new ImageFigure(null);
        }
        newFigure.setCentered(false);
        // Record the figure so that the image can be updated
        // if it is changed or scaled.
        _addLiveFigure(newFigure);

        return newFigure;
    }

    /** Create a new Swing icon. This overrides the base class to
     *  wait until image has been rendered. Otherwise, we get a null
     *  pointer exception in Diva, and also the library collapses
     *  and has to be re-opened.
     *  @return A new Swing Icon.
     */
    @Override
    public javax.swing.Icon createIcon() {
        if (_scalePercentage == _scalePercentageImplemented) {
            // Image processing is done.
            return super.createIcon();
        }
        // Provide a placeholder, but do not store it in
        // the icon cache.
        return new FigureIcon(_PLACEHOLDER_ICON, 20, 15);
    }

    /** This method, which is required by the ImageObserver interface,
     *  is called if something has changed in a background loading of
     *  the image.
     *  @param image The image being observed.
     *  @param infoflags The bitwise inclusive OR of the following flags:
     *   WIDTH, HEIGHT, PROPERTIES, SOMEBITS, FRAMEBITS, ALLBITS, ERROR,
     *   ABORT.
     *  @param x The x coordinate of the image.
     *  @param y The y coordinate of the image.
     *  @param width The width of the image.
     *  @param height The height of the image.
     *  @return False if the infoflags indicate that the image is
     *   completely loaded; true otherwise.
     */
    @Override
    public synchronized boolean imageUpdate(Image image, int infoflags, int x,
            int y, int width, int height) {
        if ((infoflags & ImageObserver.ALLBITS) != 0) {
            // The image is now fully loaded.
            if (_scalePercentage != 0.0
                    && _scalePercentage != _scalePercentageImplemented) {
                // Scaling has been deferred until the original image
                // was fully rendered.  Start the scaling operation again.
                scaleImage(_scalePercentage);
                // Nothing more to be done on this image.
                return true;
            }
            // Either the image passed in is already the scaled image,
            // or the scaling has already been implemented.
            _updateFigures();
            return false;
        }

        if ((infoflags & (ImageObserver.ERROR | ImageObserver.ABORT)) != 0) {
            URL url = getClass().getClassLoader().getResource(
                    "/diva/canvas/toolbox/errorImage.gif");
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image errorImage = tk.getImage(url);
            synchronized (this) {
                _image = errorImage;
                _scaledImage = errorImage;
            }
            // Further updates will be needed when the above image
            // is updated. To ensure the updates are called, do this:
            if (tk.prepareImage(_image, -1, -1, this)) {
                // Image has been fully prepared. Request a re-rendering.
                _updateFigures();
            }
            return true;
        }

        // Image is neither complete nor in error.
        // Needed to trigger further updates.
        return true;
    }

    /** Specify a scaling for the image as a percentage.
     *  @param percentage The scaling percentage.
     */
    public synchronized void scaleImage(double percentage) {

        // Record the new scale, even if we can't implement it now.
        _scalePercentage = percentage;
        _scalePercentageImplemented = -1.0;

        if (_image == null) {
            // No image has been set yet, so return.
            return;
        }

        // Wait for the original image to be fully rendered, so we
        // can get its size, then create a new scaled image and set
        // the images of any Figures that have already been created.
        // This needs to be in the swing thread.
        Runnable doScale = new Runnable() {
            @Override
            public void run() {
                synchronized (ImageIcon.this) {
                    Toolkit tk = Toolkit.getDefaultToolkit();
                    // NOTE: Oddly, the following two calls below may not
                    // return the correct sizes unless the image is
                    // already loaded. Since we are waiting above
                    // until it is fully loaded, we should be OK.
                    int width = _image.getWidth(ImageIcon.this);
                    int height = _image.getHeight(ImageIcon.this);
                    if (width < 0 || height < 0) {
                        // Original image is not fully loaded. Wait until it is.
                        // This will be handled in imageUpdate().
                        return;
                    }
                    int newWidth = (int) Math.round(width * _scalePercentage
                            / 100.0);
                    int newHeight = (int) Math.round(height * _scalePercentage
                            / 100.0);

                    if (newWidth != 0 && newHeight != 0) {
                        // Avoid "Exception in thread "AWT-EventQueue-0" java.lang.IllegalArgumentException: Width (0) and height (0) must be non-zero"
                        // which is thrown by java.awt.Image.getScaledInstance() when the height or width is 0.
                        // This occurs when running
                        // $PTII/bin/ptcg -language java $PTII/ptolemy/moml/filter/test/auto/modulation2.xml
                        // Negative argument indicates to maintain aspect ratio.
                        _scaledImage = _image.getScaledInstance(newWidth, -1,
                                Image.SCALE_SMOOTH);

                        _scalePercentageImplemented = _scalePercentage;

                        if (tk.prepareImage(_scaledImage, width, height,
                                ImageIcon.this)) {
                            // Image is fully prepared. Request a re-rendering.
                            _updateFigures();
                        }
                    }
                }
            }
        };

        SwingUtilities.invokeLater(doScale);
    }

    /** Specify an image to display. Note that this
     *  does not actually result in the image displaying.
     *  You must call scaleImage().
     *  @param image The image to display.
     */
    public synchronized void setImage(Image image) {
        _image = image;
        _scaledImage = image;

        // scaleImage() may have been called before this,
        // in which case it would have done nothing because
        // _image was null.
        if (_scalePercentage != _scalePercentageImplemented) {
            // Delegate to scaleImage().
            scaleImage(_scalePercentage);
            return;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Update any previously rendered Diva figures that contain
     *  this image, and request a re-rendering.
     */
    private void _updateFigures() {
        // If the figure has been previously rendered, first update
        // the ImageFigure to use the new image.
        synchronized (_figures) {
            Iterator figures = _liveFigureIterator();
            while (figures.hasNext()) {
                Object figure = figures.next();
                ((ImageFigure) figure).setImage(_scaledImage);
            }
        }

        ChangeRequest request = new ChangeRequest(this, "Dummy change request") {
            @Override
            protected void _execute() {
            }
        };
        // Prevent save() being triggered on close just because of this.
        request.setPersistent(false);
        requestChange(request);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The image that is the master.
    private Image _image;

    // Placeholder icon to be used if images are not fully processed.
    private static Figure _PLACEHOLDER_ICON = new BasicRectangle(0.0, 0.0,
            10.0, 10.0);

    // The scaled version of the image that is the master.
    private Image _scaledImage;

    // The scale percentage. 0.0 means unspecified.
    private double _scalePercentage = 0.0;

    // The scale percentage that has been implemented.
    // 0.0 means that the specified percentage has not been implemented.
    private double _scalePercentageImplemented = -1.0;
}

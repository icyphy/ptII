/* An icon that displays a specified java.awt.Image.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.SwingUtilities;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.ImageFigure;

//////////////////////////////////////////////////////////////////////////
//// ImageEditorIcon
/**
An icon that displays a specified java.awt.Image.
Note that this icon is not persistent, so an actor that uses
this icon should create it in its constructor.  It will not be
represented in the MoML file.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ImageEditorIcon extends EditorIcon implements ImageObserver {

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ImageEditorIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method, which is required by the ImageObserver interface,
     *  is called if something has changed in a background loading of
     *  the image.
     *  @param img The image being observed.
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
    public boolean imageUpdate(Image image,
            int infoflags,
            int x,
            int y,
            int width,
            int height) {
        if (((infoflags | ImageObserver.WIDTH) != 0)
                && ((infoflags | ImageObserver.HEIGHT) != 0)) {
            // Width and height information is available now.
            scaleImage(_scalePercentage);
            return false;
        }
        if (((infoflags | ImageObserver.ERROR) != 0)
                && ((infoflags | ImageObserver.ABORT) != 0)) {
            // FIXME: Set an error image.
            return false;
        }
        // If this was called for any other reason, repaint.
        _imageFigure.repaint();
        
        // This method returns true to indicate that further
        // updates are needed.  However, I can't begin to understand
        // how this method could possibly know whether further updates
        // are needed.  This seems like a ridiculous API.
        return true;
    }
    
    /** Specify a scaling for the image as a percentage.
     *  @param percentage The scaling percentage.
     */
    public void scaleImage(final double percentage) {
        Runnable doScale = new Runnable() {
            public void run() {
                // This needs to be in the swing thread.
                _scalePercentage = percentage;
                
                if (_image == null) {
                    // No image has been set yet, so return.
                    return;
                }
                // NOTE: Oddly, the following two calls below may not return the
                // correct sizes unless the image is already loaded.
                // Although it is not documented, it appears that if the
                // the returned size is not positive, then it is not correct,
                // and imageUpdate() will be called later. So in that case,
                // we do nothing.
                int width = _image.getWidth(ImageEditorIcon.this);
                int height = _image.getHeight(ImageEditorIcon.this);
                if (width > 0 && height > 0) {
                    int newWidth = (int) Math.round(width * percentage/100.0);
                    int newHeight = (int) Math.round(height * percentage/100.0);
                    Image newImage = _image.getScaledInstance(
                            newWidth, newHeight, Image.SCALE_SMOOTH);
                    _imageFigure.setImage(newImage);
                }
            }
        };
        SwingUtilities.invokeLater(doScale);
    }
    
    /** Specify an image to display.  This is deferred and executed
     *  in the Swing thread.
     *  @param image The image to display.
     */
    public void setImage(final Image image) {
        Runnable doSet = new Runnable() {
            public void run() {
                _image = image;
                _imageFigure.setImage(image);
                if (_scalePercentage != 100.0) {
                    scaleImage(_scalePercentage);
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new default background figure, which is a white box.
     *  Subclasses of this class should generally override
     *  the createBackgroundFigure method instead.  This method is provided
     *  so that subclasses are always able to create a default figure even if
     *  an error occurs or the subclass has not been properly initialized.
     *  NOTE: This 
     *  @return A figure representing a rectangular white box.
     */
    protected Figure _createDefaultBackgroundFigure() {
        if (_imageFigure != null) {
            // NOTE: This violates the Diva MVC architecture!
            // This attribute is part of the model, and should not have
            // a reference to this figure.  By doing so, it precludes the
            // possibility of having multiple views on this model.
            return _imageFigure;
        } else {
            // NOTE: center at the origin.
            return new BasicRectangle(-30, -30, 60, 60, Color.blue, 1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The image that is the master of a possibly scaled version in the
    // _imageFigure.
    private Image _image;
    
    // The Diva figure containing the associated image.
    private ImageFigure _imageFigure = new ImageFigure();
    
    // The scale percentage.
    private double _scalePercentage = 100.0;
}

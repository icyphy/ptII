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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SwingUtilities;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import diva.canvas.Figure;
import diva.canvas.toolbox.ImageFigure;

//////////////////////////////////////////////////////////////////////////
//// ImageIcon

/**
An icon that displays a specified java.awt.Image.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ImageIcon extends EditorIcon implements ImageObserver {

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
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ImageIcon newObject = (ImageIcon)super.clone(workspace);
        newObject._figures = new LinkedList();
        newObject._image = null;
        newObject._scaledImage = null;
        return newObject;
    }

    /** Create a new default background figure, which is the shape set
     *  by setShape, if it has been called, or a small box if not.
     *  This must be called in the Swing thread, or a concurrent
     *  modification exception could occur.
     *  @return A figure representing the specified shape.
     */
    public Figure createBackgroundFigure() {
        // NOTE: This gets called every time that the graph gets
        // repainted, which seems excessive to me.  This will happen
        // every time there is a modification to the model that is
        // carried out by a MoMLChangeRequest.
        
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
        if (_scaledImage == null) {
            // NOTE: This default has to be an ImageFigure, since it
            // will later have its image set. Create a default image.
            URL url = getClass().getResource(
                    "/doc/img/PtolemyIISmall.gif");
            Toolkit tk = Toolkit.getDefaultToolkit();
            setImage(tk.getImage(url));
        }
        Figure newFigure = new ImageFigure(_scaledImage);
        _figures.add(new WeakReference(newFigure));
        
        return newFigure;
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
    public boolean imageUpdate(Image image,
            int infoflags,
            int x,
            int y,
            int width,
            int height) {
        
        if (((infoflags | ImageObserver.WIDTH) != 0)
                || ((infoflags | ImageObserver.HEIGHT) != 0)) {
            // Width and height information is available now.
            scaleImage(_scalePercentage);
            
            // If the width and height change, then ports may need to be
            // laid out again. 
            // FIXME: Regrettably, the only way to do this is to
            // trigger a graph listener, which lays out the entire graph.
            // This is a flaw that needs to fixed. As a major hack,
            // what we do here is to issue a MoMLChangeRequest, which
            // will cause the graph to lay out again.  This will also
            // introduce a bug in that there will be an empty undo action.
            
            return false;
        }
        if (((infoflags | ImageObserver.ERROR) != 0)
                || ((infoflags | ImageObserver.ABORT) != 0)) {
            // FIXME: Set an error image.
            return false;
        }
        // If this was called for any other reason, repaint.
        ListIterator figures = _figures.listIterator();
        while (figures.hasNext()) {
            Object figure = ((WeakReference)figures.next()).get();
            if (figure == null) {
                // The figure has been garbage collected, so we
                // remove it from the list.
                figures.remove();
            } else {
                ((ImageFigure)figure).repaint();
            }
        }
        
        // This method returns true to indicate that further
        // updates are needed.  However, I can't begin to understand
        // how this method could possibly know whether further updates
        // are needed.  This seems like a ridiculous API.
        return true;
    }
    
    /** Specify a scaling for the image as a percentage.
     *  @param percentage The scaling percentage.
     */
    public void scaleImage(double percentage) {
        _scalePercentage = percentage;
        
        // This needs to be in the swing thread.
        Runnable doScale = new Runnable() {
            public void run() {        
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
                int width = _image.getWidth(ImageIcon.this);
                int height = _image.getHeight(ImageIcon.this);
                if (width > 0 && height > 0) {
                    int newWidth = (int) Math.round(
                           width * _scalePercentage/100.0);
                    int newHeight = (int) Math.round(
                           height * _scalePercentage/100.0);
                    _scaledImage = _image.getScaledInstance(
                           newWidth, newHeight, Image.SCALE_SMOOTH);
                    ListIterator figures = _figures.listIterator();
                    while (figures.hasNext()) {
                        Object figure = ((WeakReference)figures.next()).get();
                        if (figure == null) {
                            // The figure has been garbage collected, so we
                            // remove it from the list.
                            figures.remove();
                        } else {
                            // Repaint twice since the scale has changed
                            // and we need to cover the damage area prior
                            // the change as well as after.
                            ((ImageFigure)figure).repaint();
                            ((ImageFigure)figure).setImage(_scaledImage);
                            ((ImageFigure)figure).repaint();
                        }
                    }
                }
            }
        };
        SwingUtilities.invokeLater(doScale);
    }
    
    /** Specify an image to display.  This is deferred and executed
     *  in the Swing thread.
     *  @param image The image to display.
     */
    public void setImage(Image image) {
        _image = image;
        // Temporarily set the scaled image to the same image.
        // This will get reset when scaleImage() completes its work.
        _scaledImage = image;

        // Update the images of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                ListIterator figures = _figures.listIterator();
                while (figures.hasNext()) {
                    Object figure = ((WeakReference)figures.next()).get();
                    if (figure == null) {
                        // The figure has been garbage collected, so we
                        // remove it from the list.
                        figures.remove();
                    } else {
                        ((ImageFigure)figure).setImage(_scaledImage);
                        if (_scalePercentage != 100.0) {
                            scaleImage(_scalePercentage);
                        }
                    }
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of weak references to figures that this has created.
    private List _figures = new LinkedList();
    
    // The image that is the master.
    private Image _image;
    
    // The scaled version of the image that is the master.
    private Image _scaledImage;
    
    // The scale percentage.
    private double _scalePercentage = 100.0;
}

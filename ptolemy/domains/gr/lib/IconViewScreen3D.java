/* A GR scene viewer

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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.media.j3d.*;
import java.awt.*;
import javax.vecmath.Point3f;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.lib.ViewScreen;
import ptolemy.kernel.CompositeEntity;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.ImageIcon;

import com.sun.j3d.utils.universe.SimpleUniverse;

//////////////////////////////////////////////////////////////////////////
//// IconViewScreen3D
/** 
A sink actor that renders a two-dimensional scene into an Icon.  

NOTE: This doesn't seem to be possible (using the techniques I tried)
in the current version of Java3D without having the frame visible.
Alternatively, we would work on embedding a Canvas3D inside Diva, but
then we run into standard nastiness getting AWT objects to work inside
Swing objects.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.1
*/
public class IconViewScreen3D extends ViewScreen {

    /** Construct a ViewScreen2D in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this ViewScreen2D.
     *  @exception IllegalActionException If this actor
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public IconViewScreen3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        icon = new ImageIcon(this, "_icon");
    }
    
    /** The icon for this actor. */
    public ImageIcon icon;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the execution.  Create the IconViewScreen3D frame if 
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _frameNumber = 0;
        _frameWidth = _getHorizontalPixels();
        _frameHeight = _getVerticalPixels();
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     */
    protected void _createViewScreen() {
        GraphicsConfiguration config =
            SimpleUniverse.getPreferredConfiguration();

        int horizontalDimension = 400;
        int verticalDimension = 400;

        try {
            horizontalDimension = _getHorizontalPixels();
            verticalDimension = _getVerticalPixels();
        } catch (Exception e) {
            // FIXME handle this
        }

        // Create a frame, if placeable was not called.
        if (_container == null) {
            _frame = new JFrame("ViewScreen");
            _frame.show();
            _frame.validate();
            _frame.setSize(horizontalDimension + 50, verticalDimension);
            _container = _frame.getContentPane();
        }

        // Set the frame to be visible.
        if (_frame != null) {
            _frame.setVisible(true);
        }

        // Lastly drop the canvas in the frame.
        if (_canvas != null) {
            _container.remove(_canvas);
        }
        _canvas = new CapturingCanvas3D(config, false);

        _container.add("Center", _canvas);
        _canvas.setSize(new Dimension(horizontalDimension,
                verticalDimension));
        _simpleUniverse = new SimpleUniverse(_canvas);
        _simpleUniverse.getViewingPlatform().setNominalViewingTransform();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
 
    /** Class CapturingCanvas3D, using the instructions from the Java3D 
        FAQ pages on how to capture a still image in jpeg format.
        
        Peter Z. Kunszt
        Johns Hopkins University
        Dept of Physics and Astronomy
        Baltimore MD
     */    
    private class CapturingCanvas3D extends Canvas3D  {
        public CapturingCanvas3D(
                GraphicsConfiguration gc, boolean offscreen) {
            super(gc, offscreen);
        }
        
        public void postSwap() {
            GraphicsContext3D context = getGraphicsContext3D();
            // The raster components need all be set!
            Raster raster = new Raster(
                    new Point3f(-1.0f,-1.0f,-1.0f),
                    Raster.RASTER_COLOR,
                    0,0,
                    _frameWidth, _frameHeight,
                    new ImageComponent2D(
                            ImageComponent.FORMAT_RGB,
                            new BufferedImage(_frameWidth, _frameHeight,
                                    BufferedImage.TYPE_INT_RGB)),
                    null);
            
            context.readRaster(raster);
            
            // Now strip out the image info
            _image = raster.getImage().getImage();
            
            icon.setImage(_image);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private BufferedImage _image;
    private int _frameWidth = 400;
    private int _frameHeight = 400;
    private int _frameNumber;
    private int _frameRateValue;
}


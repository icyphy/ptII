/* A GR scene viewer

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (ismael@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/
package ptolemy.domains.gr.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.domains.gr.kernel.ViewScreenInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.OverlayLayer;
import diva.canvas.event.EventLayer;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.LayerListener;
import diva.canvas.event.LayerMotionListener;
import diva.canvas.toolbox.BasicFigure;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Iterator;

import javax.swing.JFrame;


//////////////////////////////////////////////////////////////////////////
//// ViewScreen2D

/**
A sink actor that renders a two-dimensional scene into a display
screen.  All mouse and keyboard events within the viewscreen are
handled by a ViewScreen2DListener.

@author Steve Neuendorffer, Ismael M. Sarmiento
@version $Id$
@since Ptolemy II 3.1
*/
public class ViewScreen2D extends GRActor2D
    implements Placeable, ViewScreenInterface{

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
    public ViewScreen2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setTypeEquals(Scene2DToken.TYPE);
        sceneGraphIn.setMultiport(true);

        horizontalPixels = new Parameter(this,
                "horizontalPixels", new IntToken(400));
        horizontalPixels.setTypeEquals(BaseType.INT);

        verticalPixels = new Parameter(this,
                "verticalPixels", new IntToken(400));
        verticalPixels.setTypeEquals(BaseType.INT);

        upperLeftX = new Parameter(this,
                "upperLeftX", new IntToken(0));
        upperLeftX.setTypeEquals(BaseType.INT);

        upperLeftY = new Parameter(this,
                "upperLeftY", new IntToken(400));
        upperLeftY.setTypeEquals(BaseType.INT);

        lowerRightX = new Parameter(this,
                "lowerRightX", new IntToken(400));
        lowerRightX.setTypeEquals(BaseType.INT);

        lowerRightY = new Parameter(this,
                "lowerRightY", new IntToken(0));
        lowerRightY.setTypeEquals(BaseType.INT);

        rotatable = new Parameter(this,
                "rotatable", new BooleanToken(true));
        rotatable.setTypeEquals(BaseType.BOOLEAN);

        scalable = new Parameter(this,
                "scalable", new BooleanToken(false));
        scalable.setTypeEquals(BaseType.BOOLEAN);

        showAxes = new Parameter(this,"showAxes", new BooleanToken(false));
        showAxes.setTypeEquals(BaseType.BOOLEAN);

        translatable = new Parameter(this,
                "translatable", new BooleanToken(false));
        translatable.setTypeEquals(BaseType.BOOLEAN);

        backgroundColor = new ColorAttribute(this, "backgroundColor");
        backgroundColor.setExpression("{1.0, 1.0, 1.0, 1.0}");

        _originRelocatable = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The red, green, blue, and alpha components of the background color
     *  of the viewscreen.  This parameter must contain an array of double
     *  values.  The default value is {1.0, 1.0, 1.0, 1.0}, corresponding to
     *  opaque white.
     */
    public ColorAttribute backgroundColor;

    /** The input scene graph.
     */
    public TypedIOPort sceneGraphIn;

    /** The horizontal resolution of the display screen.
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is the IntToken 400.
     */
    public Parameter horizontalPixels;

    /** Boolean variable that determines if the user is allowed to
     *  rotate the scene.
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken true.
     */
    public Parameter rotatable;

    /** Boolean variable that determines if the user is allowed to
     *   scale the scene.
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken false.
     */
    public Parameter scalable;

    /** Boolean variable that determines if the axes crosshairs are shown
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken true.
     */
    public Parameter showAxes;

    /** Boolean variable that determines if the user is allowed to
     *  translate the scene.
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken false.
     */
    public Parameter translatable;

    /** The vertical resolution of the display screen.
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is IntToken 400.
     */
    public Parameter verticalPixels;

    /** The x coordinate of the canvas point visible in the upper left
     * corner of the display screen.
     */
    public Parameter upperLeftX;

    /** The y coordinate of the canvas point visible in the upper left
     * corner of the display screen.
     */
    public Parameter upperLeftY;

    /** The x coordinate of the canvas point visible in the lower right
     * corner of the display screen.
     */
    public Parameter lowerRightX;

    /** The y coordinate of the canvas point visible in the lower right
     * corner of the display screen.
     */
    public Parameter lowerRightY;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Repaint the canvas.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // Repaint the entire canvas.  This ensures that drawing is
        // done correctly, despite the fact that all of the transform
        // calls on figures are happening outside of the swing thread.

        _canvas.repaint();
    }

    /** Return the Diva canvas used by this view screen.
     */
    public JCanvas getCanvas() {
        return _canvas;
    }

    /** Return the horizontal component of the crosshair which marks
     * the origin.
     * @return The horizontal component of the crosshair which marks
     * the origin.
     */
    public BasicFigure getCrosshairX() {
        return _crosshairX;
    }


    /** Return the vertical component of the crosshair which marks the
     * origin.
     * @return The vertical component of the crosshair which marks the
     * origin.
     */
    public BasicFigure getCrosshairY() {
        return _crosshairY;
    }

    /** Return the location of the origin of the viewscreen.
     * @return The origin of the viewscreen.
     */
    public Point2D.Double getOrigin() {
        return _origin;
    }

    /** Return the figure currently selected in the viewscreen.
     * @return The figure currently selected in the viewscreen.
     */
    public Figure getSelectedFigure() {
        return _selectedFigure;
    }


    /** Initialize the execution.  Create the ViewScreen2D frame if
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {

        super.initialize();

        // Create a frame, if necessary, along with the canvas and
        // simple universe.
        _createViewScreen2D();

        // Make the frame visible.
        if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }

        _canvas.setBackground(backgroundColor.asColor());

        if (_isRotatable()) {
            // FIXME: handle rotation
        }

        if (_isScalable()) {
            // FIXME: handle scaling
        }

        if (_isTranslatable()) {
            // FIXME: handle translation
        }
    }

    /** Set the container that this actor should display data in.  If
     * place is not called, then the actor will create its own frame
     * for display.
     */
    public void place(Container container) {
        _container = container;

        if (_container == null) return;
        Container c = _container.getParent();
        while (c.getParent() != null) {
            c = c.getParent();
        }
        // If we had created a frame before, then blow it away.
        if(_frame != null) {
            _frame.dispose();
            _frame = null;
        }
        try {
            _createViewScreen2D();
        } catch (IllegalActionException ex) {
            // Ignore;
        }
    }


    /** Update the state of this object to reflect which figure is
     *  currently selected in the viewscreen.
     * @param figure The figure currently selected.
     */
    public void setSelectedFigure(Figure figure)
    {
        _selectedFigure = figure;
        System.out.println("setSelectedFigure Called");
    }

    /** Wrap up an execution
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _isSceneGraphInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**  Add a figure to the figure layer and set its interactor.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    protected void _addChild(Figure figure) throws IllegalActionException {
        if(figure.getInteractor() instanceof FigureInteractor) {
            ((FigureInteractor)(figure.getInteractor())).setViewScreen(this);
        }
        _layer.add(figure);
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     */
    protected void _createViewScreen2D() throws IllegalActionException {
        // Default size.
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
            _frame = new JFrame("ViewScreen2D");
            _frame.show();
            _frame.validate();
            //  _frame.setSize(horizontalDimension+50,verticalDimension);
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
        GraphicsPane pane = new GraphicsPane();
        _layer = pane.getForegroundLayer();
        _overlayLayer = new OverlayLayer();

        _canvas = new JCanvas(pane);

        _container.add("Center", _canvas);
        _canvas.setMinimumSize(new Dimension(horizontalDimension,
                verticalDimension));
        _canvas.setMaximumSize(new Dimension(horizontalDimension,
                verticalDimension));
        _canvas.setPreferredSize(new Dimension(horizontalDimension,
                verticalDimension));
        if(_frame != null) {
            _frame.pack();
        }

        double upperLeftXValue =
            ((IntToken)upperLeftX.getToken()).doubleValue();
        double upperLeftYValue =
            ((IntToken)upperLeftY.getToken()).doubleValue();
        double lowerRightXValue =
            ((IntToken)lowerRightX.getToken()).doubleValue();
        double lowerRightYValue =
            ((IntToken)lowerRightY.getToken()).doubleValue();
        java.awt.geom.Rectangle2D visibleRect =
            new java.awt.geom.Rectangle2D.Double(
                    upperLeftXValue,
                    upperLeftYValue,
                    lowerRightXValue - upperLeftXValue,
                    lowerRightYValue - upperLeftYValue);
        if(visibleRect.getHeight() == 0 || visibleRect.getWidth() == 0) {
            throw new IllegalActionException(this, "The width and height " +
                    "of the visible rectangle cannot be zero.");
        }

        AffineTransform transform = CanvasUtilities.computeTransform(
                visibleRect, _canvas.getBounds());
        System.out.println("transform = " + transform);

        //       _origin = new Point2D.Double(
        //                 _container.getWidth()/2, _container.getHeight()/2);
        //         pane.translate(_origin.x, _origin.y);

        pane.setTransform(transform);
        pane.setAntialiasing(true);

        _crosshairX = new BasicFigure(
                new java.awt.geom.Line2D.Double(0, 2, 0, -2));
        _crosshairY = new BasicFigure(
                new java.awt.geom.Line2D.Double(2, 0, -2, 0));
        _overlayLayer.add(_crosshairX.getShape());
        _overlayLayer.add(_crosshairY.getShape());

        _eventHandler = new ViewScreen2DListener();
        _eventLayer  = new EventLayer();
        _eventLayer.addLayerListener(_eventHandler);
        _eventLayer.addLayerMotionListener(_eventHandler);

        pane.setOverlayLayer(_overlayLayer);
        pane.setForegroundEventLayer(_eventLayer);

        if(_frame != null) {
            _frame.addKeyListener(_eventHandler);
        }

        // Must call validate after calling add in case this is called
        // from initialize AND from place.  (This caused some
        // artifacts if the run window was used)
        _container.validate();
    }

    protected int _getHorizontalPixels() throws IllegalActionException {
        return ((IntToken) horizontalPixels.getToken()).intValue();
    }

    protected int _getVerticalPixels() throws IllegalActionException {
        return ((IntToken) verticalPixels.getToken()).intValue();
    }

    protected boolean _isRotatable() throws IllegalActionException  {
        return ((BooleanToken) rotatable.getToken()).booleanValue();
    }

    protected boolean _isScalable() throws IllegalActionException  {
        return ((BooleanToken) scalable.getToken()).booleanValue();
    }

    protected boolean _isTranslatable() throws IllegalActionException  {
        return ((BooleanToken) translatable.getToken()).booleanValue();
    }

    /** Set up the scene graph connections of this actor.
     */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        int width = sceneGraphIn.getWidth();
        for (int i = 0 ; i < width; i++) {
            Scene2DToken sceneToken = (Scene2DToken)
                sceneGraphIn.get(i);
            Figure figure = sceneToken.getFigure();
            _addChild(figure);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private inner classes               ////

    public class ViewScreen2DListener
        implements LayerListener, LayerMotionListener, KeyListener {

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Forward any keyPressed events to the figure which is selected
         *  in the viewscreen.  Allow the origin of the viewscreen to be
         *  translated if the user holds the 'o' key.
         * @param e The KeyEvent generated by the frame containing the
         * viewscreen.
         */
        public void keyPressed(KeyEvent e) {
            Figure selectedFigure = getSelectedFigure();
            Iterator allFigures = _layer.figures();

            if (selectedFigure != null) {
                ((FigureInteractor)selectedFigure.getInteractor())
                    .keyPressed(e);
                while(allFigures.hasNext()) {
                    AbstractFigure figure =
                        ((AbstractFigure)allFigures.next());
                    //if(selectedFigure.hit(figure.getFigure().getBounds()))
                    {

                        //((RectangularFigure2D)selectedFigure)
                        //.collision(), false);

                    }
                }
            }

            if (e.getKeyCode() == KeyEvent.VK_O) {
                _originRelocatable = true;
            }

            if (e.getKeyCode() == KeyEvent.VK_Z) {
                _canvas.getCanvasPane().scale(_origin.x, _origin.y, 2, 2);
            }

            if (e.getKeyCode() == KeyEvent.VK_A) {
                _canvas.getCanvasPane().scale(_origin.x, _origin.y, .5, .5);

            }
        }


        /** Forward any keyReleased events to the figure which is
         * selected in the viewscreen.  Disable relocation of the
         * origin of the viewscreen if the user releases the 'o' key.
         * @param e The KeyEvent generated by the frame containing the
         * viewscreen.
         */
        public void keyReleased(KeyEvent e) {
            Figure selectedFigure = getSelectedFigure();
            if(selectedFigure != null) {
                ((FigureInteractor)selectedFigure
                        .getInteractor()).keyReleased(e);
            }

            if (e.getKeyCode() == KeyEvent.VK_O) {
                _originRelocatable = true;
            }
        }


        /** Forward any keyTyped events to the figure which is
         *  selected in the viewscreen.  If no figure is selected,
         *  this method does nothing.
         * @param e The KeyEvent generated by the frame containing the
         * viewscreen.
         */
        public void keyTyped(KeyEvent e) {
            Figure selectedFigure = getSelectedFigure();

            if(selectedFigure != null) {
                ((FigureInteractor)selectedFigure.getInteractor()).keyTyped(e);
            }
        }


        /** Included to comply with the LayerListener interface
         *  requirements.  This method does nothing in its current
         *  implementation.
         *  @param e The layer event generated by the informing layer.
         */
        public void mouseClicked(LayerEvent e) {
        }


        /** Translate the origin of the viewScreen2D if the user drags the
         *  origin marker.  To enable this function, the user must hold
         *  the 'o' key while dragging the origin marker.
         * @param e The layer event generated by the informing layer.
         */
        public void mouseDragged(LayerEvent e) {
            Iterator allFigures = _layer.figures();

            //Translate the origin while the mouse cursor is in the window
            //and the user is dragging the origin marker.
            if(_originRelocatable && _mouseInWindow &&
                    (getCrosshairX().getBounds().contains(e.getLayerPoint()) ||
                            getCrosshairY().getBounds()
                            .contains(e.getLayerPoint()) ||
                            _mouseDragging == true)) {

                _mouseDragging = true;
                _canvas.setCursor(
                        Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                _origin = (Point2D.Double)e.getLayerPoint();
                _canvas.getCanvasPane().translate(_origin.x, _origin.y);
            }

        }


        /** Update the state of this object to reflect that the mouse
         * is in the viewscreen.
         * @param e The layer event generated by the informing layer.
         */
        public void mouseEntered(LayerEvent e) {
            _mouseInWindow = true;
        }


        /** Update the state of this object to reflect that the mouse is
         * no longer in the viewscreen.
         */
        public void mouseExited(LayerEvent e) {
            _mouseInWindow = false;
        }


        /** Included to comply with the LayerMotionListener interface
         *  requirement.  This method does nothing in its current
         *  implementation.
         *  @param e The layer event generated by the informing layer.
         */
        public void mouseMoved(LayerEvent e) {}


        /** Update the status of any figures which were deselected by the
         *  user clicking on a blank area of the viewscreen.
         *  @param e The layer event generated by the informing layer.
         */
        public void mousePressed(LayerEvent e) {
            Iterator figureIterator = _layer.figures();
            while(figureIterator.hasNext()) {
                Figure figure = (Figure)figureIterator.next();
                if(!figure.contains(e.getLayerPoint())) {
                    ((FigureInteractor)figure.getInteractor())
                        .setSelected(false);
                }
            }
        }


        /** Update the state of this object to reflect that the mouse is no
         *  longer being dragged.
         * @param e The layer event generated by the informing layer.
         */
        public void mouseReleased(LayerEvent e) {
            _mouseDragging = false;
            _canvas.setCursor(
                    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }


        ///////////////////////////////////////////////////////////////////
        ////                         private fields                    ////

        // Whether or not the mousing is being dragged.
        private boolean _mouseDragging;

        // Whether or not the mouse is in the window.
        private boolean _mouseInWindow;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    // The diva canvas component.
    private JCanvas _canvas;

    // The container set in the place() method, or the content pane of the
    // created frame if place was not called.
    private Container _container;

    //The horizontal portion of the origin marker.
    private BasicFigure _crosshairX;

    //The vertical portion of the origin marker.
    private BasicFigure _crosshairY;

    //A listener to handle mouse events and keystrokes within the viewscreen.
    private ViewScreen2DListener _eventHandler;

    //A layer for handling events on the viewscreen.
    private EventLayer _eventLayer;

    // The frame containing our canvas, if we created it.
    private JFrame _frame;

    // The Figure layer containing the figures being displayed.
    private FigureLayer _layer;

    //The location of the origin on the viewscreen.  By default is value is the
    //center of the viewscreen.
    private Point2D.Double _origin;

    //Whether or not the origin can be relocated.
    private boolean _originRelocatable;

    //An overlay layer to display objects (such as the origin marker) which are
    //to be displayed on top of all other figures.
    private OverlayLayer _overlayLayer;

    //The figure, if any, currently selected in the viewscreen.
    private Figure _selectedFigure;
}

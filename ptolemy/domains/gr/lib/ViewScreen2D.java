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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Placeable;
import ptolemy.apps.superb.actor.lib.ViewScreen2DListener;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.GRUtilities2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.domains.gr.kernel.ViewScreenInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.OverlayLayer;
import diva.canvas.event.EventLayer;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.toolbox.BasicFigure;

//////////////////////////////////////////////////////////////////////////
//// ViewScreen

/** 
A sink actor that renders a two-dimensional scene into a display screen.

@author Steve Neuendorffer, Ismael M. Sarmiento
@version $Id$
@since Ptolemy II 1.0
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

        horizontalResolution = new Parameter(this,
                "horizontalResolution", new IntToken(400));
        horizontalResolution.setTypeEquals(BaseType.INT);

        verticalResolution = new Parameter(this,
                "verticalResolution", new IntToken(400));
        verticalResolution.setTypeEquals(BaseType.INT);

        rotatable = new Parameter(this,
                "rotatable", new BooleanToken(true));
        rotatable.setTypeEquals(BaseType.BOOLEAN);

        scalable = new Parameter(this,
                "scalable", new BooleanToken(false));
        scalable.setTypeEquals(BaseType.BOOLEAN);

        translatable = new Parameter(this,
                "translatable", new BooleanToken(false));
        translatable.setTypeEquals(BaseType.BOOLEAN);

        backgroundColor = new Parameter(this, "backgroundColor",
                new DoubleMatrixToken(new double[][] {{ 1.0, 1.0, 1.0}} ));
        backgroundColor.setTypeEquals(BaseType.DOUBLE_MATRIX);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The input scene graph.
     */
    public TypedIOPort sceneGraphIn;

    /** The background color, given as a 3-element array representing
     *  RGB color components.
     */
    public Parameter backgroundColor;

    /** The horizontal resolution of the display screen.
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is the IntToken 400.
     */
    public Parameter horizontalResolution;

    /** Boolean variable that determines if the user is allowed to
     *   rotate the scene.
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken true.
     */
    public Parameter rotatable;

    /** Boolean variable that determines if the user is allowed to
     *   scale the scene.
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken false.Th
     */
    public Parameter scalable;

    /** Boolean variable that determines if the user is allowed to
     *   translate the scene.
     *  This parameter should contain a BooleanToken.
     *  The default value of this parameter is BooleanToken false.
     */
    public Parameter translatable;

    /** The vertical resolution of the display screen.
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is IntToken 400.
     */
    public Parameter verticalResolution;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor. 
     */
    public void fire() throws IllegalActionException {
        super.fire();
        // Repaint the entire canvas.  This ensures that drawing is
        // done correctly, despite the fact that all of the transform
        // calls on figures are happening outside of the swing thread.
        
        
        //_canvas.repaint();
    }

    /** Return the Diva canvas used by this view screen.
     */
    public JCanvas getCanvas() {
        return _canvas;
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
        }
                
        // FIXME: default = ??  
        DoubleMatrixToken colorVector =
            (DoubleMatrixToken) backgroundColor.getToken();
        Color backgroundColor =  GRUtilities2D.makeColor(colorVector);
        _canvas.setBackground(backgroundColor);

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
        _createViewScreen2D();
    }

    /** Wrapup an execution
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _isSceneGraphInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

   /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor. Derived GR Actors should override this method
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    protected void _addChild(Figure figure) throws IllegalActionException {
        _layer.add(figure);
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     */
    protected void _createViewScreen2D() {
        // Default size.
        int horizontalDimension = 400;
        int verticalDimension = 400;

        try {
            horizontalDimension = _getHorizontalResolution();
            verticalDimension = _getVerticalResolution();
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
        
        _origin = new Point2D.Double(_container.getWidth()/2, _container.getHeight()/2);
        pane.translate(_origin.x, _origin.y);       
        pane.setAntialiasing(true);
        
        _crosshairX = new BasicFigure(new Line2D.Double(0,2,0,-2));
        _crosshairY = new BasicFigure(new Line2D.Double(2,0,-2,0));
        
        _eventHandler = new ViewScreen2DListener(
            _crosshairX.getBounds(), _crosshairY.getBounds(), _origin, _canvas);
        _eventLayer  = new EventLayer();
        _eventLayer.addLayerListener(_eventHandler);
        _eventLayer.addLayerMotionListener(_eventHandler);
        _overlayLayer.add(_crosshairX.getShape());
        _overlayLayer.add(_crosshairY.getShape());
        pane.setOverlayLayer(_overlayLayer);
        pane.setForegroundEventLayer(_eventLayer);
        
        Graphics2D graphics = (Graphics2D)_container.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }

    /** Setup the scene graph connections of this actor.  
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
    ////                         private methods                   ////
    protected int _getHorizontalResolution() throws IllegalActionException {
        return ((IntToken) horizontalResolution.getToken()).intValue();
    }

    protected int _getVerticalResolution() throws IllegalActionException {
        return ((IntToken) verticalResolution.getToken()).intValue();
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
    
    
    // The diva canvas component.
    private JCanvas _canvas;
    // The container set in the place() method, or the content pane of the
    // created frame if place was not called.
    private Container _container;
    // The frame containing our canvas, if we created it.
    private JFrame _frame;
    // The Figure layer we are using.
    private FigureLayer _layer;
    
    private OverlayLayer _overlayLayer;
    
    private EventLayer _eventLayer; 
    
    private Point2D.Double _origin;
    
    private DragInteractor dragInteractor;
    
    private MouseInputAdapter mouseAdapter;
    
    private BasicFigure _crosshairX;
    private BasicFigure _crosshairY;
    private ViewScreen2DListener _eventHandler;
}


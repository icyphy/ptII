package ptolemy.schematic.editor;

import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.interactor.*;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.LayerEventMulticaster;
import diva.canvas.event.LayerListener;
import diva.canvas.event.LayerMotionListener;
import diva.canvas.event.MouseFilter;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

/** 
 * This class is an interactor that works as a drag recognizer.  It 
 * detects mouse events and triggers the start of a drag and drop sequence.
 *
 * @version $Id$
 * @author Steve Neuendorffer
 */
public class DragRecognizer 
    extends DragGestureRecognizer implements LayerListener {

    public DragRecognizer(GraphController c, DragSource source, 
				    Component component, int actions, 
				    DragGestureListener listener) {
	super(source, component, actions, listener);
	_controller = c;
    }

    public void registerListeners() {
	GraphController controller = ((SchematicPalette) getComponent()).getGraphPane().getGraphController();
	System.out.println("controller = " + controller);
	NodeInteractor ni = controller.getNodeInteractor();
	System.out.println("nodeinteractor = " + ni);
	DragInteractor di = ni.getDragInteractor();
	di.addLayerListener(this);
    }
	
    public void mouseDragged (LayerEvent layerEvent) {
	appendEvent(layerEvent);
	int currentX = layerEvent.getX();
	int currentY = layerEvent.getY();
	int deltaX = java.lang.Math.abs(currentX - _startX);
	int deltaY = java.lang.Math.abs(currentY - _startY);
	if((deltaX + deltaY) > _gestureDistance) {
	    ((SchematicPalette) getComponent()).setDraggedNode((Node)_draggedFigure.getUserObject());
	    System.out.println("source action = " + getSourceActions());
	    fireDragGestureRecognized(DnDConstants.ACTION_COPY, 
				     _startPoint);
	}
    }

    public void mousePressed (LayerEvent layerEvent) {
	System.out.println("mouse pressed");
	_draggedFigure = layerEvent.getFigureSource();
	_startX = layerEvent.getX();
	_startY = layerEvent.getY();
	_startPoint = layerEvent.getPoint();
    }

    /** 
     */
    public void mouseReleased (LayerEvent layerEvent) {
	System.out.println("mouse released");
        _draggedFigure = null;
    }

    public void resetRecognizer() {
	super.resetRecognizer();
	_draggedFigure = null;
    }
    public void unregisterListeners() {
	NodeInteractor ni = _controller.getNodeInteractor();
	DragInteractor di = ni.getDragInteractor();
	di.removeLayerListener(this);
    }

    /** The controller that this interactor is a part of
     */
    private GraphController _controller;
    private Figure _draggedFigure;
    private int _startX;
    private int _startY;
    private Point _startPoint;
    private int _gestureDistance = DEFAULTGESTUREDISTANCE;
    
    public static final int DEFAULTGESTUREDISTANCE = 6;
}

/* An icon that displays a specified java.awt.Shape.

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
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;

//////////////////////////////////////////////////////////////////////////
//// ShapeIcon
/**
An icon that displays a specified java.awt.Shape.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ShapeIcon extends DynamicEditorIcon {

    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ShapeIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
    
    /** Create a new icon with the given name in the given container
     *  with the given default shape.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @param defaultShape The default shape.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ShapeIcon(NamedObj container, String name, Shape defaultShape)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _defaultShape = defaultShape;
        setShape(defaultShape);
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
        ShapeIcon newObject = (ShapeIcon)super.clone(workspace);
        // NOTE: Do not set back to the default shape!
        // newObject._shape = _defaultShape;
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
        BasicFigure newFigure;
        if (_shape != null) {
            newFigure = new BasicFigure(_shape);
        } else {
            // Create a white rectangle.
            newFigure = new BasicFigure(new Rectangle2D.Double(
                   0.0, 0.0, 20.0, 20.0));
        }
        // By default, the origin should be the upper left.
        newFigure.setCentered(_centered);
        newFigure.setLineWidth(_lineWidth);
        newFigure.setStrokePaint(_lineColor);
        newFigure.setFillPaint(_fillColor);
        
        _addLiveFigure(newFigure);
        
        return newFigure;
    }
    
    /** Return whether the figure should be centered on its origin.
     *  @return False If the origin of the figure, as
     *   returned by getOrigin(), is the upper left corner.
     */
    public boolean isCentered() {
        return _centered;
    }

    /** Specify whether the figure should be centered or not.
     *  By default, the origin of the figure is the center.
     *  This is deferred and executed in the Swing thread.
     *  @param centered False to make the figure's origin at the
     *   upper left.
     */
    public void setCentered(boolean centered) {
        _centered = centered;
        
        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                 Iterator figures = _liveFigureIterator();
                while (figures.hasNext()) {
                    Object figure = figures.next();
                    ((BasicFigure)figure).setCentered(_centered);
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }
    
    /** Specify the fill color to use.  This is deferred and executed
     *  in the Swing thread.
     *  @param fillColor The fill color to use.
     */
    public void setFillColor(Color fillColor) {
        _fillColor = fillColor;
        
        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                Iterator figures = _liveFigureIterator();
                while (figures.hasNext()) {
                    Object figure = figures.next();
                    ((BasicFigure)figure).setFillPaint(_fillColor);
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }
    
    /** Specify the line color to use.  This is deferred and executed
     *  in the Swing thread.
     *  @param lineColor The line color to use.
     */
    public void setLineColor(Color lineColor) {
        _lineColor = lineColor;
        
        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                Iterator figures = _liveFigureIterator();
                while (figures.hasNext()) {
                    Object figure = figures.next();
                    ((BasicFigure)figure).setStrokePaint(_lineColor);
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }

    /** Specify the line width to use.  This is deferred and executed
     *  in the Swing thread.
     *  @param lineWidth The line width to use.
     */
    public void setLineWidth(float lineWidth) {
        _lineWidth = lineWidth;
        
        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                Iterator figures = _liveFigureIterator();
                while (figures.hasNext()) {
                    Object figure = figures.next();
                    ((BasicFigure)figure).setLineWidth(_lineWidth);
                }
            }
        };
        SwingUtilities.invokeLater(doSet);
    }

    /** Specify a path to display.  This is deferred and executed
     *  in the Swing thread.
     *  @param path The path to display.
     */
    public void setShape(Shape path) {
        _shape = path;

        // Update the shapes of all the figures that this icon has
        // created (which may be in multiple views). This has to be
        // done in the Swing thread.  Assuming that createBackgroundFigure()
        // is also called in the Swing thread, there is no possibility of
        // conflict here where that method is trying to add to the _figures
        // list while this method is traversing it.
        Runnable doSet = new Runnable() {
            public void run() {
                Iterator figures = _liveFigureIterator();
                while (figures.hasNext()) {
                    Object figure = figures.next();
                    ((BasicFigure)figure).setPrototypeShape(_shape);
                }
            }
        };
        Top.deferIfNecessary(doSet);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicator of whether the figure should be centered on its origin.
    private boolean _centered = false;
    
    // Default shape specified in the constructor.
    private Shape _defaultShape;
    
    // The specified fill color.
    private Color _fillColor = Color.white;

    // The specified line color.
    private Color _lineColor = Color.black;
      
    // The specified line width.
    private float _lineWidth = 1f;
    
    // The shape that is rendered.
    private Shape _shape;
    
    // The scale percentage.
    private double _scalePercentage = 100.0;
}

/* A component for editing icons.

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

// Diva imports.
import java.awt.Paint;
import java.awt.Shape;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RectangularShape;
import java.awt.print.PrinterJob;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.interactor.BasicSelectionModel;
import diva.canvas.interactor.BoundsManipulator;
import diva.canvas.interactor.CircleManipulator;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.PathManipulator;
import diva.canvas.interactor.SelectionDragger;
import diva.canvas.interactor.SelectionInteractor;
import diva.util.java2d.PaintedObject;
import diva.util.java2d.Polygon2D;

// Java imports.

// Javax imports.

// Ptolemy imports.

//////////////////////////////////////////////////////////////////////////
//// IconEditorPane
/**

@author Nick Zamora (nzamor@uclink4.berkeley.edu)
@author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
@version $Id$
@since Ptolemy II 1.0
*/
public class IconEditorPane extends JCanvas {

    /**
     * Create a new icon editor acting on the given icon.
     */
    public IconEditorPane(XMLIcon icon) {
        _icon = icon;

        // Instantiate the color chooser for the color button.
        _colorChooser = new JColorChooser();

        _pane =(GraphicsPane) this.getCanvasPane();
        _layer = _pane.getForegroundLayer();
        _layer.setPickHalo(MOUSE_SENSITIVITY);

        // I have to make the figures "drag-able".
        _interactor1.addInteractor(new DragInteractor());
        _interactor2.addInteractor(new DragInteractor());
        _interactor3.addInteractor(new DragInteractor());

        // When they are selected, put grab handles on them.
        _interactor1.setPrototypeDecorator(new PathManipulator());
        _interactor2.setPrototypeDecorator(new BoundsManipulator());
        _interactor3.setPrototypeDecorator(new CircleManipulator());

        // This next part allows the user to select multiple figures
        // with the mouse by dragging a rectangle box around the figures
        // the user wishes to be selected.
        _selectionDragger = new SelectionDragger(_pane);
        _selectionDragger.addSelectionModel(
                _interactor1.getSelectionModel());
        _selectionDragger.addSelectionModel(
                _interactor2.getSelectionModel());
        _selectionDragger.addSelectionModel(
                _interactor3.getSelectionModel());

        // Begin with the elements specified in the icon passed into
        // the constructor.
        addXMLIcon(icon);
    }

    //         StringBufferInputStream xml_stream = null;
    //         xml_stream = new StringBufferInputStream("<xmlgraphic> <rectangle coords=\"0 0 60 40\" fill=\"white\"/> <polygon coords=\"10 10 50 20 10 30\" fill=\"blue\"/> </xmlgraphic>\n");


    ///////////////////////////////////////////////////////////////////
    ///////////////////      Private variables.       /////////////////////

    // The icon of the icon editor application.

    private XMLIcon _icon;

    // Create the combo box for the toolbars(pull-down menus)

    //private JComboBox _thicknessComboBox;

    //private JComboBox _fillComboBox;

    //private JComboBox _outlineComboBox;

    // Used to distinguish which color we are changing, the fill of
    // the shape or the outline of the shape.

    //private boolean _changingFill;

    // The color chooser.

    private JColorChooser _colorChooser;

    // Here are the interactors for each shape

    private BasicSelectionModel _m = new BasicSelectionModel();

    private SelectionInteractor _interactor1 = new SelectionInteractor(_m);

    private SelectionInteractor _interactor2 = new SelectionInteractor(_m);

    private SelectionInteractor _interactor3 = new SelectionInteractor(_m);

    // For dragging

    private SelectionDragger _selectionDragger;

    // This is the current shape, line thickness, and paint colors.

    private VersatileFigure _currentFigure = null;

    private float _outlineThickness = 3.0f;

    // Blue and Gold(Go Bears!)

    //private Paint _strokeColor = new Color(255, 213, 20);

    //private Paint _fillColor = new Color(0, 0, 170);

    // Here is the figure kept in memory for the "cut" or
    // "pasted" figure.

    //private VersatileFigure _cutOrCopiedFigure = null;

    // Window objects

    private GraphicsPane _pane;

    private FigureLayer _layer;

    //    private JCanvas this;

    //private JDialog _dialog;

    // The help "About" frame.

    //private JFrame _helpFrame;

    // Constants for the program.  Decreasing MOUSE_SENSITIVITY will require
    // the user to be more precise when trying to click on figures.

    private static final double MOUSE_SENSITIVITY = 4.0;

    // Defines the horizontal and vertical size of the main window.

    //private static final int WINDOW_SIZE_HORIZONTAL = 600;

    //private static final int WINDOW_SIZE_VERTICAL = 300;

    // This is the extension we allow for opening and saving files within the
    // program.

    //private static final String FILE_FORMAT_EXTENSION = "xml";

    // The type of data that is operable via the cut, copy, and paste commands.

    public static final DataFlavor dataFlavor =
    new DataFlavor(VersatileFigure.class, "Versatile Figure");

    ///////////////////////////////////////////////////////////////////
    //////////////////      Private methods.            /////////////////

    ///////////////////////////////////////////////////////////////////
    //////////////////      Inner Classes.              /////////////////

    ///////////////////////////////////////////////////////////////////
    /////////////////      Public Methods               /////////////////

    public void addFigure(VersatileFigure figure) {
        // Get the shape of this figure.
        Shape shape = figure.getShape();

        // Now assign an appropriate interactor for the shape.
        if (shape instanceof RectangularShape) {
            figure.setInteractor(_interactor2);
        } else if (shape instanceof Ellipse2D) {
            figure.setInteractor(_interactor3);
        } else if (shape instanceof GeneralPath) {
            figure.setInteractor(_interactor1);
        } else if (shape instanceof Line2D) {
            figure.setInteractor(_interactor1);
        } else if (shape instanceof QuadCurve2D) {
            figure.setInteractor(_interactor1);
        } else if (shape instanceof CubicCurve2D) {
            figure.setInteractor(_interactor1);
        } else if (shape instanceof Polygon2D) {
            figure.setInteractor(_interactor2);
        } else {
            throw new RuntimeException("unknown shape: " + shape);
        }

        // Finally, add the figure to the figure layer.
        _layer.add(figure);

        _selectionDragger.clearSelection();
        _selectionDragger.expandSelection(_interactor2, _currentFigure);
    }

    /** I need to setup the icon that was passed in, possibly from
     * another application.  I enumerate over the graphical elements
     * contained within the XMLIcon and, for each element, I create a
     * versatile figure from it and add it to the FigureLayer.  Also,
     * I set up an appropriate interactor for each figure so that the
     * figure can be edited.
     *
     * Note that this icon being added adds its graphic elements to
     * the _icon member associated with this instance.
     */
    public void addXMLIcon(XMLIcon my_icon) {
        // First get an enumeration over all the elements in the icon.
        Iterator i = my_icon.paintedList().paintedObjects.iterator();

        // And as long as the icon has more elements...
        while (i.hasNext()) {
            // Create a new figure represented by this graphic element.
            VersatileFigure figure =
                new VersatileFigure((PaintedObject)i.next());

            addFigure(figure);
        }
    }

    /**
     * Clear all the figures and remove all the graphic elements from the
     * icon.
     */
    public void clear() {
        _m.clearSelection();
        _layer.clear();
        _icon.paintedList().paintedObjects.clear();
    }

    /** Copy the currently selected item onto the clipboard.
     * Beep if something goes wrong.
     */
    public void copy(Clipboard clipboard) {
        Iterator iter = _m.getSelection();
        Vector v = new Vector();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                _currentFigure = (VersatileFigure)iter.next();
                v.add(_currentFigure);
            }
            SimpleSelection s = new SimpleSelection(v, dataFlavor);

            clipboard.setContents(s, s);

        } else {
            getToolkit().beep();
        }
    }

    /**
     * Put the currently selected item onto the given clipboard, and remove
     * the currently selected item from the canvas.
     */
    public void cut(Clipboard clipboard) {
        Iterator iter = _m.getSelection();
        Vector vector = new Vector();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                _currentFigure =(VersatileFigure) iter.next();
                iter.remove();
                _m.removeSelection(_currentFigure);
                _layer.remove(_currentFigure);
                vector.add(_currentFigure);
            }
            SimpleSelection s = new SimpleSelection(vector,
                    dataFlavor);
            clipboard.setContents(s, s);
        } else {
            getToolkit().beep();
        }
    }

    /**
     * Delete the currently selected figures.
     */
    public void delete() {
        Iterator iter = _m.getSelection();
        VersatileFigure v = null;
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                v =(VersatileFigure) iter.next();
                iter.remove();
                _m.removeSelection(v);
                _layer.remove(v);
            }
        } else {
            getToolkit().beep();
        }
    }

    /**
     * Return the fill color of the currently selected figures.  If
     * no figures are selected or the selected figures have different fills,
     * then return null.
     */
    public Paint getFillPaint() {
        Paint paint = null;
        Iterator iter = _m.getSelection();
        while (iter.hasNext()) {
            VersatileFigure v =(VersatileFigure) iter.next();
            if (paint == null) {
                paint = v.getFillPaint();
            } else {
                if (paint != v.getFillPaint())
                    return null;
            }
        }
        return paint;
    }

    /**
     * Return the outline thickness of the currently selected figures.  If
     * no figures are selected or the selected figures have different fills,
     * then return null.
     */
    public float getThickness() {
        float thickness = 0.0f;
        Iterator iter = _m.getSelection();
        while (iter.hasNext()) {
            VersatileFigure v =(VersatileFigure) iter.next();
            if (thickness == 0.0f) {
                thickness = v.getLineWidth();
            } else {
                if (thickness != v.getLineWidth());
                return 0.0f;
            }
        }
        return thickness;
    }

    /**
     * Return the stroke color of the currently selected figures.  If
     * no figures are selected or the selected figures have different colors,
     * then return null.
     */
    public Paint getOutlinePaint() {
        Paint paint = null;
        Iterator iter = _m.getSelection();
        while (iter.hasNext()) {
            VersatileFigure v =(VersatileFigure) iter.next();
            if (paint == null) {
                paint = v.getStrokePaint();
            } else {
                if (paint != v.getStrokePaint())
                    return null;
            }
        }
        return paint;
    }

    /**
     * Return the icon
     */
    public XMLIcon getXMLIcon() {
        return _icon;
    }

    /**
     * Get the
     * current data object on the clipboard, copy of it,
     * and add it to the figure layer of the canvas.
     * If something goes wrong, beep.
     */
    public void paste(Clipboard clipboard) {
        // clear the current selection
        _m.clearSelection();

        Transferable t = clipboard.getContents(this);
        if (t == null) {
            getToolkit().beep();
            return;
        }

        try {
            Vector v = (Vector)t.getTransferData(dataFlavor);
            Enumeration enum = v.elements();
            if (enum.hasMoreElements()) {
                while (enum.hasMoreElements()) {
                    VersatileFigure vf = (VersatileFigure)enum.nextElement();
                    VersatileFigure vf2 = (VersatileFigure)vf.clone();
                    _layer.add(vf2);
                    if (vf2.getInteractor() instanceof SelectionInteractor) {
                        _selectionDragger.expandSelection
                            ((SelectionInteractor)vf2.getInteractor(), vf2);
                    }
                }
            } else {
                getToolkit().beep();
            }
        }
        catch(Exception ex) {
            getToolkit().beep();
        }
    }

    /**
     * Print this pane.
     */
    public void print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        _selectionDragger.clearSelection();
        if (job.printDialog()) {
            try {
                job.print();
            }
            catch(Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Printing failed:\n" + ex.toString(),
                        "Print Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Change the fill color of the selected figure(s).
     * If nothing it currently selected, then beep.
     * @param c The new color for the selected figure(s).
     */
    public void setFillPaint(Paint c) {
        Iterator iter = _m.getSelection();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                VersatileFigure v =(VersatileFigure) iter.next();
                v.setFillPaint(c);
            }
        } else {
            getToolkit().beep();
        }
    }

    /**
     * Change the thickness of the selected figure(s).
     * @param newThickness The new thickness for the selected figure(s).
     */
    public void setThickness(float newThickness) {
        _outlineThickness = newThickness;
        Iterator iter = _m.getSelection();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                VersatileFigure v = (VersatileFigure)iter.next();
                v.setLineWidth(newThickness);
            }
        } else {
            getToolkit().beep();
        }
    }

    /**
     * Change the outline color of the selected figure(s).
     * @param c The new color for the selected figure(s).
     */
    public void setOutlinePaint(Paint c) {
        Iterator iter = _m.getSelection();
        if (iter.hasNext()) {
            while (iter.hasNext()) {
                VersatileFigure v =(VersatileFigure) iter.next();
                v.setStrokePaint(c);
            }
        } else {
            getToolkit().beep();
        }
    }
}



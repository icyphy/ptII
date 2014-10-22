/* Listen for and handle events on Diva figures.

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
package ptolemy.domains.gr.lib;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import diva.canvas.AbstractFigure;
import diva.canvas.FigureLayer;
import diva.canvas.JCanvas;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.toolbox.BasicFigure;

///////////////////////////////////////////////////////////////////
//// FigureInteractor

/**
 Listen for and handle events on a Diva figure.  Because Diva figures
 are not derived from Java's component class, implementing a key
 listener directly would be problematic.  Instead, this class must be
 made aware of the ViewScreen2D object that contains the figure, as the
 ViewScreen2D object will forward all keyboard events that occur on a
 selected figure to this listener.  When initially adding a figure to
 the view screen, the view screen must call the setViewScreen() method
 of the figure with a reference to itself as the parameter.

 @author Ismael M. Sarmiento
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (ismael)
 @Pt.AcceptedRating Red (cxh)
 */
public class FigureInteractor extends AbstractInteractor implements KeyListener {
    /** Construct a FigureInteractor for the given figure.
     * @param figure The figure this interactor is to listen and respond to.
     */
    public FigureInteractor(AbstractFigure figure) {
        super();
        _figure = figure;
        _isSelected = false;
    }

    /** Return whether or not a figure has been selected in the
     *  viewscreen.  A figure is selected by a single mouse click on
     *  the figure, and deselected by a single mouse click anywhere
     *  outside the figure.
     *  @return A boolean <b>true</b> if the figure is selected, and
     *  <b>false</b> otherwise.
     */
    public boolean isSelected() {
        return _isSelected;
    }

    /** Translate a selected figure according to which arrow key is
     *  pressed.  This will respond only to the arrow keys outside of
     *  the number pad.
     *  @param e The KeyEvent received.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
            _figure.translate(0, -1);
            break;

        case KeyEvent.VK_DOWN:
            _figure.translate(0, 1);
            break;

        case KeyEvent.VK_LEFT:
            _figure.translate(-1, 0);
            break;

        case KeyEvent.VK_RIGHT:
            _figure.translate(1, 0);
            break;

        /*
         * case KeyEvent.VK_A: _figureActions.aPressed();
        break;
         * case KeyEvent.VK_B: _figureActions.bPressed();
        break;
         * case KeyEvent.VK_C: _figureActions.cPressed();
        break;
         */
        }
    }

    /** Included to comply with the KeyListener interface requirement.
     *  This method does nothing in its current implementation.
     *  @param e The KeyEvent received.
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /** Included to comply with the KeyListener interface requirement.
     *  This method does nothing in its current implementation.
     *  @param e The KeyEvent received.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /** Included to comply with the AbstractListener implementation
     *  requirement.  This method does nothing in its current
     *  implementation.
     *  @param layerEvent The LayerEvent received.
     */
    @Override
    public void mouseClicked(LayerEvent layerEvent) {
    }

    /** Translate the figure to wherever the mouse is dragged.
     *  @param layerEvent The LayerEvent received.
     */
    @Override
    public void mouseDragged(LayerEvent layerEvent) {
        _figure.translate(layerEvent.getLayerX() - dragPointX,
                layerEvent.getLayerY() - dragPointY);
        dragPointX = layerEvent.getLayerX();
        dragPointY = layerEvent.getLayerY();
    }

    /** Included to comply with the AbstractListener implementation
     *  requirement.  This method does nothing in its current
     *  implementation.
     * @param layerEvent The LayerEvent received.
     */
    @Override
    public void mouseEntered(LayerEvent layerEvent) {
    }

    /** Included to comply with the AbstractListener implementation
     *  requirement.  This method does nothing in its current
     *  implementation.
     *  @param layerEvent The LayerEvent received.
     */
    @Override
    public void mouseExited(LayerEvent layerEvent) {
    }

    /** Included to comply with the AbstractListener implementation
     *  requirement.  This method does nothing in its current
     *  implementation.
     *  @param layerEvent The LayerEvent received.
     */
    @Override
    public void mouseMoved(LayerEvent layerEvent) {
    }

    /** Update the state of this listener to reflect where on the
     *  figure the mouse button was pressed, and change the mouse
     *  cursor to show that the figure can now be dragged.
     *  @param layerEvent The LayerEvent received.
     */
    @Override
    public void mousePressed(LayerEvent layerEvent) {
        setSelected(true);
        dragPointX = layerEvent.getLayerX();
        dragPointY = layerEvent.getLayerY();
        _canvas = ((FigureLayer) _figure.getParent()).getCanvasPane()
                .getCanvas();
        _canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }

    /** Included to comply with the AbstractListener implementation
     *  requirement.  This method does nothing in its current
     *  implementation.
     *  @param layerEvent The LayerEvent received.
     */
    @Override
    public void mouseReleased(LayerEvent layerEvent) {
    }

    /** Set whether the figure being listened to is selected or not selected.
     *  @param selected <b>true</b> if the figure being listened to is
     *  selected, false otherwise.
     */
    public void setSelected(boolean selected) {
        _isSelected = selected;

        if (_isSelected) {
            if (_figure instanceof BasicFigure) {
                ((BasicFigure) _figure).setLineWidth(4);
            }

            _viewScreen.setSelectedFigure(_figure);
        } else {
            if (_figure instanceof BasicFigure) {
                ((BasicFigure) _figure).setLineWidth(1);
            }

            _viewScreen.setSelectedFigure(null);
        }
    }

    /** Notify this object of the view screen which contains the
     *  figure this object is listening to.
     *  @param viewScreen The viewScreen containing the figure this
     *  interactor is listening to.
     */
    public void setViewScreen(ViewScreen2D viewScreen) {
        _viewScreen = viewScreen;
        System.out.println("Viewscreen set for" + _figure);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    //The canvas containing the figure this interactor is listening to.
    private JCanvas _canvas;

    //The x-coordinate of the last point the figure was dragged to.
    private double dragPointX;

    //The y-coordinate of the last point the figure was dragged to.
    private double dragPointY;

    //The figure this object is listening to.
    private AbstractFigure _figure;

    //The state of the figure this objected is listening to.
    private boolean _isSelected;

    //The view screen containing the figure.
    private ViewScreen2D _viewScreen;

    //The set of actions the user can perform by interacting with a figure.
    //private FigureActions _figureActions;
}

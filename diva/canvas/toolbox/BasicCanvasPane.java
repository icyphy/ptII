/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import java.util.ArrayList;
import java.util.Iterator;

import diva.canvas.CanvasLayer;
import diva.canvas.CanvasPane;
import diva.canvas.FigureLayer;

/** A basic implementation of a canvas pane, provided for simple
 * applications. This class keeps a linear list of canvas layers, and
 * provides a couple of methods so that layers can be added to
 * it. Real applications will probably want to create their own
 * CanvasPane subclass, instead of using this class.
 *
 * @version        $Revision$
 * @author John Reekie
*/
public class BasicCanvasPane extends CanvasPane {

    /** The array of layers
     */
    ArrayList _layers = new ArrayList();

    /** Create a new canvas pane with a single figure layer
     * at index zero. This is a convenience constructor for
     * test suites and simple demos -- in general, an application
     * will want to construct a more sophisticated set of layers.
     */
    public BasicCanvasPane () {
        super();
        addLayer(new FigureLayer());
    }

    /** Create a new canvas pane with the given layer at index zero.
     */
    public BasicCanvasPane (CanvasLayer layer) {
        super();
        addLayer(layer);
    }

    /** Add the given layer to the list of layers.   The new
     * layer will be drawn over the top of any existing layers.
     */
    public void addLayer (CanvasLayer layer) {
        _initNewLayer(layer);
        _layers.add(layer);
    }

    /** Insert the given layer into the list of layers at the
     * given index. Lower indexes are drawn above higher indexes.
     */
    public void addLayer (int index, CanvasLayer layer) {
        _initNewLayer(layer);
        _layers.add(index, layer);
    }

    /** Get the layer at the given index.
     */
    public FigureLayer getLayer (int index) {
        return (FigureLayer) _layers.get(index);
    }

    /** Get the index of the given layer, or -1 if it is not
     * in this pane.
     */
    public int indexOf (CanvasLayer layer) {
        return _layers.indexOf(layer);
    }

    /** Return an iteration of the layers, in event-processing order (that is,
     * from front to back).
     */
    public Iterator layersFromFront () {
        return _layers.iterator();
    }

    /** Return an iteration of the layers, in redraw order (that is,
     * from back to front).
     */
    public Iterator layersFromBack () {
        return new Iterator() {
                int cursor = _layers.size();
                public boolean hasNext() {
                    return cursor > 0;
                }
                public Object next() {
                    cursor--;
                    return _layers.get(cursor);
                }
                public void remove() {
                    throw new UnsupportedOperationException(
                            "Cannot delete layer from canvas pane");
                }
            };
    }

    /** Remove the given layer. Do nothing if the layer is
     * not in this pane.
     */
    public void removeLayer (CanvasLayer layer) {
        _layers.remove(layer);
    }
}



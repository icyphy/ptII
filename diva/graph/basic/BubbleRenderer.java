/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.basic;

import java.awt.Color;
import java.awt.Paint;

import diva.canvas.Figure;
import diva.canvas.toolbox.LabelWrapper;
import diva.graph.NodeRenderer;
import diva.graph.toolbox.StateBubble;

/**
 * A factory which creates and returns a bubble given a node input
 * to render.
 *
 * @author  Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author  John Reekie  (johnr@eecs.berkeley.edu)
 * @version $Revision$
 * @rating  Red
 */
public class BubbleRenderer implements NodeRenderer {

    /**
     * The fill paint
     */
    private Paint _fillPaint = Color.white;

    /**
     * The stroke paint
     */
    private Paint _strokePaint = Color.black;

    /**
     * The size of nodes
     */
    private double _size = 100.0;

    /**
     * Create a renderer which renders bubbles white
     */
    public BubbleRenderer () {
        ;
    }

    /**
     * Create a renderer which renders bubbles in the given fill paint,
     * outlie paint, and size.
     */
    public BubbleRenderer (Paint fillPaint, Paint strokePaint,
            double size) {
        _fillPaint = fillPaint;
        _strokePaint = strokePaint;
        _size = size;
    }

    /** Get the fill paint pattern of this figure.
     */
    public Paint getFillPaint () {
        return _fillPaint;
    }

    /** Get the stroke paint pattern of this figure.
     */
    public Paint getStrokePaint () {
        return _strokePaint;
    }

    /**
     * Return the rendered visual representation of this node.
     */
    public Figure render(Object n) {
        StateBubble e = new StateBubble(0, 0, _size, _size);
        e.setFillPaint(_fillPaint);
        e.setStrokePaint(_strokePaint);

        // Set the state appearance
        //Object s = n.getProperty("stateType");
        int type = StateBubble.NORMAL_STATE;
        //if (s != null) {
        //    type = ((Integer) s).intValue();
        //}
        e.setStateType(type);

        // Set the label
        Object p = "node";//n.getProperty("label");
        String label = p == null ? "Unnamed" : (String) p;
        LabelWrapper w = new LabelWrapper(e, label);
        return w;
    }

    /** Set the fill paint pattern of this figure. The figure will be
     *  filled with this paint pattern. If no pattern is given, do not
     *  fill it.
     */
    public void setFillPaint (Paint p) {
        _fillPaint = p;
    }

    /** Set the stroke paint pattern of this figure.
     */
    public void setStrokePaint (Paint p) {
        _strokePaint = p;
    }
}



/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
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
 * @author  Michael Shilman
 * @author  John Reekie
 * @version $Id$
 * @Pt.AcceptedRating  Red
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
    public BubbleRenderer() {
        ;
    }

    /**
     * Create a renderer which renders bubbles in the given fill paint,
     * outlie paint, and size.
     */
    public BubbleRenderer(Paint fillPaint, Paint strokePaint, double size) {
        _fillPaint = fillPaint;
        _strokePaint = strokePaint;
        _size = size;
    }

    /** Get the fill paint pattern of this figure.
     */
    public Paint getFillPaint() {
        return _fillPaint;
    }

    /** Get the stroke paint pattern of this figure.
     */
    public Paint getStrokePaint() {
        return _strokePaint;
    }

    /**
     * Return the rendered visual representation of this node.
     */
    @Override
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
        Object p = "node"; //n.getProperty("label");
        String label = (String) p;
        LabelWrapper w = new LabelWrapper(e, label);
        return w;
    }

    /** Set the fill paint pattern of this figure. The figure will be
     *  filled with this paint pattern. If no pattern is given, do not
     *  fill it.
     */
    public void setFillPaint(Paint p) {
        _fillPaint = p;
    }

    /** Set the stroke paint pattern of this figure.
     */
    public void setStrokePaint(Paint p) {
        _strokePaint = p;
    }
}

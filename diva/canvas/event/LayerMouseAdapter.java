/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.event;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * An object which wraps a mouse listener and
 * makes it compatible with the Diva canvas,
 * sending it mouse events in the local coordinate
 * system.  XXX haven't translated it yet.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 */
public class LayerMouseAdapter implements LayerListener, LayerMotionListener {
    MouseFilter _filter;
    MouseListener _ml;
    MouseMotionListener _mml;

    public LayerMouseAdapter(MouseListener ml, MouseMotionListener mml) {
        _ml = ml;
        _mml = mml;
        _filter = MouseFilter.defaultFilter;
    }

    public MouseFilter getMouseFilter() {
        return _filter;
    }

    public void mouseClicked(LayerEvent e) {
        if(_filter.accept(e)) {
            _ml.mouseClicked(e);
        }
    }

    public void mousePressed(LayerEvent e) {
        if(_filter.accept(e)) {
            _ml.mousePressed(e);
        }
    }

    public void mouseDragged(LayerEvent e) {
        if(_filter.accept(e)) {
            _mml.mouseDragged(e);
        }
    }

    public void mouseMoved(LayerEvent e) {
        if(_filter.accept(e)) {
            _mml.mouseMoved(e);
        }
    }

    private void debug(String s) {
        System.out.println(s);
    }

    public void mouseReleased(LayerEvent e) {
        if(_filter.accept(e)) {
            _ml.mouseReleased(e);
        }
    }

    public void mouseEntered(LayerEvent e) {
        //  _mml.mouseEntered(e);
    }

    public void mouseExited(LayerEvent e) {
        //  _mml.mouseExited(e);
    }

    public void setMouseFilter(MouseFilter f) {
        _filter = f;
    }
}



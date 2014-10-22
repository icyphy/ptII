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
package diva.canvas.event;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * An object which wraps a mouse listener and
 * makes it compatible with the Diva canvas,
 * sending it mouse events in the local coordinate
 * system.  XXX haven't translated it yet.
 *
 * @author Michael Shilman
 * @version $Id$
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

    @Override
    public void mouseClicked(LayerEvent e) {
        if (_filter.accept(e)) {
            _ml.mouseClicked(e);
        }
    }

    @Override
    public void mousePressed(LayerEvent e) {
        if (_filter.accept(e)) {
            _ml.mousePressed(e);
        }
    }

    @Override
    public void mouseDragged(LayerEvent e) {
        if (_filter.accept(e)) {
            _mml.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(LayerEvent e) {
        if (_filter.accept(e)) {
            _mml.mouseMoved(e);
        }
    }

    //private void debug(String s) {
    //    System.out.println(s);
    //}

    @Override
    public void mouseReleased(LayerEvent e) {
        if (_filter.accept(e)) {
            _ml.mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(LayerEvent e) {
        //  _mml.mouseEntered(e);
    }

    @Override
    public void mouseExited(LayerEvent e) {
        //  _mml.mouseExited(e);
    }

    public void setMouseFilter(MouseFilter f) {
        _filter = f;
    }
}

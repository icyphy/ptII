/*
@Copyright (c) 1998-1999 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
*/
package ptolemy.domains.pn.demo.RunLength;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.media.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

//////////////////////////////////////////////////////////////////////////
//// ImageDisplay
/**
@author Steve Neuendorffer, Mudit Goel
@version $Id$
*/

public final class ImageDisplay extends AtomicActor {
    public ImageDisplay(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {
        super(container,name);
        _port_image = new IOPort(this, "image", true, false);
	new Parameter(this, "FrameName", new StringToken("ImageDisplay"));
    }

    //     public ImageDisplay(CompositeActor container, String name, Picture pan) 
    //             throws IllegalActionException, NameDuplicationException {
    //         super(container,name);
    //         _port_image = new IOPort(this, "image", true, false);
    //         _panel = pan;
    //     }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _frame = null;
        _oldxsize = 0;
        _oldysize = 0;
	StringToken name = 
	    (StringToken)((Parameter)getAttribute("FrameName")).getToken();
	_framename = name.stringValue();
	if(_panel == null) {
            _frame = new _PictureFrame(_framename);
            _panel = _frame.getPanel();
        } else {
            _frame = null;
        }
    }

    public void fire() throws IllegalActionException {
        IntMatrixToken message = (IntMatrixToken)_port_image.get(0);
        int frame[][] = message.intMatrix();
        int xsize = message.getColumnCount();
        int ysize = message.getRowCount();

        if((_oldxsize != xsize) || (_oldysize != ysize)) {
            _oldxsize = xsize;
            _oldysize = ysize;
            _RGBbuffer = new int[xsize*ysize];

            if(_panel == null) {
                System.out.println("panel disappeared!");
                _frame = new _PictureFrame(_framename);
		_frame.addWindowListener(new _PictureFrameListener(_frame));
                //_frame._picture.setImage(_RGBbuffer);
                _panel = _frame.getPanel();
            } else {
                _frame = null;
            }
            if(_picture != null)
                _panel.remove(_picture);
            _panel.setSize(xsize, ysize);
            _picture = new Picture(xsize, ysize);
            _picture.setImage(_RGBbuffer);
            _panel.add("Center", _picture);
            _panel.validate();

            Container c = _panel.getParent();
            while(c.getParent() != null) {
                c = c.getParent();
            }
            if(c instanceof Window) {
                ((Window) c).pack();
            } else {
                c.validate();
            }

            _panel.invalidate();
            _panel.repaint();

            if(_frame != null) {
                _frame.pack();
            }

            //System.out.println("new buffer");
	}

	// convert the B/W image to a packed RGB image
	int i, j, index = 0;
	for(j = 0; j<ysize; j++) {
	    for(i = 0; i < xsize; i++, index++) {
		int tem = 0;
		if (frame[j][i] == 0) tem = 255;
		else tem = 0;
		_RGBbuffer[index] = (255 << 24) |
		    ((tem & 255) << 16) | 
		    ((tem & 255) << 8) | 
		    (tem & 255);
	    }  
	}
	_picture.displayImage();
	_picture.repaint();
    }

    public void setPanel(Panel panel) {
        _panel = panel;
    }

    private class _PictureFrame extends Frame {
        public _PictureFrame(String title) {
            super(title);
            this.setLayout(new BorderLayout(15, 15));
            this.show();
            _panel = new Panel();
            this.add("Center", _panel);
            this.pack();
            this.validate();
        }
        public Panel getPanel() {
            return _panel;
        }
        private Panel _panel;
    }

    private class _PictureFrameListener implements WindowListener {

	public _PictureFrameListener(Window window) {
	    _window = window;
	}

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {
	    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
		_window.dispose();
	    }
	}
	
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	private Window _window;
    }

    //private Picture _panel;
    private Picture _picture;
    private _PictureFrame _frame;
    private Panel _panel;
    private IOPort _port_image;
    private int _oldxsize, _oldysize;
    //private Image _image;
    private int _RGBbuffer[] = null;
    private String _framename;

}



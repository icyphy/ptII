/*
@Copyright (c) 1998 The Regents of the University of California.
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
package ptolemy.domains.sdf.lib.vq;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import java.io.*;
import ptolemy.actor.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

//////////////////////////////////////////////////////////////////////////
//// ImageDisplay
/**
@author Steve Neuendorffer
@version $Id$
*/

public final class ImageDisplay extends SDFAtomicActor {
    public ImageDisplay(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
        IOPort inputport = (IOPort) newPort("image");
        inputport.setInput(true);
        setTokenConsumptionRate(inputport, 1);
    }

    public void initialize() throws IllegalActionException {
        _frame = null;
        _port_image = (IOPort) getPort("image");
        _oldxsize = 0;
        _oldysize = 0;
    }

    public void wrapup() throws IllegalActionException {
        //      _frame.setVisible(false);
        _frame.dispose();
    }

    public void fire() throws IllegalActionException {
        IntMatrixToken message = (IntMatrixToken)
            _port_image.get(0);
        int frame[][] = message.intMatrix();
        int xsize = message.getColumnCount();
        int ysize = message.getRowCount();

        if((_oldxsize != xsize) || (_oldysize != ysize)) {
            _oldxsize = xsize;
            _oldysize = ysize;
            _RGBbuffer = new int[xsize*ysize];
            _imagesource = new MemoryImageSource(xsize, ysize, _RGBbuffer, 0, xsize);
            _imagesource.setAnimated(true);
            if(_frame != null) {
                _frame.dispose();
            }
            _frame = new _InputFrame("ImageDisplay", xsize, ysize);            
            _image = _frame._panel.createImage(_imagesource);

            System.out.println("new buffer");
        }     

        //        System.out.println("xsize = "+xsize);
        // System.out.println("ysize = "+ysize);
       
        // convert the B/W image to a packed RGB image
        int i, j, index = 0;
        for(j = 0; j < ysize; j++) {
            for(i = 0; i < xsize; i++, index++) 
                _RGBbuffer[index] = (255 << 24) |
                    ((frame[j][i] & 255) << 16) | 
                    ((frame[j][i] & 255) << 8) | 
                    (frame[j][i] & 255);
             }  

        //        Image img = _frame._panel.createImage(
         //       new MemoryImageSource(xsize, ysize, _RGBbuffer, 0, xsize));
        //        if(s == null) 
        
        _imagesource.newPixels();
        _frame._panel.BLTImage(_image);
        
        //      System.out.println("image drawn");
    }

    private class _InputFrame extends Frame {
        public _InputFrame(String title, int xsize, int ysize) {
            super(title);
            this.setLayout(new BorderLayout(15, 15));
            _panel = new _VideoPanel(xsize, ysize);
            this.setVisible(true);
            add("Center",_panel);
            this.pack();

        }
        private _VideoPanel _panel;        
     
    }

    private class _VideoPanel extends Canvas {
        public _VideoPanel(int width, int height) {
            super(); 
            _buffer = createImage(width, height);
            this.setSize(width, height);
            this.validate();
            
        }
        
        public void paint(Graphics graphics) {
            if(_buffer == null) return;
            graphics.drawImage(_buffer, 0, 0, null);
        }

        public void update(Graphics g) {
            paint(g);
        }

        public void BLTImage(Image im) {
            _buffer = im;
            prepareImage(im,null);
            Graphics g = getGraphics();
            paint(g);
            g.dispose();

        }
        
        private Image _buffer;
    }
    private _InputFrame _frame;
    private IOPort _port_image;
    private int _oldxsize, _oldysize;
    private Image _image;
    private MemoryImageSource _imagesource;
    private int _RGBbuffer[] = null;

}

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

public class ImageDisplay extends SDFAtomicActor {
    public ImageDisplay(CompositeActor container, String name) 
            throws IllegalActionException, NameDuplicationException {

        super(container,name);
        IOPort inputport = (IOPort) newPort("image");
        inputport.setInput(true);
        setTokenProductionRate(inputport, 1);
    }

    public void initialize() throws IllegalActionException {
        _frame = new _InputFrame("ImageDisplay");
    }

    public void wrapup() throws IllegalActionException {
        _frame.setVisible(false);
    }

    public void fire() throws IllegalActionException {
        IntMatrixToken message = (IntMatrixToken)
            ((IOPort) getPort("image")).get(0);
        int frame[][] = message.intMatrix();
        int xsize = message.getColumnCount();
        int ysize = message.getRowCount();
        
        // conver the B/W image to a packed RGB image
        int RGBbuffer[] = new int[xsize*ysize];
        int i, j, index = 0;
        for(j = 0; j < ysize; j++)
            for(i = 0; i < xsize; i++, index++)
                RGBbuffer[index] = 
                    frame[j][i]>>16 + frame[j][i]>>8 + frame[j][i];        
        Image img = _frame._panel.createImage(
                new MemoryImageSource(xsize, ysize, RGBbuffer, 0, xsize));
        _frame._graphic.drawImage(img, 0, 0, null);
    }

    private class _InputFrame extends Frame {
        public _InputFrame(String title) {
            super(title);
            this.setLayout(new BorderLayout(15, 15));
            this.setSize(100,100);
            _panel = new Panel();
            _graphic = _panel.getGraphics();
            add("Center",_panel);
            this.setVisible(true);

        }

        private Panel _panel;        
        private Graphics _graphic;
    }
    _InputFrame _frame;

}

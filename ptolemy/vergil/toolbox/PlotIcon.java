/*  Icon for a plotter

 Copyright (c) 2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.moml.*;
import ptolemy.actor.lib.gui.Plotter;
import ptolemy.plot.Plot;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import diva.canvas.*;
import diva.canvas.toolbox.*;
import diva.gui.toolbox.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.Image;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Timer;

//////////////////////////////////////////////////////////////////////////
//// PlotIcon
/** Icon for a plotter
 * @author Paul Whitaker
 * @version $Id$
 */
public class PlotIcon extends EditorIcon {

    private double _iconScale = 0.5;
//  private JLabel _label;
//  private JPanel _panel;
    private Figure _figure;

    public PlotIcon(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        super(container, "_icon");
    }

    public PlotIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        /*
                 Timer _timer = new Timer(3000, new ActionListener() {
                     public void actionPerformed(ActionEvent evt) {
                         _figure = createBackgroundFigure();
// Now how should this figure actually be updated?  Override EditorIcon
// methods?
                     }
                 });

        _timer.start();
        */
    }

    public Figure createBackgroundFigure() {
//      _panel=new JPanel();
        try {
//               _label = new JLabel("Some label");
//               ((Plotter)getContainer()).place(_panel);
//               _figure = new SwingWrapper(_panel);
//               _figure = new SwingWrapper(((Plotter)getContainer()).plot);
//               _figure = new SwingWrapper(_label);

// The code below throws a NullPointerException because the plot does
// not yet exist.

// How do we know when we need to update the image?

              // For now, use a timer.
              // Eventually, implement a listener for plot changes.

            Plotter _plotter = (Plotter) getContainer();
            Plot _plot = _plotter.plot;

            if (_plot == null) {
                throw(new NullPointerException());
            }
            else {
                Rectangle _rect = _plot.getBounds();
                int x = (int) (_rect.getWidth());
                int y = (int) (_rect.getHeight());
                int xNew = (int) (x * _iconScale);
                int yNew = (int) (y * _iconScale);
                if ((x <= 0)||(y <= 0)) {
                    x = 60;
                    y = 40;
                    xNew = 60;
                    yNew = 40;
                }
                BufferedImage originalImage = new
                    BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
                Graphics2D graphics = originalImage.createGraphics();
                graphics.setBackground(new Color(0,0,0,0));
                graphics.clearRect(0, 0, x, y);
                _plot.paint(graphics);
                _figure = new ImageFigure(originalImage);
                /*
                _figure = new ImageFigure(originalImage.getScaledInstance(xNew,
                        yNew, java.awt.Image.SCALE_FAST));
                        */   
                return _figure;
            }
        }
        catch (Exception ex) {
            //            ex.printStackTrace();
            return super.createBackgroundFigure();
        }
    }   
}


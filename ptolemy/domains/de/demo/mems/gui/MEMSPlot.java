/* MEMSPlot

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.demo.mems.gui;


import ptolemy.plot.*;
import ptolemy.domains.de.demo.mems.lib.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Math;

//////////////////////////////////////////////////////////////////////////
//// Entity
/**

@author Allen Miu
@version $Id$
*/

public class MEMSPlot extends PlotFrame {

    private CoordPair[] buffer;
    int bufCount;
    
    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    
    public MEMSPlot(int yRange, int xRange) {
        super("MEMSPlot", new Plot());
    
        // Handle window closing by exiting the application.;
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Strangely, calling _close() here sends javac into
                // an infinite loop (in jdk 1.1.4).
                //              _close();
                System.exit(0);
            }
        });

        buffer = new CoordPair[256];
        bufCount = 0;

        ((Plot)plot).setTitle("MEMS Simulation");
        ((Plot)plot).setYRange(0, yRange);
        ((Plot)plot).setXRange(0, xRange);
    
        ((Plot)plot).setXLabel("x-coordinate");
        ((Plot)plot).setYLabel("y-corrdinate");
        	    
        ((Plot)plot).setMarksStyle("dots");
        ((Plot)plot).setVisible(true);
    }
    /** Add a point on the plot object.
     *  Points on the graph represent nodes
     */
    public void addPoint(double x, double y) {
        ((Plot)plot).addPoint(0, x, y, false);
        
    }

    //---------------------------------------------------------
    // connect two points with specified color
    // the line will show for seconds and disappear
    //---------------------------------------------------------
    public synchronized void connect(Coord c1, Coord c2, int color) {
        /*
          plot.addPoint(color,
          c1.getX(),
          c1.getY(), 
          false);
          plot.addPoint(color,
          c2.getX(),
          c2.getY(),
          true);
    
          try {
          wait(2000);
          } catch (InterruptedException e) {}
    
          plot.erasePoint(color,0);
          plot.erasePoint(color,0);
          plot.update(plot.getGraphics());
        */

        buffer[bufCount] = new CoordPair(c1,c2,color);
        bufCount++;

        if (bufCount > 255) {
            System.out.println("MEMSPlot:  Ran out of buffer space.");
            System.exit(1);
        }

    }

    public synchronized void flush() {
        if (bufCount > 0) {
            int counter;
      
            for (counter = 0; counter < bufCount; counter++) {
                ((Plot)plot).addPoint(buffer[counter].color,
                        buffer[counter].one.getX(),
                        buffer[counter].one.getY(), 
                        false);
                ((Plot)plot).addPoint(buffer[counter].color,
                        buffer[counter].two.getX(),
                        buffer[counter].two.getY(),
                        true);
            }
      
            try {
                wait(2000);
            } catch (InterruptedException e) {}
      
            for (counter = 0; counter < bufCount; counter++) {
                ((Plot)plot).erasePoint(buffer[bufCount-counter-1].color,0);
                ((Plot)plot).erasePoint(buffer[bufCount-counter-1].color,0);
            }
            ((Plot)plot).update(((Plot)plot).getGraphics());
      
            bufCount = 0;
        }

    }

    //---------------------------------------------------------
    // return native plot object
    //    
    //---------------------------------------------------------
    public Plot plotObj() {
        return (Plot)plot;
    
    }

}

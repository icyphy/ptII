package ptolemy.domains.de.demo.mems.gui;


import ptolemy.plot.*;
import ptolemy.domains.de.demo.mems.lib.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.Math;


public class MEMSPlot extends PlotFrame {

  private CoordPair[] buffer;
  int bufCount;

  /** Construct a plot with the specified command-line arguments.
   *  @param args The command-line arguments.
   */
  
  public MEMSPlot(int yRange, int xRange) {
    super();
    
    // Handle window closing by exiting the application.
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

    plot.setTitle("MEMS Simulation");
    plot.setYRange(0, yRange);
    plot.setXRange(0, xRange);
    
    plot.setXLabel("x-coordinate");
    plot.setYLabel("y-corrdinate");
        	    
    plot.setMarksStyle("dots");
    plot.setVisible(true);
  }
  //---------------------------------------------------------
  //  Add a point on the graph. 
  //  points on the graph represent nodes
  //--------------------------------------------------------- 
  public void addPoint(double x, double y) {
    plot.addPoint(0, x, y, false);

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
	plot.addPoint(buffer[counter].color,
		      buffer[counter].one.getX(),
		      buffer[counter].one.getY(), 
		      false);
	plot.addPoint(buffer[counter].color,
		      buffer[counter].two.getX(),
		      buffer[counter].two.getY(),
		      true);
      }
      
      try {
	wait(2000);
      } catch (InterruptedException e) {}
      
      for (counter = 0; counter < bufCount; counter++) {
	plot.erasePoint(buffer[bufCount-counter-1].color,0);
	plot.erasePoint(buffer[bufCount-counter-1].color,0);
      }
      plot.update(plot.getGraphics());
      
      bufCount = 0;
    }

  }

  //---------------------------------------------------------
  // return native plot object
  //    
  //---------------------------------------------------------
  public Plot plotObj() {
    return plot;
    
  }

}



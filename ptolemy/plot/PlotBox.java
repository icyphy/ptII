/* A labeled box for signal plots.

@Author: Edward A. Lee and Christopher Hylands

@Contributors:  William Wu

@Version: $Id$

@Copyright (c) 1997 The Regents of the University of California.
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

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY
*/
package plot;

import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

//////////////////////////////////////////////////////////////////////////
//// PlotBox
/** 
 * Construct a labeled box within which to place a data plot.  A title,
 * X and Y axis labels, tick marks, and a legend are all supported.
 * The box can be configured either through a file with commands or
 * through direct invocation of the public methods of the class.
 * If a file is used, the file can be given as a URL through the
 * applet parameter called "dataurl". The file contains any number
 * commands, one per line.  Unrecognized commands and commands with
 * syntax errors are ignored.  Comments are denoted by a line starting
 * with a pound sign "#".  The recognized commands include:
 * <pre>
 * TitleText: <i>string</i>
 * XLabel: <i>string</i>
 * YLabel: <i>string</i>
 * </pre>
 * These commands provide a title and labels for the X (horizontal) and Y
 * (vertical) axes.
 * A <i>string</i> is simply a sequence of characters, possibly
 * including spaces.  There is no need here to surround them with
 * quotation marks, and in fact, if you do, the quotation marks will
 * be included in the labels.
 * <p>
 * The ranges of the X and Y axes can be optionally given by commands like:
 * <pre>
 * XRange: <i>min</i>, <i>max</i>
 * YRange: <i>min</i>, <i>max</i>
 * </pre>
 * The arguments <i>min</i> and <i>max</i> are numbers, possibly
 * including a sign and a decimal point. If they are not specified,
 * then the ranges are computed automatically from the data.
 * <p>
 * The tick marks for the axes are usually computed automatically from
 * the ranges.  Every attempt is made to choose reasonable positions
 * for the tick marks regardless of the data ranges (i.e. powers of
 * ten multiplied by 1, 2, or 5 are used).  However, they can also be
 * specified explicitly using commands like:
 * <pre>
 * XTicks: <i>label position, label position, ...</i>
 * YTicks: <i>label position, label position, ...</i>
 * </pre>
 * A <i>label</i> is a string that must be surrounded by quotation
 * marks if it contains any spaces.  A <i>position</i> is a number
 * giving the location of the tick mark along the axis.  For example,
 * a horizontal axis for a frequency domain plot might have tick marks
 * as follows:
 * <pre>
 * XTicks: -PI -3.14159, -PI/2 -1.570795, 0 0, PI/2 1.570795, PI 3.14159
 * </pre>
 * Tick marks could also denote years, months, days of the week, etc.
 * <p>
 * By default, tick marks are connected by a light grey background grid.
 * This grid can be turned off with the following command:
 * <pre>
 * Grid: off
 * </pre>
 * It can be turned back on with
  * <pre>
 * Grid: on
 * </pre>
 * Also, by default, the first ten data sets are shown each in a unique color.
 * The use of color can be turned off with the command:
 * <pre>
 * Color: off
 * </pre>
 * It can be turned back on with
 * <pre>
 * Color: on
 * </pre>
 * All of the above commands can also be invoked directly by calling the
 * the corresponding public methods from some Java procedure.
 *
 * @author Edward A. Lee
 * @version $Id$
 */
public class PlotBox extends Applet {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** 
     * Handle button presses to fill the plot.  This rescales so that
     * the data that is currently plotted just fits.
     */
    public boolean action (Event evt, Object arg) {
        if (evt.target == _fillButton) {
            fillPlot();
            return true;
        } else {
            return super.action (evt, arg);
        }
    }

    /** 
     * Add a legend (displayed at the upper right) for the specified
     * data set with the specified string.  Short strings generally
     * fit better than long strings.
     */
    public void addLegend(int dataset, String legend) {
        _legendStrings.addElement(legend);
        _legendDatasets.addElement(new Integer(dataset));
    }
    
    /** 
     * Specify a tick mark for the X axis.  The label given is placed
     * on the axis at the position given by <i>position</i>. If this
     * is called once or more, automatic generation of tick marks is
     * disabled.  The tick mark will appear only if it is within the X
     * range.
     */
    public void addXTick (String label, double position) {
        if (_xticks == null) {
            _xticks = new Vector();
            _xticklabels = new Vector();
        }
       	_xticks.addElement(new Double(position));
        _xticklabels.addElement(label);
    }
    
    /** 
     * Specify a tick mark for the Y axis.  The label given is placed
     * on the axis at the position given by <i>position</i>. If this
     * is called once or more, automatic generation of tick marks is
     * disabled.  The tick mark will appear only if it is within the Y
     * range.
     */
    public void addYTick (String label, double position) {
       	if (_yticks == null) {
            _yticks = new Vector();
     	    _yticklabels = new Vector();
        }
       	_yticks.addElement(new Double(position));
        _yticklabels.addElement(label);
    }
    
    /**
      * Draw the axes using the current range, label, and title information.
      * If the argument is true, clear the display before redrawing.
      */
    public synchronized void drawPlot(boolean clearfirst) {
	if (graphics == null) {
	    System.out.println("Attempt to draw axes without a Graphics object.");
	    return;
	    }
	    
	// Give other threads a chance, so that hopefully things are
	// up to date.
	Thread.yield();
	    
        // Find the width and height of the total drawing area, and clear it.
        Rectangle drawRect = bounds();
        graphics.setPaintMode();
        if (clearfirst) {
            graphics.clearRect(drawRect.x, drawRect.y,
			       drawRect.width, drawRect.height);
        }
        
	// For use by all text displays below.
	// FIXME - consolidate for efficiency.
        graphics.setFont(_titlefont);
        FontMetrics tfm = graphics.getFontMetrics();
        graphics.setFont(_superscriptfont);
        FontMetrics sfm = graphics.getFontMetrics();
        graphics.setFont(_labelfont);
        FontMetrics lfm = graphics.getFontMetrics();

	// If an error message has been set, display it and return.
        if (_errorMsg != null) {
            int fheight = lfm.getHeight() + 2;
            int msgy = fheight;
            graphics.setColor(Color.black);
            for(int i=0; i<_errorMsg.length;i++) {
                graphics.drawString(_errorMsg[i],10, msgy);
                msgy += fheight;
            }
            return;
         }

         // Make sure we have an x and y range
         if (!xRangeGiven) {
             if (xBottom > xTop) {
                 // have nothing to go on.
                 _setXRange(0,0);
             } else {
                 _setXRange(xBottom, xTop);
             }
         }
         if (!yRangeGiven) {
             if (yBottom > yTop) {
                 // have nothing to go on.
                 _setYRange(0,0);
             } else {
                 _setYRange(yBottom, yTop);
             }
         }
         
         // Vertical space for title, if appropriate.
         // NOTE: We assume a one-line title.
         int titley = 0;
         int titlefontheight = tfm.getHeight();
         if (_title != null || _yExp != 0) {
             titley = titlefontheight + topPadding;
         }
        
        // Number of vertical tick marks depends on the height of the font
        // for labeling ticks and the height of the window.
        graphics.setFont(_labelfont);
        int labelheight = lfm.getHeight();
        int halflabelheight = labelheight/2;

        // Draw scaling annotation for x axis.
        // NOTE: 5 pixel padding on bottom.
        int ySPos = drawRect.y + drawRect.height - 5; 
        if (_xExp != 0 && _xticks == null) {
            int xSPos = drawRect.x + drawRect.width - rightPadding;
            String superscript = Integer.toString(_xExp);
            xSPos -= sfm.stringWidth(superscript);
            graphics.setFont(_superscriptfont);
            graphics.drawString(superscript, xSPos, ySPos - halflabelheight);
            xSPos -= lfm.stringWidth("x10");
            graphics.setFont(_labelfont);
            graphics.drawString("x10", xSPos, ySPos);
            // NOTE: 5 pixel padding on bottom
            bottomPadding = (3 * labelheight)/2 + 5;
        }
        
        // NOTE: 5 pixel padding on the bottom.
        if (_xlabel != null && bottomPadding < labelheight + 5) {
            bottomPadding = titlefontheight + 5;
        }
        
        // Compute the space needed around the plot, starting with vertical.
        // NOTE: padding of 5 pixels below title.
        uly = drawRect.y + titley + 5;
        // NOTE: 3 pixels above bottom labels.
        lry = drawRect.height-labelheight-bottomPadding-3; 
        int height = lry-uly;
        yscale = height/(yMax - yMin);
        _yscale = height/(_yMax - _yMin);

        ///////////////////// vertical axis

        // Number of y tick marks.
        // NOTE: subjective spacing factor.
        int ny = 2 + height/(labelheight+10);
        // Compute y increment.
        double yStep=_roundUp((_yMax-_yMin)/(double)ny);
        
        // Compute y starting point so it is a multiple of yStep.
        double yStart=yStep*Math.ceil(_yMin/yStep);
        
        // NOTE: Following disables first tick.  Not a good idea?
        // if (yStart == _yMin) yStart+=yStep;
        
        // Define the strings that will label the y axis.
        // Meanwhile, find the width of the widest label.
        // The labels are quantized so that they don't have excess resolution.
        int widesty = 0;
        // These do not get used unless ticks are automatic, but the
        // compiler is not smart enough to allow us to reference them
        // in two distinct conditional clauses unless they are
        // allocated outside the clauses.
        String ylabels[] = new String[ny];
        int ylabwidth[] = new int[ny];
        int ind = 0;
        if (_yticks == null) {
            // automatic ticks
            for (double ypos=yStart; ypos <= _yMax; ypos += yStep) {
                // Prevent out of bounds exceptions
                if (ind >= ny) break;
                // NOTE: The following clever solution doesn't always work:
                String yfull = Double.toString(Math.floor(ypos*1000.0+0.5)*0.001);
                // ... so we have to patch up the solution...
                // This method just copies digits up to the third
                // after the decimal point.  However, for numbers near
                // zero, if the above yields something in scientific
                // notation, this fails too.  We really need printf!!
                // FIXME: add printf when java people finally realize
                // they have to have it.
                int point = yfull.indexOf('.');
                if (point < 0) {
                    ylabels[ind] = yfull;
                } else {
                    int endpoint = point+4;
                    if (endpoint < yfull.length()) {
                        ylabels[ind] = yfull.substring(0,endpoint);
                    } else {
                        ylabels[ind] = yfull;
                    }
                }
                String yl = ylabels[ind];
                ylabels[ind] = yl;
                int lw = lfm.stringWidth(yl);
                ylabwidth[ind++] = lw;
                if (lw > widesty) {widesty = lw;}
            }
        } else {
            // explictly specified ticks
            Enumeration nl = _yticklabels.elements();
            while (nl.hasMoreElements()) {
                String label = (String) nl.nextElement();
                int lw = lfm.stringWidth(label);
                if (lw > widesty) {widesty = lw;}
            }            
        }

        // Next we do the horizontal spacing.
        if (_ylabel != null) {
            ulx = drawRect.x + widesty + lfm.stringWidth("W") + leftPadding;
        } else {     
            ulx = drawRect.x + widesty + leftPadding;
        }
        int legendwidth = _drawLegend(drawRect.width-rightPadding, uly);
        lrx = drawRect.width-legendwidth-rightPadding;
        int width = lrx-ulx;
        xscale = width/(xMax - xMin);
        _xscale = width/(_xMax - _xMin);
        
        // White background for the plotting rectangle
        graphics.setColor(Color.white);
        graphics.fillRect(ulx,uly,width,height);

        graphics.setColor(Color.black);
        graphics.drawRect(ulx,uly,width,height);
        
        // NOTE: subjective tick length.
        int tickLength = 5;
        int xCoord1 = ulx+tickLength;
        int xCoord2 = lrx-tickLength;
        
        if (_yticks == null) {
            // auto-ticks
            ind = 0;
            for (double ypos=yStart; ypos <= _yMax; ypos += yStep) {
                // Prevent out of bounds exceptions
                if (ind >= ny) break;
                int yCoord1 = lry - (int)((ypos-_yMin)*_yscale);
                // The lowest label is shifted up slightly to avoid
                // colliding with x labels.
                int offset = 0;
                if (ind > 0) offset = halflabelheight;
                graphics.drawLine(ulx,yCoord1,xCoord1,yCoord1);
                graphics.drawLine(lrx,yCoord1,xCoord2,yCoord1);
                if (grid && yCoord1 != uly && yCoord1 != lry) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord2,yCoord1);
                    graphics.setColor(Color.black);
                }
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(ylabels[ind],
				    ulx-ylabwidth[ind++]-3, yCoord1+offset);
            }
        
            // Draw scaling annotation for y axis.
            if (_yExp != 0) {
                graphics.drawString("x10", 2, titley);
                graphics.setFont(_superscriptfont);
                graphics.drawString(Integer.toString(_yExp),
				    lfm.stringWidth("x10") + 2, 
				    titley-halflabelheight);
                graphics.setFont(_labelfont);
            }
        } else {
            // ticks have been explicitly specified
            Enumeration nt = _yticks.elements();
            Enumeration nl = _yticklabels.elements();
            while (nl.hasMoreElements()) {
                String label = (String) nl.nextElement();
                double ypos = ((Double)(nt.nextElement())).doubleValue();
                if (ypos > yMax || ypos < yMin) continue;
                int yCoord1 = lry - (int)((ypos-yMin)*_yscale);
                int offset = 0;
                if (ypos < lry - labelheight) offset = halflabelheight;
                graphics.drawLine(ulx,yCoord1,xCoord1,yCoord1);
                graphics.drawLine(lrx,yCoord1,xCoord2,yCoord1);
                if (grid && yCoord1 != uly && yCoord1 != lry) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord2,yCoord1);
                    graphics.setColor(Color.black);
                }
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(label, ulx - lfm.stringWidth(label) - 3,
				    yCoord1+offset);
            }
        }
        
        ///////////////////// horizontal axis

        int yCoord1 = uly+tickLength;
        int yCoord2 = lry-tickLength;
        if (_xticks == null) {
            // auto-ticks
            // Number of x tick marks.
            // Assume a worst case of 4 characters and a period for each label.
            int maxlabelwidth = lfm.stringWidth("8.888");
        
            // NOTE: 5 additional pixels between labels.
            int nx = 2 + width/(maxlabelwidth+5);
            // Compute x increment.
            double xStep=_roundUp((_xMax-_xMin)/(double)nx);
        
            // Compute x starting point so it is a multiple of xStep.
            double xStart=xStep*Math.ceil(_xMin/xStep);
        
            // NOTE: Following disables first tick.  Not a good idea?
            // if (xStart == xMin) xStart+=xStep;
        
            // Label the x axis.  The labels are quantized so that
            // they don't have excess resolution.
            for (double xpos=xStart; xpos <= _xMax; xpos += xStep) {
                String _xlabel = Double.toString(Math.floor(xpos*1000.0+0.5)
						 * 0.001);
                xCoord1 = ulx + (int)((xpos-_xMin)*_xscale);
                graphics.drawLine(xCoord1,uly,xCoord1,yCoord1);
                graphics.drawLine(xCoord1,lry,xCoord1,yCoord2);
                if (grid && xCoord1 != ulx && xCoord1 != lrx) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord1,yCoord2);
                    graphics.setColor(Color.black);
                }
                int labxpos = xCoord1 - lfm.stringWidth(_xlabel)/2;
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(_xlabel, labxpos, lry + 3 + labelheight);
            }
        } else {
            // ticks have been explicitly specified
            Enumeration nt = _xticks.elements();
            Enumeration nl = _xticklabels.elements();
            while (nl.hasMoreElements()) {
                String label = (String) nl.nextElement();
                double xpos = ((Double)(nt.nextElement())).doubleValue();
                if (xpos > xMax || xpos < xMin) continue;
                xCoord1 = ulx + (int)((xpos-_xMin)*_xscale);
                graphics.drawLine(xCoord1,uly,xCoord1,yCoord1);
                graphics.drawLine(xCoord1,lry,xCoord1,yCoord2);
                if (grid && xCoord1 != ulx && xCoord1 != lrx) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord1,yCoord2);
                    graphics.setColor(Color.black);
                }
                int labxpos = xCoord1 - lfm.stringWidth(label)/2;
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(label, labxpos, lry + 3 + labelheight);
            }
        }
        
        ///////////////////// Draw title and axis labels now.
        
    	// Center the title and X label over the plotting region, not
    	// the window.
        graphics.setColor(Color.black);
        
        if (_title != null) {
         	graphics.setFont(_titlefont);
            int titlex = ulx + (width - tfm.stringWidth(_title))/2;
            graphics.drawString(_title,titlex,titley);
        }
        
        graphics.setFont(_labelfont);
        if (_xlabel != null) {
            int labelx = ulx + (width - lfm.stringWidth(_xlabel))/2;
            graphics.drawString(_xlabel,labelx,ySPos);
        }
        
        int charcenter = 2 + lfm.stringWidth("W")/2;
        int charheight = labelheight;
        if (_ylabel != null) {
            // Vertical label is fairly complex to draw.
            int yl = _ylabel.length();
            int starty = uly + (lry-uly)/2 - yl*charheight/2 + charheight;
            for (int i = 0; i < yl; i++) {
                String nchar = _ylabel.substring(i,i+1);
                int cwidth = lfm.stringWidth(nchar);
                graphics.drawString(nchar,charcenter - cwidth/2, starty);
                starty += charheight;
            }
        }
    }
    
    /**
     * Rescales so that the data that is currently plotted just fits.
     */
    public synchronized void fillPlot () {
        setXRange(xBottom, xTop);
        setYRange(yBottom, yTop);
        paint(graphics);
    }

    /**
     * Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "PlotBox 1.0: Base class for plots. By: Edward A. Lee, eal@eecs.berkeley.edu";
    }
       
    /**
     * Return information about parameters.
     */
    public String[][] getParameterInfo () {
        String pinfo[][] = {
            {"dataurl",   "url",     "the URL of the data to plot"}
        };
	    return pinfo;
    }
    
    /**
     * Initialize the applet.  If a dataurl parameter has been specified,
     * read the file given by the URL and parse the commands in it.
     */
    public void init() {
        super.init();
		
        _labelfont = new Font("Helvetica", Font.PLAIN, 12);
        _superscriptfont = new Font("Helvetica", Font.PLAIN, 9);
        _titlefont = new Font("Helvetica", Font.BOLD, 14);
        
        _legendStrings = new Vector();
        _legendDatasets = new Vector();
        
        _xticks = null;
        _xticklabels = null;
        _yticks = null;
        _yticklabels = null;

        graphics = this.getGraphics();

	if (graphics == null) {
	    System.out.println("PlotBox::init(): Internal error: " +
			       "Graphic was null");
	    return;
	}

        // Check to see whether a data URL has been given.
        // Need the catch here because applets used as components have
        // no parameters. 
	String dataurl = null;
        try {
            dataurl = getParameter("dataurl");
        } catch (NullPointerException e) {
	    dataurl = _dataurl;
	}

	// Open up the input file, which could be stdin, a URL or a file.
	// This code can be called from an application, which means that
	// getDocumentBase() might fail.
	DataInputStream in;
	if (dataurl == null || dataurl.length() == 0) {
	    // Open up stdin
	    in = new DataInputStream(System.in);
	} else {
	   try {
	       URL url;
	       try {
		   url = new URL(getDocumentBase(), dataurl);
	       } catch (NullPointerException e) {
		   // If we got a NullPointerException, then perhaps
		   // we are calling this as an application, not as an applet.
		   url = new URL(_dataurl);
	       }
	       in = new DataInputStream(url.openStream());
	   } catch (MalformedURLException e) {
	       try {
		   // Just try to open it as a file.
		   in = new DataInputStream(new FileInputStream(dataurl));
	       } catch (FileNotFoundException me) {
		   System.out.println("File not found: " + dataurl + " "+me);
		   return;
	       } catch (SecurityException me) {
		   System.out.println("SecurityException: " + dataurl +" "+me);
		   return;
	       }
	   } catch (IOException ioe) {
	       System.out.println("Failure opening URL: " + dataurl + " "+ioe);
	       return;
	   }
	}

	// At this point, we've opened the data source, now read it in
	try {
	    if (_binary) {
		convertBinaryStream(in);
	    } else {
		String line = in.readLine();
		while (line != null) {
		    parseLine(line);
		    line = in.readLine();
		}
	    }
	} catch (MalformedURLException e) {
	    System.out.println("Malformed URL: " + dataurl + " "+ e);
	} catch (IOException e) {
	    System.out.println("Failure reading data: " + dataurl + " "+ e);
	} catch (PlotDataException me) {
	    System.out.println("Bad Plot Data: " + me);
	} finally {
	    try {
		in.close();
	    } catch (IOException me) {}
	}
        
        // Make a button that auto-scales the plot.
        // NOTE: The button infringes on the title space.
        // If more buttons are added, we may have to find some other place
        // for them, like below the legend, stacked vertically.
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        _fillButton = new Button("fill");
        add(_fillButton);
    }
	
    /**
     * Set the starting point for an interactive zoom box.
     */
    public boolean mouseDown(Event evt, int x, int y) {
        // ignore if out of range
        if (y <= lry && y >= uly && x <= lrx && x >= ulx) {
            _zoomx = x;
            _zoomy = y;
            return true;
        }
        return false;
    }
    
    /**
     * Set the starting point for an interactive zoom box.
     * Return a boolean indicating whether or not we have dealt with
     * the event.
     */
    public boolean mouseDrag(Event evt, int x, int y) {
        boolean pointinside = y <= lry && y >= uly && x <= lrx && x >= ulx;
        // erase previous rectangle, if there was one.
        if ((_zoomx != -1 || _zoomy != -1) && pointinside) {
            // Ability to zoom out added by William Wu.
            // If we are not already zooming, figure out whether we
            // are zooming in or out.
            if (_zoomin == false && _zoomout == false){
                if (y < _zoomy) {
                    _zoomout = true;
                    // Draw reference box.
                    graphics.drawRect(_zoomx-15, _zoomy-15, 30, 30);
                } else if (y > _zoomy) {
                    _zoomin = true; 
                }
            }

            if (_zoomin == true){   
                graphics.setXORMode(Color.white);
                // Erase the previous box if necessary.
                if (_zoomxn != -1 || _zoomyn != -1) {
                    int minx = Math.min(_zoomx, _zoomxn);
                    int maxx = Math.max(_zoomx, _zoomxn);
                    int miny = Math.min(_zoomy, _zoomyn);
                    int maxy = Math.max(_zoomy, _zoomyn);
                    graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                }
                // Draw a new box if necessary.
                if (y > _zoomy) {
                    _zoomxn = x;
                    _zoomyn = y;
                    int minx = Math.min(_zoomx, _zoomxn);
                    int maxx = Math.max(_zoomx, _zoomxn);
                    int miny = Math.min(_zoomy, _zoomyn);
                    int maxy = Math.max(_zoomy, _zoomyn);
                    graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                    graphics.setPaintMode();
                    return true;
                }
            } else if (_zoomout == true){
                graphics.setXORMode(Color.white);
                // Erase previous box if necessary.
                if (_zoomxn != -1 || _zoomyn != -1) {
                    int x_diff = Math.abs(_zoomx-_zoomxn);
                    int y_diff = Math.abs(_zoomy-_zoomyn);
                    graphics.drawRect(_zoomx-15-x_diff, _zoomy-15-y_diff,
                           30+x_diff*2, 30+y_diff*2);
                }
                if (y < _zoomy){
                    _zoomxn = x;
                    _zoomyn = y;     
                    int x_diff = Math.abs(_zoomx-_zoomxn);
                    int y_diff = Math.abs(_zoomy-_zoomyn);
                    graphics.drawRect(_zoomx-15-x_diff, _zoomy-15-y_diff,
                            30+x_diff*2, 30+y_diff*2);
                    graphics.setPaintMode();
                    return true;
                }
            }
        }
        graphics.setPaintMode();
        return false;
    }

    /**
     * Set the starting point for an interactive zoom box.
     */
    public boolean mouseUp(Event evt, int x, int y) {
        // ignore if there hasn't been a drag, or if x,y is out of range
        boolean pointinside = y <= lry && y >= uly && x <= lrx && x >= ulx;
        boolean handled = false;
        if (_zoomin == true){  
            if (_zoomxn != -1 || _zoomyn != -1) {
                // erase previous rectangle.
                int minx = Math.min(_zoomx, _zoomxn);
                int maxx = Math.max(_zoomx, _zoomxn);
                int miny = Math.min(_zoomy, _zoomyn);
                int maxy = Math.max(_zoomy, _zoomyn);
                graphics.setXORMode(Color.white);
                graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                graphics.setPaintMode();
                // if in range, zoom
                if (pointinside) {
                    double a = xMin + (_zoomx - ulx)/xscale;
                    double b = xMin + (x - ulx)/xscale;
                    if (a < b) setXRange(a, b);
                    else setXRange(b, a);
                    a = yMax - (_zoomy - uly)/yscale;
                    b = yMax - (y - uly)/yscale;
                    if (a < b) setYRange(a, b);
                    else setYRange(b, a);
                    drawPlot(true);
                }
                handled = true;
            }
        } else if (_zoomout == true){
            // Erase previous rectangle.
            graphics.setXORMode(Color.white);
            int x_diff = Math.abs(_zoomx-_zoomxn);
            int y_diff = Math.abs(_zoomy-_zoomyn);
            graphics.drawRect(_zoomx-15-x_diff, _zoomy-15-y_diff,
                    30+x_diff*2, 30+y_diff*2);
            graphics.setPaintMode();
            if (pointinside) {
                // Calculate zoom factor.
                double a = (double)(Math.abs(_zoomx - x)) / 30.0;
                double b = (double)(Math.abs(_zoomy - y)) / 30.0;
                double newx1 = xMax + (xMax - xMin) * a;
                double newx2 = xMin - (xMax - xMin) * a;
                if (newx1 > xTop) newx1 = xTop; 
                if (newx2 < xBottom) newx2 = xBottom; 
                double newy1 = yMax + (yMax - yMin) * b;
                double newy2 = yMin - (yMax - yMin) * b;
                if (newy1 > yTop) newy1 = yTop; 
                if (newy2 < yBottom) newy2 = yBottom; 
                setXRange(newx2, newx1);
                setYRange(newy2, newy1);
                drawPlot(true);
            } 
            handled = true;
        }
        _zoomin = _zoomout = false;
        _zoomxn = _zoomyn = _zoomx = _zoomy = -1;
        return handled;
    }

    /** 
      * Paint the applet contents, which in this base class is
      * only the axes.
      */
    public void paint(Graphics g) {
	super.paint(g);
	drawPlot(true);
    }
    
    /** Set the binary flag to true if we are reading pxgraph format binar
     * data.
     */
    public void setBinary (boolean binary) {
	this._binary = binary;
    }

    /** Set the dataurl.  This method is used by Applications, applets
     * should just set the dataurl parameter with:
     * &lt;param name="dataurl" value="data.plt"&gt;
     */
    public void setDataurl (String dataurl) {
	this._dataurl = dataurl;
    }


    /**
     * Control whether the grid is drawn.
     */
    public void setGrid (boolean grid) {
        this.grid = grid;
    }
    
    /**
     * Set the title of the graph.  The title will appear on the subsequent
     * call to <code>paint()</code> or <code>drawPlot()</code>.
     */
    public void setTitle (String title) {
        this._title = title;
    }
    

    /** 
     * Set the label for the X (horizontal) axis.  The label will
     * appear on the subsequent call to <code>paint()</code> or
     * <code>drawPlot()</code>.
     */
    public void setXLabel (String label) {
        this._xlabel = label;
    }

    /** 
     * Set the label for the Y (vertical) axis.  The label will
     * appear on the subsequent call to <code>paint()</code> or
     * <code>drawPlot()</code>.
     */
    public void setYLabel (String label) {
        this._ylabel = label;
    }

    /** 
     * Set the X (horizontal) range of the plot.  If this is not done
     * explicitly, then the range is computed automatically from data
     * available when <code>paint()</code> or <code>drawPlot()</code>
     * are called.  If min and max are identical, then the range is
     * arbitrarily spread by 1.
     */
    public void setXRange (double min, double max) {
        _setXRange(min,max);
        xRangeGiven = true;
    }

    /**
     * Set the Y (vertical) range of the plot.  If this is not done
     * explicitly, then the range is computed automatically from data
     * available when <code>paint()</code> or <code>drawPlot()</code>
     * are called.  If min and max are identical, then the range is
     * arbitrarily spread by 0.1.
     */
    public void setYRange (double min, double max) {
        _setYRange(min,max);
        yRangeGiven = true;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /**
     * Abstract method - convert a Binary Stream
     * @exception PlotDataException if there is a serious data format problem.
     * @exception java.io.IOException if an I/O error occurs.
     */
    protected void convertBinaryStream(DataInputStream in) throws
	PlotDataException, IOException {
	    throw new PlotDataException("Binary data not supported in the" +
					"baseclass");
    }

    /**
     * Put a mark corresponding to the specified dataset at the
     * specified x and y position.  In this base class, a point is a
     * filled circle 6 pixels across.  Note that marks greater than
     * about 6 pixels in size will not look very good since they will
     * overlap axis labels and may not fit well in the legend.  The
     * <i>connected</i> argument is ignored, but in derived classes,
     * it specifies whether the point should be connected by a line to
     * previously drawn points.  The <i>clip</i> argument, if
     * <code>true</code>, states that the point should not be drawn if
     * it is out of range.  The return value indicates whether the
     * point is drawn.
     */
    protected boolean drawPoint(int dataset, int xpos, int ypos,
				boolean connected, boolean clip) {
        boolean pointinside = ypos <= lry && ypos >= uly && xpos <= lrx && xpos >= ulx;
        if (!pointinside && clip) {return false;}
        // Points are only distinguished up to 10 data sets.
        dataset %= 10;
        if (usecolor) {
            graphics.setColor(colors[dataset]);
        }
        graphics.fillOval(xpos-1, ypos-1, 3, 3);
        graphics.setColor(Color.black);
        return true;
    }

    /**
     * Parse a line that gives plotting information.  In this base
     * class, only lines pertaining to the title and labels are processed.
     * Everything else is ignored. Return true if the line is recognized.
     */
    public boolean parseLine (String line) {
        // Parse commands in the input file, ignoring lines with
        // syntax errors or unrecognized commands.
        if (line.startsWith("#")) {
            // comment character
            return true;
        }
        if (line.startsWith("TitleText:")) {
            setTitle((line.substring(10)).trim());
            return true;
        }
        if (line.startsWith("XLabel:")) {
            setXLabel((line.substring(7)).trim());
            return true;
        }
        if (line.startsWith("YLabel:")) {
            setYLabel((line.substring(7)).trim());
            return true;
        }
        if (line.startsWith("XRange:")) {
        	int comma = line.indexOf(",", 7);
        	if (comma > 0) {
        	    String min = (line.substring(7,comma)).trim();
        	    String max = (line.substring(comma+1)).trim();
        	    try {
        	        Double dmin = new Double(min);
        	        Double dmax = new Double(max);
        	        setXRange(dmin.doubleValue(), dmax.doubleValue());
        	    } catch (NumberFormatException e) {
        	        // ignore if format is bogus.
        	    }
        	}
        	return true;
        }
        if (line.startsWith("YRange:")) {
        	int comma = line.indexOf(",", 7);
        	if (comma > 0) {
        	    String min = (line.substring(7,comma)).trim();
        	    String max = (line.substring(comma+1)).trim();
        	    try {
        	        Double dmin = new Double(min);
        	        Double dmax = new Double(max);
        	        setYRange(dmin.doubleValue(), dmax.doubleValue());
        	    } catch (NumberFormatException e) {
        	        // ignore if format is bogus.
        	    }
        	}
        	return true;
        }
        if (line.startsWith("XTicks:")) {
            // example:
            // XTicks "label" 0, "label" 1, "label" 3
            boolean cont = true;
            _parsePairs(line.substring(7), true);
        	return true;
        }
        if (line.startsWith("YTicks:")) {
            // example:
            // YTicks "label" 0, "label" 1, "label" 3
            boolean cont = true;
            _parsePairs(line.substring(7), false);
        	return true;
        }
        
        if (line.startsWith("Grid:")) {
            if (line.indexOf("off",5) >= 0) {
                grid = false;
            } else {
                grid = true;
            }
            return true;
        }
        if (line.startsWith("Color:")) {
            if (line.indexOf("off",6) >= 0) {
                usecolor = false;
            } else {
                usecolor = true;
            }
            return true;
        }
        return false;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                           protected variables                    ////
    
    Graphics graphics;

	// The range of the plot.
    protected double yMax, yMin, xMax, xMin;

    // Whether the ranges have been given.
    protected boolean xRangeGiven = false;
    protected boolean yRangeGiven = false;
    // The minimum and maximum values registered so far, for auto ranging.
    protected double xBottom = Double.MAX_VALUE;
    protected double xTop = Double.MIN_VALUE;
    protected double yBottom = Double.MAX_VALUE;
    protected double yTop = Double.MIN_VALUE;
    
    // Whether to draw a background grid.
    protected boolean grid = true;
    
    // Derived classes can increment these to make space around the plot.
    protected int topPadding = 10;
    protected int bottomPadding = 5;
    protected int rightPadding = 10;
    protected int leftPadding = 10;

    // The plot rectangle in pixels.
    // The naming convention is: "ulx" = "upper left x", where "x" is
    // the horizontal dimension.
    protected int ulx, uly, lrx, lry;

    // Scaling used in plotting points.
    protected double yscale, xscale;
    
    // Indicator whether to use colors
    protected boolean usecolor = true;

    // Default colors, by data set.
    static protected Color[] colors = {
        new Color(0xcd0000),   // red3
        new Color(0x4a708b),   // skyblue4
        new Color(0x6b1063),   // violet-ish
        new Color(0x000000),   // black
        new Color(0xeec900),   // gold2
        new Color(0x008b00),   // green4
        new Color(0x8a2be2),   // blueviolet
        new Color(0x53868b),   // cadetblue4
        new Color(0xd2691e),   // chocolate
        new Color(0x556b2f),   // darkolivegreen
    };
        
    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /**
     * Draw the legend in the upper right corner and return the width
     * (in pixels)  used up.  The arguments give the upper right corner
     * of the region where the legend should be placed.
     */
    private int _drawLegend(int urx, int ury) {
        // FIXME: consolidate all these for efficiency
        graphics.setFont(_labelfont);
        FontMetrics lfm = graphics.getFontMetrics();
        int spacing = lfm.getHeight();

        Enumeration v = _legendStrings.elements();
        Enumeration i = _legendDatasets.elements();
        int ypos = ury + spacing;
        int maxwidth = 0;
        while (v.hasMoreElements()) {
            String legend = (String) v.nextElement();
            // NOTE: relies on _legendDatasets having the same num. of entries.
            int dataset = ((Integer) i.nextElement()).intValue();
            // NOTE: 6 pixel width of point assumed.
            if (!drawPoint(dataset, urx-3, ypos-3, false, false)) {
                // Point was not drawn, perhaps because there is no mark.
                // Draw a colored rectangle.
                if (usecolor) {
                    graphics.setColor(colors[dataset]);
                }
                graphics.fillRect(urx-6, ypos-6, 6, 6);
                graphics.setColor(Color.black);
            }
            int width = lfm.stringWidth(legend);
            if (width > maxwidth) maxwidth = width;
            graphics.drawString(legend, urx - 15 - width, ypos);
            ypos += spacing;
        }
        return 22 + maxwidth;  // NOTE: subjective spacing parameter.
    }
    
    /*
     * Parse a string of the form: "word num, word num, word num, ..."
     * where the word must be enclosed in quotes if it contains spaces,
     * and the number is interpreted as a floating point number.  Ignore
     * any incorrectly formatted fields.  Append the words in order to the
     * vector wordved and the numbers (as Doubles) to the vector numvec.
     */
    private void _parsePairs (String line, boolean xtick) {    
        int start = 0;
        boolean cont = true;
        while (cont) {
        	int comma = line.indexOf(",", start);
        	String pair;
        	if (comma > start) {
        	    pair = (line.substring(start,comma)).trim();
            } else {
      	        pair = (line.substring(start)).trim();
       	        cont = false;
       	    }
       	    int close;
       	    int open = 0;
       	    if (pair.startsWith("\"")) {
        	    close = pair.indexOf("\"",1);
        	    open = 1;
        	} else {
                close = pair.indexOf(" ");	        
            }
       	    if (close > 0) {
       	        String label = pair.substring(open,close);
       	        String index = (pair.substring(close+1)).trim();
       	        try {
       	            double idx = (Double.valueOf(index)).doubleValue();
       	            if (xtick) addXTick(label, idx);
       	            else addYTick(label,idx);
       	        } catch (NumberFormatException e) {
       	            // ignore if format is bogus.
       	        }
       	    }
            start = comma + 1;
       	    comma = line.indexOf(",",start);
       	}
    }

    /*
     * Given a number, round up to the nearest power of ten
     * times 1, 2, or 5.
     *
     * Note: The argument must be strictly positive.
     */
     private double _roundUp(double val) {
         int exponent, idx;
         exponent = (int) Math.floor(Math.log(val)*_log10scale);
         val *= Math.pow(10, -exponent);
         if (val > 5.0) val = 10.0;
         else if (val > 2.0) val = 5.0;
         else if (val > 1.0) val = 2.0;
         val *= Math.pow(10, exponent);
         return val;
    }

    /*
     * Internal implementation of setXRange, so that it can be called when
     * autoranging. 
     */
    private void _setXRange (double min, double max) {
        // If values are invalid, try for something reasonable.
        if (min > max) {
            min = -1.0;
            max = 1.0;
        } else if (min == max) {
            min -= 1.0;
            max += 1.0;
        }
        // Find the exponent.
        double largest = Math.max(Math.abs(min),Math.abs(max));
        _xExp = (int) Math.floor(Math.log(largest)*_log10scale);
        // Use the exponent only if it's larger than 1 in magnitude.
        if (_xExp > 1 || _xExp < -1) {
            double xs = 1.0/Math.pow(10.0,(double)_xExp);
            _xMin = min*xs;
            _xMax = max*xs;
        } else {
            _xMin = min;
            _xMax = max;
            _xExp = 0;
        }
        xMin = min;
        xMax = max;
    }

    /*
     * Internal implementation of setYRange, so that it can be called when
     * autoranging.
     */
    private void _setYRange (double min, double max) {
        // If values are invalid, try for something reasonable.
        if (min > max) {
            min = -1.0;
            max = 1.0;
        } else if (min == max) {
            min -= 0.1;
            max += 0.1;
        }
        // Find the exponent.
        double largest = Math.max(Math.abs(min),Math.abs(max));
        _yExp = (int) Math.floor(Math.log(largest)*_log10scale);
        // Use the exponent only if it's larger than 1 in magnitude.
        if (_yExp > 1 || _yExp < -1) {
            double ys = 1.0/Math.pow(10.0,(double)_yExp);
            _yMin = min*ys;
            _yMax = max*ys;
        } else {
            _yMin = min;
            _yMax = max;
            _yExp = 0;
        }
        yMin = min;
        yMax = max;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    // The URL to be opened.  This variable is not used if we are running
    // as an applet, but applications should call setDataurl().
    private String _dataurl = null;

    // Set to true if we are reading in pxgraph format binary data.
    private boolean _binary = false;

    // The range of the plot as labeled (multiply by 10^exp for actual range.
    private double _yMax, _yMin, _xMax, _xMin;
    // The power of ten by which the range numbers should be multiplied.
    private int _yExp, _xExp;

    // Scaling used in making tick marks
    private double _yscale, _xscale;

    private Font _labelfont, _superscriptfont, _titlefont;
    
    // For use in calculating log base 10.  A log times this is a log base 10.
    private static final double _log10scale = 1/Math.log(10);
    
    // An array of strings for reporting errors.
    private String _errorMsg[];
    
    // The title and label strings.
    private String _xlabel, _ylabel, _title;
    
    // Legend information.
    private Vector _legendStrings;
    private Vector _legendDatasets;
    
    // If XTicks or YTicks are given
    private Vector _xticks, _xticklabels, _yticks, _yticklabels;

    // A button for filling the plot
    private Button _fillButton;
    
    // Variables keeping track of the interactive zoom box.
    // Initialize to impossible values.
    private int _zoomx = -1;
    private int _zoomy = -1;
    private int _zoomxn = -1;
    private int _zoomyn = -1;

    // Control whether we are zooming in or out.
    private boolean _zoomin = false;
    private boolean _zoomout = false;
}

/* A labeled box for signal plots.

@Author: Edward A. Lee and Christopher Hylands

@Contributors:  William Wu

@Version: $Id$

@Copyright (c) 1997- The Regents of the University of California.
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
package ptplot;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

//////////////////////////////////////////////////////////////////////////
//// PlotBox
/** 
 * Construct a labeled box within which to place a data plot.  A title,
 * X and Y axis labels, tick marks, and a legend are all supported.
 * Zooming in and out is supported.  To zoom in, drag the mouse
 * downwards to draw a box.  To zoom out, drag the mouse upward.
 * Zooming out stops automatically at the point where the data fills
 * the drawing rectangle.
 *
 * The box can be configured either through a file with commands or
 * through direct invocation of the public methods of the class.
 * If a file is used, the file can be given as a URL through the
 * <code>setDataurl</code> method. The file contains any number
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
 * then the ranges are computed automatically from the data and padded
 * slightly so that datapoints are not plotted on the axes.
 * <p>
 * The tick marks for the axes are usually computed automatically from
 * the ranges.  Every attempt is made to choose reasonable positions
 * for the tick marks regardless of the data ranges (powers of
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
 * The X and Y axes can use a logarithmic scale with the following commands:
 * <pre>
 * XLog: on
 * YLog: on
 * </pre>
 * The grid labels represent powers of 10.  Note that if a logarithmic
 * scale is used, then the values must be positive.  Non-positive values
 * will be silently dropped.
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
 * @author Edward A. Lee, Christopher Hylands
 * @version $Id$
 */
public class PlotBox extends Panel {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** 
     * Handle button presses to fill the plot.  This rescales so that
     * the data that is currently plotted just fits.
     * @deprecated As of JDK1.1 in java.awt.component 
     * but we need to compile under 1.0.2 for netscape3.x compatibility.
     */
    public boolean action (Event evt, Object arg) {
        if (evt.target == _fillButton) {
            fillPlot(_graphics);
            return true;
        } else {
            return super.action (evt, arg); // action() is deprecated in 1.1
            // but we need to compile under 
            // jdk1.0.2 for netscape3.x
        }
    }

    /**
     * Create the peer of the plot box.
     */
    public void addNotify() {
        // This method is called after our Panel is first created
        // but before it is actually displayed.
        super.addNotify();
        _measureFonts();
    }

    /** 
     * Add a legend (displayed at the upper right) for the specified
     * data set with the specified string.  Short strings generally
     * fit better than long strings.  You must call <code>init()</code>
     * before calling this method.
     */
    public void addLegend(int dataset, String legend) {
        if (_debug > 8) System.out.println("PlotBox addLegend: " +
                dataset + " " + legend);
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
    public synchronized void drawPlot(Graphics graphics, boolean clearfirst) {
        if (_debug > 7)
            System.out.println("PlotBox: drawPlot"+graphics+" "+clearfirst);
        if (graphics == null) {
            System.out.println("Attempt to draw axes without "+
                    "a Graphics object.");
            return;
        }
        //        super.paint(graphics);
        // Give other threads a chance, so that hopefully things are
        // up to date.
        //        Thread.yield();
            
        // Find the width and height of the total drawing area, and clear it.
        Rectangle drawRect = bounds(); // FIXME: bounds() is deprecated
        // in JDK1.1, but we need to compile under 1.0.2 for
        // netscape3.x compatibility.

        graphics.setPaintMode();
        if (clearfirst) {
            // Clear all the way from the top so that we erase the title.
            // If we don't do this, then zooming in with the pxgraph
            // application ends up blurring the title.
            graphics.clearRect(0,0,drawRect.width, drawRect.height);
        }

        if (_debug > 8) {
            System.out.println("PlotBox: drawPlot drawRect ="+
                    drawRect.width+" "+drawRect.height+" "+
                    drawRect.x+" "+drawRect.y); 
            graphics.drawRect(0,0,drawRect.width, drawRect.height);
        }

        // If an error message has been set, display it and return.
        if (_errorMsg != null) {
            int fheight = _labelFontMetrics.getHeight() + 2;
            int msgy = fheight;
            graphics.setColor(Color.black);
            for(int i=0; i<_errorMsg.length;i++) {
                graphics.drawString(_errorMsg[i],10, msgy);
                msgy += fheight;
            }
            return;
        }

        // Make sure we have an x and y range
        if (!_xRangeGiven) {
            if (_xBottom > _xTop) {
                // have nothing to go on.
                _setXRange(0,0);
            } else {
                _setXRange(_xBottom, _xTop);
            }
        }
        if (!_yRangeGiven) {
            if (_yBottom > _yTop) {
                // have nothing to go on.
                _setYRange(0,0);
            } else {
                _setYRange(_yBottom, _yTop);
            }
        }
         
        // Vertical space for title, if appropriate.
        // NOTE: We assume a one-line title.
        int titley = 0;
        int titlefontheight = _titleFontMetrics.getHeight();
        if (_title != null || _yExp != 0) {
            titley = titlefontheight + _topPadding;
        }
        
        // Number of vertical tick marks depends on the height of the font
        // for labeling ticks and the height of the window.
        graphics.setFont(_labelfont);
        int labelheight = _labelFontMetrics.getHeight();
        int halflabelheight = labelheight/2;

        // Draw scaling annotation for x axis.
        // NOTE: 5 pixel padding on bottom.
        int ySPos = drawRect.height - 5;
        int xSPos = drawRect.width - _rightPadding;
        if (_xlog) 
            _xExp = (int)Math.floor(_xtickMin);
        if (_xExp != 0 && _xticks == null) {
            String superscript = Integer.toString(_xExp);
            xSPos -= _superscriptFontMetrics.stringWidth(superscript);
            graphics.setFont(_superscriptfont);
            if (!_xlog) {
                graphics.drawString(superscript,xSPos,ySPos - halflabelheight);
                xSPos -= _labelFontMetrics.stringWidth("x10");
                graphics.setFont(_labelfont);
                graphics.drawString("x10", xSPos, ySPos);
            }
            // NOTE: 5 pixel padding on bottom
            _bottomPadding = (3 * labelheight)/2 + 5;
        }
        
        // NOTE: 5 pixel padding on the bottom.
        if (_xlabel != null && _bottomPadding < labelheight + 5) {
            _bottomPadding = titlefontheight + 5;
        }
        
        // Compute the space needed around the plot, starting with vertical.
        // NOTE: padding of 5 pixels below title.
        _uly = titley + 5;
        // NOTE: 3 pixels above bottom labels.
        _lry = drawRect.height-labelheight-_bottomPadding-3; 
        int height = _lry-_uly;
        _yscale = height/(_yMax - _yMin);
        _ytickscale = height/(_ytickMax - _ytickMin);

        ///////////////////// vertical axis

        // Number of y tick marks.
        // NOTE: subjective spacing factor.
        int ny = 2 + height/(labelheight+10);
        // Compute y increment.
        double yStep=_roundUp((_ytickMax-_ytickMin)/(double)ny);
        
        // Compute y starting point so it is a multiple of yStep.
        double yStart=yStep*Math.ceil(_ytickMin/yStep);
        
        // NOTE: Following disables first tick.  Not a good idea?
        // if (yStart == _ytickMin) yStart+=yStep;
        
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
            Vector ygrid = null;
            if (_ylog) {
                ygrid = _gridInit(yStart, yStep);
            }

            // automatic ticks
            // First, figure out how many digits after the decimal point
            // will be used.
            int numfracdigits = _numFracDigits(yStep);

            // NOTE: Test cases kept in case they are needed again.
            // System.out.println("0.1 with 3 digits: " + _formatNum(0.1, 3));
            // System.out.println("0.0995 with 3 digits: " +
            //                    _formatNum(0.0995, 3));
            // System.out.println("0.9995 with 3 digits: " +
            //                    _formatNum(0.9995, 3));
            // System.out.println("1.9995 with 0 digits: " +
            //                    _formatNum(1.9995, 0));
            // System.out.println("1 with 3 digits: " + _formatNum(1, 3));
            // System.out.println("10 with 0 digits: " + _formatNum(10, 0));
            // System.out.println("997 with 3 digits: " + _formatNum(997,3));
            // System.out.println("0.005 needs: " + _numFracDigits(0.005));
            // System.out.println("1 needs: " + _numFracDigits(1));
            // System.out.println("999 needs: " + _numFracDigits(999));
            // System.out.println("999.0001 needs: "+_numFracDigits(999.0001));
            // System.out.println("0.005 integer digits: " +
            //                    _numIntDigits(0.005));
            // System.out.println("1 integer digits: " + _numIntDigits(1));
            // System.out.println("999 integer digits: " + _numIntDigits(999));
            // System.out.println("-999.0001 integer digits: " +
            //                    _numIntDigits(999.0001));

            
            double yTmpStart = yStart;
            if (_ylog)
                yTmpStart = _gridStep(ygrid,yStart, yStep, _ylog);

            for (double ypos = yTmpStart; ypos <= _ytickMax;
                 ypos = _gridStep(ygrid, ypos, yStep, _ylog)) {
                // Prevent out of bounds exceptions
                if (ind >= ny) break;
                String yticklabel;
                if (_ylog) {
                    yticklabel = _formatLogNum(ypos, numfracdigits);
                } else {
                    yticklabel = _formatNum(ypos, numfracdigits);
                }
                ylabels[ind] = yticklabel;
                int lw = _labelFontMetrics.stringWidth(yticklabel);
                ylabwidth[ind++] = lw;
                if (lw > widesty) {widesty = lw;}
            }
        } else {
            // explictly specified ticks
            Enumeration nl = _yticklabels.elements();
            while (nl.hasMoreElements()) {
                String label = (String) nl.nextElement();
                int lw = _labelFontMetrics.stringWidth(label);
                if (lw > widesty) {widesty = lw;}
            }            
        }

        // Next we do the horizontal spacing.
        if (_ylabel != null) {
            _ulx = widesty + _labelFontMetrics.stringWidth("W") + _leftPadding;
        } else {     
            _ulx = widesty + _leftPadding;
        }
        int legendwidth = _drawLegend(graphics,
                drawRect.width-_rightPadding, _uly);
        _lrx = drawRect.width-legendwidth-_rightPadding;
        int width = _lrx-_ulx;
        _xscale = width/(_xMax - _xMin);
        _xtickscale = width/(_xtickMax - _xtickMin);
        
        if (_debug > 8) {
            System.out.println("PlotBox: drawPlot _ulx "+_ulx+" "+_uly+" "+
                    _lrx+" "+_lry+" "+width+" "+height);
        }

        if (_background == null) {
                throw new Error("PlotBox.drawPlot(): _background == null\n" +
                        "Be sure to call init() before calling paint().");
        }

        // background for the plotting rectangle
        graphics.setColor(_background);
        graphics.fillRect(_ulx,_uly,width,height);

        graphics.setColor(_foreground);
        graphics.drawRect(_ulx,_uly,width,height);
        
        // NOTE: subjective tick length.
        int tickLength = 5;
        int xCoord1 = _ulx+tickLength;
        int xCoord2 = _lrx-tickLength;
        
        if (_yticks == null) {
            // auto-ticks
            Vector ygrid = null;
            double yTmpStart = yStart;
            if (_ylog) {
                ygrid = _gridInit(yStart, yStep);
                yTmpStart = _gridStep(ygrid, yStart, yStep, _ylog);
                if (_debug == 5) 
                    System.out.println("PlotBox: drawPlot: YAXIS ind="+ind+
                            " ny="+ny+" yStart="+yStart+" yStep="+yStep+
                            " yTmpStart="+yTmpStart);
                ny = ind;
            }
            // FIXME: What does ind do here?  It is never set in the loop?
            ind = 0;
            // Set to false if we don't need the exponent  
            boolean needExponent = _ylog;
            for (double ypos=yTmpStart; ypos <= _ytickMax;
                 ypos = _gridStep(ygrid, ypos, yStep, _ylog)) {
                // Prevent out of bounds exceptions
                if (ind >= ny) break;
                int yCoord1 = _lry - (int)((ypos-_ytickMin)*_ytickscale);
                // The lowest label is shifted up slightly to avoid
                // colliding with x labels.
                int offset = 0;
                if (ind > 0) offset = halflabelheight;
                graphics.drawLine(_ulx,yCoord1,xCoord1,yCoord1);
                graphics.drawLine(_lrx,yCoord1,xCoord2,yCoord1);
                if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord2,yCoord1);
                    graphics.setColor(_foreground);
                }
                // Check to see if any of the labels printed contain
                // the exponent.  If we don't see an exponent, then print it.
                if (_ylog && ylabels[ind].indexOf('e') != -1 )
                    needExponent = false;

                if (_debug == 5) 
                    System.out.println("PlotBox: drawPlot: ypos = "+ypos+
                            " _ytickMax="+_ytickMax+" ny="+ny+
                            " ylabels["+ind+"] ="+ylabels[ind] );
                // NOTE: 4 pixel spacing between axis and labels.
                graphics.drawString(ylabels[ind],
                        _ulx-ylabwidth[ind++]-4, yCoord1+offset);
            }

            if (_ylog) {
                if (needExponent) {
                    // We zoomed in, so we need the exponent
                    _yExp = (int)Math.floor(yTmpStart);
                } else {
                    _yExp = 0;
                }
            }

            // Draw scaling annotation for y axis.
            if (_yExp != 0) {
                graphics.drawString("x10", 2, titley);
                graphics.setFont(_superscriptfont);
                graphics.drawString(Integer.toString(_yExp),
                        _labelFontMetrics.stringWidth("x10") + 2, 
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
                if (ypos > _yMax || ypos < _yMin) continue;
                int yCoord1 = _lry - (int)((ypos-_yMin)*_yscale);
                int offset = 0;
                if (ypos < _lry - labelheight) offset = halflabelheight;
                graphics.drawLine(_ulx,yCoord1,xCoord1,yCoord1);
                graphics.drawLine(_lrx,yCoord1,xCoord2,yCoord1);
                if (_grid && yCoord1 != _uly && yCoord1 != _lry) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord2,yCoord1);
                    graphics.setColor(_foreground);
                }
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(label,
                        _ulx - _labelFontMetrics.stringWidth(label) - 3,
                        yCoord1+offset);
            }
        }
        
        ///////////////////// horizontal axis

        int yCoord1 = _uly+tickLength;
        int yCoord2 = _lry-tickLength;
        if (_xticks == null) {
            // auto-ticks

            // Number of x tick marks. 
            // Need to start with a guess and converge on a solution here.
            int nx = 10;
            double xStep = 0.0;
            int numfracdigits = 0;
            int charwidth = _labelFontMetrics.stringWidth("8");
            if (_xlog) {
                // X axes log labels will be at most 6 chars: -1E-02
                nx = 2 + width/((charwidth * 6) + 10);
            } else {
                // Limit to 10 iterations
                int count = 0;
                while (count++ <= 10) {
                    xStep=_roundUp((_xtickMax-_xtickMin)/(double)nx);
                    // Compute the width of a label for this xStep
                    numfracdigits = _numFracDigits(xStep);
                    // Number of integer digits is the maximum of two endpoints
                    int intdigits = _numIntDigits(_xtickMax);
                    int inttemp = _numIntDigits(_xtickMin);
                    if (intdigits < inttemp) {
                        intdigits = inttemp;
                    }
                    // Allow two extra digits (decimal point and sign).
                    int maxlabelwidth = charwidth *
                        (numfracdigits + 2 + intdigits);
                    // Compute new estimate of number of ticks.
                    int savenx = nx;
                    // NOTE: 10 additional pixels between labels.
                    // NOTE: Try to ensure at least two tick marks.
                    nx = 2 + width/(maxlabelwidth+10);
                    if (nx - savenx <= 1 || savenx - nx <= 1) break;
                }
            }
            xStep=_roundUp((_xtickMax-_xtickMin)/(double)nx);
            numfracdigits = _numFracDigits(xStep);
            
            // Compute x starting point so it is a multiple of xStep.
            double xStart=xStep*Math.ceil(_xtickMin/xStep);
        
            // NOTE: Following disables first tick.  Not a good idea?
            // if (xStart == _xMin) xStart+=xStep;
        
            Vector xgrid = null;
            double xTmpStart = xStart;
            if (_xlog) {
                xgrid = _gridInit(xStart, xStep);
                //xTmpStart = _gridStep(xgrid, xStart, xStep, _xlog);
                if (_debug == 5) 
                    System.out.println("PlotBox: drawPlot: XAXIS "+
                            " xStart="+xStart+" nx="+nx+" xStep="+xStep+
                            " xTmpStart="+xTmpStart);
            }

            // Set to false if we don't need the exponent  
            boolean needExponent = _xlog;

            // Label the x axis.  The labels are quantized so that
            // they don't have excess resolution.
            for (double xpos=xTmpStart; xpos <= _xtickMax;
                 xpos = _gridStep(xgrid, xpos, xStep, _xlog)) {
                String xticklabel;
                if (_xlog) {
                    xticklabel = _formatLogNum(xpos, numfracdigits);
                    if (xticklabel.indexOf('e') != -1 )
                        needExponent = false;
                } else {
                    xticklabel = _formatNum(xpos, numfracdigits);
                }
                xCoord1 = _ulx + (int)((xpos-_xtickMin)*_xtickscale);
                graphics.drawLine(xCoord1,_uly,xCoord1,yCoord1);
                graphics.drawLine(xCoord1,_lry,xCoord1,yCoord2);
                if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord1,yCoord2);
                    graphics.setColor(_foreground);
                }
                int labxpos = xCoord1 -
                    _labelFontMetrics.stringWidth(xticklabel)/2;
                if (_debug == 5) 
                    System.out.println("PlotBox: drawPlot: xpos = "+xpos+
                            " _xtickMax="+_xtickMax+
                            " xticklabel="+xticklabel);
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(xticklabel, labxpos,
                        _lry + 3 + labelheight);
            }
            if (_xlog) {
                if (needExponent) {
                    _xExp = (int)Math.floor(xTmpStart);
                    graphics.setFont(_superscriptfont);
                    graphics.drawString(Integer.toString(_xExp), xSPos,
                            ySPos - halflabelheight);
                    xSPos -= _labelFontMetrics.stringWidth("x10");
                    graphics.setFont(_labelfont);
                    graphics.drawString("x10", xSPos, ySPos);
                } else {
                    _xExp = 0;
                }
            }


        } else {
            // ticks have been explicitly specified
            Enumeration nt = _xticks.elements();
            Enumeration nl = _xticklabels.elements();
            while (nl.hasMoreElements()) {
                String label = (String) nl.nextElement();
                double xpos = ((Double)(nt.nextElement())).doubleValue();
                if (xpos > _xMax || xpos < _xMin) continue;
                xCoord1 = _ulx + (int)((xpos-_xMin)*_xscale);
                graphics.drawLine(xCoord1,_uly,xCoord1,yCoord1);
                graphics.drawLine(xCoord1,_lry,xCoord1,yCoord2);
                if (_grid && xCoord1 != _ulx && xCoord1 != _lrx) {
                    graphics.setColor(Color.lightGray);
                    graphics.drawLine(xCoord1,yCoord1,xCoord1,yCoord2);
                    graphics.setColor(_foreground);
                }
                int labxpos = xCoord1 - _labelFontMetrics.stringWidth(label)/2;
                // NOTE: 3 pixel spacing between axis and labels.
                graphics.drawString(label, labxpos, _lry + 3 + labelheight);
            }
        }
        
        ///////////////////// Draw title and axis labels now.
        
        // Center the title and X label over the plotting region, not
        // the window.
        graphics.setColor(_foreground);
        
        if (_title != null) {
            graphics.setFont(_titlefont);
            int titlex = _ulx +
                (width - _titleFontMetrics.stringWidth(_title))/2;
            graphics.drawString(_title,titlex,titley);
        }
        
        graphics.setFont(_labelfont);
        if (_xlabel != null) {
            int labelx = _ulx +
                (width - _labelFontMetrics.stringWidth(_xlabel))/2;
            graphics.drawString(_xlabel,labelx,ySPos);
        }
        
        int charcenter = 2 + _labelFontMetrics.stringWidth("W")/2;
        int charheight = labelheight;
        if (_ylabel != null) {
            // Vertical label is fairly complex to draw.
            int yl = _ylabel.length();
            int starty = _uly + (_lry-_uly)/2 - yl*charheight/2 + charheight;
            for (int i = 0; i < yl; i++) {
                String nchar = _ylabel.substring(i,i+1);
                int cwidth = _labelFontMetrics.stringWidth(nchar);
                graphics.drawString(nchar,charcenter - cwidth/2, starty);
                starty += charheight;
            }
        }
    }
    
    /**
     * Rescales so that the data that is currently plotted just fits.
     */
    public synchronized void fillPlot () {
        if (_debug > 7) System.out.println("PlotBox: fillPlot()");
        fillPlot(_graphics);
    }

    /**
     * Rescales so that the data that is currently plotted just fits.
     */
    public synchronized void fillPlot (Graphics graphics) {
        setXRange(_xBottom, _xTop);
        setYRange(_yBottom, _yTop);
        paint(graphics);
    }

    /**
     * Get the Font by name.  
     * @deprecated: As of JDK1.1, use Font.decode() instead.
     * We need to compile under JDK1.0.2, so we use this method.
     */
    public Font getFontByName(String fullfontname) {
        // Can't use Font.decode() here, it is not present in jdk1.0.2
        //_labelfont = Font.decode(fullfontname);

        String fontname = new String ("helvetica");
        int style = Font.PLAIN;
        int size = 12;
        StringTokenizer stoken = new StringTokenizer(fullfontname,"-");
        
        if (stoken.hasMoreTokens()) {
            fontname = stoken.nextToken();
        }
        if (stoken.hasMoreTokens()) {
            String stylename = stoken.nextToken();
            // FIXME: we need to be able to mix and match these
            if (stylename.equals("PLAIN")) {
                style = Font.PLAIN;
            } else if (stylename.equals("BOLD")) {
                style = Font.BOLD;
            } else if (stylename.equals("ITALIC")) {
                style = Font.ITALIC;
            } else {
                // Perhaps this is a font size?
                try {
                    size = Integer.valueOf(stylename).intValue();
                } catch (NumberFormatException e) {}
            }
        }
        if (stoken.hasMoreTokens()) {
            try {
                size = Integer.valueOf(stoken.nextToken()).intValue();
            } catch (NumberFormatException e) {}
        }
        if (_debug > 7) System.out.println("PlotBox: getFontByName: "+
                fontname+" "+style+" "+size);
        return new Font(fontname, style, size);
    }

    /** 
     * Convert a color name into a Color.
     */
    public static Color getColorByName(String name) {
        try {
            // Check to see if it is a hexadecimal
            // Can't use Color decode here, it is not in 1.0.2
            //Color col = Color.decode(name);
            Color col = new Color(Integer.parseInt(name,16));
            return col;
        } catch (NumberFormatException e) {}
        // FIXME: This is a poor excuse for a list of colors and values.
        // We should use a hash table here.
        // Note that Color decode() wants the values to start with 0x.
        String names[][] = {
            {"black","00000"},{"white","ffffff"},
            {"red","ff0000"}, {"green","00ff00"}, {"blue","0000ff"}
        };
        for(int i=0;i< names.length; i++) {
            if(name.equals(names[i][0])) {
                try {
                    Color col = new Color(Integer.parseInt(names[i][1],16));
                    return col;
                } catch (NumberFormatException e) {}
            }
        }
        return null;
    }

    /**
     * Get the dataurl.
     */
    public String getDataurl () {
        return _dataurl;
    }

    /** Get the document base
     */
    public URL getDocumentBase () {
        return _documentBase;
    }

    /** 
     * Get the legend for a dataset.
     */
    public String getLegend(int dataset) {
        int idx = _legendDatasets.indexOf(new Integer(dataset),0);
        if (idx != -1) {
            return (String)_legendStrings.elementAt(idx);
        } else {
            return null;
        }
    }
      
    /** Get the minimum size of this component.
     */
    public Dimension getMinimumSize() {
        if (_debug > 8) System.out.println("PlotBox: getMinimumSize");
        return new Dimension(_width, _height);
    }


    /** Get the preferred size of this component.
     */
    public Dimension getPreferredSize() {
        if (_debug > 8) System.out.println("PlotBox: getPreferredSize");
       return new Dimension(_width, _height);
    }


    /** Initialize the component, creating the fill button and setting
     * the colors.  If the dataurl has been set, then parse that file.
     */
    public void init() {
        //super.init();
        if (_debug > 8) System.out.println("PlotBox: init");
                
        _xticks = null;
        _xticklabels = null;
        _yticks = null;
        _yticklabels = null;

        _graphics = getGraphics();

        if (_graphics == null) {
            System.out.println("PlotBox::init(): Internal error: " +
                    "_graphic was null, be sure to call show()\n"+
                    "before calling init()");
            return;
        }

        if (_foreground != null) {
            setForeground(_foreground);
        } else {
            _foreground = Color.black;
        }

        if (_background != null) {
            setBackground(_background);
        } else {
            _background = Color.white;
        }
        if (_debug > 6)
            System.out.println("PlotBox: color = "+_foreground+" "+
                    _background);

        // Make a button that auto-scales the plot.
        // NOTE: The button infringes on the title space.
        // If more buttons are added, we may have to find some other place
        // for them, like below the legend, stacked vertically.
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        _fillButton = new Button("fill");
        add(_fillButton);
        validate();

        if (_dataurl != null) {
            parseFile(_dataurl,_documentBase);
        }
    }
        
    /** The minimum size.
     * @deprecated As of JDK1.1 in java.awt.component, but we need 
     * to compile under 1.0.2 for netscape3.x compatibility.
     */
    public Dimension minimumSize() {
        if (_debug > 9)
            System.out.println("PlotBox: minimumSize "+_width+" "+_height);
        return getMinimumSize();
    }

    /**
     * Set the starting point for an interactive zoom box.
     * @deprecated As of JDK1.1 in java.awt.component 
     * but we need to compile under 1.0.2 for netscape3.x compatibility.
     */
    public boolean mouseDown(Event evt, int x, int y) { // deprecated
        // constrain to be in range
        if (_debug > 9) System.out.println("PlotBox: mouseDown "+x+" "+y);
        if (y > _lry) y=_lry;
        if (y < _uly) y=_uly;
        if (x > _lrx) x=_lrx;
        if (x < _ulx) x=_ulx;
        _zoomx = x;
        _zoomy = y;
        return true;
    }
    
    /**
     * Draw a box for an interactive zoom box.
     * Return a boolean indicating whether or not we have dealt with
     * the event.
     * @deprecated As of JDK1.1 in java.awt.component 
     * but we need to compile under 1.0.2 for netscape3.x compatibility.
     */
    public synchronized boolean mouseDrag(Event evt, int x, int y) {
        // We make this method synchronized so that we can draw the drag
        // box properly.  If this method is not synchronized, then
        // we could end up calling setXORMode, being interrupted
        // and having setPaintMode() called in another method.

        if (_debug > 9) System.out.println("PlotBox: mouseDrag "+x+" "+y);

        if (_graphics == null) {
            System.out.println("PlotBox.mouseDrag(): Internal error: " +
                    "_graphic was null, be sure to call init()");
        }

        // Bound the rectangle so it doesn't go outside the box.
        if (y > _lry) y=_lry;
        if (y < _uly) y=_uly;
        if (x > _lrx) x=_lrx;
        if (x < _ulx) x=_ulx;
        // erase previous rectangle, if there was one.
        if ((_zoomx != -1 || _zoomy != -1)) {
            // Ability to zoom out added by William Wu.
            // If we are not already zooming, figure out whether we
            // are zooming in or out.
            if (_zoomin == false && _zoomout == false){
                if (y < _zoomy) {
                    _zoomout = true;
                    // Draw reference box.
                    _graphics.drawRect(_zoomx-15, _zoomy-15, 30, 30);

                } else if (y > _zoomy) {
                    _zoomin = true; 
                }
            }

            if (_zoomin == true){   
                _graphics.setXORMode(_background);
                // Erase the previous box if necessary.
                if ((_zoomxn != -1 || _zoomyn != -1) && (_drawn == true)) {
                    int minx = Math.min(_zoomx, _zoomxn);
                    int maxx = Math.max(_zoomx, _zoomxn);
                    int miny = Math.min(_zoomy, _zoomyn);
                    int maxy = Math.max(_zoomy, _zoomyn);
                    _graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                }
                // Draw a new box if necessary.
                if (y > _zoomy) {
                    _zoomxn = x;
                      _zoomyn = y;
                    int minx = Math.min(_zoomx, _zoomxn);
                    int maxx = Math.max(_zoomx, _zoomxn);
                    int miny = Math.min(_zoomy, _zoomyn);
                    int maxy = Math.max(_zoomy, _zoomyn);
                    _graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                    _graphics.setPaintMode();
                    _drawn = true;
                    return true;
                } else _drawn = false;
            } else if (_zoomout == true){
                _graphics.setXORMode(_background);
                // Erase previous box if necessary.
                if ((_zoomxn != -1 || _zoomyn != -1) && (_drawn == true)) {
                    int x_diff = Math.abs(_zoomx-_zoomxn);
                    int y_diff = Math.abs(_zoomy-_zoomyn);
                    _graphics.drawRect(_zoomx-15-x_diff, _zoomy-15-y_diff,
                            30+x_diff*2, 30+y_diff*2);
                }
                if (y < _zoomy){
                    _zoomxn = x;
                    _zoomyn = y;     
                    int x_diff = Math.abs(_zoomx-_zoomxn);
                    int y_diff = Math.abs(_zoomy-_zoomyn);
                    _graphics.drawRect(_zoomx-15-x_diff, _zoomy-15-y_diff,
                            30+x_diff*2, 30+y_diff*2);
                    _graphics.setPaintMode();
                    _drawn = true;
                    return true;
                } else _drawn = false;
            }
        }
        _graphics.setPaintMode();
        return false;
    }

    /**
     * Zoom in or out based on the box that has been drawn.
     * @deprecated As of JDK1.1 in java.awt.component 
     * but we need to compile under 1.0.2 for netscape3.x compatibility.
     */
    public synchronized boolean mouseUp(Event evt, int x, int y) { //deprecated
        // We make this method synchronized so that we can draw the drag
        // box properly.  If this method is not synchronized, then
        // we could end up calling setXORMode, being interrupted
        // and having setPaintMode() called in another method.

        if (_debug > 9) System.out.println("PlotBox: mouseUp");
        boolean handled = false;
        if ((_zoomin == true) && (_drawn == true)){  
            if (_zoomxn != -1 || _zoomyn != -1) {
                // erase previous rectangle.
                int minx = Math.min(_zoomx, _zoomxn);
                int maxx = Math.max(_zoomx, _zoomxn);
                int miny = Math.min(_zoomy, _zoomyn);
                int maxy = Math.max(_zoomy, _zoomyn);
                _graphics.setXORMode(_background);
                _graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
                _graphics.setPaintMode();
                // constrain to be in range
                if (y > _lry) y=_lry;
                if (y < _uly) y=_uly;
                if (x > _lrx) x=_lrx;
                if (x < _ulx) x=_ulx;
                // NOTE: ignore if total drag less than 5 pixels.
                if ((Math.abs(_zoomx-x) > 5) && (Math.abs(_zoomy-y) > 5)) {
                    double a = _xMin + (_zoomx - _ulx)/_xscale;
                    double b = _xMin + (x - _ulx)/_xscale;
                    if (a < b) setXRange(a, b);
                    else setXRange(b, a);
                    a = _yMax - (_zoomy - _uly)/_yscale;
                    b = _yMax - (y - _uly)/_yscale;
                    if (a < b) setYRange(a, b);
                    else setYRange(b, a);
                }
                drawPlot(_graphics, true);
                handled = true;
            }
        } else if ((_zoomout == true) && (_drawn == true)){
            // Erase previous rectangle.
            _graphics.setXORMode(_background);
            int x_diff = Math.abs(_zoomx-_zoomxn);
            int y_diff = Math.abs(_zoomy-_zoomyn);
            _graphics.drawRect(_zoomx-15-x_diff, _zoomy-15-y_diff,
                    30+x_diff*2, 30+y_diff*2);
            _graphics.setPaintMode();

            // Calculate zoom factor.
            double a = (double)(Math.abs(_zoomx - x)) / 30.0;
            double b = (double)(Math.abs(_zoomy - y)) / 30.0;
            double newx1 = _xMax + (_xMax - _xMin) * a;
            double newx2 = _xMin - (_xMax - _xMin) * a;
            if (newx1 > _xTop) newx1 = _xTop; 
            if (newx2 < _xBottom) newx2 = _xBottom; 
            double newy1 = _yMax + (_yMax - _yMin) * b;
            double newy2 = _yMin - (_yMax - _yMin) * b;
            if (newy1 > _yTop) newy1 = _yTop; 
            if (newy2 < _yBottom) newy2 = _yBottom; 
            setXRange(newx2, newx1);
            setYRange(newy2, newy1);
            drawPlot(_graphics, true);
            handled = true;
        } else if (_drawn == false){
            drawPlot(_graphics, true);
            handled = true;
        }
        _drawn = false;
        _zoomin = _zoomout = false;
        _zoomxn = _zoomyn = _zoomx = _zoomy = -1;
        return handled;
    }

    /** 
     * Paint the component contents, which in this base class is
     * only the axes.
     */
    public void paint(Graphics graphics) {
        if (_debug > 7) System.out.println("PlotBox: paint");
        //super.paint(graphics);
        drawPlot(graphics, true);
    }

    /**
     * Syntactic sugar for parseFile(dataurl, documentBase);
     */ 
    public void parseFile(String dataurl) {
       parseFile(dataurl, (URL)null);
    }

    /**
     * Open up the input file, which could be stdin, a URL or a file.
     * This code can be called from an application, which means that
     * getDocumentBase() might fail.
     */
    public void parseFile(String dataurl, URL documentBase) {
        DataInputStream in = null;
        if (_debug > 2) System.out.println("PlotBox: parseFile("+ dataurl+" "+
                documentBase+") _dataurl = "+_dataurl+" "+_documentBase);
        if (dataurl == null || dataurl.length() == 0) {
            // Open up stdin
            in = new DataInputStream(System.in);
        } else {
            try {
                URL url = null;
                if (documentBase == null && _documentBase != null) {
                    documentBase = _documentBase;
                }
                if (documentBase == null) {
                    url = new URL(_dataurl);
                } else {
                    try {
                        url = new URL(documentBase, dataurl);
                    } catch (NullPointerException e) {
                        // If we got a NullPointerException, then perhaps we
                        // are calling this as an application, not as an applet
                        url = new URL(_dataurl);
                    }
                }
                in = new DataInputStream(url.openStream());
            } catch (MalformedURLException e) {
                try {
                    // Just try to open it as a file.
                    in = new DataInputStream(new FileInputStream(dataurl));
                } catch (FileNotFoundException me) {
                    _errorMsg = new String [2];
                    _errorMsg[0] = "File not found: " + dataurl;
                    _errorMsg[1] = me.getMessage();
                    return;
                } catch (SecurityException me) {
                    _errorMsg = new String [2];
                    _errorMsg[0] = "Security Exception: " + dataurl;
                    _errorMsg[1] = me.getMessage();
                    return;
                }
            } catch (IOException ioe) {
                _errorMsg = new String [2];
                _errorMsg[0] = "Failure opening URL: " + dataurl;
                _errorMsg[1] = ioe.getMessage();
                return;
            }
        }

        _newFile(); // Hook for child classes to do any preprocessing.

        // At this point, we've opened the data source, now read it in
        try {
            if (_binary) {
                _parseBinaryStream(in);
            } else {

                String line = in.readLine(); // FIXME: readLine() is
                // deprecated in JDK1.1, but we need to compile under
                //1.0.2 for netscape3.x compatibility.
                while (line != null) {
                    _parseLine(line);
                    line = in.readLine(); // readLine() is deprecated.
                }
            }
        } catch (MalformedURLException e) {
            _errorMsg = new String [2];
            _errorMsg[0] = "Malformed URL: " + dataurl;
            _errorMsg[1] = e.getMessage();
            return;
        } catch (IOException e) {
            _errorMsg = new String [2];
            _errorMsg[0] = "Failure reading data: " + dataurl;
            _errorMsg[1] = e.getMessage();
        } catch (PlotDataException e) {
            _errorMsg = new String [2];
            _errorMsg[0] = "Incorrectly formatted plot data in " + dataurl;
            _errorMsg[1] = e.getMessage();
        } finally {
            try {
                in.close();
            } catch (IOException me) {}
        }
        
    } 
  
    /** The preferred size.
     * @deprecated As of JDK1.1 in java.awt.component, but we need 
     * to compile under 1.0.2 for netscape3.x compatibility.
     */
    public Dimension preferredSize() {
        if (_debug > 9)
            System.out.println("PlotBox: preferredSize "+_width+" "+_height);
        return getPreferredSize();
    }

    /** Reshape
     */
    public void reshape(int x, int y, int width, int height) {
        if (_debug > 9)
            System.out.println("PlotBox: reshape: "+x+" "+y+" "+
                    width+" "+height);
        _width = width;
        _height = height;
        super.reshape(x,y,_width,_height);
    }

    /** 
     * Resize the plot.
     * @deprecated As of JDK1.1 in java.awt.component, but we need 
     * to compile under 1.0.2 for netscape3.x compatibility.
     */
    public void resize(int width, int height) {
        if (_debug > 8)
            System.out.println("PlotBox: resize"+width+" "+height);
        _width = width;
        _height = height;
        super.resize(width,height); // FIXME: resize() is deprecated.
    }

    /** Set the background color.
     */
    public void setBackground (Color background) {
        _background = background;
        super.setBackground(_background);
    }

    /** Set the debug value.  The higher the integer, the greater the
     * number of debugging messages printed to stdout.  Useful values
     * are 10 - argument parsing, 20 - legend parsing, 101 - data point
     * debugging.
     */
    public void setDebug (int debug ) {
        _debug = debug;
    }

    /** Set the foreground color.
     */
    public void setForeground (Color foreground) {
        _foreground = foreground;
        super.setForeground(_foreground);
    }

    /** Set the binary flag to true if we are reading pxgraph format binary
     * data.
     */
    public void setBinary (boolean binary) {
        _binary = binary;
    }

    /** Set the dataurl.
     */
    public void setDataurl (String dataurl) {
        _dataurl = dataurl;
    }

    /** Set the document base so that we can find the dataurl.
     */
    public void setDocumentBase (URL documentBase) {
        _documentBase = documentBase;
    }

    /** Control whether the grid is drawn.
     */
    public void setGrid (boolean grid) {
        _grid = grid;
    }
    
    /** Set the label font, which is used for axis labels and legend labels.
     */
    public void setLabelFont (String fullfontname) {
        // Can't use Font.decode() here, it is not present in jdk1.0.2
        //_labelfont = Font.decode(fullfontname);

        _labelfont = getFontByName(fullfontname);
    }
    /**
     * Set the title of the graph.  The title will appear on the subsequent
     * call to <code>paint()</code> or <code>drawPlot()</code>.
     */
    public void setTitle (String title) {
        _title = title;
    }
    
    /** Set the title font.
     */
    public void setTitleFont (String fullfontname) {
        // Can't use Font.decode() here, it is not present in jdk1.0.2
        //_titlefont = Font.decode(fullfontname);

        _titlefont = getFontByName(fullfontname);
        _titleFontMetrics = getFontMetrics(_titlefont);
    }

    /** 
     * Set the label for the X (horizontal) axis.  The label will
     * appear on the subsequent call to <code>paint()</code> or
     * <code>drawPlot()</code>.
     */
    public void setXLabel (String label) {
        _xlabel = label;
    }

    /** 
     * Control whether the X axis is drawn with a logarithmic scale.
     */
    public void setXLog (boolean xlog) {
        _xlog = xlog;
    }

    /** 
     * Set the X (horizontal) range of the plot.  If this is not done
     * explicitly, then the range is computed automatically from data
     * available when <code>paint()</code> or <code>drawPlot()</code>
     * are called.  If min and max are identical, then the range is
     * arbitrarily spread by 1.
     */
    public void setXRange (double min, double max) {
        if (_debug > 7) System.out.println("PlotBox: setXRange");
        _xRangeGiven = true;
        _setXRange(min,max);
    }

    /** 
     * Set the label for the Y (vertical) axis.  The label will
     * appear on the subsequent call to <code>paint()</code> or
     * <code>drawPlot()</code>.
     */
    public void setYLabel (String label) {
        _ylabel = label;
    }

    /** 
     * Control whether the Y axis is drawn with a logarithmic scale.
     */
    public void setYLog (boolean ylog) {
        _ylog = ylog;
    }

    /**
     * Set the Y (vertical) range of the plot.  If this is not done
     * explicitly, then the range is computed automatically from data
     * available when <code>paint()</code> or <code>drawPlot()</code>
     * are called.  If min and max are identical, then the range is
     * arbitrarily spread by 0.1.
     */
    public void setYRange (double min, double max) {
        _yRangeGiven = true;
        _setYRange(min,max);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /**
     * Put a mark corresponding to the specified dataset at the
     * specified x and y position.   The mark is drawn in the
     * current color.  In this base class, a point is a
     * filled rectangle 6 pixels across.  Note that marks greater than
     * about 6 pixels in size will not look very good since they will
     * overlap axis labels and may not fit well in the legend.   The
     * <i>clip</i> argument, if <code>true</code>, states
     * that the point should not be drawn if
     * it is out of range.
     */
    protected void _drawPoint(Graphics graphics,
            int dataset, long xpos, long ypos, boolean clip) {
        boolean pointinside = ypos <= _lry && ypos >= _uly && 
            xpos <= _lrx && xpos >= _ulx;
        if (!pointinside && clip) {return;}
        graphics.fillRect((int)xpos-6, (int)ypos-6, 6, 6);
    }

    /** Hook for child classes to do any file preprocessing
     */ 
    protected void _newFile(){
    }

    /**
     * Hook to parse a binary stream.
     * @exception PlotDataException if there is a serious data format problem.
     * @exception java.io.IOException if an I/O error occurs.
     */
    protected void _parseBinaryStream(DataInputStream in) throws
    PlotDataException, IOException {
        throw new PlotDataException("Binary data not supported in the" +
                "baseclass");
    }

    /**
     * Parse a line that gives plotting information.  In this base
     * class, only lines pertaining to the title and labels are processed.
     * Everything else is ignored. Return true if the line is recognized.
     */
    protected boolean _parseLine (String line) {
        // Parse commands in the input file, ignoring lines with
        // syntax errors or unrecognized commands.
        if (_debug > 20) System.out.println("PlotBox: parseLine "+ line);
        // We convert the line to lower case so that the command
        // names are case insensitive.
        String lcLine = new String(line.toLowerCase());
        if (lcLine.startsWith("#")) {
            // comment character
            return true;
        }
        if (lcLine.startsWith("titletext:")) {
            setTitle((line.substring(10)).trim());
            return true;
        }
        if (lcLine.startsWith("xlabel:")) {
            setXLabel((line.substring(7)).trim());
            return true;
        }
        if (lcLine.startsWith("ylabel:")) {
            setYLabel((line.substring(7)).trim());
            return true;
        }
        if (lcLine.startsWith("xrange:")) {
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
        if (lcLine.startsWith("yrange:")) {
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
        if (lcLine.startsWith("xticks:")) {
            // example:
            // XTicks "label" 0, "label" 1, "label" 3
            _parsePairs(line.substring(7), true);
            return true;
        }
        if (lcLine.startsWith("xlog:")) {
            if (lcLine.indexOf("off",5) >= 0) {
                _xlog = false;
            } else {
                _xlog = true;
            }
            return true;
        }
        
        if (lcLine.startsWith("ylog:")) {
            if (lcLine.indexOf("off",5) >= 0) {
                _ylog = false;
            } else {
                _ylog = true;
            }
            return true;
        }
        
        if (lcLine.startsWith("grid:")) {
            if (lcLine.indexOf("off",5) >= 0) {
                _grid = false;
            } else {
                _grid = true;
            }
            return true;
        }
        if (lcLine.startsWith("color:")) {
            if (lcLine.indexOf("off",6) >= 0) {
                _usecolor = false;
            } else {
                _usecolor = true;
            }
            return true;
        }
        return false;
    }

    /** Set the visibility of the Fill button.
     */
    protected void _setButtonsVisibility(boolean vis) {
        // _fillButton.setVisible(vis);
        if (vis) {
            _fillButton.show(); // FIXME: show() is
            // deprecated in JDK1.1, but we need to compile under
            // 1.0.2 for netscape3.x compatibility.
        } else {
            _fillButton.hide(); // FIXME: hide() is
            // deprecated in JDK1.1, but we need to compile under
            // 1.0.2 for netscape3.x compatibility.
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                           protected variables                    ////
    
    // If non-zero, print out debugging messages.  Use setDebug() to set this.
    protected int _debug = 0;
    
    // The graphics context to operate in.  Note that printing will call
    // paint with a different graphics object, so we have to pass this
    // around properly.
    protected Graphics _graphics = null;

    // The range of the data to be plotted.
    protected double _yMax = 0, _yMin = 0, _xMax = 0, _xMin = 0;

    // The factor we pad by so that we don't plot points on the axes.
    protected static final double _PADDING = 0.05;

    // Whether the ranges have been given.
    protected boolean _xRangeGiven = false;
    protected boolean _yRangeGiven = false;
    // The minimum and maximum values registered so far, for auto ranging.
    protected double _xBottom = Double.MAX_VALUE;
    protected double _xTop = - Double.MAX_VALUE;
    protected double _yBottom = Double.MAX_VALUE;
    protected double _yTop = - Double.MAX_VALUE;
    
    // Whether to draw the axes using a logarithmic scale.
    protected boolean _xlog = false, _ylog = false;

    // For use in calculating log base 10.  A log times this is a log base 10.
    protected static final double _LOG10SCALE = 1/Math.log(10);
    
    // Whether to draw a background grid.
    protected boolean _grid = true;
    
    // Color of the background, settable from HTML.
    protected Color _background = null;
    // Color of the foreground, settable from HTML.
    protected Color _foreground = null;

    // Derived classes can increment these to make space around the plot.
    protected int _topPadding = 10;
    protected int _bottomPadding = 5;
    protected int _rightPadding = 10;
    protected int _leftPadding = 10;

    // The plot rectangle in pixels.
    // The naming convention is: "_ulx" = "upper left x", where "x" is
    // the horizontal dimension.
    protected int _ulx = 1 , _uly = 1, _lrx = 100, _lry = 100;

    // Scaling used in plotting points.
    protected double _yscale = 1.0, _xscale = 1.0;
    
    // Indicator whether to use _colors
    protected boolean _usecolor = true;

    // Default _colors, by data set.
    // There are 11 colors so that combined with the
    // 10 marks of the Plot class, we can distinguish 110
    // distinct data sets.
    static protected Color[] _colors = {
        new Color(0xff0000),   // red
        new Color(0x0000ff),   // blue
        new Color(0x14ff14),   // green-ish
        new Color(0x000000),   // black
        new Color(0xffa500),   // orange
        new Color(0x53868b),   // cadetblue4
        new Color(0xff7f50),   // coral
        new Color(0x55bb2f),   // dark green-ish
        new Color(0x90422d),   // sienna-ish
        new Color(0xa0a0a0),   // grey-ish
        new Color(0x00aaaa),   // cyan-ish
    };
        
    // Width and height of component in pixels.
    protected int _width = 400, _height = 400;

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /**
     * Draw the legend in the upper right corner and return the width
     * (in pixels)  used up.  The arguments give the upper right corner
     * of the region where the legend should be placed.
     */
    private int _drawLegend(Graphics graphics, int urx, int ury) {
        // FIXME: consolidate all these for efficiency
        graphics.setFont(_labelfont);
        int spacing = _labelFontMetrics.getHeight();

        Enumeration v = _legendStrings.elements();
        Enumeration i = _legendDatasets.elements();
        int ypos = ury + spacing;
        int maxwidth = 0;
        while (v.hasMoreElements()) {
            String legend = (String) v.nextElement();
            // NOTE: relies on _legendDatasets having the same num. of entries.
            int dataset = ((Integer) i.nextElement()).intValue();
            if (dataset >= 0) {
                if (_usecolor) {
                    // Points are only distinguished up to the number of colors
                    int color = dataset % _colors.length;
                    graphics.setColor(_colors[color]);
                }
                _drawPoint(graphics, dataset, urx-3, ypos-3, false);

                graphics.setColor(_foreground);
                int width = _labelFontMetrics.stringWidth(legend);
                if (width > maxwidth) maxwidth = width;
                graphics.drawString(legend, urx - 15 - width, ypos);
                ypos += spacing;
            }
        }
        return 22 + maxwidth;  // NOTE: subjective spacing parameter.
    }

    /*
     * Return the number as a String for use as a label on a
     * logarithmic axis.
     * Since this is a log plot, number passed in will not have too many
     * digits to cause problems.
     * If the number is an integer, then we print 1e<num>.
     * If the numer is not an integer, then print only the fractional
     * components.
     */
    private String _formatLogNum (double num, int numfracdigits) {
        String results;
        int exponent = (int)num;

        // Determine the exponent, prepending 0 or -0 if necessary.
        if (exponent >= 0 && exponent < 10) {
            results = "0" + exponent;
        } else {
            if (exponent < 0 && exponent > -10) {
                results = "-0" + (-exponent);
            } else {
                results = Integer.toString(exponent);
            }
        }

        // Handle the mantissa.
        if (num >= 0.0 ) {
            if (num - (int)(num) < 0.001) {
                results = "1e" + results;
            } else {
                results = _formatNum(Math.pow(10.0,(num - (int)num)),
                        numfracdigits);
            }
        } else {
            if (-num - (int)(-num) < 0.001) {
                results = "1e" + results;
            } else {
                results = _formatNum(Math.pow(10.0,(num - (int)num))*10,
                        numfracdigits);
            }
        }
        if (_debug == 5) 
            System.out.println("PlotBox: _formatLogNum: "+num+" "+
                    Math.pow(10.0,num)+" "+results+" "+(num -(int)num));
        return results;
    }

    /*
     * Return a string for displaying the specified number
     * using the specified number of digits after the decimal point.
     * NOTE: This could be replaced by the NumberFormat class
     * which is available in jdk 1.1.  We don't do this now so that
     * it will run on today's browsers, which use jdk 1.0.2.
     */
    private String _formatNum (double num, int numfracdigits) {
        // First, round the number. 
        double fudge = 0.5;
        if (num < 0.0) fudge = -0.5;
        String numString = Double.toString(num +
                fudge*Math.pow(10.0, -numfracdigits));
        // Next, find the decimal point.
        int dpt = numString.lastIndexOf(".");
        StringBuffer result = new StringBuffer();
        if (dpt < 0) {
            // The number we are given is an integer.
            if (numfracdigits <= 0) {
                // The desired result is an integer.
                result.append(numString);
                return result.toString();
            }
            // Append a decimal point and some zeros.
            result.append(".");
            for (int i = 0; i < numfracdigits; i++) {
                result.append("0");
            }
            return result.toString();
        } else {
            // There are two cases.  First, there may be enough digits.
            int shortby = numfracdigits - (numString.length() - dpt -1);
            if (shortby <= 0) {
                int numtocopy = dpt + numfracdigits + 1;
                if (numfracdigits == 0) {
                    // Avoid copying over a trailing decimal point.
                    numtocopy -= 1;
                }
                result.append(numString.substring(0, numtocopy));
                return result.toString();
            } else {
                result.append(numString);
                for (int i = 0; i < shortby; i++) {
                    result.append("0");
                }
                return result.toString();                
            }
        }
    }
 
    /*
     * Determine what values to use for log axes.
     * Based on initGrid() from xgraph.c by David Harrison.
     */
    private Vector _gridInit(double low, double step) {
        double ratio = Math.pow(10.0, step);
        double x = 0;
        // Vector containing axes tick values that we return.
        Vector grid = new Vector(101); // 101 comes from xgraph.c

        // How log axes work:
        // _gridInit() creates a vector with the values to use for the
        // log axes.  For example, the vector might contain
        // {0.0 0.301 0.698}, which could correspond to
        // axis labels {1 1.2 1.5 10 12 15 100 120 150}
        //
        // _gridStep() gets the proper value.  _gridInit is cycled through
        // for each integer log value.
        //
        // To turn on debugging messages for the log axes, set _debug to 5.
        //
        // Bugs in log axes:
        // * Sometimes not enough grid lines are displayed because the
        // region is small.  This bug is present in the original xgraph
        // binary, which is the basis of this code.  The problem is that
        // as ratio gets closer to 1.0, we need to add more and more
        // grid marks.

        _gridCurJuke = 0;
        grid.addElement(new Double(0.0));

	_gridBase = Math.floor(low);
	
	if (ratio <= 3.0) {
	    if (ratio > 2.0) {
                grid.addElement(new Double(_LOG10SCALE*Math.log(3.0)));
	    } else if (ratio > 1.333) {
                grid.addElement(new Double(_LOG10SCALE*Math.log(2.0)));
                grid.addElement(new Double(_LOG10SCALE*Math.log(5.0)));
	    } else if (ratio > 1.25) {
                grid.addElement(new Double(_LOG10SCALE*Math.log(1.5)));
                grid.addElement(new Double(_LOG10SCALE*Math.log(2.0)));
                grid.addElement(new Double(_LOG10SCALE*Math.log(3.0)));
                grid.addElement(new Double(_LOG10SCALE*Math.log(5.0)));
                grid.addElement(new Double(_LOG10SCALE*Math.log(7.0)));
	    } else {
                // FIXME: If ratio is close to 1.0, then these values are wrong
		for (x = 1.0; x < 10.0 && (x+.5)/(x+.4) >= ratio; x += .5) {
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + .1)));
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + .2)));
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + .3)));
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + .4)));
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + .5)));
		}
		if (Math.floor(x) != x) {
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x += .5)));
                }
		for ( ; x < 10.0 && (x+1.0)/(x+.5) >= ratio; x += 1.0) {
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + .5)));
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + 1.0)));
		}
		for ( ; x < 10.0 && (x+1.0)/x >= ratio; x += 1.0) {
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + 1.0)));
		}
		if (x == 7.0) {
		    x = 6.0;
                    grid.removeElement(grid.lastElement());
		}
		if (x < 7.0) {
                    grid.addElement(new Double(_LOG10SCALE*Math.log(x + 2.0)));
		}
		if (x == 10.0) {
                    grid.removeElement(grid.lastElement());
                }
	    }
	    x = low - _gridBase;
            // Set gridCurJuke so that the value in grid is greater than
            // or equal to x.  This sets us up to process the first point.
            for (_gridCurJuke = -1;
                 (_gridCurJuke+1) < grid.size() && x >= 
                     ((Double)grid.elementAt(_gridCurJuke+1)).doubleValue();
                 _gridCurJuke++){
            }
	}
        if (_debug == 5 )
            System.out.println("PlotBox: _gridInit("+low+","+step+
                    ": ratio = "+ratio+" _gridCurJuke = "+_gridCurJuke+
                    " grid.size()"+grid.size()+grid.toString());
        return grid;
    }

    /*
     * Used to find the next value for the axis label.
     * For non-log axes, we just return pos + step.
     * For log axes, we read the appropriate value in the grid Vector,
     * add it to _gridBase and return the sum.  We also take care
     * to reset _gridCurJuke if necessary.
     * Note that for log axes, _gridInit() must be called before
     * calling _gridStep().
     * Based on stepGrid() from xgraph.c by David Harrison.
     */
    private double _gridStep(Vector grid, double pos, double step,
            boolean logflag) {
        if (logflag) {
            if (++_gridCurJuke >= grid.size()) {
                _gridCurJuke = 0;
                _gridBase+=Math.ceil(step);
                if (_debug == 5)
                    System.out.println("PlotBox: _gridStep: pos = "+pos+
                            " _roundUp("+step+") = "+_roundUp(step));
            }
            if (_debug == 5)
                    System.out.println("PlotBox: _gridStep: pos="+pos+
                            " step="+step+" "+" _gridCurJuke="+
                            _gridCurJuke+" results="+_gridBase+"+"+
                          ((Double)grid.elementAt(_gridCurJuke)).doubleValue());
            return _gridBase +
                ((Double)grid.elementAt(_gridCurJuke)).doubleValue();
        } else {
            return pos + step;
        }
    }


    /*
     * Measure the various fonts.  
     */
    private void _measureFonts() {
        // We only measure the fonts once, and we do it from addNotify().
        if (_labelfont == null)  
            _labelfont = new Font("Helvetica", Font.PLAIN, 12);
        if (_superscriptfont == null)  
            _superscriptfont = new Font("Helvetica", Font.PLAIN, 9);
        if (_titlefont == null)  
            _titlefont = new Font("Helvetica", Font.BOLD, 14);

        _labelFontMetrics = getFontMetrics(_labelfont);
        _superscriptFontMetrics = getFontMetrics(_superscriptfont);
        _titleFontMetrics = getFontMetrics(_titlefont);
    }

    /*
     * Return the number of fractional digits required to display the
     * given number.  No number larger than 15 is returned (if
     * more than 15 digits are required, 15 is returned).
     */
    private int _numFracDigits (double num) {
        int numdigits = 0;
        while (numdigits <= 15 && num != Math.floor(num)) {
            num *= 10.0;
            numdigits += 1;
        }
        return numdigits;
    }
 
    /*
     * Return the number of integer digits required to display the
     * given number.  No number larger than 15 is returned (if
     * more than 15 digits are required, 15 is returned).
     */
    private int _numIntDigits (double num) {
        int numdigits = 0;
        while (numdigits <= 15 && (int)num != 0.0) {
            num /= 10.0;
            numdigits += 1;
        }
        return numdigits;
    }

    /*
     * Parse a string of the form: "word num, word num, word num, ..."
     * where the word must be enclosed in quotes if it contains spaces,
     * and the number is interpreted as a floating point number.  Ignore
     * any incorrectly formatted fields.  I <i>xtick</i> is true, then
     * interpret the parsed string to specify the tick labels on the x axis.
     * Otherwise, do the y axis.
     */
    private void _parsePairs (String line, boolean xtick) {    
        int start = 0;
        boolean cont = true;
        while (cont) {
            int comma = line.indexOf(",", start);
            String pair = null ;
            if (comma > start) {
                pair = (line.substring(start,comma)).trim();
            } else {
                pair = (line.substring(start)).trim();
                cont = false;
            }
            int close = -1;
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
        int exponent = (int) Math.floor(Math.log(val)*_LOG10SCALE);
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
        //if (_xRangeGiven) {
            // The user specified the range, so don't pad.
        //_xMin = min;
        //  _xMax = max;
        //} else {
            // Pad slightly so that we don't plot points on the axes.
            _xMin = min - ((max - min) * _PADDING);
            _xMax = max + ((max - min) * _PADDING);
            //}

        // Find the exponent.
        double largest = Math.max(Math.abs(_xMin),Math.abs(_xMax));
        _xExp = (int) Math.floor(Math.log(largest)*_LOG10SCALE);
        // Use the exponent only if it's larger than 1 in magnitude.
        if (_xExp > 1 || _xExp < -1) {
            double xs = 1.0/Math.pow(10.0,(double)_xExp);
            _xtickMin = _xMin*xs;
            _xtickMax = _xMax*xs;
        } else {
            _xtickMin = _xMin;
            _xtickMax = _xMax;
            _xExp = 0;
        }
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
        //        if (_yRangeGiven) {
            // The user specified the range, so don't pad.
        //            _yMin = min;
        //            _yMax = max;
        //        } else {
            // Pad slightly so that we don't plot points on the axes.
            _yMin = min - ((max - min) * _PADDING);
            _yMax = max + ((max - min) * _PADDING);
            //        }

        // Find the exponent.
        double largest = Math.max(Math.abs(_yMin),Math.abs(_yMax));
        _yExp = (int) Math.floor(Math.log(largest)*_LOG10SCALE);
        // Use the exponent only if it's larger than 1 in magnitude.
        if (_yExp > 1 || _yExp < -1) {
            double ys = 1.0/Math.pow(10.0,(double)_yExp);
            _ytickMin = _yMin*ys;
            _ytickMax = _yMax*ys;
        } else {
            _ytickMin = _yMin;
            _ytickMax = _yMax;
            _yExp = 0;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The URL to be opened.
    private String _dataurl = null;

    // The document base we use to find the _dataurl.
    private URL _documentBase = null;

    // Set to true if we are reading in pxgraph format binary data.
    private boolean _binary = false;

    // The range of the plot as labeled (multiply by 10^exp for actual range.
    private double _ytickMax = 0.0, _ytickMin = 0.0,
        _xtickMax = 0.0 , _xtickMin = 0.0 ;
    // The power of ten by which the range numbers should be multiplied.
    private int _yExp = 0, _xExp = 0;

    // Scaling used in making tick marks
    private double _ytickscale = 0.0, _xtickscale = 0.0;

    // Font information.
    private Font _labelfont = null, _superscriptfont = null,
        _titlefont = null;
    private FontMetrics _labelFontMetrics = null, 
        _superscriptFontMetrics = null,
        _titleFontMetrics = null;

    // Used for log axes. Index into vector of axis labels.
    private int _gridCurJuke = 0;

    // Used for log axes.  Base of the grid.
    private double _gridBase = 0.0;

    // An array of strings for reporting errors.
    private String _errorMsg[];
    
    // The title and label strings.
    private String _xlabel, _ylabel, _title;
    
    // Legend information.
    private Vector _legendStrings = new Vector();
    private Vector _legendDatasets = new Vector();
    
    // If XTicks or YTicks are given
    private Vector _xticks = null, _xticklabels = null,
        _yticks = null, _yticklabels = null;

    // A button for filling the plot
    private Button _fillButton = null;
    
    // Variables keeping track of the interactive zoom box.
    // Initialize to impossible values.
    private int _zoomx = -1;
    private int _zoomy = -1;
    private int _zoomxn = -1;
    private int _zoomyn = -1;

    // Control whether we are zooming in or out.
    private boolean _zoomin = false;
    private boolean _zoomout = false;
    private boolean _drawn = false;
}

/* Appletable IIRfilter 


@Copyright (c) 1997-1998 The Regents of the University of California.
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
@Author: William Wu 

@Version: @(#)PlotApplet.java	1.13    01/11/98
$Id$\t$Date$
*/
package ptolemy.filter.applet;

import ptolemy.filter.controller.*;
import ptolemy.filter.view.*;

import java.applet.Applet;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// FiltApplet
/** Create an Applet that can design IIR filter. 
 *  This uses ptfilter.top package's Manager class, as a private
 *  variables.  It also allocate the spaces to put all the filter window.
 * 
 * @author William Wu 
 */
public class FiltApplet extends Applet implements Runnable {

    /** Return a string describing this applet.
     */
    public String getAppletInfo() {
        return "IIRApplet : A IIR filter design tool.\n" +
            "By: William Wu, wbwu@eecs.berkeley.edu and\n " +
            "4/23/98)";
    }

    /** Return information about parameters.
     */
    public String[][] getParameterInfo () {
        String pinfo[][] = {
            {"background", "hexcolor value", "background color"},
            {"foreground", "hexcolor value", "foreground color"},
        };
        return pinfo;
    }

    /**
     * Initialize the applet.  Read the applet parameters.
     */
    public void init() {
        if (_debug > 8) System.out.println("IIRApplet: init");
//        int width,height;
        String type;
        setLayout(new FlowLayout(5,5,5));

        if ( manager() == null) {
            newManager();
        }

        show();

        try {
            type = getParameter("type");
        } catch (NullPointerException e) {
            type = "IIR";
        }

//        try {
//            width = Integer.valueOf(getParameter("width")).intValue();
//        } catch (NullPointerException e) {
//            width = 400;
//        }

//        try {
//            height = Integer.valueOf(getParameter("height")).intValue();
//        } catch (NullPointerException e) {
//            height = 400;
//        }

//        plot().resize(width,height);

        // Process the background parameter.
//        try {
//            Color background = Color.white;
//            background = PlotBox.getColorByName(getParameter("background"));
//            setBackground(background);
//            plot().setBackground(background);
//       } catch (NullPointerException e) {}

        // Process the foreground parameter.
//        try {
//            Color foreground = Color.white;
//            foreground = PlotBox.getColorByName(getParameter("foreground"));
//            setForeground(foreground);
//            plot().setForeground(foreground);
//        } catch (NullPointerException e) {}

        // Process the dataurl parameter.
//        String dataurl = null;
//        try {
//            dataurl = getParameter("dataurl");
//            plot().setDataurl(dataurl);
//        } catch (NullPointerException e) {}

        
        
        if (type.equals("IIR")) { // IIR filter
             manager().newFilter(1, "Filter Applet");
             IIRsetPanel = manager().getIIRSetView().getPanel();
             this.add(IIRsetPanel);
        } else if (type.equals("Blank")) { // Blank filter
             manager().newFilter(0, "Filter Applet");
        }   

        freqPanel = manager().getFreqView().getPanel();
        impulsPanel = manager().getImpulsView().getPanel();
        polezeroPanel = manager().getPoleZeroView().getPanel();

         
//        _quit = new Button("quit");
//        this.add(_quit);
        
        this.add(polezeroPanel);
        this.add(freqPanel);
        this.add(impulsPanel);

        manager().getFreqView().initPlots();
        manager().getImpulsView().initPlots();
        manager().getPoleZeroView().initPlots();

System.out.println("before show");

        freqPanel.show();
        impulsPanel.show();
        polezeroPanel.show();
        super.init();
    }

    /** Create a new Plot object to operate on.  Derived classes can
     * redefine this method to create multiple Plot objects.
     */
    public void newManager() {
        _myManager = new Manager(1);
    }
        

    /** Paint the screen with our plot.
     */
    public void paint(Graphics graphics) {
        if (_debug > 8) System.out.println("FilterApplet: paint");
        if (impulsPanel!=null) impulsPanel.paint(graphics);
        if (freqPanel!=null) freqPanel.paint(graphics);
        if (polezeroPanel!=null) polezeroPanel.paint(graphics);
        if (IIRsetPanel!=null) IIRsetPanel.paint(graphics);
    }

    /** Return the Plot object to operate on.
     */  
    public Manager manager() {
        return _myManager;
    }

    public void run() {
        if (_debug > 8) System.out.println("FilterApplet: run");
	repaint();
    }

    /** Start the plot.
     */
    public void start () {
        if (_debug > 8) System.out.println("FilterApplet: start");
	_filterThread = new Thread(this);
        _filterThread.start();
        super.start();
    }

    /**
     * Close all the pop window, and delete filter
     * doesn't really quit the applet
     */
    public boolean action(Event evt, Object arg){
        if (evt.target == _quit){
             return manager().deletefilter();
        }
        return false;
    }

    /** Stop the plot.
     */
    public void stop () {
        if (_debug > 8) System.out.println("FilterApplet: stop");
        _filterThread.stop();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    // If non-zero, print out debugging messages.
    // See also the _debug declared in PlotBox.
    protected int _debug = 0;

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private Manager _myManager;
    private Thread _filterThread;
    private Panel impulsPanel = null; 
    private Panel freqPanel = null; 
    private Panel polezeroPanel = null; 
    private Panel IIRsetPanel = null; 
    private Button _quit;

    public static void main(String[] args){
          Frame f = new Frame("Filter Applet");
          f.resize(1000, 800);
          FiltApplet fa = new FiltApplet(); 
          f.add(fa);
          f.show(); 
          fa.show();
          fa.init();
          fa.start(); 
    }
}

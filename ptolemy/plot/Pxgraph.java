/* Java implementation of the pxgraph plotting program

 Copyright (c) 1997 The Regents of the University of California.
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
*/

package plot;

import java.awt.Button;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.Thread;
import java.lang.InterruptedException; 

import java.net.URL;
import java.net.MalformedURLException;

/* A class thrown by Pxgraph */
class CmdLineArgException extends Throwable {

  public CmdLineArgException() { super(); }
  public CmdLineArgException(String s) { super(s); }
 
}


//////////////////////////////////////////////////////////////////////////
//// Pxgraph
/** 
 * Java implementation of the Unix X Windows xgraph plotter program.
 * 
 * This class is a Java Application that uses the Plot Java Applet to
 * simulate the <code>pxgraph</code> X Windows system program.  
 * <code>pxgraph</code> takes a number of command line arguments, 
 * type <code>pxgraph -help</code> to see them.
 *
 * The <code>pxgraph</code> script is a Bourne shell script that
 * attempts to call java with the proper environment.
 *
 * @author Christopher Hylands
 * @version $Id$
 * @see Plot
 */
public class Pxgraph extends Frame { 

    /** Constructor
     */	
    public Pxgraph(int maxdatasets) {
	//        setLayout(new FlowLayout(FlowLayout.RIGHT));
	//	_exitButton = new Button();
	//	_exitButton.setLabel("Exit");
	//	add(_exitButton);
	// Initialize the array that contains the dataset descriptors.
	_datasets = new String[maxdatasets];


    }

    //    public boolean action(Event e, Object arg) {
    //	Object target = e.target;
    //	if (target == _exitButton) {
    //	    System.exit(1);
    //	    return true;
    //	} else {
    //            return super.action (e, arg);
    //	}
    //    }

    /** handle an event.
     * @deprecated As of JDK1.1 in java.awt.component 
     * but we need to compile under 1.0.2 for netscape3.x compatibility.
     */
    public boolean handleEvent(Event e) {
        switch (e.id) {
          case Event.WINDOW_ICONIFY:
	      //stopAnimation();
            break;
          case Event.WINDOW_DEICONIFY:
	      //startAnimation();
            break;
          case Event.WINDOW_DESTROY:
            System.exit(0);
            break;
        }  

        return super.handleEvent(e); // FIXME: handleEvent is
 	// deprecated in 1.1, we should use processEvent(),
	// However, we need to compile under 1.0.2 for compatibility with
	// netscape3.x so we stick with handleEvent().
    }

    /** Parse the command line arguments, do any preprocessing, then plot.
      * If you have the <code>pxgraph</code> shell script, then 
      * type <code>pxgraph -help</code> for the complete set of arguments.
      */
    public static void main(String arg[]) {
        int argsread = 0, i;
	Plot plotApplet = new Plot();
	Pxgraph pxgraph = new Pxgraph(plotApplet.getMaxDataSets());

	pxgraph.pack();
	pxgraph.add(plotApplet);

	try {
	    argsread = pxgraph._parseArgs(plotApplet, arg);
	} catch (CmdLineArgException e) {
	    System.err.println("Failed to parse command line arguments: "
			       + e);
	    System.exit(1);
	}

        pxgraph.show();
	plotApplet.init();
        for(i = argsread+1; i < arg.length; i++) {
            if (_debug) System.out.println(arg[i]);
            //plotApplet.parseFile(arg[i]);
        }

        
	plotApplet.start();

	if (_test) {
	    if (_debug) System.out.println("Sleeping for 2 seconds");
	    try {
		Thread.currentThread().sleep(2000);
	    }
	    catch (InterruptedException e) {
	    }
	    System.exit(0);
	}
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////


    /* help - print out help
     */	
    private void _help () {
	// We use a table here to keep things neat.
	// If we have:
	//  {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
	// -bd       - The argument
	// <color>   - The description of the value of the argument
	// Border    - The Xgraph file directive (not supported at this time).
	// White     - The default (not supported at this time)
	// "(Unsupported)" - The string that is printed to indicate if
	//                   a option is unsupported.
	String commandOptions[][] = {
	    {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
	    {"-bg",  "<color>", "BackGround",  "White", "(Unsupported)"},
	    {"-brb", "<base>", "BarBase",  "0", "(Unsupported)"},
	    {"-brw", "<width>", "BarWidth",  "1", ""},
	    {"-bw",  "<size>", "BorderSize",  "1", "(Unsupported)"},
	    {"-fg",  "<color>", "Foreground",  "Black", "(Unsupported)"},
	    {"-gw",  "<pixels>", "GridStyle",  "1", "(Unsupported)"},
	    {"-lf",  "<fontname>", "LabelFont",  "helvetica-12", "(Unsupported)"},
	    {"-lw",  "<width>", "LineWidth",  "0", "(Unsupported)"},
	    {"-lx",  "<xl,xh>", "XLowLimit, XHighLimit",  "0", ""},
	    {"-ly",  "<yl,yh>", "YLowLimit, YHighLimit",  "0", ""},
	    {"-t",   "<title>", "TitleText",  "An X Graph", ""},
	    {"-tf",  "<fontname>", "TitleFont",  "helvetica-18", "(Unsupported)"},
	    {"-x",   "<unitName>", "XUnitText",  "X", ""},
	    {"-y",   "<unitName>", "YUnitText",  "Y", ""},
	    {"-zg",  "<color>", "ZeroColor",  "Black", "(Unsupported)"},
	    {"-zw",  "<width>", "ZeroWidth",  "0", "(Unsupported)"},
	};

	String commandFlags[][] = {
	    {"-bar", "BarGraph",  ""},
	    {"-bb", "BoundBox",  "(Unsupported)"},
	    {"-binary", "Binary",  ""},
	    {"-db", "Debug",  ""},
	    // -help is not in the original X11 pxgraph.
	    {"-help", "Help",  ""},
	    {"-lnx", "LogX",  "(Unsupported)"},
	    {"-lny", "LogY",  "(Unsupported)"},
	    {"-m", "Markers",  ""},
	    {"-M", "StyleMarkers",  ""},
	    {"-nl", "NoLines",  ""},
	    {"-p", "PixelMarkers",  ""},
	    {"-P", "LargePixel",  ""},
	    {"-rv", "ReverseVideo",  ""},
	    // -test is not in the original X11 pxgraph.  We use it for testing
	    {"-test", "Test",  ""},
	    {"-tk", "Ticks",  ""},
	};
	int i;
	System.out.println("Usage: pxgraph [ options ] [=WxH+X+Y] [file ...]");
	System.out.println(" options that take values as second args:");
	for(i=0; i < commandOptions.length; i++) {
	    System.out.println(" " + commandOptions[i][0] +
			       " " + commandOptions[i][1] +
			       " " + commandOptions[i][4] );
	}
	System.out.println(" Boolean flags:");
	for(i=0; i < commandFlags.length; i++) {
	    System.out.println(" " + commandFlags[i][0] +
			       " " + commandFlags[i][2]);
	}
	System.out.println("The following pxgraph features are not supported:");
	System.out.println(" * Directives in pxgraph input files");
	System.out.println(" * Xresources");
	System.out.println(" * -<digit> dataset_description");
	System.out.println(" * More than one input file");
	System.exit(1);
    }

    /* Parse the arguments and make calls to the plotApplet accordingly.
     */	
    private int _parseArgs(Plot plotApplet, String args[])
	throws CmdLineArgException
	{
        int i = 0, j, argsread;
        String arg;
	String unsupportedOptions[] = {
	    "-bd", "-bg", "-brb", "-bw", "-fg", "-gw", "-lf", "-lw",
	    "-tf", "-zg", "-zw"
	};
	String unsupportedFlags[] = {
	    "-bb", "-lnx", "-lny", "-rv"
	};
	// Default URL to be opened
	String dataurl = "";

	String title = "A plot";
	int width = 400;      // Default width of the graph
	int height = 400;     // Default height of the graph


        while (i < args.length && (args[i].startsWith("-") || 
            args[i].startsWith("=")) ) {
            arg = args[i++];
	    if (_debug) System.out.print("arg = " + arg + "\n");

	    if (arg.startsWith("-")) {
		// Search for unsupported options that take arguments
		boolean badarg = false;
		for(j = 0; j < unsupportedOptions.length; j++) {
		    if (arg.equals(unsupportedOptions[j])) {
			System.err.println("pxgraph: " + arg +
					   " is not yet supported");
			i++;
			badarg = true;
		    }
		}
		if (badarg) continue;
		// Search for unsupported boolean flags
		for(j = 0; j < unsupportedFlags.length; j++) {
		    if (arg.equals(unsupportedFlags[j])) {
			System.err.println("pxgraph: " + arg +
					   " is not yet supported");
			badarg = true;
		    }

		}
		if (badarg) continue;

		if (arg.equals("-brw")) {
		    // -brw <width> BarWidth Bars: 
		    if (!plotApplet.parseLine("Bars: " + args[i++])) {
			throw new 
			    CmdLineArgException("Failed to parse `"+arg+"'");
		    }
		    continue;
		} else if (arg.equals("-lx")) {
		    // -lx <xl,xh> XLowLimit, XHighLimit  XRange: 
		    if (!plotApplet.parseLine("XRange: " + args[i++])) {
			throw new 
			    CmdLineArgException("Failed to parse `"+arg+"'");
		    }
		    continue;
		} else if (arg.equals("-ly")) {
		    // -ly <yl,yh> YLowLimit, YHighLimit  YRange: 
		    if (!plotApplet.parseLine("YRange: " + args[i++])) {
			throw new 
			    CmdLineArgException("Failed to parse `"+arg+"'");
		    }
		    continue;
		} else if (arg.equals("-t")) {
		    // -t <title> TitleText "An X Graph"
		    title =  args[i++];
		    continue;
		} else if (arg.equals("-x")) {
		    // -x <unitName> XUnitText XLabel:
		    plotApplet.setXLabel(args[i++]); 
		    continue;
		} else if (arg.equals("-y")) {
		    // -y <unitName> YUnitText YLabel:
		    plotApplet.setYLabel(args[i++]); 
		    continue;		    
		} else if (arg.equals("-bar")) {
		    //-bar BarGraph Bars: on Marks: none Lines: off
		    plotApplet.setBars(true); 
		    plotApplet.setMarksStyle("none");
		    plotApplet.setConnected(false);
		    continue;
		} else if (arg.equals("-binary")) {
		    plotApplet.setBinary(true);
		    continue;
		} else if (arg.equals("-db")) {
		    _debug = true;
		    continue;
		} else if (arg.equals("-debug")) {
		    // -debug is not in the original X11 pxgraph.
		    _debug = true;
		    continue;
		} else if (arg.equals("-help")) {
		    // -help is not in the original X11 pxgraph.
		    _help();
		    continue;
		} else if (arg.equals("-m")) {
		    // -m Markers Marks: various
		    plotApplet.setMarksStyle("various");
		    continue;
		} else if (arg.equals("-M")) {
		    // -M StyleMarkers Marks: various
		    plotApplet.setMarksStyle("various");
		    continue;
		} else if (arg.equals("-nl")) {
		    // -nl NoLines Lines: off
		    plotApplet.setConnected(false);
		    continue;
		} else if (arg.equals("-p")) {
		    // -p PixelMarkers Marks: points
		    plotApplet.setMarksStyle("points");
		    continue;
		} else if (arg.equals("-P")) {
		    // -P LargePixel Marks: dots\n 
		    plotApplet.setMarksStyle("dots");
		    continue;
		} else if (arg.equals("-test")) {
		    // -test is not in the original X11 pxgraph.
		    _test = true;
		    continue;
		} else if (arg.equals("-tk")) {
		    plotApplet.setGrid(false);
		    continue;
		} if (arg.length() > 1  && arg.charAt(0) == '-') {
		    // Process '-<digit> <datasetname>'
		    try {
			Integer datasetnumberint = new
			    Integer(arg.substring(1));
			int datasetnumber = datasetnumberint.intValue();
			if (datasetnumber >= 0 &&
			    datasetnumber <= plotApplet.getMaxDataSets()) {
			    // Save the next arg in the dataset array
			    _datasets[datasetnumber] = args[i++];
			    if (_debug)
				System.out.println("dataset " + datasetnumber
						 + " = " +
						 _datasets[datasetnumber]);
			    continue;
			}
		    } catch (NumberFormatException e) {
		    }
		}
	    } else if (arg.startsWith("=")) {
		// Process =WxH+X+Y
                int endofheight;
                width = (int)Integer.valueOf(arg.substring(1,
					   arg.indexOf('x'))).intValue();
                if (arg.indexOf('+') != -1) {
                   height = 
		       (int)Integer.valueOf(arg.substring(arg.indexOf('x')+1,
					  arg.indexOf('+'))).intValue();
                } else {
                    if (arg.length() > arg.indexOf('x')) {
                        height =
			    Integer.valueOf(arg.substring(arg.indexOf('x')+1,
					  arg.length())).intValue();
                    }
                }
		// FIXME: need to handle X and Y in =WxH+X+Y
		continue;

            }
	    // If we got to here, then we failed to parse the arg 
	    throw new 
		CmdLineArgException("Failed to parse `" + arg + "'");
	}
        if (i < args.length) {
            dataurl=args[i];
	}
        argsread = i++;
	// Now we call methods in the Plot applet and the Frame
	// according to the defaults and the values that over rode them
	plotApplet.setDataurl(dataurl); // Set the dataurl in PlotBox
	plotApplet.setTitle(title);

	// Set up the frame
	resize(width,height); 	// FIXME: resize is deprecated in 1.1,
	                        // we should use setsize(width,height)
				// but setsize is not in JDK1.0.2
	setTitle(title);

        if (_debug) {
	    System.err.println("dataurl = " + dataurl);
	    System.err.println("title= " + title);
	    System.err.println("width = " + width + " height = " + height);
	}
        return argsread;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private Button _exitButton;

    // For debugging, call with -db or -debug.
    private static boolean _debug = false;

    // If true, then auto exit after a few seconds.
    private static boolean _test = false;

    private String _datasets[];
}

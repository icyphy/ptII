/* Java implementation of the X11 pxgraph plotting program

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
    public Pxgraph() {
	//        setLayout(new FlowLayout(FlowLayout.RIGHT));
	//	_exitButton = new Button();
	//	_exitButton.setLabel("Exit");
	//	add(_exitButton);
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
    public static void main(String args[]) {
        int argsread = 0, i;
	Plot plotApplet = new Plot();
	Pxgraph pxgraph = new Pxgraph();

	pxgraph.pack();
	pxgraph.add(plotApplet);

	try {
	    // First we parse the args for things like -help or -version
	    // then we have the Plot applet parse them
	    pxgraph._parseArgs(args);
	    argsread = plotApplet.parseArgs(args);
	} catch (CmdLineArgException e) {
	    System.err.println("Failed to parse command line arguments: "
			       + e);
	    System.exit(1);
	}

        pxgraph.show();
	plotApplet.init();

        for(i = argsread+1; i < args.length; i++) {
            if (_debug > 0) System.out.println(args[i]);
            plotApplet.parseFile(args[i]);
        }
        
	plotApplet.start();

	if (_test) {
	    if (_debug > 4) System.out.println("Sleeping for 2 seconds");
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
	// FIXME: we should bring up a dialog box or something.
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
	    // -v is not in the original X11 pxgraph.
	    {"-v", "Version",  ""},
	    {"-version", "Version",  ""},
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
	System.out.println(" * More than one input file");
	System.exit(1);
    }

    /* Parse the arguments and make calls to the plotApplet accordingly.
     */	
    private int _parseArgs(String args[])
	{
        int i = 0, j, argsread;
        String arg;

	// Default URL to be opened
	String dataurl = "";

	String title = "A plot";
	int width = 400;      // Default width of the graph
	int height = 400;     // Default height of the graph


        while (i < args.length && (args[i].startsWith("-") || 
            args[i].startsWith("=")) ) {
            arg = args[i++];
	    if (_debug > 2) System.out.print("Pxgraph: arg = " + arg + "\n");

	    if (arg.startsWith("-")) {
		if (arg.equals("-db") || arg.equals("-debug")) {
		    _debug = (int)Integer.valueOf(args[i++]).intValue();
		    continue;
		} else if (arg.equals("-help")) {
		    // -help is not in the original X11 pxgraph.
		    _help();
		    continue;
		} else if (arg.equals("-test")) {
		    // -test is not in the original X11 pxgraph.
		    _test = true;
		    continue;
		} else if (arg.equals("-t")) {
		    // -t <title> TitleText "An X Graph"
		    title =  args[i++];
		    continue;
		} else if (arg.equals("-v") || arg.equals("-version")) {
		    // -version is not in the original X11 pxgraph.
		    _version();
		    continue;
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
	}

	// Set up the frame
	resize(width,height); 	// FIXME: resize is deprecated in 1.1,
	                        // we should use setsize(width,height)
				// but setsize is not in JDK1.0.2
	setTitle(title);

        if (i < args.length) {
            dataurl=args[i];
	}
        argsread = i++;

        if (_debug > 2) {
	    System.err.println("Pxgraph: dataurl = " + dataurl);
	    System.err.println("Pxgraph: title = " + title);
	    System.err.println("Pxgraph: width = " + width + 
			       " height = " + height +
			       " _debug = " + _debug);
	}
        return argsread;
    }

    /* version - print out version info
     */	
    private void _version () {
	// FIXME: we should bring up a dialog box or something.
	System.out.println("Pxgraph - (Java implementation) by\n" +
			   "By: Edward A. Lee, eal@eecs.berkeley.edu and\n " +
			   "Christopher Hylands, cxh@eecs.berkeley.edu\n" +
			   "($Id$)");
	System.exit(0);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private Button _exitButton;

    // For debugging, call with -db or -debug.
    private static int _debug = 0;

    // If true, then auto exit after a few seconds.
    private static boolean _test = false;
}

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
import tycho.Cool_Beans.util.AppletFrame;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.Byte;

import java.net.URL;
import java.net.MalformedURLException;

//////////////////////////////////////////////////////////////////////////
//// Pxgraph
/** 
Java implementation of the Unix X Windows xgraph plotter program
@author Christopher Hylands
@version $Id$
@see Plot
*/
public class Pxgraph {
    /** Constructor
     */	
    public Pxgraph() {
    }

    /** Main
	Parse the command line arguments, do any preprocessing, then plot.
      */
    public static void main(String arg[]) {
        String args[] = new String[12];
        AppletFrame myAppletFrame;
        Pxgraph pxgraph = new Pxgraph();


	pxgraph.parseArgs(arg);

	pxgraph.preprocess();

        args[0] = new String("-width");
        args[1] = new String(pxgraph.getWidth());
        args[2] = new String("-height");
        args[3] = new String(pxgraph.getHeight());
        args[4] = new String("-codebase");
        args[5] = new String(".");
        args[6] = new String("-documentbase");
        args[7] = new String(".");

        args[8] = new String("dataurl=" + pxgraph.getDataURL() );
        args[9] = new String("-noinit");
        args[10] = new String("-name");
        args[11] = new String(pxgraph.getCommandOption("-t"));

	myAppletFrame = new AppletFrame("plot.Plot",args);
    }

     /** Return the value of the boolean command line flag.
     */	
    public boolean getCommandFlag(String commandFlag) {
	// FIXME: we should throw an exception if the arg is bogus?
	int i;
	for(i=0;i<commandFlags.length;i++) {
	    if (commandFlag.equals(commandFlags[i][0])) {
		if (commandFlags[i][2].equals("true"))
		    return true;
		else
		    return false;
	    }
	}
	return false;
    }

     /** Return the value of the boolean command line flag.
     */	
    public String getCommandOption(String commandOption) {
	// FIXME: we should throw an exception if the arg is bogus?
	int i;
	for(i=0;i<commandFlags.length;i++) {
	    if (commandOption.equals(commandOptions[i][0])) {
		return commandOptions[i][3];
	    }
	}
	return "";
    }

     /** Return the name of the file to be opened.
     */	
    public String getDataURL() {
	return dataurl;
    }

     /** Return the height of the graph.
     */	
    public String getHeight() {
	return height;
    }

     /** Return the width of the graph.
     */	
    public String getWidth() {
	return width;
    }

     /** Preprocess the datafile by converting from binary if necessary
       * and adding any header information.  If it is necessary	  
       * to preprocess, then we create a temporary file.
     */	
    public void preprocess() {
	// FIXME: we need a better tmpname mechanism
	String tmpfilename = new String("/tmp/pxgraph.tmp");
	File tmpfile = new File(tmpfilename);

        if (debug)  
	    System.out.print("preprocess: top\n");
	
	try {
	    FileOutputStream fos = new FileOutputStream(tmpfile);
	    addHeader(fos);
	    if (getCommandFlag("-binary")) {
		convertBinaryFile(fos);
	    } else {
		copyDataURLFile(fos);
	    }
	    fos.close();
	} catch (IOException e) {
	    System.err.println("preprocess: " + e);
	}
	dataurl=tmpfilename;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /* Based on the command line arguments, add a header to the temporary
       file.
     */	
    private void addHeader(FileOutputStream fos){
	if (debug)
	    System.out.print("addHeader: top\n");
	try {
	    int i;
	    for(i=0;i<commandFlags.length;i++) {
		if (commandFlags[i][2].equals("true")) {
		    String plotDirective = new String(commandFlags[i][3]);
		    if (debug)
			System.out.print("addHeader: " + plotDirective + "\n");
		    fos.write(plotDirective.getBytes());
		}
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("FileStreamsTest: " + e);
	} catch (IOException e) {
	    System.err.println("FileStreamsTest: " + e);
	}
    }

    /* Convert a binary pxgraph file to a ascii file
     */	
    private void convertBinaryFile(FileOutputStream fos){ 
    }

    /* Copy the dataurl file to the already open temporary file
     */	
    private void copyDataURLFile(FileOutputStream fos){ 
	try {
	    URL data = new URL(dataurl);
		DataInputStream dis = new DataInputStream(data.openStream());
	    String inputLine;
	    while ((inputLine = dis.readLine()) != null) {
		fos.write(inputLine.getBytes());
		fos.write('\n');
	    }
	    dis.close();
	} catch (MalformedURLException me) {
	    System.out.println("MalformedURLException: " + me);
	} catch (IOException ioe) {
	    System.out.println("IOException: " + ioe);
	}
    }


    /* DumpArgs - print the argument table
     */	
    private void dumpArgs () {
        int j;
        for(j=0;j<commandOptions.length;j++) {
            System.out.print(commandOptions[j][0] + " "
                + commandOptions[j][1] + " " 
                + commandOptions[j][2] + " "
                + commandOptions[j][3] + "\n");
        }

        for(j=0;j<commandFlags.length;j++) {
            System.out.print(commandFlags[j][0] + " "
                + commandFlags[j][1] + " " 
            	+ commandFlags[j][2] + "\n");
        }
        System.out.print("dataurl = " + dataurl + "\n");
        System.out.print("width = " + width + "\n");
        System.out.print("height = " + height + "\n");
    }


    private void parseArgs(String[] args) {
        int i = 0, j;
        String arg;
        char flag;
        boolean vflag = false;
        String outputfile = "";
        
        while (i < args.length && (args[i].startsWith("-") || 
            args[i].startsWith("=")) ) {
            arg = args[i++];

            // use this type of check for "wordy" arguments
            for(j=0;j<commandFlags.length;j++) {
                if (arg.equals(commandFlags[j][0])) {
                    commandFlags[j][2] = "true";
                    continue;
                }
            }
            for(j=0;j<commandOptions.length;j++) {
                if (arg.equals(commandOptions[j][0])) {
                    commandOptions[j][3] = args[i++];
                    continue;
                }
            }
            if (arg.charAt(0) == '=') {
                int endofheight;
                System.out.print("Saw =");
                width = arg.substring(1,arg.indexOf('x'));
                if (arg.indexOf('+') != -1) {
                    height = arg.substring(arg.indexOf('x')+1,
                        arg.indexOf('+')-1);
                } else {
                    if (arg.length() > arg.indexOf('x')) {
                        height = arg.substring(arg.indexOf('x')+1,
                        	arg.length());
                    }
                }

            }
            // Fixme:  Need to deal with -<digit> <name>
        }
        if (i < args.length) {
            dataurl=args[i];
        }
        if (debug) dumpArgs();
        
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // For debugging, set debug to true and recompile
    private boolean debug = true;

    // Default URL to be opened
    private String dataurl = "http://ptolemy.eecs.berkeley.edu/java/plot/demo/data.plt";

    private String width = "400";      // Default width of the graph
    private String height = "400";     // Default height of the graph

    // Array of command line arguments that have values.
    private String commandOptions[][] = {
        {"-bd", "<color>", "Border", "White"},
        {"-bg", "<color>", "BackGround", "White"},
        {"-brb", "<base>", "BarBase", "0"},
        {"-brw", "<width>", "BarWidth", "1"},
        {"-bw", "<size>", "BorderSize", "1"},
        {"-fg", "<color>", "Foreground", "Black"},
        {"-gw", "<pixels>", "GridStyle", "1"},
        {"-lf", "<fontname>", "LabelFont", "helvetica-12"},
        {"-lw", "<width>", "LineWidth", "0"},
        {"-lx", "<xl,xh>", "XLowLimit, XHighLimit", "0"},
        {"-ly", "<yl,yh>", "YLowLimit, YHighLimit", "0"},
        {"-t", "<title>", "TitleText", "An X Graph"},
        {"-tf", "<fontname>", "TitleFont", "helvetica-18"},
        {"-x", "<unitName>", "XUnitText", "X"},
        {"-y", "<unitName>", "YUnitText", "Y"},
        {"-zg", "<color>", "ZeroColor", "Black"},
        {"-zw", "<width>", "ZeroWidth", "0"},
    };

    // Array of command line flags that are booleans.
    // For example, if we have:
    //  {"-bar", "BarGraph", "false", "Bars: on"}
    // The elements in the row are as follows:
    // -bar         - The command line argument we search for.
    // BarGraph     - The X Resource or option name, currently not used.
    // false        - The initial default, which is updated with the
    //                value true if this arg is present in the command line.
    // Bars: on     - If the arg is set, then we add this string to
    //                the header file.
    private String commandFlags[][] = {
        {"-bar", "BarGraph", "false", "Bars: on"},
        {"-bb", "BoundBox", "false", ""},
        {"-binary", "Binary", "false", ""},
        {"-db", "Debug", "false", ""},
        {"-lnx", "LogX", "false", "NotSupported"},
        {"-lny", "LogY", "false", "NotSupported"},
        {"-m", "Markers", "false", "Marks: various" },
        {"-M", "StyleMarkers", "false", "Marks: various" },
        {"-nl", "NoLines", "false", "Lines: off" },
        {"-p", "PixelMarkers", "false", "Marks: points" },
        {"-P", "LargePixel", "false", "Marks: dots" },
        {"-rv", "ReverseVideo", "false", ""},
        {"-tk", "Ticks", "false", ""},
    };
}

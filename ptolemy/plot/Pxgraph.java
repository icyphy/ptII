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

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.lang.Byte;
import java.lang.Thread;
import java.lang.InterruptedException; 

import java.net.URL;
import java.net.MalformedURLException;

/* A class thrown by Pxgraph */
class CmdLineArgException extends Throwable {

  public CmdLineArgException() { super(); }
  public CmdLineArgException(String s) { super(s); }
 
}

class PlotDataException extends Throwable {

  public PlotDataException() { super(); }
  public PlotDataException(String s) { super(s); }
 
}

//////////////////////////////////////////////////////////////////////////
//// Pxgraph
/** 
 * Java implementation of the Unix X Windows xgraph plotter program.
@author Christopher Hylands
@version $Id$
@see Plot
*/
public class Pxgraph {
    /** Constructor
     */	
    public Pxgraph() {
	// Initialize the array that contains the dataset descriptors.
	datasets = new String[MAX_DATASETS];
    }

    /** Main
	Parse the command line arguments, do any preprocessing, then plot.
      */
    public static void main(String arg[]) {
        String args[] = new String[11];
        AppletFrame myAppletFrame;
        Pxgraph pxgraph = new Pxgraph();


	try {
	    pxgraph.parseArgs(arg);
	} catch (CmdLineArgException e) {
	    System.err.println("Failed to parse command line arguments: "
			       + e);
	    System.exit(1);
	}
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
        args[9] = new String("-name");
        args[10] = new String(pxgraph.getCommandOption("-t"));

	myAppletFrame = new AppletFrame("plot.Plot",args);
	if (pxgraph.getCommandFlag("-test")) {
	    if (debug) System.out.println("Sleeping for 2 seconds");
	    try {
		Thread.currentThread().sleep(2000);
	    }
	    catch (InterruptedException e) {
	    }
	    System.exit(0);
	}
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

	FileOutputStream fos = null;
	DataInputStream dis = null;

        if (debug)  
	    System.out.print("preprocess: top\n");
	
	try {
	    fos = new FileOutputStream(tmpfile);
	    addHeader(fos);

	    if (dataurl.length() == 0) {
		// Open up stdin
		dis = new DataInputStream(System.in);
	    } else {
		// Try opening as URL, if that fails, open as a file.
		try {
		    URL url;
		    url = new URL(dataurl);
		    dis = new DataInputStream(url.openStream());
		} catch (MalformedURLException e) {
		    try {
			dis = new DataInputStream(new FileInputStream(dataurl));
		    } catch (FileNotFoundException me) {
			System.out.println("FileNotFoundException:" + me);
			return;
		    } catch (SecurityException me) {
			System.out.println("SecurityException:" + me);
			return;
		    }
		} catch (IOException ioe) {
		    System.out.println("IOException: " + ioe);
		    return;
		}
	    }

	    if (getCommandFlag("-binary")) {
		convertBinaryFile(dis,fos);
	    } else {
		copyDataURLFile(dis,fos);
	    }

	    if (dis != null) dis.close();
	    if (fos != null) fos.close();

	} catch (IOException e) {
	    System.err.println("preprocessor IOException: " + e);
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
			System.out.println("addHeader: " + plotDirective);
		    if (plotDirective.equals("NotSupported")) {
			System.err.println("pxgraph: Warning " +
					   commandFlags[i][0] + 
					   " is not yet supported");
		    } else {
			fos.write(plotDirective.getBytes());
		    }
		}
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("addHeader: " + e);
	} catch (IOException e) {
	    System.err.println("addHeader: " + e);
	}
    }

    /* Convert a binary pxgraph file to a ascii file
     */	
    private void convertBinaryFile(DataInputStream dis, FileOutputStream fos){ 
	try {
	    int c;
	    boolean printedDataSet = false;
	    try {
		while (true) {
		    // Here, we read pxgraph binary format data.
		    // For speed reasons, the Ptolemy group extended 
		    // pxgraph to read binary format data.
		    // The format consists of a command character,
		    // followed by optional arguments
		    // d <4byte float> <4byte float> - Draw a X,Y point
		    // e                             - End of a data set
		    // n <chars> \n                  - New set name, ends in \n
		    // m                             - Move to a point
		    c = dis.readByte();
		    //System.out.print(c);
		    switch (c) {
		    case 'd':
			{
			    // Data point.
			    float x = dis.readFloat();
			    float y = dis.readFloat();
			    //if (debug) System.out.print(outputline);
			    if (!printedDataSet) {
				String datasetstring;
				printedDataSet = true;
				if (datasets[0] != null) {
				    datasetstring = new String("DataSet: "
							 + datasets[0] + "\n");
				} else {
				    datasetstring =new String("DataSet: Set 0"
							 + "\n");
				}
			        fos.write(datasetstring.getBytes());
			    }
			    String outputline = new String(x + "," + y + '\n');
			    fos.write(outputline.getBytes());

			}
			break;
		    case 'e':
			// End of set name.
			fos.write('\n');
			break;
		    case 'n':
			// New set name, ends in \n.
			while (c != '\n')
			    fos.write(dis.readChar());
			break;
		    case 'm':
			break;
		    default:
			throw new PlotDataException("Don't understand `"
                                              + c + 
					      "'character in binary data");
		    }


		}
	    } catch (EOFException me) {
		dis.close();
	    } catch (PlotDataException me) {
		dis.close();
		System.out.println("PlotDataException: " + me);
	    }
	} catch (IOException ioe) {
	    System.out.println("IOException: " + ioe);
	}
    }

    /* Copy the dataurl file to the already open temporary file
     */	
    private void copyDataURLFile(DataInputStream dis, FileOutputStream fos){ 
        try {
            String inputLine;
            while ((inputLine = dis.readLine()) != null) {
                fos.write(inputLine.getBytes());
                fos.write('\n');
            }
            dis.close();
        } catch (IOException ioe) {
            System.out.println("IOException: " + ioe);
            return;
        }
    }


    /* DumpArgs - print the argument table
     */	
    private void dumpArgs () {
        int j;
	boolean printeddatasetheader = false;
	System.out.println("Command options that take values:"); 
        for(j=0;j<commandOptions.length;j++) {
            System.out.println("  " + commandOptions[j][0] + " "
                + commandOptions[j][1] + " " 
                + commandOptions[j][2] + " "
                + commandOptions[j][3]);
        }

	System.out.println("Boolean command flags:");
        for(j=0;j<commandFlags.length;j++) {
            System.out.println("  " + commandFlags[j][0] + " "
                + commandFlags[j][1] + " " 
            	+ commandFlags[j][2]);
        }
	
	System.out.println("Values of various parameters:");
        System.out.println("dataurl = " + dataurl);
        System.out.println("width = " + width);
        System.out.println("height = " + height);
        for(j=0;j<datasets.length;j++) {
	    if (datasets[j] != null) {
		if (!printeddatasetheader) {
		    printeddatasetheader = true;
		    System.out.println("Dataset titles passed in as args");
		}
		System.out.println(" dataset " + j + " = " + datasets[j]);
	    }
	}
    }


    /* help - print out help
     */	
    private void help () {
	System.out.println("Usage: pxgraph [ options ] [=WxH+X+Y] [file ...]");
	dumpArgs();
	System.exit(1);
    }

    private void parseArgs(String[] args) 
	throws CmdLineArgException{
        int i = 0, j;
        String arg;
        boolean parsedFlag;

        while (i < args.length && (args[i].startsWith("-") || 
            args[i].startsWith("=")) ) {
            arg = args[i++];
	    if (debug) System.out.print("arg = " + arg + "\n");
	    parsedFlag = false;
            // use this type of check for "wordy" arguments
            for(j=0;j<commandFlags.length;j++) {
                if (arg.equals(commandFlags[j][0])) {
                    commandFlags[j][2] = "true";
		    if (debug) System.out.print(commandFlags[j][0] + "=1\n");
		    parsedFlag = true;
                }
            }
            for(j=0;j<commandOptions.length;j++) {
                if (arg.equals(commandOptions[j][0])) {
                    commandOptions[j][3] = args[i++];
		    parsedFlag = true;
                }
            }
            if (arg.charAt(0) == '=') {
		// Process =WxH+X+Y
                int endofheight;
                width = arg.substring(1,arg.indexOf('x'));
                if (arg.indexOf('+') != -1) {
                    height = arg.substring(arg.indexOf('x')+1,
                        arg.indexOf('+'));
                } else {
                    if (arg.length() > arg.indexOf('x')) {
                        height = arg.substring(arg.indexOf('x')+1,
                        	arg.length());
                    }
                }
		// FIXME: need to handle X and Y in =WxH+X+Y
		parsedFlag = true;
            }
	    if (arg.length() > 1  && arg.charAt(0) == '-') {
		// Process '-<digit> <datasetname>'
		try {
		    Integer datasetnumberint = new Integer(arg.substring(1));
		    int datasetnumber = datasetnumberint.intValue();
		    if (datasetnumber >= 0 && datasetnumber <= MAX_DATASETS) {
			// Save the next arg in the dataset array
			datasets[datasetnumber] = args[i++];
			if (debug)
			    System.out.print("dataset " + datasetnumber + " = " + datasets[datasetnumber] + "\n");

			parsedFlag = true;
		    }
		} catch (NumberFormatException e) {
		}
	    }
	    if (!parsedFlag) {
	    // If we got to here, then we failed to parse the arg 
		throw new 
		    CmdLineArgException("Failed to parse `" 
                          + arg + "'");
	    }
	}
        if (i < args.length) {
            dataurl=args[i];
        }

	if (getCommandFlag("-help")) help();
	if (getCommandFlag("-db")) debug=true;
        if (debug) dumpArgs();
        
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // For debugging, set debug to true and recompile.
    private static boolean debug = false;

    // Default URL to be opened
    private String dataurl = "";

    private String width = "400";      // Default width of the graph
    private String height = "400";     // Default height of the graph

    // Array of command line arguments that have values.
    // For example, we have:
    //    {"-bd", "<color>", "Border", "false", "White", "Unsupported"},
    // Then the elements of the row are
    // -bd          - The command line argument we search for.
    // <color>      - A description of the value that is expected.
    // Border       - The X Resource or option name, currently not used. 
    // false        - If the arg is set, then this is set to "true"
    // White        - The initial default, which is updated with the
    //                value true if this arg is present in the command line.
    // Unsupported  - If the arg is set, then we add this string to the
    //                header file, followed by the value of the 
    private String commandOptions[][] = {
        {"-bd", "<color>", "Border", "false", "White", "Unsupported"},
        {"-bg", "<color>", "BackGround", "false", "White", "Unsupported"},
        {"-brb", "<base>", "BarBase", "false", "0", "Unsupported"},
        {"-brw", "<width>", "BarWidth", "false", "1", "Bars: "},
        {"-bw", "<size>", "BorderSize", "false", "1", "Unsupported"},
        {"-fg", "<color>", "Foreground", "false", "Black", "Unsupported"},
        {"-gw", "<pixels>", "GridStyle", "false", "1", "Unsupported"},
        {"-lf", "<fontname>", "LabelFont", "false", "helvetica-12", "Unsupported"},
        {"-lw", "<width>", "LineWidth", "false", "0", "UnSupported"},
        {"-lx", "<xl,xh>", "XLowLimit, XHighLimit", "false", "0", "XRange: "},
        {"-ly", "<yl,yh>", "YLowLimit, YHighLimit", "false", "0", "YRange: "},
        {"-t", "<title>", "TitleText", "false", "An X Graph", ""},
        {"-tf", "<fontname>", "TitleFont", "false", "helvetica-18"},
        {"-x", "<unitName>", "XUnitText", "false", "X", "XLabel: "},
        {"-y", "<unitName>", "YUnitText", "false", "Y", "YLabel: "},
        {"-zg", "<color>", "ZeroColor", "false", "Black", "Unsupported"},
        {"-zw", "<width>", "ZeroWidth", "false", "0", "Unsupported"},
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
        {"-bar", "BarGraph", "false", "Bars: on\nMarks: none\nLines: off\n"},
        {"-bb", "BoundBox", "false", "NotSupported"},
        {"-binary", "Binary", "false", ""},
        {"-db", "Debug", "false", ""},
	// -help is not in the original X11 pxgraph.
        {"-help", "Help", "false", ""},
        {"-lnx", "LogX", "false", "NotSupported"},
        {"-lny", "LogY", "false", "NotSupported"},
        {"-m", "Markers", "false", "Marks: various\n" },
        {"-M", "StyleMarkers", "false", "Marks: various\n" },
        {"-nl", "NoLines", "false", "Lines: off\n" },
        {"-p", "PixelMarkers", "false", "Marks: points\n" },
        {"-P", "LargePixel", "false", "Marks: dots\n" },
        {"-rv", "ReverseVideo", "false", "NotSupported"},
	// -test is not in the original X11 pxgraph.  We use it for testing
        {"-test", "Test", "false", ""},
        {"-tk", "Ticks", "false", "Grid: off\n"},
    };
    private static final int MAX_DATASETS = 63; // Maximum number of datasets
    private String datasets[];
}

/* A standalone plot application.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.plot;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

// TO DO:
//   - Improve the help mechanism and separate from the usage message.

//////////////////////////////////////////////////////////////////////////
//// PlotApplication
/**
PlotApplication is a versatile two-dimensional data plotter application.
It can read files compatible with the Ptolemy plot
file format (currently only ASCII).  For a description of the file
format, see the Plot and PlotBox classes.
Command-line options include:
<dl>

<dt><code>-help</code></a>
<dt>Display the usage, including all command-line options
    that exist for backward compatibility.

<dt><code>-test</code></a>
<dt>Display the plot, then exit after 2 seconds.

<dt><code>-version</code></a>
<dt>Display the program version.
</dl>

<p>
For compatibility with historical applications, this application has
a limited ability to read pxgraph files.  The command line arguments
must be used, and the options that are understood are exactly those
of the pxgraph application, plus some more to allow for cross-platform
reading.  It is not possible to read pxgraph files
using the "Open" menu command (because of the cross-platform problems).
The additional command-line arguments are:
<dl>

<dt><code>-bigendian</code></a>
<dt>Data files are in big-endian, or network binary format.
If you are on a little-endian machine, such as a machine
with an Intel x86 chip, and you would like to read a binary
format file created on a big-endian machine, such as a Sun SPARC,
use the <code>-bigendian</code> flag.

<dt><code>-littleendian</code></a>
<dt>Data files are in little-endian, or x86 binary format.
If you are on a big-endian machine, such as a Sun Sparc,
and you would like to read a binary
format file created on a little-endian machine, such as Intel x86
machine, then use the <code>-littleendian</code> flag.
</dl>
<p>
To compile and run this application, do the following (in Unix):
<pre>
    setenv CLASSPATH ../..
    javac PlotApplication.java
    java ptolemy.plot.PlotApplication
</pre>
or in a bash shell in Windows NT:
<pre>
    CLASSPATH=../..
    export CLASSPATH
    javac PlotApplication.java
    java ptolemy.plot.PlotApplication
</pre>
This assumes a particular directory structure.  If this is not what you
have, then alter the above accordingly.

@see Plot
@see PlotBox
@author Christopher Hylands and Edward A. Lee
@version $Id$
*/
public class PlotApplication extends PlotFrame {

    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     */
    public PlotApplication() {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public PlotApplication(String args[]) {
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

        if (args != null && args.length != 0) {
            try {
                _parseArgs(args);
            } catch (FileNotFoundException ex) {
                System.err.println("File not found: " + ex + "\n" + _usage());
                throw new RuntimeException("cancelled");
            } catch (IOException ex) {
                System.err.println("Error reading input: " + ex
                        + "\n" + _usage());
                throw new RuntimeException("cancelled");
            } catch (CmdLineArgException ex) {
                System.err.println("Command line format error: " + ex
                        + "\n" + _usage());
                throw new RuntimeException("cancelled");
            }
            String _cmdfile = plot.getCmdLineFilename();
            if (_cmdfile != null) {
                _filename = _cmdfile;
            }
        } else {
            samplePlot();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     */
    public static void main(String args[]) {
        PlotApplication plot = new PlotApplication(args);

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            }
            catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    protected void _about() {
        Message message = new Message(
                "Ptplot\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "and Christopher Hylands, cxh@eecs.berkeley.edu\n" +
                "Version 2.0, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n" +
                "Copyright (c) 1997-1998,\n" +
                "The Regents of the University of California."); 
        message.setTitle("About Ptolemy Plot");
    }

    /** Exit the application.
     */
    protected void _close() {
        System.exit(0);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        // Use newlines here since we are displaying with scrollbars.
        Message message = new Message(
                "PlotApplication is a standalone Java 2D plot application\n" +
                "It can read files compatible with the Ptolemy plot\n" +
                "file format (currently only ASCII).  For a description\n " +
                "of the file format, see the Plot and PlotBox classes.\n" +
                "Command-line options include:\n" + _usage(),
                null, null, 20, 40,
                TextArea.SCROLLBARS_BOTH);
        message.setTitle("Usage of Ptolemy Plot");
    }

    /** Parse the command-line
     *  arguments and make calls to the Plot class accordingly.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If a command line argument cannot
     *  be parsed.
     *  @exception FileNotFoundException If an input file cannot be found.
     *  @exception IOException If there is a problem reading an input.
     */
    protected int _parseArgs(String args[]) throws CmdLineArgException,
            FileNotFoundException, IOException {
        int i = 0, j, argsread;
        String arg;
        String title = "Ptolemy plot";

        int width = 400;      // Default width of the graph
        int height = 300;     // Default height of the graph

        // Although most of the arguments are handled by the Plot class,
        // a few are dealt with here.
        while (i < args.length) {
            arg = args[i++];

            if (arg.equals("-help")) {
                // -help is not in the original X11 pxgraph.
                System.out.println(_usage());
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
                _about();
                continue;
            } else if (arg.startsWith("=")) {
                // Process =WxH+X+Y
                int xscreen = 1, yscreen = 1;
                boolean screenlocationgiven = false;
                StringTokenizer stoken =
                    new StringTokenizer(arg.substring(1, arg.length()),
                            "=x-+");
                if (stoken.hasMoreTokens()) {
                    width = (int)Integer.valueOf(stoken.nextToken()).
                        intValue();
                }
                if (stoken.hasMoreTokens()) {
                    height = (int)Integer.valueOf(stoken.nextToken()).
                        intValue();
                }
                if (stoken.hasMoreTokens()) {
                    xscreen = (int)Integer.valueOf(stoken.nextToken()).
                        intValue();
                    screenlocationgiven = true;
                }
                if (stoken.hasMoreTokens()) {
                    yscreen = (int)Integer.valueOf(stoken.nextToken()).
                        intValue();
                    screenlocationgiven = true;
                }
                if (screenlocationgiven) {
                    // Note: we add one so that =800x200+0+0 will show up
                    // in the proper location.
                    setLocation(new Point(xscreen+1, yscreen+1));
                }
                continue;
            }
        }

        setSize(width, height);
        setTitle(title);

        argsread = i++;

        plot.parseArgs(args);
        return argsread;
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
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
            {"-bg",  "<color>", "BackGround",  "White", ""},
            {"-brb", "<base>", "BarBase",  "0", "(Unsupported)"},
            {"-brw", "<width>", "BarWidth",  "1", ""},
            {"-bw",  "<size>", "BorderSize",  "1", "(Unsupported)"},
            {"-fg",  "<color>", "Foreground",  "Black", ""},
            {"-gw",  "<pixels>", "GridStyle",  "1", "(Unsupported)"},
            {"-lf",  "<fontname>", "LabelFont",  "helvetica-12", ""},
            {"-lw",  "<width>", "LineWidth",  "0", "(Unsupported)"},
            {"-lx",  "<xl,xh>", "XLowLimit, XHighLimit",  "0", ""},
            {"-ly",  "<yl,yh>", "YLowLimit, YHighLimit",  "0", ""},
            // -o is not in the original X11 pxgraph.
            {"-o",   "<output filename>", "",  "/tmp/t.ps", ""},
            {"-t",   "<title>", "TitleText",  "An X Graph", ""},
            {"-tf",  "<fontname>", "TitleFont",  "helvetica-b-14", ""},
            {"-x",   "<unitName>", "XUnitText",  "X", ""},
            {"-y",   "<unitName>", "YUnitText",  "Y", ""},
            {"-zg",  "<color>", "ZeroColor",  "Black", "(Unsupported)"},
            {"-zw",  "<width>", "ZeroWidth",  "0", "(Unsupported)"},
        };

        String commandFlags[][] = {
            {"-bar", "BarGraph",  ""},
            {"-bb", "BoundBox",  "(Ignored)"},
            {"-binary", "Binary",  ""},
            // -impulses is not in the original X11 pxgraph.
            {"-impulses", "Impulses",  ""},
            {"-lnx", "XLog",  ""},
            {"-lny", "YLog",  ""},
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
        String result = "Usage: plot [ options ] [=WxH+X+Y] [file ...]\n\n"
            + " options that take values as second args:\n";

        int i;
        for(i = 0; i < commandOptions.length; i++) {
            result += " " + commandOptions[i][0] +
                " " + commandOptions[i][1] +
                " " + commandOptions[i][4] + "\n";
        }
        result += "\nBoolean flags:\n";
        for(i = 0; i < commandFlags.length; i++) {
            result += " " + commandFlags[i][0] +
                " " + commandFlags[i][2] + "\n";
        }
        result += "\nThe following pxgraph features are not supported:\n"
            + " * Directives in pxgraph input files\n"
            + " * Xresources\n"
            + "For complete documentation, see the pxgraph program docs.";
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // If true, then auto exit after a few seconds.
    private static boolean _test = false;
}

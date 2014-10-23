/* Plotter application that is capable of reading pxgraph files.

 @Author: Edward A. Lee and Christopher Hylands

 @Version: $Id$

 @Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.plot.compat;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ptolemy.plot.CmdLineArgException;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplication;
import ptolemy.plot.PlotBox;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// PxgraphApplication

/**
 An application that can plot data in pxgraph format.
 To compile and run this application, do the following:
 <pre>
 javac -classpath ../../.. PxgraphApplication.java
 java -classpath ../../.. ptolemy.plot.compat.PxgraphApplication
 </pre>
 <p>
 This class uses the helper class PxgraphParser to parse command-line
 arguments and binary files.  See that class for documentation on
 the formats.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating red (eal)
 @Pt.AcceptedRating red (cxh)
 @see PxgraphParser
 @see Plot
 */
@SuppressWarnings("serial")
public class PxgraphApplication extends PlotApplication {
    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     *  @exception Exception If command line arguments have problems.
     */
    public PxgraphApplication() throws Exception {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PxgraphApplication(String[] args) throws Exception {
        this(new Plot(), args);
    }

    /** Construct a plot with the specified command-line arguments
     *  and instance of Plot.
     *  @param plot The instance of Plot.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PxgraphApplication(Plot plot, String[] args) throws Exception {
        super(plot, args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     *  @param args The command line arguments.  To see what command
     *  line arguments are available, run with "-help" as the first element.
     */
    public static void main(final String[] args) {
        try {
            // Run this in the Swing Event Thread.
            Runnable doActions = new Runnable() {
                @Override
                public void run() {
                    try {
                        new PxgraphApplication(new Plot(), args);
                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                        ex.printStackTrace();
                    }
                }
            };

            SwingUtilities.invokeAndWait(doActions);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            StringUtilities.exit(0);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    @Override
    protected void _about() {
        JOptionPane
        .showMessageDialog(
                this,
                "               pxgraph\n"
                        + "        A Java Plotting Tool\n\n"
                        + "By: Edward A. Lee and\n"
                        + "    Christopher Brooks\n"
                        + "Version "
                        + PlotBox.PTPLOT_RELEASE
                        + ", Build: $Id$\n\n"
                        + "For help, type 'pxgraph -help', or see \n"
                        + "the class documentation in the plot.compat package.\n"
                        + "For more information, see\n"
                        + "http://ptolemy.eecs.berkeley.edu/java/ptplot\n",
                        "About pxgraph", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Parse those command-line arguments that are relevant to the
     *  application only, and refer other arguments to the PxgraphParser
     *  helper class.
     *  @param args  The command line arguments to parse.
     *  @return The number of arguments read.
     *  @exception CmdLineArgException If a command line argument cannot
     *   be parsed.
     *  @exception FileNotFoundException If an input file cannot be found.
     *  @exception IOException If there is a problem reading an input.
     */
    @Override
    protected int _parseArgs(String[] args) throws CmdLineArgException,
    FileNotFoundException, IOException {
        int i = 0;
        int argumentsRead;
        String arg;
        String title = "Ptolemy plot, pxgraph version";

        int width = 400; // Default width of the graph
        int height = 300; // Default height of the graph

        // Although most of the arguments are handled by the Plot class,
        // a few are dealt with here.
        while (args != null && i < args.length) {
            arg = args[i++];

            if (arg.equals("-help")) {
                // -help is not in the original X11 pxgraph.
                System.out.println(_usage());
                StringUtilities.exit(0);
                continue;
            } else if (arg.equals("-test")) {
                // -test is not in the original X11 pxgraph.
                _test = true;
                continue;
            } else if (arg.equals("-t")) {
                // -t <title> TitleText "An X Graph"
                title = args[i++];
                continue;
            } else if (arg.equals("-v") || arg.equals("-version")) {
                // -version is not in the original X11 pxgraph.
                System.out
                .println("Version "
                        + PlotBox.PTPLOT_RELEASE
                        + ", Build $Id$");
                StringUtilities.exit(0);
                continue;
            } else if (arg.startsWith("=")) {
                // Process =WxH+X+Y
                int xscreen = 1;

                // Process =WxH+X+Y
                int yscreen = 1;
                boolean screenlocationgiven = false;
                StringTokenizer stoken = new StringTokenizer(arg.substring(1,
                        arg.length()), "=x-+");

                if (stoken.hasMoreTokens()) {
                    width = Integer.parseInt(stoken.nextToken());
                }

                if (stoken.hasMoreTokens()) {
                    height = Integer.parseInt(stoken.nextToken());
                }

                if (stoken.hasMoreTokens()) {
                    xscreen = Integer.parseInt(stoken.nextToken());
                    screenlocationgiven = true;
                }

                if (stoken.hasMoreTokens()) {
                    yscreen = Integer.parseInt(stoken.nextToken());
                    screenlocationgiven = true;
                }

                if (screenlocationgiven) {
                    // Note: we add one so that =800x200+0+0 will show up
                    // in the proper location.
                    setLocation(new Point(xscreen + 1, yscreen + 1));
                }

                continue;
            }
        }

        setSize(width, height);
        setTitle(title);

        argumentsRead = i++;

        if (_parser == null) {
            _parser = new PxgraphParser((Plot) plot);
        }

        _parser.parseArgs(args);
        return argumentsRead;
    }

    /** Read the specified stream.  This method assumes the stream
     *  contains pxgraph-compatible binary or ascii data.  If it is
     *  binary, then the -binary flag must have been specified on
     *  the command line.
     *  @param base The base for relative file references, or null if
     *   there are not relative file references.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    @Override
    protected void _read(URL base, InputStream in) throws IOException {
        _parser.read(in);
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    @Override
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
        String[][] commandOptions = {
                { "-bd", "<color>", "Border", "White", "(Unsupported)" },
                { "-bg", "<color>", "BackGround", "White", "" },
                { "-brb", "<base>", "BarBase", "0", "(Unsupported)" },
                { "-brw", "<width>", "BarWidth", "1", "" },
                { "-bw", "<size>", "BorderSize", "1", "(Unsupported)" },
                { "-fg", "<color>", "Foreground", "Black", "" },
                { "-gw", "<pixels>", "GridStyle", "1", "(Unsupported)" },
                { "-lf", "<fontname>", "LabelFont", "helvetica-12", "" },
                { "-lw", "<width>", "LineWidth", "0", "(Unsupported)" },
                { "-lx", "<xl,xh>", "XLowLimit, XHighLimit", "0", "" },
                { "-ly", "<yl,yh>", "YLowLimit, YHighLimit", "0", "" },
                // -o is not in the original X11 pxgraph.
                { "-o", "<output filename>", "", "/tmp/t.ps", "" },
                { "-t", "<title>", "TitleText", "An X Graph", "" },
                { "-tf", "<fontname>", "TitleFont", "helvetica-b-14", "" },
                { "-x", "<unitName>", "XUnitText", "X", "" },
                { "-y", "<unitName>", "YUnitText", "Y", "" },
                { "-zg", "<color>", "ZeroColor", "Black", "(Unsupported)" },
                { "-zw", "<width>", "ZeroWidth", "0", "(Unsupported)" }, };

        String[][] commandFlags = {
                // - is not in the original xgraph.
                { "-", "", "(read from standard in)" },
                { "-bar", "BarGraph", "" },
                { "-bb", "BoundBox", "(Ignored)" },
                { "-bigendian", "", "" },
                { "-littleendian", "", "" },
                { "-binary", "Binary", "" },
                // -impulses is not in the original X11 pxgraph.
                { "-impulses", "Impulses", "" }, { "-help", "", "" },
                { "-lnx", "XLog", "" }, { "-lny", "YLog", "" },
                { "-m", "Markers", "" }, { "-M", "StyleMarkers", "" },
                { "-nl", "NoLines", "" }, { "-p", "PixelMarkers", "" },
                { "-P", "LargePixel", "" }, { "-rv", "ReverseVideo", "" },
                // -test is not in the original X11 pxgraph.  We use it for testing
                { "-test", "Test", "" }, { "-tk", "Ticks", "" },
                // -v is not in the original X11 pxgraph.
                { "-v", "Version", "" }, { "-version", "Version", "" }, };
        StringBuffer result = new StringBuffer(
                "Usage: ptplot [ options ] [=WxH+X+Y] [file ...]\n\n"
                        + " options that take values as second args:\n");

        int i;

        for (i = 0; i < commandOptions.length; i++) {
            result.append(" " + commandOptions[i][0] + " "
                    + commandOptions[i][1] + " " + commandOptions[i][4] + "\n");
        }

        result.append("\nBoolean flags:\n");

        for (i = 0; i < commandFlags.length; i++) {
            result.append(" " + commandFlags[i][0] + " " + commandFlags[i][2]
                    + "\n");
        }

        result.append("\nThe following pxgraph features are not supported:\n"
                + " * Directives in pxgraph input files\n" + " * Xresources\n");
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Parser.
    private PxgraphParser _parser;
}

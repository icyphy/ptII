/* A standalone image rendering application.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.plot;

import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

//////////////////////////////////////////////////////////////////////////
//// RenderApplication
/**
<p>
To compile and run this application, do the following:
<pre>
    javac -classpath ../.. RenderApplication.java
    java -classpath ../.. ptolemy.plot.RenderApplication
</pre>
<p>
This assumes a particular directory structure.  If this is not what you
have, then alter the above accordingly.

@see Render
@see PlotBox
@author Neil Turner
@version $Id$
*/
public class RenderApplication extends PlotFrame {

    /** Construct a plot with no command-line arguments.
     *  It initially displays a sample plot.
     *  @exception Exception Not thrown in this base class.
     */
    public RenderApplication() throws Exception {
        this(null);
    }

    /** Construct a plot with the specified command-line arguments.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public RenderApplication(String args[]) throws Exception {
        this(new Render(), args);
    }

    /** Construct a plot with the specified command-line arguments
     *  and instance of plot.
     *  @param plot The instance of Render to use.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public RenderApplication(Render render, String args[]) throws Exception {

        // invoke the base class constructor and pass in the argument a Render
        // object.
        super("RenderApplication", render);

        // Handle window closing by exiting the application.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                // Strangely, calling _close() here sends javac into
                // an infinite loop (in jdk 1.1.4).
                //              _close();
                System.exit(0);
            }
        });

        // The Java look & feel is pretty lame, so we use the native
        // look and feel of the platform we are running on.
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore exceptions, which only result in the wrong look and feel.
        }

        _parseArgs(args);
        if (args == null || args.length == 0) {
            samplePlot();
        }
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new window and map it to the screen.
     */
    public static void main(String args[]) {
        try {
            RenderApplication render =
                   new RenderApplication(new Render(), args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }


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

    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "RenderApplication class\n" +
                "By: Neil Turner net@eecs.berkeley.edu\n" +
                "Version 0.1, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n\n" +
                "Copyright (c) 1997-2000, " +
                "The Regents of the University of California.",
                "About Ptolemy Render", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Exit the application.
     */
    protected void _close() {
        System.exit(0);
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "RenderApplication is a standalone plot " +
                " application.\n" +
                _usage(),
                "About Ptolemy Render", JOptionPane.INFORMATION_MESSAGE);
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
        int i = 0, j, argumentsRead;
        String arg;
        String title = "Ptolemy render";

        int width = 500;      // Default width of the graph
        int height = 300;     // Default height of the graph

        while (args != null && i < args.length) {
            arg = args[i++];

            if (arg.equals("-height")) {
                if (i > args.length - 1) {
                    throw new CmdLineArgException(_usage());
                }
                height = (int)Integer.valueOf(args[i++]).intValue();
                continue;
            } else if (arg.equals("-help")) {
                System.out.println(_usage());
                System.exit(0);
                continue;
            } else if (arg.equals("-test")) {
                _test = true;
                continue;
            } else if (arg.equals("-version")) {
                System.out.println("Version 0.1, Build $Id$");
                System.exit(0);
                continue;
            } else if (arg.equals("-width")) {
                if (i > args.length - 1) {
                    throw new CmdLineArgException(
                            "-width requires an integer argument");
                }

                width = (int)Integer.valueOf(args[i++]).intValue();
                continue;
            } else if (arg.equals("")) {
                // Ignore blank argument.
            } else if (!arg.startsWith("-")) {
                // Have a filename.  First attempt to open it as a URL.
                InputStream instream;
                URL base;
                try {
                    // First argument is null because we are only
                    // processing absolute URLs this way.  Relative
                    // URLs are opened as ordinary files.
                    URL inurl = new URL(null, arg);
                    base = inurl;
                    instream = inurl.openStream();
                } catch (MalformedURLException ex) {
                    File file = new File(arg);
                    instream = new FileInputStream(file);
                    _file = new File(file.getAbsolutePath());
                    title = _file.getName();
                    _directory = _file.getParentFile();
                    base = new URL("file", null, _directory.getAbsolutePath());
                }
                _read(base, instream);
            } else {
                // Unrecognized option.
                throw new CmdLineArgException("Unrecognized option: " + arg);
            }
        }

        setSize(width, height);
        setTitle(title);

        argumentsRead = i;

        return argumentsRead;
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
            {"-height",  "<pixels>"},
            {"-width",  "<pixels>"},
        };

        String commandFlags[] = {
            "-help",
            "-test",
            "-version",
        };
        String result = "Usage: ptplot [ options ] [file ...]\n\n"
            + "Options that take values:\n";

        int i;
        for(i = 0; i < commandOptions.length; i++) {
            result += " " + commandOptions[i][0] +
                " " + commandOptions[i][1] + "\n";
        }
        result += "\nBoolean flags:\n";
        for(i = 0; i < commandFlags.length; i++) {
            result += " " + commandFlags[i];
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;
}

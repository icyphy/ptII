/* A viewer for sound files.

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
*/

package ptolemy.plot.apps;
import ptolemy.plot.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import sun.audio.AudioPlayer;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// Sound
/**
Display sound files.

@see Plot
@see PlotBox
@author Edward A. Lee
@version $Id$
*/
public class Sound extends PtolemyPlot {

    public Sound(String args[]) {
        super(args);
        MenuItem play = new MenuItem("Play", new MenuShortcut(KeyEvent.VK_H));
        play.setActionCommand("Play");
        PlayListener playlistener = new PlayListener();
        play.addActionListener(playlistener);
        _specialMenu.add(play);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Display basic information about the application.
     */
    protected void _about() {
        Message message = new Message(
                "Ptolemy Sound Plotter\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "Version 2.0, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot\n",
                Color.white, Color.black);
        message.setTitle("About Ptolemy Plot");
    }

    /** Display some help.
     */
    protected void _help() {
        // NOTE:  This is a pretty lame excuse for help...
        Message message = new Message("Use Control-P to play the sound");
        message.setTitle("Usage of Ptolemy Sound");
    }

    /** Open a new file and plot its data.
     */
    protected void _open() {
        FileDialog filedialog = new FileDialog(this, "Select a sound file");
        filedialog.setFilenameFilter(new SoundFilenameFilter());
        if (_directory != null) {
            filedialog.setDirectory(_directory);
        }
        filedialog.setVisible(true);
        String filename = filedialog.getFile();
        if (filename == null) return;
        _directory = filedialog.getDirectory();
        File file = new File(_directory, filename);
        _filename = null;
        try {
            _sound = new ptolemy.math.SignalProcessing.AudioData(
                new DataInputStream(
                    new FileInputStream(file)));
        } catch (FileNotFoundException ex) {
            Message msg = new Message("File not found: " + ex);
        } catch (IOException ex) {
            Message msg = new Message("Error reading input: " + ex);
        }
        _filename = filename;

        // Configure the plot.
        _plot.clear(true);
        _plot.setTitle(new String(_sound.info));
        setTitle(filename);
        _plot.setXRange(0, (_sound.size - 1)/8000.0);
        _plot.setXLabel("Time in seconds");
        _plot.setYRange(-1.0, 1.0);
        double[] plotdata = _sound.toDouble(0);
        if (plotdata != null) {
            _plot.addPoint(0, 0, plotdata[0], false);
            for (int i=1; i < plotdata.length; i++) {
                _plot.addPoint(0, i/8000.0, plotdata[i], true);
            }
        }
        _plot.repaint();
    }

    /** Parse the command-line
     *  arguments and make calls to the Plot class accordingly.
     *  @return The number of arguments read.
     */
    protected int _parseArgs(String args[]) throws CmdLineArgException,
            FileNotFoundException, IOException {
        // FIXME: Need a much smaller set...
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
                // FIXME
                // _test = true;
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

        _plot.parseArgs(args);
        return argsread;
    }

    /** Play the sound.
     */
    protected void _play() {
        if (_instream == null) {
            // Fill the iobuffer with audio data.
            ByteArrayOutputStream out =
                    new ByteArrayOutputStream(_sound.size + _sound.offset);
            try {
                _sound.write(new DataOutputStream(out));
            } catch (IOException ex) {
                throw new RuntimeException(
                    "Failed to convert audio data to stream.");
            }
            byte[] _iobuffer = out.toByteArray();
            _instream = new ByteArrayInputStream(_iobuffer);
        }
        _instream.reset();
        AudioPlayer.player.start(_instream);
    }

    /** Save the plot to the current file, determined by the _directory
     *  and _filename protected variables.
     */
    protected void _save() {
        // FIXME: Save the sound file only...
        if (_filename != null) {
            File file = new File(_directory, _filename);
            try {
                FileOutputStream fout = new FileOutputStream(file);
                _plot.write(fout);
            } catch (IOException ex) {
                Message msg = new Message("Error writing file: " + ex);
            }
        } else {
            _saveAs();
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
//     protected String _usage() {
//         // FIXME: need a simpler profile...
// 
//         // We use a table here to keep things neat.
//         // If we have:
//         //  {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
//         // -bd       - The argument
//         // <color>   - The description of the value of the argument
//         // Border    - The Xgraph file directive (not supported at this time).
//         // White     - The default (not supported at this time)
//         // "(Unsupported)" - The string that is printed to indicate if
//         //                   a option is unsupported.
//         String commandOptions[][] = {
//             {"-bd",  "<color>", "Border",  "White", "(Unsupported)"},
//             {"-bg",  "<color>", "BackGround",  "White", ""},
//             {"-brb", "<base>", "BarBase",  "0", "(Unsupported)"},
//             {"-brw", "<width>", "BarWidth",  "1", ""},
//             {"-bw",  "<size>", "BorderSize",  "1", "(Unsupported)"},
//             {"-fg",  "<color>", "Foreground",  "Black", ""},
//             {"-gw",  "<pixels>", "GridStyle",  "1", "(Unsupported)"},
//             {"-lf",  "<fontname>", "LabelFont",  "helvetica-12", ""},
//             {"-lw",  "<width>", "LineWidth",  "0", "(Unsupported)"},
//             {"-lx",  "<xl,xh>", "XLowLimit, XHighLimit",  "0", ""},
//             {"-ly",  "<yl,yh>", "YLowLimit, YHighLimit",  "0", ""},
//             // -o is not in the original X11 pxgraph.
//             {"-o",   "<output filename>", "",  "/tmp/t.ps", ""},
//             {"-t",   "<title>", "TitleText",  "An X Graph", ""},
//             {"-tf",  "<fontname>", "TitleFont",  "helvetica-b-14", ""},
//             {"-x",   "<unitName>", "XUnitText",  "X", ""},
//             {"-y",   "<unitName>", "YUnitText",  "Y", ""},
//             {"-zg",  "<color>", "ZeroColor",  "Black", "(Unsupported)"},
//             {"-zw",  "<width>", "ZeroWidth",  "0", "(Unsupported)"},
//         };
// 
//         String commandFlags[][] = {
//             {"-bar", "BarGraph",  ""},
//             {"-bb", "BoundBox",  "(Ignored)"},
//             {"-binary", "Binary",  ""},
//             // -impulses is not in the original X11 pxgraph.
//             {"-impulses", "Impulses",  ""},
//             {"-lnx", "XLog",  ""},
//             {"-lny", "YLog",  ""},
//             {"-m", "Markers",  ""},
//             {"-M", "StyleMarkers",  ""},
//             {"-nl", "NoLines",  ""},
//             {"-p", "PixelMarkers",  ""},
//             {"-P", "LargePixel",  ""},
//             {"-rv", "ReverseVideo",  ""},
//             // -test is not in the original X11 pxgraph.  We use it for testing
//             {"-test", "Test",  ""},
//             {"-tk", "Ticks",  ""},
//             // -v is not in the original X11 pxgraph.
//             {"-v", "Version",  ""},
//             {"-version", "Version",  ""},
//         };
//         String result = "Usage: plot [ options ] [=WxH+X+Y] [file ...]\n\n"
//                 + " options that take values as second args:\n";
// 
//         int i;
//         for(i=0; i < commandOptions.length; i++) {
//             result += " " + commandOptions[i][0] +
//                     " " + commandOptions[i][1] +
//                     " " + commandOptions[i][4] + "\n";
//         }
//         result += "\nBoolean flags:\n";
//         for(i=0; i < commandFlags.length; i++) {
//             result += " " + commandFlags[i][0] +
//                     " " + commandFlags[i][2] + "\n";
//         }
//         result += "\nThe following pxgraph features are not supported:\n"
//                 + " * Directives in pxgraph input files\n"
//                 + " * Xresources\n"
//                 + "For complete documentation, see the pxgraph program docs.";
//         return result;
//     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ptolemy.math.SignalProcessing.AudioData _sound;
    private ByteArrayInputStream _instream;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MenuItem target = (MenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Play")) {
                _play();
            }
        }
    }
    
    // FIXME: This filter doesn't work.  Why?
    private class SoundFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            if (name.endsWith(".au")) return true;
            return false;
        }
    }
}

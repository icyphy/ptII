/* A viewer for audio files.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.media;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import ptolemy.plot.Plot;
import ptolemy.plot.PlotApplication;
import ptolemy.util.StringUtilities;
import sun.audio.AudioPlayer;

///////////////////////////////////////////////////////////////////
//// AudioViewer

/**
 Display sound files.

 @see PlotApplication
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class AudioViewer extends PlotApplication {
    /** Construct an audio plot with no command-line arguments.
     *  @exception Exception Not thrown in this base class.
     */
    public AudioViewer() throws Exception {
        this(null);
    }

    /** Construct an audio plot with no command-line arguments.
     *  @param args The command line arguments that are handed to
     *  PlotApplication.
     *  @exception Exception If the command-line arguments have problems.
     */
    public AudioViewer(String[] args) throws Exception {
        super(args);

        JMenuItem play = new JMenuItem("Play", KeyEvent.VK_P);
        play.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                Event.CTRL_MASK));
        play.setActionCommand("Play");

        PlayListener playlistener = new PlayListener();
        play.addActionListener(playlistener);
        _specialMenu.add(play);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Release an resources. */
    public void cleanup() {
        if (_dataInputStream != null) {
            try {
                _dataInputStream.close();
            } catch (Throwable throwable) {
                System.out.println("Ignoring failure to close stream " + "on '"
                        + _dataInputStream + "'");
                throwable.printStackTrace();
            }
        }
    }

    /** Create a new plot window and map it to the screen.
     *  @param args The command line arguments that are eventually
     *  passed to PlotApplication.
     */
    public static void main(String[] args) {
        AudioViewer plot = null;

        try {
            plot = new AudioViewer(args);
            plot.setTitle("Ptolemy Audio Viewer");
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        } finally {
            if (plot != null) {
                plot.cleanup();
            }
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
                "Ptolemy AudioViewer (ptaudio program)\n"
                        + "By: Edward A. Lee\n"
                        + "Version 2.0, Build: "
                        + "$Id$"
                        + "\n\n"
                        + "For more information, see\n"
                        + "http://ptolemy.eecs.berkeley.edu/java/ptplot",
                        "About Ptolemy AudioViewer",
                        JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display some help.
     */
    @Override
    protected void _help() {
        JOptionPane
        .showMessageDialog(this, "Use Control-P to play the sound",
                "Usage of Ptolemy AudioViewer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Read the specified stream.  This method checks to see whether
     *  the data is PlotML data, and if so, creates a parser to read it.
     *  If not, it defers to the parent class to read it.
     *  @param base The base for relative file references, or null if
     *   there are not relative file references.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    @Override
    protected void _read(URL base, InputStream in) throws IOException {
        try {
            _dataInputStream = new DataInputStream(in);
            _sound = new Audio(_dataInputStream);

            // Configure the plot.
            Plot plt = (Plot) plot;
            plt.clear(true);
            plt.setXRange(0, (_sound.size - 1) / 8000.0);
            plt.setXLabel("Time in seconds");
            plt.setYRange(-1.0, 1.0);

            double[] pltdata = _sound.toDouble(0);

            if (pltdata != null) {
                plt.addPoint(0, 0, pltdata[0], false);

                for (int i = 1; i < pltdata.length; i++) {
                    plt.addPoint(0, i / 8000.0, pltdata[i], true);
                }
            }

            plt.repaint();
        } catch (IOException ex) {
            cleanup();

            // FIXME: fill in stack trace?
            IOException newException = new IOException();
            newException.initCause(ex);
            throw newException;
        }
    }

    /** Play the sound.
     */
    protected void _play() {
        if (_instream == null) {
            // Fill the iobuffer with audio data.
            ByteArrayOutputStream out = new ByteArrayOutputStream(_sound.size);
            DataOutputStream dataOutputStream = null;

            try {
                dataOutputStream = new DataOutputStream(out);
                _sound.writeRaw(dataOutputStream);
            } catch (IOException ex) {
                throw new RuntimeException(
                        "Failed to convert audio data to stream.");
            } finally {
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on '" + dataOutputStream + "'");
                        throwable.printStackTrace();
                    }
                }
            }

            byte[] _iobuffer = out.toByteArray();
            _instream = new ByteArrayInputStream(_iobuffer);
        }

        _instream.reset();
        AudioPlayer.player.start(_instream);
    }

    /** Save the plot to the current file, determined by the _directory
     *  and _file protected variables.
     */
    @Override
    protected void _save() {
        if (_file != null) {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(_file);
                _sound.write(new DataOutputStream(out));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing file \""
                        + _file + "\". " + ex, "AudioViewer error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ex2) {
                        JOptionPane.showMessageDialog(this,
                                "Error closing file \"" + _file + "\". " + ex2,
                                "AudioViewer error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            _saveAs();
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    @Override
    protected String _usage() {
        String result = "Usage: ptaudio file";
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private DataInputStream _dataInputStream;

    private Audio _sound;

    private ByteArrayInputStream _instream;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private class PlayListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem) e.getSource();
            String actionCommand = target.getActionCommand();

            if (actionCommand.equals("Play")) {
                _play();
            }
        }
    }
}

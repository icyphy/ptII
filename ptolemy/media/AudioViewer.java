/* A viewer for audio files.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.media;

import ptolemy.plot.*;

import java.awt.Event;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import sun.audio.AudioPlayer;

//////////////////////////////////////////////////////////////////////////
//// AudioViewer
/**
Display sound files.

@see PlotApplication
@author Edward A. Lee
@version $Id$
*/
public class AudioViewer extends PlotApplication {

    /** Construct an audio plot with no command-line arguments.
     *  @exception Exception Not thrown in this base class.
     */
    public AudioViewer() throws Exception {
        this(null);
    }

    /** Construct an audio plot with no command-line arguments.
     *  @exception Exception If the command-line arguments have problems.
     */
    public AudioViewer(String args[]) throws Exception {
        super(args);
        JMenuItem play = new JMenuItem("Play", KeyEvent.VK_P);
        play.setAccelerator(
            KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
        play.setActionCommand("Play");
        PlayListener playlistener = new PlayListener();
        play.addActionListener(playlistener);
        _specialMenu.add(play);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new plot window and map it to the screen.
     */
    public static void main(String args[]) {
        try {
            AudioViewer plot = new AudioViewer(args);
            plot.setTitle("Ptolemy Audio Viewer");
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

    /** Display basic information about the application.
     */
    protected void _about() {
        JOptionPane.showMessageDialog(this,
                "Ptolemy AudioViewer (ptaudio program)\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "Version 2.0, Build: " +
                "$Id$" +
                "\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot",
                "About Ptolemy AudioViewer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Display some help.
     */
    protected void _help() {
        JOptionPane.showMessageDialog(this,
                "Use Control-P to play the sound",
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
    protected void _read(URL base, InputStream in) throws IOException {
        _sound = new Audio(new DataInputStream(in));
        // Configure the plot.
        Plot plt = (Plot)plot;
        plt.clear(true);
        plt.setXRange(0, (_sound.size - 1)/8000.0);
        plt.setXLabel("Time in seconds");
        plt.setYRange(-1.0, 1.0);
        double[] pltdata = _sound.toDouble(0);
        if (pltdata != null) {
            plt.addPoint(0, 0, pltdata[0], false);
            for (int i = 1; i < pltdata.length; i++) {
                plt.addPoint(0, i/8000.0, pltdata[i], true);
            }
        }
        plt.repaint();
    }

    /** Play the sound.
     */
    protected void _play() {
        if (_instream == null) {
            // Fill the iobuffer with audio data.
            ByteArrayOutputStream out =
                new ByteArrayOutputStream(_sound.size);
            try {
                _sound.writeRaw(new DataOutputStream(out));
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
     *  and _file protected variables.
     */
    protected void _save() {
        if (_file != null) {
            try {
                FileOutputStream fout = new FileOutputStream(_file);
                _sound.write(new DataOutputStream(fout));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                "Error writing file: " + ex,
                "AudioViewer error",
                JOptionPane.ERROR_MESSAGE);
            }
        } else {
            _saveAs();
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: ptaudio file";
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Audio _sound;
    private ByteArrayInputStream _instream;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class PlayListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JMenuItem target = (JMenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Play")) {
                _play();
            }
        }
    }
}

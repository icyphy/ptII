/* A viewer for audio files.

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

package ptolemy.media;

import ptolemy.plot.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import sun.audio.AudioPlayer;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// AudioViewer
/**
Display sound files.

@see PlotApplication
@author Edward A. Lee
@version $Id$
*/
public class AudioViewer extends PlotApplication {

    public AudioViewer() {
        super();
        MenuItem play = new MenuItem("Play", new MenuShortcut(KeyEvent.VK_H));
        play.setActionCommand("Play");
        PlayListener playlistener = new PlayListener();
        play.addActionListener(playlistener);
        _specialMenu.add(play);
    }

    public AudioViewer(String args[]) {
        super(args);
        MenuItem play = new MenuItem("Play", new MenuShortcut(KeyEvent.VK_H));
        play.setActionCommand("Play");
        PlayListener playlistener = new PlayListener();
        play.addActionListener(playlistener);
        _specialMenu.add(play);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Display basic information about the application.
     */
    protected void _about() {
        Message message = new Message(
                "Ptolemy AudioViewer (ptaudio program)\n" +
                "By: Edward A. Lee, eal@eecs.berkeley.edu\n" +
                "Version 2.0, Build: $Id$\n\n"+
                "For more information, see\n" +
                "http://ptolemy.eecs.berkeley.edu/java/ptplot");
        message.setTitle("About Ptolemy AudioViewer");
    }

    /** Display some help.
     */
    protected void _help() {
        Message message = new Message("Use Control-P to play the sound");
        message.setTitle("Usage of Ptolemy AudioViewer");
    }

    /** Open a new file and plot its data.
     */
    protected void _open() {
        FileDialog filedialog = new FileDialog(this, "Select a sound file");
        filedialog.setFilenameFilter(new AudioViewerFilenameFilter());
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
            _sound = new Audio(
                    new DataInputStream( new FileInputStream(file)));
        } catch (FileNotFoundException ex) {
            Message msg = new Message("File not found: " + ex);
        } catch (IOException ex) {
            Message msg = new Message("Error reading input: " + ex);
        }
        _filename = filename;

        // Configure the plot.
        Plot plt = (Plot)plot;
        plt.clear(true);
        plt.setTitle(new String(_sound.info));
        setTitle(filename);
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
     *  and _filename protected variables.
     */
    protected void _save() {
        if (_filename != null) {
            File file = new File(_directory, _filename);
            try {
                FileOutputStream fout = new FileOutputStream(file);
                _sound.write(new DataOutputStream(fout));
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
            MenuItem target = (MenuItem)e.getSource();
            String actionCommand = target.getActionCommand();
            if (actionCommand.equals("Play")) {
                _play();
            }
        }
    }

    // FIXME: This filter doesn't work.  Why?
    private class AudioViewerFilenameFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            if (name.endsWith(".au")) return true;
            return false;
        }
    }
}

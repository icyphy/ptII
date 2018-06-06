/* Helper for the clip player.

   Copyright (c) 2017 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.audio;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;

/** Helper for the ClipPlayer in the audio.js module.
 *  See the module for documentation.
 *
 *  Note that this class requires a graphical display because
 *  instantiating a JFXPanel attempts to connect to the display.
 *
 *  @author Elizabeth Osyk
 *  @version $Id: CliPlayerHelper.java 75850 2017-04-03 21:53:00Z cxh $
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 *
 */
public class ClipPlayerHelper extends HelperBase {

    /** Construct a new ClipPlayerHelper.
     *
     * @param actor  The actor associated with this ClipPlayerHelper.
     * @param helping The Javascript object being helped.
     */
    public ClipPlayerHelper(Object actor, ScriptObjectMirror helping) {
        super(actor, helping);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Play the sound clip.
     */
    public void play() {
        if (_player == null) {
            _error("No URL specified.  Cannot play clip.");
        } else {
            if (_player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
                _player.stop();
            }
            _player.play();
        }
    }

    /** Set the URL of the clip to load and construct a new MediaPlayer to it.
     *
     * @param url The URL of the clip to load.
     */
    public void setURL(String url) {
        if (_player != null
                && _player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            _player.stop();
        }

        // Wait for stop callback?
        try {
            _player = new MediaPlayer(new Media(url));
        } catch (UnsupportedOperationException ex) {
            throw new UnsupportedOperationException(
                    "Failed to create a Media or MediaPlayer based on \"" + url
                            + "\". Note that Java 1.8.0 before 8u72 have a bug that prevent https from working."
                            + "The solution is to update to a version of the Java Development Kit (JDK) after 1.8.0_72, "
                            + "See https://bugs.openjdk.java.net/browse/JDK-8091132.",
                    ex);
        }
        _player.setOnError(new Runnable() {
            @Override
            public void run() {
                if (_player != null && _player.getStatus()
                        .equals(MediaPlayer.Status.PLAYING)) {
                    _player.stop();
                }
                _error("Error in ClipPlayer: "
                        + _player.getError().getMessage(), _player.getError());
            }
        });

        // Stop the player at the end of the clip so we can emit a "done" event on stop.
        _player.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                _player.stop();
            }
        });

        // Emit a "done" event on stop.
        _player.setOnStopped(new Runnable() {
            @Override
            public void run() {
                _currentObj.callMember("emit", "done", true);
            }
        });

        // For debugging.
        /*
          _player.setOnReady(new Runnable() {
          @Override
          public void run() {
          System.out.println("ClipPlayer is ready");
          }

          });
        */
    }

    /** Stop playback, if playing.
     */
    public void stop() {
        if (_player != null
                && _player.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            _player.stop();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    private static JFXPanel _fxPanel;

    /** Instantiate a JavaFXRuntime to avoid a "Toolkit not
     *  initialized" exception.
     *  Instantiating a JFXPanel requires that there is a graphical
     *  display.
     */
    static {
        try {
            _fxPanel = new JFXPanel();
        } catch (UnsupportedOperationException ex) {
            System.out.println(
                    "ClipPlayerHelper: failed to instantiate a JFXPanel, "
                            + "which can happen if a graphical display is not present: "
                            + ex);
        }
    }
    /** The sound clip player. */
    private MediaPlayer _player = null;
}

/* An actor that reads in audio samples and plays the audio data.

 @Copyright (c) 2000-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.javasound;

import java.io.IOException;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.media.javasound.LiveSound;

///////////////////////////////////////////////////////////////////
//// AudioPlayer

/**
 This actor reads audio samples and plays them. 

 <p>Specifically, the input stream that this actor reads is
 interpreted as consisting of audio samples. This actor writes this
 stream of audio samples to the audio output port of the computer,
 which typically consists of the computer speaker or the headphones
 output. The audio samples that are supplied to this actor should be
 doubles in the range [-1.0, 1.0]. Thus, the input port of this actor
 is of type DoubleToken. Any input tokens that are outside of the
 valid range will be hard-clipped to fall within the range [-1.0, 1.0]
 before they are written to the audio output port of the computer.</p>

 <p>This actor should be fired often enough to prevent underflow of
 the internal audio playback buffer. Underflow should be avoided,
 since it will result in audio discontinuities (heard as clicks)
 in the output. No exception will be thrown if underflow occurs.</p>

 <p>The following parameters should be set accordingly. In all cases,
 an exception is thrown if an illegal parameter value is used.  Note
 that these parameters may be changed while audio capture is
 active. If this actor is used in conjunction with an AudioCapture
 actor, changing a parameter of this actor will cause the
 corresponding parameter value of the AudioCapture actor to
 automatically be set to the same value. This behavior is required
 because the AudioCapture and AudioPlayer actors both share access to
 the audio hardware, which is associated with a single sample rate,
 bit resolution, and number of channels.</p>

 <ul>
 <li><i>sampleRate</i> should be set to desired sample rate, in Hz.
 The default value is 8000. Allowable values are 8000, 11025,
 22050, 44100, and 48000 Hz. Some sound cards support 96000 Hz
 operation, but this is not supported in Java.</li>
 <li><i>bitsPerSample</i> should be set to desired bit
 resolution. The default value is 16. Allowable values are 8 and 16.
 Some sound cards support 20 and 24 bit audio, but this is not
 supported in Java.</li>
 <li><i>channels</i> should be set to desired number of audio
 channels. Allowable values are 1 (for mono) and 2 (for stereo).
 The default value is 1. Some sound cards support more than two
 audio channels, but this is not supported in Java.</li>
 </ul>

 <p> If more than one AudioPlayer actor is used in model, the sounds
 produced by these actors will be interleaved. This may make sense,
 for example, if the actor is used in a ModalModel.</p>

 @author  Brian K. Vogel, Steve Neuendorffer
 @version  $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (chf)
 @see ptolemy.media.javasound.LiveSound
 @see AudioCapture
 @see AudioReader
 @see AudioWriter
 */
public class AudioPlayer extends LiveSoundActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioPlayer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        input.setMultiport(true);

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setExpression("transferSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This port must receive double tokens (in the
     * range of -1.0 to 1.0);
     */
    public TypedIOPort input;

    /** The input rate.
     */
    public Parameter input_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Obtain access to the audio playback hardware, and start playback.
     *  An exception will occur if there is a problem starting
     *  playback.
     *  @exception IllegalActionException If there is a problem
     *   beginning audio playback.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        try {
            if (!LiveSound.isPlaybackActive()) {
                // Set the correct parameters in LiveSound.
                _initializeAudio();
            }

            // Reallocate the arrays.
            if (_audioPutArray == null || _channels != _audioPutArray.length) {
                _audioPutArray = new double[_channels][];
            }

            for (int i = 0; i < _channels; i++) {
                _audioPutArray[i] = new double[_transferSize];
            }

            // Start audio playback.
            LiveSound.startPlayback(this);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Error initializing audio playback.");
        }
    }

    /** Return true if the actor has enough data to fire.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!super.prefire()) {
            return false;
        }

        for (int j = 0; j < _channels; j++) {
            if (!input.hasToken(j, _transferSize)) {
                return false;
            }
        }

        return true;
    }

    /** Read a block of inputs as given by the <i>transferSize</i>
     *  parameter from each input channel and send them to the audio
     *  hardware. If the audio buffer cannot accept them, then this
     *  method will stall the calling thread until it can.
     *  @exception IllegalActionException If there is a problem
     *   playing audio.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        for (int j = 0; j < _channels; j++) {
            // NOTE: inArray[j].length may be > count, in which case
            // only the first count tokens are valid.
            Token[] inputArray = input.get(j, _transferSize);

            // Convert to doubles.
            for (int element = 0; element < _transferSize; element++) {
                _audioPutArray[j][element] = ((DoubleToken) inputArray[element])
                        .doubleValue();
            }
        }

        try {
            // Write out samples to speaker.
            LiveSound.putSamples(this, _audioPutArray);
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Cannot playback audio.");
        }

        return super.postfire();
    }

    /** Stop audio playback and free up any audio resources used
     *  for audio playback.
     *  @exception IllegalActionException If there is a problem
     *   stopping audio playback.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        // Stop playback. Close any open sound files. Free
        // up audio system resources.
        if (LiveSound.isPlaybackActive()) {
            try {
                LiveSound.stopPlayback(this);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Cannot free audio resources.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double[][] _audioPutArray;
}

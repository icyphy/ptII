/* An actor that outputs audio samples that are captured from the
 audio input port of the computer.

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
package ptolemy.actor.lib.javasound;

import java.io.IOException;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.media.javasound.LiveSound;

///////////////////////////////////////////////////////////////////
//// AudioCapture

/**
 This actor sequentially outputs audio samples that are captured
 from the audio input port of the computer. The audio input port
 typically corresponds to either the microphone input, line-in,
 or cd audio from the cdrom or dvd drive. It is not possible to
 select the desired input port under Java. This must be done from
 the operating system. This actor should be fired often enough to
 prevent overflow of the internal audio capture buffer.
 Overflow should be avoided, since it will result in loss of
 data. Each captured audio sample is converted to a double that
 may range from -1.0 to 1.0. Thus, the output type of this actor
 is DoubleToken.
 <p>
 The following parameters should be set accordingly. In all cases,
 an exception is thrown if an illegal parameter value is used.
 Note that these parameters may be changed while audio playback
 is active. If this actor is used in conjunction with an
 AudioPlayer actor, changing a parameter will cause the corresponding
 parameter value of the AudioPlayer to automatically be set to the
 same value. This behavior is required because the AudioCapture and
 AudioPlayer actors both share access to the audio hardware, which
 is associated with a single sample rate, bit resolution, and
 number of channels.
 <ul>
 <li><i>sampleRate</i> should be set to the desired sample rate, in Hz.
 The default value is 8000. Allowable values are 8000, 11025,
 22050, 44100, and 48000 Hz. Note that Java does not support
 96000 Hz operation, even if the audio hardware supports it.
 <li><i>bitsPerSample</i> should be set to the desired bit
 resolution. The default value is 16. Allowable values are 8 and 16.
 Note that Java does not support 20 or 24 bit audio, even if the
 audio hardware supports it.
 <li><i>channels</i> should be set to desired number of audio
 channels. The default value is 1 (for mono audio). Allowable
 values are 1 and 2 (for stereo). Note that more than two
 channels of audio is not currently supported in Java, even if
 the audio hardware supports it.
 </ul>
 <p>
 It should be noted that at most one AudioCapture and one AudioPlayer
 actor may be used simultaneously. Otherwise, an exception will
 occur. This restriction may be lifted in a future version of
 this actor.
 <p>
 There are security issues involved with accessing files and audio
 resources in applets. Applets are not
 allowed to capture audio from the audio input port (e.g., the
 microphone) by default since this could present a security risk.
 Therefore, the actor will not run in an applet by default. The
 .java.policy file may be modified to grant applets more
 privileges.
 <p>
 Note: Requires Java 2 v1.3.0 or later.
 @author Brian K. Vogel, Christopher Hylands, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (vogel)
 @Pt.AcceptedRating Yellow (chf)
 @see ptolemy.media.javasound.LiveSound
 @see AudioPlayer
 @see ptolemy.media.javasound.SoundReader
 @see ptolemy.media.javasound.SoundWriter
 */
public class AudioCapture extends LiveSoundActor {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the parameters and initialize them to their default values.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioCapture(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.DOUBLE);
        trigger.setMultiport(true);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
        output.setMultiport(true);

        // Bert Rodiers suggests, "... treat this like the Minimum Actor. See
        // http://www.eecs.berkeley.edu/Pubs/TechRpts/2010/EECS-2010-120.pdf for details."
        output.setDefaultWidth(1);

        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate");
        output_tokenProductionRate.setTypeEquals(BaseType.INT);
        output_tokenProductionRate.setExpression("transferSize");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The trigger port.
     */
    public TypedIOPort trigger;

    /** The output port.  This will always produce double data,
     * between -1.0 and 1.0.
     */
    public TypedIOPort output;

    /** The output rate.
     */
    public Parameter output_tokenProductionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AudioCapture newObject = (AudioCapture) super.clone(workspace);

        newObject._audioSendArray = new DoubleToken[1];

        return newObject;
    }

    /** Read parameter values and begin the sound capture process.
     *  An exception will occur if there is a problem starting
     *  the audio capture. This will occur if another AudioCapture actor has
     *  already started capturing.
     *  @exception IllegalActionException If there is a problem
     *   starting audio capture.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        if (LiveSound.isCaptureActive()) {
            throw new IllegalActionException(
                    this,
                    "This actor cannot start audio capture because "
                            + "another actor currently has access to the audio "
                            + "capture resource. Only one AudioCapture actor may "
                            + "be used at a time.");
        }

        try {
            // Set the parameters of the audio system.
            _initializeAudio();

            LiveSound.startCapture(this);
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Cannot initialize audio capture.");
        }
    }

    /** Capture and output a single audio sample on each channel.
     *  This method causes audio samples to be captured from the audio
     *  input device (e.g., the microphone or line-in).
     *  One token is written to the output port in an invocation.
     *  This method should be invoked
     *  often enough to prevent overflow of the internal audio capture
     *  buffer. Overflow should be avoided, since it will result in loss
     *  of data. This method will block until the samples have been
     *  read, so it is not possible to invoke this method too
     *  frequently.
     *
     *  @return True
     *  @exception IllegalActionException If audio cannot be captured.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

        int count = _transferSize;

        if (count > _audioSendArray.length) {
            _audioSendArray = new DoubleToken[count];
        }

        try {
            // Read in audio data.
            _audioInDoubleArray = LiveSound.getSamples(this);
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Cannot capture audio.");
        }

        for (int j = 0; j < _channels; j++) {
            // Convert to DoubleToken.
            for (int element = 0; element < count; element++) {
                _audioSendArray[element] = new DoubleToken(
                        _audioInDoubleArray[j][element]);
            }

            output.send(j, _audioSendArray, count);
        }

        return super.postfire();
    }

    /** Stop capturing audio. Free up any system resources involved
     *  in the capturing process.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        // Stop capturing audio.
        if (LiveSound.isCaptureActive()) {
            try {
                LiveSound.stopCapture(this);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Error stopping audio capture.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double[][] _audioInDoubleArray;

    private DoubleToken[] _audioSendArray = new DoubleToken[1];
}

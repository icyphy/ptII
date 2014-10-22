/* An actor that writes audio samples to a sound file.

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

import ptolemy.actor.lib.Sink;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.media.javasound.SoundWriter;

///////////////////////////////////////////////////////////////////
//// AudioWriter

/**
 This actor reads audio samples from the input port and writes
 the samples to the specified sound file. Any existing file
 with the same name will be silently overwritten. The input port
 is of type DoubleToken. Each DoubleToken read from the input
 port represents one sample of the audio data and should be in
 the range [-1.0, 1.0]. Any samples that are outside of this range
 will be hard-clipped to fall within this range before they are
 written to the sound file. Single channel (mono) and two
 channel (stereo) formats are supported. For single channel
 audio, tokens are written to channel 0 of the output port. For
 stereo , tokens are written to channel 0 (left) and channel 1
 (right) of the output port.
 <p>
 The following parameters should be set to specify the format
 of the file to write. In all cases, an exception is thrown if
 an illegal parameter value is used. Note that if a parameter is
 changed while audio writing is active, all data written so far will
 be saved, and the sound file will be closed. Subsequent audio
 samples will then be written to a new sound file with the new
 parameter values.
 <p>
 <ul>
 <li><i>pathName</i> should be set to the name of the output
 file. Any existing file with the same name will be silently
 overwritten. Relative filenames are supported. The default value is
 "outfile.wav". The audio format to use is determined by the file
 extension. E.g., "outfile.wav" will create a WAV format file.
 The supported file formats are AU, WAV, and, AIFF.  For example,
 to write samples to a Sun AU format file with the name "test.au"
 in the directory "c:\tmp", this parameter should be set to the
 value c:\tmp\test.au. To write samples to a file with name "test.au"
 in the current directory, this parameter should be set to the value
 test.au.
 <li><i>channels</i> should be set to desired number of audio
 channels. Allowable values are 1 (for mono) and 2 (for stereo).
 The default value is 1.
 <li><i>sampleRate</i> should be set to desired sample rate,
 in Hz. The DoubleTokens read in by this actor will be
 interpreted as having this sample rate. Allowable values are
 8000, 11025, 22050, 44100, and 48000. The default value is 8000.
 <li><i>bitsPerSample</i> should be set to desired bit
 resolution. Allowable values are 8 and 16. The default value is 16.
 </ul>
 <p>
 There are security issues involved with accessing files and
 audio resources in applets. By default, applets are not
 allowed to write files. The .java.policy file may be modified
 to grant applets more privileges.
 <p>
 Note: Requires Java 2 v1.3.0 or later.
 @author  Brian K. Vogel
 @version  $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (vogel)
 @Pt.AcceptedRating Yellow (chf)
 @see AudioReader
 @see AudioCapture
 @see AudioPlayer
 */
public class AudioWriter extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);

        pathName = new StringAttribute(this, "pathName");
        pathName.setExpression("outfile.wav");

        sampleRate = new Parameter(this, "sampleRate", new IntToken(8000));
        sampleRate.setTypeEquals(BaseType.INT);

        bitsPerSample = new Parameter(this, "bitsPerSample", new IntToken(16));
        bitsPerSample.setTypeEquals(BaseType.INT);
        channels = new Parameter(this, "channels", new IntToken(1));
        channels.setTypeEquals(BaseType.INT);
        attributeChanged(channels);
        _curElement = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to. The default
     *  value of this parameter is "test.wav", which creates a file called
     *  test.wav in the current directory and writes samples to this file.
     *  <p>
     *  The audio format to use is determined by the file extension.
     *  E.g., "outfile.wav" will create a WAV format file.
     *  The supported file formats are AU, WAV, and, AIFF.
     *  <p>
     *  An exception will be occur if the path references an
     *  unsupported sound file.
     */
    public StringAttribute pathName;

    /** The desired sample rate to use, in Hz. Valid values
     *  include: 8000, 11025, 22050, 44100, and 48000.
     *  The default value of the sample rate is an IntToken equal
     *  to 8000.
     *  <p>
     *  An exception will be thrown if an illegal value is used.
     */
    public Parameter sampleRate;

    /** The desired number of bits per sample. The default value is
     *  an IntToken equal to 16. Supported values are 8 and 16.
     *  An exception will be thrown if an illegal value is
     *  used.
     */
    public Parameter bitsPerSample;

    /** The number of audio channels to use. Supported values are
     *  1 (single channel) and 2 (stereo).
     *  The default value is an IntToken equal to 1.
     *  An exception will be thrown if an illegal value is used.
     */
    public Parameter channels;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle change requests for all parameters. An exception is
     *  thrown if the requested change is not allowed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not
     *   allowed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == channels) {
            _channels = ((IntToken) channels.getToken()).intValue();

            if (_channels < 1) {
                throw new IllegalActionException(this,
                        "Attempt to set channels parameter to an illegal "
                                + "value of: " + _channels
                                + " . The value must be a "
                                + "positive integer.");
            }

            // Check if we need to reallocate.
            if (_inArray == null || _channels != _inArray.length) {
                _inArray = new Token[_channels][];
            }

            if (_audioPutArray == null || _channels != _audioPutArray.length) {
                _audioPutArray = new double[_channels][];
            }

            for (int i = 0; i < _channels; i++) {
                _audioPutArray[i] = new double[_putSampleSize];
            }
        } else {
            super.attributeChanged(attribute);
            return;
        }

        if (_safeToInitialize == true) {
            _initializeWriter();
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AudioWriter newObject = (AudioWriter) super.clone(workspace);

        newObject._audioPutArray = new double[newObject._channels][];
        newObject._inArray = new Token[newObject._channels][];

        return newObject;
    }

    /** Open a new audio file for writing. Any existing file
     *  with the same name will be overwritten.
     *  @exception IllegalActionException If the file cannot be opened,
     *   or if the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Initialize/Reinitialize audio resources.
        _initializeWriter();
        _safeToInitialize = true;
    }

    /** Invoke <i>count</i> iterations of this actor. If there
     *  at least <i>count</i> tokens on channel 0 (and also on
     *  channel 1 if stereo mode is used), then read <i>count</i>
     *  tokens from the corresponding channels and write the token
     *  values to the specified sound file. Otherwise, do nothing,
     *  and return a value of NOT_READY. Note that at most one token
     *  is read from each channel in an iteration.
     *  <p>
     *  This method should be called instead of the prefire(),
     *  fire(), and postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, return NOT_READY if there
     *   are not enough tokens on the input port, or throw an exception
     *   if there is a problem writing audio samples to the specified
     *   file.
     *  @see ptolemy.actor.Executable
     *  @exception IllegalActionException If there is a problem
     *   writing the audio sample(s) to the specified file.
     */
    @Override
    public int iterate(int count) throws IllegalActionException {
        for (int j = 0; j < _channels; j++) {
            if (input.hasToken(j, count)) {
                // NOTE: inArray[j].length may be > count, in which case
                // only the first count tokens are valid.
                _inArray[j] = input.get(j, count);
            } else {
                // Not enough tokens on the input port, so just return.
                return NOT_READY;
            }
        }

        // For each sample.
        for (int k = 0; k < count; k++) {
            // For each channel.
            for (int m = 0; m < _channels; m++) {
                // Keep writing samples until the array argument to
                // putSamples() is full, then call putSamples().
                // Array argument to putSamples() is not full yet,
                // so write another sample for each channel.
                _audioPutArray[m][_curElement] = ((DoubleToken) _inArray[m][k])
                        .doubleValue();
            }

            // Increment pointer.
            _curElement++;

            if (_curElement == _putSampleSize) {
                try {
                    // write out samples to speaker and/or file.
                    _soundWriter.putSamples(_audioPutArray);
                } catch (Exception ex) {
                    throw new IllegalActionException(this,
                            "Cannot write audio: \n" + ex.getMessage());
                }

                // Reset pointer to beginning of array.
                _curElement = 0;
            }
        }

        return COMPLETED;
    }

    /** If there is at least 1 token on channel 0 (and also on
     *  channel 1 if stereo mode is used), then read 1 token
     *  from the corresponding channels and write the token
     *  values to the specified sound file. Otherwise, do nothing,
     *  and return a value of false.
     *  @exception IllegalActionException If there is a problem
     *   writing the audio sample(s) to the specified file.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            return false;
        }
        int returnVal = iterate(1);

        if (returnVal == COMPLETED) {
            return true;
        } else if (returnVal == NOT_READY) {
            // This should never happen.
            throw new IllegalActionException(this, "Actor "
                    + "is not ready to fire.");
        } else if (returnVal == STOP_ITERATING) {
            return false;
        }

        return false;
    }

    /** Set up the number channels to use.
     *  for use in the postfire() method.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _channels = ((IntToken) channels.getToken()).intValue();
    }

    /** Close the specified file.
     *  @exception IllegalActionException If there is a problem
     *   closing the file.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        // Close any open sound files.
        if (_soundWriter != null) {
            try {
                _soundWriter.closeFile();
            } catch (IOException ex) {
                throw new IllegalActionException(this, "Error closing file:\n"
                        + ex.getMessage());
            }
        }

        _safeToInitialize = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize/Reinitialize audio resources. First close any
     *  open sound files. Then reread all parameters, create a
     *  new SoundWriter object.
     *  <p>
     *  This method is synchronized since it is not safe to call
     *  SoundWriter methods while this method is executing.
     *  @exception IllegalActionException If there is a problem
     *   initializing the SoundWriter object.
     */
    private synchronized void _initializeWriter() throws IllegalActionException {
        // Close any open sound files. Free
        // up audio system resources.
        if (_soundWriter != null) {
            try {
                _soundWriter.closeFile();
            } catch (IOException ex) {
                throw new IllegalActionException(this, "Cannot write audio: \n"
                        + ex.getMessage());
            }
        }

        _putSampleSize = 64;

        for (int i = 0; i < _channels; i++) {
            _audioPutArray[i] = new double[_putSampleSize];
        }

        String pathNameString = pathName.getExpression();

        // Write audio data to a file.
        int sampleRateInt = ((IntToken) sampleRate.getToken()).intValue();
        int bitsPerSampleInt = ((IntToken) bitsPerSample.getToken()).intValue();
        int channelsInt = ((IntToken) channels.getToken()).intValue();
        int putSamplesSize = _putSampleSize;

        _soundWriter = new SoundWriter(pathNameString, sampleRateInt,
                bitsPerSampleInt, channelsInt, putSamplesSize);
        _curElement = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private SoundWriter _soundWriter;

    private int _channels;

    private int _putSampleSize;

    private double[][] _audioPutArray;

    // Pointer to the current sample of the array parameter of
    // putSamples() method of SoundWriter.
    private int _curElement;

    private Token[][] _inArray;

    private boolean _safeToInitialize = false;
}

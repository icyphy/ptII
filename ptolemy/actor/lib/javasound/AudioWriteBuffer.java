/* An actor that writes audio samples to a buffer and saves the buffer
 as a sound file on wrapup.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
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
import ptolemy.media.javasound.SoundWriter;

///////////////////////////////////////////////////////////////////
//// AudioWriteBuffer

/**
 This actor writes audio samples to a buffer and saves the buffer
 to a sound file on wrapup. In each iteration, if there is a token
 available on the <i>data</i> and <i>address</i> ports, then the
 data DoubleToken is written to the buffer location specified by
 the address IntToken. The valid address range is 0 to
 <i>bufferLength</i> -1. If an invalid address is specified, then
 the data value will be ignored and no data will be written.
 The audio samples in the buffer will be written to the sound file
 specified by the <i>pathName</i> parameter on wrapup.
 <p>
 Any existing file with the same name will be silently
 overwritten. The data port is of type DoubleToken. Each DoubleToken
 read from the data port represents one sample of the audio data and
 should be in the range [-1.0, 1.0]. Any samples that are outside of
 this range will be hard-clipped to fall within this range before
 they are written to the sound file. Single channel (mono) audio is
 supported but stereo is not supported.
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
 The default value is 1. Only single-channel audio is currently
 supported. This limitation will be removed in a future version
 of this actor.
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
 <p>FIXME: this should extend AudioWriter
 @author  Brian K. Vogel
 @version  $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (vogel)
 */
public class AudioWriteBuffer extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioWriteBuffer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        address = new TypedIOPort(this, "address", true, false);
        address.setMultiport(true);
        address.setTypeEquals(BaseType.INT);
        data = new TypedIOPort(this, "data", true, false);
        data.setMultiport(true);
        data.setTypeEquals(BaseType.DOUBLE);

        pathName = new StringAttribute(this, "pathName");
        pathName.setExpression("outfile.wav");

        sampleRate = new Parameter(this, "sampleRate", new IntToken(8000));
        sampleRate.setTypeEquals(BaseType.INT);

        bitsPerSample = new Parameter(this, "bitsPerSample", new IntToken(16));
        bitsPerSample.setTypeEquals(BaseType.INT);
        channels = new Parameter(this, "channels", new IntToken(1));
        channels.setTypeEquals(BaseType.INT);
        attributeChanged(channels);
        bufferLength = new Parameter(this, "bufferLength", new IntToken(8000));
        overwrite = new Parameter(this, "overwrite", new BooleanToken(true));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The address port, which is a multiport.
     */
    public TypedIOPort address;

    /** The data port, which is a multiport.
     */
    public TypedIOPort data;

    /** The length of the audio buffer to use. The default value
     *  is 8000.
     */
    public Parameter bufferLength;

    /** The write mode to use. If this parameter is set to true, then
     *  a write to a buffer element overwrites the old value in the
     *  buffer. Otherwise, a write adds the new value to the old value
     *  of the buffer element.
     */
    public Parameter overwrite;

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
        } else if (attribute == bufferLength) {
            if (_safeToInitialize == true) {
                //try {
                _initializeWriter();

                //} catch (IOException ex) {
                //throw new IllegalActionException(this,
                //      "Cannot read audio:\n" +
                //      ex);
                //}
            }
        } else {
            super.attributeChanged(attribute);
            return;
        }

        if (_safeToInitialize == true) {
            _initializeWriter();
        }
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

    /** If there is a token available on the <i>data</i> and
     *  <i>address</i> ports, then the data DoubleToken is written
     *  to the buffer location specified by the address IntToken.
     *  The valid address range is 0 to <i>bufferLength</i> -1. If an
     *  invalid address is specified, then the data value will be
     *  ignored and no data will be written. The audio samples in the
     *  buffer will be written to the sound file specified by the
     *  <i>pathName</i> parameter on wrapup.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (address.hasToken(0) && data.hasToken(0)) {
            int addressValue = ((IntToken) address.get(0)).intValue();

            if (addressValue < 0) {
                // invalid index, so write nothing.
                // still need to consume the data token, however.
                data.get(0);
            } else if (addressValue > _audioBuffer.length - 1) {
                // invalid index. exceeds buffer length, so
                // do nothing.
                // still need to consume the data token, however.
                data.get(0);
            } else {
                // Read one token from the data port and write the
                // token to the specified address in the buffer.
                double sampleValue = ((DoubleToken) data.get(0)).doubleValue();
                boolean overwriteMode = ((BooleanToken) overwrite.getToken())
                        .booleanValue();

                if (overwriteMode == true) {
                    // Overwrite the element at the specified
                    // address.
                    _audioBuffer[addressValue] = sampleValue;
                } else {
                    // Add the data value to the element at the
                    // specified address.
                    _audioBuffer[addressValue] += sampleValue;
                }
            }
        } else if (address.hasToken(0)) {
            System.out.println(getName()
                    + "WARNING: address port does not have a token!");
        } else if (data.hasToken(0)) {
            System.out.println(getName()
                    + "WARNING: data port does not have a token!");
        } else {
            System.out.println(getName()
                    + "WARNING: neither data port has a token!");
        }
    }

    /** Return true unless super.postfire() returns false.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            return false;
        }
        return true;
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
                for (double element : _audioBuffer) {
                    _audioPutArray[0][0] = element;
                    _soundWriter.putSamples(_audioPutArray);
                }

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

        _putSampleSize = 1;

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

        int length = ((IntToken) bufferLength.getToken()).intValue();
        _audioBuffer = new double[length];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private SoundWriter _soundWriter;

    private int _channels;

    private int _putSampleSize;

    private double[][] _audioPutArray;

    private Token[][] _inArray;

    private boolean _safeToInitialize = false;

    private double[] _audioBuffer;
}

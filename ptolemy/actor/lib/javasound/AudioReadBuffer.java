/* An actor that reads a sound file into a buffer and outputs the
 audio sample at the specified location in the buffer.

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

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.media.javasound.SoundReader;

//import java.net.*;
//import java.util.*;
//import javax.sound.sampled.*;
///////////////////////////////////////////////////////////////////
//// AudioReadBuffer

/**
 This actor reads a sound file into a buffer and outputs the the
 sample value at the specified buffer element. The sound file
 is read from the URL specified by the <i>sourceURL</i>
 parameter.  Although the sound file must be specified
 as a URL, it is still possible to specify files on the local
 file system.
 <p>
 In each iteration, an IntToken is read from the input port, if
 there is a token available. This token specifies the address of
 the buffer element to read from. The address is zero-based, so
 address 0 corresponds to the first element and addres
 <i>bufferLength</i> -1 corresponds to the last element. The sample
 value at the specified buffer location is converted to a DoubleToken
 and sent to the output port.
 <p>
 If an element outside of the valid address range is specified, then
 a DoubleToken with value 0.0 is output. If the the specified address
 is greater than <i>bufferLength</i> -1, then this actor will return
 false in postfire.
 <p>
 This actor does not currently support multichannel sound files, so
 if a stereo sound file is specified, only the left channel will be
 used. This limitation may be lifted in a future version of this actor.
 <p>
 The audio samples that are read from the file are
 converted to DoubleTokens that may range from [-1.0, 1.0].
 Thus, the output type of this actor is DoubleToken.
 <p>
 <b>Usage</b>
 <p>
 The <i>sourceURL</i> parameter should be set to the name of the file,
 specified as a fully qualified URL. It is possible to load a file
 from the local file system by using the prefix "file://" instead of
 "http://". Relative file paths are allowed. To specify a file
 relative to the current directory, use "../" or "./". For example,
 if the current directory contains a file called "test.wav", then
 <i>sourceURL</i> should be set to "file:./test.wav". If the parent
 directory contains a file called "test.wav", then <i>sourceURL</i>
 should be set to "file:../test.wav". To reference the file
 test.wav, located at "/tmp/test.wav", <i>sourceURL</i>
 should be set to "file:///tmp/test.wav" The default value is
 "file:///tmp/test.wav".
 <p>
 There are security issues involved with accessing files and audio
 resources in applets. Applets are only allowed access to files
 specified by a URL and located on the machine from which the
 applet is loaded. The .java.policy file may be modified to grant
 applets more privileges.
 <p>FIXME: This actor should extend AudioReadBuffer?
 <p>
 Note: Requires Java 2 v1.3.0 or later.
 @author Brian K. Vogel
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (vogel)
 @Pt.AcceptedRating Red (vogel)
 @see ptolemy.media.javasound.LiveSound
 */
public class AudioReadBuffer extends Transformer {
    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the parameters and initialize them to their default values.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AudioReadBuffer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(BaseType.DOUBLE);
        output.setMultiport(true);
        sourceURL = new StringAttribute(this, "sourceURL");
        bufferLength = new Parameter(this, "bufferLength", new IntToken(8000));
        sourceURL.setExpression("file:///tmp/test.wav");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     parameters                            ////

    /** The URL of the file to read from. The default value of this
     *  parameter is the URL "file:///tmp/test.wav".
     *  Supported file formats are  WAV, AU, and AIFF. The sound
     *  file format is determined from the file extension.
     *  <p>
     *  An exception will occur if the path references a
     *  non-existent or unsupported sound file.
     */
    public StringAttribute sourceURL;

    /** The length of the audio buffer to use. The default value
     *  is 8000.
     */
    public Parameter bufferLength;

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
        if (attribute == sourceURL) {
            if (_safeToInitialize == true) {
                try {
                    _initializeReader();
                } catch (IOException ex) {
                    throw new IllegalActionException(this,
                            "Cannot read audio:\n" + ex);
                }
            }
        } else if (attribute == bufferLength) {
            if (_safeToInitialize == true) {
                try {
                    _initializeReader();
                } catch (IOException ex) {
                    throw new IllegalActionException(this,
                            "Cannot read audio:\n" + ex);
                }
            }
        } else {
            super.attributeChanged(attribute);
            return;
        }
    }

    /** Open the sound file specified by the URL for reading.
     *  @exception IllegalActionException If there is a problem opening
     *   the specified URL, or if the file has an unsupported audio
     *   format.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        try {
            _initializeReader();
        } catch (IOException ex) {
            throw new IllegalActionException(this,
                    "Cannot open the specified URL: " + ex);
        }

        _safeToInitialize = true;
    }

    /** Read the buffer address from the input port and output
     *  the corresponding audio sample, if there is a token
     *  available on the input port.
     *  <p>
     *  The IntToken read from the input port specifies the buffer
     *  address to read from. The valid address range is 0 to
     *  <i>bufferLength</i> -1. If an invalid address is read,
     *  then the value 0.0 is output.  If the the specified address
     *  is greater than <i>bufferLength</i> -1, then this actor will return
     *  false in postfire. The audio sample is
     *  converted to a DoubleToken with range [-1.0, 1.0] and
     *  sent to the output. Note that if a stereo sound file is
     *  specified, only the left channel will be used.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            int in = ((IntToken) input.get(0)).intValue();

            if (in < 0) {
                // invalid index, so just output a 0.0
                output.send(0, new DoubleToken(0.0));
            } else if (in > _audioBuffer.length - 1) {
                // invalid index. exceeds buffer length, so
                // just output a 0.0 and return false in postfire.
                _postfireReturn = false;
                output.send(0, new DoubleToken(0.0));
            } else {
                double sampleValue = _audioBuffer[in];
                output.send(0, new DoubleToken(sampleValue));
            }
        }
    }

    /** This method causes one audio sample per channel to be
     *  read from the specified file. Each sample is converted to
     *  a double token, with a maximum range of -1.0 to 1.0.
     *  One double token per channel is written to the output port.
     *  @return True if there are samples available from the
     *  audio source. False if there are no more samples (end
     *  of sound file reached).
     *  @exception IllegalActionException If there is a problem reading
     *   from the specified sound file.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (!super.postfire()) {
            return false;
        }
        return _postfireReturn;
    }

    /** Free up any system resources involved in the audio
     *  reading process and close any open sound files.
     *
     *  @exception IllegalActionException If there is a
     *   problem closing the file.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        // Stop capturing audio.
        if (_soundReader != null) {
            try {
                _soundReader.closeFile();
            } catch (IOException ex) {
                throw new IllegalActionException(this,
                        "Problem closing sound file: \n" + ex.getMessage());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize/Reinitialize audio reading. First close any
     *  open files. Then read the <i>sourceURL</i> parameter and
     *  open the file specified by this parameter.
     *  <p>
     *  This method is synchronized since it is not safe to call
     *  other SoundReader methods while this method is executing.
     *  @exception IllegalActionException If there is a problem initializing
     *   the audio reader.
     */
    private synchronized void _initializeReader() throws IOException,
    IllegalActionException {
        if (_soundReader != null) {
            _soundReader.closeFile();
        }

        // Load audio from a URL.
        String theURL = sourceURL.getExpression();

        // Each read this many samples per channel when
        // _soundReader.getSamples() is called.
        // This value was chosen somewhat arbitrarily.
        int getSamplesArraySize = 1;
        _soundReader = new SoundReader(theURL, getSamplesArraySize);

        // Read the number of audio channels and set
        // parameter accordingly.
        //_channels = _soundReader.getChannels();

        int length = ((IntToken) bufferLength.getToken()).intValue();
        _audioBuffer = new double[length];

        // Read all of the samples into an array.
        double[][] samples /* Avoid Dead Store: = new double[_channels][getSamplesArraySize]*/;
        boolean done = false;

        for (int i = 0; i < length; i++) {
            if (!done) {
                samples = _soundReader.getSamples();

                // Write the sample to the buffer.
                if (samples != null) {
                    _audioBuffer[i] = samples[0][0];
                } else {
                    done = true;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private SoundReader _soundReader;

    //private int _channels;

    private boolean _safeToInitialize = false;

    private double[] _audioBuffer;

    private boolean _postfireReturn = true;
}

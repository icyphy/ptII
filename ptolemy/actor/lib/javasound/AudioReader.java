/* An actor that outputs the sequence of sample values from a
   sound file specified as a URL.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (chf@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.javasound;

import java.io.IOException;

import ptolemy.actor.lib.Source;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.media.javasound.SoundReader;


/////////////////////////////////////////////////////////////////
//// AudioReader
/**

This actor outputs samples from a sound file as doubles in
the range [-1.0, 1.0]. If the file has multiple channels of
output data, then the separate channels are sent on successive
output channels.  If the output has more channels than there
are channels in the audio file, then nothing will be send
on the output channels where there is no corresponding
output data.
<p>
The <i>fileOrURL</i> parameter should be set to the name of the file
or a URL, in any form accepted by FileAttribute. The default initial value is
<code>$CLASSPATH/ptolemy/actor/lib/javasound/voice.wav</code>,
which refers to a file that is found relative to the classpath.
<p>
Supported file formats are  WAV, AU, and AIFF. The sound
file format is determined from the file extension.
<p>
When the end of the file is reached, postfire() return false, which
in some domains will cause the model to stop executing (e.g. SDF),
and in some will prevent further firings of this actor (e.g. DE).
<p>
There are security issues involved with accessing files and audio
resources in applets. Applets are only allowed access to files
specified by a URL and located on the machine from which the
applet is loaded. The .java.policy file may be modified to grant
applets more privileges.
<p>
Note: Requires Java 2 v1.3.0 or later.

@author Brian K. Vogel, Christopher Hylands, Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.media.javasound.LiveSound
@see ptolemy.media.javasound.SoundWriter
@see ptolemy.media.javasound.SoundPlayback
*/
public class AudioReader extends Source {

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
    public AudioReader(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        fileOrURL = new FileParameter(this, "fileOrURL");
        // We use voice.wav so that we can include the voice.wav file
        // in the jar file for use under Web Start.
        fileOrURL.setExpression(
                "$CLASSPATH/ptolemy/actor/lib/javasound/voice.wav");

        // Set the type of the output port.
        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fileOrURL</i> and there is an
     *  open file being read, then close that file and open the new one;
     *  do nothing if the file name is the same as the previous value of
     *  this attribute.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileOrURL</i> and the file cannot be opened, or the previously
     *   opened file cannot be closed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            // NOTE: We do not want to close the file if the file
            // has not in fact changed.  We check this by just comparing
            // name, which is not perfect...
            String newFileOrURL = ((StringToken)fileOrURL.getToken()).stringValue();
            if (_previousFileOrURL != null
                    && !newFileOrURL.equals(_previousFileOrURL)) {
                _previousFileOrURL = newFileOrURL;
                _openReader();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Open the sound file specified by the URL for reading.
     *  @exception IllegalActionException If there is a problem opening
     *   the specified URL, or if the file has an unsupported audio
     *   format.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if (_firedSinceWrapup || _soundReader == null) {
            // It would be better if there were a way to reset the
            // input stream, but apparently there is not, short of
            // closing it and reopening it.
            fileOrURL.close();
            _openReader();
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
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        if (_reachedEOF || _soundReader == null) {
            return false;
        }
        _firedSinceWrapup = true;
        // Check whether we need to reallocate the output token array.
        if (_audioSendArray == null
                || _channels > _audioSendArray.length) {
            _audioSendArray = new DoubleToken[_channels];
        }
        // Copy a sample to the output array for each channel.
        for (int j = 0; j < _channels; j++) {
            _audioSendArray[j] = new DoubleToken(
                    _audioIn[j][_sampleIndex]);
        }
   
        _sampleIndex++;
       
        // Check whether we still have at least one sample left.
        // NOTE: This assumes that all channels have the same length
        // as the 0 channel.
        if ((_audioIn[0].length - _sampleIndex) <= 0) {
            // We just ran out of samples.
            // Need to read more data.
            try {
                // Read in audio data.
                _audioIn = _soundReader.getSamples();
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Unable to get samples from the file.");
            }
            _sampleIndex = 0;
            // Check that the read was successful
            if (_audioIn != null) {
                _reachedEOF = false;
            } else {
                _reachedEOF = true;
            }
        }
    
        // Send outputs.
        for (int j = 0; j < _channels; j++) {
            output.send(j, _audioSendArray[j]);
        }
        if (_reachedEOF) {
            return false;
        } else {
            return true;
        }
    }

    /** Return false if there is no more data available in the file.
     *  Otherwise, return whatever the superclass returns.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        _firedSinceWrapup = true;
        if (_reachedEOF || _soundReader == null) return false;
        else return super.prefire();
    }

    /** Free up any system resources involved in the audio
     *  reading process and close any open sound files.
     *
     *  @exception IllegalActionException If there is a
     *   problem closing the file.
     */
    public void wrapup() throws IllegalActionException {
        fileOrURL.close();
        _soundReader = null;
        _firedSinceWrapup = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize/Reinitialize audio reading. First close any
     *  open files. Then read the <i>fileOrURL</i> parameter and
     *  open the file specified by this parameter.
     *  @exception IllegalActionException If there is a problem initializing
     *   the audio reader.
     */
    protected void _openReader() throws IllegalActionException {
        fileOrURL.close();
        // Ignore if the fileOrUL is blank.
        if (fileOrURL.getExpression().trim().equals("")) {
            _soundReader = null;
            _reachedEOF = true;
        } else {
            // Each read this many samples per channel when
            // _soundReader.getSamples() is called.
            // This value was chosen arbitrarily.
            int getSamplesArraySize = 64;
            try {
                _soundReader = new SoundReader(fileOrURL.asURL(),
                        getSamplesArraySize);
            } catch (IOException ex) {
                String newFileOrURL = ((StringToken)fileOrURL.getToken()).stringValue();
                throw new IllegalActionException(this, ex,
                        "Cannot open fileOrURL '"
                        + newFileOrURL
                        + "'.");
            }
            // Get the number of audio channels.
            _channels = _soundReader.getChannels();

            // Begin immediately reading data so that we stay at
            // least one sample ahead.  This is important so that
            // postfire() will return false when the last sample
            // in the file is encountered, rather than on the next
            // iteration.
            try {
                // Read in audio data.
                _audioIn = _soundReader.getSamples();
            } catch (Exception ex) {
                throw new IllegalActionException(this, ex,
                        "Unable to get samples from the file.");
            }
            _sampleIndex = 0;
            // Check that the read was successful
            if (_audioIn != null) {
                _reachedEOF = false;
            } else {
                _reachedEOF = true;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Most recently read audio data. */
    private double[][] _audioIn;

    /** Buffer of tokens to send. */
    private DoubleToken[] _audioSendArray;

    /** The number of channels. */
    private int _channels;

    /** Indicator that the fire() method has been called, but wrapup
     *  has not.  That is, we are in the middle of a run.
     */
    private boolean _firedSinceWrapup = false;

    /** Previous value of fileOrURL parameter. */
    private String _previousFileOrURL;

    /** Indicator that we have reached the end of the file. */
    private boolean _reachedEOF = true;

    /** Index of the next output to produce from _audioIn. */
    private int _sampleIndex;

    /** The current reader for the input file. */
    private SoundReader _soundReader;
}

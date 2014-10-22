/* A class that supports live audio capture and playback.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.media.javasound;

import java.io.IOException;

import ptolemy.actor.injection.PtolemyInjector;

///////////////////////////////////////////////////////////////////
//// LiveSound

/**
 This class supports live capture and playback of audio
 samples. For audio capture, audio samples are captured
 from the audio input port of the computer. The audio input
 port is typically associated with the line-in port,
 microphone-in port, or cdrom audio-in port.
 For audio playback, audio samples are written to
 the audio output port. The audio output port is typically
 associated with the headphones jack or the internal
 speaker of the computer.
 <p>
 <b>Format of audio samples</b>
 <p>
 In this class, audio samples are double-valued and have a
 valid range of [-1.0, 1.0]. Thus when this class is used
 for audio capture, the returned samples will all range in
 value from -1.0 to 1.0. When this class is used for audio
 playback, the valid range of input samples is from -1.0 to
 1.0. Any samples that are outside of this range will be
 hard-clipped to fall within this range.
 <p>
 <b>Supported audio formats</b>
 <p>
 This class supports the subset of the hardware supported
 audio formats that are also supported under Java. Provided
 that the computer has a sound card that was manufactured
 after 1998, the following formats are likely to be supported.
 <ul>
 <li><i>channels</i>: Mono (channels = 1) and stereo
 (channels = 2) audio is supported. Note that some newer sound
 cards support more than two channels, such as 4 input, 4 output
 channels or better. Java does not support more than two
 channels, however. The default value assumed by this class is
 mono (1 channel). The number of channels may be set by the
 setChannels() method and read by the getChannels method().
 <li><i>sample rates</i>: 8000, 11025, 22050, 44100, and
 48000 Hz are supported. Note that some newer sound cards
 support 96000 Hz sample rates, but this is not supported under
 Java. The default sample rate used by this class is 8000 Hz.
 The sample rate may be set by the setSampleRate() method and
 read by the getSampleRate() method.
 <li><i>bit resolution</i>: 8 bit and 16 bit audio is supported.
 Note that some newer sound cards support 20 bit, 24 bit, and 32
 bit audio, but this is not supported under Java. The default
 bit resolution used by this class is 16 bit audio. The bit
 resolution may be set by the setBitsPerSample() method and
 read by the getBitsPerSample() method.
 </ul>
 <p>
 <b>Input/output latency</b>
 <p>
 When capturing audio samples, there will be some delay (latency)
 from the time the sound arrives at the input port to the time
 that the corresponding audio samples are available from this
 class. Likewise, there will be some delay from the time sample
 are written until the corresponding audio signal reaches the
 output port (e.g., the speaker). This is because an internal
 buffer is used to temporarily store audio samples when they are
 captured or played. The size of this internal buffer affects
 the latency in that a lower bound on the capture (playback)
 latency is given by (<i>bufferSize</i> / <i>sampleRate</i>)
 seconds. Here, <i>bufferSize</i> parameter is the size of the
 buffer in samples per channel. This class provides a method,
 setBufferSize(), to simultaneously set the size of internal
 capture buffer and the internal playback buffer. The method
 getBufferSize() may be used to read the buffer size. The default
 size of the internal buffer is 4096 samples.
 <p>
 <b>Constraints</b>
 <p>
 This class requires that the sample rate, number of channels,
 bit resolution, and internal buffer size be the same for both
 capture and playback. The motivation for this constraint is to
 simplify usage. Most audio hardware requires this anyway.
 <p>
 <b>Usage: audio capture</b>
 <p>
 First, call the appropriate methods to set the desired audio
 format parameters such as sample rate, channels, bit resolution
 if values other than the defaults are desired. The
 setTransferSize() method should also be invoked to set the size
 of the array (in samples per channel) that is returned when
 audio samples are captured. The default value is 128 samples
 per channel. Then invoke the startCapture(consumer) method to
 start the audio capture process. This class will be ready to
 capture audio immediately after startCapture() returns. Note
 that startCapture() takes an Object parameter, <i>consumer</i>.
 In addition to starting the audio capture process, startCapture()
 also grants an object permission to capture audio.
 <p>
 After calling startCapture(consumer), the consumer object can
 capture audio from the input port by calling getSamples(consumer).
 The getSamples() method returns an array of samples from the input
 port. The getSamples() blocks until the requested number of
 samples (which is set by the setTransferSize method) are available.
 Thus, it is not possible to call this method too frequently.
 Note that if getSamples() is not called frequently enough,
 the internal buffer will overflow and some audio data will
 be lost, which is generally undesirable. After the consumer
 object no longer wishes to capture audio, it should free up the
 audio system resources by calling the stopCapture(consumer)
 method. It should be noted that only one object may capture
 audio simultaneously from the audio input port. A future
 version of this class may support multiple objects capturing
 from the input port simultaneously.
 <p>
 <b>Usage: audio Playback</b>
 <p>
 First, call the appropriate methods to set the desired audio
 format parameters such as sample rate, channels, bit
 resolution if values other than the defaults are desired.
 The setTransferSize() method should also be invoked to set the
 size of the array (in samples per channel) that is supplied
 when audio samples are played. The default value is 128 samples
 per channel. Then invoke the startPlayback(producer) method
 to start the audio playback process. This class will be ready
 to playback audio immediately after startPlayback() returns.
 Note that startPlayback() takes an Object parameter,
 <i>producer</i>. In addition to starting the audio playback
 process, startPlayback() also grants an object permission to
 playback audio.
 <p>
 After calling startPlayback(producer), the producer object can
 playback audio to the output port by calling putSamples(producer).
 The putSamples() method takes an array of samples and sends the
 audio data to the output port. The putSamples() method blocks
 until the requested number of samples (which is set by the
 setTransferSize method) have been written to the output port.
 Thus, it is not possible to call this method too frequently.
 Note that if putSamples() is not called frequently enough,
 the internal buffer will underflow, causing audible artifacts
 in the output signal. After the producer object no longer wishes
 to playback audio, it should free up the audio system resources
 by calling the stopPlayback(producer) method. It should be noted
 that only one object may playback audio simultaneously to the
 audio output port. A future version of this class may support
 multiple objects playing to the output port simultaneously.

 @author Brian K. Vogel and Neil E. Turner and Steve Neuendorffer, Edward A. Lee, Contributor: Dennis Geurts
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (net)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.media.javasound.SoundReader
 @see ptolemy.media.javasound.SoundWriter
 */
public class LiveSound {

    /** Add a live sound listener. The listener will be notified
     *  of all changes in live audio parameters. If the listener
     *  is already listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to add.
     *  @see #removeLiveSoundListener(LiveSoundListener)
     */
    public static void addLiveSoundListener(LiveSoundListener listener) {
        _implementation.addLiveSoundListener(listener);

    }

    /** Flush queued data from the capture buffer.  The flushed data is
     *  discarded.  It is only legal to flush the capture buffer after
     *  startCapture() is called.  Flushing an active audio buffer is likely to
     *  cause a discontinuity in the data, resulting in a perceptible click.
     *  <p>
     *  Note that only the object with the exclusive lock on the capture audio
     *  resources is allowed to invoke this method. An exception will occur if
     *  the specified object does not have the lock on the playback audio
     *  resources.
     *
     *  @param consumer The object that has an exclusive lock on
     *   the capture audio resources.
     *
     *  @exception IllegalStateException If audio capture is currently
     *  inactive. That is, if startCapture() has not yet been called
     *  or if stopCapture() has already been called.
     *
     *  @exception IOException If the calling program does not have permission
     *  to access the audio capture resources.
     */
    public static void flushCaptureBuffer(Object consumer) throws IOException,
            IllegalStateException {
        _implementation.flushCaptureBuffer(consumer);

    }

    /** Flush queued data from the playback buffer.  The flushed data is
     *  discarded.  It is only legal to flush the playback buffer after
     *  startPlayback() is called, and only makes sense to do so (but is
     *  not required) after putSamples() is called.  Flushing an active audio
     *  buffer is likely to cause a discontinuity in the data, resulting in a
     *  perceptible click.
     *  <p>
     *  Note that only the object with the exclusive lock on the playback audio
     *  resources is allowed to invoke this method. An exception will occur if
     *  the specified object does not have the lock on the playback audio
     *  resources.
     *
     *  @param producer The object that has an exclusive lock on
     *   the playback audio resources.
     *
     *  @exception IllegalStateException If audio playback is currently
     *  inactive. That is, if startPlayback() has not yet been called
     *  or if stopPlayback() has already been called.
     *
     *  @exception IOException If the calling program does not have permission
     *  to access the audio playback resources.
     */
    public static void flushPlaybackBuffer(Object producer) throws IOException,
            IllegalStateException {
        _implementation.flushPlaybackBuffer(producer);

    }

    /** Return the number of bits per audio sample, which is
     *  set by the setBitsPerSample() method. The default
     *  value of this parameter is 16 bits.
     *
     * @return The sample size in bits.
     * @see #setBitsPerSample(int)
     */
    public static int getBitsPerSample() {
        return _implementation.getBitsPerSample();

    }

    /** Return the suggested size of the internal capture and playback audio
     *  buffers, in samples per channel. This parameter is set by the
     *  setBufferSize() method.  There is no guarantee that the value returned
     *  is the actual buffer size used for capture and playback.
     *  Furthermore, the buffers used for capture and playback may have
     *  different sizes.  The default value of this parameter is 4096.
     *
     *  @return The suggested internal buffer size in samples per
     *   channel.
     *  @see #setBufferSize(int)
     */
    public static int getBufferSize() {
        return _implementation.getBufferSize();

    }

    /** Return the size of the internal capture audio buffer, in samples per
     *  channel.
     *
     *  @return The internal buffer size in samples per channel.
     *
     *  @exception IllegalStateException If audio capture is inactive.
     */
    public static int getBufferSizeCapture() throws IllegalStateException {

        return _implementation.getBufferSizeCapture();

    }

    /** Return the size of the internal playback audio buffer, in samples per
     *  channel. This may differ from the requested buffer size if the hardware
     *  does not support the requested buffer size. If playback has not
     *  been started, then will simply return the requested buffer size.
     *  @return The internal buffer size in samples per channel.
     *  @exception IllegalStateException If audio playback is inactive.
     */
    public static int getBufferSizePlayback() {

        return _implementation.getBufferSizePlayback();

    }

    /** Return the number of audio channels, which is set by
     *  the setChannels() method. The default value of this
     *  parameter is 1 (for mono audio).
     *
     *  @return The number of audio channels.
     *  @see #setChannels(int)
     */
    public static int getChannels() {

        return _implementation.getChannels();

    }

    /** Return the current sampling rate in Hz, which is set
     *  by the setSampleRate() method. The default value of
     *  this parameter is 8000 Hz.
     *
     *  @return The sample rate in Hz.
     *  @see #setSampleRate(int)
     */
    public static int getSampleRate() {

        return _implementation.getSampleRate();

    }

    /** Return an array of captured audio samples. This method
     *  should be repeatedly called to obtain audio data.
     *  The returned audio samples will have values in the range
     *  [-1, 1], regardless of the audio bit resolution (bits per
     *  sample).  This method should be called often enough to
     *  prevent overflow of the internal audio buffer. If
     *  overflow occurs, some audio data will be lost but no
     *  exception or other error condition will occur. If
     *  the audio data is not yet available, then this method
     *  will block until the data is available.
     *  <p>
     *  The first index of the returned array
     *  represents the channel number (0 for first channel, 1 for
     *  second channel). The number of channels is set by the
     *  setChannels() method. The second index represents the
     *  sample index within a channel. For example,
     *  <i>returned array</i>[n][m] contains the (m+1)th sample
     *  of the (n+1)th channel. For each channel, n, the length of
     *  <i>returned array</i>[n] is equal to the value returned by
     *  the getTransferSize() method.
     *  The size of the 2nd dimension of the returned array
     *  is set by the setTransferSize() method.
     *  <p>
     *  Note that only the object with the exclusive lock on
     *  the captured audio resources is allowed to invoked this
     *  method. An exception will occur if the specified object
     *  does not have the lock on the captured audio resources.
     *
     *  @param consumer The object that has an exclusive lock on
     *   the capture audio resources.
     *
     *  @return Two dimensional array of captured audio samples.
     *
     *  @exception IllegalStateException If audio capture is currently
     *   inactive.  That is, if startCapture() has not yet been called or if
     *   stopCapture() has already been called.
     *
     *  @exception IOException If the calling program does not have permission
     *   to access the audio capture resources.
     */
    public static double[][] getSamples(Object consumer) throws IOException,
            IllegalStateException {

        return _implementation.getSamples(consumer);

    }

    /** Get the array length (in samples per channel) to use
     *  for capturing and playing samples via the putSamples()
     *  and getSamples() methods. This method gets the size
     *  of the 2nd dimension of the 2-dimensional array
     *  used by the putSamples() and getSamples() methods. This
     *  method returns the value that was set by the
     *  setTransferSize(). If setTransferSize() was not invoked,
     *  the default value of 128 is returned.
     *
     *  @return The size of the 2nd dimension of the 2-dimensional
     *   array used by the putSamples() and getSamples() methods.
     *  @see #setTransferSize(int)
     */
    public static int getTransferSize() {
        return _implementation.getTransferSize();

    }

    /** Return true if audio capture is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio capture is currently active.
     *  Otherwise return false.
     */
    public static boolean isCaptureActive() {
        return _implementation.isCaptureActive();

    }

    /** Return true if audio playback is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio playback is currently active.
     *  Otherwise return false.
     */
    public static boolean isPlaybackActive() {
        return _implementation.isPlaybackActive();
    }

    /** Play an array of audio samples. There will be a
     *  delay before the audio data is actually heard, since the
     *  audio data in <i>samplesArray</i> is queued to an
     *  internal audio buffer. The setBufferSize() method suggests a size
     *  for the internal buffer. An upper bound
     *  on the latency is given by (<i>bufferSize</i> /
     *  <i>sampleRate</i>) seconds. This method should be invoked often
     *  enough to prevent underflow of the internal audio buffer.
     *  Underflow is undesirable since it will cause audible gaps
     *  in audio playback, but no exception or error condition will
     *  occur. If the caller attempts to write more data than can
     *  be written, this method blocks until the data can be
     *  written to the internal audio buffer.
     *  <p>
     *  The samples should be in the range (-1, 1). Samples that are
     *  outside this range will be hard-clipped so that they fall
     *  within this range.
     *  <p>
     *  The first index of the specified array
     *  represents the channel number (0 for first channel, 1 for
     *  second channel, etc.). The number of channels is set by the
     *  setChannels() method. The second index represents the
     *  sample index within a channel. For example,
     *  putSamplesArray[n][m] contains the (m+1)th sample
     *  of the (n+1)th channel.
     *  <p>
     *  Note that only the object with the exclusive lock on
     *  the playback audio resources is allowed to invoke this
     *  method. An exception will occur if the specified object
     *  does not have the lock on the playback audio resources.
     *
     *  @param producer The object that has an exclusive lock on
     *   the playback audio resources.
     *
     *  @param samplesArray A two dimensional array containing
     *  the samples to play or write to a file.
     *
     *  @exception IOException If the calling program does not have permission
     *  to access the audio playback resources.
     *
     *  @exception IllegalStateException If audio playback is currently
     *  inactive. That is, If startPlayback() has not yet been called
     *  or if stopPlayback() has already been called.
     */
    public static void putSamples(Object producer, double[][] samplesArray)
            throws IOException, IllegalStateException {

        _implementation.putSamples(producer, samplesArray);

    }

    /** Remove a live sound listener. If the listener is
     *  is not listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to remove.
     *  @see #addLiveSoundListener(LiveSoundListener)
     */
    public static void removeLiveSoundListener(LiveSoundListener listener) {

        _implementation.removeLiveSoundListener(listener);

    }

    /** Stop audio capture. If audio capture is already inactive,
     *  then do nothing. This method should generally not be used,
     *  but it may be needed to turn of audio capture for the
     *  case where an ill-behaved application exits without calling
     *  stopCapture(). The preferred way of stopping audio capture
     *  is by calling the stopCapture() method.
     *
     */
    public static void resetCapture() {

        _implementation.resetCapture();

    }

    /** Stop audio playback. If audio playback is already inactive,
     *  then do nothing. This method should generally not be used,
     *  but it may be needed to turn of audio playback for the
     *  case where an ill-behaved application exits without calling
     *  stopPlayback(). The preferred way of stopping audio playback
     *  is by calling the stopPlayback() method.
     *
     */
    public static void resetPlayback() {

        _implementation.resetPlayback();

    }

    /** Set the number of bits per sample to use for audio capture
     *  and playback and notify any registered listeners of the change.
     *  Allowable values include 8 and 16 bits. If
     *  this method is not invoked, then the default value of 16
     *  bits is used.
     *  @param bitsPerSample The number of bits per sample.
     *  @exception IOException If the specified bits per sample is
     *   not supported by the audio hardware or by Java.
     *  @see #getBitsPerSample()
     */
    public static void setBitsPerSample(int bitsPerSample) throws IOException {

        _implementation.setBitsPerSample(bitsPerSample);

    }

    /** Request that the internal capture and playback
     *  audio buffers have bufferSize samples per channel and notify the
     *  registered listeners of the change. If this method
     *  is not invoked, the default value of 1024 is used.
     *
     *  @param bufferSize The suggested size of the internal capture and
     *   playback audio buffers, in samples per channel.
     *  @exception IOException If the specified number of channels is
     *   not supported by the audio hardware or by Java.
     *  @see #getBufferSize()
     */
    public static void setBufferSize(int bufferSize) throws IOException {

        _implementation.setBufferSize(bufferSize);

    }

    /** Set the number of audio channels to use for capture and
     *  playback and notify any registered listeners of the change.
     *  Allowable values are 1 (for mono) and 2 (for
     *  stereo). If this method is not invoked, the default
     *  value of 1 audio channel is used. Note that this method
     *  sets the size of the first dimension of the
     *  2-dimensional array used by the putSamples() and
     *  getSamples() methods.
     *
     *  @param channels The number audio channels.
     *
     *  @exception IOException If the specified number of channels is
     *   not supported by the audio hardware or by Java.
     *  @see #getChannels()
     */
    public static void setChannels(int channels) throws IOException {

        _implementation.setChannels(channels);

    }

    /** Set the sample rate to use for audio capture and playback
     *  and notify an registered listeners of the change.
     *  Allowable values for this parameter are 8000, 11025,
     *  22050, 44100, and 48000 Hz. If this method is not invoked,
     *  then the default value of 8000 Hz is used.
     *
     *  @param sampleRate Sample rate in Hz.
     *
     *  @exception IOException If the specified sample rate is
     *   not supported by the audio hardware or by Java.
     *  @see #getSampleRate()
     */
    public static void setSampleRate(int sampleRate) throws IOException {

        _implementation.setSampleRate(sampleRate);

    }

    /** Set the array length (in samples per channel) to use
     *  for capturing and playing samples via the putSamples()
     *  and getSamples() methods. This method sets the size
     *  of the 2nd dimension of the 2-dimensional array
     *  used by the putSamples() and getSamples() methods. If
     *  this method is not invoked, the default value of 128 is
     *  used.
     *  <p>
     *  This method should only be called while audio capture and
     *  playback are inactive. Otherwise an exception will occur.
     *
     *  @param transferSize The  size of the 2nd dimension of
     *   the 2-dimensional array used by the putSamples() and
     *   getSamples() methods
     *
     *  @exception IllegalStateException If this method is called
     *   while audio capture or playback are active.
     *  @see #getTransferSize()
     */
    public static void setTransferSize(int transferSize)
            throws IllegalStateException {

        _implementation.setTransferSize(transferSize);

    }

    /** Start audio capture. The specified object will be
     *  given an exclusive lock on the audio capture resources
     *  until the stopCapture() method is called with the
     *  same object reference. After this method returns,
     *  the getSamples() method may be repeatedly invoked
     *  (using the object reference as a parameter) to
     *  capture audio.
     *  <p>
     *  If audio capture is already active, then an
     *  exception will occur.
     *
     *  @param consumer The object to be given exclusive access
     *   to the captured audio resources.
     *
     *  @exception IOException If another object currently has access
     *   to the audio capture resources or if starting the capture or
     *   playback throws it.
     *
     *  @exception IllegalStateException If this method is called
     *   while audio capture is already active.
     */
    public static void startCapture(Object consumer) throws IOException,
            IllegalStateException {

        _implementation.startCapture(consumer);

    }

    /** Start audio playback. The specified object will be
     *  given an exclusive lock on the audio playback resources
     *  until the stopPlayback() method is called with the
     *  same object reference. After this method returns,
     *  the putSamples() method may be repeatedly invoked
     *  (using the object reference as a parameter) to
     *  playback audio.
     *  <p>
     *  If audio playback is already active, then an
     *  exception will occur.
     *
     *  @param producer The object to be given exclusive access
     *   to the playback resources.
     *
     *  @exception IOException If another object currently has access
     *   to the audio capture resources or if starting the playback throws it.
     *
     *  @exception IllegalStateException If this method is called
     *   while audio playback is already active.
     */
    public static void startPlayback(Object producer) throws IOException,
            IllegalStateException {

        _implementation.startPlayback(producer);

    }

    /** Stop audio capture. If the specified object has
     *  the lock on audio capture when this method is
     *  invoked, then stop audio capture. Otherwise
     *  an exception will occur.
     *
     *  @param consumer The object that held on exclusive
     *   lock on the captured audio resources when this
     *   method was invoked.
     *
     *  @exception IOException If another object currently has access
     *   to the audio capture resources or if stopping the capture throws it.
     *
     *  @exception IllegalStateException If the specified
     *   object did not hold an exclusive lock on the
     *   captured audio resources when this method was invoked.
     */
    public static void stopCapture(Object consumer) throws IOException,
            IllegalStateException {
        _implementation.stopCapture(consumer);
    }

    /** Stop audio playback. If the specified object has
     *  the lock on audio playback when this method is
     *  invoked, then stop audio playback. Otherwise
     *  an exception will occur.
     *
     *  @param producer The object that held on exclusive
     *   lock on the playback audio resources when this
     *   method was invoked.
     *
     *  @exception IOException If another object currently has access
     *   to the audio capture resources or if stopping the playback throws it.

     *  @exception IllegalStateException If the specified
     *   object did not hold an exclusive lock on the
     *   playback audio resources when this method was invoked.
     *
     */
    public static void stopPlayback(Object producer) throws IOException,
            IllegalStateException {
        _implementation.stopPlayback(producer);
    }

    private static LiveSoundInterface _implementation = PtolemyInjector
            .getInjector().getInstance(LiveSoundInterface.class);

}

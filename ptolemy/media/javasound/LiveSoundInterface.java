/* Interface for implementing for implementing LiveSound actor for different
  platforms

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

///////////////////////////////////////////////////////////////////
////LiveSoundInterface

/**
Interface for implementing for implementing LiveSoundActor on different
platforms.

@author Ishwinder Singh
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Yellow (ishwinde)
@Pt.AcceptedRating Yellow (ishwinde)
 */

public interface LiveSoundInterface {

    /** Add a live sound listener. The listener will be notified
     *  of all changes in live audio parameters. If the listener
     *  is already listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to add.
     *  @see #removeLiveSoundListener(LiveSoundListener)
     */
    public void addLiveSoundListener(LiveSoundListener listener);

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
    public void flushCaptureBuffer(Object consumer) throws IOException,
            IllegalStateException;

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
    public void flushPlaybackBuffer(Object producer) throws IOException,
            IllegalStateException;

    /** Return the number of bits per audio sample, which is
     *  set by the setBitsPerSample() method. The default
     *  value of this parameter is 16 bits.
     *
     * @return The sample size in bits.
     * @see #setBitsPerSample(int)
     */
    public int getBitsPerSample();

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
    public int getBufferSize();

    /** Return the size of the internal capture audio buffer, in samples per
     *  channel.
     *
     *  @return The internal buffer size in samples per channel.
     *
     *  @exception IllegalStateException If audio capture is inactive.
     */
    public int getBufferSizeCapture() throws IllegalStateException;

    /** Return the size of the internal playback audio buffer, in samples per
     *  channel. This may differ from the requested buffer size if the hardware
     *  does not support the requested buffer size. If playback has not
     *  been started, then will simply return the requested buffer size.
     *  @return The internal buffer size in samples per channel.
     *  @exception IllegalStateException If audio playback is inactive.
     */
    public int getBufferSizePlayback();

    /** Return the number of audio channels, which is set by
     *  the setChannels() method. The default value of this
     *  parameter is 1 (for mono audio).
     *
     *  @return The number of audio channels.
     *  @see #setChannels(int)
     */
    public int getChannels();

    /** Return the current sampling rate in Hz, which is set
     *  by the setSampleRate() method. The default value of
     *  this parameter is 8000 Hz.
     *
     *  @return The sample rate in Hz.
     *  @see #setSampleRate(int)
     */
    public int getSampleRate();

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
    public double[][] getSamples(Object consumer) throws IOException,
            IllegalStateException;

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
    public int getTransferSize();

    /** Return true if audio capture is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio capture is currently active.
     *  Otherwise return false.
     */
    public boolean isCaptureActive();

    /** Return true if audio playback is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio playback is currently active.
     *  Otherwise return false.
     */
    public boolean isPlaybackActive();

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
    public void putSamples(Object producer, double[][] samplesArray)
            throws IOException, IllegalStateException;

    /** Remove a live sound listener. If the listener is
     *  is not listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to remove.
     *  @see #addLiveSoundListener(LiveSoundListener)
     */
    public void removeLiveSoundListener(LiveSoundListener listener);

    /** Stop audio capture. If audio capture is already inactive,
     *  then do nothing. This method should generally not be used,
     *  but it may be needed to turn of audio capture for the
     *  case where an ill-behaved application exits without calling
     *  stopCapture(). The preferred way of stopping audio capture
     *  is by calling the stopCapture() method.
     *
     */
    public void resetCapture();

    /** Stop audio playback. If audio playback is already inactive,
     *  then do nothing. This method should generally not be used,
     *  but it may be needed to turn of audio playback for the
     *  case where an ill-behaved application exits without calling
     *  stopPlayback(). The preferred way of stopping audio playback
     *  is by calling the stopPlayback() method.
     *
     */
    public void resetPlayback();

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
    public void setBitsPerSample(int bitsPerSample) throws IOException;

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
    public void setBufferSize(int bufferSize) throws IOException;

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
    public void setChannels(int channels) throws IOException;

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
    public void setSampleRate(int sampleRate) throws IOException;

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
    public void setTransferSize(int transferSize) throws IllegalStateException;

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
    public void startCapture(Object consumer) throws IOException,
            IllegalStateException;

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
    public void startPlayback(Object producer) throws IOException,
            IllegalStateException;

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
    public void stopCapture(Object consumer) throws IOException,
            IllegalStateException;

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
    public void stopPlayback(Object producer) throws IOException,
            IllegalStateException;

}

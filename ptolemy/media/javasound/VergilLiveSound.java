package ptolemy.media.javasound;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class VergilLiveSound implements LiveSoundInterface {
    /** Add a live sound listener. The listener will be notified
     *  of all changes in live audio parameters. If the listener
     *  is already listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to add.
     *  @see #removeLiveSoundListener(LiveSoundListener)
     */
    @Override
    public void addLiveSoundListener(LiveSoundListener listener) {
        if (!_liveSoundListeners.contains(listener)) {
            _liveSoundListeners.add(listener);
        }
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
    @Override
    public void flushCaptureBuffer(Object consumer) throws IOException,
            IllegalStateException {
        if (!isCaptureActive()) {
            throw new IllegalStateException("Object: " + consumer.toString()
                    + " attempted to call LiveSound.flushCaptureBuffer(), but "
                    + "capture is inactive.  Try to startCapture().");
        }

        if (!_soundConsumers.contains(consumer)) {
            throw new IOException("Object: " + consumer.toString()
                    + " attempted to call LiveSound.flushCaptureBuffer(), but "
                    + "this object does not have permission to access the "
                    + "audio capture resource.");
        }
        _flushCaptureBuffer();
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
    @Override
    public void flushPlaybackBuffer(Object producer) throws IOException,
            IllegalStateException {
        _flushPlaybackBuffer();
    }

    /** Return the number of bits per audio sample, which is
     *  set by the setBitsPerSample() method. The default
     *  value of this parameter is 16 bits.
     *
     * @return The sample size in bits.
     * @see #setBitsPerSample(int)
     */
    @Override
    public int getBitsPerSample() {
        return _bitsPerSample;
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
    @Override
    public int getBufferSize() {
        return _bufferSize;
    }

    /** Return the size of the internal capture audio buffer, in samples per
     *  channel.
     *
     *  @return The internal buffer size in samples per channel.
     *
     *  @exception IllegalStateException If audio capture is inactive.
     */
    @Override
    public int getBufferSizeCapture() throws IllegalStateException {
        if (_targetLine != null) {
            return _targetLine.getBufferSize() / (_bytesPerSample * _channels);
        } else {
            throw new IllegalStateException("LiveSound: "
                    + "getBufferSizeCapture(), capture is probably inactive."
                    + "Try to startCapture().");
        }
    }

    /** Return the size of the internal playback audio buffer, in samples per
     *  channel. This may differ from the requested buffer size if the hardware
     *  does not support the requested buffer size. If playback has not
     *  been started, then will simply return the requested buffer size.
     *  @return The internal buffer size in samples per channel.
     *  @exception IllegalStateException If audio playback is inactive.
     */
    @Override
    public int getBufferSizePlayback() {
        if (_sourceLine != null) {
            return _sourceLine.getBufferSize() / (_bytesPerSample * _channels);
        } else {
            return _bufferSize;
        }
    }

    /** Return the number of audio channels, which is set by
     *  the setChannels() method. The default value of this
     *  parameter is 1 (for mono audio).
     *
     *  @return The number of audio channels.
     *  @see #setChannels(int)
     */
    @Override
    public int getChannels() {
        return _channels;
    }

    /** Return the current sampling rate in Hz, which is set
     *  by the setSampleRate() method. The default value of
     *  this parameter is 8000 Hz.
     *
     *  @return The sample rate in Hz.
     *  @see #setSampleRate(int)
     */
    @Override
    public int getSampleRate() {
        return (int) _sampleRate;
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
    @Override
    public double[][] getSamples(Object consumer) throws IOException,
            IllegalStateException {
        if (!isCaptureActive()) {
            throw new IllegalStateException("Object: " + consumer.toString()
                    + " attempted to call LiveSound.getSamples(), but "
                    + "capture is inactive.  Try to startCapture().");
        }

        if (!_soundConsumers.contains(consumer)) {
            throw new IOException("Object: " + consumer.toString()
                    + " attempted to call LiveSound.getSamples(), but "
                    + "this object does not have permission to access the "
                    + "audio capture resource.");
        }

        // Real-time capture.
        int numBytesRead = _targetLine.read(_captureData, 0,
                _captureData.length);

        // Check if we need to reallocate.
        if ((_channels != _audioInDoubleArray.length)
                || (_transferSize != _audioInDoubleArray[0].length)) {
            // Reallocate
            _audioInDoubleArray = new double[_channels][_transferSize];
        }

        if (numBytesRead == _captureData.length) {
            // Convert byte array to double array.
            _byteArrayToDoubleArray(_audioInDoubleArray, _captureData);
            return _audioInDoubleArray;
        } else {
            throw new IOException("Failed to capture correct number of bytes");
        }
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
    @Override
    public int getTransferSize() {
        return _transferSize;
    }

    /** Return true if audio capture is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio capture is currently active.
     *  Otherwise return false.
     */
    @Override
    public boolean isCaptureActive() {
        return _captureIsActive;
    }

    /** Return true if audio playback is currently active.
     *  Otherwise return false.
     *
     *  @return True If audio playback is currently active.
     *  Otherwise return false.
     */
    @Override
    public boolean isPlaybackActive() {
        return _playbackIsActive;
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
    @Override
    public void putSamples(Object producer, double[][] samplesArray)
            throws IOException, IllegalStateException {
        if (!isPlaybackActive()) {
            throw new IllegalStateException("Object: " + producer.toString()
                    + " attempted to call LiveSound.putSamples(), but "
                    + "playback is inactive.  Try to startPlayback().");
        }
        // Convert array of double valued samples into
        // the proper byte array format.
        byte[] playbackData = _doubleArrayToByteArray(samplesArray);

        // Now write the array to output device.
        int written = _sourceLine.write(playbackData, 0, playbackData.length);

        if (written != playbackData.length) {
            System.out.println("dropped samples!");
        }
    }

    /** Remove a live sound listener. If the listener is
     *  is not listening, then do nothing.
     *
     *  @param listener The LiveSoundListener to remove.
     *  @see #addLiveSoundListener(LiveSoundListener)
     */
    @Override
    public void removeLiveSoundListener(LiveSoundListener listener) {
        if (_liveSoundListeners.contains(listener)) {
            _liveSoundListeners.remove(listener);
        }
    }

    /** Stop audio capture. If audio capture is already inactive,
     *  then do nothing. This method should generally not be used,
     *  but it may be needed to turn of audio capture for the
     *  case where an ill-behaved application exits without calling
     *  stopCapture(). The preferred way of stopping audio capture
     *  is by calling the stopCapture() method.
     *
     */
    @Override
    public void resetCapture() {
        if (_targetLine != null) {
            if (_targetLine.isOpen() == true) {
                _targetLine.stop();
                _targetLine.close();
                _targetLine = null;
            }
        }

        _captureIsActive = false;
    }

    /** Stop audio playback. If audio playback is already inactive,
     *  then do nothing. This method should generally not be used,
     *  but it may be needed to turn of audio playback for the
     *  case where an ill-behaved application exits without calling
     *  stopPlayback(). The preferred way of stopping audio playback
     *  is by calling the stopPlayback() method.
     *
     */
    @Override
    public void resetPlayback() {
        _stopPlayback();
        _playbackIsActive = false;
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
    @Override
    public void setBitsPerSample(int bitsPerSample) throws IOException {
        _bitsPerSample = bitsPerSample;
        // FIXME: The following is wrong. Probably should just set bytes per sample.
        _bytesPerSample = _bitsPerSample / 8;
        // Note: _maxSample is maximum positive number, which ensures
        // that maximum negative number is also in range.
        switch (_bytesPerSample) {
        case 1:
            _maxSampleReciprocal = 1.0 / 128;
            _maxSample = 127;
            break;
        case 2:
            _maxSampleReciprocal = 1.0 / 32768;
            _maxSample = 32767;
            break;
        case 3:
            _maxSampleReciprocal = 1.0 / 8388608;
            _maxSample = 8388607;
            break;
        case 4:
            _maxSampleReciprocal = 1.0 / 147483648e9;
            _maxSample = 147483647e9;
            break;
        default:
            // Should not happen.
            _maxSampleReciprocal = 0;
        }

        if ((_captureIsActive) && (_playbackIsActive)) {
            // Restart capture/playback with new bitsPerSample.
            _stopCapture();
            _stopPlayback();
            _startCapture();
            _startPlayback();
        } else if (_captureIsActive) {
            // Restart capture with new bitsPerSample.
            _stopCapture();
            _startCapture();
        } else if (_playbackIsActive) {
            // Restart playback with new bitsPerSample.
            _stopPlayback();
            _startPlayback();
        }

        // Notify listeners of the change.
        _notifyLiveSoundListeners(LiveSoundEvent.BITS_PER_SAMPLE);
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
    @Override
    public void setBufferSize(int bufferSize) throws IOException {
        _bufferSize = bufferSize;
        if ((_captureIsActive) && (_playbackIsActive)) {
            // Restart capture/playback with new bufferSize.
            _stopCapture();
            _stopPlayback();
            _startCapture();
            _startPlayback();
        } else if (_captureIsActive) {
            // Restart capture with new bufferSize.
            _stopCapture();
            _startCapture();
        } else if (_playbackIsActive) {
            // Restart playback with new bufferSize.
            _stopPlayback();
            _startPlayback();
        }
        // Notify listeners of the change.
        _notifyLiveSoundListeners(LiveSoundEvent.BUFFER_SIZE);
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
    @Override
    public void setChannels(int channels) throws IOException {
        _channels = channels;
        if ((_captureIsActive) && (_playbackIsActive)) {
            // Restart capture/playback with new number of channels.
            _stopCapture();
            _stopPlayback();
            _startCapture();
            _startPlayback();
        } else if (_captureIsActive) {
            // Restart capture with new number of channels.
            _stopCapture();
            _startCapture();
        } else if (_playbackIsActive) {
            // Restart playback with new number of channels.
            _stopPlayback();
            _startPlayback();
        }
        // Notify listeners of the change.
        _notifyLiveSoundListeners(LiveSoundEvent.CHANNELS);
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
    @Override
    public void setSampleRate(int sampleRate) throws IOException {
        _sampleRate = sampleRate;
        if ((_captureIsActive) && (_playbackIsActive)) {
            // Restart capture/playback with new sample rate.
            _stopCapture();
            _stopPlayback();
            _startCapture();
            _startPlayback();
        } else if (_captureIsActive) {
            // Restart capture with new sample rate.
            _stopCapture();
            _startCapture();
        } else if (_playbackIsActive) {
            // Restart playback with new sample rate.
            _stopPlayback();
            _startPlayback();
        }
        // Notify listeners of the change.
        _notifyLiveSoundListeners(LiveSoundEvent.SAMPLE_RATE);
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
    @Override
    public void setTransferSize(int transferSize) throws IllegalStateException {
        // This change only affects capture, so it's OK for it to occur
        // while there is playback.
        if (_captureIsActive) {
            throw new IllegalStateException("LiveSound: "
                    + "setTransferSize() was called while audio capture "
                    + "or playback was active.");
        } else {
            _transferSize = transferSize;
        }
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
    @Override
    public void startCapture(Object consumer) throws IOException,
            IllegalStateException {
        // FIXME: consider allowing several object to
        // share the captured audio resources.
        if (_soundConsumers.size() > 0) {
            throw new IOException("Object: " + consumer.toString()
                    + " is not allowed to start audio capture because "
                    + "another object currently has access to the audio "
                    + "capture resources.");
        }

        if (!_soundConsumers.contains(consumer)) {
            _soundConsumers.add(consumer);
        } else {
            throw new IllegalStateException("Object: " + consumer.toString()
                    + " attempted to call LiveSound.startCapture() while "
                    + "audio capture was active.");
        }
        // This is a workaround for a javasound bug. In javasound,
        // when doing simultaneous capture and playback, the
        // capture process must be started first. So, if
        // there is already a playback process running then
        // stop it before starting capture.
        if (isPlaybackActive()) {
            _stopPlayback();
            _startCapture();
            _startPlayback();
        } else {
            _startCapture();
        }

        _captureIsActive = true;
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
    @Override
    public void startPlayback(Object producer) throws IOException,
            IllegalStateException {
        if (!_playbackIsActive) {
            _startPlayback();
            _playbackIsActive = true;
        }
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
    @Override
    public void stopCapture(Object consumer) throws IOException,
            IllegalStateException {
        if (_soundConsumers.contains(consumer)) {
            _soundConsumers.remove(consumer);
        } else {
            throw new IOException("Object: " + consumer.toString()
                    + " attempted to call LiveSound.stopCapture(), but "
                    + "never called LiveSound.startCapture().");
        }

        // Free up audio system resources.
        _stopCapture();
        _captureIsActive = false;
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
    @Override
    public void stopPlayback(Object producer) throws IOException,
            IllegalStateException {
        if (_playbackIsActive) {
            _stopPlayback();
        }
        _playbackIsActive = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /* Convert a byte array of audio samples in linear signed PCM big endian
     * format into a double array of audio samples (-1, 1) range.
     * @param byteArray  The linear signed pcm big endian byte array
     * formatted array representation of audio data.
     * @param bytesPerSample Number of bytes per sample. Supported
     * bytes per sample by this method are 8, 16, 24, 32.
     * @return Two dimensional array holding audio samples.
     * For each channel, m, doubleArray[m] is a single dimensional
     * array containing samples for channel m.
     */
    private static void _byteArrayToDoubleArray(double[][] doubleArray,
            byte[] byteArray) {
        int lengthInSamples = byteArray.length / (_bytesPerSample * _channels);

        for (int currSamp = 0; currSamp < lengthInSamples; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < _channels; currChannel++) {
                // Starting index of relevant bytes.
                int j = (currSamp * _bytesPerSample * _channels)
                        + (_bytesPerSample * currChannel);
                // Note: preserve sign of high order bits.
                int result = byteArray[j++];

                // Shift and add in low order bits.
                // Note that it is ok to fall through the cases here (I think).
                switch (_bytesPerSample) {
                case 4:
                    result <<= 8;
                    // Dennis Geurts:
                    // Use & instead of | here.
                    // Running ptolemy/actor/lib/javasound/test/auto/testAudioCapture_AudioPlayer.xml 
                    // results in a much better sound.
                    // See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=356
                    result += (byteArray[j++] & 0xff);

                case 3:
                    result <<= 8;
                    result += (byteArray[j++] & 0xff);

                case 2:
                    result <<= 8;
                    result += (byteArray[j++] & 0xff);
                }

                doubleArray[currChannel][currSamp] = result
                        * _maxSampleReciprocal;
            }
        }
    }

    /* Convert a double array of audio samples into a byte array of
     * audio samples in linear signed PCM big endian format. The
     * samples contained in <i>doubleArray</i> should be in the
     * range (-1, 1). Samples outside this range will be hard clipped
     * to the range (-1, 1).
     * @param doubleArray Two dimensional array holding audio samples.
     *  For each channel, m, doubleArray[m] is a single dimensional
     *  array containing samples for channel m. All channels are
     *  required to have the same number of samples, but this is
     *  not checked.
     * @return The linear signed PCM big endian byte array formatted
     *  array representation of <i>doubleArray</i>. The length of
     *  the returned array is (doubleArray[i].length*bytesPerSample*channels).
     */
    private static byte[] _doubleArrayToByteArray(double[][] doubleArray)
            throws IllegalArgumentException {
        // This method is most efficient if repeated calls pass the same size
        // array. In this case, it does not re-allocate the byte array that
        // it returns, but rather reuses the same array on the heap.
        int numberOfSamples = doubleArray[0].length;
        int bufferSize = numberOfSamples * _bytesPerSample * _channels;
        if (_playbackData == null || _playbackData.length != bufferSize) {
            // Hopefully, the allocation is done only once.
            _playbackData = new byte[bufferSize];
        }
        // Iterate over the samples.
        for (int currSamp = 0; currSamp < doubleArray[0].length; currSamp++) {
            // For each channel,
            for (int currChannel = 0; currChannel < _channels; currChannel++) {
                double sample = doubleArray[currChannel][currSamp];

                // Perform clipping, if necessary.
                if (sample > 1.0) {
                    sample = 1.0;
                } else if (sample < -1.0) {
                    sample = -1.0;
                }

                // signed integer representation of current sample of the
                // current channel.
                // Note: Floor instead of cast to remove deadrange at zero.
                int intValue = (int) Math.floor(sample * _maxSample);

                int base = (currSamp * _bytesPerSample * _channels)
                        + (_bytesPerSample * currChannel);
                // Create byte representation of current sample.
                // Note: unsigned Shift right.
                // Note: fall through from higher number cases.
                switch (_bytesPerSample) {
                case 4:
                    _playbackData[base + 3] = (byte) intValue;
                    intValue >>>= 8;
                case 3:
                    _playbackData[base + 2] = (byte) intValue;
                    intValue >>>= 8;
                case 2:
                    _playbackData[base + 1] = (byte) intValue;
                    intValue >>>= 8;
                case 1:
                    _playbackData[base] = (byte) intValue;
                }
            }
        }
        return _playbackData;
    }

    /** Return a string that describes the possible encodings for an
     *  AudioFormat.
     *  @param format The audio format.
     *  @return A string describing the audio formats available.
     */
    private static String _encodings(AudioFormat format) {
        // Print out the possible encodings
        AudioFormat.Encoding[] encodings = AudioSystem
                .getTargetEncodings(format);
        StringBuffer encodingDescriptions = new StringBuffer();
        for (int i = 0; i < encodings.length; i++) {
            encodingDescriptions.append(encodings[i] + "\n");
            AudioFormat[] formats = AudioSystem.getTargetFormats(encodings[i],
                    format);
            for (int j = 0; j < formats.length; j++) {
                encodingDescriptions.append("  " + formats[j] + "\n");
            }
        }
        return encodingDescriptions.toString();
    }

    private static void _flushCaptureBuffer() {
        _targetLine.flush();
    }

    private static void _flushPlaybackBuffer() {
        _sourceLine.flush();
    }

    /** Notify the live sound listeners about a change in an audio
     *  parameter.
     *
     *  @param parameter The audio parameter of LiveSound that
     *   has changed. The value of parameter should be one of
     *   LiveSoundEvent.SAMPLE_RATE, LiveSoundEvent.CHANNELS,
     *   LiveSoundEvent.BUFFER_SIZE, or
     *   LiveSoundEvent.BITS_PER_SAMPLE.
     *
     *  @exception Exception If a listener has a problem responding
     *   to the change.
     */
    private static void _notifyLiveSoundListeners(int parameter) {
        if (_liveSoundListeners.size() > 0) {
            LiveSoundEvent event = new LiveSoundEvent(parameter);
            Iterator listeners = _liveSoundListeners.iterator();

            while (listeners.hasNext()) {
                ((LiveSoundListener) listeners.next()).liveSoundChanged(event);
            }
        }
    }

    /** Start audio capture.
     */
    private static void _startCapture() throws IOException {
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(_sampleRate, _bitsPerSample,
                _channels, signed, bigEndian);

        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class,
                format, AudioSystem.NOT_SPECIFIED);

        try {
            _targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);

            // Note: 2nd parameter is the buffer size (in bytes).
            // Larger values increase latency but may be required if
            // garbage collection, etc. is an issue.
            _targetLine.open(format, _bufferSize * _bytesPerSample * _channels);
        } catch (IllegalArgumentException ex) {
            IOException exception = new IOException(
                    "Incorrect argument, possible encodings for\n" + format
                            + "\n are:\n" + _encodings(format));
            exception.initCause(ex);
            throw exception;
        } catch (LineUnavailableException ex2) {
            throw new IOException("Unable to open the line for "
                    + "real-time audio capture: " + ex2);
        }

        // Array of audio samples in byte format.
        _captureData = new byte[_transferSize * _bytesPerSample * _channels];
        _audioInDoubleArray = new double[_channels][_transferSize];

        // Start the target data line
        _targetLine.start();
    }

    /** Start audio playback.
     */
    private static void _startPlayback() throws IOException {
        boolean signed = true;
        boolean bigEndian = true;

        AudioFormat format = new AudioFormat(_sampleRate, _bitsPerSample,
                _channels, signed, bigEndian);

        // As of Java 5.0, no longer need to specify this.
        // Use the convenience method AudioSystem.getSourceDataLine(AudioFormat).
        // DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class,
        //        format, AudioSystem.NOT_SPECIFIED);

        // Get and open the source data line for playback.
        try {
            // Source DataLine is really a target for
            // audio data, not a source.
            _sourceLine = AudioSystem.getSourceDataLine(format);

            // Open line and suggest a buffer size (in bytes) to use or
            // the internal audio buffer.
            _sourceLine.open(format, _bufferSize * _bytesPerSample * _channels);
        } catch (IllegalArgumentException ex) {
            IOException exception = new IOException(
                    "Incorrect argument, possible encodings for\n" + format
                            + "\n are:\n" + _encodings(format));
            exception.initCause(ex);
            throw exception;
        } catch (LineUnavailableException ex) {
            throw new IOException("Unable to open the line for "
                    + "real-time audio playback: " + ex);
        }
        // Start the source data line
        _sourceLine.start();
    }

    /** Stop audio playback.
     */
    private static void _stopPlayback() {
        if (_sourceLine != null) {
            _sourceLine.drain();
            _sourceLine.stop();
            _sourceLine.close();
        }

        _sourceLine = null;
    }

    /** Stop audio capture.
     */
    private static void _stopCapture() {
        if (_targetLine != null) {
            if (_targetLine.isOpen() == true) {
                _targetLine.stop();
                _targetLine.close();
                _targetLine = null;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Array of audio samples in double format. */
    private static double[][] _audioInDoubleArray;

    /** The number of bits per sample. Default is 16. */
    private static int _bitsPerSample = 16;

    /** The requested buffer size in samples per channel. */
    private static int _bufferSize = 1024;

    /** The number of bytes per sample, default 2. */
    private static int _bytesPerSample = 2;

    /** true is audio capture is currently active. */
    private static boolean _captureIsActive = false;

    /** The number of channels. Deafult is 1. */
    private static int _channels = 1;

    /** Array of audio samples in byte format. */
    private static byte[] _captureData;

    /** Byte buffer used for playback data. */
    private static byte[] _playbackData;

    private static List _liveSoundListeners = new LinkedList();

    // Cashed value of the maximum value scaling factor, default for 16 bits.
    private static double _maxSampleReciprocal = 1.0 / 32768;

    // Cashed value of the maximum integer value, default for 16 bits.
    private static double _maxSample = 32767;

    // true is audio playback is currently active
    private static boolean _playbackIsActive = false;

    private static float _sampleRate;

    private static List _soundConsumers = new LinkedList();

    /** Interface to the hardware for reading data. */
    private static SourceDataLine _sourceLine;

    /** Interface to the hardware for producing sound. */
    private static TargetDataLine _targetLine;

    /** The number of audio samples to transfer per channel when getSamples() is invoked. */
    private static int _transferSize = 128;

}

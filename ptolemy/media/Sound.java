/* A library of audio operations using the Java Sound API.

Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (srao@eecs.berkeley.edu)
@AcceptedRating Red (srao@eecs.berkeley.edu)
*/

package ptolemy.media;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.NoSuchElementException;
import javax.media.sound.sampled.AudioInputStream;
import javax.media.sound.sampled.AudioSystem;
import javax.media.sound.sampled.AudioFormat;
import javax.media.sound.sampled.AudioFileFormat;
import javax.media.sound.sampled.Type;
import javax.media.sound.sampled.SourceDataLine;
import javax.media.sound.sampled.Line;
import javax.media.sound.sampled.DataLine;
import javax.media.sound.sampled.Mixer;
import javax.media.sound.sampled.LineUnavailableException;
import javax.media.sound.sampled.UnsupportedAudioFileException;

//////////////////////////////////////////////////////////////////////////
//// Sound 
/** Instances of this class represent streams of audio data 
 *  based on the new Java Sound API. The audio data may be retrieved 
 *  from a file, a URL, an array of bytes, or an array of doubles. 
 *  The file formats that are currently available are AU, WAVE, and AIFF. 
 *  The format of audio data (i.e. bytes per sample, sample rate, and 
 *  number of channels) must be specified if the audio data is not
 *  retrieved from a file or URL.
 * @author Shankar R. Rao
 * @version $Id$
 */


public class Sound {
    
    /** Create a new instance which initially has no data in its audio
     *  queue. By default this stores two bytes per sample, has a sample
     *  rate of 8 kHz, and one audio channel (mono playback). To change
     *  to a different format, use the setFormat method.
     */
    public Sound() {
        setFormat(2, 8000, 1);
        _soundQueue = new AudioQueue(true,
                getBytesPerSample(), getSampleRate());	
    }
         
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
     
    /** Empty the sound queue of all samples.
     */
    public void clearSamples() {
	_soundQueue.clearSamples();
	_byteBuffer = null;
	_doubleBuffer = null;
    }

    /** Return the audio data currently stored in the sound queue as an  
     *  array of bytes. This does not modify the audio data.
     *	@return a byte array representing the audio data.
     */
    public byte[] getByteArray() {
	if (_byteBuffer == null)
	    _refreshByteBuffer();
	return _byteBuffer;
    }
    
    /** Return the number of bytes of the audio data.
     *  @return the size (in bytes) of the current audio data.
     */
    public int getByteCount() {
        return _soundQueue.getByteCount();
    }

    /** Return the number of bytes per sample in this Sound.
     *  @return the number of bytes per sample in this Sound.
     */
    public int getBytesPerSample() {
        return _format.getSampleSizeInBits() / 8;
    }

    /** Return the number of audio channels in this Sound (one for mono, 
     *  and two for stereo).
     *  @return the number of audio channels in this Sound.
     */
    public int getChannelCount() {
        return _format.getChannels();
    }

    /** Return the audio data currently stored in the sound queue as an 
     *  array of doubles. The array is indexed from the oldest stored 
     *  sample to the most recently stored sample. This does not modify 
     *  the audio data.
     *  @return a double array with linearly encoded samples between -1.0 
     *          and 1.0 .
     */
    public double[] getDoubleArray() {
	if (_doubleBuffer == null) 
	    _refreshDoubleBuffer();
	return _doubleBuffer;
    }

    /** Obtain one sample from the sound queue. This sample is assumed to
     *  be linear encoded and normalized between -1.0 and 1.0. If the
     *  sound queue is empty, this method will return 0.0 .
     *  @return one sample from the sound queue, or 0.0 if the sound 
     *          queue is empty.
     */
    public double getSample() {
        if (_soundQueue.isEmpty())
	    return 0.0;
	_byteBuffer = null;
	_doubleBuffer = null;
	return _soundQueue.getDouble();
    }

    /** Obtain the number of samples currently stored in the sound queue
     *  of this Sound.
     *  @return the number of samples.
     */
    public double getSampleCount() {
	return _soundQueue.getSampleCount();
    }

    /** Return the sample rate (in samples per second).
     *  @return the sample rate (in samples per second).
     */
    public double getSampleRate() {
        return _format.getSampleRate();
    }

    /** Obtain an array of samples from the sound queue. The samples 
     *  are assumed to be linearly encoded and between -1.0 and 1.0. 
     *  The array returned is indexed from oldest sample in the queue 
     *  to the most recent sample in the queue. If there are not enough 
     *  samples in the sound queue, then it will return a smaller array.
     *  @return an array of samples from the sound queue.
     */
    public double[] getSamples(int numSamples) {
	_byteBuffer = null;
	_doubleBuffer = null;
        return _soundQueue.getDoubles(numSamples);
    }
    
    /** Load the contents of the array into the sound queue so that the
     *  first element of the array will ne the oldest sample in the 
     *  sound queue, and the last element of the array will be the most
     *  recent sample in the sound queue. The previous samples in the
     *  sound queue will be lost. The format of this Sound (bytes per 
     *  sample, sample rate, and number of channels) will remain unchanged.
     *  If this method is called during playback, then playback will be 
     *  stopped before the array is loaded.
     *  @param buffer an array of samples to be stored in the sound queue.
     */
    public void load(double[] buffer) {
        _doubleBuffer = buffer;
        _refreshByteBufferFromDoubleBuffer();
        _soundQueue = new ByteArrayAudioQueue(_byteBuffer);
    }

    /** Load the contents of the array into the sound queue so that the
     *  first element of the array will be the oldest byte in the 
     *  sound queue, and the last element of the array will be the most
     *  recent byte in the sound queue. The previous bytes in the
     *  sound queue will be lost. The format of this Sound (bytes per 
     *  sample, sample rate, and number of channels) will remain unchanged.
     *  If this method is called during playback, then playback will be 
     *  stopped before the array is loaded. 
     *  @param buffer an array of bytes to be stored in the sound queue.
     */
    public void load(byte[] buffer) {
        _byteBuffer = buffer;
        _soundQueue = new ByteArrayAudioQueue(_byteBuffer);
    }

    /** Load the audio data stored in the given file into the sound queue. 
     *  The previous data stored in the sound queue will be destroyed.
     *  The format of this Sound (bytes per sample, sample rate, and number
     *  of channels) will be changed to match the audio format of the file.
     *  If this method is called during playback, then playback will
     *  be stopped before the file is loaded.
     *  @param file an audio file to be loaded into the sound queue.
     */
    public void load(File file) {
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        _format = _getProperFormat(ais.getFormat(), AudioFormat.PCM_SIGNED);
        _loadAsStream(AudioSystem.getAudioInputStream(_format, ais));
    }

    /** Load the audio data stored in the given url into the sound queue. 
     *  The previous data stored in the sound queue will be destroyed.
     *  The format of this Sound (bytes per sample, sample rate, and number
     *  of channels) will be changed to match the audio format of the url.
     *  If this method is called during playback, then playback will
     *  be stopped before the url is loaded.
     *  @param url an audio url to be loaded into the sound queue.
     */
    public void load(URL url) {
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        _format = _getProperFormat(ais.getFormat(), AudioFormat.PCM_SIGNED);
        _loadAsStream(AudioSystem.getAudioInputStream(_format, ais));
    }

    /** Obtain control of an output channel of the mixer and play 
     *  the audio data stored in the sound queue in a new thread of
     *  exectuion . This method will loop continuously until either 
     *  the program exits or the stopPlayback() method is called.
     *  @exception LineUnavailableException if the output channel is
     *   currently in use by another audio application.
     */
    public void loopPlayback() throws LineUnavailableException {
	_pushThread = new LoopThread();
	if (_byteBuffer == null)
	    _refreshByteBuffer();
	_playback();
    }
    
    /** Pause the playback of audio through the output channel. If there is
     *  currently no audio being played, then this method will do nothing.
     */
    public void pausePlayback() {
	if( _target == null ) 
	    return;
	if( _target.isActive() ) 
	    _target.stop();
    }

    /** Place sample into the sound queue to be processed in real time.
     *  The sample is assumed to be linear encoded and normalized 
     *  between -1.0 and 1.0 . If this method is called when _isRealTime
     *  is not true (when audio data is read in batch mode from memory,
     *  a file or a URL), then this method will do nothing.
     *  @param sample the sample to be placed in the sound queue.
     */
    public void putSample(double sample) {
	_soundQueue.put(sample);
	_byteBuffer = null;
    }

    /** Place samples from the double array into the sound queue to 
     *  be processed in real time. The samples are assumed to be linear 
     *  encoded and normalized between -1.0 and 1.0 .
     *  @param sampleArray A double array of samples to be placed in 
     *   the sound queue.
     */
    public void putSample(double[] sampleArray) {
	_soundQueue.put(sampleArray);
	_byteBuffer = null;
    }

    /** Obtain an output channel and play audio through it in real time.
     *  @exception LineUnavailableException If the output channel is locked 
     *   by another application
     */
    public void realTimePlayback() throws LineUnavailableException{
	_pushThread = new RealTimeThread();
	_playback();
    }

    /** Resume playback of the current audio stream after being
     *  paused. If this method is called before being paused,
     *  then it will simply start playback of the audio stream.
     *  If it is called during playback of audio it will do 
     *  nothing.
     *  @exception LineUnavailableException if another audio application
     *   has a lock on the output channel.
     */
    public void resumePlayback() throws LineUnavailableException {
	if( _target == null ) 
	    startPlayback();
	else if( !_target.isActive() ) 
	    _target.start();
    }

    /** Write audio data to a file in the given file format. The
     *  arguments are the name of the file to which the audio data 
     *  will be written, and the type of file that it will be saved
     *  as. The current file formats that can be used are AU, WAVE, 
     *  and AIFF. The audio data will not be modified.
     *	@param fileName the name of the file.
     *  @param fileType the filetype that the audio data will be saved as.
     *  @exception IOException if the audio data could not be saved to file.
     */ 
    public void saveAs(String filename, Type fileType) 
            throws IOException {
	if (_byteBuffer == null)
	    _refreshByteBuffer();
	InputStream is = new ByteArrayInputStream(_byteBuffer); 
	File file = new File( filename );
	int length = _byteBuffer.length / _format.getFrameSize();
	AudioFileFormat aff = 
            new AudioFileFormat(fileType, _format, _byteBuffer.length);
	AudioSystem.write(is, aff, file);
    }
    
    /** Set the format of this Sound to have the given parameters.
     *  @param bytesPerSample The number of bytes per sample.
     *  @param sampleRate The sample rate (in samples per second).
     *  @param channelCount The number of audio channels (one for mono, 
         and two for stereo)
    */
    public void setFormat(int bytesPerSample, double sampleRate, 
            int channelCount) {
        _format =  new AudioFormat((float) sampleRate, bytesPerSample * 8,
                channelCount, true, true);
    }

    /** Create a new thread of execution that plays back the
     *  audio data stored in the sound queue.
     *  @exception LineUnavailableException if another audio application
     *   has control of the current output channel. 
     */ 
    public void startPlayback() throws LineUnavailableException {
	_pushThread = new BatchThread();
	if (_byteBuffer == null)
	    _refreshByteBuffer();
	_playback();
    }

    /** Stop playback of audio through the output channel. 
     */
    public void stopPlayback() {
	/*  FIXME: Currently this method takes a long time to actually
	 *  stop the audio data because the output channel has a very large 
	 *  buffer (4096), and it when the output channel writes to its
	 *  buffer, it can't be interrupted. I have tried to uses a smaller
	 *  buffer for the output channel but that results in the audio data 
	 *  sounding terrible at high sampling rates. 
	 */
        _target.close();
	if (_pushThread != null) {
	    _stopping = true;
	    while(!_stopped) {
		try{
		    Thread.sleep(10);
		} catch (InterruptedException e) {}
	    }
	    _pushThread = null;
	}
        
	_looping = false;
    }
    
    /** Return a String representation of the type of audio data stored
     *  in this class.
     *  @return a String representation of the audio data.
     */
    public String toString() {
	return "Ptolemy Sound ( " + "format = " + _format + " )";
    }
     
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Make the given stream the current audio stream for playback.
     *  Since at this time, the Java Sound API can't playback in
     *  mu-law or a-law format, the stream is first converted
     *  to a linear format. This method will throw an IOException
     *  if pass a null audio stream or it can't convert the
     *  audio stream to a linear format for playback.
     */
    private void _loadAsStream(AudioInputStream ais) throws IOException {
	_format = ais.getFormat();
	_byteBuffer = new byte[(int) ais.getLength() * _format.getFrameSize()];
	ais.read(_byteBuffer);
	_soundQueue = new ByteArrayAudioQueue(_byteBuffer, true, getBytesPerSample(), getSampleRate());
	_doubleBuffer = null;
    }

    /** Refresh the byte array and double array buffers used by
     *  getByteArray() and getDoubleArray() respectively. This
     *  should not modify the audio stream. This method will
     *  throw an IOException if an I/O error occurs while reading
     *  from the audio stream into the byte array buffer.
     */
    private void _refreshByteBuffer() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	while (!_soundQueue.isEmpty())
            baos.write(_soundQueue.getByte());
	_byteBuffer = baos.toByteArray();
	for (int i = 0; i < _byteBuffer.length; i += 1) 
            _soundQueue.put(_byteBuffer[i]);
    }
 

    /** Transfer the audio data stored in the byte array buffer 
     *  into the double array buffer so that every element in the 
     *  double array will be a linear encoded sample normalized 
     *  between -1.0 and 1.0 . 
     */
    private void _refreshDoubleBuffer() {
        AudioQueue aqs = new ByteArrayAudioQueue(_byteBuffer, true, getBytesPerSample(), getSampleRate());
        int numSamples = _byteBuffer.length * 8 
            / _format.getSampleSizeInBits();
        _doubleBuffer = new double[numSamples];  
        for (int i = 0; !aqs.isEmpty(); i += 1) 
	    _doubleBuffer[i] = aqs.getDouble();
    }
   
    /** Transfer the audio data stored in the double array buffer into 
     *  the byte array buffer. 
     */
    private void _refreshByteBufferFromDoubleBuffer() {
	AudioQueue aqs = new DoubleArrayAudioQueue(_doubleBuffer, true, getBytesPerSample(), getSampleRate());
	int numBytes = _doubleBuffer.length * 
            _format.getSampleSizeInBits() / 8;
	_byteBuffer = new byte[numBytes];  
	for (int i = 0; !aqs.isEmpty(); i += 1)
  	    _byteBuffer[i] = aqs.getByte();
    }

    /** Return true if the input encoding is a compressed encoding
     *  and the output is not compressed, and false otherwise.
     */
    private boolean _needToDoubleSampleSize(Type input, Type output) {
	return (((input == AudioFormat.ALAW) || 
                (input == AudioFormat.ULAW)) &&
		!((output == AudioFormat.ALAW) || 
                        (output == AudioFormat.ULAW)));
    }

    /** Return true if the input encoding is not a compressed encoding
     *  and the output encoding is compressed, and false otherwise.
     *  Currently this method should never be used because it is
     *  not possible to convert from a linear format back to mu-law
     *  or a-law.
     */
    private boolean _needToHalfSampleSize(Type input, Type output) {
	return _needToDoubleSampleSize(output, input);
    }

    /** Convert the audio data to the given audio encoding. Currently the
     *  only conversions that are available are from mu-law or a-law
     *  encoded data to linear encoded data that is either MSB or LSB.
     *  If converting form mu-law or a-law to a linear format, the size
     *  of a sample will be doubled, but everything else about the 
     *  current AudioFormat (i.e sample rate, number of channels) will
     *  remain the same.
     */
    private AudioFormat _getProperFormat(AudioFormat af, Type ae) {
	if (af.getEncoding() == ae)
	    return af;
	double srate = af.getSampleRate();
	int ssize = af.getSampleSizeInBits();
	int channels = af.getChannels();
	boolean isBigEndian = true;
        boolean isSigned = true;
	if (_needToDoubleSampleSize(af.getEncoding(), ae)) 
	    af = new AudioFormat((float) srate, ssize * 2, channels, isSigned,
                    isBigEndian);
	else if (_needToHalfSampleSize(_format.getEncoding(),ae))
	    af = new AudioFormat((float) srate, ssize / 2, channels, 
                    isSigned, isBigEndian);
	else af = new AudioFormat((float) srate, ssize, channels, 
               isSigned, isBigEndian);
	return af;
    } 

    private void _playback() throws LineUnavailableException {
	 DataLine.Info lineInfo = 
            new DataLine.Info(SourceDataLine.class, null, null,
                    new Class[0], _format, AudioSystem.NOT_SPECIFIED);
        if (!AudioSystem.isSupportedLine(lineInfo))
                throw new LineUnavailableException("Line is not supported: " +
                        lineInfo);
	_target = (SourceDataLine) AudioSystem.getLine(lineInfo);
        _target.open(_format, _target.getBufferSize());
	_stopping = false;
	_stopped = false; 
	_pushThread.start();
    }

    private class BatchThread extends Thread {
	public BatchThread() {
	    super ("BatchThread");
	}

	/** Play audio through the output channel until the sound queue 
         *  is empty.
	 */
	public void run() {
	    byte[] data = new byte[_target.getBufferSize() 
				  * _format.getFrameSize() / 8];
	    boolean endOfStream = false;
	    int framesRemaining, bytesRead;
	    _target.start();
	    while(!_stopping && !endOfStream) {
		for (bytesRead = 0; bytesRead < data.length; bytesRead += 1) {
		    if (_soundQueue.isEmpty()) {
			endOfStream = true;
			break;
		    }
		    data[bytesRead] = _soundQueue.getByte();
		}
		framesRemaining = bytesRead / _format.getFrameSize();
		while ((framesRemaining > 0) && !_stopping) 
		    framesRemaining -= _target.write(data, 0, framesRemaining);
	    }
	    _stopped = true;
	    _target.write(null, 0, 0);
	    _target.stop();
	    _soundQueue = new ByteArrayAudioQueue
                (_byteBuffer, true, getBytesPerSample(), getSampleRate());
	}
    }

    private class RealTimeThread extends Thread {
	public RealTimeThread() {
	    super ("RealTimeThread");
	}
	
	/** Play audio continuously through the output channel.
	 */
	public void run() {
	    byte[] data = new byte[_target.getBufferSize()];
	    int framesRemaining, bytesRead;
	    _target.start();
	    while(!_stopping) {
		for (bytesRead = 0; bytesRead < data.length; bytesRead += 1) {
		    if (_soundQueue.isEmpty()) 
			if (_isRealTime)
			    for (int i = 0; i < getChannelCount(); i += 1) 
				_soundQueue.put(0.0);
		    data[bytesRead] = _soundQueue.getByte();
		}
		framesRemaining = bytesRead / _format.getFrameSize();
		while ((framesRemaining > 0) && !_stopping) 
		    framesRemaining -= _target.write(data, 0, framesRemaining);
	    }
	    _stopped = true;
	    _target.write(null, 0, 0);
	    _target.stop();
	}
    }

    private class LoopThread extends Thread {
	public LoopThread() {
	    super ("LoopThread");
	}
	
	/** Play audio continuously through the output channel.
	 */
	public void run() {
	    byte[] data = new byte[_target.getBufferSize()];
	    boolean endOfStream = false;
	    int framesRemaining, bytesRead;
	    _target.start();
	    while(!_stopping) {
		for (bytesRead = 0; bytesRead < data.length; bytesRead += 1) {
		    if (_soundQueue.isEmpty()) 
			_soundQueue = new ByteArrayAudioQueue
                            (_byteBuffer, true, getBytesPerSample(), 
                                    getSampleRate());
		    data[bytesRead] = _soundQueue.getByte();
		}
		framesRemaining = bytesRead / _format.getFrameSize();
		while ((framesRemaining > 0) && !_stopping) 
		    framesRemaining -= _target.write(data, 0, framesRemaining);
	    }
	    _stopped = true;
	    _target.write(null, 0, 0);
	    _target.stop();
	    _soundQueue = new ByteArrayAudioQueue
                (_byteBuffer, true, getBytesPerSample(), getSampleRate());
	}
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private AudioFormat _format = null;

    // Queue of audio data 
    private AudioQueue _soundQueue = null;
    
    // The mixer used to obtain an output channel
    private Mixer _mixer = null;

    // The thread of execution used for playback
    private Thread _pushThread = null;
  
    // true if the audio data will be sent to the output channel 
    // in real time through the putSample method. 
    private boolean _isRealTime = false;

    // true if the current audio stream is stopped
    private boolean _stopped = false;
    
    // true if the current audio stream is in the process of stopping
    private boolean _stopping = false;

    // true if the current audio stream is set to loop continuously
    private boolean _looping = false;

    // the output channel to which audio data is written
    private SourceDataLine _target = null;

    // the current byte array representation of the audio data. the
    // AudioFormat _format  specifies exactly how the bytes in
    // this buffer represent audio data, such as how many
    // bytes are used per audio sample, and whether those bytes
    // are stored in MSB or LSB order.
    private byte[] _byteBuffer = null;

    // the current double array representation of the audio data. Each
    // element in the double array is a linear encoded sampled 
    // normalized between -1.0 and 1.0. This format is especially nice
    // because it can be easily used with Ptolemy, and it does not
    // have a specific byte representation as the byte array does,
    // thus it could be used to convert between MSB and LSB, a
    // functionality which Java Sound currently lacks.
    private double[] _doubleBuffer = null;

    // The size of the buffer for the output channel. I pulled
    // this figure from a Java Sound demo, and it seems to work
    // ok. The only problem is that this causes the stopPlayback()
    // method to take a ridiculous amount of time. If I lower
    // this value, however, the sound quality for streams with 
    // high sample rates degrades noticably because not enough data 
    // can be stored on the buffer.
    private static final int _OUTPUT_BUFFER_SIZE = 4096;

}

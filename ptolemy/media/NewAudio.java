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
import javax.media.sound.sampled.AudioStream;
import javax.media.sound.sampled.AudioSystem;
import javax.media.sound.sampled.AudioFormat;
import javax.media.sound.sampled.AudioFormat.Encoding;
import javax.media.sound.sampled.FileStream;
import javax.media.sound.sampled.OutputChannel;
import javax.media.sound.sampled.Channel;
import javax.media.sound.sampled.Mixer;
import javax.media.sound.sampled.AudioUnavailableException;

//////////////////////////////////////////////////////////////////////////
//// NewAudio FIXME : Need to change the name of this class!!!
/** Instances of this class represent streams of audio data 
 *  based on the new Java Sound API. The audio data may be retrieved 
 *  from a file, a URL, a byte array, a double array, or another 
 *  input stream. Currently the file formats that are available 
 *  are AU, WAVE, and AIFF. The audio data can have sample rates
 *  ranging from 8.0 kHz to 44.1 kHz. If the audio data is retrieved
 *  from a file or URL, no other specification is neccessary, but 
 *  if obtained from an input stream or an array, and AudioFormat 
 *  must also be provided. For more information on the Java Sound
 *  API, and in particular the AudioFormat class, please visit
 *  following link : 
 *  </CENTER> <p>
 *  <a href=http://java.sun.com/products/java-media/sound/index.html>
 *  http://java.sun.com/products/java-media/sound/index.html</a>
 *
 * @author Shankar R. Rao
 * @version $Id$
 */


public class NewAudio implements Runnable {
    
    /** Create a new instance which is initially empty. This instance
     *  can receive data through the receiveSample method in real time.
     *  These samples are assumed to be linear encoded and normalized
     *  between -1.0 and 1.0 .
     *	@param af The AudioFormat that data samples should have.
     */
    public NewAudio(AudioFormat af) {
        _soundQueue = new AudioQueue(af);
	_format = _getProperFormat(af, Encoding.PCM_SIGNED_BIG_ENDIAN);
	_byteBuffer = null;
	_doubleBuffer = null;
	_isRealTime = true;
    }

    /** Create a new instance initialized with the given double array.
     *	The arguments are a double array containing the audio data
     *	and an AudioFormat which specified how the data should be
     *  proccessed. The array is assumed to have linear samples
     *	normalized between -1.0 and 1.0 .
     *	@param buffer A double array containing the audio data. 
     *	@param af The AudioFormat of the audio data.
     * 	@throws IOException If the audio data could not be converted 
     *	 to a usable format, or an I/O error occurs while the data is
     *   being converted. 
     */
    public NewAudio(double[] buffer, AudioFormat af) throws IOException {
        _format = af;
        _doubleBuffer = buffer;
	_doubleToByte();
	InputStream is = new ByteArrayInputStream(_byteBuffer);
	_loadAsStream(new AudioStream(is, af, _byteBuffer.length));
    }
  
    /** Create a new instance initialized with the given byte array.
     *  The arguments are a byte array containing the audio data
     *  and an AudioFormat which specifies how the data should be
     *  processed. 	
     *	@param buffer A byte array containing the audio data.
     *	@param af The AudioFormat of the audio data.
     * 	@throws IOException If the audio data could not be converted 
     *   to a usable format, or an I/O error occurs while the data is
     *   being converted.
     */
    public NewAudio(byte[] buffer, AudioFormat af) throws IOException {
	this(new ByteArrayInputStream(buffer), af, buffer.length);
    }  
  
    /** Create a new instance initialized with the given input stream
     *  and a given length.
     *  @param is An input stream containing the audio data.
     *  @param af The AudioFormat of the audio data.
     *  @param length The length, in bytes, of the audio data.
     *  @throws IOException if the audio data could not be converted 
     *   to a usable format, or an I/O error occurs while the data is 
     *   being converted.
     */
    public NewAudio(InputStream is, AudioFormat af, long length) 
            throws IOException {
	_loadAsStream(new AudioStream(is, af, length));
    }

    /** Create a new instance initialized with the given audio stream.
     *  @param as An audio stream containing the audio data.
     *  @throws IOException if the audio data could not be converted 
     *   to a usable format, or an I/O error occurs while the data is
     *   being converted.
     */
    public NewAudio(AudioStream as) throws IOException {
	_loadAsStream(as);
    }

    /** Create a new instance initialized with the audio data provided
     *  by a file or URL specified with the given String. The current file
     *  types that can be parsed for audio data are AU, WAVE and AIFF.
     *  @param pathName A String that specifies a file containing audio data.
     *  @throws IOException if the file does not exist, can't be parsed 
     *   for audio data, or some I/O error occurs while reading the file.
     */
    public NewAudio(String pathName) throws IOException {
	try {
	    URL theurl = new URL(pathName);
	    InputStream urlstream = theurl.openStream();
	    if (urlstream == null) 
		throw new IOException("Cannot open URL: " + pathName);
	    else _loadAsStream(urlstream);
	} catch (MalformedURLException e) {
	    FileInputStream infile = new FileInputStream(pathName);
	    if (infile == null) 
		throw new IOException ("Cannot open file: " + pathName);
	    else _loadAsStream(infile);
	} 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
     
    /** Return the format of the current AudioStream.
     *  @return the format of the current AudioStream. 
     */
    public AudioFormat getFormat() {
	return _format;
    }

    /** Return the size (in bytes) of t
        he current audio data.
        *  @return the size (in bytes) of the current audio data.
        */
    public int size() {
        return _soundQueue.numBytes();
    }

    /** Return the audio data as a double array. This does not modify 
     *  the audio data.
     *  @return a double array with values normalized between -1.0 and 1.0 .
     *	@throws IOException if an I/O error occurs while the audio data 
     *   is being converted. 
     */
    public double[] getDoubleArray() throws IOException {
	if (_doubleBuffer == null)
	    if (_byteBuffer == null)
		_refreshBuffer();
	    else _byteToDouble();
	return _doubleBuffer;
    }

    /** Return the audio data as a byte array. This does not modify 
     *  the audio data.
     *	@return a byte array representing the audio data.
     *	@throws IOException if an I/O error occurs while the audio 
     *   data is being converted. 
     */
    public byte[] getByteArray() throws IOException {
	if (_byteBuffer == null)
	    _refreshBuffer();
	return _byteBuffer;
    }

    /** Place sample into the sound queue to be processed in real time.
     *  The sample is assumed to be linear encoded and normalized 
     *  between -1.0 and 1.0 . If this method is called when _isRealTime
     *  is not true (when audio data is read in batch mode from memory,
     *  a file or a URL), then this method will do nothing.
     *  @param sample the sample to be placed in the sound queue.
     */
    public void putSample(double sample) {
	if (_isRealTime)
	    _soundQueue.put(sample);
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
	return _soundQueue.getDouble();
    }

    /** Write audio data to a file in the given file format. The
     *  arguments are the name of the file to which the audio data 
     *  will be written, and the type of file that it will be saved
     *  as. The current file formats that can be used are AU, WAVE, 
     *  and AIFF.
     *	@param fileName the name of the file.
     *  @param fileType the filetype that the audio data will be saved as.
     *  @throws NullPointerException if the current audio stream is null.
     *  @throws IOException if the audio data could not be saved to file.
     */ 
    public void saveAs(String filename, FileStream.FileType fileType) 
            throws IOException {
        _refreshBuffer();
	InputStream is = new ByteArrayInputStream(_byteBuffer);
        AudioStream as = new AudioStream
            (is, _format, AudioStream.UNKNOWN_LENGTH); 
	File file = new File( filename );
	if (AudioSystem.write(as, file, fileType, -1) == null) 
	    throw new IOException ("Could not save audio data to file");
    }
    
    /** Obtain control of an output channel of the mixer and play 
     *  the current audio stream. The audio stream will loop 
     *  continuously until either the program exits or the 
     *  stopPlayback() method is called.
     *  @throws AudioUnavailableException if the output channel is
     *   currently in use by another audio application.
     */
    public void loopPlayback() throws AudioUnavailableException {
	_looping = true;
	startPlayback();
    }
    
    /** Obtain control of an output channel of the mixer for
     *  audio playback. The audio data can then be played with
     *  a subsequent call to startPlayback(). Note: currently
     *  startPlayback calls this method so that users do not
     *  need to actually ever call this method. It might be
     *  made private in later versions.
     *  @throws NullPointerException if the current audio stream is null.
     *  @throws AudioUnavailableException if another audio application
     *   has a lock on the output channel.
     */
    public void openPlayback() throws AudioUnavailableException {
	if (_mixer == null) 
	    _mixer = AudioSystem.getMixer(null);        
	_target = _mixer.getOutputChannel(_format, _OUTPUT_BUFFER_SIZE);
    }

    /** Create a new thread of execution that plays back the
     *  current audio stream. 
     *  @throws AudioUnavailableException if another audio application
     *   has control of the current output channel. 
     */ 
    public void startPlayback() throws AudioUnavailableException {
	openPlayback();
	if (_pushThread != null)
	    throw new NullPointerException 
		("Playback thread is not null, cannot start playback : ");
	if (_target == null) 
	    throw new NullPointerException 
		("OutputChannel not set, cannot start playback");
	_stopping = false;
	_stopped = false; 
	_pushThread = new Thread(this);
	_pushThread.start();
    }

    /** FIXME : Stop playback of the current audio stream through the output
     *  channel. Currently this method takes a long time to actually
     *  stop the audio data because the output channel has a very large 
     *  buffer (4096), and it when the output channel writes to its
     *  buffer, it can't be interrupted. I have tried to uses a smaller
     *  buffer for the output channel but that results in the audio data 
     *  sounding terrible at high sampling rates. 
     */
    public void stopPlayback() {
        if (_target.isPaused()) {
            _target.flush();
            _target.resume();
        }
	if (_pushThread != null) {
	    _stopping = true;
	    while(!_stopped) {
		try{
		    Thread.sleep(10);
		} catch (InterruptedException e) {}
	    }
	    _pushThread = null;
	}
        
	if (!_isRealTime) 
            _soundQueue = new ByteArrayAudioQueue(_byteBuffer, getFormat());
	_looping = false;
    }
  
    /** Pause the playback of the current audio stream through
     *  the output channel.
     */
    public void pausePlayback() {
	if( _target == null ) 
	    throw new NullPointerException 
		("OutputChannel not set, cannot pause");
	if( !_target.isPaused() ) 
	    _target.pause();
    }

    /** Resume playback of the current audio stream after being
     *  paused. If this method is called before being paused,
     *  then it will simply start playback of the audio stream.
     *  @throws AudioUnavailableException if another audio application
     *   has a lock on the output channel.
     */
    public void resumePlayback() throws AudioUnavailableException {
	if( _target == null ) 
	    startPlayback();
	else if( _target.isPaused() ) 
	    _target.resume();
    }

    /** Play audio continuously through the output channel.
     */
    public void run() {
	byte[] data = new byte[_target.getBufferSize()/ 2];
	boolean endOfStream = false;
	int bytesRemaining, bytesRead;
	while(!_stopping && (!endOfStream || _isRealTime)) {
	    for (bytesRead = 0; bytesRead < data.length; bytesRead += 1) {
		if (_soundQueue.isEmpty()) 
		    if (_isRealTime)
                        for (int i = 0; i < _format.getChannels(); i += 1) 
                            _soundQueue.put(0.0);
		    else if (!_looping) {
		        endOfStream = true;
			break;
		    } else _soundQueue = new ByteArrayAudioQueue
                               (_byteBuffer, _format);
                try {
                    data[bytesRead] = _soundQueue.getByte();
                } catch (NoSuchElementException e) {
                    data[bytesRead] = 0;
System.out.println("NO SUCH");
 
                }
	    }
	    bytesRemaining = bytesRead;
	    while ((bytesRemaining > 0) && !_stopping) 
	        bytesRemaining -= _target.write(data, 0, bytesRead);
     	}
	_stopped = true;
	_target.write(null, 0, 0);
    }

    /** Return a String representation of the type of audio data stored
     *  in this class.
     *  @return a String representation of the audio data.
     */
    public String toString() {
	return "Ptolemy Audio\n"
	    + "-------------------\n"
	    + "format = " + getFormat() + "\n";
    }
       
    /** Convert the audio data to the given audio encoding. Currently the
     *  only conversions that are available are from mu-law or a-law
     *  encoded data to linear encoded data that is either MSB or LSB.
     *  If converting form mu-law or a-law to a linear format, the size
     *  of a sample will be doubled, but everything else about the 
     *  current AudioFormat (i.e sample rate, number of channels) will
     *  remain the same.
     */
    private AudioFormat _getProperFormat(AudioFormat af, Encoding ae) {
	if (af.getEncoding() == ae)
	    return af;
	double srate = af.getSampleRate();
	int ssize = af.getSampleSizeInBits();
	int channels = af.getChannels();
	int fsize = af.getFrameSizeInBits();
	double frate = af.getFrameRate();
	if (_needToDoubleSampleSize(af.getEncoding(), ae)) 
	    af = new AudioFormat(ae, srate, ssize * 2, channels, 
                    fsize * 2, frate);
	else if (_needToHalfSampleSize(getFormat().getEncoding(),ae))
	    af = new AudioFormat(ae, srate, ssize / 2, channels, 
                    fsize / 2, frate);
	else af = new AudioFormat(ae, srate, ssize, channels, 
                fsize, frate);
	return af;
    } 

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the input encoding is a compressed encoding
     *  and the output is not compressed, and false otherwise.
     */
    private boolean _needToDoubleSampleSize(Encoding input, 
            Encoding output) {
	return (((input == Encoding.ALAW) || 
                (input == Encoding.ULAW)) &&
		!((output == Encoding.ALAW) || 
                        (output == Encoding.ULAW)));
    }

    /** Return true if the input encoding is not a compressed encoding
     *  and the output encoding is compressed, and false otherwise.
     *  Currently this method should never be used because it is
     *  not possible to convert from a linear format back to mu-law
     *  or a-law.
     */
    private boolean _needToHalfSampleSize(Encoding input, 
            Encoding output) {
	return _needToDoubleSampleSize(output, input);
    }

    /** Load the given input stream as the current audio stream 
     *  for playback. This method assumes that the audio data in 
     *  the input stream conforms to one of the supported filetypes. 
     *  If you wish to load an input stream that just contains raw 
     *  data, it is better to accomplish this by turning the input 
     *  stream into an audio stream, and then loading it. This method
     *  will throw an IOException if either the input stream can't
     *  we parsed into one of the known file formats, or the audio
     *  data can't be extracted from the stream.
     */
    private void _loadAsStream(InputStream is) throws IOException {
	FileStream fs = AudioSystem.getFileStream(is);
	if (fs == null)        
	    throw new IOException("Unable to obtain audio data from file");
	AudioStream as = AudioSystem.getAudioStream( fs );
	if (as == null)
	    throw new IOException
		("Unable to convert file data into audio data");
	_loadAsStream(as);
    }

    /** Make the given stream the current audio stream for playback.
     *  Since at this time, the Java Sound API can't playback in
     *  mu-law or a-law format, the stream is first converted
     *  to a linear format. This method will throw an IOException
     *  if pass a null audio stream or it can't convert the
     *  audio stream to a linear format for playback.
     */
    private void _loadAsStream(AudioStream as) throws IOException {
	if (as == null) 
	    throw new IOException("Can't load empty audio stream");
	_format = as.getFormat();
	AudioStream asold = as;
	if (!(_format.getEncoding() == Encoding.PCM_SIGNED_BIG_ENDIAN) &&
                !(_format.getEncoding() == Encoding.PCM_SIGNED_LITTLE_ENDIAN))
            as = AudioSystem.getAudioStream
                (_getProperFormat(_format, Encoding.PCM_SIGNED_BIG_ENDIAN), asold);
	if (as == null) 
	    throw new IOException
		("Could not convert audio stream to linear format");
	_format = as.getFormat();
	_byteBuffer = new byte[(int) as.getLength()];
	as.read(_byteBuffer);
	_soundQueue = new ByteArrayAudioQueue(_byteBuffer, _format);
	_doubleBuffer = null;
    }

    /** Refresh the byte array and double array buffers used by
     *  getByteArray() and getDoubleArray() respectively. This
     *  should not modify the audio stream. This method will
     *  throw an IOException if an I/O error occurs while reading
     *  from the audio stream into the byte array buffer.
     */
    private void _refreshBuffer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	while (!_soundQueue.isEmpty())
            baos.write(_soundQueue.getByte());
	_byteBuffer = baos.toByteArray();
	for (int i = 0; i < _byteBuffer.length; i += 1) 
            _soundQueue.put(_byteBuffer[i]);
	_byteToDouble();
    }
 

    /** Transfer the audio data stored in the byte array buffer 
     *  into the double array buffer so that every element in the 
     *  double array will be a linear encoded sample normalized 
     *  between -1.0 and 1.0 . 
     */
    private void _byteToDouble() {
        AudioQueue aqs = new ByteArrayAudioQueue(_byteBuffer, _format);
        int numSamples = _byteBuffer.length * 8 
            / _format.getSampleSizeInBits();
        _doubleBuffer = new double[numSamples];  
        for (int i = 0; !aqs.isEmpty(); i += 1) 
	    _doubleBuffer[i] = aqs.getDouble();
    }
   
    /** Transfer the audio data stored in the double array buffer into 
     *  the byte array buffer. The AudioFormat tells how many bytes 
     *  per sample there should be in the byte array, and whether the 
     *  bytes should be stored in MSB or LSB order.
     */
    private void _doubleToByte() {
	AudioQueue aqs = new DoubleArrayAudioQueue(_doubleBuffer, _format);
	int numBytes = _doubleBuffer.length * 
            _format.getSampleSizeInBits() / 8;
	_byteBuffer = new byte[numBytes];  
	for (int i = 0; !aqs.isEmpty(); i += 1)
  	    _byteBuffer[i] = aqs.getByte();
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private AudioFormat _format = null;

    // Queue of audio data waiting to be outputted
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
    private OutputChannel _target = null;

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

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
import javax.media.sound.sampled.AudioStream;
import javax.media.sound.sampled.AudioSystem;
import javax.media.sound.sampled.AudioFormat;
import javax.media.sound.sampled.AudioFormat.Encoding;
import javax.media.sound.sampled.FileStream;
import javax.media.sound.sampled.OutputChannel;
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
	this(_doubleToByte(buffer, af), af);
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
     
    /** Return the current AudioStream.
     *	@return the current AudioStream. 
     */
    public AudioStream getStream() {
	return _stream;
    }

    /** Return the format of the current AudioStream.
     *  @return the format of the current AudioStream. 
     */
    public AudioFormat getFormat() {
	return _stream.getFormat();
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
	    else _byteToDouble(getFormat());
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
	if(_stream == null) 
	    throw new NullPointerException ("No loaded audio to save");
	File file = new File( filename );
	if (AudioSystem.write(_stream, file, fileType, -1) == null) 
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
	OutputChannel channel = null;
	if (_stream == null)
	    throw new NullPointerException 
		("AudioStream is null, cannot start playback");
	if (_mixer == null) 
	    _mixer = AudioSystem.getMixer(null);        
	channel = _mixer.getOutputChannel(_stream.getFormat(), 
					  _OUTPUT_BUFFER_SIZE);
	_target = channel;
	_current = _stream;
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
	if (_current == null) 
	    throw new NullPointerException 
		("InputStream not set, cannot start playback");
	if (_target == null) 
	    throw new NullPointerException 
		("OutputChannel not set, cannot start playback");
	_stopping = false;
	_stopped = false; 
	_soundBuffer = new ByteArrayOutputStream();
	_pushThread = new Thread(this);
	_pushThread.start();
    }

    /** FIXME : Stop playback of the current audio stream through the output
     *  channel. Currently this method takes a long time to actually
     *  stop the audio data because the output channel has a very large 
     *  buffer (16384), and it when the output channel writes to its
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
	_byteBuffer = _soundBuffer.toByteArray();
	ByteArrayInputStream bais = new ByteArrayInputStream(_byteBuffer);
	_stream = new AudioStream(bais , getFormat(), _byteBuffer.length);
	_current = _stream;
	_soundBuffer = new ByteArrayOutputStream();
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

    /** Play the current audio stream through the output channel.
     *  This method is the implementation of the Runnable 
     *  interface and is used by startPlayback(). It might
     *  be a good idea to make a private extension of the
     *  Thread class instead of making NewAudio implement
     *  the Runnable Interface, so that the end user will not have 
     *  to this method. This method is a modified version of
     *  one used in a demo of the Java Sound API made by SUN.
     *  @throws RuntimeException FIXME : if an I/O error occurs 
     *   during playback. This is a hack because I can't throw
     *   an IOException, and yet one must be thrown if there
     *   is a I/O error.
     */
    public void run() {
	byte[] dataArray = new byte[_target.getBufferSize()/2];
	byte[] soundByte;
	int bytesRead, bytesRemaining;
	while(!_stopping) {         
	    try {						
		bytesRead = _current.read(dataArray);
		if (bytesRead == -1) 
		    if (!_looping) {
			_target.write(null, 0, 0);
			break;
		    } else {
			_byteBuffer = _soundBuffer.toByteArray();
			_current = new ByteArrayInputStream(_byteBuffer);
			_soundBuffer = new ByteArrayOutputStream();
			continue;
		    }
		bytesRemaining = bytesRead;
		while ((bytesRemaining > 0) && !_stopping){
		    bytesRemaining -= _target.write(dataArray, 0, bytesRead);
		    if (getFormat().getEncoding() == 
			Encoding.PCM_SIGNED_BIG_ENDIAN)
			_soundBuffer.write(dataArray, 0 , bytesRead);
		    else _soundBuffer.write(_reverse(dataArray, getFormat()),
					    0, bytesRead);
		}
	    } catch (IOException ioe) {
		throw new RuntimeException (ioe.getMessage());
	    }
	}
	_stopped = true;
    }
  
    /** Return a String representation of the type of audio data stored
     *  in this class.
     *  @return a String representation of the audio data.
     */
    public String toString() {
	return "Ptolemy Audio\n"
	    + "-------------------\n"
	    + "size = " + getStream().getLength() + " bytes\n"
	    + "format = " + getFormat() + "\n";
    }

    /** Convert the audio data to the given audio format. Currently the
     *  only conversions that are available are from mu-law or a-law
     *  encoded data to linear encoded data that is either MSB or LSB.
     *  Note : should this method be made private?
     *  @return true if the conversion was successful.
     */
    public boolean convert(AudioFormat af) {
	AudioStream asold = _stream;
	_stream = AudioSystem.getAudioStream(af, asold);
	if (_stream == null) {
	    _stream = asold;
	    return false;
	}
	_byteBuffer = null;
	_doubleBuffer = null;
	return true;
    }
       
    /** Convert the audio data to the given audio encoding. Currently the
     *  only conversions that are available are from mu-law or a-law
     *  encoded data to linear encoded data that is either MSB or LSB.
     *  If converting form mu-law or a-law to a linear format, the size
     *  of a sample will be doubled, but everything else about the 
     *  current AudioFormat (i.e sample rate, number of channels) will
     *  remain the same.
     *  Note : should this method be made private?
     *  @return true if the conversion was successful.
     */
    public boolean convert(Encoding ae) {
	AudioFormat af = getFormat();
	if (af.getEncoding() == ae)
	    return true;
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
	return convert(af);
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
	_stream = as;
	boolean converted = convert(Encoding.PCM_SIGNED_BIG_ENDIAN);
	if (!converted)
	    converted = convert(Encoding.PCM_SIGNED_LITTLE_ENDIAN);
	if (!converted) 
	    throw new IOException
		("Could not convert audio stream to linear format");        
	_byteBuffer = null;
	_doubleBuffer = null;
    }

    /** Refresh the byte array and double array buffers used by
     *  getByteArray() and getDoubleArray() respectively. This
     *  should not modify the audio stream. This method will
     *  throw an IOException if an I/O error occurs while reading
     *  from the audio stream into the byte array buffer.
     */
    private void _refreshBuffer() throws IOException {
	_byteBuffer = new byte[(int) _stream.getLength()];
	AudioFormat af = _stream.getFormat();
	_stream.read(_byteBuffer);
	ByteArrayInputStream bais = new ByteArrayInputStream(_byteBuffer);
	_stream = new AudioStream(bais, af, _byteBuffer.length);
	_byteToDouble(af);
    }

    /** Transfer the audio data stored in the byte array buffer 
     *  into the double array buffer so that every element in the 
     *  double array will be a linear encoded sample normalized 
     *  between -1.0 and 1.0 . The AudioFormat tells how many bytes 
     *  per sample there are, and whether the bytes are stored in 
     *  MSB or LSB order.
     */
    private void _byteToDouble(AudioFormat af) {
	long bits;
	int i, j, index;
	int bytesPerSample = af.getSampleSizeInBits() / 8;
	_doubleBuffer = new double[_byteBuffer.length / bytesPerSample];
	for (i = 0; i < _doubleBuffer.length ; i += 1) {
	    for (j = 0, bits = 0; j < bytesPerSample; j += 1) {
		if (af.getEncoding() == Encoding.PCM_SIGNED_LITTLE_ENDIAN)
		    index = i * bytesPerSample + j;
		else index = (i + 1) * bytesPerSample - j - 1;
		bits += (long) (_byteBuffer[index] * Math.pow(256, j));
	    }
	    _doubleBuffer[i] = ((double) bits) / 
		(0.4999 * Math.pow(256, bytesPerSample));
	}
    }
 
    /** Transfer the audio data stored in the given double array into 
     *  the given byte array. The AudioFormat tells how many bytes 
     *  per sample there should be in the byte array, and whether the 
     *  bytes should be stored in MSB or LSB order.
     */
    private static byte[] _doubleToByte(double[] d, AudioFormat af) {
	long bits;
	int bytesPerSample = af.getSampleSizeInBits() / 8;
	byte[] b = new byte[bytesPerSample * d.length];
	int i, j, index;
	for (i = 0; i < d.length; i += 1) {
	    bits = Math.round(0.4999 * d[i] * Math.pow(256, bytesPerSample));
	    for (j = 0; j < bytesPerSample; j += 1, bits /= 256) {
		if (af.getEncoding() == Encoding.PCM_SIGNED_BIG_ENDIAN)
		    index = (i + 1) * bytesPerSample - j - 1;
		else index = i * bytesPerSample + j;
		b[index] = (byte) (bits % 256);
	    }
	}
	return b;
    }

   
    /** Transfer the audio data stored in the double array buffer into 
     *  the byte array buffer. The AudioFormat tells how many bytes 
     *  per sample there should be in the byte array, and whether the 
     *  bytes should be stored in MSB or LSB order.
     */
    private void _doubleToByte(AudioFormat af) {
	_byteBuffer = _doubleToByte(_doubleBuffer, af);
    }

    /** Reverse the elements in the given byte array, such that
     *  if they were in MSB order, they should now be LSB order,
     *  and vice versa.
     */
    private static byte[] _reverse(byte[] b, AudioFormat af) {
	byte[] br = new byte[b.length];
	int bytesPerSample = af.getSampleSizeInBits() / 8;
	int rindex, index;
	for (int i = 0; i < (b.length / bytesPerSample); i += 1) 
	    for (int j = 0; j < bytesPerSample ; j += 1) {
		rindex = (i + 1) * bytesPerSample  - j - 1;
		index = i * bytesPerSample + j;
		br[rindex] = b[index];
	    }
	return br;
    }
    
  ///////////////////////////////////////////////////////////////////
  ////                         private members                   ////

    // The mixer used to obtain an output channel
    private Mixer _mixer = null;

    // The thread of exceution used for playback
    private Thread _pushThread = null;

    // The current stream attached to the output channel
    private InputStream _current = null;
    
    // Stores the part of the audio stream that has already written
    // to the output channel so that the audio data is never destroyed.
    private ByteArrayOutputStream _soundBuffer = new ByteArrayOutputStream();

    // true if the current audio stream is stopped
    private boolean _stopped = false;
    
    // true if the current audio stream is in the process of stopping
    private boolean _stopping = false;

    // true if the current audio stream is set to loop continuously
    private boolean _looping = false;

    // the output channel to which audio data is written
    private OutputChannel _target = null;
    
    // the underlying audio stream to which all conversions are made
    private AudioStream _stream = null;

    // the current byte array representation of the audio data. the
    // AudioFormat of _stream specifies exactly how the bytes in
    // this buffer represent audio data. This includes how many
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
    private static final int _OUTPUT_BUFFER_SIZE = 16384;

}

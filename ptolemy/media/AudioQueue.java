/* A special queue for use with Sound.

Copyright (c) 1998-2001 The Regents of the University of California.
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

import java.io.PushbackInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.media.sound.sampled.AudioFormat;

//////////////////////////////////////////////////////////////////////////
//// AudioQueue
/** Instances of this class are used as specialized queues for sound
 *  processing byte Sound. Audio data of the types double, 
 *  byte and longs can be stored in the 
 *  AudioQueue, but they each represent different things. A 
 *  byte is simply a byte of audio data, and is part of a 
 *  group of bytes that represent one sample. A Long 
 *  represented one linear encoded sample, and a double also 
 *  represents a linear encoded sample, but normalized between -1.0 and 
 *  1.0 . This class allows for the abstraction of audio data as 
 *  a group of linear samples.
 */
public class AudioQueue {

    /** Create a new empty AudioQueue initialized with the 
     *  given bytes per sample and  sample rate. 
     *  @param isBigEndian true if the data is to be stored in Big-Endian
     *         (most significant first) byte order, and false otherwise.
     *  @param bytesPerSample the number of bytes per sample stored in the
     *         queue
     *  @param sampleRate the sample rate (in samples per second)
     */
    public AudioQueue(boolean isBigEndian, int bytesPerSample, 
            double sampleRate) {
        _isBigEndian = isBigEndian;
	_bytesPerSample = bytesPerSample;
        _queue = new ByteQueue(_bytesPerSample * (int) sampleRate);
	_sampleScaleFactor = 0.9999 * Math.pow(2, 8 *_bytesPerSample - 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the AudioQueue is empty.
     *  @return true if the AudioQueue is empty.
     */
    synchronized public boolean isEmpty() {
        return (getByteCount() == 0);
    }

    /** Store a byte of audio data into the 
     *  AudioQueue.
     *  @param data the byte to be placed in the 
     *   AudioQueue.
     */
    synchronized public void put(byte data) {
	_queue.put(data);
    }
    
    /** Store a integer into the AudioQueue. 
     *  The intger is assumed to represent one linear encoded 
     *  sample. It is broken up into a number of bytes equal to the 
     *  number of bytes per sample specified by the AudioFormat, 
     *  and then those bytes are stored in the AudioQueue in 
     *  the order specified by the AudioFormat.
     *  @param sample the sample to be stored in the AudioQueue.
     */
    synchronized public void put(int sample) {
        byte[] b = new byte[_bytesPerSample];
	for (int i = 0; i < _bytesPerSample; i += 1, sample >>= 8)
	    b[_bytesPerSample - i - 1] = (byte) sample;
	for (int i = 0; i < _bytesPerSample; i += 1)
	    if (_isBigEndian)
	        put(b[i]);
	    else put(b[_bytesPerSample - i - 1]);
    }

    /** Store a double into the AudioQueue. The 
     *  double is assumed to be a linear encoding sample with 
     *  value ranging from -1.0 to 1.0 . The sample is then converted to a 
     *  long of the appropriate range and then stored into the 
     *  queue as a long. This conversion is lossy.
     *  @param sample the sample to be stored in the AudioQueue.
     */
    synchronized public void put(double sample) {
	put((int) Math.round(sample * _sampleScaleFactor));
    }

    /** Store an double array  into the AudioQueue. The 
     *  doubles are assumed to be a linear encoding samples with values
     *  ranging from -1.0 to 1.0 . The samples are then converted to  
     *  integers of the appropriate range and then stored into the queue 
     *  as integers. This conversion is lossy.
     *  @param samples the array of samples to be stored in the 
     *                 AudioQueue.
     */
    synchronized public void put(double[] samples) {
        for (int i = 0; i < samples.length; i += 1)
            put((int) Math.round(samples[i] * _sampleScaleFactor));
    }

    /** Return the oldest byte stored in the queue and 
     *  remove it from the queue.
     *  @return the oldest byte in the queue
     *  @throws NoSuchElementException if the queue is empty.
     */
    synchronized public byte getByte() {
        return _queue.get();
    }

    /** Return the oldest integer sample stored in the queue 
     *  and remove it from the queue. This is accomplished by retrieving 
     *  a group of bytes and combining them into a long.
     *  @return the oldest integer sample in the queue
     *  @throws NoSuchElementException if the queue is empty, or does
     *   not have enough bytes to make one sample.
     */
    synchronized public int getInt() {
        byte[] b = new byte[_bytesPerSample];
	for (int i = 0; i < _bytesPerSample; i += 1)
	    if (_isBigEndian)
	        b[i] = getByte();
	    else b[_bytesPerSample - i - 1] = getByte();
	int result = (b[0] >> 7) ;
	for (int i = 0; i < _bytesPerSample; i += 1)
	    result = (result << 8) + (b[i] & 0xff);
	return result;
    }

    /** Return the oldest double sample in the queue, and 
     *  remove it from the queue. This is accomplished by retrieving a 
     *  long sample from the queue and then normalizing it 
     *  into a double.
     *  @return the oldest double sample in the queue.
     *  @throws NoSuchElementException if the queue is empty, or 
     *   there are not enough bytes to make a sample.
     */
    synchronized public double getDouble() {
	return (((double) getInt()) / _sampleScaleFactor); 
    }
  
    /** Return an array of the oldest double samples in the queue, and 
     *  remove them from the queue. This is accomplished by retrieving a 
     *  long sample from the queue and then normalizing it 
     *  into a double.
     *  @param sampleCount the number of samples to retrieve
     *  @return the oldest double samples in the queue.
     *  @throws NoSuchElementException if there are not enough bytes 
     *   to make a single sample.
     */
    synchronized public double[] getDoubles(int sampleCount) {
        if (sampleCount > getSampleCount())
            sampleCount = getSampleCount();
        double[] d = new double[sampleCount];
        for (int i = 0; i < sampleCount; i += 1)
            d[i] = (((double) getInt())/  _sampleScaleFactor);
        return d;
    }

    /** Return the number of bytes stored in the queue.
     *  @return the number of bytes stored in the queue.
     */
    synchronized public int getByteCount() {
        return _queue.size();
    }

    /** Return the number of samples stored in the queue.
     *  @return the number of samples stored in the queue.
     */
    synchronized public int getSampleCount() {
       return getByteCount() / _bytesPerSample;
    }

    /** Empty the contents of the AudioQueue.
     */
    synchronized public void clearSamples() {
        _queue.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The number of bytes per sample.
    private int _bytesPerSample;

    // scale factor to apply to linearly encoded integer samples to convert
    // them into doubles.
    private double _sampleScaleFactor;

    // true if the bytes are store in Big-Endian (most significant
    // byte first) byte order.
    private boolean _isBigEndian;

    // the underlying queue that stores the bytes.
    private ByteQueue _queue;

}


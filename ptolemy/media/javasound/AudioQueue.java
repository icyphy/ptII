/* A special queue for use with NewAudio.

Copyright (c) 1999 The Regents of the University of California.
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

package ptolemy.media.javasound;

import java.io.PushbackInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.NoSuchElementException;
import javax.media.sound.sampled.AudioFormat;
import javax.media.sound.sampled.AudioFormat.Encoding;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// AudioQueue
/** Instances of this class are used as specialized queues for sound
 *  processing byte NewAudio. Audio data of the types <code>double</code>, 
 *  <code>byte</code> and <code>long</code>s can be stored in the 
 *  <code>AudioQueue</code>, but they each represent different things. A 
 *  <code>byte</code> is simply a byte of audio data, and is part of a 
 *  group of bytes that represent one sample. A <code>Long</code> 
 *  represented one linear encoded sample, and a <code>double</code> also 
 *  represents a linear encoded sample, but normalized between -1.0 and 
 *  1.0 . This class allows for the abstraction of audio data as 
 *  a group of linear samples.
 */
public class AudioQueue {

    /** Create a new empty <code>AudioQueue</code> initialized with the 
     *  given <code>AudioFormat</code>. The <code>AudioFormat</code> 
     *  specifies how many bytes make up one sample, and whether the 
     *  bytes are stored in Big-Endian (most significant byte first) 
     *  or Little-Endian (least significant byte first) byte order. 
     *  @param af The <code>AudioFormat</code> for this 
     *   <code>AudioQueue</code>.
     */
    public AudioQueue(AudioFormat af) {
	if ((af.getEncoding() == Encoding.PCM_SIGNED_BIG_ENDIAN) ||
	    (af.getEncoding() == Encoding.PCM_SIGNED))
	    _isBigEndian = true;
	else if (af.getEncoding() == Encoding.PCM_SIGNED_LITTLE_ENDIAN)
	    _isBigEndian = false;
	else throw new IllegalArgumentException
		 ("AudioFormat must be a linear format");
	_bytesPerSample = af.getSampleSizeInBits() / 8;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return <code>true</code> if the <code>AudioQueue</code> is empty.
     *  @return <code>true</code> if the <code>AudioQueue</code> is empty.
     */
    public boolean isEmpty() {
        return (numBytes() == 0);
    }

    /** Store a <code>byte</code> of audio data into the 
     *  <code>AudioQueue</code>.
     *  @param b the <code>byte</code> to be placed in the 
     *   <code>AudioQueue</code>.
     */
    public void put(byte b) {
	_queue.add(new Byte(b));
    }
    
    /** Store a <code>long</code> into the <code>AudioQueue</code>. 
     *  The <code>long</code> is assumed to represent one linear encoded 
     *  sample. It is broken up into a number of bytes equal to the 
     *  number of bytes per sample specified by the <code>AudioFormat</code>, 
     *  and then those bytes are stored in the <code>AudioQueue</code> in 
     *  the order specified by the <code>AudioFormat</code>.
     *  @param l the sample to be stored in the <code>AudioQueue</code>.
     */
    public void put(long l) {
        byte[] b = new byte[_bytesPerSample];
	for (int i = 0; i < _bytesPerSample; i += 1, l >>= 8)
	    b[_bytesPerSample - i - 1] = (byte) l;
	for (int i = 0; i < _bytesPerSample; i += 1)
	    if (_isBigEndian)
	        put(b[i]);
	    else put(b[_bytesPerSample - i - 1]);
    }

    /** Store a <code>double</code> into the <code>AudioQueue</code>. The 
     *  <code>double</code> is assumed to be a linear encoding sample with 
     *  value ranging from -1.0 to 1.0 . The sample is then converted to a 
     *  <code>long</code> of the appropriate range and then stored into the 
     *  queue as a <code>long</code>. This conversion is lossy.
     *  @param d the sample to be stored in the <code>AudioQueue</code>.
     */
    public void put(double d) {
	put(Math.round((0.9999 * d * Math.pow(2, 8 * _bytesPerSample - 1))));
    }

    /** Return the oldest <code>byte</code> stored in the queue and 
     *  remove it from the queue.
     *  @return the oldest <code>byte</code> in the queue
     *  @throws NoSuchElementException if the queue is empty.
     */
    public byte getByte() {
        return ((Byte) _queue.removeFirst()).byteValue();
    }

    /** Return the oldest <code>long</code> sample stored in the queue 
     *  and remove it from the queue. This is accomplished by retrieving 
     *  a group of bytes and combining them into a <code>long</code>.
     *  @return the oldest <code>long</code> sample in the queue
     *  @throws NoSuchElementException if the queue is empty, or does
     *   not have enough bytes to make one sample.
     */
    public long getLong() {
        byte[] b = new byte[_bytesPerSample];
	for (int i = 0; i < _bytesPerSample; i += 1)
	    if (_isBigEndian)
	        b[i] = getByte();
	    else b[_bytesPerSample - i - 1] = getByte();
	long result = (b[0] >> 7) ;
	for (int i = 0; i < _bytesPerSample; i += 1)
	    result = (result << 8) + (b[i] & 0xff);
	return result;
    }

    /** Return the oldest <code>double</code> sample in the queue, and 
     *  remove it from the queue. This is accomplished by retrieving a 
     *  <code>long</code> sample from the queue and then normalizing it 
     *  into a <code>double</code>.
     *  @return the oldest <code>double</code> sample in the queue.
     *  @throws NoSuchElementException if the queue is empty, or 
     *   there are not enough bytes to make a sample.
     */
    public double getDouble() {
	return (((double) getLong())/  
		(0.9999 * Math.pow(2, 8 * _bytesPerSample - 1)));
    }
  
    /** Return the number of bytes stored in the queue.
     *  @return the number of bytes stored in the queue.
     */
    public int numBytes() {
        return _queue.size();
    }

    /** Return the number of samples stored in the queue.
     *  @return the number of samples stored in the queue.
     */
    public int numSamples() {
       return numBytes() / _bytesPerSample;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The number of bytes per sample.
    private int _bytesPerSample;

    // true if the bytes are store in Big-Endian (most significant
    // byte first) byte order.
    private boolean _isBigEndian;

    // the underlying queue that stores the bytes.
    private LinkedList _queue = new LinkedList();

}


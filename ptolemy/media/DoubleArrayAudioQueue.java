/* An audio queue that is initialized with an array of doubles.

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

import javax.media.sound.sampled.AudioFormat;

//////////////////////////////////////////////////////////////////////////
//// DoubleArrayAudioQueue
/** Instances of this class represent audio queues that are initialized
 *  with a double array buffer.
 */
public class DoubleArrayAudioQueue extends AudioQueue {

    /** Create an audio queue initialized with the given double
     *  array buffer.
     *  @param d the double array buffer used to initialize the queue.
     *  @param isBigEndian true if the data is to be stored in Big-Endian
     *         (most significant first) byte order, and false otherwise.
     *  @param bytesPerSample the number of bytes per sample stored in the
     *         queue
     *  @param sampleRate the sample rate (in samples per second)
     */
    public DoubleArrayAudioQueue(double[] d, boolean isBigEndian,
            int bytesPerSample, double sampleRate) {
        super(isBigEndian, bytesPerSample, sampleRate);
	_buffer = d;
	_front = new AudioQueue(isBigEndian, bytesPerSample, sampleRate);
	_bytesPerSample = bytesPerSample;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

     /** Return the oldest byte stored in the queue.
     *  @return the oldest byte stored in the queue.
     *  @throws NoSuchElementException if the queue is empty.
     */
    synchronized public byte getByte() {
        if (!_front.isEmpty())
	    return _front.getByte();
	if (_index == _buffer.length)
	    return super.getByte();
	_front.put(_buffer[_index]);
	_index += 1;
	return _front.getByte();
    }

    /** Return the number of bytes currently stored in the queue.
     *  @return the number of bytes currently stored in the queue.
     */
    synchronized public int getByteCount() {
	return (_buffer.length - _index) * _bytesPerSample + super.getByteCount();
    }

    /** Return the number of bytes currently stored in the queue.
     *  @return the number of bytes currently stored in the queue.
     */
    synchronized public int getSampleCount() {
        return (_buffer.length - _index) + super.getSampleCount();
    }

    /** Empty the contents of the AudioQueue.
     */
    synchronized public void clearSamples() {
        super.clearSamples();
        _front.clearSamples();
        _buffer = new double[0];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////


    // the double array used to initialize the queue.
    private double[] _buffer;

    // the AudioQueue used to process sthe samples in the double array.
    private AudioQueue _front;

    // the current position with in the double array. 
    private int _index = 0;

    // the number of bytes per sample
    private int _bytesPerSample;
}

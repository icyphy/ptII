/* An audio queue that can be initialized with an array of bytes.

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
//// ByteArrayAudioQueue
/** Instances of this class represent audio queues that are initialized
 *  with a byte array buffer.
 */
public class ByteArrayAudioQueue extends AudioQueue {
  
    /** Create an audio queue initialized with the given byte
     *  array buffer.
     *  @param b the byte array buffer used to 
     *   initialize the queue.
     *  @param isBigEndian true if the data is to be stored in Big-Endian
     *         (most significant first) byte order, and false otherwise.
     *  @param bytesPerSample the number of bytes per sample stored in the
     *         queue
     *  @param sampleRate the sample rate (in samples per second)
     *  @param af the AudioFormat of this 
     *   AudioQueue.
     */
    public ByteArrayAudioQueue(byte[] data, boolean isBigEndian,
            int bytesPerSample, double sampleRate) {
        super(isBigEndian, bytesPerSample, sampleRate);
	_buffer = data;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the oldest byte stored in the queue.
     *  @return the oldest byte stored in the queue.
     *  @throws NoSuchElementException if the queue is empty.
     */
    synchronized public byte getByte() {
	if (_index == _buffer.length)
	    return super.getByte();
	_index += 1;
	return _buffer[_index - 1];
    }

    /** Return the number of bytes currently stored in the queue.
     *  @return the number of bytes currently stored in the queue.
     */
    synchronized public int getByteCount() {
        return (_buffer.length - _index + super.getByteCount());
    }

    /** Empty the contents of the AudioQueue.
     */
    synchronized public void clearSamples() {
        super.clearSamples();
        _buffer = new byte[0];
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // the byte array used to initialize the queue.
    private byte[] _buffer;

    // the current position with in the byte array.
    private int _index = 0;
}

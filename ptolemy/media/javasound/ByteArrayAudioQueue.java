/* An audio queue that can be initialized with an array of bytes.

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

import ptolemy.media.javasound.AudioQueue;

import javax.media.sound.sampled.AudioFormat;


//////////////////////////////////////////////////////////////////////////
//// ByteArrayAudioQueue
/** Instances of this class represent audio queues that are initialized
 *  with a <code>byte</code> array buffer.
 */
public class ByteArrayAudioQueue extends AudioQueue {
  
    /** Create an audio queue initialized with the given <code>byte</code>
     *  array buffer.
     *  @param b the <code>byte</code> array buffer used to 
     *   initialize the queue.
     *  @param af the <code>AudioFormat</code> of this 
     *   <code>AudioQueue</code>.
     *  @throw NullPointerException if a <code>null</code> array was passed as 
     *   the <code>byte</code> array buffer.
     */
    public ByteArrayAudioQueue(byte[] b, AudioFormat af) {
        super(af);
        if (b == null)
	    throw new NullPointerException 
	        ("passed null buffer in ByteArrayAudioQueue");
	_buffer = b;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the oldest byte stored in the queue.
     *  @return the oldest byte stored in the queue.
     *  @throws NoSuchElementException if the queue is empty.
     */
    public byte getByte() {
	if (_index == _buffer.length)
	    return super.getByte();
	_index += 1;
	return _buffer[_index - 1];
    }

    /** Return the number of bytes currently stored in the queue.
     *  @return the number of bytes currently stored in the queue.
     */
    public int numBytes() {
        return (_buffer.length - _index + super.numBytes());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // the byte array used to initialize the queue.
    private byte[] _buffer;

    // the current position with in the byte array.
    private int _index = 0;
}

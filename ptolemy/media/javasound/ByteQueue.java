/* A speed optimized queue of bytes that is implemented using a 
   growable array.

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

package ptolemy.media.javasound;

import java.util.NoSuchElementException;

//////////////////////////////////////////////////////////////////////////
//// ByteQueue
/** Instances of this class are speed optimized queue of bytes built from 
 *  growable arrays. This is used as the base queue for AudioQueue so that
 *  real time audio processing will be fast.
 */
public class ByteQueue {
    
    /** Create an empty queue with the given initial capacity.
     *  @param size the number of bytes this queue can initially hold.
     */
    public ByteQueue(int size) {
        _data = new byte[size];
    }

    /** Return true if this queue is empty.
     *  @return true if this queue is empty.
     */
    public boolean isEmpty() {
        return (_front == _back) && _isEmpty;
    }

    /** Return true if this queue is full.
     *  @return true if this queue is full.
     */
    public boolean isFull() {
        return (_front == _back) && !_isEmpty;
    }
    
    /** Remove the oldest byte from the queue and return it.
     *  @return the oldest byte in the queue.
     *  @throws NoSuchElementException if the queue is empty.
     */
    public byte get() {
        if (isEmpty()) 
            throw new NoSuchElementException();
        int index = _front;
        _front = (_front + 1) % _data.length;
        _isEmpty = (_front == _back);
        return _data[index];
    }

    /** Store the given byte in the queue. 
     *  @param b the byte to be stored in the queue.
     */
    public void put(byte b) {
        if (isFull())
            _resize();
        _data[_back] = b;
        _back = (_back + 1) % _data.length;
        _isEmpty = false;
    }

    /** Return the number of bytes in the queue
     *  @return the number of bytes in the queue.
     */
    public int size() {
        if (_back >= _front)
            return _back - _front;
        return _back - _front + _data.length;
    }

    /** Empty the contents of the queue.
     */
    public void clear() {
        _isEmpty = true;
        _front = _back = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Double the capacity of the queue.
    private void _resize() {
        System.out.println("resizing now");
        
        byte[] newdata = new byte[_data.length * 2];
        System.arraycopy(_data, _front, newdata, 
                _front, _data.length - _front);
        if (_back != 0)
            System.arraycopy(_data, 0, newdata, _data.length, _back);
        _back += _data.length;
        _data = newdata;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////\

    // the underlying array that stores the bytes.
    private byte[] _data;

    // the index of the oldest byte in the queue.
    private int _front = 0;

    // the index of the most recent byte in the queue.
    private int _back = 0;

    // true if the queue is empty. This is needed because the empty
    // condition and the full condition of the queue are identical.
    private boolean _isEmpty = true;
        
            
}

/*
@Copyright (c) 1996-2000 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
*/
package ptolemy.kvm.demo.primes;

/**
A channel is a first-in first-out (FIFO) queue.
@author Thomas M. Parks
@version $Id$
*/

public class Channel implements InputChannel, OutputChannel
{
    protected Object data[];
    protected int read, write, size;

    /** Create a new channel with fixed capacity. */
    public Channel(int capacity)
        {
            data = new Object[capacity];
            read = write = 0;
            size = 0;
        }

    /** Get an object from the channel.  Wait if the channel is empty. */
    public synchronized Object get()
        {
            while(isEmpty())
                {
                    try { wait(); }
                    catch(InterruptedException ignore) {}
                }
            if (isFull()) notify();

            Object o = data[read++];
            if (read >= data.length) read -= data.length;
            size--;
            return o;
        }

    /** Put an object in the channel.  Wait if the channel is full. */
    public void put(Object o)
        {
            synchronized(this)
                {
                    while (isFull())
                        {
                            try { wait(); }
                            catch(InterruptedException ignore) {}
                        }
                    if (isEmpty()) notify();

                    data[write++] = o;
                    if (write >= data.length) write -= data.length;
                    size++;
                }
            Thread.yield();		// Give the consumer a chance to run.
        }

    protected synchronized boolean isFull()
        {
            return (size >= data.length);
        }

    protected synchronized boolean isEmpty()
        {
            return (size <= 0);
        }
}

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
package ptolemy.kvm.primes;

/** Filter out all multiples of the specified constant.
@author		Thomas M. Parks  
@version	$Id$
@see		ptolemy.kvm.primes.Sift
*/
class Filter implements Runnable
{
    protected InputChannel input;	/** The input channel */
    protected OutputChannel output;	/** The output channel */
    protected int prime;		/** A prime number. */

    public Filter(InputChannel input, OutputChannel output, int prime)
        {
            this.input = input;
            this.output = output;
            this.prime = prime;
        }

    public void run()
        {
            while(true)
                {
                    Integer x = (Integer)input.get();
                    if (x.intValue() % prime != 0) output.put(x);
                }
        }

    protected void finalize()
        {
            synchronized(input) { input.notifyAll(); }
            synchronized(output) { output.notifyAll(); }
        }
}

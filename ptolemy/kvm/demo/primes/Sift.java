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

/** Compute prime Numbers using the Sieve of Erastosthenes.
@author		Thomas M. Parks  
@version	$Id$
@see		ptolemy.kvm.primes.Filter
*/
public class Sift implements Runnable
{
    protected InputChannel input;
    protected OutputChannel output;

    public Sift(InputChannel input, OutputChannel output)
        {
            this.input = input;
            this.output = output;
        }

    public void run()
        {
            while(true)
                {
                    Integer prime = (Integer)input.get();
                    output.put(prime);

                    /** Create a new instance of Filter to remove all
                        multiples of the prime number just received.
                    */
                    Channel out = new Channel(1);
                    //	    Thread t = new Thread(new Filter(input, out, prime.intValue()),
                    //	       "filter-" + prime.intValue());
                    Thread t = new Thread(new Filter(input, out, prime.intValue()));
                    input = out;
                    t.start();
                }
        }

    protected void finalize()
        {
            synchronized(input) { input.notifyAll(); }
            synchronized(output) { output.notifyAll(); }
        }
}

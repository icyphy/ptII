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

/** Compute prime numbers using the Sieve of Erastosthenes on
    a machine other than the PalmOS
@author		Thomas M. Parks, Christopher Hylands
@version	$Id$
@see		ptolemy.kvm.primes.Ramp
@see		ptolemy.kvm.primes.Sift
*/
public class PrimesText {
    //protected ThreadGroup group;
    protected Thread thread;

    protected final int MAX = 40;	// Maximum number of primes allowed.

    /** Run the applet as an application. */
    public static void main(String args[])
        {
            Channel c1 = new Channel(1);
            Channel c2 = new Channel(1);

            //ThreadGroup subGroup = new ThreadGroup("sub-group");
            new Thread(new Ramp(c1, 2)).start();
            new Thread(new Sift(c1, c2)).start();

            int limit = 10;

            for (int i = 0; i < limit; i++)	{
                System.out.println(c2.get().toString());
            }

            //subGroup.stop();
            synchronized(c1) { c1.notifyAll(); }
            synchronized(c2) { c2.notifyAll(); }

            Thread.yield();
            Thread.yield();
            Thread.yield();
            Thread.yield();

            System.gc();
            // System.runFinalization();
            // subGroup.join();
        }
}

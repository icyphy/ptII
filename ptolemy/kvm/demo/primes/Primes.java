/* Sieve of Eratosthenese for PalmOS

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (yourname@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.kvm.demo.primes;

import com.sun.kjava.*;


//////////////////////////////////////////////////////////////////////////
//// Primes
/**
Generate Prime Numbers on the Palm using a Spotlet
@author Christopher Hylands, Tom Parks
@version $Id$
*/
public class Primes extends Spotlet {
    public Primes() {
        g = Graphics.getGraphics();
        textBox = new ScrollTextBox("Primes, PalmOS-1",5,5,150,130);
        paint();
    }

    public static void main(String args[]) {
        primes = new Primes();
	Graphics.getGraphics().clearScreen();
	textBox.paint();
        primes.register(NO_EVENT_OPTIONS);

	Channel c1 = new Channel(1);
	Channel c2 = new Channel(1);

        // java.lang.ThreadGroups does not exist on the kvm 
	//ThreadGroup subGroup = new ThreadGroup("sub-group");
	//new Thread(subGroup, new Ramp(c1, 2), "ramp").start();
	//new Thread(subGroup, new Sift(c1, c2), "sift").start();

	new Thread(new Ramp(c1, 2)).start();
	new Thread(new Sift(c1, c2)).start();

	int limit = 10;

        String outText =  new String();

        outText = "First " + limit + " Primes\n";
	for (int i = 0; i < limit; i++)	{
	    outText += c2.get().toString() + " ";
            textBox.setText(outText);
            //outText += Integer.toString(i) + " ";
            //textBox.setText(outText);
            primes.paint();
       	}


	//subGroup.stop();
        /*
          synchronized(c1) { c1.notifyAll(); }
          synchronized(c2) { c2.notifyAll(); }

          Thread.yield();
          Thread.yield();
          Thread.yield();
          Thread.yield();
        */
	//System.gc();
	//System.runFinalization();

    }

    void paint() {
        // display the frames
        textBox.paint();
    }

    public Graphics g;
    public static Primes primes;
    public static ScrollTextBox textBox;

    //protected ThreadGroup group;
    protected Thread thread;

    protected final int MAX = 40;	// Maximum number of primes allowed.

}

/* 

Copyright (c) 2005 The Regents of the University of California.
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

*/

package ptolemy.backtrack.util.java.util.test;

import ptolemy.backtrack.util.java.util.Random;

//////////////////////////////////////////////////////////////////////////
//// RandomTest1
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RandomTest1 {
    
    private Random _random = new Random();
    
    void test() {
        long handle = _random.$GET$CHECKPOINT().createCheckpoint();
        int nIteration = 20;
        int[] buffer = new int[nIteration];
        for (int i = 0; i < nIteration; i++)
            buffer[i] = _random.nextInt();
        _random.$GET$CHECKPOINT().rollback(handle, true);
        for (int i = 0; i < nIteration; i++)
            System.out.print(_random.nextInt() - buffer[i] + " ");
        System.out.println();
    }

    /**
     *  @param args
     */
    public static void main(String[] args) {
        new RandomTest1().test();
    }

}

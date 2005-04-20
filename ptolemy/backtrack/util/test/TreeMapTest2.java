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

package ptolemy.backtrack.util.test;

import ptolemy.backtrack.util.java.util.Map;
import ptolemy.backtrack.util.java.util.TreeMap;

//////////////////////////////////////////////////////////////////////////
//// TreeMapTest1
/**
 
 
 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TreeMapTest2 {

    /**
     *  @param args
     */
    public static void main(String[] args) {
        Map map = new TreeMap();
        int iteration = 20;
        long[] handles = new long[iteration];
        for (int i = 0; i < iteration; i++)
            map.put(new Integer(i), new Integer(iteration - i));
        for (int i = 0; i < iteration; i++) {
            handles[i] = map.$GET$CHECKPOINT().createCheckpoint();
            map.remove(new Integer(i));
        }
        System.out.println(map);
        for (int i = iteration - 1; i >= 0; i--) {
            map.$GET$CHECKPOINT().rollback(handles[i], true);
            System.out.println(map);
        }
    }

}

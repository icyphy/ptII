/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2006-2014 The Regents of the University of California.
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
 */

package ptolemy.kernel.test;

import ptolemy.kernel.ComponentEntity;

/**
 Class that creates lots of ComponentEntities.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ComponentEntityTimeTest {
    public static void main(String args[]) {
        ComponentEntity entities[] = new ComponentEntity[10000];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            entities[i] = new ComponentEntity();
        }
        long stopTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / 1024;
        long freeMemory = runtime.freeMemory() / 1024;
        System.out
                .println(stopTime
                        - startTime
                        + " ms. Memory: "
                        + totalMemory
                        + " K Free: "
                        + freeMemory
                        + " K ("
                        + Math.round((double) freeMemory / (double) totalMemory
                                * 100.0) + "%)");

    }
}

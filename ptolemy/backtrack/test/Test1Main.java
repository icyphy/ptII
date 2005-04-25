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

package ptolemy.backtrack.test;

import java.util.LinkedList;
import java.util.List;

import ptolemy.backtrack.test.ptolemy.backtrack.test.test1.Test1;

//////////////////////////////////////////////////////////////////////////
//// Test1
/**


@author Thomas Feng
@version $Id$
@since Ptolemy II 5.1
@Pt.ProposedRating Red (tfeng)
@Pt.AcceptedRating Red (tfeng)
*/
public class Test1Main {

    public static void main(String[] args) {
        try {
            List objects = new LinkedList();

            Test1 t1 = new Test1();
            Test1 t2 = new Test1();

            t1.setT(new Test1());
            long handle1 = t1.$GET$CHECKPOINT().createCheckpoint();
            objects.add(t1.getT());
            System.out.print(objects.indexOf(t1.getT()) + " ");

            t1.setT(new Test1());
            long handle2 = t1.$GET$CHECKPOINT().createCheckpoint();
            objects.add(t1.getT());
            System.out.print(objects.indexOf(t1.getT()) + " ");

            t2.setT(t1);
            long handle3 = t2.$GET$CHECKPOINT().createCheckpoint();
            objects.add(t2.getT());
            System.out.print(objects.indexOf(t2.getT()) + " ");

            t2.getT().setT(null);
            long handle4 = t2.$GET$CHECKPOINT().createCheckpoint();
            objects.add(t2.getT().getT());
            System.out.print(objects.indexOf(t2.getT().getT()) + " ");

            t2.setT(null);

            t2.$GET$CHECKPOINT().rollback(handle4, true);
            System.out.print(objects.indexOf(t2.getT().getT()) + " ");

            t2.$GET$CHECKPOINT().rollback(handle3, true);
            System.out.print(objects.indexOf(t2.getT()) + " ");

            t2.$GET$CHECKPOINT().rollback(handle2, true);
            System.out.print(objects.indexOf(t1.getT()) + " ");

            t1.$GET$CHECKPOINT().rollback(handle1, true);
            System.out.print(objects.indexOf(t1.getT()) + " ");

            System.out.println();
        } catch (Throwable throwable) {
            // Catch errors and print the stack trace.
            throwable.printStackTrace();
        }
    }

}

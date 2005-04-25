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

import java.util.Iterator;

import ptolemy.backtrack.util.java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// TreeMapTest1
/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class MapTest3 {

    /**
     *  @param args
     */
    public static void main(String[] args) throws Exception {
        String className = args[0];
        Class mapClass = MapTest1.class.getClassLoader().loadClass(className);
        Map map = (Map)mapClass.newInstance();
        int iteration = 20;

        // Test clear() function.
        for (int i = 0; i < iteration; i++)
            map.put(new Integer(i), new Integer(iteration - i));
        long handle1 = map.$GET$CHECKPOINT().createCheckpoint();
        map.clear();
        map.$GET$CHECKPOINT().rollback(handle1, true);
        System.out.println(map);

        // Test keySet() and clear() of the key set.
        for (int i = 0; i < iteration; i++)
            map.put(new Integer(i), new Integer(iteration - i));
        long handle2 = map.$GET$CHECKPOINT().createCheckpoint();
        map.keySet().clear();
        map.$GET$CHECKPOINT().rollback(handle2, true);
        System.out.println(map);

        // Test entrySet() and clear() of the entry set.
        for (int i = 0; i < iteration; i++)
            map.put(new Integer(i), new Integer(iteration - i));
        long handle3 = map.$GET$CHECKPOINT().createCheckpoint();
        map.entrySet().clear();
        map.$GET$CHECKPOINT().rollback(handle2, true);
        System.out.println(map);

        // Test removal with iterator().
        for (int i = 0; i < iteration; i++)
            map.put(new Integer(i), new Integer(iteration - i));
        long handle4 = map.$GET$CHECKPOINT().createCheckpoint();
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
        map.$GET$CHECKPOINT().rollback(handle2, true);
        System.out.println(map);
    }

}

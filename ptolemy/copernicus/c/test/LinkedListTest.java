/* A simple class for testing that Linked Lists work.

Copyright (c) 2001-2003 The University of Maryland
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)
*/

import java.util.LinkedList;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// LinkedListTest
/**

A simple class for testing standard output.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0

*/

public class  LinkedListTest{

    public static void main(String args[]) {
        LinkedList list = new LinkedList();

        // Test add
        list.add("1");
        list.add("3");
        list.add(1, "2");
        list.addLast("4");
        list.addFirst("0");


        LinkedList list2 = new LinkedList();
        list.addAll(list2);
        list2.add("5");
        list2.add("6");
        list.addAll(list2);

        // Test Iterator.
        Iterator items = list.iterator();
        while (items.hasNext()) {
            System.out.println(items.next());
        }
        System.out.println();

        // Test size.
        int size = list.size();

        // Test get()
        for (int i = 0; i < size; i++) {
            System.out.println(list.get(i));
        }
        System.out.println();

        // Test removeFirst()
        for (int i = 0; i < size; i++) {
            System.out.println(list.removeFirst());
        }
    }
}

/* A simple class for testing HashSets.

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


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// HashSetTest
/**

A simple class for testing HashSets. It only prints out data that are
guaranteed to be consistent. For example, the order of elements in a HashSet
is not guaranteed to be consistent, but the size of the HashSet is.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0

*/

public class HashSetTest{

    public static void main(String[] args) {

        // Check that Object.hashCode works.
        /* Cannot be checked by a script.
           Object object= new Object();
           System.out.println(object.hashCode());
        */

        System.out.println(
                "Checking that strings have consistent HashCodes...");
        // Check that String.hashCode is consistent.
        String string1 = new String("abcdef");
        String string2 = new String("abcdef");
        System.out.println(string1.hashCode() == string2.hashCode());

        // Generate and populate a HashSet.
        System.out.println("\nGenerating and populating a HashSet ...");
        HashSet set = new HashSet();
        set.add("1");
        System.out.println(set);
        set.add("2");
        System.out.println(set);
        set.add("3");
        set.add("1"); // This is a duplicate, so no effect expected.

        // This is always the same. However, the order of the elements in
        // the set is not guaranteed.
        System.out.println(set.size());

        System.out.println(
                "\nChecking that HashSets work with LinkedLists.");
        LinkedList list = new LinkedList();
        list.add("1");
        list.add("1");
        list.add("4");
        list.add("5");
        System.out.println(list.size());

        set.addAll(list);
        System.out.println(set.size());
        // Add the list itself as an object.
        set.add(list);
        System.out.println(set.size());

        System.out.println("\nChecking HashSet.remove() ...");
        // Check removal of an element in the set.
        System.out.println(set.size());
        System.out.println(set);
        set.remove("2");
        System.out.println(set);
        System.out.println(set.size());
        set.remove("1729"); // Not present in set.
        System.out.println(set);
        System.out.println(set.size());

        // Initialize with a collection.
        System.out.println("\nChecking HashSet(Collection) ...");
        HashSet set2 = new HashSet(list);
        System.out.println(set2.size());

        // Checking HashSet.addAll(HashSet)
        System.out.println("\nChecking HashSet.addAll(HashSet) ...");
        set2.add("7");
        set2.add("8");
        set2.add("9");
        set.addAll(set2);
        System.out.println(set.size());

        // Check Iteration and removal.
        System.out.println("\nChecking HashSet.iterator() ...");
        Iterator items = set.iterator();
        int i = 1;
        while (items.hasNext()) {
            Object item = items.next();
            System.out.println(i++);
        }

    }
}

/* A simple class for testing that the String and StringBuffer classes work.

Copyright (c) 2003-2005 The University of Maryland
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

*/

//////////////////////////////////////////////////////////////////////////
//// StringTest

/**

A simple class for testing that the String and StringBuffer classes work.

@author Ankush Varma
@version $Id$
@since Ptolemy II 4.0
@Pt.ProposedRating Red (ssb)
@Pt.AcceptedRating Red (ssb)

*/
public class StringTest {
    public static void main(String[] args) {
        testStringConstructors();
        testStringMethods();
        testStringBuffer();
    }

    // Test the important constructors of java.lang.String.
    public static void testStringConstructors() {
        String a = new String();
        System.out.println(a);

        // Initialize with char Array.
        char[] charArray = {
                'a',
                'b',
                'c',
                'd',
                'e'
            };
        a = new String(charArray);
        System.out.println(a);

        a = new String(charArray, 2, 2);
        System.out.println(a);

        // Initialize with String.
        a = "123";

        String b = new String(a);
        System.out.println(b);
    }

    // Test some methods of the String class.
    public static void testStringMethods() {
        String a = "abcde";
        char[] b = new char[2];

        a.getChars(1, 3, b, 0);
        System.out.println(new String(b));
    }

    // Test the important stuff in StringBuffers.
    public static void testStringBuffer() {
        StringBuffer buffer = new StringBuffer("Et tu, brute?!");
        buffer.append("\nThen fall, Caesar!");
        System.out.println(buffer);
    }
}

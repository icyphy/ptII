/*
A class that runs multiple array tests.

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




//////////////////////////////////////////////////////////////////////////
//// Terp
/**

A class that runs multiple array tests. Runs the Array1,
Array2DInt, Array3DInt and ArrayOfObjects tests.

@author Ankush Varma
@version $Id$
@since Ptolemy II 2.0

*/

public class Arrays{

    public static void main(String args[]) {
        //1-D Array.
        {
            // Initialize array.
            int data[] = new int[10];

            // Set the data.
            for (int i = 0; i < 10; i++) {
                data[i] = i * 10;
            }

            // Print out the data.
            for (int i = 0; i < 10; i++) {
                System.out.println(data[i]);
            }
        }

        //2-D Int Array.
        {
            // Initialize array.
            int data[][] = new int[2][];
            data [0] = new int[2];
            data [1] = new int[2];

            // Set the data.
            data[0][0] = 0;
            data[0][1] = 1;
            data[1][0] = 10;
            data[1][1] = 11;

            // Print out the data.
            System.out.println(data[0][0]);
            System.out.println(data[0][1]);
            System.out.println(data[1][0]);
            System.out.println(data[1][1]);
        }

        //3-D Int Array
        {
            // Initialize array.
            int data[][][] = new int[3][3][3];

            // Set the diagonal of the data.
            for (int i = 0; i < 3; i++) {
                data[i][i][i] = i;
            }

            for (int i = 0; i < 3; i++) {
                System.out.println(data[i][i][i]);
            }
        }

        // Array of Objects
        {
            // Initialize array.
            String data[] = new String[2];

            // Set the data.
            data[0] = new String("I, said the sparrow.");
            data[1] = new String("With my bow and arrow.");

            // Print out the data.
            for (int i = 0; i < 2; i++) {
                System.out.println(data[i]);
            }
        }

        // Test Array.length
        {
            int[] a = new int[10];
            System.out.println(a.length);
        }

        // Test System.arrayCopy()
        {
            int[] source = new int[10];
            for (int i = 0; i < 10; i++) {
                source[i] = i;
            }

            int[] dest = new int[4];
            dest[0] = 8;
            System.arraycopy(source, 2, dest, 1, 2);
            dest[3] = 666;
            for (int i = 0; i < dest.length; i++) {
                System.out.println(dest[i]);
            }
        }

    }
}

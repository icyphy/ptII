
/* A simple Java class for testing compilation of multiple source
files using the C code generator.
 

Copyright (c) 2001 The University of Maryland   
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
//// TestMultiple1
/**
A simple Java class for testing compilation of multiple source
files using the C code generator.

@author Shuvra S. Bhattacharyya 
@version $Id$

*/

public class TestMultiple1 {

    // Constructor with no arguments.
    public TestMultiple1() { _privatevar1 = _privatevar2 = 0; }

    // Constructor with arguments.
    public TestMultiple1(int val1, int val2) {
       int i, sum1, sum2;
       _privatevar1 = val1;
       for (i = 1, sum1 = sum2 = 0; i <= val2; i++) {
         sum1 += i;
         sum2 += sum1;
       }
       _privatevar2 = ((sum1==0) ? 0 : (sum2 / sum1)); 
    }

    // Return the product of the two private fields.
    public int method1() {
        return _privatevar1 * _privatevar2;
    }


    // Return the difference of the two private fields raised
    // to the given argument value.
    public int method2(int power) {
        int i, product, base;
        if (power <= 0) return -1;
        else {
            base = _privatevar1 - _privatevar2;
            product = 1;
            for (i=0; i < (power-1); i++) product *= base;
            return product;
        }
    }

    // Private fields
    private int _privatevar1;
    private int _privatevar2;

}

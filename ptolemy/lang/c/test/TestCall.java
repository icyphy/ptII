
/* A simple test class that emphasizes testing the Java-to-C translation
   of method calls.

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
//// TestCall
/**
A simple class that emphasizes testing the Java-to-C translation
of method calls.

@author Shuvra S. Bhattacharyya 
@version $Id$

*/

public class TestCall {

    // Constructor with no arguments.
    public TestCall() { _privatevar1 = _privatevar2 = 0; }

    // Constructor with arguments.
    public TestCall(int val1, int val2) {
       _privatevar1 = val1;
       _privatevar2 = val2;
    }


    // Method with a single argument
    public void method1(int x) {
       _privatevar1 = _privatevar2 = x;
    }

    // Overloaded method with no arguments
    public void method1() {
        int var1 = 5;
        int var2 = 7;
        var1 = var1 + var2;
        var2 = var2 - var1;
        _privatevar1 = var1 * var2;
        _privatevar2 = var2 / var1;
    }


    // Method with arguments and two method calls.
    public int method2(int denom) {
        method1();
        if (denom <= 0) return -1;
        else {
            method1(5);
            _privatevar1 = 17 + _privatevar2;
            _privatevar2 = 22 * _privatevar1;
            return (_privatevar1 * _privatevar2) / denom;
        }
    }

    // Private fields
    private int _privatevar1;
    private int _privatevar2;

}

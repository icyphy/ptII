
/* A simple Java class for testing compilation of multiple source
files using the C code generator. For use in conjuction with
class TestMultiple1.
 

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
//// TestMultiple2
/**
A simple Java class for testing compilation of multiple source
files using the C code generator.

@author Shuvra S. Bhattacharyya 
@version $Id$

*/

public class TestMultiple2 {

    // Constructor with no arguments.
    public TestMultiple2() { 
        _c1 = 'a'; 
        _c2 = 'z'; 
        _testMultipleOneInstance = new TestMultiple1();
    }

    // Constructor with arguments.
    public TestMultiple2(char c, int val1, int val2) {
        _c1 = _c2 = c; 
        _testMultipleOneInstance = new TestMultiple1(val1, val2);
    }

    // A simple method. 
    public char method1(int threshold, int power) {
        int temp = power;
        // FIXME Method calls are not implemented yet. 
        // int temp = _testMultipleOneInstance.method2(power);
        if (temp < threshold) return _c1;
        else return _c2;
    }

    // A method that returns an object.
    public TestMultiple1 method2(int x) {
        return new TestMultiple1(x, x*x);
    }


    // Private fields
    private TestMultiple1 _testMultipleOneInstance;
    private char _c1, _c2;
}

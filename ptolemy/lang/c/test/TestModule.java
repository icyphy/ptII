/* A simple class that tests translation of Ptolemy II ASTs to
C code.

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
//// TestModule
/**
/* A simple class that tests translation of Ptolemy II abstract syntax
trees to C code.

@author Shuvra S. Bhattacharyya 
@version $Id$

*/

// A simple class that tests translation of Ptolemy II ASTs to
// C code.
public class TestModule {

    // Constructor with no arguments.
    public TestModule() { _privatevar1 = _privatevar2 = 0; }

    // Constructor with arguments.
    public TestModule(int val1, int val2) {
       _privatevar1 = val1;
       _privatevar2 = val2;
    }

    // Method with no arguments
    public void method1() {
        int var1 = 5;
        int var2 = 7;
        float y=2;
        var1 = var1 + var2;
        var2 = var2 - var1;
        _privatevar1 = var1 * var2;
        _privatevar2 = var2 / var1;
    }


    // Method with arguments
    public int method2(int denom) {
        if (denom <= 0) return -1;
        else {
            _privatevar1 = 17;
            _privatevar2 = 22;
            return (_privatevar1 * _privatevar2) / denom;
        }
    }

    // Private fields
    private int _privatevar1;
    private int _privatevar2;

}

/* A simple class that is useful for testing C code generation under
single class mode.

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
//// Simple
/**
/* A simple class that is useful for testing C code generation under
single class mode. C code generated for this class, when created
under single class mode, can be compiled independently of any other
generated code.

@author Shuvra S. Bhattacharyya
@version $Id$
@since Ptolemy II 2.0

*/

public class Simple {

    public void main()
    {
        method1();
        method2(1,2);
        staticMethod(this);
    }
    // Constructor with no arguments.
    public Simple() {
        publicField1 = publicField2 = privateField = 0;
    }

    // Constructor with arguments.
    public Simple(int value1, int value2) {
       publicField1 = value1;
       publicField2 = value2 + 5;
       privateField = value1 - value2;
    }

    // Another constructor with arguments.
    public Simple(boolean value1, boolean value2) {
        this();
        staticFlag1 = value1;
        staticFlag2 = !value2 || value1;
    }

    // Method with no arguments.
    public void method1() {
        int local1 = 5;
        int local2 = 7;
        int local3 = 2;
        int i;

        for (i = publicField1; i < local3; i++) {
            local1 = local1 + local2 + local3;
            local2 = local2 - local1;
        }
        publicField1 = local1 * local2;
        publicField2 = local2 / local1;
    }

    // Method with arguments.
    public int method2(int denom, long p) {
        int x = denom;
        if (p < 7) denom++;
        if (p >= 19) denom++;
        if (staticFlag1 || staticFlag2) denom += privateField;
        denom = (denom < 0) ? (denom++) : (denom--);
        if (denom != 6) denom -= 10;
        while (x > 10) {
            publicField1++;
            publicField2++;
            x--;
        }
        if (denom == 0) {
             denom = 1;
        } else {
             denom *= 2;
        }
        if (denom < 0) {
            publicField1 = 15;
            publicField2 = 9;
            return -1;
        } else {
            publicField1 = 17;
            publicField2 = 22;
            return (publicField1 * publicField2) / denom;
        }
    }

    // Static method.
    public static void staticMethod(Simple x) {
        int z = x.publicField1;
        do {
            x.publicField2++;
            x.publicField1--;
            z--;
        } while (z > 0);
        x.method2(5, 9);
    }

    // Public fields.
    public int publicField1;
    public int publicField2;

    // Private fields.
    private int privateField;

    // Static fields.
    public static boolean staticFlag1 = false;
    private static boolean staticFlag2 = true;
}

/* A type polymorphic FIR filter.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.copernicus.jhdl.test;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.FixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class test1 extends TypedAtomicActor {

    public test1(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(input);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    public TypedIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    public int e;

    public int f() { return 3; }
    public int g(int a) { return a+4; }

    /**
     * Summary of test methods:
     *
     * method1: Negate operation
     *
     **/

    /**
     * Integer Negate Operation (UnopExpr)
     **/
    public void method1(int b) {
        int a = -b;
    }
    /**
     * ControlFlow
     * - none
     * DataFlow
     * - UnopExpr
     **/
    public void method2(int b) {
        int a = (b + 5) * 4 + 3;
        a = -a;
    }

    /** fieldref **/
    public void method3() {
        test1 b=null;
        int a = b.e + 4;
        this.e = a;
    }

    /** invoke statement * expr **/
    public int method4(int b) {
        g(b+3);
        int a=g(b+2);
        return a;
    }

    /** If-Else Control Flow (no Boolean expressions) **/
    public void method10(int b) {
        int d=0;
        if (b < 4) {
            d = 2;
            if (b==3) {
                d += b;
            }
        } else {
            d = b;
        }
    }

    /** Boolean expression Control Flow **/
    public void method11(int b, int c) {
        int d=0;
        if ((b < 4 || b > 10) && c == 10 || c < 3) {
            d += 2;
        } else {
            if (c > 10)
                d = b+c;
            d += 4;
        }
    }

    /** Boolean expression Control Flow **/
    public void method12(int b, int c) {
        int d=0;
        boolean z = b<4 || b > 10 && c == 10;
        boolean y = b<4 && b > 10 || c == 10;
    }
    public int method13(int b) {
        if ((b>2) || b++ < 5)
            return b++;
        return 4;
    }

    /** Boolean expression Control Flow **/
    public void method14(int b, int c) {
        int d=0;
        if ((b < 4 || b > c) && c == 10) {
            d = 2;
        } else {
            d = 4;
        }
        d = 2;
        if ((b < 4 || b > 10) && c == 10 || (c < 3 && b > 5)) {
            d = 2;
        } else {
            d = 4;
        }
        d = 3;
        if ((b < 4 || b > 10) && c == 10 || (c < 3 && b > 5) ||
                (b > 4 || b < 6)) {
            d = 2;
        } else {
            d = 4;
        }
    }
    public void method15(int b, int c) {
        int d=0;
        if (b < 4 || b > 10) {
            d = 2;
        } else {
            d = 4;
        }
    }
    public void method16(int b, int c) {
        int d=0;
        if (b < 4 || b > 10) {
            d = 2;
            if (b == 9)
                d = 5;
        } else {
            d = 4;
        }
    }

    /** If-Else Control Flow (no Boolean expressions) **/
    public void method17(int b) {
        int d=0;
        if (b < 4) {
            if (b==3) {
                d += b;
            } else
                d -= b;
        } else {
            if (b > 7)
                d -= b;
            else
                d += b;
        }
    }

    /** tableswitch expression Control Flow **/
    public void method20(int b) {
        int d=0;
        switch(b) {
        case 0:
            d += 1;
            break;
        case 1:
            d += 2;
            break;
        case 2:
            d += 3;
            break;
        default:
            d += d;
            break;
        }
    }

    /** tableswitch expression Control Flow (non-breaks) **/
    public void method21(int b) {
        int d=0;
        switch(b) {
        case 0:
            d += 1;
        case 1:
            d += 2;
        case 2:
            d += 3;
        default:
            d += d;
            break;
        }
    }

    /** look-up switch expression Control Flow (breaks) **/
    public void method22(int b) {
        int d=0;
        switch(b) {
        case 2:
            d += 1;
            break;
        case 4:
            d += 2;
            break;
        case 1:
            d += 3;
            break;
        default:
            d += d;
            break;
        }
    }

    /** tableswitch expression Control Flow (non-breaks) **/
    public void method23(int b) {
        int d=0;
        switch(b) {
        case 0:
            d += 1;
        case 1:
            d += 2;
            break;
        case 2:
            d += 3;
            break;
        default:
            d += d;
            break;
        }
    }

    /** For loop **/
    public void method33(int b) {
        int d=0;
        for (int i=0;i<b;i++)
            d++;
    }

    /** Used to test serial combining (two forks) **/
    public int method34(int a) {
        int d=0;
        if (a > 5) {
            d = a * 2 + 3 + d;
            d += a;
        } else {
            d += a;
            d = d * 2 + a;
        }
        d = d * 2 + a;
        d += a + 5;
        return d;
    }
    public int method34_1(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        } else {
            d -= a;
        }
        d += 5;
        return d;
    }
    public int method34_2(int a) {
        int d=0;
        int e=1;
        if (a > 5) {
            d += a;
        } else {
            d -= a;
            e += a;
        }
        d += 5;
        return d;
    }

    /** Used to test serial combining (one fork) **/
    public int method35(int a) {
        int d=0;
        if (a > 5) {
            d = a * 2 + 3 + d;
            d += a;
        }
        d = d * 2 + a;
        d += a + 5;
        return d;
    }

    /** Serial combining (one fork) with limited dataflow **/
    public int method35_1(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        }
        d += 5;
        return d;
    }

    /** Serial combining (two forks). Value defined in
     * both branches. **/
    public int method35_2(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        } else {
            d -= a;
        }
        d += 5;
        return d;
    }

    /** Serial combining (two forks). Value defined in
     * true branch only. **/
    public int method35_3(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        } else {
            a += 3;
        }
        d += a;
        return d;
    }

    /** Serial combining (two forks). Value defined in
     * false branch only. **/
    public int method35_4(int a) {
        int d=0;
        if (a > 5) {
            a += 3;
        } else {
            d += 4;
        }
        d += a;
        return d;
    }

    /** Used to test serial combining (one fork) **/
    public int method36(int a) {
        int b=1;
        int c=2;
        int d=3;
        // b defined by both branches
        // c defined by true branch
        // d defined by false branch
        if (a > 5) {
            b += a;
            c = a * 2 + d;
        } else {
            b -= a;
            d = c * 2 + a;
        }
        return b;
    }


    /** Used to test serial combining (one fork) with field member **/
    public int method37(int a) {
        int d = 4;
        if (a > 5) {
            e = a * 2;
            e *= 4;
            e += 3;
        }
        e += d;
        return d;
    }

    public int method37a(int a) {
        int d = 4;
        if (a > 5) {
            d = a * 2;
        }
        d += a;
        return d;
    }

    /** Used to test serial combining (one fork) with field member **/
    public int method37xx(int a) {
        int d = 4;
        e += a;
        if (a > 5) {
            e = a * 2 + e;
        }
        return d;
    }

    /** e is not used before block **/
    public int method37_1(int a) {
        int d = 4;
        if (a > 5) {
            e = a * 2 + e;
        }
        return d;
    }

    /** e is not used before block **/
    public int method37_2(int a) {
        int d = 4;
        e = d;
        if (a > 5) {
            e = a * 2 + e;
        }
        return d;
    }

    /** e is not used before block **/
    public int method37_2_1(int a) {
        int d = 4;
        d = e;
        if (a > 5) {
            e = a * 2 + e;
        }
        return d;
    }

    /** e is not used before block **/
    public int method37_3(int a) {
        int d = 4;
        e = d;
        if (a > 5) {
            e = a * 2 + e;
        } else {
            e = a * d - e;
        }
        return d;
    }

    /** InstanceFieldRef **/
    public int method38(test1 t) {
        int a = t.e;
        a += t.e;
        return a;
    }

    /** InstanceFieldRef **/
    public int method38_1(test1 t) {
        int a = t.e;
        t.e = a + t.e;
        return t.e+2;
    }

    /** Use of an array **/
    public int method50(int a[]) {
        return a[0];
    }

    /****************************************************
     * DataFlow
     ****************************************************/
    public int localAdd(int a, int b) {
        return a + b;
    }

    /****************************************************
     * Control Flow
     ****************************************************/

    /** A single fork operation with limited dataflow.
     * No corresponding "else" statement.
     **/
    public int fork_1(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        }
        d += 5;
        return d;
    }

    /** If-Else fork operation with limited dataflow.
     *  The "d" value is defined in both forks.
     *  (test combining two forks).
     */
    public int method35_2a(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        } else {
            d -= a;
        }
        d += 5;
        return d;
    }

    /** If-Else fork operation with limited dataflow.
     *  The "d" value is defined in in the true branch
     * only.
     */
    public int method35_3a(int a) {
        int d=0;
        if (a > 5) {
            d += a;
        } else {
            a += 3;
        }
        d += 5;
        return d;
    }

    /** If-Else fork operation with limited dataflow.
     *  The "d" value is defined in in the false branch
     * only.
     */
    public int method35_4a(int a) {
        int d=0;
        if (a > 5) {
            a += 3;
        } else {
            d += 4;
        }
        d += 5;
        return d;
    }

    public int nested_fork(int a) {
        int d=0;
        if (a > 5) {
            d += 5;
            if (a < 10)
                d += 6;
        }
        d += 7;
        return d;
    }

    /****************************************************
     * Hardware Generation
     ****************************************************/

    /* Simple method with one basic block and one operation */
    public int hwgen1(int a, int b) {
        int c = a + b;
        return c;
    }

    // Use a constant
    public int hwgen1_1(int a) {
        int c = a + 1;
        return c;
    }

    /* Simple method with one basic block and several operations */
    public int hwgen2(int a, int b, int d) {
        int c = a * b + d - a;
        return c;
    }

    // Uses AndExpr, OrExpr, XorExpr (with Boolean type)
    public boolean hwgen3(boolean a, boolean b, boolean c) {
        return (a & b | c) ^ a;
    }
    // Uses AndExpr, OrExpr, XorExpr (with int type)
    public int hwgen3_1(int a, int b, int c) {
        return (a & b | c) ^ a;
    }

    public boolean hwgen3_1_1(boolean a, boolean b, boolean c) {
        return !((a & b | c) ^ a);
    }

    // Causes erro
    public boolean hwgen3_1_2(boolean a, boolean b, boolean c) {
        return !((a & b | !c) ^ a);
    }

    // TODO:
    public boolean hwgen3_2(boolean a, boolean b, boolean c) {
        return a && b || c;
    }

    // TODO: Causes an error in merging
    public int hwgen4(int a, int b) {
        if (a > 5)
            return a+b;
        else
            return a;
    }

    public int hwgen4_1(int a, int b) {
        int c;
        if (a > 5)
            c = a+b;
        else
            c = a+5;
        return c;
    }


    public void fire() throws IllegalActionException {
        int a,b,c,d;
        test1 t1=null;

        a=3;
        b=a*4;

        t1.e = b;

        a=2;
        c=a+5+t1.e+t1.e;
        d=b-c + f();

        if (d > 8){
            d=a*a+e;
        }
    }

}

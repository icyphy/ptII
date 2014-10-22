/* Matlab engine interface test (demo)

 Copyright (c) 1998-2014 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.
 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.matlab.test;

import ptolemy.data.ArrayToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.matlab.Engine;

///////////////////////////////////////////////////////////////////////////
//// TestEngine

/** Provides a simple demo of capabilities.<p>
 Use: "ptinvoke ptolemy.matlab.test.TestEngine"
 in this directory to execute, output goes to stdout.<p>
 TODO: automate regression test (python?, jtcl?)
 @author Zoltan Kemenczy, Research in Motion Limited
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestEngine {
    /** Invoke the Matlab engine and run a few tests.
     *  @param args Not used.
     *  @exception IllegalActionException If there is a problem running the tests.
     */
    public static void main(String[] args) throws IllegalActionException {
        Engine eng = new Engine();
        eng.setDebugging((byte) 0);

        long[] engineHandle = eng.open();
        eng.evalString(engineHandle, "clear");

        DoubleMatrixToken tx = new DoubleMatrixToken(
                new double[][] { { 1, 2, 3 } });
        System.out.println("\nNote: All data output is via "
                + "Token.toString() on tokens");
        System.out.println("that are put/get from the matlab engineHandle.");
        System.out.println("\nCreate 1x3 double matrix x:");
        eng.put(engineHandle, "x", tx);
        System.out.println("x = " + tx.toString());
        System.out.println("Eval: y = x.*x;");
        eng.evalString(engineHandle, "y = x.*x;");

        DoubleMatrixToken ty = (DoubleMatrixToken) eng.get(engineHandle, "y");
        System.out.println("y = " + ty.toString());

        System.out.println("\nCreate 2x3 double matrix x:");
        tx = new DoubleMatrixToken(new double[][] { { 1, 2, 3 }, { 4, 5, 6 } });
        eng.put(engineHandle, "x", tx);
        System.out.println("x = " + tx.toString());
        System.out.println("Eval: y = x.*x;");
        eng.evalString(engineHandle, "y = x.*x;");
        ty = (DoubleMatrixToken) eng.get(engineHandle, "y");
        System.out.println("y = " + ty.toString());

        System.out.println("\nEval: z = exp(j*pi/2*x);");
        eng.evalString(engineHandle, "z = exp(j*pi/2*x);");

        ComplexMatrixToken tz = (ComplexMatrixToken) eng.get(engineHandle, "z");
        System.out.println("z = " + tz.toString());

        System.out.println("\nEval: w = z';");
        eng.evalString(engineHandle, "w = z';");

        ComplexMatrixToken tw = (ComplexMatrixToken) eng.get(engineHandle, "w");
        System.out.println("w = " + tw.toString());

        System.out.println("\nCreate 1xn string s:");

        StringToken ts = new StringToken("a string");
        System.out.println("s = " + ts.toString());
        eng.put(engineHandle, "s", ts);
        System.out.println("\nEval: rc = [s;s];");
        eng.evalString(engineHandle, "rc = [s;s];");

        Token ta = eng.get(engineHandle, "rc");
        System.out.println("rc = " + ta.toString());

        System.out.println("\nCreate 2xn string s:");
        ta = new ArrayToken(new Token[] { new StringToken("str one"),
                new StringToken("str two") });
        System.out.println("s = " + ta.toString());
        eng.put(engineHandle, "s", ta);
        System.out.println("\nEval: rr = [s,s];");
        eng.evalString(engineHandle, "rr = [s,s];");
        ta = eng.get(engineHandle, "rr");
        System.out.println("rr = " + ta.toString());

        System.out.println("\nCreate 1x1 struct r (RecordToken):");

        RecordToken tr = new RecordToken(new String[] { "x", "r", "s" },
                new Token[] {
                        tx,
                        new RecordToken(new String[] { "a" },
                                new Token[] { new IntToken() }), ts });
        System.out.println("r = " + tr.toString());
        eng.put(engineHandle, "r", tr);

        Token t = eng.get(engineHandle, "r");
        System.out.println("\nRead back 1x1 struct r into RecordToken t:");
        System.out.println("t = " + t.toString());

        System.out.println("\nEval: ta = [r,r,r;r,r,r];");
        eng.evalString(engineHandle, "ta = [r,r,r;r,r,r];");
        t = eng.get(engineHandle, "ta");
        System.out.println("\nRead 2x3 struct ta into ArrayToken "
                + "of ArrayToken of RecordTokens:");
        System.out.println("ta = " + t.toString());

        System.out.println("\nCreate 1x3 cell array from ta, "
                + "an ArrayToken of RecordTokens:");

        RecordToken r1 = new RecordToken(new String[] { "a" },
                new Token[] { new ComplexMatrixToken(new Complex[][] { {
                        new Complex(1.0, 1.0), new Complex(2.0, 2.0) } }) });
        RecordToken r2 = new RecordToken(new String[] { "a" },
                new Token[] { new ComplexMatrixToken(new Complex[][] { {
                        new Complex(3.0, 3.0), new Complex(4.0, 4.0) } }) });
        RecordToken r3 = new RecordToken(new String[] { "a" },
                new Token[] { new ComplexMatrixToken(new Complex[][] { {
                        new Complex(5.0, 5.0), new Complex(6.0, 6.0) } }) });
        ta = new ArrayToken(new Token[] { r1, r2, r3 });
        System.out.println("ta = " + ta.toString());
        eng.put(engineHandle, "ta", ta);
        eng.evalString(engineHandle, "tb = ta;");
        ta = eng.get(engineHandle, "tb");
        System.out.println("\nRead 1x3 cell array back into tb:");
        System.out.println("tb = " + ta.toString());

        eng.close(engineHandle);
    }
}

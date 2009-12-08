/** An interface to an SMT solver.
 * 
 */

package ptolemy.apps.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import yices.YicesLite;

/** An interface to an SMT solver.
 * 
 *  Its behavior is encapsulated in a single method,
 *  <pre>check</pre>, which takes in a list of constraints
 *  and returns a satisfying assignment if one exists,
 *  and "unsat" if no such assignment exists.
 *  Currently, the argument and return value are strings in
 *  the form accepted by the Yices SMT solver.
 * 
 *  @author Ben Lickly
 *
 */
public class SMTSolver {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Check the satisfiability of the given formula.
     * 
     *  @param formula A formula to be checked.
     *  @return A satisfying assignment if it exists, "unsat" if it does not,
     *   and the empty string if no assertion can be made.
     */
    public String check(String formula) {
        final int ctx = yl.yicesl_mk_context();
        formula += "(set-evidence! true)\n(check)\n";

        final StringBuffer result = new StringBuffer();
        try {
            final File tmpfile = File.createTempFile("yicesout", "ycs");
            yl.yicesl_set_output_file(tmpfile.getAbsolutePath());

            yl.yicesl_read(ctx, formula);
            yl.yicesl_del_context(ctx);

            final BufferedReader resultBuf = new BufferedReader(new FileReader(
                    tmpfile));
            while (resultBuf.ready()) {
                result.append(resultBuf.readLine());
            }
            resultBuf.close();
            if (!tmpfile.delete()) {
                System.err.println("Error deleting temporary file:");
            }
        } catch (final IOException e) {
            System.err.println("Error accessing temporary file:");
            e.printStackTrace();
        }
        return result.toString();
    }

    /** Test that the SMT Solver works correctly.
     * 
     *  @param args Ignored.
     *  @throws AssertionError If the test fails.
     */
    public static void main(String[] args) {
        final SMTSolver ytm = new SMTSolver();
        final String result = ytm
                .check("(define x::int)" + "\n(assert (= x 9))"
                        + "\n(set-evidence! true)" + "\n(check)");
        System.out.println("Result: " + result);
        assert result == "sat(= x 9)";
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////
    /** The interface to Yices SMT solver using the Yices Java API Lite.
     */
    private final YicesLite yl = new YicesLite();

}

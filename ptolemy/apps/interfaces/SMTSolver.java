/** An interface to an SMT solver.
 *
 */

package ptolemy.apps.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

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
    ////                         public methods                    ////

    /** Check the satisfiability of the given formula.
     *
     *  @param formula A formula to be checked.
     *  @return A satisfying assignment if it exists, "unsat" if it does not,
     *   and the empty string if no assertion can be made.
     */
    public String check(String formula) {
        formula += "(set-evidence! true)\n(check)\n";

        final StringBuffer result = new StringBuffer();
        try {
            final File tmpfile = File.createTempFile("yicesout", "ycs");

            runYices(formula, tmpfile);

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
     *  @exception AssertionError If the test fails.
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
    ////                         private methods                   ////
    /** Invoke the Yices SMT solver on the given formula, saving the
     *  results in the given file.
     *  This method attempts to interface with the solver using the
     *  Yices Java API Lite.  We use reflection here to ensure the code
     *  will compile even without Yices installed. If we encounter a
     *  problem here, we assume it is because Yices is not installed
     *  and notify the user accordingly.
     *
     *  @param formula The formula to be checked for satisfiability.
     *  @param tmpfile The file to save the results in.
     */
    private void runYices(final String formula, final File tmpfile) {
        final String fileName = tmpfile.getAbsolutePath();

        try {
            // The code in this try block is a reflected version of
            // fairly simple code for interfacing with Yices.
            // Original unreflected import at top of file:
            //    import yices.YicesLite;
            //
            // Original unreflected interfacing code:
            //    YicesLite yicesLite = new YicesLite();
            //    final int ctx = yicesLite.yicesl_mk_context();
            //    yicesLite.yicesl_set_output_file(fileName);
            //    yicesLite.yicesl_read(ctx, formula);
            //    yicesLite.yicesl_del_context(ctx);

            // Reflect an instance of the Yices Java API
            ClassLoader myClassLoader = ClassLoader.getSystemClassLoader();
            Class<?> yicesClass = myClassLoader.loadClass("yices.YicesLite");
            Object yicesLite = yicesClass.newInstance();

            // Reflect required methods
            Method makeContext = yicesClass.getMethod("yicesl_mk_context",
                    new Class[] {});
            Method setOutputFile = yicesClass.getMethod("yicesl_set_output_file",
                    new Class[] { String.class });
            Method readFormula = yicesClass.getMethod("yicesl_read",
                    new Class[] { int.class, String.class });
            Method deleteContext = yicesClass.getMethod("yicesl_del_context",
                    new Class[] { int.class });

            // Actually create and invoke solver
            final int ctx = (Integer) makeContext.invoke(yicesLite, new Object[] {});
            setOutputFile.invoke(yicesLite, new Object[] { fileName });
            readFormula.invoke(yicesLite, new Object[] { ctx, formula });
            deleteContext.invoke(yicesLite, new Object[] { ctx });

        } catch (Exception e) {
            System.err.println(
                    "Could not interface with the Yices SMT solver.\n"
                  + "Please make sure that Yices and the Yices Java API Lite are both installed"
                  + " and the yiceslite jar is in your LD_LIBRARY_PATH.\n"
                  + "For more information, please see "
                  + "http://atlantis.seidenberg.pace.edu/wiki/lep/Yices%20Java%20API%20Lite");
            e.printStackTrace();
        }
    }

}

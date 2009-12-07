package ptolemy.data.smtsolver;
/**
 * 
 */


import yices.YicesLite;

import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;

/** This class provides an interface to an SMT solver.
 * Its behavior is encapsulated in a single method,
 * <pre>check</pre>, which takes in a list of constraints
 * and returns a satisfying assignment if one exists,
 * and "unsat" if no such assignment exists.
 * Currently, the argument and return value are strings in
 * the form accepted by the Yices SMT solver.
 * @author blickly
 *
 */
public class SMTSolver {
    
    YicesLite yl = new YicesLite();
    
    /**
     * Check the satisfiability of the given formula.
     * 
     * @param formula A formula to be checked.
     * @throws IOException If a temporary file cannot be created
     * @return A satisfying assignment if it exists, otherwise "unsat"
     */
    public String check(String formula) throws IOException {
        int ctx = yl.yicesl_mk_context();
        
        File tmpfile = File.createTempFile("yicesout", "ycs");
        yl.yicesl_set_output_file(tmpfile.getAbsolutePath());
        
        yl.yicesl_read(ctx, formula);
        yl.yicesl_del_context(ctx);

        BufferedReader resultBuf = new BufferedReader(new FileReader(tmpfile));
        String result = "";
        while (resultBuf.ready()) {
            result += resultBuf.readLine();
        }
        tmpfile.delete();
        return result;   
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        SMTSolver ytm = new SMTSolver();
        String result = "";
        try {
            result = ytm.check("(define x::int)"
                    + "\n(assert (= x 9))"
                    + "\n(set-evidence! true)"
                    + "\n(check)");
        } catch (IOException ex) {
            System.out.println("Oh noes! No file for yices to use :-(");
        }
        System.out.println("Result: " + result);
        assert(result == "sat(= x 9)");
    }

}

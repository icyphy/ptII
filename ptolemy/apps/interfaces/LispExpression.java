/** Helper functions for building Lisp-style S-expressions.
 *
 */
package ptolemy.apps.interfaces;

import java.util.Collection;

/** Helper functions for building Lisp-style S-expressions.
 *  @author Ben Lickly
 *
 */
public class LispExpression {

    /** Create a new S-expression from a function name and a collection
     *  of subexpressions.
     *  @param functionName The name of the new root function.
     *  @param subexpressions The arguments of the function.
     *  @return The new S-expression.
     */
    public static String node(final String functionName,
            final Collection<String> subexpressions) {
        final StringBuffer result = new StringBuffer("(" + functionName);
        for (final String e : subexpressions) {
            result.append(" " + e);
        }
        result.append(')');
        return result.toString();
    }

    /** Return an S-expression that is a conjunction of other S-expressions.
     *  @param arguments The expressions to take the conjunction of.
     *  @return The conjunction.
     */
    public static String conjunction(final Collection<String> arguments) {
        arguments.remove("true");
        if (arguments.isEmpty()) {
            return "true";
        } else if (arguments.size() == 1) {
            return arguments.iterator().next();
        } else {
            return node("and", arguments);
        }
    }

}

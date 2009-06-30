/* A wrapper for a string containing a Ptolemy expression.

@Copyright (c) 2007-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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



 */

package ptolemy.actor.gt.util;

import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.PtParser;
import ptolemy.domains.ptera.kernel.VariableScope;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// PtolemyExpressionString

/**
 A wrapper for a string containing a Ptolemy expression.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PtolemyExpressionString {

    /** Construct a Ptolemy expression string.
     */
    public PtolemyExpressionString() {
        this(null, "");
    }

    /** Construct a Ptolemy expression string with the given container as its
     *  scope.
     *
     *  @param container The container.
     */
    public PtolemyExpressionString(NamedObj container) {
        this(container, "");
    }

    /** Construct a Ptolemy expression string with the given container as its
     *  scope and the given value as its initial value.
     *
     *  @param container The container.
     *  @param value The initial value.
     */
    public PtolemyExpressionString(NamedObj container, String value) {
        _variableScope = container == null ? null
                : new VariableScope(container);
        set(value);
    }

    /** Get the current value.
     *
     *  @return The value.
     *  @see #set(String)
     */
    public String get() {
        return _value;
    }

    /** Evaluate the Ptolemy expression and return the result in a token.
     *
     *  @return The result.
     *  @exception IllegalActionException If error occurs in the evaluation.
     */
    public Token getToken() throws IllegalActionException {
        if (_needReparse) {
            ASTPtRootNode tree = new PtParser().generateParseTree(_value);
            _token = new ParseTreeEvaluator().evaluateParseTree(tree,
                    _variableScope);
            _needReparse = false;
        }
        return _token;
    }

    /** Set the value.
     *
     *  @param value The value.
     *  @see #get()
     */
    public void set(String value) {
        _value = value;
        _needReparse = true;
    }

    /** Return the Ptolemy expression in a string.
     *
     *  @return The Ptolemy expression.
     */
    public String toString() {
        return get();
    }

    /** Whether the Ptolemy expression needs to be reparsed.
     */
    private boolean _needReparse;

    /** The last evaluation result.
     */
    private Token _token;

    /** The Ptolemy expression.
     */
    private String _value;

    /** The scope in which the Ptolemy expression is evaluated.
     */
    private VariableScope _variableScope;

}

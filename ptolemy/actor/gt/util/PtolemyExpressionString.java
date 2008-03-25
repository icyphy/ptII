/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PtolemyExpressionString {

    public PtolemyExpressionString() {
        this(null, "");
    }

    public PtolemyExpressionString(NamedObj container) {
        this(container, "");
    }

    public PtolemyExpressionString(NamedObj container, String value) {
        _variableScope = container == null ? null
                : new VariableScope(container);
        set(value);
    }

    public String get() {
        return _value;
    }

    public Token getToken() throws IllegalActionException {
        if (_needReparse) {
            ASTPtRootNode tree = _TYPE_PARSER.generateParseTree(_value);
            _token = _TYPE_EVALUATOR.evaluateParseTree(tree, _variableScope);
            _needReparse = false;
        }
        return _token;
    }

    public void set(String value) {
        _value = value;
        _needReparse = true;
    }

    public String toString() {
        return get();
    }

    private boolean _needReparse;

    private Token _token;

    private static final ParseTreeEvaluator _TYPE_EVALUATOR = new ParseTreeEvaluator();

    private static final PtParser _TYPE_PARSER = new PtParser();

    private String _value;

    private VariableScope _variableScope;

}

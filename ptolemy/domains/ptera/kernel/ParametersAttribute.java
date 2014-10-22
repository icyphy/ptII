/* Attributes for the parameters of Ptera events.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptera.kernel;

import java.util.List;

import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 This attribute keeps the parameters for an Ptera event. These parameters are
 syntactically defined as pairs of names and types separated by colons. An
 example of this syntax is as follows:
 <pre>(a:int, b:{boolean, string}, c:{x=double, y=object("ptolemy.actor.Actor")})</pre>

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ParametersAttribute extends StringParameter {

    /** Construct a attribute for a list of typed parameters with the given name
     *  contained by the specified container. The container argument must not be
     *  null, or a NullPointerException will be thrown.  This attribute will use
     *  the workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ParametersAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Clone the attribute into the specified workspace. The new attribute is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an attribute with no container.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ParametersAttribute attribute = (ParametersAttribute) super
                .clone(workspace);
        attribute._parseTree = null;
        attribute._parseTreeVersion = -1;
        return attribute;
    }

    /** Return a list of names (in the String type) of the parameters defined in
     *  this attribute.
     *
     *  @return A list of names.
     *  @exception IllegalActionException If the list of parameters cannot be
     *   parsed.
     *  @see #getParameterTypes()
     */
    public List<String> getParameterNames() throws IllegalActionException {
        _parse();
        return _parseTree.getArgumentNameList();
    }

    /** Return an array of parameter types.
     *
     *  @return An array of parameter types.
     *  @exception IllegalActionException If the list of parameters cannot be
     *   parsed.
     *  @see #getParameterNames()
     */
    public Type[] getParameterTypes() throws IllegalActionException {
        _parse();
        return _parseTree.getArgumentTypes();
    }

    /** Evaluate the current expression to a token. The expression of this
     *  parameter must be a parentheses enclosed string with comma-separated
     *  pairs of parameter names and types. Each name and type must be separated
     *  by a colon. Examples of an acceptable expressions are:
     *  <pre>(a)</pre>
     *  and
     *  <pre>(a:int, b:double)</pre>
     *
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if a dependency loop is found.
     */
    @Override
    protected void _evaluate() throws IllegalActionException {
        super._evaluate();
        _parse();
    }

    /** Parse the expression of this attribute to generate a parse tree to be
     *  recorded locally.
     *  <p>
     *  To parse the expression, "function" is first added to the head and " 1"
     *  is appended to the end so that the new string becomes a function
     *  definition, and the parameters to be defined are the parameters of that
     *  function.
     *
     *  @exception IllegalActionException If the format of the expression is
     *   invalid, or some parameter names are invalid, or some of the types
     *   cannot be evaluated.
     */
    private void _parse() throws IllegalActionException {
        if (_parseTree == null || _parseTreeVersion != _workspace.getVersion()) {
            try {
                String function = "function" + getExpression() + " 1";
                _parseTree = (ASTPtFunctionDefinitionNode) new PtParser()
                        .generateParseTree(function);
                _parseTreeVersion = _workspace.getVersion();
            } catch (Exception e) {
                throw new IllegalActionException(this, e, "The parameter "
                        + "list must be in the form of (v1 : type1, v2 : "
                        + "type2, ...).");
            }
        }
    }

    /** The parse tree of the expression with "function" added to the head and
        " 1" appended to the end. */
    private ASTPtFunctionDefinitionNode _parseTree;

    /** Version of _parseTree. */
    private long _parseTreeVersion = -1;
}

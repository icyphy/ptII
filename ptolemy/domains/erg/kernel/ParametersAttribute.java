/*

 Copyright (c) 1997-2008 The Regents of the University of California.
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

 */

package ptolemy.domains.erg.kernel;

import java.util.List;

import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ParametersAttribute extends StringAttribute {

    /**
     *
     */
    public ParametersAttribute() {
    }

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public ParametersAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * @param workspace
     */
    public ParametersAttribute(Workspace workspace) {
        super(workspace);
    }

    public List<?> getArgumentNameList() {
        return _argumentNameList;
    }

    public Type[] getArgumentTypes() {
        return _argumentTypes;
    }

    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        expression = "function" + expression + " 1";

        try {
            ASTPtFunctionDefinitionNode parseTree =
                (ASTPtFunctionDefinitionNode) _parser.generateParseTree(
                        expression);
            _argumentNameList = parseTree.getArgumentNameList();
            _argumentTypes = parseTree.getArgumentTypes();
        } catch (Exception e) {
            throw new IllegalActionException("The argument list must be in the "
                    + "form of (v1 : type1, v2 : type2, ...).");
        }
    }

    protected PtParser _parser = new PtParser();

    /** The scope. */
    protected ParserScope _scope;

    private List<?> _argumentNameList;

    private Type[] _argumentTypes;
}

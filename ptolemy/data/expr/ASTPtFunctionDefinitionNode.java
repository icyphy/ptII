/* ASTPtFunctionDefinitionNode represent function definitions in the
parse tree.

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created: September 2002

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.data.type.*;
import java.util.List;
import java.util.ArrayList;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionDefinitionNode
/**
The parse tree created from function definitions of the form:
<pre>
    function (x) x + 5
</pre>
which defines a function of one argument.

FIXME: check argument name duplication

@author Xiaojun Liu, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtFunctionDefinitionNode extends ASTPtRootNode {

    public ASTPtFunctionDefinitionNode(int id) {
        super(id);
    }

    public ASTPtFunctionDefinitionNode(PtParser p, int id) {
        super(p, id);
    }

    /** Clone the parse tree node by invoking the clone() method of
     *  the base class. The new node copies the list of operators (+, -)
     *  represented by this node.
     *  @return A new parse tree node.
     *  @exception CloneNotSupportedException If the superclass clone()
     *   method throws it.
     */
    public Object clone() throws CloneNotSupportedException {
        ASTPtFunctionDefinitionNode newNode =
            (ASTPtFunctionDefinitionNode)super.clone();
        newNode._argList = (ArrayList)_argList.clone();
        return newNode;
    }

    /** Return the list of argument names.
     */
    public List getArgumentNameList() {
        return _argList;
    }

    /** Return the type of the arguments, or null if type inference
     *  has not occurred yet.
     */
    public Type[] getArgumentTypes() {
        return _argTypes;
    }

    /** Return the parse tree of the expression for this function.
     */
    public ASTPtRootNode getExpressionTree() {
        // The first children are the arguments.
        return (ASTPtRootNode)jjtGetChild(jjtGetNumChildren() - 1);
    }

    /** Close this node.
     */
    public void jjtClose() {
        super.jjtClose();
        _argList.trimToSize();
    }

    /** Traverse this node with the given visitor.
     */
    public void visit(ParseTreeVisitor visitor)
            throws IllegalActionException {
        visitor.visitFunctionDefinitionNode(this);
    }

    protected ArrayList _argList = new ArrayList();
    protected Type[] _argTypes = null;
}


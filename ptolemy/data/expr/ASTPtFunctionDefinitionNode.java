/* ASTPtFunctionDefinitionNode represent function definitions in the
 parse tree.

 Copyright (c) 2002-2014 The Regents of the University of California.
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


 Created: September 2002

 */
package ptolemy.data.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionDefinitionNode

/**
 The parse tree created from function definitions of the form:
 <pre>
 function (x) x + 5
 </pre>

 which defines a function of one argument.  The above is assumed to
 have arguments declared of type general.  Monomorphic type-safe
 functions can be declared using syntax like:

 <pre>
 function (x:int) x+5
 </pre>

 This declares that the function only takes integer arguments.  The
 return type (in this case integer, since the result of adding 5 to an
 integer is an integer) is inferred automatically.

 FIXME: check argument name duplication

 @author Xiaojun Liu, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtFunctionDefinitionNode extends ASTPtRootNode {
    /** Create a function definition node with an id.
     *  @param id the id.
     */
    public ASTPtFunctionDefinitionNode(int id) {
        super(id);
    }

    /** Create a function definition node with a parser and an id.
     *  @param p The parser
     *  @param id the id
     */
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
    @Override
    public Object clone() throws CloneNotSupportedException {
        ASTPtFunctionDefinitionNode newNode = (ASTPtFunctionDefinitionNode) super
                .clone();
        newNode._argList = (ArrayList) _argList.clone();
        return newNode;
    }

    /** Return the list of argument names.
     *  @return The list of argument names.	
     */
    public List getArgumentNameList() {
        return _argList;
    }

    /** Return the type of the arguments, or null if type inference
     *  has not occurred yet.
     *  @return the type of the arguments, or null if type inference
     *  has not occurred yet.
     */
    public Type[] getArgumentTypes() {
        return _argTypes;
    }

    /** Return the parse tree of the expression for this function.
     *  @return the parse tree of the expression for this function.
     */
    public ASTPtRootNode getExpressionTree() {
        // The first children are the arguments.
        return (ASTPtRootNode) jjtGetChild(jjtGetNumChildren() - 1);
    }

    /** Return true if this node is (hierarchically) congruent to the
     *  given node, under the given renaming of bound identifiers.
     *  Derived classes should extend this method to add additional
     *  necessary congruency checks.
     *  @param node The node to compare to.
     *  @param renaming A map from String to String that gives a
     *  renaming from identifiers in this node to identifiers in the
     *  given node.
     */
    @Override
    public boolean isCongruent(ASTPtRootNode node, Map renaming) {
        if (!(node instanceof ASTPtFunctionDefinitionNode)) {
            return false;
        }

        ASTPtFunctionDefinitionNode functionNode = (ASTPtFunctionDefinitionNode) node;

        // The number of arguments must be the same.
        if (getArgumentNameList().size() != functionNode.getArgumentNameList()
                .size()) {
            return false;
        }

        Map newRenaming = new HashMap(renaming);

        Iterator argNames = functionNode.getArgumentNameList().iterator();

        for (Iterator names = getArgumentNameList().iterator(); names.hasNext();) {
            String name = (String) names.next();
            String argName = (String) argNames.next();
            newRenaming.put(name, argName);
        }

        if (!super.isCongruent(node, newRenaming)) {
            return false;
        }

        return true;
    }

    /** Close this node.
     */
    @Override
    public void jjtClose() {
        super.jjtClose();
        _argList.trimToSize();
    }

    /** Traverse this node with the given visitor.
     */
    @Override
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        visitor.visitFunctionDefinitionNode(this);
    }

    protected ArrayList _argList = new ArrayList();

    protected Type[] _argTypes = null;
}
